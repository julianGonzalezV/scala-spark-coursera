package stackoverflow

import org.scalatest.{FunSuite, BeforeAndAfterAll}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.rdd.RDD
import java.io.File

@RunWith(classOf[JUnitRunner])
class StackOverflowSuite extends FunSuite with BeforeAndAfterAll {


  lazy val testObject = new StackOverflow {
    override val langs =
      List(
        "JavaScript", "Java", "PHP", "Python", "C#", "C++", "Ruby", "CSS",
        "Objective-C", "Perl", "Scala", "Haskell", "MATLAB", "Clojure", "Groovy")
/*
    override val langs =
      List(
        "JavaScript", "Java", "PHP")*/
    override def langSpread = 50000
    override def kmeansKernels = 15
    override def kmeansEta: Double = 20.0D
    override def kmeansMaxIterations = 120
  }

  test("testObject can be instantiated") {
    val instantiatable = try {
      testObject
      true
    } catch {
      case _: Throwable => false
    }
    assert(instantiatable, "Can't instantiate a StackOverflow object")
  }

  /*
//Posting(postingType: Int, id: Int, acceptedAnswer: Option[Int], parentId: Option[QID], score: Int, tags: Option[String])
  test("groupedPostings test1 ") {
    import StackOverflow._
    val postings = List(
      Posting(1,1,Some(1),None,1,Some("Java")),
      Posting(1,2,Some(1),None,1,Some("Java")),
      Posting(1,3,Some(1),None,1,Some("Java")),
      Posting(1,11,Some(1),None,1,Some("Scala")),
      Posting(2,4,Some(1),Some(1),1,Some("Java")),
      Posting(2,5,Some(1),Some(1),5,Some("Java")),
      Posting(2,6,Some(1),Some(1),10,Some("Java")),
      Posting(2,7,Some(1),Some(2),1,Some("Java")),
      Posting(2,8,Some(1),Some(3),1,Some("Java")),
      Posting(2,9,Some(1),None,1,Some("Java")),
      Posting(2,10,Some(1),None,1,Some("Java"))
    )

    val rdd = sc.parallelize(postings)
    val groupedPx: RDD[(QID, Iterable[(Question, Answer)])] = groupedPostings(rdd)
    groupedPx.foreach(println(_))
    assert(groupedPx.count() == 3)
  }



  test("groupedPostings test 2 ") {
    import StackOverflow._
    val postings = List(
      Posting(1,1,Some(1),None,1,Some("Java")),
      Posting(1,2,Some(1),None,1,Some("Java")),
      Posting(1,3,Some(1),None,1,Some("Java")),
      Posting(1,11,Some(1),None,1,Some("Scala")),
      Posting(2,4,Some(1),Some(1),1,Some("Java")),
      Posting(2,5,Some(1),Some(1),5,Some("Java")),
      Posting(2,6,Some(1),Some(1),10,Some("Java")),
      Posting(2,7,Some(1),Some(2),1,Some("Java")),
      Posting(2,8,Some(1),Some(3),1,Some("Java")),
      Posting(2,9,Some(1),None,1,Some("Java")),
      Posting(2,10,Some(1),None,1,Some("Java"))
    )

    val rdd = sc.parallelize(postings)
    val groupedPx: RDD[(QID, Iterable[(Question, Answer)])] = groupedPostings(rdd)
    val scoredP: RDD[(Question, HighScore)] = scoredPostings(groupedPx)
    scoredP.foreach(println(_))
    val testRdd = scoredP.filter(post => post._1.id == 1).collect()
    assert(testRdd.head._2 == 10)
  }


  test("vectorPostings ") {
    import StackOverflow._
    val postings = List(
      Posting(1,1,Some(1),None,1,Some("Java")),
      Posting(1,2,Some(1),None,5,Some("Java")),
      Posting(1,3,Some(1),None,7,Some("Java")),
      Posting(1,11,Some(1),None,1,Some("Scala")),
      Posting(2,4,Some(1),Some(1),1,Some("Java")),
      Posting(2,5,Some(1),Some(1),5,Some("Java")),
      Posting(2,6,Some(1),Some(1),10,Some("Java")),
      Posting(2,7,Some(1),Some(2),5,Some("Java")),
      Posting(2,8,Some(1),Some(3),7,Some("Java")),
      Posting(2,9,Some(1),None,1,Some("Java")),
      Posting(2,10,Some(1),None,1,Some("Java"))
    )

    val rdd = sc.parallelize(postings)
    val groupedPx: RDD[(QID, Iterable[(Question, Answer)])] = groupedPostings(rdd)
    val scoredP: RDD[(Question, HighScore)] = scoredPostings(groupedPx)
    val vectorsP = vectorPostings(scoredP)
    vectorsP.foreach(println(_))
    assert(vectorsP.filter(post => post._1 == 50000).count() ==3)
  }


  test("groupedPostings test 4 ") {
    import StackOverflow._
    val postings = List(
      Posting(1,1,Some(1),None,1,Some("Java")),
      Posting(1,2,Some(1),None,1,Some("Java")),
      Posting(1,3,Some(1),None,1,Some("Java")),
      Posting(1,11,Some(1),None,1,Some("Scala")),
      Posting(2,4,Some(1),Some(1),1,Some("Java")),
      Posting(2,5,Some(1),Some(1),5,Some("Java")),
      Posting(2,6,Some(1),Some(1),10,Some("Java")),
      Posting(2,7,Some(1),Some(2),1,Some("Java")),
      Posting(2,8,Some(1),Some(3),1,Some("Java")),
      Posting(2,9,Some(1),None,1,Some("Java")),
      Posting(2,10,Some(1),None,1,Some("Java"))
    )



    val rdd = sc.parallelize(postings)
    val groupedPx: RDD[(QID, Iterable[(Question, Answer)])] = testObject.groupedPostings(rdd)
    val scoredP: RDD[(Question, HighScore)] = testObject.scoredPostings(groupedPx)
    val vectorsP = testObject.vectorPostings(scoredP)
    vectorsP.foreach(println(_))
    val sampleV = testObject.sampleVectors(vectorsP)
    sampleV.foreach(println(_))
    assert(true)
  }*/


  test("groupedPostings test 5 ") {
    import StackOverflow._
    val postings = List(

      //Questions per language
      Posting(1,1,Some(1),None,1,Some("Java")),
      Posting(1,2,Some(1),None,1,Some("JavaScript")),
      Posting(1,3,Some(1),None,1,Some("PHP")),

      //java answers
      Posting(2,4,Some(1),Some(1),1,Some("Java")),
      Posting(2,5,Some(1),Some(1),5,Some("Java")),
      Posting(2,6,Some(1),Some(1),10,Some("Java")),

    //JavaScript answers
      Posting(2,7,Some(1),Some(2),2,Some("JavaScript")),
      Posting(2,8,Some(1),Some(2),4,Some("JavaScript")),
      Posting(2,9,Some(1),Some(2),6,Some("JavaScript")),

    //PHP answers
      Posting(2,10,Some(1),Some(3),8,Some("PHP")),
      Posting(2,11,Some(1),Some(3),10,Some("PHP")),
      Posting(2,12,Some(1),Some(3),12,Some("PHP"))

    )



    val rdd = sc.parallelize(postings)
    val groupedPx: RDD[(QID, Iterable[(Question, Answer)])] = testObject.groupedPostings(rdd)
    val scoredP: RDD[(Question, HighScore)] = testObject.scoredPostings(groupedPx)
    val vectorsP = testObject.vectorPostings(scoredP)
    val sampleV = testObject.sampleVectors(vectorsP)
    val meansP   = testObject.kmeans(sampleV, vectorsP, debug = true)
    val results = testObject.clusterResults(meansP, vectorsP)
    testObject.printResults(results)
    meansP.foreach(println(_))
    assert(true)
  }


  test("Print Results") {
    import StackOverflow._


    val vectors = sc.parallelize(List( (450000, 39),(500000, 31),(150000,1),(150000,10),(500000, 55),(150000,2) ,(150000,22)))
    val sampleV = testObject.sampleVectors(vectors)
    val means = Array((500000, 13),(150000,10))
    val prnt = testObject.clusterResults(means,vectors)
    prnt.foreach(println(_))
    assert(true)
  }


}
