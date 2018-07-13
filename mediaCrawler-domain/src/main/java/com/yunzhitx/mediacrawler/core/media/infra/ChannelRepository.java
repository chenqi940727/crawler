package com.yunzhitx.mediacrawler.core.media.infra;


import com.yunzhitx.mediacrawler.core.media.domain.Channel;
import com.yunzhitx.mediacrawler.core.media.domain.ChannelVO;
import com.yunzhitx.mediacrawler.utils.MyBatisBaseMapper;

import java.util.List;

/**
 * 代码生成器自动生成
 * Date:2017-11-14 11:40:46
 *
 * @author
 */
public interface ChannelRepository extends MyBatisBaseMapper<Channel> {

    /**
     * 获取频道分页列表
     *
     * @return
     */
    List<ChannelVO> getChannelPage();

    /**
     * 根据频道id获取该频道的详情
     *
     * @param channelId
     * @return
     */
    ChannelVO getChannelDetail(Integer channelId);
}