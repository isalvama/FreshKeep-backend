package com.isalvama.fresh_keep.modules.account.application.port.out;

import com.isalvama.fresh_keep.modules.account.application.port.out.dto.event.UserAccountRegisteredEvent;

public interface UserAccountRegisteredEventPublisher {
    void publish(UserAccountRegisteredEvent event);
}
