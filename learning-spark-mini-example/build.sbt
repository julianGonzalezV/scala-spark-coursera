//https://github.com/phatak-dev/spark2.0-examples/blob/master/build.sbt

name := "learning-spark-mini-example"

version := "0.1"

scalaVersion := "2.10.4"
// additional libraries
libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "1.2.0" % "provided"
)
