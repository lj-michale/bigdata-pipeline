package org.turing.flink.common.partition;

import org.apache.flink.api.common.functions.Partitioner;

/**
 * @descri:
 *
 * @author: lj.michale
 * @date: 2024/1/26 17:32
 */
public class MyStreamPartitioner implements Partitioner<String> {

    /**
     * @param key
     * @param numPartitions
     * @return
     */
    @Override
    public int partition(String key, int numPartitions) {
        return (key.hashCode() & Integer.MAX_VALUE) % numPartitions;
    }
}
