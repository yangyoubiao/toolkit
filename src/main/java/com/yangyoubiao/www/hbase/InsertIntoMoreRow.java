package com.yangyoubiao.www.hbase;

import java.io.IOException;
import java.util.*;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;

/**
 * 批量插入数据
 */
public class InsertIntoMoreRow {
	public static void main(String[] args) throws IOException {
		Configuration configuration = HBaseConfiguration.create();
		Connection connection = ConnectionFactory.createConnection(configuration);
		Table table = connection.getTable(TableName.valueOf("test"));

		List<Put> list = new ArrayList<Put>();
		for (int i = 0; i < 100; i++) {
			String row = "row" + i;
			String columnFamily = "data";
			String qualifier = "1";
			String value = row + "-" + "value" + i;
			Put put = new Put(row.getBytes());
			put.addColumn(columnFamily.getBytes(), qualifier.getBytes(), value.getBytes());
			list.add(put);
		}
		table.put(list);

		table.close();
		connection.close();
	}
}
