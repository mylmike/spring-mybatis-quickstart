package com.itheima.mapper.second;

import org.apache.ibatis.jdbc.SQL;

import java.util.ArrayList;
import java.util.List;
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
     *      sfaastus 工单状态：未传/空串 -> 'F'；传入 'C,F,M' -> 按逗号拆解 IN(...)
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
            // UPPH = 3600 / 标准工时(imae051)；imae051 为 0 或 NULL 时 UPPH = 0
            SELECT("CASE WHEN i.imae051 IS NULL OR i.imae051 = 0 THEN 0 ELSE ROUND(3600 / i.imae051) END AS UPPH");
            FROM("SFAA_T a");
            LEFT_OUTER_JOIN("ooefl_t b on b.ooeflent = a.sfaaent and b.ooefl001 = a.sfaa068 and b.ooefl002 = 'zh_CN'");
            // LEFT JOIN 品号基础信息 imae_t（ent=账套、site=据点、imae001=生产料号=sfaa010），无匹配时 UPPH 记 0
            LEFT_OUTER_JOIN("imae_t i on i.imaeent = a.sfaaent and i.imaesite = a.sfaasite and i.imae001 = a.sfaa010");
            // 固定条件：账套（数值型）、营运据点
            WHERE("a.sfaaent = TO_NUMBER(#{sfaaent})");
            WHERE("a.sfaasite = #{sfaasite}");
            // 排除已排过产的单（sfahuc_t 中已存在对应工单号），即"未排产"工单才返回
            WHERE("a.sfaadocno NOT IN (SELECT sfahuc001 FROM sfahuc_t WHERE sfahucent = a.sfaaent AND sfahucsite = a.sfaasite)");
            // 工单状态过滤：
            //   未传 / 空串  ->  sfaastus = 'F'
            //   传入 'C,F,M' ->  按逗号拆解后  sfaastus IN ('C','F','M')
            Object statusObj = params.get("sfaastus");
            List<String> statuses = new ArrayList<>();
            if (statusObj != null) {
                for (String t : String.valueOf(statusObj).split(",")) {
                    String s = t.trim();
                    if (!s.isEmpty()) {
                        statuses.add(s);
                    }
                }
            }
            if (statuses.isEmpty()) {
                statuses.add("F");
            }
            if (statuses.size() == 1) {
                WHERE("a.sfaastus = '" + statuses.get(0) + "'");
            } else {
                StringBuilder inSql = new StringBuilder("a.sfaastus IN (");
                for (int i = 0; i < statuses.size(); i++) {
                    if (i > 0) {
                        inSql.append(",");
                    }
                    inSql.append("'").append(statuses.get(i)).append("'");
                }
                inSql.append(")");
                WHERE(inSql.toString());
            }
            if (hasText(params.get("orderNo"))) {
                WHERE("a.sfaa022 = #{orderNo}");
            }
            // 来源序号
            if (hasText(params.get("sfaa023"))) {
                WHERE("a.sfaa023 = #{sfaa023}");
            }
            // 成本中心 sfaa068 支持逗号分隔多值 IN 查询（如 'A18,A15,A16,A17' -> IN ('A18','A15','A16','A17')）
            // 仅允许字母数字/下划线，防 SQL 注入；含非法字符的值忽略，全部非法则该条件不拼接
            if (hasText(params.get("sfaa068"))) {
                StringBuilder inSql = new StringBuilder("a.sfaa068 IN (");
                boolean first = true;
                for (String t : String.valueOf(params.get("sfaa068")).split(",")) {
                    String s = t.trim();
                    if (!s.isEmpty() && s.matches("[A-Za-z0-9_]+")) {
                        if (!first) {
                            inSql.append(",");
                        }
                        inSql.append("'").append(s).append("'");
                        first = false;
                    }
                }
                if (!first) {
                    WHERE(inSql.append(")").toString());
                }
            }
            // 工单号
            if (hasText(params.get("sfaadocno"))) {
                WHERE("a.sfaadocno = #{sfaadocno}");
            }
            // 生产料号
            if (hasText(params.get("sfaa010"))) {
                WHERE("a.sfaa010 = #{sfaa010}");
            }
            // 开单日期范围
            if (hasText(params.get("sfaadocdt_start"))) {
                WHERE("a.sfaadocdt >= TO_DATE(SUBSTR(NULLIF(#{sfaadocdt_start},''),1,10),'YYYY-MM-DD')");
            }
            if (hasText(params.get("sfaadocdt_end"))) {
                WHERE("a.sfaadocdt <= TO_DATE(SUBSTR(NULLIF(#{sfaadocdt_end},''),1,10),'YYYY-MM-DD')");
            }
            // 预计开工日期范围
            if (hasText(params.get("sfaa019_start"))) {
                WHERE("a.sfaa019 >= TO_DATE(SUBSTR(NULLIF(#{sfaa019_start},''),1,10),'YYYY-MM-DD')");
            }
            if (hasText(params.get("sfaa019_end"))) {
                WHERE("a.sfaa019 <= TO_DATE(SUBSTR(NULLIF(#{sfaa019_end},''),1,10),'YYYY-MM-DD')");
            }
            // 预计完工日期范围
            if (hasText(params.get("sfaa020_start"))) {
                WHERE("a.sfaa020 >= TO_DATE(SUBSTR(NULLIF(#{sfaa020_start},''),1,10),'YYYY-MM-DD')");
            }
            if (hasText(params.get("sfaa020_end"))) {
                WHERE("a.sfaa020 <= TO_DATE(SUBSTR(NULLIF(#{sfaa020_end},''),1,10),'YYYY-MM-DD')");
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

    /**
     * 查询「可排产」工单：
     *   - 去掉已排完（可排数量 = 0）的工单
     *   - 已排但未排完的工单，返回剩余可排数量 kpsl
     *   - 从未排过的工单，kpsl = 生产数量（sfaa012）
     * 关联 sfahuc_t 汇总每个工单已排/已分配数量，计算可排数量：
     *   可排数量 = sfahuc003(已排数量) - sum(sfahuc011)(已分配订单需求)
     *            < 0 时取 0；未排过时该工单不在 sfahuc_t 汇总结果中，LEFT JOIN 后为 NULL，用 NVL 取 sfaa012
     * 过滤条件：账套(sfaaent)/据点(sfaasite) 固定；生产品号(sfaa010) 必填；工单状态(sfaastus) 可选（逗号分隔 IN）；
     *          来源单号(sfaa022)/来源序号(sfaa023)/工单号(sfaadocno) 可选精确匹配；空则不拼接；
     *          工单开立日期(sfaadocdt) 限制最近一年内（无日期的工单不限制）
     * 不再限制返回条数（原 ROWNUM<=30 已移除）
     */
    public String listSchedulableByItem(final Map<String, Object> params) {
        StringBuilder sb = new StringBuilder();
        // 返回列新增：sfaa022(来源单号)、sfaa023(来源序号)、xmdd005(订单数量)
        sb.append("select x.* ");
        sb.append("from ( ");
        sb.append("  select a.sfaadocno, a.sfaa010, a.sfaa012, a.sfaa022, a.sfaa023, ");
        sb.append("         NVL(M.kpsl, a.sfaa012) kpsl, ");
        sb.append("         a.sfaa050, a.sfaa019, a.sfaa020, a.sfaa068, a.sfaastus, ");
        // NVL 保证 xmdd005 非 null：无匹配订单行时返回 0，避免该字段因 null 在序列化时被丢弃
        sb.append("         NVL(d.xmdd005, 0) xmdd005 ");
        sb.append("  from sfaa_t a ");
        sb.append("  left join ( ");
        sb.append("    select sfahuc001, ");
        sb.append("           case when (sfahuc003 - 订单数) < 0 then 0 else (sfahuc003 - 订单数) end kpsl ");
        sb.append("    from ( ");
        sb.append("      select sfahuc001, sfahuc003, sum(NVL(sfahuc011,0)) 订单数, count(1) ");
        sb.append("      from sfahuc_t ");
        sb.append("      where sfahucent = TO_NUMBER(#{sfaaent}) and sfahucsite = #{sfaasite} ");
        sb.append("      group by sfahuc001, sfahuc003 ");
        sb.append("    ) ");
        sb.append("  ) M on M.sfahuc001 = a.sfaadocno ");
        // 关联订单表 xmdd_t：按 账套(sfaaent=xmddent) / 来源单号(sfaa022=xmdddocno) / 来源序号(sfaa023=xmddseq) 匹配，取订单数量 xmdd005
        sb.append("  left join xmdd_t d on d.xmddent = a.sfaaent and d.xmdddocno = a.sfaa022 and d.xmddseq = a.sfaa023 ");
        sb.append("  where a.sfaaent = TO_NUMBER(#{sfaaent}) ");
        sb.append("    and a.sfaasite = #{sfaasite} ");
        sb.append("    and a.sfaa010 = #{sfaa010} ");
        // 仅取工单开立日期(sfaadocdt)在最近一年内的工单，避免历史工单过多、结果失真
        // （如工单无开立日期，保留不限制：OR a.sfaadocdt IS NULL）
        sb.append("    and (a.sfaadocdt IS NULL OR a.sfaadocdt >= ADD_MONTHS(TRUNC(SYSDATE), -12)) ");

        // 可选：工单状态，逗号分隔；为空则不拼接该条件
        Object statusObj = params.get("sfaastus");
        List<String> statuses = new ArrayList<>();
        if (statusObj != null) {
            for (String t : String.valueOf(statusObj).split(",")) {
                String s = t.trim();
                // 仅允许字母数字下划线，防止拼接 SQL 注入
                if (!s.isEmpty() && s.matches("[A-Za-z0-9_]+")) {
                    statuses.add(s);
                }
            }
        }
        if (!statuses.isEmpty()) {
            StringBuilder inSql = new StringBuilder("    and a.sfaastus IN (");
            for (int i = 0; i < statuses.size(); i++) {
                if (i > 0) {
                    inSql.append(",");
                }
                inSql.append("'").append(statuses.get(i)).append("'");
            }
            inSql.append(") ");
            sb.append(inSql.toString());
        }

        // 可选精确匹配条件：工单号；为空则不拼接
        // 注：sfaa022/ sfaa023 不再用于过滤主查询（避免把"尚未关联来源单号"的推荐工单过滤掉），
        //     改由 Controller 用于计划档推荐逻辑（见 querySchedulableWorkOrders）
        // 列名固定（非用户输入），值用 #{} 绑定，避免 SQL 注入
        for (String col : new String[]{"sfaadocno"}) {
            Object v = params.get(col);
            if (v != null && !String.valueOf(v).trim().isEmpty()) {
                sb.append("    and a.").append(col).append(" = #{").append(col).append("} ");
            }
        }

        // 只返回「有来源单号 且 工单生产数量(sfaa012) 与 订单数量(xmdd005) 不一致」的工单：
        // 数量一致的说明工单与订单已一对一关联好，无需返回；仅返回有差异的
        // 注：左连接下若 sfaa022 指向的订单在 xmdd_t 中无匹配行，d.xmdd005 为 NULL，
        //     (sfaa012 - NULL) <> 0 结果为 NULL，该行也会被过滤掉（等同不返回）。
        //     若要连「有来源单号但查不到对应订单」的悬挂引用也返回，可改为 (a.sfaa012 - NVL(d.xmdd005,0)) <> 0
        sb.append("    and (a.sfaa012 - NVL(d.xmdd005,0)) <> 0 ");

        // 去掉已排完（可排数量 = 0）的工单
        sb.append("    and NVL(M.kpsl, a.sfaa012) > 0 ");
        sb.append("  order by a.sfaadocno ");
        sb.append(") x ");

        return sb.toString();
    }

    /**
     * 根据「来源单号(sfaa022) + 来源序号(sfaa023) + 品号(sfaa010)」查询对应的工单信息
     * 用于前端按「订单 + 序号 + 品号」关联对应的工单：
     *   与 listSchedulableByItem 不同，本方法以 sfaa022/sfaa023 作为主过滤条件（必填精确匹配），
     *   且【不】排除已排完(kpsl=0)的工单、也不限制工单开立日期，
     *   目的是把「已与该订单行关联」的工单都找出来提供给前端做关联展示。
     * 过滤条件：账套(sfaaent)/据点(sfaasite) 固定；品号(sfaa010) 必填；来源单号(sfaa022)/来源序号(sfaa023) 必填；
     *          工单状态(sfaastus) 可选，逗号分隔，空则不拼接该条件。
     * 返回列：sfaadocno(工单号)、sfaa010(生产品号)、sfaa012(生产数量)、kpsl(可排数量)、sfaa050(入库数量)、
     *         sfaa019(预计开工日期)、sfaa020(预计完工日期)、sfaa068(成本中心)、sfaastus(工单状态码)、
     *         sfaa022(来源单号)、sfaa023(来源序号)
     */
    public String listBySourceOrderItem(final Map<String, Object> params) {
        StringBuilder sb = new StringBuilder();
        sb.append("select x.* ");
        sb.append("from ( ");
        sb.append("  select a.sfaadocno, a.sfaa010, a.sfaa012, a.sfaa022, a.sfaa023, ");
        sb.append("         NVL(M.kpsl, a.sfaa012) kpsl, ");
        sb.append("         a.sfaa050, a.sfaa019, a.sfaa020, a.sfaa068, a.sfaastus ");
        sb.append("  from sfaa_t a ");
        sb.append("  left join ( ");
        sb.append("    select sfahuc001, ");
        sb.append("           case when (sfahuc003 - 订单数) < 0 then 0 else (sfahuc003 - 订单数) end kpsl ");
        sb.append("    from ( ");
        sb.append("      select sfahuc001, sfahuc003, sum(NVL(sfahuc011,0)) 订单数, count(1) ");
        sb.append("      from sfahuc_t ");
        sb.append("      where sfahucent = TO_NUMBER(#{sfaaent}) and sfahucsite = #{sfaasite} ");
        sb.append("      group by sfahuc001, sfahuc003 ");
        sb.append("    ) ");
        sb.append("  ) M on M.sfahuc001 = a.sfaadocno ");
        sb.append("  where a.sfaaent = TO_NUMBER(#{sfaaent}) ");
        sb.append("    and a.sfaasite = #{sfaasite} ");
        sb.append("    and a.sfaa010 = #{sfaa010} ");
        // 来源单号/来源序号：主过滤条件，精确匹配（必填，由 Controller 校验非空）
        sb.append("    and a.sfaa022 = #{sfaa022} ");
        sb.append("    and a.sfaa023 = #{sfaa023} ");

        // 可选：工单状态，逗号分隔；为空则不拼接该条件
        Object statusObj = params.get("sfaastus");
        List<String> statuses = new ArrayList<>();
        if (statusObj != null) {
            for (String t : String.valueOf(statusObj).split(",")) {
                String s = t.trim();
                // 仅允许字母数字下划线，防止拼接 SQL 注入
                if (!s.isEmpty() && s.matches("[A-Za-z0-9_]+")) {
                    statuses.add(s);
                }
            }
        }
        if (!statuses.isEmpty()) {
            StringBuilder inSql = new StringBuilder("    and a.sfaastus IN (");
            for (int i = 0; i < statuses.size(); i++) {
                if (i > 0) {
                    inSql.append(",");
                }
                inSql.append("'").append(statuses.get(i)).append("'");
            }
            inSql.append(") ");
            sb.append(inSql.toString());
        }

        sb.append("  order by a.sfaadocno ");
        sb.append(") x ");

        return sb.toString();
    }

    /**
     * 按订单行（来源单号 sfahuc004 + 来源序号 sfahuc005）查询已排工单合计数量
     *   ypgds = sum(sfahuc003)，即该订单行累计已排工单数
     * 用于判断订单行是否已排完：ypgds 与订单需求量比较，= 订单量即排完；< 订单量则可继续排
     * 过滤条件：账套(sfahucent)/据点(sfahucsite) 固定；来源单号(sfahuc004)/来源序号(sfahuc005) 必填
     */
    public String orderScheduledQty(final Map<String, Object> params) {
        StringBuilder sb = new StringBuilder();
        sb.append("select sfahuc004, sfahuc005, sum(NVL(sfahuc003, 0)) ypgds ");
        sb.append("from sfahuc_t ");
        sb.append("where sfahucent = TO_NUMBER(#{sfahucent}) ");
        sb.append("  and sfahucsite = #{sfahucsite} ");
        sb.append("  and sfahuc004 = #{sfahuc004} ");
        sb.append("  and sfahuc005 = #{sfahuc005} ");
        if (params.get("sfahuc002") != null
                && !String.valueOf(params.get("sfahuc002")).trim().isEmpty()) {
            sb.append("  and sfahuc002 = #{sfahuc002} ");
        }
        sb.append("group by sfahuc004, sfahuc005 ");
        return sb.toString();
    }

    /**
     * 按订单行（来源单号 sfahuc004 + 来源序号 sfahuc005）聚合每个品号(sfahuc002)已排工单数量
     *   ypgds = sum(NVL(sfahuc003, 0))，按 sfahuc002 分组
     * 用于 queryOrderBom：按 BOM 元件品号(bmba003) 取该品号已排工单数量，计算订单可排数量 ddkpsj
     * 过滤条件：账套(sfahucent)/据点(sfahucsite) 固定；来源单号(sfahuc004) 必填；来源序号(sfahuc005) 可选
     */
    public String orderScheduledQtyByItem(final Map<String, Object> params) {
        StringBuilder sb = new StringBuilder();
        sb.append("select sfahuc002, sum(NVL(sfahuc003, 0)) ypgds ");
        sb.append("from sfahuc_t ");
        sb.append("where sfahucent = TO_NUMBER(#{sfahucent}) ");
        sb.append("  and sfahucsite = #{sfahucsite} ");
        sb.append("  and sfahuc004 = #{sfahuc004} ");
        if (params.get("sfahuc005") != null
                && !String.valueOf(params.get("sfahuc005")).trim().isEmpty()) {
            sb.append("  and sfahuc005 = #{sfahuc005} ");
        }
        sb.append("group by sfahuc002 ");
        return sb.toString();
    }

    /**
     * 将 sfahuc_t 的 订单号(sfahuc004)/订单序号(sfahuc005)/预计开工(sfahuc006)/预计完工(sfahuc007)
     * 同步回写 sfaa_t 对应字段（sfaa022/sfaa023/sfaa019/sfaa020）
     * 匹配：sfaaent=ent and sfaasite=site and sfaadocno=docno（工单号务必对应，避免误更新）
     * 规则：源值非空 且（目标列当前值不同 或 目标列为空）时才更新该列，否则保持原值
     *   - 字符字段(022/023) 直接比较赋值
     *   - 日期字段(019/020) 用 TO_DATE(#{sd},'YYYY-MM-DD') 转换后比较/赋值，避免隐式转换风险
     */
    public String syncSfaaFromSfahuc(final Map<String, Object> params) {
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE sfaa_t SET ");
        // sfaa022(来源单号) 类型不定(CHAR/NUMBER)：用 TO_CHAR(sfaa022) 转字符比较，CASE 两分支均为字符
        sb.append("  sfaa022 = CASE WHEN #{o} IS NOT NULL AND (#{o} <> TO_CHAR(sfaa022) OR sfaa022 IS NULL) THEN #{o} ELSE TO_CHAR(sfaa022) END, ");
        // sfaa023(来源序号) 类型不定(CHAR/NUMBER)：用 TO_CHAR(sfaa023) 转字符比较，CASE 两分支均为字符，
        // 赋值给 NUMBER 列时走隐式转换，避免解析期 CASE 字符/数字类型冲突(ORA-00932)
        sb.append("  sfaa023 = CASE WHEN #{s} IS NOT NULL AND (#{s} <> TO_CHAR(sfaa023) OR sfaa023 IS NULL) THEN #{s} ELSE TO_CHAR(sfaa023) END, ");
        // sfaa019/020 为 DATE 型：用 TO_DATE 显式转换后比较/赋值
        // 注意：源值可能带时分秒（如 '2026-10-18 00:00:00'），需 SUBSTR(?,1,10) 截取前 10 位日期部分，否则 ORA-01830
        sb.append("  sfaa019 = CASE WHEN #{sd} IS NOT NULL AND (TO_DATE(SUBSTR(#{sd},1,10),'YYYY-MM-DD') <> sfaa019 OR sfaa019 IS NULL) THEN TO_DATE(SUBSTR(#{sd},1,10),'YYYY-MM-DD') ELSE sfaa019 END, ");
        sb.append("  sfaa020 = CASE WHEN #{ed} IS NOT NULL AND (TO_DATE(SUBSTR(#{ed},1,10),'YYYY-MM-DD') <> sfaa020 OR sfaa020 IS NULL) THEN TO_DATE(SUBSTR(#{ed},1,10),'YYYY-MM-DD') ELSE sfaa020 END ");
        sb.append("WHERE sfaaent = TO_NUMBER(#{ent}) AND sfaasite = #{site} AND sfaadocno = #{docno}");
        return sb.toString();
    }
}
