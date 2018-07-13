package com.yunzhitx.mediacrawler.web.rest.worldcupcrawler;

import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.plugin.berkeley.BreadthCrawler;
import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.yunzhitx.mediacrawler.web.rest.enumer.SourceType;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author admin$
 * @Description: TODO
 * @date 2018/6/8$ 10:53$
 */
public class TxCrawler2{

    public static final String IMGURL = "http://juhe.fs.cdtown.cn/media/";
    public static final int sourceType = SourceType.TYPE_TENXUN.getIndex();
    public static final String fieldName = SourceType.TYPE_TENXUN.getName();
    public static final String pkgType = "资讯";
    public static final Integer cateforyId = 10000006;
    public static final Integer propertyId = 20000029;
    public static final Integer[] propertyValueIds= new Integer[]{30000443, 30000485, 30000487};
    public static final String splitTag = "-yztx-";

    private static String url = "http://sports.qq.com/l/isocce/2018wc/list20180228153732{page}.htm";
    private static String url2 = "http://sports.qq.com/l/isocce/2018wc/2018yc/list2018033142648{page}.htm";

    private static Pattern divPattern = Pattern.compile("<div id=\"articleLiInHidden\" style=\"display:none;\">([\\s\\S]+?)</div>");
    private static Pattern scriptPattern = Pattern.compile("<script>([\\s\\S]+?)</script>");
    private static Pattern blankPattern = Pattern.compile("\\s*|\t|\r|\n");

    public static void visit(Integer pageNumber, RestTemplate restTemplate) {

        Map map = new HashMap();
        String flag = "";
        for(int i = 1; i < pageNumber; i++){
            if(i != 1){
                flag = "_" + i;
            }
            map.put("page", flag);
            String response = restTemplate.getForObject(url, String.class, map);
            dealwithResponse(response);
            String response2 = restTemplate.getForObject(url, String.class, map);
            dealwithResponse(response2);
        }
    }

    private static void dealwithResponse(String response) {
        if(StringUtils.isEmpty(response)){
            return;
        }
        response = getInfoByPattern(response, divPattern);
        response = getInfoByPattern(response, scriptPattern);
        if(StringUtils.isEmpty(response)){
            return;
        }
        response = response.replace("//列表数据", "");
        response = response.split("ARTICLE_LIST=")[1];
        response = response.replace("</script>","");
        Map infoMap = JSON.parseObject(replaceBlank(response));
        List<Map> infoList = (List<Map>) infoMap.get("listInfo");
        for(Map info : infoList){
            String title = (String) info.get("title");
            String playUrl = (String) info.get("url");
            String imgSrc = (String) info.get("imgurl");

            WorldCupUtil.addWorldCupMedia(pkgType, sourceType, title, title, imgSrc, fieldName, playUrl);
        }
    }

    private static String getInfoByPattern(String sourceString, Pattern pattern) {
        if(StringUtils.isEmpty(sourceString)){
            return null;
        }
        String targetString = null;
        Matcher matcher = pattern.matcher(sourceString);
        while(matcher.find()){
            targetString = matcher.group();
        }
        return targetString;
    }

    public static String replaceBlank(String str) {
        String dest = "";
        if (str!=null) {
            Matcher m = blankPattern.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }
}
