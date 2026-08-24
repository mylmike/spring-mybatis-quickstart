package com.itheima.mapper.second;

import com.itheima.pojo.sfaa;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SfaaMapper {

    @Select("select a.*, b.ooefl003 from SFAA_T a left join ooefl_t b on b.ooeflent=a.sfaaent and b.ooefl001=a.sfaa068 and b.ooefl002='zh_CN' where a.sfaaent=60 and a.sfaa022=#{orderNo}")
    List<sfaa> listByOrderNo(@Param("orderNo") String orderNo);

}
