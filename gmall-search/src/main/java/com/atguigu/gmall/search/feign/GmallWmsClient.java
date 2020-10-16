package com.atguigu.gmall.search.feign;

import com.atguigu.gmall.wms.api.GmallWmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author fzqqq
 * @create 2020-09-28 14:59
 */
@FeignClient("wms-service")
public interface GmallWmsClient extends GmallWmsApi {

}
