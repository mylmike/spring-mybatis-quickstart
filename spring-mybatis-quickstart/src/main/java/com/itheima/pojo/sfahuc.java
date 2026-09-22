package com.itheima.pojo;

public class sfahuc {
    private String sfahucent;
    private String sfahucsite;
    private String sfahucdocno;
    private String sfahucseq;
    private String sfahuc001;
    private String sfahuc002;
    private String sfahuc003;
    private String sfahuc004;
    private String sfahuc005;
    private String sfahuc006;
    private String sfahuc007;
    private String sfahuc008;
    private String sfahuc009;
    private String sfahuc010;   // 产线（字符型）
    private String sfahuc011;   // 订单需求数量（NUMBER）

    private String sfaastus;    // 工单结案状态（取自 sfaa_t，LEFT JOIN 工单主表）
    private String sfaa050;     // 入库数量（取自 sfaa_t）
    private String yuLiang;     // 余量 = sfaa012 - sfaa050（取自 sfaa_t）
    private String sfahuc012;   // 人数
    private String sfahuc013;   // 工作时长
    private String sfahuc014;   // UPPH值
    private String sfahuc015;   // 日产量 = sfahuc012 * sfahuc013 * sfahuc014

    public String getSfahucent() { return sfahucent; }
    public void setSfahucent(String sfahucent) { this.sfahucent = sfahucent; }

    public String getSfahucsite() { return sfahucsite; }
    public void setSfahucsite(String sfahucsite) { this.sfahucsite = sfahucsite; }

    public String getSfahucdocno() { return sfahucdocno; }
    public void setSfahucdocno(String sfahucdocno) { this.sfahucdocno = sfahucdocno; }

    public String getSfahucseq() { return sfahucseq; }
    public void setSfahucseq(String sfahucseq) { this.sfahucseq = sfahucseq; }

    public String getSfahuc001() { return sfahuc001; }
    public void setSfahuc001(String sfahuc001) { this.sfahuc001 = sfahuc001; }

    public String getSfahuc002() { return sfahuc002; }
    public void setSfahuc002(String sfahuc002) { this.sfahuc002 = sfahuc002; }

    public String getSfahuc003() { return sfahuc003; }
    public void setSfahuc003(String sfahuc003) { this.sfahuc003 = sfahuc003; }

    public String getSfahuc004() { return sfahuc004; }
    public void setSfahuc004(String sfahuc004) { this.sfahuc004 = sfahuc004; }

    public String getSfahuc005() { return sfahuc005; }
    public void setSfahuc005(String sfahuc005) { this.sfahuc005 = sfahuc005; }

    public String getSfahuc006() { return sfahuc006; }
    public void setSfahuc006(String sfahuc006) { this.sfahuc006 = sfahuc006; }

    public String getSfahuc007() { return sfahuc007; }
    public void setSfahuc007(String sfahuc007) { this.sfahuc007 = sfahuc007; }

    public String getSfahuc008() { return sfahuc008; }
    public void setSfahuc008(String sfahuc008) { this.sfahuc008 = sfahuc008; }

    public String getSfahuc009() { return sfahuc009; }
    public void setSfahuc009(String sfahuc009) { this.sfahuc009 = sfahuc009; }

    public String getSfahuc010() { return sfahuc010; }
    public void setSfahuc010(String sfahuc010) { this.sfahuc010 = sfahuc010; }

    public String getSfahuc011() { return sfahuc011; }
    public void setSfahuc011(String sfahuc011) { this.sfahuc011 = sfahuc011; }

    public String getSfaastus() { return sfaastus; }
    public void setSfaastus(String sfaastus) { this.sfaastus = sfaastus; }

    public String getSfahuc012() { return sfahuc012; }
    public void setSfahuc012(String sfahuc012) { this.sfahuc012 = sfahuc012; }

    public String getSfahuc013() { return sfahuc013; }
    public void setSfahuc013(String sfahuc013) { this.sfahuc013 = sfahuc013; }

    public String getSfahuc014() { return sfahuc014; }
    public void setSfahuc014(String sfahuc014) { this.sfahuc014 = sfahuc014; }

    public String getSfahuc015() { return sfahuc015; }
    public void setSfahuc015(String sfahuc015) { this.sfahuc015 = sfahuc015; }

    public String getSfaa050() { return sfaa050; }
    public void setSfaa050(String sfaa050) { this.sfaa050 = sfaa050; }

    public String getYuLiang() { return yuLiang; }
    public void setYuLiang(String yuLiang) { this.yuLiang = yuLiang; }
}
