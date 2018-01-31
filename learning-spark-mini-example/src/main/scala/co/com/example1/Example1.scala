package co.com.example1

import java.io.File

import org.apache.spark.{SparkConf, SparkContext}

object Example1 extends App {

  // Create a Scala Spark Context.
  val conf = new SparkConf().setMaster("local").setAppName("wordCount")
  val sc = new SparkContext(conf)
  // Load our input data.
  val resource = this.getClass.getClassLoader.getResource("inputfile1.dat")

  val input = sc.textFile( new File(resource.toURI).getPath)
  // Split it up into words.
  val words = input.flatMap(line => line.split(" "))
  // Transform into pairs and count.
  val counts = words.map(word => (word, 1)).reduceByKey{case (x, y) => x + y}
    //.reduceByKey{case (x, y) => x + y}
  // Save the word count back out to a text file, causing evaluation.
  counts.saveAsTextFile("outputfile1.data")

}
