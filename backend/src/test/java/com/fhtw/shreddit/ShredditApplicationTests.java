package com.fhtw.shreddit;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages({
	"com.fhtw.shreddit.controller",
	"com.fhtw.shreddit.service",
	"com.fhtw.shreddit.security",
	"com.fhtw.shreddit.exception",
	"com.fhtw.shreddit.integration"
})
class ShredditApplicationTests {
	// This test suite executes all tests in the specified packages
}
