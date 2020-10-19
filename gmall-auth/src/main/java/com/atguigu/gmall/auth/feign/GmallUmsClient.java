package com.atguigu.gmall.auth.feign;

import com.atguigu.gmall.ums.api.GmallUmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author fzqqq
 * @create 2020-10-19 14:17
 */
@FeignClient("ums-service")
public interface GmallUmsClient extends GmallUmsApi {
}
