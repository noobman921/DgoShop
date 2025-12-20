package com.example.shop.vo;

import lombok.Data;
import java.util.List;

@Data
public class PageResultVO<T> {

    // 响应码（200=成功，500=失败，可自定义）
    private Integer code;

    private String msg;
    private Integer pageOffset;
    private Integer pageSize;
    private List<T> list;

    /**
     * 成功响应
     * 
     * @param pageOffset 页偏移
     * @param pageSize   页大小
     * @param list       页内容
     * @return pageResultVO 实体
     */
    public static <T> PageResultVO<T> success(Integer pageOffset, Integer pageSize, List<T> list) {
        PageResultVO<T> pageResultVO = new PageResultVO<>();

        pageResultVO.setCode(200);
        pageResultVO.setMsg("操作成功");

        pageResultVO.setPageOffset(pageOffset);
        pageResultVO.setPageSize(pageSize);
        pageResultVO.setList(list);
        return pageResultVO;
    }

    /**
     * 成功响应
     * 
     * @param msg 消息提示
     * @return pageResultVO 实体
     */
    public static <T> PageResultVO<T> fail(String msg) {
        PageResultVO<T> pageResultVO = new PageResultVO<>();

        pageResultVO.setCode(500);
        pageResultVO.setMsg(msg);

        return pageResultVO;
    }
}
