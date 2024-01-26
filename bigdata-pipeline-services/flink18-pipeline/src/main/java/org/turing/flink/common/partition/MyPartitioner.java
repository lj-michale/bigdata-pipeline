package org.turing.flink.common.partition;

import org.apache.flink.api.common.functions.Partitioner;

/**
 * @descri: 自定义分区器：数据%分区数，只输入奇数
 *
 * @author: lj.michale
 * @date: 2024/1/26 17:21
 */
public class MyPartitioner implements Partitioner<String> {
    @Override
    public int partition(String key, int numPartitions) {
        return Integer.parseInt(key) % numPartitions;
    }

}
