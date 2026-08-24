package com.itheima.mapper.second;

import org.apache.ibatis.jdbc.SQL;

import java.util.Map;

/**
 * ooefl_t + ooeg_t 联合查询 SQL 构造器
 */
public class OoeflSqlProvider {

    public String queryDept(final Map<String, Object> params) {
        // 语言默认 zh_CN
        String lang = "zh_CN";
        if (params.containsKey("ooefl002") && params.get("ooefl002") != null) {
            String v = params.get("ooefl002").toString().trim();
            if (!v.isEmpty()) {
                lang = v;
            }
        }
        params.put("_lang", lang);

        // 部门名称模糊匹配
        if (params.containsKey("ooefl003") && params.get("ooefl003") != null) {
            String v = params.get("ooefl003").toString().trim();
            if (!v.isEmpty()) {
                params.put("_ooefl003Like", "%" + v + "%");
            }
        }

        return new SQL() {{
            SELECT("a.ooefl001, a.ooefl003");
            FROM("ooefl_t a");
            LEFT_OUTER_JOIN("ooeg_t b ON b.ooegent = a.ooeflent AND b.ooeg001 = a.ooefl001");
            WHERE("b.ooegent = 60");
            WHERE("b.ooeg009 = 'NBYL'");
            WHERE("b.ooegstus = 'Y'");
            WHERE("a.ooefl002 = #{_lang}");
            if (params.containsKey("ooefl001")
                    && params.get("ooefl001") != null
                    && !params.get("ooefl001").toString().trim().isEmpty()) {
                WHERE("a.ooefl001 = #{ooefl001}");
            }
            if (params.containsKey("_ooefl003Like")) {
                WHERE("a.ooefl003 LIKE #{_ooefl003Like}");
            }
            // 责任中心类型（ooeg_t 的 ooeg003），前端传值才加条件
            if (params.containsKey("ooeg003")
                    && params.get("ooeg003") != null
                    && !params.get("ooeg003").toString().trim().isEmpty()) {
                WHERE("b.ooeg003 = #{ooeg003}");
            }
        }}.toString();
    }

    /**
     * auth 认证成功后查询用户姓名与所属部门
     * 入参: username(ooag001 登录账号)
     * 返回: ooag011(姓名), ooag003(部门编号), ooefl003(部门名称)
     */
    public String queryAuthDept(final Map<String, Object> params) {
        return new SQL() {{
            SELECT("a.ooag011 \"userName\"");
            SELECT("a.ooag003 \"deptNo\"");
            SELECT("b.ooefl003 \"deptName\"");
            FROM("ooag_t a");
            LEFT_OUTER_JOIN("ooefl_t b ON b.ooeflent = a.ooagent AND b.ooefl001 = a.ooag003 AND b.ooefl002 = 'zh_CN'");
            WHERE("a.ooagent = 60");
            WHERE("a.ooag001 = #{username}");
        }}.toString();
    }
}
