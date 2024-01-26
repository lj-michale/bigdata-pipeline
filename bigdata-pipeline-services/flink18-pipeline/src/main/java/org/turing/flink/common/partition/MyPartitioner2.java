package org.turing.flink.common.partition;

import org.apache.flink.api.common.functions.Partitioner;


/**
 * @descri:
 *
 * @author: lj.michale
 * @date: 2024/1/26 17:30
 */
public class MyPartitioner2 implements Partitioner<Integer> {
    /**
     * 定义具体的分区规则
     * 比如：
     * 将数据的数据的按照如下的规则进行划分：
     * 范围  <= 9   在一个分区
     * 范围  10-99  在一个分区
     * 范围  > 99   在一个分区
     * @param integer  输入的数据
     * @param numPartitions  分区的个数  现在在如下的案例中分区的数是3个，也就是输入的并行度。
     * @return
     */
    @Override
    public int partition(Integer integer, int numPartitions) {
        if (integer <= 9) {
            return 0;
        } else if (integer >= 10 && integer <= 99) {
            return 1;
        } else {
            return 2;
        }
    }
}
