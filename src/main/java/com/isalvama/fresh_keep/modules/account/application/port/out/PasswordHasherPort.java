package com.isalvama.fresh_keep.modules.account.application.port.out;

public interface PasswordHasherPort {
    public String hash(String rawPassword);
}
