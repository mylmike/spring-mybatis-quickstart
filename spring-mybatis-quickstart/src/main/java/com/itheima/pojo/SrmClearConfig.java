package com.itheima.pojo;

/**
 * 自动清除在线用户定时配置（MySQL wms.srm_clear_config 单行配置表）
 */
public class SrmClearConfig {
    private Integer id;
    private Boolean enabled;
    private String clearTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getClearTime() {
        return clearTime;
    }

    public void setClearTime(String clearTime) {
        this.clearTime = clearTime;
    }
}
