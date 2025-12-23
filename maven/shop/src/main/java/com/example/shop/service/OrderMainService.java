package com.example.shop.service;

import com.example.shop.entity.OrderMain;
import com.example.shop.mapper.OrderMainMapper;
import com.example.shop.vo.PageResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
public class OrderMainService {

    @Autowired
    private OrderMainMapper orderMainMapper;

    /**
     * 私有方法：生成唯一订单号（含主键冲突重试，内嵌到Service中）
     * 
     * @return 唯一订单号
     */
    private String generateOrderNo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Random random = new Random();
        String orderNo;

        // 重试3次，避免主键冲突
        for (int i = 0; i < 3; i++) {
            String timeStr = sdf.format(new Date());
            int randomNum = random.nextInt(9000) + 1000; // 4位随机数（1000-9999）
            orderNo = timeStr + randomNum;

            // 调用当前Service的getByOrderNo方法校验唯一性
            if (this.getByOrderNo(orderNo) == null) {
                return orderNo;
            }
            log.warn("订单号{}已存在，第{}次重试生成...", orderNo, i + 1);
        }

        throw new RuntimeException("生成唯一订单号失败（3次重试均主键冲突）");
    }

    /**
     * 根据订单号查询订单
     * 
     * @param orderNo 订单号
     * @return 订单主实体（无数据返回 null）
     */
    public OrderMain getByOrderNo(String orderNo) {
        if (orderNo == null || orderNo.trim().isEmpty()) {
            log.error("根据订单号查询失败：订单号为空");
            return null;
        }

        try {
            return orderMainMapper.selectByOrderNO(orderNo);
        } catch (Exception e) {
            log.error("根据订单号{}查询订单失败", orderNo, e);
            return null;
        }
    }

    /**
     * 根据用户ID查询订单列表（不分页）
     * 
     * @param userId 用户主键ID
     * @return 订单列表（无数据返回空列表）
     */
    public List<OrderMain> listByUserId(Long userId) {
        if (userId == null || userId <= 0) {
            log.error("根据用户ID查询订单失败：用户ID不合法，userId={}", userId);
            return Collections.emptyList();
        }

        try {
            List<OrderMain> orderList = orderMainMapper.selectByUserId(userId);
            return CollectionUtils.isEmpty(orderList) ? Collections.emptyList() : orderList;
        } catch (Exception e) {
            log.error("根据用户ID{}查询订单列表失败", userId, e);
            return Collections.emptyList();
        }
    }

    /**
     * 分页查询用户订单列表
     * 
     * @param userId   用户主键ID
     * @param pageNo   当前页号（默认=1，从1开始）
     * @param pageSize 每页条数（默认=4）
     * @return 已有分页VO（PageResultVO），含列表、总条数、总页数等
     */
    public PageResultVO<OrderMain> listByUserIdWithPage(Long userId,
            Integer pageNo,
            Integer pageSize) {
        // 参数默认值处理（页尺寸默认=4）
        int defaultPageNo = 1;
        int defaultPageSize = 4;
        pageNo = (pageNo == null || pageNo < 1) ? defaultPageNo : pageNo;
        pageSize = (pageSize == null || pageSize < 1) ? defaultPageSize : pageSize;

        // 核心参数校验
        if (userId == null || userId <= 0) {
            log.error("分页查询订单失败：用户ID不合法，userId={}", userId);
            // 返回空的PageResultVO
            return new PageResultVO<>(0, 0, pageNo, pageSize, Collections.emptyList());
        }

        try {
            // 计算分页偏移量（MySQL LIMIT偏移量从0开始）
            Integer offset = (pageNo - 1) * pageSize;

            // 查询总条数 + 分页列表
            Integer total = orderMainMapper.selectCountByUserId(userId);
            List<OrderMain> orderList = orderMainMapper.selectByUserIdWithPage(userId, offset, pageSize);

            // 结果兜底（避免null）
            total = total == null ? 0 : total;
            orderList = CollectionUtils.isEmpty(orderList) ? Collections.emptyList() : orderList;

            // 计算总页数（向上取整：(总条数 + 每页条数 - 1) / 每页条数）
            Integer pages = total == 0 ? 0 : (total + pageSize - 1) / pageSize;

            log.info("分页查询订单成功：用户ID={}，页号={}，页尺寸={}，总条数={}，当前页条数={}，总页数={}",
                    userId, pageNo, pageSize, total, orderList.size(), pages);

            // 构造已有PageResultVO返回
            return new PageResultVO<>(total, pages, pageNo, pageSize, orderList);
        } catch (Exception e) {
            log.error("分页查询订单异常：用户ID={}，页号={}，页尺寸={}", userId, pageNo, pageSize, e);
            // 异常时返回空PageResultVO
            return new PageResultVO<>(0, 0, pageNo, pageSize, Collections.emptyList());
        }
    }

    /**
     * 新增订单主信息（仅需用户ID，无商家ID）
     * 
     * @param userId 用户ID
     * @return 新增的OrderMain（含主键orderNo）
     */
    @Transactional(rollbackFor = Exception.class)
    public OrderMain addOrderMain(Long userId) {
        // 调用内嵌的生成方法，生成唯一订单号（作为主键）
        String orderNo = this.generateOrderNo();

        // 封装订单主实体（仅赋值orderNo和userId）
        OrderMain orderMain = new OrderMain();
        orderMain.setOrderNo(orderNo); // 主键赋值
        orderMain.setUserId(userId);

        // 插入数据库
        int affectRows = orderMainMapper.insertOrderMain(orderMain);
        if (affectRows <= 0) {
            log.error("插入订单主信息失败：主键orderNo={}，userId={}", orderNo, userId);
            throw new RuntimeException("插入订单主信息失败（主键冲突/数据库异常）");
        }

        log.info("插入订单主信息成功：主键orderNo={}，userId={}", orderNo, userId);
        return orderMain;
    }
}