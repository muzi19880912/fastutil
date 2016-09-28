package org.fastutil.mainland.util.db;

import org.fastutil.mainland.util.db.annotation.SelectDataBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.datasource.AbstractDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DBDataSource extends AbstractDataSource implements InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(DBDataSource.class);
    private SingleReadWriteSwitch defaultReadWriteSwitch;
    private Object readWriteSwitch;
    private String db = SelectDataBase.DataBase.DB_0.toString();

    private static SelectDataBase.DataBase DEFAULT_DB = SelectDataBase.DataBase.DB_0;

    public static SelectDataBase.DataBase getDefaultDB() {
        return DBDataSource.DEFAULT_DB;
    }

    public void setDb(String db) {
        if (db != null && db.trim().length() > 0) {
            try {
                DBDataSource.DEFAULT_DB = SelectDataBase.DataBase.valueOf(db.trim().toUpperCase());
                this.db = db.trim().toUpperCase();
            } catch (Exception e) {
                throw new IllegalArgumentException("property 'db' is required!DB_0 or DB_1 ... DB_31");
            }
        }
    }

    public String getDb() {
        return db;
    }

    public void setDefaultReadWriteSwitch(SingleReadWriteSwitch defaultReadWriteSwitch) {
        this.defaultReadWriteSwitch = defaultReadWriteSwitch;
    }

    public void setReadWriteSwitch(Object readWriteSwitch) {
        if (readWriteSwitch instanceof SingleReadWriteSwitch) {
            this.readWriteSwitch = readWriteSwitch;
        } else if (readWriteSwitch instanceof MultiReadWriteSwitch) {
            this.readWriteSwitch = readWriteSwitch;
        } else {
            throw new IllegalArgumentException(
                    "property 'readWriteSwitch' is required!SingleReadWriteSwitch?MultiReadWriteSwitch?");
        }
    }

    public void afterPropertiesSet() throws Exception {
        if (readWriteSwitch == null) {
            throw new IllegalArgumentException("property 'readWriteSwitch' is required");
        }
    }

    private DataSource determineDataSource() {
        SelectDataBase selectDataBase = Decision.getDataBaseDecision().get();
        if (selectDataBase == null) {
            throw new IllegalArgumentException(Thread.currentThread().getName() + " SelectDataBase is null");
        }

        SingleReadWriteSwitch srws = null;

        if (readWriteSwitch != null) {
            if (readWriteSwitch instanceof SingleReadWriteSwitch) {
                srws = (SingleReadWriteSwitch) readWriteSwitch;
            } else if (readWriteSwitch instanceof MultiReadWriteSwitch) {
                SelectDataBase.DataBase db = selectDataBase.value();
                if (db != null) {
                    srws = ((MultiReadWriteSwitch) readWriteSwitch).getMultiReadWriteSwitchMap()
                            .get(db.toString());
                    if (srws == null && selectDataBase.defaultValue() != null && db != selectDataBase.defaultValue()) {
                        srws = ((MultiReadWriteSwitch) readWriteSwitch).getMultiReadWriteSwitchMap()
                                .get(selectDataBase.defaultValue().toString());
                        log.debug("{} is null,select default {}", selectDataBase.value(), selectDataBase.defaultValue());
                    }
                } else if ((db = selectDataBase.defaultValue()) != null) {
                    srws = ((MultiReadWriteSwitch) readWriteSwitch).getMultiReadWriteSwitchMap()
                            .get(db.toString());
                    log.debug("{} is null,select default {}", selectDataBase.value(), selectDataBase.defaultValue());
                }
            } else {
                throw new IllegalArgumentException(
                        "property 'readWriteSwitch or defaultReadWriteSwitch' is required!SingleReadWriteSwitch?MultiReadWriteSwitch?SelectDataBase.DataBase="
                                + selectDataBase.value());
            }
        } else {
            srws = defaultReadWriteSwitch;
        }

        if (srws != null) {
            boolean readOnly = Decision.getReadOnlyDecision().get();
            log.debug("current Database={},readOnly={}", toString(selectDataBase), readOnly);
            return readOnly ? srws.selectRead() : srws.selectWrite();
        } else {
            throw new IllegalArgumentException(
                    "property 'readWriteSwitch or defaultReadWriteSwitch' is required!SingleReadWriteSwitch?MultiReadWriteSwitch?SelectDataBase.DataBase="
                            + toString(selectDataBase));
        }
    }

    public Connection getConnection() throws SQLException {
        return determineDataSource().getConnection();
    }

    public Connection getConnection(String username, String password) throws SQLException {
        return determineDataSource().getConnection(username, password);
    }

    public static String toString(SelectDataBase selectDataBase) {
        return selectDataBase == null ? null
                : (new StringBuilder(32).append(selectDataBase.value()).append(" default ")
                .append(selectDataBase.defaultValue())).toString();
    }

}
