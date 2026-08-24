package com.itheima.mapper.second;

import org.apache.ibatis.jdbc.SQL;

import java.util.Map;

/**
 * ooeluc_t 产线与成本中心对应表动态 SQL 构造器
 * ooeluc003 产线名, ooeluc004 成本中心编码
 */
public class OoelucSqlProvider {

    private static final String TABLE = "ooeluc_t";

    /**
     * 按条件查询产线名和成本中心
     * 固定：ooelucent(账套)、ooelucsite(据点)，默认由前端传入（Controller 兜底 60/NBYL）
     * 可选：ooeluc003(产线名，模糊匹配)、ooeluc004(成本中心编码，精确匹配)
     */
    public String query(final Map<String, Object> params) {
        // 产线名模糊匹配
        if (params.containsKey("ooeluc003") && params.get("ooeluc003") != null) {
            String v = params.get("ooeluc003").toString().trim();
            if (!v.isEmpty()) {
                params.put("_ooeluc003Like", "%" + v + "%");
            }
        }

        return new SQL() {{
            SELECT("ooelucent, ooelucsite, ooeluc003, ooeluc004");
            FROM(TABLE);
            if (params.containsKey("ooelucent")
                    && params.get("ooelucent") != null
                    && !params.get("ooelucent").toString().trim().isEmpty()) {
                WHERE("ooelucent = #{ooelucent}");
            }
            if (params.containsKey("ooelucsite")
                    && params.get("ooelucsite") != null
                    && !params.get("ooelucsite").toString().trim().isEmpty()) {
                WHERE("ooelucsite = #{ooelucsite}");
            }
            if (params.containsKey("_ooeluc003Like")) {
                WHERE("ooeluc003 LIKE #{_ooeluc003Like}");
            }
            if (params.containsKey("ooeluc004")
                    && params.get("ooeluc004") != null
                    && !params.get("ooeluc004").toString().trim().isEmpty()) {
                WHERE("ooeluc004 = #{ooeluc004}");
            }
            ORDER_BY("ooeluc001, ooeluc004, ooeluc003");
        }}.toString();
    }
}
