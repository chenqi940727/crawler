package com.yunzhitx.mediacrawler.web.rest.enumer;

/**
 * @author admin$
 * @Description: TODO
 * @date 2018/5/30$ 15:49$
 */
public enum MediaPackageType {

    TYPE_PACKAGES("媒资包","packages"),

    TYPE_SINGLE("单片","single"),

    TYPE_MEDIA("子集","media");

    private String name;

    private String index;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private MediaPackageType(String name, String index) {
        this.name = name;
        this.index = index;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

}
