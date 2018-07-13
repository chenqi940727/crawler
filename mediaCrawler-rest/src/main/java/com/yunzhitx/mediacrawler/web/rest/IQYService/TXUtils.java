package com.yunzhitx.mediacrawler.web.rest.IQYService;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.yunzhitx.mediacrawler.core.categoryproperty.domain.CategoryProperty;
import com.yunzhitx.mediacrawler.core.categorypropertyvalue.domain.CategoryPropertyValue;
import com.yunzhitx.mediacrawler.core.media.domain.Media;
import com.yunzhitx.mediacrawler.core.media.infra.MediaRepository;
import com.yunzhitx.mediacrawler.core.mediacategory.domain.MediaCategory;
import com.yunzhitx.mediacrawler.core.mediarefcategory.domain.MediaRefCategory;
import com.yunzhitx.mediacrawler.core.poster.domain.Poster;
import com.yunzhitx.mediacrawler.web.rest.elasticsearch.EsController;
import com.yunzhitx.mediacrawler.web.rest.enumer.MediaPackageType;
import com.yunzhitx.mediacrawler.web.rest.enumer.MediaType;
import com.yunzhitx.mediacrawler.web.rest.enumer.SourceType;
import com.yunzhitx.mediacrawler.web.rest.redis.BaseRedisDao;
import com.yunzhitx.mediacrawler.web.util.DownloadImage;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author admin$
 * @Description: TODO
 * @date 2018/6/1$ 15:07$
 */
public class TXUtils {

    private static final Logger logger = LoggerFactory.getLogger(TXUtils.class);

    private static volatile Integer count = 0;

    private static Integer sourceType = SourceType.TYPE_TENXUN.getIndex(); //腾讯视频id
    private static final String fieldName = SourceType.TYPE_TENXUN.getName();
    private static final String pkgType = MediaType.TYPE_FILM.getName();
    private static final String tvpkgType = MediaType.TYPE_TV.getName();
    private static final String IMGURL = "http://juhe.fs.cdtown.cn/media/";
    private static final String tenantFlag = "yztx";


    @Autowired
    private BaseRedisDao<String, Object> redisDao;

    public Media addTVMedia(String tvId, RestTemplate restTemplate) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat(" yyyy-MM-dd HH:mm:ss ");
        Date now = new Date();

        Connection connect = Jsoup.connect("https://v.qq.com/detail/n/"+ tvId +".html")
                .ignoreHttpErrors(true)
                .ignoreContentType(true);
        Document document = connect.data("query", "Java")
                .userAgent("Mozilla")
                .cookie("auth", "token")
                .timeout(5000)
                .get();
        String mediaInfo = document.getElementsByAttributeValue("r-component", "p-index").attr("r-props");
        Map rProps = (Map) JSON.parse(mediaInfo);
        String name = (String) rProps.get("title");
        String vpic = (String) rProps.get("vpic");
        String aliasName;
        String area;
        String language;
        Integer totalSerial = 0;
        String year;

        Elements typeItems = document.getElementsByClass("type_item");
        for(Element element : typeItems){
            String typeTit = element.select("span.type_tit").text().replace(" ","");
            String typeTxt = element.select("span.type_txt").text();
            if(typeTit.startsWith("别名")){
                aliasName = typeTxt;
            }else if(typeTit.startsWith("地区")){
                area = typeTxt;
            }else if(typeTit.startsWith("语言")){
                language = typeTxt;
            }else if(typeTit.startsWith("总集数")){
                totalSerial = Integer.valueOf(typeTxt);
            }else if(typeTit.startsWith("出品时间")){
                year = typeTxt;
            }
        }
        MediaRepository mediaRepository = Media.getMediaRepository();
        Example example = new Example(Media.class);
        example.createCriteria().andEqualTo("name", name)
                .andEqualTo("pkgType", tvpkgType)
                .andEqualTo("type", "packages");
        List<Media> packageMediaList = mediaRepository.selectByExample(example);
        if(packageMediaList.size() > 1){
            logger.info(name + "  媒资包有多个");
            return null;
        }else if(packageMediaList.size() == 1){
            Media packageMedia = packageMediaList.get(0);
            Map params = new HashMap();
            params.put("mediaId", packageMedia.getId());
            params.put("resourceType", sourceType);
            Integer maxSerial = mediaRepository.selectMaxSerialByPackageMediaId(params);
            if(totalSerial.intValue() == maxSerial){
                logger.info(name + " 电视剧已经集全");
                return null;
            }else if(totalSerial.intValue() < maxSerial){
                logger.info(name + " 怕是遇到鬼了");
                return null;
            }else{
                packageMedia.setSerial(maxSerial);
                return packageMedia;
            }
        }else{
            Long startTime = System.currentTimeMillis();
            String scoreValue = document.select(".video_score .score").text();
            Integer score = new BigDecimal(scoreValue).multiply(new BigDecimal(10)).intValue();
            //添加媒资或媒资包
            //获取演员信息
            String directors = "";
            String actors = "";

            Element actorListUl = document.select(".actor_list ").first();
            Elements actorListLi = actorListUl.select("li.item");



            Map actorInfos = (Map) data.get("cast");
            List<Map> directorsMaps = (List<Map>) actorInfos.get("directors");
            for(Map director : directorsMaps){
                directors += director.get("name") + ",";
            }
            List<Map> actorsMaps = (List<Map>) actorInfos.get("mainActors");
            for(Map actor : actorsMaps){
                actors += actor.get("name") + ",";
            }
            if(!StringUtils.isEmpty(directors)){
                directors = directors.substring(0, directors.length() - 1);
            }
            if(!StringUtils.isEmpty(actors)){
                actors = actors.substring(0, actors.length() - 1);
            }
            Long issueTime = (Long) data.get("publishTime");
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy");
            String year = null;
            if(issueTime != null){
                Date issueDate = new Date(issueTime);
                year = sdf1.format(issueDate);
            }
            String description = (String) data.get("description");

            Media media = new Media();
            media.setParentId(0);
            media.setPkgType(tvpkgType);
            media.setCreateDate(sdf.format(now));
            media.setType(MediaPackageType.TYPE_PACKAGES.getIndex());
            media.setTerminal("pc+app");
            media.setState("1");
            media.setPlayCount(0L);
            media.setSearchCount(0);

            media.setName(name);
            media.setDoubanScore(score);
            media.setDirector(directors);
            media.setStarring(actors);
            media.setTotal(videoCount);
            media.setUpsaleDate(year);
            media.setIntroduce(description);
            media.setDetail(description);
            media.save();
            media.setSerial(0);

            Integer packageMediaId = media.getId();

            //save poster
            Poster poster = new Poster();
            String posterSrc = (String) data.get("vpic");
            posterSrc = DownloadImage.download(posterSrc);//下载媒资包图片

            poster.setMediaId(packageMediaId);
            poster.setType("title");
            poster.setTitle(name);
            poster.setMiddle(IMGURL + posterSrc);
            poster.setCreateDate(sdf.format(now));
            poster.save();

            //save actors
            for(Map director : directorsMaps){
                addActorInfo(director, "director", packageMediaId);
            }
            for(Map actor : actorsMaps){
                addActorInfo(actor, "actor", packageMediaId);
            }
            MediaRefCategory mediaRefCategory = new MediaRefCategory();
            Map params = new HashMap();
            List<Map> categorysList = (List<Map>) data.get("categories");
            MediaCategory mediaCategory = MediaCategory.getMediaCategoryRepository().selectCategoryByType(tvpkgType);
            if(categorysList.size() > 0){
                List<CategoryProperty> categoryPropertyList = CategoryProperty.getCategoryPropertyRepository().selectByCategoryId(mediaCategory.getId());
                for(CategoryProperty categoryProperty : categoryPropertyList){
//                    CategoryProperty categoryProperty = CategoryProperty.getCategoryPropertyRepository().selectTypeByCategoryId(mediaCategory.getId());
                    for(Map category : categorysList){
                        params.clear();
                        String categoryName = (String) category.get("name");
                        if(categoryName.endsWith("剧") && !categoryName.equals("喜剧")){
                            categoryName = categoryName.substring(0, categoryName.length()-1);
                        }
                        params.put("id", categoryProperty.getId());
                        params.put("params", categoryName);
                        CategoryPropertyValue categoryPropertyValue = CategoryPropertyValue.getCategoryPropertyValueRepository().selectPropertyValueById(params);
                        if (null != categoryPropertyValue) {
                            mediaRefCategory.setMediaId(packageMediaId);
                            mediaRefCategory.setCategoryId(mediaCategory.getId());
                            mediaRefCategory.setPropertyId(categoryProperty.getId());
                            mediaRefCategory.setProperty(categoryProperty.getName());
                            mediaRefCategory.setPropertyValueId(categoryPropertyValue.getId());
                            mediaRefCategory.save();
                        }
                    }
                }

            }
            if(year != null){
                CategoryProperty yearCategoryProperty = CategoryProperty.getCategoryPropertyRepository().selectYearByCategoryId(mediaCategory.getId());
                mediaRefCategory.setMediaId(packageMediaId);
                mediaRefCategory.setCategoryId(mediaCategory.getId());
                mediaRefCategory.setPropertyId(yearCategoryProperty.getId());
                mediaRefCategory.setProperty(yearCategoryProperty.getName());
                params.clear();
                params.put("id", yearCategoryProperty.getId());
                params.put("params", year);
                CategoryPropertyValue categoryPropertyValue1 = CategoryPropertyValue.getCategoryPropertyValueRepository().selectPropertyValueById(params);
                if(categoryPropertyValue1 == null){
                    params.clear();
                    params.put("id", yearCategoryProperty.getId());
                    params.put("params", "更早");
                    categoryPropertyValue1 = CategoryPropertyValue.getCategoryPropertyValueRepository().selectPropertyValueById(params);
                    mediaRefCategory.setPropertyValueId(categoryPropertyValue1.getId());
                }else{
                    mediaRefCategory.setPropertyValueId(categoryPropertyValue1.getId());
                }
                mediaRefCategory.save();
            }

            Media media2 = Media.getMediaRepository().getMediaDetail(packageMediaId);
            EsController.addEsDate(media2);
            Long endTime = System.currentTimeMillis();
            logger.info(SourceType.TYPE_IQIYI.getName() + " " + name + "  媒资包创建结束, hs：" + (endTime - startTime));
            return media;
        }

    }
}
