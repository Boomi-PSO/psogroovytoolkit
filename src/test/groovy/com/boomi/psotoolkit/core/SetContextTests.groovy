package com.boomi.psotoolkit.core

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test;

import com.boomi.execution.ExecutionManager
import com.boomi.execution.ExecutionTask
import com.boomi.execution.ExecutionUtil
import com.boomi.psotoolkit.BaseTests

class SetContextTests extends BaseTests {

	// Constants
	String DPP_FWK_DISABLE_NOTIFICATION_DEFAULT = "DPP_FWK_DISABLE_NOTIFICATION_DEFAULT";
	String DPP_FWK_DISABLE_NOTIFICATION = "DPP_FWK_DISABLE_NOTIFICATION";
	String DPP_FWK_DISABLE_AUDIT_DEFAULT = "DPP_FWK_DISABLE_AUDIT_DEFAULT";
	String DPP_FWK_DISABLE_AUDIT = "DPP_FWK_DISABLE_AUDIT";
	String DPP_FWK_ENABLE_ERROR_TERM_DEFAULT = "DPP_FWK_ENABLE_ERROR_TERM_DEFAULT";
	String DPP_FWK_ENABLE_ERROR_TERM = "DPP_FWK_ENABLE_ERROR_TERM";
	String DPP_FWK_PROCESSNAME = "DPP_FWK_ProcessName";
	String DPP_FWK_PROCESSID = "DPP_FWK_ProcessId";
	String DPP_FWK_EXECUTIONID = "DPP_FWK_ExecutionId";
	String DPP_FWK_CONTAINERID = "DPP_FWK_ContainerId";
	String DPP_FWK_PROCESS_ERROR = "DPP_FWK_PROCESS_ERROR";
	String DPP_FWK_STARTTIME = "DPP_FWK_StartTime";
	String INMETHOD = "inmethod";
	String INPATH = "inpath";
	String DPP_FWK_APIURL = "DPP_FWK_APIURL";
	String DPP_FWK_TRACKEDFIELDS = "DPP_FWK_TrackedFields";
	String DPP_FWK_TRACKINGID = "DPP_FWK_TrackingId";
	String DDP_INHEADER = "document.dynamic.userdefined.inheader_";
	String DPP_FWK_INHEADER_ = "DPP_FWK_inheader_";
	String DPP_FWK_TF_ = "DPP_FWK_TF_";
	String DPP_FWK_TRACKINGID_DEFAULT = "DPP_FWK_TrackingId_Default";

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

		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_DISABLE_NOTIFICATION_DEFAULT, "1");
		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_DISABLE_AUDIT_DEFAULT, "1");
		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_ENABLE_ERROR_TERM_DEFAULT, "1");
		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_TRACKINGID_DEFAULT, "134567890");

		new SetContext(dataContext).execute();

		ExecutionTask execTaskCurrent = ExecutionManager.getCurrent();
		String containerId = ExecutionUtil.getRuntimeExecutionProperty('NODE_ID');

		assert ExecutionUtil.dynamicProcessProperties.get(DPP_FWK_DISABLE_NOTIFICATION) == "1";
		assert ExecutionUtil.dynamicProcessProperties.get(DPP_FWK_DISABLE_AUDIT) == "1";
		assert ExecutionUtil.dynamicProcessProperties.get(DPP_FWK_ENABLE_ERROR_TERM) == "1";
		assert ExecutionUtil.dynamicProcessProperties.get(DPP_FWK_CONTAINERID) == containerId;
		assert ExecutionUtil.dynamicProcessProperties.get(DPP_FWK_EXECUTIONID) == execTaskCurrent.getTopLevelExecutionId();
		assert ExecutionUtil.dynamicProcessProperties.get(DPP_FWK_PROCESSID) == execTaskCurrent.getTopLevelProcessId();
		assert ExecutionUtil.dynamicProcessProperties.get(DPP_FWK_PROCESSNAME) == execTaskCurrent.getProcessName();
		assert ExecutionUtil.dynamicProcessProperties.get(DPP_FWK_STARTTIME) == execTaskCurrent.getStartTime().toString();
		assert ExecutionUtil.dynamicProcessProperties.get(DPP_FWK_TRACKINGID) == "134567890";
	}

	@Test
	void testEnvelope() {
		def dataContext = setupDataContextFromFolder("src/test/resources/com/boomi/psotoolkit/core/setcontext");

		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_DISABLE_NOTIFICATION_DEFAULT, "1");
		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_DISABLE_AUDIT_DEFAULT, "1");
		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_ENABLE_ERROR_TERM_DEFAULT, "1");
		dataContext.getProperties(0).put(DDP_INHEADER + "X-API-Key", "42939e45-c8bd-475f-96a5-80d259e72f01");

		new SetContext(dataContext).execute();

		ExecutionTask execTaskCurrent = ExecutionManager.getCurrent();
		String containerId = ExecutionUtil.getRuntimeExecutionProperty('NODE_ID');

		assert ExecutionUtil.dynamicProcessProperties.get(DPP_FWK_CONTAINERID) == containerId;
		assert ExecutionUtil.dynamicProcessProperties.get(DPP_FWK_EXECUTIONID) == execTaskCurrent.getTopLevelExecutionId();
		assert ExecutionUtil.dynamicProcessProperties.get(DPP_FWK_PROCESSID) == execTaskCurrent.getTopLevelProcessId();
		assert ExecutionUtil.dynamicProcessProperties.get(DPP_FWK_PROCESSNAME) == execTaskCurrent.getProcessName();
		assert ExecutionUtil.dynamicProcessProperties.get(DPP_FWK_STARTTIME) == execTaskCurrent.getStartTime().toString();
		assert ExecutionUtil.dynamicProcessProperties.get(DPP_FWK_TRACKINGID) == "987654321";
		assert ExecutionUtil.dynamicProcessProperties.get(DPP_FWK_TRACKEDFIELDS) == "orderId#0001,customerId#0002";
		assert ExecutionUtil.dynamicProcessProperties.get("DPP_FWK_TF_ORDERID") == "0001";
		assert ExecutionUtil.dynamicProcessProperties.get("DPP_FWK_TF_CUSTOMERID") == "0002";
		assert ExecutionUtil.dynamicProcessProperties.get(DPP_FWK_INHEADER_ + "X-API-Key") == "42939e45-c8bd-475f-96a5-80d259e72f01";
	}

	@Test
	void testMultiEnvelope() {
		def dataContext = setupDataContextFromFolder("src/test/resources/com/boomi/psotoolkit/core/setcontextEnvs");

		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_DISABLE_NOTIFICATION_DEFAULT, "1");
		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_DISABLE_AUDIT_DEFAULT, "1");
		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_ENABLE_ERROR_TERM_DEFAULT, "1");
		dataContext.getProperties(0).put(DDP_INHEADER + "X-API-Key", "42939e45-c8bd-475f-96a5-80d259e72f01");

		try {
			new SetContext(dataContext).execute();
		}
		catch (IllegalStateException ise) {
			assert ise.getMessage() == "The number of detected Message Envelopes is 2. Only a single Message Envelope allowed at SET Context.";
		}

		//assert !ExecutionUtil.dynamicProcessProperties.get(DPP_FWK_TRACKINGID);
	}
}