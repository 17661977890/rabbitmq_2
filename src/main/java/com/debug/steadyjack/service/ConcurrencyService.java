package com.debug.steadyjack.service;

import com.debug.steadyjack.entity.Product;
import com.debug.steadyjack.entity.ProductRobbingRecord;
import com.debug.steadyjack.mapper.ProductMapper;
import com.debug.steadyjack.mapper.ProductRobbingRecordMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Administrator on 2018/8/25.
 */
@Service
public class ConcurrencyService {

    private static final Logger log= LoggerFactory.getLogger(ConcurrencyService.class);

    private static final String ProductNo="product_10010";

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ProductRobbingRecordMapper productRobbingRecordMapper;

    /**
     * 处理抢单：有库存 扣减库存  异步发送通知用户抢单成功
     * @param mobile
     */
    public void manageRobbing(String mobile){
        /*try {
            Product product=productMapper.selectByProductNo(ProductNo);
            if (product!=null && product.getTotal()>0){
                log.info("当前手机号：{} 恭喜您抢到单了!",mobile);
                productMapper.updateTotal(product);
            }else{
                log.error("当前手机号：{} 抢不到单!",mobile);

            }
        }catch (Exception e){
            log.error("处理抢单发生异常：mobile={} ",mobile);
        }*/ //--v1.0


        //+v2.0
        try {
            //查有没有库存
            Product product=productMapper.selectByProductNo(ProductNo);
            if (product!=null && product.getTotal()>0){
                //有库存，扣减库存-1
                int result=productMapper.updateTotal(product);
                if (result>0) {
                    ProductRobbingRecord entity=new ProductRobbingRecord();
                    entity.setMobile(mobile);
                    entity.setProductId(product.getId());
                    //抢单成功+1
                    productRobbingRecordMapper.insertSelective(entity);
                    //todo 异步通知用户抢单成功（发送短信等）
                }
            }
        }catch (Exception e){
            log.error("处理抢单发生异常：mobile={} ",mobile);
        }
    }
}

















