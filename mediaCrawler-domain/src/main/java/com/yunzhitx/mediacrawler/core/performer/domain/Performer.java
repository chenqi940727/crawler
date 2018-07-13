package com.yunzhitx.mediacrawler.core.performer.domain;

import com.yunzhitx.cloud.common.model.ddd.InstanceFactory;
import com.yunzhitx.mediacrawler.core.performer.infra.PerformerRepository;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 代码生成器自动生成
 * Date:2017-9-4 17:07:46
 *
 * @author
 */
@Data
@Table(name = "me_performer")
public class Performer {
    @Id
    @GeneratedValue(generator = "JDBC")
    @Column(name = "fd_id")
    private Integer id; // 主键id
    @Column(name = "fd_name")
    private String name; // 演员姓名
    @Column(name = "fd_gender")
    private String gender; // 性别
    @Column(name = "fd_birth")
    private String birth; // 生日
    @Column(name = "fd_birthPlace")
    private String birthPlace; // 出生地
    @Column(name = "fd_job")
    private String job; // 工作
    @Column(name = "fd_introduce")
    private String introduce; // 简介
    @Column(name = "fd_state")
    private String state; // 状态
    @Column(name = "fd_headerImg")
    private String headerImg; // 演员头像
    @Column(name = "fd_remark")
    private String remark; // 备注
    @Column(name = "fd_createDate")
    private String createDate; // 创建时间
    @Column(name = "fd_reserved2")
    private String reserved2; // 租户标识

    public void save() {

        getPerformerRepository().insert(this);
    }

    public Performer selectActorByName(String actorName) {
        Performer performer = getPerformerRepository().selectActorByName(actorName);
        return performer;
    }

    private static PerformerRepository performerRepository;

    public static PerformerRepository getPerformerRepository() {
        if (null == performerRepository) {
            performerRepository = InstanceFactory.getInstance(PerformerRepository.class);
        }
        return performerRepository;
    }

    public Integer selectActorCountByActorName(String actorName) {
        return getPerformerRepository().selectActorCountByActorName(actorName);
    }
}