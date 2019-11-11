package com.yangyoubiao.www.poi;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;



/**
 * 将文件由*.xlsx格式转换成txt格式
 * .xlsx文档的第一行数据通过getFirstFields()获得
 */
public class XlsxToTxt2 extends DefaultHandler {
	private SharedStringsTable sst;
	private OPCPackage pkg;
	
	private String lastContents;
	private boolean nextIsString;
	private int sheetIndex = -1;
	private List<String> rowlist = new ArrayList<String>();// 存储一行的数据
	private int curRow = 0;// 行从下标0开始
	private int curCol = 0;// 列从下标0开始
	private List<String> firstFields;// 存储第一行的数据

	private final static String def = "";// 当单元格为空时的默认值
	private BufferedWriter bufferedWriter;
	
	private String headCode = null;
	private String endCode = null;
	private boolean isDealColumn=true;//是否处理当前的C标签
	

	/**
	 * 只遍历一个sheet，其中sheetId为要遍历的sheet索引，从1开始，1-3
	 * 
	 * @param filename 文件路径
	 * @param sheetId  第几个sheet
	 * @param head     sheet的起始坐标 例如：A1
	 * @param end      sheet的结束坐标 例如：C22
	 */
	public void processOneSheet(String filename, int sheetId, String head, String end) throws Exception {
		headCode = head;
		endCode = end;
		OPCPackage pkg = OPCPackage.open(filename);
		XSSFReader r = new XSSFReader(pkg);
		SharedStringsTable sst = r.getSharedStringsTable();

		XMLReader parser = fetchSheetParser(sst);

		// rId2 found by processing the Workbook
		// 根据 rId# 或 rSheet# 查找sheet
		InputStream sheet2 = r.getSheet("rId" + sheetId);
		sheetIndex++;
		InputSource sheetSource = new InputSource(sheet2);
		parser.parse(sheetSource);
		sheet2.close();
	}

	/**
	 * 遍历 excel 文件
	 */
	public void process(String filename) throws Exception {
		OPCPackage pkg = null;
		pkg = OPCPackage.open(filename);
		XSSFReader r = new XSSFReader(pkg);
		setPkg(pkg);
		SharedStringsTable sst = r.getSharedStringsTable();
		XMLReader parser = fetchSheetParser(sst);
		Iterator<InputStream> sheets = r.getSheetsData();
		while (sheets.hasNext()) {
			curRow = 0;
			sheetIndex++;
			InputStream sheet = sheets.next();
			InputSource sheetSource = new InputSource(sheet);
			parser.parse(sheetSource);
			sheet.close();
		}
		pkg.close();
	}

	public void process(InputStream is) throws Exception {
		OPCPackage pkg = OPCPackage.open(is);
		XSSFReader r = new XSSFReader(pkg);
		SharedStringsTable sst = r.getSharedStringsTable();

		XMLReader parser = fetchSheetParser(sst);

		Iterator<InputStream> sheets = r.getSheetsData();
		while (sheets.hasNext()) {
			curRow = 0;
			sheetIndex++;
			InputStream sheet = sheets.next();
			InputSource sheetSource = new InputSource(sheet);
			parser.parse(sheetSource);
			sheet.close();
		}
	}

	public XMLReader fetchSheetParser(SharedStringsTable sst) throws SAXException {
		XMLReader parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
		this.sst = sst;
		parser.setContentHandler(this);
		return parser;
	}

	// headCode A1 cur B8 endCode Z10
	private boolean dealColumn(String cur, String headCode, String endCode) {
		if (headCode != null && endCode != null) {
			String curCol = cur.replaceAll("[0-9]", ""); // B
			long curRow = Long.valueOf(cur.replaceAll("[a-wA-W]", ""));// 8

			String headCol = headCode.replaceAll("[0-9]", ""); // A
			long headRow = Long.valueOf(headCode.replaceAll("[a-wA-W]", ""));// 1
			String endCol = endCode.replaceAll("[0-9]", ""); // Z
			long endRow = Long.valueOf(endCode.replaceAll("[a-wA-W]", ""));// 10

			if (curCol.compareTo(headCol) >= 0 && curCol.compareTo(endCol) <= 0 && curRow >= headRow
					&& curRow <= endRow) {
				return true;
			} else {
				return false;
			}
		}
		return true;
	}
	public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
		// c => 单元格
		if (name.equals("c")) {
			String r = attributes.getValue("r");// r元素时c标签的位置属性 例如【A1】
			
			isDealColumn = dealColumn(r, headCode, endCode);
			
			// 如果下一个元素是 SST 的索引，则将nextIsString标记为true
			String cellType = attributes.getValue("t");
			if (cellType != null && cellType.equals("s")) {
				nextIsString = true;
			} else {
				nextIsString = false;
			}

			// 从第二行开始补全rowlist里的数据【当单元格里没数据时，这个解析组件时没有对应的一个c标签的，这里手动补全】
			if (!"1".equals(r.replaceAll("[A-Z]", "")) && isDealColumn) {
				preRepairRowlist(r);
			}
		}
		// 置空
		lastContents = "";
	}

	public void endElement(String uri, String localName, String name) throws SAXException {
		// 根据SST的索引值的到单元格的真正要存储的字符串
		// 这时characters()方法可能会被调用多次
		if (nextIsString) {
			try {
				int idx = Integer.parseInt(lastContents);
				lastContents = new XSSFRichTextString(sst.getEntryAt(idx)).toString();
			} catch (Exception e) {
			}
		}

		// v => 单元格的值，如果单元格是字符串则v标签的值为该字符串在SST中的索引
		// 将单元格内容加入rowlist中，在这之前先去掉字符串前后的空白符
		if (name.equals("v")) {
			String value = lastContents.trim();
			value = value.equals("") ? " " : value;
			if (isDealColumn) {
				rowlist.add(curCol, value);
			}
			curCol++;
		} else {
			// 如果标签名称为 row ，这说明已到行尾，调用 optRows() 方法
			if (name.equals("row")) {
				try {
					// 设置第一行的数据
					if (curRow == 0) {
						this.setFirstFields(rowlist);
					} else {
						// 从第二行开始处理，写出数据
						suffixRepairRowlist();
						optRows(sheetIndex, curRow, rowlist);
					}
				} catch (Exception e) {
					e.printStackTrace();
					throw new SAXException(e.getMessage());
				}
				rowlist.clear();
				curRow++;
				curCol = 0;
			}
		}
	}

	public void characters(char[] ch, int start, int length) throws SAXException {
		// 得到单元格内容的值
		lastContents += new String(ch, start, length);
	}

	// excel记录行操作方法，以sheet索引，行索引和行元素列表为参数，对sheet的一行元素进行操作，元素为String类型
	// 从第二行开始
	public void optRows(int sheetIndex, int curRow, List<String> rowlist) throws Exception {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < rowlist.size(); i++) {
			if (sb.length() <= 0) {
				sb.append(rowlist.get(i));
			} else {
				sb.append("," + ObjectUtils.toString(rowlist.get(i)));
			}
		}
		this.getBufferedWriter().write(sb.toString() + "\n");
		if (curRow % 1000 == 0) {
			this.getBufferedWriter().flush();
		}
	}

	private void setFirstFields(List<String> rowlist) {
		List<String> firstFields = new LinkedList<String>();
		for (Object name : rowlist) {
			firstFields.add(name != null ? name + "" : "");
		}
		if (firstFields.isEmpty()) {
			throw new NullPointerException("导入字段定义为空");
		}
		this.firstFields = firstFields;
	}

	/**
	 * 若一行的单元格值如下：null,null,1,null,2,null【这里null表示单元格里没有值】<br>
	 * 这个方法的作用是补全1前的两个null，和2前的1个null<br>
	 * 
	 * @param r 当前要读取元素的位置【一定有值】
	 */
	private void preRepairRowlist(String r) {
		int num = excelColStrToNum(r);
		while (rowlist.size() < num) {
			rowlist.add(def);
			curCol++;
		}
	}

	/**
	 * 若一行的单元格值如下：null,null,1,null,2,null【这里null表示单元格里没有值】<br>
	 * 这个方法的作用是补全2后的一个null<br>
	 */
	private void suffixRepairRowlist() {
		while (rowlist.size() < firstFields.size()) {
			rowlist.add(def);
		}
	}

	/**
	 * excel列号转数字，下标从0开始<br>
	 * excel的坐标表示如下A1、AA1 ,这里的A、AA为列号
	 * 
	 * @param r excel的坐标：例如AA1
	 * @return
	 */
	public static int excelColStrToNum(String r) {
		String colStr = r.replaceAll("\\d", "");
		int length = colStr.length();
		int num = 0;
		int result = 0;
		for (int i = 0; i < length; i++) {
			char ch = colStr.charAt(length - i - 1);
			num = (int) (ch - 'A' + 1);
			num *= Math.pow(26, i);
			result += num;
		}
		return result - 1;
	}

	public List<String> getFirstFields() {
		return firstFields;
	}

	public OPCPackage getPkg() {
		return pkg;
	}

	public void setPkg(OPCPackage pkg) {
		this.pkg = pkg;
	}

	public BufferedWriter getBufferedWriter() {
		return bufferedWriter;
	}

	public void setBufferedWriter(BufferedWriter bufferedWriter) {
		this.bufferedWriter = bufferedWriter;
	}

	// 测试
	public static void main(String[] args) throws Exception {
		File tmpFile = new File("E:\\aaaa.txt");
		File parentFile = tmpFile.getParentFile();
		if (!parentFile.exists()) {
			parentFile.mkdirs();
		}
		tmpFile.createNewFile();
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tmpFile, true)));

		XlsxToTxt2 xxlsBig = new XlsxToTxt2();
		xxlsBig.setBufferedWriter(out);
		xxlsBig.processOneSheet("C:\\Users\\Administrator\\Desktop\\自主探索导入excel文件\\010.xlsx", 1, "A8", "C22");
		out.close();
	}

}