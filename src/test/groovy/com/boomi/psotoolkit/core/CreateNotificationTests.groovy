package com.boomi.psotoolkit.core

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test;

import com.boomi.execution.ExecutionUtil
import com.boomi.psotoolkit.BaseTests

import groovy.json.JsonSlurper

class CreateNotificationTests extends BaseTests {

	// Constants
	String DDP_FWK_DOC_KEY = "document.dynamic.userdefined.DDP_FWK_DOC_KEY";
	String DDP_FWK_DOC_VAL = "document.dynamic.userdefined.DDP_FWK_DOC_VAL";
	String DDP_FWK_DOC_CRUD_TYPE = "document.dynamic.userdefined.DDP_FWK_DOC_CRUD_TYPE";
	String DDP_FWK_NS_LEVEL = "document.dynamic.userdefined.DDP_FWK_NS_LEVEL";
	String DDP_FWK_NS_CLASS = "document.dynamic.userdefined.DDP_FWK_NS_CLASS";
	String DDP_FWK_NS_DOC = "document.dynamic.userdefined.DDP_FWK_NS_DOC";
	String DDP_FWK_NS_MSG = "document.dynamic.userdefined.DDP_FWK_NS_MSG";
	String DDP_FWK_NS_INTERNAL_ID = "document.dynamic.userdefined.DDP_FWK_NS_INTERNAL_ID";
	String DDP_FWK_NS_PROCESSCALLSTACK = "document.dynamic.userdefined.DDP_FWK_NS_ProcessCallStack";

	String DPP_FWK_EXEC_SUMMARY_FLAG = "DPP_FWK_EXEC_SUMMARY_FLAG";
	String DPP_FWK_ERROR_LEVEL = "DPP_FWK_ERROR_LEVEL";
	String DPP_FWK_WARN_LEVEL = "DPP_FWK_WARN_LEVEL";

	def dataContext;

	@BeforeEach
	void setUp() {
		dataContext = setupDataContextFromFolder("src/test/resources/com/boomi/psotoolkit/core/any");
		dataContext.getProperties(0).put(DDP_FWK_NS_INTERNAL_ID, "8486312778851429004");
		dataContext.getProperties(0).put(DDP_FWK_NS_PROCESSCALLSTACK, "[FWK] CLOSE Context (route) (inline-test) (Continuation f_0) > [FWK] CREATE Notification (facade)");
		dataContext.getProperties(0).put(DDP_FWK_NS_DOC, "Hello World!");
		dataContext.getProperties(0).put(DDP_FWK_NS_CLASS, "TransformationError");
	}

	@AfterEach
	void tearDown() {
	}

	@Test
	void testSuccess() {
		dataContext.getProperties(0).put(DDP_FWK_NS_LEVEL, "ERROR");
		dataContext.getProperties(0).put(DDP_FWK_NS_MSG, "This is a test error");

		new CreateNotification(dataContext).execute();

		JsonSlurper jsluper = new JsonSlurper();

		String jsonOut = '{"Auditlogitem":[{"Level":"ERROR","Id":"8486312778851429004","Timestamp":"2024-07-03T16:16:20.342Z","ProcessCallStack":"[FWK] CLOSE Context (route) (inline-test) (Continuation f_0) > [FWK] CREATE Notification (facade)","Step":"Notification","ErrorClass":"TransformationError","Details":"This is a test error","DocType":"NOTIFICATION_DOC","DocBase64":"SGVsbG8gV29ybGQh"}]}';
		def expectedJson = jsluper.parseText(jsonOut);
		def actualJson = jsluper.parseText(dataContext.getOutStreams()[0].getText());

		assert expectedJson.Auditlogitem[0].Level.equals(actualJson.Auditlogitem[0].Level);
		assert expectedJson.Auditlogitem[0].Id.equals(actualJson.Auditlogitem[0].Id);
		assert expectedJson.Auditlogitem[0].ProcessCallStack.equals(actualJson.Auditlogitem[0].ProcessCallStack);
		assert expectedJson.Auditlogitem[0].Step.equals(actualJson.Auditlogitem[0].Step);
		assert expectedJson.Auditlogitem[0].ErrorClass.equals(actualJson.Auditlogitem[0].ErrorClass);
		assert expectedJson.Auditlogitem[0].Details.equals(actualJson.Auditlogitem[0].Details);
		assert expectedJson.Auditlogitem[0].DocType.equals(actualJson.Auditlogitem[0].DocType);
		assert expectedJson.Auditlogitem[0].DocBase64.equals(actualJson.Auditlogitem[0].DocBase64);
	}

	@Test
	void testWarning() {
		dataContext.getProperties(0).put(DDP_FWK_NS_LEVEL, "WARNING");
		dataContext.getProperties(0).put(DDP_FWK_NS_MSG, "This is a test warning");

		new CreateNotification(dataContext).execute();

		JsonSlurper jsluper = new JsonSlurper();

		String jsonOut = '{"Auditlogitem":[{"Level":"WARNING","Id":"8486312778851429004","Timestamp":"2024-07-03T16:16:20.342Z","ProcessCallStack":"[FWK] CLOSE Context (route) (inline-test) (Continuation f_0) > [FWK] CREATE Notification (facade)","Step":"Notification","ErrorClass":"TransformationError","Details":"This is a test warning","DocType":"NOTIFICATION_DOC","DocBase64":"SGVsbG8gV29ybGQh"}]}';
		def expectedJson = jsluper.parseText(jsonOut);
		def actualJson = jsluper.parseText(dataContext.getOutStreams()[0].getText());

		assert expectedJson.Auditlogitem[0].Level.equals(actualJson.Auditlogitem[0].Level);
		assert expectedJson.Auditlogitem[0].Id.equals(actualJson.Auditlogitem[0].Id);
		assert expectedJson.Auditlogitem[0].ProcessCallStack.equals(actualJson.Auditlogitem[0].ProcessCallStack);
		assert expectedJson.Auditlogitem[0].Step.equals(actualJson.Auditlogitem[0].Step);
		assert expectedJson.Auditlogitem[0].ErrorClass.equals(actualJson.Auditlogitem[0].ErrorClass);
		assert expectedJson.Auditlogitem[0].Details.equals(actualJson.Auditlogitem[0].Details);
		assert expectedJson.Auditlogitem[0].DocType.equals(actualJson.Auditlogitem[0].DocType);
		assert expectedJson.Auditlogitem[0].DocBase64.equals(actualJson.Auditlogitem[0].DocBase64);
	}

	@Test
	void testCRUDCreate() {
		ExecutionUtil.setDynamicProcessProperty(DPP_FWK_EXEC_SUMMARY_FLAG, 'false', false);

		dataContext.getProperties(0).put(DDP_FWK_NS_LEVEL, "WARNING");
		dataContext.getProperties(0).put(DDP_FWK_NS_MSG, "This is a test warning");
		dataContext.getProperties(0).put(DDP_FWK_DOC_KEY, "key");
		dataContext.getProperties(0).put(DDP_FWK_DOC_VAL, "val");
		dataContext.getProperties(0).put(DDP_FWK_DOC_CRUD_TYPE, "CREATE");

		new CreateNotification(dataContext).execute();

		JsonSlurper jsluper = new JsonSlurper();

		String jsonOut = '{"Auditlogitem":[{"Level":"WARNING","Id":"8486312778851429004","ProcessCallStack":"[FWK] CLOSE Context (route) (inline-test) (Continuation f_0) > [FWK] CREATE Notification (facade)","Step":"Notification","ErrorClass":"TransformationError","Details":"This is a test warning","DocType":"NOTIFICATION_DOC","DocBase64":"SGVsbG8gV29ybGQh"}]}';
		def expectedJson = jsluper.parseText(jsonOut);
		def actualJson = jsluper.parse(dataContext.getOutStreams()[0]);

		String ts = actualJson.Auditlogitem[0].remove('Timestamp');
		assert ts != null;
		assert expectedJson == actualJson;
		assert "true".equals(ExecutionUtil.getDynamicProcessProperty(DPP_FWK_EXEC_SUMMARY_FLAG));
	}

	@Test
	void testCRUDUpsert() {
		ExecutionUtil.setDynamicProcessProperty(DPP_FWK_EXEC_SUMMARY_FLAG, 'false', false);

		dataContext.getProperties(0).put(DDP_FWK_NS_LEVEL, "WARNING");
		dataContext.getProperties(0).put(DDP_FWK_NS_MSG, "This is a test warning");
		dataContext.getProperties(0).put(DDP_FWK_DOC_KEY, "key");
		dataContext.getProperties(0).put(DDP_FWK_DOC_VAL, "val");
		dataContext.getProperties(0).put(DDP_FWK_DOC_CRUD_TYPE, "UPSERT");

		new CreateNotification(dataContext).execute();

		JsonSlurper jsluper = new JsonSlurper();

		String jsonOut = '{"Auditlogitem":[{"Level":"WARNING","Id":"8486312778851429004","ProcessCallStack":"[FWK] CLOSE Context (route) (inline-test) (Continuation f_0) > [FWK] CREATE Notification (facade)","Step":"Notification","ErrorClass":"TransformationError","Details":"This is a test warning","DocType":"NOTIFICATION_DOC","DocBase64":"SGVsbG8gV29ybGQh"}]}';
		def expectedJson = jsluper.parseText(jsonOut);
		def actualJson = jsluper.parse(dataContext.getOutStreams()[0]);

		String ts = actualJson.Auditlogitem[0].remove('Timestamp');
		assert ts != null;
		assert expectedJson == actualJson;
		assert "true".equals(ExecutionUtil.getDynamicProcessProperty(DPP_FWK_EXEC_SUMMARY_FLAG));
	}
}