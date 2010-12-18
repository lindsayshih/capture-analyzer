package capanalyzer.netutils.build;

/**
 * IP packet types constants.<br>
 * To be compared with the IP packet type field in the IP header.<br>
 * 
 * @author roni bar yanai
 *
 */
final public class IPPacketType
{
	final public static int UDP = 0x11;

	/**
	 * Internet Control Message Protocol. 
	 */
	final public static int ICMP = 1;

	/**
	 * Internet Group Management Protocol.
	 */
	final public static int IGMP = 2;

	/**
	 * Transmission Control Protocol. 
	 */
	final public static int TCP = 6;

	/**
	 * Raw IP packets. 
	 */
	final public static int RAW = 255;
}
