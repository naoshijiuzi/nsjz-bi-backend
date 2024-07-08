package com.nsjz.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;

/**
 * @author 郭春燕
 */
public class SingleProducer {

    private final static String QUEUE_NAME = "hello";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            //queueDeclare(String queue, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments)
            //queue: 消息队列名称，同名称的消息队列，只能用同样的参数创建一次
            //durable:消息队列重启后，消息是否丢失
            //exclusive:是否只允许当前这个创建消息队列的连接操作消息队列
            //autoDelete:没有人用队列后，是否要删除队列
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            String message = "hello world啊啊啊啊啊啊";
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println(" [x] Sent '" + message + "'");
        }
    }
}
