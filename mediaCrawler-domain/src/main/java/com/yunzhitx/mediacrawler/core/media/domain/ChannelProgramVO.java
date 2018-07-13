package com.yunzhitx.mediacrawler.core.media.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;


/**
 * 代码生成器自动生成
 * Date:2017-11-14 11:40:46
 *
 * @author
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChannelProgramVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer id;
    private Integer channelId; // 频道ID
    private String ssChannelId; // 搜视网节目单ID（用于获取图片）
    private String code; // 编码
    private String name; // 节目名称
    private String beginTime; // 开始时间
    private String endTime; // 结束时间
    private Boolean isRecommend; // 是否推荐
    private String keyWord; // 搜索关键词，使用逗号进行分割
    private Integer playCount; // 播放次数
    private String resolution; // 频道清晰度，流畅：fluency，标清：standard，高清：hight，超清：super
    private String volumeName; //
    private Date createDate;
    private Date updateDate;
}