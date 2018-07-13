package com.yunzhitx.mediacrawler.core.categorypropertyvalue.domain;


import com.yunzhitx.cloud.common.model.ddd.InstanceFactory;
import com.yunzhitx.mediacrawler.core.categorypropertyvalue.infra.CategoryPropertyValueRepository;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Table;
import java.util.Map;

/**
 * 代码生成器自动生成
 * Date:2017-9-4 17:07:46
 *
 * @author
 */
@Data
@Table(name = "me_category_property_value")
public class CategoryPropertyValue {
    @Column(name = "fd_id")
    private Integer id; // 主键id
    @Column(name = "fd_propertyId")
    private Integer propertyId; // 属性Id
    @Column(name = "fd_name")
    private String name; // 属性值名称
    @Column(name = "fd_fieldName")
    private String fieldName; // 分类字段名称
    @Column(name = "fd_icon")
    private String icon; // 图标
    @Column(name = "fd_isShow")
    private Integer isShow; // 是否上线0否1是
    @Column(name = "fd_terminal")
    private String terminal; // 终端
    @Column(name = "fd_serial")
    private Integer serial; // 顺序
    @Column(name = "fd_remark")
    private String remark; // 备注
    @Column(name = "fd_createDate")
    private String createDate; // 创建时间
    @Column(name = "fd_updateDate")
    private String updateDate; // 更新时间
    @Column(name = "fd_reserved1")
    private String reserved1; // 租户标识
    @Column(name = "fd_reserved2")
    private String reserved2; // 租户标识

    public void save() throws Exception {
        getCategoryPropertyValueRepository().insert(this);
    }

public CategoryPropertyValue selectPropertyValueById(Map<String,Object> params){
    CategoryPropertyValue categoryPropertyValue = getCategoryPropertyValueRepository().selectPropertyValueById(params);
    return categoryPropertyValue;
}


    private static CategoryPropertyValueRepository categoryPropertyValueRepository;
    public static CategoryPropertyValueRepository getCategoryPropertyValueRepository() {
        if (null == categoryPropertyValueRepository) {
            categoryPropertyValueRepository = InstanceFactory.getInstance(CategoryPropertyValueRepository.class);
        }
        return categoryPropertyValueRepository;
    }


    public void saveCPV(CategoryPropertyValue categoryPropertyValue2) {
        getCategoryPropertyValueRepository().saveCPV(categoryPropertyValue2);
    }
}