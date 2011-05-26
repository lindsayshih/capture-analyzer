package capanalyzer.netutils;

import java.util.HashMap;
import java.util.Map;

public class FlowInfoStructForDB
{
	private Map<String, Integer> integerMap = null;
	private Map<String, Long> longMap = null;
	private Map<String, String> stringMap = null;
	private Map<String, byte[]> byteArrayMap = null;
	
	public FlowInfoStructForDB()
	{
	}

	public void addIntegerResult(String theResultName, int theResultValue)
	{
		if(integerMap == null)
		{
			integerMap = new HashMap<String, Integer>();
		}
		
		integerMap.put(theResultName, theResultValue);
	}
	
	public void addLongResult(String theResultName, long theResultValue)
	{
		if(longMap == null)
		{
			longMap = new HashMap<String, Long>();
		}
		
		longMap.put(theResultName, theResultValue);
	}
	
	public void addStringResult(String theResultName, String theResultValue)
	{
		if(stringMap == null)
		{
			stringMap = new HashMap<String, String>();
		}
		
		stringMap.put(theResultName, theResultValue);
	}
	
	public void addByteArrayResult(String theResultName, byte[] theResultValue)
	{
		if(byteArrayMap == null)
		{
			byteArrayMap = new HashMap<String, byte[]>();
		}
		
		byteArrayMap.put(theResultName, theResultValue);
	}
	
	/**
	 * @return the integerMap
	 */
	public Map<String, Integer> getIntegerMap()
	{
		return integerMap;
	}

	/**
	 * @return the longMap
	 */
	public Map<String, Long> getLongMap()
	{
		return longMap;
	}

	/**
	 * @return the stringMap
	 */
	public Map<String, String> getStringMap()
	{
		return stringMap;
	}
	
	/**
	 * @return the byteArrayMap
	 */
	public Map<String, byte[]> getByteArrayMap()
	{
		return byteArrayMap;
	}
	
}
