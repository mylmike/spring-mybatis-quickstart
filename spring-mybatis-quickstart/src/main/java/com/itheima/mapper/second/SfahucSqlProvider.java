package com.itheima.mapper.second;

import org.apache.ibatis.jdbc.SQL;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * sfahuc_t 相关动态 SQL 构造器
 * 空值约定：可选条件参数为 null / 空串 / 纯空白 时，视为该项条件未设置，不拼接 where 条件
 */
public class SfahucSqlProvider {

    /** 可选查询条件（顺序即 where 拼接顺序），前端未传 / 空值 时不拼接 */
    private static final List<String> CONDITION_FIELDS = Arrays.asList(
            "sfahucdocno",   // 单号
            "sfahuc008",     // 成本中心
            "sfahuc004",     // 订单号
            "sfahuc005",     // 订单序号
            "sfahuc001"      // 工单号
    );

    /**
     * 按条件查询 sfahuc_t 明细（主表，不展开排产子表）
     * 固定条件：
     *   sfahucent 账套，数值型，Controller 保证有值（默认 60）
     *   sfahucsite 营运据点，Controller 保证有值（默认 NBYL）
     * 可选条件（CONDITION_FIELDS）：
     *   null / 空串 / 纯空白 均视为条件未设置，不拼接 where
     * 固定排序：order by sfahucseq
     */
    public String listByDocno(final Map<String, Object> params) {
        return new SQL() {{
            SELECT("h.*");
            // 工单结案状态 sfaastus 取自 sfaa_t（无需新表，LEFT JOIN 即可）
            SELECT("a.sfaastus AS SFAASTUS");
            // 入库数量 sfaa050、余量 = 订单量(sfaa012) - 入库量(sfaa050)，均取自 sfaa_t
            SELECT("a.sfaa050 AS SFAA050");
            SELECT("NVL(a.sfaa012, 0) - NVL(a.sfaa050, 0) AS YULIANG");
            // 人数(sfahuc012)、工作时长(sfahuc013)、UPPH(sfahuc014) 均为主表 sfahuc_t 自有列；
            // 日产量(sfahuc015) = 人数 * 工作时长 * UPPH，由 Controller 计算后返回（避免与 h.* 的 sfahuc015 列重名冲突）
            FROM("sfahuc_t h");
            // LEFT JOIN 工单主表 sfaa_t 取工单结案状态（ent=账套、site=据点、sfaadocno=工单号=sfahuc001），无匹配时 sfaastus 为 NULL
            LEFT_OUTER_JOIN("sfaa_t a ON a.sfaaent = h.sfahucent " +
                            "AND a.sfaasite = h.sfahucsite " +
                            "AND a.sfaadocno = h.sfahuc001");
            // 固定条件：账套（数值型）、营运据点
            WHERE("h.sfahucent = TO_NUMBER(#{sfahucent})");
            WHERE("h.sfahucsite = #{sfahucsite}");
            // 可选条件
            for (String field : CONDITION_FIELDS) {
                if (hasText(params.get(field))) {
                    WHERE("h." + field + " = #{" + field + "}");
                }
            }
            // 工单主表 sfaa_t（LEFT JOIN 的 a 表）的日期/状态过滤
            if (hasText(params.get("sfaadocdt_start"))) {
                WHERE("a.sfaadocdt >= TO_DATE(SUBSTR(NULLIF(#{sfaadocdt_start},''),1,10),'YYYY-MM-DD')");
            }
            if (hasText(params.get("sfaadocdt_end"))) {
                WHERE("a.sfaadocdt <= TO_DATE(SUBSTR(NULLIF(#{sfaadocdt_end},''),1,10),'YYYY-MM-DD')");
            }
            if (hasText(params.get("sfaa019_start"))) {
                WHERE("a.sfaa019 >= TO_DATE(SUBSTR(NULLIF(#{sfaa019_start},''),1,10),'YYYY-MM-DD')");
            }
            if (hasText(params.get("sfaa019_end"))) {
                WHERE("a.sfaa019 <= TO_DATE(SUBSTR(NULLIF(#{sfaa019_end},''),1,10),'YYYY-MM-DD')");
            }
            if (hasText(params.get("sfaa020_start"))) {
                WHERE("a.sfaa020 >= TO_DATE(SUBSTR(NULLIF(#{sfaa020_start},''),1,10),'YYYY-MM-DD')");
            }
            if (hasText(params.get("sfaa020_end"))) {
                WHERE("a.sfaa020 <= TO_DATE(SUBSTR(NULLIF(#{sfaa020_end},''),1,10),'YYYY-MM-DD')");
            }
            // 状态码过滤：支持逗号分隔多状态，如 'F,N,M' -> a.sfaastus IN ('F','N','M')
            // 单值也走 IN（IN ('F')），仅允许字母数字下划线，防止拼接 SQL 注入
            if (hasText(params.get("sfaastus"))) {
                StringBuilder inSql = new StringBuilder("a.sfaastus IN (");
                boolean first = true;
                for (String t : String.valueOf(params.get("sfaastus")).split(",")) {
                    String s = t.trim();
                    if (!s.isEmpty() && s.matches("[A-Za-z0-9_]+")) {
                        if (!first) {
                            inSql.append(",");
                        }
                        inSql.append("'").append(s).append("'");
                        first = false;
                    }
                }
                inSql.append(")");
                if (!first) {
                    WHERE(inSql.toString());
                }
            }
            ORDER_BY("h.sfahucseq");
        }}.toString();
    }

    /**
     * 查询 sfajuc_t 按 工单号(sfajuc001) + 产线(sfajuc004) + 排产日期(TRUNC(sfajuc007)) 汇总的排产数量。
     * 用于挂到 sfahuc_t 主表记录下、作为「排产子表」返回（一对多：一个工单+产线可对应多天排产）。
     * 固定条件：sfajucent（默认 60）、sfajucsite（默认 NBYL），由 Controller 保证有值。
     * 返回列：sfajuc001(工单号)、sfajuc004(产线)、PAICHANDATE(排产日期 YYYY-MM-DD)、PAICHANQTY(排产数量 SUM)
     */
    public String listSfajucPlan(final Map<String, Object> params) {
        return new SQL() {{
            SELECT("sfajuc001");
            SELECT("sfajuc004");
            SELECT("TO_CHAR(TRUNC(sfajuc007),'YYYY-MM-DD') AS PAICHANDATE");
            SELECT("SUM(sfajuc003) AS PAICHANQTY");
            FROM("sfajuc_t");
            WHERE("sfajucent = TO_NUMBER(#{sfahucent})");
            WHERE("sfajucsite = #{sfahucsite}");
            GROUP_BY("sfajuc001, sfajuc004, TRUNC(sfajuc007)");
            ORDER_BY("sfajuc001, sfajuc004, TRUNC(sfajuc007)");
        }}.toString();
    }

    /** 判断参数是否为有效非空文本（null / 空串 / 纯空白 均返回 false） */
    private boolean hasText(Object val) {
        return val != null && !String.valueOf(val).trim().isEmpty();
    }
}
