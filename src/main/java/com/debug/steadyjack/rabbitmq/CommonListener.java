package com.debug.steadyjack.rabbitmq;

import com.debug.steadyjack.dto.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by Administrator on 2018/8/23.
 */
@Component
public class CommonListener {

    private static final Logger log= LoggerFactory.getLogger(CommonListener.class);

    @Autowired
    private ObjectMapper objectMapper;


    /**
     * 监听消费消息
     * @param message
     */
//    @RabbitListener(queues = "${basic.info.mq.queue.name}",containerFactory = "singleListenerContainer")
//    public void consumeMessage(@Payload byte[] message){
//        try {
//            //TODO：接收String
//            /*String result=new String(message,"UTF-8");
//            log.info("接收到消息： {} ",result);*/
//
//            //TODO：接收对象
//            /*User user=objectMapper.readValue(message, User.class);
//            log.info("接收到消息： {} ",user);*/
//
//            //TODO：接收多类型字段信息
//            Map<String,Object> resMap=objectMapper.readValue(message,Map.class);
//            log.info("接收到消息： {} ",resMap);
//        }catch (Exception e){
//            log.error("监听消费消息 发生异常： ",e.fillInStackTrace());
//        }
//    }

    /**
     * 监听消费消息-map多类型字段信息
     */
//    @RabbitListener(queues = "${basic.info.mq.queue.name}",containerFactory = "singleListenerContainer")
//    public void consumeMessage(Map<String,Object> resMap){
//        try {
//            //Map<String,Object> resMap=objectMapper.readValue(message,Map.class);
//            log.info("接收到消息： {} ",resMap);
//        }catch (Exception e){
//            log.error("监听消费消息 发生异常： ",e.fillInStackTrace());
//        }
//    }

    /**
     * 监听消费消息-实体信息
     */
    @RabbitListener(queues = "${basic.info.mq.queue.name}",containerFactory = "singleListenerContainer")
    public void consumeMessage(@Payload User user){
        try {
            log.info("接收到实体消息： {} ",user);
        }catch (Exception e){
            log.error("监听消费消息 发生异常： ",e.fillInStackTrace());
        }
    }
}

















