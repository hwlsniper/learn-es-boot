package com.learn.es.suggest;

import cn.hutool.json.JSONUtil;
import com.learn.es.SpringBootEsApplicationTests;
import com.learn.es.service.KwSuggestService;
import lombok.extern.log4j.Log4j2;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.suggest.TestSuggest
 * @description 搜索提示
 * @date 2020/8/19 19:53
 */
@Log4j2
public class TestSuggest extends SpringBootEsApplicationTests {

	@Autowired
	private KwSuggestService suggestService;

	@Test
	public void testSuggest() {
		List<String> list = suggestService.GetKwSuggestList("qiy");
		log.info("结果:{}", JSONUtil.toJsonPrettyStr(list));
	}
}
