package capanalyzer.netutils.patternFinder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import capanalyzer.netutils.NetUtilsException;
import capanalyzer.netutils.utils.LineArgs;

/**
 * find is pkt size can be found in the packet.
 * 
 * @author rbaryana
 *
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class CapPktSizeInPacketFinder extends CapSearch
{
	public static final int DEFAULT_MAX_PKT_SEARCH = 10;
	
	/**
	 * search for constant (exact packet length is in the packet)
	 * @param files
	 * @param args
	 * @return matched
	 * @throws CapException
	 * @throws IOException 
	 * @throws NetUtilsException 
	 */
	public String findConstantCapPktSizeInPacketString(File[] files,String args) throws CapException, NetUtilsException, IOException
	{
		LineArgs largs = new LineArgs();
		largs.addArg("-pnum",1,"");
		largs.init(args);
		int maxpkts = largs.hasOption("-pnum")?largs.getArgAsInt("-pnum"):DEFAULT_MAX_PKT_SEARCH;
		return Utils.sumString(findConstantCapPktSizeInPacketString(files,maxpkts));
	}
	
	/**
	 * 
	 * @param files
	 * @param maxpkts
	 * @return
	 * @throws CapException
	 * @throws IOException 
	 * @throws NetUtilsException 
	 */
	public String[] findConstantCapPktSizeInPacketString(File[] files,int maxpkts) throws CapException, NetUtilsException, IOException
	{
		FlowStruct flows[] = Utils.getAllFlows(files);
		flows = Utils.filterRetransmittionsFlows(flows, maxpkts);
		CapPktSizeInPktResult[] tmp = findConstantCapPktSizeInPacketString(flows, maxpkts);
		String[] toreturn = new String[tmp.length];
		for (int i = 0; i < toreturn.length; i++)
		{
			toreturn[i] = tmp[i].toString();
		}
		return toreturn;
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
	public String[] findConstantCapPktSizeInPacketString(String theDir, String namePattern, int maxpkts) throws CapException, NetUtilsException, IOException
	{
		File[] files = Utils.filter(theDir, namePattern);
		FlowStruct flows[] = Utils.getAllFlows(files);
		flows = Utils.filterRetransmittionsFlows(flows, maxpkts);
		CapPktSizeInPktResult[] tmp = findConstantCapPktSizeInPacketString(flows, maxpkts);
		String[] toreturn = new String[tmp.length];
		for (int i = 0; i < toreturn.length; i++)
		{
			toreturn[i] = tmp[i].toString();
		}
		return toreturn;
	}
	
	/**
	 * run with command line
	 * @param files
	 * @param args
	 * @return the matches
	 * @throws CapException
	 * @throws IOException 
	 * @throws NetUtilsException 
	 */
	public String findDynamicCapPktSizeInPacketString(File[] files, String args) throws CapException, NetUtilsException, IOException
	{
		LineArgs largs = new LineArgs();
		largs.addArg("-pnum",1,"");
		largs.init(args);
		int maxpkts = largs.hasOption("-pnum")?largs.getArgAsInt("-pnum"):DEFAULT_MAX_PKT_SEARCH;
		return Utils.sumString(findDynamicCapPktSizeInPacketString(files,maxpkts));
	}
	/**
	 * 
	 * @param files
	 * @param maxpkts
	 * @return
	 * @throws CapException
	 * @throws IOException 
	 * @throws NetUtilsException 
	 */
	public String[] findDynamicCapPktSizeInPacketString(File[] files, int maxpkts) throws CapException, NetUtilsException, IOException
	{
		FlowStruct flows[] = Utils.getAllFlows(files);
		flows = Utils.filterRetransmittionsFlows(flows, 5);
		CapPktSizeInPktResult[] tmp = findDynamicCapPktSizeInPacketString(flows, maxpkts);
		String[] toreturn = new String[tmp.length];
		for (int i = 0; i < toreturn.length; i++)
		{
			toreturn[i] = tmp[i].toString();
		}
		return toreturn;
	}

	
	private CapPktSizeInPktResult[] findConstantCapPktSizeInPacketString(FlowStruct[] theFlows, int theMaxpkts)
	{
		HashMap hasharr[] = new HashMap[theMaxpkts];
		 for (int i = 0; i < hasharr.length; i++)
		{
			hasharr[i] = new HashMap();
		}
		for (int i = 0; i < theMaxpkts; i++)
		{
			searchConstantFlows(hasharr[i],theFlows,theMaxpkts,i);
		}
		ArrayList tmplist = new ArrayList();
		for (int i = 0; i < hasharr.length; i++)
		{
			CapPktSizeInPktResult[] toReturn = (CapPktSizeInPktResult[]) hasharr[i].values().toArray(new CapPktSizeInPktResult[]{});
			for (int j = 0; j < toReturn.length; j++)
			{
				tmplist.add(toReturn[j]);
			}
		}
		
		CapPktSizeInPktResult[] tmp2 = (CapPktSizeInPktResult[]) tmplist.toArray(new CapPktSizeInPktResult[]{});
		Arrays.sort(tmp2);
		return tmp2;
	}
	
	private CapPktSizeInPktResult[] findDynamicCapPktSizeInPacketString(FlowStruct[] theFlows, int theMaxpkts)
	{
		HashMap hasharr[] = new HashMap[theMaxpkts];
        for (int i = 0; i < hasharr.length; i++)
		{
			hasharr[i] = new HashMap();
		}
		for (int i = 0; i < theMaxpkts; i++)
		{
			searchDynamicFlows(hasharr[i],theFlows,i);
		}
		
		ArrayList tmplist = new ArrayList();
		for (int i = 0; i < hasharr.length; i++)
		{
			CapPktSizeInPktResult[] toReturn = (CapPktSizeInPktResult[]) hasharr[i].values().toArray(new CapPktSizeInPktResult[]{});
			for (int j = 0; j < toReturn.length; j++)
			{
				tmplist.add(toReturn[j]);
			}
		}
		
		CapPktSizeInPktResult[] tmp2 = (CapPktSizeInPktResult[]) tmplist.toArray(new CapPktSizeInPktResult[]{});
		Arrays.sort(tmp2);
		return tmp2;
	}

	private HashMap searchConstantFlows(HashMap hash,FlowStruct[] theFlows, int theMaxPkt, int pktNum)
	{
		for (int j = 0; j < theFlows.length; j++)
		{
			byte[] data = theFlows[j].getData(pktNum);
			
			if (data == null) continue;

			int[] results = isSuspectedByte(data, true);
			if (results == null || results.length == 0) 
			{
				results = isSuspectedByte(data, false);
			}

			for (int i = 0; i < results.length; i++)
			{
				if (hash.containsKey(new Integer(results[i])))
				{
					CapPktSizeInPktResult res = (CapPktSizeInPktResult) hash.get(new Integer(results[i]));
					res.incNumOfTimes();

				}
				else
				{
					CapPktSizeInPktResult res = new CapPktSizeInPktResult(pktNum, false, results[i],theFlows.length);
					hash.put(new Integer(results[i]), res);
				}
			}
		}
		return hash;
	}
	
	private HashMap searchDynamicFlows(HashMap hash,FlowStruct[] theFlows, int pktNum)
	{
		for (int j = 0; j < theFlows.length; j++)
		{
			byte[] data = theFlows[j].getData(pktNum);
			if (data == null) continue;

			int[] results = isSuspectedDynamicByte(data, true);
			if (results == null || results.length == 0)
			{
				
				results = isSuspectedDynamicByte(data, false);
			}
	
			for (int i = 0; i < results.length; i++)
			{
				if (hash.containsKey(new Integer(results[i])))
				{
					CapPktSizeInPktResult res = (CapPktSizeInPktResult) hash.get(new Integer(results[i]));
					res.incNumOfTimes();
 				}
				else
				{
					CapPktSizeInPktResult res = new CapPktSizeInPktResult(pktNum, false, results[i],theFlows.length);
					hash.put(new Integer(results[i]), res);
				}
			}
		}
		return hash;
	}
	
	

	/**
	 * check if there is a constant byte with the data length.
	 * @param data - the data array.
	 * @param high - better probability much 
	 * @return array of possible indxs.
	 */
	private int[] isSuspectedByte(byte[] data, boolean high)
	{
		ArrayList arrlst = new ArrayList();
		int length = data.length;
		int low = length % 256;
		for (int i = 0; i < data.length; i++)
		{
			if (data[i] == low)
			{
				if (!high)
					arrlst.add(new Integer(i));

				else if (makeSure(data, i, length)) arrlst.add(new Integer(i));
			}

		}
		int toreturn[] = new int[arrlst.size()];
		for (int i = 0; i < toreturn.length; i++)
		{
			toreturn[i] = ((Integer) arrlst.get(i)).intValue();
		}
		return toreturn;
	}

	/**
	 * check if there is a dynamic byte with the data length.
	 * @param data - the data array.
	 * @param high - better probability much 
	 * @return array of possible indxs.
	 */
	private int[] isSuspectedDynamicByte(byte[] data, boolean high)
	{
		ArrayList arrlst = new ArrayList();
		for (int i = 0; i < data.length; i++)
		{
			int dynamiclow = (data.length - (i + 1)) % 256;

			if (data[i] == dynamiclow)
			{
				if (!high)
					arrlst.add(new Integer(i));

				else if (makeSure(data, i, dynamiclow)) arrlst.add(new Integer(i));
			}
		}
		int toreturn[] = new int[arrlst.size()];
		for (int i = 0; i < toreturn.length; i++)
		{
			toreturn[i] = ((Integer) arrlst.get(i)).intValue();
		}
		return toreturn;
	}

	/**
	 * check that the match remains with one byte forward or backward.
	 * it is assumed that the length must be at least 2 bytes.
	 * @param arr
	 * @param idx
	 * @param size
	 * @return true on success and flase on failure.
	 */
	private boolean makeSure(byte[] arr, int idx, int size)
	{
		int sum = 0;
		int size2Bytes = size % 256 + ((size >> 8) % 256) * 256;
		if (idx > 0)
		{
			sum = arr[idx] + (arr[idx - 1] * 256);
			if (sum == size2Bytes)
			{
				return true;
			}
		}
		if (idx < arr.length - 1)
		{
			sum = arr[idx] + (arr[idx + 1] * 256);
			if (sum == size2Bytes)
			{
				return true;
			}
		}
		return false;
	}
}
