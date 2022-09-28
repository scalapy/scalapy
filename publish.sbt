ThisBuild / publishMavenStyle := true

ThisBuild / pomIncludeRepository := { _ => false }

ThisBuild / Test / publishArtifact := false

ThisBuild / publishTo := sonatypePublishToBundle.value

ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"

ThisBuild / pomExtra :=
  <url>https://github.com/scalapy/scalapy</url>
    <licenses>
      <license>
        <name>MIT</name>
        <url>https://opensource.org/licenses/MIT</url>
        <distribution>repo</distribution>
      </license>
    </licenses>
    <scm>
      <url>https://github.com/scalapy/scalapy.git</url>
      <connection>https://github.com/scalapy/scalapy.git</connection>
    </scm>
    <developers>
      <developer>
        <id>shadaj</id>
        <name>Shadaj Laddad</name>
        <url>http://shadaj.me</url>
      </developer>
    </developers>

Global / useGpgPinentry := true
