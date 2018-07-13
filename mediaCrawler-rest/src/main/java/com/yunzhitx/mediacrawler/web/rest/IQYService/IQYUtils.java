package com.yunzhitx.mediacrawler.web.rest.IQYService;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.yunzhitx.mediacrawler.core.categoryproperty.domain.CategoryProperty;
import com.yunzhitx.mediacrawler.core.categorypropertyvalue.domain.CategoryPropertyValue;
import com.yunzhitx.mediacrawler.core.media.domain.Media;
import com.yunzhitx.mediacrawler.core.media.infra.MediaRepository;
import com.yunzhitx.mediacrawler.core.mediacategory.domain.MediaCategory;
import com.yunzhitx.mediacrawler.core.mediarefcategory.domain.MediaRefCategory;
import com.yunzhitx.mediacrawler.core.performer.domain.Performer;
import com.yunzhitx.mediacrawler.core.poster.domain.Poster;
import com.yunzhitx.mediacrawler.core.refmediaperformer.domain.RefMediaPerformer;
import com.yunzhitx.mediacrawler.core.resource.domain.Resource;
import com.yunzhitx.mediacrawler.web.rest.elasticsearch.EsController;
import com.yunzhitx.mediacrawler.web.rest.enumer.MediaPackageType;
import com.yunzhitx.mediacrawler.web.rest.enumer.MediaType;
import com.yunzhitx.mediacrawler.web.rest.enumer.SourceType;
import com.yunzhitx.mediacrawler.web.util.DownloadActorImg;
import com.yunzhitx.mediacrawler.web.util.DownloadImage;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.util.StringUtil;

import java.io.IOException;
import java.math.BigDecimal;
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
 * @date 2018/6/1$ 15:07$
 */
public class IQYUtils {

    private static final Logger logger = LoggerFactory.getLogger(IQYUtils.class);

    private static volatile Integer count = 0;

    private static Integer sourceType = SourceType.TYPE_IQIYI.getIndex(); //爱奇艺视频id
    private static final String fieldName = SourceType.TYPE_IQIYI.getName();
    private static final String pkgType = MediaType.TYPE_FILM.getName();
    private static final String tvpkgType = MediaType.TYPE_TV.getName();
    private static final String IMGURL = "http://juhe.fs.cdtown.cn/media/";
    private static final String tenantFlag = "yztx";
    private static final Pattern itemPattern = Pattern.compile("(?<=v_)(.+?)(?=.html)");


    public static void addMedia(Map<String, Object> infoMap) throws Exception {
        if(infoMap == null){
            return;
        }
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        MediaRepository mediaRepository = Media.getMediaRepository();
        String mediaName = (String) infoMap.get("albumName");
        logger.info(SourceType.TYPE_IQIYI.getName() + " " + mediaName + "  开始");
        Long startTime = System.currentTimeMillis();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("fd_name",mediaName);
        params.put("fd_pkgType",pkgType);
        params.put("fd_type", MediaPackageType.TYPE_PACKAGES.getIndex());
        Integer packageMediaId = mediaRepository.selectMediaIdIfMediaPackageExsit(params);
        if(packageMediaId != null){
            params.clear();
            params.put("fd_resourceTypeId", sourceType);
            params.put("packageMediaId", packageMediaId);
            Integer mediaExsit = mediaRepository.selectMediaResoureExsit(params);
            if(mediaExsit == 1){
                logger.info("名称：" + mediaName + ", 来源：" + SourceType.TYPE_IQIYI.getName() + "  已存在");
                return;
            }
        }
        //添加媒资或媒资包
        //获取演员信息
        String directors = "";
        String actors = "";
        Map actorInfos = (Map) infoMap.get("cast");
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

        BigDecimal scoreValue = (BigDecimal) infoMap.get("score");
        Integer score = scoreValue.multiply(new BigDecimal(10)).intValue();

        Long issueTime = (Long) infoMap.get("issueTime");
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy");
        String year = null;
        if(issueTime != null){
            Date issueDate = new Date(issueTime);
            year = sdf1.format(issueDate);
        }

        String intro = (String) infoMap.get("description");
        Media media = new Media();
        media.setName(mediaName);
        media.setPkgType(pkgType);
        media.setTerminal("pc+app");
        media.setStarring(actors);
        media.setDirector(directors);
        media.setDoubanScore(score);
        media.setState(1 + "");
        media.setTotal(1);
        media.setSerial(1);
        media.setUpsaleDate(year);
        media.setIntroduce(intro);
        media.setDirector(directors);
        media.setStarring(actors);
        media.setDetail(intro);
        media.setCreateDate(sdf.format(now));
        media.setPlayCount(0L);
        if(packageMediaId == null){
            media.setParentId(0);
            media.setType(MediaPackageType.TYPE_PACKAGES.getIndex());
            media.save();
            packageMediaId = media.getId();
        }
        media.setId(null);
        media.setType(MediaPackageType.TYPE_MEDIA.getIndex());
        media.setParentId(packageMediaId);
        media.save();

        //save poster
        Poster poster = new Poster();
        String posterSrc = (String) infoMap.get("imageUrl");
        posterSrc = DownloadImage.download(posterSrc);//下载媒资包图片

        poster.setMediaId(packageMediaId);
        poster.setType("title");
        poster.setTitle(mediaName);
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
        //save resources
        String playSrc = (String) infoMap.get("url");//电影播放路径
        String itemId = getItemIdByFilmPlayUrl(playSrc,itemPattern);

        Resource resource = new Resource();
        resource.setMediaId(media.getId());
        resource.setResourceTypeId(sourceType);
        resource.setThirdId(itemId);
        resource.setFieldName(fieldName);
        resource.setType("main");
        resource.setPlayType("third");
        resource.setName(mediaName);
        resource.setPlayUrl(playSrc);
        resource.setState(1 + "");
        resource.setKeyword(mediaName + " " + actors + " " + directors);
        resource.setTerminal("pc+app");
        resource.setPlayCount(0);
        resource.setCreateDate(sdf.format(now));
        resource.save();
        //save category
        List<Map> categorysList = (List<Map>) infoMap.get("categories");
        MediaRefCategory mediaRefCategory = new MediaRefCategory();
        MediaCategory mediaCategory = MediaCategory.getMediaCategoryRepository().selectCategoryByType(pkgType);
        if(categorysList.size() > 0){
            CategoryProperty categoryProperty = CategoryProperty.getCategoryPropertyRepository().selectTypeByCategoryId(mediaCategory.getId());
            for(Map category : categorysList){
                params.clear();
                String categoryName = (String) category.get("name");
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
        logger.info(SourceType.TYPE_IQIYI.getName() + " " + mediaName + "  结束, hs：" + (endTime - startTime));
    }

    private static void addActorInfo(Map actor, String actorType, Integer packageMediaId) throws Exception {
        Performer performer = new Performer();
        String actorName = (String) actor.get("name");
        Integer performerIdIfExsit = performer.selectActorCountByActorName(actorName);
        if(performerIdIfExsit == null){
            Integer roleId = (Integer) actor.get("id");
            Connection connect = Jsoup.connect("http://www.iqiyi.com/lib/s_"+ roleId +".html")
                                      .ignoreHttpErrors(true)
                                      .ignoreContentType(true);
            Document document = connect.data("query", "Java")
                    .userAgent("Mozilla")
                    .cookie("auth", "token")
                    .timeout(5000)
                    .get();
            performer.setName(actorName);
            String actorPic = (String) actor.get("imageUrl");
            if(StringUtil.isNotEmpty(actorPic)){
                actorPic = DownloadActorImg.download(actorPic);//下载演员图片
            }
            performer.setHeaderImg(actorPic);
            String job = document.select("li[itemprop=jobTitle]").text().trim().toString();
            if (job.length() > 4) {
                job = job.split("职业：")[1].trim();
                performer.setJob(job);
            }
            try {
                String birthday = document.select("li[itemprop=birthdate]").text().split("生日：")[1];
                if (!birthday.equalsIgnoreCase(" -")) {
                    performer.setBirth(birthday);
                }
            }catch (Exception e){
                System.out.println(actorName + "id: " + roleId);
            }

            String actorIntro = document.select("p[class=introduce-info]").text();
            performer.setIntroduce(actorIntro);
            performer.setState(1 + "");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            performer.setCreateDate(sdf.format(new Date()));
            try {
                performer.save();
            }catch (Exception e){
                System.out.println("该演员已存在");
                performerIdIfExsit = performer.selectActorCountByActorName(actorName);
            }
            performerIdIfExsit = performer.getId();
        }


        RefMediaPerformer refMediaPerformer = new RefMediaPerformer();
        refMediaPerformer.setMediaId(packageMediaId);
        refMediaPerformer.setPerformerId(performerIdIfExsit);
        if(actorType.equals("director")){
            refMediaPerformer.setType("director");
        }else{
            refMediaPerformer.setType("starring");
        }
        refMediaPerformer.save();
    }

    private static String getItemIdByFilmPlayUrl(String sourceString, Pattern pattern) {
        String targetString = null;
        Matcher matcher = pattern.matcher(sourceString);
        while(matcher.find()){
            targetString = matcher.group();
        }
        return targetString;
    }

    public static void main (String[] args) throws IOException {
//        for (int i = 0; i < 50; i++){
//            Connection connect = Jsoup.connect("http://www.iqiyi.com/lib/s_228795405.html");
//            Document document = connect.data("query", "Java")
//                    .userAgent("Mozilla")
//                    .cookie("auth", "token")
//                    .timeout(3000)
//                    .get();
//            String job = document.select("li[itemprop=jobTitle]").text().trim().toString();
//            System.out.println(job);
//        }

    }

    public static void addTVMedia(String albumId, RestTemplate restTemplate) throws Exception {
        Map param = new HashMap();
        param.put("albumId", albumId);
        String packageResponse = restTemplate.getForObject("http://pcw-api.iqiyi.com/video/video/videoinfowithuser/{albumId}", String.class, param);
        Map tvInfoMap = JSON.parseObject(packageResponse);
        Media packageMedia = dealTVPageInfo(tvInfoMap);
        if(packageMedia == null){
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat(" yyyy-MM-dd HH:mm:ss ");
        Map data = (Map) tvInfoMap.get("data");
        Integer latestOrder = (Integer) data.get("latestOrder");
        param.put("page", 1);
        param.put("size", latestOrder);
        addSubMedia(param, restTemplate, data, packageMedia, sdf);
        packageMedia.setUpdateDate(sdf.format(new Date()));
        packageMedia.setSerial(latestOrder);
        Media.getMediaRepository().updateByPrimaryKeySelective(packageMedia);

        /**
         * 修改搜索引擎中的信息
         */
        Integer id = packageMedia.getId();
        Media media2 = Media.getMediaRepository().getMediaDetail(id);
        EsController.addEsDate(media2);
        System.out.println("====================！！已更新搜索引擎信息！！========================");


    }

    private static void addSubMedia(Map param, RestTemplate restTemplate, Map data, Media packageMedia, SimpleDateFormat sdf) {
        String response = restTemplate.getForObject("https://mixer.video.iqiyi.com/jp/mixin/videos/avlist?albumId={albumId}&page={page}&size={size}", String.class, param);
        String infoMapString = response.split("tvInfoJs=")[1];
        Map infoMap = JSON.parseObject(infoMapString);
        if(infoMap == null){
            return;
        }

        List<Map> mixinVideos = (List<Map>) infoMap.get("mixinVideos");
        Date now = new Date();
        for(Map video : mixinVideos){
            Media subMedia = new Media();
            MediaRepository mediaRepository = Media.getMediaRepository();
            String mediaName = (String) video.get("name");
            Integer timeLength = (Integer) video.get("duration");
            String playUrl = (String) video.get("url");
            Boolean flag = checkSubMediaExsit(playUrl);
            if(flag){
                continue;
            }
            String itemId = playUrl.split("/v_")[1];
            itemId = itemId.split(".html")[0];
            String shortTitle = (String) video.get("shortTitle");
            Integer order = (Integer) video.get("order");
            String desc = (String) video.get("description");
            String subtitle = (String) video.get("subtitle");
            Object scoreValue = (Object) data.get("score");
            Integer score = new BigDecimal(scoreValue.toString()).multiply(new BigDecimal(10)).intValue();

            subMedia.setIntroduce(subtitle);
            subMedia.setDetail(desc);
            subMedia.setTerminal("pc+app");
            subMedia.setDoubanScore(score);

            subMedia.setParentId(packageMedia.getId());
            subMedia.setPkgType(tvpkgType);
            if (shortTitle.contains("预告")) {
                subMedia.setState("1");
                subMedia.setType("trailer");
            } else if (shortTitle.contains("花絮")) {
                subMedia.setState("1");
                subMedia.setType("tidbits");
            } else {
                subMedia.setState("1");
                subMedia.setType("main");
            }
            subMedia.setPlayLength(timeLength);
            subMedia.setName(mediaName);
            subMedia.setSerial(order);
            //set default
            subMedia.setPlayCount(0L);
            subMedia.setSearchCount(0);
            subMedia.setCreateDate(sdf.format(now));
            subMedia.setUpdateDate(sdf.format(now));
            subMedia.saveMedia(subMedia);
            Integer subVideoId = subMedia.getId();

            Resource resource = new Resource();
            resource.setMediaId(subVideoId);
            resource.setResourceTypeId(sourceType);
            resource.setThirdId(itemId);
            resource.setFieldName(fieldName);
            if (shortTitle.contains("预告")) {
                resource.setState(0 + "");
                resource.setType("trailer");
            } else if (shortTitle.contains("花絮")) {
                resource.setState(0 + "");
                resource.setType("tidbits");
            } else {
                resource.setState(1 + "");
                resource.setType("main");
            }
            resource.setPlayType("third");
            resource.setSerial(order);
            resource.setName(mediaName);
            resource.setPlayUrl(playUrl);
            resource.setKeyword(mediaName + " " + packageMedia.getStarring() + " " + packageMedia.getDirector());
            resource.setTerminal("pc+app");
            resource.setPlayCount(0);
            resource.setCreateDate(sdf.format(new Date()));
            resource.save();
        }
    }

    private static Boolean checkSubMediaExsit(String playUrl) {
        Example example = new Example(Resource.class);
        example.createCriteria().andEqualTo("playUrl", playUrl);
        Integer count = Resource.getResourceRepository().selectCountByExample(example);
        return count>0;
    }

    private static Media dealTVPageInfo(Map tvInfoMap) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat(" yyyy-MM-dd HH:mm:ss ");
        Date now = new Date();
        Map data = (Map) tvInfoMap.get("data");
        String name = (String) data.get("name");
        Integer videoCount = (Integer) data.get("videoCount");
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
            if(videoCount.intValue() == maxSerial){
                logger.info(name + " 电视剧已经集全");
                return null;
            }else if(videoCount.intValue() < maxSerial){
                logger.info(name + " 怕是遇到鬼了");
                return null;
            }else{
                packageMedia.setSerial(maxSerial);
                return packageMedia;
            }
        }else{
            Long startTime = System.currentTimeMillis();
            Object scoreValue = (Object) data.get("score");
            Integer score = new BigDecimal(scoreValue.toString()).multiply(new BigDecimal(10)).intValue();
            //添加媒资或媒资包
            //获取演员信息
            String directors = "";
            String actors = "";
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
            String posterSrc = (String) data.get("imageUrl");
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
