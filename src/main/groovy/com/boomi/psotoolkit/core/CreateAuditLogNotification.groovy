package com.boomi.psotoolkit.core;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.Deflater;

import com.boomi.execution.ExecutionUtil;

import groovy.json.JsonBuilder;
import groovy.json.JsonSlurper;

/**
 * Description : This Groovy script sets dynamic process properties.
 *
 * Input:
 *       document "on the flow"
 *       DDP_DocSize - optional size in bytes of document
 *
 * Output:
 *       DPP_FWK_ProcessName - top level process name
 *       DPP_FWK_ProcessId   - top level process id
 *       DPP_FWK_ExecutionId - top level execution id
 *       DPP_FWK_PROCESS_ERROR - empty string
 *       DPP_FWK_DISABLE_NOTIFICATION - if not already set then default property
 *       DPP_FWK_DISABLE_AUDIT - if not already set then default property
 *       DPP_FWK_ENABLE_ERROR_TERM - if not already set then default property
 *       DPP_FWK_APIURL - derived from default DPPs
 *       DPP_FWK_inheader_<postscript> - derived from default DDPs
 *       DPP_FWK_TF_<key> = <value> - derived from DPP_FWK_TrackedFields
 * **************************************************************************
 **/
class CreateAuditLogNotification extends BaseCommand {
	// Constants
	private static final String SCRIPT_NAME = this.getSimpleName();
	private static final String DDP_DOCSIZE = "document.dynamic.userdefined.DDP_DocSize";
	// Setup global objects
	private int auditlogProcessContextSize;
	private String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
	private int auditlogSizeMax;

	public CreateAuditLogNotification(def dataContext) {
		super(dataContext);
	}

	@Override
	public void execute() {
		logScriptName(SCRIPT_NAME);
		// Get max size of audit log
		String auditlogSizeMaxString = ExecutionUtil.getDynamicProcessProperty("DPP_FWK_AUDITLOG_SIZE_MAX");
		auditlogSizeMax = (auditlogSizeMaxString && auditlogSizeMaxString.isInteger()) ? Integer.parseInt(auditlogSizeMaxString) : 9216;
		// Set up the Process Context Json Header
		def auditlogProcessContext = getAuditlogProcessContext();

		// for each document
		for (int i = 0; i < dataContext.getDataCount(); i++) {
			Properties props = dataContext.getProperties(i);
			// Parse audit log items
			def auditLogItems = new JsonSlurper().parse(dataContext.getStream(i));
			// get input document size from DDP else derive
			int docSize = 0;
			String docSizeProp = props.getProperty(DDP_DOCSIZE);
			if (docSizeProp) {
				docSize = docSizeProp.toInteger();
			}
			else {
				docSize = getJsonLength(auditLogItems);
			}
			// don't count leading curly bracket twice --> -1 on size
			int auditlogSize = auditlogProcessContextSize + docSize - 1;
			// store audit log items with process context
			if (auditlogSize <= auditlogSizeMax) {
				// Do not modify
				storeWithoutCompression(auditlogProcessContext, auditLogItems, props);
			}
			// Else Remove attached document and recalculate size - if exists
			else if ((auditlogSize = removeAttachedDocs(auditLogItems, auditlogSize)) <= auditlogSizeMax) {
				storeWithoutCompression(auditlogProcessContext, auditLogItems, props);
			}
			// Else compress and base64
			else if (!storeWithCompression(auditlogProcessContext, auditLogItems, props)) {
				// Else too big so error
				storeWithError(auditlogProcessContext, auditLogItems, props);
			}
		}
	}

	// return parsed audit log header - ProcessContext
	private def getAuditlogProcessContext() {
		JsonBuilder builder = new JsonBuilder();
		builder {
			ProcessContext {
				'TrackingId' ExecutionUtil.getDynamicProcessProperty("DPP_FWK_TrackingId")
				'TrackedFields' ExecutionUtil.getDynamicProcessProperty("DPP_FWK_TrackedFields")
				'Container' ExecutionUtil.getDynamicProcessProperty("DPP_FWK_ContainerId")
				'ExecutionId' ExecutionUtil.getDynamicProcessProperty("DPP_FWK_ExecutionId")
				'API' ExecutionUtil.getDynamicProcessProperty("DPP_FWK_APIURL")
				'MainProcessName' ExecutionUtil.getDynamicProcessProperty("DPP_FWK_ProcessName")
				'MainProcessComponentId' ExecutionUtil.getDynamicProcessProperty("DPP_FWK_ProcessId")
				'Folder' ExecutionUtil.getDynamicProcessProperty("DPP_FWK_Directory")
			}
		};
		// convert to string to get length
		String auditlogProcessContextJson = builder.toString();
		auditlogProcessContextSize = auditlogProcessContextJson.length();
		// return parsed json
		return new JsonSlurper().parseText(auditlogProcessContextJson);
	}
	// remove possible base64 docs form audit log
	private int removeAttachedDocs(def auditLogItems, int startSize) {
		// reduce audit log and re-calculate json size
		int endSize = startSize
		auditLogItems.Auditlogitem.each { auditlogitem ->
			String base64Doc = auditlogitem.remove("DocBase64");
			if (base64Doc) {
				// 15 chars for comma, attr name, quotes and semicolon --> ,"DocBase64":"..."
				endSize -= (base64Doc.length() + 15);
			}
		}
		return endSize;
	}
	// Calculate parsed Json size
	private int getJsonLength(def parsedJson) {
		JsonBuilder builder = new JsonBuilder(parsedJson);
		return builder.toString().length();
	}
	// Output Json Builder
	private void storeStreamJsonBuilder(JsonBuilder builder, Properties props) {
		storeStreamJson(builder.toString(), props);
	}
	// Output Json String
	private void storeStreamJson(String json, Properties props) {
		dataContext.storeStream(new ByteArrayInputStream(json.getBytes("UTF-8")), props);
	}

	// Output full audit log
	private void storeWithoutCompression(def processContext, def auditLogItems, Properties props) {
		JsonBuilder builder = new JsonBuilder();
		builder {
			ProcessContext processContext.ProcessContext
			Auditlogitem auditLogItems.Auditlogitem
		}
		storeStreamJsonBuilder(builder, props);
	}
	// output error
	private void storeWithError(def processContext, def auditLogItems, Properties props) {

		JsonBuilder builder = new JsonBuilder();
		// Create over sized audit log items
		builder {
			Auditlogitem auditLogItems.Auditlogitem
		}
		// Truncate to max size - header json and leading curly bracket
		String truncatedData = builder.toString().substring(0, (auditlogSizeMax - auditlogProcessContextSize - 1));
		// add truncate data node
		processContext.ProcessContext.put("TruncatedData", truncatedData);
		// create error json to store
		builder {
			ProcessContext processContext.ProcessContext
			Auditlogitem([
				{
					'Level' 'ERROR'
					'Timestamp' now
					'Step' 'Notification'
					'ErrorClass' 'InternalError'
				}
			])
		};
		storeStreamJsonBuilder(builder, props);
	}
	// zip and base64 encode and embed - output if small enough, otherwise return false
	private boolean storeWithCompression(def processContext, def auditLogItems, Properties props) {
		boolean result = false;
		JsonBuilder builder = new JsonBuilder();
		// build uncompressed json
		builder {
			ProcessContext processContext.ProcessContext
			Auditlogitem auditLogItems.Auditlogitem
		}
		// compress and base64
		String compressedAuditlog = compressEncode(new ByteArrayInputStream(builder.toString().getBytes("UTF-8")));
		// add compressed data node
		processContext.ProcessContext.put("CompressedData", compressedAuditlog);
		// create json with compression
		builder {
			ProcessContext processContext.ProcessContext
			Auditlogitem([
				{
					'Level' 'LOG'
					'Timestamp' now
				}
			])
		}
		// only store if small enough
		String json = builder.toString();
		logger.fine("size with compression=" + json.length() + " max size=" + auditlogSizeMax);
		if (json.length() <= auditlogSizeMax) {
			storeStreamJson(json, props);
			result = true;
		}
		else {
			// still too long, remove compression
			processContext.ProcessContext.remove("CompressedData");
		}
		return result;
	}
	// zip and base64encode
	private String compressEncode(ByteArrayInputStream is) {
		ByteArrayOutputStream input = new ByteArrayOutputStream();
		String base64EncodedOutput;
		try {
			byte[] b = new byte[4096];
			int n = 0;
			while ((n = is.read(b)) != -1) {
				input.write(b, 0, n);
			}
		} finally {
			input.close();
		}
		logger.fine("original document size: " + input.size());

		if (input.size() > 0) {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
			try {
				deflater.setInput(input.toByteArray());
				deflater.finish();
				byte[] buffer = new byte[1024];
				int count = 0;
				int defSize = 0;
				while (!deflater.finished()) {
					count = deflater.deflate(buffer);
					defSize += count;
					stream.write(buffer, 0, count);
				}
				logger.fine("compressed document size: " + defSize);
				logger.fine("compression factor: " + (input.size() / (float) defSize));
				byte[] outputbytes = new ByteArrayInputStream(stream.toByteArray()).getBytes();
				base64EncodedOutput = Base64.getEncoder().encodeToString(outputbytes);
			} finally {
				stream.close();
				deflater.end();
			}
		}
		return base64EncodedOutput;
	}
}