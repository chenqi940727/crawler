package com.yunzhitx.mediacrawler.core.worldcup.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.yunzhitx.cloud.common.model.ddd.AbstractEntity;
import com.yunzhitx.cloud.common.model.ddd.InstanceFactory;
import com.yunzhitx.mediacrawler.core.worldcup.infra.GameInfoRepository;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * 代码生成器自动生成
 * Date:2018-5-4 10:50:45
 * @author
 */
@Data
@Table(name = "wc_game_info")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GameInfo extends AbstractEntity<GameInfoRepository> implements Serializable{

    private static final long serialVersionUID = 1L;

    /** */
    @Column(name = "gameId")
    private Integer gameId;
    /** */
    @Column(name = "teamId")
    private Integer teamId;
    /** */
    @Column(name = "score")
    private Integer score;
    /** */
    @Column(name = "result")
    private Integer result;

    public GameInfo(){}

    public GameInfo(Integer id){setId(id);}

    @Override
    public Serializable getKey() {
        return getId();
    }

    @Override
    public boolean existed() {
        return false;
    }

    private static GameInfoRepository gameInfoRepository;
    public static GameInfoRepository getGameInfoRepository(){
        if(null==gameInfoRepository){
            gameInfoRepository = InstanceFactory.getInstance(GameInfoRepository.class);
        }
        return gameInfoRepository;
    }

    /**
    * 根据Id，更新GameInfo
    *
    * @param
    * @return
    */
    public void update() {
        getGameInfoRepository().updateByPrimaryKeySelective(this);
    }

    public void save() {
        getGameInfoRepository().insert(this);
    }

    /**
    * 根据主键ID查询GameInfo
    * @param id
    * @return
    */
    /*
    public static GameInfoVO selectDetailById(int id) {
        GameInfoVO vo = getGameInfoRepository().selectDetailById(id);
        return vo;
    }
    */

}