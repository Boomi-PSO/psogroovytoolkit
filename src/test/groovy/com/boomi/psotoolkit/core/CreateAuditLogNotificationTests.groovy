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

		String jsonOut = '{"ProcessContext":{"API":null,"Container":"1a945e1-bb16-443c-9ef0-d7f91bf342a8","ExecutionId":"e546a65d-52b4-4acb-9577-e368491c7009","Folder":"Mock/It","MainProcessComponentId":"2466a0cf-2951-4d60-8ef7-548416c414ea","MainProcessName":"Mock It","TrackedFields":"name#testit","TrackingId":"134567890"},"Auditlogitem":[{"Details":"Test it","DocBase64":"eyJERFBfRG9jdW1lbnRJZCI6IjY4MjUxNzcxNTQ5ODk3MDM3NDciLCJERFBfUHJldmlvdXNFdmVudElkIjoiNTQ0In0=","DocType":"json","Id":"1","Level":"LOG","Step":"Error","Timestamp":"20240701 094015.389"}]}';
		def expectedJson = jsluper.parseText(jsonOut);
		def actualJson = jsluper.parseText(dataContext.getOutStreams()[0].getText());

		assert actualJson == expectedJson;
	}

	@Test
	void testTrackedFieldsTooBig() {
		def dataContext = setupDataContextFromFolder("src/test/resources/com/boomi/psotoolkit/core/createauditlognotification");

		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_AUDITLOG_SIZE_MAX, "1000");
		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_TRACKEDFIELDS, "CountryCode#ABW,CountryCode#AFG,CountryCode#AGO,CountryCode#AIA,CountryCode#ALA,CountryCode#ALB,CountryCode#AND,CountryCode#ANT,CountryCode#ARE,CountryCode#ARG,CountryCode#ARM,CountryCode#ASC,CountryCode#ASM,CountryCode#ATA,CountryCode#ATF,CountryCode#ATG,CountryCode#AUS,CountryCode#AUT,CountryCode#AZE,CountryCode#BDI,CountryCode#BEL,CountryCode#BEN,CountryCode#BES,CountryCode#BFA,CountryCode#BGD,CountryCode#BGR,CountryCode#BHR,CountryCode#BHS,CountryCode#BIH,CountryCode#BLM,CountryCode#BLR,CountryCode#BLZ,CountryCode#BMU,CountryCode#BNO,CountryCode#BOL,CountryCode#BRA,CountryCode#BRB,CountryCode#BRN,CountryCode#BTN,CountryCode#BVT,CountryCode#BWA,CountryCode#CAF,CountryCode#CAN,CountryCode#CCK,CountryCode#CHE,CountryCode#CHI,CountryCode#CHL,CountryCode#CHN,CountryCode#CIV,CountryCode#CMR,CountryCode#COD,CountryCode#COG,CountryCode#COK,CountryCode#COL,CountryCode#COM,CountryCode#CPV,CountryCode#CRI,CountryCode#CUB,CountryCode#CUW,CountryCode#CXR,CountryCode#CYE,CountryCode#CYM,CountryCode#CYN,CountryCode#CYP,CountryCode#CZE,CountryCode#DEU,CountryCode#DJI,CountryCode#DMA,CountryCode#DNK,CountryCode#DOM,CountryCode#DZA,CountryCode#ECU,CountryCode#EGY,CountryCode#ENG,CountryCode#ERI,CountryCode#ESH,CountryCode#ESP,CountryCode#EST,CountryCode#ETH,CountryCode#FIN,CountryCode#FJI,CountryCode#FLK,CountryCode#FRA,CountryCode#FRO,CountryCode#FSM,CountryCode#GAB,CountryCode#GBR,CountryCode#GEO,CountryCode#GGY,CountryCode#GHA,CountryCode#GIB,CountryCode#GIN,CountryCode#GLP,CountryCode#GMB,CountryCode#GNB,CountryCode#GNQ,CountryCode#GRC,CountryCode#GRD,CountryCode#GRL,CountryCode#GTM,CountryCode#GUF,CountryCode#GUM,CountryCode#GUY,CountryCode#HKG,CountryCode#HMD,CountryCode#HND,CountryCode#HRV,CountryCode#HTI,CountryCode#HUN,CountryCode#ICC,CountryCode#IDN,CountryCode#IMN,CountryCode#IND,CountryCode#IOT,CountryCode#IRL,CountryCode#IRN,CountryCode#IRQ,CountryCode#ISL,CountryCode#ISR,CountryCode#ITA,CountryCode#JAM,CountryCode#JEY,CountryCode#JOR,CountryCode#JPN,CountryCode#KAZ,CountryCode#KEN,CountryCode#KGZ,CountryCode#KHM,CountryCode#KIR,CountryCode#KNA,CountryCode#KOR,CountryCode#KOS,CountryCode#KWT,CountryCode#LAO,CountryCode#LBN,CountryCode#LBR,CountryCode#LBY,CountryCode#LCA,CountryCode#LIE,CountryCode#LKA,CountryCode#LSO,CountryCode#LTU,CountryCode#LUX,CountryCode#LVA,CountryCode#MAC,CountryCode#MAF,CountryCode#MAR,CountryCode#MCO,CountryCode#MDA,CountryCode#MDG,CountryCode#MDV,CountryCode#MEX,CountryCode#MHL,CountryCode#MKD,CountryCode#MLI,CountryCode#MLT,CountryCode#MMR,CountryCode#MNE,CountryCode#MNG,CountryCode#MNP,CountryCode#MOZ,CountryCode#MRT,CountryCode#MSR,CountryCode#MTQ,CountryCode#MUS,CountryCode#MWI,CountryCode#MYS,CountryCode#MYT,CountryCode#NAM,CountryCode#NAP,CountryCode#NCL,CountryCode#NER,CountryCode#NFK,CountryCode#NGA,CountryCode#NIC,CountryCode#NIR,CountryCode#NIU,CountryCode#NLD,CountryCode#NOR,CountryCode#NPL,CountryCode#NRE,CountryCode#NRU,CountryCode#NZL,CountryCode#OMN,CountryCode#OS,CountryCode#PAK,CountryCode#PAN,CountryCode#PCN,CountryCode#PER,CountryCode#PHL,CountryCode#PLW,CountryCode#PNG,CountryCode#POL,CountryCode#PRI,CountryCode#PRK,CountryCode#PRT,CountryCode#PRY,CountryCode#PSE,CountryCode#PYF,CountryCode#QAT,CountryCode#REU,CountryCode#ROU,CountryCode#RUS,CountryCode#RWA,CountryCode#SAU,CountryCode#SBR,CountryCode#SCT,CountryCode#SDN,CountryCode#SEN,CountryCode#SGP,CountryCode#SGS,CountryCode#SHN,CountryCode#SJM,CountryCode#SLB,CountryCode#SLE,CountryCode#SLV,CountryCode#SMR,CountryCode#SOM,CountryCode#SPM,CountryCode#SRB,CountryCode#SSD,CountryCode#STP,CountryCode#SUN,CountryCode#SUR,CountryCode#SVK,CountryCode#SVN,CountryCode#SWE,CountryCode#SWZ,CountryCode#SXM,CountryCode#SYC,CountryCode#SYR,CountryCode#TCA,CountryCode#TCD,CountryCode#TGO,CountryCode#THA,CountryCode#TJK,CountryCode#TKL,CountryCode#TKM,CountryCode#TLS,CountryCode#TON,CountryCode#TTO,CountryCode#TUN,CountryCode#TUR,CountryCode#TUV,CountryCode#TWN,CountryCode#TZA,CountryCode#UGA,CountryCode#UKR,CountryCode#UMI,CountryCode#URY,CountryCode#USA,CountryCode#UZB,CountryCode#VAT,CountryCode#VCT,CountryCode#VEN,CountryCode#VGB,CountryCode#VIR,CountryCode#VNM,CountryCode#VUT,CountryCode#WAL,CountryCode#WLF,CountryCode#WSM,CountryCode#XYZ,CountryCode#YEM,CountryCode#YUG,CountryCode#ZAF,CountryCode#ZMB,CountryCode#ZWE");

		new CreateAuditLogNotification(dataContext).executeWithoutFilterSortCombine();

		JsonSlurper jsluper = new JsonSlurper();

		String jsonOut = '{"ProcessContext":{"API":null,"Container":"1a945e1-bb16-443c-9ef0-d7f91bf342a8","ExecutionId":"e546a65d-52b4-4acb-9577-e368491c7009","Folder":"Mock/It","MainProcessComponentId":"2466a0cf-2951-4d60-8ef7-548416c414ea","MainProcessName":"Mock It","TrackedFields":"CountryCode#ABW,CountryCode#AFG,CountryCode#AGO,CountryCode#AIA,CountryCode#ALA,CountryCode#ALB,CountryCode#AND,CountryCode#ANT,CountryCode#ARE,CountryCode#ARG,CountryCode#ARM,CountryCode#ASC,CountryCode#ASM,TRUNCATED#4286","TrackingId":"134567890"},"Auditlogitem":[{"Details":"Test it","DocBase64":"eyJERFBfRG9jdW1lbnRJZCI6IjY4MjUxNzcxNTQ5ODk3MDM3NDciLCJERFBfUHJldmlvdXNFdmVudElkIjoiNTQ0In0=","DocType":"json","Id":"1","Level":"LOG","Step":"Error","Timestamp":"20240701 094015.389"}]}';
		def expectedJson = jsluper.parseText(jsonOut);
		def actualJson = jsluper.parseText(dataContext.getOutStreams()[0].getText());
		assert actualJson == expectedJson;
	}

	@Test
	void testTrackedFieldsAndItemsTooBig() {
		def dataContext = setupDataContextFromFolder("src/test/resources/com/boomi/psotoolkit/core/filtersortcreateauditlognotification");

		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_AUDITLOG_SIZE_MAX, "450");
		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_TRACKEDFIELDS, "CountryCode#ABW,CountryCode#AFG,CountryCode#AGO,CountryCode#AIA,CountryCode#ALA,CountryCode#ALB,CountryCode#AND,CountryCode#ANT,CountryCode#ARE,CountryCode#ARG,CountryCode#ARM,CountryCode#ASC,CountryCode#ASM,CountryCode#ATA,CountryCode#ATF,CountryCode#ATG,CountryCode#AUS,CountryCode#AUT,CountryCode#AZE,CountryCode#BDI,CountryCode#BEL,CountryCode#BEN,CountryCode#BES,CountryCode#BFA,CountryCode#BGD,CountryCode#BGR,CountryCode#BHR,CountryCode#BHS,CountryCode#BIH,CountryCode#BLM,CountryCode#BLR,CountryCode#BLZ,CountryCode#BMU,CountryCode#BNO,CountryCode#BOL,CountryCode#BRA,CountryCode#BRB,CountryCode#BRN,CountryCode#BTN,CountryCode#BVT,CountryCode#BWA,CountryCode#CAF,CountryCode#CAN,CountryCode#CCK,CountryCode#CHE,CountryCode#CHI,CountryCode#CHL,CountryCode#CHN,CountryCode#CIV,CountryCode#CMR,CountryCode#COD,CountryCode#COG,CountryCode#COK,CountryCode#COL,CountryCode#COM,CountryCode#CPV,CountryCode#CRI,CountryCode#CUB,CountryCode#CUW,CountryCode#CXR,CountryCode#CYE,CountryCode#CYM,CountryCode#CYN,CountryCode#CYP,CountryCode#CZE,CountryCode#DEU,CountryCode#DJI,CountryCode#DMA,CountryCode#DNK,CountryCode#DOM,CountryCode#DZA,CountryCode#ECU,CountryCode#EGY,CountryCode#ENG,CountryCode#ERI,CountryCode#ESH,CountryCode#ESP,CountryCode#EST,CountryCode#ETH,CountryCode#FIN,CountryCode#FJI,CountryCode#FLK,CountryCode#FRA,CountryCode#FRO,CountryCode#FSM,CountryCode#GAB,CountryCode#GBR,CountryCode#GEO,CountryCode#GGY,CountryCode#GHA,CountryCode#GIB,CountryCode#GIN,CountryCode#GLP,CountryCode#GMB,CountryCode#GNB,CountryCode#GNQ,CountryCode#GRC,CountryCode#GRD,CountryCode#GRL,CountryCode#GTM,CountryCode#GUF,CountryCode#GUM,CountryCode#GUY,CountryCode#HKG,CountryCode#HMD,CountryCode#HND,CountryCode#HRV,CountryCode#HTI,CountryCode#HUN,CountryCode#ICC,CountryCode#IDN,CountryCode#IMN,CountryCode#IND,CountryCode#IOT,CountryCode#IRL,CountryCode#IRN,CountryCode#IRQ,CountryCode#ISL,CountryCode#ISR,CountryCode#ITA,CountryCode#JAM,CountryCode#JEY,CountryCode#JOR,CountryCode#JPN,CountryCode#KAZ,CountryCode#KEN,CountryCode#KGZ,CountryCode#KHM,CountryCode#KIR,CountryCode#KNA,CountryCode#KOR,CountryCode#KOS,CountryCode#KWT,CountryCode#LAO,CountryCode#LBN,CountryCode#LBR,CountryCode#LBY,CountryCode#LCA,CountryCode#LIE,CountryCode#LKA,CountryCode#LSO,CountryCode#LTU,CountryCode#LUX,CountryCode#LVA,CountryCode#MAC,CountryCode#MAF,CountryCode#MAR,CountryCode#MCO,CountryCode#MDA,CountryCode#MDG,CountryCode#MDV,CountryCode#MEX,CountryCode#MHL,CountryCode#MKD,CountryCode#MLI,CountryCode#MLT,CountryCode#MMR,CountryCode#MNE,CountryCode#MNG,CountryCode#MNP,CountryCode#MOZ,CountryCode#MRT,CountryCode#MSR,CountryCode#MTQ,CountryCode#MUS,CountryCode#MWI,CountryCode#MYS,CountryCode#MYT,CountryCode#NAM,CountryCode#NAP,CountryCode#NCL,CountryCode#NER,CountryCode#NFK,CountryCode#NGA,CountryCode#NIC,CountryCode#NIR,CountryCode#NIU,CountryCode#NLD,CountryCode#NOR,CountryCode#NPL,CountryCode#NRE,CountryCode#NRU,CountryCode#NZL,CountryCode#OMN,CountryCode#OS,CountryCode#PAK,CountryCode#PAN,CountryCode#PCN,CountryCode#PER,CountryCode#PHL,CountryCode#PLW,CountryCode#PNG,CountryCode#POL,CountryCode#PRI,CountryCode#PRK,CountryCode#PRT,CountryCode#PRY,CountryCode#PSE,CountryCode#PYF,CountryCode#QAT,CountryCode#REU,CountryCode#ROU,CountryCode#RUS,CountryCode#RWA,CountryCode#SAU,CountryCode#SBR,CountryCode#SCT,CountryCode#SDN,CountryCode#SEN,CountryCode#SGP,CountryCode#SGS,CountryCode#SHN,CountryCode#SJM,CountryCode#SLB,CountryCode#SLE,CountryCode#SLV,CountryCode#SMR,CountryCode#SOM,CountryCode#SPM,CountryCode#SRB,CountryCode#SSD,CountryCode#STP,CountryCode#SUN,CountryCode#SUR,CountryCode#SVK,CountryCode#SVN,CountryCode#SWE,CountryCode#SWZ,CountryCode#SXM,CountryCode#SYC,CountryCode#SYR,CountryCode#TCA,CountryCode#TCD,CountryCode#TGO,CountryCode#THA,CountryCode#TJK,CountryCode#TKL,CountryCode#TKM,CountryCode#TLS,CountryCode#TON,CountryCode#TTO,CountryCode#TUN,CountryCode#TUR,CountryCode#TUV,CountryCode#TWN,CountryCode#TZA,CountryCode#UGA,CountryCode#UKR,CountryCode#UMI,CountryCode#URY,CountryCode#USA,CountryCode#UZB,CountryCode#VAT,CountryCode#VCT,CountryCode#VEN,CountryCode#VGB,CountryCode#VIR,CountryCode#VNM,CountryCode#VUT,CountryCode#WAL,CountryCode#WLF,CountryCode#WSM,CountryCode#XYZ,CountryCode#YEM,CountryCode#YUG,CountryCode#ZAF,CountryCode#ZMB,CountryCode#ZWE");
		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_DISABLE_NOTIFICATION, "0");
		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_DISABLE_AUDIT, "0");
		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_ERROR_LEVEL, "true");
		dataContext.getProperties(0).put(DDP_FWK_SORT_TS, "2024-07-02T14:17:56.541Z");
		dataContext.getProperties(1).put(DDP_FWK_SORT_TS, "2024-07-02T14:17:52.944Z");
		dataContext.getProperties(2).put(DDP_FWK_SORT_TS, "2024-07-02T14:17:54.823Z");

		new CreateAuditLogNotification(dataContext).execute();

		JsonSlurper jsluper = new JsonSlurper();

		String jsonOut = '{"ProcessContext":{"API":null,"Container":"1a945e1-bb16-443c-9ef0-d7f91bf342a8","ExecutionId":"e546a65d-52b4-4acb-9577-e368491c7009","Folder":"Mock/It","MainProcessComponentId":"2466a0cf-2951-4d60-8ef7-548416c414ea","MainProcessName":"Mock It","TrackedFields":"CountryCode#ABW,CountryCode#AFG,CountryCode#AGO,CountryCode#AIA,CountryCode#ALA,CountryCode#ALB,TRUNCATED#4286","TrackingId":"134567890"},"Auditlogitem":[{"Level":"ERROR","Step":"Notification","ErrorClass":"InternalError"}]}';
		def expectedJson = jsluper.parseText(jsonOut);
		def actualJson = jsluper.parseText(dataContext.getOutStreams()[0].getText());
		String ts = actualJson.Auditlogitem[0].remove('Timestamp');
		assert ts != null;
		String truncatedData =  actualJson.ProcessContext.remove('TruncatedData');
		assert truncatedData.length() == 50;
		assert actualJson == expectedJson;
	}

	@Test
	void testTrackedFieldsTruncateAll() {
		def dataContext = setupDataContextFromFolder("src/test/resources/com/boomi/psotoolkit/core/filtersortcreateauditlognotification");

		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_AUDITLOG_SIZE_MAX, "450");
		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_TRACKEDFIELDS, "CountryCodeABWCountryCodeAFGCountryCodeAGOCountryCodeAIACountryCodeALACountryCodeALBCountryCodeAND#Truncate All");
		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_DISABLE_NOTIFICATION, "0");
		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_DISABLE_AUDIT, "0");
		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_ERROR_LEVEL, "true");
		dataContext.getProperties(0).put(DDP_FWK_SORT_TS, "2024-07-02T14:17:56.541Z");
		dataContext.getProperties(1).put(DDP_FWK_SORT_TS, "2024-07-02T14:17:52.944Z");
		dataContext.getProperties(2).put(DDP_FWK_SORT_TS, "2024-07-02T14:17:54.823Z");

		new CreateAuditLogNotification(dataContext).execute();

		JsonSlurper jsluper = new JsonSlurper();

		String jsonOut = '{"ProcessContext":{"API":null,"Container":"1a945e1-bb16-443c-9ef0-d7f91bf342a8","ExecutionId":"e546a65d-52b4-4acb-9577-e368491c7009","Folder":"Mock/It","MainProcessComponentId":"2466a0cf-2951-4d60-8ef7-548416c414ea","MainProcessName":"Mock It","TrackedFields":"TRUNCATED#111","TrackingId":"134567890"},"Auditlogitem":[{"Level":"ERROR","Step":"Notification","ErrorClass":"InternalError"}]}';
		def expectedJson = jsluper.parseText(jsonOut);
		def actualJson = jsluper.parseText(dataContext.getOutStreams()[0].getText());
		String ts = actualJson.Auditlogitem[0].remove('Timestamp');
		assert ts != null;
		String truncatedData =  actualJson.ProcessContext.remove('TruncatedData');
		assert truncatedData.length() == 147;
		assert actualJson == expectedJson;
	}

	@Test
	void testSuccessAndError() {
		def dataContext = setupDataContextFromFolder("src/test/resources/com/boomi/psotoolkit/core/createauditlognotification");

		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_AUDITLOG_SIZE_MAX, "450");
		LocalDateTime now = LocalDateTime.now();

		new CreateAuditLogNotification(dataContext).executeWithoutFilterSortCombine();

		JsonSlurper jsluper = new JsonSlurper();

		String jsonOut = '{"ProcessContext":{"API":null,"Container":"1a945e1-bb16-443c-9ef0-d7f91bf342a8","ExecutionId":"e546a65d-52b4-4acb-9577-e368491c7009","Folder":"Mock/It","MainProcessComponentId":"2466a0cf-2951-4d60-8ef7-548416c414ea","MainProcessName":"Mock It","TrackedFields":"name#testit","TrackingId":"134567890"},"Auditlogitem":[{"Details":"Test it","DocType":"json","Id":"1","Level":"LOG","Step":"Error","Timestamp":"20240701 094015.389"}]}';
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

		String jsonOut = '{"ProcessContext":{"API":null,"Container":"1a945e1-bb16-443c-9ef0-d7f91bf342a8","ExecutionId":"e546a65d-52b4-4acb-9577-e368491c7009","Folder":"Mock/It","MainProcessComponentId":"2466a0cf-2951-4d60-8ef7-548416c414ea","MainProcessName":"Mock It","TrackedFields":"name#testit","TrackingId":"134567890"},"Auditlogitem":[{"Details":"Test it","DocBase64":"eyJERFBfRG9jdW1lbnRJZCI6IjY4MjUxNzcxNTQ5ODk3MDM3NDciLCJERFBfUHJldmlvdXNFdmVudElkIjoiNTQ0In0=","DocType":"json","Id":"1","Level":"LOG","Step":"Error","Timestamp":"20240701 094015.389"}]}';
		def expectedJson = jsluper.parseText(jsonOut);
		def actualJson = jsluper.parseText(dataContext.getOutStreams()[0].getText());

		assert actualJson == expectedJson;

		jsonOut = '{"ProcessContext":{"API":null,"Container":"1a945e1-bb16-443c-9ef0-d7f91bf342a8","ExecutionId":"e546a65d-52b4-4acb-9577-e368491c7009","Folder":"Mock/It","MainProcessComponentId":"2466a0cf-2951-4d60-8ef7-548416c414ea","MainProcessName":"Mock It","TrackedFields":"name#testit","TrackingId":"134567890"},"Auditlogitem":[{"Details":"Test it","DocType":"json","Id":"1","Level":"LOG","Step":"Error","Timestamp":"20240701 094015.389"},{"Details":"Test it again","DocType":"json","Id":"2","Level":"LOG","Step":"Warning","Timestamp":"20240701 094016.389"}]}'
		expectedJson = jsluper.parseText(jsonOut);
		actualJson = jsluper.parseText(dataContext.getOutStreams()[1].getText());

		assert actualJson == expectedJson;
	}

	@Test
	void testSuccessAndCompress() {
		def dataContext = setupDataContextFromFolder("src/test/resources/com/boomi/psotoolkit/core/createauditlognotification");

		ExecutionUtil.dynamicProcessProperties.put(DPP_FWK_AUDITLOG_SIZE_MAX, "915");
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