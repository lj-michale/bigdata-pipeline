package org.turing.flink.function;

import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.java.tuple.Tuple3;


/**
 * @descri:
 * // 算子计算
 * dataStream.assignTimestampsAndWatermarks(new Covid19Watermark())
 *         .map(new Covid19MapFunc())
 *         .keyBy(0)//可以访问keyedstate
 *         .window(SlidingEventTimeWindows.of(Time.seconds(10), Time.seconds(5)))//10秒钟的窗口，滑动间隔是5秒 滑动窗口可能触发两次计算
 *         .aggregate(new Covid19AggFunc())
 *         .addSink(esSinkBuilder.build());
 * @author: lj.michale
 * @date: 2024/1/26 14:26
 */
public class Covid19AggFunc implements AggregateFunction<Tuple3<String, Integer, Long>, Tuple3<String, Integer, Long>, Tuple3<String, Integer, Long>> {
    // 城市code -> 确诊数量
    // private MapState<String,Integer> mapState;

    /**
     * 初始化列累加器 .创建一个新的累加器，启动一个新的聚合,负责迭代状态的初始化
     *
     * @return
     */
    @Override
    public Tuple3<String, Integer, Long> createAccumulator() {
        return new Tuple3<>("", 0, 0L);
    }

    /**
     * 累加器的累加方法 来一条数据执行一次 对于数据的每条数据，和迭代数据的聚合的具体实现
     *
     * @param tpInput
     * @param tpAcc
     * @return 返回新的累加器
     */
    @Override
    public Tuple3<String, Integer, Long> add(Tuple3<String, Integer, Long> tpInput, Tuple3<String, Integer, Long> tpAcc) {
        if (tpAcc.f0.equals(tpInput.f0)) {
            return new Tuple3<>(tpInput.f0, tpInput.f1 + tpAcc.f1, tpInput.f2);
        } else {
            return tpInput;
        }
    }

    /**
     * 返回值  在窗口内满足2个，计算结束的时候执行一次，从累加器获取聚合的结果
     *
     * @param tpAcc
     * @return
     */
    @Override
    public Tuple3<String, Integer, Long> getResult(Tuple3<String, Integer, Long> tpAcc) {
        String city_code = tpAcc.f0;
        Integer nowCount = tpAcc.f1;
        Integer level;
        if (nowCount.compareTo(50) > 0) {
            //高风险
            level = 2;
        } else if (nowCount.compareTo(10) > 0 && nowCount.compareTo(50) <= 0) {
            //中风险
            level = 1;
        } else {
            //低风险
            level = 0;
        }
        return new Tuple3<>(tpAcc.f0, level, tpAcc.f2);
    }

    /**
     * 累加器合并 merge方法仅SessionWindow会调用
     *
     * @param stringIntegerTuple2
     * @param acc1
     * @return
     */
    @Override
    public Tuple3<String, Integer, Long> merge(Tuple3<String, Integer, Long> stringIntegerTuple2, Tuple3<String, Integer, Long> acc1) {
        return null;
        //return new Tuple2<>(stringIntegerTuple2.f0, stringIntegerTuple2.f1 + acc1.f1);
    }
}