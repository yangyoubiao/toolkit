package com.yangyoubiao.www.poi;

import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.util.SAXHelper;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;

/**
 * <b> Event Model</b>
 * http://poi.apache.org/components/spreadsheet/how-to.html#xssf_sax_api
 * 
 * Event Model节约内存原理 :<br>
 * User
 * Model的缺点是一次性将文件读入内存,构建一颗Dom树.并且在POI对Excel的抽象中,每一行,每一个单元格都是一个对象.当文件大,数据量多的时候对内存的占用可想而知.
 * Event
 * Model使用的方式是边读取边解析,并且不会将这些数据封装成Row,Cell这样的对象.而都只是普通的数字或者是字符串.并且这些解析出来的对象是不需要一直驻留在内存中,
 * 而是解析完使用后就可以回收. 所以相比于User Model,Event Model更节省内存.效率也更高.但是作为代价,相比User
 * Model功能更少.门槛也要高一些.
 */
public class EventModelDemo {

	/**
	 * 处理第一个sheet
	 */
	public void processFirstSheet(String filename) throws Exception {
		// OPCPackage 能够存储多个数据对象的容器
		try (OPCPackage pkg = OPCPackage.open(filename, PackageAccess.READ)) {
			// XSSFReader 使得获得.xlsx文件变的容易，它是EventUserModel的核心
			XSSFReader r = new XSSFReader(pkg);
			// SharedStringsTable 工作簿中所有工作表共享的字符表
			SharedStringsTable sst = r.getSharedStringsTable();

			XMLReader parser = fetchSheetParser(sst);

			// process the first sheet
			try (InputStream sheet = r.getSheetsData().next()) {
				InputSource sheetSource = new InputSource(sheet);
				parser.parse(sheetSource);
			}
		}
	}

	/**
	 * 处理多个cheet
	 */
	public void processAllSheets(String filename) throws Exception {
		try (OPCPackage pkg = OPCPackage.open(filename, PackageAccess.READ)) {
			XSSFReader r = new XSSFReader(pkg);
			SharedStringsTable sst = r.getSharedStringsTable();

			XMLReader parser = fetchSheetParser(sst);

			Iterator<InputStream> sheets = r.getSheetsData();
			while (sheets.hasNext()) {
				System.out.println("Processing new sheet:\n");
				try (InputStream sheet = sheets.next()) {
					InputSource sheetSource = new InputSource(sheet);
					parser.parse(sheetSource);
				}
				System.out.println();
			}
		}
	}

	/**
	 * 读取xml文档的回调对象
	 */
	public XMLReader fetchSheetParser(SharedStringsTable sst) throws SAXException, ParserConfigurationException {
		XMLReader parser = SAXHelper.newXMLReader();
		ContentHandler handler = new SheetHandler(sst);
		parser.setContentHandler(handler);
		return parser;
	}

	/**
	 * See org.xml.sax.helpers.DefaultHandler javadocs
	 */
	private static class SheetHandler extends DefaultHandler {
		private final SharedStringsTable sst;
		private String lastContents;// 存储下一个元素的内容
		private boolean nextIsString;// 下一个元素是否是字符串
		private boolean inlineStr;// 下一个元素是否是lineStr
		private final LruCache<Integer, String> lruCache = new LruCache<>(50);

		private static class LruCache<A, B> extends LinkedHashMap<A, B> {
			private final int maxEntries;

			public LruCache(final int maxEntries) {
				super(maxEntries + 1, 1.0f, true);
				this.maxEntries = maxEntries;
			}

			@Override
			protected boolean removeEldestEntry(final Map.Entry<A, B> eldest) {
				return super.size() > maxEntries;
			}
		}

		private SheetHandler(SharedStringsTable sst) {
			this.sst = sst;
		}

		/**
		 * 接收开始的通知
		 */
		@Override
		public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
			// c => cell,c标签中的r元素表示位置，s表示元素类型
			if (name.equals("c")) {
				System.out.print(attributes.getValue("r") + " - ");
				String cellType = attributes.getValue("t");
				nextIsString = cellType != null && cellType.equals("s");
				inlineStr = cellType != null && cellType.equals("inlineStr");
			}
			// Clear contents cache
			lastContents = "";
		}

		/**
		 * 接收结束的通知
		 */
		@Override
		public void endElement(String uri, String localName, String name) throws SAXException {
			if (nextIsString) {
				Integer idx = Integer.valueOf(lastContents);
				System.out.print("【" + lastContents + "】");
				lastContents = lruCache.get(idx);
				if (lastContents == null && !lruCache.containsKey(idx)) {
					lastContents = new XSSFRichTextString(sst.getEntryAt(idx)).toString();
					lruCache.put(idx, lastContents);
				}
				nextIsString = false;
			}

			// v => contents of a cell
			// Output after we've seen the string contents
			if (name.equals("v") || (inlineStr && name.equals("c"))) {
				System.out.println(lastContents);
			}
		}

		/**
		 * 接受元素中字符的通知
		 */
		@Override
		public void characters(char[] ch, int start, int length) throws SAXException { // NOSONAR
			lastContents += new String(ch, start, length);
		}
	}

	public static void main(String[] args) throws Exception {
		EventModelDemo howto = new EventModelDemo();
		howto.processFirstSheet("E:\\aaaa.xlsx");
		// howto.processAllSheets(args[0]);
	}
}