package com.learn.es.web.form;

import lombok.Data;

import java.io.Serializable;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.web.form.PhotoForm
 * @description TODO
 * @date 2020/8/11 17:19
 */
@Data
public class PhotoForm implements Serializable {

	private String path;
	private int width;
	private int height;
}
