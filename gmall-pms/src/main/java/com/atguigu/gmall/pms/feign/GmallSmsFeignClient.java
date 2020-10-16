package com.atguigu.gmall.pms.feign;

import com.atguigu.gmall.sms.api.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author fzqqq
 * @create 2020-09-23 21:05
 */
@FeignClient("sms-service")
public interface GmallSmsFeignClient extends GmallSmsApi {

}
