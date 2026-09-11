# 项目记忆

## 项目概览
- **查询条件空值约定（用户明确要求）**：前端传入的查询条件若为 空串 / 纯空白，一律视为「该项条件未设置」，后端**不得**拼接 `where 字段=''`。建议统一在 SQL Provider 的 Java 侧做 `trim().isEmpty()` 判断，避免使用注解内 OGNL `<if test='x != ""'>`（无法处理纯空白）。
- Spring Boot + MyBatis 项目，多数据源架构（primary=ecology/Oracle, second=dsdata/Oracle, third=SRM）
- Controller 直调 Mapper，无 Service 层
- SQL 使用 @SelectProvider 或 @Select 注解，动态 SQL 用 org.apache.ibatis.jdbc.SQL
- **【坑 2026-09-11】`@SelectProvider` 里 `#{}` 的键名必须与 Controller 放进 map 的键一一核对**。Map 参数取不到键时 MyBatis **不报错、绑定 NULL**，导致 where 条件恒不成立（如 `字段 = NULL`）而接口正常返回，极难排查。曾出现 SQL 用 `#{sfahucent}`/`#{sfahucsite}` 而 Controller 只放了 `sfaaent`/`sfaasite`，导致子查询零匹配。
- 返回格式统一为 Hutool JSONObject/JSONArray
- 所有 POST 接口（除 /auth）需要 token 参数
- **token 传参**（`TokenFilter`，urlPatterns="/*"）：取值顺序 URL 参数 → 请求体 JSON 顶层 `token`（数组则取首元素）。约定放请求体顶层，与 `list` 平级
- **部署方式（重要）**：改 Java 代码后必须 `mvn clean package -DskipTests` 重新打包，部署 `target/snapshot.war` 才生效。
  - 曾因只改源码未重新打包，线上跑旧代码，导致字段取 null 被 `isBlank` 静默 `continue`，返回计数全 0 且不报错，排查很久
  - 判断线上代码版本的方法：在返回 JSON 里加新字段（如 `receivedCount`），看响应里有没有
  - 类路径：开发用 `target/classes/...`，war 内嵌为 `target/snapshot/WEB-INF/classes/...`（最终部署以 war 内为准）

## 数据源
- primary: ecology (Oracle 192.168.0.81:1521/TOPPRD) - 核心ERP表（glaq_t, glap_t, glacl_t, ooefl_t等）
- second: dsdata (Oracle 192.168.0.81:1521/TOPPRD) - 预算表（bgbsuc_t、bgbtuc_t等）
- 跨数据源查询在 Java 层 JOIN
- **【默认约定 2026-09-01 用户明确】后续所有新增表/功能，若未特别说明数据源，一律默认在 dsdata（second）下，Mapper 一律放 `com.itheima.mapper.second` 包**
- 已确认在 dsdata/second 的表：bgbsuc_t、bgbtuc_t、ooefl_t、ooeg_t、ooag_t、ooeluc_t、glacl_t、glac_t、xmdd_t、bmaa_t、bmba_t、bmbb_t、gzweuc_t、sfajuc_t、imae_t（品号基础信息，imae051=标准工时）
- 例外（primary/ecology）：user 相关表
- 例外（third/MySQL 192.168.0.83:3306/sdm）：srm_delivery_head、srm_delivery_body

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
  - **BOM 用量公式口径（重要，2026-09-04 用户纠正）**：`BOM用量 = bmba011 × (1 + bmbb011/100) ÷ bmba012`。`bmbb011` 是「百分比损耗率」（如 5 表示 5%），**必须除以 100** 才是真实损耗率；`bmba012` 为底数（为 0 时按 1）；`实际用量 = 父件实际用量 × BOM用量`；`订单需求用量 = 实际用量 × 订单数量`。损耗逐层叠加（父件已含损耗，子件再乘自身损耗）。**BOM用量精度：先 `setScale(6, HALF_UP)` 保留6位，再 `stripTrailingZeros()` 去尾随无效0**（如 2.100000→2.1，2.000000→2）。**`实际用量` 同样保留6位并去尾随0**；`订单需求用量` 仍 `setScale(2)` 保留2位
  - **imaf013 过滤规则（2026-09-05 调整）**：前端传 `imaf013='2'` 时 `queryBomChildren` 仅展开自制件，条件为 `f.imaf013 IN ('2','3')`（自制件含 2 与 3 两类）；前端不传或传其它值时返回全部子件。根节点（订单品号）本身不受此过滤影响，始终返回。
  - **多工单合并规则（2026-09-08）**：工单索引为 `Map<sfaa010 品号, List<工单>>`，同一品号命中多张工单时在 `attachSfaaInfo` 合并——`sfaadocno` 多单号空格分隔、`sfaa019` 取最早、`sfaa020` 取最晚、`sfaa012`/`sfaa050` 求和、`sfaa068`/`sfaastus`/`ooefl003` 取第一条；无工单则这些字段为空串。数值合计输出用 `stripTrailingZeros().toPlainString()`（防 `1E+3` 科学计数法）。

## 2026-08-31~09-01 新增功能
- **日计划排产（sfajuc_t）**：保存 + 查询
  - 表结构（dsdata/second）：`sfajucent`(账套 NUMBER，默认60)、`sfajucsite`(据点，默认NBYL)、`sfajuc001`(工单号)、`sfajuc003`(数量 NUMBER)、`sfajuc004`(产线)、`sfajuc007`(排产日期 DATE)
  - 关键字：账套+据点+工单号+产线+日期
  - 接口：`POST /saveSfajucDailyPlan`（list；存在且数量不同则更新、相同跳过、不存在插入）、`POST /querySfajucDailyPlan`（返回含中文键 `数量`）
  - 代码位置：`com.itheima.pojo.sfajuc`、`com.itheima.mapper.second.SfajucMapper`、`IndexController` 两个新接口
  - 日期用 `TRUNC(sfajuc007)` 按天匹配；插入用 `TO_TIMESTAMP(date || ' 00:00:00', 'YYYY-MM-DD HH24:MI:SS')`
  - 产线字段曾误写为 `sfajuc008`（ORA-00904），实际是 `sfajuc004`；现代码保留 `getStringAny("sfajuc004","sfajuc008")` 兼容
  - 注意：**表结构截图模型无法解析，字段名由用户确认后修正**，后续涉及新表优先让用户直接给字段名文字而非截图

## sfahuc_t 字段语义（dsdata/second）
- 接口：`POST /querySfahuc`（返回数组 master）、`POST /saveSfahuc`（list，判重主键=账套+据点+工单号sfahuc001+品号sfahuc002）、`POST /deleteSfahuc`
- 字段：`sfahucent`(账套 NUMBER,默认60)、`sfahucsite`(据点 VARCHAR2,默认NBYL)、`sfahucdocno`(单号 VARCHAR2)、`sfahucseq`(序号 NUMBER)、`sfahuc001`(工单号 VARCHAR2)、`sfahuc002`(品号 VARCHAR2)、`sfahuc003`(数量 NUMBER)、`sfahuc004`(订单号 VARCHAR2)、`sfahuc005`(订单序号 NUMBER)、`sfahuc006`(开工日期 TIMESTAMP)、`sfahuc007`(完工日期 TIMESTAMP)、`sfahuc008`(成本中心 VARCHAR2)、`sfahuc009`(已入库数量 NUMBER)、**`sfahuc010`(产线 VARCHAR2)**、**`sfahuc011`(订单需求数量 NUMBER，2026-09-08 新增)**。
- **主键**：`sfahucent` + `sfahucsite` + `sfahuc001` + `sfahuc002`。
- **`sfahuc010` = 产线（VARCHAR2，2026-09-08 最终决定）**：曾用订单需求量后改回，现仍为产线字符型，可传 "五金组" 等文本。
- **`sfahuc011` = 订单需求数量（NUMBER，2026-09-08 新增）**：原计划用 sfahuc010 表示，因类型冲突改为新增列。前端传数值字符串或空。注意与 sfajuc_t 的「产线 sfajuc004」区分。
- **sfahuc 写入不落库修复（2026-09-08）**：原 `second` 数据源缺 `PlatformTransactionManager`，`saveSfahuc` 已加 `@Transactional("secondTransactionManager")`（事务管理器 bean 在 `SecondDataSourceConfig`）。返回新增 `verifiedCount` 自检落库。改后必须重新打包 war。
- **`second` 数据源登录用户是 `dsdata`**（非 ecology），`sfahuc_t` 实际在 `dsdata` 用户下；若用 ecology 用户查会看不到，请用 dsdata 用户查 `dsdata.sfahuc_t`。

## 2026-08-25 新增功能
- 管理系统菜单目录树接口 `/queryMenuTree`
  - 数据源：dsdata（second）表 `gzweuc_t`
  - 查询条件：`ent`（默认 60）
  - 返回字段：`gzweuc001`（上阶目录编号）、`gzweuc002`（目录编号）、`gzweuc003`（显示顺序）、`gzweuc004`（菜单名称）+ 递归 `children`
  - 树形构建逻辑：`gzweuc001` 为空、上阶目录编号不存在于结果集、或上阶目录等于自身编号（顶层自引用如 000）时视为根节点，按 `gzweuc003` 排序
