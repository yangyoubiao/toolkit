package com.yangyoubiao.www.file;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class FileUtils {

	/**
	 * 将文件的编码由gbk转为utf-8<br>
	 * 转换之后，将文件保存到原有路径下，文件名加后缀temp
	 * 
	 * @param path gbk编码格式的文件所在路径
	 */
	private String gbkToUtf8(String path) throws IOException {
		String tempPath = path + ".temp";
		InputStreamReader isr = new InputStreamReader(new FileInputStream(path), "gbk");
		OutputStreamWriter isw = new OutputStreamWriter(new FileOutputStream(tempPath), "utf-8");
		int len = 0;
		char ch[] = new char[1024 * 1024];
		while ((len = isr.read(ch)) != -1) {
			isw.write(ch, 0, len);
			isw.flush();
		}
		isw.flush();
		isr.close();
		isw.close();
		return tempPath;
	}

}
