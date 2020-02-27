package com.yangyoubiao.www.impala;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.yangyoubiao.www.phoenix.PhoenixUtils;
import com.yangyoubiao.www.util.CollectionUtils;

public class ImpalaUtils {
	private static String driverClassName = "com.cloudera.impala.jdbc41.Driver";// 驱动
	private static String url = "jdbc:impala://slave3:21050";// url

	/**
	 * 获取impala连接
	 * 
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static Connection getConn() throws ClassNotFoundException, SQLException {
		Class.forName(driverClassName);
		return (Connection) DriverManager.getConnection(url);
	}

	/**
	 * 关闭资源
	 */
	public static void close(Connection conn, Statement statement, ResultSet result) throws SQLException {
		if (result != null && !result.isClosed()) {
			result.close();
		}
		if (statement != null && !statement.isClosed()) {
			statement.close();
		}
		if (conn != null && !conn.isClosed()) {
			conn.close();
		}
	}

	/**
	 * 查询表的数据格式
	 * 
	 * @param tableName impala表名
	 * 
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 * 
	 * @return Map<String,String> <表列名称，表类型>
	 */
	public static Map<String, String> descTable(String tableName) throws Exception {
		Map<String, String> map = new HashMap<String, String>();
		Connection conn = null;
		Statement statement = null;
		ResultSet result = null;
		try {
			conn = getConn();
			statement = conn.createStatement();
			if (statement.execute("DESCRIBE " + tableName)) {
				result = statement.getResultSet();
				// [NAME, TYPE, COMMENT]
				while (result.next()) {
					map.put(result.getString("NAME").toUpperCase(), result.getString("TYPE").toUpperCase());
				}
			}
		} finally {
			close(conn, statement, result);
		}
		return map;
	}

	/**
	 * show tables
	 */
	public static List<String> showTables() throws Exception {
		List<String> list = new ArrayList<String>();
		Connection conn = null;
		Statement statement = null;
		ResultSet result = null;
		try {
			conn = getConn();
			statement = conn.createStatement();
			if (statement.execute(" show tables ")) {
				result = statement.getResultSet();
				// [NAME]
				while (result.next()) {
					list.add(result.getString("NAME"));
				}
			}
		} finally {
			close(conn, statement, result);
		}
		return list;
	}

	/**
	 * DESCRIBE FORMATTED tableName;<br>
	 * 获取表的元数据信息:<br>
	 * 1.表字段名称，类型，要求有序<br>
	 * 2.表的存储位置<br>
	 * 3.存储类型<br>
	 * 4.行分隔符<br>
	 * 5.列分隔符<br>
	 * 
	 * @param tableName 表名
	 * @return Map<String,Object> {<br>
	 *         name:[],<br>
	 *         tyle:[],<br>
	 *         location:"",<br>
	 *         fileType:"text",<br>
	 *         lineDelimiter:""<br>
	 *         fieldDelimiter:"\\u001" }
	 */
	public static Map<String, Object> descFormatted(String tableName) throws Exception {
		Character c01 = 0x01;
		Map<String, Object> map = new HashMap<String, Object>();
		ArrayList<String> name = new ArrayList<String>();
		ArrayList<String> type = new ArrayList<String>();
		String location = "";
		String fileType = "text";
		String fieldDelimiter = c01.toString();

		Connection conn = null;
		Statement statement = null;
		ResultSet result = null;

		// DESCRIBE FORMATTED tableName 命令查询结果的三列在一下的三个list里保存
		ArrayList<String> nameList = new ArrayList<String>();
		ArrayList<String> typeList = new ArrayList<String>();
		ArrayList<String> commentList = new ArrayList<String>();
		try {
			conn = getConn();
			statement = conn.createStatement();
			if (statement.execute(" DESCRIBE FORMATTED " + tableName)) {
				result = statement.getResultSet();
				while (result.next()) {
					nameList.add(result.getString("NAME"));
					typeList.add(result.getString("TYPE"));
					commentList.add(result.getString("COMMENT"));
				}
			}
		} finally {
			close(conn, statement, result);
		}
		// # col_name ,data_type , comment
		// ...
		// # Detailed Table Information ,null, null
		// ...
		// # Storage Information ,null, null
		// ...
		for (int i = 0; i < nameList.size(); i++) {
			String n = nameList.get(i);
			// # col_name ,data_type , comment
			if (n.startsWith("# col_name")) {
				i++;
				for (; i < nameList.size(); i++) {
					n = nameList.get(i);
					if (n != null && n.startsWith("#")) {
						break;
					}
					if (n != null && n.length() != 0) {
						name.add(n);
						type.add(typeList.get(i));
					}
				}
			}
			// # Detailed Table Information ,null, null
			if (n.startsWith("# Detailed")) {
				i++;
				for (; i < nameList.size(); i++) {
					n = nameList.get(i);
					if (n != null && n.startsWith("#")) {
						break;
					}
					if (n != null && n.length() > 0 && n.startsWith("Location")) {
						location = typeList.get(i);
					}
				}
			}
			// # Storage Information ,null, null
			if (n.startsWith("# Storage")) {
				i++;
				for (; i < nameList.size(); i++) {
					n = nameList.get(i);
					if (n != null && n.startsWith("#")) {
						break;
					}
					if (n == null || n.length() == 0) {
						String t = typeList.get(i).trim();
						String c = commentList.get(i).trim();
						if ("field.delim".equals(t)) {
							fieldDelimiter = ((Character) (char) (int) (Integer.valueOf(c))).toString();
						}
					}
				}
			}
		}

		map.put("name", name);
		map.put("type", type);
		map.put("location", location);
		map.put("fileType", fileType);
		map.put("fieldDelimiter", fieldDelimiter);
		return map;
	}

	/**
	 * 根据impala表名新建一张中间表 create table TEMPTABLENAME as select UUID() UUIDNAME ,t.*
	 * from TABLENAME t;
	 */
	public static void createTempTable(String tableName) throws Exception {
		Connection conn = null;
		Statement statement = null;
		ResultSet result = null;
		String sql = "create table TEMPTABLENAME as select UUID() UUIDNAME ,t.* from TABLENAME t";
		try {
			conn = getConn();
			statement = conn.createStatement();
			String TEMPTABLENAME = tableName + "_TEMP_PHOENIX";
			String UUIDNAME = PhoenixUtils.PHOENIX_PRIMARY_KEY;
			statement.execute(sql.replaceAll("TEMPTABLENAME", TEMPTABLENAME).replaceAll("UUIDNAME", UUIDNAME)
					.replaceAll("TABLENAME", tableName).toUpperCase());
			// result = statement.getResultSet();
			// ResultSetMetaData metaData = result.getMetaData();
			// int columnCount = metaData.getColumnCount();
		} finally {
			close(conn, statement, result);
		}
	}

	/**
	 * 删除缓存表，和createTempTable(String tableName)方法对应
	 */
	public static void dropTempTable(String tableName) throws Exception {
		Connection conn = null;
		Statement statement = null;
		ResultSet result = null;
		String sql = "drop table " + tableName + "_TEMP_PHOENIX";
		try {
			conn = getConn();
			statement = conn.createStatement();
			statement.execute(sql);
			result = statement.getResultSet();
		} finally {
			close(conn, statement, result);
		}
	}

	public static void main(String[] args) throws Exception {
		createTempTable("TOTAL_PRODUCT_USER");
		dropTempTable("TOTAL_PRODUCT_USER");
	}
}
