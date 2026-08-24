package com.itheima.mapper.second;

import com.itheima.pojo.WorkOrderRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface SfbaMapper {

    @Select("<script>" +
            "select * from (" +
            "  select sfbaent, sfaasite, sfaadocno, sfaastus, sfaa010, sfaa012, sfaa068, " +
            "  sfaadocdt, sfaa019, sfaa020, sfaa022, sfaa023, sfaa050, sfaa047, " +
            "  sfbaseq, sfba006, sfba023, sfba024, sfba013, sfba017, sfba025, sfba009, sfba028 " +
            "  from sfba_t " +
            "  left join sfaa_t on sfbaent=sfaaent and sfbasite=sfaasite and sfbadocno=sfaadocno " +
            "  <where>" +
            "  <if test='sfbaent != null and sfbaent != \"\"'>and sfbaent=#{sfbaent}</if>" +
            "  <if test='sfaasite != null and sfaasite != \"\"'>and sfaasite=#{sfaasite}</if>" +
            "  <if test='sfaadocno != null and sfaadocno != \"\"'>and sfaadocno=#{sfaadocno}</if>" +
            "  <if test='sfaastus != null and sfaastus != \"\"'>and sfaastus=#{sfaastus}</if>" +
            "  <if test='sfaa010 != null and sfaa010 != \"\"'>and sfaa010=#{sfaa010}</if>" +
            "  <if test='sfaa012 != null and sfaa012 != \"\"'>and sfaa012=#{sfaa012}</if>" +
            "  <if test='sfaa068 != null and sfaa068 != \"\"'>and sfaa068=#{sfaa068}</if>" +
            "  <if test='sfaadocdt != null and sfaadocdt != \"\"'>and sfaadocdt=#{sfaadocdt}</if>" +
            "  <if test='sfaa019 != null and sfaa019 != \"\"'>and sfaa019=#{sfaa019}</if>" +
            "  <if test='sfaa020 != null and sfaa020 != \"\"'>and sfaa020=#{sfaa020}</if>" +
            "  <if test='sfaa022 != null and sfaa022 != \"\"'>and sfaa022=#{sfaa022}</if>" +
            "  <if test='sfaa023 != null and sfaa023 != \"\"'>and sfaa023=#{sfaa023}</if>" +
            "  <if test='sfaa050 != null and sfaa050 != \"\"'>and sfaa050=#{sfaa050}</if>" +
            "  <if test='sfaa047 != null and sfaa047 != \"\"'>and sfaa047=#{sfaa047}</if>" +
            "  <if test='sfbaseq != null and sfbaseq != \"\"'>and sfbaseq=#{sfbaseq}</if>" +
            "  <if test='sfba006 != null and sfba006 != \"\"'>and sfba006=#{sfba006}</if>" +
            "  <if test='sfba023 != null and sfba023 != \"\"'>and sfba023=#{sfba023}</if>" +
            "  <if test='sfba024 != null and sfba024 != \"\"'>and sfba024=#{sfba024}</if>" +
            "  <if test='sfba013 != null and sfba013 != \"\"'>and sfba013=#{sfba013}</if>" +
            "  <if test='sfba017 != null and sfba017 != \"\"'>and sfba017=#{sfba017}</if>" +
            "  <if test='sfba025 != null and sfba025 != \"\"'>and sfba025=#{sfba025}</if>" +
            "  <if test='sfba009 != null and sfba009 != \"\"'>and sfba009=#{sfba009}</if>" +
            "  <if test='sfba028 != null and sfba028 != \"\"'>and sfba028=#{sfba028}</if>" +
            "  </where>" +
            "  order by sfaadocdt desc" +
            ")" +
            "<if test='row_max != null and row_max > 0'> where rownum &lt;= #{row_max}</if>" +
            "</script>")
    List<WorkOrderRow> queryWorkOrder(Map<String, Object> params);
}
