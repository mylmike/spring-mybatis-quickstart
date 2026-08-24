package com.itheima.mapper.second;

import org.apache.ibatis.jdbc.SQL;

import java.util.Map;

/**
 * 订单 BOM 递归展阶 SQL 构造器
 * 涉及表：xmdd_t（销售订单单身）、bmaa_t（BOM 单头）、bmba_t（BOM 单身）、bmbb_t（BOM 损耗）
 */
public class BomSqlProvider {

    /**
     * 查询销售订单单身品号、数量、单价
     * 参数：ent(必填)、site(必填)、xmdddocno(可选)、xmdd001(可选)
     */
    public String queryXmddItems(final Map<String, Object> params) {
        return new SQL() {{
            SELECT("xmdd001, xmdd005, xmdd014, xmdddocno, xmddseq");
            FROM("xmdd_t");
            WHERE("xmddent = #{ent}");
            WHERE("xmddsite = #{site}");
            Object docNo = params.get("xmdddocno");
            if (docNo != null && !String.valueOf(docNo).trim().isEmpty()) {
                WHERE("xmdddocno = #{xmdddocno}");
            }
            Object itemNo = params.get("xmdd001");
            if (itemNo != null && !String.valueOf(itemNo).trim().isEmpty()) {
                WHERE("xmdd001 = #{xmdd001}");
            }
            Object seq = params.get("xmddseq");
            if (seq != null && !String.valueOf(seq).trim().isEmpty()) {
                WHERE("xmddseq = #{xmddseq}");
            }
            ORDER_BY("xmdddocno, xmddseq");
        }}.toString();
    }

    /**
     * 查询指定主件的下阶 BOM 子件（默认取 bmaa002 最小的生效版本）
     * 参数：ent(必填)、site(必填)、itemNo(必填，即 bmaa001)
     */
    public String queryBomChildren(final Map<String, Object> params) {
        return new SQL() {{
            SELECT("a.bmaa001 AS bmba001");
            SELECT("m.bmba002, m.bmba003, m.bmba004, m.bmba005, m.bmba009, m.bmba010, m.bmba011, m.bmba012");
            SELECT("b.bmbb011");
            FROM("bmaa_t a");
            INNER_JOIN("bmba_t m ON m.bmbaent = a.bmaaent AND m.bmbasite = a.bmaasite AND m.bmba001 = a.bmaa001 AND m.bmba002 = a.bmaa002");
            LEFT_OUTER_JOIN("bmbb_t b ON b.bmbbent = a.bmaaent AND b.bmbbsite = a.bmaasite " +
                    "AND b.bmbb001 = m.bmba001 AND b.bmbb002 = m.bmba002 AND b.bmbb003 = m.bmba003 " +
                    "AND b.bmbb004 = m.bmba004 AND b.bmbb005 = m.bmba005");
            WHERE("a.bmaaent = #{ent}");
            WHERE("a.bmaasite = #{site}");
            WHERE("a.bmaa001 = #{itemNo}");
            WHERE("a.bmaastus = 'Y'");
            WHERE("a.bmaa002 = (SELECT MIN(bmaa002) FROM bmaa_t WHERE bmaaent = #{ent} AND bmaasite = #{site} AND bmaa001 = #{itemNo} AND bmaastus = 'Y')");
            // 过滤已失效的 BOM 单身：bmba006（失效日期）必须为 NULL
            WHERE("m.bmba006 IS NULL");
            ORDER_BY("m.bmba009");
        }}.toString();
    }
}
