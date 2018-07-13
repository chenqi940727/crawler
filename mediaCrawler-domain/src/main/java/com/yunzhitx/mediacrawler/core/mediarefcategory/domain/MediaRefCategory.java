package com.yunzhitx.mediacrawler.core.mediarefcategory.domain;

import com.yunzhitx.cloud.common.model.ddd.InstanceFactory;
import com.yunzhitx.mediacrawler.core.mediarefcategory.infra.MediaRefCategoryRepository;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Table;

/**
 * 代码生成器自动生成
 * Date:2017-9-4 17:07:46
 *
 * @author
 */
@Table(name = "me_media_ref_category")
@Data
public class MediaRefCategory {
    @Column(name = "fd_mediaId")
    private Integer mediaId; // 媒资id
    @Column(name = "fd_categoryId")
    private Integer categoryId; // 分类id
    @Column(name = "fd_propertyId")
    private Integer propertyId; // 属性id
    @Column(name = "fd_property")
    private String property; // 属性名称
    @Column(name = "fd_propertyValueId")
    private Integer propertyValueId; // 属性值id

    public void save() {
        getMediaRefCategoryRepository().insert(this);
    }

    private static MediaRefCategoryRepository mediaRefCategoryRepository;
    public static MediaRefCategoryRepository getMediaRefCategoryRepository() {
        if (null == mediaRefCategoryRepository) {
            mediaRefCategoryRepository = InstanceFactory.getInstance(MediaRefCategoryRepository.class);
        }
        return mediaRefCategoryRepository;
    }


}