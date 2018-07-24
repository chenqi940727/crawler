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
import com.yunzhitx.mediacrawler.web.rest.redis.BaseRedisDao;
import com.yunzhitx.mediacrawler.web.util.DownloadActorImg;
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
public class TXUtils {

    private static final Logger logger = LoggerFactory.getLogger(TXUtils.class);

    private static volatile Integer count = 0;

    private static Integer sourceType = SourceType.TYPE_TENXUN.getIndex(); //腾讯视频id
    private static final String fieldName = SourceType.TYPE_TENXUN.getName();
    private static final String tvpkgType = MediaType.TYPE_TV.getName();
    private static final String filmpkgType = MediaType.TYPE_FILM.getName();
    private static final String animepkgType = MediaType.TYPE_ANIMATION.getName();
    private static final String varietypkgType = MediaType.TYPE_VARIETY.getName();
    private static final String IMGURL = "http://juhe.fs.cdtown.cn/media/";
    private static final String tenantFlag = "yztx";



    @Autowired
    private BaseRedisDao<String, Object> redisDao;


    //电视剧
    public static void addTVMedia(String tvId, RestTemplate restTemplate) throws Exception {
        Media packageMedia = addPackageMedia(tvId);
        if(packageMedia == null){
            return;
        }
        Integer total = packageMedia.getTotal();
        Integer serial = packageMedia.getSerial();
        if(serial == 0 || serial < total){

        }else{
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat(" yyyy-MM-dd HH:mm:ss ");
        Map param = new HashMap();
        param.put("range", serial + "-" + total);
        param.put("videoId", tvId);
        Integer latestOrder = addSubMedia(param, restTemplate, packageMedia, sdf);
        if(latestOrder.equals(serial) || latestOrder == 0){
            return;
        }
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

    private static Integer addSubMedia(Map param, RestTemplate restTemplate, Media packageMedia, SimpleDateFormat sdf) {
        Integer serial = 0;
        String response = restTemplate.getForObject("https://s.video.qq.com/get_playsource?id={videoId}&type=4&range={range}&otype=json", String.class, param);
        String infoMapString = response.split("QZOutputJson=")[1];
        infoMapString = infoMapString.substring(0,infoMapString.length()-1);
        Map infoMap = JSON.parseObject(infoMapString);
        if(infoMap == null){
            return 0;
        }

        Map videoPlayList = (Map) infoMap.get("PlaylistItem");
        List<Map> videos = (List<Map>) videoPlayList.get("videoPlayList");
        Date now = new Date();
        for(Map video : videos){
            String type = (String) video.get("type");
            if(!"1".equals(type)){
                logger.info("不是正片，跳过");
                continue;
            }
            Media subMedia = new Media();
            MediaRepository mediaRepository = Media.getMediaRepository();
            String mediaName = (String) video.get("title");
            String playUrl = (String) video.get("playUrl");
            Boolean flag = checkSubMediaExsit(playUrl, mediaName);
            if(flag){
                continue;
            }
            String itemId = (String) video.get("id");
            String orderStr = (String) video.get("episode_number");
            Integer order = Integer.valueOf(orderStr);
            subMedia.setTerminal("pc+app");

            subMedia.setParentId(packageMedia.getId());
            subMedia.setPkgType(packageMedia.getPkgType());
            subMedia.setState("1");
            subMedia.setType("main");
            subMedia.setName(mediaName);
            subMedia.setSerial(order);
            //set default
            subMedia.setPlayCount(0L);
            subMedia.setSearchCount(0);
            subMedia.setCreateDate(sdf.format(now));
            subMedia.setUpdateDate(sdf.format(now));
            subMedia.saveMedia(subMedia);
            serial = order;
            Integer subVideoId = subMedia.getId();

            Resource resource = new Resource();
            resource.setMediaId(subVideoId);
            resource.setResourceTypeId(sourceType);
            resource.setThirdId(itemId);
            resource.setFieldName(fieldName);
            resource.setState("1");
            resource.setType("main");
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
        return serial;
    }

    private static Boolean checkSubMediaExsit(String playUrl, String mediaName) {
        Example example = new Example(Resource.class);
        example.createCriteria().andEqualTo("playUrl", playUrl)
                                .andEqualTo("name", mediaName);
        Integer count = Resource.getResourceRepository().selectCountByExample(example);
        return count>0;
    }

    private static Media addPackageMedia(String tvId) throws Exception {
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
        mediaInfo = replaceBlank(mediaInfo);
        mediaInfo = mediaInfo.replace(";",",").replace("'","");
        mediaInfo = mediaInfo.replace("http:","http");
        mediaInfo = mediaInfo.replaceAll("(\\{|,)([^:]+)", "$1\"$2\"").replaceAll("([^:,\\}]+)(\\}|,)", "\"$1\"$2");
        mediaInfo = mediaInfo.replace("http","http:");
        Map rProps = (Map) JSON.parse(mediaInfo);
        String name = (String) rProps.get("title");
        String vpic = (String) rProps.get("vpic");
        String aliasName;
        String area;
        String language;
        Integer totalSerial = 0;
        String year = null;

        Elements typeItems = document.getElementsByClass("type_item");
        for(Element element : typeItems){
            String typeTit = element.select("span.type_tit").text().replace(" ","").replace("　","");
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
            }/*else if(totalSerial.intValue() < maxSerial){
                logger.info(name + " 怕是遇到鬼了");
                return null;
            }*/else{
                packageMedia.setSerial(maxSerial);
                return packageMedia;
            }
        }else{
            Long startTime = System.currentTimeMillis();
            String scoreValue = document.select(".video_score .score_db .score").text();
            Integer score = null;
            if(StringUtil.isNotEmpty(scoreValue)){
                score = new BigDecimal(scoreValue).multiply(new BigDecimal(10)).intValue();
            }
            //添加媒资或媒资包
            //获取演员信息
            String directorNames = "";
            String actorNames = "";

            Element actorListUl = document.select(".actor_list").first();
            Elements actorListLi = actorListUl.select("li.item");
            for(Element actor : actorListLi){
                if(actor.hasClass("item_more")){
                    continue;
                }
                Element actorInfo = actor.select("div.actor_info").first();
                String actorName = actorInfo.select(".actor_detail .actor_name").text();
                Element directorFlag = actor.select("span.director").first();
                if(directorFlag != null){
                    directorNames += actorName + ",";
                }else{
                    actorNames += actorName + ",";
                }
            }
            if(!StringUtils.isEmpty(directorNames)){
                directorNames = directorNames.substring(0, directorNames.length() - 1);
            }
            if(!StringUtils.isEmpty(actorNames)){
                actorNames = actorNames.substring(0, actorNames.length() - 1);
            }


            String description = document.select(".video_desc .desc_txt span").text();


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
            media.setDirector(directorNames);
            media.setStarring(actorNames);
            if(totalSerial == 0){
                logger.info(name);
            }
            media.setTotal(totalSerial);
            media.setUpsaleDate(year);
            media.setIntroduce(description);
            media.setDetail(description);
            media.save();
            media.setSerial(0);

            Integer packageMediaId = media.getId();

            //save poster
            Poster poster = new Poster();
            vpic = DownloadImage.download(vpic);//下载媒资包图片

            poster.setMediaId(packageMediaId);
            poster.setType("title");
            poster.setTitle(name);
            poster.setMiddle(IMGURL + vpic);
            poster.setCreateDate(sdf.format(now));
            poster.save();

            //save actors
            for(Element actor : actorListLi){
                if(actor.hasClass("item_more")){
                    continue;
                }
                String actorId = actor.attr("data-id");
                Element actorInfo = actor.select("div.actor_info").first();
                String actorName = actorInfo.select(".actor_detail .actor_name").text();
                Element directorFlag = actor.select("span.director").first();
                if(directorFlag != null){
                    addActorInfo(actorId,"director",packageMediaId, actorName);
                }else{
                    addActorInfo(actorId,"actor",packageMediaId, actorName);
                }
            }

            Elements videoTags = document.select(".video_tag .tag_list a.tag");
            MediaRefCategory mediaRefCategory = new MediaRefCategory();
            Map params = new HashMap();
            MediaCategory mediaCategory = MediaCategory.getMediaCategoryRepository().selectCategoryByType(tvpkgType);
            if(videoTags.size() > 0){
                List<CategoryProperty> categoryPropertyList = CategoryProperty.getCategoryPropertyRepository().selectByCategoryId(mediaCategory.getId());
                for(CategoryProperty categoryProperty : categoryPropertyList){
                    for(Element ele : videoTags){
                        params.clear();
                        String categoryName = ele.text();
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
            logger.info(SourceType.TYPE_TENXUN.getName() + " " + name + "  媒资包创建结束, hs：" + (endTime - startTime));
            return media;
        }
    }


    private static void addActorInfo(String actorId, String actorType, Integer packageMediaId, String actorName) throws Exception {
        Performer performer = new Performer();
        Integer performerIdIfExsit = performer.selectActorCountByActorName(actorName);
        if (performerIdIfExsit == null) {
            Connection connect = Jsoup.connect("http://v.qq.com/x/star/" + actorId + "?tabid=2.html")
                    .ignoreHttpErrors(true)
                    .ignoreContentType(true);
            Document document = connect.data("query", "Java")
                    .userAgent("Mozilla")
                    .cookie("auth", "token")
                    .timeout(5000)
                    .get();
            performer.setName(actorName);

            String actorPic = document.select(".star_pic img").attr("src");
            if (StringUtil.isNotEmpty(actorPic)) {
                if(!actorPic.startsWith("http:")){
                    actorPic = "http:" + actorPic;
                }
                actorPic = DownloadActorImg.download(actorPic);//下载演员图片
            }
            performer.setHeaderImg(actorPic);

            Element wikiInfo = document.select("#baikeInfo").first();
            if(wikiInfo == null){
                return;
            }
            String intro = wikiInfo.select(".wiki_content").text();
            performer.setIntroduce(intro);
            Elements infoList = wikiInfo.select(".wiki_info .line");
            for (Element ele : infoList) {
                String lable = ele.select(".lable").text().replace(" ", "");
                String content = ele.select(".content").text();
                if (lable.startsWith("职业")) {
                    performer.setJob(content);
                }
                if (lable.startsWith("出生日期")) {
                    performer.setBirth(replaceZhongWen(content));
                }
                if (lable.startsWith("出生地")) {
                    performer.setBirthPlace(content);
                }
            }
            performer.setState("1");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            performer.setCreateDate(sdf.format(new Date()));
            try {
                performer.save();
            } catch (Exception e) {
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

    public static String replaceBlank(String str) {
        String dest = "";
        if (str!=null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }

    public static String replaceZhongWen(String str){
        String reg = "[\u4e00-\u9fa5]";
        Pattern pat = Pattern.compile(reg);

        Matcher mat=pat.matcher(str);
        String repickStr = mat.replaceAll("-");
        return repickStr.substring(0,repickStr.length() - 1);
    }

    //电影
    public static void addFilmMedia(String tvId, RestTemplate restTemplate) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat(" yyyy-MM-dd HH:mm:ss ");
        Media packageMedia = addFilmPackageMedia(tvId);
        if(packageMedia == null){
            return;
        }
        Boolean flag = addSubFilmMedia(tvId, restTemplate, packageMedia);
        if(flag){
            packageMedia.setUpdateDate(sdf.format(new Date()));
            Media.getMediaRepository().updateByPrimaryKeySelective(packageMedia);

            /**
             * 修改搜索引擎中的信息
             */
            Integer id = packageMedia.getId();
            Media media2 = Media.getMediaRepository().getMediaDetail(id);
            EsController.addEsDate(media2);
            System.out.println("====================！！已更新搜索引擎信息！！========================");
        }
    }

    private static Boolean addSubFilmMedia(String tvId, RestTemplate restTemplate, Media packageMedia) {
        Boolean updateFlag = false;
        SimpleDateFormat sdf = new SimpleDateFormat(" yyyy-MM-dd HH:mm:ss ");
        Date now = new Date();
        Map param = new HashMap();
        param.put("videoId", tvId);
        String response = restTemplate.getForObject("https://s.video.qq.com/get_playsource?id={videoId}&type=4&range=1-10&otype=json", String.class, param);
        String infoMapString = response.split("QZOutputJson=")[1];
        infoMapString = infoMapString.substring(0,infoMapString.length()-1);
        Map infoMap = JSON.parseObject(infoMapString);
        if(infoMap == null){
            return false;
        }

        Map videoPlayList = (Map) infoMap.get("PlaylistItem");
        if(videoPlayList == null){
            return false;
        }
        List<Map> videos = (List<Map>) videoPlayList.get("videoPlayList");
        if(videos == null || videos.size() == 0){
            return false;
        }
        for(Map video : videos){
            String type = (String) video.get("type");
            if(!"1".equals(type)){
                logger.info("不是正片，跳过");
                continue;
            }
            Media subMedia = new Media();
            MediaRepository mediaRepository = Media.getMediaRepository();
            String mediaName = (String) video.get("title");
//            Integer timeLength = (Integer) video.get("duration");
            String playUrl = (String) video.get("playUrl");
            Boolean flag = checkSubMediaExsit(playUrl,mediaName);
            if(flag){
                continue;
            }
            String itemId = (String) video.get("id");
//            String shortTitle = (String) video.get("shortTitle");
            String orderStr = (String) video.get("episode_number");
            Integer order = Integer.valueOf(orderStr);
//            String desc = (String) video.get("description");
//            String subtitle = (String) video.get("subtitle");
//            Object scoreValue = (Object) data.get("score");
//            Integer score = new BigDecimal(scoreValue.toString()).multiply(new BigDecimal(10)).intValue();

//            subMedia.setIntroduce(subtitle);
//            subMedia.setDetail(desc);
            subMedia.setTerminal("pc+app");
//            subMedia.setDoubanScore(score);

            subMedia.setParentId(packageMedia.getId());
            subMedia.setPkgType(filmpkgType);
            subMedia.setState("1");
            subMedia.setType("main");
//            subMedia.setPlayLength(timeLength);
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
            resource.setState("1");
            resource.setType("main");
            resource.setPlayType("third");
            resource.setSerial(1);
            resource.setName(mediaName);
            resource.setPlayUrl(playUrl);
            resource.setKeyword(mediaName + " " + packageMedia.getStarring() + " " + packageMedia.getDirector());
            resource.setTerminal("pc+app");
            resource.setPlayCount(0);
            resource.setCreateDate(sdf.format(new Date()));
            resource.save();
            updateFlag = true;
        }
        return updateFlag;
    }

    private static Media addFilmPackageMedia(String tvId) throws Exception {
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
        mediaInfo = replaceBlank(mediaInfo);
        mediaInfo = mediaInfo.replace(";",",").replace("'","");
        mediaInfo = mediaInfo.replace("http:","http");
        mediaInfo = mediaInfo.replaceAll("(\\{|,)([^:]+)", "$1\"$2\"").replaceAll("([^:,\\}]+)(\\}|,)", "\"$1\"$2");
        mediaInfo = mediaInfo.replace("http","http:");
        Map rProps = (Map) JSON.parse(mediaInfo);
        String name = (String) rProps.get("title");
        String vpic = (String) rProps.get("vpic");
        String aliasName;
        String area;
        String language;
        Integer totalSerial = 0;
        String year = null;

        Elements typeItems = document.getElementsByClass("type_item");
        for(Element element : typeItems){
            String typeTit = element.select("span.type_tit").text().replace(" ","").replace("　","");
            String typeTxt = element.select("span.type_txt").text();
            if(typeTit.startsWith("别名")){
                aliasName = typeTxt;
            }else if(typeTit.startsWith("地区")){
                area = typeTxt;
            }else if(typeTit.startsWith("语言")){
                language = typeTxt;
            }else if(typeTit.startsWith("上映时间")){
                year = typeTxt.substring(0,4);
            }
        }
        MediaRepository mediaRepository = Media.getMediaRepository();
        Example example = new Example(Media.class);
        example.createCriteria().andEqualTo("name", name)
                .andEqualTo("pkgType", filmpkgType)
                .andEqualTo("type", "packages");
        List<Media> packageMediaList = mediaRepository.selectByExample(example);
        if(packageMediaList.size() > 1){
            logger.info(name + "  媒资包有多个");
            return null;
        }else if(packageMediaList.size() == 1){
            Media packageMedia = packageMediaList.get(0);
            return packageMedia;
        }else{
            Long startTime = System.currentTimeMillis();
            String scoreValue = document.select(".video_score .score_db .score").text();
            Integer score = null;
            if(StringUtil.isNotEmpty(scoreValue)){
                score = new BigDecimal(scoreValue).multiply(new BigDecimal(10)).intValue();
            }
            //添加媒资或媒资包
            //获取演员信息
            String directorNames = "";
            String actorNames = "";

            Element actorListUl = document.select(".actor_list").first();
            Elements actorListLi = actorListUl.select("li.item");
            for(Element actor : actorListLi){
                if(actor.hasClass("item_more")){
                    continue;
                }
                Element actorInfo = actor.select("div.actor_info").first();
                String actorName = actorInfo.select(".actor_detail .actor_name").text();
                Element directorFlag = actor.select("span.director").first();
                if(directorFlag != null){
                    directorNames += actorName + ",";
                }else{
                    actorNames += actorName + ",";
                }
            }
            if(!StringUtils.isEmpty(directorNames)){
                directorNames = directorNames.substring(0, directorNames.length() - 1);
            }
            if(!StringUtils.isEmpty(actorNames)){
                actorNames = actorNames.substring(0, actorNames.length() - 1);
            }


            String description = document.select(".video_desc .desc_txt span").text();


            Media media = new Media();
            media.setParentId(0);
            media.setPkgType(filmpkgType);
            media.setCreateDate(sdf.format(now));
            media.setType(MediaPackageType.TYPE_PACKAGES.getIndex());
            media.setTerminal("pc+app");
            media.setState("1");
            media.setPlayCount(0L);
            media.setSearchCount(0);

            media.setName(name);
            media.setDoubanScore(score);
            media.setDirector(directorNames);
            media.setStarring(actorNames);
            media.setUpsaleDate(year);
            media.setIntroduce(description);
            media.setDetail(description);
            media.save();
            media.setSerial(0);

            Integer packageMediaId = media.getId();

            //save poster
            Poster poster = new Poster();
            vpic = DownloadImage.download(vpic);//下载媒资包图片

            poster.setMediaId(packageMediaId);
            poster.setType("title");
            poster.setTitle(name);
            poster.setMiddle(IMGURL + vpic);
            poster.setCreateDate(sdf.format(now));
            poster.save();

            //save actors
            for(Element actor : actorListLi){
                if(actor.hasClass("item_more")){
                    continue;
                }
                String actorId = actor.attr("data-id");
                Element actorInfo = actor.select("div.actor_info").first();
                String actorName = actorInfo.select(".actor_detail .actor_name").text();
                Element directorFlag = actor.select("span.director").first();
                if(directorFlag != null){
                    addActorInfo(actorId,"director",packageMediaId, actorName);
                }else{
                    addActorInfo(actorId,"actor",packageMediaId, actorName);
                }
            }

            Elements videoTags = document.select(".video_tag .tag_list a.tag");
            MediaRefCategory mediaRefCategory = new MediaRefCategory();
            Map params = new HashMap();
            MediaCategory mediaCategory = MediaCategory.getMediaCategoryRepository().selectCategoryByType(filmpkgType);
            if(videoTags.size() > 0){
                List<CategoryProperty> categoryPropertyList = CategoryProperty.getCategoryPropertyRepository().selectByCategoryId(mediaCategory.getId());
                for(CategoryProperty categoryProperty : categoryPropertyList){
                    for(Element ele : videoTags){
                        params.clear();
                        String categoryName = ele.text();
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
            logger.info(SourceType.TYPE_TENXUN.getName() + " " + name + "  媒资包创建结束, hs：" + (endTime - startTime));
            return media;
        }
    }


    //动漫
    public static void addAnimeMedia(String tvId, RestTemplate restTemplate) throws Exception {
        Media packageMedia = addAnimaPackageMedia(tvId);
        if(packageMedia == null){
            return;
        }
        Integer total = packageMedia.getTotal();
        Integer serial = packageMedia.getSerial();
        if(serial == 0 || serial < total){

        }else{
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat(" yyyy-MM-dd HH:mm:ss ");
        Map param = new HashMap();
        param.put("range", serial + "-" + total);
        param.put("videoId", tvId);
        Integer latestOrder = addSubMedia(param, restTemplate, packageMedia, sdf);
        if(latestOrder.equals(serial) || latestOrder == 0){
            return;
        }
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

    private static Media addAnimaPackageMedia(String tvId) throws Exception {
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
        mediaInfo = replaceBlank(mediaInfo);
        mediaInfo = mediaInfo.replace(";",",").replace("'","");
        mediaInfo = mediaInfo.replace("http:","http");
        mediaInfo = mediaInfo.replaceAll("(\\{|,)([^:]+)", "$1\"$2\"").replaceAll("([^:,\\}]+)(\\}|,)", "\"$1\"$2");
        mediaInfo = mediaInfo.replace("http","http:");
        Map rProps = (Map) JSON.parse(mediaInfo);
        String name = (String) rProps.get("title");
        String vpic = (String) rProps.get("vpic");
        String aliasName;
        String area;
        String language;
        Integer totalSerial = 0;
        String year = null;

        Elements typeItems = document.getElementsByClass("type_item");
        for(Element element : typeItems){
            String typeTit = element.select("span.type_tit").text().replace(" ","").replace("　","");
            String typeTxt = element.select("span.type_txt").text();
            if(typeTit.startsWith("别名")){
                aliasName = typeTxt;
            }else if(typeTit.startsWith("地区")){
                area = typeTxt;
            }else if(typeTit.startsWith("语言")){
                language = typeTxt;
            }else if(typeTit.startsWith("总集数")){
                if(typeTit.endsWith("集")){
                    typeTit = typeTit.substring(0, typeTit.length()-1);
                }
                totalSerial = Integer.valueOf(typeTxt);
            }else if(typeTit.startsWith("出品时间")){
                year = typeTxt;
            }
        }
        MediaRepository mediaRepository = Media.getMediaRepository();
        Example example = new Example(Media.class);
        example.createCriteria().andEqualTo("name", name)
                .andEqualTo("pkgType", animepkgType)
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
                logger.info(name + " 动漫已经集全");
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
            String scoreValue = document.select(".video_score .score_db .score").text();
            Integer score = null;
            if(StringUtil.isNotEmpty(scoreValue)){
                score = new BigDecimal(scoreValue).multiply(new BigDecimal(10)).intValue();
            }
            //添加媒资或媒资包
            //获取演员信息
            String directorNames = "";
            String actorNames = "";

            Element actorListUl = document.select(".actor_list").first();
            Elements actorListLi = actorListUl.select("li.item");
            for(Element actor : actorListLi){
                if(actor.hasClass("item_more")){
                    continue;
                }
                Element actorInfo = actor.select("div.actor_info").first();
                String actorName = actorInfo.select(".actor_detail .actor_name").text();
                Element directorFlag = actor.select("span.director").first();
                if(directorFlag != null){
                    directorNames += actorName + ",";
                }else{
                    actorNames += actorName + ",";
                }
            }
            if(!StringUtils.isEmpty(directorNames)){
                directorNames = directorNames.substring(0, directorNames.length() - 1);
            }
            if(!StringUtils.isEmpty(actorNames)){
                actorNames = actorNames.substring(0, actorNames.length() - 1);
            }


            String description = document.select(".video_desc .desc_txt span").text();


            Media media = new Media();
            media.setParentId(0);
            media.setPkgType(animepkgType);
            media.setCreateDate(sdf.format(now));
            media.setType(MediaPackageType.TYPE_PACKAGES.getIndex());
            media.setTerminal("pc+app");
            media.setState("1");
            media.setPlayCount(0L);
            media.setSearchCount(0);

            media.setName(name);
            media.setDoubanScore(score);
            media.setDirector(directorNames);
            media.setStarring(actorNames);
            if(totalSerial == 0){
                logger.info(name);
            }
            media.setTotal(totalSerial);
            media.setUpsaleDate(year);
            media.setIntroduce(description);
            media.setDetail(description);
            media.save();
            media.setSerial(0);

            Integer packageMediaId = media.getId();

            //save poster
            Poster poster = new Poster();
            vpic = DownloadImage.download(vpic);//下载媒资包图片

            poster.setMediaId(packageMediaId);
            poster.setType("title");
            poster.setTitle(name);
            poster.setMiddle(IMGURL + vpic);
            poster.setCreateDate(sdf.format(now));
            poster.save();

            //save actors
            for(Element actor : actorListLi){
                if(actor.hasClass("item_more")){
                    continue;
                }
                String actorId = actor.attr("data-id");
                Element actorInfo = actor.select("div.actor_info").first();
                String actorName = actorInfo.select(".actor_detail .actor_name").text();
                Element directorFlag = actor.select("span.director").first();
                if(directorFlag != null){
                    addActorInfo(actorId,"director",packageMediaId, actorName);
                }else{
                    addActorInfo(actorId,"actor",packageMediaId, actorName);
                }
            }

            Elements videoTags = document.select(".video_tag .tag_list a.tag");
            MediaRefCategory mediaRefCategory = new MediaRefCategory();
            Map params = new HashMap();
            MediaCategory mediaCategory = MediaCategory.getMediaCategoryRepository().selectCategoryByType(animepkgType);
            if(videoTags.size() > 0){
                List<CategoryProperty> categoryPropertyList = CategoryProperty.getCategoryPropertyRepository().selectByCategoryId(mediaCategory.getId());
                for(CategoryProperty categoryProperty : categoryPropertyList){
                    for(Element ele : videoTags){
                        params.clear();
                        String categoryName = ele.text();
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
            logger.info(SourceType.TYPE_TENXUN.getName() + " " + name + "  媒资包创建结束, hs：" + (endTime - startTime));
            return media;
        }
    }

    public static void addVarietyMedia(String tvId, RestTemplate restTemplate) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat(" yyyy-MM-dd HH:mm:ss ");
        Media packageMedia = addVarietyPackageMedia(tvId);
        if(packageMedia == null){
            return;
        }

        Map param = new HashMap();
        String year = packageMedia.getUpsaleDate();
        Integer updateSerial = packageMedia.getSerial();
        if(year == null){
            year = "2018";
        }
        param.put("videoId", tvId);
        param.put("year", year);
        String response = restTemplate.getForObject("https://s.video.qq.com/get_playsource?id={videoId}&type=4&year={year}&otype=json", String.class, param);
        String infoMapString = response.split("QZOutputJson=")[1];
        infoMapString = infoMapString.substring(0,infoMapString.length()-1);
        Map infoMap = JSON.parseObject(infoMapString);
        if(infoMap == null){
            return;
        }
        Map videoPlayList = (Map) infoMap.get("PlaylistItem");
        if(videoPlayList == null){
            return;
        }
        List<String> indexList = (List<String>) videoPlayList.get("indexList");
        List<String> indexList2 = (List<String>) videoPlayList.get("indexList2");
        if(indexList == null || indexList.size() == 0){
            return;
        }
        Map queryMap = new HashMap();
        Integer count = 0;
        for(String index : indexList){
            queryMap.put("year", index);
            for(String index2 : indexList2){
                if(index2 != null){
                    Integer updateDate = Integer.valueOf(index + index2);
                    if(updateSerial > updateDate){
                        continue;
                    }
                    queryMap.put("month", index2);
                }else{
                    queryMap.put("month", "");
                }
                Integer updateCount = addSubVarietyMedia(tvId, restTemplate, packageMedia, queryMap);
                if(updateCount > count){
                    count = updateCount;
                }
            }
        }

        if(count > 0){
            packageMedia.setUpdateDate(sdf.format(new Date()));
            packageMedia.setSerial(count);
            Media.getMediaRepository().updateByPrimaryKeySelective(packageMedia);

            /**
             * 修改搜索引擎中的信息
             */
            Integer id = packageMedia.getId();
            Media media2 = Media.getMediaRepository().getMediaDetail(id);
            EsController.addEsDate(media2);
            System.out.println("====================！！已更新搜索引擎信息！！========================");
        }
    }

    private static Integer addSubVarietyMedia(String tvId, RestTemplate restTemplate, Media packageMedia, Map param) {
        Integer count = -1;
        SimpleDateFormat sdf = new SimpleDateFormat(" yyyy-MM-dd HH:mm:ss ");
        Date now = new Date();
        param.put("videoId", tvId);
        String response = restTemplate.getForObject("https://s.video.qq.com/get_playsource?id={videoId}&type=4&year={year}&month={month}&otype=json", String.class, param);
        String infoMapString = response.split("QZOutputJson=")[1];
        infoMapString = infoMapString.substring(0,infoMapString.length()-1);
        Map infoMap = JSON.parseObject(infoMapString);
        if(infoMap == null){
            return count;
        }

        Map videoPlayList = (Map) infoMap.get("PlaylistItem");
        if(videoPlayList == null){
            return count;
        }
        List<Map> videos = (List<Map>) videoPlayList.get("videoPlayList");
        if(videos == null || videos.size() == 0){
            return count;
        }
        for(Map video : videos){
            String type = (String) video.get("type");
            if(!"1".equals(type)){
                logger.info("不是正片，跳过");
                continue;
            }
            Media subMedia = new Media();
            MediaRepository mediaRepository = Media.getMediaRepository();
            String mediaName = (String) video.get("title");
//            Integer timeLength = (Integer) video.get("duration");
            String playUrl = (String) video.get("playUrl");
            Boolean flag = checkSubMediaExsit(playUrl,mediaName);
            if(flag){
                continue;
            }
            String itemId = (String) video.get("id");
//            String shortTitle = (String) video.get("shortTitle");
            String orderStr = (String) video.get("episode_number");
            Integer order = Integer.valueOf(orderStr.replace("-",""));
//            String desc = (String) video.get("description");
//            String subtitle = (String) video.get("subtitle");
//            Object scoreValue = (Object) data.get("score");
//            Integer score = new BigDecimal(scoreValue.toString()).multiply(new BigDecimal(10)).intValue();

//            subMedia.setIntroduce(subtitle);
//            subMedia.setDetail(desc);
            subMedia.setTerminal("pc+app");
//            subMedia.setDoubanScore(score);

            subMedia.setParentId(packageMedia.getId());
            subMedia.setPkgType(varietypkgType);
            subMedia.setState("1");
            subMedia.setType("main");
//            subMedia.setPlayLength(timeLength);
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
            resource.setState("1");
            resource.setType("main");
            resource.setPlayType("third");
            resource.setSerial(order);
            resource.setName(mediaName);
            resource.setPlayUrl(playUrl);
            resource.setKeyword(mediaName + " " + packageMedia.getStarring() + " " + packageMedia.getDirector());
            resource.setTerminal("pc+app");
            resource.setPlayCount(0);
            resource.setCreateDate(sdf.format(new Date()));
            resource.save();
            if(order > count){
                count = order;
            }
        }
        return count;
    }

    private static Media addVarietyPackageMedia(String tvId) throws Exception {
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
        mediaInfo = replaceBlank(mediaInfo);
        mediaInfo = mediaInfo.replace(";",",").replace("'","");
        mediaInfo = mediaInfo.replace("http:","http");
        mediaInfo = mediaInfo.replaceAll("(\\{|,)([^:]+)", "$1\"$2\"").replaceAll("([^:,\\}]+)(\\}|,)", "\"$1\"$2");
        mediaInfo = mediaInfo.replace("http","http:");
        Map rProps = (Map) JSON.parse(mediaInfo);
        String name = (String) rProps.get("title");
        String vpic = (String) rProps.get("vpic");
        String aliasName;
        String area = null;
        String language;
        String year = null;
        Integer serial = 0;

        Elements typeItems = document.getElementsByClass("type_item");
        for(Element element : typeItems){
            String typeTit = element.select("span.type_tit").text().replace(" ","").replace("　","");
            String typeTxt = element.select("span.type_txt").text();
            if(typeTit.startsWith("别名")){
                aliasName = typeTxt;
            }else if(typeTit.startsWith("地区")){
                area = typeTxt;
            }else if(typeTit.startsWith("语言")){
                language = typeTxt;
            }else if(typeTit.startsWith("首播时间")){
                year = typeTxt.substring(0,4);
            }else if(typeTit.startsWith("更新期数")){
                serial = Integer.valueOf(typeTxt.replace("-",""));
            }
        }
        MediaRepository mediaRepository = Media.getMediaRepository();
        Example example = new Example(Media.class);
        example.createCriteria().andEqualTo("name", name)
                .andEqualTo("pkgType", varietypkgType)
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
            packageMedia.setSerial(maxSerial);
            return packageMedia;
        }else{
            Long startTime = System.currentTimeMillis();
            String scoreValue = document.select(".video_score .score_db .score").text();
            Integer score = null;
            if(StringUtil.isNotEmpty(scoreValue)){
                score = new BigDecimal(scoreValue).multiply(new BigDecimal(10)).intValue();
            }
            //添加媒资或媒资包
            //获取演员信息
            String directorNames = "";
            String actorNames = "";

            Element actorListUl = document.select(".actor_list").first();
            Elements actorListLi = actorListUl.select("li.item");
            for(Element actor : actorListLi){
                if(actor.hasClass("item_more")){
                    continue;
                }
                Element actorInfo = actor.select("div.actor_info").first();
                String actorName = actorInfo.select(".actor_detail .actor_name").text();
                Element directorFlag = actor.select("span.director").first();
                if(directorFlag != null){
                    directorNames += actorName + ",";
                }else{
                    actorNames += actorName + ",";
                }
            }
            if(!StringUtils.isEmpty(directorNames)){
                directorNames = directorNames.substring(0, directorNames.length() - 1);
            }
            if(!StringUtils.isEmpty(actorNames)){
                actorNames = actorNames.substring(0, actorNames.length() - 1);
            }


            String description = document.select(".video_desc .desc_txt span").text();


            Media media = new Media();
            media.setParentId(0);
            media.setPkgType(varietypkgType);
            media.setCreateDate(sdf.format(now));
            media.setType(MediaPackageType.TYPE_PACKAGES.getIndex());
            media.setTerminal("pc+app");
            media.setState("1");
            media.setPlayCount(0L);
            media.setSearchCount(0);
            media.setSerial(serial);

            media.setName(name);
            media.setDoubanScore(score);
            media.setDirector(directorNames);
            media.setStarring(actorNames);
            media.setUpsaleDate(year);
            media.setIntroduce(description);
            media.setDetail(description);
            media.save();
            media.setSerial(0);

            Integer packageMediaId = media.getId();

            //save poster
            Poster poster = new Poster();
            vpic = DownloadImage.download(vpic);//下载媒资包图片

            poster.setMediaId(packageMediaId);
            poster.setType("title");
            poster.setTitle(name);
            poster.setMiddle(IMGURL + vpic);
            poster.setCreateDate(sdf.format(now));
            poster.save();

            //save actors
            for(Element actor : actorListLi){
                if(actor.hasClass("item_more")){
                    continue;
                }
                String actorId = actor.attr("data-id");
                Element actorInfo = actor.select("div.actor_info").first();
                String actorName = actorInfo.select(".actor_detail .actor_name").text();
                Element directorFlag = actor.select("span.director").first();
                if(directorFlag != null){
                    addActorInfo(actorId,"director",packageMediaId, actorName);
                }else{
                    addActorInfo(actorId,"actor",packageMediaId, actorName);
                }
            }

            Elements videoTags = document.select(".video_tag .tag_list a.tag");
            MediaRefCategory mediaRefCategory = new MediaRefCategory();
            Map params = new HashMap();
            MediaCategory mediaCategory = MediaCategory.getMediaCategoryRepository().selectCategoryByType(varietypkgType);
            if(videoTags != null && videoTags.size() > 0){
                List<CategoryProperty> categoryPropertyList = CategoryProperty.getCategoryPropertyRepository().selectByCategoryId(mediaCategory.getId());
                for(CategoryProperty categoryProperty : categoryPropertyList){
                    for(Element ele : videoTags){
                        params.clear();
                        String categoryName = ele.text();
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
            if(area != null){
                CategoryProperty areaCategoryProperty = CategoryProperty.getCategoryPropertyRepository().selectAreaByCategoryId(mediaCategory.getId());
                mediaRefCategory.setMediaId(packageMediaId);
                mediaRefCategory.setCategoryId(mediaCategory.getId());
                mediaRefCategory.setPropertyId(areaCategoryProperty.getId());
                mediaRefCategory.setProperty(areaCategoryProperty.getName());
                params.clear();
                params.put("id", areaCategoryProperty.getId());
                params.put("params", area);
                CategoryPropertyValue categoryPropertyValue1 = CategoryPropertyValue.getCategoryPropertyValueRepository().selectPropertyValueById(params);
                if(categoryPropertyValue1 != null){
                    mediaRefCategory.setPropertyValueId(categoryPropertyValue1.getId());
                    mediaRefCategory.save();
//                    params.clear();
//                    params.put("id", areaCategoryProperty.getId());
//                    params.put("params", "更早");
//                    categoryPropertyValue1 = CategoryPropertyValue.getCategoryPropertyValueRepository().selectPropertyValueById(params);
//                    mediaRefCategory.setPropertyValueId(categoryPropertyValue1.getId());
                }
            }

            Media media2 = Media.getMediaRepository().getMediaDetail(packageMediaId);
            EsController.addEsDate(media2);
            Long endTime = System.currentTimeMillis();
            logger.info(SourceType.TYPE_TENXUN.getName() + " " + name + "  媒资包创建结束, hs：" + (endTime - startTime));
            return media;
        }
    }

    public static void fixVarietyMedia(String tvId, RestTemplate restTemplate) throws Exception {
        fixVarietyPackageMedia(tvId);
    }

    private static void fixVarietyPackageMedia(String tvId) throws Exception {
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
        mediaInfo = replaceBlank(mediaInfo);
        mediaInfo = mediaInfo.replace(";",",").replace("'","");
        mediaInfo = mediaInfo.replace("http:","http");
        mediaInfo = mediaInfo.replaceAll("(\\{|,)([^:]+)", "$1\"$2\"").replaceAll("([^:,\\}]+)(\\}|,)", "\"$1\"$2");
        mediaInfo = mediaInfo.replace("http","http:");
        Map rProps = (Map) JSON.parse(mediaInfo);
        String name = (String) rProps.get("title");
        String vpic = (String) rProps.get("vpic");
        String aliasName;
        String area = null;
        String language;
        String year = null;
        Integer serial = 0;

        Elements typeItems = document.getElementsByClass("type_item");
        for(Element element : typeItems){
            String typeTit = element.select("span.type_tit").text().replace("　","");
            String typeTxt = element.select("span.type_txt").text();
            if(typeTit.startsWith("别名")){
                aliasName = typeTxt;
            }else if(typeTit.startsWith("地区")){
                area = typeTxt;
            }else if(typeTit.startsWith("语言")){
                language = typeTxt;
            }else if(typeTit.startsWith("首播时间")){
                year = typeTxt.substring(0,4);
            }else if(typeTit.startsWith("更新期数")){
                serial = Integer.valueOf(typeTxt.replace("-",""));
            }
        }
        MediaRepository mediaRepository = Media.getMediaRepository();
        Example example = new Example(Media.class);
        example.createCriteria().andEqualTo("name", name)
                .andEqualTo("pkgType", varietypkgType)
                .andEqualTo("type", "packages");
        List<Media> packageMediaList = mediaRepository.selectByExample(example);
        if(packageMediaList.size() > 1){
            logger.info(name + "  媒资包有多个");
        }else if(packageMediaList.size() == 1){
            Media packageMedia = packageMediaList.get(0);
            Elements videoTags = document.select(".video_tag .tag_list a.tag");
            MediaRefCategory mediaRefCategory = new MediaRefCategory();
            Example example1 = new Example(MediaRefCategory.class);
            example1.createCriteria().andEqualTo("mediaId", packageMedia.getId());
            MediaRefCategory.getMediaRefCategoryRepository().deleteByExample(example1);
            Map params = new HashMap();
            MediaCategory mediaCategory = MediaCategory.getMediaCategoryRepository().selectCategoryByType(varietypkgType);
            if(videoTags != null && videoTags.size() > 0){
                List<CategoryProperty> categoryPropertyList = CategoryProperty.getCategoryPropertyRepository().selectByCategoryId(mediaCategory.getId());
                for(CategoryProperty categoryProperty : categoryPropertyList){
                    for(Element ele : videoTags){
                        params.clear();
                        String categoryName = ele.text();
                        if(categoryName.endsWith("剧") && !categoryName.equals("喜剧")){
                            categoryName = categoryName.substring(0, categoryName.length()-1);
                        }
                        params.put("id", categoryProperty.getId());
                        params.put("params", categoryName);
                        CategoryPropertyValue categoryPropertyValue = CategoryPropertyValue.getCategoryPropertyValueRepository().selectPropertyValueById(params);
                        if (null != categoryPropertyValue) {
                            mediaRefCategory.setMediaId(packageMedia.getId());
                            mediaRefCategory.setCategoryId(mediaCategory.getId());
                            mediaRefCategory.setPropertyId(categoryProperty.getId());
                            mediaRefCategory.setProperty(categoryProperty.getName());
                            mediaRefCategory.setPropertyValueId(categoryPropertyValue.getId());
                            mediaRefCategory.save();
                        }
                    }
                }

            }
//            if(year != null){
//                CategoryProperty yearCategoryProperty = CategoryProperty.getCategoryPropertyRepository().selectYearByCategoryId(mediaCategory.getId());
//                mediaRefCategory.setMediaId(packageMedia.getId());
//                mediaRefCategory.setCategoryId(mediaCategory.getId());
//                mediaRefCategory.setPropertyId(yearCategoryProperty.getId());
//                mediaRefCategory.setProperty(yearCategoryProperty.getName());
//                params.clear();
//                params.put("id", yearCategoryProperty.getId());
//                params.put("params", year);
//                CategoryPropertyValue categoryPropertyValue1 = CategoryPropertyValue.getCategoryPropertyValueRepository().selectPropertyValueById(params);
//                if(categoryPropertyValue1 == null){
//                    params.clear();
//                    params.put("id", yearCategoryProperty.getId());
//                    params.put("params", "更早");
//                    categoryPropertyValue1 = CategoryPropertyValue.getCategoryPropertyValueRepository().selectPropertyValueById(params);
//                    mediaRefCategory.setPropertyValueId(categoryPropertyValue1.getId());
//                }else{
//                    mediaRefCategory.setPropertyValueId(categoryPropertyValue1.getId());
//                }
//                mediaRefCategory.save();
//            }
            if(area != null){
                CategoryProperty areaCategoryProperty = CategoryProperty.getCategoryPropertyRepository().selectAreaByCategoryId(mediaCategory.getId());
                mediaRefCategory.setMediaId(packageMedia.getId());
                mediaRefCategory.setCategoryId(mediaCategory.getId());
                mediaRefCategory.setPropertyId(areaCategoryProperty.getId());
                mediaRefCategory.setProperty(areaCategoryProperty.getName());
                params.clear();
                params.put("id", areaCategoryProperty.getId());
                params.put("params", area);
                CategoryPropertyValue categoryPropertyValue1 = CategoryPropertyValue.getCategoryPropertyValueRepository().selectPropertyValueById(params);
                if(categoryPropertyValue1 != null){
                    mediaRefCategory.setPropertyValueId(categoryPropertyValue1.getId());
                    mediaRefCategory.save();
//                    params.clear();
//                    params.put("id", areaCategoryProperty.getId());
//                    params.put("params", "更早");
//                    categoryPropertyValue1 = CategoryPropertyValue.getCategoryPropertyValueRepository().selectPropertyValueById(params);
//                    mediaRefCategory.setPropertyValueId(categoryPropertyValue1.getId());
                }
            }

            Media media2 = Media.getMediaRepository().getMediaDetail(packageMedia.getId());
            EsController.addEsDate(media2);
            System.out.println(name + "  更新属性完成");
        }else{
            System.out.println();
        }
    }
}
