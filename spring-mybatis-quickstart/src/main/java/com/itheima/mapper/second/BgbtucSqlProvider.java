package com.itheima.mapper.second;

import org.apache.ibatis.jdbc.SQL;

import java.util.List;
import java.util.Map;

/**
 * bgbtuc_t 采购价格预算表动态 SQL 构造器
 */
public class BgbtucSqlProvider {

    private static final String TABLE = "bgbtuc_t";
    private static final String PK_FIELDS_KEY = "pkFields";

    /**
     * 按主键查询是否存在
     */
    public String findByPk(final Map<String, Object> params) {
        final List<String> pkFields = (List<String>) params.get(PK_FIELDS_KEY);
        return new SQL() {{
            SELECT("*");
            FROM(TABLE);
            for (String field : pkFields) {
                if (params.containsKey(field) && params.get(field) != null) {
                    WHERE(field + " = #{" + field + "}");
                } else {
                    WHERE(field + " IS NULL");
                }
            }
        }}.toString();
    }

    /**
     * 动态插入（只插入非 null 字段）
     */
    public String insert(final Map<String, Object> params) {
        return new SQL() {{
            INSERT_INTO(TABLE);
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                String key = entry.getKey();
                if (PK_FIELDS_KEY.equals(key)) continue;
                if ("token".equalsIgnoreCase(key)) continue;
                if (entry.getValue() == null) continue;
                VALUES(key, "#{" + key + "}");
            }
        }}.toString();
    }

    /**
     * 动态更新（主键字段同时用于 where 和 set，保证字段同步）
     */
    public String update(final Map<String, Object> params) {
        final List<String> pkFields = (List<String>) params.get(PK_FIELDS_KEY);
        return new SQL() {{
            UPDATE(TABLE);
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                String key = entry.getKey();
                if (PK_FIELDS_KEY.equals(key)) continue;
                if ("token".equalsIgnoreCase(key)) continue;
                if (entry.getValue() == null) continue;
                SET(key + " = #{" + key + "}");
            }
            for (String pk : pkFields) {
                if (params.containsKey(pk) && params.get(pk) != null) {
                    WHERE(pk + " = #{" + pk + "}");
                } else {
                    WHERE(pk + " IS NULL");
                }
            }
        }}.toString();
    }

    /**
     * 动态查询（前端传入的非空字段均作为 where 条件）
     * 关联 imaal_t 取品名(imaal003)、规格(imaal004)
     */
    public String query(final Map<String, Object> params) {
        return new SQL() {{
            SELECT("b.*");
            SELECT("m.imaal003 AS imaal003");
            SELECT("m.imaal004 AS imaal004");
            FROM(TABLE + " b");
            LEFT_OUTER_JOIN("imaal_t m ON m.imaalent = b.bgbtucent AND m.imaal002 = 'zh_CN' AND m.imaal001 = b.bgbtuc001");
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                String key = entry.getKey();
                if ("token".equalsIgnoreCase(key)) continue;
                Object val = entry.getValue();
                if (val == null) continue;
                if (val instanceof String && ((String) val).trim().isEmpty()) continue;
                WHERE("b." + key + " = #{" + key + "}");
            }
        }}.toString();
    }

    /**
     * 按主键删除
     */
    public String deleteByPk(final Map<String, Object> params) {
        final List<String> pkFields = (List<String>) params.get(PK_FIELDS_KEY);
        return new SQL() {{
            DELETE_FROM(TABLE);
            for (String field : pkFields) {
                if (params.containsKey(field) && params.get(field) != null) {
                    WHERE(field + " = #{" + field + "}");
                } else {
                    WHERE(field + " IS NULL");
                }
            }
        }}.toString();
    }

    /**
     * 按条件删除（前端传入的非空字段均作为 where 条件）
     */
    public String deleteByCondition(final Map<String, Object> params) {
        return new SQL() {{
            DELETE_FROM(TABLE);
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                String key = entry.getKey();
                if ("token".equalsIgnoreCase(key)) continue;
                Object val = entry.getValue();
                if (val == null) continue;
                if (val instanceof String && ((String) val).trim().isEmpty()) continue;
                WHERE(key + " = #{" + key + "}");
            }
        }}.toString();
    }
}
