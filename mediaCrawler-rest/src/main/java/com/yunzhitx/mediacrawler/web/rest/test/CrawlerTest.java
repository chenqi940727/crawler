package com.yunzhitx.mediacrawler.web.rest.test;

import com.alibaba.druid.support.json.JSONUtils;
import com.yunzhitx.cloud.common.model.InvokeResult;
import com.yunzhitx.mediacrawler.core.poster.domain.Poster;
import com.yunzhitx.mediacrawler.web.rest.worldcupcrawler.BallKingCrawler;
import com.yunzhitx.mediacrawler.web.rest.worldcupcrawler.CCTVWorldCupCrawler;
import com.yunzhitx.mediacrawler.web.rest.elasticsearch.EsController;
import com.yunzhitx.mediacrawler.web.rest.iqiyicrawler.IQYFilmTestCrawler;
import com.yunzhitx.mediacrawler.web.rest.mediaInfo.MeidiaCustomer;
import com.yunzhitx.mediacrawler.web.rest.redis.BaseRedisDao;
import com.yunzhitx.mediacrawler.web.rest.worldcupcrawler.TxCrawler;
import com.yunzhitx.mediacrawler.web.rest.worldcupcrawler.TxCrawler2;
import com.yunzhitx.mediacrawler.web.util.RedisKey;
import com.yunzhitx.mediacrawler.web.util.SFTPUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import tk.mybatis.mapper.util.StringUtil;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author admin$
 * @Description: TODO
 * @date 2018/5/31$ 11:50$
 */
@RestController
@RequestMapping("crawler")
public class CrawlerTest {

    @Autowired
    private BaseRedisDao<String, Object> redisDao;

    private RestTemplate restTemplate = new RestTemplate();

    @RequestMapping("crawler")
    public InvokeResult crawlerTest() throws Exception {
        Integer length = 30;
        IQYFilmTestCrawler crawler2 = new IQYFilmTestCrawler("crawler", true,redisDao);
        for (int i = 1; i <= length; i++) {
            crawler2.addSeed("http://list.iqiyi.com/www/1/-------------4-" + i + "-1-iqiyi--.html");
            crawler2.addSeed("http://list.iqiyi.com/www/1/-------------11-" + i + "-1-iqiyi--.html");
        }
        crawler2.getConf().setTopN(300);
        crawler2.setMaxExecuteCount(3);
        crawler2.start(1);
//        for(int i=0; i < 10; i++){
            MeidiaCustomer meidiaCustomer = new MeidiaCustomer();
            Thread meidiaCustomerThred = new Thread(meidiaCustomer);
            meidiaCustomerThred.start();
//        }
        InvokeResult result = new InvokeResult();
        result.setData(redisDao.getList(RedisKey.FILM_ALBUMID_LIST));
        return result;
    }

    @RequestMapping("cctvCrawler")
    public InvokeResult CCTVCrawlerTest() throws Exception {
        SFTPUtil sftpUtil = SFTPUtil.getInstance();
        sftpUtil.login();

        CCTVWorldCupCrawler crawler2 = new CCTVWorldCupCrawler("cctvCrawler", false);
        crawler2.addSeed("http://worldcup.cctv.com/2018/videos/index.shtml");
        crawler2.getConf().setTopN(300);
        crawler2.setMaxExecuteCount(3);
        crawler2.setThreads(1);
        crawler2.start(1);

        BallKingCrawler.goGetBallKingWorldCupMedia(10,restTemplate);

        TxCrawler2.visit(10,restTemplate);

        sftpUtil.logout();
        return InvokeResult.ok();
    }


    @RequestMapping("getMediaImgs")
    public InvokeResult getMediaImgs() throws IOException {
        List<Integer> mediaIdList = Poster.getPosterRepository().selectAllMediaIds();
        for(Integer mediaId : mediaIdList){
            Map<String, Object> map = EsController.selectMediaByMediaId(mediaId);
            List<Map<String, Object>> mediaMapList = (List<Map<String, Object>>) map.get("data");
            if(mediaMapList.size() == 0){
                System.out.println(mediaId);
                continue;
            }
            Map<String, Object>mediaMap = mediaMapList.get(0);
            String poster = (String) mediaMap.get("posters");
            redisDao.addMap(RedisKey.MEDIA_ES, mediaId.toString(), poster);
        }
        return InvokeResult.ok();
    }

    @RequestMapping("recoverLargeImg")
    public InvokeResult recoverLargeImg() throws IOException {
        Map<String, Object> map = redisDao.getMap(RedisKey.MEDIA_ES);
        Poster mediaPoster = new Poster();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String value = entry.getValue().toString();
            List<Map> posterList = (List<Map>) JSONUtils.parse(value);
            Map poster = posterList.get(0);
            String large = (String) poster.get("large");
            if(StringUtil.isNotEmpty(large)){
                Integer mediaId = Integer.valueOf(entry.getKey());
                mediaPoster.updateLargeByMediaId(mediaId, large);
            }
        }
        return InvokeResult.ok();
    }

}
