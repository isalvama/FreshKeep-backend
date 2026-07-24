package com.isalvama.fresh_keep;

import com.isalvama.fresh_keep.modules.account.infrastructure.persistence.jpa.AccountSpringDataRepository;
import com.isalvama.fresh_keep.modules.account.infrastructure.web.dto.request.AuthRequest;
import com.isalvama.fresh_keep.modules.account.domain.model.Account;
import com.isalvama.fresh_keep.modules.account.domain.value_object.Email;
import com.isalvama.fresh_keep.modules.account.infrastructure.persistence.jpa.JpaAccountRepositoryAdapter;
import com.isalvama.fresh_keep.modules.account.infrastructure.security.token.CustomUserPrincipal;
import com.isalvama.fresh_keep.modules.account.infrastructure.security.token.JwtTokenGeneratorAdapter;
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
        private AccountSpringDataRepository accountSpringDataRepository;

        @Autowired
        private JwtTokenGeneratorAdapter jwtTokenGeneratorAdapter;

        @Nested
        @DisplayName("POST " + API_AUTH + "/register")
        class RegisterAccount {

            @Nested
            @DisplayName("POST " + API_AUTH + "/register/user")
            class RegisterUser {

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
                            .andExpect(jsonPath("$.email").value(EMAIL))
                            .andExpect(jsonPath("$.jwtString").exists());

                    String resultAsString = result.andReturn().getResponse().getContentAsString();
                    String resultToken = com.jayway.jsonpath.JsonPath.read(resultAsString, "$.jwtString");

                    CustomUserPrincipal userPrincipal = jwtTokenGeneratorAdapter.extractCustomUserPrincipal(resultToken);
                    assertNotNull(userPrincipal.id());
                    assertEquals(EMAIL, userPrincipal.getUsername());
                    assertNull(userPrincipal.passwordHash());

                    Collection<? extends GrantedAuthority> authorities = userPrincipal.getAuthorities();

                    assertThat(authorities)
                            .hasSize(1)
                            .extracting(GrantedAuthority::getAuthority)
                            .containsExactly("ROLE_USER")
                            .doesNotContain("ROLE_ADMIN");
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
                    adminToken = jwtTokenGeneratorAdapter.generateToken(admin).token();
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
                            .andExpect(jsonPath("$.email").value(email))
                            .andExpect(jsonPath("$.jwtString").exists());

                    String resultAsString = result.andReturn().getResponse().getContentAsString();
                    String resultToken = com.jayway.jsonpath.JsonPath.read(resultAsString, "$.jwtString");

                    CustomUserPrincipal userPrincipal = jwtTokenGeneratorAdapter.extractCustomUserPrincipal(resultToken);
                    assertNotNull(userPrincipal.id());
                    assertEquals(email, userPrincipal.getUsername());
                    assertNull(userPrincipal.passwordHash());

                    Collection<? extends GrantedAuthority> authorities = userPrincipal.getAuthorities();

                    assertThat(authorities)
                            .hasSize(1)
                            .extracting(GrantedAuthority::getAuthority)
                            .containsExactly("ROLE_ADMIN")
                            .doesNotContain("ROLE_USER");
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
}

