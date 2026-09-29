package com.isalvama.fresh_keep.modules.admin.application.service;

import com.isalvama.fresh_keep.modules.admin.application.command.GetRegisteredUsersCommand;
import com.isalvama.fresh_keep.modules.admin.application.command.GetUserDetailsCommand;
import com.isalvama.fresh_keep.modules.admin.application.command.GetUserRegistrationsCommand;
import com.isalvama.fresh_keep.modules.admin.application.port.out.UsersQueryPort;
import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.*;
import com.isalvama.fresh_keep.modules.admin.domain.exception.NonExistentUserException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetUsersServiceTest {
    @Mock
    private UsersQueryPort queryPort;
    @InjectMocks
    private GetUsersService service;

    @Test
    void getUserRegistrationMetrics_mapsMetrics() {
        GetUserRegistrationsCommand command = new GetUserRegistrationsCommand(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 10));
        when(queryPort.getUserRegistrations(command.from(), command.to()))
                .thenReturn(List.of(new DailyMetricDto(command.from(), 3)));

        assertEquals(3, service.getUserRegistrationMetrics(command).getFirst().count());
    }

    @Test
    void getUsers_appliesPaginationAndBuildsPageResult() {
        GetRegisteredUsersCommand command = new GetRegisteredUsersCommand(
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 10), 2, 10);
        RegisteredUserDto user = new RegisteredUserDto(UUID.randomUUID(), "user@email.com", "user",
                Instant.parse("2026-01-01T10:00:00Z"), null);
        when(queryPort.findRegisteredUsers(command.from(), command.to(), 10, 10))
                .thenReturn(new RegisteredUsersPageDto(List.of(user), 21));

        var result = service.getUsers(command);

        assertEquals(2, result.page());
        assertEquals(3, result.totalPages());
        assertEquals(user, result.content().getFirst());
    }

    @Test
    void getUser_mapsDetailsAndNestedData() {
        UUID userId = UUID.randomUUID();
        UserDetailsDto user = new UserDetailsDto(userId, "user@email.com", "user", Instant.now(), null,
                List.of("USER"), List.of(new UserDetailsDto.UserSpaceDto(UUID.randomUUID(), "Kitchen")),
                List.of(new UserDetailsDto.UserReceiptDto(UUID.randomUUID(), Instant.now(), LocalDate.now(), "Store")));
        when(queryPort.findByUserId(userId)).thenReturn(Optional.of(user));

        var result = service.getUser(new GetUserDetailsCommand(userId));

        assertEquals(userId, result.id());
        assertEquals(1, result.spaces().size());
        assertEquals(1, result.receipts().size());
    }

    @Test
    void getUser_throwsWhenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();
        when(queryPort.findByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(NonExistentUserException.class, () -> service.getUser(new GetUserDetailsCommand(userId)));
    }
}
