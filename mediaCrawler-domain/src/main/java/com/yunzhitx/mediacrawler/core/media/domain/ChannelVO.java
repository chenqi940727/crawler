package com.yunzhitx.mediacrawler.core.media.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 代码生成器自动生成
 * Date:2017-11-14 11:40:46
 * @author
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChannelVO implements Serializable{
	private static final long serialVersionUID = 1L;
    private Integer id;
	private String name; // 频道名称
	private String playCode; // 频道播放串
	private String ssChannelId; // 搜视网频道ID
	private String resolution; // 频道清晰度，流畅：fluency，标清：standard，高清：hight，超清：super
	private String logoUrl; // logo图片
	private String liveRealImg; // 频道实时截图
	private Boolean del; // 是否删除
	private Boolean display; // 是否显示
	private String remark; // 备注
    private Date createDate;
    private Date updateDate;
}