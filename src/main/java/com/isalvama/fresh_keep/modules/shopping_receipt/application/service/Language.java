package com.isalvama.fresh_keep.modules.shopping_receipt.application.service;

import com.isalvama.fresh_keep.modules.shopping_receipt.domain.exception.InvalidLanguageException;
import lombok.Getter;
import lombok.Setter;

@Getter
public enum Language {
    EN("English"),
    ES("Spanish"),
    CA("Catalan"),
    FR("French"),
    DE("German"),
    PT("Portuguese"),
    IT("Italian");

    private String displayName;

    Language (String displayName){
        this.displayName = displayName;
    }

    public static Language getValueOf (String value){
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidLanguageException(value + " does not match any of the available supported Language constant names. " + e);
        }
    }

    public static Language getDefaultLanguage(){
        return EN;
    }
}

