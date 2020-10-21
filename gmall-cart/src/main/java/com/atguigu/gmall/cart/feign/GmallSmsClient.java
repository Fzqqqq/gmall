package com.atguigu.gmall.cart.feign;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import com.atguigu.gmall.sms.api.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author fzqqq
 * @create 2020-10-20 9:24
 */
@FeignClient("sms-service")
public interface GmallSmsClient extends GmallSmsApi {
}
