package capanalyzer.netutils.patternFinder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import capanalyzer.netutils.NetUtilsException;
import capanalyzer.netutils.build.ByteUtils;
import capanalyzer.netutils.build.EthernetPacket;
import capanalyzer.netutils.build.FiveTuple;
import capanalyzer.netutils.build.IPPacket;
import capanalyzer.netutils.build.IPPacketType;
import capanalyzer.netutils.build.TCPPacket;
import capanalyzer.netutils.build.UDPPacket;
import capanalyzer.netutils.files.erf.ErfFileReader;

/**
 * represent flow.
 * @author rbaryana
 *
 */
@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
public class FlowStruct
{
	private static final String PADDING_STRING = "                                                                                                                                                                                            ";
	private static final int DATA_FACTOR_DOWNLOAD_SUSPCET = 10;
	private static final int REMOVE_DATA_FROM_PACKET = 40;
	
	private int myId = 0;
	private byte[][] myRawData = null;
	private byte[][] myData = null;
	private String myFileName = null;
	private FiveTuple myFiveTouple = null;
	
	private boolean isTcp = false;
	private boolean isUdp = false;
	
	private long myInitiatorIp = 0;
	private long myDstIp = 0;
	
	
	private long myClientToServerDataSize =0;
	private long myServerToClientDataSize =0;
	
	private boolean isLoaded = true;
	private boolean isValidIPFlow = true;

	public FlowStruct()
	{
		myData = new byte[][]{};
		myRawData = new byte[][]{};
	}
	
	public FlowStruct(int theId,String theFileName,byte[][] rawData) throws CapException, NetUtilsException
	{
		myId = theId;
		myRawData = rawData;
		myData = new byte[myRawData.length][];
		myFileName = theFileName;
		if (rawData.length == 0)
			return;
		myFiveTouple = new FiveTuple(rawData[0], false);
		
		
	    myInitiatorIp = myFiveTouple.getMySrcIp();
	    myDstIp = myFiveTouple.getMyDstIp();
    
		for (int i = 0; i < rawData.length; i++)
		{
			if (EthernetPacket.statIsIpPacket(rawData[i]) && IPPacket.isFragment(rawData[i])==false)
			{
				if (IPPacket.getIpProtocolType(rawData[i]) == IPPacketType.TCP)
				{
				    isTcp = true;
					TCPPacket tcppkt = (TCPPacket) IPPacket.getPacket(rawData[i]);
					myData[i] = tcppkt.getTCPData();
					if (getPktSise(tcppkt) == PacketSide.CLIENT_TO_SERVER)
					{
					    myClientToServerDataSize+=myData[i].length;
					}
					else
					    myServerToClientDataSize+=myData[i].length;
    			}
				else if(IPPacket.getIpProtocolType(rawData[i]) == IPPacketType.UDP)
				{
					UDPPacket udppckt = (UDPPacket) IPPacket.getPacket(rawData[i]);
					
					if(udppckt.getIpPktTotalLength()<udppckt.getUDPLength())
					{
						isValidIPFlow = false;
						myData[i] = new byte[]{};
					} else
					{
						myData[i] = udppckt.getUDPData();
						
						isUdp = true;
						if (getPktSide(udppckt) == PacketSide.CLIENT_TO_SERVER)
						{
						    myClientToServerDataSize+=myData[i].length;
						}
						else
						    myServerToClientDataSize+=myData[i].length;
					}			
				}
				else
				{
					myData[i] = new byte[]{};
				}
			}
			else
			{
				isValidIPFlow = false;
				myData[i] = new byte[]{};
			}
			
			FiveTuple tmp = new FiveTuple(rawData[i], false);
	
			if (!tmp.equals(myFiveTouple) && !tmp.isOpposite(myFiveTouple))
				throw new CapException("Got more then one flow in cap");
		}
	}
	
	
	/**
	 * for performance.
	 * most of the time only the few first packets ( less then 20) are interting
	 * there is not use to keep them all in memory.
	 * the method clean all non relevant packets.
	 * (will be loadedagain  if needed).
	 */
	private void cleanData()
	{
		for(int i=REMOVE_DATA_FROM_PACKET ; i<myData.length ; i++)
		{
			myRawData[i] = new byte[]{};
			myData[i] = new byte[]{};
		}
		isLoaded = false;
	}
	
	/**
	 * if pkt not loaded then will load it.
	 * @param pkt
	 */
	private void loadData(int pkt)
	{
		if (isLoaded)
			return;
		
		if (pkt >= (REMOVE_DATA_FROM_PACKET - 1))
		{
			loadData();
			isLoaded = true;
		}
	}
	
	/**
	 * load all packets to memory
	 *@see cleanData()/
	 */
	private void loadData()
	{
		if (isLoaded)
			return;
		try
		{
			myRawData = ErfFileReader.readCapRawData(myFileName);
		}
		 catch (IOException e)
		{
			e.printStackTrace();
		}
		myData = new byte[myRawData.length][];
		for (int i = 0; i < myRawData.length; i++)
		{
			if (EthernetPacket.statIsIpPacket(myRawData[i]))
			{
				if (IPPacket.getIpProtocolType(myRawData[i]) == IPPacketType.TCP)
				{
				    isTcp = true;
					TCPPacket tcppkt = (TCPPacket) IPPacket.getPacket(myRawData[i]);
					myData[i] = tcppkt.getTCPData();
    			}
				else if(IPPacket.getIpProtocolType(myRawData[i]) == IPPacketType.UDP)
				{
					UDPPacket udppckt = (UDPPacket) IPPacket.getPacket(myRawData[i]);
					
					myData[i] = udppckt.getUDPData();
						
					isUdp = true;
				}
				else
				{
					myData[i] = new byte[]{};
				}
			}
		}
		isLoaded = true;
	}
	
	/**
	 * will return only one side of the flow.
	 * @param side - the wnated side
	 * @return the new flow
	 * @throws CapException
	 * @throws NetUtilsException 
	 */
	public FlowStruct splitFlow(PacketSide side) throws CapException, NetUtilsException
	{
		loadData();
		ArrayList list = new ArrayList();
		for (int i = 0; i < myRawData.length; i++)
		{
			if (getPktSide(myRawData[i]) == side)
				list.add(myRawData[i]);
		}
		
		byte[][] data = new byte[list.size()][];
		for (int i = 0; i < data.length; i++)
		{
			data[i] = (byte[]) list.get(i);
		}
		return new FlowStruct(-1,"split flow",data);
	}
	
	/*
	 * return the packet side.
	 */
	private PacketSide getPktSide(byte[] pkt)
	{
		IPPacket ippkt = new IPPacket(pkt);
		return (ippkt.getSourceIP() == myInitiatorIp)?PacketSide.CLIENT_TO_SERVER:PacketSide.SERVER_TO_CLIENT;
	}
 
	/**
	 * 
	 * @param maxPkts - the max packet to look
	 * @return all data as string
	 * @throws CapException
	 */
	public String getAllDataAsSingleString(int maxPkts) 
	{
		loadData(maxPkts);
		int length = 0;
		for (int i = 0; i < myData.length && i<=maxPkts ; i++)
		{
			length+=myData[i].length;
		}
		byte[] toRetrun = new byte[length];
		int idx = 0;
		for (int i = 0; i < myData.length && i<=maxPkts ; i++)
		{
			System.arraycopy(myData[i],0,toRetrun,idx,myData[i].length);
			idx+=myData[i].length;
		}
		return new String(toRetrun);
	}
	
	/**
	 * will return all the data as single byte array.
	 * @param numOfPkts - max packet to look
	 * @return all bytes.
	 * @throws CapException
	 */
	public byte[] getAsSingleByteArray(int numOfPkts) 
	{
		loadData(numOfPkts);
		int length = 0;
		for (int i = 0; i < myData.length; i++)
		{
			length+=myData[i].length;
		}
		byte[] toRetrun = new byte[length];
		int idx = 0;
		for (int i = 0; i < myData.length && i<numOfPkts; i++)
		{
			System.arraycopy(myData[i],0,toRetrun,idx,myData[i].length);
			idx+=myData[i].length;
		}
		return toRetrun;
	}

	/**
	 * @return the flow five touple.
	 */
	public FiveTuple getFiveTouple()
	{
		return myFiveTouple;
	}

	/**
	 * @return all flows as array of packets (each packet byte[])
	 * @throws CapException
	 */
	public byte[][] getRawData() 
	{
		loadData();
		return myRawData;
	}
	
	/**
	 * will return the data packet src ip.
	 * will ignore all packets that do not contain any data.
	 * @param theNum
	 * @return the ip, will return 0 ip no enough data
	 * @throws CapException 
	 */
	public long getDataPacketSrcIp(int theNum)
	{
		if (theNum<= myData.length && theNum>=1)
	    {
	        int j = 0;
	        for(int i=0 ; i<myRawData.length ; i++)
	        {
	        	loadData(i);
	            if(myData[i].length > 0)
	                j++;
	            if (j==theNum)
	            {
	                IPPacket pkt = new IPPacket(myRawData[i]);
	            	return pkt.getSourceIP();
	            }
	        }
	        return 0;
	    }
	    return 0;
	}
	
	/**
	 * will return the data packet dst ip.
	 * will ignore all packets that do not contain any data.
	 * @param theNum
	 * @return the ip, will return 0 ip no enough data
	 * @throws CapException 
	 */
	public long getDataPacketDstIp(int theNum)
	{
		if (theNum<= myData.length && theNum>=1)
	    {
	        int j = 0;
	        for(int i=0 ; i<myRawData.length ; i++)
	        {
	        	loadData(i);
	            if(myData[i].length > 0)
	                j++;
	            if (j==theNum)
	            {
	                IPPacket pkt = new IPPacket(myRawData[i]);
	            	return pkt.getDestinationIP();
	            }
	        }
	        return 0;
	    }
	    return 0;
	}
	
	/**
	 * return the data byte array of pkt number ...
	 * @param pkt - the pkt number (start with 1)
	 * @return the pkt data or null if no such pkt num in the flow.
	 * @throws CapException 
	 */
	public byte[] getData(int pkt)
	{
	    if (pkt<= myData.length && pkt>=1)
	    {
	        int j = 0;
	        for(int i=0 ; i<myRawData.length ; i++)
	        {
	        	loadData(i);
	            if(myData[i].length > 0)
	                j++;
	            if (j==pkt)
	                return myData[i];
	        }
	        return null;
	    }
	    return null;
	}
	
	/**
	 * return the data byte array of pkt number ...
	 * @param pkt - the pkt number (start with 0)
	 * @return the pkt data or null if no such pkt num in the flow.
	 * @throws CapException 
	 */
	public byte[] getDataStartZero(int pkt)
	{
	    if (pkt<= myData.length && pkt>=0)
	    {
	        int j = -1;
	        for(int i=0 ; i<myRawData.length ; i++)
	        {
	        	loadData(i);
	            if(myData[i].length > 0)
	                j++;
	            if (j==pkt)
	                return myData[i];
	        }
	        return null;
	    }
	    return null;
	}
	
	/**
	 * return the total packet number in the flow include non data
	 * packets such as syn,acks etc.
	 * the first data packet will probably be the 4 packet in tcp.
	 * @param num - the data packet number
	 * @return the total packet number
	 * @throws CapException 
	 */
	public int getDataPacketFlowNum(int num) 
	{
	    if (num<= myData.length && num>=1)
	    {
	        int j = 0;
	        for(int i=0 ; i<myRawData.length ; i++)
	        {
	        	loadData(i);
	            if(myData[i].length > 0)
	                j++;
	            if (j==num)
	                return i;
	        }
	        return -1;
	    }
	    return -1;
	}
	
	
	/**
	 * return the data pkt number (parameter) side.
	 * will return null if not enough data packets in the flow
	 * @param pkt
	 * @return the packet side.
	 * @throws CapException 
	 */
	public PacketSide getDataPktSide(int pkt)
	{
	    if (pkt<= myRawData.length && pkt>=1)
	    {
	        byte raw[] =  null;
	        if (pkt<= myData.length && pkt>=1)
		    {
	        	int j = 0;
		        for(int i=0 ; i<myRawData.length ; i++)
		        {
		        	loadData(i);
		            if(myData[i].length > 0)
		                j++;
		            if (j==pkt)
		            {
		                raw = myRawData[i];
		                break;
		            }
		                
		        }
     	    }
	        
	        if (raw == null)
	            return null;
	       
	        if (isTcp)
	            return getPktSise(new TCPPacket(raw));
	        
	        if(isUdp)
	            return getPktSide(new UDPPacket(raw));
	    }
	    return null;
	}
	
	/**
	 * return the data pkt number (parameter) side.
	 * will return null if not enough data packets in the flow
	 * @param pkt
	 * @return the packet side.
	 * @throws CapException 
	 */
	public PacketSide getDataPktSideStartZero(int pkt)
	{
	    if (pkt<= myRawData.length && pkt>=0)
	    {
	        byte raw[] =  null;
	        if (pkt<= myData.length && pkt>=0)
		    {
	        	int j = -1;
		        for(int i=0 ; i<myRawData.length ; i++)
		        {
		        	loadData(i);
		            if(myData[i].length > 0)
		                j++;
		            if (j==pkt)
		            {
		                raw = myRawData[i];
		                break;
		            }
		                
		        }
     	    }
	        
	        if (raw == null)
	            return null;
	       
	        if (isTcp)
	            return getPktSise(new TCPPacket(raw));
	        
	        if(isUdp)
	            return getPktSide(new UDPPacket(raw));
	    }
	    return null;
	}
	
	/**
	 * return the packet side.
	 * @param pkt - the packet number in the flow (including non data packtes such syn)
	 * @return the packet side.
	 * @throws CapException
	 */
	public PacketSide getPktSide(int pkt) 
	{
	    if (pkt<= myRawData.length && pkt>=1)
	    {
	    	loadData(pkt);
	        byte raw[] =  myRawData[pkt-1];
	        if (isTcp)
	            return getPktSise(new TCPPacket(raw));
	        
	        if(isUdp)
	            return getPktSide(new UDPPacket(raw));
	    }
	    return null;
	}
	
	/**
	 * @param pkt - the packet number in the flow (including non data packtes such syn)
	 * @return the packet is arrow 
	 * @throws CapException
	 */
	public String getPktSideArrow(int pkt)
	{
		PacketSide side = getPktSide(pkt);
		if (side == null)
			return "Not Known";
		
		if (side == PacketSide.CLIENT_TO_SERVER)
			return "--------->";
		
		return "<----------";
	}
	
	/**
	 * @return all packets sizes as int array,
	 */
	public int[] getFlowPktSizes()
	{
		loadData();
		int toreturn[] = new int[myData.length];
		for (int i = 0; i < toreturn.length; i++)
		{
		     toreturn[i] = myData[i].length;	
		}
		return toreturn;
	}
	
	/**
	 * @return the amount of data bytes in the flow.
	 */
	public int getTotalFlowDataSize()
	{
		loadData();
		int n = 0;
		for (int i = 0; i < myRawData.length; i++)
		{
			n+=myData[i].length;
		}
		return n;
	}
	
	/**
	 * @return all the flow as readble string (similar to ethreal)
	 * @throws CapException
	 */
	public String getDataStreamASKiwiLikeText() 
	{
		return getDataStreamASKiwiLikeText(myData.length);
	}
	
	/**
     * @param maxPkt - the max packet in the flow to take.
	 * @return all the flow as readble string (similar to ethreal)
	 * @throws CapException
	 */
	public String getDataStreamASKiwiLikeText(int maxPkt) 
	{
		int chunk = 16;
		int padding = 70;
		BigString toRetrun = new BigString();
		
		if (isTransmissions(getNumberOfPktsInFlow()))
		{
			toRetrun.addString("FILES CONTAINS RETRANSMISSION, NOT FULLY SUPPORTED YET! \n");
    	}
		
		//toRetrun.addString(myFiveTouple.toString());
		ByteUtils byteutils = new ByteUtils();
		
		for (int i = 0; i < maxPkt; i++)
		{
			PacketSide pktside = getPktSide(i+1);
			if (myData[i] == null || myData[i].length == 0)
				continue;
								
			String pkttype = (isTcp)?"TCP":((isUdp)?"UDP":"UNKOWN");
			toRetrun.addString("\r\n"+"Packet #"+i+".      Payload length=" +myData[i].length+".      Protocol="+pkttype+"    "+pktside.toArrow()+"\r\n");
			toRetrun.addString("-----------------------------------------------------------------------------------\r\n");
			toRetrun.addString(pktside.toArrow());
			if (pktside.equals(PacketSide.CLIENT_TO_SERVER))
				toRetrun.addString(myFiveTouple.getMySrcIpAsString()+" : "+myFiveTouple.getSrcPort()+" , "+myFiveTouple.getMyDstIpAsString()+" : "+myFiveTouple.getDstPort()+"\r\n");
			else
				toRetrun.addString(myFiveTouple.getMyDstIpAsString()+" : "+myFiveTouple.getDstPort()+" , "+myFiveTouple.getMySrcIpAsString()+" : "+myFiveTouple.getSrcPort()+"\r\n");
			toRetrun.addString("-----------------------------------------------------------------------------------\r\n");

			if (myData[i].length >0)
			{
				for (int j = 0; j < myData[i].length; )
				{
					int size = (j+chunk);
					size = (size>myData[i].length)?myData[i].length:size;
					String tmp = byteutils.getAsString(myData[i],j,size,chunk);
					tmp = (tmp.length() < padding)?tmp+getPaddString(padding-tmp.length()-(j==0?6:0)):tmp;
					toRetrun.addString(tmp);
					tmp = new String(myData[i],j,size-j);
					tmp = tmp.replaceAll("\r",".");
					tmp = tmp.replaceAll("\n",".");
					tmp = tmp.replaceAll("\t",".");
					tmp = tmp.replaceAll("[^\\p{Print}]",".");
					toRetrun.addString(" "+tmp.trim());
					j+=chunk;
				}
			}
			else
			{
				TCPPacket tcp = new TCPPacket(myRawData[i]);
				String tmp ="";
				if (tcp.isSyn())
				{
					tmp = tmp + "[Syn] ";
				}
				if (tcp.isFin())
				{
					tmp = tmp + "[Fin] ";
				}
				if (tcp.isAck())
				{
					tmp = tmp + "[Ack] ";
				}
				
				toRetrun.addString(tmp);
				
			}
			toRetrun.addString("\r\n");
		
		}
		return toRetrun.getAsString();
	}
	
	/**
	 * @return the flow packet sizes and directions as readble string
	 * @throws CapException
	 */
	public String getOnlySizesAndDirections() 
	{
		return getOnlySizesAndDirections(myData.length);
	}
	
	/**
	 * @param maxPkt - max packet to take.
	 * @return the flow packet sizes and directions as readble string
	 * @throws CapException
	 */
	public String getOnlySizesAndDirections(int maxPkt) 
	{
		//int chunk = 32;
		//int padding = chunk*2+(chunk/4)+1;
		BigString toRetrun = new BigString();
		
		if (isTransmissions(getNumberOfPktsInFlow()))
		{
			toRetrun.addString("FILES CONTAINS RETRANSMISSION, NOT FULLY SUPPORTED YET! \n");
    	}
		
		toRetrun.addString(myFiveTouple.toString());
		//ByteUtils byteutils = new ByteUtils();
		
		for (int i = 0; i < maxPkt; i++)
		{
			
			toRetrun.addString("\n\n"+"packet number:"+i+"\n");
			toRetrun.addString(getPktSideArrow(i+1)+"\n");
			toRetrun.addString("data size:"+myData[i].length+"\n=================\n");
			
				TCPPacket tcp = new TCPPacket(myRawData[i]);
				String tmp ="";
				if (tcp.isSyn())
				{
					tmp = tmp + "[Syn] ";
				}
				if (tcp.isFin())
				{
					tmp = tmp + "[Fin] ";
				}
				if (tcp.isAck())
				{
					tmp = tmp + "[Ack] ";
				}
				
				toRetrun.addString(tmp);
    	}
		return toRetrun.getAsString();
	}
	
	/**
	 * calculate the average of the data in a packet.
	 * used to find encrypted packets, thier average should be ~128.
	 * (if they are long enough)  
	 * @param packt - the packet to check.
	 * @param bytes - the number of bytes to take from this packet.
	 * @return the average
	 */
	public int getDataAverage(int packt,int bytes)
	{
		byte[] data = getData(packt);
		long sum = 0;
		if (data != null)
		{
			int counter =0;
			for (int i = 0; i < data.length && (i<bytes || bytes<=0); i++)
			{
				sum = sum + (0xff & data[i]);
				counter++;
			}
			return (int) (sum/counter);
				
		}
		
		return 0;
	}
	
	/**
	 *
	 * @param arr
	 * @return the average of the array.
	 */
	private int getAverage(byte[] arr)
	{
		long sum = 0;
		for (int i = 0; i < arr.length; i++)
		{
			sum = sum + (0xff & arr[i]);
		}
		return (int) (sum/(arr.length));
	}
	
	/**
	 * @return the flow packet sizes and directions as readble string
	 * (only data packets are counted)
	 * @throws CapException
	 */
	public String getOnlySizesAndDirectionsOnlyDataPkts() 
	{
		return getOnlySizesAndDirectionsOnlyDataPkts(myData.length);
	}
	
	/**
	 * @param maxPkt - the max packet to take,
	 * @return the flow packet sizes and directions as readble string
	 * (only data packets are counted).
	 * @throws CapException
	 */
	public String getOnlySizesAndDirectionsOnlyDataPkts(int maxPkt)
	{
		//int chunk = 32;
		//int padding = chunk*2+(chunk/4)+1;
		BigString toRetrun = new BigString();
		
		if (isTransmissions(getNumberOfPktsInFlow()))
		{
			toRetrun.addString("FILES CONTAINS RETRANSMISSION, NOT FULLY SUPPORTED YET! \n");
    	}
		
		toRetrun.addString(myFiveTouple.toString());
		
		
		for (int i = 0; i < maxPkt; i++)
		{
			if (myData[i].length ==0)
				continue;
			toRetrun.addString("\n\n"+"packet number:"+i+"\n");
			toRetrun.addString(getPktSideArrow(i+1)+"\n");
			toRetrun.addString("data size:"+myData[i].length+"\n=================\n");
			
			int avreage = getAverage(myData[i]);
			toRetrun.addString("data average:"+avreage+"\n=================\n");
			
				TCPPacket tcp = new TCPPacket(myRawData[i]);
				String tmp ="";
				if (tcp.isSyn())
				{
					tmp = tmp + "[Syn] ";
				}
				if (tcp.isFin())
				{
					tmp = tmp + "[Fin] ";
				}
				if (tcp.isAck())
				{
					tmp = tmp + "[Ack] ";
				}
				
				toRetrun.addString(tmp);
				
			
		
		}
		return toRetrun.getAsString();
	}
	
	
	/*
	 * 
	 */
	private String getPaddString(int size)
	{
		if (size < PADDING_STRING.length())
			return PADDING_STRING.substring(0,size);

		String toReturn ="";
		for (int i = 0; i < size; i++)
		{
			toReturn = toReturn+" ";
		}
		return toReturn;
	}
	
	/**
	 * @return the number of packets in the flow.
	 */
	public int getNumberOfPktsInFlow()
	{
		return myRawData.length;
	}
	
    /**
     * @return Returns the isTcp.
     */
    public boolean isTcp() {
        return isTcp && !isUdp;
    }
    /**
     * @return Returns the isUdp.
     */
    public boolean isUdp() {
        return isUdp && !isUdp;
    }
    /**
     * @return Returns the myId.
     */
    public int getMyId() {
        return myId;
    }
    
    /**
     * @return true if the flow contains the syn,synack,ack hande shake on 
     * the first 3 pkts.
     */
    public boolean isCompleteFlowStart()
    {
        if (isUdp)
            return true;
        
          if (myRawData.length<3)
              return false;
          
          TCPPacket syn = new TCPPacket(myRawData[0]);
          TCPPacket synack = new TCPPacket(myRawData[1]);
          TCPPacket ack = new TCPPacket(myRawData[2]);
          return  (syn.isSyn() && synack.isSyn() && synack.isAck() && ack.isAck());
    }
    
    
    /**
     * @param maxpkts - max packets to check
     * @return true if packet contains retransmissions in the first maxpkts of the flow
     *    and false otherwise.
     */
    public boolean isTransmissions(int maxpkts)
    {
        if (isUdp)
            return false;
        HashSet cleintseq = new HashSet();
        HashSet serverseq = new HashSet();
        for(int i=0 ; i<maxpkts && i<myRawData.length ; i++)
        {
        	loadData(i);
            TCPPacket tcp = new TCPPacket(myRawData[i]);
            if (tcp.isFin())
                return false;
            
            PacketSide side = getPktSise(tcp);
            if (side == PacketSide.CLIENT_TO_SERVER)
            {
                if (cleintseq.contains(""+tcp.getSequenceNumber()+myData[i].length))
                {
   
                    return true;
                }
                
                cleintseq.add(""+tcp.getSequenceNumber()+myData[i].length);
                    
            } 
            else
            {

                if (serverseq.contains(""+tcp.getSequenceNumber()+myData[i].length))
                    return true;
                
                serverseq.add(""+tcp.getSequenceNumber()+myData[i].length);
                    
            
            }
        }
        
        return false;
    }
    
    /**
     * if src ip is the same as the initiator of the stream
     * then clinet to server
     * else
     * server to client
     * @param pkt
     * @return PacketSide
     */
    private PacketSide getPktSise(TCPPacket pkt)
    {
        if (pkt.getSourceIP() == myInitiatorIp)
            return PacketSide.CLIENT_TO_SERVER;
        
        return PacketSide.SERVER_TO_CLIENT;
    }
    
    /**
     * if src ip is the same as the initiator of the stream
     * then clinet to server
     * else
     * server to client
     * @param pkt
     * @return PacketSide
     */
    private PacketSide getPktSide(UDPPacket pkt)
    {
        if (pkt.getSourceIP() == myInitiatorIp)
            return PacketSide.CLIENT_TO_SERVER;
        
        return PacketSide.SERVER_TO_CLIENT;
    }
    
    /**
     * @return the total bytes dowonloaded
     */
    public long getMyTotalDownloadInBytes()
    {
        return myServerToClientDataSize;
    }
    
    /**
     * @return the total bytes uploaded 
     */
    public long getMyTotalUploadInBytes()
    {
        return myClientToServerDataSize;
    }
    
    /**
     * @param factor
     * @return true if total download is bigger then total upload*factor
     */
    public boolean isDownloadSuspected(int factor)
    {
        return (myServerToClientDataSize > (myClientToServerDataSize*factor));
    }
    
    /**
     * @return true if total download is bigger then total upload* default  factor
     */
    public boolean isDownloadSuspected()
    {
        return isDownloadSuspected(DATA_FACTOR_DOWNLOAD_SUSPCET);
    }
    
    /**
     * @return Returns the myFileName.
     */
    public String getMyFileName() {
        return myFileName;
    }

    /**
     * @return the flow initiator ip.
     */
	public long getFlowInitiatorIp()
	{
		return myInitiatorIp;
	}

	/**
	 * @return the flow "server" ip
	 */
	public long getDstIp()
	{
		return myDstIp;
	}
	
	/**
	 * 
	 * @return
	 */
	public int getFlowType()
	{
		return myFiveTouple.getMyType();
	}
	
	/**
	 * @return the number of packets in the flow
	 */
	public int getFlowLength()
	{
		return myRawData.length;
	}
	
	/**
	 * @return the isValidIPPacket
	 */
	public boolean isValidIPFlow()
	{
		return isValidIPFlow;
	}
}
