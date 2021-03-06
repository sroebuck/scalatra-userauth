sbtVersion in update := "0.11.2"

resolvers ++= Seq(
  "coda" at "http://repo.codahale.com",
  "gseitz@github" at "http://gseitz.github.com/maven/",
  "lessis" at "http://repo.lessis.me"
)

addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.6")

addSbtPlugin("me.lessis" % "ls-sbt" % "0.1.2")

// addSbtPlugin("net.databinder" % "posterous-sbt" % "0.3.2")

addSbtPlugin("com.typesafe.sbt" % "sbt-scalariform" % "1.0.1")
