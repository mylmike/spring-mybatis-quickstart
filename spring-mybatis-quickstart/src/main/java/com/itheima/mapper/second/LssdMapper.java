package com.itheima.mapper.second;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Map;

/**
 * Oracle lssd_t 表 Mapper（ERP 采购送货收货记录）
 */
@Mapper
public interface LssdMapper {

    /**
     * 按账套 + 据点 + 采购单号 + 采购序号查询 lssd_t 记录
     */
    @Select("select * from lssd_t where lssdent = #{ent} and lssdsite = #{site} and lssddocno = #{purchaseNo} and lssdseq = #{purchaseSeq}")
    Map<String, Object> findByPurchase(@Param("ent") String ent,
                                       @Param("site") String site,
                                       @Param("purchaseNo") String purchaseNo,
                                       @Param("purchaseSeq") String purchaseSeq);

    /**
     * 更新 lssd_t 收货数量（lssd005=0 时更新）
     */
    @Update("update lssd_t set lssd005 = #{deliveryQty}, lssd003 = sysdate " +
            "where lssdent = #{ent} and lssdsite = #{site} and lssddocno = #{purchaseNo} and lssdseq = #{purchaseSeq}")
    int updateReceiptQty(@Param("ent") String ent,
                         @Param("site") String site,
                         @Param("purchaseNo") String purchaseNo,
                         @Param("purchaseSeq") String purchaseSeq,
                         @Param("deliveryQty") String deliveryQty);

    /**
     * 减少 lssd_t 送货数量（删除送货单时，回退未收货部分）
     */
    @Update("update lssd_t set lssd004 = lssd004 - #{deliveryQty}, lssd003 = sysdate " +
            "where lssdent = #{ent} and lssdsite = #{site} and lssddocno = #{purchaseNo} and lssdseq = #{purchaseSeq}")
    int reduceDeliveryQty(@Param("ent") String ent,
                          @Param("site") String site,
                          @Param("purchaseNo") String purchaseNo,
                          @Param("purchaseSeq") String purchaseSeq,
                          @Param("deliveryQty") String deliveryQty);
}
