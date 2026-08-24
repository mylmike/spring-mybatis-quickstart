package com.itheima.pojo;

/**
 * sfba_t LEFT JOIN sfaa_t 结果行
 */
public class WorkOrderRow {

    // ---- 单头字段 (sfaa_t) ----
    private String sfbaent;     // 账套 (sfbaent=sfaaent)
    private String sfaasite;    // 据点
    private String sfaadocno;   // 工单号
    private String sfaastus;    // 状态码
    private String sfaa010;     // 生产料号
    private String sfaa012;     // 生产数量
    private String sfaa068;     // 成本中心
    private String sfaadocdt;   // 开单日期
    private String sfaa019;     // 开工日
    private String sfaa020;     // 完工日
    private String sfaa022;     // 来源单号
    private String sfaa023;     // 来源序号
    private String sfaa050;     // 已入库合格数
    private String sfaa047;     // 结案日期

    // ---- 单身字段 (sfba_t) ----
    private String sfbaseq;     // 项次
    private String sfba006;     // 料号
    private String sfba023;     // 标准应发数量
    private String sfba024;     // 调整应发数
    private String sfba013;     // 总应发数
    private String sfba017;     // 报废数量
    private String sfba025;     // 超领数量
    private String sfba009;     // 是否倒扣料
    private String sfba028;     // 是否客供料

    // ====== getter/setter ======

    public String getSfbaent() { return sfbaent; }
    public void setSfbaent(String sfbaent) { this.sfbaent = sfbaent; }

    public String getSfaasite() { return sfaasite; }
    public void setSfaasite(String sfaasite) { this.sfaasite = sfaasite; }

    public String getSfaadocno() { return sfaadocno; }
    public void setSfaadocno(String sfaadocno) { this.sfaadocno = sfaadocno; }

    public String getSfaastus() { return sfaastus; }
    public void setSfaastus(String sfaastus) { this.sfaastus = sfaastus; }

    public String getSfaa010() { return sfaa010; }
    public void setSfaa010(String sfaa010) { this.sfaa010 = sfaa010; }

    public String getSfaa012() { return sfaa012; }
    public void setSfaa012(String sfaa012) { this.sfaa012 = sfaa012; }

    public String getSfaa068() { return sfaa068; }
    public void setSfaa068(String sfaa068) { this.sfaa068 = sfaa068; }

    public String getSfaadocdt() { return sfaadocdt; }
    public void setSfaadocdt(String sfaadocdt) { this.sfaadocdt = sfaadocdt; }

    public String getSfaa019() { return sfaa019; }
    public void setSfaa019(String sfaa019) { this.sfaa019 = sfaa019; }

    public String getSfaa020() { return sfaa020; }
    public void setSfaa020(String sfaa020) { this.sfaa020 = sfaa020; }

    public String getSfaa022() { return sfaa022; }
    public void setSfaa022(String sfaa022) { this.sfaa022 = sfaa022; }

    public String getSfaa023() { return sfaa023; }
    public void setSfaa023(String sfaa023) { this.sfaa023 = sfaa023; }

    public String getSfaa050() { return sfaa050; }
    public void setSfaa050(String sfaa050) { this.sfaa050 = sfaa050; }

    public String getSfaa047() { return sfaa047; }
    public void setSfaa047(String sfaa047) { this.sfaa047 = sfaa047; }

    public String getSfbaseq() { return sfbaseq; }
    public void setSfbaseq(String sfbaseq) { this.sfbaseq = sfbaseq; }

    public String getSfba006() { return sfba006; }
    public void setSfba006(String sfba006) { this.sfba006 = sfba006; }

    public String getSfba023() { return sfba023; }
    public void setSfba023(String sfba023) { this.sfba023 = sfba023; }

    public String getSfba024() { return sfba024; }
    public void setSfba024(String sfba024) { this.sfba024 = sfba024; }

    public String getSfba013() { return sfba013; }
    public void setSfba013(String sfba013) { this.sfba013 = sfba013; }

    public String getSfba017() { return sfba017; }
    public void setSfba017(String sfba017) { this.sfba017 = sfba017; }

    public String getSfba025() { return sfba025; }
    public void setSfba025(String sfba025) { this.sfba025 = sfba025; }

    public String getSfba009() { return sfba009; }
    public void setSfba009(String sfba009) { this.sfba009 = sfba009; }

    public String getSfba028() { return sfba028; }
    public void setSfba028(String sfba028) { this.sfba028 = sfba028; }
}
