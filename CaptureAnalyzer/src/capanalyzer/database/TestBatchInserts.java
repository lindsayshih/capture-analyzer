package capanalyzer.database;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import com.mysql.jdbc.NonRegisteringDriver;

public class TestBatchInserts
{

	public static void main(String[] args) throws Exception
	{

		Connection c = new NonRegisteringDriver().connect("jdbc:mysql:///test?user=root&rewriteBatchedStatements=true", null);
		Map<Integer, List<Long>> results = new TreeMap<Integer, List<Long>>();

		for (int repeat = 0; repeat < 34; repeat++)
		{
			for (int numberOfRows = 1; numberOfRows < 10000; numberOfRows *= 2)
			{
				List<Long> forThisRowCount = results.get(new Integer(numberOfRows));

				if (forThisRowCount == null)
				{
					forThisRowCount = new LinkedList<Long>();
					results.put(new Integer(numberOfRows), forThisRowCount);
				}
				c.createStatement().execute("DROP TABLE IF EXISTS testBug41532");

				c.createStatement()
						.execute(
								"CREATE TABLE testBug41532(ID"
										+ " INTEGER, S1 VARCHAR(100), S2 VARCHAR(100), S3 VARCHAR(100), D1 DATETIME, D2 DATETIME, D3 DATETIME, N1 DECIMAL(28,6), N2 DECIMAL(28,6), N3 DECIMAL(28,6), UNIQUE KEY"
										+ " UNIQUE_KEY_TEST_DUPLICATE (ID) ) ENGINE=MYISAM");
				PreparedStatement pstmt = c.prepareStatement("INSERT INTO testBug41532(ID, S1, S2, S3, D1," + "D2, D3, N1, N2, N3) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
				try
				{
					c.setAutoCommit(false);
					c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
					Date d1 = new Date(System.currentTimeMillis());
					Date d2 = new Date(System.currentTimeMillis() + 1000000);
					Date d3 = new Date(System.currentTimeMillis() + 1250000);

					for (int i = 0; i < numberOfRows; i++)
					{
						pstmt.setObject(1, new Integer(i), Types.INTEGER);
						pstmt.setObject(2, String.valueOf(i), Types.VARCHAR);
						pstmt.setObject(3, String.valueOf(i * 0.1), Types.VARCHAR);
						pstmt.setObject(4, String.valueOf(i / 3), Types.VARCHAR);
						pstmt.setObject(5, new Timestamp(d1.getTime()), Types.TIMESTAMP);
						pstmt.setObject(6, new Timestamp(d2.getTime()), Types.TIMESTAMP);
						pstmt.setObject(7, new Timestamp(d3.getTime()), Types.TIMESTAMP);
						pstmt.setObject(8, new BigDecimal(i + 0.1), Types.DECIMAL);
						pstmt.setObject(9, new BigDecimal(i * 0.1), Types.DECIMAL);
						pstmt.setObject(10, new BigDecimal(i / 3), Types.DECIMAL);
						pstmt.addBatch();
					}
					long startTime = System.currentTimeMillis();
					pstmt.executeBatch();
					c.commit();
					long stopTime = System.currentTimeMillis();

					long elapsedTime = stopTime - startTime;

					forThisRowCount.add(new Long(elapsedTime));

					System.out.println(numberOfRows + ": elapsedTime: " + elapsedTime + " rows/permilli: " + (double) numberOfRows / (double) elapsedTime);

					ResultSet rs = c.createStatement().executeQuery("SELECT COUNT(*) FROM testBug41532");
					rs.next();
					if (rs.getInt(1) != numberOfRows)
					{
						System.out.println("Failed!");
					}
				} finally
				{

				}

			}
		}

		Iterator<Entry<Integer, List<Long>>> iter = results.entrySet().iterator();

		System.out.println("size\t\t\tmin\t\t\tmax\t\t\tmean\t\t\tstddev");

		while (iter.hasNext())
		{
			Entry<Integer, List<Long>> entry = (Entry<Integer, List<Long>>) iter.next();

			List<Long> forRowCount = entry.getValue();

			DescriptiveStatistics stats = new DescriptiveStatistics();
			double rowCount = (Integer) entry.getKey();
			for (Object val : forRowCount)
			{
				stats.addValue(rowCount / (double) ((Long) val).longValue());
			}

			double min = stats.getMin();
			double max = stats.getMax();
			double mean = stats.getMean();
			double std = stats.getStandardDeviation();

			System.out.println(rowCount + "\t\t\t" + min + "\t\t\t" + max + "\t\t\t" + mean + "\t\t\t" + std);

		}
	}

}