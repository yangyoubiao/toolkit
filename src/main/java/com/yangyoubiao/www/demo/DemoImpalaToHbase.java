package com.yangyoubiao.www.demo;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.phoenix.schema.types.PBoolean;
import org.apache.phoenix.schema.types.PDate;
import org.apache.phoenix.schema.types.PDouble;
import org.apache.phoenix.schema.types.PFloat;
import org.apache.phoenix.schema.types.PInteger;
import org.apache.phoenix.schema.types.PLong;
import org.apache.phoenix.schema.types.PSmallint;
import org.apache.phoenix.schema.types.PTimestamp;
import org.apache.phoenix.schema.types.PTinyint;

import com.yangyoubiao.www.hbase.HbaseUtils;
import com.yangyoubiao.www.impala.ImpalaUtils;
import com.yangyoubiao.www.phoenix.PhoenixUtils;

/**
 * 将impala的表迁移到hbase之中<br>
 * 插入hbase表需要自定义主键ID：phoinex_only_id，从1开始自增
 */
public class DemoImpalaToHbase {
	public static long PHOENIX_PRIMARY_KEY_value = 0L;

	public static void main(String[] args) throws Exception {
		String tableName = "total_product_user";
		tableName = tableName.toUpperCase();
		Map<String, String> map = ImpalaUtils.descTable(tableName);
		String createTable = PhoenixUtils.createTableSqlFromImpalaAddKey(map, tableName);
		System.out.println(createTable);
		if (PhoenixUtils.executeSQL(createTable)) {
			insertHbaseFromImpala(tableName);
		} else {
			System.out.println("建表失败！");
		}
	}

	/**
	 * 批量插入hbase
	 */
	public static void insertHbaseFromImpala(String tableName) throws Exception {
		int numble = 10000;// 每次插入数据条数
		Map<String, String> impalaTypes = ImpalaUtils.descTable(tableName);// <表字段[大写]，impala类型>
		Map<String, String> phoenixType = PhoenixUtils.transTypeImpalaToPhoenix(impalaTypes);// <表字段[大写]，phoenix类型>
		Map<String, byte[]> phoenixAlias = HbaseUtils.getTableInfo(tableName); // <表字段[大写]，phoenix别名>
		byte[] zero = "0".getBytes();// 0族
		byte[] x = "x".getBytes();
		Set<String> phoenixTypeSet = phoenixType.keySet();

		Connection conn = null;
		Statement statement = null;
		ResultSet result = null;
		
		org.apache.hadoop.hbase.client.Connection hbase = null;
		Table table = null;
		try {
			// impala连接
			conn = ImpalaUtils.getConn();
			statement = conn.createStatement();
			result = statement.executeQuery("select * from " + tableName + "");
			ResultSetMetaData metaData = result.getMetaData();
			int columnCount = metaData.getColumnCount();
			
			//hbase
			hbase=HbaseUtils.getConn();
			table =hbase.getTable(TableName.valueOf(tableName));

			String row = null;
			List<Put> insert = new ArrayList<Put>();
			int num = 0;

			while (result.next()) {
				PHOENIX_PRIMARY_KEY_value++;
				Put put = new Put(PLong.INSTANCE.toBytes(PHOENIX_PRIMARY_KEY_value));// rowId
				put.addColumn(zero, phoenixAlias.get(PhoenixUtils.PHOENIX_PRIMARY_KEY), x);// 主键值
				
				for (String str : phoenixTypeSet) {
					String value = result.getString(str);
					if (value != null) {
						byte[] bvalue = null;
						String impalaType = impalaTypes.get(str);
						switch (impalaType) {
						case "STRING":bvalue = result.getString(str).getBytes();	break;
						case "DOUBLE":bvalue = PDouble.INSTANCE.toBytes(result.getDouble(str));	break;
						case "REAL":bvalue = PDouble.INSTANCE.toBytes(result.getDouble(str));	break;
						case "BOOLEAN":bvalue = PBoolean.INSTANCE.toBytes(result.getBoolean(str));	break;
						case "BIGINT":bvalue = PLong.INSTANCE.toBytes(result.getLong(str));	break;
						case "FLOAT":bvalue = PFloat.INSTANCE.toBytes(result.getFloat(str));	break;
						case "INT":bvalue = PInteger.INSTANCE.toBytes(result.getInt(str));	break;
						case "SMALLINT":bvalue = PSmallint.INSTANCE.toBytes(result.getShort(str));	break;
						case "TINYINT":bvalue = PTinyint.INSTANCE.toBytes(result.getByte(str));	break;
						case "DATE":bvalue = PDate.INSTANCE.toBytes(result.getDate(str));	break;
						case "TIMESTAMP":bvalue = PTimestamp.INSTANCE.toBytes(result.getTimestamp(str));	break;
						case "VARCHAR":bvalue = result.getString(str).getBytes();	break;
						default:bvalue=result.getString(str).getBytes();
						}
						put.addColumn(zero, phoenixAlias.get(str), bvalue);
					}
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
		} finally {
			ImpalaUtils.close(conn, statement, result);
			table.close();
			hbase.close();
		}
	}
}
