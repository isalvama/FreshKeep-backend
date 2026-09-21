package com.isalvama.fresh_keep.modules.account.infrastructure.event;

import com.isalvama.fresh_keep.modules.account.domain.event.AdminAccountRegisteredEvent;
import com.isalvama.fresh_keep.modules.account.domain.event.AdminAccountRegisteredEventPublisher;
import com.isalvama.fresh_keep.modules.account.domain.event.UserAccountRegisteredEvent;
import com.isalvama.fresh_keep.modules.account.domain.event.UserAccountRegisteredEventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class AccountEventPublisherAdapter implements UserAccountRegisteredEventPublisher, AdminAccountRegisteredEventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;

    public AccountEventPublisherAdapter(ApplicationEventPublisher applicationEventPublisher){
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish (UserAccountRegisteredEvent event) {
        applicationEventPublisher.publishEvent(event);
    }

    @Override
    public void publish(AdminAccountRegisteredEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
