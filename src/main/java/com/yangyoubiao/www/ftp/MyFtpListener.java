package com.yangyoubiao.www.ftp;

import it.sauronsoftware.ftp4j.FTPDataTransferListener;

/**
 * FTP监听器,做了简单实现，可以使用commons logger替换System.out.println
 */
public class MyFtpListener implements FTPDataTransferListener {
	private String tag;

	private MyFtpListener(String tags) {
		this.tag = tags;
	}

	public static MyFtpListener instance(String tags) {
		return new MyFtpListener(tags);
	}

	public void started() {
		System.out.println(tag + "：FTP启动。。。。。。");
	}

	public void transferred(int length) {
		System.out.println(tag + "：FTP传输[" + length + "]。。。。。。");
	}

	public void completed() {
		System.out.println(tag + "：FTP完成。。。。。。");
	}

	public void aborted() {
		System.out.println(tag + "：FTP中止。。。。。。");
	}

	public void failed() {
		System.out.println(tag + "：FTP挂掉。。。。。。");
	}
}