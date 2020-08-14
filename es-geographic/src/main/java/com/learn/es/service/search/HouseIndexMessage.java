package com.learn.es.service.search;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.service.search.HouseIndexMessage
 * @description TODO
 * @date 2020/8/12 20:09
 */
@NoArgsConstructor
public class HouseIndexMessage {

	public static final String INDEX = "index";
	public static final String REMOVE = "remove";

	public static final int MAX_RETRY = 3;

	@Setter
	@Getter
	private Long houseId;

	@Setter
	@Getter
	private String operation;

	@Setter
	@Getter
	private int retry = 0;

	public HouseIndexMessage(Long houseId, String operation, int retry) {
		this.houseId = houseId;
		this.operation = operation;
		this.retry = retry;
	}
}
