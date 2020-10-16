package com.atguigu.es.repository;

import com.atguigu.es.entity.User;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @author fzqqq
 * @create 2020-09-27 19:42
 */
public interface UserRepository extends ElasticsearchRepository<User, Long> {

}
