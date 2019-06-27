package com.debug.steadyjack.controller;

import com.debug.steadyjack.dto.User;
import com.debug.steadyjack.response.BaseResponse;
import com.debug.steadyjack.response.StatusCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.AbstractJavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Created by Administrator on 2018/8/23.
 */
@RestController
public class RabbitController {

    private static final Logger log= LoggerFactory.getLogger(HelloWorldController.class);

    private static final String Prefix="rabbit";

    @Autowired
    private Environment env;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;


    /**
     * 发送简单消息
     * @param message
     * @return
     */
    @RequestMapping(value = Prefix+"/simple/message/send",method = RequestMethod.GET)
    public BaseResponse sendSimpleMessage(@RequestParam String message){
        BaseResponse response=new BaseResponse(StatusCode.Success);
        try {
            log.info("待发送的消息： {} ",message);

            rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
            rabbitTemplate.setExchange(env.getProperty("basic.info.mq.exchange.name"));
            rabbitTemplate.setRoutingKey(env.getProperty("basic.info.mq.routing.key.name"));

            //Message msg=MessageBuilder.withBody(message.getBytes("UTF-8")).build();
            //rabbitTemplate.send(msg);

            Message msg=MessageBuilder.withBody(objectMapper.writeValueAsBytes(message)).build();
            rabbitTemplate.send(msg);
        }catch (Exception e){
            log.error("发送简单消息发生异常： ",e.fillInStackTrace());
        }
        return response;
    }


    /**
     * 发送对象消息
     * @param user
     * @return
     */
    @RequestMapping(value = Prefix+"/object/message/send",method = RequestMethod.POST,consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public BaseResponse sendObjectMessage(@RequestBody User user){
        BaseResponse response=new BaseResponse(StatusCode.Success);
        try {
            log.info("待发送的消息： {} ",user);

            rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
            rabbitTemplate.setExchange(env.getProperty("basic.info.mq.exchange.name"));
            rabbitTemplate.setRoutingKey(env.getProperty("basic.info.mq.routing.key.name"));

            /*Message msg=MessageBuilder.withBody(objectMapper.writeValueAsBytes(user)).setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                    .build();
            rabbitTemplate.convertAndSend(msg);*/

            rabbitTemplate.convertAndSend(user, new MessagePostProcessor() {
                @Override
                public Message postProcessMessage(Message message) throws AmqpException {
                    MessageProperties messageProperties=message.getMessageProperties();
                    messageProperties.setHeader(AbstractJavaTypeMapper.DEFAULT_CONTENT_CLASSID_FIELD_NAME,User.class);
                    return message;
                }
            });

        }catch (Exception e){
            log.error("发送对象消息发生异常： ",e.fillInStackTrace());
        }
        return response;
    }


    /**
     * 发送多类型字段消息
     * @return
     */
    @RequestMapping(value = Prefix+"/multi/type/message/send",method = RequestMethod.GET)
    public BaseResponse sendMultiTypeMessage(){
        BaseResponse response=new BaseResponse(StatusCode.Success);
        try {
            Integer id=120;
            String name="阿修罗";
            Long longId=12000L;
            Map<String,Object> dataMap= Maps.newHashMap();

            dataMap.put("id",id);
            dataMap.put("name",name);
            dataMap.put("longId",longId);
            log.info("待发送的消息： {} ",dataMap);

            rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
            rabbitTemplate.setExchange(env.getProperty("basic.info.mq.exchange.name"));
            rabbitTemplate.setRoutingKey(env.getProperty("basic.info.mq.routing.key.name"));

            Message message=MessageBuilder.withBody(objectMapper.writeValueAsBytes(dataMap)).build();
            rabbitTemplate.convertAndSend(message);
        }catch (Exception e){
            log.error("发送多类型字段消息发生异常： ",e.fillInStackTrace());
        }
        return response;
    }
}





























