package com.learn.es.repository;

import cn.hutool.json.JSONUtil;
import com.learn.es.SpringBootEsApplicationTests;
import com.learn.es.model.Person;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.ParsedAvg;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.repository.PersonRepositoryTest
 * @description repository 方式操作 ES
 * @date 2020/8/3 18:01
 */
@Slf4j
public class PersonRepositoryTest extends SpringBootEsApplicationTests {

	@Autowired
	private PersonRepository repo;

	@Autowired
	private ElasticsearchRestTemplate restTemplate;

	/**
	 * 测试新增
	 */
	@Test
	public void save() {
		Person person = new Person(1L, "刘备", "蜀国", 18, LocalDate.parse("1990-01-02 03:04:05", DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")), "刘备（161年－223年6月10日），即汉昭烈帝（221年－223年在位），又称先主，字玄德，东汉末年幽州涿郡涿县（今河北省涿州市）人，西汉中山靖王刘胜之后，三国时期蜀汉开国皇帝、政治家。\n刘备少年时拜卢植为师；早年颠沛流离，备尝艰辛，投靠过多个诸侯，曾参与镇压黄巾起义。先后率军救援北海相孔融、徐州牧陶谦等。陶谦病亡后，将徐州让与刘备。赤壁之战时，刘备与孙权联盟击败曹操，趁势夺取荆州。而后进取益州。于章武元年（221年）在成都称帝，国号汉，史称蜀或蜀汉。《三国志》评刘备的机权干略不及曹操，但其弘毅宽厚，知人待士，百折不挠，终成帝业。刘备也称自己做事“每与操反，事乃成尔”。\n章武三年（223年），刘备病逝于白帝城，终年六十三岁，谥号昭烈皇帝，庙号烈祖，葬惠陵。后世有众多文艺作品以其为主角，在成都武侯祠有昭烈庙为纪念。");
		Person save = repo.save(person);
		log.info("【save】= {}", save);
	}

	/**
	 * 测试批量新增
	 */
	@Test
	public void saveList() {
		List<Person> personList = Lists.newArrayList();
		personList.add(new Person(5L, "关羽", "蜀国", 19, LocalDate.parse("1989-01-02 03:04:05", DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")), "关羽（？—220年），本字长生，后改字云长，河东郡解县（今山西运城）人，雅号“美髯公”。早年跟随刘备颠沛流离，辗转各地，和刘备、张飞情同兄弟，因而虽然受到了曹操的厚待，但关羽仍然借机离开曹操，去追随刘备。赤壁之战后，关羽助刘备、周瑜攻打曹仁所驻守的南郡，而后刘备势力逐渐壮大，关羽则长期镇守荆州。\n建安二十四年，关羽在与曹仁之间的军事摩擦中逐渐占据上风，随后水陆并进，围襄阳，攻樊城，并利用秋季大雨，水淹七军，将前来救援的于禁打的全军覆没，进而包围樊城。关羽威震华夏，使得曹操一度产生迁都以避关羽锋锐的想法。\n但随后东吴孙权派遣吕蒙、陆逊袭击了关羽的后方，麋芳、士仁都背弃关羽。同时，关羽又在与徐晃的交战中失利，最终进退失据，兵败被杀。谥曰壮缪侯。\n关羽去世后，逐渐被神化，民间尊其为“关公”，历代朝廷多有褒封，清代奉为“忠义神武灵佑仁勇威显关圣大帝”，崇为“武圣”，与“文圣” 孔子齐名。《三国演义》尊其为蜀国“五虎上将”之首，毛宗岗称其为《演义》三绝中的“义绝”。"));
		personList.add(new Person(6L, "张飞", "蜀国", 18, LocalDate.parse("1990-01-02 03:04:05", DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")), "张飞（？—221年），字益德（《华阳国志》作翼德），涿郡（今河北涿州）人，三国时期蜀汉名将。张飞勇武过人，与结拜兄弟关羽并称为“万人敌”。 [1]  关羽年长数岁，张飞兄事之。公元184年黄巾起义爆发，刘备在涿县组织起了一支义勇军参与扑灭黄巾军的战争，张飞与关羽一起加入，随刘备辗转各地。三人情同兄弟，寝则同床，刘备出席各种宴会时，和关羽终日侍立在刘备身旁。 [2]  公元196年因交恶曹豹而被吕布所破。吕布败亡之后，张飞被任命为中郎将。公元200年刘备衣带诏事情泄漏，率领关羽、张飞逃走，杀下邳太守车胄，刘备战败，关羽被擒，刘备与张飞投奔袁绍。公元208年刘备于长坂坡败退时，张飞仅率二十骑断后，曹军无人敢逼近，刘备因此得以免难。\n刘备入蜀后，张飞与诸葛亮、赵云进军西川，分定郡县。在抵达江州时义释了刘璋手下的巴郡太守严颜。在巴西之战中，击败魏国名将张郃。在武都之战中，兵败而还。刘备称帝后，张飞晋升为车骑将军、领司隶校尉，封西乡侯。同年，张飞因为暴而无恩，被部将范强、张达杀害。谥曰桓侯。"));
		personList.add(new Person(7L, "赵云", "蜀国", 16, LocalDate.parse("1992-01-02 03:04:05", DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")), "赵云（？－229年），字子龙，常山国真定县（今河北省正定县）人。身长八尺，姿颜雄伟，三国时期蜀汉名将。\n 汉末军阀混战，赵云受本郡推举，率领义从加入白马将军公孙瓒。期间结识了汉室皇亲刘备，但不久之后，赵云因为兄长去世而离开。赵云离开公孙瓒大约七年后，在邺城与刘备相见，从此追随刘备。\n赵云跟随刘备将近三十年，先后参加过博望坡之战、长坂坡之战、江南平定战，独自指挥过入川之战、汉水之战、箕谷之战，都取得了非常好的战果。除了四处征战，赵云还先后以偏将军任桂阳太守，以留营司马留守公安，以翊军将军督江州。除此之外，赵云于平定益州时引霍去病故事劝谏刘备将田宅归还百姓，又于关羽、张飞被害之后劝谏刘备不要伐吴，被后世赞为有大臣局量的儒将，甚至被认为是三国时期的完美人物 。\n赵云去世后，于蜀汉景耀四年（261年）被追谥为“顺平侯”，其“常胜将军”的形象在后世被广为流传。"));

		personList.add(new Person(8L, "张辽 ", "魏国", 21, LocalDate.parse("1987-01-02 03:04:05", DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")), "张辽（169年—222年），字文远，雁门马邑（今山西朔州市）人。汉末三国时期曹魏名将，聂壹的后人。\n起初，担任雁门郡吏。又先后跟随丁原、何进、董卓、吕布，恪尽职守，历尽坎坷。吕布败亡后，张辽归属曹操。此后，立下众多显赫的功勋。洞察敌情而劝降昌豨。攻袁氏而转战河北。在白狼山之战率领先锋大破乌桓并斩杀乌桓单于蹋顿。驱逐辽东大将柳毅。以静制动平定军中谋反。进军江淮击灭陈兰、梅成。此后，长期镇守合肥。\n黄初元年（220年），张辽进封晋阳侯。染病之后，依旧令孙权非常忌惮。黄初三年（222年），张辽抱病击破吴将吕范。同年，病逝于江都，谥曰刚侯。张辽为历代所推崇，成为古今六十四名将之一。"));
		personList.add(new Person(9L, "于禁", "魏国", 22, LocalDate.parse("1986-01-02 03:04:05", DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")), "于禁（？－221年），字文则，泰山钜平（今山东泰安）人。三国时期曹魏名将，原为鲍信部将，鲍信战死后被王朗引荐给曹操，之后便随曹操南征北战，立下了许多战功。因为敢于攻击不守军纪的青州兵，且为了维护军法不惜杀掉自己的故友，被曹操称赞胜过古代名将。\n关羽围攻襄、樊时，于禁督七军前往救援，被关羽利用秋季大雨打得全军覆没，投降后被监押在南郡。而后关羽被吕蒙击败，于禁流落至东吴，孙权向魏国称藩后，将于禁送还魏国。虽然魏文帝曹丕表面安慰于禁，却暗里让人作壁画羞辱于禁，于禁因此惭恚而死，被恶谥为厉侯。\n于禁带军严肃庄重，战斗中所缴获的财物从不私藏，因此深得曹操器重，是曹操所有的将领中唯一的假节钺之人。但于禁常以军法处理下属，不得士卒众心。 [1]  后世将于禁与张辽、徐晃等合称为“五子良将”。"));
		personList.add(new Person(10L, "夏侯渊", "魏国", 20, LocalDate.parse("1988-01-02 03:04:05", DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")), "夏侯渊（？－219年），字妙才，沛国谯（今安徽亳州）人，东汉末年名将，擅长千里奔袭作战，常常出敌不意，官至征西将军，封博昌亭侯。\n 初期随曹操征伐，官渡之战为曹操督运粮草，又督诸将先后平定昌豨、徐和、雷绪、商曜等叛乱。后率军征伐关中与凉州，斩梁兴、逐马超、破韩遂、灭宋建，横扫羌族、氐族、屠各等外族势力，虎步关右。张鲁降曹操后，夏侯渊留守汉中，与刘备相拒逾年，在定军山被刘备部将黄忠所袭，战死，谥曰愍侯。"));
		personList.add(new Person(11L, "许褚", "魏国", 23, LocalDate.parse("1985-01-02 03:04:05", DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")), "许褚（chǔ，见《辞海》）字仲康，谯国谯人（今安徽亳州市古城镇）。长八尺馀，腰大十围，容貌雄毅，勇力绝人。\n年轻时在家乡聚集了数千户人家，共同抵御贼寇。曾有一次因缺粮与贼寇用牛交换粮食，牛到了对方手中后又跑了回来，结果许褚单手倒拖牛尾走了百步，贼寇大惊，不敢要牛就走了。从此淮、汝、陈、梁之地，听到许褚之名都感到畏惧。\n后追随曹操，自典韦战死之后，主要负责曹操的护卫工作。官渡之战时发现欲谋害曹操者，将刺客全部杀掉。\n渭南之战时在身披重甲的情况下左手掩面，右手控船浆令曹操安然成功渡河，上岸才发现早已身中数箭，在与马超、韩遂会面时只让许褚相随，期间马超欲袭曹操，但听闻许褚之名兼怀疑从骑就是许褚，便问曹操虎侯安在，曹操指着许褚，许褚怒视马超以令他放弃。\n当曹操去世时许褚哭至吐血，曹丕其迁作武卫将军，负责宫中安全。曹叡继位时封其为牟乡侯，不久去世，谥曰壮侯。"));
		personList.add(new Person(12L, "曹洪", "魏国", 23, LocalDate.parse("1985-01-02 03:04:05", DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")), "曹洪（？－232年），字子廉，沛国谯（今安徽亳州）人。汉末三国时期曹魏名将，魏武帝曹操从弟。\n曹洪早年随曹操起兵讨伐董卓。曹操于荥阳兵败失马时，曹洪舍命献马，并救护曹操，使其免于厄难。随军征伐四方，平兖州、征刘表、讨祝臂。官渡之战时，曹洪留守本阵，击退了张郃、高览的猛攻。汉中之战时，与曹休在下辩抵御刘备，破斩吴兰、任夔，逼退张飞与马超。\n曹丕即位后，曹洪被拜为骠骑将军。曹丕欲处死曹洪，曹洪因卞太后的求情而得以免死，被贬为庶民。曹叡即位后，曹洪被拜为后将军，受封乐城侯。太和四年（230年），复拜骠骑将军。"));

		personList.add(new Person(13L, "太史慈", "魏国", 23, LocalDate.parse("1985-01-02 03:04:05", DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")), "太史慈（166年—206年），字子义，东莱黄县（今山东龙口东黄城集）人。东汉末年名将，官至建昌都尉。弓马熟练，箭法精良。曾为救孔融而单骑突围向刘备求援。原为刘繇[yáo]部下，后被孙策收降，自此太史慈为孙氏大将，助其扫荡江东。孙权统事后，因太史慈能制刘磐[pán]，便将管理南方的要务委托给他。\n建安十一年（206年），太史慈逝世，死前说道：“丈夫生世，当带三尺之剑，以升天子之阶。今所志未从，奈何而死乎！”（《吴书》，《三国演义》为“大丈夫生于乱世，当带三尺剑立不世之功；今所志未遂，奈何死乎！”）言讫而亡，年四十一岁。"));
		personList.add(new Person(14L, "甘宁", "魏国", 20, LocalDate.parse("1988-01-02 03:04:05", DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")), "甘宁（？—215年？220年？存疑），字兴霸，巴郡临江（今重庆忠县）人，三国时期孙吴名将，官至西陵太守，折冲将军。\n甘宁少年时在地方上为非作歹，组成渠师抢夺船只财物，崇尚奢华，人称锦帆贼。青年时停止抢劫，熟读诸子。曾任蜀郡丞，后历仕于刘表和黄祖麾下，未受重用。建安十三年（208年），甘宁率部投奔孙权，开始建功立业。曾经力劝孙权攻破黄祖占据楚关，随周瑜攻曹仁夺取夷陵，随鲁肃镇益阳对峙关羽，随孙权攻皖城擒获朱光。率百余人夜袭曹营，斩得数十首级而回。在逍遥津之战，他保护孙权蹴马趋津，死里逃生。孙权曾说：“孟德有张辽，孤有甘兴霸，足相敌也”。吕蒙曾说：“天下未定，斗将如宁难得，宜容忍之。”"));
		personList.add(new Person(15L, "潘璋", "魏国", 18, LocalDate.parse("1990-01-02 03:04:05", DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")), "潘璋（？－234年），字文珪，东郡发干（今山东冠县东）人。三国时期吴国将领。\n潘璋年轻时家贫，跟随孙权后得到其赏识，加上其作战勇猛，不断升迁，其一生为孙权东征西讨，在合肥之战、追擒关羽、夷陵之战、江陵保卫战中多次立下战功。但其为人奢侈贪财。经常设立军市，又劫杀将士以获得财物，但孙权念其有功未予深究。被陈寿盛赞为“江表之虎臣”"));

		Iterable<Person> people = repo.saveAll(personList);
		log.info("【people】= {}", people);
	}

	/**
	 * 测试更新
	 */
	@Test
	public void update() {
		repo.findById(15L).ifPresent(person -> {
			person.setCountry("吴国");
			Person save = repo.save(person);
			log.info("【update】= {}", save);
		});
	}

	/**
	 * 测试删除
	 */
	@Test
	public void delete() {
		// 主键删除
		repo.deleteById(1L);

		// 对象删除
		repo.findById(2L).ifPresent(person -> {
			repo.delete(person);
		});

		// 批量删除
		repo.deleteAll(repo.findAll());
	}

	/**
	 * 测试普通查询，按生日倒序
	 */
	@Test
	public void select() {
		repo.findAll(Sort.by(Sort.Direction.DESC, "birthday"))
				.forEach(person -> log.info("{} 生日: {}", person.getName(), person.getBirthday()));
	}

	/**
	 * 自定义查询，根据年龄范围查询
	 */
	@Test
	public void customSelectRangeOfAge() {
		repo.findByAgeBetween(18, 20).forEach(person -> log.info("{} 年龄: {}", person.getName(), person.getAge()));
	}

	/**
	 * 高级查询
	 */
	@Test
	public void advanceSelect() {
		// QueryBuilders 提供了很多静态方法，可以实现大部分查询条件的封装
		MatchQueryBuilder queryBuilder = QueryBuilders.matchQuery("name", "孙权");
		log.info("【queryBuilder】= {}", queryBuilder.toString());
		NativeSearchQuery query = new NativeSearchQueryBuilder().withQuery(queryBuilder).build();
		SearchHits<Person> search = restTemplate.search(query, Person.class);

		List<SearchHit<Person>> searchHits = search.getSearchHits();
		if(!CollectionUtils.isEmpty(searchHits)){
			//不为空遍历
			List<Person> list=searchHits.stream().map(hit->{
				//获取高亮数据
				Map<String, List<String>> highlightFields = hit.getHighlightFields();
				//更换高亮数据,判断高亮字段是否存在
				if(!CollectionUtils.isEmpty(highlightFields.get("remark"))) {
					hit.getContent().setRemark(highlightFields.get("remark").get(0));
				}
				if(!CollectionUtils.isEmpty(highlightFields.get("name"))) {
					hit.getContent().setName(highlightFields.get("name").get(0));
				}
				return hit.getContent();
			}).collect(Collectors.toList());

			log.info("【advanceSelect】= {}", list);
		}

		List<Person> list = repo.findByName("孙权");
		log.info("【advanceSelect】= {}", list);
	}

	/**
	 * 自定义高级查询
	 */
	@Test
	public void customAdvanceSelect() {
		// 构造查询条件
		NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
		// 添加基本的分词条件
		queryBuilder.withQuery(QueryBuilders.matchQuery("remark", "东汉"));
		// 排序
		queryBuilder.withSort(SortBuilders.fieldSort("age").order(SortOrder.DESC));

		queryBuilder.withPageable(PageRequest.of(0, 2));

		Page<Person> people = repo.search(queryBuilder.build());
		log.info("【people】总条数 = {}", people.getTotalElements());
		log.info("【people】总页数 = {}", people.getTotalPages());
		people.forEach(person -> log.info("【person】= {}，年龄 = {}", person.getName(), person.getAge()));



		Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "age"));
		Page<Person> pages = repo.findAll(pageable);
		log.info("【people】总条数 = {}", pages.getTotalElements());
		log.info("【people】总页数 = {}", pages.getTotalPages());
		people.forEach(person -> log.info("【person】= {}，年龄 = {}", person.getName(), person.getAge()));
	}

	/**
	 * 测试聚合，测试平均年龄
	 */
	@Test
	public void agg() {
		// 构造查询条件
		NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
		// 不查询任何结果
		queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{""}, null));
		// 平均年龄
		queryBuilder.addAggregation(AggregationBuilders.avg("avg").field("age"));
		log.info("【queryBuilder】= {}", JSONUtil.toJsonPrettyStr(queryBuilder.build()));

		AggregatedPage<Person> people = (AggregatedPage<Person>) repo.search(queryBuilder.build());
		double avgAge = ((ParsedAvg) people.getAggregation("avg")).getValue();
		log.info("【avgAge】= {}", avgAge);
	}

	/**
	 * 测试高级聚合查询，每个国家的人有几个，每个国家的平均年龄是多少
	 */
	@Test
	public void advanceAgg() {
		// 构造查询条件
		NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
		// 不查询任何结果
		queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{""}, null));
		// 1. 添加一个新的聚合，聚合类型为terms，聚合名称为country，聚合字段为country
		queryBuilder.addAggregation(AggregationBuilders.terms("country").field("country")
				// 2. 在国家聚合桶内进行嵌套聚合，求平均年龄
				.subAggregation(AggregationBuilders.avg("avg").field("age")));
		log.info("【queryBuilder】= {}", JSONUtil.toJsonPrettyStr(queryBuilder.build()));

		// 3. 查询
		AggregatedPage<Person> people = (AggregatedPage<Person>) repo.search(queryBuilder.build());

		// 4. 解析
		// 4.1. 从结果中取出名为 country 的那个聚合，因为是利用String类型字段来进行的term聚合，所以结果要强转为 StringTerm 类型
		ParsedStringTerms term = (ParsedStringTerms)people.getAggregation("country");
		// 4.2. 获取桶
		List<? extends Terms.Bucket> buckets = term.getBuckets();
		for(Terms.Bucket bucket : buckets) {
			// 4.3. 获取桶中的key，即国家名称  4.4. 获取桶中的文档数量
			log.info("{} 总共有 {} 人", bucket.getKeyAsString(), bucket.getDocCount());
			// 4.5. 获取子聚合结果：
			ParsedAvg avg = (ParsedAvg) bucket.getAggregations().asMap().get("avg");
			log.info("平均年龄：{}", avg.getValue());
		}
	}
}
