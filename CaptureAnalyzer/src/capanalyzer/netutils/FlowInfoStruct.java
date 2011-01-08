package capanalyzer.netutils;

import java.util.ArrayList;
import java.util.List;

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
	
	private List<Long> packetTimes;
	private List<Integer> packetSizes;

	public FlowInfoStruct(FiveTuple theFlowTuple)
	{
		flowId = totalNumberOfFlows;
		totalNumberOfFlows++;

		fiveTuple = theFlowTuple;
		
		sourceIp = theFlowTuple.getMySrcIp();
		destinationIp = theFlowTuple.getMyDstIp();
		sourcePort = theFlowTuple.getMySrcPort();
		destinationPort = theFlowTuple.getMyDstPort();

		packetTimes = new ArrayList<Long>();
		packetSizes = new ArrayList<Integer>();
	}

	public void addNewPacketTime(long thePacketTime)
	{
		packetTimes.add(thePacketTime);
	}

	public void addNewPacketSize(int thePacketSize)
	{
		packetSizes.add(thePacketSize);
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
	 * @return the packetTimes
	 */
	public List<Long> getPacketTimes()
	{
		return packetTimes;
	}

	/**
	 * @return the packetSizes
	 */
	public List<Integer> getPacketSizes()
	{
		return packetSizes;
	}
	
	/**
	 * @return the fiveTuple
	 */
	public FiveTuple getFiveTuple()
	{
		return fiveTuple;
	}

}
