package com.debug.steadyjack.controller;

import com.debug.steadyjack.mapper.OrderRecordMapper;
import com.debug.steadyjack.response.BaseResponse;
import com.debug.steadyjack.response.StatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by steadyjack on 2018/8/20.
 */
@RestController
public class HelloWorldController {

    private static final Logger log= LoggerFactory.getLogger(HelloWorldController.class);

    private static final String Prefix="helloWorld";

    @Autowired
    private OrderRecordMapper orderRecordMapper;

    /**
     * 测试SpringBoot整合是否有问题-hello world
     * @return
     */
    @RequestMapping(value = Prefix+"/rabbitmq",method = RequestMethod.GET)
    public BaseResponse rabbitmq(){
        BaseResponse response=new BaseResponse(StatusCode.Success);
        String str="rabbitmq的第二阶段-spring boot整合rabbitmq";
        response.setData(str);
        return response;
    }

    /**
     * 整合mybatis访问数据列表
     * @return
     */
    @RequestMapping(value = Prefix+"/data/list",method = RequestMethod.GET)
    public BaseResponse dataList(){
        BaseResponse response=new BaseResponse(StatusCode.Success);
        response.setData(orderRecordMapper.selectAll());
        return response;
    }


}
