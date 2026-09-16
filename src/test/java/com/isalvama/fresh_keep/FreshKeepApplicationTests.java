package com.isalvama.fresh_keep;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@TestPropertySource(properties = {
		"JWT_SECRET=a-very-long-secret-to-avoid-any-key-size-related-errors-123456",
		"JWT_EXPIRATION=3600000",
		"GOOGLE_GENAI_API_KEY=test-key",
		"CLOUDINARY_CLOUD_NAME=test-cloud",
		"CLOUDINARY_API_KEY=test-api-key",
		"CLOUDINARY_API_SECRET=test-api-secret"
})
class FreshKeepApplicationTests {

	@Test
	void contextLoads() {
	}

}
