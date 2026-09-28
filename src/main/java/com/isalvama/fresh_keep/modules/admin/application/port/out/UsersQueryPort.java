package com.isalvama.fresh_keep.modules.admin.application.port.out;

import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.DailyMetricDto;
import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.RegisteredUsersPageDto;
import com.isalvama.fresh_keep.modules.admin.application.port.out.dto.UserDetailsDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsersQueryPort {
    List<DailyMetricDto> getUserRegistrations(LocalDate from, LocalDate to);
    RegisteredUsersPageDto findRegisteredUsers(LocalDate from, LocalDate to, int offset, int limit);
    Optional<UserDetailsDto> findByUserId(UUID userId);
}
