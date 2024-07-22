package com.boomi.psotoolkit.core

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test;

import com.boomi.execution.ExecutionUtil
import com.boomi.psotoolkit.BaseTests

class GetExecDurationTests extends BaseTests {

	String DPP_FWK_STARTTIME = "DPP_FWK_StartTime";
	String DPP_FWK_EXEC_DURATION_MSEC = "DPP_FWK_EXEC_DURATION_MSEC";

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
		ExecutionUtil.setDynamicProcessProperty(DPP_FWK_STARTTIME, '1719997897000', false);
		new GetExecDuration(dataContext).execute();
	}
}