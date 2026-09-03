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
     */
    @SelectProvider(type = OrderSqlProvider.class, method = "queryUndeliveredOrders")
    List<Map<String, Object>> queryUndeliveredOrders(Map<String, Object> params);

}
