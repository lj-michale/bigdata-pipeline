package org.turing.flink.pipeline.other;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.turing.flink.bean.Event;
import org.turing.flink.source.ClickSource;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

/**
 * @descri: 实时统计一段时间内的top2的url
 *
 * @author: lj.michale
 * @date: 2024/1/26 13:12
 */
public class TopNExample_ProcessWindowFunction1 {
    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        SingleOutputStreamOperator<Event> stream = env.addSource(new ClickSource())
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<Event>forBoundedOutOfOrderness(Duration.ZERO)
                                .withTimestampAssigner(((event, l) -> event.getTimestamps()))
                );

        stream.print("input");

        //将所有url分到一个组，之后基于keyedDataStream进行开窗，调用.process函数实现ProcessWindowFunction抽象类
        SingleOutputStreamOperator<String> process = stream.keyBy(value -> true)
                .window(TumblingEventTimeWindows.of(Time.seconds(10)))
                .process(new ProcessWindowFunction<Event, String, Boolean, TimeWindow>() {
                    @Override
                    public void process(Boolean aBoolean,
                                        ProcessWindowFunction<Event, String, Boolean, TimeWindow>.Context context,
                                        Iterable<Event> iterable,
                                        Collector<String> collector) throws Exception {
                        //计算每个url的count，创建HashSet存放url以及访问量，每来一个url，count值+1
                        HashMap<String, Long> hashMap = new HashMap<>();
                        for (Event event : iterable) {
                            Long count = hashMap.getOrDefault(event.getUrl(), 0L);
                            hashMap.put(event.getUrl(), count + 1);
                        }

                        //将HashSet中的数据放入列表中 方便后续排序
                        ArrayList<Tuple2<String, Long>> urlList = new ArrayList<>();
                        for (String url : hashMap.keySet()) {
                            urlList.add(Tuple2.of(url, hashMap.get(url)));
                        }
                        //从大到小降序排列
                        urlList.sort(new Comparator<Tuple2<String, Long>>() {
                            @Override
                            public int compare(Tuple2<String, Long> o1, Tuple2<String, Long> o2) {

                                return Long.compare(o2.f1, o1.f1);
                            }
                        });

                        //方便起见，我们将其包装成String字符串打印
                        StringBuffer buffer = new StringBuffer();
                        buffer.append("===================\n");
                        //从context中获取窗口的起始和结束时间
                        buffer.append("窗口[" + context.window().getStart() + "~" + context.window().getEnd() + "]\n");

                        //循环遍历List列表，[0-2)元素个数，寻找Top2
                        for (int i = 0; i < Math.min(2, urlList.size()); i++) {
                            Tuple2<String, Long> tuple2 = urlList.get(i);
                            buffer.append("NO." + (i + 1) + ":url: " + tuple2.f0 + " 的访问量： " + tuple2.f1 + "\n");
                        }

                        collector.collect(buffer.toString());
                    }
                });

        process.print("processed");//打印输出

        env.execute();

    }

}
