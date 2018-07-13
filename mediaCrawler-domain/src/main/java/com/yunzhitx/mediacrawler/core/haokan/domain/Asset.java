package com.yunzhitx.mediacrawler.core.haokan.domain;

import com.yunzhitx.cloud.common.model.ddd.InstanceFactory;
import com.yunzhitx.mediacrawler.core.haokan.infra.AssetRepository;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.List;

/**
 * @author
 * @Description: 媒资
 */
@Data
@Table(name = "asset")
public class Asset {
    @Column(name = "resourceCode")
    private String resourceCode;    //媒资编号  (主键ID)
    @Column(name = "assetID")
    private String assetID;            //资产ID(没有就算了)
    @Column(name = "assetName")
    private String assetName;        //媒资名称
    @Column(name = "assetENName")
    private String assetENName;        //媒资英文名
    @Column(name = "assetTypeIds")
    private String assetTypeIds;    //媒资分类ID,逗号分隔
    @Column(name = "assetTypes")
    private String assetTypes;        //媒资分类
    @Column(name = "captionName")
    private String captionName;        //语言
    @Column(name = "chapters")
    private int chapters;        //剧集集数（媒资则表示当前集数  媒资包则表示总集数）
    @Column(name = "createTime")
    private String createTime;        //创建时间
    @Column(name = "director")
    private String director;        //导演
    @Column(name = "issuerName")
    private String issuerName;        //展示终端（pc、app、pc+app）
    @Column(name = "keyWord")
    private String keyWord;            //关键字
    @Column(name = "leadingActor")
    private String leadingActor;    //主演
    @Column(name = "mlName")
    private String mlName;            //字幕语言
    @Column(name = "originName")
    private String originName;        //地区
    @Column(name = "playCount")
    private long playCount;        //点击量
    @Column(name = "publishDate")
    private String publishDate;    //发布时间
    @Column(name = "remark")
    private String remark;            //备注
    @Column(name = "score")
    private String score;            //评分
    @Column(name = "screenWriter")
    private String screenWriter;    //编剧1ssetpo.setType(1);0
    @Column(name = "searchCount")
    private long searchCount;    //搜索量
    @Column(name = "series")
    private String series;            //系列（null单媒资，1电视剧，2系列剧）
    @Column(name = "status")
    private int status = 1;  //0下架  1上架
    @Column(name = "summaryLong")
    private String summaryLong;        //长简介
    @Column(name = "summaryMedium")
    private String summaryMedium;    //中长简介
    @Column(name = "summaryShort")
    private String summaryShort;    //短简介
    @Column(name = "type")
    private int type;            //类型（0媒资 1媒资包）
    @Column(name = "year")
    private String year;            //年份
    @Column(name = "playTime")
    private String playTime = "0"; //时长
    @Column(name = "upnumber")
    private int upnumber = 1;
    @Column(name = "paktype")
    private int paktype = 0;
    @Column(name = "updateChapters")
    private int updateChapters;//更新至
    @Column(name = "providerID")
    private String providerID;

    @Transient
    private List<AssetPoster> assetPosterList;

    private static AssetRepository assetRepository;

    public static AssetRepository getAssetRepository() {
        if (null == assetRepository) {
            assetRepository = InstanceFactory.getInstance(AssetRepository.class);
        }
        return assetRepository;
    }


}
