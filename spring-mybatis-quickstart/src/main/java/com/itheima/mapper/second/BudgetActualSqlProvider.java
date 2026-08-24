package com.itheima.mapper.second;

import java.util.Map;

/**
 * 预算与实际差异明细表 SQL 构造器
 * 数据来源：dsdata 库（glaq_t / glap_t / glacl_t / ooefl_t / bgbsuc_t）
 */
public class BudgetActualSqlProvider {

    /**
     * 按用户提供的原 SQL 整体执行：实际费用 LEFT JOIN 预算结存，并计算差异与合计
     */
    public String queryBudgetActualVariance(final Map<String, Object> params) {
        String ent = (String) params.get("ent");            // 账套
        String site = (String) params.get("site");           // 账别
        String year = (String) params.get("year");           // 年度
        String dept = (String) params.get("dept");           // 部门（模糊匹配 ooefl003）
        String subjectName = (String) params.get("subjectName"); // 科目名称（模糊匹配 glacl004）
        String summary = (String) params.get("summary");     // 摘要（模糊匹配 glaq001）

        StringBuilder sql = new StringBuilder();

        // 外层差异、合计列（显式列名，避免 sj/ys 的 科目/部门 重复导致歧义）
        // 预算/差异列用 NVL 转 0，避免 LEFT JOIN 未匹配时返回 NULL 导致字段丢失
        sql.append("SELECT sj.科目, sj.科目名称, sj.部门");
        for (int i = 1; i <= 12; i++) {
            sql.append(", sj.实际").append(String.format("%02d", i)).append("月");
        }
        for (int i = 1; i <= 12; i++) {
            sql.append(", NVL(ys.预算").append(String.format("%02d", i)).append("月, 0)");
        }
        // 预算合计金额
        sql.append(", ");
        for (int i = 1; i <= 12; i++) {
            if (i > 1) sql.append("+");
            sql.append("NVL(ys.预算").append(String.format("%02d", i)).append("月, 0)");
        }
        sql.append(" 预算合计金额");
        // 实际合计金额
        sql.append(", ");
        for (int i = 1; i <= 12; i++) {
            if (i > 1) sql.append("+");
            sql.append("NVL(sj.实际").append(String.format("%02d", i)).append("月, 0)");
        }
        sql.append(" 实际合计金额");
        // 差异合计金额 = 实际合计 - 预算合计
        sql.append(", ");
        for (int i = 1; i <= 12; i++) {
            if (i > 1) sql.append("+");
            sql.append("NVL(sj.实际").append(String.format("%02d", i)).append("月, 0)");
        }
        sql.append(" - ");
        for (int i = 1; i <= 12; i++) {
            if (i > 1) sql.append("+");
            sql.append("NVL(ys.预算").append(String.format("%02d", i)).append("月, 0)");
        }
        sql.append(" 合计差异金额");
        // 差异 = 实际 - 预算（1~12 月均如此）
        for (int i = 1; i <= 12; i++) {
            sql.append(", NVL(sj.实际").append(String.format("%02d", i)).append("月, 0) - NVL(ys.预算")
               .append(String.format("%02d", i)).append("月, 0) 差异").append(i).append("月");
        }
        sql.append(" FROM (");

        // 实际费用子查询（行转列）
        sql.append("SELECT 科目, 科目名称, 部门");
        for (int i = 1; i <= 12; i++) {
            sql.append(", SUM(DECODE(月,'").append(i).append("',金额,0)) \"实际").append(String.format("%02d", i)).append("月\"");
        }
        sql.append(" FROM (");
        sql.append("  SELECT EXTRACT(YEAR FROM glapdocdt) 年, EXTRACT(MONTH FROM glapdocdt) 月, ");
        sql.append("  glaq002 科目, glacl004 科目名称, ooefl003 部门, SUM(glaq003) 金额 ");
        sql.append("  FROM glaq_t ");
        sql.append("  LEFT JOIN glap_t ON glapent=glaqent AND glapld=glaqld AND glapdocno=glaqdocno ");
        sql.append("  LEFT JOIN glacl_t ON glaclent=glaqent AND glacl002=glaq002 AND glacl003='zh_CN' ");
        sql.append("  LEFT JOIN ooefl_t ON ooeflent=glaqent AND ooefl001=glaq018 AND ooefl002='zh_CN' ");
        sql.append("  WHERE glaqent=").append(ent);
        sql.append(" AND glaqld='").append(site).append("'");
        sql.append(" AND (glaq002 LIKE '51%' OR glaq002 LIKE '66%') ");
        sql.append(" AND glapstus='S' ");
        sql.append(" AND EXTRACT(YEAR FROM glapdocdt)='").append(year).append("' ");
        if (dept != null && !dept.trim().isEmpty()) {
            sql.append(" AND ooefl003 LIKE '%").append(dept.trim()).append("%' ");
        }
        if (subjectName != null && !subjectName.trim().isEmpty()) {
            sql.append(" AND glacl004 LIKE '%").append(subjectName.trim()).append("%' ");
        }
        if (summary != null && !summary.trim().isEmpty()) {
            sql.append(" AND glaq001 LIKE '%").append(summary.trim()).append("%' ");
        }
        sql.append("  GROUP BY EXTRACT(YEAR FROM glapdocdt), EXTRACT(MONTH FROM glapdocdt), glaq002, glacl004, ooefl003 ");
        sql.append(") GROUP BY 科目, 科目名称, 部门 ORDER BY 科目, 科目名称, 部门");
        sql.append(") sj ");

        // 预算子查询（行转列）
        sql.append("LEFT JOIN (");
        sql.append("SELECT 科目, 部门");
        for (int i = 1; i <= 12; i++) {
            sql.append(", SUM(DECODE(月,'").append(i).append("',金额,0)) \"预算").append(String.format("%02d", i)).append("月\"");
        }
        sql.append(" FROM (");
        sql.append("  SELECT bgbsuc004 科目, bgbsuc005 金额, bgbsuc001 部门, bgbsuc002 年, bgbsuc003 月 ");
        sql.append("  FROM bgbsuc_t ");
        sql.append("  WHERE bgbsucent=").append(ent);
        sql.append(" AND bgbsucld='").append(site).append("'");
        sql.append(" AND bgbsuc002='").append(year).append("' ");
        sql.append(") GROUP BY 科目, 部门, 年 ORDER BY 科目, 部门, 年");
        sql.append(") ys ON sj.部门=ys.部门 AND sj.科目=ys.科目");

        return sql.toString();
    }
}
