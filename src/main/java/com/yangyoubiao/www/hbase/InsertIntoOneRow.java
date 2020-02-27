package com.yangyoubiao.www.hbase;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;

/**
 * 插入一条数据
 */
public class InsertIntoOneRow {
	public static void main(String[] args) throws IOException {
		Configuration configuration = HBaseConfiguration.create();
		Connection connection = ConnectionFactory.createConnection(configuration);
		Table table = connection.getTable(TableName.valueOf("TESTZXH"));

		byte[] row = "3".getBytes();
		byte[] columnFamily = "0".getBytes();

		Put put = new Put(row);
		put.addColumn(columnFamily, new byte[] { 0x00, 0x00, 0x00, 0x00 }, "x".getBytes());
		put.addColumn(columnFamily, new byte[] { (byte) 0x80, 0x0B }, "yyb".getBytes());//name
		put.addColumn(columnFamily, new byte[] { (byte) 0x80, 0x0C }, "china".getBytes());//address

		table.put(put);

		table.close();
		connection.close();
	}
}
