package com.yunzhitx.mediacrawler.core.media.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.yunzhitx.cloud.common.model.ddd.AbstractEntity;
import com.yunzhitx.cloud.common.model.ddd.InstanceFactory;
import com.yunzhitx.mediacrawler.core.media.infra.ChannelRefProgramRepository;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Map;

/**
 * 代码生成器自动生成
 * Date:2017-11-14 11:40:46
 *
 * @author
 */
@Data
@Table(name = "ch_channel_ref_program")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChannelRefProgram extends AbstractEntity<ChannelRefProgramRepository> implements Serializable {
    private static final long serialVersionUID = 1L;
    @Column(name = "fd_channelId")
    private Integer channelId; // 频道id
    @Column(name = "fd_programId")
    private Integer programId; // 节目单id

    public ChannelRefProgram() {
    }

    public ChannelRefProgram(Integer id) {
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
     * 保存节目和频道关联信息
     *
     * @param params
     */
    public static void save(Map<String, Object> params) {
        getChannelRefProgramRepository().save(params);
    }

    private static ChannelRefProgramRepository channelRefProgramRepository;

    public static ChannelRefProgramRepository getChannelRefProgramRepository() {
        if (null == channelRefProgramRepository) {
            channelRefProgramRepository = InstanceFactory.getInstance(ChannelRefProgramRepository.class);
        }
        return channelRefProgramRepository;
    }


}