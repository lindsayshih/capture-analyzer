package capanalyzer.netutils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import capanalyzer.database.ConnectionPool;
import capanalyzer.database.SQLExecutor;
import capanalyzer.netutils.build.FiveTuple;
import capanalyzer.netutils.build.IPPacket;
import capanalyzer.netutils.files.CaptureFileBlock;
import capanalyzer.netutils.files.CaptureFileFactory;
import capanalyzer.netutils.files.CaptureFileReader;
//import capanalyzer.netutils.files.erf.ErfPacketHeader;

public class AnalyzeCaptureFile
{
	static String erfFile = "e:\\capture_012_15_06_2009.erf";
	//static String erfFile = "c:\\capture_012_15_06_2009.erf";
	
	/** JDBC driver name */
	private static String driverName;

	/** JDBC connection URL */
	private static String connURL;

	/** JDBC connection username */
	private static String username;

	/** JDBC connection password */
	private static String password;

	/** select statement used by some test methods */
	static String sqlSelect = "SELECT * " + "FROM all_flows";

	
	public static void main(String[] args) throws IOException, NetUtilsException
	{	
		int agingTime = 120 * 1000000;
		List<IPacketAnalyzer> packetAnalyzers = new ArrayList<IPacketAnalyzer>();
		packetAnalyzers.add(new BaseAnalyzer());
		
		CaptureFileReader frd = CaptureFileFactory.createCaptureFileReader(erfFile);
		CaptureFileBlock nextblock = null;
		long packetTime;
		int counter = 0;
		
		FlowsDataStructure flowsDataStructure = FlowsDataStructure.getInstance();
		FlowsDataStructureForDB flowsDataStructureForDb = FlowsDataStructureForDB.getInstance();
		List<FiveTuple> listOfFiveTuples;
		
		setMySQLConnectInfo();
		SQLExecutor sqlExec2 = new SQLExecutor(getConnectionPool());
		sqlExec2.setAutoCommit(true);
		
		String resultNames = "";
		String resultValues = "";
		
		try
		{		
			while ((nextblock = frd.readNextBlock()) != null)
			{
				if (IPPacket.statIsIpPacket(nextblock.getMyData()))
				{
					for (IPacketAnalyzer packetAnalyzer : packetAnalyzers)
					{
						packetAnalyzer.processPacket(nextblock);
					}
					
					counter++;
					if(counter==500000)
					{
						packetTime = nextblock.getMyPktHdr().getTime();
						listOfFiveTuples = flowsDataStructure.getAllFlowFiveTuples();
						
						for (FiveTuple fiveTuple : listOfFiveTuples)
						{
							if(flowsDataStructure.getFlowInfoStruct(fiveTuple).getPacketTimes().get(flowsDataStructure.getFlowInfoStruct(fiveTuple).getPacketTimes().size()-1) < packetTime+agingTime)
							{
								for (IPacketAnalyzer packetAnalyzer : packetAnalyzers)
								{
									packetAnalyzer.finalizeFlow(fiveTuple);
									
									resultNames = "";
									resultValues = "";
									
									Map<String, Integer> resultIntegerMap = flowsDataStructureForDb.getFlowInfoForDbStruct(fiveTuple).getIntegerMap();
									Map<String, Long> resultLongMap = flowsDataStructureForDb.getFlowInfoForDbStruct(fiveTuple).getLongMap();
									
									Set<String> allIntResults = resultIntegerMap.keySet();
									for (String intKey : allIntResults)
									{
										resultNames += intKey + ",";
										resultValues += "'" + resultIntegerMap.get(intKey) + "',";
									}
									
									Set<String> allLongResults = resultLongMap.keySet();
									for (String longKey : allLongResults)
									{
										resultNames += longKey + ",";
										resultValues += "'" + resultLongMap.get(longKey) + "',";
									}
									
									resultNames = resultNames.substring(0, resultNames.length()-1);
									resultValues = resultValues.substring(0, resultValues.length()-1);
									
									String sql = "INSERT INTO all_flows (" + resultNames + ") VALUES (" + resultValues + ")";
									sqlExec2.runQuery(sql);
											
									flowsDataStructureForDb.removeFlow(fiveTuple);
								}
							}
						}
						
						counter=0;
					}
				}
			}
		} catch (Exception e)
		{
			System.out.println("Exception Caught");
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates a new connection pool object
	 * @return ConnectionPool
	 */
	public static ConnectionPool getConnectionPool()
	{
		return(new ConnectionPool(1, driverName, connURL, username, password));
	}
	
	/**
	 * Sets fields to values required to connect to a sample MySQL database
	 */
	private static void setMySQLConnectInfo()
	{
		driverName = "com.mysql.jdbc.Driver";
		connURL = "jdbc:mysql://localhost/capture_analyzer";
		username = "root";
		password = "oklydokly";
	}
}
