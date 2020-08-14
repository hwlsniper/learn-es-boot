package com.learn.es.service.search.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.primitives.Longs;
import com.learn.es.base.HouseSort;
import com.learn.es.base.RentValueBlock;
import com.learn.es.entity.House;
import com.learn.es.entity.HouseDetail;
import com.learn.es.entity.HouseTag;
import com.learn.es.entity.SupportAddress;
import com.learn.es.repository.HouseDetailRepository;
import com.learn.es.repository.HouseRepository;
import com.learn.es.repository.HouseTagRepository;
import com.learn.es.repository.SupportAddressRepository;
import com.learn.es.result.ServiceMultiResult;
import com.learn.es.result.ServiceResult;
import com.learn.es.service.house.IAddressService;
import com.learn.es.service.search.*;
import com.learn.es.web.form.MapSearch;
import com.learn.es.web.form.RentSearch;
import lombok.extern.log4j.Log4j2;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.AnalyzeRequest;
import org.elasticsearch.client.indices.AnalyzeResponse;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.*;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.SuggestionBuilder;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.service.search.impl.SearchServiceImpl
 * @description 搜索服务
 * @date 2020/8/13 9:53
 */
@Log4j2
@Service
public class SearchServiceImpl implements ISearchService {

	private static final String INDEX_NAME = "xunwu";

	private static final String INDEX_TYPE = "house";

	private static final String INDEX_TOPIC = "house_build";

	@Autowired
	private HouseRepository houseRepository;

	@Autowired
	private HouseDetailRepository houseDetailRepository;

	@Autowired
	private HouseTagRepository tagRepository;

	@Autowired
	private SupportAddressRepository supportAddressRepository;

	@Autowired
	private IAddressService addressService;

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private RestHighLevelClient client;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;

	/**
	 * 监听 kafka 消息, 处理消息
	 * @param content
	 */
	@KafkaListener(topics = INDEX_TOPIC)
	private void handleMessage(String content) {
		try {
			HouseIndexMessage message = objectMapper.readValue(content, HouseIndexMessage.class);

			switch(message.getOperation()) {
				case HouseIndexMessage.INDEX:
					// 创建或更新 index
					log.info("方法:{};创建或更新 index",SearchServiceImpl.class.getName()+".handleMessage()");
					this.createOrUpdateIndex(message);
					break;
				case HouseIndexMessage.REMOVE:
					// 删除 index
					log.info("方法:{};删除 index",SearchServiceImpl.class.getName()+".handleMessage()");
					this.removeIndex(message);
					break;
				default:
					log.warn("方法:{};错误信息:{}", SearchServiceImpl.class.getName()+".handleMessage()","Not support message content " + content);
					break;
			}
		} catch (JsonProcessingException e) {
			log.error("方法:{};错误信息:{};异常信息:{}", SearchServiceImpl.class.getName()+".handleMessage()", "Cannot parse json for " + content, e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * 删除 index
	 * @param message
	 */
	private void removeIndex(HouseIndexMessage message) {
		Long houseId = message.getHouseId();

		try {
			DeleteByQueryRequest deleteRequest = new DeleteByQueryRequest(INDEX_NAME);
			deleteRequest.setQuery(QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID, houseId));
			deleteRequest.setScroll(TimeValue.timeValueSeconds(60));
			BulkByScrollResponse response = client.deleteByQuery(deleteRequest, RequestOptions.DEFAULT);
			long deleted = response.getDeleted();

			ServiceResult serviceResult = addressService.removeLbs(houseId);

			if (!serviceResult.isSuccess() || deleted <= 0) {
				log.warn("Did not remove data from es for response: " + response);
				// 重新加入消息队列
				this.remove(houseId, message.getRetry() + 1);
			}
		} catch(IOException ex) {
			log.error("方法:{};异常信息:{}", SearchServiceImpl.class.getName()+".removeIndex()", ex.getMessage());
			ex.printStackTrace();
		}
	}

	/**
	 * 创建或更新 index
	 * @param message
	 */
	private void createOrUpdateIndex(HouseIndexMessage message) {
		Long houseId = message.getHouseId();

		Optional<House> optional = houseRepository.findById(houseId);
		if(!optional.isPresent()) {
			log.error("方法:{};Index house {} dose not exist!", SearchServiceImpl.class.getName()+".createOrUpdateIndex()", houseId);
			this.index(houseId, message.getRetry() + 1);
			return;
		}

		HouseDetail houseDetail = houseDetailRepository.findByHouseId(houseId);
		if(null == houseDetail) {
			log.error("方法:{};错误信息:{}", SearchServiceImpl.class.getName()+".createOrUpdateIndex()", "房屋明细数据不存在!");
			return;
		}

		House house = optional.get();
		HouseIndexTemplate template = new HouseIndexTemplate();
		modelMapper.map(house, template);
		modelMapper.map(houseDetail, template);

		SupportAddress city = supportAddressRepository.findByEnNameAndLevel(house.getCityEnName(), SupportAddress.Level.CITY.getValue());
		SupportAddress region = supportAddressRepository.findByEnNameAndLevel(house.getRegionEnName(), SupportAddress.Level.REGION.getValue());

		// 拼接地址
		String address = city.getCnName() + region.getCnName() + house.getStreet() + house.getDistrict() + houseDetail.getDetailAddress();

		// 根据城市和详细地址调用百度地图 API 获取相关数据
		ServiceResult<BaiduMapLocation> location = addressService.getBaiduMapLocation(city.getCnName(), address);
		if (!location.isSuccess()) {
			this.index(message.getHouseId(), message.getRetry() + 1);
			return;
		}
		template.setLocation(location.getResult());

		List<HouseTag> tags = tagRepository.findAllByHouseId(houseId);
		if(CollectionUtil.isNotEmpty(tags)) {
			List<String> tagStrs = new ArrayList<>();
			tags.forEach(tag -> tagStrs.add(tag.getName()));
			template.setTags(tagStrs);
		}

		try {
			SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
			SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
			// 精准匹配
			TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID, houseId);
			searchSourceBuilder.query(termQueryBuilder);
			searchRequest.source(searchSourceBuilder);
			SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

			boolean success;
			TotalHits totalHits = response.getHits().getTotalHits();
			if(totalHits.value == 0) {
				success = create(template);
			} else if(totalHits.value == 0) {
				String id = response.getHits().getAt(0).getId();
				success = update(id, template);
			} else {
				success = deleteAndCreate(totalHits.value, template);
			}

			// 上传百度LBS数据
			ServiceResult serviceResult = addressService.lbsUpload(location.getResult(), house.getStreet() + house.getDistrict(),
					city.getCnName() + region.getCnName() + house.getStreet() + house.getDistrict(),
					message.getHouseId(), house.getPrice(), house.getArea());

			if (!success || !serviceResult.isSuccess()) {
				this.index(message.getHouseId(), message.getRetry() + 1);
			} else {
				log.debug("Index success with house{}", houseId);
			}
		} catch(IOException ex) {
			log.error("方法:{};异常信息:{}", SearchServiceImpl.class.getName()+".createOrUpdateIndex()", ex.getMessage());
		}
	}

	/**
	 * 文档处理失败的重试机制
	 * @param houseId
	 * @param retry
	 */
	private void index(Long houseId, int retry) {
		if (retry > HouseIndexMessage.MAX_RETRY) {
			log.error("方法:{};Retry index times over 3 for house: {} Please check it!", SearchServiceImpl.class.getName()+".index()", houseId);
			return;
		}

		HouseIndexMessage message = new HouseIndexMessage(houseId, HouseIndexMessage.INDEX, retry);
		try {
			// 重新发送数据到 MQ
			kafkaTemplate.send(INDEX_TOPIC, objectMapper.writeValueAsString(message));
		} catch (JsonProcessingException e) {
			log.error("方法:{};Json encode error for:{};异常信息:{}", SearchServiceImpl.class.getName()+".index()", message, e.getMessage());
		}
	}

	@Override
	public void index(Long houseId) {
		this.index(houseId, 0);
	}

	/**
	 * 添加文档
	 * @param template
	 * @return
	 */
	private boolean create(HouseIndexTemplate template) {
		if(!updateSuggest(template)) {
			return false;
		}

		try {
			IndexRequest indexRequest = new IndexRequest();
			indexRequest.index(INDEX_NAME).source(objectMapper.writeValueAsBytes(template), XContentType.JSON);
			IndexResponse response = client.index(indexRequest, RequestOptions.DEFAULT);
			if (response.status() == RestStatus.CREATED) {
				return true;
			} else {
				return false;
			}
		} catch (IOException e) {
			log.error("方法:{};异常信息:{}", SearchServiceImpl.class.getName()+".create()", e.getMessage());
			return false;
		}
	}

	/**
	 * 更新索引
	 * @param indexId
	 * @param indexTemplate
	 * @return
	 */
	private boolean update(String indexId, HouseIndexTemplate indexTemplate) {
		if(!updateSuggest(indexTemplate)) {
			return false;
		}

		try {
			UpdateRequest updateRequest = new UpdateRequest();
			updateRequest.index(INDEX_NAME).id(indexId).doc(XContentType.JSON, indexTemplate);
			UpdateResponse response = client.update(updateRequest, RequestOptions.DEFAULT);

			if (response.status() == RestStatus.OK) {
				return true;
			} else {
				return false;
			}
		} catch (IOException e) {
			log.error("方法:{};异常信息:{}", SearchServiceImpl.class.getName()+".update()", e.getMessage());
			return false;
		}
	}

	/**
	 * 删除或添加文档
	 * @param totalHit
	 * @param indexTemplate
	 * @return
	 */
	private boolean deleteAndCreate(long totalHit, HouseIndexTemplate indexTemplate) {
		DeleteByQueryRequest deleteRequest = new DeleteByQueryRequest();
		deleteRequest.indices(INDEX_NAME);
		deleteRequest.setScroll(TimeValue.timeValueSeconds(60));
		deleteRequest.setQuery(QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID, indexTemplate.getHouseId()));
		try {
			log.info("删除文档条件:{}", JSON.toJSONString(deleteRequest));
			BulkByScrollResponse response = client.deleteByQuery(deleteRequest, RequestOptions.DEFAULT);
			long deleted = response.getDeleted();
			if (deleted != totalHit) {
				log.warn("方法:{},Need delete {}, but {} was deleted!", SearchServiceImpl.class.getName()+".deleteAndCreate()", totalHit, deleted);
				return false;
			} else {
				return create(indexTemplate);
			}
		} catch (IOException e) {
			log.error("方法:{};异常信息:{}", SearchServiceImpl.class.getName()+".deleteAndCreate()", e.getMessage());
		}
		return false;
	}

	@Override
	public void remove(Long houseId) {
		this.remove(houseId, 0);
	}

	@Override
	public ServiceMultiResult<Long> query(RentSearch rentSearch) {
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
		// 过滤，精准查询
		boolQueryBuilder.filter(QueryBuilders.termQuery(HouseIndexKey.CITY_EN_NAME, rentSearch.getCityEnName()));
		if(StrUtil.isNotBlank(rentSearch.getRegionEnName()) && !"*".equals(rentSearch.getRegionEnName())) {
			boolQueryBuilder.filter(QueryBuilders.termQuery(HouseIndexKey.REGION_EN_NAME, rentSearch.getRegionEnName()));
		}

		// 获取房屋的面积区间
		RentValueBlock area = RentValueBlock.matchArea(rentSearch.getAreaBlock());
		if(!RentValueBlock.ALL.equals(area)) {
			RangeQueryBuilder areaRangeQuery = QueryBuilders.rangeQuery(HouseIndexKey.AREA);
			if(area.getMax() > 0) {
				areaRangeQuery.lte(area.getMax());
			}
			if(area.getMin() > 0) {
				areaRangeQuery.gte(area.getMin());
			}
			boolQueryBuilder.filter(areaRangeQuery);
		}

		// 价格区间
		RentValueBlock price = RentValueBlock.matchPrice(rentSearch.getPriceBlock());
		if(!RentValueBlock.ALL.equals(price)) {
			RangeQueryBuilder priceRangeQuery = QueryBuilders.rangeQuery(HouseIndexKey.PRICE);
			if(price.getMax() > 0) {
				priceRangeQuery.lte(price.getMax());
			}
			if(price.getMin() > 0) {
				priceRangeQuery.gte(price.getMin());
			}
			boolQueryBuilder.filter(priceRangeQuery);
		}

		if(rentSearch.getDirection() > 0) {
			boolQueryBuilder.filter(QueryBuilders.termQuery(HouseIndexKey.DIRECTION, rentSearch.getDirection()));
		}

		// 房屋出租的方式
		if(rentSearch.getRentWay() > -1) {
			boolQueryBuilder.filter(QueryBuilders.termQuery(HouseIndexKey.RENT_WAY, rentSearch.getRentWay()));
		}

		//boolQueryBuilder.must(QueryBuilders.matchQuery(HouseIndexKey.TITLE, rentSearch.getKeywords()).boost(2.0f));

		// 关键字与多字段匹配
		boolQueryBuilder.must(QueryBuilders.multiMatchQuery(rentSearch.getRentWay(),
				HouseIndexKey.TITLE,
				HouseIndexKey.TRAFFIC,
				HouseIndexKey.DISTRICT,
				HouseIndexKey.ROUND_SERVICE,
				HouseIndexKey.SUBWAY_LINE_NAME,
				HouseIndexKey.SUBWAY_STATION_NAME));

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(boolQueryBuilder);
		searchSourceBuilder.from(rentSearch.getStart());
		searchSourceBuilder.size(rentSearch.getSize());
		searchSourceBuilder.fetchSource(HouseIndexKey.HOUSE_ID, null);
		searchSourceBuilder.sort(HouseSort.getSortKey(rentSearch.getOrderBy()), SortOrder.fromString(rentSearch.getOrderDirection()));

		SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
		searchRequest.source(searchSourceBuilder);
		log.info("搜索条件:{}", JSON.toJSONString(searchRequest));
		List<Long> houseIds = new ArrayList<>();
		long total = 0L;
		try {
			SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

			if (response.status() != RestStatus.OK) {
				log.warn("Search status is no ok for:{}", searchRequest.toString());
				return new ServiceMultiResult<>(0, houseIds);
			}

			for(SearchHit hit : response.getHits()) {
				houseIds.add(Longs.tryParse(String.valueOf(hit.getSourceAsMap().get(HouseIndexKey.HOUSE_ID))));
			}

			TotalHits totalHits = response.getHits().getTotalHits();
			total = totalHits.value;
		} catch (IOException e) {
			log.error("方法:{};异常信息:{}", SearchServiceImpl.class.getName()+".query()", e.getMessage());
			return new ServiceMultiResult<>(0, null);
		}
		return new ServiceMultiResult<>(total, houseIds);
	}

	@Override
	public ServiceResult<List<String>> suggest(String prefix) {
		SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		// 搜索时自动补全功能
		SuggestionBuilder suggestionBuilder = SuggestBuilders.completionSuggestion("suggest")
				.prefix(prefix)
				.size(10);

		SuggestBuilder suggestBuilder = new SuggestBuilder();
		suggestBuilder.addSuggestion("autocomplete", suggestionBuilder);
		searchSourceBuilder.suggest(suggestBuilder);
		searchRequest.source(searchSourceBuilder);
		log.info("自动补全搜索条件:{}", JSON.toJSONString(searchRequest));
		try {
			SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

			//处理返回结果
			Suggest suggest = searchResponse.getSuggest();
			if (suggest == null) {
				log.warn("未查询到补全数据!");
				return ServiceResult.of(new ArrayList<>());
			}

			// 支持自动补全搜索的字段有 autocomplete
			CompletionSuggestion termSuggestion = suggest.getSuggestion("autocomplete");

			List<CompletionSuggestion.Entry> entries = termSuggestion.getEntries();
			/*List<String> suggestList = new ArrayList<>();
			for (CompletionSuggestion.Entry entry : entries) {
				for (CompletionSuggestion.Entry.Option option : entry) {
					String suggestText = option.getText().string();
					if(!suggestList.contains(suggestText)){
						suggestList.add(suggestText);
					}
				}
			}
			return ServiceResult.of(suggestList);*/

			int maxSuggest = 0;
			Set<String> suggestSet = new HashSet<>();
			for (CompletionSuggestion.Entry entry : entries) {
				if(entry.getOptions().isEmpty()) {
					continue;
				}
				for(CompletionSuggestion.Entry.Option option : entry) {
					String tip = option.getText().string();
					if(suggestSet.contains(tip)) {
						continue;
					}
					suggestSet.add(tip);
					maxSuggest++;
				}
				if(maxSuggest >= 10) {
					break;
				}
			}
			List<String> suggests = Lists.newArrayList(suggestSet.toArray(new String[]{}));
			return ServiceResult.of(suggests);
		} catch (IOException e) {
			log.error("方法:{};异常信息:{}", SearchServiceImpl.class.getName()+".suggest()", e.getMessage());
			return ServiceResult.of(new ArrayList<>());
		}
	}

	@Override
	public ServiceResult<Long> aggregateDistrictHouse(String cityEnName, String regionEnName, String district) {
		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
				.filter(QueryBuilders.termQuery(HouseIndexKey.CITY_EN_NAME, cityEnName))
				.filter(QueryBuilders.termQuery(HouseIndexKey.REGION_EN_NAME, regionEnName))
				.filter(QueryBuilders.termQuery(HouseIndexKey.DISTRICT, district));

		SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(boolQuery);
		searchSourceBuilder.aggregation(AggregationBuilders
				.terms(HouseIndexKey.AGG_DISTRICT)
				.field(HouseIndexKey.DIRECTION));
		searchSourceBuilder.size(0);
		log.info("聚合搜索条件:{}", JSON.toJSONString(searchRequest));
		searchRequest.source(searchSourceBuilder);

		try {
			SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
			if(response.status() == RestStatus.OK) {
				Aggregations aggregations = response.getAggregations();
				Terms terms = aggregations.get(HouseIndexKey.AGG_DISTRICT);
				if(CollectionUtil.isNotEmpty(terms.getBuckets())) {
					return ServiceResult.of(terms.getBucketByKey(district).getDocCount());
				}
			} else {
				log.warn("Failed to Aggregate for:{}", HouseIndexKey.AGG_DISTRICT);
			}
		} catch (IOException e) {
			log.error("方法:{};异常信息:{}", SearchServiceImpl.class.getName()+".aggregateDistrictHouse()", e.getMessage());
		}

		return ServiceResult.of(0L);
	}

	@Override
	public ServiceMultiResult<HouseBucketDTO> mapAggregate(String cityEnName) {
		// 过滤条件; filter 不需要计算相关度分数, 性能较高
		BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
		boolQueryBuilder.filter(QueryBuilders.termQuery(HouseIndexKey.CITY_EN_NAME, cityEnName));

		// 分组聚合
		AggregationBuilder aggregationBuilder = AggregationBuilders.terms(HouseIndexKey.AGG_REGION)
				.field(HouseIndexKey.REGION_EN_NAME);

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(boolQueryBuilder)
				.aggregation(aggregationBuilder);

		SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
		searchRequest.source(searchSourceBuilder);

		try {
			SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
			List<HouseBucketDTO> buckets = new ArrayList<>();
			if (response.status() != RestStatus.OK) {
				log.warn("Aggregate status is not ok for:{}", JSON.toJSONString(searchRequest));
				return new ServiceMultiResult<>(0, buckets);
			}

			Terms terms = response.getAggregations().get(HouseIndexKey.AGG_REGION);
			for(Terms.Bucket bucket : terms.getBuckets()) {
				buckets.add(new HouseBucketDTO(bucket.getKeyAsString(), bucket.getDocCount()));
			}
			return new ServiceMultiResult<>(response.getHits().getTotalHits().value, buckets);
		} catch (IOException e) {
			log.error("方法:{};异常信息:{}", SearchServiceImpl.class.getName()+".mapAggregate()", e.getMessage());
		}
		return new ServiceMultiResult<>(0, new ArrayList<>());
	}

	@Override
	public ServiceMultiResult<Long> mapQuery(String cityEnName, String orderBy, String orderDirection,
	                                         int start, int size) {
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
				.filter(QueryBuilders.termQuery(HouseIndexKey.CITY_EN_NAME, cityEnName));

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(boolQueryBuilder)
				.sort(HouseSort.getSortKey(orderBy), SortOrder.fromString(orderDirection))
				.from(start)
				.size(size);

		SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
		searchRequest.source(searchSourceBuilder);

		try {
			SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

			List<Long> houseIds = new ArrayList<>();
			if (response.status() != RestStatus.OK) {
				log.warn("Search status is not ok for:{}", JSON.toJSONString(searchRequest));
				return new ServiceMultiResult<>(0, houseIds);
			}

			for(SearchHit hit : response.getHits()) {
				houseIds.add(Longs.tryParse(String.valueOf(hit.getSourceAsMap().get(HouseIndexKey.HOUSE_ID))));
			}
			return new ServiceMultiResult<>(response.getHits().getTotalHits().value, houseIds);
		} catch (IOException e) {
			log.error("方法:{};异常信息:{}", SearchServiceImpl.class.getName()+".mapQuery()", e.getMessage());
		}

		return new ServiceMultiResult<>(0, new ArrayList<>());
	}

	@Override
	public ServiceMultiResult<Long> mapQuery(MapSearch mapSearch) {
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
				.filter(QueryBuilders.termQuery(HouseIndexKey.CITY_EN_NAME, mapSearch.getCityEnName()));

		// 地理位置搜索
		boolQueryBuilder.filter(QueryBuilders.geoBoundingBoxQuery("location").setCorners(
				new GeoPoint(mapSearch.getLeftLatitude(), mapSearch.getLeftLongitude()),
				new GeoPoint(mapSearch.getRightLatitude(), mapSearch.getRightLongitude())
		));

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(boolQueryBuilder)
				.sort(HouseSort.getSortKey(mapSearch.getOrderBy()), SortOrder.fromString(mapSearch.getOrderDirection()))
				.from(mapSearch.getStart())
				.size(mapSearch.getSize());

		SearchRequest searchRequest = new SearchRequest(INDEX_NAME);

		try {
			List<Long> houseIds = new ArrayList<>();
			SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
			if (RestStatus.OK != response.status()) {
				log.warn("Search status is not ok for:{}", JSON.toJSONString(searchRequest));
				return new ServiceMultiResult<>(0, houseIds);
			}

			for (SearchHit hit : response.getHits()) {
				houseIds.add(Longs.tryParse(String.valueOf(hit.getSourceAsMap().get(HouseIndexKey.HOUSE_ID))));
			}
			return new ServiceMultiResult<>(response.getHits().getTotalHits().value, houseIds);
		} catch (IOException e) {
			log.error("地理位置,方法:{};异常信息:{}", SearchServiceImpl.class.getName()+".mapQuery()", e.getMessage());
		}

		return new ServiceMultiResult<>(0, new ArrayList<>());
	}

	private boolean updateSuggest(HouseIndexTemplate indexTemplate) {
		// 分词
		/*AnalyzeRequestBuilder requestBuilder = new AnalyzeRequestBuilder(
				elasticsearchTemplate.getClient(), AnalyzeAction.INSTANCE, INDEX_NAME, indexTemplate.getTitle(),
				indexTemplate.getLayoutDesc(), indexTemplate.getRoundService(),
				indexTemplate.getDescription(), indexTemplate.getSubwayLineName(),
				indexTemplate.getSubwayStationName());*/
		// ik-max-word
		AnalyzeRequest analyzeRequest = AnalyzeRequest.withIndexAnalyzer(INDEX_NAME, "ik_smart", indexTemplate.getTitle(),
				indexTemplate.getLayoutDesc(), indexTemplate.getRoundService(),
				indexTemplate.getDescription(), indexTemplate.getSubwayLineName(),
				indexTemplate.getSubwayStationName());
		log.info("分词请求:{}", JSON.toJSONString(analyzeRequest));
		try {
			AnalyzeResponse response = client.indices().analyze(analyzeRequest, RequestOptions.DEFAULT);
			List<AnalyzeResponse.AnalyzeToken> tokens = response.getTokens();
			if(CollectionUtil.isEmpty(tokens)) {
				log.warn("Can not analyze token for house: {}", indexTemplate.getHouseId());
				return false;
			}

			List<HouseSuggest> suggests = new ArrayList<>();
			for(AnalyzeResponse.AnalyzeToken token : tokens) {
				// 排序数字类型 & 小于2个字符的分词结果
				if ("<NUM>".equals(token.getType()) || token.getTerm().length() < 2) {
					continue;
				}

				HouseSuggest suggest = new HouseSuggest();
				suggest.setInput(token.getTerm());
				suggests.add(suggest);
			}

			// 定制化小区自动补全
			HouseSuggest suggest = new HouseSuggest();
			suggest.setInput(indexTemplate.getDistrict());
			suggests.add(suggest);

			indexTemplate.setSuggest(suggests);
			return true;
		} catch (IOException e) {
			log.error("方法:{};异常信息:{}", SearchServiceImpl.class.getName()+".updateSuggest()", e.getMessage());
			return false;
		}
	}

	/**
	 * 删除 索引文档
	 * @param houseId
	 * @param retry
	 */
	private void remove(Long houseId, int retry) {
		if (retry > HouseIndexMessage.MAX_RETRY) {
			log.error("Retry remove times over 3 for house: " + houseId + " Please check it!");
			return;
		}

		// 消息的处理操作设置为 remove
		HouseIndexMessage message = new HouseIndexMessage(houseId, HouseIndexMessage.REMOVE, retry);
		try {
			// 重新发送消息到 MQ
			this.kafkaTemplate.send(INDEX_TOPIC, objectMapper.writeValueAsString(message));
		} catch (JsonProcessingException e) {
			log.error("方法:{};错误信息:{};异常信息:{}", SearchServiceImpl.class.getName()+".remove()","Cannot encode json for " + message, e.getMessage());
		}
	}
}
