package com.atguigu.gmall.search.service.impl;


import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchParamVo;
import com.atguigu.gmall.search.pojo.SearchResponseAttrValueVo;
import com.atguigu.gmall.search.pojo.SearchResponseVo;
import com.atguigu.gmall.search.service.SearchService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author fzqqq
 * @create 2020-09-28 20:33
 */
@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public SearchResponseVo search(SearchParamVo searchParamVo) {

        try {
            System.out.println("searchParamVo = " + searchParamVo);

            SearchRequest searchRequest = new SearchRequest(new String[]{"goods"}, this.buildDsl(searchParamVo));
            SearchResponse searchResponse = this.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            SearchResponseVo searchResponseVo = parseResult(searchResponse);
            System.out.println(searchResponseVo);
            searchResponseVo.setPageNum(searchParamVo.getPageNum());
            searchResponseVo.setPageSize(searchParamVo.getPageSize());

            return searchResponseVo;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private SearchResponseVo parseResult(SearchResponse searchResponse) {
        SearchResponseVo searchResponseVo = new SearchResponseVo();
        SearchHits hits = searchResponse.getHits();
        searchResponseVo.setTotal(hits.getTotalHits());

        SearchHit[] hits1 = hits.getHits();
        List<Goods> goodsList = Stream.of(hits1).map(hitsHit -> {
            try {
                String json = hitsHit.getSourceAsString();
                Goods goods = MAPPER.readValue(json, Goods.class);

                // 用高亮的标题覆盖普通标题
                Map<String, HighlightField> highlightFields = hitsHit.getHighlightFields();
                HighlightField title = highlightFields.get("title");
                Text[] titleFragments = title.getFragments();
                goods.setTitle(titleFragments[0].string());
                return goods;
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return null;
        }).collect(Collectors.toList());
        searchResponseVo.setData(goodsList);


        Map<String, Aggregation> aggregationMap = searchResponse.getAggregations().asMap();

        ParsedLongTerms brandIdAgg = (ParsedLongTerms) aggregationMap.get("brandIdAgg");
        List<? extends Terms.Bucket> buckets = brandIdAgg.getBuckets();
        if (!CollectionUtils.isEmpty(buckets)) {
            List<BrandEntity> brands = buckets.stream().map(bucket -> {
                BrandEntity brandEntity = new BrandEntity();
                Long key = bucket.getKeyAsNumber().longValue();
                brandEntity.setId(key);

                // 解析子聚合。获取name and logo
                Map<String, Aggregation> subAggregationMap = bucket.getAggregations().asMap();
                ParsedStringTerms brandNameAgg = (ParsedStringTerms) subAggregationMap.get("brandNameAgg");
                List<? extends Terms.Bucket> nameAggBuckets = brandNameAgg.getBuckets();
                if (!CollectionUtils.isEmpty(nameAggBuckets)) {
                    Terms.Bucket bucket1 = nameAggBuckets.get(0);
                    String brandName = bucket1.getKeyAsString();
                    brandEntity.setName(brandName);
                }

                ParsedStringTerms logoAgg = (ParsedStringTerms) subAggregationMap.get("logoAgg");
                List<? extends Terms.Bucket> logoAggBuckets = logoAgg.getBuckets();
                if (!CollectionUtils.isEmpty(logoAggBuckets)) {
                    brandEntity.setLogo(logoAggBuckets.get(0).getKeyAsString());

                }
                return brandEntity;
            }).collect(Collectors.toList());
            searchResponseVo.setBrands(brands);
        }

        // 解析分类的聚合结果集获取分类
        ParsedLongTerms categoryIdAgg = (ParsedLongTerms) aggregationMap.get("categoryIdAgg");
        List<? extends Terms.Bucket> categoryIdAggBuckets = categoryIdAgg.getBuckets();
        if (!CollectionUtils.isEmpty(categoryIdAggBuckets)) {
            List<CategoryEntity> categoryNameAgg1 = categoryIdAggBuckets.stream().map(bucket -> {
                CategoryEntity categoryEntity = new CategoryEntity();
                categoryEntity.setId(((Terms.Bucket) bucket).getKeyAsNumber().longValue());
                ParsedStringTerms categoryNameAgg = (ParsedStringTerms)((Terms.Bucket) bucket).getAggregations().get("categoryNameAgg");
                List<? extends Terms.Bucket> categoryNameAggBuckets = categoryNameAgg.getBuckets();
                if (!CollectionUtils.isEmpty(categoryIdAggBuckets)) {
                    categoryEntity.setName(categoryIdAggBuckets.get(0).getKeyAsString());
                }
                return categoryEntity;
            }).collect(Collectors.toList());
            searchResponseVo.setCategoryEntities(categoryNameAgg1);
        }
        // 获取规格参数的聚合结果集， 解析出规格参数attrAgg
        ParsedNested attrAgg = (ParsedNested) aggregationMap.get("attrAgg");
        ParsedLongTerms attrIdAgg = (ParsedLongTerms)attrAgg.getAggregations().get("attrIdAgg");
        List<? extends Terms.Bucket> attrIdAggBuckets = attrIdAgg.getBuckets();
        if (!CollectionUtils.isEmpty(attrIdAggBuckets)) {
            List<SearchResponseAttrValueVo> filters = attrIdAggBuckets.stream().map(bucket -> {
                SearchResponseAttrValueVo searchResponseAttrValueVo = new SearchResponseAttrValueVo();
                long longValue = bucket.getKeyAsNumber().longValue();
                searchResponseAttrValueVo.setAttrId(longValue);


                // 获取每个规格参数id聚合下的子聚合
                Map<String, Aggregation> stringAggregationMap = bucket.getAggregations().asMap();
                ParsedStringTerms attrNameAgg = (ParsedStringTerms) stringAggregationMap.get("attrNameAgg");
                List<? extends Terms.Bucket> attrNameAggBuckets = attrNameAgg.getBuckets();
                if (!CollectionUtils.isEmpty(attrNameAggBuckets)) {
                    searchResponseAttrValueVo.setAttrName(attrNameAggBuckets.get(0).getKeyAsString());
                }

                ParsedStringTerms attrValueAgg = (ParsedStringTerms) stringAggregationMap.get("attrValueAgg");
                List<? extends Terms.Bucket> attrValueAggBuckets = attrValueAgg.getBuckets();
                if (!CollectionUtils.isEmpty(attrValueAggBuckets)) {
                    List<String> collect = attrValueAggBuckets.stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());
                    searchResponseAttrValueVo.setAttrValues(collect);
                }
                return searchResponseAttrValueVo;
            }).collect(Collectors.toList());
            searchResponseVo.setFilters(filters);
        }
        return searchResponseVo;
    }

    private SearchSourceBuilder buildDsl(SearchParamVo searchParamVo) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        String keyword = searchParamVo.getKeyword();
        if (StringUtils.isBlank(keyword)) {
            return null;
        }
        // 1构建搜索条件
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        // 1.1 构建匹配查询
        boolQueryBuilder.must(QueryBuilders.matchQuery("title", keyword).operator(Operator.AND));


        // 1.2 构建过滤条件
        // 1.2.1 品牌过滤
        List<Long> brandId = searchParamVo.getBrandId();
        if (!CollectionUtils.isEmpty(brandId)) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", brandId));
        }

        // 1.2.2 分类过滤
        List<Long> cid3 = searchParamVo.getCid3();
        if (!CollectionUtils.isEmpty(cid3)) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("categoryId", cid3));
        }

        // 1.2.3 价格区间的过滤
        Double priceFrom = searchParamVo.getPriceFrom();
        Double priceTo = searchParamVo.getPriceTo();
        if (priceFrom != null || priceTo != null) {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("price");
            if (priceFrom != null) {
                rangeQuery.gte(priceFrom);
            }
            if (priceTo != null) {
                rangeQuery.lte(priceTo);
            }
            boolQueryBuilder.filter(rangeQuery);
        }

        // 1.2.4 库存过滤
        Boolean store = searchParamVo.getStore();
        if (store != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("store", store));
        }

        // 1.2.5 规格参数的嵌套过滤
        List<String> props = searchParamVo.getProps();
        if (!CollectionUtils.isEmpty(props)) {
            props.forEach(prop -> {
                String[] attrs = StringUtils.split(prop, ":");
                if (attrs != null && attrs.length == 2) {
                    String attrId = attrs[0];
                    String attrValueString = attrs[1];
                    String[] attrValues = StringUtils.split(attrValueString, "-");
                    System.out.println("attrValues = " + attrValues);
                    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                    boolQuery.must(QueryBuilders.termsQuery("searchAttrs.attrId", attrId));
                    boolQuery.must(QueryBuilders.termsQuery("searchAttrs.attrValue", attrValues));
                    boolQueryBuilder.filter(QueryBuilders.nestedQuery("searchAttrs", boolQuery, ScoreMode.None));
                }
            });
        }

        searchSourceBuilder.query(boolQueryBuilder);
        // 2构建排序条件
        Integer sort = searchParamVo.getSort();
        String field = "";
        SortOrder order = null;
        if (sort != null) {
            switch (sort) {
                case 1:
                    field = "price";
                    order = SortOrder.ASC;
                    break;
                case 2:
                    field = "price";
                    order = SortOrder.DESC;
                    break;
                case 3:
                    field = "createTime";
                    order = SortOrder.DESC;
                    break;
                case 4:
                    field = "sales";
                    order = SortOrder.DESC;
                    break;
                default:
                    field = "_score";
                    order = SortOrder.DESC;
                    break;
            }
            searchSourceBuilder.sort(field, order);
        }
        // 3构建分页条件
        Integer pageNum = searchParamVo.getPageNum();
        Integer pageSize = searchParamVo.getPageSize();
        searchSourceBuilder.from((pageNum - 1) * pageSize);
        searchSourceBuilder.size(pageSize);

        // 4构建高亮条件
        searchSourceBuilder.highlighter(new HighlightBuilder().field("title").preTags("<font style='color:red'>").postTags("</font>"));

        // 5构建聚合
        // 5.1 品牌聚合
        searchSourceBuilder.aggregation(AggregationBuilders.terms("brandIdAgg").field("brandId")
                .subAggregation(AggregationBuilders.terms("brandNameAgg").field("brandName"))
                .subAggregation(AggregationBuilders.terms("logoAgg").field("logo")));

        // 5.2 分类聚合
        searchSourceBuilder.aggregation(AggregationBuilders.terms("categoryIdAgg").field("categoryId")
                .subAggregation(AggregationBuilders.terms("categoryNameAgg").field("categoryName")));

        // 5.3 规格参数的嵌套聚合
        searchSourceBuilder.aggregation(AggregationBuilders.nested("attrAgg", "searchAttrs")
                .subAggregation(AggregationBuilders.terms("attrIdAgg").field("searchAttrs.attrId")
                .subAggregation(AggregationBuilders.terms("attrNameAgg").field("searchAttrs.attrName"))
                .subAggregation(AggregationBuilders.terms("attrValueAgg").field("searchAttrs.attrValue"))));

        // 6 结果集过滤
        searchSourceBuilder.fetchSource(new String[]{"skuId", "title", "subTitle","price", "defaultImage"}, null);

        System.out.println(searchSourceBuilder);
        return searchSourceBuilder;
    }
}
