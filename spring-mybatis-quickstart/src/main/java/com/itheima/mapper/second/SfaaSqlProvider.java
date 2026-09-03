package com.itheima.mapper.second;

import org.apache.ibatis.jdbc.SQL;

import java.util.Map;

/**
 * SFAA_T 相关动态 SQL 构造器
 */
public class SfaaSqlProvider {

    /** 返回行数硬上限：最多 1000 条 */
    private static final int MAX_ROWS = 1000;

    /**
     * 按可选条件查询工单
     * 参数（全部可选）：
     *   orderNo    来源单号 sfaa022
     *   sfaa023    来源序号
     *   sfaa068    成本中心
     *   sfaadocno  工单号
     *   sfaa010    生产料号
     *   rowMax     期望行数
     * 规则：
     *   0. 固定条件（始终拼接，Controller 保证有默认值）：
     *      sfaaent  账套，默认 60（数值型）
     *      sfaasite 营运据点，默认 NBYL
     *      sfaastus = 'F'（只查已发放工单）
     *   1. 其余条件为 null / 空串 / 纯空白 时，视为不设置该条件，不拼接 where
     *   2. 始终限制返回行数，最多 MAX_ROWS(1000) 条：
     *      - 未传 rowMax 或 rowMax <= 0  →  取 1000
     *      - rowMax > 1000               →  截断为 1000
     *      - 0 < rowMax <= 1000          →  取 rowMax
     *   3. 排序：按预计完工日期 sfaa020 降序（NULL 排最后），同日期再按 sfaadocno 降序
     *      内层先排好序，外层再截 rownum，保证截断结果确定（取完工日期最新的一批），
     *      而非 Oracle 任意扫描到的行
     */
    public String listByOrderNo(final Map<String, Object> params) {
        final int rowMax = parseRowMax(params.get("rowMax"));

        SQL sql = new SQL() {{
            SELECT("a.*, b.ooefl003");
            FROM("SFAA_T a");
            LEFT_OUTER_JOIN("ooefl_t b on b.ooeflent = a.sfaaent and b.ooefl001 = a.sfaa068 and b.ooefl002 = 'zh_CN'");
            // 固定条件：账套（数值型）、营运据点
            WHERE("a.sfaaent = TO_NUMBER(#{sfaaent})");
            WHERE("a.sfaasite = #{sfaasite}");
            // 只查已发放的工单
            WHERE("a.sfaastus = 'F'");
            if (hasText(params.get("orderNo"))) {
                WHERE("a.sfaa022 = #{orderNo}");
            }
            // 来源序号
            if (hasText(params.get("sfaa023"))) {
                WHERE("a.sfaa023 = #{sfaa023}");
            }
            if (hasText(params.get("sfaa068"))) {
                WHERE("a.sfaa068 = #{sfaa068}");
            }
            // 工单号
            if (hasText(params.get("sfaadocno"))) {
                WHERE("a.sfaadocno = #{sfaadocno}");
            }
            // 生产料号
            if (hasText(params.get("sfaa010"))) {
                WHERE("a.sfaa010 = #{sfaa010}");
            }
            // 按预计完工日期(sfaa020)降序，NULL 排最后
            // 内层先排好序，外层 rownum 截断取到的才是"完工日期最新的一批"，而非随机行
            // 第二排序键 sfaadocno 保证同日期时结果稳定
            ORDER_BY("a.sfaa020 DESC NULLS LAST, a.sfaadocno DESC");
        }};

        return "select * from (" + sql.toString() + ") where rownum <= " + rowMax;
    }

    /** 判断参数是否为有效非空文本（null / 空串 / 纯空白 均返回 false） */
    private boolean hasText(Object val) {
        return val != null && !String.valueOf(val).trim().isEmpty();
    }

    /** 解析行数限制：未传/非法/<=0 取 MAX_ROWS，超过 MAX_ROWS 截断为 MAX_ROWS */
    private int parseRowMax(Object val) {
        if (val == null) {
            return MAX_ROWS;
        }
        try {
            int n = Integer.parseInt(String.valueOf(val).trim());
            if (n <= 0) {
                return MAX_ROWS;
            }
            return Math.min(n, MAX_ROWS);
        } catch (NumberFormatException e) {
            return MAX_ROWS;
        }
    }
}
