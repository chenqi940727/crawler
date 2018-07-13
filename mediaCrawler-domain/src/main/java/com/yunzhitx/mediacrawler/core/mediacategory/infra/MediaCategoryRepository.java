package com.yunzhitx.mediacrawler.core.mediacategory.infra;

import com.yunzhitx.mediacrawler.core.mediacategory.domain.MediaCategory;
import com.yunzhitx.mediacrawler.utils.MyBatisBaseMapper;

/**
* 代码生成器自动生成
* Date:2017-9-4 17:07:46
* @author
*/
public interface MediaCategoryRepository extends MyBatisBaseMapper<MediaCategory>{

    MediaCategory selectCategoryByType(String pkgType);

}