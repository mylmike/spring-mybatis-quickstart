package com.itheima.mapper.second;

import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
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
                                               @Param("itemNo") String itemNo);

    /**
     * 查询管理系统菜单目录 gzweuc_t 原始数据
     */
    @SelectProvider(type = GzweucSqlProvider.class, method = "queryMenuTree")
    List<Map<String, Object>> queryMenuTree(Map<String, Object> params);

}
