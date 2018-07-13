package com.yunzhitx.mediacrawler.core.resource.infra;

import com.yunzhitx.mediacrawler.core.resource.domain.Resource;
import com.yunzhitx.mediacrawler.utils.MyBatisBaseMapper;

/**
 * 代码生成器自动生成
 * Date:2017-9-4 17:07:46
 *
 * @author
 */
public interface ResourceRepository extends MyBatisBaseMapper<Resource> {
    Resource selectResourceByVideoId(Integer videoId);

    Resource getMaxSerial(Integer id);


    void deleteByMediaId(Integer mediaId);
}