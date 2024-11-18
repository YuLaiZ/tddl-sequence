package com.yulaiz.tddl.sequence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yulaiz.tddl.sequence.exception.SequenceException;
import com.yulaiz.tddl.sequence.service.Sequence;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@SpringBootTest
@Slf4j
class SequenceTest {
    @Autowired
    private Sequence sequence;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private DataSource dataSource;

    private static final String sequenceName1 = "tddl_test_seq";
    private static final String sequenceName2 = "tddl_test_seq1";
    private static final int step = 100;

    @Test
    void getNextValueTest() throws Exception {
        long value = this.sequence.nextValue(sequenceName1);
        long nextValue = this.sequence.nextValue(sequenceName1);
        log.debug("value:{}, nextValue:{}", value, nextValue);
        Assertions.assertEquals(value + 1, nextValue);
    }

    @Test
    void getSameThreadNextValueTest() throws Exception {
        long value = this.sequence.nextValue(sequenceName1);
        Sequence seq = applicationContext.getBean(Sequence.class);
        long nextValue = seq.nextValue(sequenceName1);
        log.debug("value:{}, nextValue:{}", value, nextValue);
        Assertions.assertEquals(value + 1, nextValue);
    }

    @Test
    void getDiffThreadNextValueTest() throws Exception {
        AtomicLong value = new AtomicLong();
        Thread thread1 = new Thread(() -> {
            try {
                value.set(this.sequence.nextValue(sequenceName1));
            } catch (SequenceException e) {
                throw new RuntimeException(e);
            }
        });

        AtomicLong nextValue = new AtomicLong();
        Thread thread2 = new Thread(() -> {
            try {
                nextValue.set(this.sequence.nextValue(sequenceName1));
            } catch (SequenceException e) {
                throw new RuntimeException(e);
            }
        });

        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();
        log.debug("value:{}, nextValue:{}", value, nextValue);
        Assertions.assertEquals(value.get() + 1, nextValue.get());
    }

    @Test
    void getDiffThreadAndDiffSeqNextValueTest() throws Exception {
        AtomicLong value1 = new AtomicLong();
        Thread thread1 = new Thread(() -> {
            try {
                value1.set(this.sequence.nextValue(sequenceName1));
            } catch (SequenceException e) {
                throw new RuntimeException(e);
            }
        });

        AtomicLong nextValue1 = new AtomicLong();
        Thread thread2 = new Thread(() -> {
            try {
                nextValue1.set(this.sequence.nextValue(sequenceName1));
            } catch (SequenceException e) {
                throw new RuntimeException(e);
            }
        });

        AtomicLong value2 = new AtomicLong();
        Thread thread3 = new Thread(() -> {
            try {
                value2.set(this.sequence.nextValue(sequenceName2));
            } catch (SequenceException e) {
                throw new RuntimeException(e);
            }
        });

        AtomicLong nextValue2 = new AtomicLong();
        Thread thread4 = new Thread(() -> {
            try {
                nextValue2.set(this.sequence.nextValue(sequenceName2));
            } catch (SequenceException e) {
                throw new RuntimeException(e);
            }
        });

        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();
        thread1.join();
        thread2.join();
        thread3.join();
        thread4.join();
        log.debug("value1:{}, nextValue1:{}", value1, nextValue1);
        log.debug("value2:{}, nextValue2:{}", value2, nextValue2);
        Assertions.assertEquals(value1.get() + 1, nextValue1.get());
        Assertions.assertEquals(value2.get() + 1, nextValue2.get());
        Assertions.assertNotEquals(value1.get(), value2.get());
        Assertions.assertNotEquals(nextValue1.get(), nextValue2.get());
    }

    @Test
    void getSameTheadInStepTest() throws Exception {
        Set<Long> values = new ConcurrentSkipListSet<>();
        int testStep = step - 3;
        for (int i = 0; i < testStep; i++) {
            values.add(this.sequence.nextValue(sequenceName1));
        }
        log.debug("values:{}", values);
        Assertions.assertEquals(testStep, values.size());
        Assertions.assertEquals(values.size(), new HashSet<>(values).size());
    }

    @Test
    void getSameTheadOutStepTest() throws Exception {
        Set<Long> values = new ConcurrentSkipListSet<>();
        int testStep = step + 3;
        for (int i = 0; i < testStep; i++) {
            values.add(this.sequence.nextValue(sequenceName1));
        }
        log.debug("values:{}", values);
        Assertions.assertEquals(testStep, values.size());
        Assertions.assertEquals(values.size(), new HashSet<>(values).size());
    }

    @Test
    void getDiffTheadInStepTest() throws Exception {
        Set<Long> values = new ConcurrentSkipListSet<>();
        int testStep = step - 3;
        Thread thread1 = new Thread(() -> {
            try {
                for (int i = 0; i < testStep; i++) {
                    values.add(this.sequence.nextValue(sequenceName1));
                }
            } catch (SequenceException e) {
                throw new RuntimeException(e);
            }
        });
        Thread thread2 = new Thread(() -> {
            try {
                values.add(this.sequence.nextValue(sequenceName1));
            } catch (SequenceException e) {
                throw new RuntimeException(e);
            }
        });
        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();
        log.debug("values:{}", values);
        Assertions.assertEquals(testStep + 1, values.size());
        Assertions.assertEquals(values.size(), new HashSet<>(values).size());
    }

    @Test
    void getDiffTheadOutStepTest() throws Exception {
        Set<Long> values = new ConcurrentSkipListSet<>();
        int testStep = step + 3;
        Thread thread1 = new Thread(() -> {
            try {
                for (int i = 0; i < testStep; i++) {
                    values.add(this.sequence.nextValue(sequenceName1));
                }
            } catch (SequenceException e) {
                throw new RuntimeException(e);
            }
        });
        Thread thread2 = new Thread(() -> {
            try {
                values.add(this.sequence.nextValue(sequenceName1));
            } catch (SequenceException e) {
                throw new RuntimeException(e);
            }
        });
        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();
        log.debug("values:{}", values);
        Assertions.assertEquals(testStep + 1, values.size());
        Assertions.assertEquals(values.size(), new HashSet<>(values).size());
    }

    @Test
    void multiThreadValueTest() throws Exception {
        Set<Long> values1 = new ConcurrentSkipListSet<>();
        Set<Long> values2 = new ConcurrentSkipListSet<>();
        ExecutorService es = Executors.newFixedThreadPool(100);
        final CountDownLatch count = new CountDownLatch(1);
        AtomicInteger seqCnt = new AtomicInteger();
        int times = 100;
        for (int i = 0; i < times; i++) {
            int finalI = i;
            es.execute(() -> {
                String name;
                Set<Long> values;
                if (finalI % 2 == 0) {
                    name = sequenceName1;
                    values = values1;
                } else {
                    name = sequenceName2;
                    values = values2;
                }
                try {
                    count.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                try {
                    values.add(this.sequence.nextValue(name));
                    seqCnt.getAndIncrement();
                } catch (SequenceException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        count.countDown();
        while (seqCnt.get() < times) {
            TimeUnit.MICROSECONDS.sleep(10);
        }
        Assertions.assertEquals(times, values1.size() + values2.size());
        Assertions.assertEquals(values1.size(), new HashSet<>(values1).size());
        Assertions.assertEquals(values2.size(), new HashSet<>(values2).size());
    }

    @Test
    void getNextValueListTest() throws Exception {
        long value = this.sequence.nextValue(sequenceName1);
        int listStep = 25;
        List<Long> list = this.sequence.nextValueList(sequenceName1, listStep);
        long nextValue = this.sequence.nextValue(sequenceName1);
        ObjectMapper objectMapper = new ObjectMapper();
        log.debug("value:{}, list:{}, nextValue:{}", value, objectMapper.writeValueAsString(list), nextValue);
        Assertions.assertEquals(listStep, list.size());
        Assertions.assertEquals(list.size(), new HashSet<>(list).size());
        Assertions.assertEquals(value + 1 + listStep, nextValue);
    }

    @Test
    void multiThreadValueListTest() throws Exception {
        Set<Long> values1 = new ConcurrentSkipListSet<>();
        Set<Long> values2 = new ConcurrentSkipListSet<>();
        ExecutorService es = Executors.newFixedThreadPool(100);
        final CountDownLatch count = new CountDownLatch(1);
        AtomicInteger seqCnt = new AtomicInteger();
        int times = 100;
        long sum = 0L;
        List<Integer> stepList = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < times; i++) {
            //1-100
            int randomNumber = random.nextInt(100) + 1;
            stepList.add(randomNumber);
            sum += randomNumber;
        }
        log.debug("stepList:{}", stepList);
        log.debug("sum:{}", sum);
        for (int i = 0; i < times; i++) {
            int finalI = i;
            int listStep = stepList.get(i);
            es.execute(() -> {
                String name;
                Set<Long> values;
                if (finalI % 2 == 0) {
                    name = sequenceName1;
                    values = values1;
                } else {
                    name = sequenceName2;
                    values = values2;
                }
                try {
                    log.debug("count.await()");
                    count.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                try {
                    log.debug("values.addAll(this.sequence.nextValueList(name, listStep))");
                    values.addAll(this.sequence.nextValueList(name, listStep));
                    log.debug("seqCnt.getAndIncrement()");
                    seqCnt.getAndIncrement();
                } catch (SequenceException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        log.debug("count.countDown()");
        count.countDown();
        while (seqCnt.get() < times) {
            TimeUnit.MICROSECONDS.sleep(10);
        }
        Assertions.assertEquals(sum, values1.size() + values2.size());
        Assertions.assertEquals(values1.size(), new HashSet<>(values1).size());
        Assertions.assertEquals(values2.size(), new HashSet<>(values2).size());
    }

    @Test
    void multiThreadCompareListTest() throws Exception {
        int times = 100;
        List<Long> list1 = Collections.synchronizedList(new ArrayList<>());
        AtomicLong listCount1 = new AtomicLong(0L);
        List<Long> list2 = Collections.synchronizedList(new ArrayList<>());
        AtomicLong listCount2 = new AtomicLong(0L);
        ExecutorService es = Executors.newFixedThreadPool(times);
        final CountDownLatch count = new CountDownLatch(times);
        Random random = new Random();
        List<Integer> stepList = new ArrayList<>();
        long sum = 0L;
        for (int i = 0; i < times; i++) {
            //1-100
            int randomNumber = random.nextInt(100) + 1;
            stepList.add(randomNumber);
            sum += randomNumber;
        }
        for (int i = 0; i < times; i++) {
            int finalI = i;
            es.execute(() -> {
                String name;
                List<Long> list;
                int listStep = stepList.get(finalI);
                if (finalI % 2 == 0) {
                    name = sequenceName1;
                    list = list1;
                    listCount1.addAndGet(listStep);
                } else {
                    name = sequenceName2;
                    list = list2;
                    listCount2.addAndGet(listStep);
                }
                List<Long> values = new ArrayList<>();
                for (int j = 0; j < listStep; j++) {
                    try {
                        values.add(this.sequence.nextValue(name));
                    } catch (SequenceException e) {
                        throw new RuntimeException(e);
                    }
                }
                list.addAll(values);
                count.countDown();
            });
        }
        count.await();
        Assertions.assertEquals(list1.size(), listCount1.get());
        Assertions.assertEquals(list1.size(), new HashSet<>(list1).size());
        Assertions.assertEquals(list2.size(), listCount2.get());
        Assertions.assertEquals(list2.size(), new HashSet<>(list2).size());
        Assertions.assertEquals(sum, list1.size() + list2.size());
        Long min1 = Collections.min(list1);
        Long max1 = Collections.max(list1);
        Assertions.assertEquals(max1 - min1, listCount1.get() - 1);
        Long min2 = Collections.min(list2);
        Long max2 = Collections.max(list2);
        Assertions.assertEquals(max2 - min2, listCount2.get() - 1);
    }

    @Test
    void multiThreadSingleListTest() throws Exception {
        int times = 100;
        List<Long> list = Collections.synchronizedList(new ArrayList<>());
        AtomicLong listCount = new AtomicLong(0L);
        ExecutorService es = Executors.newFixedThreadPool(times);
        final CountDownLatch count = new CountDownLatch(times);
        Random random = new Random();
        List<Integer> stepList = new ArrayList<>();
        long sum = 0L;
        for (int i = 0; i < times; i++) {
            //1-100
            int randomNumber = random.nextInt(100) + 1;
            stepList.add(randomNumber);
            sum += randomNumber;
        }
        for (int i = 0; i < times; i++) {
            int finalI = i;
            es.execute(() -> {
                int listStep = stepList.get(finalI);
                listCount.addAndGet(listStep);
                List<Long> values = new ArrayList<>();
                for (int j = 0; j < listStep; j++) {
                    try {
                        values.add(this.sequence.nextValue(sequenceName1));
                    } catch (SequenceException e) {
                        throw new RuntimeException(e);
                    }
                }
                list.addAll(values);
                count.countDown();
            });
        }
        count.await();
        Assertions.assertEquals(list.size(), listCount.get());
        Assertions.assertEquals(list.size(), new HashSet<>(list).size());
        Assertions.assertEquals(sum, list.size());
        Long min = Collections.min(list);
        Long max = Collections.max(list);
        Assertions.assertEquals(max - min, sum - 1);
    }

    @Test
    void multiThreadSingleTest() throws Exception {
        int times = 100;
        List<Long> list = Collections.synchronizedList(new ArrayList<>());
        ExecutorService es = Executors.newFixedThreadPool(times);
        final CountDownLatch count = new CountDownLatch(times);
        for (int i = 0; i < times; i++) {
            es.execute(() -> {
                try {
                    list.add(this.sequence.nextValue(sequenceName1));
                } catch (SequenceException e) {
                    throw new RuntimeException(e);
                }
                count.countDown();
            });
        }
        count.await();
        Assertions.assertEquals(times, list.size());
        Assertions.assertEquals(list.size(), new HashSet<>(list).size());
        Long min = Collections.min(list);
        Long max = Collections.max(list);
        Assertions.assertEquals(times - 1, max - min);
    }

    void refreshSequenceValue() throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            String updateQuery1 = "update sequence set value=10000000 where name='tddl_test_seq'";
            int rowsAffected1 = statement.executeUpdate(updateQuery1);
            Assertions.assertEquals(1, rowsAffected1);

            String updateQuery2 = "update sequence set value=50000000 where name='tddl_test_seq1'";
            int rowsAffected2 = statement.executeUpdate(updateQuery2);
            Assertions.assertEquals(1, rowsAffected2);
        }
    }

    @Test
    void multiThreadListRepeatTest() throws Exception {
        this.refreshSequenceValue();
        int nThreads = 100;
        List<Long> list1 = Collections.synchronizedList(new ArrayList<>());
        List<Long> list2 = Collections.synchronizedList(new ArrayList<>());
        List<Long> errorList1 = Collections.synchronizedList(new ArrayList<>());
        List<Long> errorList2 = Collections.synchronizedList(new ArrayList<>());
        ExecutorService es = Executors.newFixedThreadPool(100);
        AtomicLong count = new AtomicLong(0L);
        for (int i = 0; i < nThreads; i++) {
            es.execute(() -> {
                for (int j = 0; j < 10; j++) {
                    count.getAndIncrement();
                    Random random = new Random();
                    int randomNumber = random.nextInt(10) + 1;
                    if (randomNumber % 2 == 0) {
                        for (int n = 0; n < randomNumber; n++) {
                            try {
                                long value = this.sequence.nextValue(sequenceName1);
                                if (value >= 10000000 && value <= 19999999) {
                                    list1.add(value);
                                } else {
                                    errorList1.add(value);
                                }
                            } catch (SequenceException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    } else {
                        for (int n = 0; n < randomNumber; n++) {
                            try {
                                long value = this.sequence.nextValue(sequenceName2);
                                if (value >= 50000000 && value <= 59999999) {
                                    list2.add(value);
                                } else {
                                    errorList2.add(value);
                                }
                            } catch (SequenceException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
            });
        }
        es.shutdown();
        boolean b = es.awaitTermination(10, TimeUnit.HOURS);
        Assertions.assertTrue(b);
        log.debug("次数:{}", count.get());
        log.debug("errorList1:{}", errorList1);
        log.debug("errorList2:{}", errorList2);
        Assertions.assertEquals(0, errorList1.size());
        Assertions.assertEquals(0, errorList2.size());
        Assertions.assertEquals(list1.size(), new HashSet<>(list1).size());
        Assertions.assertEquals(list2.size(), new HashSet<>(list2).size());
    }

}
