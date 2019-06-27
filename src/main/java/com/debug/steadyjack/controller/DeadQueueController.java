package com.debug.steadyjack.controller;

import com.debug.steadyjack.response.BaseResponse;
import com.debug.steadyjack.response.StatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Administrator on 2018/9/1.
 */
@RestController
public class DeadQueueController {

    private static final Logger log= LoggerFactory.getLogger(MailController.class);

    private static final String Prefix="dead/queue";

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private Environment env;


    @RequestMapping(value = Prefix+"/send",method = RequestMethod.GET)
    public BaseResponse sendMail(){
        BaseResponse response=new BaseResponse(StatusCode.Success);
        try {
            rabbitTemplate.setExchange(env.getProperty("simple.produce.exchange.name"));
            rabbitTemplate.setRoutingKey(env.getProperty("simple.produce.routing.key.name"));

            String str="死信队列的消息";
            Message message=MessageBuilder.withBody(str.getBytes("UTF-8")).build();
            rabbitTemplate.convertAndSend(message);

        }catch (Exception e){
            e.printStackTrace();
        }

        log.info("发送消息完毕----");
        return response;
    }
}






























