package org.turing.flink.function;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.apache.flink.table.annotation.DataTypeHint;
import org.apache.flink.table.functions.ScalarFunction;

/**
 * @descri:
 *
 * @author: lj.michale
 * @date: 2024/1/25 15:08
 */
public class MyFloor extends ScalarFunction {

    public @DataTypeHint("TIMESTAMP(3)") LocalDateTime eval(
            @DataTypeHint("TIMESTAMP(3)") LocalDateTime timestamp) {

        return timestamp.truncatedTo(ChronoUnit.HOURS);
    }

}