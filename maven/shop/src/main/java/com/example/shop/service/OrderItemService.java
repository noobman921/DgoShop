package com.example.shop.service;

import com.example.shop.entity.OrderItem;
import com.example.shop.mapper.OrderItemMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service // 标记为Spring服务组件，自动注入IOC容器
public class OrderItemService {

    // 自动注入Mapper实例
    @Autowired
    private OrderItemMapper orderItemMapper;

    /**
     * 根据订单项目ID（主键）查询订单项目
     * 
     * @param id 订单项目号（主键ID）
     * @return 订单项目实体（无数据返回null）
     */
    public OrderItem getById(Long id) {
        // 参数合法性校验
        if (id == null || id <= 0) {
            log.error("根据订单项目ID查询失败：ID不合法，id={}", id);
            return null;
        }

        // 数据库操作 + 异常捕获
        try {
            return orderItemMapper.selectById(id);
        } catch (Exception e) {
            log.error("根据订单项目ID{}查询订单项目失败", id, e);
            return null;
        }
    }

    /**
     * 根据订单号查询订单项目列表
     * 
     * @param orderNo 订单号
     * @return 订单项目列表（无数据返回空列表，避免空指针）
     */
    public List<OrderItem> listByOrderNo(String orderNo) {
        // 参数合法性校验
        if (orderNo == null || orderNo.trim().isEmpty()) {
            log.error("根据订单号查询订单项目失败：订单号为空");
            return Collections.emptyList();
        }

        // 数据库操作 + 异常捕获 + 结果兜底
        try {
            List<OrderItem> orderItemList = orderItemMapper.selectByOrderNo(orderNo);
            // 兜底：查询结果为null时返回空列表
            return CollectionUtils.isEmpty(orderItemList) ? Collections.emptyList() : orderItemList;
        } catch (Exception e) {
            log.error("根据订单号{}查询订单项目列表失败", orderNo, e);
            return Collections.emptyList();
        }
    }

    /**
     * 新增订单项目信息
     * 
     * @param orderItem 订单项目实体
     * @return 新增成功返回true，失败返回false
     */
    public boolean addOrderItem(OrderItem orderItem) {
        // 多层参数校验（核心字段非空/合法校验）
        if (orderItem == null) {
            log.error("新增订单项目失败：订单项目实体为空");
            return false;
        }
        if (orderItem.getOrderNo() == null || orderItem.getOrderNo().trim().isEmpty()) {
            log.error("新增订单项目失败：订单号为空");
            return false;
        }
        if (orderItem.getProductId() == null || orderItem.getProductId() <= 0) {
            log.error("新增订单项目失败：商品ID不合法，productId={}", orderItem.getProductId());
            return false;
        }
        if (orderItem.getQuantity() == null || orderItem.getQuantity() <= 0) {
            log.error("新增订单项目失败：数量不合法，quantity={}", orderItem.getQuantity());
            return false;
        }
        if (orderItem.getMerchantId() == null || orderItem.getMerchantId() <= 0) {
            log.error("新增订单项目失败：商家ID不合法，merchantId={}", orderItem.getMerchantId());
            return false;
        }

        // 数据库插入操作 + 结果校验
        try {
            int affectRows = orderItemMapper.insertOrderItem(orderItem);
            boolean success = affectRows > 0;
            if (success) {
                log.info("新增订单项目成功，订单号={}，商品ID={}，生成的订单项目ID={}",
                        orderItem.getOrderNo(), orderItem.getProductId(), orderItem.getId());
            } else {
                log.warn("新增订单项目失败：无数据插入，订单号={}", orderItem.getOrderNo());
            }
            return success;
        } catch (Exception e) {
            log.error("新增订单项目异常，订单号={}，商品ID={}", orderItem.getOrderNo(), orderItem.getProductId(), e);
            return false;
        }
    }

    /**
     * 批量插入订单项目（merchantId为自身字段，严格校验）
     * 
     * @param orderNo       订单号（外键关联OrderMain）
     * @param orderItemList 订单项目列表（每个项含merchantId）
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchAddOrderItem(String orderNo, List<OrderItem> orderItemList) {
        if (CollectionUtils.isEmpty(orderItemList)) {
            log.warn("批量插入订单项目：列表为空，无需插入");
            return;
        }

        // 1. 遍历校验+绑定外键orderNo
        for (OrderItem item : orderItemList) {
            item.setOrderNo(orderNo); // 绑定外键

            // 强化校验：merchantId是必传且合法的
            if (item.getMerchantId() == null || item.getMerchantId() <= 0) {
                throw new RuntimeException(
                        "订单项目的商家ID不合法：productId=" + item.getProductId() + "，merchantId=" + item.getMerchantId());
            }
            if (item.getProductId() == null || item.getProductId() <= 0) {
                throw new RuntimeException("订单项目的商品ID不合法：productId=" + item.getProductId());
            }
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new RuntimeException(
                        "订单项目的数量不合法：productId=" + item.getProductId() + "，quantity=" + item.getQuantity());
            }
        }

        // 2. 批量插入
        int successCount = 0;
        for (OrderItem item : orderItemList) {
            int affectRows = orderItemMapper.insertOrderItem(item);
            if (affectRows > 0) {
                successCount++;
                log.info("插入订单项目成功：orderNo={}，productId={}，merchantId={}，自增Id={}",
                        orderNo, item.getProductId(), item.getMerchantId(), item.getId());
            } else {
                throw new RuntimeException(
                        "插入订单项目失败：productId=" + item.getProductId() + "，merchantId=" + item.getMerchantId());
            }
        }

        log.info("批量插入订单项目完成：orderNo={}，共{}条，成功{}条",
                orderNo, orderItemList.size(), successCount);
    }
}