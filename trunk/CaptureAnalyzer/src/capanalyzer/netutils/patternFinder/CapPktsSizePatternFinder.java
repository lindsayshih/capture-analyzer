package capanalyzer.netutils.patternFinder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import capanalyzer.netutils.NetUtilsException;
import capanalyzer.netutils.files.erf.ErfFileReader;
import capanalyzer.netutils.utils.LineArgs;

/**
 * search for packets size pattern.
 * 
 * 1 --> 60 bytes
 * 2 <-- 70 bytes
 * ..etx
 * 
 * Only data bytes!
 * 
 * 
 * @author rbaryana
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class CapPktsSizePatternFinder extends CapSearch
{
	public static final int DEFAULT_NO_RETR_NUM = 10;

	/**
	 * return pattern if founded.
	 * @param files - the files to search
	 * @param args - 
	 * @throws CapException
	 * @throws IOException 
	 * @throws NetUtilsException 
	 */
	public String getCapPktsSizePatternFinder(File[] files,String args) throws CapException, NetUtilsException, IOException
	{
		LineArgs largs = new LineArgs();
		largs.addArg("-pnum",1,"");
		largs.init(args);
		int maxpkts = largs.hasOption("-pnum")?largs.getArgAsInt("-pnum"):DEFAULT_NO_RETR_NUM; 
		FlowStruct flows[] = getAllFlows(files);
		flows = Utils.filterRetransmittionsFlows(flows, maxpkts);
		return findPktSizePatten(flows, maxpkts);
	}
	
	/**
	 * 
	 * @param theDir
	 * @param namePattern
	 * @param maxpkts
	 * @return
	 * @throws CapException
	 * @throws IOException 
	 * @throws NetUtilsException 
	 */
	public String getCapPktsSizePatternFinder(String theDir, String namePattern, int maxpkts) throws CapException, NetUtilsException, IOException
	{
		File[] files = getAllMatchingFiles(theDir, namePattern);
		FlowStruct flows[] = getAllFlows(files);
		flows = Utils.filterRetransmittionsFlows(flows, maxpkts);
		return findPktSizePatten(flows, maxpkts);
	}
		
	/**
	 * find pattenr
	 * @param files - files to look in.
	 * @param maxpkts - max pkt to search
	 * @return string result
	 * @throws CapException
	 * @throws IOException 
	 * @throws NetUtilsException 
	 */
	public String getCapPktsSizePatternFinder(File[] files, int maxpkts) throws CapException, NetUtilsException, IOException
	{
    	FlowStruct flows[] = getAllFlows(files);
		flows = Utils.filterRetransmittionsFlows(flows, maxpkts);
		return findPktSizePatten(flows, maxpkts);
	}

	private FlowStruct[] getAllFlows(File[] files) throws CapException, NetUtilsException, IOException
	{
		FlowStruct toReturn[] = new FlowStruct[files.length];

		for (int i = 0; i < files.length; i++)
		{

			toReturn[i] = new FlowStruct(i, files[i].toString(), ErfFileReader.readCapRawData(files[i].toString()));
		}

		return toReturn;
	}

	/**
	 * filter files by thier name.
	 * 
	 * @param theDir -
	 *            the directory with the files
	 * @param thePattern -
	 *            the matching pattern
	 * @return the matching files
	 * @throws CapException
	 */
	private File[] getAllMatchingFiles(String theDir, String thePattern) throws CapException
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
			//System.out.println("checking File : "+files[i].getName());
			if (files[i].getName().matches(thePattern)) matchingFiles.add(files[i]);
		}

		return (File[]) matchingFiles.toArray(new File[] {});
	}

	private String findPktSizePatten(FlowStruct[] flows, int maxpkts)
	{

		if (flows.length < myMinFlowsToCheck) return "No Results -- too few flows";

		HashMap[] sizes = new HashMap[maxpkts+1];
		for (int i = 0; i < sizes.length; i++)
		{
			sizes[i] = new HashMap();
		}

		for (int i = 0; i < flows.length; i++)
		{

			for (int j = 0; j < maxpkts+1; j++)
			{
				byte[] data = flows[i].getData(j + 1);
				if (data == null) break;
				
				PacketSide side = flows[i].getDataPktSide(j + 1);

				String match = side.toString() + ":" + data.length;

				if (sizes[j].containsKey(match))
				{
					Integer integer = (Integer) sizes[j].get(match);
					sizes[j].put(match, new Integer(integer.intValue() + 1));
				}
				else
				{
					sizes[j].put(match, new Integer(1));
				}
			}
		}
		return processPattern(sizes, flows.length);
	}

	private String processPattern(HashMap[] arr, int flownum)
	{
		String toReturn = "Pattern found :\n";

		boolean found = false;

		for (int i = 0; i < arr.length; i++)
		{
			if (isClearPatternExists(arr[i], flownum))
			{
				found = true;
				toReturn = toReturn + "pkt num: " + i + " , size " + getMaxSizeExists(arr[i], flownum) + "\n";
			}
			else
			{
				toReturn = toReturn + "pkt num: " + i + " ," + getSidePattern(arr[i]) + ", No clear pattern -->range (" + getMinPktsSize(arr[i]) + " -- " + getMaxPktsSize(arr[i]) + ")\n";
			}
		}

		if (found == false)
		{
			return "No Pattern";
		}
		
		return toReturn;
	}

	private boolean isClearPatternExists(HashMap hash, int flownum)
	{
		for (Iterator iter = hash.values().iterator(); iter.hasNext();)
		{
			Integer num = (Integer) iter.next();
			if (num.intValue() > (flownum / myMinResultRatio)) return true;
		}
		return false;
	}

	private int getMinPktsSize(HashMap hash)
	{
		int min = 1400;
		for (Iterator ir = hash.keySet().iterator(); ir.hasNext();)
		{
			String key = (String) ir.next();
			String[] split = key.split(":");
			int num = Integer.parseInt(split[1].trim());
			if (num < min) min = num;
		}

		return min;
	}

	private int getMaxPktsSize(HashMap hash)
	{
		int max = 0;
		for (Iterator ir = hash.keySet().iterator(); ir.hasNext();)
		{
			String key = (String) ir.next();
			String[] split = key.split(":");
			int num = Integer.parseInt(split[1].trim());
			if (num > max) max = num;
		}

		return max;
	}

	private String getMaxSizeExists(HashMap hash, int numOfFlows)
	{
		int max = 0;
		String key = "";
		for (Iterator iter = hash.keySet().iterator(); iter.hasNext();)
		{
			String currentkey = (String) iter.next();
			Integer num = (Integer) hash.get(currentkey);
			if (num.intValue() > max)
			{
				max = num.intValue();
				key = currentkey;
			}
		}
		int precent = (max * 100) / numOfFlows;
		return key + " " + precent + "% ";
	}

	public String getSidePattern(HashMap hash)
	{
		int clinet = 0;
		int server = 0;
		for (Iterator ir = hash.keySet().iterator(); ir.hasNext();)
		{
			String key = (String) ir.next();
			if (key.indexOf(PacketSide.CLIENT_TO_SERVER.toString()) != -1)
				clinet++;
			else
				server++;
		}
		if (clinet > server)
			return PacketSide.CLIENT_TO_SERVER.toString();

		else
			return PacketSide.SERVER_TO_CLIENT.toString();

	}

	public static void main(String[] args) throws CapException, NetUtilsException, IOException
	{

		CapPktsSizePatternFinder finder = new CapPktsSizePatternFinder();

		String result = finder.getCapPktsSizePatternFinder("D:\\taskforce\\bittorent\\tmp2", "(\\w)+Big.cap", 15);
		System.out.println(result);
	}

}