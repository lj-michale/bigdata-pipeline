package org.turing.flink.function;

import org.apache.flink.api.common.functions.MapFunction;
import org.turing.flink.bean.WaterSensor;

public class WaterSensorMapFunction implements MapFunction<String, WaterSensor>
{
    @Override
    public WaterSensor map(String value) throws Exception {
        String[] words = value.split(",");
        return new WaterSensor(
                words[0],
                Long.valueOf(words[1]),
                Integer.valueOf(words[2])
        );
    }
}

