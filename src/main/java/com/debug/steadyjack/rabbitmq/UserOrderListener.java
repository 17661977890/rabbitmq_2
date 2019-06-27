package com.debug.steadyjack.rabbitmq;

import com.debug.steadyjack.dto.UserOrderDto;
import com.debug.steadyjack.entity.UserOrder;
import com.debug.steadyjack.mapper.UserOrderMapper;
import com.debug.steadyjack.service.ConcurrencyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by Administrator on 2018/8/30.
 */
@Component("userOrderListener")
public class UserOrderListener implements ChannelAwareMessageListener{

    private static final Logger log= LoggerFactory.getLogger(UserOrderListener.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserOrderMapper userOrderMapper;

    @Autowired
    private ConcurrencyService concurrencyService;

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        long tag=message.getMessageProperties().getDeliveryTag();
        try {
            byte[] body=message.getBody();
            /*UserOrderDto entity=objectMapper.readValue(body, UserOrderDto.class);
            log.info("用户商城抢单监听到消息： {} ",entity);

            UserOrder userOrder=new UserOrder();
            BeanUtils.copyProperties(entity,userOrder);
            userOrder.setStatus(1);
            userOrderMapper.insertSelective(userOrder);*/

            String mobile=new String(body,"UTF-8");
            log.info("监听到抢单手机号： {} ",mobile);

            concurrencyService.manageRobbing(String.valueOf(mobile));
            channel.basicAck(tag,true);
        }catch (Exception e){
            log.error("用户商城下单 发生异常：",e.fillInStackTrace());
            channel.basicReject(tag,false);
        }
    }
}




























