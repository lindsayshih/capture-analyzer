package capanalyzer.netutils.examples;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

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
	public static void main(String[] args) throws IOException, NetUtilsException
	{
	/*	ErfPacketHeader[] temp = ErfFileReader.getPktsHeaders("c:\\capture_012_15_06_2009.erf");
		for (int i = 0; i < 20; i++)
		{
			System.out.println(temp[i]);
		}
		*/
		
		HashSet<String> toupleHash = new HashSet<String>();
		long numOfTcpPackets=0;
		long numOfUdpPackets=0;
		long numOfNonTcpUdpPackets=0;
		
		long numOfTcpFlows=0;
		long numOfUdpFlows=0;
		long numOfNonTcpUdpFlows=0;
		
		CaptureFileReader frd = CaptureFileFactory.createCaptureFileReader("c:\\capture_012_15_06_2009.erf");
		CaptureFileBlock nextblock = null;
		
		long start = System.currentTimeMillis();
		try
		{		
			while ((nextblock = frd.readNextBlock()) != null)
			{
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
									
					if (toupleHash.contains(flowTouple.getKey()))
						continue;
					else
					{
						toupleHash.add(flowTouple.getKey());
						//System.out.println(flowTouple.toString());
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
						
						System.out.println("Percentage Done: " + frd.getBytesRead()/(float)frd.getCapFileSizeInBytes());
					}
				}
			}
		} catch (Exception e)
		{
			System.out.println("Exception Caught");
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
