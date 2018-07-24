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
public class TxFilmCustomer implements Runnable{


    private volatile BaseRedisDao redisDao;

    private static volatile RestTemplate restTemplate = new RestTemplate();


    @Override
    public void run() {
        this.redisDao = BeanContext.getApplicationContext().getBean(BaseRedisDao.class);
        Boolean flag = true;
        while(flag){
            if(redisDao.exists(RedisKey.TX_FILM_ALBUMID_LIST) && redisDao.getListSize(RedisKey.TX_FILM_ALBUMID_LIST) > 0){
                Long startTime = System.currentTimeMillis();
                String tvId = (String) redisDao.lPop(RedisKey.TX_FILM_ALBUMID_LIST);
                try {
                    TXUtils.addFilmMedia(tvId, restTemplate);
                    Long endTime = System.currentTimeMillis();
                    redisDao.addMap(RedisKey.DEAL_RESULT, tvId, "success, hs: " + (endTime-startTime));
                } catch (Exception e) {
                    Long endTime = System.currentTimeMillis();
                    redisDao.addMap(RedisKey.DEAL_RESULT, tvId, "failed, hs: " + (endTime-startTime));
                    e.printStackTrace();
                }
            }else{
                flag = false;
            }
        }
    }
}
