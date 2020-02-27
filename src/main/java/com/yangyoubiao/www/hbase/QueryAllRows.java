package com.yangyoubiao.www.hbase;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.*;

/**
 * 遍历表中所有的行
 */
public class QueryAllRows {
	public static void main(String[] args) throws IOException {
		Configuration configuration = HBaseConfiguration.create();
		Connection connection = ConnectionFactory.createConnection(configuration);
		Table table = connection.getTable(TableName.valueOf("SYSTEM.CATALOG"));

		HTableDescriptor descriptor = table.getTableDescriptor();
		String family = "0";
		String[] qualifierArr = new String[] { "DATA_TYPE", "IS_ROW_TIMESTAMP", "IS_VIEW_REFERENCED", "KEY_SEQ",
				"NULLABLE", "ORDINAL_POSITION", "PK_NAME", "SORT_ORDER", "_0" };

		ResultScanner scanner = table.getScanner(family.getBytes());
		Iterator<Result> iterator = scanner.iterator();
		// 每行数据
		while (iterator.hasNext()) {

			Result result = iterator.next();
			System.out.println(new String(result.getRow()));
			// 每个单元格
			for (String qualifier : qualifierArr) {
				if (result.containsColumn(family.getBytes(), qualifier.getBytes())) {
					Cell cell = result.getColumnLatestCell(family.getBytes(), qualifier.getBytes());
					System.out.print(new String(qualifier + ":" + new String(CellUtil.cloneValue(cell))) + "-----");
				}
			}
			System.out.println("");

		}

		table.close();
		connection.close();
	}
}
