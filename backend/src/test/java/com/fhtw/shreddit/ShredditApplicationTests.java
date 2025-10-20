package com.fhtw.shreddit;

import org.junit.jupiter.api.Test;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Suite
@SelectPackages("com.fhtw.shreddit")
@SpringBootTest
@ActiveProfiles("test")
class ShredditApplicationTests {

	@Test
	void contextLoads() {
		// This test verifies that the Spring context loads correctly
		// The @Suite annotation makes this class run all tests in the specified packages
	}

}
