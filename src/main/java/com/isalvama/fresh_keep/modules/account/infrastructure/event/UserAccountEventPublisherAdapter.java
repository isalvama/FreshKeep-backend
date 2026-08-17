package com.isalvama.fresh_keep.modules.account.infrastructure.event;

import com.isalvama.fresh_keep.modules.account.domain.event.UserAccountRegisteredEvent;
import com.isalvama.fresh_keep.modules.account.domain.event.UserAccountRegisteredEventPublisher;
import org.springframework.context.ApplicationEventPublisher;

public class UserAccountEventPublisherAdapter implements UserAccountRegisteredEventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;

    public UserAccountEventPublisherAdapter(ApplicationEventPublisher applicationEventPublisher){
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish (UserAccountRegisteredEvent accountRegisteredEvent) {
        applicationEventPublisher.publishEvent(accountRegisteredEvent);
    }
}
