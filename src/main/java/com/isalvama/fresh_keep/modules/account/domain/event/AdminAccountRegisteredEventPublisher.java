package com.isalvama.fresh_keep.modules.account.domain.event;

public interface AdminAccountRegisteredEventPublisher {
    void publish (AdminAccountRegisteredEvent event);
}
