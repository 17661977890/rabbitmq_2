package com.debug.steadyjack.service;

import com.debug.steadyjack.dto.LogDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Created by Administrator on 2018/8/30.
 */
@Service
public class CommonLogService {

    private static final Logger log= LoggerFactory.getLogger(CommonLogService.class);

    /**
     * 通用的写日志服务逻辑
     */
    public void insertLog(LogDto dto){
        log.info("通用的写日志服务逻辑 数据；{} ",dto);

    }


}