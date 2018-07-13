package com.yunzhitx.mediacrawler.core.refmediaperformer.domain;

import com.yunzhitx.cloud.common.model.ddd.InstanceFactory;
import com.yunzhitx.mediacrawler.core.refmediaperformer.infra.RefMediaPerformerRepository;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Table;

/**
 * 代码生成器自动生成
 * Date:2017-9-4 17:07:46
 *
 * @author
 */
@Table(name = "me_ref_media_performer")
@Data
public class RefMediaPerformer {
    @Column(name = "fd_mediaId")
    private Integer mediaId; // 演员Id
    @Column(name = "fd_performerId")
    private Integer performerId; // 演员Id
    @Column(name = "fd_type")
    private String type; // starring主演director导演

    public void save() {
        getRefMediaPerformerRepository().insert(this);
    }



    private static RefMediaPerformerRepository refMediaPerformerRepository;
    public static RefMediaPerformerRepository getRefMediaPerformerRepository() {
        if (null == refMediaPerformerRepository) {
            refMediaPerformerRepository = InstanceFactory.getInstance(RefMediaPerformerRepository.class);
        }
        return refMediaPerformerRepository;
    }


}