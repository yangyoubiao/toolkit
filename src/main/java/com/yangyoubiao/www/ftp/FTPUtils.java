package com.yangyoubiao.www.ftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPFile;

/**
 * TTP客户端工具
 * 
 */
public final class FTPUtils {
	private final Logger logger = Logger.getLogger(FTPUtils.class);

	/**
	 * 工具类，不允许实例化
	 */
	private FTPUtils() {
	}

	/**
	 * 创建FTP连接
	 * 
	 * @param host     主机名或IP
	 * @param port     ftp端口
	 * @param username ftp用户名
	 * @param password ftp密码
	 * @return 一个单例客户端
	 * @throws Exception
	 */
	public static FTPClient ftpConn(String host, int port, String charset, String username, String password)
			throws Exception {
		FTPClient client = new FTPClient();
		client.connect(host, port);
		client.login(username, password);
		client.setCharset(charset);
		client.setType(FTPClient.TYPE_BINARY);
		return client;
	}

	/**
	 * 关闭FTP连接，关闭时候像服务器发送一条关闭命令
	 * 
	 * @param client FTP客户端
	 * @return 关闭成功，或者链接已断开，或者链接为null时候返回true，通过两次关闭都失败时候返回false
	 */
	public static boolean closeConn(FTPClient client) {
		if (client == null)
			return true;
		if (client.isConnected()) {
			try {
				client.disconnect(true);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				try {
					client.disconnect(false);
				} catch (Exception e1) {
					e.printStackTrace();
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 取得目录下所有文件列表
	 * 
	 * @param path
	 * @return
	 */
	public static List<FTPFile> getlistFiles(FTPClient client, String path) {
		List<FTPFile> filesList = new ArrayList<FTPFile>();
		try {
			client.changeDirectory(path);
			FTPFile[] fileNames = client.list(); // .listNames();
			if (null != fileNames) {
				for (FTPFile file : fileNames) {
					if (file.getType() == FTPFile.TYPE_FILE) {
						filesList.add(file);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return filesList;
	}

	/**
	 * 取得目录下所有文件列表
	 * 
	 * @param path
	 * @return
	 */
	public static List<String> getlistFileNames(FTPClient client, String path) {
		List<String> filesList = new ArrayList<String>();
		try {
			client.changeDirectory(path);
			String[] fileNames = client.listNames();
			if (null != fileNames) {
				for (String file : fileNames) {
					filesList.add(file);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return filesList;
	}

	/**
	 * 根据文件路径获取文件流
	 * 
	 * @param path
	 * @return
	 */
	public static InputStream fetchInputStream(FTPClient client, String ftpFilePath, String ftpPath) {
		InputStream is = null;
		try {
			String localFilePath = getFileName("ftptmp/" + ftpFilePath) + "_ftp.tmp";
			File tempLocalFile = new File(localFilePath);
			if (!tempLocalFile.exists()) {
				tempLocalFile.createNewFile();
			}

			client.download(ftpPath, tempLocalFile);
			is = new FileInputStream(tempLocalFile);

			// 删除临时文件(由于不能关闭文件流，删除临时文件无效,所以最好使用deleteOnExit，外层程序使用文件流结束后，关闭流，自动删除)
			// tempLocalFile.deleteOnExit();
		} catch (Exception e) {
			System.out.println("###[Error] FTPToolkit.fetchInputStream()" + e.getMessage());
		}
		return is;
	}

	// 递归创建层级目录（坑爹的问题，原本想着递归处理，后来才发现如此的简单）
	public static void mkDirs(FTPClient client, String p) throws Exception {
		if (null == p) {
			return;
		}

		if (p != null && !"".equals(p) && !"/".equals(p)) {
			String ps = "";
			for (int i = 0; i < p.split("/").length; i++) {
				ps += p.split("/")[i] + "/";
				if (!isDirExist(client, ps)) {
					client.createDirectory(ps);// 创建目录
					client.changeDirectory(ps);// 进入创建的目录
					System.out.println(">>>>> create directory:[" + i + "][" + ps + "]");
				} else {
					// System.out.println("select directory:["+i+"]["+ps+"]");
				}
			}
		}
	}

	// 检查目录是否存在
	private static boolean isDirExist(FTPClient client, String dir) {
		try {
			client.changeDirectory(dir);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * FTP上传本地文件到FTP的一个目录下
	 * 
	 * @param client           FTP客户端
	 * @param localfilepath    本地文件路径
	 * @param remoteFolderPath FTP上传目录
	 * @throws Exception
	 */
	public static void upload(FTPClient client, String localfilepath, String remoteFolderPath) throws Exception {
		mkDirs(client, remoteFolderPath);
		File localfile = new File(localfilepath);
		upload(client, localfile, remoteFolderPath);
	}

	/**
	 * FTP上传本地文件到FTP的一个目录下
	 * 
	 * @param client           FTP客户端
	 * @param localfile        本地文件
	 * @param remoteFolderPath FTP上传目录
	 */
	public static void upload(FTPClient client, File localfile, String remoteFolderPath) throws Exception {
		remoteFolderPath = formatPath4FTP(remoteFolderPath);
		MyFtpListener listener = MyFtpListener.instance("upload");
		client.changeDirectory("/");
		client.changeDirectory(remoteFolderPath);
		if (listener != null) {
			client.upload(localfile, listener);
		} else {
			client.upload(localfile);
		}
		client.changeDirectory("/");
	}

	/**
	 * 批量上传本地文件到FTP指定目录上
	 * 
	 * @param client           FTP客户端
	 * @param localFilePaths   本地文件路径列表
	 * @param remoteFolderPath FTP上传目录
	 */
	public static void uploadListPath(FTPClient client, List<String> localFilePaths, String remoteFolderPath) {
		try {
			remoteFolderPath = formatPath4FTP(remoteFolderPath);
			client.changeDirectory(remoteFolderPath);
			MyFtpListener listener = MyFtpListener.instance("uploadListPath");
			for (String path : localFilePaths) {
				File file = new File(path);
				if (listener != null) {
					client.upload(file, listener);
				} else {
					client.upload(file);
				}
			}
			client.changeDirectory("/");
		} catch (Exception e) {
			System.out.println("###[Error] FTPToolkit.uploadListPath()" + e.getMessage());
		}
	}

	/**
	 * 批量上传本地文件到FTP指定目录上
	 * 
	 * @param client           FTP客户端
	 * @param localFiles       本地文件列表
	 * @param remoteFolderPath FTP上传目录
	 */
	public static void uploadListFile(FTPClient client, List<File> localFiles, String remoteFolderPath) {
		try {
			client.changeDirectory(remoteFolderPath);
			MyFtpListener listener = MyFtpListener.instance("uploadListFile");
			for (File file : localFiles) {
				if (listener != null) {
					client.upload(file, listener);
				} else {
					client.upload(file);
				}
			}
			client.changeDirectory("/");
		} catch (Exception e) {
			System.out.println("###[Error] FTPToolkit.uploadListFile() " + e.getMessage());
		}
	}

	/**
	 * 判断一个FTP路径是否存在，如果存在返回类型(FTPFile.TYPE_DIRECTORY=1、FTPFile.TYPE_FILE=0、FTPFile.TYPE_LINK=2)
	 * 
	 * @param client     FTP客户端
	 * @param remotePath FTP文件或文件夹路径
	 * @return 存在时候返回类型值(文件0，文件夹1，连接2)，不存在则返回-1
	 */
	public static int isExist(FTPClient client, String remotePath) {
		int x = -1;
		try {
			remotePath = formatPath4FTP(remotePath);

			FTPFile[] list = client.list(remotePath);
			if (list.length > 1) {
				x = 1;
			} else if (list.length == 1) {
				FTPFile f = list[0];
				if (f.getType() == FTPFile.TYPE_DIRECTORY) {
					x = 1;
				}
				// 假设推理判断
				String _path = remotePath + "/" + f.getName();
				if (client.list(_path).length == 1) {
					x = 1;
				} else {
					x = 0;
				}
			} else {
				client.changeDirectory(remotePath);
				x = 1;
			}
		} catch (Exception e) {
			x = -1;
			System.out.println("###[Error] FTPToolkit.isExist() " + e.getMessage());
		}

		return x;
	}

	/**
	 * FTP 下载文件
	 * 
	 * @param client
	 * @param remoteFolderPath
	 * @param localfile
	 */
	public static void download(FTPClient client, String remoteFileName, File localfile) {
		remoteFileName = formatPath4FTP(remoteFileName);
		String fileName = "";
		String filePath = "";
		if (remoteFileName.contains("/")) {
			fileName = remoteFileName.substring(remoteFileName.lastIndexOf("/") + 1);
			filePath = remoteFileName.substring(0, remoteFileName.lastIndexOf("/"));
		} else {
			fileName = remoteFileName;
		}
		MyFtpListener listener = MyFtpListener.instance("download");
		try {
			if (StringUtils.isNotEmpty(filePath)) {
				client.changeDirectory(filePath);
			}
			if (listener != null) {
				client.download(fileName, localfile, listener);
			} else {
				client.download(fileName, localfile);
			}
		} catch (Exception e) {
			System.out.println("###[Error] FTPToolkit.download()" + e.getMessage());
			e.printStackTrace();
		}
	}

	// ###---------------------------------------------------------------------
	/**
	 * 格式化文件路径，将其中不规范的分隔转换为标准的分隔符,并且去掉末尾的文件路径分隔符。 本方法操作系统自适应
	 * 
	 * @param path 文件路径
	 * @return 格式化后的文件路径
	 */
	public static String formatPath4File(String path) {
		String reg0 = "\\\\+";
		String reg = "\\\\+|/+";
		String temp = path.trim().replaceAll(reg0, "/");
		temp = temp.replaceAll(reg, "/");
		if (temp.length() > 1 && temp.endsWith("/")) {
			temp = temp.substring(0, temp.length() - 1);
		}
		temp = temp.replace('/', File.separatorChar);
		return temp;
	}

	/**
	 * 格式化文件路径，将其中不规范的分隔转换为标准的分隔符 并且去掉末尾的"/"符号(适用于FTP远程文件路径或者Web资源的相对路径)。
	 * 
	 * @param path 文件路径
	 * @return 格式化后的文件路径
	 */
	public static String formatPath4FTP(String path) {
		String reg0 = "\\\\+";
		String reg = "\\\\+|/+";
		String temp = path.trim().replaceAll(reg0, "/");
		temp = temp.replaceAll(reg, "/");
		if (temp.length() > 1 && temp.endsWith("/")) {
			temp = temp.substring(0, temp.length() - 1);
		}
		return temp;
	}

	/**
	 * 获取FTP路径的父路径，但不对路径有效性做检查
	 * 
	 * @param path FTP路径
	 * @return 父路径，如果没有父路径，则返回null
	 */
	public static String genParentPath4FTP(String path) {
		String f = new File(path).getParent();
		if (f == null) {
			return null;
		} else {
			return formatPath4FTP(f);
		}
	}

	// ###---------------------------------------------------------------------

	// 获取指定目录下的文件
	public static File[] getPathFiles(String path) {
		File file = new File(path);
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			return files;
		}
		return null;
	}

	// 删除指定目录下的临时文件
	public static void deleteFiles(String path) {
		File file = new File(path);
		if (!file.exists()) {
			return;
		}
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			if (files == null || files.length < 1) {
				return;
			}
			for (int i = 0; i < files.length; i++) {
				// String name = files[i].getName();
				// if(name.trim().toLowerCase().endsWith("_ftp.tmp")) {
				// System.out.println(name + "\t");
				// }
				// 清空临时文件 _ftp.tmp
				files[i].delete();
			}
		}
	}

	// 截取文件名称
	public static String getFileName(String fileName) {
		String fs = "F" + System.currentTimeMillis();
		try {
			if (fileName != null && !"".equals(fileName)) {
				if (fileName.lastIndexOf("/") > 0 && fileName.lastIndexOf(".") > 0) {
					fs = fileName.substring(fileName.lastIndexOf("/") + 1);
				} else {
					fs = fileName;
				}

				if (fs.length() > 50) {
					fs = fs.substring(0, 50);
				}
				return fs;
			} else {
				return fs;
			}
		} catch (Exception e) {
			System.out.println("###[Error] FtpTools.getFileName()" + e.getMessage());
		}
		return fs;
	}

	public static void main(String args[]) throws Exception {
		String host = "183.63.172.108";
		String ftpUser = "ftpadmin";
		String ftpPsw = "ftpadmin";
		String localPath = "C:\\Users\\Administrator\\Desktop\\";
		String localFileName = "c.json";
		long start = System.currentTimeMillis();
		FTPClient client = FTPUtils.ftpConn(host, 10122, "UTF-8", ftpUser, ftpPsw);
		FTPUtils.upload(client, localPath + localFileName, "/dataExplore/upload");
		FTPUtils.closeConn(client);
		long end = System.currentTimeMillis();
		System.out.println(end - start);
	}
}