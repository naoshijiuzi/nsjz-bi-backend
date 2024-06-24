package com.nsjz.common;

/**
 * @author 郭春燕
 */
public enum ChartType {
    LineChart(1,"折线图"),
    Histogram(2,"柱状图"),
    StackDiagram(3,"堆叠图"),
    PieChart(4,"饼图"),
    RadarMap(5,"雷达图"),
    ;

    private final int code;
    private final String description;

    ChartType(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public boolean isContain(String name){
        for (ChartType c :
                ChartType.values()) {
            if (c.getDescription().equals(name)){
                return true;
            }
        }
        return false;
    }
}
