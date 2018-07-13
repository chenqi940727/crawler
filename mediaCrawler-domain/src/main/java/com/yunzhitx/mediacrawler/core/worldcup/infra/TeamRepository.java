package com.yunzhitx.mediacrawler.core.worldcup.infra;

import com.yunzhitx.mediacrawler.core.worldcup.domain.Team;
import com.yunzhitx.mediacrawler.utils.MyBatisBaseMapper;

/**
* 代码生成器自动生成
* Date:2018-5-3 17:52:20
* @author
*/

public interface TeamRepository extends MyBatisBaseMapper<Team> {

    Team getTeamByCountryName(String countryName);

    /**
    * 根据主键ID查询Team
    * @param id
    * @return
    */
    /*
    TeamVO selectDetailById(int id) ;
    */

}