package org.turing.flink.pipeline.other;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.turing.flink.source.ClickSource;
import org.turing.flink.source.ClickSourceWithWatermark;


/**
 * @descri: 在自定义数据源中发送水位线
 * 也可以在自定义的数据源中抽取事件时间，然后发送水位线。这里要注意的是，在自定义数据源中发送了水位线以后，
 * 就不能再在程序中使用assignTimestampsAndWatermarks方法 来 生 成 水 位 线 了 。
 * 在自定义 数 据 源 中 生 成 水 位 线 和 在 程 序 中 使用assignTimestampsAndWatermarks方法生成水位线二者只能取其一。
 * @author: lj.michale
 * @date: 2024/1/26 16:27
 */
public class ClickSourceWithWatermarkExample {

    public static void main(String[] args) throws Exception {
        
        Configuration conf = new Configuration();
        conf.setInteger("rest.port", 8081);
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment(conf);
        env.setParallelism(1);
        
        env.addSource(new ClickSourceWithWatermark()).print();

        env.execute();

    }
}
