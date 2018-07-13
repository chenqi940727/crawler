package com.yunzhitx.mediacrawler.core.worldcup.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.yunzhitx.cloud.common.model.ddd.AbstractEntity;
import com.yunzhitx.cloud.common.model.ddd.InstanceFactory;
import com.yunzhitx.mediacrawler.core.worldcup.infra.TeamRepository;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * 代码生成器自动生成
 * Date:2018-5-3 17:52:20
 * @author
 */
@Data
@Table(name = "wc_team")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Team extends AbstractEntity<TeamRepository> implements Serializable{

    private static final long serialVersionUID = 1L;

    /** */
    @Column(name = "teamName")
    private String teamName;
    /** */
    @Column(name = "teamCountry")
    private String teamCountry;
    /** */
    @Column(name = "teamGroup")
    private String teamGroup;
    /** */
    @Column(name = "flagUrl")
    private String flagUrl;

    @Column(name="groupRank")
    private Integer groupRank;

    public Team(){}

    public Team(Integer id){setId(id);}

    @Override
    public Serializable getKey() {
        return getId();
    }

    @Override
    public boolean existed() {
        return false;
    }

    private static TeamRepository teamRepository;
    public static TeamRepository getTeamRepository(){
        if(null==teamRepository){
            teamRepository = InstanceFactory.getInstance(TeamRepository.class);
        }
        return teamRepository;
    }

    /**
    * 根据Id，更新Team
    *
    * @param
    * @return
    */
    public void update() {
        getTeamRepository().updateByPrimaryKeySelective(this);
    }


    public void save(){
        getTeamRepository().insert(this);
    }

    public Team getTeamByCountryName(String countryName) {
        return getTeamRepository().getTeamByCountryName(countryName);
    }



    /**
    * 根据主键ID查询Team
    * @param id
    * @return
    */
    /*
    public static TeamVO selectDetailById(int id) {
        TeamVO vo = getTeamRepository().selectDetailById(id);
        return vo;
    }
    */


}