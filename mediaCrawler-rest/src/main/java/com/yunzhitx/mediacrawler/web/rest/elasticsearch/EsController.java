package com.yunzhitx.mediacrawler.web.rest.elasticsearch;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yunzhitx.mediacrawler.core.categoryproperty.domain.CategoryProperty;
import com.yunzhitx.mediacrawler.core.categorypropertyvalue.domain.CategoryPropertyValue;
import com.yunzhitx.mediacrawler.core.media.domain.Media;
import com.yunzhitx.mediacrawler.core.mediarefcategory.domain.MediaRefCategory;
import com.yunzhitx.mediacrawler.core.poster.domain.Poster;
import com.yunzhitx.mediacrawler.core.resourcetype.domain.ResourceType;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author liyang
 * @Date 2017年11月30日16:03:53
 */
public class EsController {

    private static final String INDEX = "media";
    static RestHighLevelClient client = ESRestClient.instance();

    private final static String MEDIAID = "id";
    private final static String NAME = "name";
    private final static String PKGTYPE = "pkgType";
    private final static String YEAR = "year";
    private final static String DOUBANSCORE = "doubanScore";
    private final static String DIRECTOR = "director";
    private final static String TYPE = "type";
    private final static String STARRING = "starring";
    private final static String DETAIL = "detail";
    private final static String INTRODUCE = "introduce";
    private final static String AREA = "area";
    private final static String POSTERS = "posters";
    private final static String RESOURCETYPE = "resourceType";

    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    /**
     * 添加搜索引擎数据
     *
     * @param media
     * @return
     * @throws IOException
     */
    public static void addEsDate(Media media) throws IOException {
        String mediaId = media.getId().toString();
        Map<String, Object> source = new HashMap<>();
        source.put(MEDIAID, media.getId());
        source.put(NAME, media.getName());
        source.put(PKGTYPE, media.getPkgType());
        source.put(DOUBANSCORE, media.getDoubanScore());
        source.put(DIRECTOR, media.getDirector());
        source.put(TYPE, media.getType());
        source.put(STARRING, media.getStarring());
        source.put(DETAIL, media.getDetail());
        source.put(INTRODUCE, media.getIntroduce());
        //影片年代和地区需要判断是否有，有的是没有年代和地区属性
        CategoryPropertyValue area = media.getArea();
        if (null != area) {
            Map map = new HashMap();
            map.put("name", area.getName());
            String s = JSONUtils.toJSONString(map);
            source.put(AREA, s);
        }
        CategoryPropertyValue year = media.getYear();
        if (null != year) {
            Map map = new HashMap();
            map.put("name", year.getName());
            String s = JSONUtils.toJSONString(map);
            source.put(YEAR, s);
        }
        //海报
        List<Poster> poster = media.getPosterVOS();
        if (poster.size() > 0) {
            List list = new ArrayList();
            Map map = new HashMap();
            Poster posterVO = poster.get(0);
            if (null != posterVO) {
                if (null != posterVO.getMiddle()) {
                    map.put("middle", posterVO.getMiddle());
                }
                if (null != posterVO.getLarge()) {
                    map.put("large", posterVO.getLarge());
                }
                list.add(map);
                source.put(POSTERS, JSON.toJSONString(list));
//            source.put("large", poster.get(0).getLarge());
            }
        }
        //来源类型，重新封装成一个list然后放入搜索引擎
        List<ResourceType> resourceTypeVOS = media.getResourceTypeVOS();
        if (resourceTypeVOS.size() > 0) {
            List list = new ArrayList();
            for (int j = 0; j < resourceTypeVOS.size(); j++) {
                list.add(resourceTypeVOS.get(j).getFieldName());
            }
            source.put(RESOURCETYPE, JSON.toJSONString(list));
        }
        //添加属性
        List<MediaRefCategory> mediaRefCategoryVOS = MediaRefCategory.getMediaRefCategoryRepository().selectAllPropsByMeidaId(media.getId());
        if (mediaRefCategoryVOS.size() > 0) {
            Map param = new HashMap<>();
            for (int i = 0; i < mediaRefCategoryVOS.size(); i++) {
                CategoryProperty categoryProperty = CategoryProperty.getCategoryPropertyRepository().selectByPrimaryKey(mediaRefCategoryVOS.get(i).getPropertyId());
                param.put("mediaId", media.getId());
                param.put("propertyName", categoryProperty.getName());
                List list = new ArrayList();
                List<MediaRefCategory> mediaRefCategoryVOS1 = MediaRefCategory.getMediaRefCategoryRepository().selectSomePropS(param);
                for (int j = 0; j < mediaRefCategoryVOS1.size(); j++) {
                    list.add(mediaRefCategoryVOS1.get(j).getPropertyValueId());
                }
                source.put(categoryProperty.getFieldName().toString(), JSONUtils.toJSONString(list.toString()));
            }
            source.put("categoryId", JSONUtils.toJSONString(mediaRefCategoryVOS.get(0).getCategoryId()));
        }
        //添加播放量和创建时间
        source.put("playCount", media.getPlayCount());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String createDate = media.getCreateDate();
        try {
            source.put("createDate",sdf.parse(createDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // id存在则是覆盖(根据 setRefreshPolicy)
        IndexRequest request = new IndexRequest(INDEX, "china", mediaId).source(source);

        request.timeout(TimeValue.timeValueSeconds(1));

        // 写数据时的刷新策略，IMMEDIATE 立即刷新，适合测试时使用
        request.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);

        client.index(request);

    }

    public static void delete(String id) throws IOException {
        DeleteRequest request = new DeleteRequest(INDEX, "china", id);
        client.delete(request);
//        DeleteResponse response = client.delete(request);
    }

    public static Map<String, Object> selectMediaByMediaId(Integer mediaId) throws IOException {
        QueryBuilder queryBuilder = QueryBuilders.matchQuery("id",mediaId);
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(queryBuilder);
        SearchRequest searchRequest = new SearchRequest("media");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(10);
        searchRequest.source(searchSourceBuilder);
        SearchResponse response = client.search(searchRequest);
        return getReturnData(response);
    }

    public static  Map<String, Object>getReturnData(SearchResponse searchResponse){
        Map<String, Object> map = new HashMap<String, Object>();
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        SearchHit[] searchHits = searchResponse.getHits().getHits();
        for(SearchHit searchHit : searchHits){
            list.add(toBean(searchHit.getSourceAsString()));
        }
        map.put("data", list);
        return map;
    }


    @SuppressWarnings("unchecked")
    public static HashMap<String, Object> toBean(String json) {
        return toBean(json, HashMap.class);
    }

    public static <T> T toBean(String json, Class<T> clazz) {
        try {
            T bean = (T) mapper.readValue(json, clazz);
            return bean;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}