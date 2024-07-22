package com.boomi.psotoolkit.core

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test;

import com.boomi.execution.ExecutionUtil
import com.boomi.psotoolkit.BaseTests

class FilterSortAuditItemsTests extends BaseTests {

	// Constants
	String DDP_FWK_SORT_TS = "document.dynamic.userdefined.DDP_FWK_SORT_TS";
	String DDP_FWK_LEVEL = "document.dynamic.userdefined.DDP_FWK_LEVEL";

	String DPP_FWK_DISABLE_NOTIFICATION = "DPP_FWK_DISABLE_NOTIFICATION";
	String DPP_FWK_DISABLE_AUDIT = "DPP_FWK_DISABLE_AUDIT";
	String DPP_FWK_ERROR_LEVEL = "DPP_FWK_ERROR_LEVEL";
	String DPP_FWK_WARN_LEVEL = "DPP_FWK_WARN_LEVEL";

	@BeforeEach
	void setUp() {
	}

	@AfterEach
	void tearDown() {
	}

	@Test
	void testSuccess() {
		def dataContext = setupDataContextFromFolder("src/test/resources/com/boomi/psotoolkit/core/filtersortaudititems");

		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_DISABLE_NOTIFICATION, "0");
		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_DISABLE_AUDIT, "0");
		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_ERROR_LEVEL, "true");
		//ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_WARN_LEVEL, "false");

		new FilterSortAuditItems(dataContext).execute();
	}
}