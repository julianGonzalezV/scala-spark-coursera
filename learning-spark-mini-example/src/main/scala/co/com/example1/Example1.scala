package co.com.example1

import org.apache.spark.{SparkConf, SparkContext}

class Example1 extends App {

  // Create a Scala Spark Context.
  val conf = new SparkConf().setAppName("wordCount")
  val sc = new SparkContext(conf)
  // Load our input data.
  val input = sc.textFile("inputfile1.md")
  // Split it up into words.
  val words = input.flatMap(line => line.split(" "))
  // Transform into pairs and count.
  val counts = words.map(word => (word, 1)).reduceByKey{case (x, y) => x + y}
  // Save the word count back out to a text file, causing evaluation.
  counts.saveAsTextFile("outputfile1.md")

}
