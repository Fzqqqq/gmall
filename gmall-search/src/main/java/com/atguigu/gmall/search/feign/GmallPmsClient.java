package com.atguigu.gmall.search.feign;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author fzqqq
 * @create 2020-09-28 14:49
 */
@FeignClient("pms-service")
public interface GmallPmsClient extends GmallPmsApi {

}
