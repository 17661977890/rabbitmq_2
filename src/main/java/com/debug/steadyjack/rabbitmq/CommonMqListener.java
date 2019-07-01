package com.debug.steadyjack.rabbitmq;

import com.debug.steadyjack.entity.UserLog;
import com.debug.steadyjack.mapper.UserLogMapper;
import com.debug.steadyjack.service.MailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * 单一消费者监听案例(并发量不高的场景使用-----商城抢单、秒杀活动不适用)
 */
@Component
public class CommonMqListener {

    private static final Logger log= LoggerFactory.getLogger(CommonMqListener.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserLogMapper userLogMapper;

    @Autowired
    private MailService mailService;

    /**
     * 监听消费用户日志
     * 使用 @Payload 和 @Headers 注解，消息中的 body 与 headers 信息
     * @RabbitListener 也可以指定在类上，当标注在类上面，需配合 @RabbitHandler 注解一起使用；
     * 如果监听一个队列，有多个监听消费消息的方法，具体使用哪个方法处理，根据 MessageConverter 转换后的参数类型
     * 如果不同队列，则此注解@RabbitListener在方法上使用。
     * @param message
     */
    @RabbitListener(queues = "${log.user.queue.name}",containerFactory = "singleListenerContainer")
    public void consumeUserLogQueue(@Payload byte[] message){
        try {
            UserLog userLog=objectMapper.readValue(message, UserLog.class);
            log.info("监听消费用户日志 监听到消息： {} ",userLog);

            userLogMapper.insertSelective(userLog);
        }catch (Exception e){
            e.printStackTrace();
        }
    }



    /**
     * 监听消费邮件发送
     * @param message
     */
    @RabbitListener(queues = "${mail.queue.name}",containerFactory = "singleListenerContainer")
    public void consumeMailQueue(@Payload byte[] message){
        try {
            log.info("监听消费邮件发送 监听到消息： {} ",new String(message,"UTF-8"));
            //调用邮件发送工具类，发送相关配置再yml配置
            mailService.sendEmail();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * 监听消费死信队列中的消息
     * @param message
     */
    @RabbitListener(queues = "${simple.dead.real.queue.name}",containerFactory = "singleListenerContainer")
    public void consumeDeadQueue(@Payload byte[] message){
        try {
            log.info("监听消费死信队列中的消息： {} ",new String(message,"UTF-8"));

            mailService.sendEmail();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


}






























