package com.yangyoubiao.www.hbase;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

/**
 * 要运行这个测试：需要把/toolkit/src/main/resources/hbase-site.xml这个配置文件给干掉
 */
public class Test {
	public static void main(String[] args) throws IOException {
		// 第一步，设置HBsae配置信息
		Configuration configuration = HBaseConfiguration.create();
		// 注意。这里这行目前没有注释掉的，这行和问题3有关系 是要根据自己zookeeper.znode.parent的配置信息进行修改。

//		configuration.set("hbase.rootdir", "hdfs://gzdxmcservice/hbase");
//		configuration.set("hbase.replication", "true");
//		configuration.set("hbase.client.write.buffer", "2097152");
//		configuration.set("hbase.client.pause", "100");
//		configuration.set("hbase.client.retries.number", "35");
//		configuration.set("hbase.client.scanner.caching", "100");
//		configuration.set("hbase.client.keyvalue.maxsize", "10485760");
//		configuration.set("hbase.ipc.client.allowsInterrupt", "true");
//		configuration.set("hbase.client.primaryCallTimeout.get", "10");
//		configuration.set("hbase.client.primaryCallTimeout.multiget", "10");
//		configuration.set("hbase.coprocessor.region.classes",
//				"org.apache.hadoop.hbase.security.access.SecureBulkLoadEndpoint");
//		configuration.set("hbase.regionserver.thrift.http", "false");
//		configuration.set("hbase.thrift.support.proxyuser", "false");
//		configuration.set("hbase.rpc.timeout", "60000");
//		configuration.set("hbase.snapshot.enabled", "true");
//		configuration.set("hbase.snapshot.master.timeoutMillis", "60000");
//		configuration.set("hbase.snapshot.region.timeout", "60000");
//		configuration.set("hbase.security.authentication", "simple");
//		configuration.set("hbase.rpc.protection", "authentication");
//		configuration.set("zookeeper.session.timeout", "60000");
//		configuration.set("hbase.rest.ssl.enabled", "false");
//		configuration.set("zookeeper.znode.parent", "/hbase"); // 与 hbase-site-xml里面的配置信息 zookeeper.znode.parent 一致
//		configuration.set("zookeeper.znode.rootserver", "/root-region-server");
//		configuration.set("hbase.zookeeper.quorum", "gzdxmccdh02,gzdxmccdh05,gzdxmccdh08,gzdxmccdh03"); // hbase 服务地址
//		configuration.set("hbase.zookeeper.property.clientPort", "2181"); // 端口号

		try {
			Connection conn = ConnectionFactory.createConnection(configuration);
			for (int i = 0; i < 100; i++) {
				System.out.println("times : " + i);

//				Table table = conn.getTable(TableName.valueOf("user_data.2019-12-25"));
//				Get g = new Get(Bytes.toBytes("04033129331"));
//				Result result = table.get(g);
//				table.close();
//				System.out.println("user_data.2019-12-25 : " + result.toString());

				Table table2 = conn.getTable(TableName.valueOf("test"));
				Get g2 = new Get(Bytes.toBytes("1578898160204"));
				Result result2 = table2.get(g2);
				table2.close();
				System.out.println("acc_act : " + result2.toString());

			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
