package com.itheima.controller;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.itheima.mapper.primary.UserMapper;
import com.itheima.mapper.second.*;
import com.itheima.mapper.third.SrmDeliveryBodyMapper;
import com.itheima.mapper.third.SrmDeliveryHeadMapper;
import com.itheima.pojo.PmdsdtRow;
import com.itheima.pojo.SrmDeliveryBody;
import com.itheima.pojo.WorkOrderRow;
import com.itheima.pojo.sfaa;
import com.itheima.pojo.sfahuc;
import com.itheima.pojo.sfajuc;
import com.itheima.pojo.user;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
public class  IndexController {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SfaaMapper sfaaMapper;

    @Autowired
    private SfahucMapper sfahucMapper;

    @Autowired
    private SfajucMapper sfajucMapper;

    @Autowired
    private SfbaMapper sfbaMapper;

    @Autowired
    private SrmDeliveryBodyMapper srmDeliveryBodyMapper;

    @Autowired
    private PmdsMapper pmdsMapper;

    @Autowired
    private SrmDeliveryHeadMapper srmDeliveryHeadMapper;

    @Autowired
    private PmdlMapper pmdlMapper;

    @Autowired
    private LssdMapper lssdMapper;

    @Autowired
    private DsdataMapper dsdataMapper;

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 工单查询：返回单头+单身
     * 前端 POST /queryWorkOrder
     * 请求体 JSON:
     * {
     *     "token": "xxx",                           // 必填
     *     // ---- 查询条件（全部可选）----
     *     "sfbaent": "...", "sfaasite": "...", "sfaadocno": "...", "sfaastus": "...",
     *     "sfaa010": "...", "sfaa012": "...", "sfaa068": "...", "sfaadocdt": "...",
     *     "sfaa019": "...", "sfaa020": "...", "sfaa022": "...", "sfaa023": "...", "sfaa050": "...",
     *     "sfbaseq": "...", "sfba006": "...", "sfba023": "...", "sfba024": "...",
     *     "sfba013": "...", "sfba017": "...", "sfba025": "...", "sfba009": "...", "sfba028": "...",
     *     // ---- 限制返回行数（可选）----
     *     "row_max": 200                           // 不传 / <=0 / 非法 时默认 200
     * }
     * 规则：所有条件均可选；值为 null / 空串 / 纯空白 / 未传 时，视为该项条件未设置，不拼接 where。
     *      例如 sfaastus、sfaa068 前端不传或传空串，SQL 中不会出现这两个条件（而不是 =''）。
     * 返回: { "head": [{ 单头字段..., "detail": [{ 单身字段... }] }] }
     */
    @PostMapping("/queryWorkOrder")
    public JSON queryWorkOrder(@RequestBody Map<String, Object> request) {
        JSONObject result = new JSONObject();
        try {
            System.out.println("[queryWorkOrder] 收到请求: " + new JSONObject(request).toString());
            // 去掉 token，其余全部作为查询条件传入 SQL
            request.remove("token");
            List<WorkOrderRow> rows = sfbaMapper.queryWorkOrder(request);

            JSONArray headArr = new JSONArray();

            // 按 sfaadocno 分组
            Map<String, JSONObject> headMap = new LinkedHashMap<>();

            for (WorkOrderRow row : rows) {
                String docno = row.getSfaadocno();
                JSONObject head = headMap.get(docno);
                if (head == null) {
                    head = new JSONObject();
                    head.set("sfbaent", row.getSfbaent());
                    head.set("sfaasite", row.getSfaasite());
                    head.set("sfaadocno", row.getSfaadocno());
                    head.set("sfaastus", row.getSfaastus());
                    head.set("sfaa010", row.getSfaa010());
                    head.set("sfaa012", row.getSfaa012());
                    head.set("sfaa068", row.getSfaa068());
                    head.set("sfaadocdt", row.getSfaadocdt());
                    head.set("sfaa019", row.getSfaa019());
                    head.set("sfaa020", row.getSfaa020());
                    head.set("sfaa022", row.getSfaa022());
                    head.set("sfaa023", row.getSfaa023());
                    head.set("sfaa050", row.getSfaa050());
                    head.set("sfaa047", row.getSfaa047());
                    head.set("detail", new JSONArray());
                    headMap.put(docno, head);
                    headArr.add(head);
                }

                JSONObject detail = new JSONObject();
                detail.set("sfbaseq", row.getSfbaseq());
                detail.set("sfba006", row.getSfba006());
                detail.set("sfba023", row.getSfba023());
                detail.set("sfba024", row.getSfba024());
                detail.set("sfba013", row.getSfba013());
                detail.set("sfba017", row.getSfba017());
                detail.set("sfba025", row.getSfba025());
                detail.set("sfba009", row.getSfba009());
                detail.set("sfba028", row.getSfba028());

                ((JSONArray) head.get("detail")).add(detail);
            }

            result.set("success", true);
            result.set("head", headArr);
            result.set("total", headArr.size());
        } catch (Exception e) {
            System.err.println("[queryWorkOrder] 异常: " + e.getMessage());
            e.printStackTrace();
            result.set("success", false);
            result.set("message", e.getMessage());
            result.set("cause", e.getCause() != null ? e.getCause().getMessage() : "");
        }
        return result;
    }

    /**
     * 根据条件查询 sfahuc_t 明细
     * 前端 POST /querySfahuc
     * 请求体 JSON 示例：
     * {
     *     "token": "xxx",
     *     "sfahucent": "60",          // 账套，可选，默认 60（始终作为条件）
     *     "sfahucsite": "NBYL",       // 营运据点，可选，默认 NBYL（始终作为条件）
     *     "sfahucdocno": "单号",       // 可选
     *     "sfahuc008": "成本中心",      // 可选
     *     "sfahuc004": "订单号",        // 可选
     *     "sfahuc005": "订单序号",      // 可选
     *     "sfahuc001": "工单号"         // 可选
     * }
     * 规则：
     *   1. 账套 sfahucent（默认 60）、营运据点 sfahucsite（默认 NBYL）为固定条件，始终拼接；前端传了则用前端的值
     *   2. 其余条件（sfahucdocno / sfahuc008 / sfahuc004 / sfahuc005 / sfahuc001）
     *      值为 null / 空串 / 纯空白 / 未传 时，视为该项条件未设置，不拼接 where（而不是 =''）
     * 返回: { "master": [{ sfahucent, sfahucsite, sfahucdocno, sfahucseq, ... }] }
     */
    @PostMapping("/querySfahuc")
    public JSON querySfahuc(@RequestBody Map<String, Object> request) {
        JSONObject result = new JSONObject();
        JSONArray master = new JSONArray();

        // 固定条件：账套、营运据点，前端不传则用默认值
        String sfahucent = getString(request, "sfahucent");
        if (isBlank(sfahucent)) sfahucent = "60";
        String sfahucsite = getString(request, "sfahucsite");
        if (isBlank(sfahucsite)) sfahucsite = "NBYL";

        Map<String, Object> params = new HashMap<>();
        params.put("sfahucent", sfahucent);
        params.put("sfahucsite", sfahucsite);
        // 可选条件：只把有效的传入 SQL，其余不拼接
        putIfNotBlank(params, request, "sfahucdocno");
        putIfNotBlank(params, request, "sfahuc008");
        putIfNotBlank(params, request, "sfahuc004");
        putIfNotBlank(params, request, "sfahuc005");
        putIfNotBlank(params, request, "sfahuc001");
        // 工单主表 sfaa_t 的日期/状态过滤条件（空值不拼接）
        putIfNotBlank(params, request, "sfaadocdt_start");
        putIfNotBlank(params, request, "sfaadocdt_end");
        putIfNotBlank(params, request, "sfaa019_start");
        putIfNotBlank(params, request, "sfaa019_end");
        putIfNotBlank(params, request, "sfaa020_start");
        putIfNotBlank(params, request, "sfaa020_end");
        putIfNotBlank(params, request, "sfaastus");

        List<sfahuc> sfahucList = sfahucMapper.listByDocno(params);

        // 排产子表：按 (工单号 sfajuc001 + 产线 sfajuc004) 分组，一对多挂到主表记录下
        Map<String, JSONArray> planMap = new LinkedHashMap<>();
        for (Map<String, Object> row : sfahucMapper.listSfajucPlan(params)) {
            String docno = getString(row, "SFAJUC001");
            String line = getString(row, "SFAJUC004");
            String key = docno + "|" + line;
            JSONArray arr = planMap.get(key);
            if (arr == null) {
                arr = new JSONArray();
                planMap.put(key, arr);
            }
            JSONObject plan = new JSONObject();
            plan.set("排产日期", getString(row, "PAICHANDATE"));
            plan.set("排产数量", getString(row, "PAICHANQTY"));
            arr.add(plan);
        }

        for (sfahuc h : sfahucList) {
            JSONObject item = new JSONObject();
            item.set("sfahucent", h.getSfahucent());
            item.set("sfahucsite", h.getSfahucsite());
            item.set("sfahucdocno", h.getSfahucdocno());
            item.set("sfahucseq", h.getSfahucseq());
            item.set("sfahuc001", h.getSfahuc001());
            item.set("sfahuc002", h.getSfahuc002());
            item.set("sfahuc003", h.getSfahuc003());
            item.set("sfahuc004", h.getSfahuc004());
            item.set("sfahuc005", h.getSfahuc005());
            item.set("sfahuc006", h.getSfahuc006());
            item.set("sfahuc007", h.getSfahuc007());
            item.set("sfahuc008", h.getSfahuc008());
            item.set("sfahuc009", h.getSfahuc009());
            item.set("sfahuc010", h.getSfahuc010());   // 产线
            item.set("sfahuc011", h.getSfahuc011());   // 订单需求数量
            item.set("sfaastus", h.getSfaastus());     // 工单结案状态（取自 sfaa_t）
            item.set("sfaa050", h.getSfaa050());       // 入库数量（取自 sfaa_t）
            item.set("余量", h.getYuLiang());          // 余量 = sfaa012 - sfaa050（取自 sfaa_t）
            // 主表自有列：人数、工作时长、UPPH值
            item.set("sfahuc012", h.getSfahuc012());   // 人数
            item.set("sfahuc013", h.getSfahuc013());   // 工作时长
            item.set("sfahuc014", h.getSfahuc014());   // UPPH值
            // 日产量 = 人数 * 工作时长 * UPPH（空值按 0 处理）
            BigDecimal r12 = toBigDecimal(h.getSfahuc012());
            BigDecimal r13 = toBigDecimal(h.getSfahuc013());
            BigDecimal r14 = toBigDecimal(h.getSfahuc014());
            item.set("sfahuc015", r12.multiply(r13).multiply(r14));   // 日产量
            // 来自 sfajuc_t 的排产汇总（一对多：一个工单+产线对应多天排产，以下一级子表返回）
            String planKey = h.getSfahuc001() + "|" + h.getSfahuc010();
            item.set("排产子表", planMap.getOrDefault(planKey, new JSONArray()));
            master.add(item);
        }

        result.set("master", master);
        return result;
    }

    @GetMapping("/")
    public JSON hello(){
    Map<String, String> mainMap = new HashMap<String, String>();
    JSONObject parameter = new JSONObject();
    JSONArray master = new JSONArray();
    final String[] ruser = new String[1];
    List<user> userList= userMapper.list();
    userList.stream().forEach(user -> {
        JSONObject data = new JSONObject();
        System.out.println(user.getName());
        System.out.println(user.getLoginid());
        System.out.println(user.getZb());
        ruser[0] = ruser[0] +" || "+user.getName()+" "+user.getLoginid()+" "+user.getZb();
//        mainMap.put("姓名",user.getName());
//        mainMap.put("职位",user.getLoginid());
        data.set("xm",user.getName());
        data.set("zw",user.getLoginid());
        master.add(data);

    });

    parameter.set("master",master);
    return parameter;
}

    /**
     * 按可选条件查询工单（sfaa_t）
     * 前端 POST /queryOrder
     * 请求体 JSON 示例：
     * {
     *     "token": "xxx",
     *     "sfaaent": "60",          // 可选，账套，默认 60（始终作为条件）
     *     "sfaasite": "NBYL",       // 可选，营运据点，默认 NBYL（始终作为条件）
     *     "orderNo": "订单号",       // 可选，来源单号 sfaa022
     *     "sfaa023": "来源序号",      // 可选
     *     "sfaa068": "成本中心",      // 可选
     *     "sfaadocno": "工单号",      // 可选
     *     "sfaa010": "生产料号",      // 可选
     *     "rowMax": 500             // 可选，最多 300
     * }
     * 规则：
     *   固定条件（始终拼接）：sfaaent(默认60)、sfaasite(默认NBYL)、sfaastus='F'（只查已发放工单）
     *   其余条件为 null / 空串 / 纯空白 / 未传 时，视为不设置该条件，不拼接 where
     *   rowMax 可选，期望返回行数；最多返回 300 条（不传/<=0 取 300，超过 300 截断为 300）
     * 返回: { "master": [{ sfaadocno, sfaa010, ... }], "total": n, "truncated": true/false }
     *   truncated=true 表示结果已被截断到上限
     *   master 每项含：sfaastus(工单状态码)、sfaa050(入库合格数量)、
     *                 余量 = sfaa012(生产数量) - sfaa050（空值按 0 处理）
     */
    @PostMapping("/queryOrder")
    public JSON queryOrder(@RequestBody Map<String, Object> request) {
        // 固定条件：账套、营运据点，前端不传则用默认值
        String sfaaent = getString(request, "sfaaent");
        if (isBlank(sfaaent)) sfaaent = "60";
        String sfaasite = getString(request, "sfaasite");
        if (isBlank(sfaasite)) sfaasite = "NBYL";
        String orderNo = (String) request.get("orderNo");
        String sfaa023 = getString(request, "sfaa023");
        String sfaa068 = (String) request.get("sfaa068");
        String sfaadocno = (String) request.get("sfaadocno");
        String sfaa010 = (String) request.get("sfaa010");
        String sfaastus = getString(request, "sfaastus");
        // 期望行数：不传或非法传 0，由 Provider 兜底为上限 300
        int rowMax = 0;
        Object rowMaxObj = request.get("rowMax");
        if (rowMaxObj != null) {
            try {
                rowMax = Integer.parseInt(String.valueOf(rowMaxObj).trim());
            } catch (NumberFormatException ignore) {
                rowMax = 0;
            }
        }
        JSONObject result = new JSONObject();
        JSONArray master = new JSONArray();

        // 参数统一用 Map 传入 Mapper
        Map<String, Object> params = new HashMap<>();
        params.put("sfaaent", sfaaent);
        params.put("sfaasite", sfaasite);
        params.put("orderNo", orderNo);
        params.put("sfaa023", sfaa023);
        params.put("sfaa068", sfaa068);
        params.put("sfaadocno", sfaadocno);
        params.put("sfaa010", sfaa010);
        putIfNotBlank(params, request, "sfaadocdt_start");
        putIfNotBlank(params, request, "sfaadocdt_end");
        putIfNotBlank(params, request, "sfaa019_start");
        putIfNotBlank(params, request, "sfaa019_end");
        putIfNotBlank(params, request, "sfaa020_start");
        putIfNotBlank(params, request, "sfaa020_end");
        params.put("sfaastus", sfaastus);
        params.put("rowMax", rowMax);

        try {
            List<sfaa> sfaaList = sfaaMapper.listByOrderNo(params);
            for (sfaa s : sfaaList) {
                JSONObject item = new JSONObject();
                item.set("sfaadocno", s.getSfaadocno());
                item.set("sfaastus", s.getSfaastus());
                item.set("sfaa010", s.getSfaa010());
                item.set("sfaa012", s.getSfaa012());
                item.set("sfaa019", s.getSfaa019());
                item.set("sfaa020", s.getSfaa020());
                item.set("sfaa021", s.getSfaa021());
                item.set("sfaa022", s.getSfaa022());
                item.set("sfaa023", s.getSfaa023());
                item.set("sfaa068", s.getSfaa068());
                item.set("sfaa050", s.getSfaa050());
                // 余量 = 生产数量(sfaa012) - 入库合格数量(sfaa050)，空值按 0 处理
                BigDecimal produceQty = toBigDecimal(s.getSfaa012());
                BigDecimal qualifiedQty = toBigDecimal(s.getSfaa050());
                item.set("余量", produceQty.subtract(qualifiedQty));
                item.set("ooefl003", s.getOoefl003());
                item.set("UPPH", s.getUpph());   // UPPH = 3600 / imae051（标准工时），imae051 为 0 或 NULL 时为 0
                master.add(item);
            }
            result.set("success", true);
        } catch (Exception e) {
            // 把 SQL 异常暴露出来，避免前端只看到空数据却不知道原因
            result.set("success", false);
            result.set("message", e.getMessage());
            result.set("cause", e.getCause() != null ? e.getCause().getMessage() : "");
            e.printStackTrace();
        }

        // 硬上限
        final int MAX_ROWS = 300;
        int effectiveMax = rowMax <= 0 ? MAX_ROWS : Math.min(rowMax, MAX_ROWS);
        result.set("total", master.size());
        result.set("limit", effectiveMax);
        result.set("truncated", master.size() >= effectiveMax);
        result.set("master", master);
        return result;
    }

    /**
     * 订单未交货查询清单（用于前端下拉框绑定）
     * 基于 xmdd_t / xmdc_t / xmda_t，返回未交货订单明细行
     * 限制：仅返回开单日期(xmdadocdt)最近 6 个月的记录
     * 返回：{ success, list: [{ docno, seq, item, undeliveredQty, customer, docDate }], total }
     */
    @PostMapping("/queryUndeliveredOrders")
    public JSONObject queryUndeliveredOrders(@RequestBody Map<String, Object> request) {
        JSONObject result = new JSONObject();
        try {
            java.util.Map<String, Object> p = new java.util.HashMap<>();
            List<Map<String, Object>> rows = orderMapper.queryUndeliveredOrders(p);

            JSONArray list = new JSONArray();
            for (Map<String, Object> row : rows) {
                JSONObject opt = new JSONObject();
                opt.set("docno", stringValueIgnoreCase(row, "docno"));
                opt.set("seq", stringValueIgnoreCase(row, "seq"));
                opt.set("item", stringValueIgnoreCase(row, "item"));
                opt.set("undeliveredQty", getValueIgnoreCase(row, "undeliveredQty"));
                opt.set("customer", stringValueIgnoreCase(row, "customer"));
                opt.set("docDate", stringValueIgnoreCase(row, "docDate"));
                // 下拉框展示用
                opt.set("value", stringValueIgnoreCase(row, "docno"));
                String label = stringValueIgnoreCase(row, "docno");
                String cust = stringValueIgnoreCase(row, "customer");
                if (!isBlank(cust)) {
                    label = label + " - " + cust;
                }
                opt.set("label", label);
                list.add(opt);
            }

            result.set("success", true);
            result.set("list", list);
            result.set("total", list.size());
        } catch (Exception e) {
            result.set("success", false);
            result.set("error", e.getMessage());
        }
        return result;
    }

    /**
     * 保存/更新 sfahuc_t 数据
     * 前端 POST /saveSfahuc
     * 请求体 JSON 示例：
     * {
     *     "token": "xxx",
     *     "list": [
     *         {
     *             "sfahucent": "60",        // 可选，默认 60
     *             "sfahucsite": "NBYL",     // 可选，默认 NBYL
     *             "sfahuc001": "工单号",      // 必填，判重关键字（表主键）
     *             "sfahuc002": "品号",        // 必填，判重关键字（表主键）
     *             "sfahucdocno": "单号",     // 可选
     *             "sfahucseq": "1",        // 可选
     *             "sfahuc003": "数量", "sfahuc004": "订单号",
     *             "sfahuc005": "订单序号", "sfahuc006": "2026-09-02", "sfahuc007": "2026-09-20",
     *             "sfahuc008": "成本中心", "sfahuc009": "已入库数量",
     *             "sfahuc010": "产线", "sfahuc011": "订单需求数量（数值）"
     *         }
     *     ]
     * }
     * 判重条件（= 表主键，4 字段）：sfahucent(默认60) + sfahucsite(默认NBYL)
     *                              + sfahuc001(工单号) + sfahuc002(品号)
     *   四者相同 → 更新该行（where 用主键四列，set 不改主键字段，可改 docno/seq/其它列）
     *   不存在 → 插入新行
     *
     *   一个工单号(sfahuc001) 可对应多个品号(sfahuc002)，每个品号组合都是独立行
     *   注意：update 的 set 中【不修改】主键四列（sfahucent/site/001/002），否则会把多行撞成同一主键 ORA-00001
     * 返回: { "success": true, "insertCount": x, "updateCount": y, "skipCount": z,
     *         "verifiedCount": v, "sfaaUpdateCount": u }
     *   保存成功(insert+update>0)后，会按 工单号 将 sfahuc_t 的 订单号/订单序号/预计开工/预计完工
     *   同步回写 sfaa_t（匹配 sfaaent=sfahucent and sfaasite=sfahucsite and sfaadocno=sfahuc001），
     *   仅当源值非空且与目标列不同才更新；sfaaUpdateCount 为回写影响行数，异常写入 sfaaSyncMsg
     */
    @PostMapping("/saveSfahuc")
    @Transactional("secondTransactionManager")
    public JSON saveSfahuc(@RequestBody Map<String, Object> request) {
        JSONObject result = new JSONObject();
        int insertCount = 0;
        int updateCount = 0;
        int skipCount = 0;
        List<sfahuc> savedRecords = new ArrayList<>();

        try {
            List<Map<String, Object>> list = (List<Map<String, Object>>) request.get("list");

            // 预校验：NUMBER 型列必须是合法数字或空，否则直接给出明确提示（避免 Oracle ORA-01722 难以定位）
            if (list != null) {
                String[] numericFields = {"sfahucseq", "sfahuc003", "sfahuc005", "sfahuc009", "sfahuc011"};
                for (Map<String, Object> item : list) {
                    String wo = getString(item, "sfahuc001");
                    String pn = getString(item, "sfahuc002");
                    for (String f : numericFields) {
                        String v = getString(item, f);
                        if (!isBlank(v) && !isNumeric(v)) {
                            result.set("success", false);
                            result.set("message", "字段 " + f + " 是数值列，收到非法数字值 [" + v
                                    + "]（工单号=" + wo + "，品号=" + pn + "）。请传数字或留空，不能传文本。");
                            return result;
                        }
                    }
                }
            }

            if (list != null) {
                for (Map<String, Object> item : list) {
                    // 判重关键字（= 表主键）：账套、据点取默认值；工单号、品号必填
                    String sfahucent = getString(item, "sfahucent");
                    if (isBlank(sfahucent)) sfahucent = "60";
                    String sfahucsite = getString(item, "sfahucsite");
                    if (isBlank(sfahucsite)) sfahucsite = "NBYL";
                    String sfahuc001 = getString(item, "sfahuc001");
                    String sfahuc002 = getString(item, "sfahuc002");

                    // 工单号、品号为表主键，必填
                    if (isBlank(sfahuc001) || isBlank(sfahuc002)) {
                        System.out.println("[saveSfahuc] 跳过本条：sfahuc001/sfahuc002 为空（主键必填）");
                        skipCount++;
                        continue;
                    }

                    sfahuc record = new sfahuc();
                    record.setSfahucent(sfahucent);
                    record.setSfahucsite(sfahucsite);
                    record.setSfahuc001(sfahuc001);
                    record.setSfahuc002(sfahuc002);
                    record.setSfahucdocno(getString(item, "sfahucdocno"));
                    record.setSfahucseq(getString(item, "sfahucseq"));
                    record.setSfahuc003(getString(item, "sfahuc003"));
                    record.setSfahuc004(getString(item, "sfahuc004"));
                    record.setSfahuc005(getString(item, "sfahuc005"));
                    record.setSfahuc006(getString(item, "sfahuc006"));
                    record.setSfahuc007(getString(item, "sfahuc007"));
                    record.setSfahuc008(getString(item, "sfahuc008"));
                    record.setSfahuc009(getString(item, "sfahuc009"));
                    record.setSfahuc010(getString(item, "sfahuc010"));   // 产线（字符）
                    record.setSfahuc011(getString(item, "sfahuc011"));   // 订单需求数量（NUMBER）
                    // 人数、工作时长、UPPH值
                    record.setSfahuc012(getString(item, "sfahuc012"));   // 人数
                    record.setSfahuc013(getString(item, "sfahuc013"));   // 工作时长
                    record.setSfahuc014(getString(item, "sfahuc014"));   // UPPH值
                    // 日产量 = 人数 * 工作时长 * UPPH（空值按 0 处理）
                    BigDecimal d12 = toBigDecimal(record.getSfahuc012());
                    BigDecimal d13 = toBigDecimal(record.getSfahuc013());
                    BigDecimal d14 = toBigDecimal(record.getSfahuc014());
                    record.setSfahuc015(d12.multiply(d13).multiply(d14).toPlainString());

                    // 按新主键判重：账套+据点+工单号+品号
                    List<sfahuc> existingList = sfahucMapper.findByEntSiteDocno(
                            sfahucent, sfahucsite, sfahuc001, sfahuc002);

                    if (existingList == null || existingList.isEmpty()) {
                        sfahucMapper.insert(record);
                        insertCount++;
                    } else {
                        // 更新时 where 主键四列，set 不改主键字段，可改 docno/seq/其它列
                        sfahucMapper.updateByPk(record);
                        updateCount++;
                    }
                    // 记录已保存（insert/update）的 sfahuc，供后续同步回写 sfaa_t
                    savedRecords.add(record);
                }
            }

            // 保存成功后，将 sfahuc_t 的 订单号/订单序号/预计开工/预计完工 按工单号同步回写 sfaa_t
            // 匹配条件：sfaaent=sfahucent and sfaasite=sfahucsite and sfaadocno=sfahuc001（工单号务必对应）
            int sfaaUpdateCount = 0;
            StringBuilder sfaaSyncMsg = new StringBuilder();
            if ((insertCount + updateCount) > 0 && !savedRecords.isEmpty()) {
                for (sfahuc r : savedRecords) {
                    String o = r.getSfahuc004();   // 订单号
                    String s = r.getSfahuc005();   // 订单序号
                    String sd = r.getSfahuc006();  // 预计开工日期
                    String ed = r.getSfahuc007();  // 预计完工日期
                    // 四个同步字段都为空则无需更新
                    if (isBlank(o) && isBlank(s) && isBlank(sd) && isBlank(ed)) {
                        continue;
                    }
                    Map<String, Object> sp = new HashMap<>();
                    sp.put("ent", r.getSfahucent());
                    sp.put("site", r.getSfahucsite());
                    sp.put("docno", r.getSfahuc001());
                    sp.put("o", o);
                    sp.put("s", s);
                    sp.put("sd", sd);
                    sp.put("ed", ed);
                    try {
                        int n = sfaaMapper.syncSfaaFromSfahuc(sp);
                        sfaaUpdateCount += n;
                    } catch (Exception ex) {
                        sfaaSyncMsg.append("工单号[").append(r.getSfahuc001())
                                .append("]同步sfaa失败:").append(ex.getMessage()).append("; ");
                    }
                }
            }

            // 落库自校验：按主键回查，确认数据确实已写入（排查「显示成功但库里没有」）
            int verifiedCount = 0;
            if (list != null) {
                for (Map<String, Object> item : list) {
                    String c = getString(item, "sfahucent");
                    if (isBlank(c)) c = "60";
                    String s = getString(item, "sfahucsite");
                    if (isBlank(s)) s = "NBYL";
                    String a = getString(item, "sfahuc001");
                    String b = getString(item, "sfahuc002");
                    if (isBlank(a) || isBlank(b)) continue;
                    List<sfahuc> ex = sfahucMapper.findByEntSiteDocno(c, s, a, b);
                    if (ex != null && !ex.isEmpty()) verifiedCount++;
                }
            }

            result.set("success", true);
            result.set("insertCount", insertCount);
            result.set("updateCount", updateCount);
            result.set("skipCount", skipCount);
            result.set("verifiedCount", verifiedCount);
            result.set("sfaaUpdateCount", sfaaUpdateCount);
            if (sfaaSyncMsg.length() > 0) {
                result.set("sfaaSyncMsg", sfaaSyncMsg.toString());
            }
        } catch (Exception e) {
            result.set("success", false);
            result.set("message", e.getMessage());
            result.set("cause", e.getCause() != null ? e.getCause().getMessage() : "");
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 删除 sfahuc_t 记录，并同步删除 sfajuc_t 中对应工单号的日计划记录
     * 前端 POST /deleteSfahuc
     * 请求体 JSON 示例：
     * {
     *     "token": "xxx",
     *     "sfahucent": "60",        // 可选，默认 60
     *     "sfahucsite": "NBYL",     // 可选，默认 NBYL
     *     "sfahuc001": "...",       // 必填，工单号
     *     "sfahuc002": "...",       // 必填
     *     "sfahuc008": "成本中心",     // 可选，有值才作为条件
     *     "sfahuc004": "订单号",       // 可选，有值才作为条件
     *     "sfahuc005": "订单序号"      // 可选，有值才作为条件
     * }
     * 删除条件：账套 + 据点 + sfahuc001 + sfahuc002 [+ 成本中心 sfahuc008] [+ 订单号 sfahuc004 + 订单序号 sfahuc005]
     * 同步删除 sfajuc_t：sfajucent=sfahucent and sfajucsite=sfahucsite and sfajuc001=sfahuc001
     *   （仅当 sfahuc_t 实际删到记录时才执行，避免误删日计划）
     * 返回: { success, deletedCount, sfajucDeletedCount, deleted, condition }
     */
    @PostMapping("/deleteSfahuc")
    public JSON deleteSfahuc(@RequestBody Map<String, Object> request) {
        JSONObject result = new JSONObject();
        try {
            String sfahucent = getString(request, "sfahucent");
            if (isBlank(sfahucent)) sfahucent = "60";
            String sfahucsite = getString(request, "sfahucsite");
            if (isBlank(sfahucsite)) sfahucsite = "NBYL";
            String sfahuc001 = getString(request, "sfahuc001");
            String sfahuc002 = getString(request, "sfahuc002");
            String sfahuc008 = getString(request, "sfahuc008");
            String sfahuc004 = getString(request, "sfahuc004");
            String sfahuc005 = getString(request, "sfahuc005");

            // 必填校验
            if (isBlank(sfahuc001) || isBlank(sfahuc002)) {
                result.set("success", false);
                result.set("message", "sfahuc001 和 sfahuc002 为必填");
                return result;
            }

            Map<String, Object> params = new HashMap<>();
            params.put("sfahucent", sfahucent);
            params.put("sfahucsite", sfahucsite);
            params.put("sfahuc001", sfahuc001);
            params.put("sfahuc002", sfahuc002);
            if (!isBlank(sfahuc008)) {
                params.put("sfahuc008", sfahuc008);
            }
            if (!isBlank(sfahuc004)) {
                params.put("sfahuc004", sfahuc004);
            }
            if (!isBlank(sfahuc005)) {
                params.put("sfahuc005", sfahuc005);
            }

            int deletedCount = sfahucMapper.deleteByKey(params);

            // 同步删除 sfajuc_t 中该工单号的日计划（仅当主表确实删到记录）
            int sfajucDeletedCount = 0;
            if (deletedCount > 0) {
                sfajucDeletedCount = sfajucMapper.deleteByDocno(sfahucent, sfahucsite, sfahuc001);
            }

            result.set("success", true);
            result.set("deletedCount", deletedCount);
            result.set("sfajucDeletedCount", sfajucDeletedCount);
            result.set("deleted", deletedCount > 0);

            JSONObject condition = new JSONObject();
            condition.set("sfahucent", sfahucent);
            condition.set("sfahucsite", sfahucsite);
            condition.set("sfahuc001", sfahuc001);
            condition.set("sfahuc002", sfahuc002);
            condition.set("sfahuc008", sfahuc008);
            condition.set("sfahuc004", sfahuc004);
            condition.set("sfahuc005", sfahuc005);
            result.set("condition", condition);
        } catch (Exception e) {
            result.set("success", false);
            result.set("message", e.getMessage());
            result.set("cause", e.getCause() != null ? e.getCause().getMessage() : "");
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 发货-收货匹配查询（跨 MySQL + Oracle 数据源）
     * 前端 POST /queryDeliveryMatch
     * 请求体 JSON: { "token": "xxx", "ent": "60", "site": "NBYL",
     *                "receiptQty": "0",        // 收货数量条件，不传默认 0（未收货）
     *                "czf": ">",               // 收货数量操作符：> < = >= <=，不传默认 =，非法值回退 =
     *                "status": "2",            // 单头状态条件，不传默认非 X/4/1
     *                "deliveryDateStart": "2026-08-01",  // 送货日期起始（含），可选
     *                "deliveryDateEnd": "2026-08-12" }   // 送货日期截止（含），可选
     * 逻辑:
     *   1. MySQL srm_delivery_body JOIN srm_delivery_head 按条件查询
     *   2. Oracle pmds_t JOIN pmdt_t WHERE pmds000='1'
     *   3. Java 中匹配: deliver_no=pmds010 AND purchase_no=pmdt001 AND purchase_seq=pmdt002
     * 返回每行: deliverNo, purchaseNo, purchaseSeq, itemNo, deliveryQty, pmdtdocno, pmdt020
     */
    @PostMapping("/queryDeliveryMatch")
    public JSON queryDeliveryMatch(@RequestBody Map<String, Object> request) {
        JSONObject result = new JSONObject();

        String ent = getString(request, "ent");
        if (ent == null || ent.isEmpty()) ent = "60";

        // 查询条件：不传则使用默认值
        String receiptQty = getString(request, "receiptQty");
        String czf = getString(request, "czf");
        // 标准化操作符，避免在 Mapper XML 的 OGNL 中比较 XML 特殊字符（> < >= <=）导致解析异常
        if (czf == null || czf.isEmpty()) {
            czf = "eq";
        } else if (czf.equals(">")) {
            czf = "gt";
        } else if (czf.equals("<")) {
            czf = "lt";
        } else if (czf.equals(">=")) {
            czf = "ge";
        } else if (czf.equals("<=")) {
            czf = "le";
        } else if (czf.equals("=")) {
            czf = "eq";
        } else {
            czf = "eq"; // 非法值回退为等于
        }
        String status = getString(request, "status");
        String deliveryDateStart = getString(request, "deliveryDateStart");
        String deliveryDateEnd = getString(request, "deliveryDateEnd");

        JSONArray matches = new JSONArray();

        // 1. 查 MySQL：按条件查询送货单（未传 receiptQty 时默认只查未收货 receipt_qty=0）
        List<SrmDeliveryBody> deliveryList = srmDeliveryBodyMapper.findUnreceived(receiptQty, czf, status, deliveryDateStart, deliveryDateEnd);

        if (deliveryList.isEmpty()) {
            result.set("matches", matches);
            return result;
        }

        // 2. 收集送货单号，去重后到 Oracle 按条件精确查询（避免全表扫描）
        List<String> deliverNos = new ArrayList<>();
        for (SrmDeliveryBody d : deliveryList) {
            if (d.getDeliveryNo() != null && !d.getDeliveryNo().isEmpty()) {
                deliverNos.add(d.getDeliveryNo());
            }
        }

        // Oracle IN 子句最多 1000 个，分批查询后合并
        List<PmdsdtRow> pmdsList = new ArrayList<>();
        if (!deliverNos.isEmpty()) {
            for (int i = 0; i < deliverNos.size(); i += 1000) {
                int end = Math.min(i + 1000, deliverNos.size());
                List<String> batch = deliverNos.subList(i, end);
                pmdsList.addAll(pmdsMapper.findByDeliverNos(batch));
            }
        }

        // 3. Java 中匹配: deliver_no=pmds010 AND purchase_no=pmdt001 AND purchase_seq=pmdt002
        //    未匹配到的送货单也返回，pmdtdocno/pmdt020 为空
        java.util.Date today = new java.util.Date();
        for (SrmDeliveryBody delivery : deliveryList) {
            // 计算延误天数
            Long delayDays = null;
            if (delivery.getDeliveryDate() != null) {
                long diff = today.getTime() - delivery.getDeliveryDate().getTime();
                delayDays = diff / (1000 * 60 * 60 * 24);
            }

            boolean found = false;
            for (PmdsdtRow pmds : pmdsList) {
                if (delivery.getDeliveryNo() != null && pmds.getPmds010() != null
                        && delivery.getDeliveryNo().equals(pmds.getPmds010())
                        && delivery.getPurchaseNo() != null && pmds.getPmdt001() != null
                        && delivery.getPurchaseNo().equals(pmds.getPmdt001())
                        && delivery.getPurchaseSeq() != null && pmds.getPmdt002() != null
                        && delivery.getPurchaseSeq().equals(pmds.getPmdt002())) {

                    JSONObject match = new JSONObject();
                    match.set("deliveryNo", delivery.getDeliveryNo());
                    match.set("deliverySeq", delivery.getDeliverySeq());
                    match.set("purchaseNo", delivery.getPurchaseNo());
                    match.set("purchaseSeq", delivery.getPurchaseSeq());
                    match.set("itemNo", delivery.getItemNo());
                    match.set("deliveryQty", delivery.getDeliveryQty());
                    match.set("deliveryDate", delivery.getDeliveryDate());
                    match.set("delayDays", delayDays);
                    match.set("supplierNo", delivery.getSupplierNo());
                    match.set("pmdtdocno", pmds.getPmdtdocno());
                    match.set("pmdtseq", pmds.getPmdtseq());
                    match.set("pmdt020", pmds.getPmdt020());

                    matches.add(match);
                    found = true;
                }
            }
            // 未匹配到收货单的送货单，收货字段为空也返回
            if (!found) {
                JSONObject match = new JSONObject();
                match.set("deliveryNo", delivery.getDeliveryNo());
                match.set("deliverySeq", delivery.getDeliverySeq());
                match.set("purchaseNo", delivery.getPurchaseNo());
                match.set("purchaseSeq", delivery.getPurchaseSeq());
                match.set("itemNo", delivery.getItemNo());
                match.set("deliveryQty", delivery.getDeliveryQty());
                match.set("deliveryDate", delivery.getDeliveryDate());
                match.set("delayDays", delayDays);
                match.set("supplierNo", delivery.getSupplierNo());
                match.set("pmdtdocno", "");
                match.set("pmdtseq", "");
                match.set("pmdt020", "");

                matches.add(match);
            }
        }

        // 批量查询采购员姓名，缓存结果避免重复查库
        java.util.Map<String, String> purchaserCache = new java.util.HashMap<>();
        for (Object obj : matches) {
            JSONObject m = (JSONObject) obj;
            String pno = m.getStr("purchaseNo");
            if (pno != null && !pno.isEmpty()) {
                if (!purchaserCache.containsKey(pno)) {
                    String name = pmdlMapper.findPurchaserName(ent, pno);
                    purchaserCache.put(pno, name != null ? name : "");
                }
                m.set("purchaserName", purchaserCache.get(pno));
            } else {
                m.set("purchaserName", "");
            }
        }

        result.set("matches", matches);
        return result;
    }

    /**
     * 手动同步收货状态（处理送货系统 receipt_qty=0 但 ERP 已收货的异常）
     * 前端 POST /syncReceiptManual
     * 请求体 JSON: { "deliveryNo": "xxx", "deliverySeq": "x", "purchaseNo": "xxx",
     *                "purchaseSeq": "x", "deliveryQty": "x", "pmdt020Qty": "x",
     *                "ent": "60",  "site": "NBYL" }
     * ent 默认 60，site 默认 NBYL
     */
    @PostMapping("/syncReceiptManual")
    public JSONObject syncReceiptManual(@RequestBody(required = false) Object request) {
        JSONObject result = new JSONObject();

        // 兼容两种格式：JSON 对象 {token:..., deliveryNo:...} 或 JSON 数组 [{...}]
        Map<String, Object> reqMap;
        if (request instanceof List) {
            List<?> list = (List<?>) request;
            if (list.isEmpty()) {
                result.set("success", false);
                result.set("message", "请求体不能为空");
                return result;
            }
            Object first = list.get(0);
            if (!(first instanceof Map)) {
                result.set("success", false);
                result.set("message", "请求体格式错误：数组元素必须是对象");
                return result;
            }
            reqMap = (Map<String, Object>) first;
        } else if (request instanceof Map) {
            reqMap = (Map<String, Object>) request;
        } else {
            result.set("success", false);
            result.set("message", "请求体格式错误：必须是 JSON 对象或数组");
            return result;
        }

        final Map<String, Object> data = reqMap;

        String deliveryNo = getString(data, "deliveryNo");
        String deliverySeq = getString(data, "deliverySeq");
        String purchaseNo = getString(data, "purchaseNo");
        String purchaseSeq = getString(data, "purchaseSeq");
        String deliveryQtyStr = getString(data, "deliveryQty");
        String pmdt020QtyStr = getString(data, "pmdt020Qty");

        // 账套/据点，默认 ENT=60, SITE=NBYL
        String ent = getString(data, "ent");
        if (ent == null || ent.isEmpty()) ent = "60";
        String site = getString(data, "site");
        if (site == null || site.isEmpty()) site = "NBYL";

        // 回显接收到的参数，方便排查
        result.set("params", new JSONObject()
            .set("deliveryNo", deliveryNo)
            .set("deliverySeq", deliverySeq)
            .set("purchaseNo", purchaseNo)
            .set("purchaseSeq", purchaseSeq)
            .set("deliveryQty", deliveryQtyStr)
            .set("pmdt020Qty", pmdt020QtyStr)
            .set("ent", ent)
            .set("site", site));

        if (deliveryNo == null || deliverySeq == null) {
            result.set("success", false);
            result.set("message", "deliveryNo 和 deliverySeq 不能为空");
            return result;
        }

        // ========== 步骤0：先查询更新前的数据 ==========
        SrmDeliveryBody bodyBefore = srmDeliveryBodyMapper.findByNoAndSeq(deliveryNo, deliverySeq, ent, site);
        result.set("rowExists", bodyBefore != null);
        if (bodyBefore != null) {
            JSONObject beforeInfo = new JSONObject();
            beforeInfo.set("deliveryNo_db", bodyBefore.getDeliveryNo());
            beforeInfo.set("deliverySeq_db", bodyBefore.getDeliverySeq());
            beforeInfo.set("receiptQty_before", bodyBefore.getReceiptQty());
            beforeInfo.set("ent_db", bodyBefore.getEnt());
            beforeInfo.set("site_db", bodyBefore.getSite());
            beforeInfo.set("purchaseNo_db", bodyBefore.getPurchaseNo());
            beforeInfo.set("purchaseSeq_db", bodyBefore.getPurchaseSeq());
            result.set("beforeRow", beforeInfo);
        }

        // 条件：收货数量 = 送货数量（使用 BigDecimal 比较）
        boolean qtyMatch = false;
        // 结清未收部分：送货数量 > 收货数量 且 单头状态非 'X'/'1' 时触发（这张单没收完但不想再收了）
        boolean finishUnreceived = false;
        BigDecimal deliveryQtyBd = null;
        BigDecimal pmdt020QtyBd = null;
        if (deliveryQtyStr != null && pmdt020QtyStr != null) {
            try {
                deliveryQtyBd = new BigDecimal(deliveryQtyStr);
                pmdt020QtyBd = new BigDecimal(pmdt020QtyStr);
                qtyMatch = deliveryQtyBd.compareTo(pmdt020QtyBd) == 0;
                result.set("qtyMatch", qtyMatch);
                result.set("deliveryQtyNum", deliveryQtyBd.stripTrailingZeros().toPlainString());
                result.set("pmdt020QtyNum", pmdt020QtyBd.stripTrailingZeros().toPlainString());

                // 送货数量 > 收货数量：说明有未收部分
                // 若单头状态非 'X'/'1'，说明这张单没收完但不想再收了，结清未收部分
                if (deliveryQtyBd.compareTo(pmdt020QtyBd) > 0) {
                    String headStatus = srmDeliveryHeadMapper.findStatus(deliveryNo, ent, site);
                    result.set("headStatus", headStatus);
                    if (headStatus != null && !"X".equals(headStatus) && !"1".equals(headStatus)) {
                        finishUnreceived = true;
                        BigDecimal unreceived = deliveryQtyBd.subtract(pmdt020QtyBd);
                        result.set("unreceivedQty", unreceived.stripTrailingZeros().toPlainString());
                    }
                }
            } catch (NumberFormatException e) {
                result.set("success", false);
                result.set("message", "数量格式错误: " + e.getMessage());
                return result;
            }
        } else {
            result.set("qtyMatch", false);
        }

        if (qtyMatch) {

            // 1. 更新 srm_delivery_body 收货数量和标记
            System.out.println("=== syncReceiptManual 开始更新 ===");
            System.out.println("deliveryNo=" + deliveryNo + ", deliverySeq=" + deliverySeq + ", receiptQty=" + pmdt020QtyStr + ", ent=" + ent + ", site=" + site);
            int bodyUpdated = srmDeliveryBodyMapper.updateReceiptQty(deliveryNo, deliverySeq, pmdt020QtyStr, ent, site);
            System.out.println("bodyUpdated 返回值: " + bodyUpdated);
            result.set("bodyUpdated", bodyUpdated);

            // 更新后立即回查验证
            SrmDeliveryBody bodyAfter = srmDeliveryBodyMapper.findByNoAndSeq(deliveryNo, deliverySeq, ent, site);
            result.set("verifyAfter", bodyAfter != null);
            if (bodyAfter != null) {
                result.set("receiptQty_after", bodyAfter.getReceiptQty());
                result.set("remark2_after", bodyAfter.getRemark2());
                System.out.println("更新后 receiptQty=" + bodyAfter.getReceiptQty() + ", remark2=" + bodyAfter.getRemark2());
            }

            // 2. 检查该送货单号下是否全部收完
            int unreceivedCount = srmDeliveryBodyMapper.countUnreceivedByDeliveryNo(deliveryNo, ent, site);
            result.set("unreceivedCount", unreceivedCount);
            if (unreceivedCount == 0) {
                int headUpdated = srmDeliveryHeadMapper.updateStatusToComplete(deliveryNo, ent, site);
                result.set("headUpdated", headUpdated > 0);
            } else {
                result.set("headUpdated", false);
            }

            // 3. 查询 ERP 表 lssd_t（采购送货收货记录）
            Map<String, Object> lssdRow = lssdMapper.findByPurchase(ent, site, purchaseNo, purchaseSeq);
            if (lssdRow != null && !lssdRow.isEmpty()) {
                result.set("lssdFound", true);
                Object lssd005 = lssdRow.get("LSSD005");
                if (lssd005 == null || "0".equals(String.valueOf(lssd005))) {
                    int lssdUpdated = lssdMapper.updateReceiptQty(ent, site, purchaseNo, purchaseSeq, deliveryQtyStr);
                    result.set("lssdUpdated", lssdUpdated);
                } else {
                    result.set("lssdUpdated", 0);
                }
            } else {
                result.set("lssdFound", false);
                result.set("lssdUpdated", 0);
            }

            result.set("success", true);
            result.set("message", "同步完成");
        } else if (finishUnreceived) {
            // ===== 结清未收部分：这张单没收完但不想再收了 =====
            // 1. 更新送货单明细：delivery_qty = receipt_qty（把送货数量清成已收货数量）
            int bodyClosed = srmDeliveryBodyMapper.updateDeliveryQtyToReceiptQty(deliveryNo, deliverySeq, ent, site);
            result.set("bodyClosed", bodyClosed);

            // 2. 更新送货单头状态为 '4'（完成）
            int headClosed = srmDeliveryHeadMapper.updateStatusToComplete(deliveryNo, ent, site);
            result.set("headClosed", headClosed > 0);

            // 3. ERP lssd_t 减去未收差异数量（相当于这批没送，下次可重新开送货）
            BigDecimal unreceivedQty = deliveryQtyBd.subtract(pmdt020QtyBd);
            int lssdClosed = lssdMapper.reduceDeliveryQty(ent, site, purchaseNo, purchaseSeq, unreceivedQty.toPlainString());
            result.set("lssdClosed", lssdClosed);

            result.set("success", true);
            result.set("message", "未收部分已结清，送货单置为完成");
        } else {
            result.set("success", false);
            result.set("message", "收货数量与送货数量不相等，不执行更新");
        }

        return result;
    }

    /**
     * 删除送货单
     */
    @PostMapping("/deleteDelivery")
    public JSONObject deleteDelivery(@RequestBody(required = false) Object request) {
        JSONObject result = new JSONObject();

        // 兼容数组和对象两种格式
        Map<String, Object> data;
        if (request instanceof List) {
            List<?> list = (List<?>) request;
            if (list.isEmpty()) {
                result.set("success", false);
                result.set("message", "请求体不能为空");
                return result;
            }
            Object first = list.get(0);
            if (!(first instanceof Map)) {
                result.set("success", false);
                result.set("message", "请求体格式错误：数组元素必须是对象");
                return result;
            }
            data = (Map<String, Object>) first;
        } else if (request instanceof Map) {
            data = (Map<String, Object>) request;
        } else {
            result.set("success", false);
            result.set("message", "请求体格式错误：必须是 JSON 对象或数组");
            return result;
        }

        String deliveryNo  = getString(data, "deliveryNo");
        String deliverySeq = getString(data, "deliverySeq");
        String purchaseNo  = getString(data, "purchaseNo");
        String purchaseSeq = getString(data, "purchaseSeq");
        String deliveryQtyStr = getString(data, "deliveryQty");

        String ent  = getString(data, "ent");
        if (ent == null || ent.isEmpty()) ent = "60";
        String site = getString(data, "site");
        if (site == null || site.isEmpty()) site = "NBYL";

        if (deliveryNo == null || deliverySeq == null || purchaseNo == null || purchaseSeq == null) {
            result.set("success", false);
            result.set("message", "deliveryNo/deliverySeq/purchaseNo/purchaseSeq 不能为空");
            return result;
        }

        // 1. 查 lssd_t 送货记录
        Map<String, Object> lssdRow = lssdMapper.findByPurchase(ent, site, purchaseNo, purchaseSeq);
        if (lssdRow == null || lssdRow.isEmpty()) {
            // ERP 中无对应记录，跳过 ERP 处理直接删送货单
            result.set("deliveredQty", 0);
            result.set("receivedQty", 0);
            result.set("unreceivedQty", 0);
            result.set("deliveryQty", 0);
            result.set("lssdNotExist", true);
            result.set("lssdSkipped", true);
            result.set("lssdUpdated", 0);
        } else {
            // 计算未收货数量: lssd004 - lssd005
            BigDecimal deliveredQty = toBigDecimal(lssdRow.get("LSSD004"));  // 已送货
            BigDecimal receivedQty = toBigDecimal(lssdRow.get("LSSD005"));  // 已收货
            BigDecimal unreceivedQty = deliveredQty.subtract(receivedQty);
            BigDecimal deliveryQty = toBigDecimal(deliveryQtyStr);

            result.set("deliveredQty", deliveredQty);
            result.set("receivedQty", receivedQty);
            result.set("unreceivedQty", unreceivedQty);
            result.set("deliveryQty", deliveryQty);

            // 2. 处理 ERP lssd_t
            // 如果 lssd004=0 且 lssd005=0，跳过 ERP 更新直接删送货单
            if (deliveredQty.compareTo(BigDecimal.ZERO) == 0 && receivedQty.compareTo(BigDecimal.ZERO) == 0) {
                result.set("lssdNotExist", false);
                result.set("lssdSkipped", true);
                result.set("lssdUpdated", 0);
            } else {
                result.set("lssdNotExist", false);
            result.set("lssdSkipped", false);
            if (unreceivedQty.compareTo(deliveryQty) < 0) {
                result.set("success", false);
                result.set("message", "未收货数量小于该笔送货数量");
                result.set("deliveryNo", deliveryNo);
                result.set("deliverySeq", deliverySeq);
                result.set("purchaseNo", purchaseNo);
                result.set("purchaseSeq", purchaseSeq);
                return result;
            }
            int lssdUpdated = lssdMapper.reduceDeliveryQty(ent, site, purchaseNo, purchaseSeq, deliveryQtyStr);
            result.set("lssdUpdated", lssdUpdated);
            }
        }

        // 3. 删除送货单身记录
        int bodyDeleted = srmDeliveryBodyMapper.deleteByKeys(ent, site, deliveryNo, deliverySeq, purchaseNo, purchaseSeq);
        result.set("bodyDeleted", bodyDeleted);

        // 4. 检查该送货单号下是否还有记录，没有则删除单头
        int bodyCount = srmDeliveryBodyMapper.countByDeliveryNo(ent, site, deliveryNo);
        result.set("bodyCountAfterDelete", bodyCount);
        if (bodyCount == 0) {
            int headDeleted = srmDeliveryHeadMapper.deleteByDeliveryNo(ent, site, deliveryNo);
            result.set("headDeleted", headDeleted);
        } else {
            result.set("headDeleted", 0);

            // 5. 单头还存在，检查是否所有记录的送货数量都已收完
            //    若 delivery_qty = receipt_qty 全部成立，更新单头状态为 '4'
            int notFullyReceived = srmDeliveryBodyMapper.countNotFullyReceived(ent, site, deliveryNo);
            result.set("notFullyReceivedAfterDelete", notFullyReceived);
            if (notFullyReceived == 0) {
                int statusUpdated = srmDeliveryHeadMapper.updateStatusToComplete(deliveryNo, ent, site);
                result.set("headStatusUpdated", statusUpdated);
            } else {
                result.set("headStatusUpdated", 0);
            }
        }

        result.set("success", true);
        result.set("message", "删除完成");
        return result;
    }

    private BigDecimal toBigDecimal(Object val) {
        if (val == null) return BigDecimal.ZERO;
        try {
            return new BigDecimal(String.valueOf(val));
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * 保存/更新 bgbsuc_t 预算结存表
     * 前端 POST /saveBgbsuc
     * 请求体 JSON 示例：
     * {
     *     "list": [
     *         {
     *             "bgbsucent": "60", "bgbsucld": "NBYL",
     *             "bgbsuc001": "...", "bgbsuc002": "...", "bgbsuc003": "...", "bgbsuc004": "...",
     *             "bgbsuc005": 100.00, ...
     *         },
     *         ...
     *     ],
     *     "pkFields": ["bgbsucent", "bgbsucld", "bgbsuc001", "bgbsuc002", "bgbsuc003", "bgbsuc004"]
     * }
     * 返回: { "success": true, "insertCount": x, "updateCount": y }
     */
    @PostMapping("/saveBgbsuc")
    public JSONObject saveBgbsuc(@RequestBody Map<String, Object> request) {
        final String DEFAULT_ENT_FIELD = "bgbsucent";
        final String DEFAULT_SITE_FIELD = "bgbsucld";
        final List<String> DEFAULT_PK_FIELDS = java.util.Arrays.asList(
                "bgbsucent", "bgbsucld", "bgbsuc001", "bgbsuc002", "bgbsucseq");

        JSONObject result = new JSONObject();
        int insertCount = 0;
        int updateCount = 0;
        int deleteCount = 0;

        try {
            List<Map<String, Object>> list;
            if (request.containsKey("list")) {
                list = (List<Map<String, Object>>) request.get("list");
            } else if (request.containsKey("data")) {
                list = new ArrayList<>();
                list.add((Map<String, Object>) request.get("data"));
            } else {
                list = new ArrayList<>();
                list.add(request);
            }

            List<String> pkFields = DEFAULT_PK_FIELDS;
            if (request.containsKey("pkFields")) {
                pkFields = (List<String>) request.get("pkFields");
            }

            if (list != null) {
                // 补默认值
                for (Map<String, Object> item : list) {
                    if (isBlank(item.get(DEFAULT_ENT_FIELD))) {
                        item.put(DEFAULT_ENT_FIELD, "60");
                    }
                    if (isBlank(item.get(DEFAULT_SITE_FIELD))) {
                        item.put(DEFAULT_SITE_FIELD, "NBYL");
                    }
                }

                // 一、保存/更新
                for (Map<String, Object> item : list) {
                    Map<String, Object> params = new HashMap<>(item);
                    params.put("pkFields", pkFields);

                    Map<String, Object> existing = dsdataMapper.findBgbsucByPk(params);
                    if (existing == null || existing.isEmpty()) {
                        insertCount += dsdataMapper.insertBgbsuc(params);
                    } else {
                        updateCount += dsdataMapper.updateBgbsuc(params);
                    }
                }

                // 二、同步删除：收集前端所有主键，查DB同账套据点下全部记录，删掉前端没有的
                Set<String> allFrontendPks = new HashSet<>();
                Set<String> entSiteSet = new HashSet<>();
                for (Map<String, Object> item : list) {
                    allFrontendPks.add(buildPkKey(item, pkFields));
                    String es = item.get(DEFAULT_ENT_FIELD) + "|" + item.get(DEFAULT_SITE_FIELD);
                    entSiteSet.add(es);
                }
                System.out.println("[saveBgbsuc] 前端主键集合: " + allFrontendPks);

                for (String es : entSiteSet) {
                    String[] parts = es.split("\\|");
                    Map<String, Object> filter = new HashMap<>();
                    filter.put("bgbsucent", parts[0]);
                    filter.put("bgbsucld", parts[1]);

                    List<Map<String, Object>> dbRows = dsdataMapper.queryBgbsuc(filter);
                    System.out.println("[saveBgbsuc] 账套=" + parts[0] + " 据点=" + parts[1] + " DB查回记录数: " + dbRows.size());
                    for (Map<String, Object> dbRow : dbRows) {
                        String dbPk = buildPkKey(dbRow, pkFields);
                        System.out.println("[saveBgbsuc]   DB行PK: " + dbPk + "  是否在前端: " + allFrontendPks.contains(dbPk));
                        if (!allFrontendPks.contains(dbPk)) {
                            Map<String, Object> delParams = new HashMap<>();
                            for (String pk : pkFields) {
                                delParams.put(pk, getValueIgnoreCase(dbRow, pk));
                            }
                            delParams.put("pkFields", pkFields);
                            deleteCount += dsdataMapper.deleteBgbsucByPk(delParams);
                        }
                    }
                }
            }

            result.set("success", true);
            result.set("insertCount", insertCount);
            result.set("updateCount", updateCount);
            result.set("deleteCount", deleteCount);
        } catch (Exception e) {
            result.set("success", false);
            result.set("message", e.getMessage());
            result.set("cause", e.getCause() != null ? e.getCause().getMessage() : "");
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 按条件删除 bgbsuc_t 记录
     * 前端传入：token + bgbsucent(账套) + bgbsucld(据点) + bgbsuc001(部门) + bgbsuc002(年度)
     * 删除匹配所有非空字段的全部记录
     */
    @PostMapping("/deleteBgbsuc")
    public JSONObject deleteBgbsuc(@RequestBody Map<String, Object> request) {
        JSONObject result = new JSONObject();
        try {
            int deleteCount = dsdataMapper.deleteBgbsucByCondition(request);
            result.set("success", true);
            result.set("deleteCount", deleteCount);
        } catch (Exception e) {
            result.set("success", false);
            result.set("message", e.getMessage());
            result.set("cause", e.getCause() != null ? e.getCause().getMessage() : "");
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 查询部门编号和名称
     * 必传：token
     * 可选：ooefl001(部门编号)、ooefl003(部门名称)、ooefl002(语言，默认zh_CN)、
     *       ooeg003(责任中心类型，有值才作为条件)
     * 固定：ooegent=60, ooeg009='NBYL', ooegstus='Y'
     */
    @PostMapping("/queryDept")
    public JSONObject queryDept(@RequestBody Map<String, Object> request) {
        JSONObject result = new JSONObject();
        try {
            List<Map<String, Object>> list = dsdataMapper.queryDept(request);
            result.set("success", true);
            result.set("data", list);
        } catch (Exception e) {
            result.set("success", false);
            result.set("message", e.getMessage());
            result.set("cause", e.getCause() != null ? e.getCause().getMessage() : "");
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 查询科目编码和名称
     * 必传：token
     * 可选：glacl002(科目编码)、glacl004(科目名称)、glacl003(语言，默认zh_CN)
     * 固定：glaclent=60
     */
    @PostMapping("/querySubject")
    public JSONObject querySubject(@RequestBody Map<String, Object> request) {
        JSONObject result = new JSONObject();
        try {
            List<Map<String, Object>> list = dsdataMapper.querySubject(request);
            result.set("success", true);
            result.set("data", list);
        } catch (Exception e) {
            result.set("success", false);
            result.set("message", e.getMessage());
            result.set("cause", e.getCause() != null ? e.getCause().getMessage() : "");
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 查询产线名和成本中心（ooeluc_t）
     * 前端 POST /queryOoeluc
     * 请求体 JSON 示例：
     * {
     *     "token": "xxx",
     *     "ooleucent": "60",        // 账套，默认 60（前端参数名，兼容 ooelucent）
     *     "ooelucsite": "NBYL",     // 据点，默认 NBYL
     *     "ooeluc003": "产线名",     // 产线名，可选，模糊匹配
     *     "ooeluc004": "成本中心编码" // 成本中心编码，可选，精确匹配
     * }
     * 返回: { "success": true, "data": [...], "total": n }
     * 数据列: ooelucent, ooelucsite, ooeluc003(产线名), ooeluc004(成本中心编码)
     */
    @PostMapping("/queryOoeluc")
    public JSONObject queryOoeluc(@RequestBody Map<String, Object> request) {
        JSONObject result = new JSONObject();
        try {
            // 前端参数名 ooleucent/ooelucent 均可，数据库列名为 ooelucent
            String ent = getString(request, "ooleucent");
            if (isBlank(ent)) ent = getString(request, "ooelucent");
            if (isBlank(ent)) ent = "60";
            String site = getString(request, "ooelucsite");
            if (isBlank(site)) site = "NBYL";
            String productLine = getString(request, "ooeluc003");
            String costCenter = getString(request, "ooeluc004");

            Map<String, Object> params = new HashMap<>();
            params.put("ooelucent", ent);
            params.put("ooelucsite", site);
            if (!isBlank(productLine)) params.put("ooeluc003", productLine);
            if (!isBlank(costCenter)) params.put("ooeluc004", costCenter);

            List<Map<String, Object>> list = dsdataMapper.queryOoeluc(params);

            JSONArray data = new JSONArray();
            for (Map<String, Object> row : list) {
                JSONObject item = new JSONObject();
                for (Map.Entry<String, Object> entry : row.entrySet()) {
                    item.set(entry.getKey(), entry.getValue());
                }
                data.add(item);
            }

            result.set("success", true);
            result.set("data", data);
            result.set("total", data.size());
        } catch (Exception e) {
            result.set("success", false);
            result.set("message", e.getMessage());
            result.set("cause", e.getCause() != null ? e.getCause().getMessage() : "");
            e.printStackTrace();
        }
        return result;
    }

    /** 用主键字段构造主键字符串（用于比较），字段名大小写不敏感 */
    private String buildPkKey(Map<String, Object> item, List<String> pkFields) {
        StringBuilder sb = new StringBuilder();
        for (String f : pkFields) {
            Object val = getValueIgnoreCase(item, f);
            sb.append(val != null ? val : "NULL").append("|");
        }
        return sb.toString();
    }

    /** 大小写不敏感地从 Map 中取字段值 */
    private Object getValueIgnoreCase(Map<String, Object> map, String key) {
        if (map == null || key == null) {
            return null;
        }
        if (map.containsKey(key)) {
            return map.get(key);
        }
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    /** 将 null 转为空串，避免 fastjson 序列化时丢弃为 null 的字段 */
    private static String nvl(Object val) {
        return val == null ? "" : String.valueOf(val);
    }

    /**
     * 查询 bgbsuc_t 预算结存表
     * 前端 POST /queryBgbsuc
     * 请求体 JSON 示例：
     * {
     *     "token": "xxx",                // 会被忽略
     *     "bgbsucent": "60",             // 可选
     *     "bgbsucld": "NBYL",            // 可选
     *     "bgbsuc001": "...",            // 可选
     *     "bgbsuc002": "...",            // 可选
     *     ... bgbsuc_t 任意字段均可作为条件
     * }
     * 返回: { "master": [{ 所有字段... }], "total": n }
     * 规则：一个条件都没有时不返回数据；有查询条件时补充默认值 bgbsucent=60, bgbsucld=NBYL。
     */
    @PostMapping("/queryBgbsuc")
    public JSONObject queryBgbsuc(@RequestBody Map<String, Object> request) {
        final String DEFAULT_ENT_FIELD = "bgbsucent";
        final String DEFAULT_SITE_FIELD = "bgbsucld";

        JSONObject result = new JSONObject();
        JSONArray master = new JSONArray();

        // 1. 收集非空查询条件（token 不作为条件）
        Map<String, Object> condition = new HashMap<>();
        for (Map.Entry<String, Object> entry : request.entrySet()) {
            String key = entry.getKey();
            if ("token".equalsIgnoreCase(key)) continue;
            Object val = entry.getValue();
            if (val == null) continue;
            if (val instanceof String && ((String) val).trim().isEmpty()) continue;
            condition.put(key, val);
        }

        // 2. 没有任何条件时直接返回空
        if (condition.isEmpty()) {
            result.set("master", master);
            result.set("total", 0);
            return result;
        }

        // 3. 补充默认值
        if (isBlank(condition.get(DEFAULT_ENT_FIELD))) {
            condition.put(DEFAULT_ENT_FIELD, "60");
        }
        if (isBlank(condition.get(DEFAULT_SITE_FIELD))) {
            condition.put(DEFAULT_SITE_FIELD, "NBYL");
        }

        // 4. 查询并统一把字段名转小写返回
        List<Map<String, Object>> rows = dsdataMapper.queryBgbsuc(condition);
        for (Map<String, Object> row : rows) {
            JSONObject item = new JSONObject();
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                item.set(entry.getKey().toLowerCase(), entry.getValue());
            }
            master.add(item);
        }

        result.set("master", master);
        result.set("total", master.size());
        return result;
    }

    /**
     * 查询 bgbtuc_t 采购价格预算表
     * 前端 POST /queryBgbtuc
     * 请求体 JSON 示例：
     * {
     *     "token": "xxx",                // 会被忽略
     *     "bgbtucent": "60",             // 可选，默认 60
     *     "bgbtucld": "NBYL",            // 可选，默认 NBYL
     *     "bgbtuc001": "...",            // 物料编号，可选
     *     "bgbtuc002": "2026",           // 年度，可选
     *     ... bgbtuc_t 任意字段均可作为条件
     * }
     * 返回: { "master": [{ 所有字段... }], "total": n }
     * 规则：一个条件都没有时不返回数据；有查询条件时补充默认值 bgbtucent=60, bgbtucld=NBYL。
     */
    @PostMapping("/queryBgbtuc")
    public JSONObject queryBgbtuc(@RequestBody Map<String, Object> request) {
        final String DEFAULT_ENT_FIELD = "bgbtucent";
        final String DEFAULT_SITE_FIELD = "bgbtucld";

        JSONObject result = new JSONObject();
        JSONArray master = new JSONArray();

        // 1. 收集非空查询条件（token 不作为条件）
        Map<String, Object> condition = new HashMap<>();
        for (Map.Entry<String, Object> entry : request.entrySet()) {
            String key = entry.getKey();
            if ("token".equalsIgnoreCase(key)) continue;
            Object val = entry.getValue();
            if (val == null) continue;
            if (val instanceof String && ((String) val).trim().isEmpty()) continue;
            condition.put(key, val);
        }

        // 2. 没有任何条件时直接返回空
        if (condition.isEmpty()) {
            result.set("master", master);
            result.set("total", 0);
            return result;
        }

        // 3. 补充默认值
        if (isBlank(condition.get(DEFAULT_ENT_FIELD))) {
            condition.put(DEFAULT_ENT_FIELD, "60");
        }
        if (isBlank(condition.get(DEFAULT_SITE_FIELD))) {
            condition.put(DEFAULT_SITE_FIELD, "NBYL");
        }

        // 4. 查询并统一把字段名转小写返回
        List<Map<String, Object>> rows = dsdataMapper.queryBgbtuc(condition);
        for (Map<String, Object> row : rows) {
            JSONObject item = new JSONObject();
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                item.set(entry.getKey().toLowerCase(), entry.getValue());
            }
            master.add(item);
        }

        result.set("master", master);
        result.set("total", master.size());
        return result;
    }

    /**
     * 保存/更新 bgbtuc_t 采购价格预算表
     * 前端 POST /saveBgbtuc
     * 请求体 JSON 示例：
     * {
     *     "list": [
     *         {
     *             "bgbtucent": "60",       // 企业代码，默认 60
     *             "bgbtucld": "NBYL",      // 账别，默认 NBYL
     *             "bgbtuc001": "...",      // 物料编号
     *             "bgbtuc002": "2026",     // 年度
     *             "bgbtuc005": 100.00,     // 本币金额
     *             "bgbtuc003": "..."       // 参考供应商
     *         },
     *         ...
     *     ],
     *     "pkFields": ["bgbtucent", "bgbtucld", "bgbtuc001", "bgbtuc002"]   // 可覆盖
     * }
     * 返回: { "success": true, "insertCount": x, "updateCount": y, "deleteCount": z }
     */
    @PostMapping("/saveBgbtuc")
    public JSONObject saveBgbtuc(@RequestBody Map<String, Object> request) {
        final String DEFAULT_ENT_FIELD = "bgbtucent";
        final String DEFAULT_SITE_FIELD = "bgbtucld";
        final List<String> DEFAULT_PK_FIELDS = java.util.Arrays.asList(
                "bgbtucent", "bgbtucld", "bgbtuc001", "bgbtuc002");

        JSONObject result = new JSONObject();
        int insertCount = 0;
        int updateCount = 0;
        int deleteCount = 0;

        try {
            List<Map<String, Object>> list;
            if (request.containsKey("list")) {
                list = (List<Map<String, Object>>) request.get("list");
            } else if (request.containsKey("data")) {
                list = new ArrayList<>();
                list.add((Map<String, Object>) request.get("data"));
            } else {
                list = new ArrayList<>();
                list.add(request);
            }

            List<String> pkFields = DEFAULT_PK_FIELDS;
            if (request.containsKey("pkFields")) {
                pkFields = (List<String>) request.get("pkFields");
            }

            if (list != null) {
                // 补默认值
                for (Map<String, Object> item : list) {
                    if (isBlank(item.get(DEFAULT_ENT_FIELD))) {
                        item.put(DEFAULT_ENT_FIELD, "60");
                    }
                    if (isBlank(item.get(DEFAULT_SITE_FIELD))) {
                        item.put(DEFAULT_SITE_FIELD, "NBYL");
                    }
                }

                // 一、保存/更新
                for (Map<String, Object> item : list) {
                    Map<String, Object> params = new HashMap<>(item);
                    params.put("pkFields", pkFields);

                    Map<String, Object> existing = dsdataMapper.findBgbtucByPk(params);
                    if (existing == null || existing.isEmpty()) {
                        insertCount += dsdataMapper.insertBgbtuc(params);
                    } else {
                        updateCount += dsdataMapper.updateBgbtuc(params);
                    }
                }

                // 二、同步删除：收集前端所有主键，查DB同账套据点下全部记录，删掉前端没有的
                Set<String> allFrontendPks = new HashSet<>();
                Set<String> entSiteSet = new HashSet<>();
                for (Map<String, Object> item : list) {
                    allFrontendPks.add(buildPkKey(item, pkFields));
                    String es = item.get(DEFAULT_ENT_FIELD) + "|" + item.get(DEFAULT_SITE_FIELD);
                    entSiteSet.add(es);
                }
                System.out.println("[saveBgbtuc] 前端主键集合: " + allFrontendPks);

                for (String es : entSiteSet) {
                    String[] parts = es.split("\\|");
                    Map<String, Object> filter = new HashMap<>();
                    filter.put("bgbtucent", parts[0]);
                    filter.put("bgbtucld", parts[1]);

                    List<Map<String, Object>> dbRows = dsdataMapper.queryBgbtuc(filter);
                    System.out.println("[saveBgbtuc] 账套=" + parts[0] + " 据点=" + parts[1] + " DB查回记录数: " + dbRows.size());
                    for (Map<String, Object> dbRow : dbRows) {
                        String dbPk = buildPkKey(dbRow, pkFields);
                        System.out.println("[saveBgbtuc]   DB行PK: " + dbPk + "  是否在前端: " + allFrontendPks.contains(dbPk));
                        if (!allFrontendPks.contains(dbPk)) {
                            Map<String, Object> delParams = new HashMap<>();
                            for (String pk : pkFields) {
                                delParams.put(pk, getValueIgnoreCase(dbRow, pk));
                            }
                            delParams.put("pkFields", pkFields);
                            deleteCount += dsdataMapper.deleteBgbtucByPk(delParams);
                        }
                    }
                }
            }

            result.set("success", true);
            result.set("insertCount", insertCount);
            result.set("updateCount", updateCount);
            result.set("deleteCount", deleteCount);
        } catch (Exception e) {
            result.set("success", false);
            result.set("message", e.getMessage());
            result.set("cause", e.getCause() != null ? e.getCause().getMessage() : "");
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 删除 bgbtuc_t 采购价格预算记录
     * 前端 POST /deleteBgbtuc
     * 请求体 JSON 示例：
     * {
     *     "bgbtucent": "60",             // 可选，默认 60
     *     "bgbtucld": "NBYL",            // 可选，默认 NBYL
     *     "bgbtuc001": "...",            // 物料编号，可选
     *     "bgbtuc002": "2026"            // 年度，必填
     * }
     * 删除匹配所有非空字段的全部记录
     */
    @PostMapping("/deleteBgbtuc")
    public JSONObject deleteBgbtuc(@RequestBody Map<String, Object> request) {
        JSONObject result = new JSONObject();
        try {
            Map<String, Object> condition = new HashMap<>();
            for (Map.Entry<String, Object> entry : request.entrySet()) {
                String key = entry.getKey();
                if ("token".equalsIgnoreCase(key)) continue;
                Object val = entry.getValue();
                if (val == null) continue;
                if (val instanceof String && ((String) val).trim().isEmpty()) continue;
                condition.put(key, val);
            }

            // 补充默认值
            if (isBlank(condition.get("bgbtucent"))) {
                condition.put("bgbtucent", "60");
            }
            if (isBlank(condition.get("bgbtucld"))) {
                condition.put("bgbtucld", "NBYL");
            }

            // bgbtuc002 年度必填
            if (isBlank(condition.get("bgbtuc002"))) {
                result.set("success", false);
                result.set("message", "bgbtuc002(年度)不能为空");
                return result;
            }

            int deleteCount = dsdataMapper.deleteBgbtucByCondition(condition);
            result.set("success", true);
            result.set("deleteCount", deleteCount);
        } catch (Exception e) {
            result.set("success", false);
            result.set("message", e.getMessage());
            result.set("cause", e.getCause() != null ? e.getCause().getMessage() : "");
            e.printStackTrace();
        }
        return result;
    }

    private boolean isBlank(Object val) {
        if (val == null) return true;
        if (val instanceof String) return ((String) val).trim().isEmpty();
        return false;
    }

    /** 判断是否为合法数字（用于校验 NUMBER 型列，避免 Oracle ORA-01722） */
    private boolean isNumeric(String val) {
        if (val == null) return false;
        String t = val.trim();
        if (t.isEmpty()) return false;
        try {
            new BigDecimal(t);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 账号认证接口
     * 前端 POST /auth
     * 请求体 JSON: { "username": "账号", "password": "密码" }
     * 返回: { "flag": 1, "userName": 姓名, "deptNo": 部门编号, "deptName": 部门名称 } 密码匹配
     *      / { "flag": 0 } 密码不匹配或异常
     */
    @PostMapping("/auth")
    public JSONObject auth(@RequestBody Map<String, Object> request) {
        JSONObject result = new JSONObject();

        String username = getString(request, "username");
        String password = getString(request, "password");

        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            result.set("flag", 0);
            result.set("message", "用户名或密码不能为空");
            return result;
        }

        try {
            // 构建请求 ERP API 的 JSON
            JSONObject erpRequest = new JSONObject();
            erpRequest.set("key", "F8B78770712B4580AFC84BA9E35297DE");
            erpRequest.set("type", "sync");

            JSONObject host = new JSONObject();
            host.set("prod", "OA");
            host.set("ip", "192.168.0.84");
            host.set("lang", "zh_CN");
            host.set("acct", "tiptop");
            host.set("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            erpRequest.set("host", host);

            JSONObject service = new JSONObject();
            service.set("prod", "T100");
            service.set("name", "user-pass");
            service.set("ip", "192.168.0.80");
            service.set("id", "topprd");
            erpRequest.set("service", service);

            JSONObject datakey = new JSONObject();
            datakey.set("EntId", "60");
            datakey.set("CompanyId", "NBYL");
            erpRequest.set("datakey", datakey);

            JSONObject payload = new JSONObject();
            JSONObject stdData = new JSONObject();
            JSONObject parameter = new JSONObject();
            parameter.set("gzxd001", username);
            stdData.set("parameter", parameter);
            payload.set("std_data", stdData);
            erpRequest.set("payload", payload);

            // 调用 ERP API
            String erpResponse = HttpUtil.post("http://192.168.0.80/wstopprd/ws/r/awsp920",
                    erpRequest.toString());

            // 解析返回 JSON，提取 r_mm（密码）
            JSONObject erpJson = new JSONObject(erpResponse);
            String erpPassword = erpJson.getJSONObject("payload")
                    .getJSONObject("std_data")
                    .getJSONObject("parameter")
                    .getStr("r_mm");

            if (erpPassword == null) {
                result.set("flag", 0);
                result.set("message", "ERP 未返回密码");
                return result;
            }

            // 比较密码
            if (password.equals(erpPassword)) {
                result.set("flag", 1);
                // 认证通过后查询用户所属部门（ooag_t + ooefl_t）
                Map<String, Object> deptParams = new HashMap<>();
                deptParams.put("username", username);
                Map<String, Object> dept = dsdataMapper.queryAuthDept(deptParams);
                if (dept != null) {
                    result.set("userName", dept.get("userName"));
                    result.set("deptNo", dept.get("deptNo"));
                    result.set("deptName", dept.get("deptName"));
                } else {
                    result.set("userName", "");
                    result.set("deptNo", "");
                    result.set("deptName", "");
                }
            } else {
                result.set("flag", 0);
            }
        } catch (Exception e) {
            result.set("flag", 0);
            result.set("message", "认证服务异常: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 预算与实际差异明细表
     * 前端 POST /queryBudgetActualVariance
     * 请求体 JSON 示例：
     * {
     *     "token": "xxx",
     *     "ent": "60",          // 账套，默认 60
     *     "site": "NBYL",       // 账别，默认 NBYL
     *     "year": "2026",       // 年度
     *     "dept": "...",        // 部门（模糊匹配），可选
     *     "subjectName": "...", // 科目名称（模糊匹配），可选
     *     "summary": "..."      // 摘要（模糊匹配），可选
     * }
     */
    @PostMapping("/queryBudgetActualVariance")
    public JSONObject queryBudgetActualVariance(@RequestBody Map<String, Object> request) {
        JSONObject result = new JSONObject();
        try {
            String ent = getString(request, "ent");
            if (isBlank(ent)) ent = "60";
            String site = getString(request, "site");
            if (isBlank(site)) site = "NBYL";
            String year = getString(request, "year");
            String dept = getString(request, "dept");
            String subjectName = getString(request, "subjectName");
            String summary = getString(request, "summary");

            System.out.println("[queryBudgetActualVariance] params: ent=" + ent
                    + ", site=" + site + ", year=" + year + ", dept=" + dept
                    + ", subjectName=" + subjectName + ", summary=" + summary);

            Map<String, Object> params = new HashMap<>();
            params.put("ent", ent);
            params.put("site", site);
            params.put("year", year);
            if (!isBlank(dept)) params.put("dept", dept);
            if (!isBlank(subjectName)) params.put("subjectName", subjectName);
            if (!isBlank(summary)) params.put("summary", summary);

            List<Map<String, Object>> list = dsdataMapper.queryBudgetActualVariance(params);

            JSONArray data = new JSONArray();
            for (Map<String, Object> row : list) {
                JSONObject item = new JSONObject();
                for (Map.Entry<String, Object> entry : row.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    // 统一差异月份字段为两位：差异1月 -> 差异01月
                    if (key.matches("差异\\d+月")) {
                        String month = key.replaceAll("[^\\d]", "");
                        key = "差异" + String.format("%02d", Integer.parseInt(month)) + "月";
                    }
                    // 合计字段名统一
                    if ("预算合计金额".equals(key)) {
                        key = "预算合计";
                    } else if ("实际合计金额".equals(key)) {
                        key = "实际合计";
                    } else if ("合计差异金额".equals(key)) {
                        key = "合计差异";
                    }
                    // 数值列如果为 null 输出 0，避免字段丢失
                    if (value == null && (key.startsWith("实际") || key.startsWith("预算")
                            || key.startsWith("差异") || key.endsWith("合计"))) {
                        value = 0;
                    }
                    item.set(key, value);
                }
                data.add(item);
            }

            result.set("success", true);
            result.set("data", data);
            result.set("total", data.size());
        } catch (Exception e) {
            result.set("success", false);
            result.set("message", e.getMessage());
            result.set("cause", e.getCause() != null ? e.getCause().getMessage() : "");
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 预算采购核价分析报表
     * 前端 POST /queryBudgetPurchaseAnalysis
     * 请求体 JSON 示例：
     * {
     *     "token": "xxx",
     *     "ent": "60",          // 账套 APBAENT，默认 60
     *     "site": "NBYL",       // 据点 APBBCOMP，默认 NBYL
     *     "lang": "zh_CN",      // 语言 imaal002，默认 zh_CN
     *     "year": "2026",       // 年度
     *     "month": "8"          // 月份
     * }
     * 返回: { "success": true, "data": [...], "total": n }
     * 数据列: 年 月 品号 品名 规格 分群号 分群名 采购单号 入库单号 供应商 供应商名称
     *         对账数量 单位 实际采购含税单价 参考供应商 预算采购含税单价
     *         采购价差 偏差率 差异额
     */
    @PostMapping("/queryBudgetPurchaseAnalysis")
    public JSONObject queryBudgetPurchaseAnalysis(@RequestBody Map<String, Object> request) {
        JSONObject result = new JSONObject();
        try {
            String ent = getString(request, "ent");
            if (isBlank(ent)) ent = "60";
            String site = getString(request, "site");
            if (isBlank(site)) site = "NBYL";
            String lang = getString(request, "lang");
            if (isBlank(lang)) lang = "zh_CN";
            String year = getString(request, "year");
            String month = getString(request, "month");

            System.out.println("[queryBudgetPurchaseAnalysis] params: ent=" + ent
                    + ", site=" + site + ", lang=" + lang + ", year=" + year + ", month=" + month);

            Map<String, Object> params = new HashMap<>();
            params.put("ent", ent);
            params.put("site", site);
            params.put("lang", lang);
            if (!isBlank(year)) params.put("year", year);
            if (!isBlank(month)) params.put("month", month);

            List<Map<String, Object>> list = dsdataMapper.queryBudgetPurchaseAnalysis(params);

            JSONArray data = new JSONArray();
            for (Map<String, Object> row : list) {
                JSONObject item = new JSONObject();
                for (Map.Entry<String, Object> entry : row.entrySet()) {
                    item.set(entry.getKey(), entry.getValue());
                }
                data.add(item);
            }

            result.set("success", true);
            result.set("data", data);
            result.set("total", data.size());
        } catch (Exception e) {
            result.set("success", false);
            result.set("message", e.getMessage());
            result.set("cause", e.getCause() != null ? e.getCause().getMessage() : "");
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 根据订单品号递归展开 BOM，返回树形结构（用于前端树形目录显示）
     * 前端 POST /queryOrderBom
     * 请求体 JSON 示例：
     * {
     *     "token": "xxx",          // 会被忽略
     *     "ent": "60",             // 账套，默认 60
     *     "site": "NBYL",          // 据点，默认 NBYL
     *     "xmdddocno": "SO-001",   // 订单单号，可选
     *     "xmdd001": "A-001"       // 品号，可选
     * }
     * 关联工单主表 sfaa_t（条件：sfaaent=ent and sfaasite=site and sfaa022=订单号
     *   and sfaa023=订单序号 and sfaa010=bmba003），每个节点附加：
     *   sfaadocno(工单号)、sfaa068(站点)、sfaa019(预计开工日)、sfaa020(预计完工日)、
     *   sfaa012(生产数量)、sfaa050(入库数量)、sfaastus(状态码)、ooefl003(成本中心名)
     *   同一品号命中多张工单（一对多）时合并：sfaadocno 多个单号空格分隔；sfaa019 取最早；
     *   sfaa020 取最晚；sfaa012/sfaa050 求和；sfaa068/sfaastus/ooefl003 取第一条
     *   未匹配到工单时这些字段为空串
     * 返回: { "success": true, "tree": [根节点(含children, level)], "flat": [扁平明细], "total": n }
     * 节点字段: 订单品号, bmba001(主件), bmba009(BOM项序), bmba003(元件),
     *         BOM用量, bmba010(单位), 实际用量, 订单需求用量, xmdd011(出货日期), level, children,
     *         imae035(默认成本中心), imae035Name(默认成本中心名),
     *         sfaadocno, sfaa068, sfaa019, sfaa020, sfaa012, sfaa050, sfaastus
     */
    @PostMapping("/queryOrderBom")
    public JSONObject queryOrderBom(@RequestBody Map<String, Object> request) {
        JSONObject result = new JSONObject();
        try {
            String ent = getString(request, "ent");
            if (isBlank(ent)) ent = "60";
            String site = getString(request, "site");
            if (isBlank(site)) site = "NBYL";
            String docNo = getString(request, "xmdddocno");
            String itemNo = getString(request, "xmdd001");
            String seq = getString(request, "xmddseq");
            String imaf013 = getString(request, "imaf013");
            String lang = getString(request, "lang");
            if (isBlank(lang)) lang = "zh_CN";

            Map<String, Object> params = new HashMap<>();
            params.put("ent", ent);
            params.put("site", site);
            if (!isBlank(docNo)) params.put("xmdddocno", docNo);
            if (!isBlank(itemNo)) params.put("xmdd001", itemNo);
            if (!isBlank(seq)) params.put("xmddseq", seq);

            List<Map<String, Object>> orderItems = dsdataMapper.queryXmddItems(params);
            JSONArray tree = new JSONArray();
            JSONArray flat = new JSONArray();

            // 按「订单号+订单序号」预先查出该订单的全部工单，按生产料号 sfaa010 建索引
            // 这样整棵树只需查一次库，避免每个节点都查一遍
            // 同一品号可能对应多张工单（一对多），此处全部保留，合并规则见 attachSfaaInfo
            Map<String, List<Map<String, Object>>> sfaaIndex = new HashMap<>();
            if (!isBlank(docNo)) {
                Map<String, Object> sfaaParams = new HashMap<>();
                sfaaParams.put("ent", ent);
                sfaaParams.put("site", site);
                sfaaParams.put("orderNo", docNo);
                sfaaParams.put("lang", lang);
                if (!isBlank(seq)) {
                    sfaaParams.put("orderSeq", seq);
                }
                List<Map<String, Object>> sfaaRows = dsdataMapper.querySfaaByOrder(sfaaParams);
                if (sfaaRows != null) {
                    for (Map<String, Object> r : sfaaRows) {
                        String item010 = stringValueIgnoreCase(r, "sfaa010");
                        // 同一品号的多张工单合并为一个列表（SQL 已按 sfaadocno 排序，结果确定）
                        if (!isBlank(item010)) {
                            sfaaIndex.computeIfAbsent(item010, k -> new ArrayList<>()).add(r);
                        }
                    }
                }
                System.out.println("[queryOrderBom] 工单索引: 订单号=" + docNo + ", 序号=" + seq
                        + ", 工单条数=" + (sfaaRows == null ? 0 : sfaaRows.size())
                        + ", 涉及品号数=" + sfaaIndex.size());
            }

            for (Map<String, Object> item : orderItems) {
                String rootItem = stringValueIgnoreCase(item, "xmdd001");
                String orderDocNo = stringValueIgnoreCase(item, "xmdddocno");
                String orderSeq = stringValueIgnoreCase(item, "xmddseq");
                BigDecimal orderQty = toBigDecimal(getValueIgnoreCase(item, "xmdd005"));
                String shipDate = stringValueIgnoreCase(item, "xmdd011");
                if (isBlank(rootItem)) continue;

                // 根节点（订单品号）的补货策略 imaf013
                Map<String, Object> rootIm = dsdataMapper.queryItemIm(ent, site, rootItem);
                String rootIm013 = (rootIm == null) ? "" : stringValueIgnoreCase(rootIm, "imaf013");
                // 根节点（订单品号）品名/规格
                Map<String, Object> rootDesc = dsdataMapper.queryItemDesc(ent, lang, rootItem);
                String rootImaal003 = (rootDesc == null) ? "" : stringValueIgnoreCase(rootDesc, "imaal003");
                String rootImaal004 = (rootDesc == null) ? "" : stringValueIgnoreCase(rootDesc, "imaal004");
                // 根节点（订单品号）默认成本中心 imae035 及其名称 ooefl003
                Map<String, Object> rootCC = dsdataMapper.queryItemCostCenter(ent, site, rootItem, lang);
                String rootIm035 = (rootCC == null) ? "" : stringValueIgnoreCase(rootCC, "imae035");
                String rootIm035Name = (rootCC == null) ? "" : stringValueIgnoreCase(rootCC, "imae035Name");

                // 根节点：订单品号本身，实际用量=1
                JSONObject root = new JSONObject();
                root.set("订单号", orderDocNo);
                root.set("订单序号", orderSeq);
                root.set("订单品号", rootItem);
                root.set("imaf013", rootIm013);
                root.set("imaal003", rootImaal003);
                root.set("imaal004", rootImaal004);
                root.set("xmdd011", shipDate);
                root.set("imae035", rootIm035);
                root.set("imae035Name", rootIm035Name);
                root.set("bmba001", rootItem);
                root.set("bmba009", "");
                root.set("bmba003", rootItem);
                root.set("BOM用量", BigDecimal.ONE);
                root.set("bmba010", "");
                root.set("实际用量", BigDecimal.ONE);
                root.set("订单需求用量", orderQty.setScale(2, RoundingMode.HALF_UP));
                root.set("level", 1);
                // 关联工单（按生产料号 sfaa010 = 根节点品号匹配）
                attachSfaaInfo(root, sfaaIndex, rootItem);
                JSONArray children = new JSONArray();
                root.set("children", children);
                tree.add(root);

                // 根节点同时加入扁平明细
                JSONObject flatRoot = buildFlatNode(orderDocNo, orderSeq, rootItem, rootItem, "", rootItem,
                        BigDecimal.ONE, "", BigDecimal.ONE, orderQty, 1, shipDate);
                attachSfaaInfo(flatRoot, sfaaIndex, rootItem);
                flatRoot.set("imaf013", rootIm013);
                flatRoot.set("imaal003", rootImaal003);
                flatRoot.set("imaal004", rootImaal004);
                flatRoot.set("xmdd011", shipDate);
                flatRoot.set("imae035", rootIm035);
                flatRoot.set("imae035Name", rootIm035Name);
                flat.add(flatRoot);

                // 从第一层 BOM 开始递归下展
                expandBomTree(ent, site, orderDocNo, orderSeq, shipDate, rootItem, rootItem, BigDecimal.ONE, orderQty,
                        children, flat, 2, new HashSet<>(), sfaaIndex, imaf013, lang);
            }

            result.set("success", true);
            result.set("tree", tree);
            result.set("flat", flat);
            result.set("total", tree.size());
        } catch (Exception e) {
            result.set("success", false);
            result.set("message", e.getMessage());
            result.set("cause", e.getCause() != null ? e.getCause().getMessage() : "");
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 递归展开 BOM，生成树形节点并同时输出扁平明细
     *
     * @param rootItem        订单根品号
     * @param parentItem      当前主件（bmba001）
     * @param parentActualQty 父件卷算后的实际用量
     * @param orderQty        订单数量
     * @param children        当前层 children 数组
     * @param flat            扁平明细结果集
     * @param level           当前层级（根为 1，向下递增）
     * @param path            当前递归路径，防止循环引用
     * @param sfaaIndex       工单索引（key = 生产料号 sfaa010），用于给节点附加工单信息
     */
    private void expandBomTree(String ent, String site, String docNo, String seq, String shipDate,
                               String rootItem, String parentItem,
                               BigDecimal parentActualQty, BigDecimal orderQty,
                               JSONArray children, JSONArray flat, int level, Set<String> path,
                               Map<String, List<Map<String, Object>>> sfaaIndex, String imaf013, String lang) {
        if (path.contains(parentItem)) return;
        path.add(parentItem);

        List<Map<String, Object>> childRows = dsdataMapper.queryBomChildren(ent, site, parentItem, imaf013, lang);
        if (childRows == null || childRows.isEmpty()) {
            path.remove(parentItem);
            return;
        }

        for (Map<String, Object> child : childRows) {
            String childItem = stringValueIgnoreCase(child, "bmba003");
            String childUnit = stringValueIgnoreCase(child, "bmba010");
            String childSeq = stringValueIgnoreCase(child, "bmba009");
            String parentNo = stringValueIgnoreCase(child, "bmba001");
            BigDecimal bmba011 = toBigDecimal(getValueIgnoreCase(child, "bmba011"));
            BigDecimal bmba012 = toBigDecimal(getValueIgnoreCase(child, "bmba012"));
            BigDecimal bmbb011 = toBigDecimal(getValueIgnoreCase(child, "bmbb011"));

            if (bmba012.compareTo(BigDecimal.ZERO) == 0) {
                bmba012 = BigDecimal.ONE;
            }

            // 损耗率 bmbb011 为百分比值（如 5 表示 5%），需除以 100 才是真实损耗率
            BigDecimal lossRate = bmbb011.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
            // BOM用量 = bmba011 * (1 + 损耗率) / bmba012，保留6位小数并去掉尾随无效0
            BigDecimal bomQty = bmba011.multiply(BigDecimal.ONE.add(lossRate))
                    .divide(bmba012, 10, RoundingMode.HALF_UP)
                    .setScale(6, RoundingMode.HALF_UP)
                    .stripTrailingZeros();
            // 实际用量 = 父件实际用量 * BOM用量，保留6位小数并去掉尾随无效0
            BigDecimal actualQty = parentActualQty.multiply(bomQty)
                    .setScale(6, RoundingMode.HALF_UP)
                    .stripTrailingZeros();
            // 订单需求用量 = 实际用量 * 订单数量，保留2位小数
            BigDecimal demandQty = actualQty.multiply(orderQty)
                    .setScale(2, RoundingMode.HALF_UP);

            JSONObject node = new JSONObject();
            node.set("订单号", docNo);
            node.set("订单序号", seq);
            node.set("订单品号", rootItem);
            node.set("bmba001", parentNo);
            node.set("bmba009", childSeq);
            node.set("bmba003", childItem);
            node.set("BOM用量", bomQty);
            node.set("bmba010", childUnit);
            node.set("实际用量", actualQty);
            node.set("订单需求用量", demandQty);
            node.set("level", level);
            // 补货策略（自制件/采购件等）
            node.set("imaf013", stringValueIgnoreCase(child, "imaf013"));
            // 品名 / 规格
            node.set("imaal003", stringValueIgnoreCase(child, "imaal003"));
            node.set("imaal004", stringValueIgnoreCase(child, "imaal004"));
            node.set("xmdd011", shipDate);
            node.set("imae035", stringValueIgnoreCase(child, "imae035"));
            node.set("imae035Name", stringValueIgnoreCase(child, "imae035Name"));
            // 关联工单（按生产料号 sfaa010 = 元件品号 bmba003 匹配）
            attachSfaaInfo(node, sfaaIndex, childItem);
            JSONArray subChildren = new JSONArray();
            node.set("children", subChildren);
            children.add(node);

            // 扁平明细副本（不含 children，便于表格/导出）
            JSONObject flatNode = buildFlatNode(docNo, seq, rootItem, parentNo, childSeq, childItem,
                    bomQty, childUnit, actualQty, demandQty, level, shipDate);
            attachSfaaInfo(flatNode, sfaaIndex, childItem);
            flatNode.set("imaf013", stringValueIgnoreCase(child, "imaf013"));
            flatNode.set("imaal003", stringValueIgnoreCase(child, "imaal003"));
            flatNode.set("imaal004", stringValueIgnoreCase(child, "imaal004"));
            flatNode.set("imae035", stringValueIgnoreCase(child, "imae035"));
            flatNode.set("imae035Name", stringValueIgnoreCase(child, "imae035Name"));
            flat.add(flatNode);

            expandBomTree(ent, site, docNo, seq, shipDate, rootItem, childItem, actualQty, orderQty,
                    subChildren, flat, level + 1, path, sfaaIndex, imaf013, lang);
        }

        path.remove(parentItem);
    }

    /** 构建扁平明细节点 */
    private JSONObject buildFlatNode(String docNo, String seq, String rootItem, String parentNo,
                                     String bomSeq, String item, BigDecimal bomQty, String unit,
                                     BigDecimal actualQty, BigDecimal demandQty, int level, String shipDate) {
        JSONObject row = new JSONObject();
        row.set("订单号", docNo);
        row.set("订单序号", seq);
        row.set("订单品号", rootItem);
        row.set("bmba001", parentNo);
        row.set("bmba009", bomSeq);
        row.set("bmba003", item);
        row.set("BOM用量", bomQty);
        row.set("bmba010", unit);
        row.set("实际用量", actualQty);
        row.set("订单需求用量", demandQty);
        row.set("level", level);
        row.set("xmdd011", shipDate);
        return row;
    }

    /**
     * 给 BOM 节点附加工单信息（按生产料号 sfaa010 = 品号匹配）
     * 关联条件已在查询工单时限定：sfaaent=ent and sfaasite=site
     *   and sfaa022=订单号 and sfaa023=订单序号
     *
     * 同一品号命中多张工单（一对多）时按以下规则合并：
     *   A sfaadocno  多个工单号以空格分隔
     *   B sfaa019    预计开工日取最早
     *   C sfaa020    预计完工日取最晚
     *   D sfaa012    生产数量求和
     *   E sfaa050    入库数量求和
     *   sfaa068 / sfaastus / ooefl003 取排序后第一条（SQL 已按 sfaadocno 排序，结果确定）
     * 未匹配到工单时这些字段置为空串
     */
    private void attachSfaaInfo(JSONObject node, Map<String, List<Map<String, Object>>> sfaaIndex, String itemNo) {
        List<Map<String, Object>> list = (sfaaIndex == null || isBlank(itemNo)) ? null : sfaaIndex.get(itemNo);
        if (list == null || list.isEmpty()) {
            node.set("sfaadocno", "");
            node.set("sfaa068", "");
            node.set("sfaa019", "");
            node.set("sfaa020", "");
            node.set("sfaa012", "");
            node.set("sfaa050", "");
            node.set("sfaastus", "");
            node.set("ooefl003", "");
            return;
        }
        StringBuilder docNos = new StringBuilder();
        String min019 = null;
        String max020 = null;
        BigDecimal sum012 = BigDecimal.ZERO;
        BigDecimal sum050 = BigDecimal.ZERO;
        String first068 = "", firstStus = "", firstOoefl = "";
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> r = list.get(i);
            // A 多个工单号以空格分隔
            String doc = stringValueIgnoreCase(r, "sfaadocno");
            if (!doc.isEmpty()) {
                if (docNos.length() > 0) docNos.append(' ');
                docNos.append(doc);
            }
            // B 预计开工日取最早（日期为 YYYY-MM-DD，字符串比较即可）
            String s019 = stringValueIgnoreCase(r, "sfaa019");
            if (!s019.isEmpty() && (min019 == null || s019.compareTo(min019) < 0)) {
                min019 = s019;
            }
            // C 预计完工日取最晚
            String s020 = stringValueIgnoreCase(r, "sfaa020");
            if (!s020.isEmpty() && (max020 == null || s020.compareTo(max020) > 0)) {
                max020 = s020;
            }
            // D 生产数量求和、E 入库数量求和
            sum012 = sum012.add(toBigDecimal(getValueIgnoreCase(r, "sfaa012")));
            sum050 = sum050.add(toBigDecimal(getValueIgnoreCase(r, "sfaa050")));
            if (i == 0) {
                first068 = stringValueIgnoreCase(r, "sfaa068");
                firstStus = stringValueIgnoreCase(r, "sfaastus");
                firstOoefl = stringValueIgnoreCase(r, "ooefl003");
            }
        }
        node.set("sfaadocno", docNos.toString());
        node.set("sfaa068", first068);
        node.set("sfaa019", min019 == null ? "" : min019);
        node.set("sfaa020", max020 == null ? "" : max020);
        // 合计结果去掉多余小数 0，避免科学计数法（如 400.00 → 400）
        node.set("sfaa012", sum012.stripTrailingZeros().toPlainString());
        node.set("sfaa050", sum050.stripTrailingZeros().toPlainString());
        node.set("sfaastus", firstStus);
        node.set("ooefl003", firstOoefl);
    }

    /**
     * 查询管理系统菜单目录树（gzweuc_t）
     * 参数：ent 默认 60；site
     */
    @PostMapping("/queryMenuTree")
    public JSONObject queryMenuTree(@RequestBody Map<String, Object> params) {
        JSONObject result = new JSONObject();
        try {
            Map<String, Object> p = new HashMap<>();
            p.put("gzweucent", getString(params, "ent") == null ? "60" : getString(params, "ent"));

            List<Map<String, Object>> rows = dsdataMapper.queryMenuTree(p);
            if (rows == null || rows.isEmpty()) {
                result.set("success", true);
                result.set("tree", new JSONArray());
                result.set("total", 0);
                return result;
            }

            // 收集所有目录编号，用于判断根节点
            Set<String> idSet = new HashSet<>();
            for (Map<String, Object> row : rows) {
                idSet.add(stringValueIgnoreCase(row, "gzweuc002"));
            }

            // 按父节点分组
            Map<String, List<Map<String, Object>>> childrenMap = new HashMap<>();
            List<Map<String, Object>> roots = new ArrayList<>();
            for (Map<String, Object> row : rows) {
                String parent = stringValueIgnoreCase(row, "gzweuc001");
                String ownId = stringValueIgnoreCase(row, "gzweuc002");
                // 根节点判定：上阶目录为空，或上阶目录不存在于结果集中，或上阶目录等于自身编号（如顶层自引用 000）
                if (isBlank(parent) || !idSet.contains(parent) || parent.equals(ownId)) {
                    roots.add(row);
                } else {
                    childrenMap.computeIfAbsent(parent, k -> new ArrayList<>()).add(row);
                }
            }

            // 按显示顺序排序
            Comparator<Map<String, Object>> orderComparator = (a, b) -> {
                BigDecimal aa = toBigDecimal(getValueIgnoreCase(a, "gzweuc003"));
                BigDecimal bb = toBigDecimal(getValueIgnoreCase(b, "gzweuc003"));
                return aa.compareTo(bb);
            };
            roots.sort(orderComparator);

            JSONArray tree = new JSONArray();
            for (Map<String, Object> root : roots) {
                tree.add(buildMenuNode(root, childrenMap, orderComparator, new HashSet<>()));
            }

            result.set("success", true);
            result.set("tree", tree);
            result.set("total", rows.size());
        } catch (Exception e) {
            result.set("success", false);
            result.set("error", e.getMessage());
        }
        return result;
    }

    private JSONObject buildMenuNode(Map<String, Object> row,
                                     Map<String, List<Map<String, Object>>> childrenMap,
                                     Comparator<Map<String, Object>> orderComparator,
                                     Set<String> visited) {
        String id = stringValueIgnoreCase(row, "gzweuc002");
        JSONObject node = new JSONObject();
        node.set("gzweuc001", stringValueIgnoreCase(row, "gzweuc001"));
        node.set("gzweuc002", id);
        node.set("gzweuc003", getValueIgnoreCase(row, "gzweuc003"));
        node.set("gzweuc004", stringValueIgnoreCase(row, "gzweuc004"));

        JSONArray children = new JSONArray();
        if (!visited.contains(id)) {
            visited.add(id);
            List<Map<String, Object>> childRows = childrenMap.get(id);
            if (childRows != null) {
                childRows.sort(orderComparator);
                for (Map<String, Object> child : childRows) {
                    children.add(buildMenuNode(child, childrenMap, orderComparator, visited));
                }
            }
            visited.remove(id);
        }
        node.set("children", children);
        return node;
    }

    private String stringValueIgnoreCase(Map<String, Object> map, String key) {
        Object val = getValueIgnoreCase(map, key);
        return val == null ? "" : String.valueOf(val);
    }

    /**
     * 保存/更新 sfajuc_t 日计划表
     * 前端 POST /saveSfajucDailyPlan
     * 请求体 JSON 示例：
     * {
     *     "list": [
     *         {
     *             "sfajucent": "60",        // 企业代码，默认 60
     *             "sfajucsite": "NBYL",       // 营运据点，默认 NBYL
     *             "sfajuc001": "WO-001",      // 工单号
     *             "sfajuc004": "LINE-A",      // 产线
     *             "sfajuc007": "2026-08-31",  // 排产日期（YYYY-MM-DD）
     *             "sfajuc003": "100.00"       // 数量
     *         },
     *         ...
     *     ]
     * }
     * 逻辑：按 账套+据点+工单号+产线+日期 作为关键字；
     *      不存在则插入，存在但数量不同则更新，数量相同则跳过。
     * 返回: { "success": true, "insertCount": x, "updateCount": y, "skipCount": z }
     */
    @PostMapping("/saveSfajucDailyPlan")
    public JSONObject saveSfajucDailyPlan(@RequestBody Map<String, Object> request) {
        JSONObject result = new JSONObject();
        int insertCount = 0;
        int updateCount = 0;
        int skipCount = 0;

        try {
            System.out.println("[saveSfajucDailyPlan] ==== 收到请求 ====");
            System.out.println("[saveSfajucDailyPlan] 顶层keys=" + request.keySet());
            System.out.println("[saveSfajucDailyPlan] 完整请求体=" + new JSONObject(request).toString());

            List<Map<String, Object>> list;
            if (request.containsKey("list")) {
                list = (List<Map<String, Object>>) request.get("list");
            } else if (request.containsKey("data")) {
                list = new ArrayList<>();
                list.add((Map<String, Object>) request.get("data"));
            } else {
                list = new ArrayList<>();
                list.add(request);
            }

            int receivedCount = (list == null) ? 0 : list.size();
            System.out.println("[saveSfajucDailyPlan] list条数=" + receivedCount);

            if (list != null) {
                for (Map<String, Object> item : list) {
                    System.out.println("[saveSfajucDailyPlan] 本条keys=" + item.keySet());
                    String sfajucent = getString(item, "sfajucent");
                    if (isBlank(sfajucent)) sfajucent = "60";
                    String sfajucsite = getString(item, "sfajucsite");
                    if (isBlank(sfajucsite)) sfajucsite = "NBYL";
                    String sfajuc001 = getString(item, "sfajuc001");
                    // 产线：主读 sfajuc004，兼容旧键名 sfajuc008
                    String sfajuc004 = getStringAny(item, "sfajuc004", "sfajuc008");
                    String sfajuc007 = getString(item, "sfajuc007");
                    String sfajuc003 = getString(item, "sfajuc003");

                    if (isBlank(sfajuc001) || isBlank(sfajuc004) || isBlank(sfajuc007)) {
                        System.out.println("[saveSfajucDailyPlan] !!!跳过本条,必填字段为空. ent=" + sfajucent
                                + ", site=" + sfajucsite + ", sfajuc001=" + sfajuc001
                                + ", sfajuc004=" + sfajuc004 + ", sfajuc007=" + sfajuc007);
                        continue;
                    }

                    List<sfajuc> existingList = sfajucMapper.findByKey(
                            sfajucent, sfajucsite, sfajuc001, sfajuc004, sfajuc007);
                    System.out.println("[saveSfajucDailyPlan] 查重结果条数=" + (existingList == null ? 0 : existingList.size()));

                    sfajuc record = new sfajuc();
                    record.setSfajucent(sfajucent);
                    record.setSfajucsite(sfajucsite);
                    record.setSfajuc001(sfajuc001);
                    record.setSfajuc004(sfajuc004);
                    record.setSfajuc007(sfajuc007);
                    record.setSfajuc003(isBlank(sfajuc003) ? "0" : sfajuc003);

                    if (existingList == null || existingList.isEmpty()) {
                        sfajucMapper.insert(record);
                        insertCount++;
                        System.out.println("[saveSfajucDailyPlan] inserted. key="
                                + sfajucent + "|" + sfajucsite + "|" + sfajuc001 + "|" + sfajuc004 + "|" + sfajuc007);
                    } else {
                        sfajuc existing = existingList.get(0);
                        BigDecimal newQty = toBigDecimal(record.getSfajuc003());
                        BigDecimal oldQty = toBigDecimal(existing.getSfajuc003());
                        if (newQty.compareTo(oldQty) != 0) {
                            sfajucMapper.update(record);
                            updateCount++;
                            System.out.println("[saveSfajucDailyPlan] updated. oldQty=" + oldQty + ", newQty=" + newQty);
                        } else {
                            skipCount++;
                            System.out.println("[saveSfajucDailyPlan] skipped. qty=" + newQty);
                        }
                    }
                }
            }

            result.set("success", true);
            result.set("receivedCount", receivedCount);
            result.set("insertCount", insertCount);
            result.set("updateCount", updateCount);
            result.set("skipCount", skipCount);
        } catch (Exception e) {
            result.set("success", false);
            result.set("message", e.getMessage());
            result.set("cause", e.getCause() != null ? e.getCause().getMessage() : "");
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 查询 sfajuc_t 日计划数量
     * 前端 POST /querySfajucDailyPlan
     * 请求体 JSON 示例：
     * {
     *     "sfajucent": "60",        // 可选，默认 60
     *     "sfajucsite": "NBYL",       // 可选，默认 NBYL
     *     "sfajuc001": "WO-001",      // 工单号
     *     "sfajuc004": "LINE-A",      // 产线
     *     "sfajuc007": "2026-08-31"   // 排产日期（YYYY-MM-DD）
     * }
     * 返回: { "success": true, "master": [{ sfajucent, sfajucsite, sfajuc001, sfajuc004, sfajuc007, sfajuc003, 数量 }], "total": n }
     */
    @PostMapping("/querySfajucDailyPlan")
    public JSONObject querySfajucDailyPlan(@RequestBody Map<String, Object> request) {
        JSONObject result = new JSONObject();
        try {
            String sfajucent = getString(request, "sfajucent");
            if (isBlank(sfajucent)) sfajucent = "60";
            String sfajucsite = getString(request, "sfajucsite");
            if (isBlank(sfajucsite)) sfajucsite = "NBYL";
            String sfajuc001 = getString(request, "sfajuc001");
            String sfajuc004 = getString(request, "sfajuc004");
            String sfajuc007 = getString(request, "sfajuc007");

            if (isBlank(sfajuc001) || isBlank(sfajuc004) || isBlank(sfajuc007)) {
                result.set("success", false);
                result.set("message", "工单号、产线、日期不能为空");
                return result;
            }

            List<sfajuc> list = sfajucMapper.findByKey(
                    sfajucent, sfajucsite, sfajuc001, sfajuc004, sfajuc007);

            JSONArray master = new JSONArray();
            if (list != null) {
                for (sfajuc row : list) {
                    JSONObject item = new JSONObject();
                    item.set("sfajucent", row.getSfajucent());
                    item.set("sfajucsite", row.getSfajucsite());
                    item.set("sfajuc001", row.getSfajuc001());
                    item.set("sfajuc004", row.getSfajuc004());
                    item.set("sfajuc007", row.getSfajuc007());
                    item.set("sfajuc003", row.getSfajuc003());
                    // 数量：与 sfajuc003 同值，空则补 0，方便前端直接绑定
                    item.set("数量", row.getSfajuc003() == null ? BigDecimal.ZERO : toBigDecimal(row.getSfajuc003()));
                    master.add(item);
                }
            }

            result.set("success", true);
            result.set("master", master);
            result.set("total", master.size());
        } catch (Exception e) {
            result.set("success", false);
            result.set("message", e.getMessage());
            result.set("cause", e.getCause() != null ? e.getCause().getMessage() : "");
            e.printStackTrace();
        }
        return result;
    }

    private String getString(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val == null ? null : String.valueOf(val);
    }

    /** 依次尝试多个键名，返回第一个非空值（用于兼容新旧字段名） */
    private String getStringAny(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            String val = getString(map, key);
            if (!isBlank(val)) {
                return val;
            }
        }
        return null;
    }

    /** 仅当源 Map 中该键有有效值（非 null / 非空串 / 非纯空白）时，才放入目标 Map */
    private void putIfNotBlank(Map<String, Object> target, Map<String, Object> source, String key) {
        Object val = source.get(key);
        if (isBlank(val)) {
            return;
        }
        target.put(key, val instanceof String ? ((String) val).trim() : val);
    }

    /**
     * 查询品号基础信息（imae_t）
     * 前端 POST /queryItemBasicInfo
     * 请求体 JSON 示例：
     * {
     *     "token": "xxx",
     *     "imaeent": "60",        // 可选，默认 60
     *     "imaesite": "NBYL",     // 可选，默认 NBYL
     *     "imae001": "品号"        // 必填
     * }
     * 返回字段：
     *   imae051 / 标准工时  - 标准工时
     *   UPPH               - 1小时产量 = 3600 / 标准工时（保留2位小数）
     *                        标准工时为 0 或 NULL 时 UPPH = 0
     * 返回: { "success": true, "data": [{ imae051, 标准工时, UPPH }], "total": n }
     */
    @PostMapping("/queryItemBasicInfo")
    public JSONObject queryItemBasicInfo(@RequestBody Map<String, Object> request) {
        JSONObject result = new JSONObject();
        try {
            String imaeent = getString(request, "imaeent");
            if (isBlank(imaeent)) imaeent = "60";
            String imaesite = getString(request, "imaesite");
            if (isBlank(imaesite)) imaesite = "NBYL";
            String imae001 = getString(request, "imae001");

            if (isBlank(imae001)) {
                result.set("success", false);
                result.set("message", "imae001(品号)不能为空");
                return result;
            }

            List<Map<String, Object>> rows = dsdataMapper.findImae051(imaeent, imaesite, imae001);

            JSONArray data = new JSONArray();
            if (rows != null) {
                for (Map<String, Object> row : rows) {
                    JSONObject item = new JSONObject();
                    for (Map.Entry<String, Object> entry : row.entrySet()) {
                        item.set(entry.getKey().toLowerCase(), entry.getValue());
                    }
                    // 标准工时
                    Object stdHourObj = getValueIgnoreCase(row, "imae051");
                    item.set("标准工时", stdHourObj);

                    // UPPH = 3600 / 标准工时，标准工时为 0 或 NULL 时为 0，保留2位小数
                    BigDecimal stdHour = toBigDecimal(stdHourObj);
                    BigDecimal upph = BigDecimal.ZERO;
                    if (stdHour.compareTo(BigDecimal.ZERO) != 0) {
                        upph = new BigDecimal("3600")
                                .divide(stdHour, 2, RoundingMode.HALF_UP);
                    }
                    item.set("UPPH", upph);
                    item.set("upph", upph);

                    data.add(item);
                }
            }

            result.set("success", true);
            result.set("data", data);
            result.set("total", data.size());
        } catch (Exception e) {
            result.set("success", false);
            result.set("message", e.getMessage());
            result.set("cause", e.getCause() != null ? e.getCause().getMessage() : "");
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 查询「可排产」工单：去掉已排完的工单，已排未排完的返回剩余可排数量
     * 前端 POST /querySchedulableWorkOrders
     * 请求体 JSON 示例：
     * {
     *     "token": "xxx",
     *     "sfaaent": "60",          // 可选，默认 60
     *     "sfaasite": "NBYL",       // 可选，默认 NBYL
     *     "sfaa010": "18097818352", // 生产品号，必填
     *     "sfaastus": "M,F,N,C",    // 可选，逗号分隔；不传则不加该条件
     *     "sfaa022": "来源单号",     // 可选，精确匹配；不传则不拼接
     *     "sfaa023": "来源序号",     // 可选，精确匹配；不传则不拼接
     *     "sfaadocno": "工单号"       // 可选，精确匹配；不传则不拼接
     * }
     * 返回字段：sfaadocno(工单号)、sfaastus(工单状态码)、sfaa010(生产品号)、sfaa012(生产数量)、kpsl(可排数量)、
     *          sfaa050(入库数量)、sfaa068(成本中心)、sfaa019(预计开工日期)、sfaa020(预计完工日期)
     * 最多返回 30 条
     */
    @PostMapping("/querySchedulableWorkOrders")
    public JSONObject querySchedulableWorkOrders(@RequestBody Map<String, Object> request) {
        JSONObject result = new JSONObject();
        JSONArray master = new JSONArray();
        try {
            String sfaaent = getString(request, "sfaaent");
            if (isBlank(sfaaent)) sfaaent = "60";
            String sfaasite = getString(request, "sfaasite");
            if (isBlank(sfaasite)) sfaasite = "NBYL";
            String sfaa010 = getString(request, "sfaa010");

            if (isBlank(sfaa010)) {
                result.set("success", false);
                result.set("message", "sfaa010(生产品号)不能为空");
                return result;
            }

            Map<String, Object> params = new HashMap<>();
            params.put("sfaaent", sfaaent);
            params.put("sfaasite", sfaasite);
            params.put("sfaa010", sfaa010);
            // 工单状态：可选，逗号分隔；空串/空白则不拼接
            putIfNotBlank(params, request, "sfaastus");
            // 以下为可选精确匹配条件；空串/空白则不拼接
            putIfNotBlank(params, request, "sfaa022");
            putIfNotBlank(params, request, "sfaa023");
            putIfNotBlank(params, request, "sfaadocno");

            List<Map<String, Object>> rows = sfaaMapper.listSchedulableByItem(params);
            if (rows != null) {
                for (Map<String, Object> row : rows) {
                    JSONObject item = new JSONObject();
                    item.set("sfaadocno", getValueIgnoreCase(row, "sfaadocno"));
                    item.set("sfaa010", getValueIgnoreCase(row, "sfaa010"));
                    item.set("sfaa012", getValueIgnoreCase(row, "sfaa012"));
                    item.set("kpsl", getValueIgnoreCase(row, "kpsl"));
                    item.set("sfaa050", getValueIgnoreCase(row, "sfaa050"));
                    item.set("sfaa068", getValueIgnoreCase(row, "sfaa068"));
                    item.set("sfaastus", getValueIgnoreCase(row, "sfaastus"));
                    item.set("sfaa019", formatDate(getValueIgnoreCase(row, "sfaa019")));
                    item.set("sfaa020", formatDate(getValueIgnoreCase(row, "sfaa020")));
                    master.add(item);
                }
            }

            result.set("success", true);
            result.set("master", master);
            result.set("total", master.size());
        } catch (Exception e) {
            result.set("success", false);
            result.set("message", e.getMessage());
            result.set("cause", e.getCause() != null ? e.getCause().getMessage() : "");
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 按订单行查询已排工单数量：用于判断订单行是否已排完
     * 前端 POST /queryOrderScheduledQty
     * 请求体 JSON 示例：
     * {
     *     "token": "xxx",
     *     "sfahucent": "60",      // 可选，默认 60
     *     "sfahucsite": "NBYL",   // 可选，默认 NBYL
     *     "sfahuc004": "来源单号", // 来源单号，必填
     *     "sfahuc005": "来源序号", // 来源序号，必填
     *     "sfahuc002": "品号"       // 可选，有值才作为过滤条件
     * }
     * 返回：success、sfahuc004、sfahuc005、ypgds(已排工单数)；未排过则 ypgds=0
     */
    @PostMapping("/queryOrderScheduledQty")
    public JSONObject queryOrderScheduledQty(@RequestBody Map<String, Object> request) {
        JSONObject result = new JSONObject();
        try {
            String sfahucent = getString(request, "sfahucent");
            if (isBlank(sfahucent)) sfahucent = "60";
            String sfahucsite = getString(request, "sfahucsite");
            if (isBlank(sfahucsite)) sfahucsite = "NBYL";
            String sfahuc004 = getString(request, "sfahuc004");
            String sfahuc005 = getString(request, "sfahuc005");
            String sfahuc002 = getString(request, "sfahuc002");

            if (isBlank(sfahuc004) || isBlank(sfahuc005)) {
                result.set("success", false);
                result.set("message", "sfahuc004(来源单号)与 sfahuc005(来源序号)不能为空");
                return result;
            }

            Map<String, Object> params = new HashMap<>();
            params.put("sfahucent", sfahucent);
            params.put("sfahucsite", sfahucsite);
            params.put("sfahuc004", sfahuc004);
            params.put("sfahuc005", sfahuc005);
            if (!isBlank(sfahuc002)) {
                params.put("sfahuc002", sfahuc002);
            }

            List<Map<String, Object>> rows = sfaaMapper.queryOrderScheduledQty(params);
            long ypgds = 0L;
            if (rows != null && !rows.isEmpty()) {
                Object v = getValueIgnoreCase(rows.get(0), "ypgds");
                if (v != null && !String.valueOf(v).trim().isEmpty()) {
                    ypgds = Math.round(Double.parseDouble(String.valueOf(v)));
                }
            }

            result.set("success", true);
            result.set("sfahuc004", sfahuc004);
            result.set("sfahuc005", sfahuc005);
            result.set("ypgds", ypgds);
        } catch (Exception e) {
            result.set("success", false);
            result.set("message", e.getMessage());
            result.set("cause", e.getCause() != null ? e.getCause().getMessage() : "");
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 查询物料品名/规格：品号精确 + 品名/规格 模糊匹配
     * 前端 POST /searchItemDesc
     * 请求体 JSON 示例：
     * {
     *     "token": "xxx",
     *     "imaalent": "60",          // 可选，默认 60
     *     "imaal002": "zh_CN",       // 可选，默认 zh_CN
     *     "imaal001": "品号",         // 可选，精确匹配
     *     "imaal003": "品名关键字",   // 可选，like 模糊
     *     "imaal004": "规格关键字"    // 可选，like 模糊
     * }
     * 三个查询条件(imaal001/imaal003/imaal004)都为空时报错，避免全表扫描；最多返回 20 条
     * 返回：list[{imaal001, imaal003, imaal004}]、total
     */
    @PostMapping("/searchItemDesc")
    public JSONObject searchItemDesc(@RequestBody Map<String, Object> request) {
        JSONObject result = new JSONObject();
        try {
            String imaalent = getString(request, "imaalent");
            if (isBlank(imaalent)) imaalent = "60";
            String imaal002 = getString(request, "imaal002");
            if (isBlank(imaal002)) imaal002 = "zh_CN";
            String imaal001 = getString(request, "imaal001");
            String imaal003 = getString(request, "imaal003");
            String imaal004 = getString(request, "imaal004");

            // 至少提供一个查询条件，避免全表扫描
            if (isBlank(imaal001) && isBlank(imaal003) && isBlank(imaal004)) {
                result.set("success", false);
                result.set("message", "请至少提供一项查询条件：imaal001(品号)/imaal003(品名)/imaal004(规格)");
                return result;
            }

            Map<String, Object> params = new HashMap<>();
            params.put("imaalent", imaalent);
            params.put("imaal002", imaal002);
            if (!isBlank(imaal001)) params.put("imaal001", imaal001);
            if (!isBlank(imaal003)) params.put("imaal003", imaal003);
            if (!isBlank(imaal004)) params.put("imaal004", imaal004);

            List<Map<String, Object>> rows = dsdataMapper.searchItemDesc(params);
            JSONArray list = new JSONArray();
            if (rows != null) {
                for (Map<String, Object> row : rows) {
                    JSONObject item = new JSONObject();
                    item.set("imaal001", getValueIgnoreCase(row, "imaal001"));
                    item.set("imaal003", getValueIgnoreCase(row, "imaal003"));
                    item.set("imaal004", getValueIgnoreCase(row, "imaal004"));
                    list.add(item);
                }
            }

            result.set("success", true);
            result.set("list", list);
            result.set("total", list.size());
        } catch (Exception e) {
            result.set("success", false);
            result.set("message", e.getMessage());
            result.set("cause", e.getCause() != null ? e.getCause().getMessage() : "");
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 按用户账号查询权限（gzypuc_t）
     * 前端 POST /queryUserPermission
     * 请求体 JSON 示例：
     * {
     *     "token": "e6338a4acxw502kmf5dwr316ss8u0ymb",
     *     "gzypucent": "60",        // 可选，默认 60
     *     "gzypucld": "NBYL",       // 可选，默认 NBYL
     *     "gzypuc001": "用户账号"    // 必填
     * }
     * 返回：{ success, list: [{ gzypuc002(功能菜单编号), gzypuc003(权限), gzypuc004(功能), gzypuc005(权限部门) }], total }
     */
    @PostMapping("/queryUserPermission")
    public JSONObject queryUserPermission(@RequestBody Map<String, Object> request) {
        JSONObject result = new JSONObject();
        try {
            String gzypucent = getString(request, "gzypucent");
            if (isBlank(gzypucent)) gzypucent = "60";
            String gzypucld = getString(request, "gzypucld");
            if (isBlank(gzypucld)) gzypucld = "NBYL";
            String gzypuc001 = getString(request, "gzypuc001");

            if (isBlank(gzypuc001)) {
                result.set("success", false);
                result.set("message", "gzypuc001(用户账号)不能为空");
                return result;
            }

            Map<String, Object> params = new HashMap<>();
            params.put("gzypucent", gzypucent);
            params.put("gzypucld", gzypucld);
            params.put("gzypuc001", gzypuc001);

            List<Map<String, Object>> rows = dsdataMapper.queryUserPermission(params);
            JSONArray list = new JSONArray();
            if (rows != null) {
                for (Map<String, Object> row : rows) {
                    JSONObject item = new JSONObject();
                    // null 转为空串，确保 gzypuc005 等字段即使库中为 NULL 也始终出现在返回 JSON 中
                    item.set("gzypuc002", nvl(getValueIgnoreCase(row, "gzypuc002")));
                    item.set("gzypuc003", nvl(getValueIgnoreCase(row, "gzypuc003")));
                    item.set("gzypuc004", nvl(getValueIgnoreCase(row, "gzypuc004")));
                    item.set("gzypuc005", nvl(getValueIgnoreCase(row, "gzypuc005")));
                    list.add(item);
                }
            }

            result.set("success", true);
            result.set("list", list);
            result.set("total", list.size());
        } catch (Exception e) {
            result.set("success", false);
            result.set("message", e.getMessage());
            result.set("cause", e.getCause() != null ? e.getCause().getMessage() : "");
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 查询未完成订单：订单量 > 已出货量（即尚未出完货）
     * 前端 POST /queryUnfinishedOrders
     * 请求体 JSON 示例：
     * {
     *     "token": "xxx",
     *     "xmddent": "60",                 // 可选，默认 60
     *     "xmddsite": "NBYL",              // 可选，默认 NBYL
     *     "xmdd011_start": "2026-01-01",   // 可选，订单交期 起始(YYYY-MM-DD)
     *     "xmdd011_end": "2026-12-31",     // 可选，订单交期 结束
     *     "xmdadocdt_start": "2026-01-01", // 可选，开单日期 起始
     *     "xmdadocdt_end": "2026-12-31",   // 可选，开单日期 结束
     *     "xmdddocno": "订单号",            // 可选，精确匹配
     *     "xmddseq": "订单序号",            // 可选，精确匹配
     *     "xmdd001": "品号",                // 可选，精确匹配
     *     "imaal003": "品名",               // 可选，模糊匹配(按品名 imaal003)
     *     "ooag011": "业务员",              // 可选，模糊匹配
     *     "imaf013": "补货策略"             // 可选，精确匹配
     * }
     * 固定过滤：(xmdd006 - xmdd014) > 0 且 xmdastus='Y' 且 xmdc045='1'
     * 硬上限：最多返回最近的 300 条
     * 返回：{ success, list: [{ xmdddocno(订单号), xmddseq(订单序号), xmdd001(品号),
     *          xmdd006(订单量), xmdd014(已出货量), undqty(未交量), xmdd011(订单交期),
     *          pmaal004(customerName 客户名称), imaal003(品名), ooag011(业务员), salesempno(业务员工号),
     *          gzcbl004(补货策略) }], total }
     */
    @PostMapping("/queryUnfinishedOrders")
    public JSONObject queryUnfinishedOrders(@RequestBody Map<String, Object> request) {
        JSONObject result = new JSONObject();
        try {
            String xmddent = getString(request, "xmddent");
            if (isBlank(xmddent)) xmddent = "60";
            String xmddsite = getString(request, "xmddsite");
            if (isBlank(xmddsite)) xmddsite = "NBYL";
            String xmdd011Start = getString(request, "xmdd011_start");
            String xmdd011End = getString(request, "xmdd011_end");
            String xmdadocdtStart = getString(request, "xmdadocdt_start");
            String xmdadocdtEnd = getString(request, "xmdadocdt_end");
            String xmdddocno = getString(request, "xmdddocno");   // 订单号
            String xmddseq = getString(request, "xmddseq");       // 订单序号
            String xmdd001 = getString(request, "xmdd001");       // 品号
            String imaal003Cond = getString(request, "imaal003"); // 品名(like)
            String ooag011 = getString(request, "ooag011");       // 业务员
            String imaf013 = getString(request, "imaf013");       // 补货策略

            Map<String, Object> params = new HashMap<>();
            params.put("xmddent", xmddent);
            params.put("xmddsite", xmddsite);
            if (!isBlank(xmdd011Start)) params.put("xmdd011_start", xmdd011Start);
            if (!isBlank(xmdd011End)) params.put("xmdd011_end", xmdd011End);
            if (!isBlank(xmdadocdtStart)) params.put("xmdadocdt_start", xmdadocdtStart);
            if (!isBlank(xmdadocdtEnd)) params.put("xmdadocdt_end", xmdadocdtEnd);
            if (!isBlank(xmdddocno)) params.put("xmdddocno", xmdddocno);
            if (!isBlank(xmddseq)) params.put("xmddseq", xmddseq);
            if (!isBlank(xmdd001)) params.put("xmdd001", xmdd001);
            if (!isBlank(imaal003Cond)) params.put("imaal003", imaal003Cond);
            if (!isBlank(ooag011)) params.put("ooag011", ooag011);
            if (!isBlank(imaf013)) params.put("imaf013", imaf013);

            List<Map<String, Object>> rows = orderMapper.queryUnfinishedOrders(params);
            JSONArray list = new JSONArray();
            if (rows != null) {
                for (Map<String, Object> row : rows) {
                    JSONObject item = new JSONObject();
                    item.set("xmdddocno", nvl(getValueIgnoreCase(row, "xmdddocno")));
                    item.set("xmddseq", nvl(getValueIgnoreCase(row, "xmddseq")));
                    item.set("xmdd001", nvl(getValueIgnoreCase(row, "xmdd001")));
                    item.set("xmdd006", nvl(getValueIgnoreCase(row, "xmdd006")));
                    item.set("xmdd014", nvl(getValueIgnoreCase(row, "xmdd014")));
                    item.set("undqty", nvl(getValueIgnoreCase(row, "undqty")));
                    item.set("xmdd011", nvl(getValueIgnoreCase(row, "xmdd011")));
                    item.set("customerName", nvl(getValueIgnoreCase(row, "pmaal004")));
                    item.set("imaal003", nvl(getValueIgnoreCase(row, "imaal003")));
                    item.set("ooag011", nvl(getValueIgnoreCase(row, "ooag011")));
                    item.set("salesempno", nvl(getValueIgnoreCase(row, "salesempno")));
                    item.set("gzcbl004", nvl(getValueIgnoreCase(row, "gzcbl004")));
                    list.add(item);
                }
            }

            result.set("success", true);
            result.set("list", list);
            result.set("total", list.size());
        } catch (Exception e) {
            result.set("success", false);
            result.set("message", e.getMessage());
            result.set("cause", e.getCause() != null ? e.getCause().getMessage() : "");
            e.printStackTrace();
        }
        return result;
    }

    /** 将日期对象格式化为 yyyy-MM-dd；null 或非日期返回空串 */
    private String formatDate(Object val) {
        if (val == null) {
            return "";
        }
        if (val instanceof java.util.Date) {
            return new SimpleDateFormat("yyyy-MM-dd").format((java.util.Date) val);
        }
        return String.valueOf(val);
    }

}
