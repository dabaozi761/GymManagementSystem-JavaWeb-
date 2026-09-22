package com.dabaozi.gymmanagementsystem.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dabaozi.gymmanagementsystem.pojo.dto.EquipmentDTO;
import com.dabaozi.gymmanagementsystem.pojo.dto.EquipmentPageReqDTO;
import com.dabaozi.gymmanagementsystem.pojo.dto.EquipmentSaveDTO;
import com.dabaozi.gymmanagementsystem.pojo.entity.Equipment;
import com.dabaozi.gymmanagementsystem.pojo.vo.EquipmentPageRespDTO;


public interface EquipmentService extends IService<Equipment> {
    void save(EquipmentSaveDTO equipmentSaveDTO);

    void deleteWithEquipmentNo(String equipmentNo);

    void updateEquipment(EquipmentDTO equipmentDTO);

    IPage<EquipmentPageRespDTO> pageEquipment(EquipmentPageReqDTO equipmentPageReqDTO);
}
