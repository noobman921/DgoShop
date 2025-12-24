package com.example.shop.service;

import com.example.shop.entity.Merchant;
import com.example.shop.mapper.MerchantMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * 商家业务层（单类实现，不拆分接口）
 * 封装商家相关的所有业务逻辑，直接对接 MerchantMapper 操作数据库
 */
@Service // 标注为Spring服务类，可被自动注入
public class MerchantService {

    // 注入Mapper层，用于数据库操作
    @Autowired
    private MerchantMapper merchantMapper;

    /**
     * 根据商家ID查询商家信息
     * 
     * @param merchantId 商家主键ID（不能为空且>0）
     * @return 商家实体（无数据返回null）
     */
    public Merchant getMerchantById(Long merchantId) {
        // 1. 参数合法性校验
        if (merchantId == null || merchantId <= 0) {
            return null;
        }

        // 2. 执行数据库查询，捕获异常
        try {
            Merchant merchant = merchantMapper.selectByMerchantId(merchantId);
            return merchant;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 根据商家账号查询商家信息
     * 
     * @param account 商家账号（不能为空/空串）
     * @return 商家实体（无数据返回null）
     */
    public Merchant getMerchantByAccount(String account) {
        // 1. 参数合法性校验
        if (!StringUtils.hasText(account)) {
            return null;
        }

        // 2. 执行数据库查询，捕获异常
        try {
            Merchant merchant = merchantMapper.selectByAccount(account);
            return merchant;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 根据商家名称查询商家列表
     * 
     * @param merchantName 商家名称（可为空，为空返回空列表）
     * @return 商家列表（无数据返回空列表，避免NPE）
     */
    public List<Merchant> listMerchantByMerchantName(String merchantName) {
        // 1. 参数合法性校验
        if (!StringUtils.hasText(merchantName)) {
            return Collections.emptyList(); // 返回空列表，避免调用方空指针
        }

        // 2. 执行数据库查询，捕获异常
        try {
            List<Merchant> merchantList = merchantMapper.selectListByMerchantName(merchantName);
            return merchantList;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * 新增商家信息
     * 
     * @param merchant 商家实体（必填：merchantName/account/password）
     * @return 新增成功返回true，失败返回false
     */
    public boolean addMerchant(Merchant merchant) {
        // 1. 全量参数校验
        if (merchant == null) {
            return false;
        }
        if (!StringUtils.hasText(merchant.getMerchantName())) {
            return false;
        }
        if (!StringUtils.hasText(merchant.getAccount())) {
            return false;
        }
        if (!StringUtils.hasText(merchant.getPassword())) {
            return false;
        }

        // 2. 校验账号唯一性（避免数据库重复插入报错）
        Merchant existMerchant = this.getMerchantByAccount(merchant.getAccount());
        if (existMerchant != null) {
            return false;
        }

        // 3. 执行插入操作，捕获异常
        try {
            int affectRows = merchantMapper.insertMerchant(merchant);
            boolean success = affectRows > 0;
            return success;
        } catch (Exception e) {
            return false;
        }
    }
}