package com.yunzhitx.mediacrawler.web.rest.enumer;

/**
 * @author admin$
 * @Description: TODO
 * @date 2018/5/30$ 15:49$
 */
public enum SourceType {

    TYPE_IQIYI("爱奇艺",1),

    TYPE_TENXUN("腾讯视频",2),

    TYPE_YOUKU("优酷",3),

    TYPE_CCTV("CCTV",8),

    TYPE_QIUTAN("球探",9),

    TYPE_BALLKING("懂球帝",10);

    private String name;

    private Integer index;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private SourceType(String name, Integer index) {
        this.name = name;
        this.index = index;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

}
