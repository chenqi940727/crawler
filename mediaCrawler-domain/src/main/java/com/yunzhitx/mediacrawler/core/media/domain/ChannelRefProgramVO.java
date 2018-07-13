package com.yunzhitx.mediacrawler.core.media.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

/**
 * 代码生成器自动生成
 * Date:2017-11-14 11:40:46
 * @author
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChannelRefProgramVO implements Serializable{
	private static final long serialVersionUID = 1L;
    private Integer channelId;
	private Integer programId; // 节目单id
}