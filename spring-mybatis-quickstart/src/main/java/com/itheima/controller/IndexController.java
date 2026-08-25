package com.itheima.controller;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.itheima.mapper.primary.UserMapper;
import com.itheima.mapper.second.PmdlMapper;
import com.itheima.mapper.second.PmdsMapper;
import com.itheima.mapper.second.SfaaMapper;
import com.itheima.mapper.second.SfahucMapper;
import com.itheima.mapper.second.SfbaMapper;
import com.itheima.mapper.second.DsdataMapper;
import com.itheima.mapper.second.LssdMapper;
import com.itheima.mapper.third.SrmDeliveryBodyMapper;
import com.itheima.mapper.third.SrmDeliveryHeadMapper;
import com.itheima.pojo.PmdsdtRow;
import com.itheima.pojo.SrmDeliveryBody;
import com.itheima.pojo.WorkOrderRow;
import com.itheima.pojo.sfaa;
import com.itheima.pojo.sfahuc;
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
     *     "row_max": 200                           // 不传或为 0 则不限行数
     * }
     * 返回: { "head": [{ 单头字段..., "detail": [{ 单身字段... }] }] }
     */
    @PostMapping("/queryWorkOrder")
    public JSON queryWorkOrder(@RequestBody Map<String, Object> request) {
        // 去掉 token，其余全部作为查询条件传入 SQL
        request.remove("token");
        List<WorkOrderRow> rows = sfbaMapper.queryWorkOrder(request);

        JSONObject result = new JSONObject();
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

        result.set("head", headArr);
        return result;
    }

    /**
     * 根据单号查询 sfahuc_t 明细
     * 前端 POST /querySfahuc
     * 请求体 JSON: { "sfahucdocno": "单号" }
     * 返回: { "master": [{ sfahucent, sfahucsite, sfahucdocno, sfahucseq, ... }] }
     */
    @PostMapping("/querySfahuc")
    public JSON querySfahuc(@RequestBody Map<String, Object> request) {
        String sfahucdocno = (String) request.get("sfahucdocno");
        JSONObject result = new JSONObject();
        JSONArray master = new JSONArray();

        List<sfahuc> sfahucList = sfahucMapper.listByDocno(sfahucdocno);
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
     * 根据订单号查询 sfaa
     * 前端 POST /queryOrder
     * 请求体 JSON: { "orderNo": "订单号" }
     * 返回: { "master": [{ sfaadocno, sfaa010, ... }] }
     */
    @PostMapping("/queryOrder")
    public JSON queryOrder(@RequestBody Map<String, Object> request) {
        String orderNo = (String) request.get("orderNo");
        JSONObject result = new JSONObject();
        JSONArray master = new JSONArray();

        List<sfaa> sfaaList = sfaaMapper.listByOrderNo(orderNo);
        for (sfaa s : sfaaList) {
            JSONObject item = new JSONObject();
            item.set("sfaadocno", s.getSfaadocno());
            item.set("sfaa010", s.getSfaa010());
            item.set("sfaa012", s.getSfaa012());
            item.set("sfaa019", s.getSfaa019());
            item.set("sfaa020", s.getSfaa020());
            item.set("sfaa021", s.getSfaa021());
            item.set("sfaa022", s.getSfaa022());
            item.set("sfaa023", s.getSfaa023());
            item.set("sfaa068", s.getSfaa068());
            item.set("ooefl003", s.getOoefl003());
            master.add(item);
        }


        result.set("master", master);
        return result;
    }

    /**
     * 保存/更新 sfahuc_t 数据
     * 前端 POST /saveSfahuc
     * 请求体 JSON: { "list": [{ sfahucent, sfahucsite, sfahucdocno, sfahucseq, sfahuc001 ... }] }
     * 返回: { "success": true, "insertCount": x, "updateCount": y }
     */
    @PostMapping("/saveSfahuc")
    public JSON saveSfahuc(@RequestBody Map<String, Object> request) {
        List<Map<String, Object>> list = (List<Map<String, Object>>) request.get("list");
        JSONObject result = new JSONObject();
        int insertCount = 0;
        int updateCount = 0;

        if (list != null) {
            for (Map<String, Object> item : list) {
                sfahuc record = new sfahuc();
                record.setSfahucent(getString(item, "sfahucent"));
                record.setSfahucsite(getString(item, "sfahucsite"));
                record.setSfahucdocno(getString(item, "sfahucdocno"));
                record.setSfahucseq(getString(item, "sfahucseq"));
                record.setSfahuc001(getString(item, "sfahuc001"));
                record.setSfahuc002(getString(item, "sfahuc002"));
                record.setSfahuc003(getString(item, "sfahuc003"));
                record.setSfahuc004(getString(item, "sfahuc004"));
                record.setSfahuc005(getString(item, "sfahuc005"));
                record.setSfahuc006(getString(item, "sfahuc006"));
                record.setSfahuc007(getString(item, "sfahuc007"));
                record.setSfahuc008(getString(item, "sfahuc008"));
                record.setSfahuc009(getString(item, "sfahuc009"));

                List<sfahuc> existingList = sfahucMapper.findByDocnoAndSeq(
                        record.getSfahucdocno(), record.getSfahucseq());

                if (existingList == null || existingList.isEmpty()) {
                    sfahucMapper.insert(record);
                    insertCount++;
                } else {
                    sfahucMapper.update(record);
                    updateCount++;
                }
            }
        }

        result.set("success", true);
        result.set("insertCount", insertCount);
        result.set("updateCount", updateCount);
        return result;
    }

    /**
     * 删除 sfahuc_t 记录
     * 前端 POST /deleteSfahuc
     * 请求体 JSON: { "sfahucent", "sfahucsite", "sfahucdocno", "sfahucseq", "sfahuc001", "sfahuc002" }
     * 返回: { "success": true, "deleted": true/false }
     */
    @PostMapping("/deleteSfahuc")
    public JSON deleteSfahuc(@RequestBody Map<String, Object> request) {
        String sfahucent = getString(request, "sfahucent");
        String sfahucsite = getString(request, "sfahucsite");
        String sfahucdocno = getString(request, "sfahucdocno");
        String sfahucseq = getString(request, "sfahucseq");
        String sfahuc001 = getString(request, "sfahuc001");
        String sfahuc002 = getString(request, "sfahuc002");

        int rows = sfahucMapper.deleteByKey(sfahucent, sfahucsite, sfahucdocno, sfahucseq, sfahuc001, sfahuc002);

        JSONObject result = new JSONObject();
        result.set("success", true);
        result.set("deleted", rows > 0);
        JSONObject condition = new JSONObject();
        condition.set("sfahucent", sfahucent);
        condition.set("sfahucsite", sfahucsite);
        condition.set("sfahucdocno", sfahucdocno);
        condition.set("sfahucseq", sfahucseq);
        condition.set("sfahuc001", sfahuc001);
        condition.set("sfahuc002", sfahuc002);
        result.set("condition", condition);
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
     * 返回: { "success": true, "tree": [根节点(含children, level)], "flat": [扁平明细], "total": n }
     * 节点字段: 订单品号, bmba001(主件), bmba009(BOM项序), bmba003(元件),
     *         BOM用量, bmba010(单位), 实际用量, 订单需求用量, level, children
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

            Map<String, Object> params = new HashMap<>();
            params.put("ent", ent);
            params.put("site", site);
            if (!isBlank(docNo)) params.put("xmdddocno", docNo);
            if (!isBlank(itemNo)) params.put("xmdd001", itemNo);
            if (!isBlank(seq)) params.put("xmddseq", seq);

            List<Map<String, Object>> orderItems = dsdataMapper.queryXmddItems(params);
            JSONArray tree = new JSONArray();
            JSONArray flat = new JSONArray();

            for (Map<String, Object> item : orderItems) {
                String rootItem = stringValueIgnoreCase(item, "xmdd001");
                String orderDocNo = stringValueIgnoreCase(item, "xmdddocno");
                String orderSeq = stringValueIgnoreCase(item, "xmddseq");
                BigDecimal orderQty = toBigDecimal(getValueIgnoreCase(item, "xmdd005"));
                if (isBlank(rootItem)) continue;

                // 根节点：订单品号本身，实际用量=1
                JSONObject root = new JSONObject();
                root.set("订单号", orderDocNo);
                root.set("订单序号", orderSeq);
                root.set("订单品号", rootItem);
                root.set("bmba001", rootItem);
                root.set("bmba009", "");
                root.set("bmba003", rootItem);
                root.set("BOM用量", BigDecimal.ONE);
                root.set("bmba010", "");
                root.set("实际用量", BigDecimal.ONE);
                root.set("订单需求用量", orderQty.setScale(2, RoundingMode.HALF_UP));
                root.set("level", 1);
                JSONArray children = new JSONArray();
                root.set("children", children);
                tree.add(root);

                // 根节点同时加入扁平明细
                flat.add(buildFlatNode(orderDocNo, orderSeq, rootItem, rootItem, "", rootItem, BigDecimal.ONE, "", BigDecimal.ONE, orderQty, 1));

                // 从第一层 BOM 开始递归下展
                expandBomTree(ent, site, orderDocNo, orderSeq, rootItem, rootItem, BigDecimal.ONE, orderQty,
                        children, flat, 2, new HashSet<>());
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
     */
    private void expandBomTree(String ent, String site, String docNo, String seq,
                               String rootItem, String parentItem,
                               BigDecimal parentActualQty, BigDecimal orderQty,
                               JSONArray children, JSONArray flat, int level, Set<String> path) {
        if (path.contains(parentItem)) return;
        path.add(parentItem);

        List<Map<String, Object>> childRows = dsdataMapper.queryBomChildren(ent, site, parentItem);
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

            // BOM用量 = bmba011 * (1 + NVL(bmbb011, 0)) / bmba012，保留2位小数
            BigDecimal bomQty = bmba011.multiply(BigDecimal.ONE.add(bmbb011))
                    .divide(bmba012, 10, RoundingMode.HALF_UP)
                    .setScale(2, RoundingMode.HALF_UP);
            // 实际用量 = 父件实际用量 * BOM用量，保留2位小数
            BigDecimal actualQty = parentActualQty.multiply(bomQty)
                    .setScale(2, RoundingMode.HALF_UP);
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
            JSONArray subChildren = new JSONArray();
            node.set("children", subChildren);
            children.add(node);

            // 扁平明细副本（不含 children，便于表格/导出）
            flat.add(buildFlatNode(docNo, seq, rootItem, parentNo, childSeq, childItem,
                    bomQty, childUnit, actualQty, demandQty, level));

            expandBomTree(ent, site, docNo, seq, rootItem, childItem, actualQty, orderQty,
                    subChildren, flat, level + 1, path);
        }

        path.remove(parentItem);
    }

    /** 构建扁平明细节点 */
    private JSONObject buildFlatNode(String docNo, String seq, String rootItem, String parentNo,
                                     String bomSeq, String item, BigDecimal bomQty, String unit,
                                     BigDecimal actualQty, BigDecimal demandQty, int level) {
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
        return row;
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

    private String getString(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val == null ? null : String.valueOf(val);
    }

}
