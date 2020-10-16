package com.atguigu.gmall.search;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.api.GmallPmsApi;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.feign.GmallPmsClient;
import com.atguigu.gmall.search.feign.GmallWmsClient;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchAttrValueVo;
import com.atguigu.gmall.search.repository.GoodsRepository;
import com.atguigu.gmall.wms.api.GmallWmsApi;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
class GmallSearchApplicationTests {

    @Autowired
    private ElasticsearchRestTemplate restTemplate;

    @Autowired
    private GmallPmsClient pmsApi;

    @Autowired
    private GmallWmsClient wmsApi;

    @Autowired
    private GoodsRepository repository;

    @Test
    void contextLoads() {
        this.restTemplate.createIndex(Goods.class);
        this.restTemplate.putMapping(Goods.class);
    }
    @Test
    void importData(){
        //pageNum pageSize
        Integer pageNum = 1;
        Integer pageSize=100;
        // 分批查询spu
        do {
            PageParamVo pageParamVo = new PageParamVo();
            pageParamVo.setPageNum(pageNum);
            pageParamVo.setPageSize(pageSize);
            ResponseVo<List<SpuEntity>> spuByPageJson = pmsApi.querySpuByPageJson(pageParamVo);
            List<SpuEntity> spus = spuByPageJson.getData();

            if (!CollectionUtils.isEmpty(spus)) {
                // 遍历spus，查询出sku
                spus.forEach(spuEntity -> {
                    ResponseVo<List<SkuEntity>> skuVo = pmsApi.list(spuEntity.getId());
                    List<SkuEntity> skuEntities = skuVo.getData();
                    if (!CollectionUtils.isEmpty(skuEntities)) {
                        List<Goods> goodsList = skuEntities.stream().map(skuEntity -> {
                            // 把sku转化成goods
                            // 商品所需字段
                            Goods goods = new Goods();
                            goods.setSkuId(skuEntity.getId());
                            goods.setDefaultImage(skuEntity.getDefaultImage());
                            goods.setTitle(skuEntity.getTitle());
                            goods.setSubTitle(skuEntity.getSubtitle());
                            goods.setPrice(skuEntity.getPrice().doubleValue());
                            // 品牌
                            ResponseVo<BrandEntity> brandEntityResponseVo = pmsApi.queryBrandById(skuEntity.getBrandId());
                            BrandEntity brandEntity = brandEntityResponseVo.getData();
                            if (!StringUtils.isEmpty(brandEntity)) {
                                goods.setBrandId(brandEntity.getId());
                                goods.setBrandName(brandEntity.getName());
                                goods.setLogo(brandEntity.getLogo());
                            }

                            // 分类
                            ResponseVo<CategoryEntity> categoryEntityResponseVo = pmsApi.queryCategoryById(skuEntity.getCategoryId());
                            CategoryEntity categoryEntity = categoryEntityResponseVo.getData();
                            if (!StringUtils.isEmpty(categoryEntity)) {
                                goods.setCategoryId(categoryEntity.getId());
                                goods.setCategoryName(categoryEntity.getName());
                            }

                            // createTime
                            goods.setCreateTime(spuEntity.getCreateTime());

                            // 库存
                            ResponseVo<List<WareSkuEntity>> responseVo = wmsApi.queryWareSkuBySkuId(skuEntity.getId());
                            List<WareSkuEntity> wareSkuEntities = responseVo.getData();
                            if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                                goods.setSales(wareSkuEntities.stream().map(WareSkuEntity::getSales).reduce((a, b) -> a + b).get());
                                goods.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
                            }

                            // 检索规格
                            List<SearchAttrValueVo> searchAttrValueVos = new ArrayList<>();
                            // SkuAttrValueEntities
                            ResponseVo<List<SkuAttrValueEntity>> listResponseVo = pmsApi.querySearchSkuAttrValuesByCidAndSkuId(skuEntity.getCategoryId(), skuEntity.getId());
                            List<SkuAttrValueEntity> searchSkuAttrValueEntities = listResponseVo.getData();
                            if (!CollectionUtils.isEmpty(searchSkuAttrValueEntities)) {
                                searchAttrValueVos.addAll(searchSkuAttrValueEntities.stream().map(skuAttrValueEntity -> {
                                    SearchAttrValueVo searchAttrValueVo = new SearchAttrValueVo();
                                    BeanUtils.copyProperties(skuAttrValueEntity, searchAttrValueVo);
                                    return searchAttrValueVo;
                                }).collect(Collectors.toList()));
                            }

                            // SpuAttrValueEntities
                            ResponseVo<List<SpuAttrValueEntity>> searchSpuAttrValuesByCidAndSpuId = pmsApi.querySearchSpuAttrValuesByCidAndSpuId(skuEntity.getCategoryId(), skuEntity.getSpuId());
                            List<SpuAttrValueEntity> searchSpuAttrValueEntities = searchSpuAttrValuesByCidAndSpuId.getData();
                            if (!CollectionUtils.isEmpty(searchSpuAttrValueEntities)) {
                                searchAttrValueVos.addAll(searchSpuAttrValueEntities.stream().map(spuAttrValueEntity -> {
                                    SearchAttrValueVo searchAttrValueVo = new SearchAttrValueVo();
                                    BeanUtils.copyProperties(spuAttrValueEntity, searchAttrValueVo);
                                    return searchAttrValueVo;
                                }).collect(Collectors.toList()));
                            }

                            goods.setSearchAttrs(searchAttrValueVos);
                            return goods;
                        }).collect(Collectors.toList());
                        // 批量导入es
                        this.repository.saveAll(goodsList);
                    }
                });
            }else {
                continue;
            }
            pageSize = spus.size();
            pageNum++;
        } while (pageSize == 100);




    }

}
