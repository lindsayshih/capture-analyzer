package capanalyzer.netutils.files.erf;

import capanalyzer.netutils.files.CaptureFileBlock;
import capanalyzer.netutils.files.CaptureFilePacketHeader;


/**
 * class for holding complete erf block.<br>
 * Erf block is built of header and data.<br>
 * 
 * @author roni bar yanai
 *
 */
public class ErfBlock implements CaptureFileBlock
{
	
	private ErfPacketHeader myPktHdr = null;
	private byte[] myData = null;
	
	/**
	 * Build Block.
	 * @param pktHdr
	 * @param data
	 */
	public ErfBlock(ErfPacketHeader pktHdr, byte[] data)
	{
		super();
		this.myPktHdr = pktHdr;
		this.myData = data;
	}

	/* (non-Javadoc)
	 * @see capanalyzer.netutils.files.erf.CaptureFileBlock#getMyPktHdr()
	 */
	public CaptureFilePacketHeader getMyPktHdr()
	{
		return myPktHdr;
	}

	/* (non-Javadoc)
	 * @see capanalyzer.netutils.files.erf.CaptureFileBlock#getMyData()
	 */
	public byte[] getMyData()
	{
		return myData;
	}
}
