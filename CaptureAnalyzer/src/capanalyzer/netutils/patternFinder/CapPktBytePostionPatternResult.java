package capanalyzer.netutils.patternFinder;

public class CapPktBytePostionPatternResult
{
	private int myIdx = 0;
	private int myPktNum = 0;
	private int myPrecent = 0;
	private byte[] myByteArray = null;
	/**
	 * @param theArray
	 * @param theIdx
	 * @param theNum
	 */
	public CapPktBytePostionPatternResult(byte[] theArray, int theIdx,int pktnum,int precent)
	{
		myByteArray = theArray;
		myIdx = theIdx;
		myPktNum = pktnum;
		myPrecent = precent;
	}
	
	public String toString()
	{
		String str = "idx : "+myIdx+"\n"+"num : "+myPktNum+"\n"+"Precent "+myPrecent+"\n";
		for (int i = 0; i < myByteArray.length; i++)
		{
			str = str + Integer.toHexString(myByteArray[i] & 0xff)+" ";
		}
		return str;
	}

	public byte[] getByteArray()
	{
		return myByteArray;
	}

	public int getIdx()
	{
		return myIdx;
	}

	public int getPktNum()
	{
		return myPktNum;
	}

}
