package stackoverflow

import org.apache.spark
import org.apache.spark.{RangePartitioner, SparkConf, SparkContext}
import org.apache.spark.SparkContext._
import org.apache.spark.rdd.RDD
import org.apache.spark.storage.StorageLevel

import annotation.tailrec
import scala.collection.GenTraversableOnce
import scala.reflect.ClassTag

/** A raw stackoverflow posting, either a question or an answer */
case class Posting(postingType: Int, id: Int, acceptedAnswer: Option[Int], parentId: Option[QID], score: Int, tags: Option[String]) extends Serializable


/** The main class */
object StackOverflow extends StackOverflow {
  @transient lazy val conf: SparkConf = new SparkConf().setAppName("StackOverflow").setMaster("local[2]").set("spark.executor.memory", "1g")
  @transient lazy val sc: SparkContext = new SparkContext(conf)

  /** Main function */
  def main(args: Array[String]): Unit = {

    val lines   = sc.textFile("src/main/resources/stackoverflow/stackoverflow.csv")
    val raw     = rawPostings(lines)

    val grouped = groupedPostings(raw)
    val scored  = scoredPostings(grouped)//.sample(true,0.1,0)
    val vectors = vectorPostings(scored)
    val means   = kmeans(sampleVectors(vectors), vectors, debug = true)
    val results = clusterResults(means, vectors)
    printResults(results)
  }
}


/** The parsing and kmeans methods */
class StackOverflow extends Serializable {

  /** Languages */
  val langs =
    List(
      "JavaScript", "Java", "PHP", "Python", "C#", "C++", "Ruby", "CSS",
      "Objective-C", "Perl", "Scala", "Haskell", "MATLAB", "Clojure", "Groovy")

  /** K-means parameter: How "far apart" languages should be for the kmeans algorithm? */
  def langSpread = 50000
  assert(langSpread > 0, "If langSpread is zero we can't recover the language from the input data!")

  /** K-means parameter: Number of clusters 45*/
  def kmeansKernels = 45

  /** K-means parameter: Convergence criteria */
  def kmeansEta: Double = 20.0D

  /** K-means parameter: Maximum iterations */
  def kmeansMaxIterations = 120


  //
  //
  // Parsing utilities:
  //
  //

  /** Load postings from the given file */
  def rawPostings(lines: RDD[String]): RDD[Posting] =
    lines.map(line => {
      val arr = line.split(",")
      Posting(postingType =    arr(0).toInt,
              id =             arr(1).toInt,
              acceptedAnswer = if (arr(2) == "") None else Some(arr(2).toInt),
              parentId =       if (arr(3) == "") None else Some(arr(3).toInt),
              score =          arr(4).toInt,
              tags =           if (arr.length >= 6) Some(arr(5).intern()) else None)
    })


  /** Group the questions and answers together */
  def groupedPostings(postings: RDD[Posting]): RDD[(QID, Iterable[(Question, Answer)])] = {

    val questionsRdd  = postings.filter(posting => posting.postingType == 1).map(q => (q.id, q))
    val answersRdd  = postings.filter(posting => posting.postingType == 2).map(a => (a.parentId.getOrElse(-1), a))

    questionsRdd.join(answersRdd).groupByKey()





  }


  /** Compute the maximum score for each posting */
  def scoredPostings(grouped: RDD[(QID, Iterable[(Question, Answer)])]): RDD[(Question, HighScore)] = {

    def answerHighScore(as: Iterable[Posting]): Int = as.map(_.score).max
    grouped.map(item => (item._2.head._1,answerHighScore(item._2.map(x => x._2 ))))
  }


  /** Compute the vectors for the kmeans */
  def vectorPostings(scored: RDD[(Question, HighScore)]): RDD[(LangIndex, HighScore)] = {
    /** Return optional index of first language that occurs in `tags`. */
    def firstLangInTag(tag: Option[String], ls: List[String]): Option[Int] = {
      if (tag.isEmpty) None
      else if (ls.isEmpty) None
      else if (tag.get == ls.head) Some(0) // index: 0
      else {
        val tmp = firstLangInTag(tag, ls.tail)
        tmp match {
          case None => None
          case Some(i) => Some(i + 1) // index i in ls.tail => index i+1
        }
      }
    }


    val pairIndexHs = scored.map(item =>  (firstLangInTag(item._1.tags,langs) match {
      case Some(value) => value * langSpread
      case None => -1
    }, item._2) ).filter(x   => x._1 != -1)

    pairIndexHs.cache()
  }


  /** Sample the vectors */
  def sampleVectors(vectors: RDD[(LangIndex, HighScore)]): Array[(Int, Int)] = {

    assert(kmeansKernels % langs.length == 0, "kmeansKernels should be a multiple of the number of languages studied.")
    val perLang = kmeansKernels / langs.length

    // http://en.wikipedia.org/wiki/Reservoir_sampling
    def reservoirSampling(lang: Int, iter: Iterator[Int], size: Int): Array[Int] = {
      val res = new Array[Int](size)
      val rnd = new util.Random(lang)

      for (i <- 0 until size) {
        assert(iter.hasNext, s"iterator must have at least $size elements")
        res(i) = iter.next
      }

      var i = size.toLong
      while (iter.hasNext) {
        val elt = iter.next
        val j = math.abs(rnd.nextLong) % i
        if (j < size)
          res(j.toInt) = elt
        i += 1
      }

      res
    }

    val res =
      if (langSpread < 500){
        // sample the space regardless of the language
        vectors.takeSample(false, kmeansKernels, 42)
      }
      else{
        // sample the space uniformly from each language partition
        vectors.groupByKey.flatMap({
          case (lang, vectors) => reservoirSampling(lang, vectors.toIterator, perLang).map((lang, _))
        }).collect()
      }

    assert(res.length == kmeansKernels, res.length)
    res
  }


  //
  //
  //  Kmeans method:
  //
  //

  /** Main kmeans computation
    * recibe un set  de vectores o puntos y retorna un ser de clusters o conjunto de puntos cercanos
    * */
  @tailrec final def kmeans(means: Array[(Int, Int)], vectors: RDD[(Int, Int)], iter: Int = 1, debug: Boolean = false): Array[(Int, Int)] = {


    val newMeans = means.clone()

    /*Este se tarda entre 3-4mins
    vectors.groupBy(findClosest(_, means))
      .mapValues(averageVectors).collect()
      .foreach(meanItem => newMeans.update(meanItem._1,meanItem._2))*/

    /*Este se tarda entre +- 3mins
      vectors.map(item => (findClosest(item,means),item))
        .groupByKey()
        .mapValues(averageVectors)
        .collect()
        .foreach(meanItem => newMeans.update(meanItem._1,meanItem._2))*/


    /*
    See how It has improved the performance only by change the way we program, in this case
    we only change groupBy or groupByKey for reduceByKey, the last one offer us a better perfomance
    in fac, if we have a cluster configuration the performance improve noticeably
    It takes 1min 30 Seg
     */
    vectors.map(item => (findClosest(item,means),(item._1.toLong,item._2.toLong,1)))
      .reduceByKey((x,y) => (x._1 + y._1, x._2 +y._2, x._3 + y._3))
      .mapValues(x => ((x._1 / x._3).toInt, (x._2 / x._3).toInt)).collect()
      .foreach(meanItem => newMeans.update(meanItem._1,meanItem._2))





// TODO: Fill in the newMeans array
val distance = euclideanDistance(means, newMeans)

if (debug) {
  println(s"""Iteration: $iter
             |  * current distance: $distance
             |  * desired distance: $kmeansEta
             |  * means:""".stripMargin)
  for (idx <- 0 until kmeansKernels)
  println(f"   ${means(idx).toString}%20s ==> ${newMeans(idx).toString}%20s  " +
          f"  distance: ${euclideanDistance(means(idx), newMeans(idx))}%8.0f")
}

if (converged(distance))
  newMeans
else if (iter < kmeansMaxIterations)
  kmeans(newMeans, vectors, iter + 1, debug)
else {
  if (debug) {
    println("Reached max iterations!")
  }
  newMeans
}
}




//
//
//  Kmeans utilities:
//
//

/** Decide whether the kmeans clustering converged */
def converged(distance: Double) =
distance < kmeansEta


/** Return the euclidean distance between two points */
def euclideanDistance(v1: (Int, Int), v2: (Int, Int)): Double = {
val part1 = (v1._1 - v2._1).toDouble * (v1._1 - v2._1)
val part2 = (v1._2 - v2._2).toDouble * (v1._2 - v2._2)
part1 + part2
}

/** Return the euclidean distance between two points */
def euclideanDistance(a1: Array[(Int, Int)], a2: Array[(Int, Int)]): Double = {
assert(a1.length == a2.length)
var sum = 0d
var idx = 0
while(idx < a1.length) {
  sum += euclideanDistance(a1(idx), a2(idx))
  idx += 1
}
sum
}

/** Return the closest point
* retorna el index (posicion) de centers cuya distancia euclidiana es menor */
def findClosest(p: (Int, Int), centers: Array[(Int, Int)]): Int = {
var bestIndex = 0
var closest = Double.PositiveInfinity
for (i <- 0 until centers.length) {
  val tempDist = euclideanDistance(p, centers(i))
  if (tempDist < closest) {
    closest = tempDist
    bestIndex = i
  }
}
bestIndex
}


/** Average the vectors */
def averageVectors(ps: Iterable[(Int, Int)]): (Int, Int) = {
val iter = ps.iterator
var count = 0
var comp1: Long = 0
var comp2: Long = 0
while (iter.hasNext) {
  val item = iter.next
  comp1 += item._1
  comp2 += item._2
  count += 1
}
((comp1 / count).toInt, (comp2 / count).toInt)
}




//
//
//  Displaying results:
//
//
def clusterResults(means: Array[(Int, Int)], vectors: RDD[(LangIndex, HighScore)]): Array[(String, Double, Int, Int)] = {
val closest  = vectors.map(p => (findClosest(p, means), p))
/*System.out.println("closest:::::::::::::::::")
closest.foreach(println(_))*/

val closestGrouped = closest.groupByKey()
/*System.out.println("closestGrouped:::::::::::::::::")
closestGrouped.foreach(println(_))*/

val median: RDD[(HighScore, (String, Double, HighScore, HighScore))] = closestGrouped.mapValues { vs =>

  /**
    * (a) the dominant programming language in the cluster;
    * NOTA ESTABA COMETIENDO EL SIGUIENTE ERROR vs.reduce((x, y)=> if (x._2 > y._2) x._1 else y._1) POR QUE NO FUNCIONA??
    * R// porque la firma de reduce es clara y dice que el elemento de retorno debe ser del tipo de la lista que
    * estoy procesano, recordar que es diferente a map que si la transforma
    */
  val langIdx: (LangIndex, HighScore) = vs.reduce((x, y)=> if (x._2 > y._2) x else y)
  val langLabel: String   = langs(langIdx._1/langSpread)//langs(vs.reduce((x, y)=> if (x._2 > y._2) x._1 else y._1)) // most common language in the cluster
  /**
    * (b) the percent of answers that belong to the dominant language;
    * : Iterable[(LangIndex, HighScore)]
    */
  val langPercent = if (vs.size>0 ) (vs.filter(x => x._1 == langIdx._1).size/vs.size)*100 else 0

  /**
    *(c) the size of the cluster (the number of questions it contains);
    */
  val clusterSize: Int    =  vs.size
  /**
    * (d) the median of the highest answer scores.
      vs es de tipo  Iterable[(LangIndex, HighScore)]
    */

  val median = vs.toList.sortBy(_._2).map(x  => x._2)
  val medianScore = {
    if((vs.size % 2) == 0) {
      Math.round((median((clusterSize/2)-1) + median(clusterSize/2)) / 2)
    }
    else median(Math.floor(clusterSize/2).toInt)
  }


  (langLabel, langPercent, clusterSize, medianScore)
}

median.collect().map(_._2).sortBy(_._4)
}

def printResults(results: Array[(String, Double, Int, Int)]): Unit = {
println("Resulting clusters:")
println("  Score  Dominant language (%percent)  Questions")
println("================================================")
for ((lang, percent, size, score) <- results)
  println(f"${score}%7d  ${lang}%-17s (${percent}%-5.1f%%)      ${size}%7d")
}
}
