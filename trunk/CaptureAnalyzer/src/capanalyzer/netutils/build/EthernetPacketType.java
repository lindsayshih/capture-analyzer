package capanalyzer.netutils.build;

/**
 * Enum for Ethernet packet types.<br>
 * 
 * @author roni bar-yanai
 *
 */
public enum EthernetPacketType
{
	/**
	 * ARP type
	 */
	ARP(0x806),
	
	/**
	 * IP type
	 */
	IP(0x800);
	
	EthernetPacketType(int code)
	{
		myCode = code;
	}
	
	private int myCode;
	
	
	/**
	 * @return the numeric value of the packet type.
	 */
	public int getCode()
	{
		return myCode;
	}
	
	
	/**
	 *@return the type as readable string 
	 */
	public String toString()
	{
		switch (myCode)
		{
		case ARP_CODE:
			return "ARP";
		case IP_CODE:
			return "IP";
		default:
			return "";
		}
	}
	
	/**
	 * ARP type constant val
	 */
	public static final int ARP_CODE = 0x806;
	
	/**
	 * IP type constant val
	 */
	public static final int IP_CODE = 0x800;
}
