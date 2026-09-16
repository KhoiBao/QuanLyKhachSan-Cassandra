package com.qlkhachsan.repository.cassandra;

import java.util.Arrays;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;

/**
 * Helper chay CQL - moi Store deu goi qua day de cau query hien ro
 * trong code VA in ra console (prefix [CQL]) phục vụ demo.
 */
final class Cql {

    private Cql() {
    }

    static ResultSet exec(CqlSession session, String cql, Object... args) {
        if (args == null || args.length == 0) {
            System.out.println("[CQL] " + cql);
            return session.execute(cql);
        }
        System.out.println("[CQL] " + cql + " | params=" + Arrays.toString(args));
        return session.execute(cql, args);
    }
}
