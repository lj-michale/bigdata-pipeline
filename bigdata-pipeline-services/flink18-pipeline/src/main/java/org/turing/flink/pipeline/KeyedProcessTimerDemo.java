package org.turing.flink.pipeline;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.TimerService;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

import java.time.Duration;

/**
 * @descri: KeyedProcessFunction案例
 *
 * @author: lj.michale
 * @date: 2024/1/26 9:29
 */
public class KeyedProcessTimerDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        SingleOutputStreamOperator<WaterSensor> sensorDS = env.socketTextStream("hadoop201", 6666)
                .map(new MapFunction<String, WaterSensor>() {
                    @Override
                    public WaterSensor map(String value) throws Exception {
                        String[] datas = value.split(",");
                        return new WaterSensor(datas[0], Long.valueOf(datas[1]), Integer.valueOf(datas[2]));
                    }
                })
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy
                                .<WaterSensor>forBoundedOutOfOrderness(Duration.ofSeconds(2))
                                .withTimestampAssigner((element, recordTimestamp) -> element.getTs() * 1000L)
                );

        KeyedStream<WaterSensor, String> sensorKS = sensorDS.keyBy(r -> r.getId());

        SingleOutputStreamOperator<String> process = sensorKS.process(
                new KeyedProcessFunction<String, WaterSensor, String>() {
                    /**
                     * 来一条数据调用一次
                     * @param value
                     * @param ctx
                     * @param out
                     * @throws Exception
                     */
                    @Override
                    public void processElement(WaterSensor value, Context ctx, Collector<String> out) throws Exception {
                        //获取当前数据的key
                        String currentKey = ctx.getCurrentKey();

                        // 定时器注册
                        TimerService timerService = ctx.timerService();

                        // 1、事件时间的案例
                        // 数据中提取出来的事件时间
                        Long currentEventTime = ctx.timestamp();
                        timerService.registerEventTimeTimer(5000L);
                        System.out.println("当前key=" + currentKey + ",当前时间=" + currentEventTime + ",注册了一个5s的定时器");

                        // 获取当前时间的watermark
                        long currentWatermark = timerService.currentWatermark();
                        System.out.println("当前数据=" + value + ",当前watermark=" + currentWatermark);

                    }

                    /**
                     * TODO 2.时间进展到定时器注册的时间，调用该方法
                     * @param timestamp 当前时间进展，就是定时器被触发时的时间
                     * @param ctx       上下文
                     * @param out       采集器
                     * @throws Exception
                     */
                    @Override
                    public void onTimer(long timestamp, OnTimerContext ctx, Collector<String> out) throws Exception {
                        super.onTimer(timestamp, ctx, out);
                        String currentKey = ctx.getCurrentKey();

                        System.out.println("key=" + currentKey + "现在时间是" + timestamp + "定时器触发");
                    }
                }
        );

        process.print();
        env.execute();
    }

    /**
     * 水位传感器：用于接收水位数据
     *
     * id:传感器编号
     * ts:时间戳
     * vc:水位
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WaterSensor {
        private String id;
        private Long ts;
        private Integer vc;
    }
}
