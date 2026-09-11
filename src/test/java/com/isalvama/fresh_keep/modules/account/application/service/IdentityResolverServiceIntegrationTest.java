package com.isalvama.fresh_keep.modules.account.application.service;

import com.isalvama.fresh_keep.modules.account.application.port.out.JwtTokenGeneratorPort;
import com.isalvama.fresh_keep.modules.account.application.port.out.PasswordHasherPort;
import com.isalvama.fresh_keep.modules.account.application.port.out.dto.ResolvedEntities;
import com.isalvama.fresh_keep.modules.account.domain.model.Account;
import com.isalvama.fresh_keep.modules.account.domain.value_object.AccountId;
import com.isalvama.fresh_keep.modules.account.infrastructure.event.UserAccountEventPublisherAdapter;
import com.isalvama.fresh_keep.modules.account.infrastructure.security.AuthenticationAdapter;
import com.isalvama.fresh_keep.modules.account.infrastructure.security.exception.AdminProvisioningPendingException;
import com.isalvama.fresh_keep.modules.account.infrastructure.security.token.JwtAuthenticationFilter;
import com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.JpaSpaceRepositoryAdapter;
import com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.mapper.SpaceMapper;
import com.isalvama.fresh_keep.modules.user.infrastructure.user_id_look_up.exception.UserProvisioningPendingException;
import com.isalvama.fresh_keep.shared.domain.Role;
import com.isalvama.fresh_keep.shared.domain.value_object.Email;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@TestPropertySource(properties = {
        "jwt.secret=a-very-long-secret-to-avoid-any-key-size-related-errors-123456",
        "JWT_SECRET=a-very-long-secret-to-avoid-any-key-size-related-errors-123456",
        "application.security.jwt.secret-key=a-very-long-secret-to-avoid-any-key-size-related-errors-123456",
        "application.security.jwt.expiration=3600000",
        "application.security.jwt.refresh-token.expiration=86400000",
        "cloudinary.cloud_name=test-cloud",
        "cloudinary.api_key=test-api-key",
        "cloudinary.api_secret=test-api-secret"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class IdentityResolverServiceIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"));

    @Autowired
    private IdentityResolverService identityResolverService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private PasswordHasherPort passwordHasherPort;

    @MockitoBean
    private JwtTokenGeneratorPort jwtTokenGeneratorPort;

    @MockitoBean
    private UserAccountEventPublisherAdapter accountEventPublisherAdapter;

    @MockitoBean
    private AuthenticationAdapter authenticationAdapter;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JpaSpaceRepositoryAdapter jpaSpaceRepositoryAdapter;

    @MockitoBean
    private SpaceMapper spaceMapper;

    @Test
    void resolveFor_returnsUserIdWhenUserRoleAccountHasMatchingPersistedUser() {
        AccountId accountId = AccountId.create();
        UUID userId = UUID.randomUUID();
        String email = "resolve-user@test.com";

        jdbcTemplate.update(
                "INSERT INTO accounts (id, email, password_hash) VALUES (?, ?, ?)",
                accountId.value(), email, "hashed"
        );
        jdbcTemplate.update(
                "INSERT INTO users (id, account_id, email, username) VALUES (?, ?, ?, ?)",
                userId, accountId.value(), email, "resolveuser"
        );

        Account account = Account.reconstitute(accountId, Email.of(email), "hashed", Set.of(Role.USER));

        ResolvedEntities resolved = identityResolverService.resolveFor(account);

        assertEquals(userId.toString(), resolved.userId());
        assertNull(resolved.adminId());
    }

    @Test
    void resolveFor_throwsUserProvisioningPendingExceptionWhenNoMatchingUserExistsYet() {
        AccountId accountId = AccountId.create();
        Account account = Account.reconstitute(accountId, Email.of("resolve-missing-user@test.com"), "hashed", Set.of(Role.USER));

        assertThrows(UserProvisioningPendingException.class, () -> identityResolverService.resolveFor(account));
    }

    @Test
    void resolveFor_throwsAdminProvisioningPendingExceptionForAdminRoleAccount() {
        AccountId accountId = AccountId.create();
        Account account = Account.reconstitute(accountId, Email.of("resolve-admin@test.com"), "hashed", Set.of(Role.ADMIN));

        assertThrows(AdminProvisioningPendingException.class, () -> identityResolverService.resolveFor(account));
    }
}
