package capanalyzer.netutils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import capanalyzer.GlobalConfig;
import capanalyzer.database.ConnectionPool;

import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.Map;
import java.util.HashMap;
import java.io.FileOutputStream;

public class QueryDbAndGenerateXlsFile
{
	protected static String					schemaName	= "capture_analyzer";
	
	protected static String					dbTableName	= "all_flows_2_packets_payload_new";

	/** JDBC driver name */
	private static String					driverName;

	/** JDBC connection URL */
	private static String					connURL;

	/** JDBC connection username */
	private static String					username;

	/** JDBC connection password */
	private static String					password;

	protected static Map<String, CellStyle>	styles;

	public static void main(String[] args) throws IOException, NetUtilsException
	{
		Connection con = null;

		try
		{
			setMySQLConnectInfo();
			con = getConnectionPool().getConnection();

			Workbook wb = new XSSFWorkbook();
			styles = createStyles(wb);

			getIpProtocolDistAndAddToExcel(con, wb);
			
			getPacketsPerFlowAndAddToExcel(con, wb);
			
			getFlowsPerDurationAndAddToExcel(con, wb);
			
			getTopPortsAndAddToExcel(con, wb);
			
			getFlowsPerMaxIpgAndAddToExcel(con, wb);
			
			getFlowsPerAverageIpgAndAddToExcel(con, wb);
			
			getFlowsPerMinIpgAndAddToExcel(con, wb);

			// Write the output to a file
			String file = "E:\\Capture_Files\\capture_summary.xlsx";
			FileOutputStream out = new FileOutputStream(file);
			wb.write(out);
			out.close();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (con != null)
					con.close();
			}
			catch (SQLException sqle)
			{
				sqle.printStackTrace();
			}
		}

	}

	/**
	 * @param con
	 * @param sheet
	 * @throws SQLException
	 * @throws NumberFormatException
	 * @throws FormulaParseException
	 */
	private static void getIpProtocolDistAndAddToExcel(Connection con, Workbook wb) throws SQLException, NumberFormatException, FormulaParseException
	{
		String sheetName = "IP Protocol Dist";
		String[] headers = new String[] { "protocol", "num of flows", "% of total" };
		String[][] queries = new String[][]{
				{"All Flows", 
					"select flow_type, count(*) as num_of_flows " + 
					"from " + schemaName + "." + dbTableName + " " +
					"group by flow_type " + 
					"order by flow_type asc"}};	
		
		Sheet sheet = addNewSheet(wb, sheetName);

		for (int q = 0; q < queries.length; q++)
		{
			String[][] queryResultOutputAs2dArray = runQueryAndGetResults(con, queries, q);
			
			addExcelTableHeader(headers, sheet, q, queries[q][0]);

			addExcelTableContent(headers, sheet, q, queryResultOutputAs2dArray);

			for (int i = 0; i < headers.length; i++)
			{
				sheet.autoSizeColumn(i +  q * (headers.length + 1)); // adjust width of the first column
			}		
		}
	}
	
	/**
	 * @param con
	 * @param sheet
	 * @throws SQLException
	 * @throws NumberFormatException
	 * @throws FormulaParseException
	 */
	private static void getPacketsPerFlowAndAddToExcel(Connection con, Workbook wb) throws SQLException, NumberFormatException, FormulaParseException
	{
		String sheetName = "Packets per Flow";
		String[] headers = new String[] { "num of pckt", "num of flows", "% of total" };
		String[][] queries = new String[][]{
				{"All Flows", 
					"select number_of_packets as num_of_pckt_in_flow, count(*) as num_of_flows " + 
					"from " + schemaName + "." + dbTableName + " " +
					"group by number_of_packets " + 
					"order by number_of_packets asc"},
				{"TCP Flows", 
					"select number_of_packets as num_of_pckt_in_flow, count(*) as num_of_flows " + 
					"from " + schemaName + "." + dbTableName + " " +  
					"where flow_type=6 " +
					"group by number_of_packets " + 
					"order by number_of_packets asc"},
				{"Full TCP Flows", 
					"select number_of_packets as num_of_pckt_in_flow, count(*) as num_of_flows " + 
					"from " + schemaName + "." + dbTableName + " " + 
					"where flow_type=6 and is_full_tcp=1 " +
					"group by number_of_packets " + 
					"order by number_of_packets asc"},
				{"UDP Flows", 
					"select number_of_packets as num_of_pckt_in_flow, count(*) as num_of_flows " + 
					"from " + schemaName + "." + dbTableName + " " +  
					"where flow_type=17 " +
					"group by number_of_packets " + 
					"order by number_of_packets asc"}};
		
		
		Sheet sheet = addNewSheet(wb, sheetName);

		for (int q = 0; q < queries.length; q++)
		{
			String[][] queryResultOutputAs2dArray = runQueryAndGetResults(con, queries, q);
			
			addExcelTableHeader(headers, sheet, q, queries[q][0]);

			addExcelTableContent(headers, sheet, q, queryResultOutputAs2dArray);

			for (int i = 0; i < headers.length; i++)
			{
				sheet.autoSizeColumn(i +  q * (headers.length + 1)); // adjust width of the first column
			}		
		}
	}
	
	/**
	 * @param con
	 * @param sheet
	 * @throws SQLException
	 * @throws NumberFormatException
	 * @throws FormulaParseException
	 */
	private static void getFlowsPerDurationAndAddToExcel(Connection con, Workbook wb) throws SQLException, NumberFormatException, FormulaParseException
	{
		String sheetName = "Flows per Duration";
		String[] headers = new String[] { "Duration(us)", "Num of Flows", "% of total" };
		String[][] queries = new String[][]{
				{"All Flows", 
					"select duration-(duration mod 1000000) as duration, count(duration - (duration mod 1000000)) as num_flows " +
					"from " + schemaName + "." + dbTableName + " " +
					"where number_of_packets>1 " +
					"group by duration - (duration mod 1000000) " +
					"order by duration - (duration mod 1000000) ASC"},	
				{"TCP Flows", 
					"select duration-(duration mod 1000000) as duration, count(duration - (duration mod 1000000)) as num_flows " +
					"from " + schemaName + "." + dbTableName + " " +
					"where number_of_packets>1 and flow_type=6 " +
					"group by duration - (duration mod 1000000) " +
					"order by duration - (duration mod 1000000) ASC"},
				{"Full TCP Flows", 
					"select duration-(duration mod 1000000) as duration, count(duration - (duration mod 1000000)) as num_flows " +
					"from " + schemaName + "." + dbTableName + " " +
					"where number_of_packets>1 and flow_type=6 and is_full_tcp=1 " +
					"group by duration - (duration mod 1000000) " +
					"order by duration - (duration mod 1000000) ASC"},
				{"UDP Flows", 
					"select duration-(duration mod 1000000) as duration, count(duration - (duration mod 1000000)) as num_flows " +
					"from " + schemaName + "." + dbTableName + " " +
					"where number_of_packets>1 and flow_type=17 " +
					"group by duration - (duration mod 1000000) " +
					"order by duration - (duration mod 1000000) ASC"}};
		
		
		Sheet sheet = addNewSheet(wb, sheetName);

		for (int q = 0; q < queries.length; q++)
		{
			String[][] queryResultOutputAs2dArray = runQueryAndGetResults(con, queries, q);
			
			addExcelTableHeader(headers, sheet, q, queries[q][0]);

			addExcelTableContent(headers, sheet, q, queryResultOutputAs2dArray);

			for (int i = 0; i < headers.length; i++)
			{
				sheet.autoSizeColumn(i +  q * (headers.length + 1)); // adjust width of the first column
			}		
		}
	}
	
	/**
	 * @param con
	 * @param sheet
	 * @throws SQLException
	 * @throws NumberFormatException
	 * @throws FormulaParseException
	 */
	private static void getTopPortsAndAddToExcel(Connection con, Workbook wb) throws SQLException, NumberFormatException, FormulaParseException
	{
		String sheetName = "Top Ports";
		String[] headers = new String[] { "port num", "num of flows", "% of total" };
		String[][] queries = new String[][]{
						
				{"All Flows", 
					"select dest_port, count(*) as num_of_flows " + 
					"from " + schemaName + "." + dbTableName + " " +
					"group by dest_port " + 
					"order by num_of_flows desc"},
				{"TCP Flows", 
					"select dest_port, count(*) as num_of_flows " + 
					"from " + schemaName + "." + dbTableName + " " +
					"where flow_type=6 " +
					"group by dest_port " + 
					"order by num_of_flows desc"},
				{"Full TCP Flows", 
					"select dest_port, count(*) as num_of_flows " + 
					"from " + schemaName + "." + dbTableName + " " +
					"where flow_type=6 and is_full_tcp=1 " +
					"group by dest_port " + 
					"order by num_of_flows desc"},
				{"UDP Flows", 
					"select dest_port, count(*) as num_of_flows " + 
					"from " + schemaName + "." + dbTableName + " " +
					"where flow_type=17 " +
					"group by dest_port " + 
					"order by num_of_flows desc"}};
		
		
		Sheet sheet = addNewSheet(wb, sheetName);

		for (int q = 0; q < queries.length; q++)
		{
			String[][] queryResultOutputAs2dArray = runQueryAndGetResults(con, queries, q);
			
			addExcelTableHeader(headers, sheet, q, queries[q][0]);

			addExcelTableContent(headers, sheet, q, queryResultOutputAs2dArray);

			for (int i = 0; i < headers.length; i++)
			{
				sheet.autoSizeColumn(i +  q * (headers.length + 1)); // adjust width of the first column
			}		
		}
	}
	
	/**
	 * @param con
	 * @param sheet
	 * @throws SQLException
	 * @throws NumberFormatException
	 * @throws FormulaParseException
	 */
	private static void getFlowsPerMaxIpgAndAddToExcel(Connection con, Workbook wb) throws SQLException, NumberFormatException, FormulaParseException
	{
		String sheetName = "Flows per Max IPG";
		String[] headers = new String[] { "max ipg", "Num of Flows", "% of total" };
		String[][] queries = new String[][]{
				{"All Flows", 
					"select max_ipg-(max_ipg mod 1000000) as max_ipg, count(max_ipg - (max_ipg mod 1000000)) as num_flows " +
					"from " + schemaName + "." + dbTableName + " " +
					"where number_of_packets>1 " +
					"group by max_ipg - (max_ipg mod 1000000) " + 
					"order by max_ipg - (max_ipg mod 1000000) ASC"},	
				{"TCP Flows", 
					"select max_ipg-(max_ipg mod 1000000) as max_ipg, count(max_ipg - (max_ipg mod 1000000)) as num_flows " +
					"from " + schemaName + "." + dbTableName + " " +
					"where number_of_packets>1 and flow_type=6 " +
					"group by max_ipg - (max_ipg mod 1000000) " + 
					"order by max_ipg - (max_ipg mod 1000000) ASC"},
				{"Full TCP Flows", 
					"select max_ipg-(max_ipg mod 1000000) as max_ipg, count(max_ipg - (max_ipg mod 1000000)) as num_flows " +
					"from " + schemaName + "." + dbTableName + " " +
					"where number_of_packets>1 and flow_type=6 and is_full_tcp=1 " +
					"group by max_ipg - (max_ipg mod 1000000) " + 
					"order by max_ipg - (max_ipg mod 1000000) ASC"},
				{"UDP Flows", 
					"select max_ipg-(max_ipg mod 1000000) as max_ipg, count(max_ipg - (max_ipg mod 1000000)) as num_flows " +
					"from " + schemaName + "." + dbTableName + " " +
					"where number_of_packets>1 and flow_type=17 " +
					"group by max_ipg - (max_ipg mod 1000000) " + 
					"order by max_ipg - (max_ipg mod 1000000) ASC"}};
		
		
		Sheet sheet = addNewSheet(wb, sheetName);

		for (int q = 0; q < queries.length; q++)
		{
			String[][] queryResultOutputAs2dArray = runQueryAndGetResults(con, queries, q);
			
			addExcelTableHeader(headers, sheet, q, queries[q][0]);

			addExcelTableContent(headers, sheet, q, queryResultOutputAs2dArray);

			for (int i = 0; i < headers.length; i++)
			{
				sheet.autoSizeColumn(i +  q * (headers.length + 1)); // adjust width of the first column
			}		
		}
	}
	
	private static void getFlowsPerAverageIpgAndAddToExcel(Connection con, Workbook wb) throws SQLException, NumberFormatException, FormulaParseException
	{
		String sheetName = "Flows per Average IPG";
		String[] headers = new String[] { "average ipg", "Num of Flows", "% of total" };
		String[][] queries = new String[][]{
				{"All Flows", 
					"select average_ipg-(average_ipg mod 1000000) as average_ipg, count(average_ipg - (average_ipg mod 1000000)) as num_flows " +
					"from " + schemaName + "." + dbTableName + " " +
					"where number_of_packets>1 " +
					"group by average_ipg - (average_ipg mod 1000000) " + 
					"order by average_ipg - (average_ipg mod 1000000) ASC"},	
				{"TCP Flows", 
					"select average_ipg-(average_ipg mod 1000000) as average_ipg, count(average_ipg - (average_ipg mod 1000000)) as num_flows " +
					"from " + schemaName + "." + dbTableName + " " +
					"where number_of_packets>1 and flow_type=6 " +
					"group by average_ipg - (average_ipg mod 1000000) " + 
					"order by average_ipg - (average_ipg mod 1000000) ASC"},
				{"Full TCP Flows", 
					"select average_ipg-(average_ipg mod 1000000) as average_ipg, count(average_ipg - (average_ipg mod 1000000)) as num_flows " +
					"from " + schemaName + "." + dbTableName + " " +
					"where number_of_packets>1 and flow_type=6 and is_full_tcp=1 " +
					"group by average_ipg - (average_ipg mod 1000000) " + 
					"order by average_ipg - (average_ipg mod 1000000) ASC"},
				{"UDP Flows", 
					"select average_ipg-(average_ipg mod 1000000) as average_ipg, count(average_ipg - (average_ipg mod 1000000)) as num_flows " +
					"from " + schemaName + "." + dbTableName + " " +
					"where number_of_packets>1 and flow_type=17 " +
					"group by average_ipg - (average_ipg mod 1000000) " + 
					"order by average_ipg - (average_ipg mod 1000000) ASC"}};
		
		
		Sheet sheet = addNewSheet(wb, sheetName);

		for (int q = 0; q < queries.length; q++)
		{
			String[][] queryResultOutputAs2dArray = runQueryAndGetResults(con, queries, q);
			
			addExcelTableHeader(headers, sheet, q, queries[q][0]);

			addExcelTableContent(headers, sheet, q, queryResultOutputAs2dArray);

			for (int i = 0; i < headers.length; i++)
			{
				sheet.autoSizeColumn(i +  q * (headers.length + 1)); // adjust width of the first column
			}		
		}
	}
	
	private static void getFlowsPerMinIpgAndAddToExcel(Connection con, Workbook wb) throws SQLException, NumberFormatException, FormulaParseException
	{
		String sheetName = "Flows per Min IPG";
		String[] headers = new String[] { "min ipg", "Num of Flows", "% of total" };
		String[][] queries = new String[][]{
				{"All Flows", 
					"select min_ipg-(min_ipg mod 1000000) as min_ipg, count(min_ipg - (min_ipg mod 1000000)) as num_flows " +
					"from " + schemaName + "." + dbTableName + " " +
					"where number_of_packets>1 " +
					"group by min_ipg - (min_ipg mod 1000000) " + 
					"order by min_ipg - (min_ipg mod 1000000) ASC"},	
				{"TCP Flows", 
					"select min_ipg-(min_ipg mod 1000000) as min_ipg, count(min_ipg - (min_ipg mod 1000000)) as num_flows " +
					"from " + schemaName + "." + dbTableName + " " +
					"where number_of_packets>1 and flow_type=6 " +
					"group by min_ipg - (min_ipg mod 1000000) " + 
					"order by min_ipg - (min_ipg mod 1000000) ASC"},
				{"Full TCP Flows", 
					"select min_ipg-(min_ipg mod 1000000) as min_ipg, count(min_ipg - (min_ipg mod 1000000)) as num_flows " +
					"from " + schemaName + "." + dbTableName + " " +
					"where number_of_packets>1 and flow_type=6 and is_full_tcp=1 " +
					"group by min_ipg - (min_ipg mod 1000000) " + 
					"order by min_ipg - (min_ipg mod 1000000) ASC"},
				{"UDP Flows", 
					"select min_ipg-(min_ipg mod 1000000) as min_ipg, count(min_ipg - (min_ipg mod 1000000)) as num_flows " +
					"from " + schemaName + "." + dbTableName + " " +
					"where number_of_packets>1 and flow_type=17 " +
					"group by min_ipg - (min_ipg mod 1000000) " + 
					"order by min_ipg - (min_ipg mod 1000000) ASC"}};
		
		
		Sheet sheet = addNewSheet(wb, sheetName);

		for (int q = 0; q < queries.length; q++)
		{
			String[][] queryResultOutputAs2dArray = runQueryAndGetResults(con, queries, q);
			
			addExcelTableHeader(headers, sheet, q, queries[q][0]);

			addExcelTableContent(headers, sheet, q, queryResultOutputAs2dArray);

			for (int i = 0; i < headers.length; i++)
			{
				sheet.autoSizeColumn(i +  q * (headers.length + 1)); // adjust width of columns
			}		
		}
	}

	/**
	 * @param headers
	 * @param sheet
	 * @param q
	 * @param queryResultOutputAs2dArray
	 * @return
	 * @throws NumberFormatException
	 * @throws FormulaParseException
	 */
	private static void addExcelTableContent(String[] headers, Sheet sheet, int q, String[][] queryResultOutputAs2dArray) throws NumberFormatException, FormulaParseException
	{
		Row row = null;
		Cell cell;
		int rownum = 2;
		for (int i = 0; i < queryResultOutputAs2dArray.length; i++, rownum++)
		{
			if(q==0)
				row = sheet.createRow(rownum);
			else
				row = sheet.getRow(rownum);
			
			if (queryResultOutputAs2dArray[i] == null)
				continue;

			for (int j = 0; j < queryResultOutputAs2dArray[i].length; j++)
			{
				cell = row.createCell(j + q * (headers.length + 1));
				cell.setCellValue(Double.parseDouble(queryResultOutputAs2dArray[i][j]));
				cell.setCellStyle(styles.get("cell_numbber"));
			}

			cell = row.createCell(queryResultOutputAs2dArray[i].length + q * (headers.length + 1));
			int r = rownum + 1;
			String fmla = null;
			if(q==0)
				fmla = "IF(B" + r + ",B" + r + "/SUM(B1:B" + (queryResultOutputAs2dArray.length) + "),\"\")";
			else if(q==1)
				fmla = "IF(F" + r + ",F" + r + "/SUM(F1:F" + (queryResultOutputAs2dArray.length) + "),\"\")";
			else if(q==2)
				fmla = "IF(J" + r + ",J" + r + "/SUM(J1:J" + (queryResultOutputAs2dArray.length) + "),\"\")";
			else if(q==3)
				fmla = "IF(N" + r + ",N" + r + "/SUM(N1:N" + (queryResultOutputAs2dArray.length) + "),\"\")";
			cell.setCellFormula(fmla);
			cell.setCellStyle(styles.get("cell_percentage"));
		}
	}
	
	/**
	 * @param con
	 * @param queries
	 * @param q
	 * @return
	 * @throws SQLException
	 */
	private static String[][] runQueryAndGetResults(Connection con, String[][] queries, int q) throws SQLException
	{
		String[][] queryResultOutputAs2dArray;
		PreparedStatement ps = con.prepareStatement(queries[q][1]);
		ResultSet rs = ps.executeQuery();
		String queryResultOutput = "";

		int numberOfColumns = rs.getMetaData().getColumnCount();
		while (rs.next())// still have records...
		{
			for (int i = 1; i <= numberOfColumns; i++)
			{
				queryResultOutput += rs.getString(i) + ((i!=numberOfColumns)?",":"");
			}
			queryResultOutput += "\n";
		}

		queryResultOutputAs2dArray = getArrayFromString(queryResultOutput);
		return queryResultOutputAs2dArray;
	}

	/**
	 * @param wb
	 * @param sheetName
	 * @return
	 */
	private static Sheet addNewSheet(Workbook wb, String sheetName)
	{
		Sheet sheet = wb.createSheet(sheetName);
		
		// turn off gridlines
		sheet.setDisplayGridlines(false);
		sheet.setPrintGridlines(false);
		sheet.setFitToPage(true);
		sheet.setHorizontallyCenter(true);
		PrintSetup printSetup = sheet.getPrintSetup();
		printSetup.setLandscape(true);
		return sheet;
	}

	/**
	 * @param headers
	 * @param sheet
	 * @param q
	 */
	private static void addExcelTableHeader(String[] headers, Sheet sheet, int q, String tableHeader)
	{
		// the header row: centered text in 48pt font
		Row headerRow1 = null;
		Row headerRow2 = null;
		if(q==0)
		{
			headerRow1 = sheet.createRow(0);
			headerRow1.setHeightInPoints(12.75f);
			
			headerRow2 = sheet.createRow(1);
			headerRow2.setHeightInPoints(12.75f);
		}
		else
		{
			headerRow1 = sheet.getRow(0);	
			headerRow2 = sheet.getRow(1);
		}
		
		Cell cell = headerRow1.createCell(q*(headers.length+1));
		cell.setCellValue(tableHeader);
		cell.setCellStyle(styles.get("header"));
		sheet.addMergedRegion(new CellRangeAddress(
	            0, 										//first row (0-based)
	            0, 										//last row  (0-based)
	            q*(headers.length+1), 					//first column (0-based)
	            q*(headers.length+1) + headers.length-1 ));//last column  (0-based)
		
		for (int i = 0; i < headers.length; i++)
		{
			Cell cellh = headerRow2.createCell(i + q*(headers.length+1));
			cellh.setCellValue(headers[i]);
			cellh.setCellStyle(styles.get("header"));
		}
		
		// freeze the first row
		sheet.createFreezePane(0, 2);
	}

	/**
	 * Creates a new connection pool object
	 * 
	 * @return ConnectionPool
	 */
	public static ConnectionPool getConnectionPool()
	{
		return (new ConnectionPool(1, driverName, connURL, username, password));
	}

	/**
	 * Sets fields to values required to connect to a sample MySQL database
	 */
	private static void setMySQLConnectInfo()
	{
		driverName = GlobalConfig.Database.getDriverName();
		connURL = GlobalConfig.Database.getConnURL();
		username = GlobalConfig.Database.getUsername();
		password = GlobalConfig.Database.getPassword();
	}

	/**
	 * create a library of cell styles
	 */
	private static Map<String, CellStyle> createStyles(Workbook wb)
	{
		styles = new HashMap<String, CellStyle>();
		DataFormat df = wb.createDataFormat();

		CellStyle style;
		Font headerFont = wb.createFont();
		headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
		style = createBorderedStyle(wb);
		style.setAlignment(CellStyle.ALIGN_CENTER);
		style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
		style.setFillPattern(CellStyle.SOLID_FOREGROUND);
		style.setFont(headerFont);
		styles.put("header", style);

		Font font1 = wb.createFont();
		font1.setBoldweight(Font.BOLDWEIGHT_BOLD);
		style = createBorderedStyle(wb);
		style.setAlignment(CellStyle.ALIGN_LEFT);
		style.setFont(font1);
		styles.put("cell_b", style);

		style = createBorderedStyle(wb);
		style.setAlignment(CellStyle.ALIGN_RIGHT);
		style.setFont(font1);
		style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		style.setFillPattern(CellStyle.SOLID_FOREGROUND);
		style.setDataFormat(df.getFormat("d-mmm"));
		styles.put("cell_g", style);

		style = createBorderedStyle(wb);
		style.setAlignment(CellStyle.ALIGN_LEFT);
		style.setDataFormat(df.getFormat("###,###,###,##0"));
		styles.put("cell_numbber", style);

		style = createBorderedStyle(wb);
		style.setAlignment(CellStyle.ALIGN_LEFT);
		style.setDataFormat(df.getFormat("0.00%"));
		styles.put("cell_percentage", style);

		return styles;
	}

	private static CellStyle createBorderedStyle(Workbook wb)
	{
		CellStyle style = wb.createCellStyle();
		style.setBorderRight(CellStyle.BORDER_THIN);
		style.setRightBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderBottom(CellStyle.BORDER_THIN);
		style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderLeft(CellStyle.BORDER_THIN);
		style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderTop(CellStyle.BORDER_THIN);
		style.setTopBorderColor(IndexedColors.BLACK.getIndex());
		return style;
	}

	/**
	 * translate String to a 2D array
	 * 
	 * @param theString
	 * @return 2D array represent the string
	 */
	private static String[][] getArrayFromString(String theString)
	{
		String[] tempLinesArray = null;
		String[][] full2DArray = null;

		String csvAsString = theString;
		tempLinesArray = csvAsString.split("\n"); // lines
		full2DArray = new String[tempLinesArray.length][];
		for (int line = 0; line < tempLinesArray.length; line++)
			full2DArray[line] = tempLinesArray[line].split(",");

		return full2DArray;
	}
}
