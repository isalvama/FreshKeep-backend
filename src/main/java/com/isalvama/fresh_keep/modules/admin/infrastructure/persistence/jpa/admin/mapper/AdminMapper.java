package com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.admin.mapper;

import com.isalvama.fresh_keep.modules.account.domain.value_object.AccountId;
import com.isalvama.fresh_keep.modules.admin.domain.model.Admin;
import com.isalvama.fresh_keep.modules.admin.domain.value_object.AdminId;
import com.isalvama.fresh_keep.modules.admin.infrastructure.persistence.jpa.admin.entity.JpaAdminEntity;
import com.isalvama.fresh_keep.shared.domain.value_object.Email;
import org.springframework.stereotype.Component;

@Component
public class AdminMapper {
    public Admin toDomain (JpaAdminEntity jpaAdminEntity) {
        return Admin.reconstitute(
                AdminId.of(jpaAdminEntity.getId()),
                AccountId.of(jpaAdminEntity.getAccountId()),
                Email.of(jpaAdminEntity.getEmail())
        );
    }

    public JpaAdminEntity toEntity (Admin admin){
        return JpaAdminEntity.builder()
                .id(admin.getId().value())
                .accountId(admin.getAccountId().value())
                .email(admin.getEmail().value())
                .build();
    }

}
