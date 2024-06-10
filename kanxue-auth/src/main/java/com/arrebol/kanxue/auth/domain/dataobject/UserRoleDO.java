package com.arrebol.kanxue.auth.domain.dataobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRoleDO {
    private Long id;

    private Long userId;

    private Long roleId;

    private LocalDate createTime;

    private LocalDate updateTime;

    private Boolean isDeleted;

}