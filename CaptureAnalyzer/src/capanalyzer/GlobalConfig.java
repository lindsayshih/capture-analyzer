package capanalyzer;

public class GlobalConfig
{
	public static class Database
	{
		static String driverName = "com.mysql.jdbc.Driver";
		static String connURL = "jdbc:mysql://localhost/capture_analyzer";
		static String username = "root";
		static String password = "oklydokly";
		static int numberOfPartitions = 80;

		/**
		 * @return the driverName
		 */
		public static String getDriverName()
		{
			return driverName;
		}

		/**
		 * @param driverName
		 *            the driverName to set
		 */
		public static void setDriverName(String driverName)
		{
			Database.driverName = driverName;
		}

		/**
		 * @return the connURL
		 */
		public static String getConnURL()
		{
			return connURL;
		}

		/**
		 * @param connURL
		 *            the connURL to set
		 */
		public static void setConnURL(String connURL)
		{
			Database.connURL = connURL;
		}

		/**
		 * @return the username
		 */
		public static String getUsername()
		{
			return username;
		}

		/**
		 * @param username
		 *            the username to set
		 */
		public static void setUsername(String username)
		{
			Database.username = username;
		}

		/**
		 * @return the password
		 */
		public static String getPassword()
		{
			return password;
		}

		/**
		 * @param password
		 *            the password to set
		 */
		public static void setPassword(String password)
		{
			Database.password = password;
		}

		/**
		 * @return the numberOfPartitions
		 */
		public static int getNumberOfPartitions()
		{
			return numberOfPartitions;
		}

		/**
		 * @param numberOfPartitions
		 *            the numberOfPartitions to set
		 */
		public static void setNumberOfPartitions(int numberOfPartitions)
		{
			Database.numberOfPartitions = numberOfPartitions;
		}
	}

	public static class CaptureFileReadParams
	{
		static int numberOfMaps = 16;
		static int numberOfDbMaps = 4;
		static int sizeOfBuffer = 32;
		static int agingTime = 120;

		/**
		 * @return the numberOfMaps
		 */
		public static int getNumberOfMaps()
		{
			return numberOfMaps;
		}

		/**
		 * @param numberOfMaps
		 *            the numberOfMaps to set
		 */
		public static void setNumberOfMaps(int numberOfMaps)
		{
			CaptureFileReadParams.numberOfMaps = numberOfMaps;
		}

		/**
		 * @return the numberOfDbMaps
		 */
		public static int getNumberOfDbMaps()
		{
			return numberOfDbMaps;
		}

		/**
		 * @param numberOfDbMaps
		 *            the numberOfMaps to set
		 */
		public static void setNumberOfDbMaps(int numberOfDbMaps)
		{
			CaptureFileReadParams.numberOfDbMaps = numberOfDbMaps;
		}
		
		/**
		 * @return the sizeOfBuffer
		 */
		public static int getSizeOfBuffer()
		{
			return sizeOfBuffer;
		}

		/**
		 * @param sizeOfBuffer
		 *            the sizeOfBuffer to set
		 */
		public static void setSizeOfBuffer(int sizeOfBuffer)
		{
			CaptureFileReadParams.sizeOfBuffer = sizeOfBuffer;
		}

		/**
		 * @return the agingTime
		 */
		public static int getAgingTime()
		{
			return agingTime;
		}

		/**
		 * @param agingTime
		 *            the agingTime to set
		 */
		public static void setAgingTime(int agingTime)
		{
			CaptureFileReadParams.agingTime = agingTime;
		}
	}
}
