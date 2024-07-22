package com.boomi.psotoolkit.core

import groovy.json.JsonSlurper
class ParseEnvelope {
	// Constants
	private final static String UTF8 = "UTF-8";
	// Setup global objects
	private def dataContext;

	public ParseEnvelope(def dataContext) {
		this.dataContext = dataContext;
	}

	public void execute() {
		for( int i = 0; i < dataContext.getDataCount(); i++ ) {
			Properties props = dataContext.getProperties(i);
			InputStream is = dataContext.getStream(i);

			def envelope = new JsonSlurper().parse(is, UTF8);
			envelope.EnvelopePayload.Multipart.each { String part ->
				dataContext.storeStream(new ByteArrayInputStream(part.decodeBase64()), props);
			}
		}
	}
}