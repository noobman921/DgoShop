package com.example.shop.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResultVO<T> {

    private Integer total; // 总记录数
    private Integer pages; // 总页数
    private Integer pageNo; // 页号
    private Integer pageSize; // 页尺寸
    private List<T> list; // 数据
}
