package com.yulaiz.tddl.sequence.service.impl;

import com.yulaiz.tddl.sequence.exception.SequenceException;
import com.yulaiz.tddl.sequence.service.SequenceDao;
import com.yulaiz.tddl.sequence.vo.SequenceRange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Lazy, @Autowired})
public class DefaultSequenceDao implements SequenceDao {
    private final DataSource dataSource;
    /**
     * 重试次数
     */
    private static final int DEFAULT_RETRY_TIMES = 150;
    /**
     * 序列所在的表名
     */
    @Value("${sequence.structure.table}")
    private String DEFAULT_TABLE_NAME;
    /**
     * 存储序列名称的列名
     */
    @Value("${sequence.structure.name}")
    private String DEFAULT_NAME_COLUMN_NAME;
    /**
     * 存储序列步长的列名
     */
    @Value("${sequence.structure.step}")
    private String DEFAULT_STEP_COLUMN_STEP;
    /**
     * 存储序列值的列名
     */
    @Value("${sequence.structure.value}")
    private String DEFAULT_VALUE_COLUMN_NAME;
    /**
     * 存储序列最后更新时间的列名
     */
    @Value("${sequence.structure.modified}")
    private String DEFAULT_GMT_MODIFIED_COLUMN_NAME;

    private static final long DELTA = 100000000L;

    private volatile String selectSql;
    private volatile String updateSql;

    /**
     * 取得下一个可用的序列区间
     *
     * @param sequenceName 序列名称
     * @return 返回下一个可用的序列区间
     * @throws SequenceException 获取序列失败
     */
    @Override
    public SequenceRange nextRange(String sequenceName) throws SequenceException {
        if (sequenceName == null) {
            throw new IllegalArgumentException("序列名称不能为空");
        }
        long oldValue;
        long newValue;
        int step;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        for (int i = 0; i < DEFAULT_RETRY_TIMES + 1; ++i) {
            try {
                conn = dataSource.getConnection();
                try {
                    stmt = conn.prepareStatement(getSelectSql());
                    stmt.setString(1, sequenceName);
                    rs = stmt.executeQuery();
                    if (!rs.next()) {
                        String message = "Sequence does not exist, " +
                                "please check table " + DEFAULT_TABLE_NAME;
                        throw new SequenceException(message);
                    }
                    oldValue = rs.getLong(1);
                    step = rs.getInt(2);
                    if (oldValue < 0) {
                        String message = "Sequence value cannot be less than zero, " +
                                "value = " + oldValue + ", " +
                                "please check table " + DEFAULT_TABLE_NAME;
                        throw new SequenceException(message);
                    }

                    if (oldValue > Long.MAX_VALUE - DELTA) {
                        String message = "Sequence value overflow, " +
                                "value = " + oldValue + ", " +
                                "please check table " + DEFAULT_TABLE_NAME;
                        throw new SequenceException(message);
                    }
                    newValue = oldValue + step;
                } finally {
                    closeResultSet(rs);
                    rs = null;
                    closeStatement(stmt);
                    stmt = null;
                }

                try {
                    stmt = conn.prepareStatement(getUpdateSql());
                    stmt.setLong(1, newValue);
                    stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                    stmt.setString(3, sequenceName);
                    stmt.setLong(4, oldValue);
                    int affectedRows = stmt.executeUpdate();
                    if (affectedRows == 0) {
                        // retry
                        continue;
                    }
                    return new SequenceRange(oldValue + 1, newValue);
                } finally {
                    closeStatement(stmt);
                    stmt = null;
                }
            } catch (SQLException e) {
                throw new SequenceException(e);
            } finally {
                closeConnection(conn);
                conn = null;
            }
        }
        throw new SequenceException("Retried too many times, retryTimes = " + DEFAULT_RETRY_TIMES);
    }

    private String getSelectSql() {
        if (selectSql == null) {
            synchronized (this) {
                if (selectSql == null) {
                    selectSql = "select " + DEFAULT_VALUE_COLUMN_NAME
                            + ", " + DEFAULT_STEP_COLUMN_STEP +
                            " from " + DEFAULT_TABLE_NAME +
                            " where " + DEFAULT_NAME_COLUMN_NAME + " = ?" +
                            " limit 1";
                }
            }
        }
        return selectSql;
    }

    private String getUpdateSql() {
        if (updateSql == null) {
            synchronized (this) {
                if (updateSql == null) {
                    updateSql = "update " + DEFAULT_TABLE_NAME +
                            " set " + DEFAULT_VALUE_COLUMN_NAME + " = ?" +
                            ", " + DEFAULT_GMT_MODIFIED_COLUMN_NAME + " = ? " +
                            "where " + DEFAULT_NAME_COLUMN_NAME + " = ? " +
                            "and " + DEFAULT_VALUE_COLUMN_NAME + " = ?";
                }
            }
        }
        return updateSql;
    }

    private static void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                log.debug("Could not close JDBC ResultSet", e);
            } catch (Throwable e) {
                log.debug("Unexpected exception on closing JDBC ResultSet", e);
            }
        }
    }

    private static void closeStatement(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                log.debug("Could not close JDBC Statement", e);
            } catch (Throwable e) {
                log.debug("Unexpected exception on closing JDBC Statement", e);
            }
        }
    }

    private static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                log.debug("Could not close JDBC Connection", e);
            } catch (Throwable e) {
                log.debug("Unexpected exception on closing JDBC Connection", e);
            }
        }
    }

}
