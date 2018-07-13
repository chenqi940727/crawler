package com.yunzhitx.mediacrawler.core.media.infra;



import com.yunzhitx.mediacrawler.core.media.domain.ChannelProgram;
import com.yunzhitx.mediacrawler.core.media.domain.ChannelProgramVO;
import com.yunzhitx.mediacrawler.utils.MyBatisBaseMapper;

import java.util.List;
import java.util.Map;

/**
 * 代码生成器自动生成
 * Date:2017-11-14 11:40:46
 *
 * @author
 */
public interface ChannelProgramRepository extends MyBatisBaseMapper<ChannelProgram> {

    /**
     * 根据频道id获取该频道所有的节目单
     *
     * @param params 参数集合
     * @return
     */
    List<ChannelProgramVO> getChannelProgram(Map<String, Object> params);

    Integer checkExist(Map<String, Object> params);
}