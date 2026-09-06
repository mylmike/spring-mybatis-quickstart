package com.itheima.mapper.second;

import org.apache.ibatis.jdbc.SQL;

import java.util.Map;

/**
 * sfahuc_t 删除语句动态构造器
 * 条件约定：
 *   sfahucent(账套)、sfahucsite(营运据点)：固定条件，Controller 保证有值（默认 60 / NBYL）
 *   sfahuc001、sfahuc002：必填，Controller 已校验
 *   sfahuc008(成本中心)：可选，null / 空串 / 纯空白 时不拼接
 */
public class SfahucDeleteSqlProvider {

    public String deleteByKey(final Map<String, Object> params) {
        return new SQL() {{
            DELETE_FROM("sfahuc_t");
            WHERE("sfahucent = TO_NUMBER(#{sfahucent})");
            WHERE("sfahucsite = #{sfahucsite}");
            WHERE("sfahuc001 = #{sfahuc001}");
            WHERE("sfahuc002 = #{sfahuc002}");
            if (hasText(params.get("sfahuc008"))) {
                WHERE("sfahuc008 = #{sfahuc008}");
            }
        }}.toString();
    }

    /** 判断参数是否为有效非空文本（null / 空串 / 纯空白 均返回 false） */
    private boolean hasText(Object val) {
        return val != null && !String.valueOf(val).trim().isEmpty();
    }
}
