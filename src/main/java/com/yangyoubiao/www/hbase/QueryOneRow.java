package com.yangyoubiao.www.hbase;

import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;

/**
 * 查询一条数据
 */
public class QueryOneRow {
	public static void main(String[] args) throws IOException {
		Configuration configuration = HBaseConfiguration.create();
		Connection connection = ConnectionFactory.createConnection(configuration);
		Table table = connection.getTable(TableName.valueOf("test"));

		for (int i = 0; i < 100; i++) {
			String row = "row" + i;
			String columnFamily = "data";
			String qualifier = "1";
			Get get = new Get(row.getBytes());
			get.addColumn(columnFamily.getBytes(), qualifier.getBytes());
			get.setTimeStamp(1578901903082L);

			Result res = table.get(get);
			Cell cell = res.getColumnLatestCell(columnFamily.getBytes(), qualifier.getBytes());

			System.out.println(new String(CellUtil.cloneRow(cell)) + "-" + new String(CellUtil.cloneFamily(cell)) + "-"
					+ new String(CellUtil.cloneQualifier(cell)) + "-" + new String(CellUtil.cloneValue(cell)));

		}
		table.close();
		connection.close();

	}
}
