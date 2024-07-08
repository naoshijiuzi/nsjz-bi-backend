package com.nsjz.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

/**
 * @author 郭春燕
 */
public class MultiConsumer {
    // 声明队列名称为"multi_queue"
    private static final String TASK_QUEUE_NAME = "multi_queue";

    public static void main(String[] argv) throws Exception {
        // 创建一个新的连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        final Connection connection = factory.newConnection();

        //创建2个消费者
        for (int i = 0; i < 2; i++) {
            final Channel channel = connection.createChannel();

            // 声明一个队列,并设置属性:队列名称,持久化,非排他,非自动删除,其他参数;如果队列不存在,则创建它
            channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);
            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

            // 设置预取计数为1，这样RabbitMQ就会在给消费者新消息之前等待先前的消息被确认,每个消费者最多同时处理 1 个任务
            channel.basicQos(1);

            int finalI = i;

            // 创建消息接收回调函数,以便接收消息
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                // 将接收到的消息转为字符串
                String message = new String(delivery.getBody(), "UTF-8");

                try {
                    System.out.println(" [x] Received '" +"编号："+finalI+" : "+ message + "'");
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(),false);
                    Thread.sleep(20000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    //指定拒绝某条消息
                    //b1:requeue:是否重新入队，可用于重试
                    channel.basicNack(delivery.getEnvelope().getDeliveryTag(),false,false);
                } finally {
                    System.out.println(" [x] Done");
                    // 手动发送应答,告诉RabbitMQ消息已经被处理
                    //basicAck(long deliveryTag, boolean multiple)
                    //multiple: 表示批量确认，也就是说，是否需要一次性确认所有的历史消息，直到当前这条消息为止
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                }
            };
            // 开始消费消息,传入队列名称,是否自动确认,投递回调和消费者取消回调
            //第二个参数autoack：如果在接收到消息后，工作尚未完成，我们是否就不需要确认成功呢？这种情况，建议将 autoack 设置为false，实际情况手动进行确认了
            channel.basicConsume(TASK_QUEUE_NAME, false, deliverCallback, consumerTag -> {});

        }



    }
}
