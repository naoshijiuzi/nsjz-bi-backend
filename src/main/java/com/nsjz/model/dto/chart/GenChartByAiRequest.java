package com.nsjz.model.dto.chart;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

/**
 * @author 郭春燕
 */
@Data
public class GenChartByAiRequest implements Serializable {

    /**
     * 上传文件
     */
    @Schema(type = "string", format = "binary")
    private MultipartFile file;

    /**
     * 图表名称
     */
    private String name;
    /**
     * 分析目标
     */
    private String goal;
    /**
     * 图表类型
     */
    private String chartType;

    private static final long serialVersionUID = 1L;
}
