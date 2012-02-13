
resolvers ++= Seq(
  "coda" at "http://repo.codahale.com",
  "gseitz@github" at "http://gseitz.github.com/maven/",
  "lessis" at "http://repo.lessis.me"
)

addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.4")

addSbtPlugin("me.lessis" % "ls-sbt" % "0.1.1")

addSbtPlugin("net.databinder" % "posterous-sbt" % "0.3.2")
