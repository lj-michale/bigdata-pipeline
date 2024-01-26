package org.turing.flink.source;

import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.turing.flink.bean.Event;

import java.util.Calendar;
import java.util.Random;


/**
 * @descri:
 *
 * @author: lj.michale
 * @date: 2024/1/26 13:09
 */
public class ClickSource implements SourceFunction<Event> {
    private static Boolean run = true;

    @Override
    public void run(SourceContext sourceContext) throws Exception {
        Random random = new Random();
        String[] users = {"tom","mary","bibby","diff","alex"};
        String[] urls = {"/home","page1","order","favor","display1"};

        while (run){
            String user = users[random.nextInt(users.length)];
            String url = urls[random.nextInt(urls.length)];
            long timestamps = Calendar.getInstance().getTimeInMillis();
            Event event = new Event(user, url, timestamps);
            sourceContext.collect(event);

            Thread.sleep(100);
        }
    }

    @Override
    public void cancel() {
        run = false;
    }

}
