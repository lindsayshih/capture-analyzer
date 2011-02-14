package capanalyzer.database;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.*;
import java.math.BigDecimal;
import java.sql.*;

/**
 * SQLResults stores the results of a sql query in the form of objects stored in an Arraylist
 * container object. It contains handy methods like getLong(), getDate(), getString(), etc.
 * for accessing fields in a SQLResults (result set). <br><br>
 *
 * Here is a complete code example:<br>
 * <pre>
 *   driverName = "oracle.jdbc.driver.OracleDriver";
 *   connURL = "jdbc:oracle:thin:@SNOWMASS:1521:WDEV";
 *   username = "wdevprja";
 *   password = "password";
 *   ConnectionPool conPool = new ConnectionPool(int numPooledCon,
 *                                               String driverName,
 *                                               String conURL,
 *                                               String username,
 *                                               String password);
 *
 *   SQLExecutor sqlExec = new SQLExecutor(conPool);
 *   <b>SQLResults res = sqlExec.runQueryCloseCon("SELECT * FROM INV");
 *   String out = "";
 *   for (int row=0; row < res.getRowCount(); row++)
 *      out += res.getLong(row, "TEST_ID") + " " + res.getString(row, "NOTES") + " " +
 *             res.getDate(row, "TEST_DT") + " " + res.getDouble(row, "AMOUNT") + " " +
 *             res.getString(row, "CODE") + "\n";</b>
 *   System.out.println(out);
 * </pre>
 *
 * There is also a handy toString() method which can be used to generate a text table
 * of the entire contents of the SQLResults result set. Here is a code example:
 * <pre>
 *   System.out.println(res.toString());  //output results as a text table
 * </pre>
 * For a simple employee table, the output of this toString() method will look
 * something like:<br>
 * <pre>
 * EMPID       FNAME       LNAME
 * ----------------------------------
 * 1.0         mark        cook
 * 2.0         carlos      moya
 * 3.0         Jeff        Smith
 * </pre>
 * @author Jeff S Smith
 */
public class SQLResults
{
	/** ArrayList containing the results of the sql query */
	private ArrayList<Object> results = null;

	/** ArrayList containing the column names returned by the sql query */
	private ArrayList<String> columnNames = null;

	/** number of columns returned by the sql query */
	private int columnCount = 0;

	/** database type (e.g. DatabaseType.ORACLE), needed for exception handling */
	private int dbType;

	/** formatted width of each field included in toString() */
	private int toStringFormatWidth = 12;

	/**
	 * Constructor creates ArrayList objects and initializes the dbType and columnCount
	 * @param dbType
	 * @param columnCount
	 */
	public SQLResults(int dbType, int columnCount)
	{
		results = new ArrayList<Object>();
		columnNames = new ArrayList<String>();
		this.dbType = dbType;
		this.columnCount = columnCount;
	}

	/**
	 * Getter for the entire SQL results object
	 * @return sql results
	 */
	public ArrayList<Object> getResults()
	{
		return(results);
	}

	/**
	 * Getter for number of columns
	 * @return count of columns in the result set
	 */
	public int getColumnCount()
	{
		return(columnNames.size());
	}

	/**
	 * Getter for SQL column names
	 * @return arraylist containing the column names
	 */
	public ArrayList<String> getColumnNames()
	{
		return(columnNames);
	}

	/**
	 * Returns the SQLResults in the form of a List
	 * @return List
	 */
	public List<Object> toList()
	{
		return(results);
	}

	/**
	 * Setter for string format width (used in toString() method)
	 * @param toStringFormatWidth
	 */
	public void setToStringFormatWidth(int toStringFormatWidth)
	{
		this.toStringFormatWidth = toStringFormatWidth;
	}

	/**
	 * Adds an object (a field value from a SELECT) to the sql results container)
	 * @param o
	 */
	public boolean add(Object o)
	{
		return(results.add(o));
	}

	/**
	 * Gets the number of rows returned by query
	 * @return number of rows
	 */
	public int getRowCount()
	{
		int rowCnt = results.size() / columnCount;
		return(rowCnt);
	}

	/**
	 * GetObject returns an object in the ArrayList results given a row and column index
	 * @param row
	 * @param col
	 * @return object
	 */
	public Object getObject(int row, int col)
	{
		int index = (row * columnCount) + col;
		return(results.get(index));
	}

	/**
	 * Adds a column name to the columnNames list
	 * @param columnName columnName to add
	 */
	public void addColumnName(String columnName)
	{
		columnNames.add(columnName);
	}

	/**
	 * Given a columnName, return the corresponding index
	 * @param columnName
	 * @return columnName corresponding to index
	 */
	private int getColumnIndex(String columnName)
	{
		int colIndex = -1;
		for (int i=0; i < columnNames.size(); i++)
		{
			String thisColumnName = (String)columnNames.get(i);
			if (thisColumnName.equalsIgnoreCase(columnName))
			{
				colIndex = i;
				break;
			}
		}

		if (colIndex < 0)
			throw ExceptionFactory.getException(dbType, "Invalid column name: " + columnName);
		return(colIndex);
	}

	/**
	 * Gets the value of the field corresponding to row, columnName as an int
	 * @param row
	 * @param columnName
	 * @return int value of field
	 */
	public int getInt(int row, String columnName)
	{
		return(getInt(row, getColumnIndex(columnName)));
	}

	/**
	 * Gets the value of the field corresponding to (row, col) index as an int
	 * @param row
	 * @param col
	 * @return int value of field
	 */
	public int getInt(int row, int col)
	{
		if (isNull(row, col))
			return(0);

		Object o = getObject(row, col);
		if (o instanceof Integer)
			return(((Integer)o).intValue());
		else
			return(((BigDecimal)o).intValue());
	}

	/**
	 * Gets the value of the field corresponding to row, columnName as a long
	 * @param row int
	 * @param columnName String
	 * @return long value of field
	 */
	public long getLong(int row, String columnName)
	{
		return(getLong(row, getColumnIndex(columnName)));
	}

	/**
	 * Gets the value of the field corresponding to row, col as a long
	 * @param row
	 * @param col
	 * @return long
	 */
	public long getLong(int row, int col)
	{
		if (isNull(row, col))
			return(0);

		Object o = getObject(row, col);
		if (o instanceof Long)
			return(((Long)o).longValue());
		else if (o instanceof Integer)
			return(((Integer)o).longValue());
		else
			return(((BigDecimal)o).longValue());
	}

	/**
	 * Gets the value of the field corresponding to (row, col) index as a String
	 * @param row int
	 * @param col int
	 * @return String value of field
	 */
	public String getString(int row, int col)
	{
		Object o = getObject(row, col);
		if (o instanceof BigDecimal)
		{
			BigDecimal b = (BigDecimal)o;
			return("" + getDouble(row, col));
		}
		else if (o instanceof Integer)
			return("" + getInt(row, col));
		else if (o instanceof Long)
			return("" + getLong(row, col));
		else if (o instanceof Boolean)
			return("" + getBoolean(row, col));
		else if (o instanceof Date)
			return("" + getDate(row, col));
		else if (o instanceof Timestamp)
			return("" + getTimestamp(row, col));
		else if (o instanceof Time)
			return("" + getTime(row, col));
		else if (o instanceof Float)
			return("" + getFloat(row, col));
		else if (o instanceof Double)
			return("" + getDouble(row, col));
		else
		{
			String s = (String)o;
			return(s);
		}
	}

	/**
	 * Gets the value of the field corresponding to row, columnName as a String
	 * @param row int
	 * @param columnName String
	 * @return String value of field
	 */
	public String getString(int row, String columnName)
	{
		return(getString(row, getColumnIndex(columnName)));
	}

	/**
	 * Gets the value of the field corresponding to (row, col) as a boolean
	 * @param row int
	 * @param col int
	 * @return boolean value of field
	 */
	public boolean getBoolean(int row, int col)
	{
		if (isNull(row, col))
			return(false);

		Object o = getObject(row, col);
		Boolean b = (Boolean)o;
		return(b.booleanValue());
	}

	/**
	 * Gets the value of the field corresponding to row, columnName as a boolean
	 * @param row int
	 * @param columnName String
	 * @return boolean value of field
	 */
	public boolean getBoolean(int row, String columnName)
	{
		return(getBoolean(row, getColumnIndex(columnName)));
	}

	/**
	 * Gets the value of the field corresponding to (row, col) as a java.sql.Date
	 * @param row int
	 * @param col int
	 * @return Date value of field
	 */
	public Date getDate(int row, int col)
	{
		if (isNull(row, col))
			return(null);

		Object o = getObject(row, col);
		if (o instanceof Timestamp)
		{
			Timestamp t = (Timestamp)o;
			Date d = new Date(t.getTime());
			return(d);
		}
		else
		{
			Date d = (Date)o;
			return(d);
		}
	}

	/**
	 * Gets the value of the field corresponding to row, columnName as a java.sql.Date
	 * @param row int
	 * @param columnName String
	 * @return Date value of field
	 */
	public Date getDate(int row, String columnName)
	{
		return(getDate(row, getColumnIndex(columnName)));
	}

	/**
	 * Gets the value of the field corresponding to (row, col) as a java.sql.Time
	 * @param row int
	 * @param col int
	 * @return Time value of field
	 */
	public Time getTime(int row, int col)
	{
		if (isNull(row, col))
			return(null);

		Object o = getObject(row, col);
		Time t = (Time)o;
		return(t);
	}

	/**
	 * Gets the value of the field corresponding to row, columnName as a java.sql.Time
	 * @param row int
	 * @param columnName String
	 * @return Time value of field
	 */
	public Time getTime(int row, String columnName)
	{
		return(getTime(row, getColumnIndex(columnName)));
	}

	/**
	 * Gets the value of the field corresponding to (row, col) as a java.sql.Timestamp
	 * @param row int
	 * @param col int
	 * @return Timestamp value of field
	 */
	public Timestamp getTimestamp(int row, int col)
	{
		if (isNull(row, col))
			return(null);

		Object o = getObject(row, col);
		if (o instanceof Date)
		{
			Date d = (Date)o;
			Timestamp t = new Timestamp(d.getTime());
			return(t);
		}
		else
		{
			Timestamp t = (Timestamp)o;
			return(t);
		}
	}

	/**
	 * Gets the value of the field corresponding to row, columnName as a java.sql.Timestamp
	 * @param row int
	 * @param columnName String
	 * @return Timestamp value of field
	 */
	public Timestamp getTimestamp(int row, String columnName)
	{
		return(getTimestamp(row, getColumnIndex(columnName)));
	}

	/**
	 * Gets the value of the field corresponding to (row, col) as a double
	 * @param row int
	 * @param col int
	 * @return double value of field
	 */
	public double getDouble(int row, int col)
	{
		if (isNull(row, col))
			return(0);

		Object o = getObject(row, col);
		if (o instanceof BigDecimal)
		{
			BigDecimal b = (BigDecimal)o;
			return(b.doubleValue());
		}
		else
		{
			Double d = (Double)o;
			return(d.doubleValue());
		}
	}

	/**
	 * Gets the value of the field corresponding to row, columnName as a double
	 * @param row int
	 * @param columnName String
	 * @return double value of field
	 */
	public double getDouble(int row, String columnName)
	{
		return(getDouble(row, getColumnIndex(columnName)));
	}

	/**
	 * Gets the value of the field corresponding to (row, col) as a float
	 * @param row int
	 * @param col int
	 * @return float value of field
	 */
	public float getFloat(int row, int col)
	{
		if (isNull(row, col))
			return(0);

		Object o = getObject(row, col);
		if (o instanceof BigDecimal)
		{
			BigDecimal b = (BigDecimal)o;
			return(b.floatValue());
		}
		else
		{
			Float f = (Float)o;
			return(f.floatValue());
		}
	}

	/**
	 * Gets the value of the field corresponding to row, columnName as a float
	 * @param row int
	 * @param columnName String
	 * @return float value of field
	 */
	public float getFloat(int row, String columnName)
	{
		return(getFloat(row, getColumnIndex(columnName)));
	}

	/**
	 * Returns the contents of SQLResults in a text table. This method is useful for debugging SQL
	 * statements.
	 * @return String
	 */
	public String toString()
	{
		if (columnCount < 1)
			return(null);

		StringBuffer out = new StringBuffer("");
		for (int col=0; col < columnCount; col++)
		{
			String formattedColName = formatWithSpaces((String)columnNames.get(col));
			out.append(formattedColName);
		}

		out.deleteCharAt(out.length()-2);
		int len = out.length();
		out.append("\n");
		for (int i=0; i < len-1; i++)
			out.append("-");
		out.append("\n");

		for (int row=0; row < getRowCount(); row++)
		{
			for (int col=0; col < columnCount; col++)
			{
				String formattedColName = null;
				if (isNull(row, col))
					formattedColName = formatWithSpaces("NULL");
				else
					formattedColName = formatWithSpaces(getString(row, col));
				out.append(formattedColName);
			}
			out.deleteCharAt(out.length()-2);
			out.append("\n");
		}

		return(out.toString());
	}

	/**
	 * Formats a string by adding spaces on the end if the string is shorter than
	 * toStringFormatWidth, or truncates string if it is too long.
	 * @param s String
	 * @return String
	 */
	private String formatWithSpaces(String s)
	{
		StringBuffer sb = new StringBuffer(s);
		if (s.length() < toStringFormatWidth)
		{
			for (int i=0; i < toStringFormatWidth-s.length(); i++)
				sb.append(" ");
			return(sb.toString());
		}
		else
		{
			return(sb.substring(0, toStringFormatWidth));
		}
	}

	/**
	 * Determines if field given by (row,col) is null
	 * @param row
	 * @param col
	 * @return true if null
	 */
	public boolean isNull(int row, int col)
	{
		Object o = getObject(row, col);
		return(o == null);
	}

	/**
	 * Determines if field given by (row, columnName) is null
	 * @param row
	 * @param col
	 * @return true if null
	 */
	public boolean isNull(int row, String columnName)
	{
		return(isNull(row, getColumnIndex(columnName)));
	}
}
