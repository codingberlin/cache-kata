resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/maven-releases/"

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.5.9")

// format code IDE independent while compile
addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.6.0")