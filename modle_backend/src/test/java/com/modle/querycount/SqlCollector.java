package com.modle.querycount;

import org.hibernate.resource.jdbc.spi.StatementInspector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SqlCollector implements StatementInspector {
    public static final List<String> SQL = Collections.synchronizedList(new ArrayList<>());

    @Override
    public String inspect(String sql) {
        SQL.add(sql);
        return sql;
    }
}
