package com.itheima.mapper.second;

import org.apache.ibatis.jdbc.SQL;

import com.itheima.pojo.sfahuc;
import java.util.ArrayList;
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
            "sfahuc004",     // 订单号
            "sfahuc005",     // 订单序号
            "sfahuc001"      // 工单号
    );
    // 注：成本中心 sfahuc008 不在此列表，改为下方逗号分隔 IN 查询（见循环后处理）

    /** sfaa_t 左连接条件：主查询与排产子表共用（别名约定 h = sfahuc_t、a = sfaa_t） */
    private static final String SFAA_JOIN = "sfaa_t a ON a.sfaaent = h.sfahucent " +
                                            "AND a.sfaasite = h.sfahucsite " +
                                            "AND a.sfaadocno = h.sfahuc001";

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
            SELECT("a.sfaa049 AS SFAA049");   // 已发料套数（取自 sfaa_t）
            SELECT("NVL(a.sfaa012, 0) - NVL(a.sfaa050, 0) AS YULIANG");
            // 人数(sfahuc012)、工作时长(sfahuc013)、UPPH(sfahuc014) 均为主表 sfahuc_t 自有列；
            // 日产量(sfahuc015) = 人数 * 工作时长 * UPPH，由 Controller 计算后返回（避免与 h.* 的 sfahuc015 列重名冲突）
            FROM("sfahuc_t h");
            // LEFT JOIN 工单主表 sfaa_t 取工单结案状态（ent=账套、site=据点、sfaadocno=工单号=sfahuc001），无匹配时 sfaastus 为 NULL
            LEFT_OUTER_JOIN(SFAA_JOIN);
            for (String cond : buildConditions(params)) {
                WHERE(cond);
            }
            ORDER_BY("h.sfahucseq");
        }}.toString();
    }

    /**
     * 主查询与排产子表共用的过滤条件（别名约定：h = sfahuc_t，a = sfaa_t）。
     * 抽出来是为了让 listSfajucPlan 与主查询保持完全一致的筛选范围，避免排产子表全据点扫描 sfajuc_t。
     */
    private List<String> buildConditions(final Map<String, Object> params) {
        List<String> conds = new ArrayList<>();
        // 固定条件：账套（数值型）、营运据点
        conds.add("h.sfahucent = TO_NUMBER(#{sfahucent})");
        conds.add("h.sfahucsite = #{sfahucsite}");
        // 可选条件
        for (String field : CONDITION_FIELDS) {
            if (hasText(params.get(field))) {
                conds.add("h." + field + " = #{" + field + "}");
            }
        }
        // 成本中心 sfahuc008 支持逗号分隔多值 IN 查询（如 'A18,A15,A16,A17' -> IN ('A18','A15','A16','A17')）
        // 仅允许字母数字/下划线，防 SQL 注入；含非法字符的值忽略，全部非法则该条件不拼接
        String ccIn = buildInList(params.get("sfahuc008"), "h.sfahuc008");
        if (ccIn != null) {
            conds.add(ccIn);
        }
        // 工单主表 sfaa_t（LEFT JOIN 的 a 表）的日期/状态过滤
        if (hasText(params.get("sfaadocdt_start"))) {
            conds.add("a.sfaadocdt >= TO_DATE(SUBSTR(NULLIF(#{sfaadocdt_start},''),1,10),'YYYY-MM-DD')");
        }
        if (hasText(params.get("sfaadocdt_end"))) {
            conds.add("a.sfaadocdt <= TO_DATE(SUBSTR(NULLIF(#{sfaadocdt_end},''),1,10),'YYYY-MM-DD')");
        }
        if (hasText(params.get("sfaa019_start"))) {
            conds.add("a.sfaa019 >= TO_DATE(SUBSTR(NULLIF(#{sfaa019_start},''),1,10),'YYYY-MM-DD')");
        }
        if (hasText(params.get("sfaa019_end"))) {
            conds.add("a.sfaa019 <= TO_DATE(SUBSTR(NULLIF(#{sfaa019_end},''),1,10),'YYYY-MM-DD')");
        }
        if (hasText(params.get("sfaa020_start"))) {
            conds.add("a.sfaa020 >= TO_DATE(SUBSTR(NULLIF(#{sfaa020_start},''),1,10),'YYYY-MM-DD')");
        }
        if (hasText(params.get("sfaa020_end"))) {
            conds.add("a.sfaa020 <= TO_DATE(SUBSTR(NULLIF(#{sfaa020_end},''),1,10),'YYYY-MM-DD')");
        }
        // 生产日期范围（prod_start=最小值, prod_end=最大值）：「区间重叠」语义
        //   工单生产期 [sfaa019, sfaa020] 与筛选期 [prod_start, prod_end] 有交集即返回：
        //   sfaa019 <= prod_end 且 sfaa020 >= prod_start
        if (hasText(params.get("prod_start"))) {
            conds.add("a.sfaa020 >= TO_DATE(SUBSTR(NULLIF(#{prod_start},''),1,10),'YYYY-MM-DD')");
        }
        if (hasText(params.get("prod_end"))) {
            conds.add("a.sfaa019 <= TO_DATE(SUBSTR(NULLIF(#{prod_end},''),1,10),'YYYY-MM-DD')");
        }
        // 状态码过滤：支持逗号分隔多状态，如 'F,N,M' -> a.sfaastus IN ('F','N','M')
        String statusIn = buildInList(params.get("sfaastus"), "a.sfaastus");
        if (statusIn != null) {
            conds.add(statusIn);
        }
        return conds;
    }

    /**
     * 构造逗号分隔多值的 IN 条件，如 'A18,A15' -> h.sfahuc008 IN ('A18','A15')。
     * 单值也走 IN（IN ('A18')），仅允许字母数字下划线防止拼接 SQL 注入；
     * 全部非法则返回 null，表示该项条件不拼接。
     */
    private String buildInList(Object val, String column) {
        if (!hasText(val)) {
            return null;
        }
        StringBuilder inSql = new StringBuilder(column).append(" IN (");
        boolean first = true;
        for (String t : String.valueOf(val).split(",")) {
            String s = t.trim();
            if (!s.isEmpty() && s.matches("[A-Za-z0-9_]+")) {
                if (!first) {
                    inSql.append(",");
                }
                inSql.append("'").append(s).append("'");
                first = false;
            }
        }
        return first ? null : inSql.append(")").toString();
    }

    /**
     * 查询 sfajuc_t 按 工单号(sfajuc001) + 产线(sfajuc004) + 排产日期(TRUNC(sfajuc007)) 汇总的排产数量。
     * 用于挂到 sfahuc_t 主表记录下、作为「排产子表」返回（一对多：一个工单+产线可对应多天排产）。
     * 固定条件：sfajucent（默认 60）、sfajucsite（默认 NBYL），由 Controller 保证有值。
     * 返回列：sfajuc001(工单号)、sfajuc004(产线)、PAICHANDATE(排产日期 YYYY-MM-DD)、PAICHANQTY(排产数量 SUM)
     * 筛选范围：与主查询 listByDocno 完全一致（共用 buildConditions），只统计主查询命中的工单。
     */
    public String listSfajucPlan(final Map<String, Object> params) {
        return new SQL() {{
            SELECT("j.sfajuc001");
            SELECT("j.sfajuc004");
            SELECT("TO_CHAR(TRUNC(j.sfajuc007),'YYYY-MM-DD') AS PAICHANDATE");
            SELECT("SUM(j.sfajuc003) AS PAICHANQTY");
            FROM("sfajuc_t j");
            // 与主查询一致的筛选范围：只取主查询命中的工单，避免全据点扫描 sfajuc_t。
            // 用 IN 子查询而不是 JOIN sfahuc_t：同一工单在 sfahuc_t 中可能有多行（多品号），
            // 直接 JOIN 会把 sfajuc_t 的行复制多份，导致 SUM(排产数量) 重复累加。
            StringBuilder inSub = new StringBuilder("j.sfajuc001 IN (SELECT h.sfahuc001 FROM sfahuc_t h LEFT OUTER JOIN ")
                    .append(SFAA_JOIN).append(" WHERE ");
            boolean first = true;
            for (String cond : buildConditions(params)) {
                if (!first) {
                    inSub.append(" AND ");
                }
                inSub.append(cond);
                first = false;
            }
            WHERE(inSub.append(")").toString());
            WHERE("j.sfajucent = TO_NUMBER(#{sfahucent})");
            WHERE("j.sfajucsite = #{sfahucsite}");
            GROUP_BY("j.sfajuc001, j.sfajuc004, TRUNC(j.sfajuc007)");
            ORDER_BY("j.sfajuc001, j.sfajuc004, TRUNC(j.sfajuc007)");
        }}.toString();
    }

    /**
     * 按表主键（账套+据点+工单号+品号）更新 sfahuc_t
     * set 中【不包含】主键四列（避免多行撞同一主键 ORA-00001）
     * sfahucstus 仅在传入非空时才更新；未传则保持原行审核状态不变
     */
    public String updateByPk(final sfahuc h) {
        return new SQL() {{
            UPDATE("sfahuc_t");
            // 业务列一律「有值才更新」：前端未传 / 空串 / 纯空白 均视为本次不涉及该列，保持原行不变。
            // 这样各页面只带自己关心的字段时，不会把其它页面已存的列覆盖成 NULL（如主计划页不带人数/工时/UPPH）
            if (hasVal(h.getSfahucdocno())) {
                SET("sfahucdocno=#{sfahucdocno}");
            }
            if (hasVal(h.getSfahucseq())) {
                SET("sfahucseq=TO_NUMBER(NULLIF(#{sfahucseq},''))");
            }
            if (hasVal(h.getSfahuc003())) {
                SET("sfahuc003=TO_NUMBER(NULLIF(#{sfahuc003},''))");
            }
            if (hasVal(h.getSfahuc004())) {
                SET("sfahuc004=#{sfahuc004}");
            }
            if (hasVal(h.getSfahuc005())) {
                SET("sfahuc005=TO_NUMBER(NULLIF(#{sfahuc005},''))");
            }
            if (hasVal(h.getSfahuc006())) {
                SET("sfahuc006=TO_DATE(SUBSTR(NULLIF(#{sfahuc006},''),1,10),'YYYY-MM-DD')");
            }
            if (hasVal(h.getSfahuc007())) {
                SET("sfahuc007=TO_DATE(SUBSTR(NULLIF(#{sfahuc007},''),1,10),'YYYY-MM-DD')");
            }
            if (hasVal(h.getSfahuc008())) {
                SET("sfahuc008=#{sfahuc008}");
            }
            if (hasVal(h.getSfahuc009())) {
                SET("sfahuc009=TO_NUMBER(NULLIF(#{sfahuc009},''))");
            }
            if (hasVal(h.getSfahuc010())) {
                SET("sfahuc010=#{sfahuc010,jdbcType=VARCHAR}");
            }
            if (hasVal(h.getSfahuc011())) {
                SET("sfahuc011=TO_NUMBER(NULLIF(#{sfahuc011,jdbcType=VARCHAR},''))");
            }
            if (hasVal(h.getSfahuc012())) {
                SET("sfahuc012=TO_NUMBER(NULLIF(#{sfahuc012},''))");
            }
            if (hasVal(h.getSfahuc013())) {
                SET("sfahuc013=TO_NUMBER(NULLIF(#{sfahuc013},''))");
            }
            if (hasVal(h.getSfahuc014())) {
                SET("sfahuc014=TO_NUMBER(NULLIF(#{sfahuc014},''))");
            }
            if (hasVal(h.getSfahuc015())) {
                SET("sfahuc015=TO_NUMBER(NULLIF(#{sfahuc015},''))");
            }
            // 排程数量：仅本次请求有值才更新（留空=不改动原值，避免被别的页面保存清空）
            if (hasVal(h.getSfahuc016())) {
                SET("sfahuc016=TO_NUMBER(NULLIF(#{sfahuc016,jdbcType=VARCHAR},''))");
            }
            // 状态码：仅前端传入非空才更新，未传则保持原行不变
            if (h.getSfahucstus() != null) {
                SET("sfahucstus=#{sfahucstus}");
            }
            SET("sfahucmoddt=sysdate");
            SET("sfahucmodid=#{sfahucmodid}");
            WHERE("sfahucent=TO_NUMBER(#{sfahucent})");
            WHERE("sfahucsite=#{sfahucsite}");
            WHERE("sfahuc001=#{sfahuc001}");
            WHERE("sfahuc002=#{sfahuc002}");
        }}.toString();
    }

    /** 判断参数是否为有效非空文本（null / 空串 / 纯空白 均返回 false） */
    private boolean hasText(Object val) {
        return val != null && !String.valueOf(val).trim().isEmpty();
    }

    /**
     * 判断更新字段是否「本次有传入」：null / 空串 / 纯空白 均视为未传入，
     * update 时不拼进 set，保持数据库原值不变。
     */
    private boolean hasVal(String val) {
        return val != null && !val.trim().isEmpty();
    }
}
