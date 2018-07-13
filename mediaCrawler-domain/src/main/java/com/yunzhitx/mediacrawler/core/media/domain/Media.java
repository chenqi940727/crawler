package com.yunzhitx.mediacrawler.core.media.domain;

import com.yunzhitx.cloud.common.model.ddd.InstanceFactory;
import com.yunzhitx.mediacrawler.core.categorypropertyvalue.domain.CategoryPropertyValue;
import com.yunzhitx.mediacrawler.core.media.infra.MediaRepository;
import com.yunzhitx.mediacrawler.core.poster.domain.Poster;
import com.yunzhitx.mediacrawler.core.resourcetype.domain.ResourceType;
import lombok.Data;

import javax.persistence.*;
import java.util.List;
import java.util.Map;

/**
 * 代码生成器自动生成
 * Date:2017-9-4 17:07:46
 *
 * @author
 */
@Data
@Table(name = "me_media")
public class Media {
    @Id
    @GeneratedValue(generator = "JDBC")
    @Column(name = "fd_id")
    private Integer id; // 主键id
    @Column(name = "fd_parentId")
    private Integer parentId; // 媒资包Id
    @Column(name = "fd_videoId")
    private String videoId; // 影片Id
    @Column(name = "fd_category")
    private String category; // 分类名称
    @Column(name = "fd_properties")
    private String properties; // 属性集合
    @Column(name = "fd_playCount")
    private Long playCount; // 播放量
    @Column(name = "fd_pkgType")
    private String pkgType; // 影片类型(电影，电视剧) 区别包类型
    @Column(name = "fd_type")
    private String type; // single单片media子集packages媒资包
    @Column(name = "fd_name")
    private String name; // 媒资名称
    @Column(name = "fd_enName")
    private String enName; // 英文名称
    @Column(name = "fd_aliasName")
    private String aliasName; // 别名
    @Column(name = "fd_pinyin")
    private String pinyin; // 拼音
    @Column(name = "fd_doubanScore")
    private Integer doubanScore; // 豆瓣评分 单位：百分制
    @Column(name = "fd_imdbScore")
    private Integer imdbScore; // IMDB评分 单位：百分制
    @Column(name = "fd_playLength")
    private Integer playLength; // 播放时长(单位：秒)
    @Column(name = "fd_terminal")
    private String terminal; // 终端
    @Column(name = "fd_starring")
    private String starring; // 主演
    @Column(name = "fd_director")
    private String director; // 导演
    @Column(name = "fd_total")
    private Integer total; // 总集数
    @Column(name = "fd_state")
    private String state; // 状态
    @Column(name = "fd_serial")
    private Integer serial; // 顺序
    @Column(name = "fd_upsaleDate")
    private String upsaleDate; // 上映时间
    @Column(name = "fd_searchCount")
    private Integer searchCount; // 搜索量
    @Column(name = "fd_introduce")
    private String introduce; // 简介
    @Column(name = "fd_detail")
    private String detail; // 详细描述
    @Column(name = "fd_remark")
    private String remark; // 备注
    @Column(name = "fd_createDate")
    private String createDate; // 创建时间
    @Column(name = "fd_updateDate")
    private String updateDate; // 更新时间
    @Column(name = "fd_reserved2")
    private String reserved2; // 租户标识


	/*===================普通接口用到(搜索引擎也用到)=========================*/
    /**
     * 地区
     */
    @Transient
    private CategoryPropertyValue area;
    /**
     * 年代
     */
    @Transient
    private CategoryPropertyValue year;
    /**
     * 图片
     */
    @Transient
    private List<Poster> posterVOS;
    /**
     * 来源类型
     */
    @Transient
    private List<ResourceType> resourceTypeVOS;

    /*===================搜索引擎=========================*/

    //修改集数用到
    @Transient
    private Integer num;

    public void save() {
        getMediaRepository().insert(this);
    }

    public void saveMedia(Media media){
        getMediaRepository().saveMedia(media);

    }

    public Media selectMediaByName(Map<String, Object> params) {
        Media media = getMediaRepository().selectMediaByName(params);
        return media;
    }

    public List<Media> selectMediasByVideoId(Integer pkgId) {
        List<Media> mediaList = getMediaRepository().selectMediasByVideoId(pkgId);
        return mediaList;
    }

    private static MediaRepository mediaRepository;

    public static MediaRepository getMediaRepository() {
        if (null == mediaRepository) {
            mediaRepository = InstanceFactory.getInstance(MediaRepository.class);
        }
        return mediaRepository;
    }


    public void updatePkgDate(Map map) {
        getMediaRepository().updatePkgDate(map);
    }

    public Media selectMediaByMap(Map<String, Object> queryMap) {
        List<Media> list = getMediaRepository(). selectMediaByMap(queryMap);
        if(list.size() > 0){
            return list.get(0);
        }else{
            return null;
        }
    }

    public Integer selectCountByParentAndFieldName(Map<String, Object> queryMap) {
        return getMediaRepository().selectCountByParentAndFieldName(queryMap);
    }

    public Integer selectMediaIdIfMediaPackageExsit(Map<String, Object> params) {
        return getMediaRepository().selectMediaIdIfMediaPackageExsit(params);
    }

    public Integer selectMediaResoureExsit(Map<String, Object> params) {
        return getMediaRepository().selectMediaResoureExsit(params);
    }

}