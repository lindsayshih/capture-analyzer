package capanalyzer.netutils.examples;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import capanalyzer.netutils.NetUtilsException;
import capanalyzer.netutils.build.FiveTuple;
import capanalyzer.netutils.build.IPPacket;
import capanalyzer.netutils.build.IPPacketType;
import capanalyzer.netutils.files.CaptureFileBlock;
import capanalyzer.netutils.files.CaptureFileFactory;
import capanalyzer.netutils.files.CaptureFileReader;
//import capanalyzer.netutils.files.erf.ErfPacketHeader;

public class ReadErfFile
{
	static String erfFile = "e:\\capture_012_15_06_2009.erf";
	//static String erfFile = "c:\\capture_012_15_06_2009.erf";
	
	public static void main(String[] args) throws IOException, NetUtilsException
	{
	/*	ErfPacketHeader[] temp = ErfFileReader.getPktsHeaders("c:\\capture_012_15_06_2009.erf");
		for (int i = 0; i < 20; i++)
		{
			System.out.println(temp[i]);
		}
		*/
		
		List<Map<String, Long>> listOfTupleMaps = new ArrayList<Map<String, Long>>();
		for (int i = 0; i < 4; i++)
		{
			listOfTupleMaps.add(new HashMap<String, Long>());
			
		}
		//Map<String, Long> toupleHash = new HashMap<String, Long>();
		
		long numOfTcpPackets=0;
		long numOfUdpPackets=0;
		long numOfNonTcpUdpPackets=0;
		
		long numOfTcpFlows=0;
		long numOfUdpFlows=0;
		long numOfNonTcpUdpFlows=0;
		
		int agingTime = 300 * 1000000;
		
		CaptureFileReader frd = CaptureFileFactory.createCaptureFileReader(erfFile);
		CaptureFileBlock nextblock = null;
		long packetTime;
		
		long start = System.currentTimeMillis();
		try
		{		
			while ((nextblock = frd.readNextBlock()) != null)
			{
				packetTime = nextblock.getMyPktHdr().getTime();
				if (IPPacket.statIsIpPacket(nextblock.getMyData()))
				{
					FiveTuple flowTouple = new FiveTuple(nextblock.getMyData(), true);
					
					switch (flowTouple.getMyType())
					{
					case IPPacketType.TCP:
						numOfTcpPackets++;
						break;
					case IPPacketType.UDP:
						numOfUdpPackets++;
						break;
					default:
						numOfNonTcpUdpPackets++;
						break;
					}
					
					if(listOfTupleMaps.get(Math.abs(flowTouple.hashCode())%4).put(flowTouple.getKey(), packetTime)==null)
					{
						//System.out.println("packetTime: " + packetTime);
						
						switch (flowTouple.getMyType())
						{
						case IPPacketType.TCP:
							numOfTcpFlows++;
							break;
						case IPPacketType.UDP:
							numOfUdpFlows++;
							break;
						default:
							numOfNonTcpUdpFlows++;
							break;
						}
						
						if((numOfTcpFlows+numOfUdpFlows+numOfNonTcpUdpFlows)%2000==0)
							System.out.println("Percentage Done: " + frd.getBytesRead()/(float)frd.getCapFileSizeInBytes());
						
		/*				if((numOfTcpFlows+numOfUdpFlows+numOfNonTcpUdpFlows)%200000==0)
						{
							for (int i = 0; i < 4; i++)
							{							
								if(listOfTupleMaps.get(i).size()>500000)
								{
									System.out.println("Checking for flows to age in Map number " + i + ". Number of flows Before aging: " + listOfTupleMaps.get(i).size());	
									Iterator<String> it = listOfTupleMaps.get(i).keySet().iterator();
									while (it.hasNext())
									{
										String key = it.next();
										if (packetTime - listOfTupleMaps.get(i).get(key) > agingTime)
										{
											it.remove();
										}
									}	
									System.out.println("Number of flows After aging: " + listOfTupleMaps.get(i).size());
								}
							}
						}
			*/		}
				}
			}
		} catch (Exception e)
		{
			System.out.println("Exception Caught");
			e.printStackTrace();
		}
		
		long end = System.currentTimeMillis();
		System.out.println();
		System.out.println();
		System.out.println("Total Number of TCP Flows: " + numOfTcpFlows);
		System.out.println("Total Number of UDP Flows: " + numOfUdpFlows);
		System.out.println("Total Number of NON TCP/UDP Flows: " + numOfNonTcpUdpFlows);
		System.out.println();
		System.out.println("Total Number of TCP Packets: " + numOfTcpPackets);
		System.out.println("Total Number of UDP Packets: " + numOfUdpPackets);
		System.out.println("Total Number of NON TCP/UDP Packets: " + numOfNonTcpUdpPackets);		
		System.out.println();
		System.out.println("Time to annalize: " + ((end-start)/1000) + " seconds");
		System.out.println();
		System.out.println("Total Number Flows in 1T capture file: " + (1000000000000L*(numOfTcpFlows+numOfUdpFlows+numOfNonTcpUdpFlows))/(new File("c:\\capture_012_15_06_2009.erf")).length());
		System.out.println("Time to annalize 1T capture file: " + ((1000000000000L*((end-start)/1000))/(new File("c:\\capture_012_15_06_2009.erf")).length())/3600 + " hours");
	}
}
