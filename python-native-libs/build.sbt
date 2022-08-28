import Dependencies._

inThisBuild(
  List(
    organization := "dev.scalapy",
    homepage     := Some(url("https://github.com/kiendang/python-native-libs")),
    licenses     := List("BSD-3-Clause" -> url("https://opensource.org/licenses/BSD-3-Clause")),
    developers := List(
      Developer(
        "kiendang",
        "Dang Trung Kien",
        "mail@kien.ai",
        url("https://kien.ai")
      )
    )
  )
)

lazy val scala212Version = "2.12.16"
lazy val scala213Version = "2.13.8"
lazy val scala3Version = "3.1.3"
lazy val supportedScalaVersions = List(scala212Version, scala213Version, scala3Version)

ThisBuild / dynverTagPrefix := "python-native-libs-v"

lazy val pythonNativeLibs = project
  .in(file("python-native-libs"))
  .settings(
    name               := "Python Native Libs",
    crossScalaVersions := supportedScalaVersions,
    libraryDependencies ++= Seq(
      scalaCollectionCompat
    )
  )
