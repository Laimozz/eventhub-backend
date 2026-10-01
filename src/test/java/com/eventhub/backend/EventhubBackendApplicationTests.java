package com.eventhub.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@Import(PostgresTestConfiguration.class)
@TestPropertySource(locations = "classpath:auth-test.properties")
class EventhubBackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
