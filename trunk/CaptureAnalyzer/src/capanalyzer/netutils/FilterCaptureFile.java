package capanalyzer.netutils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import capanalyzer.GlobalConfig;
import capanalyzer.database.ConnectionPool;
import capanalyzer.netutils.build.FiveTuple;
import capanalyzer.netutils.build.IPPacket;
import capanalyzer.netutils.files.CaptureFileBlock;
import capanalyzer.netutils.files.CaptureFileFactory;
import capanalyzer.netutils.files.CaptureFileReader;
import capanalyzer.netutils.files.erf.ErfFileWriter;
import capanalyzer.netutils.files.erf.ErfPacketHeader;

public class FilterCaptureFile
{
	static String inputErfFile = "d:\\capture_012_15_06_2009_5G.erf";
	
	static String outputEerfFile = "d:\\test_output.erf";

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
		CaptureFileReader frd = CaptureFileFactory.createCaptureFileReader(inputErfFile);
		
		ErfFileWriter fwrt = new ErfFileWriter(outputEerfFile);// TEMP
		
		CaptureFileBlock nextblock = null;

		setMySQLConnectInfo();
		Connection con = getConnectionPool().getConnection();

		Map<String, Boolean> filterMap = new HashMap<String, Boolean>();
		try
		{
			ResultSet tuples = con.createStatement().executeQuery("SELECT source_ip,source_port,dest_ip,dest_port,flow_type FROM all_flows a where flow_type=17 and number_of_packets=1");
			while (tuples.next())
			{
				long sourceIp = tuples.getBigDecimal("source_ip").longValue();
				int sourcePort = tuples.getInt("source_port");
				long destIp = tuples.getBigDecimal("dest_ip").longValue();
				int destPort = tuples.getInt("dest_port");
				int flowType = tuples.getInt("flow_type");
				
				String key = new FiveTuple(sourceIp, sourcePort, destIp, destPort, flowType).getKey();
				if(filterMap.containsKey(key)==false)
					filterMap.put(key, true);
			}
			
			while ((nextblock = frd.readNextBlock()) != null)
			{
				if (IPPacket.statIsIpPacket(nextblock.getMyData()))
				{
					FiveTuple tempFiveTuple = new FiveTuple(nextblock.getMyData(), false);
					if(filterMap.containsKey(tempFiveTuple.getKey()))
					{
						fwrt.addPacket((ErfPacketHeader)nextblock.getMyPktHdr(), nextblock.getMyData());
						filterMap.remove(tempFiveTuple.getKey());
					}
				}
			}

			fwrt.close();

		} catch (Exception e)
		{
			System.out.println("Exception Caught");
			e.printStackTrace();
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
