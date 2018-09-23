name := "item-cache"

version := "1.0.0-SNAPSHOT"
scalaVersion := "2.11.8"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

val akkaVersion = "2.4.12"
libraryDependencies ++= Seq(
  ws,
  "com.softwaremill.macwire" %% "macros" % "2.2.5",

  specs2 % Test,
  "com.typesafe.akka" %% "akka-testkit" % "2.4.12" % Test,
  "com.github.tomakehurst" % "wiremock" % "2.3.1" % Test

)
