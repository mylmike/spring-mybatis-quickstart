# 项目记忆

## 项目概览
- Spring Boot + MyBatis 项目，多数据源架构（primary=ecology/Oracle, second=dsdata/Oracle, third=SRM）
- Controller 直调 Mapper，无 Service 层
- SQL 使用 @SelectProvider 或 @Select 注解，动态 SQL 用 org.apache.ibatis.jdbc.SQL
- 返回格式统一为 Hutool JSONObject/JSONArray
- 所有 POST 接口（除 /auth）需要 token 参数

## 数据源
- primary: ecology (Oracle 192.168.0.81:1521/TOPPRD) - 核心ERP表（glaq_t, glap_t, glacl_t, ooefl_t等）
- second: dsdata (Oracle 192.168.0.81:1521/TOPPRD) - 预算表（bgbsuc_t、bgbtuc_t等）
- 跨数据源查询在 Java 层 JOIN

## 2025-08-11 新增功能
- 预算与实际差异明细表: /queryBudgetActualVariance
  - 实际费用从 glaq_t/glap_t/glacl_t/ooefl_t 查询，Oracle DECODE 行转列
  - 预算从 bgbsuc_t 查询，Oracle DECODE 行转列
  - 前端条件: ent(账套), site(账别), year(年度), dept(部门), subjectName(科目名称), summary(摘要)

## 2026-08-11 修复
- 上述所有表均在 dsdata（second 数据源），改为单条 SQL 在 DsdataMapper 执行
- 删除 primary/GlapMapper，避免 ORA-00942
- 返回列名统一：差异01月~12月、预算合计、实际合计

## 2026-08-14 新增功能
- 采购价格预算录入模块（bgbtuc_t）
  - 主键：bgbtucent（默认60）、bgbtucld（默认NBYL）、bgbtuc001（物料编号）、bgbtuc002（年度）
  - 接口：/queryBgbtuc、/saveBgbtuc、/deleteBgbtuc
  - 保存逻辑：按主键 insert/update，并按账套据点同步删除前端未提交的记录
  - 表字段：bgbtucent、bgbtucld、bgbtuc001、bgbtuc002、bgbtuc005（本币金额）、bgbtuc003（参考供应商）

## 2026-08-18 新增功能
- 订单 BOM 递归展阶接口 `/queryOrderBom`
  - 查询 `xmdd_t` 订单品号/数量，按 `bmaa_t/bmba_t/bmbb_t` 递归下展 BOM
  - 返回：订单品号、bmba001(主件)、bmba009(项序)、bmba003(元件)、BOM用量、bmba010(单位)、实际用量、订单需求用量
  - 默认账套 60、据点 NBYL；BOM 默认取最小 `bmaa002` 生效版本
