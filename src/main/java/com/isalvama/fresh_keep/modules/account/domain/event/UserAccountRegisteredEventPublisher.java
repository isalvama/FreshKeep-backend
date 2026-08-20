package com.isalvama.fresh_keep.modules.account.domain.event;

public interface UserAccountRegisteredEventPublisher {

    void publish(UserAccountRegisteredEvent accountRegisteredEvent);
}
