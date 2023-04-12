package com.chumbok.imageservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@ActiveProfiles("it")
@SpringBootTest(classes = {Application.class, TestConfig.class})
class ContextLoadIT {

	@Test
	void contextLoads() {
		// Verifies application context load properly.
	}
}
