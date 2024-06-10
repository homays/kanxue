package com.arrebol.kanxue.auth.domain.dataobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RolePermissionDO {
    private Long id;

    private Long roleId;

    private Long permissionId;

    private LocalDate createTime;

    private LocalDate updateTime;

    private Boolean isDeleted;

}