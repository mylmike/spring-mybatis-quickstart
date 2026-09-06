package com.itheima.mapper.second;

import com.itheima.pojo.sfajuc;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SfajucMapper {

    /**
     * 按关键字段查询 sfajuc_t 记录
     * 关键字：账套、据点、工单号、产线(sfajuc004)、排产日期（按日期截断匹配）
     */
    @Select("select * from sfajuc_t " +
            "where sfajucent = TO_NUMBER(#{sfajucent}) " +
            "and sfajucsite = #{sfajucsite} " +
            "and sfajuc001 = #{sfajuc001} " +
            "and sfajuc004 = #{sfajuc004} " +
            "and TRUNC(sfajuc007) = TO_DATE(#{sfajuc007}, 'YYYY-MM-DD')")
    List<sfajuc> findByKey(@Param("sfajucent") String sfajucent,
                           @Param("sfajucsite") String sfajucsite,
                           @Param("sfajuc001") String sfajuc001,
                           @Param("sfajuc004") String sfajuc004,
                           @Param("sfajuc007") String sfajuc007);

    /**
     * 新增 sfajuc_t 记录
     */
    @Insert("insert into sfajuc_t " +
            "(sfajucent, sfajucsite, sfajuc001, sfajuc003, sfajuc004, sfajuc007) " +
            "values (" +
            "TO_NUMBER(#{sfajucent}), #{sfajucsite}, #{sfajuc001}, " +
            "TO_NUMBER(#{sfajuc003}), #{sfajuc004}, " +
            "TO_TIMESTAMP(#{sfajuc007} || ' 00:00:00', 'YYYY-MM-DD HH24:MI:SS'))")
    int insert(sfajuc record);

    /**
     * 按关键字更新数量
     */
    @Update("update sfajuc_t set " +
            "sfajuc003 = TO_NUMBER(#{sfajuc003}) " +
            "where sfajucent = TO_NUMBER(#{sfajucent}) " +
            "and sfajucsite = #{sfajucsite} " +
            "and sfajuc001 = #{sfajuc001} " +
            "and sfajuc004 = #{sfajuc004} " +
            "and TRUNC(sfajuc007) = TO_DATE(#{sfajuc007}, 'YYYY-MM-DD')")
    int update(sfajuc record);

    /**
     * 删除 sfajuc_t 中某工单号的全部日计划记录
     * 用于删除 sfahuc_t 记录时同步清理（sfajucent=sfahucent, sfajucsite=sfahucsite, sfajuc001=sfahuc001）
     */
    @Delete("delete from sfajuc_t " +
            "where sfajucent = TO_NUMBER(#{sfajucent}) " +
            "and sfajucsite = #{sfajucsite} " +
            "and sfajuc001 = #{sfajuc001}")
    int deleteByDocno(@Param("sfajucent") String sfajucent,
                      @Param("sfajucsite") String sfajucsite,
                      @Param("sfajuc001") String sfajuc001);
}