package capanalyzer.netutils.patternFinder;

/**
 * result daya strcut for search packet size in packet.
 * @author rbaryana
 *
 */
@SuppressWarnings({ "rawtypes" })
public class CapPktSizeInPktResult implements Comparable
{
	private int myIdx = 0;
    //private boolean isDynamic = false;
	private int myPktNum =0;
	private int myNumOfTimes = 0;
	private int myTotalFlows = 0;
	/**
	 * @param theDynamic
	 * @param theString
	 * @param theIdx
	 */
	public CapPktSizeInPktResult(int thePktNum,boolean theDynamic, int theIdx,int totalflows)
	{
		//isDynamic = theDynamic;
	    myIdx = theIdx;
		myPktNum = thePktNum;
		myNumOfTimes++;
		myTotalFlows = totalflows;
	}
	
	public int compareTo(Object theArg0)
	{
		CapPktSizeInPktResult rs = (CapPktSizeInPktResult) theArg0;
		return rs.myNumOfTimes - this.myNumOfTimes;
	}
	
	public void incNumOfTimes()
	{
		myNumOfTimes++;
	}
	
	
	public String toString()
	{
		return "Pkt : "+myPktNum+" , Idx : "+myIdx+" , num = "+myNumOfTimes+" , "+((myNumOfTimes*100)/myTotalFlows)+"% (total flows checked = "+myTotalFlows+")";
	}
}
