package capanalyzer.netutils.examples;

import java.io.IOException;

import capanalyzer.netutils.build.EthernetPacket;
import capanalyzer.netutils.build.IPPacket;
import capanalyzer.netutils.build.IPPacketType;
import capanalyzer.netutils.build.TCPPacket;
import capanalyzer.netutils.files.pcap.PCapBlock;
import capanalyzer.netutils.files.pcap.PCapFileReader;


public class ReadPCapFile
{
	public static void main(String[] args) throws IOException
	{
		PCapFileReader frd = new PCapFileReader("c:\\get200_2.cap");
		PCapBlock nextblock = null;
		while( (nextblock = frd.readNextBlock()) != null )
		{
			byte data [] = nextblock.getMyData();
			if(EthernetPacket.statIsIpPacket(data) && IPPacket.getIpProtocolType(data) == IPPacketType.TCP)
			{
				TCPPacket tcp = new TCPPacket(data);
				System.out.println(tcp.toString());
			}
		}
	}
}
