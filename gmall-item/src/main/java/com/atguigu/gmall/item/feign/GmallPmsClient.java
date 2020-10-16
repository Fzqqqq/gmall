package com.atguigu.gmall.item.feign;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author fzqqq
 * @create 2020-10-13 8:45
 */
@FeignClient("pms-service")
public interface GmallPmsClient extends GmallPmsApi {
}
