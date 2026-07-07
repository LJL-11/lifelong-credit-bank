package org.csu.creditbank.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.csu.creditbank.common.BusinessException;
import org.csu.creditbank.dto.AddToCartRequest;
import org.csu.creditbank.entity.Cart;
import org.csu.creditbank.entity.CreditProduct;
import org.csu.creditbank.mapper.CartMapper;
import org.csu.creditbank.service.CartService;
import org.csu.creditbank.service.CreditProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {

    private final CreditProductService productService;

    public CartServiceImpl(CreditProductService productService) {
        this.productService = productService;
    }

    @Override
    @Transactional
    public Cart addToCart(Long learnerId, AddToCartRequest request) {
        CreditProduct product = productService.getById(request.getProductId());
        if (product == null || product.getIsActive() != 1) {
            throw new BusinessException("商品不存在或已下架");
        }

        // 物理删除该用户+商品的所有旧记录（包括软删除的），避免唯一键冲突
        baseMapper.delete(new LambdaUpdateWrapper<Cart>()
                .eq(Cart::getLearnerId, learnerId)
                .eq(Cart::getProductId, request.getProductId()));

        // 检查是否已在购物车中（非逻辑删除行）
        Cart existing = lambdaQuery()
                .eq(Cart::getLearnerId, learnerId)
                .eq(Cart::getProductId, request.getProductId())
                .one();
        if (existing != null) {
            existing.setNum(existing.getNum() + request.getNum());
            updateById(existing);
            return existing;
        }

        long count = lambdaQuery().eq(Cart::getLearnerId, learnerId).count();
        if (count >= 20) {
            throw new BusinessException("购物车商品不能超过20件");
        }

        Cart cart = new Cart();
        cart.setLearnerId(learnerId);
        cart.setProductId(product.getId());
        cart.setProductName(product.getProductName());
        cart.setCreditPrice(product.getCreditPrice());
        cart.setNum(request.getNum());
        save(cart);
        return cart;
    }

    @Override
    public List<Cart> myCarts(Long learnerId) {
        return lambdaQuery()
                .eq(Cart::getLearnerId, learnerId)
                .orderByDesc(Cart::getCreatedAt)
                .list();
    }

    @Override
    public void updateNum(Long cartId, Integer num) {
        Cart cart = getById(cartId);
        if (cart == null) throw new BusinessException("购物车项不存在");
        if (num <= 0) {
            baseMapper.deleteById(cartId); // 物理删除
        } else {
            cart.setNum(num);
            updateById(cart);
        }
    }

    @Override
    @Transactional
    public void removeByIds(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            // 物理删除
            baseMapper.delete(new LambdaUpdateWrapper<Cart>().in(Cart::getId, ids));
        }
    }

    @Override
    public int totalCredits(Long learnerId, List<Long> cartIds) {
        if (cartIds == null || cartIds.isEmpty()) return 0;
        return lambdaQuery()
                .eq(Cart::getLearnerId, learnerId)
                .in(Cart::getId, cartIds)
                .list()
                .stream()
                .mapToInt(c -> c.getCreditPrice() * c.getNum())
                .sum();
    }
}
