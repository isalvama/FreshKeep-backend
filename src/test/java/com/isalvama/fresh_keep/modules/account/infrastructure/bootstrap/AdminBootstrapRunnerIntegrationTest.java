package com.isalvama.fresh_keep.modules.account.infrastructure.bootstrap;

import com.isalvama.fresh_keep.modules.account.infrastructure.web.dto.request.AuthRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "jwt.secret=55676267733273357638792F423F4528482B4D6251655468576D5A7134743777",
        "jwt.expiration=3600000",
        "cloudinary.cloud_name=test-cloud",
        "cloudinary.api_key=test-api-key",
        "cloudinary.api_secret=test-api-secret",
        "admin.bootstrap.email=" + AdminBootstrapRunnerIntegrationTest.EMAIL,
        "admin.bootstrap.password=" + AdminBootstrapRunnerIntegrationTest.PASSWORD
})
class AdminBootstrapRunnerIntegrationTest {
    static final String EMAIL = "bootstrap@admin.com";
    static final String PASSWORD = "BootstrapPwd1";

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"));

    @Autowired
    private AdminBootstrapRunner adminBootstrapRunner;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldProvisionBootstrapAdminOnStartupSoItCanLogIn() throws Exception {
        assertEquals(1, jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM roles r JOIN accounts a ON a.id = r.account_id WHERE a.email = ? AND r.role = 'ADMIN'",
                Integer.class, EMAIL));
        assertEquals(1, jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM admins ad JOIN accounts a ON a.id = ad.account_id WHERE a.email = ?",
                Integer.class, EMAIL));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AuthRequest(EMAIL, PASSWORD))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwtString").exists());
    }

    // Re-running simulates a restart with the admin already present, which exercises the lazy roles lookup.
    @Test
    void shouldNotDuplicateRowsWhenRunAgain() {
        adminBootstrapRunner.run(null);

        assertEquals(1, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM accounts WHERE email = ?", Integer.class, EMAIL));
        assertEquals(1, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM admins WHERE email = ?", Integer.class, EMAIL));
    }
}
