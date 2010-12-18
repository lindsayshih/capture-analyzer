package capanalyzer.netutils.examples;

import java.io.IOException;

import capanalyzer.netutils.NetUtilsException;
import capanalyzer.netutils.build.EthernetPacket;
import capanalyzer.netutils.build.IPPacket;
import capanalyzer.netutils.build.IPPacketType;
import capanalyzer.netutils.build.TCPPacket;
import capanalyzer.netutils.files.CaptureFileBlock;
import capanalyzer.netutils.files.CaptureFileFactory;
import capanalyzer.netutils.files.CaptureFileReader;


public class ReadPCapFile
{
	public static void main(String[] args) throws IOException, NetUtilsException
	{
		CaptureFileReader frd = CaptureFileFactory.createCaptureFileReader("c:\\get200_2.cap");
		CaptureFileBlock nextblock = null;
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
