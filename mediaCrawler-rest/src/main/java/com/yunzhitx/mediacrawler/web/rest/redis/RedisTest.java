package com.yunzhitx.mediacrawler.web.rest.redis;

import com.sleepycat.je.tree.IN;
import com.yunzhitx.cloud.common.model.InvokeResult;
import com.yunzhitx.mediacrawler.web.util.RedisKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author admin$
 * @Description: TODO
 * @date 2018/5/31$ 16:24$
 */
@RestController
@RequestMapping("redis")
public class RedisTest {

    @Autowired
    private BaseRedisDao redisDao;


    @RequestMapping("test")
    public InvokeResult redisTest(){
        InvokeResult invokeResult = new InvokeResult();
//        redisDao.addList(RedisKey.FILM_ALBUMID_LIST,"zzzzzzz");
//        invokeResult.setData(redisDao.getList(RedisKey.FILM_ALBUMID_LIST));
        return invokeResult;
    }
}
