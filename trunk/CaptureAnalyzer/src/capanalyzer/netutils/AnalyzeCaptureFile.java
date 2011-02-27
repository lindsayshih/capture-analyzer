package capanalyzer.netutils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import capanalyzer.GlobalConfig;
import capanalyzer.database.ConnectionPool;
import capanalyzer.netutils.build.FiveTuple;
import capanalyzer.netutils.build.IPPacket;
import capanalyzer.netutils.files.CaptureFileBlock;
import capanalyzer.netutils.files.CaptureFileFactory;
import capanalyzer.netutils.files.CaptureFileReader;

public class AnalyzeCaptureFile
{
	static String erfFile = "e:\\capture_012_15_06_2009.erf";

	/** JDBC driver name */
	private static String driverName;

	/** JDBC connection URL */
	private static String connURL;

	/** JDBC connection username */
	private static String username;

	/** JDBC connection password */
	private static String password;

	public static void main(String[] args) throws IOException, NetUtilsException
	{
		final long agingTime = GlobalConfig.CaptureFileReadParams.getAgingTime() * 1000000;
		final int numOfIpPacketsBetweenChecks = 6000000;
		
		List<IPacketAnalyzer> packetAnalyzers = new ArrayList<IPacketAnalyzer>();
		packetAnalyzers.add(new BaseAnalyzer());

		CaptureFileReader frd = CaptureFileFactory.createCaptureFileReader(erfFile);
		CaptureFileBlock nextblock = null;
		long counter = 0;

		setMySQLConnectInfo();
		Connection con = getConnectionPool().getConnection();
		createDbTable(con, true);

		try
		{
			long startTime = System.currentTimeMillis();
			while ((nextblock = frd.readNextBlock()) != null)
			{
				if (IPPacket.statIsIpPacket(nextblock.getMyData()))
				{
					for (IPacketAnalyzer packetAnalyzer : packetAnalyzers)
					{
						packetAnalyzer.processPacket(nextblock, frd.getPrevBytesRead());
					}

					counter++;
					if (counter % numOfIpPacketsBetweenChecks == 0)
					{
						checkForAgedFlowsAndInsertToDb(agingTime, packetAnalyzers, nextblock, con);
						
						System.out.println("Percentage Done: " + frd.getBytesRead() / (float) frd.getCapFileSizeInBytes());
					}
				}
			}

			System.out.println("Total Time = " + (System.currentTimeMillis() - startTime) / 1000 + " Seconds");

		} catch (Exception e)
		{
			System.out.println("Exception Caught");
			e.printStackTrace();
		}
	}

	/**
	 * @param agingTime
	 * @param packetAnalyzers
	 * @param nextblock
	 * @param con
	 * @throws SQLException
	 */
	protected static void checkForAgedFlowsAndInsertToDb(final long agingTime, List<IPacketAnalyzer> packetAnalyzers, CaptureFileBlock nextblock, Connection con)
			throws SQLException
	{
		long currentPacketTime = nextblock.getMyPktHdr().getTime();
		FlowsDataStructure flowsDataStructure = FlowsDataStructure.getInstance();
		
		List<FiveTuple> listOfFlowsThatShouldAgeFiveTuples = flowsDataStructure.getAllFlowsThatShouldAgeFiveTuples(currentPacketTime, agingTime);

		for (FiveTuple fiveTuple : listOfFlowsThatShouldAgeFiveTuples)
		{
			for (IPacketAnalyzer packetAnalyzer : packetAnalyzers)
			{
				packetAnalyzer.finalizeFlow(fiveTuple);
			}
		}

		if (listOfFlowsThatShouldAgeFiveTuples.size() > 0)
		{
			insertAgedFlowsToDb(listOfFlowsThatShouldAgeFiveTuples, con);
		}
	}

	/**
	 * @param listOfFlowsThatShouldAgeFiveTuples
	 * @param con
	 * @param allResultNames
	 * @throws SQLException
	 */
	@SuppressWarnings("serial")
	protected static void insertAgedFlowsToDb(List<FiveTuple> listOfFlowsThatShouldAgeFiveTuples, Connection con) throws SQLException
	{
		FlowsDataStructureForDB flowsDataStructureForDb = FlowsDataStructureForDB.getInstance();
		List<String> allResultNames = new ArrayList<String>() {};
		PreparedStatement pstmt;
		
		String resultNames = "";
		String resultValues = "";

		Map<String, Integer> resultIntegerMap = flowsDataStructureForDb.getFlowInfoForDbStruct(listOfFlowsThatShouldAgeFiveTuples.get(0)).getIntegerMap();
		Map<String, Long> resultLongMap = flowsDataStructureForDb.getFlowInfoForDbStruct(listOfFlowsThatShouldAgeFiveTuples.get(0)).getLongMap();
		Set<String> allIntResults = resultIntegerMap.keySet();
		Set<String> allLongResults = resultLongMap.keySet();

		for (String intKey : allIntResults)
			allResultNames.add(intKey);
		for (String longKey : allLongResults)
			allResultNames.add(longKey);

		for (String resultName : allResultNames)
		{
			resultNames += resultName + ",";
			resultValues += "?,";
		}
		resultNames = resultNames.substring(0, resultNames.length() - 1);
		resultValues = resultValues.substring(0, resultValues.length() - 1);

		pstmt = con.prepareStatement("INSERT INTO all_flows(" + resultNames + ") VALUES (" + resultValues + ")");
		con.setAutoCommit(false);
		con.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

		for (FiveTuple fiveTuple : listOfFlowsThatShouldAgeFiveTuples)
		{
			resultIntegerMap = flowsDataStructureForDb.getFlowInfoForDbStruct(fiveTuple).getIntegerMap();
			resultLongMap = flowsDataStructureForDb.getFlowInfoForDbStruct(fiveTuple).getLongMap();

			for (int j = 0; j < allResultNames.size(); j++)
			{
				if (resultIntegerMap.containsKey(allResultNames.get(j)))
					pstmt.setInt(j + 1, resultIntegerMap.get(allResultNames.get(j)));
				else if (resultLongMap.containsKey(allResultNames.get(j)))
					pstmt.setLong(j + 1, resultLongMap.get(allResultNames.get(j)));
			}
			pstmt.addBatch();

			resultIntegerMap.clear();
			resultLongMap.clear();
			resultIntegerMap = null;
			resultLongMap = null;
			flowsDataStructureForDb.removeFlow(fiveTuple);
		}

		pstmt.executeBatch();
		con.commit();
		pstmt.clearBatch();
		pstmt.close();

		listOfFlowsThatShouldAgeFiveTuples.clear();
		allResultNames.clear();
		allIntResults.clear();
		allLongResults.clear();
		listOfFlowsThatShouldAgeFiveTuples = null;
		allResultNames = null;
		allIntResults = null;
		allLongResults = null;
		
		System.gc();
	}

	/**
	 * @param con
	 */
	protected static void createDbTable(Connection con, boolean dropTableIfExist)
	{
		try
		{
			if(dropTableIfExist)
				con.createStatement().execute("DROP TABLE IF EXISTS `capture_analyzer`.`all_flows`");

			con.createStatement().execute(
					"CREATE TABLE  `capture_analyzer`.`all_flows` " + 
					"(`flow_id` int(10) unsigned NOT NULL DEFAULT '0'," + 
					"`source_ip` bigint(20) unsigned NOT NULL DEFAULT '0'," + 
					"`source_port` int(10) unsigned NOT NULL DEFAULT '0'," + 
					"`dest_ip` bigint(20) unsigned NOT NULL DEFAULT '0'," + 
					"`dest_port` int(10) unsigned NOT NULL DEFAULT '0'," + 
					"`flow_type` int(10) unsigned NOT NULL DEFAULT '0'," + 
					"`is_full_tcp` int(10) unsigned NOT NULL DEFAULT '0'," +
					"`start_time` bigint(20) unsigned NOT NULL DEFAULT '0'," + 
					"`duration` bigint(20) unsigned NOT NULL DEFAULT '0'," + 
					"`number_of_packets` bigint(20) unsigned NOT NULL DEFAULT '0'," + 
					"`size` int(10) unsigned NOT NULL DEFAULT '0'," + 
					"`min_packet_size` int(10) unsigned NOT NULL DEFAULT '0'," + 
					"`average_packet_size` int(10) unsigned NOT NULL DEFAULT '0'," + 
					"`max_packet_size` int(10) unsigned NOT NULL DEFAULT '0'," + 
					"`min_ipg` bigint(20) unsigned NOT NULL DEFAULT '0'," + 
					"`average_ipg` bigint(20) unsigned NOT NULL DEFAULT '0'," + 
					"`max_ipg` bigint(20) unsigned NOT NULL DEFAULT '0'," + 
					"`tcp_init_min_ipg` bigint(20) unsigned NOT NULL DEFAULT '0'," + 
					"`tcp_init_average_ipg` bigint(20) unsigned NOT NULL DEFAULT '0'," + 
					"`tcp_init_max_ipg` bigint(20) unsigned NOT NULL DEFAULT '0'," + 
					"`flow_offset_in_cap` bigint(20) unsigned NOT NULL DEFAULT '0') " + 
					"ENGINE=MyISAM DEFAULT CHARSET=latin1 " + 
					"PARTITION BY RANGE (flow_type) (" +
					"PARTITION ICMP VALUES LESS THAN (2)," +
					"PARTITION OTHER1 VALUES LESS THAN (6)," +
					"PARTITION TCP VALUES LESS THAN (7)," +
					"PARTITION OTHER2 VALUES LESS THAN (17)," +
					"PARTITION UDP VALUES LESS THAN (18)," +
					"PARTITION OTHER3 VALUES LESS THAN MAXVALUE);");
		} catch (SQLException e1)
		{
			e1.printStackTrace();
		}
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
}
