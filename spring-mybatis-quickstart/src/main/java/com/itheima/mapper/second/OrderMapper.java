package com.itheima.mapper.second;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {

    /**
     * 订单未交货查询清单（用于前端下拉框绑定）
     * 基于 xmdd_t / xmdc_t / xmda_t 三表关联
     * 限制：仅返回开单日期(xmdadocdt)最近 6 个月的记录
     */
    @SelectProvider(type = OrderSqlProvider.class, method = "queryUndeliveredOrders")
    List<Map<String, Object>> queryUndeliveredOrders(Map<String, Object> params);

    /**
     * 查询未完成订单（订单量 > 已出货量，即尚未出完货）
     * 基于 xmdd_t / xmda_t / xmdc_t / pmaal_t / ooag_t 关联
     * 参数（Map）：xmddent(默认60)、xmddsite(默认NBYL)、
     *            xmdd011_start/xmdd011_end(订单交期范围,可选)、
     *            xmdadocdt_start/xmdadocdt_end(开单日期范围,可选)
     * 返回：List<Map>，列含 xmdddocno/xmddseq/xmdd001/xmdd006/xmdd014/undqty/xmdd011/
     *                      pmaal004/ooag011/salesempno
     */
    @SelectProvider(type = OrderSqlProvider.class, method = "queryUnfinishedOrders")
    List<Map<String, Object>> queryUnfinishedOrders(Map<String, Object> params);

}
