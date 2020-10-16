package com.atguigu.es;


import com.atguigu.es.entity.User;
import com.atguigu.es.repository.UserRepository;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * @author fzqqq
 * @create 2020-09-27 19:13
 */
@SpringBootTest
public class EsDemoApplicationTests {
    @Autowired
    private ElasticsearchRestTemplate restTemplate;
    @Autowired
    private UserRepository userRepository;

    @Test
    void contextLoads(){
        this.restTemplate.createIndex(User.class);
        this.restTemplate.putMapping(User.class);

    }
    @Test
    void testAdd(){
        // id相同就是更新
//        this.userRepository.save(new User(1l, "zhang3", 18, "123456"));
        List<User> users = new ArrayList<>();
        users.add(new User( 1l, "柳岩", 18, "123456"));
        users.add(new User(2l, "范冰冰", 19, "123456"));
        users.add(new User(3l, "李冰冰", 20, "123456"));
        users.add(new User(4l, "锋哥", 21, "654321"));
        users.add(new User(5l, "小鹿", 22, "654321"));
        users.add(new User(6l, "韩红", 23, "123456"));
        this.userRepository.saveAll(users);
    }

    @Test
    void testFind(){
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.withQuery(QueryBuilders.rangeQuery("age").gte(18).lte(22));
        queryBuilder.withSort(SortBuilders.fieldSort("age").order(SortOrder.DESC));
        // 页码需要减一
        queryBuilder.withPageable(PageRequest.of(1, 2));
        queryBuilder.withHighlightBuilder(new HighlightBuilder().field("name").preTags("<em>").postTags("</em>"));
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id", "name", "age"}, null));
        queryBuilder.addAggregation(AggregationBuilders.terms("passwordAgg").field("password"));
        AggregatedPage<User> search = (AggregatedPage<User>) this.userRepository.search(queryBuilder.build());

        System.out.println("一共多少页" + search.getTotalPages());
        System.out.println("总记录 = " + search.getTotalElements());
        System.out.println("当前页数据 = " + search.getContent());
    }
}
