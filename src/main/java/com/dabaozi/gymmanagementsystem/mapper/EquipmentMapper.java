package com.dabaozi.gymmanagementsystem.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dabaozi.gymmanagementsystem.pojo.dto.EquipmentPageReqDTO;
import com.dabaozi.gymmanagementsystem.pojo.entity.Equipment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EquipmentMapper extends BaseMapper<Equipment> {

    IPage<Equipment> pageEquipment(EquipmentPageReqDTO equipmentPageReqDTO);
}
