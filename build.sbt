organization := "com.proinnovate"

name := "scalatra-userauth"

version := "0.1.5-SNAPSHOT"

// set the Scala version used for the project
scalaVersion := "2.9.2"

crossScalaVersions := Seq("2.9.2", "2.10.0")

scalacOptions ++= Seq("-deprecation", "-unchecked", "-Xcheckinit", "-Xmigration", "-encoding", "UTF-8")

libraryDependencies ++= {
  val sv = "2.9.1"
  Seq(
    "javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided",
    "com.weiglewilczek.slf4s" % ("slf4s" + "_" + sv) % "1.0.7",
    "org.scalatra" % ("scalatra" + "_" + sv) % "2.0.4"
  )
}


// ls-sbt configuration
// --------------------

seq(lsSettings: _*)

(LsKeys.tags in LsKeys.lsync) := Seq("scala", "project", "template")

(externalResolvers in LsKeys.lsync) := Seq(
  "sonatype" at "http://oss.sonatype.org")

(description in LsKeys.lsync) :=
  "Scalatra UserAuth - simple authentication library"


// sbt-release configuration
// -------------------------

releaseSettings

// Maven publishing configuration
// ------------------------------

publishMavenStyle := true

publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false

pomIncludeRepository := { x => false }

pomExtra := (
  <url>http://github.com/sroebuck/scalatra-userauth</url>
  <licenses>
    <license>
      <name>MIT License</name>
      <url>http://www.opensource.org/licenses/mit-license.php</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:sroebuck/scalatra-userauth.git</url>
    <connection>scm:git:git@github.com:sroebuck/scalatra-userauth.git</connection>
  </scm>
  <developers>
    <developer>
      <id>sroebuck</id>
      <name>Stuart Roebuck</name>
      <url>https://github.com/sroebuck</url>
    </developer>
  </developers>)

// sbtscalariform
// --------------------------------------------------------------------------------------------------------------------

seq(scalariformSettings: _*)

ScalariformKeys.preferences := scalariform.formatter.preferences.FormattingPreferences().
  setPreference(scalariform.formatter.preferences.AlignParameters, true).
  setPreference(scalariform.formatter.preferences.AlignSingleLineCaseStatements, true).
  setPreference(scalariform.formatter.preferences.PreserveDanglingCloseParenthesis, true).
  setPreference(scalariform.formatter.preferences.RewriteArrowSymbols, true)

