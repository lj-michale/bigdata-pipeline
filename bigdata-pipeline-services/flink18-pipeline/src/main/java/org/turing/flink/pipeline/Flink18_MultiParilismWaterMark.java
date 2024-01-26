package org.turing.flink.pipeline;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessAllWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.turing.flink.bean.WaterSensor;
import org.turing.flink.common.MyUtil;
import org.turing.flink.function.WaterSensorMapFunction;

import java.time.Duration;

/**
 *  如果某一个并行度的watermark不推进导致整个下游的watermark无法推荐，如何处理?
 *
 *  查看水印?
 *      WEBUI: 当前看不到，看到下游收到的水印。
 *
 *  ------------------------------
 *      6个MapTask，只有1个能收到数据，但是6个人都要发送水印。
 *          设置Task发送水印的资格。
 *              如果一个Task，长期无法收到数据，导致水印无法更新，推进。就取消这个Task的水印发送资格！
 *           Idle: 空闲，赋闲。
 */
public class Flink18_MultiParilismWaterMark {
    public static void main(String[] args) {

        Configuration conf = new Configuration();
        conf.setInteger("rest.port", 3333);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment(conf);

        //修改水印的默认的发送频率
        env.getConfig().setAutoWatermarkInterval(2000);
        //模拟多并行度
        env.setParallelism(6);

        //声明水印策略
        WatermarkStrategy<WaterSensor> watermarkStrategy = WatermarkStrategy
                //第一部分: 水印的特征。 连续，乱序
                .<WaterSensor>forMonotonousTimestamps()
                //第二部分： 如何从数据中提取事件时间
                .withTimestampAssigner( (e, ts) -> e.getTs())
                .withIdleness(Duration.ofSeconds(10));

        env.socketTextStream("hadoop102", 8888)
                //全局汇总 所有的数据都给下游的第一个Task
                .global()
                .map(new WaterSensorMapFunction())
                .assignTimestampsAndWatermarks(watermarkStrategy)
                /**
                    数据落入哪个窗口不看水印(没有半毛钱关系)。只看 数据的EventTime和窗口的范围。
                    [0,4999):
                    [5000,9999):
                 */
                .windowAll(TumblingEventTimeWindows.of(Time.milliseconds(5000)))
                .process(new ProcessAllWindowFunction<WaterSensor, String, TimeWindow>() {
                    @Override
                    public void process(ProcessAllWindowFunction<WaterSensor, String, TimeWindow>.Context context, Iterable<WaterSensor> iterable, Collector<String> collector) throws Exception {
                        //输出窗口中的所有的元素
                        collector.collect(MyUtil.parseToList(iterable).toString());
                    }
                }).print();

        try {
            env.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
