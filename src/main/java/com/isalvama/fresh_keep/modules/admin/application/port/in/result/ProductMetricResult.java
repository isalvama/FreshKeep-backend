package com.isalvama.fresh_keep.modules.admin.application.port.in.result;

import java.time.LocalDate;

public record ProductMetricResult(LocalDate date, long count) {
}
