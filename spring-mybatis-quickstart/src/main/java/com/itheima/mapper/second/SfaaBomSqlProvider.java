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

    /** 判断参数是否为有效非空文本（null / 空串 / 纯空白 均返回 false） */
    private boolean hasText(Object val) {
        return val != null && !String.valueOf(val).trim().isEmpty();
    }
}
