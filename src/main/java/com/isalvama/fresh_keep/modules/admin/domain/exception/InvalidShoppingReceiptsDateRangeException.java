package com.isalvama.fresh_keep.modules.admin.domain.exception;

import com.isalvama.fresh_keep.shared.domain.exception.DomainException;

public class InvalidShoppingReceiptsDateRangeException extends DomainException {
    public InvalidShoppingReceiptsDateRangeException(String message) {
        super("Invalid shopping receipts date range: " + message);
    }
}
