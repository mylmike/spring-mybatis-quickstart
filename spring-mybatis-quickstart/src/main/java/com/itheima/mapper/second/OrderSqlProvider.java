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
            ORDER_BY("xmdadocdt DESC, xmdddocno, xmddseq");
        }}.toString();
    }
}
