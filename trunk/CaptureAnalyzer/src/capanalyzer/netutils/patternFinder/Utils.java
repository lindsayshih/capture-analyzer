package capanalyzer.netutils.patternFinder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import capanalyzer.netutils.NetUtilsException;
import capanalyzer.netutils.files.CaptureFileBlock;
import capanalyzer.netutils.files.CaptureFileFactory;
import capanalyzer.netutils.files.CaptureFileReader;
import capanalyzer.netutils.files.erf.ErfFileReader;

/**
 * Common methods collection.
 * 
 * @author rbaryana
 *
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class Utils
{
	/**
	 * filter flows with retransmittions on the first pktnum packets.
	 * @param flows - flow array
	 * @param pktnum - the number
	 * @return the array filtered
	 * @throws CapException
	 */
	public static FlowStruct[] filterRetransmittionsFlows(FlowStruct flows[], int pktnum) throws CapException
	{
		if (flows == null) throw new CapException("Got flows[] = null !?");
		ArrayList arrlist = new ArrayList();

		for (int i = 0; i < flows.length; i++)
		{

			if (flows[i].isTransmissions(pktnum) == false) arrlist.add(flows[i]);
		}

		return (FlowStruct[]) arrlist.toArray(new FlowStruct[] {});

	}

	/**
	 * Filter only files that match the regular expression
	 * @param theDir
	 * @param thePattern
	 * @return the files that passed the filter
	 * @throws CapException
	 */
	public static File[] filter(String theDir, String thePattern) throws CapException
	{
		File dir = new File(theDir);

		if (dir.isDirectory() == false)
		{
			throw new CapException("Got non directory name:" + theDir);
		}

		ArrayList matchingFiles = new ArrayList();

		File[] files = dir.listFiles();

		

		for (int i = 0; i < files.length; i++)
		{
			if (files[i].isDirectory())
				continue;
			if (thePattern == null)
			{
				if (files[i].toString().toLowerCase().endsWith(".cap"))
				    matchingFiles.add(files[i]);
				continue;
			}
				
					
			if (files[i].getName().matches(thePattern)) matchingFiles.add(files[i]);
		}

		return (File[]) matchingFiles.toArray(new File[] {});
	}

	/**
	 * clean all files in directory (should be used with carfull)
	 * @param theDirName
	 * @return true if all files were deleted or false if not.
	 * @throws CapException
	 */
	public static boolean cleanDir(String theDirName) throws CapException
	{
		System.out.println("Deleteing directory : " + theDirName);

		File dir = new File(theDirName);

		if (dir.isDirectory() == false)
		{
			throw new CapException("Got non directory name:" + theDirName);
		}
		File[] files = dir.listFiles();

		boolean result = true;

		for (int i = 0; i < files.length; i++)
		{
			result = result && files[i].delete();
			System.out.print(".");
			if (i > 0 && i % 50 == 0) System.out.print("\n");
		}

		if (result == false)
		{
			System.out.println("\nNot all files were deleted !!");
		}
		else
		{
			System.out.println("\nAll files were deleted.");
		}

		return result;

	}

	/**
	 * return all files ending accoring to the split flow convention.
	 * (TCP_Big,UDP_2...etc)
	 * @param theDir
	 * @return
	 * @throws CapException
	 */
	public static String[] getAllFilesEndings(String theDir) throws CapException
	{
		File dir = new File(theDir);

		if (dir.isDirectory() == false)
		{
			throw new CapException("Got non directory name:" + theDir);
		}

		File[] files = dir.listFiles();

		HashSet types = new HashSet();

		for (int i = 0; i < files.length; i++)
		{
			String fields[] = files[i].getName().split("_");
			if (fields == null || fields.length < 3) continue;

			String type = fields[1];
			String num = fields[2].split("\\.")[0];
			if (num == null) continue;

			try
			{
				Integer.parseInt(num);
				String name = type + "_" + num;
				if (!types.contains(name)) types.add(name);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
    	types.add("TCP_Big");

		return (String[]) types.toArray(new String[] {});
	}

	/**
	 * get all flows from files.
	 * assuming each file contains one flow.
	 * @param files
	 * @return flows array
	 * @throws CapException
	 * @throws IOException 
	 * @throws NetUtilsException 
	 */
	public static FlowStruct[] getAllFlows(File[] files) throws CapException, NetUtilsException, IOException
	{
		FlowStruct toReturn[] = new FlowStruct[files.length];
		
		for (int i = 0; i < files.length; i++)
		{

			toReturn[i] = new FlowStruct(i, files[i].toString(), ErfFileReader.readCapRawData(files[i].toString()));
		}

		return toReturn;
	}
	
	/**
	 * get all flows from specific file.
	 * @param files
	 * @return flows array
	 * @throws CapException
	 * @throws NetUtilsException 
	 * @throws IOException 
	 */
	public static FlowStruct[] getAllFlows(String fileNameAndPath, int numberOfFlows) throws CapException, IOException, NetUtilsException
	{
		CaptureFileReader frd = CaptureFileFactory.createCaptureFileReader(fileNameAndPath);
		CaptureFileBlock nextblock = null;
		int counter = 0;
		FlowStruct[] toReturn = new FlowStruct[numberOfFlows];
		
		while ((nextblock = frd.readNextBlock()) != null)
		{
			toReturn[counter] = new FlowStruct(counter, fileNameAndPath, new byte[][] {nextblock.getMyData()});
			if(toReturn[counter].isValidIPFlow())
				counter++;
		}
		
		FlowStruct[] toReturnFiltered = new FlowStruct[counter];
		for (int i = 0; i < toReturnFiltered.length; i++)
		{
			toReturnFiltered[i] = toReturn[i];
		}

		return toReturnFiltered;
	}

	/**
	 * remove all files
	 * @param theFiles
	 */
	public static void cleanFiles(File[] theFiles)
	{
		for (int i = 0; i < theFiles.length; i++)
		{
			if (theFiles[i].toString().endsWith("cap"))
			theFiles[i].delete();
		}
	}
	
	

	/**
	 * Sum array of string to single string.
	 * @param arr 
	 * @return single string
	 */
	public static String sumString(String[] arr)
	{
		BigString str = new BigString();
		for (int i = 0; i < arr.length; i++)
		{
			str.addString(arr[i] + "\n");
		}
		return str.getAsString();
	}
	
	/**
	 * @param theDir
	 * @return all files in the directory including sub directorys.
	 */
	public static File[] getAllFiles(String theDir)
	{
		return getAllFiles(theDir,null);
	}
	
	/**
	 * @param theDir
	 * @param thePattern - only file that thier name matched the regular expression will be returned.
	 * @return all files in the dir including sub direcorys.
	 */
	public static File[] getAllFiles(String theDir,String thePattern)
	{
		ArrayList lst = new ArrayList();
		File dir = new File(theDir);
		if (dir.isDirectory() == false)
			return new File[]{};
		
		File[] files = dir.listFiles();
		for (int i = 0; i < files.length; i++)
		{
			//System.out.print(".");
			if (thePattern != null && files[i].toString().matches(thePattern) == false)
				continue;
			
			if (files[i].isDirectory())
			{
				File[] tmp = getAllFiles(files[i].toString(),thePattern);
				for (int j = 0; j < tmp.length; j++)
				{
					lst.add(tmp[j]);
				}
			}
			else
				lst.add(files[i]);
		}
		return (File[]) lst.toArray(new File[]{});
	}
	
	/**
	 * 
	 * @param files
	 */
	public static void printFileNames(File[] files)
	{
		for (int i = 0; i < files.length; i++)
		{
			System.out.println(files[i].toString());
		}
	}

}
