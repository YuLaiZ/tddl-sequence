package com.yulaiz.tddl.sequence.service;

import com.yulaiz.tddl.sequence.exception.SequenceException;
import com.yulaiz.tddl.sequence.vo.SequenceRange;

public interface SequenceDao {

    /**
     * 取得下一个可用的序列区间
     *
     * @param sequenceName 序列名称
     * @return 返回下一个可用的序列区间
     * @throws SequenceException 获取序列失败
     */
    SequenceRange nextRange(String sequenceName) throws SequenceException;

}
