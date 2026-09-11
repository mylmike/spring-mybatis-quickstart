package com.itheima.mapper.second;

import org.apache.ibatis.jdbc.SQL;

import java.util.Map;

/**
 * 品号基本资料 imaf_t 查询 SQL 构造器
 */
public class ItemImSqlProvider {

    /**
     * 按 账套 + 据点 + 品号 查询补货策略 imaf013
     * 参数：ent(必填)、site(必填)、item(必填，即 imaf001)
     */
    public String queryByItem(final Map<String, Object> params) {
        return new SQL() {{
            SELECT("imaf013");
            FROM("imaf_t");
            WHERE("imafent = #{ent}");
            WHERE("imafsite = #{site}");
            WHERE("imaf001 = #{item}");
        }}.toString();
    }
}
