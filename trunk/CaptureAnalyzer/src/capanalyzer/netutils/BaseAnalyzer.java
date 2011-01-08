package capanalyzer.netutils;

import java.util.ArrayList;
import java.util.List;

import capanalyzer.netutils.build.FiveTuple;
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
			}

			flowsDataStructure.getFlowInfoStruct(flowTuple).addNewPacketTime(theFullPacket.getMyPktHdr().getTime());
			flowsDataStructure.getFlowInfoStruct(flowTuple).addNewPacketSize(theFullPacket.getMyData().length);

		} catch (NetUtilsException e)
		{
			System.out.println("Got expeption while processing packet!!!");
		}
	}

	public void finalizeFlow(FiveTuple theFlowTuple)
	{
		flowsDataStructureForDb.addNewFlow(theFlowTuple);
		FlowInfoStruct tempFlowDataStructure = flowsDataStructure.getFlowInfoStruct(theFlowTuple);
		FlowInfoStructForDB tempFlowDataStructureForDB = flowsDataStructureForDb.getFlowInfoForDbStruct(theFlowTuple);
		tempFlowDataStructureForDB.addLongResult("flow_id", tempFlowDataStructure.getFlowId());
		tempFlowDataStructureForDB.addLongResult("source_ip", tempFlowDataStructure.getSourceIp());
		tempFlowDataStructureForDB.addIntegerResult("source_port", tempFlowDataStructure.getSourcePort());
		tempFlowDataStructureForDB.addLongResult("destination_ip", tempFlowDataStructure.getDestinationIp());
		tempFlowDataStructureForDB.addIntegerResult("destination_port", tempFlowDataStructure.getDestinationPort());
		tempFlowDataStructureForDB.addIntegerResult("flow_type", tempFlowDataStructure.getFlowType());
	
		tempFlowDataStructureForDB.addLongResult("start_time", tempFlowDataStructure.getPacketTimes().get(0));
		tempFlowDataStructureForDB.addLongResult("duration", (tempFlowDataStructure.getPacketTimes().get(tempFlowDataStructure.getPacketTimes().size()-1) - tempFlowDataStructure.getPacketTimes().get(0)));	
		tempFlowDataStructureForDB.addLongResult("number_of_packets", tempFlowDataStructure.getPacketTimes().size());
		
		MinMaxAvgSum<Integer> packetSizesMinMaxAvg = calculateIntMinMaxAvg(tempFlowDataStructure.getPacketSizes());
		tempFlowDataStructureForDB.addIntegerResult("size", packetSizesMinMaxAvg.getSum());
		tempFlowDataStructureForDB.addIntegerResult("min_packet_size", packetSizesMinMaxAvg.getMin());
		tempFlowDataStructureForDB.addIntegerResult("average_packet_size", packetSizesMinMaxAvg.getAverage());
		tempFlowDataStructureForDB.addIntegerResult("max_packet_size", packetSizesMinMaxAvg.getMax());
		
		MinMaxAvgSum<Long> ipgTimesMinMaxAvg = calculateLongMinMaxAvg(getListOfDiffs(tempFlowDataStructure.getPacketTimes()));
		tempFlowDataStructureForDB.addLongResult("min_ipg", ipgTimesMinMaxAvg.getMin());
		tempFlowDataStructureForDB.addLongResult("average_ipg", ipgTimesMinMaxAvg.getAverage());
		tempFlowDataStructureForDB.addLongResult("max_packet_ipg", ipgTimesMinMaxAvg.getMax());

		flowsDataStructure.removeFlow(theFlowTuple);
	}

	public List<Long> getListOfDiffs(List<Long> listOfValues)
	{
		List<Long> listOfDiffs = new ArrayList<Long>();
		
		if(listOfValues.size()>1)
		{
			for (int i = 1; i < listOfValues.size(); i++)
			{
				listOfDiffs.add((listOfValues.get(i) - listOfValues.get(i-1)));			
			}
		} else
		{
			listOfDiffs.add(0L);
		}
		
		return listOfDiffs;
	}
	
	public MinMaxAvgSum<Integer> calculateIntMinMaxAvg(List<Integer> listOfValues)
	{
		int min = listOfValues.get(0);
		int max = listOfValues.get(0);;
		int average = 0;
		int sum = 0;
		
		
		for (Integer value: listOfValues)
		{
			sum += value;
			
			if(value<min)
				min = value;
			
			if(value>max)
				max = value;
		}
		
		average = sum/listOfValues.size();
		
		return new MinMaxAvgSum<Integer>(min, max, average, sum);	
	}
	
	public MinMaxAvgSum<Long> calculateLongMinMaxAvg(List<Long> listOfValues)
	{
		long min = listOfValues.get(0);
		long max = listOfValues.get(0);;
		long average = 0;
		long sum = 0;
		
		
		for (Long value: listOfValues)
		{
			sum += value;
			
			if(value<min)
				min = value;
			
			if(value>max)
				max = value;
		}
		
		average = sum/listOfValues.size();
		
		return new MinMaxAvgSum<Long>(min, max, average, sum);	
	}
}
