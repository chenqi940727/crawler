package com.yunzhitx.mediacrawler.web.rest.enumer;

/**
 * @author admin$
 * @Description: TODO
 * @date 2018/5/30$ 15:49$
 */
public enum MediaType {

    TYPE_FILM("电影",1),

    TYPE_TV("电视剧",2),

    TYPE_ANIMATION("动漫",3),

    TYPE_VARIETY("综艺",4);

    private String name;

    private Integer index;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private MediaType(String name, Integer index) {
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
