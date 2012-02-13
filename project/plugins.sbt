
resolvers ++= Seq(
  "coda" at "http://repo.codahale.com",
  "gseitz@github" at "http://gseitz.github.com/maven/",
  "lessis" at "http://repo.lessis.me"
)

addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.4")

addSbtPlugin("me.lessis" % "ls-sbt" % "0.1.1")

// Add this to your global ~/.sbt/plugins/build.sbt file and create user logon details etc. in a file such as
// ~/.sbt/posterous-sbt.sbt as described here: https://github.com/n8han/posterous-sbt
// addSbtPlugin("net.databinder" % "posterous-sbt" % "0.3.2")
