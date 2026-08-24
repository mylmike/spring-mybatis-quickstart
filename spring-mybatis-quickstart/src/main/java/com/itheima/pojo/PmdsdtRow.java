package com.itheima.pojo;

/**
 * Oracle pmds_t（收货单头）+ pmdt_t（收货单身）JOIN 结果
 */
public class PmdsdtRow {

    // pmds_t 单头字段
    private String pmdsent;
    private String pmdsdocno;
    private String pmds000;
    private String pmds010;

    // pmdt_t 单身字段
    private String pmdtent;
    private String pmdtdocno;
    private String pmdt001;
    private String pmdt002;
    private String pmdt020;
    private String pmdtseq;

    // ---- pmds_t getter/setter ----
    public String getPmdsent() { return pmdsent; }
    public void setPmdsent(String pmdsent) { this.pmdsent = pmdsent; }

    public String getPmdsdocno() { return pmdsdocno; }
    public void setPmdsdocno(String pmdsdocno) { this.pmdsdocno = pmdsdocno; }

    public String getPmds000() { return pmds000; }
    public void setPmds000(String pmds000) { this.pmds000 = pmds000; }

    public String getPmds010() { return pmds010; }
    public void setPmds010(String pmds010) { this.pmds010 = pmds010; }

    // ---- pmdt_t getter/setter ----
    public String getPmdtent() { return pmdtent; }
    public void setPmdtent(String pmdtent) { this.pmdtent = pmdtent; }

    public String getPmdtdocno() { return pmdtdocno; }
    public void setPmdtdocno(String pmdtdocno) { this.pmdtdocno = pmdtdocno; }

    public String getPmdt001() { return pmdt001; }
    public void setPmdt001(String pmdt001) { this.pmdt001 = pmdt001; }

    public String getPmdt002() { return pmdt002; }
    public void setPmdt002(String pmdt002) { this.pmdt002 = pmdt002; }

    public String getPmdt020() { return pmdt020; }
    public void setPmdt020(String pmdt020) { this.pmdt020 = pmdt020; }

    public String getPmdtseq() { return pmdtseq; }
    public void setPmdtseq(String pmdtseq) { this.pmdtseq = pmdtseq; }
}
