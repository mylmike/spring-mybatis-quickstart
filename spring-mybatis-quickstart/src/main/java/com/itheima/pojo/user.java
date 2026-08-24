package com.itheima.pojo;

public class user {

    private String loginid;
    private String name;

    private String zb;

    public user(String loginid, String name, String zb) {
        this.loginid = loginid;
        this.name = name;
        this.zb = zb;
    }

    public String getZb() {
        return zb;
    }

    public void setZb(String zb) {
        this.zb = zb;
    }

    public user() {
    }

    public String getLoginid() {
        return loginid;
    }

    public void setLoginid(String loginid) {
        this.loginid = loginid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
