package com.dabaozi.gymmanagementsystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dabaozi.gymmanagementsystem.pojo.dto.EquipmentDTO;
import com.dabaozi.gymmanagementsystem.pojo.dto.EquipmentSaveDTO;
import com.dabaozi.gymmanagementsystem.pojo.entity.Equipment;
import org.springframework.stereotype.Service;


public interface EquipmentService extends IService<Equipment> {
    void save(EquipmentSaveDTO equipmentSaveDTO);

    void deleteWithEquipmentNo(String equipmentNo);

    void updateEquipment(EquipmentDTO equipmentDTO);
}
