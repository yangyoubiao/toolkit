package com.yangyoubiao.www.hbase;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.phoenix.schema.types.PDouble;

import com.yangyoubiao.www.impala.ImpalaUtils;

/**
 * 批量插入数据到hbase表
 */
public class BatchInsert {
	public static void main(String[] args) throws ClassNotFoundException, IOException, SQLException {
		insertHbase(args);
	}

	/**
	 * phoenix插入语句
	 */
	public static void buildSql(String[] args) throws IOException, ClassNotFoundException, SQLException {
		int numble = 1000;// 每次插入数据条数

		byte[] serv_id = new byte[] { 0, 0, 0, 0 };
		byte[] contact_name = new byte[] { -128, 19 };
		byte[] cust_id = new byte[] { -128, 12 };
		byte[] acc_nbr = new byte[] { -128, 11 };
		byte[] cert_type = new byte[] { -128, 17 };
		byte[] zx_cust_id = new byte[] { -128, 15 };
		byte[] contact_tel = new byte[] { -128, 20 };
		byte[] zx_cust_code = new byte[] { -128, 16 };
		byte[] area = new byte[] { -128, 27 };
		byte[] city = new byte[] { -128, 26 };
		byte[] contact_addr = new byte[] { -128, 21 };
		byte[] country = new byte[] { -128, 24 };
		byte[] subst_id = new byte[] { -128, 22 };
		byte[] subst_name = new byte[] { -128, 23 };
		byte[] province = new byte[] { -128, 25 };
		byte[] cust_nbr = new byte[] { -128, 13 };
		byte[] cert_num = new byte[] { -128, 18 };
		byte[] branch_id = new byte[] { -128, 28 };
		byte[] cust_name = new byte[] { -128, 14 };

		// 从impala取数据
		Connection conn = ImpalaUtils.getConn();
		Statement statement = conn.createStatement();
		ResultSet result = statement.executeQuery("select * from total_product_user limit 100");
		ResultSetMetaData metaData = result.getMetaData();
		int columnCount = metaData.getColumnCount();

		String row = null;
		List<Put> insert = new ArrayList<Put>();
		int num = 0;
		while (result.next()) {
			StringBuffer sb = new StringBuffer();
			sb.append("upsert into TOTAL_PRODUCT_USER2 ");
			StringBuffer fields = new StringBuffer();
			StringBuffer values = new StringBuffer();
			fields.append("(");
			values.append("(");
			for (int i = 1; i <= columnCount; i++) {
				System.out.println(metaData.getColumnName(i));
				String name = metaData.getColumnName(i).toUpperCase();
				String value = result.getString(name);
				fields.append(" " + name + " ");
				if ("subst_id".equalsIgnoreCase(name)) {
					values.append(value == null ? null : " " + value + " ");
				} else {
					values.append(value == null ? null : " '" + value + "' ");
				}
				if (i != columnCount) {
					fields.append(" , ");
					values.append(" , ");
				}
			}
			fields.append(")");
			values.append(")");
			sb.append(fields);
			sb.append(" values ");
			sb.append(values);
			System.out.println(sb.toString() + ";");
		}

		// impala关闭数据连接
		result.close();
		statement.close();
		conn.close();

	}

	/**
	 * 批量插入hbase
	 */
	public static void insertHbase(String[] args) throws IOException, ClassNotFoundException, SQLException {
		int numble = 10000;// 每次插入数据条数
		// hbase信息
		org.apache.hadoop.hbase.client.Connection hbase = HbaseUtils.getConn();
		Table table = hbase.getTable(TableName.valueOf("TOTAL_PRODUCT_USER2"));
		byte[] serv_id = new byte[] { 0, 0, 0, 0 };
		byte[] contact_name = new byte[] { -128, 19 };
		byte[] cust_id = new byte[] { -128, 12 };
		byte[] acc_nbr = new byte[] { -128, 11 };
		byte[] cert_type = new byte[] { -128, 17 };
		byte[] zx_cust_id = new byte[] { -128, 15 };
		byte[] contact_tel = new byte[] { -128, 20 };
		byte[] zx_cust_code = new byte[] { -128, 16 };
		byte[] area = new byte[] { -128, 27 };
		byte[] city = new byte[] { -128, 26 };
		byte[] contact_addr = new byte[] { -128, 21 };
		byte[] country = new byte[] { -128, 24 };
		byte[] subst_id = new byte[] { -128, 22 };
		byte[] subst_name = new byte[] { -128, 23 };
		byte[] province = new byte[] { -128, 25 };
		byte[] cust_nbr = new byte[] { -128, 13 };
		byte[] cert_num = new byte[] { -128, 18 };
		byte[] branch_id = new byte[] { -128, 28 };
		byte[] cust_name = new byte[] { -128, 14 };

		// 从impala取数据
		Connection conn = ImpalaUtils.getConn();
		Statement statement = conn.createStatement();
		ResultSet result = statement.executeQuery("select * from total_product_user limit 100");
		ResultSetMetaData metaData = result.getMetaData();
		int columnCount = metaData.getColumnCount();

		String row = null;
		List<Put> insert = new ArrayList<Put>();
		int num = 0;
		while (result.next()) {
			// 组装数据
			row = result.getString("serv_id");
			Put put = new Put(row.getBytes());
			put.addColumn("0".getBytes(), serv_id, "x".getBytes());
			if (result.getString("contact_name") != null) {
				put.addColumn("0".getBytes(), contact_name, result.getString("contact_name").getBytes());
			}
			if (result.getString("cust_id") != null) {
				put.addColumn("0".getBytes(), cust_id, result.getString("cust_id").getBytes());
			}
			if (result.getString("acc_nbr") != null) {
				put.addColumn("0".getBytes(), acc_nbr, result.getString("acc_nbr").getBytes());
			}
			if (result.getString("cert_type") != null) {
				put.addColumn("0".getBytes(), cert_type, result.getString("cert_type").getBytes());
			}
			if (result.getString("zx_cust_id") != null) {
				put.addColumn("0".getBytes(), zx_cust_id, result.getString("zx_cust_id").getBytes());
			}
			if (result.getString("contact_tel") != null) {
				put.addColumn("0".getBytes(), contact_tel, result.getString("contact_tel").getBytes());
			}
			if (result.getString("zx_cust_code") != null) {
				put.addColumn("0".getBytes(), zx_cust_code, result.getString("zx_cust_code").getBytes());
			}
			if (result.getString("area") != null) {
				put.addColumn("0".getBytes(), area, result.getString("area").getBytes());
			}
			if (result.getString("city") != null) {
				put.addColumn("0".getBytes(), city, result.getString("city").getBytes());
			}
			if (result.getString("contact_addr") != null) {
				put.addColumn("0".getBytes(), contact_addr, result.getString("contact_addr").getBytes());
			}
			if (result.getString("country") != null) {
				put.addColumn("0".getBytes(), country, result.getString("country").getBytes());
			}
			if (result.getString("subst_id") != null) {
				System.out.println(result.getDouble("subst_id"));
				put.addColumn("0".getBytes(), subst_id,
						PDouble.INSTANCE.toBytes(new Double(result.getDouble("subst_id"))));
			}
			if (result.getString("subst_name") != null) {
				put.addColumn("0".getBytes(), subst_name, result.getString("subst_name").getBytes());
			}
			if (result.getString("province") != null) {
				put.addColumn("0".getBytes(), province, result.getString("province").getBytes());
			}
			if (result.getString("cust_nbr") != null) {
				put.addColumn("0".getBytes(), cust_nbr, result.getString("cust_nbr").getBytes());
			}
			if (result.getString("cert_num") != null) {
				put.addColumn("0".getBytes(), cert_num, result.getString("cert_num").getBytes());
			}
			if (result.getString("branch_id") != null) {
				put.addColumn("0".getBytes(), branch_id, result.getString("branch_id").getBytes());
			}
			if (result.getString("cust_name") != null) {
				put.addColumn("0".getBytes(), cust_name, result.getString("cust_name").getBytes());
			}

			insert.add(put);
			num++;
			if (num % numble == 0) {
				long start = System.currentTimeMillis();
				table.put(insert);
				long end = System.currentTimeMillis();
				System.out.println("插入" + insert.size() + "条耗时：" + (end - start));
				insert.clear();
			}
		}
		if (insert.size() > 0) {
			long start = System.currentTimeMillis();
			table.put(insert);
			long end = System.currentTimeMillis();
			System.out.println("插入" + insert.size() + "条耗时：" + (end - start));
			insert.clear();
		}

		// impala关闭数据连接
		result.close();
		statement.close();
		conn.close();

		table.close();
		hbase.close();
	}
}
