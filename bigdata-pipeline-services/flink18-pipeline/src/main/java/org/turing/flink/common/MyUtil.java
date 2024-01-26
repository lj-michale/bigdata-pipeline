package org.turing.flink.common;

import java.util.ArrayList;
import java.util.List;

public class MyUtil
{
    //把窗口中的所有数据转为List集合
    public static <T> List<T> parseToList(Iterable<T> iterable){

        List<T> result = new ArrayList<>();

        for (T t : iterable) {
            result.add(t);
        }

        return result;
    }
}
