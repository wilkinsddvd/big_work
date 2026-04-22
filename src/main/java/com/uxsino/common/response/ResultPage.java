package com.uxsino.common.response;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * @className ResultPage
 * @description 分页结果封装类
 * @date 2021/1/16 19:39
 */
@Data
public class ResultPage<T> {

    /**
     * 当前页码
     */
    private Integer packageNum;

    /**
     * 每页数量
     */
    private Integer packageSize;

    /**
     * 总条数
     */
    private Long totalNum;

    /**
     * 分页数据
     */
    private List<T> content;

    /**
     * 将SpringData分页后的Page转为分页信息
     *
     * @param page
     * @param <T>
     * @return
     */
    public static <T> ResultPage<T> restPage(Page<T> page) {
        ResultPage<T> result = new ResultPage<T>();
        result.setPackageNum(page.getNumber() + 1);
        result.setPackageSize(page.getSize());
        result.setTotalNum(page.getTotalElements());
        result.setContent(page.getContent());
        return result;
    }

    /**
     * 将SpringData分页后的page转为分页信息，同时更换content类型
     *
     * @param page
     * @param content
     * @param <E>
     * @param <T>
     * @return
     */
    public static <E, T> ResultPage<E> restPage(Page<T> page, List<E> content) {
        ResultPage<E> result = new ResultPage<>();
        result.setPackageNum(page.getNumber() + 1);
        result.setPackageSize(page.getSize());
        result.setTotalNum(page.getTotalElements());
        result.setContent(content);
        return result;
    }

    /**
     * 将传入的信息封装成ResultPage对象
     * @param pageNum
     * @param pageSize
     * @param total
     * @param content
     * @param <E>
     * @param <T>
     * @return
     */
    public static <E, T> ResultPage<E> restPage(int pageNum, int pageSize, long total, List<E> content) {
        ResultPage<E> result = new ResultPage<>();
        result.setPackageNum(pageNum);
        result.setPackageSize(pageSize);
        result.setTotalNum(total);
        result.setContent(content);
        return result;
    }
}
