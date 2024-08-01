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
	void testSortAll() {
		def dataContext = setupDataContextFromFolder("src/test/resources/com/boomi/psotoolkit/core/filtersortaudititems");

		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_DISABLE_NOTIFICATION, "0");
		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_DISABLE_AUDIT, "0");
		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_ERROR_LEVEL, "true");

		//ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_WARN_LEVEL, "false");

		dataContext.getProperties(0).put(DDP_FWK_SORT_TS, "2024-07-02T14:17:56.541Z");
		dataContext.getProperties(1).put(DDP_FWK_SORT_TS, "2024-07-02T14:17:52.944Z");
		dataContext.getProperties(2).put(DDP_FWK_SORT_TS, "2024-07-02T14:17:54.823Z");

		dataContext.getProperties(0).put(DDP_FWK_LEVEL, "LOG");
		dataContext.getProperties(1).put(DDP_FWK_LEVEL, "ERROR");
		dataContext.getProperties(2).put(DDP_FWK_LEVEL, "LOG");

		new FilterSortAuditItems(dataContext).execute();

		assert dataContext.getOutProperties()[0].get(DDP_FWK_SORT_TS).equals("2024-07-02T14:17:52.944Z");
		assert dataContext.getOutProperties()[1].get(DDP_FWK_SORT_TS).equals("2024-07-02T14:17:54.823Z");
		assert dataContext.getOutProperties()[2].get(DDP_FWK_SORT_TS).equals("2024-07-02T14:17:56.541Z");
	}

	@Test
	void testSortAuditLogOnly() {
		def dataContext = setupDataContextFromFolder("src/test/resources/com/boomi/psotoolkit/core/filtersortaudititems");

		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_DISABLE_NOTIFICATION, "1");
		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_DISABLE_AUDIT, "0");
		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_ERROR_LEVEL, "true");

		//ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_WARN_LEVEL, "false");

		dataContext.getProperties(0).put(DDP_FWK_SORT_TS, "2024-07-02T14:17:56.541Z");
		dataContext.getProperties(1).put(DDP_FWK_SORT_TS, "2024-07-02T14:17:52.944Z");
		dataContext.getProperties(2).put(DDP_FWK_SORT_TS, "2024-07-02T14:17:54.823Z");

		dataContext.getProperties(0).put(DDP_FWK_LEVEL, "LOG");
		dataContext.getProperties(1).put(DDP_FWK_LEVEL, "ERROR");
		dataContext.getProperties(2).put(DDP_FWK_LEVEL, "LOG");

		new FilterSortAuditItems(dataContext).execute();

		assert dataContext.getOutProperties()[0].get(DDP_FWK_SORT_TS).equals("2024-07-02T14:17:54.823Z");
		assert dataContext.getOutProperties()[1].get(DDP_FWK_SORT_TS).equals("2024-07-02T14:17:56.541Z");
		assert !dataContext.getOutProperties()[2];
	}


	@Test
	void testSortNothing() {
		def dataContext = setupDataContextFromFolder("src/test/resources/com/boomi/psotoolkit/core/filtersortaudititems");

		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_DISABLE_NOTIFICATION, "1");
		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_DISABLE_AUDIT, "1");
		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_ERROR_LEVEL, "true");
		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_WARN_LEVEL, "true");

		//ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_WARN_LEVEL, "false");

		dataContext.getProperties(0).put(DDP_FWK_SORT_TS, "2024-07-02T14:17:56.541Z");
		dataContext.getProperties(1).put(DDP_FWK_SORT_TS, "2024-07-02T14:17:52.944Z");
		dataContext.getProperties(2).put(DDP_FWK_SORT_TS, "2024-07-02T14:17:54.823Z");

		dataContext.getProperties(0).put(DDP_FWK_LEVEL, "LOG");
		dataContext.getProperties(1).put(DDP_FWK_LEVEL, "ERROR");
		dataContext.getProperties(2).put(DDP_FWK_LEVEL, "WARNING");

		new FilterSortAuditItems(dataContext).execute();

		assert !dataContext.getOutProperties()[0];
		assert !dataContext.getOutStreams()[0];
	}
	@Test

	void testSortAuditLogsBecauseNotification() {
		def dataContext = setupDataContextFromFolder("src/test/resources/com/boomi/psotoolkit/core/filtersortaudititems");

		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_DISABLE_NOTIFICATION, "0");
		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_DISABLE_AUDIT, "1");
		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_ERROR_LEVEL, "true");
		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_WARN_LEVEL, "true");

		//ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_WARN_LEVEL, "false");

		dataContext.getProperties(0).put(DDP_FWK_SORT_TS, "2024-07-02T14:17:56.541Z");
		dataContext.getProperties(1).put(DDP_FWK_SORT_TS, "2024-07-02T14:17:52.944Z");
		dataContext.getProperties(2).put(DDP_FWK_SORT_TS, "2024-07-02T14:17:54.823Z");

		dataContext.getProperties(0).put(DDP_FWK_LEVEL, "LOG");
		dataContext.getProperties(1).put(DDP_FWK_LEVEL, "ERROR");
		dataContext.getProperties(2).put(DDP_FWK_LEVEL, "WARNING");

		new FilterSortAuditItems(dataContext).execute();

		assert dataContext.getOutProperties()[0].get(DDP_FWK_SORT_TS).equals("2024-07-02T14:17:52.944Z");
		assert dataContext.getOutProperties()[1].get(DDP_FWK_SORT_TS).equals("2024-07-02T14:17:54.823Z");
		assert dataContext.getOutProperties()[2].get(DDP_FWK_SORT_TS).equals("2024-07-02T14:17:56.541Z");
	}
}