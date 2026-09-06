package com.itheima.mapper.second;

import com.itheima.pojo.sfaa;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.SelectProvider;

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

}
