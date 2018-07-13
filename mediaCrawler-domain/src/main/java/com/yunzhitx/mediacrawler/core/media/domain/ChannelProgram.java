package com.yunzhitx.mediacrawler.core.media.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yunzhitx.cloud.common.model.PageResult;
import com.yunzhitx.cloud.common.model.ddd.AbstractEntity;
import com.yunzhitx.cloud.common.model.ddd.InstanceFactory;
import com.yunzhitx.mediacrawler.core.media.infra.ChannelProgramRepository;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 代码生成器自动生成
 * Date:2017-11-14 11:40:46
 *
 * @author
 */
@Data
@Table(name = "ch_channel_program")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChannelProgram extends AbstractEntity<ChannelProgramRepository> implements Serializable {
    private static final long serialVersionUID = 1L;
    @Column(name = "fd_channelId")
    private Integer channelId; // 频道ID
    @Column(name = "fd_ssChannelId")
    private String ssChannelId; // 搜视网节目单ID（用于获取图片）
    @Column(name = "fd_code")
    private String code; // 编码
    @Column(name = "fd_name")
    private String name; // 节目名称
    @Column(name = "fd_beginTime")
    private String beginTime; // 开始时间
    @Column(name = "fd_endTime")
    private String endTime; // 结束时间
    @Column(name = "fd_isRecommend")
    private Boolean isRecommend; // 是否推荐
    @Column(name = "fd_keyWord")
    private String keyWord; // 搜索关键词，使用逗号进行分割
    @Column(name = "fd_playCount")
    private Integer playCount; // 播放次数
    @Column(name = "fd_resolution")
    private String resolution; // 频道清晰度，流畅：fluency，标清：standard，高清：hight，超清：super
    @Column(name = "fd_volumeName")
    private String volumeName; //

    public ChannelProgram() {
    }

    public ChannelProgram(Integer id) {
        setId(id);
    }

    @Override
    public Serializable getKey() {
        return getId();
    }

    @Override
    public boolean existed() {
        if (this.getKey() == null) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * 添加频道信息
     */
    public void save() {
        if (existed()) {
            getChannelProgramRepository().insert(this);
        }
    }

    /**
     * 根据频道id获取该频道所有的节目单
     *
     * @param params 参数集合
     * @param page   分页数据
     * @return
     */
    public static PageResult<ChannelProgramVO> getChannelProgram(PageResult page, Map<String, Object> params) {
        PageHelper.startPage(page.getPageNum(), page.getPageSize());
        PageInfo<ChannelProgramVO> pageInfo = new PageInfo<>(getChannelProgramRepository().getChannelProgram(params));
        page.setTotal(pageInfo.getTotal());
        page.setPageData(pageInfo.getList());
        return page;
    }

    private static ChannelProgramRepository channelProgramRepository;

    public static ChannelProgramRepository getChannelProgramRepository() {
        if (null == channelProgramRepository) {
            channelProgramRepository = InstanceFactory.getInstance(ChannelProgramRepository.class);
        }
        return channelProgramRepository;
    }

    /**
     * 删除节目单
     *
     * @param ids
     */
    public static void delete(Integer[] ids) {
        for (Integer id : ids) {
            getChannelProgramRepository().deleteByPrimaryKey(id);
        }
    }

    public Integer checkExist(Integer channelId, String smChannelId, String startTime, String endTime) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("channelId", channelId);
        params.put("ssChannelId", smChannelId);
        params.put("beginTime", startTime);
        params.put("endTime", endTime);
        return getChannelProgramRepository().checkExist(params);
    }
}