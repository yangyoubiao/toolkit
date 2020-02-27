package com.yangyoubiao.www.phoenix;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * phoenix公共类
 */
public class PhoenixUtils {
	private static String driverClassName = "org.apache.phoenix.jdbc.PhoenixDriver";// 驱动
	private static String url = "jdbc:phoenix:slave3:2181";// url

	/** phoenix表新建表的主键字段 */
	public static String PHOENIX_PRIMARY_KEY = "PHOENIX_ONLY_ID";

	/**
	 * 获取impala连接
	 * 
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static Connection getConn() throws ClassNotFoundException, SQLException {
		Class.forName(driverClassName);
		return (Connection) DriverManager.getConnection(url, "", "");
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

	/** impala类型和phoenix类型的对应关系 */
	public static Map<String, String> impalaPhoenixType = new HashMap<String, String>();
	static {
		// java.sql.Array
		impalaPhoenixType.put("ARRAY", "ARRAY");// ARRAY < type >
		// java.lang.Long
		impalaPhoenixType.put("BIGINT", "BIGINT");// BIGINT 8bytes
		// java.lang.Boolean
		impalaPhoenixType.put("BOOLEAN", "BOOLEAN");// BOOLEAN
		// java.lang.String
		impalaPhoenixType.put("CHAR", "CHAR");// CHAR(length)
		// java.sql.Date
		impalaPhoenixType.put("DATE", "DATE");// DATE YYYY-MM-DD
		// java.math.BigDecimal
		impalaPhoenixType.put("DECIMAL", "DECIMAL");// DECIMAL[(precision[, scale])]
		// java.lang.Double
		impalaPhoenixType.put("DOUBLE", "DOUBLE");// DOUBLE
		// java.lang.Float
		impalaPhoenixType.put("FLOAT", "FLOAT");// FLOAT
		// java.lang.Integer
		impalaPhoenixType.put("INT", "INTEGER");// INT -2147483648 .. 2147483647 4bytes
		impalaPhoenixType.put("MAP", "");// MAP < primitive_type, type >
		// java.lang.Double
		impalaPhoenixType.put("REAL", "DOUBLE");//
		// java.lang.Short
		impalaPhoenixType.put("SMALLINT", "SMALLINT");// SMALLINT -32768 .. 32767 2bytes
		// java.lang.String
		impalaPhoenixType.put("STRING", "VARCHAR");// STRING
		impalaPhoenixType.put("STRUCT", "");// STRUCT < name : type [COMMENT 'comment_string'], ... >
		// java.sql.Timestamp
		impalaPhoenixType.put("TIMESTAMP", "TIMESTAMP");// TIMESTAMP
		// java.lang.Byte
		impalaPhoenixType.put("TINYINT", "TINYINT");// TINYINT -128 .. 127 1byte
		// java.lang.String
		impalaPhoenixType.put("VARCHAR", "VARCHAR");// VARCHAR(max_length) 65,535.
	}

	/**
	 * 根据impala的字段名称、字段类型、表名，组合phoenix的建表语句。 <br>
	 * 建表语句中要新增一个主键字段
	 * 
	 * @param map       <impala的字段名称、impala的字段类型>
	 * @param tableName impala表的表名
	 */
	public static String createTableSqlFromImpalaAddKey(Map<String, String> map, String tableName) {
		String blank = " ";
		StringBuffer sb = new StringBuffer("create table ");
		sb.append(blank + tableName + blank);
		sb.append("(");
		sb.append(PHOENIX_PRIMARY_KEY + " BIGINT not null primary key ");

		Set<String> set = map.keySet();
		for (String str : set) {
			String type = map.get(str);
			if (impalaPhoenixType.containsKey(type.toUpperCase())) {
				sb.append(",");
				sb.append(str);
				sb.append(blank);
				sb.append(impalaPhoenixType.get(type.toUpperCase()));
			} else {
				System.out.println("无法找到该类型对应的phoenix类型：" + type);
			}
		}

		sb.append(")");
		return sb.toString();
	}

	/**
	 * 将impala的类型转换成phoenix的类型
	 * 
	 * @param map <字段名，impala字段对应的类型>
	 * @return Map<String, String> <字段名，phoenix字段对应的类型>
	 */
	public static Map<String, String> transTypeImpalaToPhoenix(Map<String, String> map) {
		Map<String, String> result = new HashMap<String, String>();
		Set<String> set = map.keySet();
		for (String str : set) {
			String value = map.get(str);
			if (impalaPhoenixType.containsKey(value.toUpperCase())) {
				result.put(str, impalaPhoenixType.get(value.toUpperCase()));
			} else {
				System.out.println("无法找到该类型对应的phoenix类型：" + value);
			}
		}
		return result;
	}

	public static boolean executeSQL(String sql) throws SQLException {
		Connection conn = null;
		Statement statement = null;
		ResultSet result = null;
		boolean res = true;
		try {
			conn = getConn();
			statement = conn.createStatement();
			statement.execute(sql);
		} catch(Exception e){
			e.printStackTrace();
			res=false;
		}finally {
			close(conn, statement, result);
		}
		return res;
	}

	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		Connection conn = null;
		Statement statement = null;
		ResultSet result = null;
		try {
			conn = getConn();
			statement = conn.createStatement();

			if (statement.execute("select * from TOTAL_PRODUCT_USER")) {
				result = statement.getResultSet();
				// [NAME, TYPE, COMMENT]
				while (result.next()) {
					System.out.println(result.getString("SERV_ID"));
				}
			}
		} finally {
			close(conn, statement, result);
		}
	}
}
