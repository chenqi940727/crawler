package com.yunzhitx.mediacrawler.core.poster.infra;

import com.yunzhitx.mediacrawler.core.poster.domain.Poster;
import com.yunzhitx.mediacrawler.utils.MyBatisBaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
* 代码生成器自动生成
* Date:2017-9-4 17:07:46
* @author
*/
public interface PosterRepository extends MyBatisBaseMapper<Poster>{
    List selectAllMediaIds();

    void updateLargeByMediaId(Map params);

    List<Poster> selectPostersByMediaId(Integer mediaId);
}