package com.itheima.mapper.second;

import java.util.Map;

/**
 * 预算采购核价分析报表 SQL 构造器
 * 数据来源：dsdata 库
 * 主表 APBA_t（对账单） LEFT JOIN APBB_T / imaal_t / imag_t / oocql_t / pmaal_t / bgbtuc_t（采购价格预算）
 * 计算：采购价差 = 实际采购含税单价 - 预算采购含税单价
 *       偏差率  = 采购价差 / 预算采购含税单价
 *       差异额  = 采购价差 * 对账数量
 */
public class BudgetPurchaseSqlProvider {

    /**
     * 预算采购核价分析报表查询
     * 参数：ent(账套,默认60) site(据点,默认NBYL) lang(语言,默认zh_CN) year(年度) month(月份)
     */
    public String queryBudgetPurchaseAnalysis(final Map<String, Object> params) {
        String ent = str(params.get("ent"));
        String site = str(params.get("site"));
        String lang = str(params.get("lang"));
        String year = str(params.get("year"));
        String month = str(params.get("month"));

        if (ent == null || ent.isEmpty()) ent = "60";
        if (site == null || site.isEmpty()) site = "NBYL";
        if (lang == null || lang.isEmpty()) lang = "zh_CN";

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT EXTRACT(YEAR FROM apbbdocdt) 年, EXTRACT(MONTH FROM apbbdocdt) 月");
        sql.append(", apba007 品号, imaal003 品名, imaal004 规格");
        sql.append(", imag011 分群号, oocql004 分群名");
        sql.append(", apba013 采购单号, apba005 入库单号");
        sql.append(", apbb002 供应商, pmaal004 供应商名称");
        sql.append(", apba010 对账数量, apba009 单位, apba014 实际采购含税单价");
        sql.append(", bgbtuc003 参考供应商, bgbtuc005 预算采购含税单价");
        sql.append(", apba014 - bgbtuc005 采购价差");
        sql.append(", (apba014 - bgbtuc005) / NULLIF(bgbtuc005, 0) 偏差率");
        sql.append(", (apba014 - bgbtuc005) * apba010 差异额");
        sql.append(" FROM APBA_t");
        sql.append(" LEFT JOIN APBB_T ON APBAENT = APBBENT AND APBBDOCNO = APBADOCNO");
        sql.append(" LEFT JOIN imaal_t ON imaalent = apbaent AND imaal001 = apba007 AND imaal002 = '").append(lang).append("'");
        sql.append(" LEFT JOIN imag_t ON imagent = apbaent AND imagsite = apbaorga AND imag001 = apba007");
        sql.append(" LEFT JOIN oocql_t ON oocqlent = apbaent AND oocql003 = imaal002 AND oocql002 = imag011 AND oocql001 = '206'");
        sql.append(" LEFT JOIN pmaal_t ON pmaalent = apbaent AND pmaal001 = apbb002 AND pmaal002 = imaal002");
        sql.append(" LEFT JOIN bgbtuc_t ON bgbtucent = apbaent AND bgbtucld = apbbcomp");
        sql.append(" AND bgbtuc002 = EXTRACT(YEAR FROM apbbdocdt) AND bgbtuc001 = apba007");
        sql.append(" WHERE APBAENT = ").append(ent);
        sql.append(" AND APBBCOMP = '").append(site).append("'");
        sql.append(" AND APBA004 = '11'");
        if (year != null && !year.isEmpty()) {
            sql.append(" AND EXTRACT(YEAR FROM apbbdocdt) = '").append(year).append("'");
        }
        if (month != null && !month.isEmpty()) {
            sql.append(" AND EXTRACT(MONTH FROM apbbdocdt) = '").append(month).append("'");
        }
        sql.append(" ORDER BY 年, 月, 品号, 采购单号");
        return sql.toString();
    }

    private String str(Object o) {
        if (o == null) return null;
        return String.valueOf(o).trim();
    }
}
