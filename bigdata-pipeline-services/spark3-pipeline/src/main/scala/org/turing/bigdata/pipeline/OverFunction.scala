package org.turing.bigdata.pipeline

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

/**
 * @descri:
 *
 * @author: lj.michale
 * @date: 2024/1/23 17:07
 */
object OverFunction {

  case class Score(name:String,clazz:Int,score:Int)

  def main(args: Array[String]): Unit = {

    val sparkConf = new SparkConf().setAppName("over").setMaster("local[*]")
    val spark = SparkSession.builder().config(sparkConf).getOrCreate()

    //隐式转换
    import spark.implicits._
    println("//***************  原始的班级表  ****************//")
    val scoreDF = spark.sparkContext.makeRDD(Array( Score("a", 1, 80),
      Score("b", 1, 78),
      Score("c", 1, 95),
      Score("d", 2, 74),
      Score("e", 2, 92),
      Score("f", 3, 99),
      Score("g", 3, 99),
      Score("h", 3, 45),
      Score("i", 3, 55),
      Score("j", 3, 78))).toDF("name","class","score")
    scoreDF.createOrReplaceTempView("score")
    scoreDF.show()

    println("//***************  求每个班最高成绩学生的信息  ***************/")
    println("    /*******  开窗函数的表  ********/")
    spark.sql("select name,class,score,rank() over(partition by class order by score desc) rank from score").show()

    println("/********计算结果的表************/")
    spark.sql("select * from(select name,class,score,rank() over(partition by class order by score desc) rank from score)as a where a.rank=1").show()

    println("/**************  求每个班最高成绩学生的信息（groupBY）  ***************/")
    spark.sql("select a.name,b.class,b.max from score as a,(select class,max(score) max from score group by class)as b where a.score=b.max").show()

    spark.stop()

  }

}
