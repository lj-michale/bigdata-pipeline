package org.turing.flink.pipeline.other;

import org.apache.flink.api.common.eventtime.*;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.turing.flink.bean.Event;
import org.turing.flink.source.ClickSource;


/**
 * @descri: 断点式水位线生成器（PunctuatedGenerator）
 * 在onEvent()中判断当前事件的user字段，只有遇到“依琳”这个特殊的值时，才调用output.emitWatermark()发出水位线。这个过程是完全依靠事件来触发的，所以水位线的生成一定在某个数据到来之后。
 * @author: lj.michale
 * @date: 2024/1/26 16:16
 */
public class CustomPunctuatedGeneratorWatermarkExample {
    public static void main(String[] args) throws Exception {

        Configuration conf = new Configuration();
        conf.setInteger("rest.port", 8081);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment(conf);
        env.setParallelism(1);

        //生成断点式水位线
        env.addSource(new ClickSource())
                .assignTimestampsAndWatermarks(new MyWatermarkStrategy())
                .print();
        
        env.execute();
    }

    // 自定义周期性生成水位线
    public static class MyWatermarkStrategy implements WatermarkStrategy<Event> {
        @Override
        public TimestampAssigner<Event> createTimestampAssigner(TimestampAssignerSupplier.Context context) {
            return new SerializableTimestampAssigner<Event>() {
                @Override
                public long extractTimestamp(Event element, long recordTimestamp) {
                    return element.getTimestamps() * 1000L;
                }
            };
        }

        @Override
        public WatermarkGenerator<Event> createWatermarkGenerator(WatermarkGeneratorSupplier.Context context) {
            //周期生成水位线
            // return new MyPeriodicGenerator();
            //断点生成水位线
            return new MyPunctuatedGenerator();
        }
    }

    //断点生成水位线
    public static class MyPunctuatedGenerator implements WatermarkGenerator<Event> {

        @Override
        public void onEvent(Event event, long eventTimestamp, WatermarkOutput output) {
            //只有遇到特定数据时，才发送水位线
            if (event.getUser().equals("依琳")) {
                output.emitWatermark(new Watermark(event.getTimestamps() - 1L));
            }
        }

        @Override
        public void onPeriodicEmit(WatermarkOutput output) {
            //onEvent 已经发送了水位线，onPeriodicEmit不做处理即可
        }
    }

}
