package capanalyzer.netutils.files;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

import capanalyzer.netutils.NetUtilsException;
import capanalyzer.netutils.build.FiveTuple;
import capanalyzer.netutils.build.FlowStruct;
import capanalyzer.netutils.build.IPPacket;
import capanalyzer.netutils.build.IPPacketType;
import capanalyzer.netutils.build.TCPPacket;
import capanalyzer.netutils.build.UDPPacket;
import capanalyzer.netutils.files.pcap.PCapFileException;
import capanalyzer.netutils.files.pcap.PCapFileReader;
import capanalyzer.netutils.utils.IP;


/**
 * Class for extracting basic information about a capture file.
 * 
 * 
 * @author roni bar yanai
 *
 */
public class CapInfoReader
{
	private String myFile = null;

	private byte[][] myRawData = null;

	/**
	 * create cap info reader
	 * @param fileName
	 * @throws ErfFileException
	 */
	public CapInfoReader(String fileName) throws PCapFileException
	{
		myFile = fileName;
		myRawData = PCapFileReader.readCapRawData(fileName);
	}

	/**
	 * Build set of all ips included in the capture file.
	 * Ip is presented as string x.x.x.x
	 * @return HashSet of all ips in the capture file
	 */
	public HashSet<String> getAllIps()
	{
		HashSet<String> tmp = new HashSet<String>();
		if (myRawData == null)
		{
			return tmp;
		}
		
		for (int i = 0; i < myRawData.length; i++)
		{
			byte[] pkt = myRawData[i];
			if (IPPacket.statIsIpPacket(pkt))
			{
				IPPacket ippkt = new IPPacket(pkt);
				String srcip = IP.getIPAsString(ippkt.getSourceIP());
				String dstip = IP.getIPAsString(ippkt.getDestinationIP());
				tmp.add(srcip);
				tmp.add(dstip);
			}
		}
		return tmp;
	}

	/**
	 * 
	 * @return array of all ips included in the capture file
	 * @see getAllIps()
	 */
	public String[] getAllIpsAsStringArr()
	{
		HashSet<String> set = getAllIps();
		return (String[]) set.toArray(new String[] {});
	}

	public HashSet<Integer> getAllPorts()
	{
		HashSet<Integer> tmp = new HashSet<Integer>();
		if (myRawData == null)
		{
			return tmp;
		}
		
		for (int i = 0; i < myRawData.length; i++)
		{
			byte[] pkt = myRawData[i];
			if (IPPacket.statIsIpPacket(pkt))
			{
				int type = IPPacket.getIpProtocolType(pkt);
				if (type == IPPacketType.TCP)
				{
					TCPPacket tcppkt = new TCPPacket(pkt);
					tmp.add(new Integer(tcppkt.getSourcePort()));
					tmp.add(new Integer(tcppkt.getDestinationPort()));
				}
				else if (type == IPPacketType.UDP)
				{
					UDPPacket udppkt = new UDPPacket(pkt);
					tmp.add(new Integer(udppkt.getSourcePort()));
					tmp.add(new Integer(udppkt.getDestinationPort()));
				}
			}
		}
		return tmp;
	}

	public int[] getAllPortAsIntArr() 
	{
		HashSet<Integer> set = getAllPorts();
		Integer[] ports = (Integer[]) set.toArray(new Integer[] {});
		int toReturn[] = new int[ports.length];
		for (int i = 0; i < ports.length; i++)
		{
			toReturn[i] = ports[i].intValue();
		}
		return toReturn;
	}

	public FiveTuple[] getAllFlows() throws NetUtilsException
	{
		if (myRawData == null)
		{
			return new FiveTuple[0];
		}
		
		HashSet<String> touplesh = new HashSet<String>();
		ArrayList<FiveTuple> tmp = new ArrayList<FiveTuple>();
		for (int i = 0; i < myRawData.length; i++)
		{
			byte[] pkt = myRawData[i];
			if (IPPacket.statIsIpPacket(pkt))
			{
				int type = IPPacket.getIpProtocolType(pkt);
				if (type == IPPacketType.TCP || type == IPPacketType.UDP)
				{
					FiveTuple ftouple = new FiveTuple(pkt, true);
					if (touplesh.contains(ftouple.getKey()))
						continue;
					else
					{
						touplesh.add(ftouple.getKey());
						tmp.add(ftouple);
					}
				}
			}
		}
		return (FiveTuple[]) tmp.toArray(new FiveTuple[] {});
	}

	public int getNumOfPackets() 
	{
		if (myRawData == null)
		{
			return 0;
		}
		return myRawData.length;
	}

	public int getNumOfTcpPackets()
	{
		if (myRawData == null)
		{
			return 0;
		}
		int counter = 0;
		for (int i = 0; i < myRawData.length; i++)
		{
			byte[] pkt = myRawData[i];
			if (IPPacket.statIsIpPacket(pkt))
			{
				int type = IPPacket.getIpProtocolType(pkt);
				if (type == IPPacketType.TCP)
				{
					counter++;
				}
			}
		}
		return counter;
	}

	public int getNumOfUDPPackets()
	{
		if (myRawData == null)
		{
			return 0;
		}
		int counter = 0;
		for (int i = 0; i < myRawData.length; i++)
		{
			byte[] pkt = myRawData[i];
			if (IPPacket.statIsIpPacket(pkt))
			{
				int type = IPPacket.getIpProtocolType(pkt);
				if (type == IPPacketType.UDP)
				{
					counter++;
				}
			}
		}
		return counter;
	}

	public int getNumOfNOnUdpTcpPackets()
	{
		if (myRawData == null)
		{
			return 0;
		}
		int counter = 0;
		for (int i = 0; i < myRawData.length; i++)
		{
			byte[] pkt = myRawData[i];
			if (IPPacket.statIsIpPacket(pkt))
			{
				int type = IPPacket.getIpProtocolType(pkt);
				if (type != IPPacketType.UDP && type != IPPacketType.TCP)
				{
					counter++;
				}
			}
			else
				counter++;
		}
		return counter;
	}

	public String[] getTopUsersIps()
	{
		HashMap<String, Top> tmp = new HashMap<String, Top>();
		for (int i = 0; i < myRawData.length; i++)
		{
			byte[] pkt = myRawData[i];
			if (IPPacket.statIsIpPacket(pkt))
			{
				IPPacket ippkt = new IPPacket(pkt);
				String srcip = IP.getIPAsString(ippkt.getSourceIP());
				String dstip = IP.getIPAsString(ippkt.getDestinationIP());
				if (tmp.containsKey(srcip))
				{
					Top a = (Top) tmp.get(srcip);
					a.num++;
				}
				else
				{
					Top a = new Top(srcip);
					tmp.put(srcip, a);
				}

				if (tmp.containsKey(dstip))
				{
					Top a = (Top) tmp.get(dstip);
					a.num++;
				}
				else
				{
					Top a = new Top(dstip);
					tmp.put(dstip, a);
				}
			}
		}

		Top[] arr = (Top[]) (tmp.values()).toArray(new Top[] {});

		Arrays.sort(arr);
		String[] toRetrun = new String[arr.length];
		for (int i = 0; i < arr.length; i++)
		{
			toRetrun[i] = arr[i].ip;
		}
		return toRetrun;
	}

	public FlowStruct[] getFlows() throws IOException, NetUtilsException
	{
		CapFlowSpliter fs = new CapFlowSpliter();
		return fs.getFlowsAsPktDataFlowStruct(myFile, false,false);
	}
	
	public String toString()
	{
		String toReturn = "";
		try
		{
			String[] ips = this.getAllIpsAsStringArr();
			int[] ports = this.getAllPortAsIntArr();
			FiveTuple[] touples = this.getAllFlows();
			toReturn = toReturn + "ips num: " + ips.length + "\n";
			toReturn = toReturn + "Ports num: " + ports.length + "\n";
			toReturn = toReturn + "flows == " + touples.length + "\n";
			toReturn = toReturn + "pkts == " + this.getNumOfPackets() + "\n";
			toReturn = toReturn + "tcp == " + this.getNumOfTcpPackets() + "\n";
			toReturn = toReturn + "udp == " + this.getNumOfUDPPackets() + "\n";
			toReturn = toReturn + "else == " + this.getNumOfNOnUdpTcpPackets() + "\n";

			FlowStruct[] flows = getFlows();
			toReturn = toReturn + "number of flows = " + flows.length + "\n";
			FlowStruct max = flows[0];
			for (int i = 0; i < flows.length; i++)
			{
				try
				{
				if (flows[i].getTotalFlowDataSize() > max.getTotalFlowDataSize()) max = flows[i];
				}
				catch (Exception e) {
				e.printStackTrace();
				}
			}
			toReturn = toReturn + "max flow = " + max.getFiveTouple().getAsReadbleString() + "\n";
			toReturn = toReturn + "max flow size = " + max.getTotalFlowDataSize() + "\n";
			toReturn = toReturn + "max flow pkts num  = " + max.getNumberOfPktsInFlow() + "\n";
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return toReturn;
	}
	
	
}


		

class Top implements Comparator<Object>, Comparable<Object>
{

	public Top(String theip)
	{
		ip = theip;
		num = 1;
	}

	public int num = 0;

	public String ip = null;

	public int compare(Object theArg0, Object theArg1)
	{
		Top a = (Top) theArg0;
		Top b = (Top) theArg1;
		return a.num - b.num;
	}

	public int compareTo(Object theArg0)
	{
		Top a = (Top) theArg0;
		return a.num - this.num;
	}
}
