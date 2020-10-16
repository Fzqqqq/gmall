package com.atguigu.gmall.index.feign;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.api.GmallPmsApi;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.springframework.cloud.openfeign.FeignClient;

import java.util.List;

/**
 * @author fzqqq
 * @create 2020-10-09 16:48
 */
@FeignClient("pms-service")
public interface GmallPmsFeign extends GmallPmsApi {
}
