package com.nsjz.manager;

import com.nsjz.common.ErrorCode;
import com.nsjz.exception.BusinessException;
import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.common.BaseResponse;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * @author 郭春燕
 * 对接AI
 */
@Service
public class AIManager {


    public String doChart(long modelId,String message){

        String ak="u1rfqvatlk4zy7907f9yt1nfao01bqcq";
        String sk="h8yvpqxws5ov3ym2r3cr3yy4m15qnwde";

        //构造请求
        DevChatRequest devChatRequest = new DevChatRequest();
        //我发现了一个 AI 对话助手，点击链接进行聊天：https://www.yucongming.com/model/1780133266368929793?inviteUser=1763475263783915521
        devChatRequest.setModelId(modelId);
        devChatRequest.setMessage(message);

        YuCongMingClient yuCongMingClient = new YuCongMingClient(ak, sk);
        //获取响应
        BaseResponse<DevChatResponse> response = yuCongMingClient.doChat(devChatRequest);
        if(response==null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"AI响应错误");
        }

        return response.getData().getContent();

    }
}

