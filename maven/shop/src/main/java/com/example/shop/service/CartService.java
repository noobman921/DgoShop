// package com.example.shop.service;

// import com.example.shop.entity.Cart;
// import com.example.shop.entity.Product;
// import com.example.shop.mapper.CartMapper;
// import com.example.shop.mapper.ProductMapper;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;
// import org.springframework.util.CollectionUtils;

// import java.util.List;
// import java.util.Objects;

// /**
// * 购物车业务层（简化版：去掉异常抛出）
// * 处理购物车所有核心业务逻辑
// *
// * @author 开发者
// * @date 2025-12-08
// */
// @Service
// public class CartService {

// @Autowired
// private CartMapper cartMapper;
// @Autowired
// private ProductMapper productMapper;

// /**
// * 根据用户ID查询购物车列表（关联商品名称/价格等信息）
// *
// * @param userId 用户ID（非空）
// * @return 购物车列表（已组装商品详情），无数据返回空列表
// */
// public List<Cart> getCartByUserId(Long userId) {
// // 1. 基础参数校验：参数非法直接返回空列表
// if (userId == null || userId <= 0) {
// System.out.println("用户ID非法：" + userId);
// return List.of();
// }

// // 2. 调用Mapper查询原始购物车数据
// List<Cart> cartList = cartMapper.selectByUserId(userId);
// if (CollectionUtils.isEmpty(cartList)) {
// return cartList; // 无数据直接返回空列表
// }

// // 3. 组装商品详情（商品不存在则跳过该条数据）
// for (Cart cart : cartList) {
// Long productId = cart.getProductId();
// Product product = productMapper.selectByProductId(productId);
// if (Objects.isNull(product)) {
// System.out.println("商品ID：" + productId + " 不存在，跳过该购物车项");
// continue;
// }
// // 补充商品信息
// cart.setProductName(product.getProductName());
// cart.setProductPrice(product.getProductPrice());
// cart.setProductPic(product.getProductPic());
// }

// return cartList;
// }

// /**
// * 添加商品到购物车（核心业务：库存校验+事务控制）
// *
// * @param cart 购物车实体（含userId/productId/quantity）
// * @return 插入/更新结果（影响行数），参数非法/业务不通过返回0
// */
// @Transactional(rollbackFor = Exception.class)
// public int addToCart(Cart cart) {
// // 1. 基础参数校验：参数非法返回0
// if (!validateCartParam(cart)) {
// return 0;
// }

// Long userId = cart.getUserId();
// Long productId = cart.getProductId();
// Integer quantity = cart.getQuantity();

// // 2. 校验商品和库存：不通过返回0
// Product product = productMapper.selectByProductId(productId);
// if (Objects.isNull(product)) {
// System.out.println("商品ID：" + productId + " 不存在");
// return 0;
// }
// if (product.getStock() < quantity) {
// System.out.println("库存不足：商品ID=" + productId + "，当前库存=" + product.getStock() +
// "，请求数量=" + quantity);
// return 0;
// }

// // 3. 检查购物车是否已有该商品（有则更新，无则新增）
// List<Cart> existCartList = cartMapper.selectByUserId(userId);
// boolean hasExist = existCartList.stream()
// .anyMatch(c -> c.getProductId().equals(productId));

// if (hasExist) {
// // 已有商品：更新数量（累加）
// return cartMapper.updateQuantityByUserIdAndProductId(userId, productId,
// quantity);
// } else {
// // 无商品：插入新购物车项
// return cartMapper.insertCart(cart);
// }
// }

// /**
// * 精准删除：按用户ID+商品ID删除购物车单条记录
// *
// * @param userId 用户ID
// * @param productId 商品ID
// * @return 删除结果（影响行数），参数非法返回0
// */
// public int deleteByUserIdAndProductId(Long userId, Long productId) {
// // 1. 参数校验：非法返回0
// if (userId == null || userId <= 0 || productId == null || productId <= 0) {
// System.out.println("用户ID/商品ID非法：userId=" + userId + "，productId=" +
// productId);
// return 0;
// }

// // 2. 检查购物车是否有该商品：无则返回0
// List<Cart> cartList = cartMapper.selectByUserId(userId);
// boolean exists = cartList.stream().anyMatch(c ->
// c.getProductId().equals(productId));
// if (!exists) {
// System.out.println("购物车中无该商品：userId=" + userId + "，productId=" + productId);
// return 0;
// }

// // 3. 执行删除
// return cartMapper.deleteByUserIdAndProductId(userId, productId);
// }

// /**
// * 清空用户购物车
// *
// * @param userId 用户ID
// * @return 删除结果（影响行数），参数非法/购物车为空返回0
// */
// @Transactional(rollbackFor = Exception.class)
// public int clearCart(Long userId) {
// // 1. 参数校验：非法返回0
// if (userId == null || userId <= 0) {
// System.out.println("用户ID非法：" + userId);
// return 0;
// }

// // 2. 检查购物车是否为空：空则返回0
// List<Cart> cartList = cartMapper.selectByUserId(userId);
// if (CollectionUtils.isEmpty(cartList)) {
// System.out.println("用户ID=" + userId + " 购物车已为空");
// return 0;
// }

// // 3. 执行清空
// return cartMapper.deleteByUserId(userId);
// }

// /**
// * 修改购物车商品数量
// *
// * @param userId 用户ID
// * @param productId 商品ID
// * @param quantity 新数量
// * @return 修改结果（影响行数），参数非法/库存不足返回0
// */
// @Transactional(rollbackFor = Exception.class)
// public int updateCartQuantity(Long userId, Long productId, Integer quantity)
// {
// // 1. 参数校验：非法返回0
// if (userId == null || userId <= 0 || productId == null || productId <= 0 ||
// quantity <= 0) {
// System.out.println("参数非法：userId=" + userId + "，productId=" + productId +
// "，quantity=" + quantity);
// return 0;
// }

// // 2. 校验库存：不足返回0
// Product product = productMapper.selectByProductId(productId);
// if (Objects.isNull(product) || product.getStock() < quantity) {
// System.out.println("库存不足/商品不存在：userId=" + userId + "，productId=" +
// productId);
// return 0;
// }

// // 3. 执行更新
// return cartMapper.updateQuantityByUserIdAndProductId(userId, productId,
// quantity);
// }

// /**
// * 私有方法：购物车参数通用校验
// *
// * @param cart 购物车实体
// * @return true=参数合法，false=参数非法
// */
// private boolean validateCartParam(Cart cart) {
// if (Objects.isNull(cart)) {
// System.out.println("购物车参数为空");
// return false;
// }
// if (cart.getUserId() == null || cart.getUserId() <= 0) {
// System.out.println("用户ID非法：" + cart.getUserId());
// return false;
// }
// if (cart.getProductId() == null || cart.getProductId() <= 0) {
// System.out.println("商品ID非法：" + cart.getProductId());
// return false;
// }
// if (cart.getQuantity() == null || cart.getQuantity() <= 0) {
// System.out.println("商品数量非法：" + cart.getQuantity());
// return false;
// }
// return true;
// }
// }