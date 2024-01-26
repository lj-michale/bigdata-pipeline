package org.turing.flink.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @descri: 
 *
 * @author: lj.michale
 * @date: 2024/1/26 10:29
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WaterSensor {
    private String id;
    private Long ts;
    private Integer vc;

    @Override
    public String toString() {
        return "ws(" + id + "," + ts + "," + vc + ")";
    }

}