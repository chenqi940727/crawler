package com.yunzhitx.mediacrawler.web.rest.mediaInfo;

import com.yunzhitx.mediacrawler.web.rest.IQYService.IQYUtils;
import com.yunzhitx.mediacrawler.web.rest.redis.BaseRedisDao;
import com.yunzhitx.mediacrawler.web.rest.redis.BeanContext;
import com.yunzhitx.mediacrawler.web.util.RedisKey;
import org.springframework.web.client.RestTemplate;

/**
 * @author admin$
 * @Description: TODO
 * @date 2018/5/31$ 17:53$
 */
public class IqyAnimeCustomer implements Runnable {

    private volatile BaseRedisDao redisDao;

    private static volatile RestTemplate restTemplate = new RestTemplate();


    @Override
    public void run() {
        this.redisDao = BeanContext.getApplicationContext().getBean(BaseRedisDao.class);
        Boolean flag = true;
        while(flag){
            if(redisDao.exists(RedisKey.IQY_ANIME_ALBUMID_LIST) && redisDao.getListSize(RedisKey.IQY_ANIME_ALBUMID_LIST) > 0){
                Long startTime = System.currentTimeMillis();
                String albumId = (String) redisDao.lPop(RedisKey.IQY_ANIME_ALBUMID_LIST);
                try {
                    IQYUtils.addAnimeMedia(albumId, restTemplate);
                    Long endTime = System.currentTimeMillis();
                    redisDao.addMap(RedisKey.DEAL_RESULT, albumId, "success, hs: " + (endTime-startTime));
                } catch (Exception e) {
                    Long endTime = System.currentTimeMillis();
                    redisDao.addMap(RedisKey.DEAL_RESULT, albumId, "failed, hs: " + (endTime-startTime));
                    e.printStackTrace();
                }
            }else{
                flag = false;
            }
        }

    }
}
