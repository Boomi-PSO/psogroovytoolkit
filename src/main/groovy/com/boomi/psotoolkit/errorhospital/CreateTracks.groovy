package com.boomi.psotoolkit.errorhospital

import com.boomi.psotoolkit.BaseCommand
import groovy.json.JsonSlurper
import groovy.json.StreamingJsonBuilder
import org.codehaus.groovy.runtime.ReverseListIterator

import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.zip.Inflater

class CreateTracks extends BaseCommand {
    // Constants
    private static final String SCRIPT_NAME = this.getSimpleName();
    private static final String UTF_8 = "UTF-8";
    private static final String EMPTY_JSON = "{}";
    private static final String DDP_FWK_NS_ATOM_ID = "document.dynamic.userdefined.DDP_FWK_NS_ATOM_ID";
    private static final String DDP_FWK_NS_ATOM_NAME = "document.dynamic.userdefined.DDP_FWK_NS_ATOM_NAME";
    private static final String DDP_FWK_NS_ENVIRONMENT = "document.dynamic.userdefined.DDP_FWK_NS_ENVIRONMENT";
    private static final String DDP_FWK_NS_ACCOUNT = "document.dynamic.userdefined.DDP_FWK_NS_ACCOUNT";
    private static final String LOG = "LOG";
    private static final String ERROR = "ERROR";
    private static final String EMAIL = "e-mail";
    private static final String FAILED = "FAILED";
    private static final String NOTIFIED = "NOTIFIED";
    private static final String SUCCESS = "SUCCESS";
    private static final String LEVEL = "Level";
    private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd'T'HHmmss.SSS'Z'"
    private static final String DEFAULT_DATE_FORMAT_PATTERN = "yyyyMMdd HHmmss.SSS"
    private static final String TIMESTAMP = 'Timestamp';
    private static final String DURATION = 'DurationInMillis';
    private static final String TRACKING_ID = 'TrackingId';
    private static final String TRACKED_PROCESS = 'TrackedProcess';
    private static final String TRACKED_FIELDS = 'TrackedFields';
    private static final String LOG_ENTRY_COUNT = 'LogEntryCount';
    private static final String STATUS = 'Status';
    private static final String LOG_ENTRIES = 'LogEntries';
    private static final String TRACK = 'TRACK';
    private static final String HAS_ERRORS = 'HasErrors';
    private static final String ERROR_MESSAGE = 'ErrorMsg';
    private static final String ERROR_CLASS = 'ErrorClass';
    private static final String ERROR_STATE = 'ErrorState';
    private static final String COMPLETED = 'COMPLETED';
    private static final String STEP_DETAILS = 'StepDetails';
    private static final String NO = 'N';
    private static final String YES = 'Y';
    private static final String INTERNAL_ID = 'InternalId';
    private static final String ACCOUNT_ID = 'AccountId';
    private static final String ENVIRONMENT = 'Environment';
    private static final String ATOM_ID = 'AtomId';
    private static final String DOCUMENT_TYPE = 'DocumentType';
    private static final String ATOM_NAME = 'AtomName';
    private static final String CONTAINER_ID = 'ContainerId';
    private static final String FOLDER = 'Folder';
    private static final String MAIN_PROCESS_NAME = 'MainProcessName';
    private static final String MAIN_PROCESS_ID = 'MainProcessId';
    private static final String API = 'API';
    private static final String EXECUTION_ID = 'ExecutionId';
    private static final String PROCESS_STEP = 'ProcessStep';
    private static final String PROCESS_CALL_STACK = 'ProcessCallStack';
    private static final String DOCUMENT_BASE64_DECODED = 'DocumentBase64Decoded';
    private static final String FORWARD_SLASH = '/';
    private static final String TRACKED_FIELDS_SPLIT_REGEX = ',|#';
    private static final String TRUNCATED_NOTIFICATION_WARNING = "WARNING: Error Message contains the truncated process notification!";

    CreateTracks(Object dataContext) {
        super(dataContext);
    }

    @Override
    void execute() {
        logScriptName(SCRIPT_NAME);
        // initialise variables
        JsonSlurper jSlurper = new JsonSlurper();
        Properties allProps = new Properties();
        Set errorProcessList = [] as Set;
        Set folderList = [] as Set;
        Set trackedFieldsList = [] as Set;
        List errorHospitalTracks = [];
        // Loop Event XML Objects
        for (int docNo = 0; docNo < dataContext.getDataCount(); docNo++) {
            Properties props = dataContext.getProperties(docNo);
            // Extract Json from Event Object
            def auditLog = jSlurper.parseText(getAuditLog(props, docNo));
            // If there is compressed Json, decompress and continue
            if (auditLog?.ProcessContext?.CompressedData) {
                auditLog = jSlurper.parseText(decompressDecode(auditLog.ProcessContext.CompressedData));
            }
            // Create List Objects
            errorProcessList.add(auditLog.ProcessContext.MainProcessName);
            folderList.add(getTrackedProcess(auditLog));
            addTrackedFieldNames(trackedFieldsList, auditLog.ProcessContext.TrackedFields);
            // If data was truncated handle differently
            if (auditLog?.ProcessContext?.TruncatedData) {
                errorHospitalTracks.add(buildTruncatedDataTrackEntry(auditLog, props));
            }
            // Process Audit Log
            else {
                errorHospitalTracks.add(buildStandardTrackEntry(auditLog, props));
            }
            allProps.putAll(props);
        }
        storeDocument(errorHospitalTracks, errorProcessList, folderList, trackedFieldsList, allProps);

    }
    // Store result in ouput stream
    private void storeDocument(List errorHospitalTracks, Set errorProcessList, Set folderList, Set trackedFieldsList, Properties allProps) {
        StringWriter json = new StringWriter();
        StreamingJsonBuilder jBuilder = new StreamingJsonBuilder(json);
        jBuilder {
            'TrackCount' errorHospitalTracks.size().toString()
            'ErrorProcessesList' errorProcessList.collect{['ProcessName': it]}
            'ErrorHospitalTracks' errorHospitalTracks
            'FolderList' folderList.collect{['FolderName': it]}
            'TrackedFieldNames' trackedFieldsList.collect{['FieldName': it]}
        }
        dataContext.storeStream(new ByteArrayInputStream(json.toString().getBytes(UTF_8)), allProps);
    }
    // Get the Audit log from input XML, extract global values into DDPs
    private String getAuditLog(Properties props, int docNo) {
        def xml = new XmlSlurper().parse(dataContext.getStream(docNo));
        String auditLog = xml.status[0].text();
        props.setProperty(DDP_FWK_NS_ATOM_ID, xml.atomId[0].text())
        props.setProperty(DDP_FWK_NS_ATOM_NAME, xml.atomName[0].text())
        props.setProperty(DDP_FWK_NS_ENVIRONMENT, xml.environment[0].text())
        props.setProperty(DDP_FWK_NS_ACCOUNT, xml.accountId[0].text())
        return auditLog ?: EMPTY_JSON;
    }
    // Build Truncated Data Error Track
    private Map buildTruncatedDataTrackEntry(def auditLog, Properties props) {
        Map trackEntry = [:];
        addTimestampAndDuration(auditLog, trackEntry);
        trackEntry.put(TRACKING_ID, auditLog.ProcessContext.TrackingId);
        trackEntry.put(TRACKED_FIELDS, auditLog.ProcessContext.TrackedFields);
        trackEntry.put(TRACKED_PROCESS, getTrackedProcess(auditLog));
        List<Map<String, String>> logEntries = [];
        Map<String, String> logEntry = [:] as Map;
        logEntry.put(INTERNAL_ID, auditLog.Auditlogitem[0].Id);
        LocalDateTime ldt = LocalDateTime.parse(auditLog.Auditlogitem[0].Timestamp, DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT_PATTERN));
        logEntry.put(TIMESTAMP, ldt.format(DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN)));
        logEntry.put(LEVEL, auditLog.Auditlogitem[0].Level);
        logEntry.put(HAS_ERRORS, YES);
        logEntry.put(ERROR_MESSAGE, auditLog.ProcessContext.TruncatedData);
        logEntry.put(ERROR_CLASS, auditLog.Auditlogitem[0].ErrorClass);
        logEntry.put(ERROR_STATE, COMPLETED);
        logEntry.put(PROCESS_STEP, auditLog.Auditlogitem[0].Step);
        logEntry.put(CONTAINER_ID, auditLog.ProcessContext.Container);
        logEntry.put(FOLDER, auditLog.ProcessContext.Folder);
        logEntry.put(MAIN_PROCESS_NAME, auditLog.ProcessContext.MainProcessName);
        logEntry.put(MAIN_PROCESS_ID, auditLog.ProcessContext.MainProcessComponentId);
        logEntry.put(API, auditLog.ProcessContext.API);
        logEntry.put(EXECUTION_ID, auditLog.ProcessContext.ExecutionId);
        logEntry.put(STEP_DETAILS, TRUNCATED_NOTIFICATION_WARNING);
        addPropertyDetails(props, logEntry);
        logEntries.add(logEntry.findAll {it.value});
        trackEntry.put(LOG_ENTRIES, logEntries);
        trackEntry.put(LOG_ENTRY_COUNT, logEntries.size().toString());
        trackEntry.put(STATUS, FAILED);
        return trackEntry;
    }
    // Build Standard Error Track
    private Map buildStandardTrackEntry(def auditLog, Properties props) {
        Map trackEntry = [:];
        addTimestampAndDuration(auditLog, trackEntry);
        trackEntry.put(TRACKING_ID, auditLog.ProcessContext.TrackingId);
        trackEntry.put(TRACKED_FIELDS, auditLog.ProcessContext.TrackedFields);
        trackEntry.put(TRACKED_PROCESS, getTrackedProcess(auditLog));
        List<Map<String, String>> logEntries = [];
        int emailStepCount = 0;
        int errorCount = 0;
        ReverseListIterator auditLogItems = new ReverseListIterator(auditLog.Auditlogitem);
        auditLogItems.each { auditLogItem ->
            Map<String, String> logEntry = [:] as Map;
            addAuditLogItemDetails(auditLogItem, logEntry);
            addProcessContextDetails(auditLog.ProcessContext, logEntry);
            addPropertyDetails(props,logEntry);
            logEntries.add(logEntry.findAll {it.value});
            if (auditLogItem.Level == ERROR) {
                errorCount++
            }
            if (auditLogItem?.Step?.toLowerCase() == EMAIL) {
                emailStepCount++;
            }
        }
        trackEntry.put(STATUS, getStatus(errorCount, emailStepCount));
        trackEntry.put(LOG_ENTRY_COUNT, logEntries.size().toString());
        trackEntry.put(LOG_ENTRIES, logEntries);
        return trackEntry;
    }
    // Tracked Process is derived form the Folder
    private String getTrackedProcess(def auditLog) {
        String trackedProcess = auditLog?.ProcessContext?.Folder;
        int nthIndex = nthIndexOf(trackedProcess, FORWARD_SLASH, 2)
        if (nthIndex > 0) {
            trackedProcess = trackedProcess.substring(nthIndex + 1)
        }
        return trackedProcess;
    }
    // Add Tracked field Names to Set
    private void addTrackedFieldNames(Set trackedFieldsList, String trackedFeids) {
        List trackedFieldsElements = trackedFeids?.split(TRACKED_FIELDS_SPLIT_REGEX);
        if (trackedFieldsElements) {
            Map keyValues =  trackedFieldsElements.indexed().findAll {index, value -> index % 2 == 0};
            trackedFieldsList.addAll(keyValues.values());
        }
    }
    // Get Status depending on number of errors and email steps
    private Object getStatus(int errorCount, int emailStepCount) {
        String status;
        if (errorCount > 0 && emailStepCount == 0) {
            status = FAILED;
        } else if (errorCount > 0 && emailStepCount > 0) {
            status = NOTIFIED;
        } else {
            status = SUCCESS;
        }
        return status;
    }
    // Add time related track fields
    private void addTimestampAndDuration(def auditLog, Map trackEntry) {
        int auditLogSize = auditLog?.Auditlogitem.size();
        if (auditLogSize) {
            String firstTimestamp = auditLog.Auditlogitem[0].Timestamp;
            String lastTimestamp = auditLog.Auditlogitem[auditLogSize - 1].Timestamp;
            LocalDateTime firstldt = LocalDateTime.parse(firstTimestamp, DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT_PATTERN));
            trackEntry.put(TIMESTAMP, firstldt.format(DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN)));
            LocalDateTime lastldt = LocalDateTime.parse(lastTimestamp, DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT_PATTERN));
            trackEntry.put(DURATION, Duration.between(firstldt, lastldt).toMillis());
        }
    }
    // Add Dynamic Document Property Fields
    private void addPropertyDetails(Properties props, Map<String, String> logEntry) {
        logEntry.put(ACCOUNT_ID, props.getProperty(DDP_FWK_NS_ACCOUNT));
        logEntry.put(ENVIRONMENT, props.getProperty(DDP_FWK_NS_ENVIRONMENT));
        logEntry.put(ATOM_ID, props.getProperty(DDP_FWK_NS_ATOM_ID));
        logEntry.put(ATOM_NAME, props.getProperty(DDP_FWK_NS_ATOM_NAME));
    }
    // Add Process Context Fields
    private void addProcessContextDetails(def processContext, Map<String, String> logEntry) {
        logEntry.put(CONTAINER_ID, processContext.Container);
        logEntry.put(FOLDER, processContext.Folder);
        logEntry.put(MAIN_PROCESS_NAME, processContext.MainProcessName);
        logEntry.put(MAIN_PROCESS_ID, processContext.MainProcessComponentId);
        logEntry.put(API, processContext.API);
        logEntry.put(EXECUTION_ID, processContext.ExecutionId);
    }
    // Add audit Log Item Fields
    private void addAuditLogItemDetails(def auditLogItem, Map<String, String> logEntry) {
        logEntry.put(INTERNAL_ID, auditLogItem.Id);
        LocalDateTime ldt = LocalDateTime.parse(auditLogItem.Timestamp, DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT_PATTERN));
        logEntry.put(TIMESTAMP, ldt.format(DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN)));
        if (LOG.equals(auditLogItem.get(LEVEL))) {
            logEntry.put(LEVEL, TRACK);
            logEntry.put(HAS_ERRORS, NO);
            logEntry.put(STEP_DETAILS, auditLogItem.Details);
        }
        else {
            logEntry.put(LEVEL, auditLogItem.Level);
            logEntry.put(HAS_ERRORS, YES);
            logEntry.put(ERROR_MESSAGE, auditLogItem.Details);
            logEntry.put(ERROR_CLASS, auditLogItem.ErrorClass);
            logEntry.put(ERROR_STATE, COMPLETED);
        }
        logEntry.put(DOCUMENT_TYPE, auditLogItem.DocType);
        if (auditLogItem.DocBase64) {
            logEntry.put(DOCUMENT_BASE64_DECODED, new String(Base64.getDecoder().decode(auditLogItem.DocBase64)));
        }
        logEntry.put(PROCESS_STEP, auditLogItem.Step);
        logEntry.put(PROCESS_CALL_STACK, auditLogItem.ProcessCallStack);
    }
    // Base64 decode then uncompress given string
    private String decompressDecode(String compressedEncoded) {
        byte[] compressedInput = Base64.getDecoder().decode(compressedEncoded);
        Inflater inflater = new Inflater();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        String decompressed;
        try {
            inflater.setInput(compressedInput);
            byte[] buffer = new byte[1024];
            while (!inflater.finished()) {
                int decompressedSize = inflater.inflate(buffer);
                outputStream.write(buffer, 0, decompressedSize);
            }
            decompressed = new String(outputStream.toByteArray());
        }
        finally {
            outputStream.close();
            inflater.end();
        }
        return decompressed;
    }
    // return nth index of given char sequence in input
    private int nthIndexOf(String input, String substring, int nth) {
        if (nth == 1) {
            return input?.indexOf(substring);
        } else {
            return input?.indexOf(substring, nthIndexOf(input, substring, nth - 1) + substring.length());
        }
    }
}
