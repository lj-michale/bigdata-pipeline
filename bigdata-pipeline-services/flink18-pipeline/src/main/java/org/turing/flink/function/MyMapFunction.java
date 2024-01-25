package org.turing.flink.function;

import org.apache.flink.api.common.functions.MapFunction;

/**
 * @descri:
 *
 * @author: lj.michale
 * @date: 2024/1/25 11:04
 */
class MyMapFunction implements MapFunction<String, Integer> {
    public Integer map(String value) {
        return Integer.parseInt(value);
    }
}