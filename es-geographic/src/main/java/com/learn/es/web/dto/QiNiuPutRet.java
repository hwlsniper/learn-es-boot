package com.learn.es.web.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.web.dto.QiNiuPutRet
 * @description TODO
 * @date 2020/8/12 11:35
 */
@Data
public final class QiNiuPutRet implements Serializable {

	public String key;
	public String hash;
	public String bucket;
	public int width;
	public int height;
}
