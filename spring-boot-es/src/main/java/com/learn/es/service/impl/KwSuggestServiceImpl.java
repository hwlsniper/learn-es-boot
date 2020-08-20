package com.learn.es.service.impl;

import com.learn.es.service.KwSuggestService;
import lombok.extern.log4j.Log4j2;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryShardContext;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.suggest.*;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

/**
 * @author he.wl
 * @version 1.0.0
 * @className com.learn.es.service.impl.KwSuggestServiceImpl
 * @description 搜索提示
 * @date 2020/8/19 18:13
 */
@Log4j2
@Service
public class KwSuggestServiceImpl implements KwSuggestService {

	@Autowired
	private RestHighLevelClient restHighLevelClient;

	/**
	 * 个性化搜索逻辑：
	 *  1: 记录用户搜索的关键字、次数和搜索时间
	 *  2: 根据时间和次数排序, 获取前几个所搜和最近搜索的关键字
	 *  3: 根据关键字计算索引文档相关度分数, 获取评分较高的文档
	 *  4: 从文档中提取关键字, 并将关键字返回给用户
	 * 以下只是搜索提示，并不是个性化搜索
	 * @param kw  搜索前缀
	 * @return
	 */
	@Override
	public List<String> GetKwSuggestList(String kw) {
		SearchRequest searchRequest = new SearchRequest("suggest_tset");

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

		SuggestBuilder suggestBuilder = new SuggestBuilder();
		suggestBuilder.addSuggestion("suggestText", SuggestBuilders.completionSuggestion("kwsuggest.suggestText").prefix(kw).skipDuplicates(true).size(5));
		suggestBuilder.addSuggestion("full_pinyin", SuggestBuilders.completionSuggestion("kwsuggest.full_pinyin").prefix(kw).skipDuplicates(true).size(5));
		suggestBuilder.addSuggestion("prefix_pinyin", SuggestBuilders.completionSuggestion("kwsuggest.prefix_pinyin").prefix(kw).skipDuplicates(true).size(5));
		suggestBuilder.addSuggestion("like_pinyin", SuggestBuilders.completionSuggestion("kwsuggest.like_pinyin").prefix(kw, Fuzziness.ONE).skipDuplicates(true).size(5));
		searchSourceBuilder.suggest(suggestBuilder);
		searchSourceBuilder.timeout(TimeValue.timeValueSeconds(10));
		searchRequest.source(searchSourceBuilder);

		Set<String> set = new HashSet<>();
		List<String> result = new ArrayList<>();
		List<String> suggestionList= Arrays.asList("suggestText","full_pinyin","prefix_pinyin","like_pinyin");
		try {
			SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
			Suggest suggestions = response.getSuggest();
			int index = 0;
			if(null == suggestions) {
				return result;
			}
			for(String suggestionType : suggestionList){
				CompletionSuggestion completionSuggestion = suggestions.getSuggestion(suggestionType);
				for(CompletionSuggestion.Entry entry : completionSuggestion.getEntries()) {
					for(CompletionSuggestion.Entry.Option option : entry) {
						String suggestText  = option.getHit().getSourceAsMap().get("kwsuggest").toString();
						set.add(suggestText);
						index++;
					}
				}

				// 按照中文匹配、全拼匹配、拼音首字母匹配、模糊匹配的顺序，结果大于5的时候返回结果，根据自己业务需要判断这个返回的数量
				if(set.size()>=5){
					break;
				}
				// 中文匹配，全拼匹配以及拼音首字母匹配存在结果的，不需要模糊匹配
				if(index==3 && set.size()>0){
					break;
				}
				// 超过3个字模糊匹配不准确
				if(kw.length()>3 && set.size()==0){
					break;
				}
			}
			result = new ArrayList<>(set);
			return result;
		} catch (IOException e) {
			log.error("异常信息:{}", e.getMessage());
			return result;
		}
	}
}
