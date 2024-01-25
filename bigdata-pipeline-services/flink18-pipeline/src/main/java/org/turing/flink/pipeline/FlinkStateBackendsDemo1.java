package org.turing.flink.pipeline;


import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.contrib.streaming.state.EmbeddedRocksDBStateBackend;
import org.apache.flink.runtime.state.hashmap.HashMapStateBackend;
import org.apache.flink.runtime.state.storage.FileSystemCheckpointStorage;
import org.apache.flink.runtime.state.storage.JobManagerCheckpointStorage;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.concurrent.TimeUnit;

/**
 * @descri: 状态后端示例
 *         Available State Backends
 *         1.HashMapStateBackend
 *         2.EmbeddedRocksDBStateBackend
 *
 * @author: lj.michale
 * @date: 2024/1/25 11:09
 */
public class FlinkStateBackendsDemo1 {

    public static void main(String[] args) {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // The fixed delay restart strategy can also be set programmatically:
        env.setRestartStrategy(RestartStrategies.failureRateRestart(
                3, // max failures per interval
                Time.of(5, TimeUnit.MINUTES), //time interval for measuring failure rate
                Time.of(10, TimeUnit.SECONDS) // delay
        ));
        env.setStateBackend(new HashMapStateBackend());
//        env.setStateBackend(new EmbeddedRocksDBStateBackend());
        env.enableChangelogStateBackend(true);
        //env.getCheckpointConfig().setCheckpointStorage(new JobManagerCheckpointStorage());
        env.getCheckpointConfig().setCheckpointStorage("file:///checkpoint-dir");
        // Advanced FsStateBackend configurations, such as write buffer size
        // can be set by manually instantiating a FileSystemCheckpointStorage object.
        env.getCheckpointConfig().setCheckpointStorage(new FileSystemCheckpointStorage("file:///checkpoint-dir"));


    }
    
}
