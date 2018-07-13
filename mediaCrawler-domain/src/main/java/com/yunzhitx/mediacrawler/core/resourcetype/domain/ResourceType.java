package com.yunzhitx.mediacrawler.core.resourcetype.domain;

import com.yunzhitx.cloud.common.model.ddd.InstanceFactory;
import com.yunzhitx.mediacrawler.core.resourcetype.infra.ResourceTypeRepository;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Table;

/**
 * 代码生成器自动生成
 * Date:2017-9-4 17:07:46
 *
 * @author
 */
@Table(name = "media_resource_type")
@Data
public class ResourceType {
    @Column(name = "fd_id")
    private Integer id; // 主键id
    @Column(name = "fd_name")
    private String name; // 类型名称
    @Column(name = "fd_fieldName")
    private String fieldName; // 类型字段名称
    @Column(name = "fd_pinyin")
    private String pinyin; // 拼音
    @Column(name = "fd_serial")
    private Integer serial; // 顺序
    @Column(name = "fd_remark")
    private String remark; // 备注

    public void save() throws Exception {

        getResourceTypeRepository().insert(this);
    }

    private static ResourceTypeRepository resourceTypeRepository;

    public static ResourceTypeRepository getResourceTypeRepository() {
        if (null == resourceTypeRepository) {
            resourceTypeRepository = InstanceFactory.getInstance(ResourceTypeRepository.class);
        }
        return resourceTypeRepository;
    }


}