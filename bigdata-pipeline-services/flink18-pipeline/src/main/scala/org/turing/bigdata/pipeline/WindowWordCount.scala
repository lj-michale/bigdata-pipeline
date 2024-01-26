//package org.turing.bigdata.pipeline
//
//import org.apache.flink.streaming.api.scala._
//import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows
//import org.apache.flink.streaming.api.windowing.time.Time
//
///**
// * @descri: 基于流窗口的单词统计
// *
// * @author: lj.michale
// * @date: 2024/1/25 10:59
// */
//object WindowWordCount {
//  def main(args: Array[String]) {
//
//    val env = StreamExecutionEnvironment.getExecutionEnvironment
//    val text = env.socketTextStream("localhost", 9999)
//
//    val counts = text.flatMap { _.toLowerCase.split("\\W+") filter { _.nonEmpty } }
//      .map { (_, 1) }
//      .keyBy(_._1)
//      .window(TumblingProcessingTimeWindows.of(Time.seconds(5)))
//      .sum(1)
//
//    counts.print()
//
//    env.execute("Window Stream WordCount")
//  }
//}