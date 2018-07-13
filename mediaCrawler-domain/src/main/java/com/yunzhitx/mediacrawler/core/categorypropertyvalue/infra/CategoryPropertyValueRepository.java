package com.yunzhitx.mediacrawler.core.categorypropertyvalue.infra;


import com.yunzhitx.mediacrawler.core.categorypropertyvalue.domain.CategoryPropertyValue;
import com.yunzhitx.mediacrawler.utils.MyBatisBaseMapper;

import java.util.List;
import java.util.Map;

/**
* 代码生成器自动生成
* Date:2017-9-4 17:07:46
* @author
*/
public interface CategoryPropertyValueRepository extends MyBatisBaseMapper<CategoryPropertyValue> {

    CategoryPropertyValue selectPropertyValueById(Map<String, Object> params);

    void saveCPV(CategoryPropertyValue categoryPropertyValue2);

    List<CategoryPropertyValue> selectByPkgType(String pkgType);
}