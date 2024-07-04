package com.nsjz.controller;

import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author 郭春燕
 */
@RestController
@RequestMapping("/queue")
@Slf4j
@Profile({"dev","local"})
public class QueueController {

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @GetMapping("/add")
    public void add(String name){
        //运行一个异步任务
        CompletableFuture.runAsync(()->{
            log.info("任务执行中："+name+",执行人："+Thread.currentThread().getName());
            try {
                Thread.sleep(600000);
            } catch (InterruptedException e) {
               e.printStackTrace();
            }
        },threadPoolExecutor);
    }

    @GetMapping("/get")
    public String get(){
        HashMap<String, Object> map = new HashMap<>();

        //线程池队列长度
        int size = threadPoolExecutor.getQueue().size();
        //线程池总任务数
        long taskCount = threadPoolExecutor.getTaskCount();
        //线程池已完成的任务数
        long completedTaskCount = threadPoolExecutor.getCompletedTaskCount();
        //线程池中正在执行的任务数
        int activeCount = threadPoolExecutor.getActiveCount();

        map.put("队列长度",size);
        map.put("任务总数",taskCount);
        map.put("已完成任务数",completedTaskCount);
        map.put("正在工作的线程数",activeCount);

        return JSONUtil.toJsonStr(map);



    }

}
