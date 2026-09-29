package com.isalvama.fresh_keep.modules.admin.application.command;

import java.time.LocalDate;

public record GetRegisteredUsersCommand(
        LocalDate from,
        LocalDate to,
        Integer page,
        Integer size
) {
}
