package org.turing.flink.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    private String user;

    private String url;

    public long getTimestamps() {
        return System.currentTimeMillis();
    }

    public void setTimestamps(long timestamps) {
        this.timestamps = timestamps;
    }

    private long timestamps;

    @Override
    public String toString() {
        return "ws(" + user + "," + url + "," + timestamps + ")";
    }

}
