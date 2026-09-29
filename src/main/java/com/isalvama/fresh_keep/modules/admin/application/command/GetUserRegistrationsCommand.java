package com.isalvama.fresh_keep.modules.admin.application.command;

import java.time.LocalDate;

public record GetUserRegistrationsCommand(
        LocalDate from,
        LocalDate to
) {
}
