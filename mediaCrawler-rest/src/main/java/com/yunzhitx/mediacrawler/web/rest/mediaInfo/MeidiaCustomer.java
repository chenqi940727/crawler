package com.yunzhitx.mediacrawler.web.rest.mediaInfo;

import com.alibaba.fastjson.JSON;
import com.yunzhitx.mediacrawler.web.rest.IQYService.IQYUtils;
import com.yunzhitx.mediacrawler.web.rest.redis.BaseRedisDao;
import com.yunzhitx.mediacrawler.web.rest.redis.BeanContext;
import com.yunzhitx.mediacrawler.web.util.RedisKey;
import com.yunzhitx.mediacrawler.web.util.SFTPUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Thread.sleep;

/**
 * @author admin$
 * @Description: TODO
 * @date 2018/5/31$ 17:53$
 */
public class MeidiaCustomer implements Runnable {

    private volatile BaseRedisDao redisDao;

    private static volatile RestTemplate restTemplate = new RestTemplate();


    @Override
    public void run() {
        this.redisDao = BeanContext.getApplicationContext().getBean(BaseRedisDao.class);
        Boolean flag = true;
        while(flag){
            if(redisDao.exists(RedisKey.FILM_ALBUMID_LIST) && redisDao.getListSize(RedisKey.FILM_ALBUMID_LIST) > 0){
                Long startTime = System.currentTimeMillis();
                Object albumId = redisDao.lPop(RedisKey.FILM_ALBUMID_LIST);
                Map param = new HashMap();
                param.put("albumId", albumId);
                String response = restTemplate.getForObject("http://mixer.video.iqiyi.com/jp/mixin/videos/{albumId}?select=cast", String.class,param);
                Long middleTime = System.currentTimeMillis();
                String infoMapString = response.split("tvInfoJs=")[1];
                Map infoMap = JSON.parseObject(infoMapString);
                try {
                    IQYUtils.addMedia(infoMap);
                    Long endTime = System.currentTimeMillis();
                    redisDao.addMap(RedisKey.DEAL_RESULT, albumId, "success, hs: " + (endTime-startTime) + ", mhs: " + (endTime-middleTime));
                } catch (Exception e) {
                    Long endTime = System.currentTimeMillis();
                    redisDao.addMap(RedisKey.DEAL_RESULT, albumId, "failed, hs: " + (endTime-startTime) + ", mhs: " + (endTime-middleTime));
                    redisDao.addMap(RedisKey.DEAL_FAILED, albumId, "failed");
                    e.printStackTrace();
                }
            }else{
                flag = false;
            }
        }

    }
}
