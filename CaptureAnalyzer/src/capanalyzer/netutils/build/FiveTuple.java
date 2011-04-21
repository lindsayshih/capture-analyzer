package capanalyzer.netutils.build;

import capanalyzer.netutils.NetUtilsException;
import capanalyzer.netutils.utils.IP;


/**
 * Five Tuple is built of (source ip,destination ip,source port,destination port
 *  ,protocol type).<br>
 * Five tuple uniquely identifies a flow between two hosts on the network on a 
 * certain time. The tuple is not ordered.<br>
 * 
 * Note: The five tuple can be used as a key in hash, equals and hash are implemented 
 *  to match two keys if they have matching pairs of ip,port regardless of the direction. 
 * 
 * @author roni bar-yanai
 *
 */
public class FiveTuple
{
	private final static int MAX_PORT = 0xffff;
	private final static long MAX_IP = 0xffffffffl;
	
	// five tuple parameters.
	protected long mySrcIp = 0;

	protected long myDstIp = 0;

	protected int mySrcPort = 0;

	protected int myDstPort = 0;

	protected int myType = 0;

	/**
	 * create tuple
	 * @param theSrcIp
	 * @param theSrcPort
	 * @param theDstIp
	 * @param theDstPort
	 * @param theType
	 * @throws NetUtilsException
	 */
	public FiveTuple(long theSrcIp, int theSrcPort, long theDstIp, int theDstPort, int theType) throws NetUtilsException
	{
		myDstIp = theDstIp;
		myDstPort = theDstPort;
		mySrcIp = theSrcIp;
		mySrcPort = theSrcPort;
		myType = theType;
		
		if (isValid() == false)
			throw new NetUtilsException("Got non valid tuple : "+this.toString());
				
	}

	/**
	 * Extract the tuple, in case of none TCP/UDP, but IP will
	 * set the ports to zero. In case of none IP packet will throw exception.
	 * 
	 * @param packet
	 * @throws NetUtilsException
	 */
	public FiveTuple(byte[] packet, boolean throwExceptionOnNonIpPackets) throws NetUtilsException
	{
		if (packet == null || packet.length == 0)
		{
			return;
		}
		
		if (EthernetPacket.statIsIpPacket(packet))
		{
			if (IPPacket.getIpProtocolType(packet) == IPPacketType.TCP)
			{
				TCPPacket tcppkt = new TCPPacket(packet);
				myType = IPPacketType.TCP;
				mySrcIp = tcppkt.getSourceIP();
				mySrcPort = tcppkt.getSourcePort();
				myDstIp = tcppkt.getDestinationIP();
				myDstPort = tcppkt.getDestinationPort();
			}
			else if (IPPacket.getIpProtocolType(packet) == IPPacketType.UDP)
			{
				UDPPacket udppckt = new UDPPacket(packet);
				myType = IPPacketType.UDP;
				mySrcIp = udppckt.getSourceIP();
				mySrcPort = udppckt.getSourcePort();
				myDstIp = udppckt.getDestinationIP();
				myDstPort = udppckt.getDestinationPort();
			}
			else
			{
				IPPacket pkt = new IPPacket(packet);
				myType = pkt.getIPProtocol();
				mySrcIp = pkt.getSourceIP();
				mySrcPort = 0;
				myDstIp =  pkt.getDestinationIP();
				myDstPort = 0;
			}
		}
		else
		{
			if(throwExceptionOnNonIpPackets)
				throw new NetUtilsException("Got non tcp | udp packet ");
		}
	}

	
	/**
	 * create five tuple from TCP packet
	 * @param thePkt
	 */
	public FiveTuple(TCPPacket thePkt)
	{
		myType = IPPacketType.TCP;
		mySrcIp = thePkt.getSourceIP();
		mySrcPort = thePkt.getSourcePort();
		myDstIp = thePkt.getDestinationIP();
		myDstPort = thePkt.getDestinationPort();
	}

	/*
	 *  (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object theArg0)
	{
		if (!(theArg0 instanceof FiveTuple))
		{
			return false;
		}
		FiveTuple tmp = (FiveTuple) theArg0;
		
		return (this.myDstIp == tmp.myDstIp && this.mySrcIp == tmp.mySrcIp && this.myDstPort == tmp.myDstPort && this.mySrcPort == tmp.mySrcPort && this.myType == tmp.myType) || isOpposite(tmp);
	}
	
	final int PRIME = 31;
	
	@Override
	public int hashCode() {
		int result = 1;
		if (mySrcIp < myDstIp)
		{
			result = (int) (result + mySrcIp);
			result = result + mySrcPort;
			result = (int) (result + myDstIp);
			result = result + myDstPort;
			result = result + myType;
			result = result * PRIME;
		} 
		else
		{
			result = (int) (result + myDstIp);
			result = result + myDstPort;
			result = (int) (result + mySrcIp);
			result = result + mySrcPort;
			result = result + myType;
			result = result * PRIME;
		}
		return result;
	}
	
	public long longHashCode() {
		long result = 1;
		if (mySrcIp < myDstIp)
		{
			result = result + mySrcIp;
			result = result + mySrcPort;
			result = result + myDstIp;
			result = result + myDstPort;
			result = result + myType;
			result = result * PRIME;
		} 
		else
		{
			result = result + myDstIp;
			result = result + myDstPort;
			result = result + mySrcIp;
			result = result + mySrcPort;
			result = result + myType;
			result = result * PRIME;
		}
		return result;
	}

	/**
	 * 
	 * @param theFiveTouple
	 * @return true if have the same ip and ports but in opposite directions.
	 */
	public boolean isOpposite(FiveTuple theFiveTouple)
	{
		return (this.myDstIp == theFiveTouple.mySrcIp && this.mySrcIp == theFiveTouple.myDstIp && this.myDstPort == theFiveTouple.mySrcPort && this.mySrcPort == theFiveTouple.myDstPort && this.myType == theFiveTouple.myType);
	}

	/**
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "Src ip : " + IP.getIPAsString(mySrcIp) + "\n" + "Src Port : " + mySrcPort + "\n" + "Dst ip : " + IP.getIPAsString(myDstIp) + "\n" + "Dst Port " + myDstPort + "\n" + "My type " + myType + "\n";
	}

	/**
	 * 
	 * @return the five tuple as a readable string.
	 */
	public String getAsReadbleString()
	{
		return "src_" + IP.getIPAsString(mySrcIp) + "." + mySrcPort + "dst_" + IP.getIPAsString(myDstIp) + "." + myDstPort;
	}

	/**
	 * five tuple key is a string of the ips,ports ordered by the bigger ip.
	 * this way both of flow direction will return the same key.
	 * @return five tuple key.
	 */
	public String getKey()
	{
		if (mySrcIp < myDstIp)
		{
			return mySrcIp + ":" + mySrcPort + ":" + myDstIp + ":" + myDstPort + ":" + myType;
		}
		return myDstIp + ":" + myDstPort + ":" + mySrcIp + ":" + mySrcPort + ":" + myType;
	}

	/**
	 * 
	 * @return unique key that will identifies packets between same clients.
	 * (only ips are the same, may be different ports and ip protocol)
	 */
	public String getIpsAsKey()
	{
		if (mySrcIp < myDstIp)
		{
			return mySrcIp + ":" + myDstIp;
		}
		return myDstIp + ":" + mySrcIp;
	}

	/**
	 * @return the flow initiator ip as string
	 */
	public String getMySrcIpAsString()
	{
		return IP.getIPAsString(mySrcIp);
	}

	/**
	 * @return the flow initiatie as string
	 */
	public String getMyDstIpAsString()
	{
		return IP.getIPAsString(myDstIp);
	}

	/**
	 * @return the flow initiate port
	 */
	public int getDstPort()
	{
		return myDstPort;
	}

	/**
	 * @return the flow initiator port
	 */
	public int getSrcPort()
	{
		return mySrcPort;
	}
	
	/**
	 * 
	 * @return true if five tuple contains valid ips and ports.
	 */
	public boolean isValid()
	{
		return ( IP.isValidIp(IP.getIPAsString(mySrcIp)) || IP.isValidIp(IP.getIPAsString(myDstIp))
		|| mySrcPort >= 0 || mySrcPort <= MAX_PORT || myDstPort >= 0 || myDstPort <= MAX_PORT);
			
	}

	/**
	 * 
	 * @return the ip protocol type (tcp/udp)
	 */
	public int getMyType()
	{
		return myType;
	}

	/**
	 * 
	 * @return source ip
	 */
	public long getMySrcIp()
	{
		return mySrcIp;
	}

	/**
	 * 
	 * @param mySrcIp
	 */
	public void setMySrcIp(long mySrcIp)
	{
		this.mySrcIp = mySrcIp;
	}

	/**
	 * 
	 * @return my destination ip
	 */
	public long getMyDstIp()
	{
		return myDstIp;
	}

	/**
	 * 
	 * @param theDstIp
	 */
	public void setMyDstIp(long theDstIp)
	{
		if (theDstIp < 0 || theDstIp > MAX_IP)
		{
			throw new IllegalArgumentException("Got illegal ip");
		}
		this.myDstIp = theDstIp;
	}

	/**
	 * 
	 * @return source port
	 */
	public int getMySrcPort()
	{
		return mySrcPort;
	}

	/**
	 * 
	 * @param theSrcPort
	 */
	public void setMySrcPort(int theSrcPort)
	{
		if (theSrcPort < 0 || theSrcPort > MAX_IP)
		{
			throw new IllegalArgumentException("Got illegal ip");
		}
		this.mySrcPort = theSrcPort;
	}

	/**
	 * 
	 * @return destination port
	 */
	public int getMyDstPort()
	{
		return myDstPort;
	}

	/**
	 * set the destination port 
	 * @param myDstPort
	 */
	public void setMyDstPort(int myDstPort)
	{
		this.myDstPort = myDstPort;
	}

	/**
	 * 
	 * @param myType
	 */
	public void setMyType(int myType)
	{
		this.myType = myType;
	}
	
}
