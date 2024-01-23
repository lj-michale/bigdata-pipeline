package org.turing.spark.other;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;

import java.util.ArrayList;
import java.util.List;

/**
 * @descri:
 *
 * @author: lj.michale
 * @date: 2024/1/23 15:40
 */
public class SparkPiApp {
    public static void main(String[] args) throws Exception {

        SparkConf conf = new SparkConf()
                .setAppName("MySpark")
                //远程连接时需要将本地包分发到 worker 否则可能报错: cannot assign instance of java.lang.invoke.SerializedLambda
                .setJars(new String[]{"E:\\IdeaProjects\\spark-demo\\target\\mySpark.jar"})
                .setMaster("spark://192.168.1.6:7077");

        JavaSparkContext jsc = new JavaSparkContext(conf);

        getPi(jsc);

    }

    /**
     * 计算 pi
     * 即(0,1)随机数落在 1/4 圆占单位正方形的概率 => (1/4 * (Pi*1^2))/(1^2) => Pi/4 = count/numSamples
     */
    public static void getPi(JavaSparkContext jsc){
        int numSamples = 1000000;
        List<Integer> l = new ArrayList<>(numSamples);
        for (int i = 0; i < numSamples; i++) {
            l.add(i);
        }
        //统计命中数
        long count = jsc.parallelize(l).filter(i -> {
            double x = Math.random();
            double y = Math.random();
            return x*x + y*y < 1;
        }).count();
        System.out.println("Pi is roughly " + 4.0 * count / numSamples);
    }
}
