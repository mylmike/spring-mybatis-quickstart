package com.itheima.mapper.second;

import com.itheima.pojo.sfahuc;
import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface SfahucMapper {

    /**
     * 按可选条件查询 sfahuc_t 明细
     * 参数统一用 Map 传入（与 SfaaMapper 既有 Provider 模式一致）：
     *   sfahucdocno  单号，可选
     *   sfahuc008    可选
     *   sfahuc004    订单号，可选
     *   sfahuc005    订单序号，可选
     *   sfahuc001    工单号，可选
     * null / 空串 / 纯空白 均视为条件未设置
     */
    @SelectProvider(type = SfahucSqlProvider.class, method = "listByDocno")
    List<sfahuc> listByDocno(Map<String, Object> params);

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

    /**
     * 按判重四字段查询：账套 + 据点 + 工单号(sfahuc001) + 品号(sfahuc002)
     * 一个工单号通常对应多个品号，故返回 List
     * 与 /saveSfahuc 的保存逻辑配套
     */
    @Select("select * from sfahuc_t " +
            "where sfahucent=TO_NUMBER(#{sfahucent}) and sfahucsite=#{sfahucsite} " +
            "and sfahuc001=#{sfahuc001} and sfahuc002=#{sfahuc002}")
    List<sfahuc> findByEntSiteDocno(@Param("sfahucent") String sfahucent,
                                    @Param("sfahucsite") String sfahucsite,
                                    @Param("sfahuc001") String sfahuc001,
                                    @Param("sfahuc002") String sfahuc002);

    @Insert("insert into sfahuc_t " +
            "(sfahucent,sfahucsite,sfahucdocno,sfahucseq,sfahuc001,sfahuc002,sfahuc003,sfahuc004,sfahuc005,sfahuc006,sfahuc007,sfahuc008,sfahuc009,sfahuc010) " +
            "values (" +
            "TO_NUMBER(#{sfahucent}),#{sfahucsite},#{sfahucdocno},TO_NUMBER(#{sfahucseq})," +
            "#{sfahuc001},#{sfahuc002},TO_NUMBER(#{sfahuc003}),#{sfahuc004},TO_NUMBER(#{sfahuc005})," +
            "TO_DATE(#{sfahuc006},'YYYY-MM-DD'),TO_DATE(#{sfahuc007},'YYYY-MM-DD'),#{sfahuc008},TO_NUMBER(#{sfahuc009})," +
            "#{sfahuc010})")
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

    /**
     * 按表主键更新：账套 + 据点 + 工单号 + 品号
     * 主键四列（sfahucent + sfahucsite + sfahuc001 + sfahuc002），用于精确定位到单行
     * 注意：set 中【不包含】主键字段，避免把多行改成相同主键导致 ORA-00001
     */
    @Update("update sfahuc_t set " +
            "sfahucdocno=#{sfahucdocno},sfahucseq=TO_NUMBER(#{sfahucseq})," +
            "sfahuc003=TO_NUMBER(#{sfahuc003})," +
            "sfahuc004=#{sfahuc004},sfahuc005=TO_NUMBER(#{sfahuc005})," +
            "sfahuc006=TO_DATE(#{sfahuc006},'YYYY-MM-DD'),sfahuc007=TO_DATE(#{sfahuc007},'YYYY-MM-DD')," +
            "sfahuc008=#{sfahuc008},sfahuc009=TO_NUMBER(#{sfahuc009})," +
            "sfahuc010=#{sfahuc010} " +
            "where sfahucent=TO_NUMBER(#{sfahucent}) and sfahucsite=#{sfahucsite} " +
            "and sfahuc001=#{sfahuc001} and sfahuc002=#{sfahuc002}")
    int updateByPk(sfahuc record);

    /**
     * 删除 sfahuc_t 记录
     * 固定条件：sfahucent(账套)、sfahucsite(营运据点)、sfahuc001、sfahuc002
     * 可选条件：sfahuc008(成本中心)，空值不拼接
     * 已去掉 sfahucdocno、sfahucseq
     */
    @DeleteProvider(type = SfahucDeleteSqlProvider.class, method = "deleteByKey")
    int deleteByKey(Map<String, Object> params);
}
