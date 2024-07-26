package com.boomi.psotoolkit.core

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test;

import com.boomi.execution.ExecutionUtil
import com.boomi.psotoolkit.BaseTests

class SetProcessCallStackTests extends BaseTests {
	private static final String DDP_FWK_NS_LEVEL = "document.dynamic.userdefined.DDP_FWK_NS_LEVEL"
	private static final String DDP_FWK_NS_PROCESS_CALL_STACK = "document.dynamic.userdefined.DDP_FWK_NS_ProcessCallStack"

	@BeforeEach
	void setUp() {
	}

	@AfterEach
	void tearDown() {
		ExecutionUtil.dynamicProcessProperties.clear();
	}

	@Test
	void testSuccess() {
		def dataContext = setupDataContextFromFolder("src/test/resources/com/boomi/psotoolkit/core/processcallstack");

		String expectedCallStack = "Mock Process > [TEST] Level 1 > [TEST] Level 2 > [TEST] Level 3 > [TEST] Current Process";

		dataContext.getProperties(0).put(DDP_FWK_NS_LEVEL, "LOG");
		dataContext.getProperties(1).put(DDP_FWK_NS_LEVEL, "ERROR");
		dataContext.getProperties(2).put(DDP_FWK_NS_LEVEL, "WARNING");

		new SetProcessCallStack(dataContext).execute();

		assert !dataContext.getOutProperties()[0].getProperty(DDP_FWK_NS_PROCESS_CALL_STACK);
		assert expectedCallStack.equals(dataContext.getOutProperties()[1].getProperty(DDP_FWK_NS_PROCESS_CALL_STACK));
		assert expectedCallStack.equals(dataContext.getOutProperties()[2].getProperty(DDP_FWK_NS_PROCESS_CALL_STACK));
	}
}