package com.isalvama.fresh_keep.modules.admin.application.port.out.dto;

import java.util.List;

public record RegisteredUsersPageDto(
        List<RegisteredUserDto> content,
        long totalElements
) {
}
