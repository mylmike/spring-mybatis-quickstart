package com.itheima.mapper.second;

import org.apache.ibatis.jdbc.SQL;

import java.util.Map;

/**
 * 用户权限查询（gzypuc_t）
 * 按账号返回其权限明细：功能菜单编号 / 权限 / 功能 / 权限部门
 * 固定条件：gzypucent(账套, 默认 60) + gzypucld(据点, 默认 NBYL)
 * 必填条件：gzypuc001(用户账号，前端传入)
 */
public class GzypucSqlProvider {

    /**
     * 根据用户账号查询权限列表
     * 参数（Map）：gzypucent(默认60)、gzypucld(默认NBYL)、gzypuc001(用户账号,必填)
     */
    public String queryByUser(final Map<String, Object> params) {
        return new SQL() {{
            SELECT("gzypuc002, gzypuc003, gzypuc004, gzypuc005");
            FROM("gzypuc_t");
            WHERE("gzypucent = #{gzypucent}");
            WHERE("gzypucld = #{gzypucld}");
            WHERE("gzypuc001 = #{gzypuc001}");
        }}.toString();
    }
}
