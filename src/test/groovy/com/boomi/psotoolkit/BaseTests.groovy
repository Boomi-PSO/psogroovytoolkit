package com.boomi.psotoolkit

import com.boomi.document.scripting.DataContext
import com.boomi.execution.ExecutionUtil

class BaseTests {

	protected DataContext setupDataContextFromFolder(String folderName) {
		List filesInFolder = [];
		readFilesInFolder(filesInFolder, new File(folderName));
		List props = [];
		1.upto(filesInFolder.size(), {
			props.add(new Properties())
		})
		return new DataContext(filesInFolder, filesInFolder.size(), props);
	}

	protected void readFilesInFolder(List filesInFolder, File folder) {
		for (final File fileEntry : folder.listFiles().sort{it.name}) {
			println fileEntry.name
			if (fileEntry.isDirectory()) {
				readFilesInFolder(filesInFolder, fileEntry);
			} else {
				filesInFolder.add(fileEntry);
			}
		}
	}

	protected void printAll(def dataContext) {
		int propsIndex = 0;
		for (outStream in dataContext.getOutStreams()) {
			println outStream.getText("UTF-8");
			println "####################################################";
			Properties props = dataContext.getOutProperties()[propsIndex++];
			props.each{ key, value ->
				println key + "=" + value + "\n";
			}
		}
	}

	protected void printProcessProperties() {
		int propsIndex = 0;
		ExecutionUtil.dynamicProcessProperties.each { key, value ->
			println key + "=" + value;
		}
		println "#####################################################";
	}

	void printDocAndProperties(def dataContext) {
		int propsIndex = 0;
		for (outStream in dataContext.getOutStreams()) {
			println outStream.getText("UTF-8");
			println "################## LOCAL Props ##################################";
			Properties props = dataContext.getOutProperties()[propsIndex++];
			props.each{ key, value ->
				println key + "=" + value + "\n";
			}
		}
	}

	void printGlobalProperties() {
		println "################## GLOBAL Props ##################################";
		ExecutionUtil.dynamicProcessProperties.each { key, value ->
			println key + "=" + value + "\n";
		}
	}
}