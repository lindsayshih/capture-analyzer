package capanalyzer.netutils.files.erf;


/**
 * class for holding complete erf block.<br>
 * Erf block is built of header and data.<br>
 * 
 * @author roni bar yanai
 *
 */
public class ErfBlock
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

	/**
	 * 
	 * @return the packet ErfPacketHeader
	 */
	public ErfPacketHeader getMyPktHdr()
	{
		return myPktHdr;
	}

	/**
	 * 
	 * @return the packet as byte array.
	 */
	public byte[] getMyData()
	{
		return myData;
	}
}
