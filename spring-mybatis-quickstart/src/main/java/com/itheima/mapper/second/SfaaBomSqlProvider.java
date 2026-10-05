package com.itheima.mapper.second;

import org.apache.ibatis.jdbc.SQL;

import java.util.Map;

/**
 * BOM 展开时关联工单主表 sfaa_t 的动态 SQL 构造器
 * 条件：sfaaent(账套) + sfaasite(据点) + sfaa022(来源单号) + sfaa023(来源序号)
 *       返回行含 sfaa010(生产料号)，由 Java 侧按 sfaa010 建索引，与 BOM 元件的 bmba003 匹配
 * 日期字段统一 TO_CHAR，避免返回时间戳
 */
public class SfaaBomSqlProvider {

    public String queryByOrder(final Map<String, Object> params) {
        return new SQL() {{
            SELECT("sfaa010");
            SELECT("sfaadocno");
            SELECT("sfaa068");
            SELECT("TO_CHAR(sfaa019,'YYYY-MM-DD') AS sfaa019");
            SELECT("TO_CHAR(sfaa020,'YYYY-MM-DD') AS sfaa020");
            SELECT("sfaa012");
            SELECT("sfaa050");
            SELECT("sfaa049");   // 已发料套数
            SELECT("sfaastus");
            // 关联成本中心描述 ooefl_t，取成本中心名称 ooefl003（按语言 lang 匹配）
            SELECT("o.ooefl003 AS ooefl003");
            FROM("sfaa_t");
            LEFT_OUTER_JOIN("ooefl_t o ON o.ooeflent = sfaaent AND o.ooefl001 = sfaa068 AND o.ooefl002 = #{lang}");
            WHERE("sfaaent = TO_NUMBER(#{ent})");
            WHERE("sfaasite = #{site}");
            if (hasText(params.get("orderNo"))) {
                WHERE("sfaa022 = #{orderNo}");
            }
            if (hasText(params.get("orderSeq"))) {
                WHERE("sfaa023 = #{orderSeq}");
            }
            // 保证同一品号存在多张工单时，取到的第一条是确定的
            ORDER_BY("sfaadocno");
        }}.toString();
    }

    /**
     * 关联「工单计划排程档」sfahuc_t 取工单信息（优先级高于直接关联 sfaa_t 的 queryByOrder）。
     * 逻辑：按订单号(sfahuc004) + 订单序号(sfahuc005) 在 sfahuc_t 中定位排程记录，
     *       再用 sfahuc001(工单号) 关联 sfaa_t 取工单主信息（sfaa010 生产品号、sfaa012 生产数量等）。
     * 与 queryByOrder 返回字段一致，便于 Java 侧统一按 sfaa010 建索引、共用 attachSfaaInfo。
     * 条件：主表 sfahuc_t 的 sfahucent(账套) + sfahucsite(据点) 固定；订单号/序号可选（空则不限）。
     *       （账套/据点放在主表过滤，避免 sfaa_t 左连接未匹配时整行被过滤掉）
     */
    public String queryBySchedule(final Map<String, Object> params) {
        return new SQL() {{
            SELECT("sfaa010");
            SELECT("sfaadocno");
            SELECT("sfaa068");
            SELECT("TO_CHAR(sfaa019,'YYYY-MM-DD') AS sfaa019");
            SELECT("TO_CHAR(sfaa020,'YYYY-MM-DD') AS sfaa020");
            SELECT("sfaa012");
            SELECT("sfaa050");
            SELECT("sfaa049");   // 已发料套数
            SELECT("sfaastus");
            // 关联成本中心描述 ooefl_t，取成本中心名称 ooefl003（按语言 lang 匹配）
            SELECT("o.ooefl003 AS ooefl003");
            FROM("sfahuc_t");
            LEFT_OUTER_JOIN("sfaa_t ON sfaaent = sfahucent AND sfaadocno = sfahuc001");
            LEFT_OUTER_JOIN("ooefl_t o ON o.ooeflent = sfahucent AND o.ooefl001 = sfaa068 AND o.ooefl002 = #{lang}");
            WHERE("sfahucent = TO_NUMBER(#{ent})");
            WHERE("sfahucsite = #{site}");
            if (hasText(params.get("orderNo"))) {
                WHERE("sfahuc004 = #{orderNo}");
            }
            if (hasText(params.get("orderSeq"))) {
                WHERE("sfahuc005 = #{orderSeq}");
            }
            // 保证同一品号存在多张工单时，取到的顺序确定，结果稳定
            ORDER_BY("sfaa010, sfaadocno");
        }}.toString();
    }

    /** 判断参数是否为有效非空文本（null / 空串 / 纯空白 均返回 false） */
    private boolean hasText(Object val) {
        return val != null && !String.valueOf(val).trim().isEmpty();
    }
}
