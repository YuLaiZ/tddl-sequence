package com.yulaiz.tddl.sequence.service;

import com.yulaiz.tddl.sequence.exception.SequenceException;

import java.util.List;

public interface Sequence {
    /**
     * 取得序列下一个值
     *
     * @param sequenceName 序列名称
     * @return 返回序列下一个值
     * @throws SequenceException 获取序列失败
     */
    long nextValue(String sequenceName) throws SequenceException;

    /**
     * 根据步长取得序列下一组值
     *
     * @param sequenceName 序列名称
     * @param step         步长,一共获取多少个值
     * @return 返回序列下一组值
     * @throws SequenceException 获取序列失败
     */
    List<Long> nextValueList(String sequenceName, int step) throws SequenceException;
}
