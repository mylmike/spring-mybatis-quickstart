package com.itheima.mapper.second;

import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;

import java.util.List;
import java.util.Map;

/**
 * dsdata 数据源对应的 Mapper
 */
@Mapper
public interface DsdataMapper {

    /**
     * 按主键查询 bgbsuc_t 记录
     */
    @SelectProvider(type = BgbsucSqlProvider.class, method = "findByPk")
    Map<String, Object> findBgbsucByPk(Map<String, Object> params);

    /**
     * 新增 bgbsuc_t 记录
     */
    @InsertProvider(type = BgbsucSqlProvider.class, method = "insert")
    int insertBgbsuc(Map<String, Object> params);

    /**
     * 更新 bgbsuc_t 记录
     */
    @UpdateProvider(type = BgbsucSqlProvider.class, method = "update")
    int updateBgbsuc(Map<String, Object> params);

    /**
     * 按条件查询 bgbsuc_t 记录
     */
    @SelectProvider(type = BgbsucSqlProvider.class, method = "query")
    List<Map<String, Object>> queryBgbsuc(Map<String, Object> params);

    /**
     * 按主键删除 bgbsuc_t 记录
     */
    @DeleteProvider(type = BgbsucSqlProvider.class, method = "deleteByPk")
    int deleteBgbsucByPk(Map<String, Object> params);

    /**
     * 按条件删除 bgbsuc_t 记录（账套+据点+部门+年度等）
     */
    @DeleteProvider(type = BgbsucSqlProvider.class, method = "deleteByCondition")
    int deleteBgbsucByCondition(Map<String, Object> params);

    /**
     * 按主键查询 bgbtuc_t 采购价格预算记录
     */
    @SelectProvider(type = BgbtucSqlProvider.class, method = "findByPk")
    Map<String, Object> findBgbtucByPk(Map<String, Object> params);

    /**
     * 新增 bgbtuc_t 采购价格预算记录
     */
    @InsertProvider(type = BgbtucSqlProvider.class, method = "insert")
    int insertBgbtuc(Map<String, Object> params);

    /**
     * 更新 bgbtuc_t 采购价格预算记录
     */
    @UpdateProvider(type = BgbtucSqlProvider.class, method = "update")
    int updateBgbtuc(Map<String, Object> params);

    /**
     * 按条件查询 bgbtuc_t 采购价格预算记录
     */
    @SelectProvider(type = BgbtucSqlProvider.class, method = "query")
    List<Map<String, Object>> queryBgbtuc(Map<String, Object> params);

    /**
     * 按主键删除 bgbtuc_t 采购价格预算记录
     */
    @DeleteProvider(type = BgbtucSqlProvider.class, method = "deleteByPk")
    int deleteBgbtucByPk(Map<String, Object> params);

    /**
     * 按条件删除 bgbtuc_t 采购价格预算记录
     */
    @DeleteProvider(type = BgbtucSqlProvider.class, method = "deleteByCondition")
    int deleteBgbtucByCondition(Map<String, Object> params);

    /**
     * 查询部门编号和名称（ooefl_t + ooeg_t 联合）
     */
    @SelectProvider(type = OoeflSqlProvider.class, method = "queryDept")
    List<Map<String, Object>> queryDept(Map<String, Object> params);

    /**
     * auth 认证成功后查询用户所属部门（ooag_t + ooefl_t）
     */
    @SelectProvider(type = OoeflSqlProvider.class, method = "queryAuthDept")
    Map<String, Object> queryAuthDept(Map<String, Object> params);

    /**
     * 查询产线名和成本中心（ooeluc_t）
     */
    @SelectProvider(type = OoelucSqlProvider.class, method = "query")
    List<Map<String, Object>> queryOoeluc(Map<String, Object> params);

    /**
     * 查询科目编码和名称（glacl_t + glac_t 联合）
     */
    @SelectProvider(type = GlaclSqlProvider.class, method = "querySubject")
    List<Map<String, Object>> querySubject(Map<String, Object> params);

    /**
     * 预算与实际差异明细表：实际费用 LEFT JOIN 预算结存，按原 SQL 一次查询
     */
    @SelectProvider(type = BudgetActualSqlProvider.class, method = "queryBudgetActualVariance")
    List<Map<String, Object>> queryBudgetActualVariance(Map<String, Object> params);

    /**
     * 预算采购核价分析报表：APBA 对账单 关联 采购价格预算(bgbtuc_t)，计算采购价差/偏差率/差异额
     */
    @SelectProvider(type = BudgetPurchaseSqlProvider.class, method = "queryBudgetPurchaseAnalysis")
    List<Map<String, Object>> queryBudgetPurchaseAnalysis(Map<String, Object> params);

    /**
     * 查询销售订单单身品号和数量
     */
    @SelectProvider(type = BomSqlProvider.class, method = "queryXmddItems")
    List<Map<String, Object>> queryXmddItems(Map<String, Object> params);

    /**
     * 查询指定主件的下阶 BOM 子件（取默认版本）
     */
    @SelectProvider(type = BomSqlProvider.class, method = "queryBomChildren")
    List<Map<String, Object>> queryBomChildren(@Param("ent") String ent,
                                               @Param("site") String site,
                                               @Param("itemNo") String itemNo,
                                               @Param("imaf013") String imaf013,
                                               @Param("lang") String lang);

    /**
     * 查询品号基本资料 imaf_t 的补货策略（imaf013）
     */
    @SelectProvider(type = ItemImSqlProvider.class, method = "queryByItem")
    Map<String, Object> queryItemIm(@Param("ent") String ent,
                                    @Param("site") String site,
                                    @Param("item") String item);

    /**
     * 查询品号描述 imaal_t 的品名（imaal003）、规格（imaal004）
     */
    @SelectProvider(type = ItemDescSqlProvider.class, method = "queryByItem")
    Map<String, Object> queryItemDesc(@Param("ent") String ent,
                                      @Param("lang") String lang,
                                      @Param("item") String item);

    /**
     * 查询管理系统菜单目录 gzweuc_t 原始数据
     */
    @SelectProvider(type = GzweucSqlProvider.class, method = "queryMenuTree")
    List<Map<String, Object>> queryMenuTree(Map<String, Object> params);

    /**
     * BOM 展开时关联工单主表 sfaa_t
     * 条件：sfaaent(账套) + sfaasite(据点) + sfaa022(来源单号) + sfaa023(来源序号，可选)
     * 返回行含 sfaa010(生产料号)，供 Java 侧与 BOM 元件 bmba003 匹配
     */
    @SelectProvider(type = SfaaBomSqlProvider.class, method = "queryByOrder")
    List<Map<String, Object>> querySfaaByOrder(Map<String, Object> params);

    /**
     * 查询品号基础信息（imae_t）
     * 当前仅取标准工时 imae051，后续有需求可直接扩展 SELECT 字段
     * imaeent 数值型（默认 60），imaesite 字符型（默认 NBYL），imae001 为品号
     */
    @Select("select imae051 from imae_t " +
            "where imaeent = #{imaeent} " +
            "and imaesite = #{imaesite} " +
            "and imae001 = #{imae001}")
    List<Map<String, Object>> findImae051(@Param("imaeent") String imaeent,
                                          @Param("imaesite") String imaesite,
                                          @Param("imae001") String imae001);

    /**
     * 查询品号默认成本中心（imae_t.imae035）及成本中心名（ooefl_t.ooefl003，按语言 lang）
     * 用于 BOM 根节点（订单品号）补充默认成本中心信息
     */
    @Select("SELECT e.imae035 AS imae035, d.ooefl003 AS imae035Name " +
            "FROM imae_t e " +
            "LEFT JOIN ooefl_t d ON d.ooeflent = e.imaeent AND d.ooefl001 = e.imae035 AND d.ooefl002 = #{lang} " +
            "WHERE e.imaeent = #{ent} AND e.imaesite = #{site} AND e.imae001 = #{item}")
    Map<String, Object> queryItemCostCenter(@Param("ent") String ent,
                                            @Param("site") String site,
                                            @Param("item") String item,
                                            @Param("lang") String lang);

}
