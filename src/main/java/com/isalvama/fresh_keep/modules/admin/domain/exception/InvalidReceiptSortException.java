package com.isalvama.fresh_keep.modules.admin.domain.exception;

import com.isalvama.fresh_keep.shared.domain.exception.DomainException;

public class InvalidReceiptSortException extends DomainException {
    public InvalidReceiptSortException(String value) {
        super("Invalid receipt sort: " + value + ". Use store_name,ASC|DESC or created_at,ASC|DESC.");
    }
}
