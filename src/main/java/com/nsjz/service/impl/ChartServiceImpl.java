package com.nsjz.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nsjz.model.domain.Chart;
import com.nsjz.service.ChartService;
import com.nsjz.mapper.ChartMapper;
import org.springframework.stereotype.Service;

/**
* @author 27297
* @description 针对表【chart(图表信息表)】的数据库操作Service实现
* @createDate 2024-04-30 09:06:45
*/
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
    implements ChartService{

}




