package com.yangyoubiao.www.shell;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.apache.commons.lang.StringUtils;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * <b>版权信息:</b> 2015,广东时代网络电子有限公司<br/>
 * <b>功能描述:</b> SSH执行本地/远程命令 <br/>
 * <b>版本历史:</b><br/>
 * 
 * @author 梁伟文 | 2015年12月3日|创建
 */

public class ShellSSHUtils {
	private final String FS = System.getProperty("file.separator");
	private final String LS = System.getProperty("line.separator");
	public static final int SSH_DEFAULT_PORT = 22;// SSH默认连接端口
	private JSch jsch = null;// 远程SSH连接容器
	// private Map<String, Session> sessionMap = new HashMap<String,
	// Session>();//连接缓存
	private static final String charset = "UTF-8";

	private static ShellSSHUtils instance = null;

	@SuppressWarnings("static-access")
	private ShellSSHUtils() {
		jsch = new JSch();
		jsch.setConfig("StrictHostKeyChecking", "no");
	}

	/** 取得实例 */
	public static ShellSSHUtils getInstance() {
		/*
		 * if(instance==null){ synchronized(ShellSSHUtils.class){ if(instance==null){
		 * instance = new ShellSSHUtils(); } } }
		 */
		instance = new ShellSSHUtils();
		return instance;
	}

	/** 命令执行后处理输出信息 */
	public class ShellOutPutHandler extends Thread {
		private InputStream is;// Shell的输出流
		private String charset = "UTF-8";
		private ExecuteResponeLine rl;// 本工具的实时输出接口
		private StringBuffer result = new StringBuffer();// 结果

		public ShellOutPutHandler(InputStream is, ExecuteResponeLine rl) {
			this.is = is;
			this.rl = rl;
		}

		public ShellOutPutHandler(InputStream is, String charset, ExecuteResponeLine rl) {
			this.is = is;
			this.charset = charset;
			this.rl = rl;
		}

		/** 获取结果 */
		public String getResult() {
			return result.toString();
		}

		public void run() {
			try {
				String buf = null;
				BufferedReader in = new BufferedReader(new InputStreamReader(this.is, charset));
				while (!isInterrupted() && (buf = in.readLine()) != null) {
					if (this.rl != null) {
						this.rl.printLine(buf);
					}
					result.append(buf).append(LS);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 取得远程主机的连接
	 * 
	 * @param host 主机名/IP
	 * @param port 端口(一般为22)
	 * @param user 登录用户名
	 * @param psw  登录密码
	 * @return
	 * @throws JSchException
	 */
	public Session getSession(String host, int port, String user, String psw) throws JSchException {
		// String key = user + "_" + host + "_" + port + "_" + psw;
		Session session = jsch.getSession(user, host, port);
		session.setTimeout(1000 * 60 * 60 * 12);// 12小时超时
		session.setPassword(psw);
		session.connect();// 12小时超时
		return session;
	}

	/**
	 * 执行远程命令脚本(完成后输出结果行)(需要远程主机支持SSH)(使用默认的22端口)
	 * 
	 * @param host    主机名/IP
	 * @param user    登录用户名
	 * @param psw     登录密码
	 * @param command 执行的脚本（多条脚本同时执行使用 \n 隔开）
	 * @param rl      实时输出接口
	 * @return 执行脚本的界面输出结果
	 * @throws Exception
	 */
	public String execRemote(String host, String user, String psw, String command, ExecuteResponeLine rl)
			throws Exception {
		return execRemote(host, SSH_DEFAULT_PORT, user, psw, command, "UTF-8", rl);
	}

	/**
	 * 执行远程命令脚本(实时输出结果行)(需要远程主机支持SSH)
	 * 
	 * @param host    主机名/IP
	 * @param port    端口(一般为22)
	 * @param user    登录用户名
	 * @param psw     登录密码
	 * @param command 执行的脚本（多条脚本同时执行使用 \n 隔开）
	 * @param rl      实时输出接口
	 * @return 执行脚本的界面输出结果
	 * @throws Exception
	 */
	public String execRemote(String host, int port, String user, String psw, String command, String charset,
			final ExecuteResponeLine rl) throws Exception {
		if (StringUtils.isEmpty(command))
			return "没有可执行的命令";
		command = command.trim() + LS + " exit 0 " + LS;// 增加退出Shell命令，每次执行完主动在Shell里中断连接，才能释放reader.readLine()
		StringBuffer resultMessage = new StringBuffer();
		Session session = null;
		ChannelShell openChannel = null;
		InputStream is = null;
		OutputStream os = null;
		BufferedReader inReader = null;
		try {
			session = getSession(host, port, user, psw);
			System.out.println("host:" + host + "--" + session);
			openChannel = (ChannelShell) session.openChannel("shell");
			openChannel.connect(1000 * 60 * 60 * 12);// 12小时超时
			// 获取输入流和输出流
			is = openChannel.getInputStream();
			os = openChannel.getOutputStream();
			// 发送需要执行的SHELL命令，需要用\n结尾，表示回车
			os.write(command.getBytes(charset));
			os.flush();
			// 获取命令执行的结果
			inReader = new BufferedReader(new InputStreamReader(is, charset));
			String buf = null;
			boolean printStart = false;
			while ((buf = inReader.readLine()) != null) {
				if (printStart) {
					if (rl != null) {
						rl.printLine(buf);
					}
					resultMessage.append(buf).append(LS);
				} else if (" exit 0 ".equals(buf) || buf.startsWith("[" + user + "@")) {// 不集中输出命令
					buf = inReader.readLine();// 从下一行开始读
					printStart = true;
				}
			}

		} catch (JSchException e) {
			throw new Exception(String.format("连接失败[地址：%s][端口：%s][用户：%s]", host, port, user), e);
		} catch (IOException e) {
			throw new Exception(String.format("读取结果失败[地址：%s][端口：%s][用户：%s]", host, port, user), e);
		} catch (Exception e) {
			throw new Exception(String.format("执行失败[地址：%s][端口：%s][用户：%s]", host, port, user), e);
		} finally {
			if (os != null)
				os.close();
			if (is != null)
				is.close();
			if (openChannel != null && !openChannel.isClosed()) {
				System.out.println("close channel:" + host + "--" + session);
				openChannel.disconnect();
			}
			if (session != null) {
				System.out.println("close session:" + host + "--" + session);
				session.disconnect();
			}
			if (inReader != null) {
				inReader.close();
			}

		}
		return resultMessage.toString();
	}

	/**
	 * 简单的上传文件到远程服务器
	 * 
	 * @param host
	 * @param port
	 * @param user
	 * @param psw
	 * @param srcPath 远程目录 + 文件名
	 * @param dstPath 本地目录 + 文件名
	 * @return
	 * @throws Exception
	 */
	public String sftpUpload(String host, String user, String psw, String srcPath, String dstPath) throws Exception {
		return sftpUpload(host, SSH_DEFAULT_PORT, user, psw, srcPath, dstPath);
	}

	public String sftpUpload(String host, int port, String user, String psw, String srcPath, String dstPath)
			throws Exception {
		if (StringUtils.isEmpty(srcPath))
			return "远程目录为空";
		if (StringUtils.isEmpty(dstPath))
			return "本地目录为空";
		StringBuffer resultMessage = new StringBuffer();
		Session session = null;
		ChannelSftp openChannel = null;
		InputStream is = null;
		OutputStream os = null;
		try {
			session = getSession(host, port, user, psw);
			openChannel = (ChannelSftp) session.openChannel("sftp");
			openChannel.connect(1000 * 60 * 60 * 12);// 12小时超时
			openChannel.put(dstPath, srcPath, ChannelSftp.OVERWRITE);// 覆盖模式
			openChannel.quit();
		} catch (JSchException e) {
			throw new Exception(String.format("连接失败[地址：%s][端口：%s][用户：%s]", host, port, user), e);
		} catch (Exception e) {
			throw new Exception(String.format("上传失败[地址：%s][端口：%s][用户：%s]", host, port, user), e);
		} finally {
			if (os != null)
				os.close();
			if (is != null)
				is.close();
			if (openChannel != null && !openChannel.isClosed()) {
				openChannel.disconnect();
			}
			if (session != null) {
				session.disconnect();
			}
		}
		return resultMessage.toString();
	}

	/**
	 * 执行本地命令脚本(实时输出结果行)(支持windows/linux/unix...)
	 * 
	 * @param command 命令脚本
	 * @param rl      实时输出接口
	 * @return 执行脚本的界面输出结果
	 * @throws Exception
	 */
	public String execLocal(String command, ExecuteResponeLine rl) throws Exception {
		return execLocal(command, null, null, rl);
	}

	/**
	 * 执行本地命令脚本(实时输出结果行)(支持windows/linux/unix...)
	 * 
	 * @param command 命令脚本
	 * @param envp    环境变量列表(变量名=变量值)
	 * @param dir     开始目录
	 * @param rl      实时输出接口
	 * @return 执行脚本的界面输出结果
	 * @throws Exception
	 */
	public String execLocal(String command, String[] envp, String dirStr, ExecuteResponeLine rl) throws Exception {
		if (StringUtils.isEmpty(command))
			throw new Exception("没有可执行的命令");
		File dir = StringUtils.isNotEmpty(dirStr) ? new File(dirStr) : null;
		Process p = Runtime.getRuntime().exec(command, envp, dir);
		ShellOutPutHandler msgThread = new ShellOutPutHandler(p.getInputStream(), "GBK", rl);
		ShellOutPutHandler errThread = new ShellOutPutHandler(p.getErrorStream(), "GBK", rl);
		msgThread.start();
		errThread.start();

		p.getOutputStream().close();

		return msgThread.getResult() + errThread.getResult();
	}

	public void uploadAndExecute(String host, int port, String user, String psw, String executeCommand,
			String sourceFilePath, final ExecuteResponeLine rl) throws Exception {
		Session session = null;
		try {
			System.out.println("开始获得远程主机连接");
			session = getSession(host, port, user, psw);
			System.out.println("获得远程主机连接成功,开始上传脚本");
			rl.printLine("开始上传执行脚本");
			// 上传文件
			String remoteExecuteFile = uploadToRemoteFile(executeCommand, sourceFilePath, session);
			rl.printLine("上传脚本成功,赋权然后开始执行脚本");
			System.out.println("上传脚本成功");
			StringBuilder command = new StringBuilder("chmod +x ");
			command.append(remoteExecuteFile);
			command.append("\n");
			command.append("sh ");
			command.append(remoteExecuteFile);
			session = getSession(host, port, user, psw);
			execRemoteFile(command.toString(), charset, session, rl);
			rl.printLine("执行脚本完成");
		} catch (JSchException e) {
			throw new Exception(String.format("连接失败[地址：%s][端口：%s][用户：%s]", host, port, user), e);
		} finally {
			if (session != null) {
				session.disconnect();
				System.out.println(session + "关闭连接成功");
			}
		}
	}

	/**
	 * 
	 * @param executeCommand
	 * @param session
	 * @throws Exception
	 */
	public String uploadToRemoteFile(String executeCommand, String sourceFilePath, Session session) throws Exception {
		ChannelSftp chSftp = null;
		InputStream input = null;
		OutputStream out = null;
		try {
			System.out.println("打开sftp渠道连接");
			chSftp = (ChannelSftp) session.openChannel("sftp");
			chSftp.connect();
			System.out.println("连接sftp");
			input = new ByteArrayInputStream(executeCommand.getBytes(charset));
			// 控制执行脚本不能在用户目录下执行，导致删除用户目录下的数据
			/*
			 * if(!StringUtils.contains(sourceFilePath, "tableCollect")){ throw new
			 * ShellExecuteException("上传的执行脚本目录不完整:"+sourceFilePath); }
			 */
			out = chSftp.put(sourceFilePath, ChannelSftp.OVERWRITE);
			byte[] buff = new byte[1024 * 256];
			int read = 0;
			if (out != null) {
				do {
					read = input.read(buff, 0, buff.length);
					if (read > 0) {
						out.write(buff, 0, read);
					}
				} while (read >= 0);
				out.flush();
			}
		} finally {
			if (input != null)
				input.close();
			if (out != null)
				out.close();
			if (chSftp != null && !chSftp.isClosed()) {
				chSftp.disconnect();
			}
			if (session != null) {
				session.disconnect();
				System.out.println(session + "关闭连接成功");
			}
		}
		return sourceFilePath;
	}

	public String execRemoteFile(String command, String charset, Session session, final ExecuteResponeLine rl)
			throws Exception {
		if (StringUtils.isEmpty(command))
			return "没有可执行的命令";
		command = command.trim() + LS + " exit 0 " + LS;// 增加退出Shell命令，每次执行完主动在Shell里中断连接，才能释放reader.readLine()
		StringBuffer resultMessage = new StringBuffer();
		ChannelShell openChannel = null;
		InputStream is = null;
		OutputStream os = null;
		try {
			openChannel = (ChannelShell) session.openChannel("shell");
			openChannel.connect(1000 * 60 * 60 * 12);// 12小时超时
			// 获取输入流和输出流
			is = openChannel.getInputStream();
			os = openChannel.getOutputStream();
			// 发送需要执行的SHELL命令，需要用\n结尾，表示回车
			os.write(command.getBytes(charset));
			os.flush();
			// 获取命令执行的结果
			BufferedReader inReader = new BufferedReader(new InputStreamReader(is, "GBK"));
			String buf = null;
			boolean printStart = false;
			while ((buf = inReader.readLine()) != null) {
				if (printStart) {
					if (rl != null) {
						rl.printLine(buf);
					}
					resultMessage.append(buf).append(LS);
				} else if (" exit 0 ".equals(buf)) {// 不集中输出命令
					buf = inReader.readLine();// 从下一行开始读
					printStart = true;
				}
			}
		} catch (Exception e) {
			throw new Exception(e.getMessage(), e);
		} finally {
			if (os != null)
				os.close();
			if (is != null)
				is.close();
			if (openChannel != null && !openChannel.isClosed()) {
				openChannel.disconnect();
			}
		}
		return resultMessage.toString();
	}

	public void uploadAndExecuteImpala(String host, int port, String user, String psw, String executeCommand,
			String sourceFilePath, final ExecuteResponeLine rl) throws Exception {
		Session session = null;
		try {
			session = getSession(host, port, user, psw);
			rl.printLine("开始上传模型固化执行脚本到服务器:" + executeCommand);
			// 上传Impala执行脚本文件
			String remoteExecuteFile = uploadToRemoteFile(executeCommand, sourceFilePath, session);
			rl.printLine("上传模型固化脚本成功,赋权然后开始执行脚本");
			// String logPath = propertyUtil.getProperty("impala.shell.log");
			StringBuilder command = new StringBuilder("");
			command.append("impala-shell -f ").append(remoteExecuteFile).append(" \n");

			session = getSession(host, port, user, psw);
			execRemoteFile(command.toString(), charset, session, rl);
			rl.printLine("执行impala模型固化脚本完成");
		} catch (JSchException e) {
			throw new Exception(String.format("连接失败[地址：%s][端口：%s][用户：%s]", host, port, user), e);
		} finally {
			if (session != null) {
				session.disconnect();
			}
		}
	}

	@SuppressWarnings("serial")
	public static void main(String[] args) {
		try {
			StringBuffer shell = new StringBuffer();
			shell.append("ls / \n ");
			shell.append("echo ================================= \n ");
			shell.append("ll /home/log \n ");
			shell.append("echo ================================= \n ");
			shell.append("ifconfig \n ");
			shell.append("java -version \n ");

			String str=ShellSSHUtils.getInstance().execRemote("132.97.110.251", "root", "GZDX_cdh123!@#", shell.toString(),
					(line) -> {
						System.out.println("测试调用远程命令：" + line);
					});
			System.out.println((int) "\n".charAt(0));
			System.out.println(str);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}