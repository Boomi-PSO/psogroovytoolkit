package com.boomi.psotoolkit.core

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test;

import com.boomi.execution.ExecutionUtil
import com.boomi.psotoolkit.BaseTests

class SetContextFacadeTests extends BaseTests {

	@BeforeEach
	void setUp() {
	}

	@AfterEach
	void tearDown() {
		ExecutionUtil.dynamicProcessProperties.clear();
	}

	@Test
	void testSuccess() {
		def dataContext = setupDataContextFromFolder("src/test/resources/com/boomi/psotoolkit/core/any");

		new SetContextFacade(dataContext).execute();
	}
}