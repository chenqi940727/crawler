package com.yunzhitx.mediacrawler.web.rest.test;

import com.yunzhitx.cloud.common.model.InvokeResult;
import com.yunzhitx.mediacrawler.core.media.domain.Media;
import com.yunzhitx.mediacrawler.core.mediarefcategory.domain.MediaRefCategory;
import com.yunzhitx.mediacrawler.core.mediarefcategory.infra.MediaRefCategoryRepository;
import com.yunzhitx.mediacrawler.core.poster.domain.Poster;
import com.yunzhitx.mediacrawler.core.resource.domain.Resource;
import com.yunzhitx.mediacrawler.web.rest.IQYService.IQYUtils;
import com.yunzhitx.mediacrawler.web.rest.IQYService.TXUtils;
import com.yunzhitx.mediacrawler.web.rest.elasticsearch.EsController;
import com.yunzhitx.mediacrawler.web.rest.iqiyicrawler.IQYAnimeCrawler;
import com.yunzhitx.mediacrawler.web.rest.iqiyicrawler.IQYTVCrawler;
import com.yunzhitx.mediacrawler.web.rest.mediaInfo.*;
import com.yunzhitx.mediacrawler.web.rest.redis.BaseRedisDao;
import com.yunzhitx.mediacrawler.web.rest.txcrawler.TxAnimeCrawler;
import com.yunzhitx.mediacrawler.web.rest.txcrawler.TxFilmCrawler;
import com.yunzhitx.mediacrawler.web.rest.txcrawler.TxTVCrawler;
import com.yunzhitx.mediacrawler.web.rest.txcrawler.TxVarietyCrawler;
import com.yunzhitx.mediacrawler.web.rest.worldcupcrawler.BallKingCrawler;
import com.yunzhitx.mediacrawler.web.rest.worldcupcrawler.YoukuLookBackCrawler;
import com.yunzhitx.mediacrawler.web.util.RedisKey;
import com.yunzhitx.mediacrawler.web.util.SFTPUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * @author admin$
 * @Description: TODO
 * @date 2018/5/30$ 17:49$
 */
@RestController
@RequestMapping("test")
public class Test {

    @Autowired
    private BaseRedisDao<String, Object> redisDao;

    private static RestTemplate restTemplate = new RestTemplate();

    @Autowired
    RabbitTemplate rabbitTemplate;

    @RequestMapping("startMediaThread")
    public void startMediaThread(){
        IqyFilmCustomer meidiaCustomer = new IqyFilmCustomer();
        Thread meidiaCustomerThred = new Thread(meidiaCustomer);
        meidiaCustomerThred.start();
    }


    @RequestMapping("redisTest")
    public void redisTest(){
//        redis.set("singleKeyTest","zzzzz");
//        Map map = new HashMap();
//        map.put("zzz",111);
//        map.put("qqq",222);
//        redis.addMap("mapKeyTest","mapField", map);
    }

    @RequestMapping("ballKing")
    public InvokeResult getBallKingInfo(){
        BallKingCrawler.goGetBallKingWorldCupMedia(5, restTemplate);
        return InvokeResult.ok();
    }


    @RequestMapping(value = "rabbbit")
    public InvokeResult test( String abc){
        rabbitTemplate.convertAndSend("spring-boot", abc + " from RabbitMQ!");
        return  InvokeResult.ok();
    }


    @RequestMapping(value = "getLocation")
    public String getLocation( String url){
        String url11 = getLocationUrlTilNullOrEmpty(url);
        return  url11;
    }

    @RequestMapping("getBack")
    public InvokeResult getWorldCupBack() throws Exception {
        SFTPUtil sftpUtil = SFTPUtil.getInstance();
        sftpUtil.login();

        YoukuLookBackCrawler.visit();


        sftpUtil.logout();
        return InvokeResult.ok();
    }

    @RequestMapping("fixPoster")
    public InvokeResult fixPoster(){
        List<Integer> mediaIdList = Media.getMediaRepository().selectIdsRepeatPosters();
        for(Integer mediaId : mediaIdList){
            List<Poster> postersList = Poster.getPosterRepository().selectPostersByMediaId(mediaId);
            if(postersList.size() > 1){
                for(int i=1; i<postersList.size(); i++){
                    postersList.get(i).delete();
                }
            }
        }
        return InvokeResult.ok();
    }


    @RequestMapping("worldCupCrawler")
    public InvokeResult worldCupCrawler(){
        YoukuLookBackCrawler.visit();
        return InvokeResult.ok();
    }

    @RequestMapping("fixMedia")
    public InvokeResult fixMedia(){
        MediaRefCategoryRepository mediaRefCategoryRepository = MediaRefCategory.getMediaRefCategoryRepository();
        List<Integer> mediaIdList = Media.getMediaRepository().selectIdsRepeatMedia();
        for(Integer mediaId : mediaIdList){
            List<Integer> subMediaIdList = Media.getMediaRepository().selectSubMediaIdListByParentId(mediaId);
            for(int i=1; i<subMediaIdList.size(); i++){
                Integer subMediaId = subMediaIdList.get(i);
                Media.getMediaRepository().deleteByPrimaryKey(subMediaId);
                Resource.getResourceRepository().deleteByMediaId(subMediaId);
                List<MediaRefCategory> list = mediaRefCategoryRepository.selectAllPropsByMeidaId(mediaId);
                mediaRefCategoryRepository.delete(list.get(0));
                mediaRefCategoryRepository.insert(list.get(0));
            }
        }
        return InvokeResult.ok();
    }


    public static String getLocationUrl(String url){
        try {
            URL serverUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) serverUrl
                    .openConnection();
            conn.setRequestMethod("GET");
            // 必须设置false，否则会自动redirect到Location的地址
            conn.setInstanceFollowRedirects(false);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.addRequestProperty("Accept-Charset", "UTF-8;");
            conn.addRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.2.8) Firefox/3.6.8");
            conn.connect();
            String location = conn.getHeaderField("Location");
            conn.disconnect();

            return location;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private static String getLocationUrlTilNullOrEmpty(String targetUrl) {
        String url = getLocationUrl(targetUrl);
        if(StringUtils.isEmpty(url)){
            return targetUrl;
        }
        url = URLDecoder.decode(url);
        return getLocationUrlTilNullOrEmpty(url);
    }

    public static void main (String[] args){
        Map<String, Object> params = new HashMap();
        params.put("channelId", 9);
        params.put("code", "cctv5hd_1200.m3u8");
        String response = restTemplate.getForObject("http://portal.ott.sc96655.com:8080/PortalServer-App/new/aaa_aut_aut002?ptype=3&plocation=610000&puser=freeuser&ptoken=WisdomWorld&pversion=1&pserverAddress=1&pserialNumber=WisdomWorldPc&DRMtoken=&epgID=&authType=0&secondAuthid=&t=WisdomWorld&u=freeuser&p=3&cid={channelId}&l=610000&d=WisdomWorldPc&n={code}&v=2\";", String.class, params);
        System.out.println(response);


    }


    @RequestMapping("iqytvCrawler")
    public InvokeResult iqytvCrawler() throws Exception {
        Integer length = 30;
        IQYTVCrawler crawler2 = new IQYTVCrawler("crawlertv", true,redisDao);
        for (int i = 1; i <= length; i++) {
            crawler2.addSeed("http://list.iqiyi.com/www/2/-------------11-" + i + "-1---.html");
            crawler2.addSeed("http://list.iqiyi.com/www/2/-------------4-" + i + "-1---.html");
        }
        crawler2.getConf().setTopN(300);
        crawler2.setMaxExecuteCount(3);
        crawler2.start(1);
//        for(int i=0; i < 10; i++){
        IqyTVCrawlerCustomer iqytvCrawlerCustomer = new IqyTVCrawlerCustomer();
        Thread iqytvCrawlerCustomerThred = new Thread(iqytvCrawlerCustomer);
        iqytvCrawlerCustomerThred.start();
//        }
        InvokeResult result = new InvokeResult();
        result.setData(redisDao.getList(RedisKey.IQY_TV_ALBUMID_LIST));
        return result;
    }

    @RequestMapping("iqytcTest")
    public InvokeResult iqyTvTest(String albumId) throws Exception {
        IQYUtils.addTVMedia(albumId, restTemplate);
        return InvokeResult.ok();
    }

    @RequestMapping("getFailedAlbumName")
    public InvokeResult getFailedAlbumName(){
        List<Map> list = new ArrayList<Map>();
//        Map<String, Object> failedMap = redisDao.getMap(RedisKey.DEAL_FAILED);
//
//
//        for(String key : failedMap.keySet()){
//            Map<String, Object> map = new HashMap<String, Object>();
//            map.put("ablumId", key);
//            map.put("name", redisDao.getMapField(RedisKey.IQY_TV_ALBUMID_NAME, key));
//            list.add(map);
//        }
        return InvokeResult.ok(list);
    }

    @RequestMapping("txtvtest")
    public InvokeResult txTvTest() throws Exception {
        Integer length = 1;
        TxTVCrawler crawler = new TxTVCrawler("txTvCrawler", true,redisDao);
        for (int i = 0; i <= length; i++) {
            crawler.addSeed("http://v.qq.com/x/list/tv?sort=19&offset=" + i * 30);
            crawler.addSeed("http://v.qq.com/x/list/tv?sort=18&offset=" + i * 30);
        }
        crawler.getConf().setTopN(300);
        crawler.setMaxExecuteCount(3);
        crawler.start(1);
//        for(int i=0; i < 10; i++){
        TxTvCustomer txTvCustomer = new TxTvCustomer();
        Thread txTvCustomerThred = new Thread(txTvCustomer);
        txTvCustomerThred.start();
//        }
        InvokeResult result = new InvokeResult();
//        result.setData(redisDao.getMap(RedisKey.TV_TXID_NAME));
        return result;
    }

    @RequestMapping("txfilmtest")
    public InvokeResult txFilmTest() throws Exception {
        Integer length = 0;
        TxFilmCrawler crawler = new TxFilmCrawler("txFilmCrawler", true,redisDao);
        for (int i = 0; i <= length; i++) {
            crawler.addSeed("http://v.qq.com/x/list/movie?sort=19&offset=" + i * 30);
            crawler.addSeed("http://v.qq.com/x/list/movie?sort=18&offset=" + i * 30);
        }
        crawler.getConf().setTopN(300);
        crawler.setMaxExecuteCount(3);
        crawler.start(1);
//        for(int i=0; i < 10; i++){
        TxFilmCustomer txFilmCustomer = new TxFilmCustomer();
        Thread txFilmCustomerThred = new Thread(txFilmCustomer);
        txFilmCustomerThred.start();
//        }
        InvokeResult result = new InvokeResult();
//        result.setData(redisDao.getMap(RedisKey.FILM_TXID_NAME));
        return result;
    }

    @RequestMapping("txanimetest")
    public InvokeResult txAnimeTest() throws Exception {
        Integer length = 1;
        TxAnimeCrawler crawler = new TxAnimeCrawler("txAnimeCrawler", true,redisDao);
        for (int i = 0; i <= length; i++) {
            crawler.addSeed("http://v.qq.com/x/list/cartoon?sort=18&offset=" + i * 30);
            crawler.addSeed("http://v.qq.com/x/list/cartoon?sort=19&offset=" + i * 30);
        }
        crawler.getConf().setTopN(300);
        crawler.setMaxExecuteCount(3);
        crawler.start(1);
//        for(int i=0; i < 10; i++){
        TxAnimeCustomer txAnimeCustomer = new TxAnimeCustomer();
        Thread txAnimeCustomerThred = new Thread(txAnimeCustomer);
        txAnimeCustomerThred.start();
//        }
        InvokeResult result = new InvokeResult();
//        result.setData(redisDao.getMap(RedisKey.TV_TXID_NAME));
        return result;
    }

    @RequestMapping("txvarietytest")
    public InvokeResult txVarietyTest() throws Exception {
        Integer length = 1;
        TxVarietyCrawler crawler = new TxVarietyCrawler("txVarietyCrawler", true,redisDao);
        for (int i = 0; i <= length; i++) {
            crawler.addSeed("http://v.qq.com/x/list/variety?sort=4&offset=" + i * 30);
            crawler.addSeed("http://v.qq.com/x/list/variety?sort=5&offset=" + i * 30);
        }
        crawler.addRegex("https://v.qq.com/x/cover/[0-9A-Za-z]{15}.html");
        crawler.getConf().setTopN(300);
        crawler.setMaxExecuteCount(3);
        crawler.start(2);
        TxVarietyCustomer txVarietyCustomer = new TxVarietyCustomer();
        Thread txVarietyCustomerThred = new Thread(txVarietyCustomer);
        txVarietyCustomerThred.start();
        InvokeResult result = new InvokeResult();
//        result.setData(redisDao.getMap(RedisKey.TV_TXID_NAME));
        return result;
    }

    @RequestMapping("txvarietyTest")
    public InvokeResult txvarietyTest(String tvId) throws Exception {
//        TXUtils.addVarietyMedia(tvId, restTemplate);
        SimpleDateFormat sdf = new SimpleDateFormat(" yyyy-MM-dd HH:mm:ss ");
        Media packageMedia = Media.getMediaRepository().selectByPrimaryKey(874795);
        packageMedia.setUpdateDate(sdf.format(new Date()));
        packageMedia.setSerial(20160708);
        Media.getMediaRepository().updateByPrimaryKeySelective(packageMedia);

        /**
         * 修改搜索引擎中的信息
         */
        Integer id = packageMedia.getId();
        Media media2 = Media.getMediaRepository().getMediaDetail(id);
        EsController.addEsDate(media2);
        return InvokeResult.ok();
    }

    @RequestMapping("startTxTvCrawl")
    public InvokeResult startTxTvCrawl(String txTvId){
        redisDao.addList(RedisKey.TX_TV_ALBUMID_LIST, txTvId);
        TxTvCustomer txTvCustomer = new TxTvCustomer();
        Thread txTvCustomerThred = new Thread(txTvCustomer);
        txTvCustomerThred.start();
        return InvokeResult.ok();
    }

    @RequestMapping("iqyanimeTest")
    public InvokeResult iqyanimeTest() throws Exception {
        Integer length = 3;
        IQYAnimeCrawler crawler = new IQYAnimeCrawler("iqyAnimeCrawler", true,redisDao);
        for (int i = 1; i <= length; i++) {
            crawler.addSeed("http://list.iqiyi.com/www/4/-------------4-"+ i +"-1-iqiyi--.html");
            crawler.addSeed("http://list.iqiyi.com/www/4/-------------11-"+ i +"-1-iqiyi--.html");
        }
        crawler.getConf().setTopN(300);
        crawler.setMaxExecuteCount(3);
        crawler.start(1);
//        for(int i=0; i < 10; i++){
        IqyAnimeCustomer iqyAnimeCustomer = new IqyAnimeCustomer();
        Thread iqyAnimeCustomerThred = new Thread(iqyAnimeCustomer);
        iqyAnimeCustomerThred.start();
        return InvokeResult.ok();
    }

    @RequestMapping("iqyAnimeTest")
    public InvokeResult iqyAnimeTest(String albumId) throws Exception {
        IQYUtils.addAnimeMedia(albumId, restTemplate);
        return InvokeResult.ok();
    }

    @RequestMapping("fixVarietyMedia")
    public InvokeResult fixVarietyMedia() throws Exception {
        Integer length = 60;
        TxVarietyCrawler crawler = new TxVarietyCrawler("txVarietyCrawler", true,redisDao);
        for (int i = 0; i <= length; i++) {
            crawler.addSeed("http://v.qq.com/x/list/variety?sort=4&offset=" + i * 30);
            crawler.addSeed("http://v.qq.com/x/list/variety?sort=5&offset=" + i * 30);
        }
        crawler.addRegex("https://v.qq.com/x/cover/[0-9A-Za-z]{15}.html");
        crawler.getConf().setTopN(300);
        crawler.setMaxExecuteCount(3);
        crawler.start(2);

        Boolean flag = true;
        while(flag){
            if(redisDao.exists(RedisKey.TX_VARIETY_ALBUMID_LIST) && redisDao.getListSize(RedisKey.TX_VARIETY_ALBUMID_LIST) > 0){
                Long startTime = System.currentTimeMillis();
                String tvId = (String) redisDao.lPop(RedisKey.TX_VARIETY_ALBUMID_LIST);
                try {
                    TXUtils.fixVarietyMedia(tvId, restTemplate);
//                    Long endTime = System.currentTimeMillis();
//                    redisDao.addMap(RedisKey.DEAL_RESULT, tvId, "success, hs: " + (endTime-startTime));
                } catch (Exception e) {
//                    Long endTime = System.currentTimeMillis();
//                    redisDao.addMap(RedisKey.DEAL_RESULT, tvId, "failed, hs: " + (endTime-startTime));
                    e.printStackTrace();
                }
            }else{
                flag = false;
            }
        }
        return InvokeResult.ok();
    }


}
