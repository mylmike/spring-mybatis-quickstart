package com.itheima.mapper.second;

import com.itheima.pojo.WorkOrderRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;
import java.util.Map;

@Mapper
public interface SfbaMapper {

    /**
     * 工单查询（sfba_t LEFT JOIN sfaa_t），返回单头+单身扁平行
     * 参数统一用 Map 传入，所有条件均可选：
     *   null / 空串 / 纯空白 视为条件未设置，不拼接 where
     *   row_max：未传 / <=0 / 非法 时默认 200
     * 详见 SfbaSqlProvider
     */
    @SelectProvider(type = SfbaSqlProvider.class, method = "queryWorkOrder")
    List<WorkOrderRow> queryWorkOrder(Map<String, Object> params);
}
