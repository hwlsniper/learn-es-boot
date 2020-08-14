package com.learn.es.template;

import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.learn.es.SpringBootEsApplicationTests;
import com.learn.es.constants.ESConstant;
import com.learn.es.model.Book;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.open.OpenIndexRequest;
import org.elasticsearch.action.admin.indices.open.OpenIndexResponse;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsRequest;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.client.indices.*;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.template.HighLevelClientTest
 * @description RestHighLevelClient 高级客户端
 * @date 2020/8/7 11:24
 */
@Slf4j
public class HighLevelClientTest extends SpringBootEsApplicationTests {

	@Autowired
	private RestHighLevelClient client;

	/**
	 * 创建索引
	 */
	@Test
	public void testCreateIndex1() throws IOException {
		CreateIndexRequest request = new CreateIndexRequest(ESConstant.INDEX_T);
		request.settings(Settings.builder()
				.put("index.number_of_shards", "5")
				.put("index.number_of_replicas", "1"));

		/***** 构建 mapping ****/
		Map<String, Object> message = new HashMap<>();
		message.put("type", "text");
		Map<String, Object> name = new HashMap<>();
		name.put("type", "keyword");
		Map<String, Object> title = new HashMap<>();
		title.put("type", "text");
		Map<String, Object> properties = new HashMap<>();
		properties.put("message", message);
		properties.put("name", name);
		properties.put("title", title);
		Map<String, Object> mapping = new HashMap<>();
		mapping.put("properties", properties);
		request.mapping(mapping);

		CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);
		log.info("创建 index,{}", createIndexResponse.toString());
	}

	/**
	 * 创建索引
	 */
	@Test
	public void testCreateIndex2() throws IOException {
		CreateIndexRequest request = new CreateIndexRequest(ESConstant.INDEX_Y);
		request.settings(Settings.builder()
				.put("index.number_of_shards", "5")
				.put("index.number_of_replicas", "1"));

		// 类似 json 结构
		XContentBuilder builder = XContentFactory.jsonBuilder();
		builder.startObject();
		{
			builder.startObject("properties");
			{
				builder.startObject("message");
				{
					builder.field("type", "text");
				}
				builder.endObject();

				builder.startObject("title");
				{
					// 数据类型
					builder.field("type","text");
					// 分词器
					builder.field("analyzer","ik_max_word");
				}
				builder.endObject();
			}
			builder.endObject();
		}
		builder.endObject();
		request.mapping(builder);

		CreateIndexResponse response = client.indices().create(request, RequestOptions.DEFAULT);
		log.info("index:{}", response.index());
	}

	/**
	 * 删除索引
	 * @throws IOException
	 */
	@Test
	public void testDeleteIndex() throws IOException {
		DeleteIndexRequest deleteRequest = new DeleteIndexRequest(ESConstant.INDEX_Y);
		AcknowledgedResponse response = client.indices().delete(deleteRequest, RequestOptions.DEFAULT);
	}

	/**
	 * 判断索引是否存在
	 */
	@Test
	public void testExistsIndex() throws IOException {
		GetIndexRequest request = new GetIndexRequest(ESConstant.INDEX_T);
		boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
	}

	/**
	 * 打开索引
	 * @throws IOException
	 */
	@Test
	public void testOpenIndex() throws IOException {
		OpenIndexRequest request = new OpenIndexRequest(ESConstant.INDEX_T);
		OpenIndexResponse openIndexResponse = client.indices().open(request, RequestOptions.DEFAULT);
		log.info("打开索引:{}", openIndexResponse.toString());
	}

	/**
	 * 获取索引的mapping
	 * throws IOException
	 */
	@Test
	public void testGetMapping() throws IOException {
		GetMappingsRequest request = new GetMappingsRequest();
		request.indices(ESConstant.INDEX_T);

		GetMappingsResponse response = client.indices().getMapping(request, RequestOptions.DEFAULT);

		Map<String, MappingMetaData> mappings = response.mappings();
		MappingMetaData mappingMetaData = mappings.get(ESConstant.INDEX_T);
		Map<String, Object> stringObjectMap = mappingMetaData.sourceAsMap();

		for(String key : stringObjectMap.keySet()) {
			log.info("key:{}; value:{}", key, stringObjectMap.get(key));
		}
	}

	/**
	 * 获取索引的Settings信息
	 */
	@Test
	public void testGetSettings() throws IOException {
		GetSettingsRequest request = new GetSettingsRequest().indices(ESConstant.INDEX_T);
		GetSettingsResponse response = client.indices().getSettings(request, RequestOptions.DEFAULT);

		String number_of_shards = response.getSetting(ESConstant.INDEX_T, "index.number_of_shards");
		String number_of_replicas = response.getSetting(ESConstant.INDEX_T, "index.number_of_replicas");

		log.info("number_of_shards:{};number_of_replicas:{}",number_of_shards,number_of_replicas);
	}

	/**
	 * 添加文档 Document
	 */
	@Test
	public void testInsertDocument() throws IOException {
		Map<String, Object> jsonMap = new HashMap<>();
		jsonMap.put("number", 125L);
		jsonMap.put("create_time", new Date());
		jsonMap.put("price", 35.5d);
		jsonMap.put("name", "测试一下");
		jsonMap.put("title", "测试数据001");
		IndexRequest indexRequest = new IndexRequest(ESConstant.INDEX_BOOK)
				.id("8").source(jsonMap);
		IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
		log.info("indexResponse:{}",indexResponse.toString());
	}

	/**
	 * 根据文档id查询
	 */
	@Test
	public void testGetDocumentById() throws IOException {
		GetRequest request = new GetRequest(ESConstant.INDEX_BOOK, "8");

		/**
		 * 查询指定字段
		 */
		/*
		// 指定查询的字段
		String[] includes = new String[]{"name", "create_time","title"};
		// 排除查询的字段
		String[] excludes = Strings.EMPTY_ARRAY;
		FetchSourceContext fetchSourceContext = new FetchSourceContext(true, includes, excludes);
		request.fetchSourceContext(fetchSourceContext);*/

		/**
		 * 排除查询指定字段
		 */
		String[] includes = Strings.EMPTY_ARRAY;
		String[] excludes = new String[]{"name"};
		FetchSourceContext fetchSourceContext = new FetchSourceContext(true, includes, excludes);
		request.fetchSourceContext(fetchSourceContext);

		GetResponse response = client.get(request, RequestOptions.DEFAULT);

		String index = response.getIndex();
		String id = response.getId();
		if (response.isExists()) {
			long version = response.getVersion();
			log.info("查询版本号，version:{}",version);

			String sourceAsString = response.getSourceAsString();
			log.info("查询结果string类型，sourceAsString:{}",sourceAsString);

			Map<String, Object> sourceAsMap = response.getSourceAsMap();
			log.info("查询结果map类型，sourceAsMap:{}",sourceAsMap);

			byte[] sourceAsBytes = response.getSourceAsBytes();
			log.info("倒排索引，sourceAsBytes:{}",sourceAsBytes);
		} else {
			log.info("暂无数据");
		}
		log.info("index:{}",index);
		log.info("id:{}",id);
	}

	/**
	 * 判断文档是否存在
	 */
	@Test
	public void testExistsDocument() throws IOException {
		GetRequest request = new GetRequest(ESConstant.INDEX_BOOK, "8");
		// 禁用获取 _source
		request.fetchSourceContext(new FetchSourceContext(false));
		// 禁用获取存储的字段
		request.storedFields("_none_");
		boolean exists = client.exists(request, RequestOptions.DEFAULT);
	}

	/**
	 * 修改文档
	 */
	@Test
	public void testUpdateDocument() throws IOException {
		UpdateRequest request = new UpdateRequest(ESConstant.INDEX_BOOK, "8")
				.doc("name","测试一下3","title","我就是在测试更新3");

		UpdateResponse updateResponse = client.update(request, RequestOptions.DEFAULT);

		String index = updateResponse.getIndex();
		log.info("index:{}",index);
		String id = updateResponse.getId();
		log.info("id:{}",id);
		long version = updateResponse.getVersion();
		log.info("version:{}",version);

		if (updateResponse.getResult() == DocWriteResponse.Result.CREATED) {
			log.info("处理首次创建文档的情况");
		} else if (updateResponse.getResult() == DocWriteResponse.Result.UPDATED) {
			log.info("处理文档更新的情况");
		} else if (updateResponse.getResult() == DocWriteResponse.Result.DELETED) {
			log.info("处理文件被删除的情况");
		} else if (updateResponse.getResult() == DocWriteResponse.Result.NOOP) {
			log.info("处理文档不受更新影响的情况，即未对文档执行任何操作（空转）");
		}
	}

	/**
	 * 删除文档
	 */
	@Test
	public void testDeleteDocument() throws IOException {
		DeleteRequest request = new DeleteRequest(ESConstant.INDEX_BOOK, "8");
		DeleteResponse deleteResponse = client.delete(request, RequestOptions.DEFAULT);
		log.info("deleteResponse:{}",deleteResponse);

	}

	/**
	 * 查询文档
	 */
	@Test
	public void testSearch1() throws IOException {
		SearchRequest searchRequest = new SearchRequest(ESConstant.INDEX_BOOK);
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(QueryBuilders.matchQuery("name", "建新"));
		searchRequest.source(searchSourceBuilder);

		SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
		// 最高相关度分数
		float maxScore = searchResponse.getHits().getMaxScore();
		TotalHits totalHits = searchResponse.getHits().getTotalHits();
		SearchHit[] hits = searchResponse.getHits().getHits();

		for (SearchHit hit : hits) {
			log.info("id:{}", hit.getId());
			log.info("索引名:{}", hit.getIndex());
			log.info("分数:{}", hit.getScore());
			log.info("string:{}", hit.getSourceAsString());
			log.info("map:{}", hit.getSourceAsMap());
		}

		log.info("totalHits value:{}",totalHits.value);
		log.info("totalHits relation:{}",totalHits.relation);
		log.info("maxScore:{}",maxScore);
		log.info("searchResponse:{}",searchResponse);
	}

	@Test
	public void testSearch2() throws IOException {
		/**
		 * 指定查询 book 索引，不指定则查询所有索引
		 */
		SearchRequest searchRequest = new SearchRequest(ESConstant.INDEX_BOOK);
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		sourceBuilder.query(QueryBuilders.matchQuery("title","夏竹 建南"));
		// 分页查询
		sourceBuilder.from(0);
		sourceBuilder.size(1);
		// 超时时间
		sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
		searchRequest.source(sourceBuilder);

		SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
		SearchHit[] hits;
		hits = searchResponse.getHits().getHits();

		for (SearchHit hit : hits) {
			log.info("id:{}", hit.getId());
			log.info("索引名:{}", hit.getIndex());
			log.info("分数:{}", hit.getScore());
			log.info("string:{}", hit.getSourceAsString());
			log.info("map:{}", hit.getSourceAsMap());
		}
	}

	/**
	 * 统计
	 * throws IOException
	 */
	@Test
	public void testStatistical() throws IOException {
		CountRequest countRequest = new CountRequest();
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(QueryBuilders.matchAllQuery());
		countRequest.query(searchSourceBuilder.query());

		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		sourceBuilder.query(QueryBuilders.matchQuery("name", "php"));
		countRequest.query(sourceBuilder.query());

		CountResponse countResponse = client.count(countRequest, RequestOptions.DEFAULT);
		long count = countResponse.getCount();
	}

	/**
	 * 写入文档
	 * @throws IOException
	 */
	@Test
	public void testInsertBook() throws IOException {
		Book book = new Book(10,126L, LocalDateTime.now(),99.9,"康东伟教你学es","由康东伟呕心沥血创作完成的作品");
		IndexRequest indexRequest = new IndexRequest();
		indexRequest
				.index(ESConstant.INDEX_BOOK)
				.id(book.getId().toString())
				.timeout(TimeValue.timeValueSeconds(5));
		indexRequest.source(book, XContentType.JSON);
		IndexResponse response = client.index(indexRequest, RequestOptions.DEFAULT);
		log.info("status:{}", response.status());
	}

	/**
	 * 判断索引中文档是否存在
	 * @throws IOException
	 */
	@Test
	public void testExistsBook() throws IOException {
		GetRequest getRequest = new GetRequest(ESConstant.INDEX_BOOK).id("10");

		boolean exists = client.exists(getRequest, RequestOptions.DEFAULT);
		if (exists) {
			GetResponse response = client.get(getRequest, RequestOptions.DEFAULT);
		}
	}

	/**
	 * 更新索引中的文档
	 * @throws IOException
	 */
	@Test
	public void testUpdateBook() throws IOException {
		UpdateRequest updateRequest = new UpdateRequest(ESConstant.INDEX_BOOK,"10");
		updateRequest.timeout("5s");
		Book book = new Book();
		book.setTitle("康东伟再次教你学习es");
		updateRequest.doc(JSONUtil.toJsonPrettyStr(book), XContentType.JSON);
		UpdateResponse update = client.update(updateRequest, RequestOptions.DEFAULT);
	}

	/**
	 * 批量插入
	 */
	@Test
	public void testBulkInsertBook() throws IOException {
		BulkRequest bulkRequest = new BulkRequest();
		bulkRequest.timeout("10s");

		List<Book> books = new ArrayList<>();
		books.add(new Book(21,127L, LocalDateTime.now(),19.9,"康东伟教你学C","由康东伟呕心沥血创作完成的作品"));
		books.add(new Book(22,128L, LocalDateTime.now(),29.9,"康东伟教你学C++","由康东伟呕心沥血创作完成的作品"));
		books.add(new Book(23,129L, LocalDateTime.now(),39.9,"康东伟教你学C#","由康东伟呕心沥血创作完成的作品"));
		books.add(new Book(24,130L, LocalDateTime.now(),49.9,"康东伟教你学RABBIT","由康东伟呕心沥血创作完成的作品"));
		books.add(new Book(25,131L, LocalDateTime.now(),59.9,"康东伟教你学KAFKA","由康东伟呕心沥血创作完成的作品"));
		books.add(new Book(26,132L, LocalDateTime.now(),69.9,"康东伟教你学MYSQL","由康东伟呕心沥血创作完成的作品"));
		books.add(new Book(27,133L, LocalDateTime.now(),79.9,"康东伟教你学ORACLE","由康东伟呕心沥血创作完成的作品"));
		books.add(new Book(28,134L, LocalDateTime.now(),89.9,"康东伟教你学DB2","由康东伟呕心沥血创作完成的作品"));
		books.add(new Book(29,135L, LocalDateTime.now(),99.9,"康东伟教你学SQL-SERVER","由康东伟呕心沥血创作完成的作品"));
		books.add(new Book(30,136L, LocalDateTime.now(),109.9,"康东伟教你学DBA","由康东伟呕心沥血创作完成的作品"));

		for(Book book : books) {
			bulkRequest.add(new IndexRequest(ESConstant.INDEX_BOOK)
					.id(book.getId().toString())
					.source(JSONUtil.toJsonPrettyStr(book), XContentType.JSON));
		}

		client.bulk(bulkRequest, RequestOptions.DEFAULT);
	}

	/**
	 * 批量修改
	 * @throws IOException
	 */
	@Test
	public void testBulkUpdateBook() throws IOException {
		BulkRequest bulkRequest = new BulkRequest();
		bulkRequest.timeout("10s");

		ArrayList<Book> books = new ArrayList<>();
		books.add(new Book(21,127L, LocalDateTime.now(),19.9,"2康东伟教你学JAVA","由康东伟呕心沥血创作完成的作品"));
		books.add(new Book(22,128L, LocalDateTime.now(),29.9,"2康东伟教你学PHP","由康东伟呕心沥血创作完成的作品"));
		books.add(new Book(23,129L, LocalDateTime.now(),39.9,"2康东伟教你学PYTHON","由康东伟呕心沥血创作完成的作品"));
		books.add(new Book(24,130L, LocalDateTime.now(),49.9,"2康东伟教你学JSON","由康东伟呕心沥血创作完成的作品"));
		books.add(new Book(25,131L, LocalDateTime.now(),59.9,"2康东伟教你学HTML","由康东伟呕心沥血创作完成的作品"));
		books.add(new Book(26,132L, LocalDateTime.now(),69.9,"2康东伟教你学CSS","由康东伟呕心沥血创作完成的作品"));
		books.add(new Book(27,133L, LocalDateTime.now(),79.9,"2康东伟教你学JAVASCRIPT","由康东伟呕心沥血创作完成的作品"));
		books.add(new Book(28,134L, LocalDateTime.now(),89.9,"2康东伟教你学SPRING","由康东伟呕心沥血创作完成的作品"));
		books.add(new Book(29,135L, LocalDateTime.now(),99.9,"2康东伟教你学MYBATIS","由康东伟呕心沥血创作完成的作品"));
		books.add(new Book(30,136L, LocalDateTime.now(),109.9,"2康东伟教你学JPA","由康东伟呕心沥血创作完成的作品"));

		for (Book book : books) {
			bulkRequest.add(new UpdateRequest(ESConstant.INDEX_BOOK, book.getId().toString())
					.doc(JSONUtil.toJsonPrettyStr(book), XContentType.JSON));

		}

		BulkResponse response = client.bulk(bulkRequest, RequestOptions.DEFAULT);
		log.info("结果：{}", response.hasFailures());
	}

	/**
	 *批量删除
	 */
	@Test
	public void testBulkDeleteBook() throws IOException {
		BulkRequest bulkRequest = new BulkRequest();
		bulkRequest.timeout("10s");

		ArrayList<Book> books = new ArrayList<>();
		books.add(new Book(21,127L, LocalDateTime.now(),19.9,"2康东伟教你学JAVA","由康东伟呕心沥血创作完成的作品"));
		books.add(new Book(22,128L, LocalDateTime.now(),29.9,"2康东伟教你学PHP","由康东伟呕心沥血创作完成的作品"));
		books.add(new Book(23,129L, LocalDateTime.now(),39.9,"2康东伟教你学PYTHON","由康东伟呕心沥血创作完成的作品"));
		books.add(new Book(24,130L, LocalDateTime.now(),49.9,"2康东伟教你学JSON","由康东伟呕心沥血创作完成的作品"));
		books.add(new Book(25,131L, LocalDateTime.now(),59.9,"2康东伟教你学HTML","由康东伟呕心沥血创作完成的作品"));
		books.add(new Book(26,132L, LocalDateTime.now(),69.9,"2康东伟教你学CSS","由康东伟呕心沥血创作完成的作品"));
		books.add(new Book(27,133L, LocalDateTime.now(),79.9,"2康东伟教你学JAVASCRIPT","由康东伟呕心沥血创作完成的作品"));
		books.add(new Book(28,134L, LocalDateTime.now(),89.9,"2康东伟教你学SPRING","由康东伟呕心沥血创作完成的作品"));
		books.add(new Book(29,135L, LocalDateTime.now(),99.9,"2康东伟教你学MYBATIS","由康东伟呕心沥血创作完成的作品"));
		books.add(new Book(30,136L, LocalDateTime.now(),109.9,"2康东伟教你学JPA","由康东伟呕心沥血创作完成的作品"));

		for (Book book : books) {
			bulkRequest.add(new DeleteRequest(ESConstant.INDEX_BOOK, book.getId().toString()));

		}

		BulkResponse response = client.bulk(bulkRequest, RequestOptions.DEFAULT);
		log.info("结果：{}", response.hasFailures());

	}

	/**
	 * 分页查询
	 */
	@Test
	public void testSearchBook() throws IOException {
		SearchRequest searchRequest = new SearchRequest(ESConstant.INDEX_BOOK);

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.from(10);
		searchSourceBuilder.size(5);
		TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("name", "康东伟");
		searchSourceBuilder.query(termQueryBuilder);
		searchSourceBuilder.timeout(new TimeValue(60,TimeUnit.SECONDS));

		searchRequest.source(searchSourceBuilder);
		SearchResponse search = client.search(searchRequest, RequestOptions.DEFAULT);

		SearchHits hits = search.getHits();

		log.info(JSONUtil.toJsonPrettyStr(hits));

		for (SearchHit hit : search.getHits().getHits()) {
			System.out.println(hit.getSourceAsMap());
		}
	}

	@Test
	public void searchHighPage() {
		try {
			ArrayList<Map<String, Object>> highPage = this.searchHighPage("短袖", 1, 10);
			log.info("数据:{}", JSONUtil.toJsonPrettyStr(highPage));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 高亮分页搜索
	 * @throws IOException
	 */
	public ArrayList<Map<String,Object>> searchHighPage(String keyWord, Integer page, Integer size) throws IOException {
		SearchRequest searchRequest = new SearchRequest(ESConstant.INDEX_GOODS_INFO);
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

		//精准匹配关键字
		/*TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("goodsName", keyWord);
		searchSourceBuilder.query(termQueryBuilder);*/

		MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("goodsName", keyWord);
		searchSourceBuilder.query(matchQueryBuilder);
		searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

		// 分页
		searchSourceBuilder.from(page);
		searchSourceBuilder.size(size);

		// 高亮
		HighlightBuilder highlightBuilder = new HighlightBuilder();
		// 高亮字段
		highlightBuilder.field(new HighlightBuilder.Field("goodsName"));
		highlightBuilder.requireFieldMatch(false);
		highlightBuilder.preTags("<span style='color:red'>");
		highlightBuilder.postTags("</span>");

		searchSourceBuilder.highlighter(highlightBuilder);
		searchRequest.source(searchSourceBuilder);

		SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

		ArrayList<Map<String,Object>> list = new ArrayList<>();
		//解析结果
		for (SearchHit hit : response.getHits().getHits()) {
			//解析高亮字段
			Map<String, HighlightField> highlightFields = hit.getHighlightFields();
			HighlightField title = highlightFields.get("goodsName");

			Map<String, Object> sourceAsMap = hit.getSourceAsMap();

			if (title!=null){
				Text[] fragments = title.fragments();
				String n_title="";
				for (Text text : fragments) {
					n_title+=text;
				}
				sourceAsMap.put("goodsName",n_title);
			}
			list.add(sourceAsMap);
		}

		return list;
	}

	public void sugget() {
		/*LinkedHashSet<String> returnSet = new LinkedHashSet<>();
		SuggestRequestBuilder suggestRequestBuilder = client.prepareSuggest(elasticsearchTemplate.getPersistentEntityFor(SuggestEntity.class).getIndexName());
		//全拼前缀匹配
		CompletionSuggestionBuilder fullPinyinSuggest = new CompletionSuggestionBuilder("full_pinyin_suggest")
				.field("full_pinyin").text(input).size(10);
		//汉字前缀匹配
		CompletionSuggestionBuilder suggestText = new CompletionSuggestionBuilder("suggestText")
				.field("suggestText").text(input).size(size);
		//拼音搜字母前缀匹配
		CompletionSuggestionBuilder prefixPinyinSuggest = new CompletionSuggestionBuilder("prefix_pinyin_text")
				.field("prefix_pinyin").text(input).size(size);
		suggestRequestBuilder = suggestRequestBuilder.addSuggestion(fullPinyinSuggest).addSuggestion(suggestText).addSuggestion(prefixPinyinSuggest);
		SuggestResponse suggestResponse = suggestRequestBuilder.execute().actionGet();
		Suggest.Suggestion prefixPinyinSuggestion = suggestResponse.getSuggest().getSuggestion("prefix_pinyin_text");
		Suggest.Suggestion fullPinyinSuggestion = suggestResponse.getSuggest().getSuggestion("full_pinyin_suggest");
		Suggest.Suggestion suggestTextsuggestion = suggestResponse.getSuggest().getSuggestion("suggestText");
		List<Suggest.Suggestion.Entry> entries = suggestTextsuggestion.getEntries();
		//汉字前缀匹配
		for (Suggest.Suggestion.Entry entry : entries) {
			List<Suggest.Suggestion.Entry.Option> options = entry.getOptions();
			for (Suggest.Suggestion.Entry.Option option : options) {
				returnSet.add(option.getText().toString());
			}
		}
		//全拼suggest补充
		if (returnSet.size() < 10) {
			List<Suggest.Suggestion.Entry> fullPinyinEntries = fullPinyinSuggestion.getEntries();
			for (Suggest.Suggestion.Entry entry : fullPinyinEntries) {
				List<Suggest.Suggestion.Entry.Option> options = entry.getOptions();
				for (Suggest.Suggestion.Entry.Option option : options) {
					if (returnSet.size() < 10) {
						returnSet.add(option.getText().toString());
					}
				}
			}
		}
		//首字母拼音suggest补充
		if (returnSet.size() == 0) {
			List<Suggest.Suggestion.Entry> prefixPinyinEntries = prefixPinyinSuggestion.getEntries();
			for (Suggest.Suggestion.Entry entry : prefixPinyinEntries) {
				List<Suggest.Suggestion.Entry.Option> options = entry.getOptions();
				for (Suggest.Suggestion.Entry.Option option : options) {
					returnSet.add(option.getText().toString());
				}
			}
		}*/
	}
}
