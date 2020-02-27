package com.yangyoubiao.www.impala;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class test {
	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		Connection conn = ImpalaUtils.getConn();
		Statement statement = conn.createStatement();
		ResultSet result = statement.executeQuery("select * from total_product_user limit 10");
		ResultSetMetaData metaData = result.getMetaData();
		int columnCount = metaData.getColumnCount();
		String colname = null;
		String str = null;
		while (result.next()) {
			for (int i = 1; i <= columnCount; i++) {
				colname = metaData.getColumnName(i);
				str = result.getString(colname);
				System.out.print(colname + ":" + str + "   |");
			}
			System.out.println();
		}

		result.close();
		statement.close();
		conn.close();

	}
}
