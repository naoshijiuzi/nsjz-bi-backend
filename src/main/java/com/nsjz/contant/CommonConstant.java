package com.nsjz.contant;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

/**
 * @author 郭春燕
 */
public interface CommonConstant {
    /**
     * 升序
     */
    String SORT_ORDER_ASC = "ascend";

    /**
     * 降序
     */
    String SORT_ORDER_DESC = " descend";

    /**
     * 1MB
     */
    long ONE_MB = 1024 * 1024L;

    /**
     * 文件后缀白名单
     */
    List<String> validFileSuffixList = Arrays.asList("xlsz","xls");

}
