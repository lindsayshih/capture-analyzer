package capanalyzer.netutils;

import capanalyzer.netutils.build.FiveTuple;
import capanalyzer.netutils.files.CaptureFileBlock;

public class BaseAnalyzer implements IPacketAnalyzer
{
	FlowsDataStructure flowsDataStructure = FlowsDataStructure.getInstance();
	FlowsDataStructureForDB flowsDataStructureForDb = FlowsDataStructureForDB.getInstance();

	public void processPacket(CaptureFileBlock theFullPacket, long thePacketOffset)
	{
		try
		{
			FiveTuple flowTuple = new FiveTuple(theFullPacket.getMyData(), false);
			if (flowsDataStructure.ifFlowExist(flowTuple) == false)
			{
				flowsDataStructure.addNewFlow(flowTuple);
				flowsDataStructure.getFlowInfoStruct(flowTuple).setStartTime(theFullPacket.getMyPktHdr().getTime());
				flowsDataStructure.getFlowInfoStruct(flowTuple).setLastTime(theFullPacket.getMyPktHdr().getTime());	
				flowsDataStructure.getFlowInfoStruct(flowTuple).setFirstPacketOffsetInCaptureFile(thePacketOffset);
				flowsDataStructure.getFlowInfoStruct(flowTuple).setFirstPacketData(theFullPacket.getMyData());
			}

			flowsDataStructure.getFlowInfoStruct(flowTuple).incrementNumberOfPackets(); //needs to be first
			flowsDataStructure.getFlowInfoStruct(flowTuple).updateTcpInitStat(theFullPacket); //needs to be after incrementNumberOfPackets and before addIpg
			flowsDataStructure.getFlowInfoStruct(flowTuple).addIpg(theFullPacket.getMyPktHdr().getTime());
			flowsDataStructure.getFlowInfoStruct(flowTuple).addPacketSize(theFullPacket.getMyData().length);
			flowsDataStructure.getFlowInfoStruct(flowTuple).setLastTime(theFullPacket.getMyPktHdr().getTime());
			
			if(flowsDataStructure.getFlowInfoStruct(flowTuple).getNumberOfPackets()==2)
				flowsDataStructure.getFlowInfoStruct(flowTuple).setSecondPacketData(theFullPacket.getMyData());
			else if(flowsDataStructure.getFlowInfoStruct(flowTuple).getNumberOfPackets()==3)
				flowsDataStructure.getFlowInfoStruct(flowTuple).setThirdPacketData(theFullPacket.getMyData());	
			else if(flowsDataStructure.getFlowInfoStruct(flowTuple).getNumberOfPackets()==4)
				flowsDataStructure.getFlowInfoStruct(flowTuple).setForthPacketData(theFullPacket.getMyData());	
			else if(flowsDataStructure.getFlowInfoStruct(flowTuple).getNumberOfPackets()==5)
				flowsDataStructure.getFlowInfoStruct(flowTuple).setFifthPacketData(theFullPacket.getMyData());

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
		tempFlowDataStructureForDB.addLongResult("dest_ip", tempFlowDataStructure.getDestinationIp());
		tempFlowDataStructureForDB.addIntegerResult("dest_port", tempFlowDataStructure.getDestinationPort());
		tempFlowDataStructureForDB.addIntegerResult("flow_type", tempFlowDataStructure.getFlowType());
		tempFlowDataStructureForDB.addIntegerResult("is_full_tcp", tempFlowDataStructure.isTcpFullStart()?1:0);
		
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

		tempFlowDataStructureForDB.addLongResult("tcp_init_min_ipg", (tempFlowDataStructure.getNumberOfPackets() > 1 && tempFlowDataStructure.isTcpFullStart()) ? tempFlowDataStructure.getTcpInitMinIpg() : 0);
		tempFlowDataStructureForDB.addLongResult("tcp_init_average_ipg", (tempFlowDataStructure.getNumberOfPackets() > 1 && tempFlowDataStructure.isTcpFullStart()) ? (tempFlowDataStructure.getTcpInitTotalIpg() / 3) : 0);
		tempFlowDataStructureForDB.addLongResult("tcp_init_max_ipg", (tempFlowDataStructure.getNumberOfPackets() > 1 && tempFlowDataStructure.isTcpFullStart()) ? tempFlowDataStructure.getTcpInitMaxIpg() : 0);
		
		tempFlowDataStructureForDB.addLongResult("flow_offset_in_cap", tempFlowDataStructure.getFirstPacketOffsetInCaptureFile());
		
		tempFlowDataStructureForDB.addByteArrayResult("first_packet_payload", tempFlowDataStructure.getFirstPacketData());
		tempFlowDataStructureForDB.addByteArrayResult("second_packet_payload", tempFlowDataStructure.getSecondPacketData());		
		tempFlowDataStructureForDB.addByteArrayResult("third_packet_payload", tempFlowDataStructure.getThirdPacketData());
		tempFlowDataStructureForDB.addByteArrayResult("forth_packet_payload", tempFlowDataStructure.getForthPacketData());
		tempFlowDataStructureForDB.addByteArrayResult("fifth_packet_payload", tempFlowDataStructure.getFifthPacketData());
		
		flowsDataStructure.removeFlow(theFlowTuple);
		tempFlowDataStructureForDB = null;
		tempFlowDataStructure = null;
	}
}
