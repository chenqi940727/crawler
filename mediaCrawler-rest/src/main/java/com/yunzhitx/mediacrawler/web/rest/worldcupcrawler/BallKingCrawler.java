package com.yunzhitx.mediacrawler.web.rest.worldcupcrawler;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.druid.util.StringUtils;
import com.yunzhitx.mediacrawler.web.rest.enumer.SourceType;
import com.yunzhitx.mediacrawler.web.rest.redis.BaseRedisDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author admin$
 * @Description: TODO
 * @date 2018/6/8$ 10:52$
 */
public class BallKingCrawler{


    private static String url = "http://www.dongqiudi.com/archives/114?page={page}";
    public static final String IMGURL = "http://juhe.fs.cdtown.cn/media/";
    public static final int sourceType = SourceType.TYPE_BALLKING.getIndex();
    public static final String fieldName = SourceType.TYPE_BALLKING.getName();
    public static final String pkgType = "资讯";

    public static void goGetBallKingWorldCupMedia(Integer pageNumber, RestTemplate restTemplate){
        Map map = new HashMap();
        for(int i = 0; i < pageNumber; i++){
            map.put("page", i);
            String response = restTemplate.getForObject(url, String.class, map);
//            redis.addMap("dongqiudi", String.valueOf(i), response);
            Map infoMap = (Map) JSONUtils.parse(response);
            List<Map> infoList = (List<Map>) infoMap.get("data");
            for(Map info : infoList){
                String title = (String) info.get("title");
                String description = (String) info.get("description");
                String posterSrc = (String) info.get("thumb");
                String playUrl = (String) info.get("web_url");
                if(StringUtils.isEmpty(description)){
                    description = title;
                }
                WorldCupUtil.addWorldCupMedia(pkgType, sourceType, title, description, posterSrc, fieldName, playUrl);
            }
        }
    }
}
