package com.debug.steadyjack.rabbitmq;

import com.debug.steadyjack.entity.UserOrder;
import com.debug.steadyjack.mapper.UserOrderMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Created by Administrator on 2018/9/1.
 */
@Component
public class UserOrderDeadListener {

    private static final Logger log= LoggerFactory.getLogger(UserOrderDeadListener.class);

    @Autowired
    private UserOrderMapper userOrderMapper;


    @RabbitListener(queues = "${user.order.dead.real.queue.name}",containerFactory = "multiListenerContainer")
    public void consumeMessage(@Payload Integer id){
        try {
            log.info("死信队列-用户下单超时未支付监听消息： {} ",id);

            UserOrder entity=userOrderMapper.selectByPkAndStatus(id,1);
            if (entity!=null){
                entity.setStatus(3);
                entity.setUpdateTime(new Date());
                userOrderMapper.updateByPrimaryKeySelective(entity);
            }else{
                //TODO：已支付-可能需要异步 减库存-异步发送其他日志消息

            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    @RabbitListener(queues = "${dynamic.dead.real.queue.name}",containerFactory = "multiListenerContainer")
    public void consumeMessageDynamic(@Payload Integer id){
        try {
            log.info("死信队列-动态TTL-用户下单超时未支付监听消息： {} ",id);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

}