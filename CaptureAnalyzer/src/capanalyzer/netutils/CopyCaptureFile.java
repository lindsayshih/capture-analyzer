package capanalyzer.netutils;

import java.io.IOException;
import capanalyzer.netutils.files.CaptureFileBlock;
import capanalyzer.netutils.files.CaptureFileFactory;
import capanalyzer.netutils.files.CaptureFileReader;
import capanalyzer.netutils.files.erf.ErfFileWriter;
import capanalyzer.netutils.files.erf.ErfPacketHeader;

public class CopyCaptureFile
{
	static String inputErfFile = "e:\\Capture_Files\\capture_012_27_04_2011.erf";
	
	static String outputEerfFile = "c:\\Capture_Files\\capture_012_27_04_2011_Fixed.erf";
	
	public static void main(String[] args) throws IOException, NetUtilsException
	{
		CaptureFileReader frd = CaptureFileFactory.createCaptureFileReader(inputErfFile);
		
		ErfFileWriter fwrt = new ErfFileWriter(outputEerfFile);
		
		CaptureFileBlock nextblock = null;
		
		try
		{	
			long numberOfPackets = 0;
			while ((nextblock = frd.readNextBlock()) != null)
			{
				if(numberOfPackets>234)
					fwrt.addPacket((ErfPacketHeader)nextblock.getMyPktHdr(), nextblock.getMyData());
				
				numberOfPackets++;
				
				if(numberOfPackets%100000==0)
				{
					System.out.println("NumberOfPackets=" + numberOfPackets);
					System.out.println("Percentage Done: " + frd.getBytesRead() / (float) frd.getCapFileSizeInBytes());
				}
			}
	
			System.out.println("Total Number of Packets = " + numberOfPackets);
			fwrt.close();

		} catch (Exception e)
		{
			System.out.println("Exception Caught");
			e.printStackTrace();
		}
	}
}
