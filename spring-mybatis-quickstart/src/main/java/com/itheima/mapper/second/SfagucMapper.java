package com.itheima.mapper.second;

import com.itheima.pojo.sfaguc;
import org.apache.ibatis.annotations.*;

/**
 * sfaguc_t 订单需求/供给汇总表 Mapper（dsdata/second）
 * 主键：sfagucent + sfagucsite + sfaguc002（订单号）+ sfaguc005（序号/订单序号）
 *   sfaguc003 需求总数：queryOrderBom 展开后，按 订单号+订单序号 合计所有「订单需求用量」
 *   sfaguc004 供给总数：saveSfahuc 后，按 订单号+订单序号 合计 sfahuc_t 的排程数量 SUM(sfahuc016)
 */
@Mapper
public interface SfagucMapper {

    /** 按主键（账套+据点+订单号+序号）查询单行；不存在返回 null */
    @Select("select * from sfaguc_t " +
            "where sfagucent=TO_NUMBER(#{sfagucent}) and sfagucsite=#{sfagucsite} " +
            "and sfaguc002=#{sfaguc002} " +
            // 序号为空时按 IS NULL 匹配（原写法 TO_NUMBER(NULL) 使条件恒假，导致永远走 insert 撞主键）
            "and ((NULLIF(#{sfaguc005},'') IS NULL and sfaguc005 IS NULL) " +
            "     or sfaguc005=TO_NUMBER(NULLIF(#{sfaguc005},'')))")
    sfaguc findByEntSiteDocnoSeq(@Param("sfagucent") String sfagucent,
                                 @Param("sfagucsite") String sfagucsite,
                                 @Param("sfaguc002") String sfaguc002,
                                 @Param("sfaguc005") String sfaguc005);

    /** 新增一行（需求总数/供给总数空值时落库为 0） */
    @Insert("insert into sfaguc_t (sfagucent,sfagucsite,sfaguc002,sfaguc003,sfaguc004,sfaguc005) " +
            "values (TO_NUMBER(#{sfagucent}),#{sfagucsite},#{sfaguc002}," +
            "NVL(TO_NUMBER(NULLIF(#{sfaguc003},'')),0),NVL(TO_NUMBER(NULLIF(#{sfaguc004},'')),0)," +
            "TO_NUMBER(NULLIF(#{sfaguc005},'')))")
    int insert(sfaguc record);

    /** 按主键仅更新「需求总数」sfaguc003，不触碰 sfaguc004 */
    @Update("update sfaguc_t set sfaguc003=TO_NUMBER(NULLIF(#{sfaguc003},'')) " +
            "where sfagucent=TO_NUMBER(#{sfagucent}) and sfagucsite=#{sfagucsite} " +
            "and sfaguc002=#{sfaguc002} " +
            "and ((NULLIF(#{sfaguc005},'') IS NULL and sfaguc005 IS NULL) " +
            "     or sfaguc005=TO_NUMBER(NULLIF(#{sfaguc005},'')))")
    int updateDemand(sfaguc record);

    /** 按主键仅更新「供给总数」sfaguc004，不触碰 sfaguc003 */
    @Update("update sfaguc_t set sfaguc004=TO_NUMBER(NULLIF(#{sfaguc004},'')) " +
            "where sfagucent=TO_NUMBER(#{sfagucent}) and sfagucsite=#{sfagucsite} " +
            "and sfaguc002=#{sfaguc002} " +
            "and ((NULLIF(#{sfaguc005},'') IS NULL and sfaguc005 IS NULL) " +
            "     or sfaguc005=TO_NUMBER(NULLIF(#{sfaguc005},'')))")
    int updateSupply(sfaguc record);
}
