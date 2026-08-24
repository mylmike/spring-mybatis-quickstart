package com.itheima.mapper.second;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface PmdlMapper {

    /**
     * 查询采购员姓名（ooag_t.ooag011）
     */
    @Select("select ooag011 from pmdl_t " +
            "left join ooag_t on ooag001 = pmdl002 and ooagent = pmdlent " +
            "where pmdlent = #{ent} and pmdldocno = #{purchaseNo}")
    String findPurchaserName(@Param("ent") String ent,
                             @Param("purchaseNo") String purchaseNo);
}
