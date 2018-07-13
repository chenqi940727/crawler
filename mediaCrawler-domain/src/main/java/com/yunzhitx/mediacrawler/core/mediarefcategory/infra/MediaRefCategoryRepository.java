package com.yunzhitx.mediacrawler.core.mediarefcategory.infra;


import com.yunzhitx.mediacrawler.core.mediarefcategory.domain.MediaRefCategory;
import com.yunzhitx.mediacrawler.utils.MyBatisBaseMapper;

import java.util.List;
import java.util.Map;

/**
* 代码生成器自动生成
* Date:2017-9-4 17:07:46
* @author
*/
public interface MediaRefCategoryRepository extends MyBatisBaseMapper<MediaRefCategory> {

    /**=======================搜索引擎=============================*/
    /**
     * 根据媒资id获取所有的
     *
     * @param id
     */
    List<MediaRefCategory> selectAllPropsByMeidaId(Integer id);

    /**
     * 根据媒资包id和属性名称获取对应的属性值
     *
     * @param param 媒资包id和属性名称
     * @return
     */
    List<MediaRefCategory> selectSomePropS(Map<String, Object> param);
    /**=======================搜索引擎=============================*/
}