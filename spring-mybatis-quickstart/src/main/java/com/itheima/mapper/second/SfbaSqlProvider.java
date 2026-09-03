package com.itheima.mapper.second;

import org.apache.ibatis.jdbc.SQL;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * sfba_t LEFT JOIN sfaa_t 工单查询 动态 SQL 构造器
 * 空值约定：条件参数为 null / 空串 / 纯空白 时，视为该项条件未设置，不拼接 where 条件
 */
public class SfbaSqlProvider {

    /** 前端未传 row_max 或传入非法值时，使用的默认返回行数 */
    private static final int DEFAULT_ROW_MAX = 200;

    /** 允许作为查询条件的字段（顺序即 where 拼接顺序） */
    private static final List<String> CONDITION_FIELDS = Arrays.asList(
            // 单头 sfaa_t
            "sfbaent", "sfaasite", "sfaadocno", "sfaastus",
            "sfaa010", "sfaa012", "sfaa068", "sfaadocdt",
            "sfaa019", "sfaa020", "sfaa022", "sfaa023", "sfaa050", "sfaa047",
            // 单身 sfba_t
            "sfbaseq", "sfba006", "sfba023", "sfba024",
            "sfba013", "sfba017", "sfba025", "sfba009", "sfba028"
    );

    /**
     * 工单查询（单头+单身）
     * 所有条件均可选，null / 空串 / 纯空白 不拼接 where
     * row_max：未传 / <=0 / 非法 时取默认值 200
     * 固定排序：order by sfaadocdt desc（内层先排好序，外层 rownum 截断才有确定语义）
     */
    public String queryWorkOrder(final Map<String, Object> params) {
        final int rowMax = parseRowMax(params.get("row_max"));

        SQL inner = new SQL() {{
            // 注意：MyBatis SQL 会自动在多个 SELECT() 片段之间补逗号，片段末尾不能再写逗号
            SELECT("sfbaent, sfaasite, sfaadocno, sfaastus, sfaa010, sfaa012, sfaa068");
            SELECT("sfaadocdt, sfaa019, sfaa020, sfaa022, sfaa023, sfaa050, sfaa047");
            SELECT("sfbaseq, sfba006, sfba023, sfba024, sfba013, sfba017, sfba025, sfba009, sfba028");
            FROM("sfba_t");
            LEFT_OUTER_JOIN("sfaa_t on sfbaent = sfaaent and sfbasite = sfaasite and sfbadocno = sfaadocno");
            for (String field : CONDITION_FIELDS) {
                if (hasText(params.get(field))) {
                    WHERE(field + " = #{" + field + "}");
                }
            }
            ORDER_BY("sfaadocdt desc");
        }};

        return "select * from (" + inner.toString() + ") where rownum <= " + rowMax;
    }

    /** 判断参数是否为有效非空文本（null / 空串 / 纯空白 均返回 false） */
    private boolean hasText(Object val) {
        return val != null && !String.valueOf(val).trim().isEmpty();
    }

    /** 解析行数限制：未传 / 非法 / <=0 取 DEFAULT_ROW_MAX */
    private int parseRowMax(Object val) {
        if (val == null) {
            return DEFAULT_ROW_MAX;
        }
        try {
            int n = Integer.parseInt(String.valueOf(val).trim());
            return n > 0 ? n : DEFAULT_ROW_MAX;
        } catch (NumberFormatException e) {
            return DEFAULT_ROW_MAX;
        }
    }
}
