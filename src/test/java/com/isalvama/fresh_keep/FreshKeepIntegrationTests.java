package com.isalvama.fresh_keep;

import com.isalvama.fresh_keep.modules.account.application.port.out.dto.ResolvedEntities;
import com.isalvama.fresh_keep.modules.account.application.service.RegisterUserAccountService;
import com.isalvama.fresh_keep.modules.account.infrastructure.persistence.jpa.AccountSpringDataRepository;
import com.isalvama.fresh_keep.modules.account.infrastructure.web.dto.request.AuthRequest;
import com.isalvama.fresh_keep.modules.account.domain.model.Account;
import com.isalvama.fresh_keep.modules.product.domain.model.ProductType;
import com.isalvama.fresh_keep.modules.product.application.port.out.ProductMovedExpirationDateCalculatorPort;
import com.isalvama.fresh_keep.modules.user.domain.model.User;
import com.isalvama.fresh_keep.modules.user.infrastructure.persistence.jpa.JpaUserRepositoryAdapter;
import com.isalvama.fresh_keep.shared.domain.value_object.Email;
import com.isalvama.fresh_keep.modules.account.infrastructure.persistence.jpa.JpaAccountRepositoryAdapter;
import com.isalvama.fresh_keep.modules.account.infrastructure.security.token.CustomUserPrincipal;
import com.isalvama.fresh_keep.modules.account.infrastructure.security.token.JwtTokenGeneratorAdapter;
import com.isalvama.fresh_keep.modules.space.infrastructure.persistence.jpa.JpaSpaceSpringDataRepository;
import com.isalvama.fresh_keep.modules.product.infrastructure.web.dto.request.DeleteProductsRequest;
import com.isalvama.fresh_keep.modules.product.infrastructure.web.dto.request.MoveProductRequest;
import com.isalvama.fresh_keep.modules.space.infrastructure.web.dto.request.CreateSpaceRequest;
import com.isalvama.fresh_keep.modules.space.infrastructure.web.dto.request.StorageSpotRequest;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.ObjectMapper;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        "jwt.expiration=3600000",
        "cloudinary.cloud_name=test-cloud",
        "cloudinary.api_key=test-api-key",
        "cloudinary.api_secret=test-api-secret"
})
@AutoConfigureMockMvc
public class FreshKeepIntegrationTests {
    private static final String API_AUTH = "/api/v1/auth";
    private static final String API_SPACES = "/api/v1/spaces";
    private static final String API_PRODUCTS = "/api/v1/products";

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

        @Autowired
        private JpaSpaceSpringDataRepository spaceSpringDataRepository;

        // Spaces created by other nested test classes (which share this same Testcontainers Postgres instance)
        // must be cleared before accounts, or a leftover spaces_participants row referencing an account this
        // class is about to delete violates its FK and breaks an unrelated test.
        @BeforeEach
        void clearSpaces() {
            spaceSpringDataRepository.deleteAll();
        }

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

        @Nested
        @DisplayName("GET " + API_SPACES + "/{spaceId}/overview")
        class GetOverview {

            @Autowired
            private JdbcTemplate jdbcTemplate;

            private String overviewSpaceId;
            private UUID overviewStorageSpotId;

            @BeforeEach
            void setUp() throws Exception {
                ResultActions creation = mockMvc.perform(MockMvcRequestBuilders.post(API_SPACES)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())));

                String creationResponse = creation.andReturn().getResponse().getContentAsString();
                overviewSpaceId = com.jayway.jsonpath.JsonPath.read(creationResponse, "$.id");
                String storageSpotId = com.jayway.jsonpath.JsonPath.read(creationResponse, "$.storageSpots[0].storageSpotId");
                overviewStorageSpotId = UUID.fromString(storageSpotId);
            }

            // Not a precise benchmark - its job is to catch a real regression (e.g. an accidental N+1 query
            // per product) rather than measure exact latency, hence the generous time budget.
            @DisplayName("should return 200 within a generous time budget for a space with a large number of products")
            @Test
            void shouldReturnOverviewWithinTimeBudgetForALargeNumberOfProducts() throws Exception {
                int productCount = 500;
                insertProducts(productCount, UUID.fromString(overviewSpaceId), overviewStorageSpotId);

                long startNanos = System.nanoTime();

                ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get(API_SPACES + "/" + overviewSpaceId + "/overview")
                        .header("Authorization", "Bearer " + userToken));

                long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;

                result.andExpect(status().isOk())
                        .andExpect(jsonPath("$.productResults", hasSize(productCount)));

                long budgetMs = 3000;
                assertTrue(elapsedMs < budgetMs, () -> "Expected the overview endpoint to respond within " + budgetMs
                        + "ms for " + productCount + " products, but took " + elapsedMs + "ms - possible N+1 query"
                        + " or other regression in the products lookup path.");
            }

            private void insertProducts(int count, UUID spaceId, UUID storageSpotId) {
                List<Object[]> batchArgs = new java.util.ArrayList<>(count);
                for (int i = 0; i < count; i++) {
                    batchArgs.add(new Object[]{
                            UUID.randomUUID(), "Product " + i, LocalDate.now().plusDays(i % 60),
                            storageSpotId, storageSpotId, "OTHER", BigDecimal.valueOf(1.0), "USD"
                    });
                }
                jdbcTemplate.batchUpdate(
                        "INSERT INTO products (id, name, expiration_date, suggested_storage_spot_id, actual_storage_spot_id, product_type, price, currency) "
                                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                        batchArgs
                );
            }
        }
    }

    @Nested
    @DisplayName(API_PRODUCTS)
    class Products {

        private static final String EMAIL = "product-owner@email.com";
        private static final String PASSWORD = "Password1";
        private String userToken;
        private UUID spaceId;
        private UUID storageSpotId;
        private UUID newStorageSpotId;
        private UUID shoppingReceiptId;

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private AccountSpringDataRepository accountSpringDataRepository;

        @Autowired
        private JpaSpaceSpringDataRepository spaceSpringDataRepository;

        @Autowired
        private JwtTokenGeneratorAdapter jwtTokenGeneratorAdapter;

        @Autowired
        private JdbcTemplate jdbcTemplate;

        @BeforeEach
        void setUp() throws Exception {
            spaceSpringDataRepository.deleteAll();
            accountSpringDataRepository.deleteAll();

            userToken = registerAndLogin(EMAIL, PASSWORD);

            ResultActions spaceCreation = mockMvc.perform(MockMvcRequestBuilders.post(API_SPACES)
                    .header("Authorization", "Bearer " + userToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new CreateSpaceRequest(
                            "Kitchen", "🏠", List.of(new StorageSpotRequest("Fridge", "FRIDGE"))))));

            String spaceResponse = spaceCreation.andReturn().getResponse().getContentAsString();
            spaceId = UUID.fromString(com.jayway.jsonpath.JsonPath.read(spaceResponse, "$.id"));
            String storageSpotIdString = com.jayway.jsonpath.JsonPath.read(spaceResponse, "$.storageSpots[0].storageSpotId");
            storageSpotId = UUID.fromString(storageSpotIdString);
            newStorageSpotId = UUID.randomUUID();
            jdbcTemplate.update(
                    "INSERT INTO storage_spots (id, name, storage_spot_type, space_id) VALUES (?, ?, ?, ?)",
                    newStorageSpotId, "Freezer", "FREEZER", spaceId
            );

            shoppingReceiptId = UUID.randomUUID();
            jdbcTemplate.update("INSERT INTO shopping_receipts (id) VALUES (?)", shoppingReceiptId);
        }

        private String registerAndLogin(String email, String password) throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.post(API_AUTH + "/register/user")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new AuthRequest(email, password))))
                    .andExpect(status().isCreated());

            ResultActions loginResult = mockMvc.perform(MockMvcRequestBuilders.post(API_AUTH + "/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new AuthRequest(email, password))));

            String loginResponse = loginResult.andReturn().getResponse().getContentAsString();
            return com.jayway.jsonpath.JsonPath.read(loginResponse, "$.jwtString");
        }

        private UUID insertProduct(UUID storageSpotId) {
            UUID id = UUID.randomUUID();
            UUID userId = jdbcTemplate.queryForObject(
                    "SELECT id FROM users WHERE email = ?", UUID.class, EMAIL);
            jdbcTemplate.update(
                    "INSERT INTO products (id, name, expiration_date, suggested_storage_spot_id, actual_storage_spot_id, product_type, shopping_receipt_id, price, currency) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    id, "Milk", LocalDate.now().plusDays(7), storageSpotId, storageSpotId, "DAIRY", shoppingReceiptId, BigDecimal.valueOf(1.5), "USD"
            );
            jdbcTemplate.update(
                    "INSERT INTO product_storage_spot_history (product_id, user_id, new_expiration_date, new_storage_spot_id) "
                            + "VALUES (?, ?, ?, ?)",
                    id, userId, LocalDate.now().plusDays(7), storageSpotId
            );
            return id;
        }

        private Instant deletedAtOf(UUID productId) {
            return jdbcTemplate.queryForObject("SELECT deleted_at FROM products WHERE id = ?", Instant.class, productId);
        }

        @Nested
        @DisplayName("PATCH " + API_PRODUCTS + "/{id}/storage-spot")
        class MoveProduct {

            @MockitoBean
            private ProductMovedExpirationDateCalculatorPort expirationDateCalculatorPort;

            @Test
            void shouldMoveProductAndPersistTheNewStorageSpotAndExpirationDate() throws Exception {
                UUID productId = insertProduct(storageSpotId);
                LocalDate newExpirationDate = LocalDate.of(2026, 9, 25);
                org.mockito.Mockito.when(expirationDateCalculatorPort.execute(org.mockito.ArgumentMatchers.any()))
                        .thenReturn(newExpirationDate);

                mockMvc.perform(MockMvcRequestBuilders.patch(API_PRODUCTS + "/" + productId + "/storage-spot")
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(
                                        new MoveProductRequest(storageSpotId, newStorageSpotId))))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.productId").value(productId.toString()))
                        .andExpect(jsonPath("$.newStorageSpotId").value(newStorageSpotId.toString()))
                        .andExpect(jsonPath("$.newExpirationDate").value(newExpirationDate.toString()));

                Map<String, Object> persistedProduct = jdbcTemplate.queryForMap(
                        "SELECT actual_storage_spot_id, expiration_date FROM products WHERE id = ?", productId);
                assertEquals(newStorageSpotId, persistedProduct.get("actual_storage_spot_id"));
                assertEquals(newExpirationDate, ((java.sql.Date) persistedProduct.get("expiration_date")).toLocalDate());
            }
        }

        @Nested
        @DisplayName("DELETE " + API_PRODUCTS + "/{id}")
        class DeleteProduct {

            @DisplayName("should return 204 and soft-delete the product when authenticated as a participant")
            @Test
            void shouldReturn204AndSoftDeleteProductWhenAuthenticatedAsParticipant() throws Exception {
                UUID productId = insertProduct(storageSpotId);

                mockMvc.perform(MockMvcRequestBuilders.delete(API_PRODUCTS + "/" + productId)
                                .header("Authorization", "Bearer " + userToken))
                        .andExpect(status().isNoContent());

                assertNotNull(deletedAtOf(productId));
            }

            @DisplayName("should return 400 when the product does not exist")
            @Test
            void shouldReturn400WhenProductDoesNotExist() throws Exception {
                mockMvc.perform(MockMvcRequestBuilders.delete(API_PRODUCTS + "/" + UUID.randomUUID())
                                .header("Authorization", "Bearer " + userToken))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Business Rule Error"));
            }

            @DisplayName("should return 409 and leave the product untouched when authenticated user is not a participant of its space")
            @Test
            void shouldReturn409WhenNotAParticipant() throws Exception {
                UUID productId = insertProduct(storageSpotId);
                String otherUserToken = registerAndLogin("other-user@email.com", PASSWORD);

                mockMvc.perform(MockMvcRequestBuilders.delete(API_PRODUCTS + "/" + productId)
                                .header("Authorization", "Bearer " + otherUserToken))
                        .andExpect(status().isConflict());

                assertNull(deletedAtOf(productId));
            }

            @DisplayName("should return 401 when not authenticated")
            @Test
            void shouldReturn401WhenNotAuthenticated() throws Exception {
                UUID productId = insertProduct(storageSpotId);

                mockMvc.perform(MockMvcRequestBuilders.delete(API_PRODUCTS + "/" + productId))
                        .andExpect(status().isUnauthorized());

                assertNull(deletedAtOf(productId));
            }
        }

        @Nested
        @DisplayName("DELETE " + API_PRODUCTS)
        class DeleteProducts {

            @DisplayName("should return 204 and soft-delete every product when authenticated as a participant")
            @Test
            void shouldReturn204AndSoftDeleteAllProductsWhenAuthenticatedAsParticipant() throws Exception {
                UUID milkId = insertProduct(storageSpotId);
                UUID yogurtId = insertProduct(storageSpotId);

                mockMvc.perform(MockMvcRequestBuilders.delete(API_PRODUCTS)
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new DeleteProductsRequest(List.of(milkId, yogurtId)))))
                        .andExpect(status().isNoContent());

                assertNotNull(deletedAtOf(milkId));
                assertNotNull(deletedAtOf(yogurtId));
            }

            @DisplayName("should return 400 and delete nothing when any requested id does not exist")
            @Test
            void shouldReturn400AndDeleteNothingWhenAnyIdDoesNotExist() throws Exception {
                UUID milkId = insertProduct(storageSpotId);

                mockMvc.perform(MockMvcRequestBuilders.delete(API_PRODUCTS)
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new DeleteProductsRequest(List.of(milkId, UUID.randomUUID())))))
                        .andExpect(status().isBadRequest())
                        .andExpect(jsonPath("$.title").value("Business Rule Error"));

                assertNull(deletedAtOf(milkId));
            }

            @DisplayName("should return 409 and delete nothing when any product is not accessible to the authenticated user")
            @Test
            void shouldReturn409AndDeleteNothingWhenAnyProductIsNotAccessible() throws Exception {
                UUID milkId = insertProduct(storageSpotId);
                String otherUserToken = registerAndLogin("other-user@email.com", PASSWORD);

                mockMvc.perform(MockMvcRequestBuilders.delete(API_PRODUCTS)
                                .header("Authorization", "Bearer " + otherUserToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new DeleteProductsRequest(List.of(milkId)))))
                        .andExpect(status().isConflict());

                assertNull(deletedAtOf(milkId));
            }

            @DisplayName("should return 400 when productsIds is empty")
            @Test
            void shouldReturn400WhenProductsIdsIsEmpty() throws Exception {
                mockMvc.perform(MockMvcRequestBuilders.delete(API_PRODUCTS)
                                .header("Authorization", "Bearer " + userToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new DeleteProductsRequest(List.of()))))
                        .andExpect(status().isBadRequest());
            }

            @DisplayName("should return 401 when not authenticated")
            @Test
            void shouldReturn401WhenNotAuthenticated() throws Exception {
                UUID milkId = insertProduct(storageSpotId);

                mockMvc.perform(MockMvcRequestBuilders.delete(API_PRODUCTS)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new DeleteProductsRequest(List.of(milkId)))))
                        .andExpect(status().isUnauthorized());

                assertNull(deletedAtOf(milkId));
            }
        }
    }

    /**
     * Exercises the real end-to-end flow with real receipt photos: real Gemini extraction, real Ollama
     * review, real Cloudinary upload, real Postgres persistence - nothing mocked. Not part of the default
     * `mvn test` run (see the "llm-eval" surefire exclusion in pom.xml).
     * <p>
     * To run it: drop one or more .jpg/.jpeg/.png receipt photos into src/test/resources/receipts
     * (gitignored - never commit real receipt photos), have a local Ollama daemon running with the
     * configured model pulled, and run with real credentials, e.g.:
     * {@code GOOGLE_GENAI_API_KEY=... CLOUDINARY_CLOUD_NAME=... CLOUDINARY_API_KEY=... CLOUDINARY_API_SECRET=... mvn test -Dtest=FreshKeepIntegrationTests$ProcessNewShoppingReceipt}
     * <p>
     * Any missing prerequisite (credentials, Ollama, image files) makes this skip cleanly rather than fail.
     */
    @Nested
    @Tag("llm-eval")
    @DisplayName(API_SPACES + "/{spaceId}/receipt-images")
    @TestPropertySource(properties = {
            "spring.ai.google.genai.api-key=${GOOGLE_GENAI_API_KEY:dummy-key-for-context-boot}",
            "cloudinary.cloud_name=${CLOUDINARY_CLOUD_NAME:}",
            "cloudinary.api_key=${CLOUDINARY_API_KEY:}",
            "cloudinary.api_secret=${CLOUDINARY_API_SECRET:}"
    })
    class ProcessNewShoppingReceipt {

        private static final String EMAIL = "receipt-tester@email.com";
        private static final String PASSWORD = "Password1";
        private static final Path RECEIPTS_DIR = Path.of("src/test/resources/receipts");

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private AccountSpringDataRepository accountSpringDataRepository;

        @Autowired
        private JpaSpaceSpringDataRepository spaceSpringDataRepository;

        private String userToken;
        private String spaceId;
        private List<Path> receiptImages;

        @BeforeEach
        void setUp() throws Exception {
            Assumptions.assumeTrue(isPresent("GOOGLE_GENAI_API_KEY"),
                    "GOOGLE_GENAI_API_KEY is not set - skipping");
            Assumptions.assumeTrue(isPresent("CLOUDINARY_CLOUD_NAME") && isPresent("CLOUDINARY_API_KEY") && isPresent("CLOUDINARY_API_SECRET"),
                    "Cloudinary credentials are not set - skipping");
            Assumptions.assumeTrue(isOllamaReachable(),
                    "Ollama is not reachable on localhost:11434 - skipping");

            receiptImages = findReceiptImages();
            Assumptions.assumeTrue(!receiptImages.isEmpty(),
                    "No receipt images found in src/test/resources/receipts - drop some .jpg/.jpeg/.png files there to run this test");

            spaceSpringDataRepository.deleteAll();
            accountSpringDataRepository.deleteAll();

            mockMvc.perform(MockMvcRequestBuilders.post(API_AUTH + "/register/user")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new AuthRequest(EMAIL, PASSWORD))))
                    .andExpect(status().isCreated());

            ResultActions loginResult = mockMvc.perform(MockMvcRequestBuilders.post(API_AUTH + "/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new AuthRequest(EMAIL, PASSWORD))));
            userToken = com.jayway.jsonpath.JsonPath.read(loginResult.andReturn().getResponse().getContentAsString(), "$.jwtString");

            CreateSpaceRequest spaceRequest = new CreateSpaceRequest(
                    "Kitchen", "🏠",
                    List.of(
                            new StorageSpotRequest("Fridge", "FRIDGE"),
                            new StorageSpotRequest("Pantry", "PANTRY"),
                            new StorageSpotRequest("Freezer", "FREEZER")
                    )
            );
            ResultActions spaceResult = mockMvc.perform(MockMvcRequestBuilders.post(API_SPACES)
                    .header("Authorization", "Bearer " + userToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(spaceRequest)));
            spaceId = com.jayway.jsonpath.JsonPath.read(spaceResult.andReturn().getResponse().getContentAsString(), "$.id");
        }

        private boolean isPresent(String envVar) {
            String value = System.getenv(envVar);
            return value != null && !value.isBlank();
        }

        private boolean isOllamaReachable() {
            try {
                HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
                HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:11434/api/tags"))
                        .timeout(Duration.ofSeconds(2)).GET().build();
                return client.send(request, HttpResponse.BodyHandlers.discarding()).statusCode() < 500;
            } catch (Exception e) {
                return false;
            }
        }

        private List<Path> findReceiptImages() throws Exception {
            if (!Files.isDirectory(RECEIPTS_DIR)) {
                return List.of();
            }
            try (Stream<Path> stream = Files.list(RECEIPTS_DIR)) {
                return stream
                        .filter(Files::isRegularFile)
                        .filter(p -> {
                            String name = p.getFileName().toString().toLowerCase();
                            return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png");
                        })
                        .toList();
            }
        }

        @DisplayName("should extract plausible data from real shopping receipt photos")
        @Test
        void shouldProcessRealReceiptPhotosEndToEnd() throws Exception {
            Set<String> validProductTypes = Arrays.stream(ProductType.values()).map(Enum::name).collect(Collectors.toSet());

            for (Path imagePath : receiptImages) {
                byte[] content = Files.readAllBytes(imagePath);
                String contentType = imagePath.getFileName().toString().toLowerCase().endsWith(".png") ? "image/png" : "image/jpeg";
                MockMultipartFile file = new MockMultipartFile("file", imagePath.getFileName().toString(), contentType, content);

                ResultActions result = mockMvc.perform(MockMvcRequestBuilders.multipart(API_SPACES + "/" + spaceId + "/receipt-images")
                        .file(file)
                        .header("Authorization", "Bearer " + userToken));

                String responseBody = result.andReturn().getResponse().getContentAsString();
                int status = result.andReturn().getResponse().getStatus();

                // @AutoConfigureMockMvc only auto-prints request/response details when a test fails, so this is
                // printed unconditionally to let a human eyeball the real extraction quality on passing runs too.
                System.out.println("=== " + imagePath.getFileName() + " (status " + status + ") ===");
                System.out.println(responseBody);

                assertEquals(201, status, () -> "unexpected status for " + imagePath.getFileName() + ": " + responseBody);

                String receiptImageId = com.jayway.jsonpath.JsonPath.read(responseBody, "$.receiptImageId");
                assertNotNull(receiptImageId, () -> imagePath.getFileName() + ": receiptImageId should not be null");
                assertFalse(receiptImageId.isBlank(), () -> imagePath.getFileName() + ": receiptImageId should not be blank");

                String purchaseShoppingDate = com.jayway.jsonpath.JsonPath.read(responseBody, "$.purchaseShoppingDate");
                assertNotNull(purchaseShoppingDate, () -> imagePath.getFileName() + ": purchaseShoppingDate should not be null");

                String storeName = com.jayway.jsonpath.JsonPath.read(responseBody, "$.storeName");
                assertNotNull(storeName, () -> imagePath.getFileName() + ": storeName should not be null");
                assertFalse(storeName.isBlank(), () -> imagePath.getFileName() + ": storeName should not be blank");

                List<Map<String, Object>> products = com.jayway.jsonpath.JsonPath.read(responseBody, "$.productExtractions");
                assertFalse(products.isEmpty(), () -> imagePath.getFileName() + ": should extract at least one product");

                for (Map<String, Object> product : products) {
                    String productName = (String) product.get("productName");
                    assertNotNull(productName, () -> imagePath.getFileName() + ": productName should not be null");
                    assertFalse(productName.isBlank(), () -> imagePath.getFileName() + ": productName should not be blank");

                    String productType = (String) product.get("productType");
                    assertTrue(validProductTypes.contains(productType),
                            () -> imagePath.getFileName() + ": unexpected productType " + productType + " for " + productName);

                    assertNotNull(product.get("expirationDate"),
                            () -> imagePath.getFileName() + ": expirationDate should not be null for " + productName);
                }

                // flaggedProducts' *content* can't be asserted deterministically - whether anything gets
                // flagged depends on this specific real receipt and the real reviewer model's judgment.
                // Its structural presence as a valid (possibly empty) list is what's actually checkable here.
                List<Map<String, Object>> flaggedProducts = com.jayway.jsonpath.JsonPath.read(responseBody, "$.flaggedProducts");
                assertNotNull(flaggedProducts, () -> imagePath.getFileName() + ": flaggedProducts should not be null");
            }
        }
    }

    /**
     * Exercises the real end-to-end reprocess flow with real receipt photos: a real processNewShoppingReceipt
     * call first (real Gemini extraction, real Cloudinary upload) to obtain a receiptImageId and an initial
     * product list, then a real reProcessShoppingReceiptWithFlaggedProducts call (real Gemini re-extraction,
     * real Postgres persistence of the shopping receipt and its products) with one product flagged for
     * re-examination. Not part of the default `mvn test` run (see the "llm-eval" surefire exclusion in pom.xml).
     * <p>
     * To run it: same prerequisites as {@link ProcessNewShoppingReceipt} - drop receipt photos into
     * src/test/resources/receipts, have a local Ollama daemon running (only needed by the initial processing
     * step's review), and run with real credentials, e.g.:
     * {@code GOOGLE_GENAI_API_KEY=... CLOUDINARY_CLOUD_NAME=... CLOUDINARY_API_KEY=... CLOUDINARY_API_SECRET=... mvn test -Dtest='FreshKeepIntegrationTests$ReProcessShoppingReceiptWithFlaggedProducts' -DexcludedGroups=}
     * <p>
     * Any missing prerequisite (credentials, Ollama, image files) makes this skip cleanly rather than fail.
     */
    @Nested
    @Tag("llm-eval")
    @DisplayName(API_SPACES + "/{spaceId}/shopping-receipt")
    @TestPropertySource(properties = {
            "spring.ai.google.genai.api-key=${GOOGLE_GENAI_API_KEY:dummy-key-for-context-boot}",
            "cloudinary.cloud_name=${CLOUDINARY_CLOUD_NAME:}",
            "cloudinary.api_key=${CLOUDINARY_API_KEY:}",
            "cloudinary.api_secret=${CLOUDINARY_API_SECRET:}"
    })
    class ReProcessShoppingReceiptWithFlaggedProducts {

        private static final String EMAIL = "reprocess-tester@email.com";
        private static final String PASSWORD = "Password1";
        private static final Path RECEIPTS_DIR = Path.of("src/test/resources/receipts");

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private AccountSpringDataRepository accountSpringDataRepository;

        @Autowired
        private JpaSpaceSpringDataRepository spaceSpringDataRepository;

        private String userToken;
        private String spaceId;
        private List<Path> receiptImages;

        @BeforeEach
        void setUp() throws Exception {
            Assumptions.assumeTrue(isPresent("GOOGLE_GENAI_API_KEY"),
                    "GOOGLE_GENAI_API_KEY is not set - skipping");
            Assumptions.assumeTrue(isPresent("CLOUDINARY_CLOUD_NAME") && isPresent("CLOUDINARY_API_KEY") && isPresent("CLOUDINARY_API_SECRET"),
                    "Cloudinary credentials are not set - skipping");
            Assumptions.assumeTrue(isOllamaReachable(),
                    "Ollama is not reachable on localhost:11434 - skipping");

            receiptImages = findReceiptImages();
            Assumptions.assumeTrue(!receiptImages.isEmpty(),
                    "No receipt images found in src/test/resources/receipts - drop some .jpg/.jpeg/.png files there to run this test");

            spaceSpringDataRepository.deleteAll();
            accountSpringDataRepository.deleteAll();

            mockMvc.perform(MockMvcRequestBuilders.post(API_AUTH + "/register/user")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new AuthRequest(EMAIL, PASSWORD))))
                    .andExpect(status().isCreated());

            ResultActions loginResult = mockMvc.perform(MockMvcRequestBuilders.post(API_AUTH + "/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new AuthRequest(EMAIL, PASSWORD))));
            userToken = com.jayway.jsonpath.JsonPath.read(loginResult.andReturn().getResponse().getContentAsString(), "$.jwtString");

            CreateSpaceRequest spaceRequest = new CreateSpaceRequest(
                    "Kitchen", "🏠",
                    List.of(
                            new StorageSpotRequest("Fridge", "FRIDGE"),
                            new StorageSpotRequest("Pantry", "PANTRY"),
                            new StorageSpotRequest("Freezer", "FREEZER")
                    )
            );
            ResultActions spaceResult = mockMvc.perform(MockMvcRequestBuilders.post(API_SPACES)
                    .header("Authorization", "Bearer " + userToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(spaceRequest)));
            spaceId = com.jayway.jsonpath.JsonPath.read(spaceResult.andReturn().getResponse().getContentAsString(), "$.id");
        }

        private boolean isPresent(String envVar) {
            String value = System.getenv(envVar);
            return value != null && !value.isBlank();
        }

        private boolean isOllamaReachable() {
            try {
                HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
                HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:11434/api/tags"))
                        .timeout(Duration.ofSeconds(2)).GET().build();
                return client.send(request, HttpResponse.BodyHandlers.discarding()).statusCode() < 500;
            } catch (Exception e) {
                return false;
            }
        }

        private List<Path> findReceiptImages() throws Exception {
            if (!Files.isDirectory(RECEIPTS_DIR)) {
                return List.of();
            }
            try (Stream<Path> stream = Files.list(RECEIPTS_DIR)) {
                return stream
                        .filter(Files::isRegularFile)
                        .filter(p -> {
                            String name = p.getFileName().toString().toLowerCase();
                            return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png");
                        })
                        .toList();
            }
        }

        // Products from the real AI extraction may have no suggestedStorageSpotId (the model is told to leave
        // it empty when nothing fits) - but ProductRequest.suggestedStorageSpotId is @NotNull, so a fallback
        // (one of the space's own storage spots) is substituted to keep this test about the reprocess flow's
        // behavior, not about whether the model always suggests a spot.
        private void addProductParams(MockHttpServletRequestBuilder builder, String prefix, int index, Map<String, Object> product, String fallbackStorageSpotId) {
            String p = prefix + "[" + index + "].";
            builder.param(p + "expirationDate", String.valueOf(product.get("expirationDate")));
            builder.param(p + "productName", String.valueOf(product.get("productName")));
            Object storageSpotId = product.get("suggestedStorageSpotId");
            builder.param(p + "suggestedStorageSpotId",
                    storageSpotId == null || storageSpotId.toString().isBlank() ? fallbackStorageSpotId : storageSpotId.toString());
            builder.param(p + "productType", String.valueOf(product.get("productType")));
            Object priceAmount = product.get("priceAmount");
            if (priceAmount != null) {
                builder.param(p + "priceAmount", priceAmount.toString());
            }
            Object currency = product.get("currency");
            if (currency != null && !currency.toString().isBlank()) {
                builder.param(p + "currency", currency.toString());
            }
        }

        @DisplayName("should reprocess a flagged product against the real receipt photo and persist the corrected shopping receipt")
        @Test
        void shouldReprocessRealReceiptPhotosEndToEnd() throws Exception {
            for (Path imagePath : receiptImages) {
                byte[] content = Files.readAllBytes(imagePath);
                String contentType = imagePath.getFileName().toString().toLowerCase().endsWith(".png") ? "image/png" : "image/jpeg";
                MockMultipartFile file = new MockMultipartFile("file", imagePath.getFileName().toString(), contentType, content);

                ResultActions processResult = mockMvc.perform(MockMvcRequestBuilders.multipart(API_SPACES + "/" + spaceId + "/receipt-images")
                        .file(file)
                        .header("Authorization", "Bearer " + userToken));
                String processResponseBody = processResult.andReturn().getResponse().getContentAsString();
                assertEquals(201, processResult.andReturn().getResponse().getStatus(),
                        () -> "unexpected status processing " + imagePath.getFileName() + ": " + processResponseBody);

                String receiptImageId = com.jayway.jsonpath.JsonPath.read(processResponseBody, "$.receiptImageId");
                String purchaseShoppingDate = com.jayway.jsonpath.JsonPath.read(processResponseBody, "$.purchaseShoppingDate");
                String storeName = com.jayway.jsonpath.JsonPath.read(processResponseBody, "$.storeName");
                List<Map<String, Object>> products = com.jayway.jsonpath.JsonPath.read(processResponseBody, "$.productExtractions");
                String fallbackStorageSpotId = com.jayway.jsonpath.JsonPath.read(processResponseBody, "$.suggestedStorageSpots[0].storageSpotId");

                if (products.isEmpty()) {
                    continue;
                }

                MockHttpServletRequestBuilder reprocessRequest = MockMvcRequestBuilders.post(API_SPACES + "/" + spaceId + "/shopping-receipt")
                        .header("Authorization", "Bearer " + userToken)
                        .param("receiptImageId", receiptImageId)
                        .param("shoppingDate", purchaseShoppingDate)
                        .param("storeName", storeName);

                for (int i = 0; i < products.size(); i++) {
                    addProductParams(reprocessRequest, "allProducts", i, products.get(i), fallbackStorageSpotId);
                }
                addProductParams(reprocessRequest, "flaggedProducts", 0, products.getFirst(), fallbackStorageSpotId);

                ResultActions reprocessResult = mockMvc.perform(reprocessRequest);
                String reprocessResponseBody = reprocessResult.andReturn().getResponse().getContentAsString();

                System.out.println(reprocessResponseBody);

                assertEquals(201, reprocessResult.andReturn().getResponse().getStatus(),
                        () -> "unexpected status reprocessing " + imagePath.getFileName() + ": " + reprocessResponseBody);

                String shoppingReceiptId = com.jayway.jsonpath.JsonPath.read(reprocessResponseBody, "$.id");
                assertNotNull(shoppingReceiptId, () -> imagePath.getFileName() + ": shopping receipt id should not be null");
                assertFalse(shoppingReceiptId.isBlank(), () -> imagePath.getFileName() + ": shopping receipt id should not be blank");

                String reprocessedStoreName = com.jayway.jsonpath.JsonPath.read(reprocessResponseBody, "$.storeName");
                assertNotNull(reprocessedStoreName, () -> imagePath.getFileName() + ": storeName should not be null");

                List<Map<String, Object>> persistedProducts = com.jayway.jsonpath.JsonPath.read(reprocessResponseBody, "$.products");
                assertEquals(products.size(), persistedProducts.size(),
                        () -> imagePath.getFileName() + ": product count should be preserved through reprocessing");

                for (Map<String, Object> product : persistedProducts) {
                    assertNotNull(product.get("id"), () -> imagePath.getFileName() + ": persisted product should have a generated id");

                    String productName = (String) product.get("productName");
                    assertNotNull(productName, () -> imagePath.getFileName() + ": productName should not be null");
                    assertFalse(productName.isBlank(), () -> imagePath.getFileName() + ": productName should not be blank");
                }

                List<Map<String, Object>> storageSpots = com.jayway.jsonpath.JsonPath.read(reprocessResponseBody, "$.storageSpots");
                assertFalse(storageSpots.isEmpty(), () -> imagePath.getFileName() + ": storageSpots should not be empty");
            }
        }
    }
}
