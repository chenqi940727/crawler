package com.yunzhitx.mediacrawler.core.categoryproperty.domain;

import com.yunzhitx.cloud.common.model.ddd.InstanceFactory;
import com.yunzhitx.mediacrawler.core.categoryproperty.infra.CategoryPropertyRepository;
import lombok.Data;

import javax.persistence.*;

/**
 * 代码生成器自动生成
 * Date:2017-9-4 17:07:46
 *
 * @author
 */
@Data
@Table(name = "me_category_property")
public class CategoryProperty {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fd_id")
    private Integer id; // 主键id
    @Column(name = "fd_parentId")
    private Integer parentId; // 上级分类
    @Column(name = "fd_categoryId")
    private Integer categoryId; // 分类Id
    @Column(name = "fd_name")
    private String name; // 分类名称
    @Column(name = "fd_fieldName")
    private String fieldName; // 分类字段名称
    @Column(name = "fd_fieldType")
    private String fieldType; // 分类类型(预留)
    @Column(name = "fd_fieldValueLength")
    private Integer fieldValueLength; // 字段长度(预留)
    @Column(name = "fd_fieldValueRegex")
    private String fieldValueRegex; // 正在匹配规则(预留)
    @Column(name = "fd_isRequried")
    private Integer isRequried; // 字段是否必填(预留)
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
    @Column(name = "fd_reserved1")
    private String reserved1; // 租户标识
    @Column(name = "fd_reserved2")
    private String reserved2; // 租户标识

    public void save() {
        getCategoryPropertyRepository().insert(this);
    }

    //查询年代的属性和属性值相关信息
    public CategoryProperty selectYearByCategoryId(Integer id){
        CategoryProperty categoryProperty = getCategoryPropertyRepository().selectYearByCategoryId(id);
        return categoryProperty ;
    }
    //查询地区的属性和属性值相关信息
    public CategoryProperty selectAreaByCategoryId(Integer id) {
        CategoryProperty categoryProperty = getCategoryPropertyRepository().selectAreaByCategoryId(id);
        return categoryProperty ;
    }
    //查询类型的属性和属性值相关信息
    public CategoryProperty selectTypeByCategoryId(Integer id) {
        CategoryProperty categoryProperty = getCategoryPropertyRepository().selectTypeByCategoryId(id);
        return categoryProperty ;
    }
    private static CategoryPropertyRepository categoryPropertyRepository;
    public static CategoryPropertyRepository getCategoryPropertyRepository() {
        if (null == categoryPropertyRepository) {
            categoryPropertyRepository = InstanceFactory.getInstance(CategoryPropertyRepository.class);
        }
        return categoryPropertyRepository;
    }



}