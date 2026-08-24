package com.itheima.mapper.second;

import com.itheima.pojo.PmdsdtRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Oracle pmds_t + pmdt_t 收货单 Mapper
 */
@Mapper
public interface PmdsMapper {

    @Select("<script>" +
            "select p.pmdsent, p.pmdsdocno, p.pmds000, p.pmds010, " +
            "t.pmdtent, t.pmdtdocno, t.pmdt001, t.pmdt002, t.pmdt020, t.pmdtseq " +
            "from pmds_t p " +
            "join pmdt_t t on p.pmdsent = t.pmdtent and p.pmdsdocno = t.pmdtdocno " +
            "where p.pmds000 = '1' " +
            "<if test='deliverNos != null and deliverNos.size() > 0'>" +
            "and p.pmds010 in " +
            "<foreach collection='deliverNos' item='no' open='(' separator=',' close=')'>" +
            "#{no}" +
            "</foreach>" +
            "</if>" +
            "</script>")
    List<PmdsdtRow> findByDeliverNos(@Param("deliverNos") List<String> deliverNos);
}
