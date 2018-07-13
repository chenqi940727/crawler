package com.yunzhitx.mediacrawler.core.performer.infra;

import com.yunzhitx.mediacrawler.core.performer.domain.Performer;
import com.yunzhitx.mediacrawler.utils.MyBatisBaseMapper;

/**
* 代码生成器自动生成
* Date:2017-9-4 17:07:46
* @author
*/
public interface PerformerRepository extends MyBatisBaseMapper<Performer>{

    Performer selectActorByName(String actorName);

    Integer selectActorCountByActorName(String actorName);
}