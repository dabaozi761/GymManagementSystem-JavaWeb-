package com.dabaozi.gymmanagementsystem.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dabaozi.gymmanagementsystem.mapper.EquipmentMapper;
import com.dabaozi.gymmanagementsystem.pojo.dto.EquipmentDTO;
import com.dabaozi.gymmanagementsystem.pojo.dto.EquipmentPageReqDTO;
import com.dabaozi.gymmanagementsystem.pojo.dto.EquipmentSaveDTO;
import com.dabaozi.gymmanagementsystem.pojo.entity.Equipment;
import com.dabaozi.gymmanagementsystem.pojo.vo.EquipmentPageRespDTO;
import com.dabaozi.gymmanagementsystem.service.EquipmentService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class EquipmentServiceImpl extends ServiceImpl<EquipmentMapper, Equipment> implements EquipmentService {

    @Override
    public void save(EquipmentSaveDTO equipmentSaveDTO) {
        Equipment equipment = Equipment.builder()
                .equipmentNo(UUID.randomUUID().toString().replace("-", ""))
                .name(equipmentSaveDTO.getName())
                .type(equipmentSaveDTO.getType())
                .remark(equipmentSaveDTO.getRemark())
                .status(equipmentSaveDTO.getStatus())
                .purchaseDate(equipmentSaveDTO.getPurchaseDate())
                .purchasePrice(equipmentSaveDTO.getPurchasePrice())
                .build();
        baseMapper.insert(equipment);

    }

    @Override
    public void deleteWithEquipmentNo(String equipmentNo) {
        LambdaUpdateWrapper<Equipment> updateWrapper = Wrappers.lambdaUpdate(Equipment.class)
                .eq(Equipment::getEquipmentNo, equipmentNo)
                .eq(Equipment::getDelFlag,0);
        baseMapper.delete(updateWrapper);
    }

    @Override
    public void updateEquipment(EquipmentDTO equipmentDTO) {
        LambdaUpdateWrapper<Equipment> updateWrapper = Wrappers.lambdaUpdate(Equipment.class)
                .eq(Equipment::getDelFlag, 0)
                .eq(Equipment::getEquipmentNo, equipmentDTO.getEquipmentNo());
        Equipment equipment = Equipment.builder()
                .name(equipmentDTO.getName())
                .type(equipmentDTO.getType())
                .remark(equipmentDTO.getRemark())
                .status(equipmentDTO.getStatus())
                .purchaseDate(equipmentDTO.getPurchaseDate())
                .purchasePrice(equipmentDTO.getPurchasePrice())
                .build();
        baseMapper.update(equipment, updateWrapper);
    }

    @Override
    public IPage<EquipmentPageRespDTO> pageEquipment(EquipmentPageReqDTO equipmentPageReqDTO) {
        IPage<Equipment> pageResult = baseMapper.pageEquipment(equipmentPageReqDTO);
        return pageResult.convert(each -> BeanUtil.toBean(each, EquipmentPageRespDTO.class));
    }
}
