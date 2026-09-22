package com.isalvama.fresh_keep.modules.admin.application.service;

import com.isalvama.fresh_keep.modules.account.domain.value_object.AccountId;
import com.isalvama.fresh_keep.modules.admin.application.command.RegisterAdminCommand;
import com.isalvama.fresh_keep.modules.admin.application.port.in.RegisterAdminUseCase;
import com.isalvama.fresh_keep.modules.admin.application.port.out.AdminRepositoryPort;
import com.isalvama.fresh_keep.modules.admin.domain.model.Admin;
import com.isalvama.fresh_keep.modules.user.domain.exception.UserAlreadyExistsException;
import com.isalvama.fresh_keep.shared.domain.value_object.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegisterAdminService implements RegisterAdminUseCase {
    private final AdminRepositoryPort adminRepositoryPort;

    @Override
    public void execute(RegisterAdminCommand command) {

        if (adminRepositoryPort.findByAccountId(UUID.fromString(command.accountId())).isPresent()){
            log.error("Admin already exists for account with id {}, skipping.", command.accountId());
            return;
        }

        if (adminRepositoryPort.findByEmail(command.email()).isPresent()){
            throw new UserAlreadyExistsException("Admin with email " + command.email() + " already exists.");
        }

        Admin admin = Admin.create(AccountId.from(command.accountId()), Email.of(command.email()));

        adminRepositoryPort.save(admin);
    }
}
