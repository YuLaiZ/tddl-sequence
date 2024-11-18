package com.yulaiz.tddl.sequence.service.impl;

import com.yulaiz.tddl.sequence.exception.SequenceException;
import com.yulaiz.tddl.sequence.service.Sequence;
import com.yulaiz.tddl.sequence.service.SequenceDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Lazy, @Autowired})
public class SequenceImpl implements Sequence {
    private final SequenceDao sequenceDao;
    private final Map<String, DefaultSequence> sequenceMap = new HashMap<>();
    private final Lock lock = new ReentrantLock();

    /**
     * 取得序列下一个值
     *
     * @param sequenceName 序列名称
     * @return 返回序列下一个值
     * @throws SequenceException 获取序列失败
     */
    @Override
    public long nextValue(String sequenceName) throws SequenceException {
        DefaultSequence sequence = this.getDefaultSequence(sequenceName);
        return sequence.nextValue();
    }

    /**
     * 根据步长取得序列下一组值
     *
     * @param sequenceName 序列名称
     * @param step         步长,一共获取多少个值
     * @return 返回序列下一组值
     * @throws SequenceException 获取序列失败
     */
    @Override
    public List<Long> nextValueList(String sequenceName, int step) throws SequenceException {
        DefaultSequence sequence = this.getDefaultSequence(sequenceName);
        List<Long> list = new ArrayList<>(step);
        for (int i = 0; i < step; i++) {
            list.add(sequence.nextValue());
        }
        return list;
    }

    private DefaultSequence getDefaultSequence(String sequenceName) {
        DefaultSequence sequence = sequenceMap.get(sequenceName);
        if (sequence == null) {
            lock.lock();
            try {
                sequence = sequenceMap.get(sequenceName);
                if (sequence == null) {
                    sequence = new DefaultSequence(sequenceDao, sequenceName);
                    sequenceMap.put(sequenceName, sequence);
                }
            } finally {
                lock.unlock();
            }
        }
        return sequence;
    }
}
