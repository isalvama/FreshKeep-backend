package com.isalvama.fresh_keep.modules.account.domain.model;
import com.isalvama.fresh_keep.modules.account.domain.exception.InvalidAccountException;
import com.isalvama.fresh_keep.modules.account.domain.value_object.AccountId;
import com.isalvama.fresh_keep.shared.domain.value_object.Email;
import com.isalvama.fresh_keep.shared.domain.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountTest {

    private final Email VALID_EMAIL = new Email("test@example.com");
    private final String VALID_PASSWORD = "hashed_password";

    @Test
    @DisplayName("Should create a valid user account with USER role")
    void shouldCreateUserAccount() {
        Account account = Account.createUser(VALID_EMAIL, VALID_PASSWORD);

        assertThat(account.getId()).isNotNull();
        assertThat(account.getEmail()).isEqualTo(VALID_EMAIL);
        assertThat(account.getPasswordHash()).isEqualTo(VALID_PASSWORD);
        assertThat(account.getRoles()).containsExactly(Role.USER);
    }

    @Test
    @DisplayName("Should create a valid admin account with ADMIN role")
    void shouldCreateAdminAccount() {
        Account account = Account.createAdmin(VALID_EMAIL, VALID_PASSWORD);

        assertThat(account.getId()).isNotNull();
        assertThat(account.getRoles()).containsExactly(Role.ADMIN);
    }

    @Test
    @DisplayName("Should reconstitute an account correctly")
    void shouldReconstituteAccount() {
        AccountId id = AccountId.create();
        Set<Role> roles = Set.of(Role.USER, Role.ADMIN);

        Account account = Account.reconstitute(id, VALID_EMAIL, VALID_PASSWORD, roles);

        assertThat(account.getId()).isEqualTo(id);
        assertThat(account.getEmail()).isEqualTo(VALID_EMAIL);
        assertThat(account.getRoles()).isEqualTo(roles);
    }

    @Test
    @DisplayName("Should throw exception when roles set is empty")
    void shouldThrowExceptionWhenRolesIsEmpty() {
        AccountId id = AccountId.create();
        Set<Role> emptyRoles = Set.of();

        assertThatThrownBy(() -> Account.reconstitute(id, VALID_EMAIL, VALID_PASSWORD, emptyRoles))
                .isInstanceOf(InvalidAccountException.class)
                .hasMessageContaining("the set of roles cannot be empty");
    }

    @ParameterizedTest
    @NullSource
    @DisplayName("Should throw exception when email is null")
    void shouldThrowExceptionWhenEmailIsNull(Email nullEmail) {
        assertThatThrownBy(() -> Account.createUser(nullEmail, VALID_PASSWORD))
                .isInstanceOf(InvalidAccountException.class)
                .hasMessageContaining("email cannot be null");
    }

    @ParameterizedTest
    @NullSource
    @DisplayName("Should throw exception when password hash is null")
    void shouldThrowExceptionWhenPasswordIsNull(String nullPassword) {
        assertThatThrownBy(() -> Account.createUser(VALID_EMAIL, nullPassword))
                .isInstanceOf(InvalidAccountException.class)
                .hasMessageContaining("passwordHash cannot be null");
    }

    @Test
    @DisplayName("Should throw exception when ID is null during reconstitution")
    void shouldThrowExceptionWhenIdIsNull() {
        assertThatThrownBy(() -> Account.reconstitute(null, VALID_EMAIL, VALID_PASSWORD, Set.of(Role.USER)))
                .isInstanceOf(InvalidAccountException.class)
                .hasMessageContaining("id cannot be null");
    }
}