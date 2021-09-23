package me.shadaj.scalapy.interpreter

import java.{util => ju}
import scala.util.Properties

import me.shadaj.scalapy.py.PythonException
import me.shadaj.scalapy.py.IndexError

object CPythonInterpreter {
  private def initialize: Unit = {
    val programName =
      Option(System.getenv("SCALAPY_PYTHON_PROGRAMNAME"))
        .orElse(Properties.propOrNone("scalapy.python.programname"))

    programName.fold(CPythonAPI.Py_Initialize())(Platform.toCWideString(_) { programName =>
      CPythonAPI.Py_SetProgramName(programName)
      CPythonAPI.Py_Initialize()
    })
  }

  initialize

  private[scalapy] val globals: Platform.Pointer = CPythonAPI.PyDict_New()
  CPythonAPI.Py_IncRef(globals)

  private val builtins = CPythonAPI.PyEval_GetBuiltins()
  Platform.Zone { implicit zone =>
    CPythonAPI.PyDict_SetItemString(globals, Platform.toCString("__builtins__"), builtins)
    throwErrorIfOccured()
  }

  private[scalapy] val falseValue = PyValue.fromNew(CPythonAPI.PyBool_FromLong(Platform.intToCLong(0)), true)
  private[scalapy] val trueValue = PyValue.fromNew(CPythonAPI.PyBool_FromLong(Platform.intToCLong(1)), true)

  private[scalapy] val noneValue: PyValue = PyValue.fromNew(CPythonAPI.Py_BuildValue(Platform.emptyCString), true)

  private val liveWrappedValues = new ju.IdentityHashMap[AnyRef, PointerBox]
  private val reverseLiveWrappedValues = new ju.HashMap[Long, AnyRef]

  val (doNotFreeMeOtherwiseJNAFuncPtrBreaks, cleanupFunctionPointer) = Platform.getFnPtr2 { (self, args) =>
    val id = CPythonAPI.PyLong_AsLongLong(CPythonAPI.PyTuple_GetItem(args, Platform.intToCSize(0)))
    val pointedTo = reverseLiveWrappedValues.remove(id)
    liveWrappedValues.remove(pointedTo)

    CPythonAPI.Py_IncRef(noneValue.underlying)
    noneValue.underlying
  }

  val emptyStrPtr = Platform.alloc(1)
  Platform.setPtrByte(emptyStrPtr, 0, 0)

  val cleanupLambdaMethodDef = Platform.alloc(Platform.ptrSize + Platform.ptrSize + 4 + Platform.ptrSize)
  Platform.setPtrLong(cleanupLambdaMethodDef, 0, Platform.pointerToLong(emptyStrPtr)) // ml_name
  Platform.setPtrLong(cleanupLambdaMethodDef, Platform.ptrSize, Platform.pointerToLong(cleanupFunctionPointer)) // ml_meth
  Platform.setPtrInt(cleanupLambdaMethodDef, Platform.ptrSize + Platform.ptrSize, 0x0001) // ml_flags (https://github.com/python/cpython/blob/master/Include/methodobject.h)
  Platform.setPtrLong(cleanupLambdaMethodDef, Platform.ptrSize + Platform.ptrSize + 4, Platform.pointerToLong(emptyStrPtr)) // ml_doc
  val pyCleanupLambda = PyValue.fromNew(CPythonAPI.PyCFunction_NewEx(cleanupLambdaMethodDef, noneValue.underlying, null), safeGlobal = true)
  throwErrorIfOccured()

  val weakRefModule = PyValue.fromNew(Platform.Zone { implicit zone =>
    CPythonAPI.PyImport_ImportModule(Platform.toCString("weakref"))
  }, safeGlobal = true)

  val typesModule = PyValue.fromNew(Platform.Zone { implicit zone =>
    CPythonAPI.PyImport_ImportModule(Platform.toCString("types"))
  }, safeGlobal = true)

  val trackerClassName = valueFromString("tracker", safeGlobal = true)
  val trackerClass = call(typesModule, "new_class", Seq(trackerClassName), Seq(), safeGlobal = true)
  try {
    throwErrorIfOccured()
  } finally {
    trackerClassName.cleanup()
  }

  // must be decrefed after being sent to Python
  def wrapIntoPyObject(value: AnyRef): PyValue = withGil {
    if (liveWrappedValues.containsKey(value)) {
      val underlying = liveWrappedValues.get(value).ptr
      CPythonAPI.Py_IncRef(underlying)
      PyValue.fromNew(underlying)
    } else {
      CPythonAPI.Py_IncRef(trackerClass.underlying)
      val trackingPtr = runCallableAndDecref(trackerClass.underlying, Seq(), Seq())

      val id = Platform.pointerToLong(trackingPtr.underlying)

      liveWrappedValues.put(value, new PointerBox(trackingPtr.underlying))
      reverseLiveWrappedValues.put(id, value)

      call(weakRefModule, "finalize", Seq(trackingPtr, pyCleanupLambda, valueFromLong(id)), Seq())
      throwErrorIfOccured()

      trackingPtr
    }
  }

  // lambda wrapper
  val (doNotFreeMeOtherwiseJNAFuncPtrBreaks2, lambdaFunctionPointer) = Platform.getFnPtr2 { (self, args) =>
    val id = Platform.pointerToLong(self)
    val pointedTo = reverseLiveWrappedValues.get(id).asInstanceOf[PyValue => PyValue]

    try {
      val res = pointedTo(PyValue.fromBorrowed(args))
      CPythonAPI.Py_IncRef(res.underlying)
      res.underlying
    } catch {
      case e: IndexError =>
        val exception = selectGlobal("IndexError")
        Platform.Zone { implicit zone =>
          CPythonAPI.PyErr_SetString(exception.underlying,
            Platform.toCString(e.message)
          )
        }
        null
      case e: Throwable =>
        val exception = selectGlobal("RuntimeError")
        Platform.Zone { implicit zone =>
          CPythonAPI.PyErr_SetString(exception.underlying,
            Platform.toCString(e.getMessage())
          )
        }
        null
    }
  }

  val lambdaMethodDef = Platform.alloc(Platform.ptrSize + Platform.ptrSize + 4 + Platform.ptrSize)
  Platform.setPtrLong(lambdaMethodDef, 0, Platform.pointerToLong(emptyStrPtr)) // ml_name
  Platform.setPtrLong(lambdaMethodDef, Platform.ptrSize, Platform.pointerToLong(lambdaFunctionPointer)) // ml_meth
  Platform.setPtrInt(lambdaMethodDef, Platform.ptrSize + Platform.ptrSize, 0x0001) // ml_flags (https://github.com/python/cpython/blob/master/Include/methodobject.h)
  Platform.setPtrLong(lambdaMethodDef, Platform.ptrSize + Platform.ptrSize + 4, Platform.pointerToLong(emptyStrPtr)) // ml_doc

  Platform.Zone { implicit zone =>
    CPythonAPI.PyRun_String(
      Platform.toCString(
        """import collections.abc
          |class SequenceProxy(collections.abc.Sequence):
          |  def __init__(self, len_fn, get_fn):
          |    self.len_fn = len_fn
          |    self.get_fn = get_fn
          |  def __len__(self):
          |    return self.len_fn()
          |  def __getitem__(self, idx):
          |    return self.get_fn(idx)""".stripMargin
      ),
      257,
      globals,
      globals
    )

    throwErrorIfOccured()
  }

  CPythonAPI.PyEval_SaveThread() // release the lock created by Py_Initialize

  @inline private[scalapy] def withGil[T](fn: => T): T = {
    val handle = CPythonAPI.PyGILState_Ensure()

    try {
      fn
    } finally {
      CPythonAPI.PyGILState_Release(handle)
    }
  }

  def eval(code: String): Unit = {
    Platform.Zone { implicit zone =>
      val Py_single_input = 256
      withGil {
        CPythonAPI.PyRun_String(Platform.toCString(code), Py_single_input, globals, globals)
        throwErrorIfOccured()
      }
    }
  }

  def execManyLines(code: String): Unit = {
    Platform.Zone { implicit zone =>
      withGil {
        CPythonAPI.PyRun_String(
          Platform.toCString(code),
          257,
          globals,
          globals
        )

        throwErrorIfOccured()
      }
    }
  }

  def set(variable: String, value: PyValue): Unit = {
    Platform.Zone { implicit zone =>
      withGil {
        CPythonAPI.Py_IncRef(value.underlying)
        CPythonAPI.PyDict_SetItemString(globals, Platform.toCString(variable), value.underlying)
        throwErrorIfOccured()
      }
    }
  }

  private var counter = 0
  def getVariableReference(value: PyValue): String = {
    val variableName = synchronized {
      val ret = "spy_o_" + counter
      counter += 1
      ret
    }

    Platform.Zone { implicit zone =>
      withGil {
        CPythonAPI.PyDict_SetItemString(globals, Platform.toCString(variableName), value.underlying)
        throwErrorIfOccured()
      }
    }

    variableName
  }

  def cleanupVariableReference(variable: String) = {
    Platform.Zone { implicit zone =>
      withGil {
        CPythonAPI.PyDict_DelItemString(globals, Platform.toCString(variable))
        throwErrorIfOccured()
      }
    }
  }

  def importModule(moduleName: String): PyValue = {
    Platform.Zone { implicit zone =>
      withGil {
        val newModule = CPythonAPI.PyImport_ImportModule(
          Platform.toCString(moduleName))
        throwErrorIfOccured()
        PyValue.fromNew(newModule)
      }
    }
  }

  def valueFromBoolean(b: Boolean): PyValue = if (b) trueValue else falseValue
  def valueFromLong(long: Long): PyValue = withGil(PyValue.fromNew(CPythonAPI.PyLong_FromLongLong(long)))
  def valueFromDouble(v: Double): PyValue = withGil(PyValue.fromNew(CPythonAPI.PyFloat_FromDouble(v)))
  def valueFromString(v: String, safeGlobal: Boolean = false): PyValue = PyValue.fromNew(toNewString(v), safeGlobal)

  // Hack to patch around Scala Native not letting us auto-box pointers
  private class PointerBox(val ptr: Platform.Pointer)

  private[scalapy] def toNewString(v: String) = withGil {
    val res = Platform.Zone { implicit zone =>
      new PointerBox(CPythonAPI.PyUnicode_FromString(
        Platform.toCString(v, java.nio.charset.Charset.forName("UTF-8"))
      ))
    }
    throwErrorIfOccured()
    res.ptr
  }

  // elemConv must produce a pointer that is owned by the converion process
  // and has no other references
  def createListCopy[T](seq: scala.collection.Seq[T], elemConv: T => Platform.Pointer): Platform.Pointer = withGil {
    val retPtr = CPythonAPI.PyList_New(seq.size)
    seq.iterator.zipWithIndex.foreach { case (v, i) =>
      val converted = elemConv(v)
      CPythonAPI.PyList_SetItem(retPtr, Platform.intToCSize(i), converted)
    }

    retPtr
  }

  val seqProxyClass = selectGlobal("SequenceProxy", safeGlobal = true)
  def createListProxy[T](seq: scala.collection.Seq[T], elemConv: T => PyValue): PyValue = {
    call(seqProxyClass, Seq(
      createLambda(_ => valueFromLong(seq.size)),
      createLambda(args => {
        val index = args(0).getLong.toInt
        if (index < seq.size) {
          elemConv(seq(index))
        } else {
          throw new IndexError(s"Scala sequence proxy index out of range: $index")
        }
      })
    ), Seq())
  }

  def createTuple(seq: Seq[PyValue], safeGlobal: Boolean = false): PyValue = {
    withGil {
      val retPtr = CPythonAPI.PyTuple_New(seq.size)
      seq.zipWithIndex.foreach { case (v, i) =>
        CPythonAPI.Py_IncRef(v.underlying) // SetItem steals reference
        CPythonAPI.PyTuple_SetItem(retPtr, Platform.intToCSize(i), v.underlying)
      }

      PyValue.fromNew(retPtr, safeGlobal)
    }
  }

  def createLambda(fn: Seq[PyValue] => PyValue): PyValue = {
    val handlerFnPtr = (args: PyValue) => fn.apply(args.getTuple)

    withGil {
      PyValue.fromNew(CPythonAPI.PyCFunction_NewEx(lambdaMethodDef, wrapIntoPyObject(handlerFnPtr).underlying, null))
    }
  }

  private def pointerPointerToString(pointer: Platform.PointerToPointer) = withGil {
    Platform.fromCString(CPythonAPI.PyUnicode_AsUTF8(
      CPythonAPI.PyObject_Str(
        Platform.dereferencePointerToPointer(pointer)
      )
    ), java.nio.charset.Charset.forName("UTF-8"))
  }

  def throwErrorIfOccured() = withGil {
    if (Platform.pointerToLong(CPythonAPI.PyErr_Occurred()) != 0) {
      Platform.Zone { implicit zone =>
        val pType = Platform.allocPointerToPointer
        val pValue = Platform.allocPointerToPointer
        val pTraceback = Platform.allocPointerToPointer

        CPythonAPI.PyErr_Fetch(pType, pValue, pTraceback)

        val pTypeStringified = pointerPointerToString(pType)

        val pValueObject = Platform.dereferencePointerToPointer(pValue)
        val pValueStringified = if (pValueObject != null) {
          " " + pointerPointerToString(pValue)
        } else ""

        throw new PythonException(pTypeStringified + pValueStringified)
      }
    }
  }

  def load(code: String): PyValue = {
    Platform.Zone { implicit zone =>
      val Py_eval_input = 258
      withGil {
        val result = CPythonAPI.PyRun_String(Platform.toCString(code), Py_eval_input, globals, globals)
        throwErrorIfOccured()

        PyValue.fromNew(result)
      }
    }
  }

  def unaryNeg(a: PyValue): PyValue = withGil {
    val ret = CPythonAPI.PyNumber_Negative(
      a.underlying
    )

    throwErrorIfOccured()

    PyValue.fromNew(ret)
  }

  def unaryPos(a: PyValue): PyValue = withGil {
    val ret = CPythonAPI.PyNumber_Positive(
      a.underlying
    )

    throwErrorIfOccured()

    PyValue.fromNew(ret)
  }

  def binaryAdd(a: PyValue, b: PyValue): PyValue = withGil {
    val ret = CPythonAPI.PyNumber_Add(
      a.underlying,
      b.underlying
    )

    throwErrorIfOccured()

    PyValue.fromNew(ret)
  }

  def binarySub(a: PyValue, b: PyValue): PyValue = withGil {
    val ret = CPythonAPI.PyNumber_Subtract(
      a.underlying,
      b.underlying
    )

    throwErrorIfOccured()

    PyValue.fromNew(ret)
  }

  def binaryMul(a: PyValue, b: PyValue): PyValue = withGil {
    val ret = CPythonAPI.PyNumber_Multiply(
      a.underlying,
      b.underlying
    )

    throwErrorIfOccured()

    PyValue.fromNew(ret)
  }

  def binaryDiv(a: PyValue, b: PyValue): PyValue = withGil {
    val ret = CPythonAPI.PyNumber_TrueDivide(
      a.underlying,
      b.underlying
    )

    throwErrorIfOccured()

    PyValue.fromNew(ret)
  }

  def binaryMod(a: PyValue, b: PyValue): PyValue = withGil {
    val ret = CPythonAPI.PyNumber_Remainder(
      a.underlying,
      b.underlying
    )

    throwErrorIfOccured()

    PyValue.fromNew(ret)
  }

  private def runCallableAndDecref(callable: Platform.Pointer, args: Seq[PyValue], kwArgs: Seq[(String, PyValue)], safeGlobal: Boolean = false): PyValue = withGil {
    val kwArgsDictionary = if (kwArgs.nonEmpty) newDictionary() else null
    Platform.Zone { implicit zone =>
      kwArgs.foreach { case (key, value) =>
        CPythonAPI.PyDict_SetItemString(kwArgsDictionary.underlying, Platform.toCString(key), value.underlying)
      }
    }

    val tupleArgs = createTuple(args, safeGlobal = true)
    val result = CPythonAPI.PyObject_Call(
      callable,
      tupleArgs.underlying,
      if (kwArgs.nonEmpty) kwArgsDictionary.underlying else null
    )

    try {
      throwErrorIfOccured()
    } finally {
      CPythonAPI.Py_DecRef(callable)
      tupleArgs.cleanup()
    }

    PyValue.fromNew(result, safeGlobal)
  }

  def callGlobal(method: String, args: Seq[PyValue], kwArgs: Seq[(String, PyValue)]): PyValue = {
    Platform.Zone { implicit zone =>
      withGil {
        val methodString = toNewString(method)
        var callable = CPythonAPI.PyDict_GetItemWithError(globals, methodString)
        if (callable == null) {
          CPythonAPI.PyErr_Clear()
          callable = CPythonAPI.PyDict_GetItemWithError(builtins, methodString)
          if (callable == null) {
            CPythonAPI.PyErr_SetString(
              selectGlobal("NameError").underlying,
              Platform.toCString(s"name '$method' is not defined")
            )
          }
        }

        CPythonAPI.Py_IncRef(callable)
        CPythonAPI.Py_DecRef(methodString)

        throwErrorIfOccured()

        runCallableAndDecref(callable, args, kwArgs)
      }
    }
  }

  def call(on: PyValue, method: String, args: Seq[PyValue], kwArgs: Seq[(String, PyValue)], safeGlobal: Boolean = false): PyValue = {
    Platform.Zone { implicit zone =>
      withGil {
        val callable = CPythonAPI.PyObject_GetAttrString(on.underlying, Platform.toCString(method))
        throwErrorIfOccured()

        runCallableAndDecref(callable, args, kwArgs, safeGlobal)
      }
    }
  }

  def call(callable: PyValue, args: Seq[PyValue], kwArgs: Seq[(String, PyValue)]): PyValue = {
    withGil {
      CPythonAPI.Py_IncRef(callable.underlying)
      runCallableAndDecref(callable.underlying, args, kwArgs)
    }
  }

  def selectGlobal(name: String, safeGlobal: Boolean = false): PyValue = {
    Platform.Zone { implicit zone =>
      val nameString = toNewString(name)

      withGil {
        var gottenValue = CPythonAPI.PyDict_GetItemWithError(globals, nameString)
        if (Platform.pointerToLong(gottenValue) == 0) {
          CPythonAPI.PyErr_Clear()
          gottenValue = CPythonAPI.PyDict_GetItemWithError(builtins, nameString)
          if (Platform.pointerToLong(gottenValue) == 0) {
            CPythonAPI.PyErr_SetString(
              selectGlobal("NameError").underlying,
              Platform.toCString(s"name '$name' is not defined")
            )
          }
        }

        CPythonAPI.Py_DecRef(nameString)

        throwErrorIfOccured()

        PyValue.fromBorrowed(gottenValue, safeGlobal)
      }
    }
  }

  def select(on: PyValue, value: String, safeGlobal: Boolean = false): PyValue = {
    val valueString = toNewString(value)

    withGil {
      val underlying = CPythonAPI.PyObject_GetAttr(
        on.underlying,
        valueString
      )

      CPythonAPI.Py_DecRef(valueString)

      throwErrorIfOccured()

      PyValue.fromNew(underlying, safeGlobal)
    }
  }

  def update(on: PyValue, value: String, newValue: PyValue): Unit = {
    val valueString = toNewString(value)

    withGil {
      CPythonAPI.PyObject_SetAttr(
        on.underlying,
        valueString,
        newValue.underlying
      )

      CPythonAPI.Py_DecRef(valueString)

      throwErrorIfOccured()
    }
  }

  def deleteAttribute(on: PyValue, attr: String): Unit = {
    Platform.Zone { implicit zone =>
      withGil {
        CPythonAPI.PyObject_SetAttrString(
          on.underlying,
          Platform.toCString(attr),
          null
        )

        throwErrorIfOccured()
      }
    }
  }

  def newDictionary(): PyValue = withGil {
    val newDictReference = CPythonAPI.PyDict_New()
    throwErrorIfOccured()
    PyValue.fromNew(newDictReference)
  }

  def selectBracket(on: PyValue, key: PyValue): PyValue = withGil {
    val ret = CPythonAPI.PyObject_GetItem(
      on.underlying,
      key.underlying
    )

    throwErrorIfOccured()

    PyValue.fromBorrowed(ret)
  }

  def updateBracket(on: PyValue, key: PyValue, newValue: PyValue): Unit = {
    withGil {
      CPythonAPI.PyObject_SetItem(
        on.underlying,
        key.underlying,
        newValue.underlying
      )

      throwErrorIfOccured()
    }
  }

  def deleteBracket(on: PyValue, key: PyValue): Unit = {
    withGil {
      CPythonAPI.PyObject_DelItem(
        on.underlying,
        key.underlying
      )

      throwErrorIfOccured()
    }
  }
}
