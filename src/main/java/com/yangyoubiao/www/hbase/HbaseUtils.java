package com.yangyoubiao.www.hbase;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.filter.SubstringComparator;

/**
 * hbase公共方法
 */
public class HbaseUtils {
	/**
	 * 获取hbase连接<br>
	 * 这里默认读取/toolkit/src/main/resources/hbase-site.xml、hdfs-site.xml两个配置文件
	 */
	public static Connection getConn() throws IOException {
		Configuration configuration = HBaseConfiguration.create();
		Connection connection = ConnectionFactory.createConnection(configuration);
		return connection;
	}

	/**
	 * 获取phoenix表的信息，返回phoenix表的字段名、字段名对应的字典值<br>
	 * 
	 * 通过phoenix在hbase建立的表，表元数据存储在SYSTEM.CATALOG表中，表名区分大小写<br>
	 * phoenix表中id对应的是hbase中的rowID<br>
	 * phoenix表中的其他字段在hbase中存储通过字典值来存储【ps:可能是为了节约空间和规范存储格式，一个字典值用2byte表示】
	 * 
	 * <br>
	 * 举例：<br>
	 * TESTZXH表ADDRESS字段的rowID：\x00\x00TESTZXH\x00ADDRESS\x000<br>
	 * TESTZXH表Id字段的rowID： \x00\x00TESTZXH\x00ID<br>
	 * TESTZXH表NAME字段的rowID：\x00\x00TESTZXH\x00NAME\x000
	 * 
	 * ADDRESS、name字段的字典值存储在'0:COLUMN_QUALIFIER'列；ID字段的字段值默认为：\x00\x00\x00\x00
	 * 
	 * 返回：{"ADDRESS"=[-128, 12],"ID"=[0, 0, 0, 0],"NAME"=[-128, 11]}
	 * 
	 * @param tableName 表名 区分大小写
	 * @throws IOException
	 */
	public static Map<String, byte[]> getTableInfo(String tableName) throws IOException {
		Map<String, byte[]> res = new HashMap<String, byte[]>();
		try (Connection connection = getConn();) {
			// 给表名添加前后缀 TESTZXH--->\x00\x00TESTZXH\x00
			byte[] arr = tableName.getBytes();
			byte[] tableNameArr = new byte[arr.length + 3];
			for (int i = 0; i < tableNameArr.length - 1; i++) {
				if (i == 0 || i == 1) {
					tableNameArr[i] = 0x00;
				} else {
					tableNameArr[i] = arr[i - 2];
				}
			}
			tableNameArr[tableNameArr.length - 1] = 0x00;
			tableName = new String(tableNameArr);

			// 获取表SYSTEM.CATALOG
			String family = "0";
			Table table = connection.getTable(TableName.valueOf("SYSTEM.CATALOG"));

			// 增加过滤条件
			Scan scan = new Scan();
			scan.setStartRow(tableNameArr);
			FilterList list = new FilterList(FilterList.Operator.MUST_PASS_ALL);
			RowFilter rowFilter = new RowFilter(CompareFilter.CompareOp.EQUAL, new SubstringComparator(tableName));
			list.addFilter(rowFilter);
			scan.setFilter(list);
			ResultScanner scanner = table.getScanner(scan);
			// 遍历结果集
			Iterator<Result> iterator = scanner.iterator();
			while (iterator.hasNext()) {
				Result result = iterator.next();
				byte[] row = result.getRow();
				
				if (row[tableNameArr.length] != 0x00) {
					// 其他字段
					if (result.containsColumn(family.getBytes(), "COLUMN_QUALIFIER".getBytes())) {
						Cell cell = result.getColumnLatestCell(family.getBytes(), "COLUMN_QUALIFIER".getBytes());
						byte[] value = CellUtil.cloneValue(cell);
						int from = tableNameArr.length;
						int to = tableNameArr.length;
						for (; to < row.length; to++) {
							if (row[to] == 0x00) {
								break;
							}
						}
						byte[] name = Arrays.copyOfRange(row, from, to);
						res.put(new String(name), value);

					} else {
						// 主键
						byte[] value = new byte[] { 0x00, 0x00, 0x00, 0x00 };
						int from = tableNameArr.length;
						int to = tableNameArr.length;
						for (; to < row.length; to++) {
							if (row[to] == 0x00) {
								break;
							}
						}
						byte[] name = Arrays.copyOfRange(row, from, to);
						res.put(new String(name), value);
					}
				}

			}
			table.close();
		}
		return res;
	}

	public static void main(String[] args) throws IOException {
		Map<String, byte[]> map=getTableInfo("TOTAL_PRODUCT_USER2");
		Set<String> set=map.keySet();
		for(String str: set) {
			System.out.println(str+":"+Arrays.toString(map.get(str)));
		}
	}
}
