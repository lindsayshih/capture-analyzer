package capanalyzer.netutils;

import java.util.HashMap;
import java.util.Map;

import capanalyzer.netutils.build.FiveTuple;

public class FlowsDataStructureForDB
{
	private static FlowsDataStructureForDB instance = null;
	private Map<String, FlowInfoStructForDB> flowsForDbMap = null;
	
	private FlowsDataStructureForDB()
	{
		flowsForDbMap = new HashMap<String, FlowInfoStructForDB>();		
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
		flowsForDbMap.put(theFlowTuple.getKey(), new FlowInfoStructForDB());
	}
	
	public boolean ifFlowExist(FiveTuple theFlowTuple)
	{
		return flowsForDbMap.containsKey(theFlowTuple.getKey());
	}
	
	public FlowInfoStructForDB getFlowInfoForDbStruct(FiveTuple theFlowTuple)
	{
		return flowsForDbMap.get(theFlowTuple.getKey());
	}
	
	public void removeFlow(FiveTuple theFlowTuple)
	{
		flowsForDbMap.remove(theFlowTuple.getKey());
	}
	
	public int getNumberOfFlows()
	{
		return flowsForDbMap.size();
	}
}
