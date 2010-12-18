package capanalyzer.netutils.files.pcap;

import capanalyzer.netutils.files.CaptureFileBlock;
import capanalyzer.netutils.files.CaptureFilePacketHeader;


/**
 * class for holding complete libpcap block.<br>
 * Libpcap block is built of header and data.<br>
 * 
 * @author roni bar yanai
 *
 */
public class PCapBlock implements CaptureFileBlock
{
	
	private PCapPacketHeader myPktHdr = null;
	private byte[] myData = null;
	
	/**
	 * Build Block.
	 * @param pktHdr
	 * @param data
	 */
	public PCapBlock(PCapPacketHeader pktHdr, byte[] data)
	{
		super();
		this.myPktHdr = pktHdr;
		this.myData = data;
	}

	/* (non-Javadoc)
	 * @see capanalyzer.netutils.files.pcap.CaptureFileBlock#getMyPktHdr()
	 */
	public CaptureFilePacketHeader getMyPktHdr()
	{
		return myPktHdr;
	}

	/* (non-Javadoc)
	 * @see capanalyzer.netutils.files.pcap.CaptureFileBlock#getMyData()
	 */
	public byte[] getMyData()
	{
		return myData;
	}
}
