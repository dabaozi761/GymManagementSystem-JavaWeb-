package com.dabaozi.gymmanagementsystem.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberUpdateDTO {

    private Long id;
    private Long userId;
    private String name;
    private Integer gender;
    private String phone;
    private LocalDate birthday;
    private LocalDate joinDate;
    private Integer status;
    private String remark;
}
