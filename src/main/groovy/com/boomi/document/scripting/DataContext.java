package com.boomi.document.scripting;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DataContext {
	private List<File> files;
	private List<InputStream> outStreams = new ArrayList<>();
	private int dataCount;
	private List<Properties> inProperties;
	private List<Properties> outProperties = new ArrayList<>();

	public DataContext(List<File> files, int dataCount, List<Properties> properties) {
		this.dataCount = dataCount;
		this.files = files;
		this.inProperties = properties;
	}

	public InputStream getStream(int i) {
		InputStream is = null;
		try {
			is = new BufferedInputStream(new FileInputStream(files.get(i)));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return is;
	}

	public int getDataCount() {
		return dataCount;
	}

	public Properties getProperties(int i) {
		return inProperties.get(i);
	}

	public void storeStream(InputStream is, Properties props) {
		outProperties.add(props);
		outStreams.add(is);
	}

	public List<Properties> getOutProperties() {
		return outProperties;
	}

	public List<InputStream> getOutStreams() {
		return outStreams;
	}
}
