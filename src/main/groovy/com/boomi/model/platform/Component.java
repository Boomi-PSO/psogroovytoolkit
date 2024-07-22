package com.boomi.model.platform;

public class Component {

	private FolderId folder = new FolderId();

	public FolderId getFolderId() {
		return folder;
	}

	public class FolderId {
		public String getName() {
			return "Mock/Folder/Testit";
		}
	}
}
