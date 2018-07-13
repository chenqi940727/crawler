package com.yunzhitx.mediacrawler.core.mediacategory.domain;

import com.yunzhitx.cloud.common.model.ddd.InstanceFactory;
import com.yunzhitx.mediacrawler.core.mediacategory.infra.MediaCategoryRepository;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Table;

/**
 * 代码生成器自动生成
 * Date:2017-9-4 17:07:46
 *
 * @author
 */
@Data
@Table(name = "me_media_category")
public class MediaCategory {

    @Column(name = "fd_id")
    private Integer id; // 主键id
    @Column(name = "fd_parentId")
    private Integer parentId; // 上级分类
    @Column(name = "fd_fieldName")
    private String fieldName; // 分类字段名称(预留)
    @Column(name = "fd_deep")
    private Integer deep; // 层级深度
    @Column(name = "fd_name")
    private String name; // 分类名称
    @Column(name = "fd_icon")
    private String icon; // 图标
    @Column(name = "fd_isShow")
    private Integer isShow; // 是否上线0否1是
    @Column(name = "fd_serial")
    private Integer serial; // 顺序
    @Column(name = "fd_terminal")
    private String terminal; // 终端
    @Column(name = "fd_remark")
    private String remark; // 备注
    @Column(name = "fd_createDate")
    private String createDate; // 创建时间
    @Column(name = "fd_updateDate")
    private String updateDate; // 更新时间
    @Column(name = "fd_reserved2")
    private String reserved2; // 租户标识

    public void save() throws Exception {
        getMediaCategoryRepository().insert(this);
    }

    public MediaCategory selectCategoryByType(String pkgType) {
        MediaCategory mediaCategory = getMediaCategoryRepository().selectCategoryByType(pkgType);
        return mediaCategory;
    }

    private static MediaCategoryRepository mediaCategoryRepository;

    public static MediaCategoryRepository getMediaCategoryRepository() {
        if (null == mediaCategoryRepository) {
            mediaCategoryRepository = InstanceFactory.getInstance(MediaCategoryRepository.class);
        }
        return mediaCategoryRepository;
    }


}