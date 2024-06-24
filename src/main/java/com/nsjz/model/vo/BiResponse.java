package com.nsjz.model.vo;

import lombok.Data;

/**
 * @author 郭春燕
 *
 * BI返回结果
 */
@Data
public class BiResponse {

    private String genChart;

    private String genResult;

    private Long chartId;
}
