package com.example.begger.utils;

public class Timer {
    private long lastMS = System.currentTimeMillis();

    public boolean hasTimeElapsed(long time, boolean reset) {
        if (System.currentTimeMillis() - lastMS >= time) {
            if (reset) reset();
            return true;
        }
        return false;
    }

    public void reset() {
        lastMS = System.currentTimeMillis();
    }

    public long getPassed() {
        return System.currentTimeMillis() - lastMS;
    }
}
