package com.isalvama.fresh_keep.modules.admin.application.service;

import com.isalvama.fresh_keep.modules.admin.application.command.GetRegisteredUsersCommand;
import com.isalvama.fresh_keep.modules.admin.application.command.GetUserDetailsCommand;
import com.isalvama.fresh_keep.modules.admin.application.command.GetUserRegistrationsCommand;
import com.isalvama.fresh_keep.modules.admin.application.port.in.GetUsersUseCase;
import com.isalvama.fresh_keep.modules.admin.application.port.in.result.PageResult;
import com.isalvama.fresh_keep.modules.admin.application.port.in.result.UserDetailsResult;
import com.isalvama.fresh_keep.modules.admin.application.port.in.result.UserRegistrationMetricResult;
import com.isalvama.fresh_keep.modules.admin.application.port.out.UsersQueryPort;
import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.RegisteredUserDto;
import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.RegisteredUsersPageDto;
import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.UserDetailsDto;
import com.isalvama.fresh_keep.modules.admin.domain.exception.NonExistentUserException;
import com.isalvama.fresh_keep.modules.admin.domain.value_object.Pagination;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetUsersService implements GetUsersUseCase {
    private final UsersQueryPort usersQueryPort;


    @Override
    @Transactional(readOnly = true)
    public List<UserRegistrationMetricResult> getUserRegistrationMetrics(GetUserRegistrationsCommand command) {
        return usersQueryPort.getUserRegistrations(command.from(), command.to()).stream()
                .map(metric -> new UserRegistrationMetricResult(metric.date(), metric.count()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<RegisteredUserDto> getUsers(GetRegisteredUsersCommand command) {
        Pagination pagination = Pagination.fromPage(command.page(), command.size());
        RegisteredUsersPageDto dto =  usersQueryPort.findRegisteredUsers(
                command.from(), command.to(), pagination.offset(), pagination.limit());

        return PageResult.of(dto.content(), pagination.page(), pagination.limit(), dto.totalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetailsResult getUser(GetUserDetailsCommand command) {
        UserDetailsDto user = usersQueryPort.findByUserId(command.userId())
                .orElseThrow(() -> new NonExistentUserException(
                        "User with id " + command.userId() + " does not exist."));
        return new UserDetailsResult(
                user.id(), user.email(), user.username(), user.registeredAt(), user.lastLoggedAt(), user.roles(),
                user.spaces().stream()
                        .map(space -> new UserDetailsResult.SpaceResult(space.id(), space.name()))
                        .toList(),
                user.receipts().stream()
                        .map(receipt -> new UserDetailsResult.ReceiptResult(
                                receipt.id(), receipt.createdAt(), receipt.purchaseDate(), receipt.storeName()))
                        .toList());
    }
}
