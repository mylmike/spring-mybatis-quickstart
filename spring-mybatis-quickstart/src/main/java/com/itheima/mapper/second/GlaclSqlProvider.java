package com.itheima.mapper.second;

import org.apache.ibatis.jdbc.SQL;

import java.util.Map;

/**
 * glacl_t + glac_t 联合查询 SQL 构造器
 */
public class GlaclSqlProvider {

    public String querySubject(final Map<String, Object> params) {
        // 语言默认 zh_CN
        String lang = "zh_CN";
        if (params.containsKey("glacl003") && params.get("glacl003") != null) {
            String v = params.get("glacl003").toString().trim();
            if (!v.isEmpty()) {
                lang = v;
            }
        }
        params.put("_lang", lang);

        // 科目名称模糊匹配
        if (params.containsKey("glacl004") && params.get("glacl004") != null) {
            String v = params.get("glacl004").toString().trim();
            if (!v.isEmpty()) {
                params.put("_glacl004Like", "%" + v + "%");
            }
        }

        return new SQL() {{
            SELECT("a.glacl002, a.glacl004");
            FROM("glacl_t a");
            LEFT_OUTER_JOIN("glac_t b ON b.glacent = a.glaclent AND a.glacl001 = b.glac001 AND a.glacl002 = b.glac002");
            WHERE("a.glaclent = 60");
            WHERE("a.glacl003 = #{_lang}");
            if (params.containsKey("glacl002")
                    && params.get("glacl002") != null
                    && !params.get("glacl002").toString().trim().isEmpty()) {
                WHERE("a.glacl002 = #{glacl002}");
            }
            if (params.containsKey("_glacl004Like")) {
                WHERE("a.glacl004 LIKE #{_glacl004Like}");
            }
            if (params.containsKey("glac007")
                    && params.get("glac007") != null
                    && !params.get("glac007").toString().trim().isEmpty()) {
                WHERE("b.glac007 = #{glac007}");
            }
            if (params.containsKey("glac003")
                    && params.get("glac003") != null
                    && !params.get("glac003").toString().trim().isEmpty()) {
                WHERE("b.glac003 = #{glac003}");
            }
        }}.toString();
    }
}
