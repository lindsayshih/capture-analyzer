package capanalyzer.netutils.files;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import capanalyzer.netutils.NetUtilsException;
import capanalyzer.netutils.build.BinaryTreeNode;
import capanalyzer.netutils.build.EthernetPacket;
import capanalyzer.netutils.build.FiveTuple;
import capanalyzer.netutils.build.FlowStruct;
import capanalyzer.netutils.build.IPPacket;
import capanalyzer.netutils.build.IPPacketType;
import capanalyzer.netutils.build.TCPPacket;
import capanalyzer.netutils.build.UDPPacket;
import capanalyzer.netutils.files.pcap.PCapFileException;
import capanalyzer.netutils.files.pcap.PCapFileHeader;
import capanalyzer.netutils.files.pcap.PCapFileReader;
import capanalyzer.netutils.utils.LineArgs;



/**
 * Class for splitting cap files into flows.
 *
 * 
 * @author roni bar yanai
 *
 */
public class CapFlowSpliter
{
	public static final int MAX_PKT_CATEGORY = 15;

	public static final int MIN_NUM_OF_WITH_NO_RETRANSMITION = 10;

	private int myMaxPacketsCategory = MAX_PKT_CATEGORY;

	private int myNumpktsWithNoRetransmittions = MIN_NUM_OF_WITH_NO_RETRANSMITION;

	// keeping the original header and use it for all caps created.
	PCapFileHeader myFilehdr = new PCapFileHeader();

	public CapFlowSpliter()
	{
	}

	/**
	 * return all flows as array list when each item contains all
	 * packetstruct belongs to that flow.
	 * @param theFileName
	 * @param onlyData
	 * @param onlyCompleteStart
	 * @return array list of all flows
	 * @throws IOException
	 * @throws NetUtilsException 
	 * @throws CapException
	 */
	private ArrayList<PacketStruct>[] getFlows(String theFileName, boolean onlyData, boolean onlyCompleteStart) throws IOException, NetUtilsException
	{
		// read all cap to raw data array
		byte[][] rawdata = CaptureFileUtils.readCapRawData(theFileName);
		
		myFilehdr = PCapFileReader.getPcapFileHeader(theFileName);
		CaptureFilePacketHeader[] pktHdrsArr = PCapFileReader.getPktsHeaders(theFileName);

		HashMap<String,ArrayList<PacketStruct>> hash = new HashMap<String,ArrayList<PacketStruct>>(); // will contain all flows by thier five touple as a key

		// run on all data (array of bytes[])
		for (int i = 0; i < rawdata.length; i++)
		{
			int type = IPPacket.getIpProtocolType(rawdata[i]);

			// if packet is not udp or tcp then ignore it 
			if (type != IPPacketType.TCP && type != IPPacketType.UDP) continue;
			
			// if not an ip then ignore.
			if (EthernetPacket.statIsIpPacket(rawdata[i]) == false)
				continue;
			
			try
			{
				// get pkt five touple
				FiveTuple ftouple = new FiveTuple(rawdata[i]);
				if (onlyData) // if only data then throw pkts that not contain data
				{
					if (type == IPPacketType.TCP)
					{
						TCPPacket pkt = new TCPPacket(rawdata[i]);
						if (pkt.getTCPData().length == 0) continue;
					}
					else if (type == IPPacketType.UDP)
					{
						UDPPacket pkt = new UDPPacket(rawdata[i]);
						if (pkt.getUDPData().length == 0) continue;
					}
				}

				// add the packet to the correct flow array list.
				if (hash.containsKey(ftouple.getKey()))
				{
					ArrayList<PacketStruct> tmp = hash.get(ftouple.getKey());
					tmp.add(new PacketStruct(pktHdrsArr[i], rawdata[i], type));
				}
				else
				{
					ArrayList<PacketStruct> tmp = new ArrayList<PacketStruct>();
					PacketStruct pktstt = new PacketStruct(pktHdrsArr[i], rawdata[i], type);
					pktstt.flowtype = type;
					tmp.add(pktstt);
					hash.put(ftouple.getKey(), tmp);
				}
			}
			catch (NetUtilsException e)
			{
				System.out.println("Couldn't find the five touple of pkt : " + i + ",file : " + theFileName);
			}
		}

		// build the arraylist to return 
		ArrayList[] toReturn = new ArrayList[hash.size()];

		int i = 0;
		for (Iterator<ArrayList<PacketStruct>> iter = hash.values().iterator(); iter.hasNext();)
		{
			ArrayList<PacketStruct> element = iter.next();
			toReturn[i] = element;
			i++;
		}
		return toReturn;
	}

	/**
	 * return all flows with the same ip.
	 * return all flows as array list when each item contains all
	 * packetstruct belongs to that flow.
	 * @param theFileName
	 * @param onlyData
	 * @return
	 * @throws IOException
	 * @throws ErfFileException 
	 * @throws CapException
	 */
	public ArrayList[] getFlowsWithSameIps(String theFileName, boolean onlyData) throws IOException, PCapFileException
	{
		// read all cap to raw data array
		byte[][] rawdata = PCapFileReader.readCapRawData(theFileName);
		myFilehdr = PCapFileReader.getPcapFileHeader(theFileName);
		CaptureFilePacketHeader[] pktHdrsArr = PCapFileReader.getPktsHeaders(theFileName);

		// will holds the flows where their five touple is the key.
		HashMap<String,ArrayList<PacketStruct>> hash = new HashMap<String,ArrayList<PacketStruct>>();

		for (int i = 0; i < rawdata.length; i++)
		{
			int type = IPPacket.getIpProtocolType(rawdata[i]);
			if (onlyData) // if only data then throw pkts that not contain data
			{

				if (type == IPPacketType.TCP)
				{
					TCPPacket pkt = new TCPPacket(rawdata[i]);
					if (pkt.getTCPData().length == 0) continue;
				}
				else if (type == IPPacketType.UDP)
				{
					UDPPacket pkt = new UDPPacket(rawdata[i]);
					if (pkt.getUDPData().length == 0) continue;
				}
			}
			try
			{
				//	 get pkt five touple
				FiveTuple ftouple = new FiveTuple(rawdata[i]);
				if (hash.containsKey(ftouple.getIpsAsKey()))
				{
					ArrayList<PacketStruct> tmp = hash.get(ftouple.getIpsAsKey());
					tmp.add(new PacketStruct(pktHdrsArr[i], rawdata[i], type));
				}
				else
				{
					ArrayList<PacketStruct> tmp = new ArrayList<PacketStruct>();
					tmp.add(new PacketStruct(pktHdrsArr[i], rawdata[i], type));
					hash.put(ftouple.getIpsAsKey(), tmp);
				}
			}
			catch (NetUtilsException e)
			{
				System.out.println("Couldn't find the five touple of pkt : " + i + ",file : " + theFileName);
			}
		}

		// build the arraylist to return 
		ArrayList<PacketStruct>[] toReturn = new ArrayList[hash.size()];

		int i = 0;
		for (Iterator<ArrayList<PacketStruct>> iter = hash.values().iterator(); iter.hasNext();)
		{
			ArrayList<PacketStruct> element = iter.next();
			toReturn[i++] = element;
		}
		return toReturn;
	}

	/**
	 * return all flows in the cap file as flowstruct array.
	 * @param fileName
	 * @param onlydata - will ignore flows with no data
	 * @param onlyCompleteStart - ignore tcp flows without full tcp handshake
	 * @return the flow array
	 * @throws IOException
	 * @throws NetUtilsException 
	 * @throws CapException
	 */
	public FlowStruct[] getFlowsAsPktDataFlowStruct(String fileName, boolean onlydata, boolean onlyCompleteStart) throws IOException, NetUtilsException
	{
		// get all flows
		ArrayList<PacketStruct>[] list = getFlows(fileName, onlydata, onlyCompleteStart);

		// build the array to retrurn
		FlowStruct[] toReturn = new FlowStruct[list.length];
		for (int i = 0; i < toReturn.length; i++)
		{
			byte[][] tmp = new byte[list[i].size()][];
			for (int j = 0; j < tmp.length; j++)
			{
				tmp[j] = ((PacketStruct) list[i].get(j)).rawData;
			}
			try
			{
				toReturn[i] = new FlowStruct(i, fileName, tmp);
			}
			catch (Exception e)
			{
				System.out.println("Faile to parse : " + fileName);
				toReturn[i] = new FlowStruct();
			}
		}
		return toReturn;
	}
	
	public FlowStruct[] getFlowsAsPktDataFlowStruct(String fileName) throws IOException, NetUtilsException
	{

		// get all flows
		ArrayList<PacketStruct>[] list = getFlows(fileName, false, false);

		// build the array to retrurn
		FlowStruct[] toReturn = new FlowStruct[list.length];
		for (int i = 0; i < toReturn.length; i++)
		{
			byte[][] tmp = new byte[list[i].size()][];
			for (int j = 0; j < tmp.length; j++)
			{
				tmp[j] = ((PacketStruct) list[i].get(j)).rawData;
			}
			try
			{
				toReturn[i] = new FlowStruct(i, fileName, tmp);
			}
			catch (Exception e)
			{
				System.out.println("Faile to parse : " + fileName);
				toReturn[i] = new FlowStruct();
			}
		}
		return toReturn;
	
	}

	/**
	 * split cap by the ip of the flows.
	 * will write all flows of the same ip into seprate cap file.
	 * @param theCap - the cap to sp;it
	 * @param theDir - the dir to write all new caps
	 * @param prefix - the prefix of the new files
	 * @param onlyData - ignore flows witout payload.
	 * @throws IOException
	 * @throws ErfFileException 
	 * @throws CapException
	 */
	public void splitByIPsCap(String theCap, String theDir, String prefix, boolean onlyData) throws IOException, PCapFileException
	{
		ArrayList<PacketStruct>[] flows = getFlowsWithSameIps(theCap, onlyData);
		splitCap(theCap, theDir, prefix, flows);

	}

	/**
	 * split the cap
	 * @param theCap
	 * @param theDir
	 * @param prefix
	 * @param flows
	 * @throws IOException
	 * @throws CapException
	 */
	private void splitCap(String theCap, String theDir, String prefix, ArrayList flows[]) throws IOException
	{

		prefix = (prefix == null) ? "tmp" : prefix;

		for (int i = 0; i < flows.length; i++)
		{
			String name = prefix + "_" + i + ".cap";
			//String txtname = prefix + "_" + i + ".txt";
			OutputStream out = null;
			try
			{
				out = new FileOutputStream(new File(theDir + "\\" + name));
				out.write(myFilehdr.getSourceByteArr());
				
				for (int j = 0; j < flows[i].size(); j++)
				{
					PacketStruct next = (PacketStruct) flows[i].get(j);
					out.write(next.pktHdr.getAsByteArray());
					out.write(next.rawData);
				}
			}
			finally
			{
				if (out != null) out.close();
			}

		}

	}

	/**
	 * for line commands
	 * @param theLine
	 * @throws IOException
	 * @throws CapException
	 */
	public File[] splitCap(String theLine,BinaryTreeNode theFlowFilterTree) throws IOException
	{
		System.out.println("Line : "+theLine);
		theLine=theLine.trim();
		LineArgs args = getLineArgs( theLine);
		if (!args.hasOption("-f") || !args.hasOption("-t"))
		{
			System.out.println("Missing Parameters file name or temp directory");
			return new File[]{};
		}
		String cap = args.getArgAsString("-f");
		String tmpDir = args.getArgAsString("-t");
		String prefix = args.hasOption("-prefix")?args.getArgAsString("-prefix"):"";
		
		boolean odata = args.hasOption("-odata")?true:false;
		boolean ochs = args.hasOption("-ochs")?true:false;
		boolean lretr = args.hasOption("-lretr");
		boolean iports = args.hasOption("-iports");
		boolean notxt = args.hasOption("-notxt");
		
		if (lretr)
		{
			myNumpktsWithNoRetransmittions = args.getArgAsInt("-lretr");
		}

		return splitCap(cap, tmpDir, prefix, odata, ochs, lretr, iports,theFlowFilterTree,!notxt);
	}
	
	/**
	 * for line commands
	 * @param theLine
	 * @throws IOException
	 * @throws CapException
	 */
	public File[] splitDir(String theLine,BinaryTreeNode theFlowFilterTree) throws IOException
	{
		System.out.println("Line : "+theLine);
		theLine=theLine.trim();
		LineArgs args = getLineArgs( theLine);
		if (!args.hasOption("-dir") || !args.hasOption("-t"))
		{
			System.out.println("Missing Parameters file name or temp directory");
			return new File[]{};
		}
		String dir = args.getArgAsString("-dir");
		String tmpDir = args.getArgAsString("-t");
		String prefix = args.hasOption("-prefix")?args.getArgAsString("-prefix"):"";
		String pattern = args.hasOption("-filter")?args.getArgAsString("-filter"):null;
		
		boolean odata = args.hasOption("-odata")?true:false;
		boolean ochs = args.hasOption("-ochs")?true:false;
		boolean lretr = args.hasOption("-lretr")?true:false;
		boolean iport = args.hasOption("-lports")?true:false;
		boolean notxt = args.hasOption("-notxt")?true:false;
		
		if (lretr)
		{
			myNumpktsWithNoRetransmittions = args.getArgAsInt("-lretr");
		}

		return splitDir(dir,pattern,tmpDir, prefix, odata, ochs, lretr, iport,theFlowFilterTree,!notxt);
	}

	/**
	 * split cap into flows where each cap will contain pne flow.
	 * the files names will be the prefix+counter_[Udp or Tcp]_[number of pkts].cap
	 * @param theCap - the cap to split
	 * @param theDir - the dir to create all new files.
	 * @param prefix 
	 * @param onlydata - will remove non data pkts.
	 * @throws IOException
	 * @throws CapException
	 */
	public File[] splitCap(String theCap, String theDir, String prefix, boolean onlydata, boolean onlyCompleteTCPHandshake, boolean noretansmittionsOnStart) throws IOException
	{
		return splitCap(theCap, theDir, prefix, onlydata, onlyCompleteTCPHandshake, noretansmittionsOnStart, false,null);
	}

	/**
	 * will split all caps in the src directoy that macth the pattern.
	 * @param theSrcDir
	 * @param thePattern
	 * @param theDir
	 * @param prefix
	 * @param onlydata
	 * @param onlyCompleteTCPHandshake
	 * @param noretansmittionsOnStart
	 * @param includePortsInName
	 * @param theFlowFilterTree
	 * @return all files splited.
	 * @throws IOException
	 * @throws CapException
	 */
	public File[] splitDir(String theSrcDir,String thePattern, String theDir, String prefix, boolean onlydata, boolean onlyCompleteTCPHandshake, boolean noretansmittionsOnStart, boolean includePortsInName,BinaryTreeNode theFlowFilterTree,boolean createtxt) throws IOException
	{
		File[] files = Utils.filter(theSrcDir,thePattern);
		ArrayList lst = new ArrayList();
		for (int i = 0; i < files.length; i++)
		{
			if (files[i].isDirectory() || !files[i].toString().endsWith("cap"))
				continue;
			
			File[] tmp = splitCap(files[i].toString(),theDir,prefix+"_"+i+"_",onlydata,onlyCompleteTCPHandshake,noretansmittionsOnStart,includePortsInName,theFlowFilterTree,createtxt);
			for (int j = 0; j < tmp.length; j++)
			{
				lst.add(tmp[j]);
			}
		}
		return (File[]) lst.toArray(new File[]{});
	}
	
	/**
	 * split cap into flows where each cap will contain pne flow.
	 * the files names will be the prefix+counter_[Udp or Tcp]_[number of pkts].cap
	 * @param theCap - the cap to split
	 * @param theDir - the dir to create all new files.
	 * @param prefix 
	 * @param onlydata - will remove non data pkts.
	 * @throws IOException
	 * @throws CapException
	 */
	public File[] splitCap(String theCap, String theDir, String prefix, boolean onlydata, boolean onlyCompleteTCPHandshake, boolean noretansmittionsOnStart, boolean includePortsInName,BinaryTreeNode theFlowFilterTree) throws IOException
	{
		return splitCap(theCap,theDir,prefix,onlydata,onlyCompleteTCPHandshake,noretansmittionsOnStart,includePortsInName,theFlowFilterTree,true);
		
	}
	/**
	 * split cap into flows where each cap will contain pne flow.
	 * the files names will be the prefix+counter_[Udp or Tcp]_[number of pkts].cap
	 * @param theCap - the cap to split
	 * @param theDir - the dir to create all new files.
	 * @param prefix 
	 * @param onlydata - will remove non data pkts.
	 * @throws IOException
	 * @throws CapException
	 */
	public File[] splitCap(String theCap, String theDir, String prefix, boolean onlydata, boolean onlyCompleteTCPHandshake, boolean noretansmittionsOnStart, boolean includePortsInName,BinaryTreeNode theFlowFilterTree,boolean createtxt) throws IOException
	{
	    ArrayList toReturn = new ArrayList();
		System.out.println("Spliting Cap : " + theCap);
		System.out.println("only data : " + onlydata);
		System.out.println("only complete tcp handshake : " + onlyCompleteTCPHandshake);
		System.out.println("only no retrnasmission at start : " + noretansmittionsOnStart);
		System.out.println("include ports in file name  : " + includePortsInName);
		System.out.println("creating txt files  : " + createtxt);

		prefix = (prefix == null) ? "tmp" : prefix;
		ArrayList[] flows = getFlows(theCap, onlydata, onlyCompleteTCPHandshake);

		System.out.println("Flows found : " + flows.length);
		int counter = 0;

		for (int i = 0; i < flows.length; i++)
		{
			System.out.print(".");
			if (i > 0 && i % 70 == 0) System.out.print("\n");
			String name = "";
			String tname = "";
			int numOfPkts = (flows[i].size());
			//System.out.println("Number of pkts == "+numOfPkts);
			if (((PacketStruct) flows[i].get(0)).flowtype == IPPacketType.TCP)
			{
				if (numOfPkts > myMaxPacketsCategory)
					name = prefix + i + "_" + "TCP" + "_Big";
				else
					name = prefix + i + "_" + "TCP" + "_" + flows[i].size();
			}
			else
			{
				if (numOfPkts > myMaxPacketsCategory)
					name = prefix + i + "_" + "UDP" + "_Big";
				else
					name = prefix + i + "_" + "UDP" + "_" + flows[i].size();
			}

			if (includePortsInName)
			{
				name = name + "_sp_" + getSrcPort(((PacketStruct) flows[i].get(0)).rawData);
				name = name + "_dp_" + getDstPort(((PacketStruct) flows[i].get(0)).rawData) + "_.cap";
			}
			else
				name = name + ".cap";

			tname = name.replace(".cap",".txt");
			
			if (onlyCompleteTCPHandshake || theFlowFilterTree != null || noretansmittionsOnStart)
			{
				try
				{
					byte[][] rawdata = new byte[flows[i].size()][];
					for (int j = 0; j < rawdata.length; j++)
					{
						rawdata[j] = ((PacketStruct) flows[i].get(j)).rawData;
					}
					FlowStruct struct = new FlowStruct(1, null, rawdata);
					if ( onlyCompleteTCPHandshake && !struct.isCompleteFlowStart()) continue;
					
					if (theFlowFilterTree != null && !FlowUtils.evalTree(theFlowFilterTree,struct))
						continue;
					
					if (noretansmittionsOnStart && struct.isTransmissions(myNumpktsWithNoRetransmittions))
						continue;
						
					
				}
				catch (Exception e)
				{
					continue;
				}
			}
			OutputStream out = null;
			FileStringWriter fw = null;
			try
			{
				ArrayList allpkts = new ArrayList();
				out = new FileOutputStream(new File(theDir + "\\" + name));
				out.write(myFilehdr.getSourceByteArr());
				
				for (int j = 0; j < flows[i].size(); j++)
				{
					PacketStruct next = (PacketStruct) flows[i].get(j);
					out.write(next.pktHdr.getAsByteArray());
					out.write(next.rawData);
					if (createtxt)
						allpkts.add(next.rawData);
				}
				toReturn.add(new File(theDir + "\\" + name));
				
				if (createtxt)
				{
					byte[][] raw = (byte[][]) allpkts.toArray(new byte[][]{});
					FlowStruct strct = new FlowStruct(1,name,raw);
					fw = new FileStringWriter(new File(theDir + "\\"+tname));
					fw.writeStringToFile(strct.getDataStreamASKiwiLikeText());
     			}
			}
			finally
			{
				counter++;
				if (out != null) out.close();
			}

		}

		System.out.println("\nDone. total files created : " + counter);
		return (File[]) toReturn.toArray(new File[]{});
	}

	/**
	 * @return get packet byte array and return its src port
	 */
	private int getSrcPort(byte[] data)
	{
		try
		{
			if (IPPacket.getIpProtocolType(data) == IPPacketType.TCP)
			{
				TCPPacket tcp = new TCPPacket(data);
				return tcp.getSourcePort();
			}
			else if (IPPacket.getIpProtocolType(data) == IPPacketType.UDP)
			{
				UDPPacket udp = new UDPPacket(data);
				return udp.getSourcePort();
			}
			else
				return 0;
		}
		catch (RuntimeException e)
		{
			return 0;
		}
	}

	/**
	 * @param data
	 * @return the pkt dt port.
	 */
	private int getDstPort(byte[] data)
	{
		try
		{
			if (IPPacket.getIpProtocolType(data) == IPPacketType.TCP)
			{
				TCPPacket tcp = new TCPPacket(data);
				return tcp.getDestinationPort();
			}
			else if (IPPacket.getIpProtocolType(data) == IPPacketType.UDP)
			{
				UDPPacket udp = new UDPPacket(data);
				return udp.getDestinationPort();
			}
			else
				return 0;
		}
		catch (RuntimeException e)
		{
			return 0;
		}
	}

	/**
	 * internal struct.
	 * 
	 * @author rbaryana
	 *
	 */
	class PacketStruct
	{
		byte[] rawData = null;

		CaptureFilePacketHeader pktHdr = null;

		int flowtype = 0;

		/**
		 * @param theHdr
		 * @param theData
		 */
		public PacketStruct(CaptureFilePacketHeader theHdr, byte[] theData, int thetype)
		{
			flowtype = thetype;
			pktHdr = theHdr;
			rawData = theData;
		}
	}

	/**
	 * cap are named according to thier num of pkts.
	 * set the min number of pkts that will count as separte name.
	 * all cap with more pkts will named "Big"
	 * @param theMaxPacketsCategory
	 */
	public void setMaxPacketsCategory(int theMaxPacketsCategory)
	{
		myMaxPacketsCategory = theMaxPacketsCategory;
	}

	/**
	 * set the min number of pkts required with no retransmitions.
	 * for example 5 will require that the 5 first pkts in the cap will not include
	 * duplicates or retransmitions.
	 * @param theNumpktsWithNoRetransmittions
	 */
	public void setNumpktsWithNoRetransmittions(int theNumpktsWithNoRetransmittions)
	{
		myNumpktsWithNoRetransmittions = theNumpktsWithNoRetransmittions;
	}

	private static LineArgs statLineArgs = null;

	/**
	 * for command line.
	 * @param line
	 * @return
	 */
	private static LineArgs getLineArgs(String line)
	{
		
			statLineArgs = new LineArgs();

			statLineArgs.addArg("-t", 1, "Temp directory");
			statLineArgs.addArg("-prefix", 1, "file prefix");
			statLineArgs.addArg("-odata", 0, "Cut only data pkts");
			statLineArgs.addArg("-ochs", 0, "Only complete tcp handshake");
			statLineArgs.addArg("-lretr", 1, "limit retransmittions");
			statLineArgs.addArg("-f", 1, "The file name");
			statLineArgs.addArg("-dir",1,"The src directory");
			statLineArgs.addArg("-filter",1,"The filter");
			statLineArgs.addArg("-iports",0,"Include port names in file name");
			statLineArgs.addArg("-notxt",0,"Will not create file names");
			
		
		statLineArgs.init(line);
		return statLineArgs;
	}
	
	public static void main(String[] args) throws IOException
	{
		
		CapFlowSpliter fs = new CapFlowSpliter();
		fs.splitCap("-f c:\\eync\\eync10.pcap -t c:\\eync\\flows -ochs -prefix eync10_ -notxt", null);
		
	}
}
