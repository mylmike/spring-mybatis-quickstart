package com.itheima.mapper.second;

import com.itheima.pojo.sfaa;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;

import java.util.List;
import java.util.Map;

@Mapper
public interface SfaaMapper {

    /**
     * 按订单号(+成本中心)查询工单
     * 参数统一用 Map 传入（与 DsdataMapper 既有模式一致）：
     *   orderNo  来源单号 sfaa022，可选
     *   sfaa068  成本中心，可选
     *   rowMax   期望行数，可选
     */
    @SelectProvider(type = SfaaSqlProvider.class, method = "listByOrderNo")
    List<sfaa> listByOrderNo(Map<String, Object> params);

    /**
     * 查询「可排产」工单（去掉已排完的工单，已排未排完的返回剩余可排数量）
     * 参数（Map 传入）：
     *   sfaaent   账套，默认 60
     *   sfaasite  营运据点，默认 NBYL
     *   sfaa010   生产品号，必填（前端传入）
     *   sfaastus  工单状态，逗号分隔，可选；空则不加该条件
     * 返回：List<Map>，列含 sfaadocno / sfaa010 / sfaa012 / kpsl / sfaa050 / sfaa019 / sfaa020
     */
    @SelectProvider(type = SfaaSqlProvider.class, method = "listSchedulableByItem")
    List<Map<String, Object>> listSchedulableByItem(Map<String, Object> params);

    /**
     * 根据「来源单号(sfaa022) + 来源序号(sfaa023) + 品号(sfaa010)」查询对应的工单信息
     * 用于前端按「订单 + 序号 + 品号」关联对应的工单。
     * 参数（Map 传入）：
     *   sfaaent   账套，默认 60
     *   sfaasite  营运据点，默认 NBYL
     *   sfaa010   生产品号，必填
     *   sfaa022   来源单号，必填
     *   sfaa023   来源序号，必填
     * 返回：List<Map>，列含 sfaadocno / sfaa010 / sfaa012 / kpsl / sfaa050 / sfaa019 / sfaa020
     *       / sfaa068 / sfaastus / sfaa022 / sfaa023 / xmdd005
     */
    @SelectProvider(type = SfaaSqlProvider.class, method = "listBySourceOrderItem")
    List<Map<String, Object>> listBySourceOrderItem(Map<String, Object> params);

    /**
     * 查询最近的 ERP 计划编号：max(pspc002)
     * 参数：ent(账套, 默认60)、site(据点, 默认NBYL)
     * 返回最新计划编号（字符串）；无记录时返回 null
     */
    @SelectProvider(type = PlanSqlProvider.class, method = "latestPlanNo")
    String latestPlanNo(Map<String, Object> params);

    /**
     * 按来源单号(sfaa022)/来源序号(sfaa023) 查询计划档，得到对应的工单号集合（记录集A）
     * 参数：ent、site、sfaa010(生产品号)、planNo(计划编号)、sfaa022、sfaa023
     * 返回：List<Map>，列含 工单号(pspa006) / 需求数量(psph020) / 可供给数量(psph026)
     */
    @SelectProvider(type = PlanSqlProvider.class, method = "planWorkOrderNos")
    List<Map<String, Object>> planWorkOrderNos(Map<String, Object> params);

    /**
     * 按订单行（来源单号 sfahuc004 + 来源序号 sfahuc005）查询已排工单合计数量
     * 用于判断订单行是否已排完：ypgds(已排工单数) = 0 表示未排，> 0 表示已排
     * 参数（Map 传入）：
     *   sfahucent   账套，默认 60
     *   sfahucsite  营运据点，默认 NBYL
     *   sfahuc004   来源单号，必填
     *   sfahuc005   来源序号，必填
     * 返回：List<Map>，列含 sfahuc004 / sfahuc005 / ypgds（已排工单数）
     */
    @SelectProvider(type = SfaaSqlProvider.class, method = "orderScheduledQty")
    List<Map<String, Object>> queryOrderScheduledQty(Map<String, Object> params);

    /**
     * 按订单行（来源单号 sfahuc004 + 来源序号 sfahuc005）聚合每个品号(sfahuc002)已排工单数量
     *   ypgds = sum(NVL(sfahuc003, 0))，按 sfahuc002 分组
     * 用于 queryOrderBom：按 BOM 元件品号(bmba003) 取该品号已排工单数量，计算订单可排数量 ddkpsj
     * 参数（Map 传入）：
     *   sfahucent   账套，默认 60
     *   sfahucsite  营运据点，默认 NBYL
     *   sfahuc004   来源单号，必填
     *   sfahuc005   来源序号，可选（为空则不拼接）
     * 返回：List<Map>，列含 sfahuc002（品号）/ ypgds（已排工单数量）
     */
    @SelectProvider(type = SfaaSqlProvider.class, method = "orderScheduledQtyByItem")
    List<Map<String, Object>> queryOrderScheduledQtyByItem(Map<String, Object> params);

    /**
     * 将 sfahuc_t 中某工单的 订单号/订单序号/预计开工/预计完工 同步回写 sfaa_t
     * 匹配条件：sfaaent=ent and sfaasite=site and sfaadocno=docno（工单号务必对应）
     * 仅在源值非空且（目标列当前值不同 或 目标列为空）时才更新对应列，否则保持原值
     */
    @UpdateProvider(type = SfaaSqlProvider.class, method = "syncSfaaFromSfahuc")
    int syncSfaaFromSfahuc(Map<String, Object> params);

}
