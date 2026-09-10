package com.isalvama.fresh_keep.modules.shopping_receipt.application.service;

import com.isalvama.fresh_keep.modules.shopping_receipt.domain.exception.InvalidLanguageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LanguageResolver {
    String resolve(String value){
        Language language = getLanguage(value);
        return language.getDisplayName();
    }

    private Language getLanguage (String value){
        try {
            return Language.getValueOf(value);
        } catch (InvalidLanguageException e) {
            log.warn("Invalid or unsupported output language {}. InvalidLanguageException thrown in LanguageResolver.resolve {}. Falling back to default (ENGLISH)", value, e.getMessage());
            return Language.getDefaultLanguage();
        }
    }
}
