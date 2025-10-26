package com.rj.ecommerce_ai_agent

import com.rj.ecommerce_ai_agent.config.TestConfig
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["spring.main.allow-bean-definition-overriding=true"]
)
@ActiveProfiles("test")
@Import(TestConfig::class)
class EcommerceAiAgentApplicationTests {

	@Test
	fun contextLoads() {
		// This test verifies that the Spring context loads successfully
	}

}
