package com.itheima.mapper.second;

import com.itheima.pojo.sfahuc;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
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

    /**
     * 查询 sfajuc_t 按 工单号+产线+日期 汇总的排产数量（排产子表数据）
     * 返回 List<Map>，列含 sfajuc001 / sfajuc004 / PAICHANDATE / PAICHANQTY
     * 由 Controller 按 (sfajuc001 + 产线) 分组挂到对应 sfahuc 主表记录下
     */
    @SelectProvider(type = SfahucSqlProvider.class, method = "listSfajucPlan")
    List<Map<String, Object>> listSfajucPlan(Map<String, Object> params);

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

    /**
     * 按 订单号+订单序号 汇总该订单在 sfahuc_t 的排程数量合计 SUM(sfahuc016)（供给总数）
     * 条件：sfahucent=账套 and sfahucsite=据点 and sfahuc004=订单号 and sfahuc005=订单序号
     * 用于 saveSfahuc 保存后回写 sfaguc_t.sfaguc004
     */
    @Select("select NVL(SUM(sfahuc016),0) from sfahuc_t " +
            "where sfahucent=TO_NUMBER(#{sfahucent}) and sfahucsite=#{sfahucsite} " +
            "and sfahuc004=#{sfahuc004} " +
            // 序号为空时按 IS NULL 匹配，避免 TO_NUMBER(NULL) 使条件恒不成立导致查不到/插不进
            "and ((NULLIF(#{sfahuc005},'') IS NULL and sfahuc005 IS NULL) " +
            "     or sfahuc005=TO_NUMBER(NULLIF(#{sfahuc005},'')))")
    BigDecimal sumScheduleQtyByOrderSeq(@Param("sfahucent") String sfahucent,
                                        @Param("sfahucsite") String sfahucsite,
                                        @Param("sfahuc004") String sfahuc004,
                                        @Param("sfahuc005") String sfahuc005);

    /**
     * 按 账套+据点+订单号(sfahuc004)+订单序号(sfahuc005)+品号(sfahuc002) 五字段
     * 汇总 sfahuc_t 的排程数量合计 SUM(sfahuc016)
     * 用于 saveSfahuc 保存前校验「排程数量是否超出订单需求数量」
     */
    @Select("select NVL(SUM(sfahuc016),0) from sfahuc_t " +
            "where sfahucent=TO_NUMBER(#{sfahucent}) and sfahucsite=#{sfahucsite} " +
            "and sfahuc004=#{sfahuc004} " +
            // 序号为空时按 IS NULL 匹配，避免 TO_NUMBER(NULL) 使条件恒不成立导致查不到/插不进
            "and ((NULLIF(#{sfahuc005},'') IS NULL and sfahuc005 IS NULL) " +
            "     or sfahuc005=TO_NUMBER(NULLIF(#{sfahuc005},''))) " +
            "and sfahuc002=#{sfahuc002}")
    BigDecimal sumScheduleQtyByOrderItem(@Param("sfahucent") String sfahucent,
                                         @Param("sfahucsite") String sfahucsite,
                                         @Param("sfahuc004") String sfahuc004,
                                         @Param("sfahuc005") String sfahuc005,
                                         @Param("sfahuc002") String sfahuc002);

    @Insert("insert into sfahuc_t " +
            "(sfahucent,sfahucsite,sfahucdocno,sfahucseq,sfahuc001,sfahuc002,sfahuc003,sfahuc004,sfahuc005,sfahuc006,sfahuc007,sfahuc008,sfahuc009,sfahuc010,sfahuc011,sfahuc012,sfahuc013,sfahuc014,sfahuc015,sfahuc016,sfahucstus,sfahuccrtdt,sfahuccrtid) " +
            "values (" +
            "TO_NUMBER(#{sfahucent}),#{sfahucsite},#{sfahucdocno},TO_NUMBER(NULLIF(#{sfahucseq},''))," +
            "#{sfahuc001},#{sfahuc002},TO_NUMBER(NULLIF(#{sfahuc003},'')),#{sfahuc004},TO_NUMBER(NULLIF(#{sfahuc005},''))," +
            "TO_DATE(SUBSTR(NULLIF(#{sfahuc006},''),1,10),'YYYY-MM-DD'),TO_DATE(SUBSTR(NULLIF(#{sfahuc007},''),1,10),'YYYY-MM-DD'),#{sfahuc008},TO_NUMBER(NULLIF(#{sfahuc009},''))," +
            "#{sfahuc010,jdbcType=VARCHAR},TO_NUMBER(NULLIF(#{sfahuc011,jdbcType=VARCHAR},''))," +
            "TO_NUMBER(NULLIF(#{sfahuc012},'')),TO_NUMBER(NULLIF(#{sfahuc013},'')),TO_NUMBER(NULLIF(#{sfahuc014},'')),TO_NUMBER(NULLIF(#{sfahuc015},'')),TO_NUMBER(NULLIF(#{sfahuc016,jdbcType=VARCHAR},''))," +
            "NVL(#{sfahucstus},'N'),sysdate,#{sfahuccrtid})")
    int insert(sfahuc record);

    @Update("update sfahuc_t set " +
            "sfahucent=TO_NUMBER(#{sfahucent}),sfahucsite=#{sfahucsite}," +
            "sfahuc001=#{sfahuc001},sfahuc002=#{sfahuc002}," +
            "sfahuc003=TO_NUMBER(NULLIF(#{sfahuc003},''))," +
            "sfahuc004=#{sfahuc004},sfahuc005=TO_NUMBER(NULLIF(#{sfahuc005},''))," +
            "sfahuc006=TO_DATE(SUBSTR(NULLIF(#{sfahuc006},''),1,10),'YYYY-MM-DD'),sfahuc007=TO_DATE(SUBSTR(NULLIF(#{sfahuc007},''),1,10),'YYYY-MM-DD')," +
            "sfahuc008=#{sfahuc008,jdbcType=VARCHAR},sfahuc009=TO_NUMBER(NULLIF(#{sfahuc009,jdbcType=VARCHAR},'')),sfahuc010=#{sfahuc010,jdbcType=VARCHAR},sfahuc011=TO_NUMBER(NULLIF(#{sfahuc011,jdbcType=VARCHAR},''))," +
            "sfahuc012=TO_NUMBER(NULLIF(#{sfahuc012},'')),sfahuc013=TO_NUMBER(NULLIF(#{sfahuc013},'')),sfahuc014=TO_NUMBER(NULLIF(#{sfahuc014},'')),sfahuc015=TO_NUMBER(NULLIF(#{sfahuc015},'')),sfahuc016=TO_NUMBER(NULLIF(#{sfahuc016,jdbcType=VARCHAR},'')) " +
            "where sfahucdocno=#{sfahucdocno} and sfahucseq=TO_NUMBER(#{sfahucseq})")
    int update(sfahuc record);

    /**
     * 按表主键更新：账套 + 据点 + 工单号 + 品号
     * 主键四列（sfahucent + sfahucsite + sfahuc001 + sfahuc002），用于精确定位到单行
     * 注意：set 中【不包含】主键字段，避免把多行改成相同主键导致 ORA-00001
     */
    @UpdateProvider(type = SfahucSqlProvider.class, method = "updateByPk")
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
