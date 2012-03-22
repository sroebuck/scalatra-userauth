organization := "com.proinnovate"

name := "scalatra-userauth"

version := "0.1.2-SNAPSHOT"

scalacOptions ++= Seq("-deprecation", "-unchecked", "-Xcheckinit", "-Xmigration", "-encoding", "UTF-8")

libraryDependencies ++= Seq(
  "javax.servlet" % "javax.servlet-api" % "3.0.1" % "provided",
  "com.weiglewilczek.slf4s" %% "slf4s" % "1.0.7",
  "org.scalatra" %% "scalatra" % "2.0.3"
)


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

seq(sbtrelease.Release.releaseSettings: _*)

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
