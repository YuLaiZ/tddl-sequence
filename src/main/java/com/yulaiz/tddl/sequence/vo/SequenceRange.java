package com.yulaiz.tddl.sequence.vo;

import lombok.Getter;

import java.util.concurrent.atomic.AtomicLong;

public class SequenceRange {

    @Getter
    private final long min;
    @Getter
    private final long max;
    private final AtomicLong value;
    @Getter
    private volatile boolean over = false;

    public SequenceRange(long min, long max) {
        this.min = min;
        this.max = max;
        this.value = new AtomicLong(min);
    }

    public long getAndIncrement() {
        long currentValue = value.getAndIncrement();
        if (currentValue > max) {
            over = true;
            return -1;
        }
        return currentValue;
    }

}
