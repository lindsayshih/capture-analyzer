package capanalyzer.netutils.files;

import java.io.IOException;

import capanalyzer.netutils.NetUtilsException;
import capanalyzer.netutils.files.erf.ErfFileReader;
import capanalyzer.netutils.files.erf.ErfFileWriter;
import capanalyzer.netutils.files.pcap.PCapFileReader;
import capanalyzer.netutils.files.pcap.PCapFileWriter;


public class CaptureFileFactory
{
	public static CaptureFileReader createCaptureFileReader(String theFileName) throws IOException, NetUtilsException
	{
		if(theFileName.toLowerCase().endsWith("cap"))
		{
			return new PCapFileReader(theFileName);
		}
		else if(theFileName.toLowerCase().endsWith("erf"))
		{
			return new ErfFileReader(theFileName);
		}
		
		throw new NetUtilsException("Capture Format not supported");
	}
	
	public static CaptureFileWriter createCaptureFileWriter(String theFileName) throws IOException, NetUtilsException
	{
		if(theFileName.toLowerCase().endsWith("cap"))
		{
			return new PCapFileWriter(theFileName);
		}
		else if(theFileName.toLowerCase().endsWith("erf"))
		{
			return new ErfFileWriter(theFileName);
		}
		
		throw new NetUtilsException("Capture Format not supported");
	}
}
