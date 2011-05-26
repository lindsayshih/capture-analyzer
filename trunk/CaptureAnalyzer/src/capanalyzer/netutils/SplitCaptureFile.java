package capanalyzer.netutils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import capanalyzer.netutils.build.FiveTuple;
import capanalyzer.netutils.build.IPPacket;
import capanalyzer.netutils.files.CaptureFileBlock;
import capanalyzer.netutils.files.CaptureFileFactory;
import capanalyzer.netutils.files.CaptureFileReader;
import capanalyzer.netutils.files.erf.ErfFileWriter;
import capanalyzer.netutils.files.erf.ErfPacketHeader;

public class SplitCaptureFile
{
	static String inputErfFile = "E:\\Capture_Files\\capture_012_27_04_2011_Fixed.erf";
	
	static String outputEerfFile_1 = "c:\\Capture_Files\\capture_012_27_04_2011_Fixed_Part1.erf";
	
	static String outputEerfFile_2 = "c:\\Capture_Files\\capture_012_27_04_2011_Fixed_Part2.erf";
	
	static int numberOfSplits = 2;
	
	static int numberOfMapsToUsePerSplit = 4;


	public static void main(String[] args) throws IOException, NetUtilsException
	{
		CaptureFileReader frd = CaptureFileFactory.createCaptureFileReader(inputErfFile);
		
		ErfFileWriter fwrt1 = new ErfFileWriter(outputEerfFile_1);
		
		CaptureFileBlock nextblock = null;

		List<List<Map<Long, Character>>> filterMapList = new ArrayList<List<Map<Long, Character>>>();
		for (int i = 0; i < numberOfSplits; i++)
		{
			filterMapList.add(new ArrayList<Map<Long, Character>>());
			for (int j = 0; j < numberOfMapsToUsePerSplit; j++)
			{
				filterMapList.get(i).add(new HashMap<Long, Character>());
			}
		}
		
		try
		{		
			long numberOfPackets = 0;
			long numberOfFlows = 0;
			while ((nextblock = frd.readNextBlock()) != null)
			{
				if (IPPacket.statIsIpPacket(nextblock.getMyData()))
				{
					FiveTuple fiveT = new FiveTuple(nextblock.getMyData(), false);
					long key = fiveT.longHashCode();

					if (filterMapList.get(0).get((int) (key % numberOfMapsToUsePerSplit)).containsKey(key))
					{
						fwrt1.addPacket((ErfPacketHeader) nextblock.getMyPktHdr(), nextblock.getMyData());
					} 
					else
					{
						if(numberOfFlows%numberOfSplits==0)
						{
							filterMapList.get(0).get((int) (key % numberOfMapsToUsePerSplit)).put(key, '1');
							fwrt1.addPacket((ErfPacketHeader) nextblock.getMyPktHdr(), nextblock.getMyData());
						}
						else
						{
							filterMapList.get(1).get((int) (key % numberOfMapsToUsePerSplit)).put(key, '1');
						}
						numberOfFlows++;
					}
					
					numberOfPackets++;
					if (numberOfPackets % 1000000 == 0)
					{
						System.out.println("NumberOfPackets=" + numberOfPackets);
						System.out.println("NumberOfFlows=" + numberOfFlows);
						System.out.println("Percentage Done: " + frd.getBytesRead() / (float) frd.getCapFileSizeInBytes());
					}

				}
			}

			frd = null;
			fwrt1.close();
			filterMapList.get(0).clear();
			
			System.gc();
			
			frd = CaptureFileFactory.createCaptureFileReader(inputErfFile);
			ErfFileWriter fwrt2 = new ErfFileWriter(outputEerfFile_2);

			while ((nextblock = frd.readNextBlock()) != null)
			{
				if (IPPacket.statIsIpPacket(nextblock.getMyData()))
				{
					FiveTuple fiveT = new FiveTuple(nextblock.getMyData(), false);
					long key = fiveT.longHashCode();

					if(filterMapList.get(1).get((int) (key % numberOfMapsToUsePerSplit)).containsKey(key))
					{
						fwrt2.addPacket((ErfPacketHeader) nextblock.getMyPktHdr(), nextblock.getMyData());		
					} 
					
					if (numberOfPackets % 1000000 == 0)
					{
						System.out.println("NumberOfPackets=" + numberOfPackets);
						System.out.println("NumberOfFlows=" + numberOfFlows);
						System.out.println("Percentage Done: " + frd.getBytesRead() / (float) frd.getCapFileSizeInBytes());
					}

				}
			}
			
			System.out.println("Total Number of Packets = " + numberOfPackets);
			fwrt2.close();
			filterMapList.clear();

		} catch (Exception e)
		{
			System.out.println("Exception Caught");
			e.printStackTrace();
		}
	}
}
