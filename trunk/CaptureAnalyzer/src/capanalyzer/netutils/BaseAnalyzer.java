package capanalyzer.netutils;

import capanalyzer.netutils.build.FiveTuple;
import capanalyzer.netutils.build.IPPacketType;
import capanalyzer.netutils.build.TCPPacket;
import capanalyzer.netutils.files.CaptureFileBlock;

public class BaseAnalyzer implements IPacketAnalyzer
{
	FlowsDataStructure flowsDataStructure = FlowsDataStructure.getInstance();
	FlowsDataStructureForDB flowsDataStructureForDb = FlowsDataStructureForDB.getInstance();

	public void processPacket(CaptureFileBlock theFullPacket)
	{
		try
		{
			FiveTuple flowTuple = new FiveTuple(theFullPacket.getMyData(), false);
			if (flowsDataStructure.ifFlowExist(flowTuple) == false)
			{
				flowsDataStructure.addNewFlow(flowTuple);
				flowsDataStructure.getFlowInfoStruct(flowTuple).setStartTime(theFullPacket.getMyPktHdr().getTime());
				flowsDataStructure.getFlowInfoStruct(flowTuple).setLastTime(theFullPacket.getMyPktHdr().getTime());		
				
				if(flowTuple.getMyType() == IPPacketType.TCP)
				{
					TCPPacket tcppkt = new TCPPacket(theFullPacket.getMyData());
					if(tcppkt.isSyn())
					{
						flowsDataStructure.getFlowInfoStruct(flowTuple).setTcpFullStart(true);
					}
				}
			}

			flowsDataStructure.getFlowInfoStruct(flowTuple).incrementNumberOfPackets();
			flowsDataStructure.getFlowInfoStruct(flowTuple).addIpg(theFullPacket.getMyPktHdr().getTime());
			flowsDataStructure.getFlowInfoStruct(flowTuple).addPacketSize(theFullPacket.getMyData().length);
			flowsDataStructure.getFlowInfoStruct(flowTuple).setLastTime(theFullPacket.getMyPktHdr().getTime());

		} catch (NetUtilsException e)
		{
			System.out.println("Got expeption while processing packet!!!");
		}
	}

	public void finalizeFlow(FiveTuple theFlowTuple)
	{
		FlowInfoStruct tempFlowDataStructure = flowsDataStructure.getFlowInfoStruct(theFlowTuple);

		flowsDataStructureForDb.addNewFlow(theFlowTuple);
		FlowInfoStructForDB tempFlowDataStructureForDB = flowsDataStructureForDb.getFlowInfoForDbStruct(theFlowTuple);
		tempFlowDataStructureForDB.addLongResult("flow_id", tempFlowDataStructure.getFlowId());
		tempFlowDataStructureForDB.addLongResult("source_ip", tempFlowDataStructure.getSourceIp());
		tempFlowDataStructureForDB.addIntegerResult("source_port", tempFlowDataStructure.getSourcePort());
		tempFlowDataStructureForDB.addLongResult("destination_ip", tempFlowDataStructure.getDestinationIp());
		tempFlowDataStructureForDB.addIntegerResult("destination_port", tempFlowDataStructure.getDestinationPort());
		tempFlowDataStructureForDB.addIntegerResult("flow_type", tempFlowDataStructure.getFlowType());
		tempFlowDataStructureForDB.addIntegerResult("is_tcp_full", tempFlowDataStructure.isTcpFullStart()?1:0);
		
		tempFlowDataStructureForDB.addLongResult("start_time", tempFlowDataStructure.getStartTime());
		tempFlowDataStructureForDB.addLongResult("duration", Math.abs((tempFlowDataStructure.getLastTime() - tempFlowDataStructure.getStartTime())));
		tempFlowDataStructureForDB.addLongResult("number_of_packets", tempFlowDataStructure.getNumberOfPackets());

		tempFlowDataStructureForDB.addLongResult("size", tempFlowDataStructure.getTotalPacketSizes());
		tempFlowDataStructureForDB.addIntegerResult("min_packet_size", tempFlowDataStructure.getMinPacketSize());
		tempFlowDataStructureForDB.addIntegerResult("average_packet_size", (int) (tempFlowDataStructure.getTotalPacketSizes() / tempFlowDataStructure.getNumberOfPackets()));
		tempFlowDataStructureForDB.addIntegerResult("max_packet_size", tempFlowDataStructure.getMaxPacketSize());

		tempFlowDataStructureForDB.addLongResult("min_ipg", (tempFlowDataStructure.getNumberOfPackets() > 1) ? tempFlowDataStructure.getMinIpg() : 0);
		tempFlowDataStructureForDB.addLongResult("average_ipg", (tempFlowDataStructure.getNumberOfPackets() > 1) ? (tempFlowDataStructure.getTotalIpg() / (tempFlowDataStructure.getNumberOfPackets() - 1)) : 0);
		tempFlowDataStructureForDB.addLongResult("max_ipg", (tempFlowDataStructure.getNumberOfPackets() > 1) ? tempFlowDataStructure.getMaxIpg() : 0);

		flowsDataStructure.removeFlow(theFlowTuple);
		tempFlowDataStructureForDB = null;
		tempFlowDataStructure = null;
	}
}
