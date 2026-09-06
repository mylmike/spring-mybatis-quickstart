package com.itheima.mapper.second;

import org.apache.ibatis.jdbc.SQL;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * sfahuc_t 相关动态 SQL 构造器
 * 空值约定：可选条件参数为 null / 空串 / 纯空白 时，视为该项条件未设置，不拼接 where 条件
 */
public class SfahucSqlProvider {

    /** 可选查询条件（顺序即 where 拼接顺序），前端未传 / 空值 时不拼接 */
    private static final List<String> CONDITION_FIELDS = Arrays.asList(
            "sfahucdocno",   // 单号
            "sfahuc008",     // 成本中心
            "sfahuc004",     // 订单号
            "sfahuc005",     // 订单序号
            "sfahuc001"      // 工单号
    );

    /**
     * 按条件查询 sfahuc_t 明细
     * 固定条件：
     *   sfahucent 账套，数值型，Controller 保证有值（默认 60）
     *   sfahucsite 营运据点，Controller 保证有值（默认 NBYL）
     * 可选条件（CONDITION_FIELDS）：
     *   null / 空串 / 纯空白 均视为条件未设置，不拼接 where
     * 固定排序：order by sfahucseq
     */
    public String listByDocno(final Map<String, Object> params) {
        return new SQL() {{
            SELECT("*");
            FROM("sfahuc_t");
            // 固定条件：账套（数值型）、营运据点
            WHERE("sfahucent = TO_NUMBER(#{sfahucent})");
            WHERE("sfahucsite = #{sfahucsite}");
            // 可选条件
            for (String field : CONDITION_FIELDS) {
                if (hasText(params.get(field))) {
                    WHERE(field + " = #{" + field + "}");
                }
            }
            ORDER_BY("sfahucseq");
        }}.toString();
    }

    /** 判断参数是否为有效非空文本（null / 空串 / 纯空白 均返回 false） */
    private boolean hasText(Object val) {
        return val != null && !String.valueOf(val).trim().isEmpty();
    }
}
