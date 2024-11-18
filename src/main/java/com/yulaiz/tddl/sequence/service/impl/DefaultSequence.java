package com.yulaiz.tddl.sequence.service.impl;

import com.yulaiz.tddl.sequence.exception.SequenceException;
import com.yulaiz.tddl.sequence.service.SequenceDao;
import com.yulaiz.tddl.sequence.vo.SequenceRange;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


@Slf4j
public class DefaultSequence {

    private final SequenceDao sequenceDao;

    private final String sequenceName;

    public DefaultSequence(SequenceDao sequenceDao, String sequenceName) {
        this.sequenceDao = sequenceDao;
        this.sequenceName = sequenceName;
    }

    private final Lock lock = new ReentrantLock();

    private volatile SequenceRange currentRange;

    public long nextValue() throws SequenceException {
        if (currentRange == null) {
            lock.lock();
            try {
                if (currentRange == null) {
                    currentRange = sequenceDao.nextRange(sequenceName);
                }
            } finally {
                lock.unlock();
            }
        }
        long value = currentRange.getAndIncrement();
        if (value == -1) {
            lock.lock();
            try {
                for (; ; ) {
                    if (currentRange.isOver()) {
                        currentRange = sequenceDao.nextRange(sequenceName);
                    }
                    value = currentRange.getAndIncrement();
                    if (value == -1) {
                        continue;
                    }
                    break;
                }
            } finally {
                lock.unlock();
            }
        }
        if (value < 0) {
            throw new SequenceException("Sequence value overflow, value = " + value);
        }
        return value;
    }

}
