package timeusage

import org.apache.spark.sql.{ColumnName, DataFrame, Row}
import org.apache.spark.sql.types.{
  DoubleType,
  StringType,
  StructField,
  StructType
}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, FunSuite}

import scala.util.Random

@RunWith(classOf[JUnitRunner])
class TimeUsageSuite extends FunSuite with BeforeAndAfterAll {

  test("smoke test"){
    assert(3==3)
  }


  test("Row test1"){
    val v1 = timeusage.TimeUsage.row(List("foo","1.0","2.0"))
    val v2 = Row.fromSeq(List("foo",1.0,2.0))
    assert(v1 == v2 )
  }


  //
  test("dfSchema test 1"){
    val v1 = timeusage.TimeUsage.dfSchema(List("foo"))
    //en los test de coursera al parecer validan que el primer elemento sea string no importa el nombre
    //antes lo tenía que si era la columna tucaseid, pero no funcionaba
    val v2 = StructType(List(StructField("foo",StringType,false)))
    assert(v1 == v2 )
  }
}
