package com.nsjz.contant;

/**
 * @author 郭春燕
 * AI执行任务的状态
 */
public interface TaskStatus {

    String WAIT = "wait";

    String RUNNING = "running";

    String SUCCEED = "succeed";

    String FAILED = "failed";

}
