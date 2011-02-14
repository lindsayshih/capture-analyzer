package capanalyzer.database;

/**
 * JDBC run example..
 * @author Rubi Boim
 *
 */
public class RunMe
{

	public static void main(String[] args)
	{
		JDBCExample		jdbc_example;
		
		// creating the example object
		jdbc_example	=	new JDBCExample();
		
		
		// connecting
		jdbc_example.openConnection();
		
		
		// demo executeQuery
		//jdbc_example.demoExecuteQuery();
		
		
		// demo executeUpdate
		jdbc_example.demoExecuteUpdate();
		
		
		// demo transactions
		//jdbc_example.demoTransactions();
		
		
		// demo without prepared statement
		//jdbc_example.demoWithoutPreparedStatement();

		
		// demo with prepared statement
		//jdbc_example.demoWithPreparedStatement();

		
		// demo batch prepared statement
		//jdbc_example.demoBatchPreparedStatement();
		
		
		// demo get back the generated id
		//jdbc_example.demoGetGeneratedID();
		
		
		// demo insert date
		//jdbc_example.demoInsertDate();
		
		

		
		
		// close the connection
		jdbc_example.closeConnection();
		
		
		
		System.out.println("Done :)");
	}

	
	
}







