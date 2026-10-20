package com.itheima.mapper.third;

import com.itheima.pojo.SrmClearConfig;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 自动清除在线用户定时配置表 Mapper（MySQL wms 库）
 */
@Mapper
public interface SrmClearConfigMapper {

    @Select("select id, enabled, clear_time as clearTime from wms.srm_clear_config where id = 1")
    SrmClearConfig selectConfig();

    @Update("update wms.srm_clear_config set enabled = #{enabled}, clear_time = #{clearTime} where id = 1")
    int updateConfig(@Param("enabled") boolean enabled, @Param("clearTime") String clearTime);

    @Insert("insert into wms.srm_clear_config(id, enabled, clear_time) values (1, #{enabled}, #{clearTime})")
    int insertConfig(@Param("enabled") boolean enabled, @Param("clearTime") String clearTime);
}
