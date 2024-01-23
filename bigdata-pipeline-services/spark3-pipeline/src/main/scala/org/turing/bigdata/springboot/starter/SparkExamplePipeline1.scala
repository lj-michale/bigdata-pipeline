package org.turing.bigdata.springboot.starter

import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.{DataFrame, SparkSession}

/**
 * @descri:
 *
 * @author: lj.michale
 * @date: 2024/1/23 10:33
 */
object SparkExamplePipeline1 {

    case class DateT(name: String, birthday: String)

    def main(args: Array[String]): Unit = {

      val spark = SparkSession.builder()
        .master("local[*]")
        .appName("SparkExamplePipeline1")
        .getOrCreate()
      import spark.implicits._
      import org.apache.spark.sql.functions._

      val sc = spark.sparkContext
      sc.setLogLevel("ERROR")

      val DateTest: DataFrame = Seq(
        DateT("aa", "1995-12-11 12:12:13"),
        DateT("bb", "2000-01-14 10:10:57")
      ).toDF()

      DateTest.withColumn("current_date", current_date())
        .withColumn("current_timestamp", current_timestamp())
        .withColumn("date", date_format(current_timestamp(), "yyyy-MM-dd HH:mm:ss"))
        .withColumn("date1", date_format(current_timestamp(), "yyyy-MM-dd hh:mm:ss"))
        .withColumn("date2", date_format(col("birthday"), "yyyy-MM-dd"))
        .show(false)

      spark.close()

    }

}
