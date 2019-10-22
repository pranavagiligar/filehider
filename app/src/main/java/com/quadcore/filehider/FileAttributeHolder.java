package com.quadcore.filehider;

public class FileAttributeHolder {
	private String fileName;
	private String path;
	private String currentPath;
	private String unique_id;
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public void setPath(String path) {
		this.path = path;
	}
	
	public void setUnique_id(String unique_id) {
		this.unique_id = unique_id;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public String getPath() {
		return path;
	}
	
	public String getUnique_id() {
		return unique_id;
	}

	public String getCurrentPath() {
		return currentPath;
	}

	public void setCurrentPath(String currentPath) {
		this.currentPath = currentPath;
	}
}
