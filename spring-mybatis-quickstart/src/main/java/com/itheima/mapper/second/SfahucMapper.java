package com.itheima.mapper.second;

import com.itheima.pojo.sfahuc;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SfahucMapper {

    @Select("select * from sfahuc_t where sfahucdocno=#{sfahucdocno} order by sfahucseq")
    List<sfahuc> listByDocno(@Param("sfahucdocno") String sfahucdocno);

    @Select("select * from sfahuc_t " +
            "where sfahucent=TO_NUMBER(#{sfahucent}) and sfahucsite=#{sfahucsite} " +
            "and sfahucdocno=#{sfahucdocno} and sfahucseq=TO_NUMBER(#{sfahucseq})")
    sfahuc findByKey(@Param("sfahucent") String sfahucent,
                     @Param("sfahucsite") String sfahucsite,
                     @Param("sfahucdocno") String sfahucdocno,
                     @Param("sfahucseq") String sfahucseq);

    @Select("select * from sfahuc_t where sfahucdocno=#{sfahucdocno} and sfahucseq=TO_NUMBER(#{sfahucseq})")
    List<sfahuc> findByDocnoAndSeq(@Param("sfahucdocno") String sfahucdocno,
                                    @Param("sfahucseq") String sfahucseq);

    @Insert("insert into sfahuc_t " +
            "(sfahucent,sfahucsite,sfahucdocno,sfahucseq,sfahuc001,sfahuc002,sfahuc003,sfahuc004,sfahuc005,sfahuc006,sfahuc007,sfahuc008,sfahuc009) " +
            "values (" +
            "TO_NUMBER(#{sfahucent}),#{sfahucsite},#{sfahucdocno},TO_NUMBER(#{sfahucseq})," +
            "#{sfahuc001},#{sfahuc002},TO_NUMBER(#{sfahuc003}),#{sfahuc004},TO_NUMBER(#{sfahuc005})," +
            "TO_DATE(#{sfahuc006},'YYYY-MM-DD'),TO_DATE(#{sfahuc007},'YYYY-MM-DD'),#{sfahuc008},TO_NUMBER(#{sfahuc009}))")
    int insert(sfahuc record);

    @Update("update sfahuc_t set " +
            "sfahucent=TO_NUMBER(#{sfahucent}),sfahucsite=#{sfahucsite}," +
            "sfahuc001=#{sfahuc001},sfahuc002=#{sfahuc002}," +
            "sfahuc003=TO_NUMBER(#{sfahuc003})," +
            "sfahuc004=#{sfahuc004},sfahuc005=TO_NUMBER(#{sfahuc005})," +
            "sfahuc006=TO_DATE(#{sfahuc006},'YYYY-MM-DD'),sfahuc007=TO_DATE(#{sfahuc007},'YYYY-MM-DD')," +
            "sfahuc008=#{sfahuc008},sfahuc009=TO_NUMBER(#{sfahuc009}) " +
            "where sfahucdocno=#{sfahucdocno} and sfahucseq=TO_NUMBER(#{sfahucseq})")
    int update(sfahuc record);

    @Delete("delete from sfahuc_t " +
            "where sfahucent=TO_NUMBER(#{sfahucent}) and sfahucsite=#{sfahucsite} " +
            "and sfahucdocno=#{sfahucdocno} and sfahucseq=TO_NUMBER(#{sfahucseq}) " +
            "and sfahuc001=#{sfahuc001} and sfahuc002=#{sfahuc002}")
    int deleteByKey(@Param("sfahucent") String sfahucent,
                    @Param("sfahucsite") String sfahucsite,
                    @Param("sfahucdocno") String sfahucdocno,
                    @Param("sfahucseq") String sfahucseq,
                    @Param("sfahuc001") String sfahuc001,
                    @Param("sfahuc002") String sfahuc002);
}
