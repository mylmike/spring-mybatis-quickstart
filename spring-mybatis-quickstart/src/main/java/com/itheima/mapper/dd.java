package com.itheima.mapper;

import java.util.List;

import com.itheima.pojo.sfaa;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.itheima.pojo.ddmx;
import org.apache.ibatis.annotations.Select;


import java.util.List;
@Mapper
public interface dd {
    @DS("dsdata")
    @Select("select * from SFAA_T where sfaaent=60 and sfaa022=#{orderNo}  ")
    public   List<sfaa> listByOrderNo(@Param("orderNo") String orderNo);


}
