package com.atguigu.gmall.cart.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.entity.Cart;
import com.atguigu.gmall.cart.entity.UserInfo;
import com.atguigu.gmall.cart.feign.GmallPmsClient;
import com.atguigu.gmall.cart.feign.GmallSmsClient;
import com.atguigu.gmall.cart.feign.GmallWmsClient;
import com.atguigu.gmall.cart.interceptor.LoginInterceptor;
import com.atguigu.gmall.cart.mapper.CartMapper;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.jsonwebtoken.lang.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import springfox.documentation.spring.web.json.Json;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * @author fzqqq
 * @create 2020-10-20 9:50
 */
@Service
public class CartService {

    @Autowired
    private GmallPmsClient pmsClient;

    @Autowired
    private GmallWmsClient wmsClient;

    @Autowired
    private GmallSmsClient smsClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private CartAsyncService cartAsyncService;
    private static  final String KEY_PREFIX= "cart:info:";
    private static  final String PRICE_PREFIX= "cart:price:";

    public void addCart(Cart cart) {
        // 1.获取登录信息
        String userId = getUserId();
        String key = KEY_PREFIX + userId;

        // 2.从redis中查询，
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);

        // 2.获取redis中该用户的购物车
        String skuId = cart.getSkuId().toString();
        BigDecimal count = cart.getCount();
        if (hashOps.hasKey(skuId)) {
            // 获取redis中的购物车对象
            String cartJson = hashOps.get(cart.getSkuId().toString()).toString();
            cart = JSON.parseObject(cartJson, Cart.class);
            // 设置数量  现在的 + 原来的
            cart.setCount(cart.getCount().add(count));
            // 更新mysql
            this.cartAsyncService.updateCart(cart, userId);
        } else {
            // 没有购物车对象，需要添加新商品记录
            cart.setUserId(userId);

            // 根据skuid查询skuentity
            ResponseVo<SkuEntity> skuEntityResponseVo = this.pmsClient.querySkuById(cart.getSkuId());
            SkuEntity skuEntity = skuEntityResponseVo.getData();
            cart.setDefaultImage(skuEntity.getDefaultImage());
            cart.setTitle(skuEntity.getTitle());
            cart.setPrice(skuEntity.getPrice());

            // 根据skid 查询销售信息
            ResponseVo<List<SkuAttrValueEntity>> listResponseVo = this.pmsClient.querySkuAttrValuesBySkuId(cart.getSkuId());
            List<SkuAttrValueEntity> skuAttrValueEntities = listResponseVo.getData();
            cart.setSaleAttrs(JSON.toJSONString(skuAttrValueEntities));

            // 根据skuid 查询store信息
            ResponseVo<List<WareSkuEntity>> responseVo = this.wmsClient.queryWareSkuBySkuId(cart.getSkuId());
            List<WareSkuEntity> wareSkuEntities = responseVo.getData();
            if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                cart.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
            }

            // 根据skuid 查询sales信息
            ResponseVo<List<ItemSaleVo>> salesBySkuId = this.smsClient.querySalesBySkuId(cart.getSkuId());
            List<ItemSaleVo> itemSaleVos = salesBySkuId.getData();
            cart.setSales(JSON.toJSONString(itemSaleVos));

            cart.setCheck(true);
            // 插入mysql
            this.cartAsyncService.saveCart(cart);
            this.redisTemplate.opsForValue().set(PRICE_PREFIX + skuId, skuEntity.getPrice().toString());
        }
        // 存入redis
        hashOps.put(skuId, JSON.toJSONString(cart));

    }

    public Cart queryCartBySkuId(Long skuId) {
        // 获取redis中的购物车对象
        // 1.获取登录信息
        String userId = getUserId();
        String key = KEY_PREFIX + userId;

        // 从redis中获取
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        if (hashOps.hasKey(skuId.toString())) {
            String cartJson = hashOps.get(skuId.toString()).toString();
            return JSON.parseObject(cartJson, Cart.class);
        }else{
            throw new RuntimeException("您购物车中没有该商品记录");
        }

    }

    public List<Cart> queryCarts() {
        // 查询购物车
        // 1.获取登录信息userKey
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String userKey = userInfo.getUserKey();
        String unLoginKey = KEY_PREFIX + userKey;
        // 未登录状态下的购物车对象
        BoundHashOperations<String, Object, Object> unLoginHashOps = this.redisTemplate.boundHashOps(unLoginKey);
        List<Object> cartJsons = unLoginHashOps.values();
        List<Cart> unLoginCarts = null;
        if (!CollectionUtils.isEmpty(cartJsons)) {
            unLoginCarts = cartJsons.stream().map(cartJson -> {
                Cart cart = JSON.parseObject(cartJson.toString(), Cart.class);
                // 查询实时价格
                String currentPriceString = this.redisTemplate.opsForValue().get(PRICE_PREFIX + cart.getSkuId());
                cart.setCurrentPrice(new BigDecimal(currentPriceString));
                return cart;
            }).collect(Collectors.toList());
        }

        Long userId = userInfo.getUserId();
        // 2. 判断是否登录，没有登录直接返回
        if (userId == null) {
            // 说明用户未登录
            // 直接返回未登录状态下的购物车列表
            return unLoginCarts;
        }
        // 3.已登录，合并购物车中的信息，删除未登录状态的购物车（redis+mysql）
        // 登录的key
        String loginKey = KEY_PREFIX+ userId;
        BoundHashOperations<String, Object, Object> loginHashOps = this.redisTemplate.boundHashOps(loginKey);
        // 查询已登录的购物车对象
        // 遍历未登录状态下的购物车对象（判空）
        if (!CollectionUtils.isEmpty(unLoginCarts)) {
            // 如果已登录的有 则更新数量，没有则插入新数据
            unLoginCarts.forEach(cart -> {
                if (loginHashOps.hasKey(cart.getSkuId().toString())) {
                    // 更新数量
                    BigDecimal count = cart.getCount();
                    String cartJson = loginHashOps.get(cart.getSkuId().toString()).toString();
                    cart = JSON.parseObject(cartJson, Cart.class);
                    cart.setCount(cart.getCount().add(count));
                    this.cartAsyncService.updateCart(cart, userId.toString());
                }else {
                    // 登录状态购物车不包含该记录，新增
                    cart.setUserId(userId.toString());
                    this.cartAsyncService.saveCart(cart);
                }
                loginHashOps.put(cart.getSkuId().toString(),JSON.toJSONString(cart));
            });
            // 4. 合并完未登录的购物车之后，要删除未登录的购物车
            this.cartAsyncService.deleteCartByUserId(userKey);
            this.redisTemplate.delete(unLoginKey);
        }
        // 5.查询登录状态所有购物车信息，反序列化后返回
        List<Object> loginCartJsons = loginHashOps.values();
        if (!CollectionUtils.isEmpty(loginCartJsons)) {
            return loginCartJsons.stream().map(loginCartJson -> {
                Cart cart = JSON.parseObject(loginCartJson.toString(), Cart.class);
                String currentPriceString = this.redisTemplate.opsForValue().get(PRICE_PREFIX + cart.getSkuId());
                cart.setCurrentPrice(new BigDecimal(currentPriceString));
                return cart;

            }).collect(Collectors.toList());
        }
        return null;
    }

    private String getUserId() {
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        if (userInfo.getUserId() != null) {
            // 如果用户的id不为空，说明该用户已登录，添加购物车应该以userId作为key
            return userInfo.getUserId().toString();
        }
        // 否则，说明用户未登录，以userKey作为key
        return userInfo.getUserKey();
    }

    public void updateNum(Cart cart) {

        String userId = this.getUserId();
        String key = KEY_PREFIX + userId;

        // 获取该用户的所有购物车
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        // 判断该用户的购物车中是否包含该条信息
        if (hashOps.hasKey(cart.getSkuId().toString())) {
            BigDecimal count = cart.getCount(); // 页面传递的要更新的数量
            String cartJson = hashOps.get(cart.getSkuId().toString()).toString();
            cart = JSON.parseObject(cartJson, Cart.class);
            cart.setCount(count);
            // 更新到mysql及redis
            this.cartAsyncService.updateCart(cart, userId);
            hashOps.put(cart.getSkuId().toString(), JSON.toJSONString(cart));
        }
    }

    public void deleteCart(Long skuId) {
        String userId = this.getUserId();
        String key = KEY_PREFIX + userId;

        // 获取该用户的所有购物车
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        // 判断该用户的购物车中是否包含该条信息
        if (hashOps.hasKey(skuId.toString())){
            this.cartAsyncService.deleteCartByUserIdAndSkuId(userId, skuId);
            hashOps.delete(skuId.toString());
        }
    }
}
