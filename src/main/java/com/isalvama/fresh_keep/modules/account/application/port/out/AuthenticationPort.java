package com.isalvama.fresh_keep.modules.account.application.port.out;

import com.isalvama.fresh_keep.modules.account.domain.model.Account;

public interface AuthenticationPort {
    Account authenticate (String email, String rawPassword);
}
