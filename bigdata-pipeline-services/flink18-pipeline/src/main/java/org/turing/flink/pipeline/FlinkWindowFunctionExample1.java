package org.turing.flink.pipeline;


import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.turing.flink.bean.WaterSensor;
import org.turing.flink.function.WaterSensorMapFunction;

/**
 * @descri:
 *
 * @author: lj.michale
 * @date: 2024/1/26 10:19
 */
public class FlinkWindowFunctionExample1 {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStreamSource<String> socketDS = env.socketTextStream("localhost", 7777);

        SingleOutputStreamOperator<WaterSensor> sensorDS = socketDS.map(new WaterSensorMapFunction());
        KeyedStream<WaterSensor, String> sensorKS = sensorDS.keyBy(value -> value.getId());
        WindowedStream<WaterSensor, String, TimeWindow> sensorWS =
                sensorKS.window(TumblingProcessingTimeWindows.of(Time.seconds(5)));

        SingleOutputStreamOperator<WaterSensor> reduceDS = sensorWS.reduce((value1, value2) -> {
            System.out.println("value1Id" + value1.getId() + "value2Id" + value2.getId());
            return new WaterSensor(value1.getId(), value2.getTs(), value1.getVc() + value2.getVc());
        });

        reduceDS.print();

        env.execute();
    }

}
