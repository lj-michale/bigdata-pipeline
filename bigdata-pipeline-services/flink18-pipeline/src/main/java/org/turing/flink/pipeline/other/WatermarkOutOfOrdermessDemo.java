package org.turing.flink.pipeline.other;


import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.turing.flink.bean.WaterSensor;
import org.turing.flink.function.WaterSensorMapFunction;

import static org.apache.flink.api.common.eventtime.WatermarkStrategy.forBoundedOutOfOrderness;
import static org.apache.flink.api.common.eventtime.WatermarkStrategy.forMonotonousTimestamps;

/**
 * @descri: 生成水位线
 *
 * @author: lj.michale
 * @date: 2024/1/26 11:39
 */
public class WatermarkOutOfOrdermessDemo {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        SingleOutputStreamOperator<WaterSensor> sensorDS = env
                .socketTextStream("localhost", 7777)
                .map(new WaterSensorMapFunction());

        //todo 定制水位线策略
        WatermarkStrategy<WaterSensor> watermarkStrategy = WatermarkStrategy
//                *******
//        forMonotonousTimestamps() //有序流
//        forBoundedOutOfOrderness(等待时间) //乱序流
//                *******
                .<WaterSensor>forMonotonousTimestamps()
                //指定时间戳分配器
                .withTimestampAssigner(new SerializableTimestampAssigner<WaterSensor>() {
                    @Override
                    public long extractTimestamp(WaterSensor waterSensor, long l) {
                        System.out.println("数据=" + waterSensor + ",recordTS=" + l);
                        return waterSensor.getTs() * 1000L;
                    }
                });

        SingleOutputStreamOperator<WaterSensor> sensorWithWaterMark = sensorDS
                .assignTimestampsAndWatermarks(watermarkStrategy);

        sensorWithWaterMark
                .keyBy(value -> value.getId())
                .window(TumblingEventTimeWindows.of(Time.seconds(10)))
                .process(new ProcessWindowFunction<WaterSensor, String, String, TimeWindow>() {
                            @Override
                            public void process(String s, Context context, Iterable<WaterSensor> iterable, Collector<String> collector) throws Exception {
                                //使用上下文获取窗口信息
                                long start = context.window().getStart(); //获取窗口开始时间
                                long end = context.window().getEnd(); //获取窗口结束时间
                                String windowStart = DateFormatUtils.format(start, "yyyy-MM-dd HH:mm:ss.SSS");
                                String windowEnd = DateFormatUtils.format(end, "yyyy-MM-dd HH:mm:ss.SSS");
                                long amount = iterable.spliterator().estimateSize();//获取数据条数
                                collector.collect("key="+s+"的窗口["+windowStart+"-"+windowEnd+")包含" +amount+"条数据==>"+iterable.toString());
                            }
                        }
                ).print();

        env.execute();

    }

}
