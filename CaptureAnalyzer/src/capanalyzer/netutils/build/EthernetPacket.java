package capanalyzer.netutils.build;

import capanalyzer.netutils.NetUtilsException;

/**
 * The class implements Ethernet frame.<br>
 * building and manipulating frames, for example extracting 
 * the header src,dst address and the packet type ip/arp.. etc.
 * <br>
 * <br>
 * Supports only basic frames with 14 bytes header length.<br>
 * (Vlan not supported currently).<br>
 * 
 * @author roni bar-yanai
 *
 */
public class EthernetPacket
{
	/*
	 * Ethernet header structure: 
	 * 
	 * 0-6    : src mac
	 * 7 - 12 : dst mac
	 * 13 -14 : protocol type
	 * 
	 *  0                14
	 *  ------------------------------------------
	 * |  Ethernet hdr    |   pay load       
	 *  ------------------------------------------
	 *  
	 *  Some types if packets have different size, for example vlan frames.
	 *  Currently not supported. 
	 *  Some implementation notes. The class use lazy initialization of the
	 *  parameters, namely some of the parameters would be initialized only when
	 *  their value is required.
	 */
	// eth header constants.
	public static final int ETHERNET_HEADER_LENGTH = 14;
	private static final int ETHERNET_SRC_MAC_OFFSET = 0;
	private static final int ETHERNET_DST_MAC_OFFSET = 6;
	private static final int ETHERNET_MAC_LENGTH = 6;
	private static final int ETHERNET_TYPE_OFFSET = 12;
	private static final int ETHERNET_IP_PKT_TYPE = 0x800;
	
	/*
	 * holds the packet raw array
	 */
	protected byte[] myPacket = null;
	
	/*
	 * instance of bytes utils for fast running
	 */
	private ByteUtils myBytesUtils = null;
	
	/*
	 * holds the source mac as string
	 */
	private String mySrcMac = null;
	
	/*
	 * holds the destination mac as string
	 */
	private String myDstMac = null;
	
	/*
	 * will hold the mac as byte array.
	 */
	private byte[] mySrcMacAsByteArr = null;
	
	/*
	 * will hold the dst mac as byte array
	 */
	private byte[] myDstMacAsByteArr = null;
	
	
	protected boolean _isSniffedPkt = false;
	
	protected EthernetPacket()
	{
		_isReadSrcMac = true;
		_isReadDstMac = true;
	}
	
	/**
	 * create new Ethernet packet from the byte array.
	 *  
	 * @param thePacket - byte array which contains a valid Ethernet frame.
	 */
	public EthernetPacket(byte[] thePacket)
	{
		myPacket = thePacket;
		_isSniffedPkt = true;
	}
	
	boolean _isReadSrcMac = false;
	
	/**
	 * @return the eth src mac address as string.
	 */
	public String getSrcMAC()
	{
		if (_isReadSrcMac == false && _isSniffedPkt)
		{
			mySrcMac = getMyBytesUtils().getAsMac(myPacket,ETHERNET_SRC_MAC_OFFSET,ETHERNET_MAC_LENGTH);
			mySrcMacAsByteArr = ByteUtils.extractBytesArray(myPacket,ETHERNET_SRC_MAC_OFFSET,ETHERNET_SRC_MAC_OFFSET+ETHERNET_MAC_LENGTH);
		}
		return  mySrcMac;
	}
	
	/**
	 * @return the source mac address
	 */
	public byte[] getSrcMacByteArray()
	{
		if (_isReadSrcMac == false)
		{
			getSrcMAC();
		}
		return mySrcMacAsByteArr;
	}
	
	/**
	 * Set the src mac address.
	 * @param mac
	 */
	public void setSrcMacAddress(byte[] mac)
	{
		mySrcMacAsByteArr = mac;
		mySrcMac = getMyBytesUtils().getAsMac(mac,0,mac.length);
	}
	
	boolean _isReadDstMac = false;
	
	/**
	 * @return the destination mac address as string
	 */
	public String getDstMAC()
	{
		if ( _isReadDstMac == false )
		{
			_isReadDstMac = true;
			myDstMac = getMyBytesUtils().getAsMac(myPacket,ETHERNET_DST_MAC_OFFSET,ETHERNET_DST_MAC_OFFSET+ETHERNET_MAC_LENGTH);
			myDstMacAsByteArr = ByteUtils.extractBytesArray(myPacket,ETHERNET_DST_MAC_OFFSET,ETHERNET_DST_MAC_OFFSET+ETHERNET_MAC_LENGTH);
		}
		return myDstMac;
	}
	
	/**
	 * @return the destination mac address.
	 * may return null
	 */
	public byte[] getDstMacByteArray()
	{
		if (_isReadDstMac == false)
		{
			getDstMAC();
		}
		return myDstMacAsByteArr;
	}
	
	/**
	 * set the destination mac address.
	 * @param mac
	 */
	public void setDstMacAddress(byte[] mac)
	{
		myDstMacAsByteArr = mac;
		myDstMac = getMyBytesUtils().getAsMac(mac,0,mac.length);
	}
	
	/**
	 * @return the packet type field value
	 */
	public int getPacketType()
	{
		return ByteUtils.getByteNetOrderTo_uint16(myPacket,ETHERNET_TYPE_OFFSET);		
	}
	
	/**
	 * @return true if packet type is ip and false otherwise
	 */
	public boolean isIpPacket()
	{
		return  (getPacketType() == EthernetPacketType.IP_CODE);
	}
	
	/**
	 * @return true if packet type is arp and false otherwise
	 */
	public boolean isArpPacket()
	{
		return  (getPacketType() == EthernetPacketType.ARP_CODE);
	}
	
	/**
	 * @return the header offset
	 */
	protected int getHeaderOffset()
	{
		return ETHERNET_HEADER_LENGTH;
	}
	
	/**
	 * should be called only when packet sniffed.
	 * @return the packet raw data as bytes array.
	 * @throws NetUtilsException
	 */
	public byte[] getRawBytes() throws NetUtilsException
	{
		if(myPacket != null)
		{
			return myPacket;
		}
		throw new NetUtilsException("No packet was sniffed");
	}
	
	/**
	 * @return
	 */
    private ByteUtils getMyBytesUtils()
    {
    	if (myBytesUtils == null) {
			myBytesUtils = new ByteUtils();
		}
    	return myBytesUtils;
    }
    
    /**
     * @param thePacket
     * @return the packet src mac as String
     */
    public static String statGetSrcMAC(byte[] thePacket)
	{
		return  getMyBytesStatUtils().getAsMac(thePacket,ETHERNET_SRC_MAC_OFFSET,ETHERNET_MAC_LENGTH);
	}
	
    /**
     * @param thePkt
     * @return the eth packet dst mac as string
     */
	public static String statGetDstMAC(byte []thePkt)
	{
		return  getMyBytesStatUtils().getAsMac(thePkt,ETHERNET_DST_MAC_OFFSET,ETHERNET_DST_MAC_OFFSET+ETHERNET_MAC_LENGTH);
	}
	
	/**
	 * @param thePacket
	 * @return the packet type as int.
	 */
	public static int statGetPacketType(byte[] thePacket)
	{
		return ByteUtils.getByteNetOrderTo_uint16(thePacket,ETHERNET_TYPE_OFFSET);		
	}
	
	/**
	 *
	 * @param thePacket
	 * @return true if the eth packet is ip packet
	 */
	public static boolean statIsIpPacket(byte [] thePacket)
	{
		return  (statGetPacketType(thePacket) == ETHERNET_IP_PKT_TYPE);
	}
	
	
	private static ByteUtils MyBytesStatUtils = null;
	
	/**
	 * save the new on each call.
	 * @return 
	 */
    private static ByteUtils getMyBytesStatUtils()
    {
    	if (MyBytesStatUtils == null) {
    		MyBytesStatUtils = new ByteUtils();
		}
    	return MyBytesStatUtils;
    }
    
}
