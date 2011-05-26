package capanalyzer.netutils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import capanalyzer.netutils.build.FiveTuple;
import capanalyzer.netutils.build.IPPacket;
import capanalyzer.netutils.files.CaptureFileBlock;
import capanalyzer.netutils.files.CaptureFileFactory;
import capanalyzer.netutils.files.CaptureFileReader;
import capanalyzer.netutils.files.erf.ErfFileWriter;
import capanalyzer.netutils.files.erf.ErfPacketHeader;

public class FilterCaptureFileFromFlowsFile
{
	static String inputErfFile = "e:\\udp_1_packet_filter_3rd_Try.erf";
	
	static String outputEerfFile = "e:\\udp_1_packet_filter_Unknown_Traffic.erf";
	
	static String flowsFile = "c:/Unknown_1_Packet_Filter.csv";
	
	static int flowType = 17;
	
	static int numberOfMapsToUse = 4;

	public static void main(String[] args) throws IOException, NetUtilsException
	{
		CaptureFileReader frd = CaptureFileFactory.createCaptureFileReader(inputErfFile);
		
		ErfFileWriter fwrt = new ErfFileWriter(outputEerfFile);// TEMP
		
		CaptureFileBlock nextblock = null;

		List<Map<Long, Boolean>> filterMapList = new ArrayList<Map<Long, Boolean>>();
		for (int i = 0; i < numberOfMapsToUse; i++)
		{
			filterMapList.add(new HashMap<Long, Boolean>());
		}
		
		try
		{
			System.out.println("Going to populate map");
			FileInputStream fstream = new FileInputStream(flowsFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String record;
			long numberOfUniqeFlows = 0;
			long numberOfFlows = 0;
			while ((record = br.readLine()) != null)   
			{
				String[] recordArray = record.split(",");
				if(recordArray[0].equals("pckt"))
				{
					long sourceIp = Long.parseLong(recordArray[1],16);
					long destIp = Long.parseLong(recordArray[2],16);
					int sourcePort = Integer.parseInt(recordArray[3], 16);
					int destPort = Integer.parseInt(recordArray[4], 16);
					int flowType = Integer.parseInt(recordArray[5]);
					
					FiveTuple fiveT = new FiveTuple(sourceIp, sourcePort, destIp, destPort, flowType);
					
					long key = fiveT.longHashCode();
					if(filterMapList.get((int)(key%numberOfMapsToUse)).containsKey(key)==false)
					{
						filterMapList.get((int)(key%numberOfMapsToUse)).put(key, true);
						numberOfUniqeFlows++;
					}
				
					numberOfFlows++;
					if(numberOfFlows%1000000==0)
					{
						System.out.println("numberOfFlows=" + numberOfFlows);
					}
				}
			}
			in.close();
			System.out.println("Nuber of unique flows = " + numberOfUniqeFlows);
			System.out.println("Total Nuber of  flows = " + numberOfFlows);
			System.out.println("Done populating");
			
			long numberOfPackets = 0;
			while ((nextblock = frd.readNextBlock()) != null)
			{
				if (IPPacket.statIsIpPacket(nextblock.getMyData()))
				{
					FiveTuple fiveT = new FiveTuple(nextblock.getMyData(), false);		
					long key = fiveT.longHashCode();
					if(fiveT.getMyType()==flowType && filterMapList.get((int)(key%numberOfMapsToUse)).containsKey(key))
					{
						fwrt.addPacket((ErfPacketHeader)nextblock.getMyPktHdr(), nextblock.getMyData());
						numberOfPackets++;
						if(numberOfPackets%50000==0)
						{
							System.out.println("NumberOfPackets=" + numberOfPackets);
							System.out.println("Percentage Done: " + frd.getBytesRead() / (float) frd.getCapFileSizeInBytes());
						}
						
						//filterMap.remove(tempFiveTuple.getKey());
					}
				}
			}

			System.out.println("Total Number of Packets = " + numberOfPackets);
			fwrt.close();
			filterMapList.clear();

		} catch (Exception e)
		{
			System.out.println("Exception Caught");
			e.printStackTrace();
		}
	}
	
	/**
	 * translate csv file to a 2D array 
	 * @param csvFileStr
	 * @return 2D array represent the csv file
	 * @throws TestFailException
	 * @author liron
	 */
	public static String[][] getArrayFromCsvFile(String theCsvFile)
	{
		String[][] full2DArray = null;

		String csvAsString = fileToString(theCsvFile);
		
		full2DArray = getArrayFromCsvString(csvAsString);
		
		return full2DArray;
	}
	
	/**
	 * translate csv file to a 2D array 
	 * @param theCsvString
	 * @return 2D array represent the csv string
	 * @throws TestFailException
	 */
	public static String[][] getArrayFromCsvString(String theCsvString)
	{
		String[] tempLinesArray = null;
		String[][] full2DArray = null;

		String csvAsString = theCsvString;
		tempLinesArray = csvAsString.split("\n"); //lines
		full2DArray = new String[tempLinesArray.length][];
		for (int line=0; line<tempLinesArray.length; line++)
			full2DArray[line] = tempLinesArray[line].split(",");
		
		return full2DArray;
	}
	
	/**
	 * read a file in the PC and store it as String 
	 * @param textFileName the file to read from 
	 * @return the String
	 * @throws TestFailException
	 */
	public static String fileToString(String textFileName)
	{
		try
		{
			File file = new File(textFileName);
			byte[] fileBytes = new byte[(int) (file.length())];
			FileInputStream in = new FileInputStream(file);
			in.read(fileBytes, 0, fileBytes.length);
			in.close();
			 
			return new String(fileBytes);
		}
		catch (Exception e)
		{

			System.out.println("Problem converting file to string");
		}
		
		return "";
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
}
