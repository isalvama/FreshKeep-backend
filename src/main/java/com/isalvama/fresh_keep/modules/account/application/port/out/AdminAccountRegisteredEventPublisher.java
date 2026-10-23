package com.isalvama.fresh_keep.modules.account.application.port.out;

import com.isalvama.fresh_keep.modules.account.application.port.out.dto.event.AdminAccountRegisteredEvent;

public interface AdminAccountRegisteredEventPublisher {
    void publish (AdminAccountRegisteredEvent event);
}
