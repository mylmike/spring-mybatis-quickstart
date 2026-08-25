package com.itheima.mapper.second;

import org.apache.ibatis.jdbc.SQL;

import java.util.Map;

/**
 * 管理系统菜单目录 gzweuc_t 动态 SQL 构造器
 */
public class GzweucSqlProvider {

    /**
     * 按账套查询菜单目录树原始数据
     * 参数：gzweucent（默认 60）
     * 返回字段：gzweuc001, gzweuc002, gzweuc003, gzweuc004
     */
    public String queryMenuTree(final Map<String, Object> params) {
        return new SQL() {{
            SELECT("gzweuc001, gzweuc002, gzweuc003, gzweuc004");
            FROM("gzweuc_t");
            WHERE("gzweucent = #{gzweucent}");
            // 按上级目录编号、显示顺序排序，便于前端直接按顺序构建树
            ORDER_BY("gzweuc001, gzweuc003");
        }}.toString();
    }
}
