package org.turing.bigdata

import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.SparkSession

/**
 * @descri:
 *
 * @author: lj.michale
 * @date: 2024/1/23 10:33
 */
object SparkExamplePipeline1 {

  def main(args: Array[String]): Unit = {

    Logger.getLogger("org").setLevel(Level.OFF)
    val spark = SparkSession
      .builder
      .master("local[*]")
      .appName("SparkSQLExample")
      .getOrCreate()

    println(" >>>>>>>>>>>>>>>>>>>>>>>>> ")

    spark.stop()


  }

}
