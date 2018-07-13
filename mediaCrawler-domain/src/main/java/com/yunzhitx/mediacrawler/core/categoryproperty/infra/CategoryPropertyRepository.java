package com.yunzhitx.mediacrawler.core.categoryproperty.infra;


import com.yunzhitx.mediacrawler.core.categoryproperty.domain.CategoryProperty;
import com.yunzhitx.mediacrawler.utils.MyBatisBaseMapper;

import java.util.List;

public interface CategoryPropertyRepository extends MyBatisBaseMapper<CategoryProperty> {
    //查询年代的属性和属性值相关信息
    CategoryProperty selectYearByCategoryId(Integer id);
    //查询地区的属性和属性值相关信息
    CategoryProperty selectAreaByCategoryId(Integer id);
    //查询类型的属性和属性值相关信息
    CategoryProperty selectTypeByCategoryId(Integer id);

    List<CategoryProperty> selectByCategoryId(Integer id);
}