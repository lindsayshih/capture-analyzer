package capanalyzer.netutils.patternFinder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import capanalyzer.netutils.NetUtilsException;
import capanalyzer.netutils.utils.LineArgs;

/**
 * Searching bytes pattern on the same flow.
 * Bytes that appear in first pkt for example and in the third..
 * 
 * 
 * @author rbaryana
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class CapPktBytesSameFlowPattern extends CapSearch
{
	public static final int DEFAULT_MAX_PKT_NUM = 10;
	public static final int DEFAULT_CHUNK_SIZE = 4;
	public static final int DEFAULT_FUIRST_PKT_NUM = 0;
	
	protected int myNoRetransmittion = 1;

	/**
	 * return the results as array of string.
	 * @param theDir - the dir containing the cap files.
	 * @param namePattern - regular expression, only files that match the expressin will be checked.
	 * @param maxpkts - max pkts in the flow to check in
	 * @param chunhksize - the min bytes pattern size.
	 * @param firstpkt - the first pkt to look in
	 * @return results
	 * @throws CapException
	 * @throws IOException 
	 * @throws NetUtilsException 
	 */
	public String[] getCapPktBytesSameFlowPattern(String theDir, String namePattern, int maxpkts, int chunhksize, int firstpkt) throws CapException, NetUtilsException, IOException
	{
		File[] files = Utils.filter(theDir, namePattern);
		FlowStruct flows[] = Utils.getAllFlows(files);
		flows = Utils.filterRetransmittionsFlows(flows, myNoRetransmittion);
		return findCapPktBytesSameFlowPattern(flows, maxpkts, chunhksize, firstpkt);
	}
	
	
	/**
	 *  return the results as array of string.
	 * @param files - the files to check
	 * @param maxpkts - max pkts in the flow to check in
	 * @param chunhksize - the min bytes pattern size.
	 * @param firstpkt - the first pkt to look in
	 * @return
	 * @throws CapException
	 * @throws IOException 
	 * @throws NetUtilsException 
	 */
	public String[] getCapPktBytesSameFlowPattern(File[] files, int maxpkts, int chunhksize, int firstpkt) throws CapException, NetUtilsException, IOException
	{
     	FlowStruct flows[] = Utils.getAllFlows(files);
		flows = Utils.filterRetransmittionsFlows(flows, maxpkts);
		return findCapPktBytesSameFlowPattern(flows, maxpkts, chunhksize, firstpkt);
	}
	
	/**
	 *  return the results as array of string.
	 * @param files
	 * @param arg - the args (for scripts)
	 * @return single string
	 * @throws CapException
	 * @throws IOException 
	 * @throws NetUtilsException 
	 */
	public String getCapPktBytesSameFlowPatternAsString(File[] files, String arg) throws CapException, NetUtilsException, IOException
	{
		LineArgs largs = new LineArgs();
		largs.addArg("-pnum",1,"");
		largs.addArg("-csize",1,"");
		largs.addArg("-fpkt",1,"");
		largs.init(arg.trim());
     	FlowStruct flows[] = Utils.getAllFlows(files);
     	
     	int maxpkts = largs.hasOption("-pnum")?largs.getArgAsInt("-pnum"):DEFAULT_MAX_PKT_NUM;
     	int chunhksize = largs.hasOption("-csize")?largs.getArgAsInt("-csize"):DEFAULT_CHUNK_SIZE;
     	int firstpkt = largs.hasOption("-fpkt")?largs.getArgAsInt("-fpkt"):DEFAULT_FUIRST_PKT_NUM;
     	
		flows = Utils.filterRetransmittionsFlows(flows, maxpkts);
		return "Number of Flows checked = "+flows.length+"\n\n"+Utils.sumString(findCapPktBytesSameFlowPattern(flows, maxpkts, chunhksize, firstpkt));
	}

	private String[] findCapPktBytesSameFlowPattern(FlowStruct[] flows, int maxpkts, int chunkSize, int firstPkt)
	{
		ArrayList result = new ArrayList();
		for (int i = 0; i < flows.length; i++)
		{
	    	PossibleMatch[] results = searchFlow(flows[i], chunkSize, maxpkts, firstPkt);
			if (results != null) result.add(results);
		}
		return processResult(result,flows.length);
	}

	/**
	 * process results for all flows to one resuls.
	 * ( check how many possible match repeats in the other flows )
	 * @param result
	 * @param flows
	 * @return array of possible matches
	 */
	private String[] processResult(ArrayList result,int flows)
	{
		HashMap hash = new HashMap();
		for (int i = 0; i < result.size(); i++)
		{
			PossibleMatch[] tmp = (PossibleMatch[]) result.get(i);
			for (int j = 0; j < tmp.length; j++)
			{
				if (hash.containsKey(tmp[j].key()))
				{
					MaxMatch m = (MaxMatch) hash.get(tmp[j].key());
					m.num++;
				}
				else
				{
					MaxMatch m = new MaxMatch(tmp[j]);
					hash.put(tmp[j].key(), m);
				}
			}
		}
		return getTopResults(hash,flows);
	}

	/**
	 * search for all matching results.
	 * ( unmistakable pattern )
	 * @param hash
	 * @param flowsnum
	 * @return array of possible results
	 */
	private String[] getTopResults(HashMap hash,int flowsnum)
	{
		MaxMatch[] result = (MaxMatch[]) hash.values().toArray(new MaxMatch[] {});
		Arrays.sort(result);
		String toreturn[] = new String[result.length];
		int counter = 0;
		for (int i = 0; i < toreturn.length; i++)
		{
			if (result[i].num > (flowsnum / myMinResultRatio) )
			{
				int precent = (result[i].num*100)/flowsnum;
			toreturn[counter] = "Number of times : "+result[i].num+" "+precent+"%\n"+result[i].match.postion();
			counter++;
			}
		}
		String[] temp = new String[counter];
		System.arraycopy(toreturn,0,temp,0,temp.length);
		return temp;
	}

	
	/**
	 * search specific flow and return possible matches
	 * @param flow
	 * @param size - chunk size
	 * @param pkts - max pkt to look in 
	 * @param firstpkt
	 * @return 
	 */
	private PossibleMatch[] searchFlow(FlowStruct flow, int size, int pkts, int firstpkt)
	{
		ArrayList result = new ArrayList();
		PacketSide side = flow.getDataPktSideStartZero(firstpkt);

		// get the pkt that repeats
		byte data[] = flow.getDataStartZero(firstpkt);
		if (data == null) return null;
		
		
		for (int i = firstpkt+1; i <= pkts; i++)
		{
			byte data2[] = flow.getDataStartZero(i);

			if (data2 == null) continue;
			
			if (flow.getDataPktSideStartZero(i).equals(side)) // only if packet come for different side.
				continue;
	
			for (int j = 0; j < data.length - size; j++)
			{
				for (int k = 0; k < data2.length - size; k++)
				{
					boolean found = true;
					for (int h = 0; h < size; h++)
					{
						if (data[j + h] != data2[k + h])
						{
							found = false;
						}
					}
					if (found)
					{
						PossibleMatch match = new PossibleMatch();
						byte[] arr = new byte[size];
						System.arraycopy(data, j, arr, 0, arr.length);
						match.myByteArray = arr;
						match.firstpktNum = firstpkt;
						match.secondPktNum = i;
						match.firtstPktIdx = j;
						match.secondPktIdx = k;
						result.add(match);
						j = j + size;
						break;
					}
				}
			}
		}
		return (PossibleMatch[]) result.toArray(new PossibleMatch[] {});
	}
    
	/**
	 * internal data struct for holding best matches. 
	 * 
	 * @author rbaryana
	 */
	class MaxMatch implements Comparable
	{
		public MaxMatch(PossibleMatch theMatch)
		{
			match = theMatch;
			num = 1;
		}

		int num = 0;

		PossibleMatch match = null;

		public int compareTo(Object theArg0)
		{
			MaxMatch m = (MaxMatch) theArg0;
			return m.num - this.num;
		}
	}

	/**
	 * internal data struct for holding possible result.
	 * @author rbaryana
	 *
	 */
	class PossibleMatch implements Comparable
	{
		byte[] myByteArray = null;

		int firstpktNum = 0;
		int firtstPktIdx = 0;
		int secondPktNum = 0;
		int secondPktIdx = 0;

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		public String toString()
		{
			String toReturn = "first num : " + firstpktNum + " ,idx:" + firtstPktIdx + "\n" + "second num : " + secondPktNum + ", idx:" + secondPktIdx + "\n" + " bytes : ";

			for (int i = 0; i < myByteArray.length; i++)
			{
				toReturn = toReturn + Integer.toHexString(myByteArray[i] & 0xff) + " ";
			}

			toReturn = toReturn + "\n";
			return toReturn;
		}
		
		public String postion()
		{
			return "first num : " + firstpktNum + " ,idx:" + firtstPktIdx + "\n" + "second num : " + secondPktNum + ", idx:" + secondPktIdx + "\n" + "bytes array length: "+myByteArray.length;
		}
		
		public String key()
		{
			return "first num : " + firstpktNum + " ,idx:" + firtstPktIdx + "\n" + "second num : " + secondPktNum + ", idx:" + secondPktIdx + "\n" + "bytes length:"+myByteArray.length;
		}

		public int compareTo(Object theArg0)
		{
			PossibleMatch m = (PossibleMatch) theArg0;
			return this.myByteArray.length - m.myByteArray.length;
		}
	}

}
