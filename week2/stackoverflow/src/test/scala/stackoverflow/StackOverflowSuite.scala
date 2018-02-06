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
    override def langSpread = 50000
    override def kmeansKernels = 45
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

//Posting(postingType: Int, id: Int, acceptedAnswer: Option[Int], parentId: Option[QID], score: Int, tags: Option[String])
  test("groupedPostings test1 ") {
    val postings = List(
      Posting(1,1,Some(1),None,1,Some("Java")),
      Posting(1,2,Some(1),None,1,Some("Java")),
      Posting(1,3,Some(1),None,1,Some("Java")),
      Posting(2,4,Some(1),Some(1),1,Some("Java")),
      Posting(2,5,Some(1),Some(2),1,Some("Java")),
      Posting(2,6,Some(1),Some(3),1,Some("Java"))
    )
    val rdd = sc.parallelize(postings)
    assert(true)
  }



  /*
  val langs = List("Scala", "Java")
     val articles = List(
         WikipediaArticle("1","Groovy is pretty interesting, and so is Erlang"),
         WikipediaArticle("2","Scala and Java run on the JVM"),
         WikipediaArticle("3","Scala is not purely functional")
       )
     val rdd = sc.parallelize(articles)
   */


}
