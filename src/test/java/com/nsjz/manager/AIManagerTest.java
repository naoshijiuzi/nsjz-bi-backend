package com.nsjz.manager;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author 郭春燕
 */
@SpringBootTest
class AIManagerTest {

    @Resource
    private AIManager aiManager;

    @Test
    void doChart() {
        String data= "日期,用户数\n1号,10\n2号,20\n3号,30\n4号,90\n5号,0\n6号,10\n7号,20\n";
        String s = aiManager.doChart(1780133266368929793L,data);
        System.out.println(s);
    }
}