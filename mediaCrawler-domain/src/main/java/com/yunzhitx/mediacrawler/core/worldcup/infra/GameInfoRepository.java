package com.yunzhitx.mediacrawler.core.worldcup.infra;

import com.yunzhitx.mediacrawler.core.worldcup.domain.GameInfo;
import com.yunzhitx.mediacrawler.utils.MyBatisBaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* 代码生成器自动生成
* Date:2018-5-4 10:50:45
* @author
*/
@Mapper
public interface GameInfoRepository extends MyBatisBaseMapper<GameInfo> {

    /**
    * 根据主键ID查询GameInfo
    * @param id
    * @return
    */
    /*
    GameInfoVO selectDetailById(int id) ;
    */

}