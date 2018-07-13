package com.yunzhitx.mediacrawler.core.media.infra;



import com.yunzhitx.mediacrawler.core.media.domain.ChannelRefProgram;
import com.yunzhitx.mediacrawler.utils.MyBatisBaseMapper;

import java.util.Map;

/**
 * 代码生成器自动生成
 * Date:2017-11-14 11:40:46
 *
 * @author
 */
public interface ChannelRefProgramRepository extends MyBatisBaseMapper<ChannelRefProgram> {

    /**
     * 保存节目和频道关联信息
     *
     * @param params
     */
    void save(Map<String, Object> params);
}