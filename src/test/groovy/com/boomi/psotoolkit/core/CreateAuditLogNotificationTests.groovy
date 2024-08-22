package com.boomi.psotoolkit.core

import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test;

import com.boomi.execution.ExecutionUtil
import com.boomi.psotoolkit.BaseTests

import groovy.json.JsonSlurper

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
	String DDP_FWK_SORT_TS = "document.dynamic.userdefined.DDP_FWK_SORT_TS";
	String DDP_FWK_LEVEL = "document.dynamic.userdefined.DDP_FWK_LEVEL";

	String DPP_FWK_ERROR_LEVEL = "DPP_FWK_ERROR_LEVEL";
	String DPP_FWK_WARN_LEVEL = "DPP_FWK_WARN_LEVEL";
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

		new CreateAuditLogNotification(dataContext).executeWithoutFilterSortCombine();

		JsonSlurper jsluper = new JsonSlurper();

		String jsonOut = '{"ProcessContext":{"TrackingId":"134567890","TrackedFields":"name#testit","Container":"1a945e1-bb16-443c-9ef0-d7f91bf342a8","ExecutionId":"e546a65d-52b4-4acb-9577-e368491c7009","API":null,"MainProcessName":"Mock It","MainProcessComponentId":"2466a0cf-2951-4d60-8ef7-548416c414ea","Folder":"Mock/It"},"Auditlogitem":[{"Level":"LOG","Id":"1","Timestamp":"2024-07-01T09:40:15.389Z","Step":"Error","Details":"Test it","DocType":"json","DocBase64":"eyJERFBfRG9jdW1lbnRJZCI6IjY4MjUxNzcxNTQ5ODk3MDM3NDciLCJERFBfUHJldmlvdXNFdmVudElkIjoiNTQ0In0="}]}';
		def expectedJson = jsluper.parseText(jsonOut);
		def actualJson = jsluper.parseText(dataContext.getOutStreams()[0].getText());

		assert actualJson == expectedJson;
	}

	@Test
	void testSuccessAndError() {
		def dataContext = setupDataContextFromFolder("src/test/resources/com/boomi/psotoolkit/core/createauditlognotification");

		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_AUDITLOG_SIZE_MAX, "450");
		LocalDateTime now = LocalDateTime.now();

		new CreateAuditLogNotification(dataContext).executeWithoutFilterSortCombine();

		JsonSlurper jsluper = new JsonSlurper();

		String jsonOut = '{"ProcessContext":{"TrackingId":"134567890","TrackedFields":"name#testit","Container":"1a945e1-bb16-443c-9ef0-d7f91bf342a8","ExecutionId":"e546a65d-52b4-4acb-9577-e368491c7009","API":null,"MainProcessName":"Mock It","MainProcessComponentId":"2466a0cf-2951-4d60-8ef7-548416c414ea","Folder":"Mock/It"},"Auditlogitem":[{"Level":"LOG","Id":"1","Timestamp":"2024-07-01T09:40:15.389Z","Step":"Error","Details":"Test it","DocType":"json"}]}';
		def expectedJson = jsluper.parseText(jsonOut);
		def actualJson = jsluper.parseText(dataContext.getOutStreams()[0].getText());

		assert actualJson == expectedJson;

		jsonOut = '{"ProcessContext":{"TrackingId":"134567890","TrackedFields":"name#testit","Container":"1a945e1-bb16-443c-9ef0-d7f91bf342a8","ExecutionId":"e546a65d-52b4-4acb-9577-e368491c7009","API":null,"MainProcessName":"Mock It","MainProcessComponentId":"2466a0cf-2951-4d60-8ef7-548416c414ea","Folder":"Mock/It"},"Auditlogitem":[{"Level":"ERROR","Step":"Notification","ErrorClass":"InternalError"}]}';
		expectedJson = jsluper.parseText(jsonOut);
		actualJson = jsluper.parseText(dataContext.getOutStreams()[1].getText())
		String ts = actualJson.Auditlogitem[0].remove('Timestamp');
		assert ts != null;
		assert now.toEpochSecond(ZoneOffset.UTC) <= LocalDateTime.parse(ts, DateTimeFormatter.ofPattern("yyyyMMdd HHmmss.SSS")).toEpochSecond(ZoneOffset.UTC);

		String td = actualJson.ProcessContext.remove('TruncatedData');
		assert td != null;

		assert actualJson == expectedJson;
	}

	@Test
	void testSuccessAndRemoveAttachement() {
		def dataContext = setupDataContextFromFolder("src/test/resources/com/boomi/psotoolkit/core/createauditlognotification");

		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_AUDITLOG_SIZE_MAX, "600");

		new CreateAuditLogNotification(dataContext).executeWithoutFilterSortCombine();

		JsonSlurper jsluper = new JsonSlurper();

		String jsonOut = '{"ProcessContext":{"TrackingId":"134567890","TrackedFields":"name#testit","Container":"1a945e1-bb16-443c-9ef0-d7f91bf342a8","ExecutionId":"e546a65d-52b4-4acb-9577-e368491c7009","API":null,"MainProcessName":"Mock It","MainProcessComponentId":"2466a0cf-2951-4d60-8ef7-548416c414ea","Folder":"Mock/It"},"Auditlogitem":[{"Level":"LOG","Id":"1","Timestamp":"2024-07-01T09:40:15.389Z","Step":"Error","Details":"Test it","DocType":"json","DocBase64":"eyJERFBfRG9jdW1lbnRJZCI6IjY4MjUxNzcxNTQ5ODk3MDM3NDciLCJERFBfUHJldmlvdXNFdmVudElkIjoiNTQ0In0="}]}';
		def expectedJson = jsluper.parseText(jsonOut);
		def actualJson = jsluper.parseText(dataContext.getOutStreams()[0].getText());

		assert actualJson == expectedJson;

		jsonOut = '{"ProcessContext":{"TrackingId":"134567890","TrackedFields":"name#testit","Container":"1a945e1-bb16-443c-9ef0-d7f91bf342a8","ExecutionId":"e546a65d-52b4-4acb-9577-e368491c7009","API":null,"MainProcessName":"Mock It","MainProcessComponentId":"2466a0cf-2951-4d60-8ef7-548416c414ea","Folder":"Mock/It"},"Auditlogitem":[{"Level":"LOG","Id":"1","Timestamp":"2024-07-01T09:40:15.389Z","Step":"Error","Details":"Test it","DocType":"json"},{"Level":"LOG","Id":"2","Timestamp":"2024-07-01T09:40:15.389Z","Step":"Warning","Details":"Test it again","DocType":"json"}]}'
		expectedJson = jsluper.parseText(jsonOut);
		actualJson = jsluper.parseText(dataContext.getOutStreams()[1].getText());

		assert actualJson == expectedJson;
	}

	@Test
	void testSuccessAndCompress() {
		def dataContext = setupDataContextFromFolder("src/test/resources/com/boomi/psotoolkit/core/createauditlognotification");

		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_AUDITLOG_SIZE_MAX, "865");
		LocalDateTime now = LocalDateTime.now();

		new CreateAuditLogNotification(dataContext).executeWithoutFilterSortCombine();

		JsonSlurper jsluper = new JsonSlurper();

		String jsonOut = '{"ProcessContext":{"TrackingId":"134567890","TrackedFields":"name#testit","Container":"1a945e1-bb16-443c-9ef0-d7f91bf342a8","ExecutionId":"e546a65d-52b4-4acb-9577-e368491c7009","API":null,"MainProcessName":"Mock It","MainProcessComponentId":"2466a0cf-2951-4d60-8ef7-548416c414ea","Folder":"Mock/It"},"Auditlogitem":[{"Level":"LOG"}]}';
		def expectedJson = jsluper.parseText(jsonOut);
		def actualJson = jsluper.parseText(dataContext.getOutStreams()[2].getText());

		String ts = actualJson.Auditlogitem[0].remove('Timestamp');
		assert ts != null;

		String cd = actualJson.ProcessContext.remove('CompressedData');
		assert cd != null;

		assert now.toEpochSecond(ZoneOffset.UTC) <= LocalDateTime.parse(ts, DateTimeFormatter.ofPattern("yyyyMMdd HHmmss.SSS")).toEpochSecond(ZoneOffset.UTC);
		assert actualJson == expectedJson;
	}

	@Test
	void testFilterSortCombine() {
		def dataContext = setupDataContextFromFolder("src/test/resources/com/boomi/psotoolkit/core/filtersortcreateauditlognotification");

		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_AUDITLOG_SIZE_MAX, "2000");
		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_DISABLE_NOTIFICATION, "0");
		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_DISABLE_AUDIT, "0");

		dataContext.getProperties(0).put(DDP_FWK_SORT_TS, "20240802 074619.486");
		dataContext.getProperties(1).put(DDP_FWK_SORT_TS, "20240802 074637.048");
		dataContext.getProperties(2).put(DDP_FWK_SORT_TS, "20240802 074639.902");

		dataContext.getProperties(0).put(DDP_FWK_LEVEL, "LOG");
		dataContext.getProperties(1).put(DDP_FWK_LEVEL, "ERROR");
		dataContext.getProperties(2).put(DDP_FWK_LEVEL, "LOG");

		new CreateAuditLogNotification(dataContext).execute();

		JsonSlurper jsluper = new JsonSlurper();

		String jsonOut = '{"ProcessContext":{"TrackingId":"134567890","TrackedFields":"name#testit","Container":"1a945e1-bb16-443c-9ef0-d7f91bf342a8","ExecutionId":"e546a65d-52b4-4acb-9577-e368491c7009","API":null,"MainProcessName":"Mock It","MainProcessComponentId":"2466a0cf-2951-4d60-8ef7-548416c414ea","Folder":"Mock/It"},"Auditlogitem":[{"Level":"LOG","Id":"482013909434641342","Timestamp":"20240802 074619.486","Step":"[FWK] SET Context (route)","Details":"","DocType":null,"DocBase64":null},{"Level":"ERROR","Id":"5886728937423444414","Timestamp":"20240802 074637.048","ProcessCallStack":"[FWK] CLOSE Context (route) (inline-test) (Continuation f_0) &gt; [FWK] CREATE Notification (facade)","Step":"Notification","ErrorClass":"TransformationError","Details":"[FWK] CLOSE Context (route) (inline-test) (Continuation f_0) - This is a test error","DocType":"json","DocBase64":"eyJERFBfRG9jdW1lbnRJZCI6IjY4MjUxNzcxNTQ5ODk3MDM3NDciLCJERFBfUHJldmlvdXNFdmVudElkIjoiNTQ0In0="},{"Level":"LOG","Id":"8881144415106398243","Timestamp":"20240802 074639.902","Step":"[FWK] CLOSE Context (route) (Continuation f_0)","Details":"&lt;style&gt; tr:hover {background-color: #D6EEEE;} &lt;/style&gt; &lt;p&gt;&lt;table&gt; &lt;tr&gt; &lt;th&gt;Process Id&lt;/th&gt; &lt;td&gt;0a469ebb-9079-499d-98ac-d5778373fa18&lt;/td&gt; &lt;/tr&gt; &lt;tr&gt; &lt;th&gt;Execution Id&lt;/th&gt; &lt;td&gt;execution-a9a990f3-ff83-4c41-a894-631cee177614-2024.08.02&lt;/td&gt; &lt;/tr&gt; &lt;tr&gt; &lt;th&gt;Duration (ms)&lt;/th&gt; &lt;td&gt;41697&lt;/td&gt; &lt;/tr&gt; &lt;/table&gt;&lt;/p&gt;","DocType":null,"DocBase64":null}]}';
		String jsonStreamText = dataContext.getOutStreams()[0].getText();
		assert jsonOut.size() == jsonStreamText.size();
		def expectedJson = jsluper.parseText(jsonOut);
		def actualJson = jsluper.parseText(jsonStreamText);
		assert actualJson == expectedJson;
	}

	@Test
	void testFilterSortCombineForceAudit() {
		def dataContext = setupDataContextFromFolder("src/test/resources/com/boomi/psotoolkit/core/filtersortcreateauditlognotification");

		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_AUDITLOG_SIZE_MAX, "2000");
		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_DISABLE_NOTIFICATION, "0");
		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_DISABLE_AUDIT, "1");
		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_ERROR_LEVEL, "true");

		dataContext.getProperties(0).put(DDP_FWK_SORT_TS, "20240802 074619.486");
		dataContext.getProperties(1).put(DDP_FWK_SORT_TS, "20240802 074637.048");
		dataContext.getProperties(2).put(DDP_FWK_SORT_TS, "20240802 074639.902");

		dataContext.getProperties(0).put(DDP_FWK_LEVEL, "LOG");
		dataContext.getProperties(1).put(DDP_FWK_LEVEL, "ERROR");
		dataContext.getProperties(2).put(DDP_FWK_LEVEL, "LOG");

		new CreateAuditLogNotification(dataContext).execute();

		JsonSlurper jsluper = new JsonSlurper();

		String jsonOut = '{"ProcessContext":{"TrackingId":"134567890","TrackedFields":"name#testit","Container":"1a945e1-bb16-443c-9ef0-d7f91bf342a8","ExecutionId":"e546a65d-52b4-4acb-9577-e368491c7009","API":null,"MainProcessName":"Mock It","MainProcessComponentId":"2466a0cf-2951-4d60-8ef7-548416c414ea","Folder":"Mock/It"},"Auditlogitem":[{"Level":"LOG","Id":"482013909434641342","Timestamp":"20240802 074619.486","Step":"[FWK] SET Context (route)","Details":"","DocType":null,"DocBase64":null},{"Level":"ERROR","Id":"5886728937423444414","Timestamp":"20240802 074637.048","ProcessCallStack":"[FWK] CLOSE Context (route) (inline-test) (Continuation f_0) &gt; [FWK] CREATE Notification (facade)","Step":"Notification","ErrorClass":"TransformationError","Details":"[FWK] CLOSE Context (route) (inline-test) (Continuation f_0) - This is a test error","DocType":"json","DocBase64":"eyJERFBfRG9jdW1lbnRJZCI6IjY4MjUxNzcxNTQ5ODk3MDM3NDciLCJERFBfUHJldmlvdXNFdmVudElkIjoiNTQ0In0="},{"Level":"LOG","Id":"8881144415106398243","Timestamp":"20240802 074639.902","Step":"[FWK] CLOSE Context (route) (Continuation f_0)","Details":"&lt;style&gt; tr:hover {background-color: #D6EEEE;} &lt;/style&gt; &lt;p&gt;&lt;table&gt; &lt;tr&gt; &lt;th&gt;Process Id&lt;/th&gt; &lt;td&gt;0a469ebb-9079-499d-98ac-d5778373fa18&lt;/td&gt; &lt;/tr&gt; &lt;tr&gt; &lt;th&gt;Execution Id&lt;/th&gt; &lt;td&gt;execution-a9a990f3-ff83-4c41-a894-631cee177614-2024.08.02&lt;/td&gt; &lt;/tr&gt; &lt;tr&gt; &lt;th&gt;Duration (ms)&lt;/th&gt; &lt;td&gt;41697&lt;/td&gt; &lt;/tr&gt; &lt;/table&gt;&lt;/p&gt;","DocType":null,"DocBase64":null}]}';
		String jsonStreamText = dataContext.getOutStreams()[0].getText();
		assert jsonOut.size() == jsonStreamText.size();
		def expectedJson = jsluper.parseText(jsonOut);
		def actualJson = jsluper.parseText(jsonStreamText);
		assert actualJson == expectedJson;
	}

	@Test
	void testFilterSortCombineAuditOnly() {
		def dataContext = setupDataContextFromFolder("src/test/resources/com/boomi/psotoolkit/core/filtersortcreateauditlognotification");

		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_AUDITLOG_SIZE_MAX, "2000");
		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_DISABLE_NOTIFICATION, "1");
		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_DISABLE_AUDIT, "0");

		dataContext.getProperties(0).put(DDP_FWK_SORT_TS, "20240802 074619.486");
		dataContext.getProperties(1).put(DDP_FWK_SORT_TS, "20240802 074637.048");
		dataContext.getProperties(2).put(DDP_FWK_SORT_TS, "20240802 074639.902");

		dataContext.getProperties(0).put(DDP_FWK_LEVEL, "LOG");
		dataContext.getProperties(1).put(DDP_FWK_LEVEL, "ERROR");
		dataContext.getProperties(2).put(DDP_FWK_LEVEL, "LOG");

		new CreateAuditLogNotification(dataContext).execute();

		JsonSlurper jsluper = new JsonSlurper();

		String jsonOut = '{"ProcessContext":{"TrackingId":"134567890","TrackedFields":"name#testit","Container":"1a945e1-bb16-443c-9ef0-d7f91bf342a8","ExecutionId":"e546a65d-52b4-4acb-9577-e368491c7009","API":null,"MainProcessName":"Mock It","MainProcessComponentId":"2466a0cf-2951-4d60-8ef7-548416c414ea","Folder":"Mock/It"},"Auditlogitem":[{"Level":"LOG","Id":"482013909434641342","Timestamp":"20240802 074619.486","Step":"[FWK] SET Context (route)","Details":"","DocType":null,"DocBase64":null},{"Level":"LOG","Id":"8881144415106398243","Timestamp":"20240802 074639.902","Step":"[FWK] CLOSE Context (route) (Continuation f_0)","Details":"&lt;style&gt; tr:hover {background-color: #D6EEEE;} &lt;/style&gt; &lt;p&gt;&lt;table&gt; &lt;tr&gt; &lt;th&gt;Process Id&lt;/th&gt; &lt;td&gt;0a469ebb-9079-499d-98ac-d5778373fa18&lt;/td&gt; &lt;/tr&gt; &lt;tr&gt; &lt;th&gt;Execution Id&lt;/th&gt; &lt;td&gt;execution-a9a990f3-ff83-4c41-a894-631cee177614-2024.08.02&lt;/td&gt; &lt;/tr&gt; &lt;tr&gt; &lt;th&gt;Duration (ms)&lt;/th&gt; &lt;td&gt;41697&lt;/td&gt; &lt;/tr&gt; &lt;/table&gt;&lt;/p&gt;","DocType":null,"DocBase64":null}]}';
		String jsonStreamText = dataContext.getOutStreams()[0].getText();
		assert jsonOut.size() == jsonStreamText.size();
		def expectedJson = jsluper.parseText(jsonOut);
		def actualJson = jsluper.parseText(jsonStreamText);
		assert actualJson == expectedJson;
	}

	@Test
	void testFilterSortCombineNoLogging() {
		def dataContext = setupDataContextFromFolder("src/test/resources/com/boomi/psotoolkit/core/filtersortcreateauditlognotification");

		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_AUDITLOG_SIZE_MAX, "2000");
		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_DISABLE_NOTIFICATION, "1");
		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_DISABLE_AUDIT, "1");

		dataContext.getProperties(0).put(DDP_FWK_SORT_TS, "20240802 074619.486");
		dataContext.getProperties(1).put(DDP_FWK_SORT_TS, "20240802 074637.048");
		dataContext.getProperties(2).put(DDP_FWK_SORT_TS, "20240802 074639.902");

		dataContext.getProperties(0).put(DDP_FWK_LEVEL, "LOG");
		dataContext.getProperties(1).put(DDP_FWK_LEVEL, "LOG");
		dataContext.getProperties(2).put(DDP_FWK_LEVEL, "LOG");

		new CreateAuditLogNotification(dataContext).execute();

		JsonSlurper jsluper = new JsonSlurper();

		assert dataContext.getOutStreams().isEmpty();
	}
}