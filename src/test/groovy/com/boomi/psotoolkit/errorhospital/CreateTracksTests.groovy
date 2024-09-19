package com.boomi.psotoolkit.errorhospital

import com.boomi.execution.ExecutionUtil
import com.boomi.psotoolkit.BaseTests
import groovy.json.JsonSlurper
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CreateTracksTests extends BaseTests {

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
		assert actualJson.ErrorHospitalTracks[0].DurationInMillis == 106;
		assert actualJson.ErrorHospitalTracks[0].LogEntryCount == "3";
		assert actualJson.ErrorHospitalTracks[0].Status == "SUCCESS";
		actualJson.ErrorHospitalTracks[0].LogEntries.each { logEntry ->
			assert logEntry.Level == "TRACK"
		}
	}

	@Test
	void testSuccessAllCases() {
		def dataContext = setupDataContextFromFolder("src/test/resources/com/boomi/psotoolkit/errorhospital/createtracks");

		new CreateTracks(dataContext).execute();

		JsonSlurper jsluper = new JsonSlurper();

		def actualJson = jsluper.parseText(dataContext.getOutStreams()[0].getText());
		assert actualJson.TrackCount == "4";
		assert actualJson.ErrorProcessesList.size() == 2;
		assert actualJson.FolderList.size() == 2;
		assert actualJson.TrackedFieldNames.size() == 3;
		assert actualJson.ErrorHospitalTracks[0].DurationInMillis == 2680;
		assert actualJson.ErrorHospitalTracks[0].LogEntryCount == "3";
		assert actualJson.ErrorHospitalTracks[1].LogEntryCount == "3";
		assert actualJson.ErrorHospitalTracks[2].LogEntryCount == "5";
		assert actualJson.ErrorHospitalTracks[3].LogEntryCount == "1";
		assert actualJson.ErrorHospitalTracks[3].Status == "FAILED";
	}
}