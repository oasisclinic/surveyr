import play.PlayJava

name := "oasis-surveyor"

version := "1.0"

scalaVersion := "2.11.2"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

libraryDependencies ++= Seq(
  javaWs,
  "uk.co.panaxiom" %% "play-jongo" % "0.7.1-jongo1.0",
  "com.wordnik" %% "swagger-play2" % "1.3.10"
)

sources in (Compile,doc) := Seq.empty

publishArtifact in (Compile, packageDoc) := false