package capanalyzer.netutils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Statisics
{
	static List<Long> dbInserts;
	static List<Long> readAndStoreInMap;
	
	static int numOfDbPartitions;
	static int numOfMaps;
	static int sizeOfBuffer;
	
	public static void initStatsArrays()
	{
		dbInserts = new ArrayList<Long>();
		readAndStoreInMap = new ArrayList<Long>();
	}
	
	public static void addStatToDbInserts(long stat)
	{
		dbInserts.add(stat);
	}
	
	public static void addStatToReadAndStoreInMap(long stat)
	{
		readAndStoreInMap.add(stat);
	}
	
	public static void exportToCsvFile(String pathAndName)
	{
		String[][] results2DArray = new String[2][];
		
		results2DArray[0] = new String[dbInserts.size()+1];
		results2DArray[1] = new String[readAndStoreInMap.size()+1];
		
		results2DArray[0][0] = "DbInserts";
		results2DArray[1][0] = "ReadAndStoreInMap";
		
		for (int i = 0; i < results2DArray[0].length; i++)
		{
			results2DArray[0][i+1] = dbInserts.get(i)+"";
		}
		
		for (int i = 0; i < results2DArray[1].length; i++)
		{
			results2DArray[1][i+1] = readAndStoreInMap.get(i)+"";
		}
	
		
		writeDoubleArrayToCsvFile(pathAndName, results2DArray);
	}
	

	public static void writeDoubleArrayToCsvFile(String fileFullPath, String[][] array2d)
	{
		String line = "";

		try
		{
			if (isFileExists(fileFullPath))
				deleteFileNoException(fileFullPath);

			createFile(fileFullPath);

			for (int i = 0; i < array2d.length; i++)
			{
				for (int j = 0; j < array2d[i].length; j++)
				{
					line += array2d[i][j];
					if (j < array2d[i].length - 1)
						line += ",";
				}
				appendTextToFile(fileFullPath, line, false, true);
				line = "";
			}

			closeFile(fileFullPath);
		} catch (Exception e)
		{
			System.out.println("Problem creating results file");
		}
	}

	public static boolean isFileExists(String filename)
	{
		boolean isFileExists = (new File(filename)).exists();
		return isFileExists;
	}

	/**
	 * delete a file from the PC. Will catch the exception if occurs and return
	 * false.
	 * 
	 * @param filenameFullPath
	 *            the full file name to delete e.g "D:/temp/1.txt"
	 * @return true if deletion succeed
	 * @author ariky
	 */
	public static boolean deleteFileNoException(String theFileNameFullPath)
	{
		deleteFile(theFileNameFullPath);

		return true;
	}

	/**
	 * delete a file from the PC
	 * 
	 * @param filenameFullPath
	 *            the full file name to delete e.g "D:/temp/1.txt"
	 * @throws TestFailException
	 * @author ariky
	 */
	public static void deleteFile(String theFileNameFullPath)
	{
		(new File(theFileNameFullPath)).delete();
	}

	/**
	 * Creates a new file on the PC
	 * 
	 * @param filename
	 *            the full file name to create e.g "D://temp//1.txt"
	 * @throws TestFailException
	 * @author ariky
	 */
	public static void createFile(String filename)
	{
		createFile(filename, false);
	}

	/**
	 * Creates a new file on the PC
	 * 
	 * @param removeIfExist
	 *            if true and file exist, than deleting it
	 * @param filename
	 *            the full file name to create e.g "D://temp//1.txt"
	 * @throws TestFailException
	 * @author ariky
	 */
	public static void createFile(String filename, boolean removeIfExist)
	{
		if (removeIfExist)
		{
			if (isFileExists(filename))
			{
				deleteFile(filename);
				System.out.println("File removed successfully.");
			}
		}
		try
		{
			File file = new File(filename);
			// Create file if it does not exist
			boolean success;

			success = file.createNewFile();

			if (success)
			{
				System.out.println("File created successfully.");
			} else
			{
				// File already exists
				System.out.println("Couldn't create the file !\r\n");
			}
		} catch (IOException e)
		{
			e.toString();
			System.out.println("Creating File " + filename + " Failed .");
		}
	}

	/**
	 * Append to file on the PC
	 * 
	 * @param filename
	 * @param textToAdd
	 */
	public void appendTextToFile(String filename, String textToAdd)
	{
		appendTextToFile(filename, textToAdd, false, true);
	}

	public boolean appendTextToFile(String theFilename, String theTextToAdd, boolean addOnlyIfNotExist)
	{
		return appendTextToFile(theFilename, theTextToAdd, addOnlyIfNotExist, true);
	}

	/**
	 * Append to file on the PC
	 * 
	 * @param filename
	 *            the full file name to append e.g "D:/temp/1.txt"
	 * @param textToAdd
	 *            the Text
	 * @param addOnlyIfNotExist
	 *            if true will look for the text in the file and will add only
	 *            if fail to find
	 * @author ariky
	 */
	public static boolean appendTextToFile(String theFilename, String theTextToAdd, boolean addOnlyIfNotExist, boolean addNewLine)
	{
		boolean add = true; // default is to add

		// add any case, regardless of its existence

		try
		{
			if (add)
			{
				// System.out.println("appending file :" + theFilename);
				BufferedWriter out = new BufferedWriter(new FileWriter(theFilename, true));
				if (addNewLine) // add new line if needed
					out.write(theTextToAdd + "\r\n");
				else
					out.write(theTextToAdd);
				out.close();
				// System.out.println("done append");
				return true;
			}
		} catch (IOException e)
		{
			System.out.println("Problem appending -->" + e.toString());
		}
		return true;
	}

	public static boolean closeFile(String theFilename)
	{
		try
		{
			System.out.println("appending file :" + theFilename);
			BufferedWriter out = new BufferedWriter(new FileWriter(theFilename, true));
			// BufferedReader in = new BufferedReader(new
			// FileReader(theFilename));
			out.flush();

			out.close();
			// in.close();
		} catch (IOException e)
		{
			System.out.println("Problem close -->");
		}
		return true;
	}
	
	/**
	 * @return the numOfDbPartitions
	 */
	public static int getNumOfDbPartitions()
	{
		return numOfDbPartitions;
	}

	/**
	 * @param numOfDbPartitions the numOfDbPartitions to set
	 */
	public static void setNumOfDbPartitions(int numOfDbPartitions)
	{
		Statisics.numOfDbPartitions = numOfDbPartitions;
	}

	/**
	 * @return the sizeOfBuffer
	 */
	public static int getSizeOfBuffer()
	{
		return sizeOfBuffer;
	}

	/**
	 * @param sizeOfBuffer the sizeOfBuffer to set
	 */
	public static void setSizeOfBuffer(int sizeOfBuffer)
	{
		Statisics.sizeOfBuffer = sizeOfBuffer;
	}
	
	/**
	 * @return the numOfMaps
	 */
	public static int getNumOfMaps()
	{
		return numOfMaps;
	}

	/**
	 * @param numOfMaps the numOfMaps to set
	 */
	public static void setNumOfMaps(int numOfMaps)
	{
		Statisics.numOfMaps = numOfMaps;
	}
	
}
