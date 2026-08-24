package com.itheima.mapper.primary;


import com.itheima.pojo.user;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper {
//    @Select("select loginid,LASTNAME name from HRMRESOURCE ")
    @Select("select 姓名 name,岗位 loginid,周报 zb from 检查工作周报 where 周报 is null ")

    public List<user> list();
}
