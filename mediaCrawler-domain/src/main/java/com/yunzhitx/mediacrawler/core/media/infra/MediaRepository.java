package com.yunzhitx.mediacrawler.core.media.infra;


import com.yunzhitx.mediacrawler.core.media.domain.Media;
import com.yunzhitx.mediacrawler.utils.MyBatisBaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
* 代码生成器自动生成
* Date:2017-9-4 17:07:46
* @author
*/
public interface MediaRepository extends MyBatisBaseMapper<Media> {
    Media selectMediaByName(Map<String, Object> params);

    List<Media> selectMediasByVideoId(Integer pkgId);

    void saveMedia(Media media);

    void updatePkgDate(Map map);

    Media getMediaDetail(Integer id);

    void updatePKGTotalAndSerial(Map map);

    List<Media> selectMediaByMap(@Param("params") Map<String, Object> queryMap);

    Integer selectCountByParentAndFieldName(Map<String, Object> queryMap);

    List<Integer> selectIdsByMistake();

    Integer countByMap(@Param("params") Map<String, Object> queryMap);

    Integer selectMediaIdIfMediaPackageExsit(Map<String, Object> params);

    Integer selectMediaResoureExsit(Map<String, Object> params);

    List<Integer> selectIdsRepeatPosters();

    List<Integer> selectIdsRepeatMedia();

    List<Integer> selectSubMediaIdListByParentId(Integer mediaId);

    Integer selectMaxSerialByPackageMediaId(Map params);
}