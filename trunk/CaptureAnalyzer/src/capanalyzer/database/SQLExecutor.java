package capanalyzer.database;

import java.sql.*;
import java.util.*;

/**
 * SQLExecutor class is an easy to use wrapper for executing JDBC queries, updates, and
 * stored procedure calls for Oracle and mySQL (other databases can be supported by
 * extending the DatabaseException class for a new database).<br><br>
 *
 * The framework doesn't throw any checked exceptions, so you aren't forced
 * to write try-catch blocks around all your code. Instead it throws custom DatabaseExceptions
 * (which extend RuntimeException), and DatabaseException has handy methods for determining the
 * cause of the database failure WITHOUT having to resort to looking at SQL error codes.<br><br>
 *
 * The constructor takes a single parameter, the ConnectionPool object.
 * You can create a ConnectionPool object by passing in the number of pooled connections (set
 * this value to one if you don't want connection pooling), the driver name, connection URL,
 * and username and password.<br><br>
 *
 * Or like so:<br>
 *   ConnectionPool conPool = new ConnectionPool(Connection existingConnection);<br><br>
 *
 * When you do a query that returns results, the results are passed back in the form of a
 * SQLResults object.<br><br>
 *
 * You can create parameterized queries by using the ? character (just like standard JDBC)
 * and successive calls to runQuery() will not re-prepare the sql statement if it hasn't
 * changed since the last call to runQuery(). You add the parameters to the query by calling
 * addParam() before you make the call to runQuery().<br><br>
 *
 * Here is a complete code example that does a simple select using an Oracle thin driver,
 * closes the connection, and outputs the results in the form of a text table:<br><br>
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
 *   SQLExecutor sqlExec = new SQLExecutor(conPool);
 *   sqlExec.addParam(3);   //add a parameter to this query (substitute '3' for the '?')
 *   SQLResults res = sqlExec.runQueryCloseCon("SELECT * FROM INV WHERE ID = ?");
 *   System.out.println(res.toString());  //output as a text table
 *
 * You can iterate through the SQLResults in a simple for loop:
 *   SQLResults res = sqlExec.runQueryCloseCon("SELECT * FROM INV");
 *   String out = "";
 *   for (int row=0; row < res.getRowCount(); row++)
 *      out += res.getLong(row, "TEST_ID") + " " + res.getString(row, "NOTES") + " " +
 *             res.getDate(row, "TEST_DT") + " " + res.getDouble(row, "AMOUNT") + " " +
 *             res.getString(row, "CODE") + "\n";
 *   System.out.println(out);
 *
 * You can do updates, inserts and deletes with SQLExecutor and determine how many rows
 * were updated like so:
 *   SQLExecutor sqlExec = new SQLExecutor(conPool);
 *   sqlExec.addParam(3);   //add a parameter to this query (substitute '3' for the '?')
 *   sqlExec.runQueryCloseCon("UPDATE INV SET NOM = ? WHERE ID < 10");
 *   System.out.println("You updated " + sqlExec.getNumRecordsUpdated() + " records");
 *
 * You can also use SQLExecutor to run stored procedures. Here is an example:
 *   SQLExecutor sqlExec = new SQLExecutor(conPool);
 *   sqlExec.addParam("Jeff");
 *   sqlExec.addParam("Smith");
 *   sqlExec.runStoredProc("JeffStoredProcTest_insertRec");
 *
 * You can also call stored procedures that return a value in an OUT param. The following
 * code example passes in two IN parameters (first name and last name) and retrieves an OUT
 * param (the full name) from the stored procedure:
 *   SQLExecutor sqlExec = new SQLExecutor(conPool);
 *   sqlExec.addParam("Jeff");
 *   sqlExec.addParam("Smith");
 *   sqlExec.addStoredProcOutParam("fullname");
 *   SQLResults res = sqlExec.runStoredProcCloseCon("jdbc_proc_test2");
 *   System.out.println("Your full name is " + res.getString(0, 2));
 *
 * SQLExecutor also supports transaction management (commit and rollback). Here is a code
 * example that does multiple updates and rolls back the transaction if a DatabaseException
 * is thrown:
 *
 *   SQLExecutor sqlExec = new SQLExecutor(conPool);
 *   try
 *   {
 *      sqlExec.setAutoCommit(false);
 *      sqlExec.addParam(new Integer(7));
 *      sqlExec.runQuery("UPDATE JDBC_TEST SET CODE = 'Z' WHERE TEST_ID = ?");
 *
 *      sqlExec.addParam(new Integer(6));
 *         //integrity constraint violation
 *      sqlExec.runQuery("UPDATE JDBC_TEST SET TEST_ID = NULL WHERE TEST_ID = ?");
 *
 *      sqlExec.commitTrans();
 *      System.out.println("transaction committed");
 *   }
 *   catch (DatabaseException e)
 *   {
 *      System.out.println("Error code=" + e.getSQLErrorCode() + ",  SQLState=" + e.getSQLState());
 *      if (e.isDataIntegrityViolation())
 *        System.out.println("data integrity violation");
 *      else if (e.isBadSQLGrammar())
 *        System.out.println("bad SQL grammar");
 *      else if (e.isNonExistentTableOrViewOrCol())
 *        System.out.println("Non existent table or view");
 *      System.out.println(e.getMessage());
 *      sqlExec.rollbackTrans();
 *      System.out.println("transaction rolled back");
 *   }
 *   finally
 *   {
 *      sqlExec.closeConnection();
 *   }
 *
 * Notice a benefit of the exception handling in this code. There is no code that is specific
 * to a database (like Oracle). Your code is portable!
 *
 * If you need to select a very large result set that you don't want to store in RAM (i.e. a
 * SQLResults object), you can use the runQueryStreamResults() method like so:
 *
 *   String sql = "select CARRIER_ID, NAME from carrier";
 *   SQLExecutor sqlExec = new SQLExecutor(getConnectionPool());
 *   ResultSet rs = sqlExec.runQueryStreamResults(sql);
 *
 *   System.out.println("SQL RESULTS FROM ResultSet:");
 *   try
 *   {
 *     while (rs.next()) //still have records...
 *     System.out.println(rs.getString("CARRIER_ID") + " " + rs.getString("NAME"));
 *   }
 *   catch (SQLException sqle) { }
 *   sqlExec.closeConnection();
 *
 * </pre>
 * @author Jeff S Smith
 */
public class SQLExecutor
{
	/** database connection */
	private Connection con = null;

	/** connection pool */
	private ConnectionPool conPool = null;

	/** maximum rows to return in a SELECT statement */
	private int maxRows = 1000;

	/** ArrayList which stores the SQL parameters (?) */
	private ArrayList<Object> params = null;

	/** ArrayList which stores whether SQL parameters are stored proc Out parameters */
	private ArrayList<Boolean> isStoredProcOutParam = null;

	/** Database type (for example, DatabaseType.ORACLE) */
	private int dbType;

	/** timeout interval for long running queries */
	private int timeoutInSec = 0;  //0 = no timeout

	/** prepared statement used in calls to runQuery() and runQueryKeepConnOpen() */
	private PreparedStatement prepStatement = null;

	/** remember the last SQL statement (so we don't re-prepare a statement if SQL doesn't change */
	private String lastSQL = null;

	/** number of rows updated in last runQuery() */
	private int numRecordsUpdated = 0;

	/**
	 * Constructor uses provided connection pool
	 * @param conPool
	 */
	public SQLExecutor(ConnectionPool conPool)
	{
		params = new ArrayList<Object>();
		isStoredProcOutParam = new ArrayList<Boolean>();
		this.conPool = conPool;
		con = conPool.getConnection();
		dbType = DatabaseType.getDbType(con);
	}

	/** Getter for connection
	 * @return Connection
	 */
	public Connection getConnection()
	{
		return(con);
	}

	/**
	 * Getter for numRecordsUpdated. Call this method to find out how many rows were updated in
	 * the last runQuery() call that did an INSERT, UPDATE, or DELETE.
	 * @return int
	 */
	public int getNumRecordsUpdated()
	{
		return(numRecordsUpdated);
	}

	/** Getter for timeoutInSec
	 * @return int query timeout in seconds
	 */
	public int getTimeoutInSec()
	{
		return(timeoutInSec);
	}

	/** Setter for timeoutInSec
	 * @param timeoutInSec int sets the query timeout in seconds
	 */
	public void setTimeoutInSec(int timeoutInSec)
	{
		this.timeoutInSec = timeoutInSec;
	}

	/**
	 * Cancels the currently running query if both the database and driver support aborting an SQL
	 * statement. This method can be used by one thread to cancel a statement that is being executed
	 * by another thread.
	 */
	public void cancelQuery()
	{
		try
		{
			if (prepStatement != null)
				prepStatement.cancel();
		}
		catch (SQLException e)
		{
			throw ExceptionFactory.getException(dbType, e.getMessage());
		}
	}


	/**
	 * Closes the SQLExecutor's statement object (releasing database and JDBC resources immediately instead of
	 * waiting for this to happen when the SQLExecutor object is garbage collected).
	 */
	public void closeQuery()
	{
		try
		{
			if (prepStatement != null)
			{
				prepStatement.close();
				prepStatement = null;
			}
		}
		catch (SQLException e)
		{
			throw ExceptionFactory.getException(dbType, e.getMessage());
		}
	}

	/**
	 * Clears out the query parameter list
	 */
	public void clearParams()
	{
		params.clear();
		isStoredProcOutParam.clear();
	}


	/**
	 * Adds a null query parameter to the list
	 */
	public void addNullParam(int sqlType)
	{
		NullParameterObject npo = new NullParameterObject(sqlType);
		params.add(npo);
	}

	/** Adds a query parameter to the list
	 * @param param parameter to add to list
	 */
	public void addParam(Object param)
	{
		params.add(param);
		isStoredProcOutParam.add(Boolean.FALSE);
	}

	/** Adds an int query parameter to the list
	 * @param param parameter to add to list
	 */
	public void addParam(int param)
	{
		addParam(new Integer(param));
	}

	/** Adds a long query parameter to the list
	 * @param param parameter to add to list
	 */
	public void addParam(long param)
	{
		addParam(new Long(param));
	}

	/** Adds a double query parameter to the list
	 * @param param parameter to add to list
	 */
	public void addParam(double param)
	{
		addParam(new Double(param));
	}

	/** Adds a boolean query parameter to the list
	 * @param param parameter to add to list
	 */
	public void addParam(boolean param)
	{
		addParam(new Boolean(param));
	}

	/** Adds a float query parameter to the list
	 * @param param parameter to add to list
	 */
	public void addParam(float param)
	{
		addParam(new Float(param));
	}

	/** Adds a short query parameter to the list
	 * @param param parameter to add to list
	 */
	public void addParam(short param)
	{
		addParam(new Short(param));
	}

	public void addStoredProcOutParam(Object param)
	{
		params.add(param);
		isStoredProcOutParam.add(Boolean.TRUE);
	}

	/** Get the of query parameters in the list
	 * @return int
	 */
	public int getCountParams()
	{
		return(params.size());
	}

	public SQLResults runStoredProc(String spName)
	{
		SQLResults results = new SQLResults(dbType, params.size());

		StringBuffer sql = new StringBuffer("{call " + spName + "(");

		for (int i=0; i < params.size(); i++)
		{
			sql.append("?");
			if (i < (params.size()-1))
				sql.append(",");
		}
		sql.append(")}");

		try
		{
			CallableStatement cs = con.prepareCall(sql.toString());

			for (int i=0; i < params.size(); i++)
			{
				Object param = params.get(i);
				boolean isOutParam = ((Boolean)isStoredProcOutParam.get(i)).booleanValue();

				if (param instanceof Integer)
				{
					int value = ((Integer)param).intValue();
					if (isOutParam)
						cs.registerOutParameter(i+1, java.sql.Types.INTEGER);
					else
						cs.setInt(i+1, value);
				}
				else if (param instanceof Short)
				{
					short sh = ((Short)param).shortValue();
					if (isOutParam)
						cs.registerOutParameter(i+1, java.sql.Types.SMALLINT);
					else
						cs.setShort(i+1, sh);
				}
				else if (param instanceof String)
				{
					String s = (String)param;
					if (isOutParam)
						cs.registerOutParameter(i+1, java.sql.Types.VARCHAR);
					else
						cs.setString(i+1, s);
				}
				else if (param instanceof Double)
				{
					double d = ((Double)param).doubleValue();
					if (isOutParam)
						cs.registerOutParameter(i+1, java.sql.Types.DOUBLE);
					else
						cs.setDouble(i+1, d);
				}
				else if (param instanceof Float)
				{
					float f = ((Float)param).floatValue();

					if (isOutParam)
						cs.registerOutParameter(i+1, java.sql.Types.FLOAT);
					else
						cs.setFloat(i+1, f);
				}
				else if (param instanceof Long)
				{
					long l = ((Long)param).longValue();

					if (isOutParam)
						cs.registerOutParameter(i+1, java.sql.Types.BIGINT);
					else
						cs.setLong(i+1, l);
				}
				else if (param instanceof Boolean)
				{
					boolean b = ((Boolean)param).booleanValue();

					if (isOutParam)
						cs.registerOutParameter(i+1, java.sql.Types.BIT);
					else
						cs.setBoolean(i+1, b);
				}
				else if (param instanceof java.sql.Date)
				{
					//java.sql.Date d = (java.sql.Date)param;
					if (isOutParam)
						cs.registerOutParameter(i+1, java.sql.Types.DATE);
					else
						cs.setDate(i+1, (java.sql.Date)param);
				}
				else
					throw ExceptionFactory.getException(dbType, "Unknown parameter type: param " + i);
			}

			cs.executeUpdate();  //run the stored procedure

			for (int i=0; i < params.size(); i++)
			{
				//add column names to ResultSet object
				String colName = "" + (i+1);
				results.addColumnName(colName);

				boolean isOutParam = ((Boolean)isStoredProcOutParam.get(i)).booleanValue();
				if (isOutParam)
					results.add(cs.getObject(i+1));  //add output parameter value to ResultSet object
					else
						results.add("null");
			}

			cs.close();
		}
		catch (SQLException e)
		{
			throw ExceptionFactory.getException(dbType, e.getMessage() + "Store proc call failed: " + sql, e);
		}
		finally
		{
			clearParams();
		}

		return(results);
	}

	/**
	 * Calls the stored proc and automatically closes the connection when done
	 * @param String spName stored procedure name
	 * @return SQLResults
	 */
	public SQLResults runStoredProcCloseCon(String spName)
	{
		try
		{
			return(runStoredProc(spName));
		}
		finally
		{
			conPool.closeConnection(con, dbType);
		}
	}

	private void setPrepStatementParameters() throws SQLException
	{
		for (int i=0; i < params.size(); i++)
		{
			Object param = params.get(i);

			if (param instanceof NullParameterObject)
			{
				int sqlType = ((NullParameterObject)param).sqlType;
				prepStatement.setNull(i+1, sqlType);
			}
			else if (param instanceof Integer)
			{
				int value = ((Integer)param).intValue();
				prepStatement.setInt(i+1, value);
			}
			else if (param instanceof Short)
			{
				short sh = ((Short)param).shortValue();
				prepStatement.setShort(i+1, sh);
			}
			else if (param instanceof String)
			{
				String s = (String)param;
				prepStatement.setString(i+1, s);
			}
			else if (param instanceof Double)
			{
				double d = ((Double)param).doubleValue();
				prepStatement.setDouble(i+1, d);
			}
			else if (param instanceof Float)
			{
				float f = ((Float)param).floatValue();
				prepStatement.setFloat(i+1, f);
			}
			else if (param instanceof Long)
			{
				long l = ((Long)param).longValue();
				prepStatement.setLong(i+1, l);
			}
			else if (param instanceof Boolean)
			{
				boolean b = ((Boolean)param).booleanValue();
				prepStatement.setBoolean(i+1, b);
			}
			else if (param instanceof java.sql.Date)
			{
				//java.sql.Date d = (java.sql.Date)param;
				//System.out.println("Date=" + d.toString());
				prepStatement.setDate(i+1, (java.sql.Date)param);
			}
			else if (param instanceof java.sql.Time)
			{
				//java.sql.Time d = (java.sql.Time)param;
				prepStatement.setTime(i+1, (java.sql.Time)param);
			}
			else if (param instanceof java.sql.Timestamp)
			{
				//java.sql.Timestamp d = (java.sql.Timestamp)param;
				prepStatement.setTimestamp(i+1, (java.sql.Timestamp)param);
			}
			else
			{
				clearParams();
				throw ExceptionFactory.getException(dbType, "Unknown parameter type: param " + i);
			}
		}
	}


	
	/**
	 * Runs the sql and does NOT close the connection
	 * @param sql sql command to run
	 * @return SQLResults if it's a query, null otherwise
	 */
	public SQLResults runQuery(String sql)
	{
		SQLResults results = null;
		ResultSet rs = null;
		numRecordsUpdated = 0;

		try
		{
			if ((sql.equalsIgnoreCase(lastSQL) == false) || (prepStatement == null))  //if sql has changed, then prepare stmt
				prepStatement = con.prepareStatement(sql);
			lastSQL = sql;

			setPrepStatementParameters();

			boolean isSelectStatement = isSelectStatement(sql);
			if ((dbType != DatabaseType.ORACLE) && (isSelectStatement == false))
			{
				prepStatement.setMaxRows(maxRows);
				numRecordsUpdated = prepStatement.executeUpdate();
			}
			else
			{
				if (timeoutInSec > 0)
					prepStatement.setQueryTimeout(timeoutInSec);

				if (isSelectStatement(sql))
				{
					prepStatement.setMaxRows(maxRows);
					rs = prepStatement.executeQuery();
				}
				else
				{
					numRecordsUpdated = prepStatement.executeUpdate();
				}
			}

			if (isSelectStatement)
			{
				//SELECT statement, so get results
				ResultSetMetaData rsmd = rs.getMetaData();
				int columnCount = rsmd.getColumnCount();
				results = new SQLResults(dbType, columnCount);

				//add field values to ResultSet object
				while (rs.next())
				{
					for (int i = 0; i < columnCount; i++)
						results.add(rs.getObject(i+1));
				}
				//add column names to ResultSet object
				for (int i = 0; i < columnCount; i++)
					results.addColumnName(rsmd.getColumnName(i+1));
			}
		}
		catch (SQLException e)
		{
			throw ExceptionFactory.getException(dbType, e.getMessage() + "\nSQL Failed: " + sql, e);
		}
		finally
		{
			clearParams();
		}

		return(results);
	}

	/**
	 * Runs the sql and does NOT close the connection. Returns a standard JDBC ResultSet object
	 * which can be scrolled through using rs.next(). This method is preferable to runQuery()
	 * when your ResultSet is too large to fit into memory (a SQLResults object).
	 * 
	 * @param sql sql command to run
	 * @param doScrollTypeInsensitive if true, it creates statement with TYPE_SCROLL_INSENSITIVE and CONCUR_UPDATABLE
	 * @return ResultSet if it's a query, null otherwise
	 */
	public ResultSet runQueryStreamResults(String sql, boolean doScrollTypeInsensitive)
	{
		ResultSet rs = null;
		numRecordsUpdated = 0;

		try
		{
			if ((sql.equalsIgnoreCase(lastSQL) == false) || (prepStatement == null))  //if sql has changed, then prepare stmt
			{
				if (!doScrollTypeInsensitive)
					prepStatement = con.prepareStatement(sql);
				else
					prepStatement = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			}
			lastSQL = sql;

			setPrepStatementParameters();

			boolean isSelectStatement = isSelectStatement(sql);
			if ((dbType != DatabaseType.ORACLE) && (isSelectStatement == false))
			{
				prepStatement.setMaxRows(maxRows);
				numRecordsUpdated = prepStatement.executeUpdate();
			}
			else
			{
				if (timeoutInSec > 0)
					prepStatement.setQueryTimeout(timeoutInSec);

				if (isSelectStatement(sql))
				{
					prepStatement.setMaxRows(maxRows);
					rs = prepStatement.executeQuery();
				}
				else
				{
					numRecordsUpdated = prepStatement.executeUpdate();
				}
			}
		}
		catch (SQLException e)
		{
			throw ExceptionFactory.getException(dbType, e.getMessage() + "\nSQL Failed: " + sql, e);
		}
		finally
		{
			clearParams();
		}

		return(rs);
	}
	
	/**
	 * Runs the sql batch and does NOT close the connection
	 * should be used when having many inserts or updates
	 * @param sql sql command to run
	 * @return SQLResults if it's a query, null otherwise
	 */
	public int[] runBatchPreparedStatement(String sql)
	{
		int[] numRecordsUpdatedArray = null;;
		numRecordsUpdated = 0;

		try
		{
			if ((sql.equalsIgnoreCase(lastSQL) == false) || (prepStatement == null))  //if sql has changed, then prepare stmt
				prepStatement = con.prepareStatement(sql);
			lastSQL = sql;

			setPrepStatementParameters();

			if (dbType != DatabaseType.ORACLE)
			{
				prepStatement.setMaxRows(maxRows);
				numRecordsUpdatedArray = prepStatement.executeBatch();
			}
			else
			{
				if (timeoutInSec > 0)
					prepStatement.setQueryTimeout(timeoutInSec);
				
				numRecordsUpdatedArray = prepStatement.executeBatch();
			}
			
			for (int i = 0; i < numRecordsUpdatedArray.length; i++)
			{
				numRecordsUpdated+=numRecordsUpdatedArray[i];
			}
		}
		catch (SQLException e)
		{
			throw ExceptionFactory.getException(dbType, e.getMessage() + "\nSQL Failed: " + sql, e);
		}
		finally
		{
			clearParams();
		}

		return(numRecordsUpdatedArray);
	}
	
	/**
	 * Runs the sql and does NOT close the connection. Returns a standard JDBC ResultSet object
	 * which can be scrolled through using rs.next(). This method is preferable to runQuery()
	 * when your ResultSet is too large to fit into memory (a SQLResults object).
	 *
	 * @param sql sql command to run
	 * @return ResultSet if it's a query, null otherwise
	 */
	public ResultSet runQueryStreamResults(String sql)
	{
		return(runQueryStreamResults(sql, false));
	}

	/**
	 * Runs the sql and automatically closes the connection when done
	 * @param sql sql command to execute
	 * @return SQLResults if it's a query, null otherwise
	 */
	public SQLResults runQueryCloseCon(String sql)
	{
		try
		{
			return(runQuery(sql));
		}
		finally
		{
			conPool.closeConnection(con, dbType);
		}
	}

	/**
	 * Is this SQL statement a select statement (returns rows?)
	 * @param sql String
	 * @return boolean
	 */
	private boolean isSelectStatement(String sql)
	{
		StringBuffer sb = new StringBuffer(sql.trim());
		String s = (sb.substring(0, 6));
		return(s.equalsIgnoreCase("SELECT"));
	}

	/**
	 * Gets the auto-commit status
	 * @return true if in auto-commit mode, false otherwise
	 */
	public boolean getAutoCommit()
	{
		try
		{
			return(con.getAutoCommit());
		}
		catch (SQLException e)
		{
			throw ExceptionFactory.getException(dbType, "Unable to get the auto commit status", e);
		}
	}

	/**
	 * Sets the auto-commit status
	 * @param autoCommit boolean
	 */
	public void setAutoCommit(boolean autoCommit)
	{
		try
		{
			con.setAutoCommit(autoCommit);
		}
		catch (SQLException e)
		{
			throw ExceptionFactory.getException(dbType, "Unable to set the auto commit status = " + autoCommit, e);
		}
	}

	/**
	 * Sets the transaction isolation level
	 * @param level int
	 */
	public void setTransactionIsolation(int level)
	{
		try
		{
			con.setTransactionIsolation(level);
		}
		catch (SQLException e)
		{
			throw ExceptionFactory.getException(dbType, "Attempted to set an invalid transaction isolation level", e);
		}
	}

	/**
	 * Commits the current transaction
	 */
	public void commitTrans()
	{
		try
		{
			con.commit();
		}
		catch (SQLException e)
		{
			throw ExceptionFactory.getException(dbType, "Failure during transaction commit", e);
		}
	}

	/**
	 * Rolls back the current transaction
	 */
	public void rollbackTrans()
	{
		try
		{
			con.rollback();
		}
		catch (SQLException e)
		{
			throw ExceptionFactory.getException(dbType, "Failure during transaction rollback", e);
		}
	}

	/**
	 * Sets the current connection readOnly status
	 * @param readOnly boolean
	 */
	public void setReadOnly(boolean readOnly)
	{
		try
		{
			con.setReadOnly(readOnly);
		}
		catch (SQLException e)
		{
			throw ExceptionFactory.getException(dbType, "Exception during setReadOnly", e);
		}
	}

	/**
	 * Is the current connection read only?
	 * @return boolean
	 */
	public boolean isReadOnly()
	{
		try
		{
			return(con.isReadOnly());
		}
		catch (SQLException e)
		{
			throw ExceptionFactory.getException(dbType, "Failure while accessing readOnly status of connection", e);
		}
	}

	/**
	 * Setter for the maximum number of rows that a query can return
	 * @param maxRow int
	 */
	public void setMaxRows(int maxRows)
	{
		this.maxRows = maxRows;
	}

	/**
	 * Getter for the maximum number of rows that a query can return
	 * @return int
	 */
	public int getMaxRows()
	{
		return(maxRows);
	}

	/**
	 * Closes this connection.
	 */
	public void closeConnection()
	{
		conPool.closeConnection(con, dbType);
	}

	/**
	 * Releases this connection (sets its available status = true). It does not close the
	 * connection. It merely makes the connection available for re-use.
	 * @param conPool ConnectionPool
	 */
	public void releaseConnection()
	{
		conPool.releaseConnection(con, dbType);
	}
}

/** NullParameterObject inner class -- defines a null parameter */
class NullParameterObject extends Object
{
	int sqlType;

	NullParameterObject(int sqlType)
	{
		this.sqlType = sqlType;
	}
}
