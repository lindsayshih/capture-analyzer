package capanalyzer.netutils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import capanalyzer.GlobalConfig;
import capanalyzer.netutils.build.FiveTuple;

public class FlowsDataStructure
{
	private static FlowsDataStructure instance = null;
	private List<Map<String, FlowInfoStruct>> listOfTupleMaps = null;
	
	private int numberOfMapsToUse = GlobalConfig.CaptureFileReadParams.getNumberOfMaps();
	
	private FlowsDataStructure()
	{
		listOfTupleMaps = new ArrayList<Map<String, FlowInfoStruct>>();
		for (int i = 0; i < numberOfMapsToUse; i++)
		{
			listOfTupleMaps.add(new HashMap<String, FlowInfoStruct>());		
		}
	}

	public static FlowsDataStructure getInstance()
	{
		if (instance == null)
		{
			instance = new FlowsDataStructure();
		}
		return instance;
	}
	
	public void addNewFlow(FiveTuple theFlowTuple)
	{
		listOfTupleMaps.get(Math.abs(theFlowTuple.hashCode())%numberOfMapsToUse).put(theFlowTuple.getKey(), new FlowInfoStruct(theFlowTuple));
	}
	
	public boolean ifFlowExist(FiveTuple theFlowTuple)
	{
		return listOfTupleMaps.get(Math.abs(theFlowTuple.hashCode())%numberOfMapsToUse).containsKey(theFlowTuple.getKey());
	}
	
	public FlowInfoStruct getFlowInfoStruct(FiveTuple theFlowTuple)
	{
		return listOfTupleMaps.get(Math.abs(theFlowTuple.hashCode())%numberOfMapsToUse).get(theFlowTuple.getKey());
	}
	
	public void removeFlow(FiveTuple theFlowTuple)
	{
		listOfTupleMaps.get(Math.abs(theFlowTuple.hashCode())%numberOfMapsToUse).remove(theFlowTuple.getKey());
	}
	
	public List<FiveTuple> getAllFlowFiveTuples()
	{
		
		List<FiveTuple> allFlowFiveTuples = new ArrayList<FiveTuple>();
		
		for (int i = 0; i < numberOfMapsToUse; i++)
		{
			for (String key : listOfTupleMaps.get(i).keySet())
			{
				allFlowFiveTuples.add(listOfTupleMaps.get(i).get(key).getFiveTuple());
			}
		}
		
		return allFlowFiveTuples;
	}
	
	public List<FiveTuple> getAllFlowsThatShouldAgeFiveTuples(long theLastPacketTime, long theAgingTime)
	{
		
		List<FiveTuple> allFlowThatShouldAgeFiveTuples = new ArrayList<FiveTuple>();
		
		for (int i = 0; i < numberOfMapsToUse; i++)
		{
			for (String key : listOfTupleMaps.get(i).keySet())
			{
				if(listOfTupleMaps.get(i).get(key).getLastTime() < theLastPacketTime-theAgingTime)
				{
					allFlowThatShouldAgeFiveTuples.add(listOfTupleMaps.get(i).get(key).getFiveTuple());
				}
			}
		}
		
		return allFlowThatShouldAgeFiveTuples;
	}
	
	public long[] getSizesOfAllMaps()
	{
		long[] numOfTuplesInMaps = new long[listOfTupleMaps.size()];
		
		for (int i = 0; i < listOfTupleMaps.size(); i++)
		{
			numOfTuplesInMaps[i] = listOfTupleMaps.get(i).size();
			
		}
		
		return numOfTuplesInMaps;
	}
}
