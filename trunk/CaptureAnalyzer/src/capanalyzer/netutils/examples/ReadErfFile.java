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
		
		CaptureFileReader frd = CaptureFileFactory.createCaptureFileReader("c:\\capture_012_15_06_2009.erf");
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
