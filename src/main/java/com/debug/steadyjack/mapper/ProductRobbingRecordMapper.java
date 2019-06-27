package com.debug.steadyjack.mapper;


import com.debug.steadyjack.entity.ProductRobbingRecord;

public interface ProductRobbingRecordMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(ProductRobbingRecord record);

    int insertSelective(ProductRobbingRecord record);

    ProductRobbingRecord selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(ProductRobbingRecord record);

    int updateByPrimaryKey(ProductRobbingRecord record);
}