name := "Actorbase"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies +=
  "com.typesafe.akka" %% "akka-actor"  % "2.4.4"

libraryDependencies +=
  "com.typesafe.akka" %% "akka-testkit"  % "2.4.4"

libraryDependencies +=
  "org.json" % "json" % "20140107"

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-reflect" % "2.11.7",
  "org.scala-lang.modules" % "scala-xml_2.11" % "1.0.4"
)

libraryDependencies +=
  "org.scalamock" %% "scalamock-scalatest-support" % "3.2.2" % "test"

libraryDependencies +=
  "ch.qos.logback" %  "logback-classic" % "1.1.7"