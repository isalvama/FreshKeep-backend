package com.isalvama.fresh_keep.modules.user.domain.model;

import com.isalvama.fresh_keep.modules.account.domain.value_object.AccountId;
import com.isalvama.fresh_keep.modules.shopping_receipt.domain.model.value_object.ShoppingReceiptId;
import com.isalvama.fresh_keep.modules.user.domain.model.value_object.UserId;

import java.util.Set;

public class User {
    private final UserId id;
    private final AccountId accountId;
    private String username;
    private Set<ShoppingReceiptId> shoppingIds;

    public User(UserId id, AccountId accountId) {
        this.id = id;
        this.accountId = accountId;
    }
}
