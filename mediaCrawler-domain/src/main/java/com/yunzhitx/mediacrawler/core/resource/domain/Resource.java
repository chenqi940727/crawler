package com.yunzhitx.mediacrawler.core.resource.domain;

import com.yunzhitx.cloud.common.model.ddd.InstanceFactory;
import com.yunzhitx.mediacrawler.core.resource.infra.ResourceRepository;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * 代码生成器自动生成
 * Date:2017-9-4 17:07:46
 *
 * @author
 */
@Data
@Table(name = "media_resource")
public class Resource {
    @Column(name = "fd_id")
    private Integer id; // 主键id
    @Column(name = "fd_mediaId")
    private Integer mediaId; // 媒资Id
    @Column(name = "fd_resourceTypeId")
    private Integer resourceTypeId; // 资源类型Id
    @Column(name = "fd_thirdId")
    private String thirdId; // 第三方播放Id
    @Column(name = "fd_fieldName")
    private String fieldName; // 类型字段名称
    @Column(name = "fd_pinyin")
    private String pinyin; // 拼音
    @Column(name = "fd_type")
    private String type; // main正片tidbits花絮
    @Column(name = "fd_playType")
    private String playType; // 本地local第三方third
    @Column(name = "fd_name")
    private String name; // 资源名称
    @Column(name = "fd_videoNo")
    private String videoNo; // 视频编号
    @Column(name = "fd_resourceNo")
    private String resourceNo; // 媒资编号
    @Column(name = "fd_assetNo")
    private String assetNo; // 资产编号
    @Column(name = "fd_playUrl")
    private String playUrl; // 播放地址
    @Column(name = "fd_state")
    private String state; // 状态
    @Column(name = "fd_serial")
    private Integer serial; // 顺序
    @Column(name = "fd_keyword")
    private String keyword; // 关键字
    @Column(name = "fd_language")
    private String language; // 语言
    @Column(name = "fd_terminal")
    private String terminal; // 展示终端
    @Column(name = "fd_captionsLanguage")
    private String captionsLanguage; // 字幕语言
    @Column(name = "fd_playCount")
    private Integer playCount; // 点击量
    @Column(name = "fd_remark")
    private String remark; // 备注
    @Column(name = "fd_createDate")
    private String createDate; // 创建时间
    @Column(name = "fd_updateDate")
    private String updateDate; // 更新时间
    @Column(name = "fd_reserved2")
    private String reserved2; // 租户标识

    //修改集数用到
    @Transient
    private Integer num;

    public void save(){
        getResourceRepository().insert(this);
    }

    public Resource selectResourceByVideoId(Integer videoId) {
        Resource resource = getResourceRepository().selectResourceByVideoId(videoId);
        return resource;
    }

    private static ResourceRepository resourceRepository;
    public static ResourceRepository getResourceRepository() {
        if (null == resourceRepository) {
            resourceRepository = InstanceFactory.getInstance(ResourceRepository.class);
        }
        return resourceRepository;
    }


}