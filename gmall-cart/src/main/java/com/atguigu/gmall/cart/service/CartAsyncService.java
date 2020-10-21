package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.cart.entity.Cart;
import com.atguigu.gmall.cart.mapper.CartMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @author fzqqq
 * @create 2020-10-20 18:13
 */
@Service
public class CartAsyncService {

    @Autowired
    private CartMapper cartMapper;

    @Async
    public void updateCart(Cart cart,String userId){
        this.cartMapper.update(cart, new UpdateWrapper<Cart>().eq("user_id", userId).eq("sku_id", cart.getSkuId()));
    }

    @Async
    public void saveCart(Cart cart){
        this.cartMapper.insert(cart);
    }

    public void deleteCartByUserId(String userKey) {
        this.cartMapper.delete(new QueryWrapper<Cart>().eq("user_id", userKey));
    }

    public void deleteCartByUserIdAndSkuId(String userId, Long skuId) {
        this.cartMapper.delete(new QueryWrapper<Cart>().eq("user_id", userId).eq("sku_id", skuId));
    }
}
