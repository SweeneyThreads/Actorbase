name := "Actorbase"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies +=
  "com.typesafe.akka" %% "akka-actor" % "2.4.4"
libraryDependencies +=
  "org.json" % "json" % "20140107"

libraryDependencies +=
  "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test"


libraryDependencies +=
  "org.scalamock" %% "scalamock-scalatest-support" % "3.2.2" % "test"
