package com.yunzhitx.mediacrawler.core.media.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yunzhitx.cloud.common.model.PageResult;
import com.yunzhitx.cloud.common.model.ddd.AbstractEntity;
import com.yunzhitx.cloud.common.model.ddd.InstanceFactory;
import com.yunzhitx.mediacrawler.core.media.infra.ChannelRepository;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * 代码生成器自动生成
 * Date:2017-11-14 11:40:46
 *
 * @author
 */
@Data
@Table(name = "ch_channel")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Channel extends AbstractEntity<ChannelRepository> implements Serializable {
    private static final long serialVersionUID = 1L;
    @Column(name = "fd_name")
    private String name; // 频道名称
    @Column(name = "fd_playCode")
    private String playCode; // 频道播放串
    @Column(name = "fd_playCount")
    private Integer playCount; // 播放次数
    @Column(name = "fd_ssChannelId")
    private String ssChannelId; // 搜视网频道ID
    @Column(name = "fd_resolution")
    private String resolution; // 频道清晰度，流畅：fluency，标清：standard，高清：hight，超清：super
    @Column(name = "fd_logoUrl")
    private String logoUrl; // logo图片
    @Column(name = "fd_liveRealImg")
    private String liveRealImg; // 频道实时截图
    @Column(name = "fd_del")
    private Boolean del; // 是否删除
    @Column(name = "fd_display")
    private Boolean display; // 是否显示
    @Column(name = "fd_remark")
    private String remark; // 备注

    public Channel() {
    }

    public Channel(Integer id) {
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
            getChannelRepository().insert(this);
        }
    }

    /**
     * 修改信息
     */
    public void update() {
        if (!existed()) {
            getChannelRepository().updateByPrimaryKeySelective(this);
        }
    }

    /**
     * 删除信息
     *
     * @param ids
     */
    public static void delete(Integer[] ids) {
        for (Integer id : ids) {
            getChannelRepository().deleteByPrimaryKey(id);
        }
    }

    /**
     * 获取频道分页列表
     *
     * @param page
     * @return
     */
    public static PageResult getChannelPage(PageResult page) {
        PageHelper.startPage(page.getPageNum(), page.getPageSize());
        PageInfo<ChannelVO> pageInfo = new PageInfo<>(getChannelRepository().getChannelPage());
        page.setTotal(pageInfo.getTotal());
        page.setPageData(pageInfo.getList());
        return page;
    }

    /**
     * 根据频道id获取该频道的详情
     *
     * @param channelId
     * @return
     */
    public static ChannelVO getChannelDetail(Integer channelId) {
        return getChannelRepository().getChannelDetail(channelId);
    }

    private static ChannelRepository channelRepository;

    public static ChannelRepository getChannelRepository() {
        if (null == channelRepository) {
            channelRepository = InstanceFactory.getInstance(ChannelRepository.class);
        }
        return channelRepository;
    }
}