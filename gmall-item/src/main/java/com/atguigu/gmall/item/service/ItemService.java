package com.atguigu.gmall.item.service;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.item.feign.GmallPmsClient;
import com.atguigu.gmall.item.feign.GmallSmsClient;
import com.atguigu.gmall.item.feign.GmallWmsClient;
import com.atguigu.gmall.item.vo.ItemVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.AttrValueVo;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @author fzqqq
 * @create 2020-10-13 8:46
 */
@Service
public class ItemService {
    @Autowired
    private GmallPmsClient gmallPmsClient;
    @Autowired
    private GmallWmsClient gmallWmsClient;
    @Autowired
    private GmallSmsClient gmallSmsClient;
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;


    public ItemVo loadItemInfo(Long skuId) {

        ItemVo itemVo = new ItemVo();

        CompletableFuture<SkuEntity> future = CompletableFuture.supplyAsync(() -> {
            // sku
            ResponseVo<SkuEntity> skuEntityResponseVo = this.gmallPmsClient.querySkuById(skuId);
            SkuEntity skuEntity = skuEntityResponseVo.getData();
            if (skuEntity == null) {
                return null;
            }
            itemVo.setSkuId(skuEntity.getId());
            itemVo.setTitle(skuEntity.getTitle());
            itemVo.setSubTitle(skuEntity.getSubtitle());
            itemVo.setPrice(skuEntity.getPrice());
            itemVo.setWeight(skuEntity.getWeight());
            itemVo.setDefaultImage(skuEntity.getDefaultImage());
            return skuEntity;
        }, threadPoolExecutor);

        CompletableFuture<Void> categoryCompletableFuture = future.thenAcceptAsync(skuEntity -> {
            // 设置123级分类
            ResponseVo<List<CategoryEntity>> categoryEntityVo = this.gmallPmsClient.queryCategoriesByCid3(skuEntity.getCategoryId());//TODO 先通过skuId查询到skuEntity 才能获得cid3
            List<CategoryEntity> categoryEntities = categoryEntityVo.getData();
            itemVo.setCategoryEntities(categoryEntities);
        }, threadPoolExecutor);

        CompletableFuture<Void> spuCompletableFuture = future.thenAcceptAsync(skuEntity -> {
            // spu
            itemVo.setSpuId(skuEntity.getSpuId());
            ResponseVo<SpuEntity> spuEntityResponseVo = this.gmallPmsClient.querySpuById(skuEntity.getSpuId());
            SpuEntity spuEntity = spuEntityResponseVo.getData();
            if (spuEntity != null) {
                itemVo.setSpuName(spuEntity.getName());
            }
        }, threadPoolExecutor);

        CompletableFuture<Void> saleAttrsCompletableFuture = future.thenAcceptAsync(skuEntity -> {
            // sku 所属的spu所有的销售属性
            ResponseVo<List<SaleAttrValueVo>> skuAttrValuesVo = this.gmallPmsClient.querySkuAttrValuesBySpuId(skuEntity.getSpuId());
            List<SaleAttrValueVo> skuAttrValues = skuAttrValuesVo.getData();
            itemVo.setSaleAttrs(skuAttrValues);
        }, threadPoolExecutor);

        CompletableFuture<Void> skusJsonCompletableFuture = future.thenAcceptAsync(skuEntity -> {
            // sku列表
            ResponseVo<String> skusJsonBySpuId = this.gmallPmsClient.querySkusJsonBySpuId(skuEntity.getSpuId());
            String skusJson = skusJsonBySpuId.getData();
            itemVo.setSkusJson(skusJson);
        }, threadPoolExecutor);

        CompletableFuture<Void> spuImagesCompletableFuture = future.thenAcceptAsync(skuEntity -> {
            // 商品描述
            ResponseVo<SpuDescEntity> spuDescEntityResponseVo = this.gmallPmsClient.querySpuDescById(skuEntity.getSpuId());
            SpuDescEntity spuDescEntity = spuDescEntityResponseVo.getData();
            if (spuDescEntity != null && StringUtils.isNotBlank(spuDescEntity.getDecript())) {

                itemVo.setSpuImages(Arrays.asList(StringUtils.split(spuDescEntity.getDecript(), ",")));
            }
        }, threadPoolExecutor);

        CompletableFuture<Void> groupsCompletableFuture = future.thenAcceptAsync(skuEntity -> {
            ResponseVo<List<ItemGroupVo>> listResponseVo = this.gmallPmsClient.queryGroupsBySpuIdAndCid(skuEntity.getSpuId(), skuId, skuEntity.getCategoryId());
            List<ItemGroupVo> itemGroupVos = listResponseVo.getData();
            itemVo.setGroups(itemGroupVos);
        }, threadPoolExecutor);

        CompletableFuture<Void> brandCompletableFuture = CompletableFuture.runAsync(() -> {
            // 设置品牌
            ResponseVo<BrandEntity> brandEntityResponseVo = this.gmallPmsClient.queryBrandById(skuId);
            BrandEntity brandEntity = brandEntityResponseVo.getData();
            if (brandEntity != null) {
                itemVo.setBrandId(brandEntity.getId());
                itemVo.setBrandName(brandEntity.getName());
            }
        }, threadPoolExecutor);

        CompletableFuture<Void> imagesCompletableFuture = CompletableFuture.runAsync(() -> {
            // sku图片
            ResponseVo<List<SkuImagesEntity>> skuImagesVo = this.gmallPmsClient.queryImagesBySkuId(skuId);
            List<SkuImagesEntity> skuImages = skuImagesVo.getData();
            itemVo.setImages(skuImages);
        }, threadPoolExecutor);

        CompletableFuture<Void> salesCompletableFuture = CompletableFuture.runAsync(() -> {
            // 营销信息
            ResponseVo<List<ItemSaleVo>> salesVo = this.gmallSmsClient.querySalesBySkuId(skuId);
            List<ItemSaleVo> sales = salesVo.getData();
            itemVo.setSales(sales);
        }, threadPoolExecutor);

        CompletableFuture<Void> storeCompletableFuture = CompletableFuture.runAsync(() -> {
            // 是否有货
            ResponseVo<List<WareSkuEntity>> wareSkuEntitiesVo = this.gmallWmsClient.queryWareSkuBySkuId(skuId);
            List<WareSkuEntity> wareSkuEntities = wareSkuEntitiesVo.getData();
            if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                itemVo.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
            }
        }, threadPoolExecutor);

        CompletableFuture<Void> saleAttrCompletableFuture = CompletableFuture.runAsync(() -> {
            // 当前sku的销售属性
            ResponseVo<List<SkuAttrValueEntity>> attrValuesBySkuIdVo = this.gmallPmsClient.querySkuAttrValuesBySkuId(skuId);
            List<SkuAttrValueEntity> skuAttrValueEntities = attrValuesBySkuIdVo.getData();
            itemVo.setSaleAttr(skuAttrValueEntities.stream().collect(Collectors.toMap(SkuAttrValueEntity::getAttrId, SkuAttrValueEntity::getAttrValue)));
        }, threadPoolExecutor);

        CompletableFuture.allOf(categoryCompletableFuture, spuCompletableFuture, saleAttrsCompletableFuture, skusJsonCompletableFuture,
                spuImagesCompletableFuture, groupsCompletableFuture, brandCompletableFuture, imagesCompletableFuture,
                salesCompletableFuture, storeCompletableFuture, saleAttrCompletableFuture).join();

        return itemVo;
    }
}
