package com.itheima.pojo;

/**
 * sfaguc_t 订单需求/供给汇总表（dsdata/second）
 * 主键：sfagucent + sfagucsite + sfaguc002 + sfaguc005
 *   sfagucent 企业代码(NUMBER, 默认60)
 *   sfagucsite 营运据点(VARCHAR2, 默认NBYL)
 *   sfaguc002 订单号(VARCHAR2, 单据号码)
 *   sfaguc005 序号(NUMBER(10,0)) —— 订单序号
 *   sfaguc003 需求总数(NUMBER(20,6)) —— 该订单+序号 BOM 展开后所有「订单需求用量」合计
 *   sfaguc004 供给总数(NUMBER(20,6)) —— 该订单+序号 sfahuc_t 排程数量 SUM(sfahuc016) 合计
 */
public class sfaguc {
    private String sfagucent;
    private String sfagucsite;
    private String sfaguc002;   // 订单号
    private String sfaguc003;   // 需求总数
    private String sfaguc004;   // 供给总数（排程总数）
    private String sfaguc005;   // 序号（订单序号）

    public String getSfagucent() { return sfagucent; }
    public void setSfagucent(String sfagucent) { this.sfagucent = sfagucent; }

    public String getSfagucsite() { return sfagucsite; }
    public void setSfagucsite(String sfagucsite) { this.sfagucsite = sfagucsite; }

    public String getSfaguc002() { return sfaguc002; }
    public void setSfaguc002(String sfaguc002) { this.sfaguc002 = sfaguc002; }

    public String getSfaguc003() { return sfaguc003; }
    public void setSfaguc003(String sfaguc003) { this.sfaguc003 = sfaguc003; }

    public String getSfaguc004() { return sfaguc004; }
    public void setSfaguc004(String sfaguc004) { this.sfaguc004 = sfaguc004; }

    public String getSfaguc005() { return sfaguc005; }
    public void setSfaguc005(String sfaguc005) { this.sfaguc005 = sfaguc005; }
}
