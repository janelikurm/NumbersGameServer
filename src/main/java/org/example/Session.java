package org.example;


import java.time.LocalDateTime;
import java.time.ZoneId;

public class Session {
    public static long lastActiveTime = 0;
    public static long timeNow = 0;
    long fifteenMinutesInMilliSeconds = 900000L;

    long tenSecondsInMilliseconds = 10000;

    public void lastActiveTime() {
        lastActiveTime = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        System.out.println(lastActiveTime);
    }

    public boolean isExpired() {
        timeNow = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return (timeNow - lastActiveTime) > tenSecondsInMilliseconds;
    }
}
