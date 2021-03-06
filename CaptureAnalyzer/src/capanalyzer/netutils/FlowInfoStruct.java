package capanalyzer.netutils;

import capanalyzer.netutils.build.EthernetPacket;
import capanalyzer.netutils.build.FiveTuple;
import capanalyzer.netutils.build.IPPacket;
import capanalyzer.netutils.build.IPPacketType;
import capanalyzer.netutils.build.TCPPacket;
import capanalyzer.netutils.build.UDPPacket;
import capanalyzer.netutils.files.CaptureFileBlock;

public class FlowInfoStruct
{
	private static long totalNumberOfFlows;

	private long flowId;

	private long sourceIp;
	private long destinationIp;
	private int sourcePort;
	private int destinationPort;
	private int flowType;

	private FiveTuple fiveTuple;
	
	private long startTime;
	private long lastTime;
	
	private int minPacketSize = Integer.MAX_VALUE;
	private int maxPacketSize = 0;
	private long totalPacketSizes = 0;
	
	private long minIpg = Long.MAX_VALUE;
	private long maxIpg = 0;
	private long totalIpg = 0;
	
	private long tcpInitMinIpg = Long.MAX_VALUE;
	private long tcpInitMaxIpg = 0;
	private long tcpInitTotalIpg = 0;

	private long numberOfPackets = 0;
	
	private boolean isTcpFullStart = false;
	private boolean hadSyn = false;
	private boolean hadSynAck = false;
	private boolean hadAck = false;
	
	private long firstPacketOffsetInCaptureFile = 0;
	
	byte[] firstPacketData = null;
	byte[] secondPacketData = null;
	byte[] thirdPacketData = null;
	byte[] forthPacketData = null;
	byte[] fifthPacketData = null;

	public FlowInfoStruct(FiveTuple theFlowTuple)
	{
		flowId = totalNumberOfFlows;
		totalNumberOfFlows++;

		fiveTuple = theFlowTuple;
		
		sourceIp = theFlowTuple.getMySrcIp();
		destinationIp = theFlowTuple.getMyDstIp();
		sourcePort = theFlowTuple.getMySrcPort();
		destinationPort = theFlowTuple.getMyDstPort();
		
		flowType = theFlowTuple.getMyType();
	}

	/**
	 * @return the totalNumberOfFlows
	 */
	public static long getTotalNumberOfFlows()
	{
		return totalNumberOfFlows;
	}

	/**
	 * @return the flowId
	 */
	public long getFlowId()
	{
		return flowId;
	}

	/**
	 * @return the sourceIp
	 */
	public long getSourceIp()
	{
		return sourceIp;
	}

	/**
	 * @return the destinationIp
	 */
	public long getDestinationIp()
	{
		return destinationIp;
	}

	/**
	 * @return the sourcePort
	 */
	public int getSourcePort()
	{
		return sourcePort;
	}

	/**
	 * @return the destinationPort
	 */
	public int getDestinationPort()
	{
		return destinationPort;
	}

	/**
	 * @return the flowType
	 */
	public int getFlowType()
	{
		return flowType;
	}
	
	/**
	 * @return the fiveTuple
	 */
	public FiveTuple getFiveTuple()
	{
		return fiveTuple;
	}
	
	/**
	 * @return the startTime
	 */
	public long getStartTime()
	{
		return startTime;
	}

	/**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(long startTime)
	{
		this.startTime = startTime;
	}

	/**
	 * @return the lastTime
	 */
	public long getLastTime()
	{
		return lastTime;
	}

	/**
	 * @param lastTime the lastTime to set
	 */
	public void setLastTime(long lastTime)
	{
		this.lastTime = lastTime;
	}

	/**
	 * @return the minPacketSize
	 */
	public int getMinPacketSize()
	{
		return minPacketSize;
	}

	/**
	 * @return the maxPacketSize
	 */
	public int getMaxPacketSize()
	{
		return maxPacketSize;
	}

	/**
	 * @param packetSize the maxPacketSize/minPacketSize to set
	 */
	public void addPacketSize(int packetSize)
	{
		if(packetSize>this.maxPacketSize)
			this.maxPacketSize = packetSize;
			
		if(packetSize<this.minPacketSize)
			this.minPacketSize = packetSize;
		
		this.totalPacketSizes += packetSize;
	}

	/**
	 * @return the totalPacketSizes
	 */
	public long getTotalPacketSizes()
	{
		return totalPacketSizes;
	}

	/**
	 * @param currentTime
	 */
	public void addIpg(long currentTime)
	{	
		if(numberOfPackets>1)
		{
			long ipg = currentTime - this.lastTime;
			if(ipg<0)
				ipg = Math.abs(ipg);
			
			if(ipg>this.maxIpg)
				this.maxIpg = ipg;
				
			if(ipg<this.minIpg)
				this.minIpg = ipg;
			
			this.totalIpg += ipg;
			
			if((numberOfPackets == 2 && hadSyn && hadSynAck) || (numberOfPackets == 3 && hadSyn && hadSynAck && hadAck))
			{
				if(ipg>this.tcpInitMaxIpg)
					this.tcpInitMaxIpg = ipg;
					
				if(ipg<this.tcpInitMinIpg)
					this.tcpInitMinIpg = ipg;
				
				this.tcpInitTotalIpg += ipg;
			} 
		}
	}

	/**
	 * @return the minIpg
	 */
	public long getMinIpg()
	{
		return minIpg;
	}

	/**
	 * @return the maxIpg
	 */
	public long getMaxIpg()
	{
		return maxIpg;
	}
	
	/**
	 * @return the totalIpg
	 */
	public long getTotalIpg()
	{
		return totalIpg;
	}

	/**
	 * @return the tcpInitMinIpg
	 */
	public long getTcpInitMinIpg()
	{
		return tcpInitMinIpg;
	}

	/**
	 * @return the tcpInitMaxIpg
	 */
	public long getTcpInitMaxIpg()
	{
		return tcpInitMaxIpg;
	}

	/**
	 * @return the tcpInitTotalIpg
	 */
	public long getTcpInitTotalIpg()
	{
		return tcpInitTotalIpg;
	}
	
	/**
	 * @return the numberOfPackets
	 */
	public long getNumberOfPackets()
	{
		return numberOfPackets;
	}

	/**
	 * @param numberOfPackets the numberOfPackets to set
	 */
	public void incrementNumberOfPackets()
	{
		this.numberOfPackets++;;
	}
	
	/**
	 * Follow the tcp 3-way handshake and update status accordingly
	 * 
	 * @param theFullPacket
	 */
	public void updateTcpInitStat(CaptureFileBlock theFullPacket)
	{
		if (flowType == IPPacketType.TCP && numberOfPackets <= 3)
		{
			TCPPacket tcppkt = new TCPPacket(theFullPacket.getMyData());
			if (numberOfPackets == 1 && tcppkt.isSyn())
			{
				hadSyn = true;
			} else if (hadSyn && numberOfPackets == 2 && tcppkt.isSyn() && tcppkt.isAck())
			{
				hadSynAck = true;
			} else if (hadSynAck && numberOfPackets == 3 && tcppkt.isAck())
			{
				hadAck = true;
				isTcpFullStart = true;
			}
		}
	}
	
	/**
	 * @return the tcpFullStart
	 */
	public boolean isTcpFullStart()
	{
		return isTcpFullStart;
	}

	/**
	 * @param tcpFullStart the tcpFullStart to set
	 */
	public void setTcpFullStart(boolean isTcpFullStart)
	{
		this.isTcpFullStart = isTcpFullStart;
	}
	
	/**
	 * @return the firstPacketOffsetInCaptureFile
	 */
	public long getFirstPacketOffsetInCaptureFile()
	{
		return firstPacketOffsetInCaptureFile;
	}

	/**
	 * @param firstPacketOffsetInCaptureFile the firstPacketOffsetInCaptureFile to set
	 */
	public void setFirstPacketOffsetInCaptureFile(long firstPacketOffsetInCaptureFile)
	{
		this.firstPacketOffsetInCaptureFile = firstPacketOffsetInCaptureFile;
	}
	
	public byte[] getFirstPacketData()
	{
		return firstPacketData;
	}
	
	/**
	 * 
	 * @param packet
	 */
	public void setFirstPacketData(byte[] packet)
	{
		if (packet == null || packet.length == 0)
		{
			return;
		}
		
		try 
		{
			if (EthernetPacket.statIsIpPacket(packet))
			{
				if(IPPacket.isFragment(packet)==false)
				{
					if (IPPacket.getIpProtocolType(packet) == IPPacketType.TCP)
					{
						TCPPacket tcppkt = new TCPPacket(packet);
						if (tcppkt.isSyn()==true && tcppkt.isAck()==false)
						{
							firstPacketData = new byte[] {0x73, 0x79, 0x6e, 0x2e}; //syn.
						} 
						else if (tcppkt.isSyn()==true && tcppkt.isAck()==true)
						{
							firstPacketData = new byte[] {0x73, 0x79, 0x6e, 0x61, 0x63, 0x6b, 0x2e};//synack.
						} 
						else
						{
							firstPacketData = tcppkt.getTCPData();
						}
					}
					else if (IPPacket.getIpProtocolType(packet) == IPPacketType.UDP)
					{
						UDPPacket udppckt = new UDPPacket(packet);
						firstPacketData = udppckt.getUDPData();
					}
					else
					{
						IPPacket pkt = new IPPacket(packet);
						firstPacketData = pkt.getIPData();
					}
				}
				else
				{
					if(IPPacket.isFirstFragment(packet)==true)
						firstPacketData = new byte[] {0x66, 0x66, 0x72, 0x61, 0x67, 0x6d, 0x65, 0x6e, 0x74};//ffragment
					else
						firstPacketData = new byte[] {0x6d, 0x66, 0x72, 0x61, 0x67, 0x6d, 0x65, 0x6e, 0x74};//mfragment
				}
			}
		} catch (Exception e) {
			// Do Nothing
		}
	}
	
	public byte[] getSecondPacketData()
	{
		return secondPacketData;
	}
	
	/**
	 * 
	 * @param packet
	 */
	public void setSecondPacketData(byte[] packet)
	{
		if (packet == null || packet.length == 0)
		{
			return;
		}
		
		try 
		{
			if (EthernetPacket.statIsIpPacket(packet))
			{
				if(IPPacket.isFragment(packet)==false)
				{
					if (IPPacket.getIpProtocolType(packet) == IPPacketType.TCP)
					{
						TCPPacket tcppkt = new TCPPacket(packet);
						if (tcppkt.isSyn()==true && tcppkt.isAck()==false)
						{
							secondPacketData = new byte[] {0x73, 0x79, 0x6e, 0x2e};
						} 
						else if (tcppkt.isSyn()==true && tcppkt.isAck()==true)
						{
							secondPacketData = new byte[] {0x73, 0x79, 0x6e, 0x61, 0x63, 0x6b, 0x2e};
						} 
						else
						{
							secondPacketData = tcppkt.getTCPData();
						}
					}
					else if (IPPacket.getIpProtocolType(packet) == IPPacketType.UDP)
					{
						UDPPacket udppckt = new UDPPacket(packet);
						secondPacketData = udppckt.getUDPData();
					}
					else
					{
						IPPacket pkt = new IPPacket(packet);
						secondPacketData = pkt.getIPData();
					}
				}
				else
				{
					if(IPPacket.isFirstFragment(packet)==true)
						secondPacketData = new byte[] {0x66, 0x66, 0x72, 0x61, 0x67, 0x6d, 0x65, 0x6e, 0x74};
					else
						secondPacketData = new byte[] {0x6d, 0x66, 0x72, 0x61, 0x67, 0x6d, 0x65, 0x6e, 0x74};
				}
			}
		} catch (Exception e) {
			// Do Nothing
		}
	}
	
	public byte[] getThirdPacketData()
	{
		return thirdPacketData;
	}
	
	/**
	 * 
	 * @param packet
	 */
	public void setThirdPacketData(byte[] packet)
	{
		if (packet == null || packet.length == 0)
		{
			return;
		}
		
		try 
		{
			if (EthernetPacket.statIsIpPacket(packet))
			{
				if(IPPacket.isFragment(packet)==false)
				{
					if (IPPacket.getIpProtocolType(packet) == IPPacketType.TCP)
					{
						TCPPacket tcppkt = new TCPPacket(packet);
						if (tcppkt.isSyn()==true && tcppkt.isAck()==false)
						{
							thirdPacketData = new byte[] {0x73, 0x79, 0x6e, 0x2e};
						} 
						else if (tcppkt.isSyn()==true && tcppkt.isAck()==true)
						{
							thirdPacketData = new byte[] {0x73, 0x79, 0x6e, 0x61, 0x63, 0x6b, 0x2e};
						} 
						else
						{
							thirdPacketData = tcppkt.getTCPData();
						}
					}
					else if (IPPacket.getIpProtocolType(packet) == IPPacketType.UDP)
					{
						UDPPacket udppckt = new UDPPacket(packet);
						thirdPacketData = udppckt.getUDPData();
					}
					else
					{
						IPPacket pkt = new IPPacket(packet);
						thirdPacketData = pkt.getIPData();
					}
				}
				else
				{
					if(IPPacket.isFirstFragment(packet)==true)
						thirdPacketData = new byte[] {0x66, 0x66, 0x72, 0x61, 0x67, 0x6d, 0x65, 0x6e, 0x74};
					else
						thirdPacketData = new byte[] {0x6d, 0x66, 0x72, 0x61, 0x67, 0x6d, 0x65, 0x6e, 0x74};
				}
			}
		} catch (Exception e) {
			// Do Nothing
		}
	}
	
	public byte[] getForthPacketData()
	{
		return forthPacketData;
	}
	
	/**
	 * 
	 * @param packet
	 */
	public void setForthPacketData(byte[] packet)
	{
		if (packet == null || packet.length == 0)
		{
			return;
		}
		
		try 
		{
			if (EthernetPacket.statIsIpPacket(packet))
			{
				if(IPPacket.isFragment(packet)==false)
				{
					if (IPPacket.getIpProtocolType(packet) == IPPacketType.TCP)
					{
						TCPPacket tcppkt = new TCPPacket(packet);
						if (tcppkt.isSyn()==true && tcppkt.isAck()==false)
						{
							forthPacketData = new byte[] {0x73, 0x79, 0x6e, 0x2e};
						} 
						else if (tcppkt.isSyn()==true && tcppkt.isAck()==true)
						{
							forthPacketData = new byte[] {0x73, 0x79, 0x6e, 0x61, 0x63, 0x6b, 0x2e};
						} 
						else
						{
							forthPacketData = tcppkt.getTCPData();
						}
					}
					else if (IPPacket.getIpProtocolType(packet) == IPPacketType.UDP)
					{
						UDPPacket udppckt = new UDPPacket(packet);
						forthPacketData = udppckt.getUDPData();
					}
					else
					{
						IPPacket pkt = new IPPacket(packet);
						forthPacketData = pkt.getIPData();
					}
				}
				else
				{
					if(IPPacket.isFirstFragment(packet)==true)
						forthPacketData = new byte[] {0x66, 0x66, 0x72, 0x61, 0x67, 0x6d, 0x65, 0x6e, 0x74};
					else
						forthPacketData = new byte[] {0x6d, 0x66, 0x72, 0x61, 0x67, 0x6d, 0x65, 0x6e, 0x74};
				}
			}
		} catch (Exception e) {
			// Do Nothing
		}
	}
	
	public byte[] getFifthPacketData()
	{
		return fifthPacketData;
	}
	
	/**
	 * 
	 * @param packet
	 */
	public void setFifthPacketData(byte[] packet)
	{
		if (packet == null || packet.length == 0)
		{
			return;
		}
		
		try 
		{
			if (EthernetPacket.statIsIpPacket(packet))
			{
				if(IPPacket.isFragment(packet)==false)
				{
					if (IPPacket.getIpProtocolType(packet) == IPPacketType.TCP)
					{
						TCPPacket tcppkt = new TCPPacket(packet);
						if (tcppkt.isSyn()==true && tcppkt.isAck()==false)
						{
							fifthPacketData = new byte[] {0x73, 0x79, 0x6e, 0x2e};
						} 
						else if (tcppkt.isSyn()==true && tcppkt.isAck()==true)
						{
							fifthPacketData = new byte[] {0x73, 0x79, 0x6e, 0x61, 0x63, 0x6b, 0x2e};
						} 
						else
						{
							fifthPacketData = tcppkt.getTCPData();
						}
					}
					else if (IPPacket.getIpProtocolType(packet) == IPPacketType.UDP)
					{
						UDPPacket udppckt = new UDPPacket(packet);
						fifthPacketData = udppckt.getUDPData();
					}
					else
					{
						IPPacket pkt = new IPPacket(packet);
						fifthPacketData = pkt.getIPData();
					}
				}
				else
				{
					if(IPPacket.isFirstFragment(packet)==true)
						fifthPacketData = new byte[] {0x66, 0x66, 0x72, 0x61, 0x67, 0x6d, 0x65, 0x6e, 0x74};
					else
						fifthPacketData = new byte[] {0x6d, 0x66, 0x72, 0x61, 0x67, 0x6d, 0x65, 0x6e, 0x74};
				}
			}
		} catch (Exception e) {
			// Do Nothing
		}
	}

}
