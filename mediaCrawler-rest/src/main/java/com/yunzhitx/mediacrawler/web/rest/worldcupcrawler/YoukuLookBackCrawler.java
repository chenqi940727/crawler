package com.yunzhitx.mediacrawler.web.rest.worldcupcrawler;

import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.plugin.berkeley.BreadthCrawler;
import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.yunzhitx.mediacrawler.web.rest.enumer.SourceType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author admin$
 * @Description: TODO
 * @date 2018/6/15$ 13:59$
 */
public class YoukuLookBackCrawler{


    private final static String youkuUrl = "http://list.youku.com/show/module?id={id}&tab={tab}&cname=体育&callback=null";
    private static int[] ids = {
                            405082, 405081, 405079, 405078, 405077, 405076, 405075, 405074,
                            405073, 405072, 405071, 405070, 405069, 405068, 405067, 405066,
                            404911, 404914, 404936, 404935, 404875, 404873, 404893, 404897,
                            404927, 404925, 404920, 404918, 404896, 404895, 404886, 404884,
                            404932, 404931, 404908, 404887, 404883, 404906, 404872, 404924,
                            404870, 404923, 404916, 404913, 404892, 404881, 404891, 404880,
                            404930, 404928, 404905, 404904, 404879, 404867, 404876, 404842,
                            404922, 404910, 404921, 404909, 404889, 404888, 404878, 404846
                        };

    public static final String IMGURL = "http://juhe.fs.cdtown.cn/media/";
    public static final int sourceType = SourceType.TYPE_YOUKU.getIndex();
    public static final String fieldName = "优酷";
    public static final String pkgType = "资讯";


    private static RestTemplate restTemplate = new RestTemplate();
    private static Pattern itemPattern = Pattern.compile("<div.*?class=\\\"p-drama-list\\\">([\\s\\S]*?)</div>");


    public static void visit() {
        Map params = new HashMap();
        for(int id : ids){
            params.put("id", id);

            crawlerLookBackMedia(params);
            crawlerHighLightMedia(params);
            crawlerShootTime(params);
            crawlerHighPowerMedia(params);
            crawlerFastViewMedia(params);
        }
    }

    private static void crawlerFastViewMedia(Map params) {
        params.put("tab","around_4");
        String response = restTemplate.getForObject(youkuUrl,String.class,params);
        response = unicodeToString(response);
        response = response.replace("window.null && null(","");
        response = response.substring(0, response.length() - 2);
        Map infoMap = JSON.parseObject(response);
        String html = (String) infoMap.get("html");
        if(StringUtils.isEmpty(html)){
            return;
        }
        Document doc = Jsoup.parse(html);
        Elements items = doc.select(".yk-col4");
        for(Element ele : items){
            Element aTag = ele.select("a").get(0);
            String playSrc = aTag.attr("href");
            if(!playSrc.startsWith("http")){
                playSrc = "https:" + playSrc;
            }
            String title = aTag.attr("title");
            String posterSrc = ele.select("img").attr("src");
            if(!posterSrc.startsWith("http")){
                posterSrc = "https:" + playSrc;
            }
            WorldCupFastViewUtil.addWorldCupMedia(pkgType, sourceType, title, title, posterSrc, fieldName, playSrc);
        }
    }

    private static void crawlerHighPowerMedia(Map params) {
        params.put("tab","around_6");
        String response = restTemplate.getForObject(youkuUrl,String.class,params);
        response = unicodeToString(response);
        response = response.replace("window.null && null(","");
        response = response.substring(0, response.length() - 2);
        Map infoMap = JSON.parseObject(response);
        String html = (String) infoMap.get("html");
        if(StringUtils.isEmpty(html)){
            return;
        }
        Document doc = Jsoup.parse(html);
        Elements items = doc.select(".yk-col4");
        for(Element ele : items){
            Element aTag = ele.select("a").get(0);
            String playSrc = aTag.attr("href");
            if(!playSrc.startsWith("http")){
                playSrc = "https:" + playSrc;
            }
            String title = aTag.attr("title");
            String posterSrc = ele.select("img").attr("src");
            if(!posterSrc.startsWith("http")){
                posterSrc = "https:" + playSrc;
            }
            WorldCupHighPowerUtil.addWorldCupMedia(pkgType, sourceType, title, title, posterSrc, fieldName, playSrc);
        }
    }

    private static void crawlerShootTime(Map params) {
        params.put("tab","around_11");
        String response = restTemplate.getForObject(youkuUrl,String.class,params);
        response = unicodeToString(response);
        response = response.replace("window.null && null(","");
        response = response.substring(0, response.length() - 2);
        Map infoMap = JSON.parseObject(response);
        String html = (String) infoMap.get("html");
        if(StringUtils.isEmpty(html)){
            return;
        }
        Document doc = Jsoup.parse(html);
        Elements items = doc.select(".yk-col4");
        for(Element ele : items){
            Element aTag = ele.select("a").get(0);
            String playSrc = aTag.attr("href");
            if(!playSrc.startsWith("http")){
                playSrc = "https:" + playSrc;
            }
            String title = aTag.attr("title");
            String posterSrc = ele.select("img").attr("src");
            if(!posterSrc.startsWith("http")){
                posterSrc = "https:" + playSrc;
            }
            WorldCupShootTimeUtil.addWorldCupMedia(pkgType, sourceType, title, title, posterSrc, fieldName, playSrc);
        }
    }

    private static void crawlerHighLightMedia(Map params) {
        params.put("tab","around_15");
        String response = restTemplate.getForObject(youkuUrl,String.class,params);
        response = unicodeToString(response);
        response = response.replace("window.null && null(","");
        response = response.substring(0, response.length() - 2);
        Map infoMap = JSON.parseObject(response);
        String html = (String) infoMap.get("html");
        if(StringUtils.isEmpty(html)){
            return;
        }
        Document doc = Jsoup.parse(html);
        Elements items = doc.select(".yk-col4");
        for(Element ele : items){
            Element aTag = ele.select("a").get(0);
            String playSrc = aTag.attr("href");
            if(!playSrc.startsWith("http")){
                playSrc = "https:" + playSrc;
            }
            String title = aTag.attr("title");
            String posterSrc = ele.select("img").attr("src");
            if(!posterSrc.startsWith("http")){
                posterSrc = "https:" + playSrc;
            }
            WorldCupHighLightUtil.addWorldCupMedia(pkgType, sourceType, title, title, posterSrc, fieldName, playSrc);
        }
    }

    private static void crawlerLookBackMedia(Map params) {
        params.put("tab","showInfo");
        String response = restTemplate.getForObject(youkuUrl,String.class,params);
        response = unicodeToString(response);
        response = response.replace("window.null && null(","");
        response = response.substring(0, response.length() - 2);
        Map infoMap = JSON.parseObject(response);
        String html = (String) infoMap.get("html");
        if(StringUtils.isEmpty(html)){
            return;
        }
        Document doc = Jsoup.parse(html);
        Element div = doc.getElementById("point_reload_1");
        if(div == null){
            return;
        }
        Elements items = div.select(".p-item");
        for(Element ele : items){
            Element aTag = ele.select("a").get(0);
            String playSrc = aTag.attr("href");
            if(!playSrc.startsWith("http")){
                playSrc = "https:" + playSrc;
            }
            String title = aTag.attr("title");
            String posterSrc = ele.select("img").attr("src");
            if(!posterSrc.startsWith("http")){
                posterSrc = "https:" + playSrc;
            }
            WorldCupLookBackUtil.addWorldCupMedia(pkgType, sourceType, title, title, posterSrc, fieldName, playSrc);
        }
    }



    public static String unicodeToString(String str) {

        Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");
        Matcher matcher = pattern.matcher(str);
        char ch;
        while (matcher.find()) {
            //group 6728
            String group = matcher.group(2);
            //ch:'木' 26408
            ch = (char) Integer.parseInt(group, 16);
            //group1 \u6728
            String group1 = matcher.group(1);
            str = str.replace(group1, ch + "");
        }
        return str;
    }

    private static String getItemIdByStr(String sourceString, Pattern pattern) {
        String targetString = null;
        Matcher matcher = pattern.matcher(sourceString);
        while(matcher.find()){
            targetString = matcher.group();
        }
        return targetString;
    }


    public static void main (String[] args){

    }
}


