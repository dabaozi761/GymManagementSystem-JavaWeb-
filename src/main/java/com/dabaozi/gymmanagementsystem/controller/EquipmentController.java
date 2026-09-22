package com.dabaozi.gymmanagementsystem.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dabaozi.gymmanagementsystem.common.convention.result.Result;
import com.dabaozi.gymmanagementsystem.common.convention.result.Results;
import com.dabaozi.gymmanagementsystem.pojo.dto.EquipmentDTO;
import com.dabaozi.gymmanagementsystem.pojo.dto.EquipmentPageReqDTO;
import com.dabaozi.gymmanagementsystem.pojo.dto.EquipmentSaveDTO;
import com.dabaozi.gymmanagementsystem.pojo.vo.EquipmentPageRespDTO;
import com.dabaozi.gymmanagementsystem.service.EquipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;

    @PostMapping("api/gym-management-system/equipment/v1/save")
    public Result<Void> save(@RequestBody EquipmentSaveDTO equipmentSaveDTO){
        equipmentService.save(equipmentSaveDTO);
        return Results.success();
    }

    @DeleteMapping("api/gym-management-system/equipment/v1/delete")
    public Result<Void>deleteWithEquipmentNo(@RequestParam("equipmentNo") String equipmentNo){
        equipmentService.deleteWithEquipmentNo(equipmentNo);
        return Results.success();
    }

    @PostMapping("api/gym-management-system/equipment/v1/update")
    public Result<Void>update(@RequestBody EquipmentDTO equipmentDTO){
        equipmentService.updateEquipment(equipmentDTO);
        return Results.success();
    }

    @PostMapping("api/gym-management-system/equipment/v1/page")
    public Result<IPage<EquipmentPageRespDTO>>page(@RequestBody EquipmentPageReqDTO equipmentPageReqDTO){
        return Results.success(equipmentService.pageEquipment(equipmentPageReqDTO));
    }
}
