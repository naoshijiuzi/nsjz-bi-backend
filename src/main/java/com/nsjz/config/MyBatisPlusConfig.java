package com.nsjz.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 郭春燕
 *
 * MyBatis Plus 配置
 */

@Configuration
public class MyBatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() { //注册插件
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor(); //new一个mybatisplus插件对象，再下面注入各插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.POSTGRE_SQL)); // 分页插件
        //interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor()); // 乐观锁插件
        //乐观锁插件机制：Plus是在数据表中加上一个数据版本号 version 字段，表示数据被修改的次数。当数据被修改时，version 值会+1。当事务A要更新数据值时，在读取数据的同时也会读取 version 值，在提交更新时，会校验刚才读取到的 version 值与当前数据库中的 version 值相等
        return interceptor;
    }
}
