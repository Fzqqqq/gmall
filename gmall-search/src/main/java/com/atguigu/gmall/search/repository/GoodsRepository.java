package com.atguigu.gmall.search.repository;

import com.atguigu.gmall.search.pojo.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @author fzqqq
 * @create 2020-09-28 15:01
 */
public interface GoodsRepository extends ElasticsearchRepository<Goods,Long> {
}
