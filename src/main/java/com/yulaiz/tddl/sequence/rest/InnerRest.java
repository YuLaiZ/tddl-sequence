package com.yulaiz.tddl.sequence.rest;

import com.yulaiz.tddl.sequence.exception.SequenceException;
import com.yulaiz.tddl.sequence.service.Sequence;
import com.yulaiz.tddl.sequence.vo.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/rest-inner-api/v1")
@RequiredArgsConstructor(onConstructor_ = {@Lazy, @Autowired})
public class InnerRest {
    private final Sequence sequence;

    @PostMapping("/nextValue")
    public Result<Long> nextValue(@RequestParam String sequenceName) {
        log.info("请求序列:{}", sequenceName);
        if (sequenceName == null) {
            log.debug("请求序列, 失败:序列名为空");
            return Result.fail("序列名为空");
        }
        sequenceName = sequenceName.trim();
        if (sequenceName.isEmpty()) {
            log.debug("请求序列, 失败:序列名为空");
            return Result.fail("序列名为空");
        }
        try {
            long sequenceValue = this.sequence.nextValue(sequenceName);
            log.debug("请求序列:{}, 成功:{}", sequenceName, sequenceValue);
            return Result.success(sequenceValue);
        } catch (Exception e) {
            log.error(e.getMessage());
            if (e instanceof SequenceException) {
                String message = e.getMessage();
                log.debug("请求序列:{}, 失败:{}", sequenceName, message);
                return Result.fail(message);
            } else {
                log.debug("请求序列:{}, 失败:系统内部错误", sequenceName);
                return Result.fail("系统内部错误");
            }
        }
    }

    @PostMapping("/nextValueList")
    public Result<List<Long>> nextValueList(@RequestParam String sequenceName,
                                            @RequestParam Integer step) {
        log.info("请求批量序列:{}, 步长:{}", sequenceName, step);
        if (sequenceName == null) {
            log.debug("请求批量序列, 失败:序列名为空");
            return Result.fail("序列名为空");
        }
        sequenceName = sequenceName.trim();
        if (sequenceName.isEmpty()) {
            log.debug("请求批量序列, 失败:序列名为空");
            return Result.fail("序列名为空");
        }
        if (step == null || step <= 0) {
            log.debug("请求批量序列:{}, 失败:步长为空", sequenceName);
            return Result.fail("步长为空");
        }
        try {
            List<Long> sequenceValueList = this.sequence.nextValueList(sequenceName, step);
            log.debug("请求批量序列:{}, 步长:{}, 成功:{}", sequenceName, step, sequenceValueList);
            return Result.success(sequenceValueList);
        } catch (Exception e) {
            log.error(e.getMessage());
            if (e instanceof SequenceException) {
                String message = e.getMessage();
                log.debug("请求批量序列:{}, 步长:{}, 失败:{}", sequenceName, step, message);
                return Result.fail(message);
            } else {
                log.debug("请求批量序列:{}, 步长:{}, 失败:系统异常", step, sequenceName);
                return Result.fail("系统内部错误");
            }
        }
    }

}
