package com.isalvama.fresh_keep.modules.user.application.service;

import com.isalvama.fresh_keep.modules.account.domain.value_object.AccountId;
import com.isalvama.fresh_keep.modules.user.application.command.RegisterUserCommand;
import com.isalvama.fresh_keep.modules.user.application.port.in.RegisterUserUseCase;
import com.isalvama.fresh_keep.modules.user.application.port.out.UserRepositoryPort;
import com.isalvama.fresh_keep.modules.user.domain.exception.UserAlreadyExistsException;
import com.isalvama.fresh_keep.modules.user.domain.model.User;
import com.isalvama.fresh_keep.shared.domain.value_object.Email;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterUserService implements RegisterUserUseCase {
    private final UserRepositoryPort userRepositoryPort;


    @Override
    public void execute(RegisterUserCommand command) {

        if (userRepositoryPort.findByAccountId(UUID.fromString(command.accountId())).isPresent()){
            log.error("User already exists for account with id {}, skipping.", command.accountId());
            return;
        }

        if (userRepositoryPort.findByEmail(command.email()).isPresent()){
            throw new UserAlreadyExistsException("User with email " + command.email() + " already exists.");
        }

        User user = User.create(AccountId.from(command.accountId()), Email.of(command.email()));

        userRepositoryPort.save(user);
    }
}
