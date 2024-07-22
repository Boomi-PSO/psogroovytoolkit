package com.boomi.psotoolkit.core

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test;

import com.boomi.execution.ExecutionUtil
import com.boomi.psotoolkit.BaseTests

class CreateAuditLogNotificationTests extends BaseTests {

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
	String DPP_FWK_DIRECTORY = "DPP_FWK_Directory";
	String DPP_FWK_AUDITLOG_SIZE_MAX = "DPP_FWK_AUDITLOG_SIZE_MAX";
	//DPP_FWK_inheader_<postscript> - derived from default DDPs

	@BeforeEach
	void setUp() {
		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_TRACKINGID, "134567890");
		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_TRACKEDFIELDS, "name#testit");
		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_CONTAINERID, "1a945e1-bb16-443c-9ef0-d7f91bf342a8");
		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_EXECUTIONID, "e546a65d-52b4-4acb-9577-e368491c7009");
		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_PROCESSNAME, "Mock It");
		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_PROCESSID, "2466a0cf-2951-4d60-8ef7-548416c414ea");
		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_DIRECTORY, "Mock/It");
	}

	@AfterEach
	void tearDown() {
	}

	@Test
	void testSuccess() {
		def dataContext = setupDataContextFromFolder("src/test/resources/com/boomi/psotoolkit/core/createauditlognotification");

		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_AUDITLOG_SIZE_MAX, "1000");

		new CreateAuditLogNotification(dataContext).execute();
	}

	@Test
	void testSuccessAndError() {
		def dataContext = setupDataContextFromFolder("src/test/resources/com/boomi/psotoolkit/core/createauditlognotification");

		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_AUDITLOG_SIZE_MAX, "450");

		new CreateAuditLogNotification(dataContext).execute();
	}

	@Test
	void testSuccessAndRemoveAttachement() {
		def dataContext = setupDataContextFromFolder("src/test/resources/com/boomi/psotoolkit/core/createauditlognotification");

		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_AUDITLOG_SIZE_MAX, "600");

		new CreateAuditLogNotification(dataContext).execute();
	}

	@Test
	void testSuccessAndCompress() {
		def dataContext = setupDataContextFromFolder("src/test/resources/com/boomi/psotoolkit/core/createauditlognotification");

		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_AUDITLOG_SIZE_MAX, "865");

		new CreateAuditLogNotification(dataContext).execute();
	}
}