package com.itheima.mapper.third;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * MySQL srm_delivery_head 表 Mapper
 */
@Mapper
public interface SrmDeliveryHeadMapper {

    /**
     * 更新送货单头状态为 4（完成收货），并标记更新时间
     */
    @Update("update srm_delivery_head set status = '4', remark = now() " +
            "where delivery_no = #{deliveryNo} and ent = #{ent} and site = #{site} and status <> '4'")
    int updateStatusToComplete(@Param("deliveryNo") String deliveryNo,
                               @Param("ent") String ent,
                               @Param("site") String site);

    /**
     * 查询送货单头状态
     */
    @Select("select status from srm_delivery_head where delivery_no = #{deliveryNo} and ent = #{ent} and site = #{site}")
    String findStatus(@Param("deliveryNo") String deliveryNo,
                      @Param("ent") String ent,
                      @Param("site") String site);

    /**
     * 删除一条送货单头记录
     */
    @Delete("delete from srm_delivery_head where ent = #{ent} and site = #{site} and delivery_no = #{deliveryNo}")
    int deleteByDeliveryNo(@Param("ent") String ent,
                           @Param("site") String site,
                           @Param("deliveryNo") String deliveryNo);
}
