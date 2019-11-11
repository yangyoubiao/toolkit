package com.yangyoubiao.www.poi;

import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;

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

/**
 * 将.xlsx文件转换成csv文件，单元格中出现逗号，使用双引号将单元格内容包括起来<br>
 * csv的列数，通过第一行的列数来确定
 */
public class XlsxToCsv {
	private char separator = ',';
	public static void main(String[] args) {
		String xlsx = "E:\\aaaa.xlsx";
		String csv = "E:\\aaaa.txt";
		

	}

//	private 

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
	 * 读取xml文档的回调对象
	 */
	public XMLReader fetchSheetParser(SharedStringsTable sst) throws SAXException, ParserConfigurationException {
		XMLReader parser = SAXHelper.newXMLReader();
		ContentHandler handler = new SheetHandler(sst);
		parser.setContentHandler(handler);
		return parser;
	}

	private static class SheetHandler extends DefaultHandler {
		private SharedStringsTable sst;
		private String lastContents;
		private boolean nextIsString;

		private SheetHandler(SharedStringsTable sst) {
			this.sst = sst;
		}

		public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
			// c => cell
			if (name.equals("c")) {
				// Print the cell reference
				System.out.print(attributes.getValue("r") + " - ");
				// Figure out if the value is an index in the SST
				String cellType = attributes.getValue("t");
				if (cellType != null && cellType.equals("s")) {
					nextIsString = true;
				} else {
					nextIsString = false;
				}
			}
			// Clear contents cache
			lastContents = "";
		}

		public void endElement(String uri, String localName, String name) throws SAXException {
			// Process the last contents as required.
			// Do now, as characters() may be called more than once
			if (nextIsString) {
				int idx = Integer.parseInt(lastContents);
				lastContents = new XSSFRichTextString(sst.getEntryAt(idx)).toString();
				nextIsString = false;
			}

			// v => contents of a cell
			// Output after we've seen the string contents
			if (name.equals("v")) {
				System.out.println(lastContents);
			}
		}

		public void characters(char[] ch, int start, int length) throws SAXException {
			lastContents += new String(ch, start, length);
		}
	}

}
