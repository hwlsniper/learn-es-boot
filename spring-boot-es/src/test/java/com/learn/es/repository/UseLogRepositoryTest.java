package com.learn.es.repository;

import cn.hutool.json.JSONUtil;
import com.learn.es.SpringBootEsApplicationTests;
import com.learn.es.model.UseLogDO;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.repository.UseLogRepositoryTest
 * @description repository 方式操作 ES
 * @date 2020/8/5 9:14
 */
@Slf4j
public class UseLogRepositoryTest extends SpringBootEsApplicationTests {

	@Autowired
	private UseLogRepository useLogRepository;

	/**
	 * 批量保存
	 */
	@Test
	public void testAdd() {
		List<UseLogDO> list = new ArrayList<>();
		for (int i = 1; i <= 10000; i++) {
			UseLogDO useLogDO = UseLogDO.builder()
					.id(String.valueOf(i))
					.sortNo(i)
					.result(String.format("我是%d号", i))
					.createTime(new Date())
					.build();
			list.add(useLogDO);
		}
		useLogRepository.saveAll(list);
	}

	/**
	 * 删
	 */
	@Test
	public void testDelete() {
		long deleteNumber = useLogRepository.deleteBySortNoIsGreaterThan(0);
		System.out.println("删除日志数量为：" + deleteNumber);
	}

	/**
	 * 此修改的实现实质是根据ID全覆盖
	 */
	@Test
	public void testUpdate() {
		Optional<UseLogDO> optional = useLogRepository.findById("1");
		if(optional.isPresent()) {
			UseLogDO useLogDO = optional.get();
			log.info("初始数据:{}", JSONUtil.toJsonPrettyStr(useLogDO));
			useLogDO.setResult("我是修改后的1号");
			UseLogDO save = useLogRepository.save(useLogDO);
			log.info("修改后的数据:{}", JSONUtil.toJsonPrettyStr(save));
		}
	}

	/**
	 * 分页
	 */
	@Test
	public void testPage() {
		Pageable pageable = PageRequest.of(0, 20);
		Page<UseLogDO> pages = useLogRepository.findBySortNoIsBetween(20, 30, pageable);
		pages.getContent().forEach(page -> {
			log.info("数据:{}", JSONUtil.toJsonPrettyStr(page));
		});
	}

	/**
	 * 查询时间段内的数据
	 */
	@Test
	public void testBetween() {
		List<UseLogDO> list = useLogRepository.findByCreateTimeBetween(1596590420182L, 1596590420184L);
		log.info("数据大小:{}", list.size());
	}
}
