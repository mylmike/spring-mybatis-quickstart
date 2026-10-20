package com.itheima.mapper.third;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * MySQL srm_login_number 表 Mapper.
 */
@Mapper
public interface SrmLoginNumberMapper {

    @Update("set sql_safe_updates = 0")
    int disableSafeUpdates();

    @Update("set sql_safe_updates = 1")
    int enableSafeUpdates();

    @Update("delete from wms.srm_login_number")
    int clearAll();

    @Update("delete from wms.srm_login_number where name = #{name}")
    int clearByName(@Param("name") String name);
}
