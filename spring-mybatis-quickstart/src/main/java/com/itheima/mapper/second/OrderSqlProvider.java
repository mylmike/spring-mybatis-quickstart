package com.itheima.mapper.second;

import org.apache.ibatis.jdbc.SQL;

import java.util.Map;

/**
 * 订单相关动态 SQL 构造器
 */
public class OrderSqlProvider {

    /**
     * 订单未交货查询清单（用于前端下拉框绑定）
     * 基于 xmdd_t / xmdc_t / xmda_t 三表关联
     * 限制：仅返回开单日期(xmdadocdt)最近 6 个月的记录
     * 返回条数由前端控制（不做服务端 ROWNUM 限制）
     */
    public String queryUndeliveredOrders(final Map<String, Object> params) {
        return new SQL() {{
            SELECT("xmdddocno AS docno");
            SELECT("xmddseq AS seq");
            SELECT("xmdd001 AS item");
            SELECT("(xmdd006 - xmdd031) AS undeliveredQty");
            SELECT("xmda004 AS customer");
            SELECT("xmdadocdt AS docDate");
            FROM("xmdd_t");
            LEFT_OUTER_JOIN("xmdc_t on xmdcent = xmddent and xmdcdocno = xmdddocno and xmdcseq = xmddseq");
            LEFT_OUTER_JOIN("xmda_t on xmdaent = xmddent and xmdadocno = xmdddocno");
            WHERE("xmddent = 60");
            WHERE("xmddsite = 'NBYL'");
            WHERE("(xmdd006 - xmdd014) > 0");
            WHERE("xmdc045 = '1'");
            WHERE("xmdastus = 'Y'");
            // 仅返回开单日期(xmdadocdt)最近 6 个月的记录
            WHERE("xmdadocdt >= ADD_MONTHS(TRUNC(SYSDATE), -6)");
            ORDER_BY("xmdadocdt DESC, xmdddocno, xmddseq");
        }}.toString();
    }

    /**
     * 查询未完成订单（订单量 > 已出货量，即尚未出完货）
     * 基于 xmdd_t / xmda_t / xmdc_t / pmaal_t / ooag_t 关联
     * 固定条件：xmddent(账套,默认60) + xmddsite(据点,默认NBYL)
     *          + (xmdd006 - xmdd014) > 0 + xmdastus='Y' + xmdc045='1'
     * 可选条件（前端传入，未传则忽略）：
     *   xmdd011_start / xmdd011_end      订单交期(xmdd011) 范围
     *   xmdadocdt_start / xmdadocdt_end  开单日期(xmdadocdt) 范围
     * 硬上限：最多返回最近的 300 条（按 xmdd011 降序取前 300，外层再升序展示）
     * 返回：订单号/序号/品号/订单量/已出货量/未交量/订单交期/客户名称(pmaal004)/品名(imaal003)/业务员(ooag011)/业务员工号(xmda002)/补货策略(gzcbl004)
     */
    public String queryUnfinishedOrders(final Map<String, Object> params) {
        final String d011Start = (String) params.get("xmdd011_start");
        final String d011End   = (String) params.get("xmdd011_end");
        final String dDocStart = (String) params.get("xmdadocdt_start");
        final String dDocEnd   = (String) params.get("xmdadocdt_end");
        final String docno  = (String) params.get("xmdddocno");   // 订单号
        final String seq    = (String) params.get("xmddseq");     // 订单序号
        final String item   = (String) params.get("xmdd001");     // 品号
        final String pname  = (String) params.get("imaal003");    // 品名(like)
        final String saler  = (String) params.get("ooag011");     // 业务员(like)
        final String imaf013 = (String) params.get("imaf013");    // 补货策略
        final String inner = new SQL() {{
            SELECT("xmdddocno");
            SELECT("xmddseq");
            SELECT("xmdd001");
            SELECT("xmdd006");
            SELECT("xmdd014");
            SELECT("(xmdd006 - xmdd014) undqty");
            SELECT("xmdd011");
            SELECT("pmaal004");
            SELECT("imaal003");
            SELECT("ooag011");
            SELECT("xmda002 salesempno");
            SELECT("gzcbl004");
            FROM("xmdd_t");
            LEFT_OUTER_JOIN("xmda_t on xmdaent = xmddent and xmdadocno = xmdddocno");
            LEFT_OUTER_JOIN("xmdc_t on xmdcent = xmddent and xmdcdocno = xmdddocno and xmdcseq = xmddseq");
            LEFT_OUTER_JOIN("pmaal_t on pmaalent = xmddent and pmaal001 = xmda004 and pmaal002 = 'zh_CN'");
            LEFT_OUTER_JOIN("imaal_t on imaalent = xmddent and imaal002 = 'zh_CN' and imaal001 = xmdd001");
            LEFT_OUTER_JOIN("imaf_t on imafent = xmddent and imaf001 = xmdd001 and imafsite = #{xmddsite}");
            LEFT_OUTER_JOIN("gzcbl_t on gzcbl001 = '2022' and gzcbl002 = imaf013 and gzcbl003 = 'zh_CN'");
            LEFT_OUTER_JOIN("ooag_t on ooagent = xmddent and ooag001 = xmdaownid");
            WHERE("xmddent = #{xmddent}");
            WHERE("xmddsite = #{xmddsite}");
            WHERE("(xmdd006 - xmdd014) > 0");
            WHERE("xmdastus = 'Y'");
            WHERE("xmdc045 = '1'");
            // 可选精确匹配条件
            if (hasText(docno)) {
                WHERE("xmdddocno = #{xmdddocno}");
            }
            if (hasText(seq)) {
                WHERE("xmddseq = #{xmddseq}");
            }
            if (hasText(item)) {
                WHERE("xmdd001 = #{xmdd001}");
            }
            // 名称类字段模糊匹配
            if (hasText(pname)) {
                WHERE("imaal003 like '%' || #{imaal003} || '%'");
            }
            if (hasText(saler)) {
                WHERE("ooag011 like '%' || #{ooag011} || '%'");
            }
            if (hasText(imaf013)) {
                WHERE("imaf013 = #{imaf013}");
            }
            if (hasText(d011Start)) {
                WHERE("xmdd011 >= TO_DATE(#{xmdd011_start}, 'YYYY-MM-DD')");
            }
            if (hasText(d011End)) {
                WHERE("xmdd011 <= TO_DATE(#{xmdd011_end}, 'YYYY-MM-DD')");
            }
            if (hasText(dDocStart)) {
                WHERE("xmdadocdt >= TO_DATE(#{xmdadocdt_start}, 'YYYY-MM-DD')");
            }
            if (hasText(dDocEnd)) {
                WHERE("xmdadocdt <= TO_DATE(#{xmdadocdt_end}, 'YYYY-MM-DD')");
            }
            ORDER_BY("xmdd011 DESC, xmddseq DESC");
        }}.toString();
        // 硬上限：最多返回最近的 300 条（内层降序取最近，外层恢复升序展示）
        return "SELECT * FROM (" + inner + ") WHERE ROWNUM <= 300 ORDER BY xmdd011, xmddseq";
    }

    /** 判断参数是否为有效非空文本（null / 空串 / 纯空白 均返回 false） */
    private boolean hasText(Object val) {
        return val != null && !String.valueOf(val).trim().isEmpty();
    }
}
