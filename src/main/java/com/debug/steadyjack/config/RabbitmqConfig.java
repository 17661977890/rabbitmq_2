package com.debug.steadyjack.config;

import com.debug.steadyjack.rabbitmq.SimpleListener;
import com.debug.steadyjack.rabbitmq.UserOrderListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by steadyjack on 2018/8/20.
 */
@Configuration
public class RabbitmqConfig {

    private static final Logger log= LoggerFactory.getLogger(RabbitmqConfig.class);

    @Autowired
    private Environment env;

    @Autowired
    private CachingConnectionFactory connectionFactory;

    @Autowired
    private SimpleRabbitListenerContainerFactoryConfigurer factoryConfigurer;

    /**
     * 单一消费者
     *
     * ACK（Message acknowledgment）默认是true
     * autoACK=true时，消息分发给消费者，则消息就会从内存中删除。如果正在执行的消费者宕机，则会丢失正在处理的消息。
     * 所以autoACK=false时，则需要等待消息应答后才删除，如果消费者宕机，则会交付给其他消费者。
     * 如果rabbitMQ宕机，则内存中的消息全部丢失。所以需要将消息持久化。（已经声明好的队列不能更改）
     * 注意：RabbitMQ不允许重新定义一个已经存在的队列。控制台删除或者重新定义一个队列。
     * @return
     */
    @Bean(name = "singleListenerContainer")
    public SimpleRabbitListenerContainerFactory listenerContainer(){
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(1);
        factory.setPrefetchCount(1);
        factory.setTxSize(1);
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);
        return factory;
    }

    /**
     * 多个消费者
     * @return
     */
    @Bean(name = "multiListenerContainer")
    public SimpleRabbitListenerContainerFactory multiListenerContainer(){
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factoryConfigurer.configure(factory,connectionFactory);
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        factory.setAcknowledgeMode(AcknowledgeMode.NONE);
        factory.setConcurrentConsumers(env.getProperty("spring.rabbitmq.listener.concurrency",int.class));
        factory.setMaxConcurrentConsumers(env.getProperty("spring.rabbitmq.listener.max-concurrency",int.class));
        factory.setPrefetchCount(env.getProperty("spring.rabbitmq.listener.prefetch",int.class));
        return factory;
    }

    /**
     * 消息可靠发送自动重试机制：
     *  rabbitTemplate的ack确认机制是异步的，这种确认机制是一种事后发现机制，并不能同步的发现问题，会有数据丢失的风险
     *
     * ======》 rabbitTemplate的发送流程是这样的：
     * 1 发送数据并返回(不确认rabbitmq服务器已成功接收)
     * 2 异步的接收从rabbitmq返回的ack确认信息
     * 3 收到ack后调用confirmCallback函数
     * @return
     */
    @Bean
    public RabbitTemplate rabbitTemplate(){
        //rabbitmq默认的处理方式为auto ack，这意味着当你从消息队列取出一个消息时，ack自动发送，mq就会将消息删除。而为了保证消息的正确处理，我们需要将消息处理修改为手动确认的方式。
        //开启sender的手工确认模式
        connectionFactory.setPublisherConfirms(true);
        //开启退回模式
        connectionFactory.setPublisherReturns(true);
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        //开启强制委托模式
        rabbitTemplate.setMandatory(true);
        //确认模式
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                log.info("消息发送成功:correlationData({}),ack({}),cause({})",correlationData,ack,cause);
            }
        });
        //未投递到 queue 退回模式
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                log.info("消息丢失:exchange({}),route({}),replyCode({}),replyText({}),message:{}",exchange,routingKey,replyCode,replyText,message);
            }
        });
        return rabbitTemplate;
    }


    //TODO：基本消息模型构建

    @Bean
    public DirectExchange basicExchange(){
        return new DirectExchange(env.getProperty("basic.info.mq.exchange.name"), true,false);
    }

    @Bean(name = "basicQueue")
    public Queue basicQueue(){
        return new Queue(env.getProperty("basic.info.mq.queue.name"), true);
    }

    @Bean
    public Binding basicBinding(){
        return BindingBuilder.bind(basicQueue()).to(basicExchange()).with(env.getProperty("basic.info.mq.routing.key.name"));
    }



    //TODO：商品抢单消息模型创建
    @Bean
    public DirectExchange robbingExchange(){
        return new DirectExchange(env.getProperty("product.robbing.mq.exchange.name"), true,false);
    }

    @Bean(name = "robbingQueue")
    public Queue robbingQueue(){
        return new Queue(env.getProperty("product.robbing.mq.queue.name"), true);
    }

    @Bean
    public Binding robbingBinding(){
        return BindingBuilder.bind(robbingQueue()).to(robbingExchange()).with(env.getProperty("product.robbing.mq.routing.key.name"));
    }




    //TODO：并发配置-消息确认机制-listener

    @Bean(name = "simpleQueue")
    public Queue simpleQueue(){
        return new Queue(env.getProperty("simple.mq.queue.name"),true);
    }

    @Bean
    public TopicExchange simpleExchange(){
        return new TopicExchange(env.getProperty("simple.mq.exchange.name"),true,false);
    }

    @Bean
    public Binding simpleBinding(){
        return BindingBuilder.bind(simpleQueue()).to(simpleExchange()).with(env.getProperty("simple.mq.routing.key.name"));
    }

    @Autowired
    private SimpleListener simpleListener;


    @Bean(name = "simpleContainer")
    public SimpleMessageListenerContainer simpleContainer(@Qualifier("simpleQueue") Queue simpleQueue){
        SimpleMessageListenerContainer container=new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setMessageConverter(new Jackson2JsonMessageConverter());

        //TODO：并发配置
        container.setConcurrentConsumers(env.getProperty("spring.rabbitmq.listener.concurrency",Integer.class));
        container.setMaxConcurrentConsumers(env.getProperty("spring.rabbitmq.listener.max-concurrency",Integer.class));
        container.setPrefetchCount(env.getProperty("spring.rabbitmq.listener.prefetch",Integer.class));

        //TODO：消息确认-确认机制种类
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        container.setQueues(simpleQueue);
        container.setMessageListener(simpleListener);

        return container;
    }



    //TODO：用户商城抢单实战

    @Bean(name = "userOrderQueue")
    public Queue userOrderQueue(){
        return new Queue(env.getProperty("user.order.queue.name"),true);
    }

    @Bean
    public TopicExchange userOrderExchange(){
        return new TopicExchange(env.getProperty("user.order.exchange.name"),true,false);
    }

    @Bean
    public Binding userOrderBinding(){
        return BindingBuilder.bind(userOrderQueue()).to(userOrderExchange()).with(env.getProperty("user.order.routing.key.name"));
    }

    @Autowired
    private UserOrderListener userOrderListener;

    @Bean
    public SimpleMessageListenerContainer listenerContainerUserOrder(@Qualifier("userOrderQueue") Queue userOrderQueue){
        SimpleMessageListenerContainer container=new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setMessageConverter(new Jackson2JsonMessageConverter());

        //TODO：并发配置
        container.setConcurrentConsumers(env.getProperty("spring.rabbitmq.listener.concurrency",Integer.class));
        container.setMaxConcurrentConsumers(env.getProperty("spring.rabbitmq.listener.max-concurrency",Integer.class));
        container.setPrefetchCount(env.getProperty("spring.rabbitmq.listener.prefetch",Integer.class));

        //TODO:消息确认机制
        container.setQueues(userOrderQueue);
        container.setMessageListener(userOrderListener);
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);

        return container;
    }




    //TODO：系统日志消息模型

    @Bean(name = "logSystemQueue")
    public Queue logSystemQueue(){
        return new Queue(env.getProperty("log.system.queue.name"),true);
    }

    @Bean
    public TopicExchange logSystemExchange(){
        return new TopicExchange(env.getProperty("log.system.exchange.name"),true,false);
    }

    @Bean
    public Binding logSystemBinding(){
        return BindingBuilder.bind(logSystemQueue()).to(logSystemExchange()).with(env.getProperty("log.system.routing.key.name"));
    }


    //TODO：用户操作日志消息模型(创建队列Queue、Direct交换器Exchange、路由键RoutingKey，绑定 )

    @Bean(name = "logUserQueue")
    public Queue logUserQueue(){
        return new Queue(env.getProperty("log.user.queue.name"),true);
    }

    @Bean
    public DirectExchange logUserExchange(){
        return new DirectExchange(env.getProperty("log.user.exchange.name"),true,false);
    }

    @Bean
    public Binding logUserBinding(){
        return BindingBuilder.bind(logUserQueue()).to(logUserExchange()).with(env.getProperty("log.user.routing.key.name"));
    }


    //TODO：发送邮件消息模型
    @Bean
    public Queue mailQueue(){
        return new Queue(env.getProperty("mail.queue.name"),true);
    }

    @Bean
    public DirectExchange mailExchange(){
        return new DirectExchange(env.getProperty("mail.exchange.name"),true,false);
    }

    @Bean
    public Binding mailBinding(){
       return BindingBuilder.bind(mailQueue()).to(mailExchange()).with(env.getProperty("mail.routing.key.name"));
    }


    //TODO：死信队列消息模型

    //创建死信队列
    @Bean
    public Queue simpleDeadQueue(){
        Map<String, Object> args=new HashMap();

        args.put("x-dead-letter-exchange", env.getProperty("simple.dead.exchange.name"));
        args.put("x-dead-letter-routing-key", env.getProperty("simple.dead.routing.key.name"));
        args.put("x-message-ttl", 10000);

        return new Queue(env.getProperty("simple.dead.queue.name"),true,false,false,args);
    }

    //绑定死信队列-面向生产端
    @Bean
    public TopicExchange simpleDeadExchange(){
        return new TopicExchange(env.getProperty("simple.produce.exchange.name"),true,false);
    }

    @Bean
    public Binding simpleDeadBinding(){
        return BindingBuilder.bind(simpleDeadQueue()).to(simpleDeadExchange()).with(env.getProperty("simple.produce.routing.key.name"));
    }


    //创建并绑定实际监听消费队列
    @Bean
    public Queue simpleDeadRealQueue(){
       return new Queue(env.getProperty("simple.dead.real.queue.name"),true);
    }

    @Bean
    public TopicExchange simpleDeadRealExchange(){
        return new TopicExchange(env.getProperty("simple.dead.exchange.name"),true,false);
    }

    @Bean
    public Binding simpleDeadRealBinding(){
        return BindingBuilder.bind(simpleDeadRealQueue()).to(simpleDeadRealExchange()).with(env.getProperty("simple.dead.routing.key.name"));
    }




    //TODO：用户下单支付超时死信队列模型

    @Bean
    public Queue userOrderDeadQueue(){
        Map<String, Object> args=new HashMap();
        args.put("x-dead-letter-exchange",env.getProperty("user.order.dead.exchange.name"));
        args.put("x-dead-letter-routing-key",env.getProperty("user.order.dead.routing.key.name"));
        args.put("x-message-ttl",10000);

        return new Queue(env.getProperty("user.order.dead.queue.name"),true,false,false,args);
    }

    //绑定死信队列-面向生产端
    @Bean
    public TopicExchange userOrderDeadExchange(){
        return new TopicExchange(env.getProperty("user.order.dead.produce.exchange.name"),true,false);
    }

    @Bean
    public Binding userOrderDeadBinding(){
        return BindingBuilder.bind(userOrderDeadQueue()).to(userOrderDeadExchange()).with(env.getProperty("user.order.dead.produce.routing.key.name"));
    }

    //创建并绑定实际监听消费队列-面向消费端
    @Bean
    public Queue userOrderDeadRealQueue(){
        return new Queue(env.getProperty("user.order.dead.real.queue.name"),true);
    }


    @Bean
    public TopicExchange userOrderDeadRealExchange(){
        return new TopicExchange(env.getProperty("user.order.dead.exchange.name"));
    }

    @Bean
    public Binding userOrderDeadRealBinding(){
        return BindingBuilder.bind(userOrderDeadRealQueue()).to(userOrderDeadRealExchange()).with(env.getProperty("user.order.dead.routing.key.name"));
    }


    //TODO：死信队列动态设置TTL消息模型

    @Bean
    public Queue dynamicDeadQueue(){
        Map<String, Object> args=new HashMap();
        args.put("x-dead-letter-exchange",env.getProperty("dynamic.dead.exchange.name"));
        args.put("x-dead-letter-routing-key",env.getProperty("dynamic.dead.routing.key.name"));

        return new Queue(env.getProperty("dynamic.dead.queue.name"),true,false,false,args);
    }

    @Bean
    public TopicExchange dynamicDeadExchange(){
        return new TopicExchange(env.getProperty("dynamic.dead.produce.exchange.name"),true,false);
    }

    @Bean
    public Binding dynamicDeadBinding(){
        return BindingBuilder.bind(dynamicDeadQueue()).to(dynamicDeadExchange()).with(env.getProperty("dynamic.dead.produce.routing.key.name"));
    }


    @Bean
    public Queue dynamicDeadRealQueue(){
        return new Queue(env.getProperty("dynamic.dead.real.queue.name"),true);
    }


    @Bean
    public TopicExchange dynamicDeadRealExchange(){
        return new TopicExchange(env.getProperty("dynamic.dead.exchange.name"));
    }

    @Bean
    public Binding dynamicDeadRealBinding(){
        return BindingBuilder.bind(dynamicDeadRealQueue()).to(dynamicDeadRealExchange()).with(env.getProperty("dynamic.dead.routing.key.name"));
    }


}



























































