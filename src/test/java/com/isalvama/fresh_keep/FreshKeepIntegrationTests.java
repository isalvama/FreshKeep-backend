package com.isalvama.fresh_keep;

import com.isalvama.fresh_keep.modules.account.application.port.out.dto.ResolvedEntities;
import com.isalvama.fresh_keep.modules.account.application.service.RegisterUserAccountService;
import com.isalvama.fresh_keep.modules.account.infrastructure.persistence.jpa.AccountSpringDataRepository;
import com.isalvama.fresh_keep.modules.account.infrastructure.web.dto.request.AuthRequest;
import com.isalvama.fresh_keep.modules.account.domain.model.Account;
import com.isalvama.fresh_keep.modules.user.domain.model.User;
import com.isalvama.fresh_keep.modules.user.infrastructure.persistence.jpa.JpaUserRepositoryAdapter;
import com.isalvama.fresh_keep.shared.domain.value_object.Email;
import com.isalvama.fresh_keep.modules.account.infrastructure.persistence.jpa.JpaAccountRepositoryAdapter;
import com.isalvama.fresh_keep.modules.account.infrastructure.security.token.CustomUserPrincipal;
import com.isalvama.fresh_keep.modules.account.infrastructure.security.token.JwtTokenGeneratorAdapter;
import com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.JpaSpaceSpringDataRepository;
import com.isalvama.fresh_keep.modules.space.infrastructure.web.dto.request.CreateSpaceRequest;
import com.isalvama.fresh_keep.modules.space.infrastructure.web.dto.request.StorageSpotRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.ObjectMapper;
import org.testcontainers.utility.DockerImageName;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = FreshKeepApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=update",
        "spring.datasource.hikari.connection-timeout=2000",
        "spring.datasource.hikari.leak-detection-threshold=2000",
        "jwt.secret=55676267733273357638792F423F4528482B4D6251655468576D5A7134743777",
        "jwt.expiration=3600000"
})
@AutoConfigureMockMvc
public class FreshKeepIntegrationTests {
    private static final String API_AUTH = "/api/v1/auth";
    private static final String API_SPACES = "/api/v1/spaces";

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"));

    @Nested
    @DisplayName(API_AUTH)
    class Authentication {

        private static final String EMAIL = "email@email.com";
        private static final String PASSWORD = "Password";

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private JpaAccountRepositoryAdapter jpaAccountRepositoryAdapter;

        @Autowired
        private JpaUserRepositoryAdapter jpaUserRepositoryAdapter;

        @Autowired
        private AccountSpringDataRepository accountSpringDataRepository;

        @Autowired
        private JwtTokenGeneratorAdapter jwtTokenGeneratorAdapter;

        @Autowired
        private RegisterUserAccountService registerUserAccountService;

        @Nested
        @DisplayName("POST " + API_AUTH + "/register")
        class RegisterAccount {

            @Nested
            @DisplayName("POST " + API_AUTH + "/register/user")
            class RegisterUser {

                @BeforeEach
                void setUp() {
                    accountSpringDataRepository.deleteAll();
                }

                @DisplayName("should return 201 with information about the new account generated authenticated login token")
                @Test
                void shouldReturn201AndRegisterUserSuccessfully() throws Exception {
                    AuthRequest registerRequest = new AuthRequest(EMAIL, PASSWORD);

                    ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(API_AUTH + "/register/user")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)));

                    result.andExpect(status().isCreated())
                            .andExpect(header().string("Location", containsString("/api/v1/users/")))
                            .andExpect(jsonPath("$.accountId").exists())
                            .andExpect(jsonPath("$.email").value(EMAIL));

                    String resultAsString = result.andReturn().getResponse().getContentAsString();

                    String resultAccountId = com.jayway.jsonpath.JsonPath.read(resultAsString, "$.accountId");

                    Optional<User> userResult = jpaUserRepositoryAdapter.findByAccountId(UUID.fromString(resultAccountId));
                    assertTrue(userResult.isPresent());
                    User user = userResult.get();
                    assertNotNull(user.getId());
                    assertEquals(resultAccountId, user.getAccountId().toString());
                    assertEquals(EMAIL, user.getEmail().toString());
                    assertNull(user.getUserName());

                }

                @DisplayName("should return 409 Conflict when an account with a matching email already exists")
                @Test
                void shouldReturnBadRequestWhenRequestHasNullParams() throws Exception {

                    Account user = Account.createUser(Email.of(EMAIL), PASSWORD);
                    jpaAccountRepositoryAdapter.save(user);

                    AuthRequest registerRequest = new AuthRequest(EMAIL, PASSWORD);

                    ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(API_AUTH + "/register/user")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)));

                    result.andExpect(status().isConflict())
                            .andExpect(jsonPath("$.title").value("Conflict Error"))
                            .andExpect(jsonPath("$.detail", containsString(EMAIL)))
                            .andExpect(jsonPath("$.detail", containsString("Account Already Exists")))
                            .andExpect(jsonPath("$.detail", containsString("account with the email address")));

                    Optional<User> userResult = jpaUserRepositoryAdapter.findByEmail(EMAIL);
                    assertFalse(userResult.isPresent());
                }

                @DisplayName("should return 400 Bad Request with information about the error (size of password)")
                @Test
                void shouldReturnBadRequestPasswordTooSmall() throws Exception {
                    AuthRequest registerRequest = new AuthRequest(EMAIL, "ps");

                    ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(API_AUTH + "/register/user")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)));

                    result.andExpect(status().isBadRequest())
                            .andExpect(jsonPath("$.title").value("Validation Error In Body Data"))
                            .andExpect(jsonPath("$.errors.password", containsString("size must be between 8 and 20")));
                }

                @DisplayName("should return 400 Bad Request with information about the error (size of password)")
                @Test
                void shouldReturnBadRequestPasswordTooLong() throws Exception {
                    AuthRequest registerRequest = new AuthRequest(EMAIL, "passwordIsTooLongToValid");

                    ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(API_AUTH + "/register/user")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)));

                    result.andExpect(status().isBadRequest())
                            .andExpect(jsonPath("$.title").value("Validation Error In Body Data"))
                            .andExpect(jsonPath("$.errors.password", containsString("size must be between 8 and 20")));
                }

                @DisplayName("should return 400 Bad Request with information about the error (invalid email)")
                @Test
                void shouldReturnBadRequestInvalidEmail() throws Exception {
                    AuthRequest registerRequest = new AuthRequest("email.com", PASSWORD);

                    ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(API_AUTH + "/register/user")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)));

                    result.andExpect(status().isBadRequest())
                            .andExpect(jsonPath("$.title").value("Validation Error In Body Data"))
                            .andExpect(jsonPath("$.errors.email", containsString("must be a well-formed email address")));
                }
            }

            @Nested
            @DisplayName("POST " + API_AUTH + "/register/admin")
            class RegisterAdmin {

                private String adminToken;

                @BeforeEach
                void setUp() {
                    accountSpringDataRepository.deleteAll();
                    Account admin = Account.createAdmin(Email.of(EMAIL), PASSWORD);
                    jpaAccountRepositoryAdapter.save(admin);
                    adminToken = jwtTokenGeneratorAdapter.generateToken(admin, ResolvedEntities.constitute(null, "adminid1234")).token();
                }

                @DisplayName("should return 201 Created with login token and information about the new account")
                @Test
                void shouldRegisterNewAdminSuccessfully() throws Exception {
                    String email = "admin@admin.com";
                    AuthRequest registerRequest = new AuthRequest(email, PASSWORD);

                    ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(API_AUTH + "/register/admin")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)));

                    result.andExpect(status().isCreated())
                            .andExpect(header().string("Location", containsString("/api/v1/admins/")))
                            .andExpect(jsonPath("$.accountId").exists())
                            .andExpect(jsonPath("$.email").value(email));

                }

                @DisplayName("should return 409 Conflict when an account with a matching email already exists")
                @Test
                void shouldReturnBadRequestWhenRequestHasNullParams() throws Exception {
                    AuthRequest registerRequest = new AuthRequest(EMAIL, PASSWORD);

                    ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(API_AUTH + "/register/admin")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)));


                    result.andExpect(status().isConflict())
                            .andExpect(jsonPath("$.title").value("Conflict Error"))
                            .andExpect(jsonPath("$.detail", containsString(EMAIL)))
                            .andExpect(jsonPath("$.detail", containsString("Account Already Exists")))
                            .andExpect(jsonPath("$.detail", containsString("account with the email address")));
                }

                @DisplayName("should return 401 Unauthorized with login token and information about the new account")
                @Test
                void shouldReturnEuthErrorDoesNotHaveAdminRole() throws Exception {
                    String email = "admin@admin.com";
                    AuthRequest registerRequest = new AuthRequest(email, PASSWORD);

                    ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(API_AUTH + "/register/admin")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)));

                    result.andExpect(status().isUnauthorized())
                            .andExpect(jsonPath("$.title").value("Unauthorized"))
                            .andExpect(jsonPath("$.detail").value("Must be authenticated to access this resource"))
                            .andExpect(jsonPath("$.instance").value("/api/v1/auth/register/admin"));
                }

                @DisplayName("should return 400 Bad Request with information about the error (size of password)")
                @Test
                void shouldReturnBadRequestPasswordTooSmall() throws Exception {
                    AuthRequest registerRequest = new AuthRequest(EMAIL, "ps");

                    ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(API_AUTH + "/register/admin")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)));

                    result.andExpect(status().isBadRequest())
                            .andExpect(jsonPath("$.title").value("Validation Error In Body Data"))
                            .andExpect(jsonPath("$.errors.password", containsString("size must be between 8 and 20")));
                }

                @DisplayName("should return 400 Bad Request with information about the error (size of password)")
                @Test
                void shouldReturnBadRequestPasswordTooLong() throws Exception {
                    AuthRequest registerRequest = new AuthRequest(EMAIL, "passwordIsTooLongToValid");

                    ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(API_AUTH + "/register/admin")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)));

                    result.andExpect(status().isBadRequest())
                            .andExpect(jsonPath("$.title").value("Validation Error In Body Data"))
                            .andExpect(jsonPath("$.errors.password", containsString("size must be between 8 and 20")));
                }

                @DisplayName("should return 400 Bad Request with information about the error (invalid email)")
                @Test
                void shouldReturnBadRequestInvalidEmail() throws Exception {
                    AuthRequest registerRequest = new AuthRequest("email.com", PASSWORD);

                    ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(API_AUTH + "/register/admin")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)));

                    result.andExpect(status().isBadRequest())
                            .andExpect(jsonPath("$.title").value("Validation Error In Body Data"))
                            .andExpect(jsonPath("$.errors.email", containsString("must be a well-formed email address")));
                }

                @DisplayName("should return 400 Bad Request with information about the error (null body)")
                @Test
                void shouldReturnBadRequestNullRequest() throws Exception {

                    ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(API_AUTH + "/register/admin")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(null)));

                    result.andExpect(status().isBadRequest())
                            .andExpect(jsonPath("$.title").value("Message Not Readable"))
                            .andExpect(jsonPath("$.detail", containsString("Required request body is missing or malformed")));
                }

                @DisplayName("should return 400 Bad Request with information about the error (request with null parameters)")
                @Test
                void shouldReturnBadRequestRequestWithNullParams() throws Exception {
                    AuthRequest registerRequest = new AuthRequest(null, null);

                    ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(API_AUTH + "/register/admin")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerRequest)));

                    result.andExpect(status().isBadRequest())
                            .andExpect(jsonPath("$.title").value("Validation Error In Body Data"))
                            .andExpect(jsonPath("$.errors.password", containsString("must not be blank")))
                            .andExpect(jsonPath("$.errors.email", containsString("must not be blank")));
                }
            }

            @Nested
            @DisplayName("POST " + API_AUTH + "/login")
            class Login {

                private static final AuthRequest REQUEST = new AuthRequest(EMAIL, PASSWORD);

                @BeforeEach
                void setUp() throws Exception {
                    accountSpringDataRepository.deleteAll();
                    mockMvc.perform(MockMvcRequestBuilders.post(API_AUTH + "/register/user")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(REQUEST)))
                            .andExpect(status().isCreated());
                }

                @DisplayName("should return 200 with generated auth session token and information about the logged in account")
                @Test
                void shouldReturn200WithAccountInfoAndJwtAuthTokenAndLoginUserSuccessfully() throws Exception {
                    ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(API_AUTH + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(REQUEST)));

                    result.andExpect(status().isOk())
                            .andExpect(jsonPath("$.accountId").exists())
                            .andExpect(jsonPath("$.email").value(EMAIL))
                            .andExpect(jsonPath("$.jwtString").exists())
                            .andExpect(jsonPath("$.expiresIn").exists());

                    String resultAsString = result.andReturn().getResponse().getContentAsString();
                    String resultToken = com.jayway.jsonpath.JsonPath.read(resultAsString, "$.jwtString");

                    CustomUserPrincipal userPrincipal = jwtTokenGeneratorAdapter.extractCustomUserPrincipal(resultToken);
                    assertNotNull(userPrincipal.id());
                    assertEquals(EMAIL, userPrincipal.getUsername());
                    assertNull(userPrincipal.passwordHash());
                    assertNotNull(userPrincipal.userId());

                    Collection<? extends GrantedAuthority> authorities = userPrincipal.getAuthorities();

                    assertThat(authorities)
                            .hasSize(1)
                            .extracting(GrantedAuthority::getAuthority)
                            .containsExactly("ROLE_USER")
                            .doesNotContain("ROLE_ADMIN");
                }

                @DisplayName("should return 401 Unauthorized with Invalid email or password error message when the email does not exist")
                @Test
                void shouldReturn401InvalidEmailOrPasswordWhenEmailDoesNotExist() throws Exception {
                    AuthRequest request = new AuthRequest("unregistered@mail.com", "password");

                    ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(API_AUTH + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)));

                    result.andExpect(status().isUnauthorized())
                            .andExpect(jsonPath("$.title", containsString("Unauthorized Error")))
                            .andExpect(jsonPath("$.detail", containsString("Invalid Credentials Error")))
                            .andExpect(jsonPath("$.detail", containsString("Invalid email or password")));
                }

                @DisplayName("should return 401 Unauthorized with Invalid email or password error message when the password is not correct")
                @Test
                void shouldReturn401InvalidEmailOrPasswordWhenPasswordIsIncorrect() throws Exception {
                    AuthRequest request = new AuthRequest(EMAIL, "incorrectPassword");

                    ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(API_AUTH + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)));

                    result.andExpect(status().isUnauthorized())
                            .andExpect(jsonPath("$.title", containsString("Unauthorized Error")))
                            .andExpect(jsonPath("$.detail", containsString("Invalid Credentials Error")))
                            .andExpect(jsonPath("$.detail", containsString("Invalid email or password")));
                }
            }
        }
    }

    @Nested
    @DisplayName(API_SPACES)
    class Spaces {

        private static final String EMAIL = "space-owner@email.com";
        private static final String PASSWORD = "Password1";
        private String userToken;
        private String userId;
        private String space1Name;
        private String storageSpotName;
        private String storageSpotType;

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private AccountSpringDataRepository accountSpringDataRepository;

        @Autowired
        private JpaSpaceSpringDataRepository spaceSpringDataRepository;

        @Autowired
        private JpaAccountRepositoryAdapter jpaAccountRepositoryAdapter;

        @Autowired
        private JwtTokenGeneratorAdapter jwtTokenGeneratorAdapter;

        @BeforeEach
        void setUp() throws Exception {
            space1Name = "Kitchen";
            storageSpotName = "Main Shelf";
            storageSpotType = "SHELF";
            spaceSpringDataRepository.deleteAll();
            accountSpringDataRepository.deleteAll();

            mockMvc.perform(MockMvcRequestBuilders.post(API_AUTH + "/register/user")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new AuthRequest(EMAIL, PASSWORD))))
                    .andExpect(status().isCreated());

            ResultActions loginResult = mockMvc.perform(MockMvcRequestBuilders.post(API_AUTH + "/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new AuthRequest(EMAIL, PASSWORD))));

            String loginResponse = loginResult.andReturn().getResponse().getContentAsString();
            userToken = com.jayway.jsonpath.JsonPath.read(loginResponse, "$.jwtString");
            userId = jwtTokenGeneratorAdapter.extractCustomUserPrincipal(userToken).userId();
        }

        private CreateSpaceRequest validRequest() {
            return new CreateSpaceRequest(
                    space1Name,
                    "🏠",
                    List.of(new StorageSpotRequest(storageSpotName, storageSpotType))
            );
        }

        @Nested
        @DisplayName("POST " + API_SPACES)
        class CreateSpace {

            @DisplayName("should return 201 with the created space when authenticated as USER")
            @Test
            void shouldReturn201AndCreateSpaceSuccessfully() throws Exception {
                ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(API_SPACES)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())));

                result.andExpect(status().isCreated())
                        .andExpect(header().string("Location", containsString(API_SPACES + "/")))
                        .andExpect(jsonPath("$.id").exists())
                        .andExpect(jsonPath("$.spaceName").value(space1Name))
                        .andExpect(jsonPath("$.creatorId").value(userId))
                        .andExpect(jsonPath("$.participantIds", hasItem(userId)))
                        .andExpect(jsonPath("$.storageSpots", hasSize(1)))
                        .andExpect(jsonPath("$.storageSpots[0].storageSpotName").value(storageSpotName))
                        .andExpect(jsonPath("$.storageSpots[0].storageSpotType").value(storageSpotType));

                assertEquals(1, spaceSpringDataRepository.count());
            }

            @DisplayName("should return 401 Unauthorized when no token is provided")
            @Test
            void shouldReturn401WhenNotAuthenticated() throws Exception {
                ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(API_SPACES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())));

                result.andExpect(status().isUnauthorized())
                        .andExpect(jsonPath("$.title").value("Unauthorized"))
                        .andExpect(jsonPath("$.instance").value(API_SPACES));

                assertEquals(0, spaceSpringDataRepository.count());
            }

            @DisplayName("should return 403 Forbidden when the authenticated account does not have the USER role")
            @Test
            void shouldReturn403WhenAccountDoesNotHaveUserRole() throws Exception {
                Account admin = Account.createAdmin(Email.of("admin-only@email.com"), PASSWORD);
                jpaAccountRepositoryAdapter.save(admin);
                String adminToken = jwtTokenGeneratorAdapter
                        .generateToken(admin, ResolvedEntities.constitute(null, "adminid1234"))
                        .token();

                ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(API_SPACES)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())));

                result.andExpect(status().isForbidden())
                        .andExpect(jsonPath("$.title").value("Forbidden"))
                        .andExpect(jsonPath("$.instance").value(API_SPACES));

                assertEquals(0, spaceSpringDataRepository.count());
            }

            @DisplayName("should return 400 Bad Request when spaceName is blank")
            @Test
            void shouldReturn400WhenSpaceNameIsBlank() throws Exception {
                CreateSpaceRequest request = new CreateSpaceRequest(
                        "", "🏠", List.of(new StorageSpotRequest(storageSpotName, storageSpotType)));

                ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(API_SPACES)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)));

                result.andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Validation Error In Body Data"))
                        .andExpect(jsonPath("$.errors.spaceName", containsString("must not be blank")));
            }

            @DisplayName("should return 400 Bad Request when spaceName exceeds max size")
            @Test
            void shouldReturn400WhenSpaceNameExceedsMaxSize() throws Exception {
                CreateSpaceRequest request = new CreateSpaceRequest(
                        "a".repeat(31), "🏠", List.of(new StorageSpotRequest(storageSpotName, storageSpotType)));

                ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(API_SPACES)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)));

                result.andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Validation Error In Body Data"))
                        .andExpect(jsonPath("$.errors.spaceName", containsString("size must be between 0 and 30")));
            }

            @DisplayName("should return 400 Bad Request when emoji is blank")
            @Test
            void shouldReturn400WhenEmojiIsBlank() throws Exception {
                CreateSpaceRequest request = new CreateSpaceRequest(
                        space1Name, " ", List.of(new StorageSpotRequest(storageSpotName, storageSpotType)));

                ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(API_SPACES)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)));

                result.andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Validation Error In Body Data"))
                        .andExpect(jsonPath("$.errors.emoji", containsString("must not be blank")));
            }

            @DisplayName("should return 400 Bad Request when emoji exceeds max size")
            @Test
            void shouldReturn400WhenEmojiExceedsMaxSize() throws Exception {
                CreateSpaceRequest request = new CreateSpaceRequest(
                        space1Name, "123456789", List.of(new StorageSpotRequest(storageSpotName, storageSpotType)));

                ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(API_SPACES)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)));

                result.andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Validation Error In Body Data"))
                        .andExpect(jsonPath("$.errors.emoji", containsString("size must be between 1 and 8")));
            }

            @DisplayName("should return 400 Bad Request when storageSpots is empty")
            @Test
            void shouldReturn400WhenStorageSpotsIsEmpty() throws Exception {
                CreateSpaceRequest request = new CreateSpaceRequest(space1Name, "🏠", List.of());

                ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(API_SPACES)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)));

                result.andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Validation Error In Body Data"))
                        .andExpect(jsonPath("$.errors.storageSpots").exists());
            }

            @DisplayName("should return 400 Bad Request when storageSpot name exceeds max size")
            @Test
            void shouldReturn400WhenStorageSpotsNameExceedsMaxSize() throws Exception {
                CreateSpaceRequest request = new CreateSpaceRequest(space1Name, "🏠", List.of(new StorageSpotRequest("s".repeat(31), storageSpotType)));

                ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(API_SPACES)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)));

                result.andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Validation Error In Body Data"))
                        .andExpect(jsonPath("$.errors['storageSpots[0].name']", containsString("size must be between 0 and 30")));
            }

            @DisplayName("should return 400 Bad Request when storageSpot name is blank")
            @Test
            void shouldReturn400WhenStorageSpotsNameIsBlank() throws Exception {
                CreateSpaceRequest request = new CreateSpaceRequest(space1Name, "🏠", List.of(new StorageSpotRequest(" ", storageSpotType)));

                ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(API_SPACES)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)));

                result.andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Validation Error In Body Data"))
                        .andExpect(jsonPath("$.errors['storageSpots[0].name']", containsString("must not be blank")));
            }

            @DisplayName("should return 400 Bad Request when storageSpot type is blank")
            @Test
            void shouldReturn400WhenStorageSpotsTypeIsBlank() throws Exception {
                CreateSpaceRequest request = new CreateSpaceRequest(space1Name, "🏠", List.of(new StorageSpotRequest(storageSpotName, " ")));

                ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(API_SPACES)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)));

                result.andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Validation Error In Body Data"))
                        .andExpect(jsonPath("$.errors['storageSpots[0].type']", containsString("must not be blank")));
            }

            @DisplayName("should return 400 Bad Request when storageSpot type does not match any StorageSpotTypeRequest enum constant name")
            @Test
            void shouldReturn400WhenStorageSpotsTypeDoesNotMatchAnyStorageSpotTypeRequestName() throws Exception {
                CreateSpaceRequest request = new CreateSpaceRequest(space1Name, "🏠", List.of(new StorageSpotRequest(storageSpotName, "INVALID_TYPE")));

                ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(API_SPACES)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)));

                result.andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Validation Error In Body Data"))
                        .andExpect(jsonPath("$.errors['storageSpots[0].type']", containsString("Invalid value")));
            }

            @DisplayName("should return 400 Bad Request when the emoji fails domain-level validation")
            @Test
            void shouldReturn400WhenEmojiIsInvalidAtDomainLevel() throws Exception {
                CreateSpaceRequest request = new CreateSpaceRequest(
                        space1Name, "abc", List.of(new StorageSpotRequest(storageSpotName, storageSpotType)));

                ResultActions result = mockMvc.perform(MockMvcRequestBuilders.post(API_SPACES)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)));

                result.andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Business Rule Error"))
                        .andExpect(jsonPath("$.detail", containsString("not a valid emoji")));
            }
        }

        @Nested
        class GetSpacesByParticipantId {

            private String user2Token;
            private String user2Id;
            private String space2Name;


            @BeforeEach
            void setUp() throws Exception {
                space1Name = "City House";
                space2Name = "Beach House";
                storageSpotName = "Freezer";
                storageSpotType = "FREEZER";

                    spaceSpringDataRepository.deleteAll();
                    accountSpringDataRepository.deleteAll();

                // Creation of an account and user1
                    mockMvc.perform(MockMvcRequestBuilders.post(API_AUTH + "/register/user")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(new AuthRequest(EMAIL, PASSWORD))))
                            .andExpect(status().isCreated());


                // Creation of another account and user2
                mockMvc.perform(MockMvcRequestBuilders.post(API_AUTH + "/register/user")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new AuthRequest("emailuser2@mail.com", "password123"))))
                        .andExpect(status().isCreated());


                // Login of the new users

                ResultActions loginResult1 = mockMvc.perform(MockMvcRequestBuilders.post(API_AUTH + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AuthRequest(EMAIL, PASSWORD))));

                String loginResponse1 = loginResult1.andReturn().getResponse().getContentAsString();
                userToken = com.jayway.jsonpath.JsonPath.read(loginResponse1, "$.jwtString");
                userId = jwtTokenGeneratorAdapter.extractCustomUserPrincipal(userToken).userId();

                ResultActions loginResult2 = mockMvc.perform(MockMvcRequestBuilders.post(API_AUTH + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AuthRequest("emailuser2@mail.com", "password123"))));

                String loginResponse2 = loginResult2.andReturn().getResponse().getContentAsString();
                user2Token = com.jayway.jsonpath.JsonPath.read(loginResponse2, "$.jwtString");
                user2Id = jwtTokenGeneratorAdapter.extractCustomUserPrincipal(user2Token).userId();
            }

            private CreateSpaceRequest createSpaceRequest(String spaceName, String emoji, List<StorageSpotRequest> storageSpotRequests) {
                return new CreateSpaceRequest(
                        spaceName,
                        emoji,
                        storageSpotRequests
                );
            }

            @DisplayName("should return 200 OK with the spaces data a user is a participant in")
            @Test
            void shouldReturn200WithDataOfSpaceUser1IsParticipant() throws Exception {

                // Creation of space 1 with user 1 as creator
                ResultActions creationOfSpace1 = mockMvc.perform(MockMvcRequestBuilders.post(API_SPACES)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createSpaceRequest(space1Name, "🏠", List.of(new StorageSpotRequest(storageSpotName, storageSpotType))))));

                String creationOfSpace1Result = creationOfSpace1.andReturn().getResponse().getContentAsString();
                String space1Id = com.jayway.jsonpath.JsonPath.read(creationOfSpace1Result, "$.id");

                ResultActions result1 = mockMvc.perform(MockMvcRequestBuilders.get(API_SPACES)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON));

                result1.andExpect(status().isOk())
                        .andExpect(jsonPath("$[0].id").value(space1Id))
                        .andExpect(jsonPath("$[0].spaceName").value(space1Name))
                        .andExpect(jsonPath("$[0].creatorId").value(userId))
                        .andExpect(jsonPath("$[0].participantIds", hasItem(userId)))
                        .andExpect(jsonPath("$[0].storageSpots", hasSize(1)))
                        .andExpect(jsonPath("$[0].storageSpots[0].storageSpotName").value(storageSpotName))
                        .andExpect(jsonPath("$[0].storageSpots[0].storageSpotType").value(storageSpotType));

                // Creation of space 2 with user 2 as creator
                ResultActions creationOfSpace2 = mockMvc.perform(MockMvcRequestBuilders.post(API_SPACES)
                        .header("Authorization", "Bearer " + user2Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createSpaceRequest(space2Name, "🏠", List.of(new StorageSpotRequest(storageSpotName, storageSpotType))))));

                String creationOfSpace2Result = creationOfSpace2.andReturn().getResponse().getContentAsString();
                String space2Id = com.jayway.jsonpath.JsonPath.read(creationOfSpace2Result, "$.id");

                ResultActions result2 = mockMvc.perform(MockMvcRequestBuilders.get(API_SPACES)
                        .header("Authorization", "Bearer " + user2Token)
                        .contentType(MediaType.APPLICATION_JSON));

                result2.andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(1)))
                        .andExpect(jsonPath("$[0].id").value(space2Id))
                        .andExpect(jsonPath("$[0].spaceName").value(space2Name))
                        .andExpect(jsonPath("$[0].creatorId").value(user2Id))
                        .andExpect(jsonPath("$[0].participantIds", hasItem(user2Id)))
                        .andExpect(jsonPath("$[0].storageSpots", hasSize(1)));

                assertEquals(2, spaceSpringDataRepository.count());
            }

            @DisplayName("should return 200 with an empty list when retrieving list of spaces of a new user")
            @Test
            void shouldReturn200OkWithEmptyList() throws Exception {

                // Creation of an account and a user
                mockMvc.perform(MockMvcRequestBuilders.post(API_AUTH + "/register/user")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new AuthRequest("emailuser3@mail.com", "password12"))))
                        .andExpect(status().isCreated());

                // Login of the new users
                ResultActions loginResult = mockMvc.perform(MockMvcRequestBuilders.post(API_AUTH + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AuthRequest("emailuser3@mail.com", "password12"))));

                String loginResponse = loginResult.andReturn().getResponse().getContentAsString();
                userToken = com.jayway.jsonpath.JsonPath.read(loginResponse, "$.jwtString");

                ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get(API_SPACES)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON));

                result.andExpect(status().isOk())
                        .andExpect(jsonPath("$", hasSize(0)));
            }

            @DisplayName("should return 401 Unauthorized when an invalid token is sent as param")
            @Test
            void shouldReturn401UnauthorizedWhenAnInvalidTokenIsSent() throws Exception {

                ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get(API_SPACES)
                        .header("Authorization", "Bearer " + "invalid-user-token")
                        .contentType(MediaType.APPLICATION_JSON));

                result.andExpect(status().isUnauthorized())
                        .andExpect(jsonPath("$.title").value("Unauthorized"))
                        .andExpect(jsonPath("$.detail", containsString("Must be authenticated to access this resource")));
            }

            @DisplayName("should return 403 Forbidden when an admin token is sent as param")
            @Test
            void shouldReturn403ForbiddenWhenAnAdminTokenIsSent() throws Exception {

                Account admin = Account.createAdmin(Email.of("admin@mail.com"), PASSWORD);
                jpaAccountRepositoryAdapter.save(admin);
                String adminToken = jwtTokenGeneratorAdapter.generateToken(admin, ResolvedEntities.constitute(null, "adminid1234")).token();

                ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get(API_SPACES)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON));

                result.andExpect(status().isForbidden())
                        .andExpect(jsonPath("$.title").value("Forbidden"))
                        .andExpect(jsonPath("$.detail", containsString("Access Denied")));
            }
        }
    }
}

