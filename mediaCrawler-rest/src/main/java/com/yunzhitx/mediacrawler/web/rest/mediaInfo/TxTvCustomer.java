package com.yunzhitx.mediacrawler.web.rest.mediaInfo;

import com.yunzhitx.mediacrawler.web.rest.IQYService.TXUtils;
import com.yunzhitx.mediacrawler.web.rest.redis.BaseRedisDao;
import com.yunzhitx.mediacrawler.web.rest.redis.BeanContext;
import com.yunzhitx.mediacrawler.web.util.RedisKey;
import org.springframework.web.client.RestTemplate;

/**
 * @description: TODO
 * @author: 陈奇
 * @createAt Date: 2018/7/12$-9:24$
 * @modificationHistory: who  when  what
 * ---------     -------------   --------------------------------------
 **/
public class TxTvCustomer implements Runnable{


    private volatile BaseRedisDao redisDao;

    private static volatile RestTemplate restTemplate = new RestTemplate();


    @Override
    public void run() {
        this.redisDao = BeanContext.getApplicationContext().getBean(BaseRedisDao.class);
        Boolean flag = true;
        while(flag){
            if(redisDao.exists(RedisKey.TV_TXID_LIST) && redisDao.getListSize(RedisKey.TV_TXID_LIST) > 0){
                Long startTime = System.currentTimeMillis();
                String tvId = (String) redisDao.lPop(RedisKey.TV_TXID_LIST);
                try {
                    TXUtils.addTVMedia(tvId, restTemplate);
                    Long endTime = System.currentTimeMillis();
                    redisDao.addMap(RedisKey.TX_TV_DEAL_RESULT, tvId, "success, hs: " + (endTime-startTime));
                    if(redisDao.getMapField(RedisKey.TX_TV_DEAL_FAILED, tvId) != null){
                        redisDao.removeMapField(RedisKey.TX_TV_DEAL_FAILED, tvId);
                    }
                } catch (Exception e) {
                    Long endTime = System.currentTimeMillis();
                    redisDao.addMap(RedisKey.TX_TV_DEAL_RESULT, tvId, "failed, hs: " + (endTime-startTime));
                    redisDao.addMap(RedisKey.TX_TV_DEAL_FAILED, tvId, "failed");
                    e.printStackTrace();
                }
            }else{
                flag = false;
            }
        }
    }
}
