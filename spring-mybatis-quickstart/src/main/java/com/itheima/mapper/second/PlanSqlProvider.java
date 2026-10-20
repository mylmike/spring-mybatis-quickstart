package com.itheima.mapper.second;

import java.util.Map;

/**
 * ERP 生产计划档（pspc_t / pspa_t / psph_t）相关动态 SQL 构造器
 * 用于 querySchedulableWorkOrders 的「推荐/默认工单号」查找：
 *   根据前端传入的来源单号(sfaa022)/来源序号(sfaa023)，从计划档得到对应的工单号集合
 */
public class PlanSqlProvider {

    /**
     * 查询最近的 ERP 计划编号：max(pspc002)
     * 参数：ent(账套, 默认60)、site(据点, 默认NBYL)
     * 用于定位当前生效的计划（第二步）
     */
    public String latestPlanNo(Map<String, Object> params) {
        StringBuilder sb = new StringBuilder();
        sb.append("select max(pspc002) from pspc_t ");
        sb.append("where pspcent = TO_NUMBER(#{ent}) ");
        sb.append("  and pspcsite = #{site} ");
        // pspc001 固定为 'PMC'（生产计划类别），取最新生产计划编号
        sb.append("  and pspc001 = 'PMC' ");
        return sb.toString();
    }

    /**
     * 查询计划档，得到与指定来源单号(sfaa022)/来源序号(sfaa023) 对应的工单号集合（记录集A）
     * 关联 pspa_t（生产计划明细）与 psph_t（生产计划），按：
     *   账套(pspaent=ent) / 据点(pspasite=site) / 计划编号(pspa002=planNo) /
     *   生产品号(psph017=前端sfaa010) / 来源单号 / 来源序号 匹配
     * 来源单号/来源序号由 psph009 按 '-' 拆分（取第2、第3个 '-' 之间的分段）得到
     * 返回列：工单号(pspa006)、需求数量(psph020)、可供给数量(psph026)
     */
    public String planWorkOrderNos(Map<String, Object> params) {
        StringBuilder sb = new StringBuilder();
        // 工单号：若 pspa006 为空（库存供应无对应工单），用 '库存' 标识，作为默认供应返回
        sb.append("select NVL(trim(pspa006),'库存') 工单号, psph020 需求数量, psph026 可供给数量 from ( ");
        sb.append("  select pspa012, pspa006, psph009, psph010, psph011, ");
        sb.append("         substr(psph009,1,instr(psph009,'-',1,2)-1) 来源单号, ");
        sb.append("         substr(psph009,instr(psph009,'-',1,2)+1,instr(psph009,'-',1,3)-instr(psph009,'-',1,2)-1) 来源序号, ");
        sb.append("         psph020, psph026 ");
        sb.append("  from pspa_t ");
        // 工单号关联：pspa006=psph010；库存行两端均为 NULL，Oracle 中 NULL=NULL 不成立，
        // 故补充 (pspa006 IS NULL AND psph010 IS NULL) 分支，使库存行也能 join 到 psph_t 取 psph020/psph026
        sb.append("  left join psph_t on psphent=pspaent and psphsite=pspasite and psph001=pspa001 and psph002=pspa002 and psph017=pspa012 and (pspa006=psph010 or (pspa006 is null and psph010 is null)) ");
        // 供应来源：在制工单(M1) 与 库存(I) 都可作为默认供应
        sb.append("  where pspaent=TO_NUMBER(#{ent}) and pspasite=#{site} and (pspa020='M1' or pspa020='I') ");
        sb.append("    and psph017=#{sfaa010} ");
        sb.append("    and pspa002=#{planNo} ");
        sb.append(") where 来源单号=#{sfaa022} and 来源序号=#{sfaa023} ");
        return sb.toString();
    }
}
