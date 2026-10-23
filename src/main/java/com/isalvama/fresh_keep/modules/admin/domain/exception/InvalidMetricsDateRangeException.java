package com.isalvama.fresh_keep.modules.admin.domain.exception;

import com.isalvama.fresh_keep.shared.domain.exception.DomainException;

public class InvalidMetricsDateRangeException extends DomainException {
    public InvalidMetricsDateRangeException(String message) {
        super("Invalid metrics date range: " + message);
    }
}
