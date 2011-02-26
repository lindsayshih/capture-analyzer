package capanalyzer.netutils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import capanalyzer.GlobalConfig;
import capanalyzer.netutils.build.FiveTuple;

public class FlowsDataStructureForDB
{
	private static FlowsDataStructureForDB instance = null;
	private List<Map<String, FlowInfoStructForDB>> listOfTupleMaps = null;
	
	private int numberOfMapsToUse = GlobalConfig.CaptureFileReadParams.getNumberOfDbMaps();
	
	private FlowsDataStructureForDB()
	{		
		listOfTupleMaps = new ArrayList<Map<String, FlowInfoStructForDB>>();
		for (int i = 0; i < numberOfMapsToUse; i++)
		{
			listOfTupleMaps.add(new HashMap<String, FlowInfoStructForDB>());		
		}
	}

	public static FlowsDataStructureForDB getInstance()
	{
		if (instance == null)
		{
			instance = new FlowsDataStructureForDB();
		}
		return instance;
	}
	
	public void addNewFlow(FiveTuple theFlowTuple)
	{
		listOfTupleMaps.get(Math.abs(theFlowTuple.hashCode())%numberOfMapsToUse).put(theFlowTuple.getKey(), new FlowInfoStructForDB());
	}
	
	public boolean ifFlowExist(FiveTuple theFlowTuple)
	{
		return listOfTupleMaps.get(Math.abs(theFlowTuple.hashCode())%numberOfMapsToUse).containsKey(theFlowTuple.getKey());
	}
	
	public FlowInfoStructForDB getFlowInfoForDbStruct(FiveTuple theFlowTuple)
	{
		return listOfTupleMaps.get(Math.abs(theFlowTuple.hashCode())%numberOfMapsToUse).get(theFlowTuple.getKey());
	}
	
	public void removeFlow(FiveTuple theFlowTuple)
	{
		listOfTupleMaps.get(Math.abs(theFlowTuple.hashCode())%numberOfMapsToUse).remove(theFlowTuple.getKey());
	}
	
	public long[] getNumberOfFlows()
	{
		long[] numOfTuplesInMaps = new long[listOfTupleMaps.size()];
		
		for (int i = 0; i < listOfTupleMaps.size(); i++)
		{
			numOfTuplesInMaps[i] = listOfTupleMaps.get(i).size();
			
		}
		
		return numOfTuplesInMaps;
	}
}
