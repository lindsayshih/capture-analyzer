package capanalyzer.database;
import java.sql.*;
import java.util.*;

/**
 * Manages jdbc connections to the database.
 * @author Jeff S Smith
 */
public class ConnectionPool
{
	/** database driver name */
	private String driverName;

	/** database connection URL */
	private String conURL;

	/** database connection user name */
	private String username;

	/** database connection password */
	private String password;

	/** DatabaseType (e.g. DatabaseType.ORACLE) */
	private int dbType;

	/** Connection pool */
	ArrayList<PooledConnection> conPool;

	/**
	 * Pooled connection object
	 * @author Jeff S Smith
	 */
	private class PooledConnection
	{
		/** database connection */
		private Connection con;

		/** is this connection available */
		private boolean available;

		/**
		 * Constructor for PooledConnection object
		 * @param con Connection
		 * @param available boolean
		 */
		PooledConnection(Connection con, boolean available)
		{
			this.con = con;
			this.available = available;
		}

		/**
		 * Get the connection
		 * @return Connection
		 */
		Connection getConnection()
		{
			return(con);
		}

		/**
		 * Is this connection available
		 * @return boolean
		 */
		boolean isAvailable()
		{
			return(available);
		}

		/**
		 * Set this connection to available
		 * @param available boolean
		 */
		void setAvailable(boolean available)
		{
			this.available = available;
		}
	}

	/**
	 * Constructor creates a JDBC connection using given parameters
	 * @param databaseType
	 * @param driverName
	 * @param conURL
	 * @param username
	 * @param password
	 */
	public ConnectionPool(int numPooledCon,
			String driverName,
			String conURL,
			String username,
			String password)
	{
		this.dbType = DatabaseType.getDbType(driverName);

		this.driverName = driverName;
		this.conURL = conURL;
		this.username = username;
		this.password = password;
		conPool = new ArrayList<PooledConnection>();
		addConnectionsToPool(numPooledCon);
	}

	/**
	 * Constructor uses the given connection (con) as its connection. Use this constructor if you
	 * want to get your connections from some custom code or from a connection pool.
	 * @param con
	 * @param dbType
	 */
	public ConnectionPool(Connection con)
	{
		conPool = new ArrayList<PooledConnection>();
		PooledConnection pc = new PooledConnection(con, true);
		conPool.add(pc);
		this.dbType = DatabaseType.getDbType(con);
	}

	/**
	 * Creates database connection(s) and adds them to the pool.
	 * @param numPooledCon
	 * @param driverName
	 * @param conURL
	 * @param username
	 * @param password
	 */
	private void addConnectionsToPool(int numPooledCon)
	{
		try
		{
			Class.forName(driverName).newInstance();
			for (int i=0; i < numPooledCon; i++)
			{
				Connection con = DriverManager.getConnection(conURL, username, password);
				PooledConnection pc = new PooledConnection(con, true);
				conPool.add(pc);
			}
		}
		catch (SQLException sqle)
		{
			throw ExceptionFactory.getException(dbType, sqle.toString() +
					"\n" + "SQL State: " +
					sqle.getSQLState(), sqle);
		}
		catch (ClassNotFoundException cnfe)
		{
			throw ExceptionFactory.getException(dbType, cnfe.getMessage());
		}
		catch (InstantiationException ie)
		{
			throw ExceptionFactory.getException(dbType, ie.getMessage());
		}
		catch (Exception e)
		{
			throw ExceptionFactory.getException(dbType, e.getMessage());
		}
	}

	/**
	 * Gets the number of connections in the pool
	 * @return int
	 */
	public int getNumConInPool()
	{
		return(conPool.size());
	}

	private void removeAnyClosedConnections()
	{
		try
		{
			boolean done = false;
			while (done == false)
			{
				done = true;

				for (int i=0; i < conPool.size(); i++)
				{
					PooledConnection pc = (PooledConnection)conPool.get(i);
					if (pc.getConnection().isClosed())  //remove any closed connections
					{
						conPool.remove(i);
						done = false;
						break;
					}
				}
			}
		}
		catch (SQLException e)
		{
			throw ExceptionFactory.getException(dbType, e.getMessage());
		}
	}

	/**
	 * Gets available connection from the pool
	 * @return Connection
	 */
	public Connection getConnection()
	{
		//if any connections have been closed, remove them from the pool before we get the
		//next available connection
		removeAnyClosedConnections();

		for (int i=0; i < conPool.size(); i++)
		{
			PooledConnection pc = (PooledConnection)conPool.get(i);
			if (pc.isAvailable())
			{
				pc.setAvailable(false);
				return(pc.getConnection());
			}
		}

		//didn't find a connection, so add one to the pool
		addConnectionsToPool(1);
		PooledConnection pc = (PooledConnection)conPool.get(conPool.size()-1);
		pc.setAvailable(false);
		return(pc.getConnection());
	}

	/**
	 * Get the dbType
	 * @return int
	 */
	public int getDbType()
	{
		return(dbType);
	}

	/**
	 * Method closeConnection closes the jdbc database connection
	 */
	void releaseConnection(Connection con)
	{
		for (int i=0; i < conPool.size(); i++)
		{
			PooledConnection pc = (PooledConnection)conPool.get(i);
			if (pc.getConnection().equals(con))
				pc.setAvailable(true);
		}
	}

	/**
	 * Closes all connections in the connection pool.
	 */
	public void closeAllConnections()
	{
		for (int i=0; i < conPool.size(); i++)
		{
			PooledConnection pc = (PooledConnection)conPool.get(i);
			closeConnection(pc.getConnection(), dbType);
		}

		conPool.clear();  //remove all PooledConnections from list
	}


	/**
	 * Attempts to resize the connection pool to the new size. This method will not free any
	 * connections which are not available (in use)--so it may not resize the pool. It will always
	 * enlarge the connection pool if newSize > current size.
	 * @param newSize
	 * @return int new size of connection pool
	 */
	public int resizeConnectionPool(int newSize)
	{
		if ((newSize < 0) || (newSize > 999))
			throw ExceptionFactory.getException(dbType, "Connection pool size must be between 0 and 999");

		removeAnyClosedConnections();

		if (newSize > conPool.size())  //add new connections to pool
		{
			int conToAdd = (newSize - conPool.size());
			addConnectionsToPool(conToAdd);
		}
		else if (newSize < conPool.size()) //try to remove available connections
		{
			boolean done = false;
			while ((newSize < conPool.size()) && (done == false))
			{
				done = true;

				for (int i=0; i < conPool.size(); i++)
				{
					PooledConnection pc = (PooledConnection)conPool.get(i);
					if (pc.isAvailable())
					{
						//found an available connection, so close and remove it
						closeConnection(pc.getConnection(), dbType);
						conPool.remove(i);
						done = false;
						break;
					}
				}
			}

		}

		return(conPool.size());
	}

	/**
	 * Makes a connection available for reuse (in the connection pool)
	 * @param con Connection
	 * @param dbType int database type
	 */
	public void releaseConnection(Connection con, int dbType)
	{
		for (int i=0; i < conPool.size(); i++)
		{
			PooledConnection pc = (PooledConnection)conPool.get(i);
			if (pc.getConnection().equals(con))
				pc.setAvailable(true);
		}
	}

	/**
	 * method closes the given connection
	 * @param con connection to close
	 * @param dbType type of database (used to throw appropriate exception)
	 */
	void closeConnection(Connection con, int dbType)
	{
		try
		{
			if (con != null)
				con.close();
		}
		catch (SQLException e)
		{
			throw ExceptionFactory.getException(dbType, e.getMessage(), e);
		}
	}
}
