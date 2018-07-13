package com.yunzhitx.mediacrawler.web.rest.rabbit.demo;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * @author chenqi
 */
public class Send {

//    private final static String QUEUE_NAME = "hello";
//
//    public static void main(String[] argv) throws Exception {
//        ConnectionFactory factory = new ConnectionFactory();
//        factory.setHost("192.168.124.129");
//        factory.setPassword("chenqi");
//        factory.setUsername("chenqi");
//        Connection connection = factory.newConnection();
//        Channel channel = connection.createChannel();
//
//        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
//        String message = "Hello World!";
//        channel.basicPublish("", QUEUE_NAME, null, message.getBytes("UTF-8"));
//        System.out.println(" [x] Sent '" + message + "'");
//
//        channel.close();
//        connection.close();
//    }
}