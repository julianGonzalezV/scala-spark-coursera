//https://github.com/phatak-dev/spark2.0-examples/blob/master/build.sbt

name := "learning-spark-mini-example"

version := "0.1"

scalaVersion := "2.11.8"
// additional libraries
libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "2.1.0",
  "org.apache.spark" %% "spark-sql" % "2.1.0"
)
