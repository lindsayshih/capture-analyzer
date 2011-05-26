package capanalyzer.netutils.patternFinder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import capanalyzer.netutils.NetUtilsException;
import capanalyzer.netutils.utils.LineArgs;

/**
 * search for bytes postion repeats. (same bytes value on the same pkt\indx)
 * 
 * @author rbaryana
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class CapPktBytePostionPattern extends CapSearch
{
	public static final int DEFAULT_MAX_PKT_SEARCH = 10;
	public static final int DEFAULT_MAX_Bytes_SEARCH = 1400;

	protected int myMaxNoRetanmsstion = 1;

	public String[] getCapPktBytePostionPattern(String fileNameAndPath, int numOfFlows, int maxpkts) throws CapException, IOException, NetUtilsException
	{
		FlowStruct[] flows = Utils.getAllFlows(fileNameAndPath, numOfFlows);
		return findPktBytePostionPattern(flows, maxpkts, DEFAULT_MAX_Bytes_SEARCH);
	}

	/**
	 * search repeats of bytes at same place\same pkts.
	 * 
	 * @param files
	 * @param maxpkts
	 * @return results
	 * @throws CapException
	 * @throws IOException 
	 * @throws NetUtilsException 
	 */
	public String getCapPktBytePostionPatternAsString(File[] files, String args) throws CapException, NetUtilsException, IOException
	{
		LineArgs largs = new LineArgs();
		largs.addArg("-pnum", 1, "");
		largs.addArg("-mbytes", 1, "");
		largs.init(args);
		int maxpkts = largs.hasOption("-pnum") ? largs.getArgAsInt("-pnum") : DEFAULT_MAX_PKT_SEARCH;
		int maxBytes = largs.hasOption("-mbytes") ? largs.getArgAsInt("-mbytes") : DEFAULT_MAX_Bytes_SEARCH;
		FlowStruct flows[] = Utils.getAllFlows(files);
		flows = Utils.filterRetransmittionsFlows(flows, myMaxNoRetanmsstion);
		return Utils.sumString(findPktBytePostionPattern(flows, maxpkts, maxBytes));
	}

	/**
	 * 
	 * @param theDir
	 *            - the dir with the caps (each cap contais one flow)
	 * @param namePattern
	 *            - regular expression for filtering caps by their names
	 * @param maxpkts
	 *            - the max packets to search
	 * @param minlength
	 *            - the min bytes array pattern
	 * @param minprecent
	 *            - the min precent to report (of the flow that contains the
	 *            pattern)
	 * @return results
	 * @throws CapException
	 * @throws IOException 
	 * @throws NetUtilsException 
	 */
	public CapPktBytePostionPatternResult[] getCapPktBytePostionPattern(String theDir, String namePattern, int maxpkts, int minlength, int minprecent) throws CapException, NetUtilsException, IOException
	{
		File[] files = Utils.filter(theDir, namePattern);
		FlowStruct flows[] = Utils.getAllFlows(files);
		flows = Utils.filterRetransmittionsFlows(flows, maxpkts);
		return findPktBytePostionPattern(flows, maxpkts, minlength, minprecent, DEFAULT_MAX_Bytes_SEARCH);
	}

	/**
	 * 
	 * @param files
	 *            - the files to scan
	 * @param maxpkts
	 *            - the max packets to search
	 * @param minlength
	 *            - the min bytes array pattern
	 * @param minprecent
	 *            - the min precent to report (of the flow that contains the
	 *            pattern)
	 * @return
	 * @throws CapException
	 * @throws IOException 
	 * @throws NetUtilsException 
	 */
	public CapPktBytePostionPatternResult[] getCapPktBytePostionPattern(File[] files, int maxpkts, int minlength, int minprecent) throws CapException, NetUtilsException, IOException
	{
		FlowStruct flows[] = Utils.getAllFlows(files);
		flows = Utils.filterRetransmittionsFlows(flows, maxpkts);
		return findPktBytePostionPattern(flows, maxpkts, minlength, minprecent, DEFAULT_MAX_Bytes_SEARCH);
	}

	/**
	 * search for patten in cpas
	 * 
	 * @param theDir
	 * @param namePattern
	 * @param result
	 * @return the files that containing the pattern
	 * @throws CapException
	 * @throws IOException 
	 * @throws NetUtilsException 
	 */
	public File[] getAllFilesWith(String theDir, String namePattern, CapPktBytePostionPatternResult result) throws CapException, NetUtilsException, IOException
	{
		File[] files = Utils.filter(theDir, namePattern);
		FlowStruct flows[] = Utils.getAllFlows(files);
		flows = Utils.filterRetransmittionsFlows(flows, result.getPktNum());
		byte[] tmp = result.getByteArray();
		ArrayList toretrun = new ArrayList();
		for (int i = 0; i < flows.length; i++)
		{
			byte[] data = flows[i].getDataStartZero(result.getPktNum());
			if (data == null || data.length < result.getIdx() + tmp.length)
				continue;

			boolean flag = true;
			int idx = result.getIdx();
			for (int j = 0; j < tmp.length; j++)
			{
				if (tmp[j] != data[j + idx])
					flag = false;
			}
			if (flag)
			{
				toretrun.add(new File(flows[i].getMyFileName()));
			}
		}

		return (File[]) toretrun.toArray(new File[] {});
	}

	/**
	 * check all flows for pattern
	 * 
	 * @param flows
	 *            - the flows to search
	 * @param maxpkts
	 *            - the max packet to search
	 * @param maxbytes
	 *            - the max bytes to search
	 * @return matches as string array
	 */
	private String[] findPktBytePostionPattern(FlowStruct[] flows, int maxpkts, int maxbytes)
	{
		if (flows.length < myMinFlowsToCheck)
			return new String[] { "No pattern found, too few flows : " + flows.length };

		String[] toReturn = new String[maxpkts + 1];
		for (int i = 0; i <= maxpkts; i++)
		{
			String tmpresult = processPktNumResults(buildHash(flows, maxpkts, i, maxbytes), flows.length);
			if (tmpresult.equals(""))
			{
				toReturn[i] = "";
			} else
				toReturn[i] = "Number of Flows checked = " + flows.length + "\nPacket number " + i + " \n======================\n" + tmpresult;
		}

		return toReturn;
	}

	/**
	 * check all flows for pattern
	 * 
	 * @param flows
	 * @param maxpkts
	 * @param minsize
	 *            - the min pattern size
	 * @param minprecent
	 *            - min precent to display
	 * @param maxbytes
	 * @return the matched as string array
	 */
	private CapPktBytePostionPatternResult[] findPktBytePostionPattern(FlowStruct[] flows, int maxpkts, int minsize, int minprecent, int maxbytes)
	{
		if (flows.length < myMinFlowsToCheck)
			return new CapPktBytePostionPatternResult[] {};

		ArrayList toReturn = new ArrayList();
		for (int i = 0; i <= maxpkts; i++)
		{
			CapPktBytePostionPatternResult[] tmp = processPktNumResults(buildHash(flows, maxpkts, i, maxbytes), flows.length, minsize, minprecent, i);
			if (tmp != null)
			{
				for (int j = 0; j < tmp.length; j++)
				{
					toReturn.add(tmp[j]);
				}
			}
		}
		return (CapPktBytePostionPatternResult[]) toReturn.toArray(new CapPktBytePostionPatternResult[] {});
	}

	/**
	 * build hash array. each hash for idx in the pkt. the hash contains the
	 * byte as the key and number of appearance in the pkt.
	 * 
	 * @param flows
	 * @param maxpkts
	 * @param pkt
	 * @return the hash.
	 */
	private HashMap[] buildHash(FlowStruct[] flows, int maxpkts, int pkt, int maxbytes)
	{
		int maxpkt = getMaxDataSize(flows, pkt);
		HashMap[] bytehash = initializeHashArray(maxpkt);
		for (int j = 0; j < flows.length; j++)
		{
			byte[] data = flows[j].getDataStartZero(pkt);
			if (data == null)
				continue;

			for (int k = 0; k < data.length && k < maxbytes; k++)
			{
				Byte key = new Byte(data[k]);
				if (bytehash[k].containsKey(key))
				{
					Integer num = (Integer) bytehash[k].get(key);
					bytehash[k].put(key, new Integer(num.intValue() + 1));
				} else
				{
					bytehash[k].put(key, new Integer(1));
				}
			}
		}
		return bytehash;
	}

	/**
	 * search for specific postion.
	 * 
	 * @param array
	 * @param flowsNum
	 * @return
	 */
	private String processPktNumResults(HashMap[] array, int flowsNum)
	{
		String toReturn = "";
		for (int i = 0; i < array.length; i++)
		{
			if (isClearPattern(array[i], flowsNum))
			{
				toReturn = toReturn + "Idx :" + i + " " + getPattern(array[i], flowsNum) + "\n";
			}
		}
		return toReturn;
	}

	/**
	 * 
	 * @param array
	 * @param flowsNum
	 * @param minLength
	 * @param minprecent
	 * @param pktnum
	 * @return
	 */
	private CapPktBytePostionPatternResult[] processPktNumResults(HashMap[] array, int flowsNum, int minLength, int minprecent, int pktnum)
	{
		ArrayList lst = new ArrayList();

		int counter = 0;
		int idx = 0;
		for (int i = 0; i < array.length; i++)
		{
			if (isClearPattern(array[i], flowsNum, minprecent))
			{
				counter++;
			} else
			{
				if (counter - idx >= minLength)
				{
					byte[] tmp = new byte[counter - idx];
					int precent = 100;
					for (int j = 0; j < tmp.length; j++)
					{
						int min = getPatternPrecent(array[idx + j], flowsNum);
						tmp[j] = getPatternAsByte(array[idx + j], flowsNum);
						precent = (precent > min) ? min : precent;
					}
					CapPktBytePostionPatternResult result = new CapPktBytePostionPatternResult(tmp, idx, pktnum, precent);
					lst.add(result);
				}
				idx = i + 1;
				counter = i + 1;
			}
		}
		return (CapPktBytePostionPatternResult[]) lst.toArray(new CapPktBytePostionPatternResult[] {});
	}

	/**
	 * 
	 * @param hash
	 * @param numOfFlows
	 * @return
	 */
	private String getPattern(HashMap hash, int numOfFlows)
	{
		int max = 0;
		Byte key = null;
		for (Iterator ir = hash.keySet().iterator(); ir.hasNext();)
		{
			Byte currentkey = (Byte) ir.next();
			Integer num = (Integer) hash.get(currentkey);
			if (num.intValue() > max)
			{
				max = num.intValue();
				key = currentkey;
			}
		}
		int precent = (max * 100) / numOfFlows;
		return "byte : " + Integer.toHexString(0xff & key.byteValue()) + " " + precent + "%";
	}

	/**
	 * 
	 * @param hash
	 * @param numOfFlows
	 * @return
	 */
	private byte getPatternAsByte(HashMap hash, int numOfFlows)
	{
		int max = 0;
		Byte key = null;
		for (Iterator ir = hash.keySet().iterator(); ir.hasNext();)
		{
			Byte currentkey = (Byte) ir.next();
			Integer num = (Integer) hash.get(currentkey);
			if (num.intValue() > max)
			{
				max = num.intValue();
				key = currentkey;
			}
		}
		// int precent = (max*100)/numOfFlows;
		return key.byteValue();
	}

	/**
	 * 
	 * @param hash
	 * @param numOfFlows
	 * @return
	 */
	private int getPatternPrecent(HashMap hash, int numOfFlows)
	{
		int max = 0;
		for (Iterator ir = hash.values().iterator(); ir.hasNext();)
		{
			Integer num = (Integer) ir.next();
			if (num.intValue() > max)
				max = num.intValue();
		}
		return (int) (((double) max * 100) / (double) numOfFlows);
	}

	/**
	 * 
	 * @param hash
	 * @param numOfFlows
	 * @param minprecent
	 * @return
	 */
	private boolean isClearPattern(HashMap hash, int numOfFlows, double minprecent)
	{
		int max = 0;
		for (Iterator ir = hash.values().iterator(); ir.hasNext();)
		{
			Integer num = (Integer) ir.next();
			if (num.intValue() > max)
				max = num.intValue();
		}
		double precent = ((double) max * 100) / (double) numOfFlows;
		if (precent > minprecent)
			return true;
		return false;
	}

	/**
	 * @param hash
	 * @param numOfFlows
	 * @return true if there is a clear pattern.
	 */
	private boolean isClearPattern(HashMap hash, int numOfFlows)
	{
		return isClearPattern(hash, numOfFlows, (double) 100 / (double) myMinResultRatio);
	}

	/**
	 * build hashmap array.
	 * 
	 * @param size
	 * @return the hashmap array.
	 */
	private HashMap[] initializeHashArray(int size)
	{
		HashMap bytehash[] = new HashMap[size];
		for (int i = 0; i < bytehash.length; i++)
		{
			bytehash[i] = new HashMap();
		}
		return bytehash;
	}

	/**
	 * 
	 * @param flows
	 * @param pkt
	 *            - the number of data packet
	 * @return the longset data packet number in the flows
	 */
	private int getMaxDataSize(FlowStruct[] flows, int pkt)
	{
		int max = 0;
		for (int i = 0; i < flows.length; i++)
		{
			byte data[] = flows[i].getDataStartZero(pkt);
			if (data != null)
			{
				max = (max < data.length ? data.length : max);
			}
		}
		return max;
	}
	
	public static void main(String[] args) throws CapException, NetUtilsException, IOException
	{

		CapPktBytePostionPattern finder = new CapPktBytePostionPattern();

		String[] result = finder.getCapPktBytePostionPattern("e:/udp_1_packet_filter_Not_DHT.erf", 2300000, 1);
		
		for (int i = 0; i < result.length; i++)
		{
			System.out.println(result[i]);
		}

	}
}