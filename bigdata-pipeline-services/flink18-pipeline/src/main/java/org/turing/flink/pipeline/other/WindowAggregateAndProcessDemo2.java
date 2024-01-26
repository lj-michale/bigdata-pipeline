package org.turing.flink.pipeline.other;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.turing.flink.bean.WaterSensor;
import org.turing.flink.function.WaterSensorMapFunction;

/**
 * @descri: 增量聚合和全窗口函数的结合使用
 *
 * @author: lj.michale
 * @date: 2024/1/26 11:13
 */
public class WindowAggregateAndProcessDemo2 {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStreamSource<String> socketDS = env.socketTextStream("localhost", 7777);
        env.setParallelism(1);

        SingleOutputStreamOperator<WaterSensor> sensorDS = socketDS.map(new WaterSensorMapFunction());
        KeyedStream<WaterSensor, String> sensorKS = sensorDS.keyBy(value -> value.getId());

        WindowedStream<WaterSensor, String, TimeWindow> sensorWS =
                sensorKS.window(TumblingProcessingTimeWindows.of(Time.seconds(10)));

        sensorWS.aggregate(new MyAgg(),new MyProcess()).print();

        env.execute();
    }

    public static class MyAgg implements AggregateFunction<WaterSensor, Integer, String> {
        @Override
        public Integer createAccumulator() {
            System.out.println("创建累加器");
            return 0;
        }
        @Override
        public Integer add(WaterSensor waterSensor, Integer integer) {
            System.out.println("调用计算逻辑");
            //integer是之前的计算结果
            return waterSensor.getVc() + integer;
        }
        @Override
        public String getResult(Integer integer) {
            System.out.println("获取计算结果");
            return integer.toString();
        }
        @Override
        public Integer merge(Integer integer, Integer acc1) {
            //合并两个累加器
            System.out.println("调用merge");
            //todo 该方法通常只有会话窗口才会使用
            return null;
        }
    }

    public static class MyProcess extends ProcessWindowFunction<String, String, String, TimeWindow> {
        @Override
        public void process(String s, Context context, Iterable<String> iterable, Collector<String> collector) throws Exception {
            //使用上下文获取窗口信息
            long start = context.window().getStart(); //获取窗口开始时间
            long end = context.window().getEnd(); //获取窗口结束时间
            String windowStart = DateFormatUtils.format(start, "yyyy-MM-dd HH:mm:ss.SSS");
            String windowEnd = DateFormatUtils.format(end, "yyyy-MM-dd HH:mm:ss.SSS");
            long amount = iterable.spliterator().estimateSize();//获取数据条数
            collector.collect("key=" + s + "的窗口[" + windowStart + "-" + windowEnd + ")包含" + amount + "条数据==>" + iterable.toString());
        }
    }
}
//运行结果:====
//        创建累加器
//        调用计算逻辑
//        调用计算逻辑
//        调用计算逻辑
//        获取计算结果
//        key=S1的窗口[2023-06-10 18:27:00.000-2023-06-10 18:27:10.000)包含1条数据==>[3]
