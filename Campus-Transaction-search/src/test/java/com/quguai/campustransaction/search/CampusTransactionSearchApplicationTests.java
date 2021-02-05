package com.quguai.campustransaction.search;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.quguai.campustransaction.search.config.ElasticSearchConfig;
import com.quguai.common.utils.R;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minidev.json.JSONValue;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.ElasticsearchClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.ml.job.results.Bucket;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

@SpringBootTest
public class CampusTransactionSearchApplicationTests {

	@Autowired
	private RestHighLevelClient restHighLevelClient;

	@Test
	void contextLoads() {
		System.out.println(restHighLevelClient);
	}

	@Data
	@AllArgsConstructor
	class User{
		private String username;
		private String gender;
		private Integer age;
	}

	@Test
	void indexData() throws IOException {
		IndexRequest request = new IndexRequest("users");
		request.id("2");
//		request.source("username", "zhangsan", "age", 18, "gender", "男");
		User user = new User("lisi", "男", 18);
		String jsonString = JSON.toJSONString(user);
		request.source(jsonString, XContentType.JSON);
		IndexResponse index = restHighLevelClient.index(request, ElasticSearchConfig.COMMON_OPTIONS);
		System.out.println(index);
	}

	@Test
	void searchTest() throws IOException {
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.indices("bank");
		// 指定DSL
		SearchSourceBuilder builder = new SearchSourceBuilder();
		builder.query(QueryBuilders.matchAllQuery());
//		builder.aggregation(AggregationBuilders.terms("aggAge").field("age").size(100).subAggregation(AggregationBuilders.avg("aggAvg").field("balance")));
		// 第一季分组
		TermsAggregationBuilder aggAge = AggregationBuilders.terms("aggAge").field("age").size(100);
		// 第二季分组
		TermsAggregationBuilder aggGender = AggregationBuilders.terms("aggGender").field("gender.keyword");
		// 第三季分组
		AvgAggregationBuilder aggAvg = AggregationBuilders.avg("aggAvg").field("balance");

		aggAge.subAggregation(aggAvg);
		aggAge.subAggregation(aggGender.subAggregation(aggAvg));

		builder.aggregation(aggAge);
		searchRequest.source(builder);
		// 执行检索
		SearchResponse search = restHighLevelClient.search(searchRequest, ElasticSearchConfig.COMMON_OPTIONS);

		SearchHit[] hits = search.getHits().getHits();
		for (SearchHit hit : hits) {
			String sourceAsString = hit.getSourceAsString();
//			R jsonObject = JSON.parseObject(sourceAsString, R.class);
		}

		Aggregations aggregations = search.getAggregations();
		Terms byAge = aggregations.get("aggAge");
		for (Terms.Bucket bucket : byAge.getBuckets()) {
			System.out.println("年龄：" + bucket.getKey());
			System.out.println("计数：" + bucket.getDocCount());

			Avg aggAvg1 = bucket.getAggregations().get("aggAvg");
			System.out.println("平资：" + aggAvg1.getValue());

			Terms aggGender1 = bucket.getAggregations().get("aggGender");
			for (Terms.Bucket aggGender1Bucket : aggGender1.getBuckets()) {
				System.out.println("	性别：" + aggGender1Bucket.getKey());
				System.out.println("	人数：" + aggGender1Bucket.getDocCount());
				Avg aggAvg2 = aggGender1Bucket.getAggregations().get("aggAvg");
				System.out.println("	年龄段均资：" + aggAvg2.getValue());
			}
		}

		System.out.println(search.toString());
	}
}
