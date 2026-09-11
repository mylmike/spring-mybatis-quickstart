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
            SELECT("xmdd001, xmdd005, xmdd014, xmdddocno, xmddseq, TO_CHAR(xmdd011, 'YYYY-MM-DD') AS xmdd011");
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
            // 关联品号基本资料 imaf_t，取出补货策略 imaf013（1=采购/2=自制/...）
            SELECT("f.imaf013 AS imaf013");
            // 关联品号描述 imaal_t，取出品名/规格（按语言）
            SELECT("al.imaal003 AS imaal003");
            SELECT("al.imaal004 AS imaal004");
            // 关联物料基础信息 imae_t，取出默认成本中心 imae035
            SELECT("ie.imae035 AS imae035");
            // 关联成本中心多语言表 ooefl_t（别名 t2），取出默认成本中心名（字段名 imae035Name，避免与工单成本中心名 ooefl003 冲突）
            SELECT("t2.ooefl003 AS imae035Name");
            FROM("bmaa_t a");
            INNER_JOIN("bmba_t m ON m.bmbaent = a.bmaaent AND m.bmbasite = a.bmaasite AND m.bmba001 = a.bmaa001 AND m.bmba002 = a.bmaa002");
            LEFT_OUTER_JOIN("bmbb_t b ON b.bmbbent = a.bmaaent AND b.bmbbsite = a.bmaasite " +
                    "AND b.bmbb001 = m.bmba001 AND b.bmbb002 = m.bmba002 AND b.bmbb003 = m.bmba003 " +
                    "AND b.bmbb004 = m.bmba004 AND b.bmbb005 = m.bmba005");
            LEFT_OUTER_JOIN("imaf_t f ON f.imafent = #{ent} AND f.imafsite = #{site} AND f.imaf001 = m.bmba003");
            // 关联品号描述多语言表（按语言 lang 取品名/规格）
            LEFT_OUTER_JOIN("imaal_t al ON al.imaalent = #{ent} AND al.imaal002 = #{lang} AND al.imaal001 = m.bmba003");
            // 关联物料基础信息 imae_t（默认成本中心），imae001 = 元件品号 bmba003
            LEFT_OUTER_JOIN("imae_t ie ON ie.imaeent = #{ent} AND ie.imaesite = #{site} AND ie.imae001 = m.bmba003");
            // 关联成本中心多语言表 ooefl_t（别名 t2），按 lang 取成本中心名
            LEFT_OUTER_JOIN("ooefl_t t2 ON t2.ooeflent = #{ent} AND t2.ooefl001 = ie.imae035 AND t2.ooefl002 = #{lang}");
            WHERE("a.bmaaent = #{ent}");
            WHERE("a.bmaasite = #{site}");
            WHERE("a.bmaa001 = #{itemNo}");
            WHERE("a.bmaastus = 'Y'");
            WHERE("a.bmaa002 = (SELECT MIN(bmaa002) FROM bmaa_t WHERE bmaaent = #{ent} AND bmaasite = #{site} AND bmaa001 = #{itemNo} AND bmaastus = 'Y')");
            // 过滤已失效的 BOM 单身：bmba006（失效日期）必须为 NULL
            WHERE("m.bmba006 IS NULL");
            // 补货策略过滤：仅当传入 imaf013='2'（自制件）时返回自制件子件；
            // 自制件含 imaf013='2' 与 imaf013='3' 两类，故条件为 IN ('2','3')；其余情况展开返回所有
            Object imaf013 = params.get("imaf013");
            if (imaf013 != null && "2".equals(String.valueOf(imaf013).trim())) {
                WHERE("f.imaf013 IN ('2', '3')");
            }
            ORDER_BY("m.bmba009");
        }}.toString();
    }
}
