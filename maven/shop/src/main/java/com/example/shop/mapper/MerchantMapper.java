package com.example.shop.mapper;

import com.example.shop.entity.Merchant;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface MerchantMapper {
    /**
     * 根据商家ID查询（主键查询）
     * 
     * @param merchantId 商家主键ID
     * @return 商家实体
     */
    @Select("SELECT * FROM merchant WHERE merchant_id = #{merchantId}")
    Merchant selectByMerchantId(Long merchantId);

    /**
     * 根据商家账号查询
     * 
     * @param account 商家账号
     * @return 商家实体
     */
    @Select("SELECT * FROM merchant WHERE account = #{account}")
    Merchant selectByAccount(String account);

    /**
     * 根据商家名查询
     * 
     * @param merchantName 商家名
     * @return 商家实体
     */
    @Select("SELECT * FROM merchant WHERE merchant_name = #{merchantName}")
    List<Merchant> selectListByMerchantName(String merchantName);

    /**
     * 插入商家信息
     * 
     * @param merchant 商家实体
     * @return 插入结果，影响的行数
     */
    @Insert("INSERT INTO merchant (merchant_name, account, password) " +
            "VALUES (#{merchantName}, #{account}, #{password})")
    @Options(useGeneratedKeys = true, keyProperty = "merchantId")
    int insertMerchant(Merchant merchant);
}
