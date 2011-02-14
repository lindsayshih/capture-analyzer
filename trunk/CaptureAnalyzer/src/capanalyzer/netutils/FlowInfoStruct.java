package capanalyzer.netutils;

import capanalyzer.netutils.build.FiveTuple;

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
	
	private long numberOfPackets = 0;

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
		}
	}

	/**
	 * @return the totalIpg
	 */
	public long getTotalIpg()
	{
		return totalIpg;
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

}
