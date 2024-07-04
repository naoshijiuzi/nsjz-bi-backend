package com.nsjz.controller;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nsjz.annotation.AuthCheck;
import com.nsjz.common.*;
import com.nsjz.contant.CommonConstant;
import com.nsjz.contant.TaskStatus;
import com.nsjz.exception.BusinessException;
import com.nsjz.manager.AIManager;
import com.nsjz.manager.RedisLimiterManager;
import com.nsjz.model.dto.chart.ChartAddRequest;
import com.nsjz.model.dto.chart.ChartQueryRequest;
import com.nsjz.model.dto.chart.ChartUpdateRequest;
import com.nsjz.model.dto.chart.GenChartByAiRequest;
import com.nsjz.model.dto.user.UserLoginRequest;
import com.nsjz.model.entity.Chart;
import com.nsjz.model.entity.User;
import com.nsjz.model.vo.BiResponse;
import com.nsjz.service.ChartService;


import com.nsjz.service.UserService;
import com.nsjz.utils.ExcelUtils;
import com.nsjz.utils.SqlUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;


/**
 * 帖子接口
 *
 * @author yupi
 */
@RestController
@RequestMapping("/chart")
@Slf4j
public class ChartController {

    @Resource
    private ChartService chartService;

    @Resource
    private UserService userService;

    @Resource
    private AIManager aiManager;

    @Resource
    private RedisLimiterManager redisLimiterManager;

    // region 增删改查

    /**
     * 创建
     *
     * @param chartAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addChart(@RequestBody ChartAddRequest chartAddRequest, HttpServletRequest request) {
        if (chartAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartAddRequest, chart);
        // 校验

        User loginUser = userService.getLoginUser(request);
        chart.setUserId(loginUser.getId());
        boolean result = chartService.save(chart);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        long newChartId = chart.getId();
        return ResultUtils.success(newChartId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteChart(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        if (oldChart == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 仅本人或管理员可删除
        if (!oldChart.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        boolean b = chartService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新
     *
     * @param chartUpdateRequest
     * @param request
     * @return
     */
    @AuthCheck(mustRole = "admin")
    @PostMapping("/update")
    public BaseResponse<Boolean> updateChart(@RequestBody ChartUpdateRequest chartUpdateRequest,
                                             HttpServletRequest request) {
        if (chartUpdateRequest == null || chartUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartUpdateRequest, chart);
        Long id = chartUpdateRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        if (oldChart == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }

        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Chart> getChartById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = chartService.getById(id);
        return ResultUtils.success(chart);
    }

    /**
     * 获取列表（仅管理员可使用）
     *
     * @param chartQueryRequest
     * @return
     */
    @AuthCheck(mustRole = "admin")
    @GetMapping("/list")
    public BaseResponse<List<Chart>> listChart(ChartQueryRequest chartQueryRequest) {
        Chart chartQuery = new Chart();
        if (chartQueryRequest != null) {
            BeanUtils.copyProperties(chartQueryRequest, chartQuery);
        }
        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>(chartQuery);
        List<Chart> chartList = chartService.list(queryWrapper);
        return ResultUtils.success(chartList);
    }

    /**
     * 分页获取列表
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/list/page")
    public BaseResponse<Page<Chart>> listChartByPage(ChartQueryRequest chartQueryRequest, HttpServletRequest request) {

        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();

        // 限制爬虫
        if (size > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Page<Chart> chartPage = chartService.page(new Page<>(current, size), getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page")
    public BaseResponse<Page<Chart>> listMyChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
                                                       HttpServletRequest request) {
        if (chartQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        chartQueryRequest.setUserId(loginUser.getId());
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        if (size > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }


    /**
     * 获取查询包装类
     *
     * @param chartQueryRequest
     * @return
     */
    private QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest) {
        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();
        if (chartQueryRequest == null) {
            return queryWrapper;
        }
        Long id = chartQueryRequest.getId();
        String name = chartQueryRequest.getName();
        String goal = chartQueryRequest.getGoal();
        String chartType = chartQueryRequest.getChartType();
        Long userId = chartQueryRequest.getUserId();
        String sortField = chartQueryRequest.getSortField();
        String sortOrder = chartQueryRequest.getSortOrder();

        queryWrapper.eq(id != null && id > 0, "id", id);
        queryWrapper.eq(StringUtils.isNotBlank(goal), "goal", goal);
        queryWrapper.eq(StringUtils.isNotBlank(name), "name", name);
        queryWrapper.eq(StringUtils.isNotBlank(chartType), "chartType", chartType);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    // endregion




    @PostMapping("/test")
    public BaseResponse<String> genChartByAitest(@RequestBody GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();

        //校验
        //分析目标为空
        if (StringUtils.isBlank(goal)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "目标为空");
        }
        //分析目标为空
        if (StringUtils.isNotBlank(name) && name.length() > 100) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "名称过长");
        }

        return ResultUtils.success("ok");
    }

    @PostMapping("/test2")
    @Operation(summary = "文件上传-不带参数")
    public BaseResponse<String> uploadFile(@RequestPart("file") MultipartFile multipartFile, HttpServletRequest request) {
        String result = ExcelUtils.excelToCsv(multipartFile);
        return ResultUtils.success(result);
    }

    @PostMapping("/gen")
    @Operation(summary = "文件上传-带多个参数")
    public BaseResponse<BiResponse> uploadFile2(@RequestParam("file") MultipartFile multipartFile, String name, String goal, String chartType, HttpServletRequest request) {
        //校验
        //分析目标为空
        if (StringUtils.isBlank(goal)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "目标为空");
        }
        //图表名称为空或过长
        if (StringUtils.isNotBlank(name) && name.length() > 100) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "名称过长");
        }
        //图表类型
        if (StringUtils.isBlank(chartType) || !ChartType.StackDiagram.isContain(chartType)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请选择图表类型");
        }
        //校验文件
        long size = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        if(size>CommonConstant.ONE_MB){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"文件过大，不得超过1MB");
        }
        String suffix = FileUtil.getSuffix(originalFilename);
        if(CommonConstant.validFileSuffixList.contains(suffix)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"文件后缀非法");
        }

        GenChartByAiRequest genChartByAiRequest = new GenChartByAiRequest();
        genChartByAiRequest.setChartType(chartType);
        genChartByAiRequest.setName(name);
        genChartByAiRequest.setFile(multipartFile);
        genChartByAiRequest.setGoal(goal);



        User loginUser = userService.getLoginUser(request);
        //模型Id先写死
        long biModelId = 1780133266368929793L;

        //构造用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");
        String userGoal=goal;
        userGoal+=",请使用"+chartType;
        userInput.append(userGoal).append("\n");

        //压缩数据
        String csvData = ExcelUtils.excelToCsv(genChartByAiRequest.getFile());
        userInput.append(csvData).append("\n");

        //获取并处理AI返回结果
        String result = aiManager.doChart(biModelId, userInput.toString());
        String[] splits = result.split("【【【【【");
        if(splits.length<3){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"AI生成错误");
        }

        //保存图表
        String genChart = splits[1].trim();
        String genResult = splits[2].trim();
        Chart chart = new Chart();
        chart.setName(name);
        chart.setGoal(goal);
        chart.setChartData(csvData);
        chart.setGenChart(genChart);
        chart.setGenResult(genResult);
        chart.setUserId(loginUser.getId());
        boolean save = chartService.save(chart);
        if(!save){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"图表保存失败");
        }

        BiResponse biResponse = new BiResponse();
        biResponse.setChartId(chart.getId());
        biResponse.setGenChart(genChart);
        biResponse.setGenResult(genResult);
        System.out.println(csvData);

        return ResultUtils.success(biResponse);
    }

    @PostMapping("/gen/async")
    @Operation(summary = "异步分析")
    public BaseResponse<BiResponse> genChartByAiAsync(@RequestParam("file") MultipartFile multipartFile, String name, String goal, String chartType, HttpServletRequest request) {
        //校验
        //分析目标为空
        if (StringUtils.isBlank(goal)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "目标为空");
        }
        //图表名称为空或过长
        if (StringUtils.isNotBlank(name) && name.length() > 100) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "名称过长");
        }
        //图表类型
        if (StringUtils.isBlank(chartType) || !ChartType.StackDiagram.isContain(chartType)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请选择图表类型");
        }
        //校验文件
        long size = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        if(size>CommonConstant.ONE_MB){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"文件过大，不得超过1MB");
        }
        String suffix = FileUtil.getSuffix(originalFilename);
        if(CommonConstant.validFileSuffixList.contains(suffix)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"文件后缀非法");
        }

        GenChartByAiRequest genChartByAiRequest = new GenChartByAiRequest();
        genChartByAiRequest.setChartType(chartType);
        genChartByAiRequest.setName(name);
        genChartByAiRequest.setFile(multipartFile);
        genChartByAiRequest.setGoal(goal);



        User loginUser = userService.getLoginUser(request);
        //模型Id先写死
        long biModelId = 1780133266368929793L;

        //构造用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");
        String userGoal=goal;
        userGoal+=",请使用"+chartType;
        userInput.append(userGoal).append("\n");

        //压缩数据
        String csvData = ExcelUtils.excelToCsv(genChartByAiRequest.getFile());
        userInput.append(csvData).append("\n");


        //保存图表
        Chart chart = new Chart();
        chart.setName(name);
        chart.setGoal(goal);
        chart.setChartData(csvData);
        chart.setStatus(TaskStatus.WAIT);
        chart.setUserId(loginUser.getId());
        boolean save = chartService.save(chart);
        if(!save){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"图表保存失败");
        }


        //获取并处理AI返回结果
        CompletableFuture.runAsync(()->{
            Chart chart1 = new Chart();
            chart1.setId(chart.getId());
            chart1.setStatus(TaskStatus.RUNNING);

            boolean b = chartService.updateById(chart1);
            if(!b){
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新图表执行中状态失败");
            }

            String result = aiManager.doChart(biModelId, userInput.toString());
            String[] splits = result.split("【【【【【");
            if(splits.length<3){
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"AI生成错误");
            }
            String genChart = splits[1].trim();
            String genResult = splits[2].trim();
            Chart chartResult = new Chart();
            chartResult.setId(chart.getId());
            chartResult.setGenChart(genChart);
            chartResult.setGenResult(genResult);
            chartResult.setStatus(TaskStatus.SUCCEED);
            boolean b1 = chartService.updateById(chartResult);
            if(!b1){
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"更新图表成功状态失败");
            }
        });


//        chart.setGenChart(genChart);
//        chart.setGenResult(genResult);

        BiResponse biResponse = new BiResponse();
        biResponse.setChartId(chart.getId());
//        biResponse.setGenChart(genChart);
//        biResponse.setGenResult(genResult);
        System.out.println(csvData);

        return ResultUtils.success(biResponse);
    }


    @Operation(summary = "文件上传-带对象参数")
    @PostMapping("/gentest")
    @Parameter(name = "file", schema = @Schema(type = "file", description = "附件一"))
    public BaseResponse<String> genChartByAi(@RequestBody GenChartByAiRequest genChartByAiRequest, HttpServletRequest request) {
        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();

        //1.校验
        //分析目标为空
        if (StringUtils.isBlank(goal)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "目标为空");
        }
        //图表名称为空或过长
        if (StringUtils.isNotBlank(name) && name.length() > 100) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "名称过长");
        }

        User loginUser = userService.getLoginUser(request);
        //模型Id先写死
        long biModelId = 1780133266368929793L;

        //构造用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");
        String userGoal=goal;
        userGoal+=",请使用"+chartType;
        userInput.append(userGoal).append("\n");

        //压缩数据
        String csvData = ExcelUtils.excelToCsv(genChartByAiRequest.getFile());
        userInput.append(csvData).append("\n");

        //

        return ResultUtils.success(csvData);

    }



}
