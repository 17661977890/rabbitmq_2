package com.debug.steadyjack.controller;

import com.debug.steadyjack.dto.LogDto;
import com.debug.steadyjack.dto.User;
import com.debug.steadyjack.dto.UserOrderDto;
import com.debug.steadyjack.entity.UserOrder;
import com.debug.steadyjack.mapper.UserOrderMapper;
import com.debug.steadyjack.response.BaseResponse;
import com.debug.steadyjack.response.StatusCode;
import com.debug.steadyjack.service.CommonLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.AbstractJavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户商城下单controller
 * Created by Administrator on 2018/8/30.
 */
@RestController
public class UserOrderController {

    private static final Logger log= LoggerFactory.getLogger(AcknowledgeController.class);

    private static final String Prefix="user/order";

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Environment env;

    @Autowired
    private CommonLogService logService;

    @Autowired
    private UserOrderMapper userOrderMapper;


    /**
     * 用户商城下单
     * @param dto
     * @return
     */
    @RequestMapping(value = Prefix+"/push",method = RequestMethod.POST,consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public BaseResponse pushUserOrder(@RequestBody UserOrderDto dto){
        BaseResponse response=new BaseResponse(StatusCode.Success);
        try {
            log.debug("接收到数据： {} ",dto);

            //TODO：用户下单记录-入库
            rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
            rabbitTemplate.setExchange(env.getProperty("user.order.exchange.name"));
            rabbitTemplate.setRoutingKey(env.getProperty("user.order.routing.key.name"));
            Message msg=
                    MessageBuilder.withBody(objectMapper.writeValueAsBytes(dto)).setDeliveryMode(MessageDeliveryMode.PERSISTENT).build();
            rabbitTemplate.convertAndSend(msg);

        }catch (Exception e){
            e.printStackTrace();
        }

        try {
            //TODO：系统级别-日志记录-异步分出去
            /*LogDto logDto=new LogDto("pushUserOrder",objectMapper.writeValueAsString(dto));
            logService.insertLog(logDto);*/ //同步写法


            rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
            rabbitTemplate.setExchange(env.getProperty("log.system.exchange.name"));
            rabbitTemplate.setRoutingKey(env.getProperty("log.system.routing.key.name"));

            LogDto logDto=new LogDto("pushUserOrder",objectMapper.writeValueAsString(dto));
            rabbitTemplate.convertAndSend(logDto, new MessagePostProcessor() {
                @Override
                public Message postProcessMessage(Message message) throws AmqpException {
                    MessageProperties properties=message.getMessageProperties();
                    properties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    properties.setHeader(AbstractJavaTypeMapper.DEFAULT_CONTENT_CLASSID_FIELD_NAME,LogDto.class);
                    return message;
                }
            });

            //TODO：还有很多业务逻辑...
            log.info("主线程还是照样坦荡荡的往前走.....");
        }catch (Exception e){
            e.printStackTrace();
        }

        return response;
    }



    /**
     * 用户商城下单
     * @param dto
     * @return
     */
    @RequestMapping(value = Prefix+"/push/dead/queue",method = RequestMethod.POST,consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public BaseResponse pushUserOrderV2(@RequestBody UserOrderDto dto){
        BaseResponse response=new BaseResponse(StatusCode.Success);
        UserOrder userOrder=new UserOrder();
        try {
            BeanUtils.copyProperties(dto,userOrder);
            userOrder.setStatus(1);
            userOrderMapper.insertSelective(userOrder);
        }catch (Exception e){
            e.printStackTrace();
        }

        try {
            Integer id=userOrder.getId();

            rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
            rabbitTemplate.setExchange(env.getProperty("user.order.dead.produce.exchange.name"));
            rabbitTemplate.setRoutingKey(env.getProperty("user.order.dead.produce.routing.key.name"));

            rabbitTemplate.convertAndSend(id, new MessagePostProcessor() {
                @Override
                public Message postProcessMessage(Message message) throws AmqpException {
                    MessageProperties properties=message.getMessageProperties();
                    properties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    properties.setHeader(AbstractJavaTypeMapper.DEFAULT_CONTENT_CLASSID_FIELD_NAME,Integer.class);
                    return message;
                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }
        return response;
    }



    /**
     * 用户商城下单-动态TTL设置
     * @param dto
     * @return
     */
    @RequestMapping(value = Prefix+"/push/dead/queue/v3",method = RequestMethod.POST,consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public BaseResponse pushUserOrderV3(@RequestBody UserOrderDto dto){
        BaseResponse response=new BaseResponse(StatusCode.Success);
        UserOrder userOrder=new UserOrder();
        try {
            BeanUtils.copyProperties(dto,userOrder);
            userOrder.setStatus(1);
            userOrderMapper.insertSelective(userOrder);
            log.info("用户商城下单成功!!");
        }catch (Exception e){
            e.printStackTrace();
        }

        try {
            Integer id=userOrder.getId();

            rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
            rabbitTemplate.setExchange(env.getProperty("dynamic.dead.produce.exchange.name"));
            rabbitTemplate.setRoutingKey(env.getProperty("dynamic.dead.produce.routing.key.name"));

            Long ttl=15000L; //可以用随机数替代

            rabbitTemplate.convertAndSend(id, new MessagePostProcessor() {
                @Override
                public Message postProcessMessage(Message message) throws AmqpException {
                    MessageProperties properties=message.getMessageProperties();
                    properties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    properties.setHeader(AbstractJavaTypeMapper.DEFAULT_CONTENT_CLASSID_FIELD_NAME,Integer.class);

                    properties.setExpiration(String.valueOf(ttl));
                    return message;
                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }

        return response;
    }



}










































