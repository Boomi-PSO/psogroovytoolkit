package com.boomi.psotoolkit.errorhospital

import com.boomi.execution.ExecutionUtil
import com.boomi.psotoolkit.BaseTests
import groovy.json.JsonSlurper
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

import static org.junit.jupiter.api.Assertions.fail;

class CreateTracksTests extends BaseTests {
	private static final String DEFAULT_DATE_FORMAT_PATTERN = "yyyyMMdd HHmmss.SSS";

	@BeforeEach
	void setUp() {
	}

	@AfterEach
	void tearDown() {
		ExecutionUtil.dynamicProcessProperties.clear();
	}

	@Test
	void testSuccessMulti() {
		def dataContext = setupDataContextFromFolder("src/test/resources/com/boomi/psotoolkit/errorhospital/createtracksmulti");

		new CreateTracks(dataContext).execute();

		JsonSlurper jsluper = new JsonSlurper();

		def actualJson = jsluper.parseText(dataContext.getOutStreams()[0].getText());
		assert actualJson.TrackCount == "12";
		assert actualJson.ErrorProcessesList.size() == 2;
		assert actualJson.FolderList.size() == 2;
		assert actualJson.TrackedFieldNames.size() == 2;
		assert actualJson.ErrorHospitalTracks[0].DurationInMillis == 133;
		assert actualJson.ErrorHospitalTracks[0].AlertCount == "0";
		assert actualJson.ErrorHospitalTracks[0].Status == "SUCCESS";
		actualJson.ErrorHospitalTracks[0].LogEntries.each { logEntry ->
			assert logEntry.Level == "TRACK"
		}
		commonTimestampVerifications(actualJson);
	}

	@Test
	void testSuccessAllCases() {
		def dataContext = setupDataContextFromFolder("src/test/resources/com/boomi/psotoolkit/errorhospital/createtracks");

		new CreateTracks(dataContext).execute();

		JsonSlurper jsluper = new JsonSlurper();

		def actualJson = jsluper.parseText(dataContext.getOutStreams()[0].getText());
		assert actualJson.TrackCount == "4";
		assert actualJson.ErrorProcessesList.size() == 3;
		assert actualJson.FolderList.size() == 3;
		assert actualJson.TrackedFieldNames.size() == 3;
		assert actualJson.ErrorHospitalTracks[0].DurationInMillis == 8445;
		assert actualJson.ErrorHospitalTracks[0].TrackingId == "7602435411494734444";
		assert actualJson.ErrorHospitalTracks[0].TrackedProcess == "Integration Toolkit Release/!Release (2024-07-04)/0-Common/03-Templates/fseq-MyLOB/fseq-MyEDIIntegration/fseq-MySrcAppMySrcObject-to-MyTradingPartnerMyEDIMessage EDI";
		assert actualJson.ErrorHospitalTracks[0].AlertCount == "0";
		assert actualJson.ErrorHospitalTracks[1].TrackingId == "511859245380735359";
		assert actualJson.ErrorHospitalTracks[1].Status == "SUCCESS_WITH_WARNING";
		assert actualJson.ErrorHospitalTracks[1].TrackedProcess == "INT-539/02020-Unit Tests";
		assert actualJson.ErrorHospitalTracks[1].AlertCount == "1";
		assert actualJson.ErrorHospitalTracks[2].TrackingId == "3868131820876138597";
		assert actualJson.ErrorHospitalTracks[2].TrackedProcess == "INT-539/02020-Unit Tests";
		assert actualJson.ErrorHospitalTracks[2].AlertCount == "1";
		assert actualJson.ErrorHospitalTracks[3].TrackingId == "134567890";
		assert actualJson.ErrorHospitalTracks[3].AlertCount == "2";
		assert actualJson.ErrorHospitalTracks[3].Status == "FAILED";
		assert actualJson.ErrorHospitalTracks[3].TrackedFields == "name#testit,TRUNCATED#111";
		assert actualJson.ErrorHospitalTracks[3].TrackedProcess == "Mock/It";
		commonTimestampVerifications(actualJson);
		assert actualJson.ErrorHospitalTracks[0].LogEntries[1].EDIFACT
		assert actualJson.ErrorHospitalTracks[0].LogEntries[1].EDIFACT.senderId == "SUORG";
		assert actualJson.ErrorHospitalTracks[0].LogEntries[1].EDIFACT.receiverId == "TECCOM";
	}

	void commonTimestampVerifications(def actualJson) {
		try {
			actualJson.ErrorHospitalTracks.each { errorTrack ->
				LocalDateTime.parse(errorTrack.Timestamp, DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT_PATTERN));
			}
		}
		catch (DateTimeParseException dtpe) {
			fail(dtpe.getMessage());
		}

		try {
			actualJson.ErrorHospitalTracks.each { errorTrack ->
				LocalDateTime.parse(errorTrack.LogEntries[0].Timestamp, DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT_PATTERN));
			}
		}
		catch (DateTimeParseException dtpe) {
			fail(dtpe.getMessage());
		}

		actualJson.ErrorHospitalTracks.each { errorTrack ->
			assert errorTrack.Timestamp == errorTrack.LogEntries[errorTrack.LogEntries.size() - 1].Timestamp;
		}
	}
}