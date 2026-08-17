package com.isalvama.fresh_keep.modules.user.infrastructure.persistence.jpa.mapper;

import com.isalvama.fresh_keep.modules.account.domain.value_object.AccountId;
import com.isalvama.fresh_keep.modules.user.domain.model.User;
import com.isalvama.fresh_keep.modules.user.domain.model.value_object.UserId;
import com.isalvama.fresh_keep.modules.user.domain.model.value_object.UserName;
import com.isalvama.fresh_keep.modules.user.infrastructure.persistence.jpa.entity.JpaUserEntity;
import com.isalvama.fresh_keep.shared.domain.value_object.Email;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public User toDomain (JpaUserEntity jpaUserEntity) {
        return User.reconstitute(
                UserId.of(jpaUserEntity.getId()),
                AccountId.of(jpaUserEntity.getAccountId()),
                Email.of(jpaUserEntity.getEmail()),
                UserName.of(jpaUserEntity.getUserName())
        );
    }

    public JpaUserEntity toEntity (User user){
        return JpaUserEntity.builder()
                .id(user.getId().value())
                .accountId(user.getAccountId().value())
                .email(user.getEmail().value())
                .userName(user.getUserName().toString())
                .build();

    }
}
