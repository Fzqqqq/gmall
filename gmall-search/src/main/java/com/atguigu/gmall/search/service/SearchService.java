package com.atguigu.gmall.search.service;

import com.atguigu.gmall.search.pojo.SearchParamVo;
import com.atguigu.gmall.search.pojo.SearchResponseVo;

/**
 * @author fzqqq
 * @create 2020-09-28 20:31
 */
public interface SearchService {
    SearchResponseVo search(SearchParamVo searchParamVo);

}
