package com.taurus.core.util;

import java.io.File;

/**
 * 文件处理工具
 * @author daixiwei	daixiwei15@126.com
 *
 */
public class FileUtil {
	
	public static void delete(File file) {
		if (file != null && file.exists()) {
			if (file.isFile()) {
				file.delete();
			}
			else if (file.isDirectory()) {
				File files[] = file.listFiles();
				if (files != null) {
					for (int i=0; i<files.length; i++) {
						delete(files[i]);
					}
				}
				file.delete();
			}
		}
	}
	
	public static String getFileExtension(String fileFullName) {
	    if (StringUtil.isEmpty(fileFullName)) {
            throw new RuntimeException("fileFullName is empty");
        }
	    return  getFileExtension(new File(fileFullName));
	}
	
	public static String getFileExtension(File file) {
	    if (null == file) {
	        throw new NullPointerException();
	    }
	    String fileName = file.getName();
	    int dotIdx = fileName.lastIndexOf('.');
	    return (dotIdx == -1) ? StringUtil.Empty : fileName.substring(dotIdx + 1);
    }
}
