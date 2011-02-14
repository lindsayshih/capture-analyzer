package capanalyzer.database;

import java.sql.*;

/**
 * Implements the JDBC examples for the DB course
 * @author Rubi Boim
 *
 */
public class JDBCExample
{
	Connection			conn;			// DB connection
	
	
	
	
	/**
	 * Empty constructor
	 */
	public JDBCExample()
	{
		this.conn			=		null;
	}
	
	
	
	
	

	/**
	 * 
	 * @return the connection (null on error)
	 */
	public void openConnection()
	{
		
		// loading the driver
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
		}
		catch (ClassNotFoundException e)
		{
			System.out.println("Unable to load the MySQL JDBC driver..");
			java.lang.System.exit(0); 
		}
		System.out.println("Driver loaded successfully");
		
		
		
		// creating the connection
		System.out.print("Trying to connect.. ");
		try
		{
			conn = DriverManager.getConnection(
					"jdbc:mysql://localhost/capture_analyzer?", "root", "oklydokly");
		}
		catch (SQLException e)
		{
			System.out.println("Unable to connect - " + e.toString());
			java.lang.System.exit(0); 
		}
		System.out.println("Connected!");
		
	}
	
	
	/**
	 * close the connection
	 */
	public void closeConnection()
	{
		// closing the connection
		try
		{
			conn.close();
		}
		catch (SQLException e)
		{
			System.out.println("Unable to close the connection - " + e.toString());
			java.lang.System.exit(0); 
		}
		
	}
	
	
	
	/**
	 * Shows the executeQuery()
	 */
	public void demoExecuteQuery()
	{
		Statement		stmt;
		ResultSet		rs;

		
		try
		{
			stmt	=	conn.createStatement();
			rs		=	stmt.executeQuery("SELECT * FROM all_flows");

			while (rs.next() == true)
			{
				System.out.print(rs.getInt("time"));
				System.out.print(" ");
				System.out.print(rs.getString("tuple"));
			}
			
			// closing
			rs.close();
			stmt.close();

		}
		catch (SQLException e)
		{
			System.out.println("ERROR executeQuery - " + e.toString());
			java.lang.System.exit(0); 
		}
	}
	
	
	
	/**
	 * Shows the executeUpdate()
	 */
	public void demoExecuteUpdate()
	{
		Statement		stmt;
		int				result;

		
		try
		{
			stmt	=	conn.createStatement();
			result	=	stmt.executeUpdate("INSERT INTO all_flows(tuple, time) VALUES('a_b_c','123')");
		//	result	=	stmt.executeUpdate("INSERT INTO demo(fname, lname) VALUES('Rubi','Boim2')");
		//	result	=	stmt.executeUpdate("DELETE FROM demo");
			System.out.println("Success - executeUpdate, result = " + result);
			
			// closing
			stmt.close();
		}
		catch (SQLException e)
		{
			System.out.println("ERROR executeUpdate - " + e.toString());
			java.lang.System.exit(0); 
		}
	}
	
	
	
	
	/**
	 * Shows the transactions()
	 */
	public void demoTransactions()
	{
		Statement		stmt;
		int				result;

		
		try
		{
			conn.setAutoCommit(false);
			
			
			
			stmt	=	conn.createStatement();
			stmt.executeUpdate("INSERT INTO demo(fname, lname) VALUES('Transaction','Demo')");
			stmt.executeUpdate("INSERT INTO demo(fname, lname) VALUES('Transaction - should fail')");
			
			// committing
			System.out.println("Commiting transaction..");
			conn.commit();
			
			
			System.out.println("Error, transaction should failed and not reach this code");
			
			
			// closing
			stmt.close();
			
			conn.setAutoCommit(true);
		}
		catch (SQLException e)
		{
			System.out.println("We have an exception, transaction is not complete: Exception: " +  e.toString());
			try
			{
				conn.rollback();
				System.out.println("Rollback Successfully :)");
			}
			catch (SQLException e2)
			{
				System.out.println("ERROR demoTransactions (when rollbacking) - " + e.toString());
				java.lang.System.exit(0); 
			}
			
		}
	}
	
	

	
	/**
	 * Shows long insert
	 */
	public void demoWithoutPreparedStatement()
	{
		Statement	stmt;
		int			i;

		
		try
		{
			stmt	=	conn.createStatement();

			i	=	0;
			while (i < 20000)
			{
				stmt.executeUpdate("INSERT INTO demo(fname, lname) VALUES('Rubi-" + i + "','Boim-" + i + "')");
				
				if (i % 1000 == 0)
					System.out.println("PreparedStatement - current record: " + i);

				i++;
			}
			
			
			System.out.println("Success - demoWithoutPreparedStatement");
			
			// closing
			stmt.close();
		}
		catch (SQLException e)
		{
			System.out.println("ERROR demoWithoutPreparedStatement - " + e.toString());
			java.lang.System.exit(0); 
		}
	}
	
	
	
	/**
	 * Shows the PreparedStatement()
	 */
	public void demoWithPreparedStatement()
	{
		PreparedStatement	pstmt;
		int					i;

		
		try
		{
			pstmt	=	conn.prepareStatement("INSERT INTO demo(fname,lname) VALUES(?,?)");

			i	=	0;
			while (i < 50000)
			{
				pstmt.setString(1, "Rubi-" + i);
				pstmt.setString(2, "Boim-" + i);
				pstmt.executeUpdate();
				
				if (i % 1000 == 0)
					System.out.println("PreparedStatement - current record: " + i);
				
				i++;
			}
			
			
			System.out.println("Success - demoWithPreparedStatement");
			
			// closing
			pstmt.close();
		}
		catch (SQLException e)
		{
			System.out.println("ERROR demoWithPreparedStatement - " + e.toString());
			java.lang.System.exit(0); 
		}
	}
	

	
	
	

	/**
	 * Shows the Batch PreparedStatement()
	 */
	public void demoBatchPreparedStatement()
	{
		PreparedStatement	pstmt;
		int					i;

		
		try
		{
			pstmt	=	conn.prepareStatement("INSERT INTO demo(fname,lname) VALUES(?,?)");

			i	=	0;
			while (i < 50000)
			{
				pstmt.setString(1, "Rubi-" + i);
				pstmt.setString(2, "Boim-" + i);
				pstmt.addBatch();
				
				i++;
			}

			pstmt.executeBatch();
			
			
			System.out.println("Success - demoWithPreparedStatement");
			
			// closing
			pstmt.close();
		}
		catch (SQLException e)
		{
			System.out.println("ERROR demoWithPreparedStatement - " + e.toString());
			java.lang.System.exit(0); 
		}
	}
	
	
	
	
	
	/**
	 * Shows how to retrieve the generated ID (by "autonumber")
	 */
	public void demoGetGeneratedID()
	{
		Statement		stmt;
		ResultSet		rs;
		int				id;
		

		
		try
		{
			stmt	=	conn.createStatement();
			stmt.executeUpdate(	"INSERT INTO demo(fname, lname) VALUES('Ru''bi','Boim')",
								new String[]{"ID"});
			rs		=	stmt.getGeneratedKeys();
            rs.next();
            id		=	rs.getInt(1);
			
			
			
			System.out.println("Success - GetGeneratedID, the generated ID is: " + id);
			
			// closing
			stmt.close();
		}
		catch (SQLException e)
		{
			System.out.println("ERROR executeUpdate - " + e.toString());
			java.lang.System.exit(0); 
		}
	}
	
	
	/**
	 * Shows how to insert a date
	 */
	public void demoInsertDate()
	{
		Statement		stmt;
		

		
		try
		{
			stmt	=	conn.createStatement();
			stmt.executeUpdate(	"INSERT INTO demo(fname, lname, mydate) VALUES('Rubi','Boim',to_date('13/12/2008', 'dd/mm/yyyy'))");
			
			System.out.println("Success - demoInsertDate");
			
			// closing
			stmt.close();
		}
		catch (SQLException e)
		{
			System.out.println("ERROR executeUpdate - " + e.toString());
			java.lang.System.exit(0); 
		}
	}
	
	
	
}
