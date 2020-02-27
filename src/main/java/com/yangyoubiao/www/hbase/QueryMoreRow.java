package com.yangyoubiao.www.hbase;

import java.io.IOException;
import java.util.*;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;

/**
 * 一次查询多条数据
 */
public class QueryMoreRow {
	public static void main(String[] args) throws IOException {
		Configuration configuration = HBaseConfiguration.create();
		Connection connection = ConnectionFactory.createConnection(configuration);
		Table table = connection.getTable(TableName.valueOf("test"));

		List<Get> gets = new ArrayList<Get>();
		for (int i = 0; i < 100; i++) {
			String row = "row" + i;
			String columnFamily = "data";
			String qualifier = "1";
			Get get = new Get(row.getBytes());
			get.addColumn(columnFamily.getBytes(), qualifier.getBytes());
			get.setTimeStamp(1578901903082L);

			gets.add(get);

		}

		Result[] results = table.get(gets);
		for (Result res : results) {
			Cell cell = res.getColumnLatestCell("data".getBytes(), "1".getBytes());

			System.out.println(new String(CellUtil.cloneRow(cell)) + "-" + new String(CellUtil.cloneFamily(cell)) + "-"
					+ new String(CellUtil.cloneQualifier(cell)) + "-" + new String(CellUtil.cloneValue(cell)));

		}

		table.close();
		connection.close();
	}
}
