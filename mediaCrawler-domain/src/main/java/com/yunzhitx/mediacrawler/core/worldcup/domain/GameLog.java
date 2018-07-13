package com.yunzhitx.mediacrawler.core.worldcup.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.yunzhitx.cloud.common.model.ddd.AbstractEntity;
import com.yunzhitx.cloud.common.model.ddd.InstanceFactory;
import com.yunzhitx.mediacrawler.core.worldcup.infra.GameLogRepository;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * 代码生成器自动生成
 * Date:2018-5-4 10:50:45
 * @author
 */
@Data
@Table(name = "wc_game_log")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GameLog extends AbstractEntity<GameLogRepository> implements Serializable{

    private static final long serialVersionUID = 1L;

    /** */
    @Column(name = "teamLeftId")
    private Integer teamLeftId;
    /** */
    @Column(name = "teamRightId")
    private Integer teamRightId;
    /** */
    @Column(name = "playUrl")
    private String playUrl;
    /** */
    @Column(name = "beginTime")
    private Date beginTime;
    /** */
    @Column(name = "finishTime")
    private Date finishTime;

    @Column(name = "gameIndex")
    private Integer gameIndex;

    @Column(name = "highlightUrl")
    private String highlightUrl;

    public GameLog(){}

    public GameLog(Integer id){setId(id);}

    @Override
    public Serializable getKey() {
        return getId();
    }

    @Override
    public boolean existed() {
        return false;
    }

    private static GameLogRepository gameLogRepository;
    public static GameLogRepository getGameLogRepository(){
        if(null==gameLogRepository){
            gameLogRepository = InstanceFactory.getInstance(GameLogRepository.class);
        }
        return gameLogRepository;
    }

    /**
    * 根据Id，更新GameLog
    *
    * @param
    * @return
    */
    public void update() {
        getGameLogRepository().updateByPrimaryKeySelective(this);
    }

    public void save() {
        getGameLogRepository().insert(this);
    }

    /**
    * 根据主键ID查询GameLog
    * @param id
    * @return
    */
    /*
    public static GameLogVO selectDetailById(int id) {
        GameLogVO vo = getGameLogRepository().selectDetailById(id);
        return vo;
    }
    */

}