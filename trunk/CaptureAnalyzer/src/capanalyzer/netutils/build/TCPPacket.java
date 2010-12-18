package capanalyzer.netutils.build;

import capanalyzer.netutils.utils.IP;

/**
 * TCP packet implementation. The class provides methods for parsing
 * and building TCP packets.<br>
 * Mainly accessing and setting fields.<br>
 * <br>
 * 
 * @author roni bar-yanai
 *<br>
 */
public class TCPPacket extends IPPacket
{
	public static final int CALCULATE_TCP_CHECKSUM = 0;
	
	// constants for internal use.
	private static final int DEFAULT_WINDOW_SIZE = 8192;
		
	private static final int DEFAULT_TCP_HDR_LENGTH = 20;

	private static int TCP_URG_MASK = 0x0020;

	private static int TCP_ACK_MASK = 0x0010;

	private static int TCP_PSH_MASK = 0x0008;

	private static int TCP_RST_MASK = 0x0004;

	private static int TCP_SYN_MASK = 0x0002;

	private static int TCP_FIN_MASK = 0x0001;

	/* 
	 * tcp packet structure:
	 * ---------------------
	 * 
	 * 
	 * 0                                 15 16                                    32
	 * ------------------------------------------------------------------------------
	 * |    16 bit src port                |          16 bit dst port               |
	 * ------------------------------------------------------------------------------
	 * |                32 bit sequence number                                      |
	 * ------------------------------------------------------------------------------
	 * |                32 bit acknowledgment number                                |
	 * ------------------------------------------------------------------------------
	 * |  hdr |  6  reserved   |U|A|P|R|S|F|                                        |
	 * | size |   bits         |R|C|S|S|Y|I|      16 bit, window size               |
	 * |4 bits|                |G|K|H|T|N|N|                                        |
	 * ------------------------------------------------------------------------------
	 * |  16 bit tcp check sum             |    16 bit urgent pointer               |
	 * ------------------------------------------------------------------------------
	 * |      options if any                                                        |
	 * |                                   |                                        |
	 * -----------------------------------------------------------------------------
	 * |
	 * |                 DATA 
	 * |
	 * --------------------------------------------------------------------------=-
	 *
	 * Constants for common offsers in the header.
	 */
	private static final int TCP_SRC_PORT_POS = 0;

	private static final int TCP_DST_PORT_POS = 2;

	private static final int TCP_SEQUENCE_NUM_POS = 4;

	private static final int TCP_ACK_NUM_POS = 8;

	private static final int TCP_HDR_LEN_POS = 12;

	private static final int TCP_HDR_FLAGS_POS = 12;

	private static final int TCP_WINDOW_SIZE_POS = 14;

	private static final int TCP_CHECKSUM_POS = 16;

	private static final int TCP_URGENT_PTR_POS = 18;

	// members for holding fields values.
	// As in other classes, fields are only parsed when need
	// or set in order to save performance.
	private int _tcpOffset = 0;
	
	// holds the header length
	private int myTcpHdrLh = DEFAULT_TCP_HDR_LENGTH;

	// holds the source port
	private int mySrcPort = 0;

	// holds the destination port
	private int myDstPort = 0;

	// holds the packet sequence number
	private long mySequenceNumber = 0;

	// holds the acknowledgement number
	private long myAcknowledgmentNumber = 0;

	// holds payload length
	private int myPayloadDataLength = 0;

	// tcp window size
	private int myWindowSize = 0;

	// hold the checksum field
	private int myTcpChecksum = 0;

	// urger pointer field
	private int myUrgentPointer = 0;

	// holds the tcp header flags (syn,rst...etc)
	private int myFlags = 0;
	
	// total tcp packet length
	private int myTotalTcpLength = 0;
	
	
	// tracing which fields were set by the user.
	boolean _isWriteAckNum = false;
	boolean _isWriteSequenceNum = false;
	boolean _isWriteTCPCheckSum = false;
	boolean _isWriteUrgentPtr = false;
	boolean _isWriteWindowSize = false;

	/**
	 * Create new empty tcp packet.
	 */
	public TCPPacket()
	{
		super();
		// make sure all fields were set as field so 
		// we will not try to read them from non existing buffer.
		_isReadAckNum = true;
		_isReadDstPort = true;
		_isReadSequenceNum = true;
		_isReadSrcPort = true;
		_isReadTCPCheckSum = true;
		_isReadUrgentPtr = true;
		_isReadWindowSize = true;
		_isReadAllFlags = true;
		setIPProtocol(IPPacketType.TCP);
		myTotalTcpLength = DEFAULT_TCP_HDR_LENGTH;
	}

	/**
	 * Create new TCP packet. <br>
	 * @pre the buffer contains valid buffer.
	 * @param thePacket - byte buffer with valid packet (including ip and eth parts).
	 */
	public TCPPacket(byte[] thePacket)
	{
		super(thePacket);
		_tcpOffset = myIPHdrOffset + myIPHdrLength;
		myTcpHdrLh = ((ByteUtils.getByteNetOrderTo_uint16(myPacket, _tcpOffset + TCP_HDR_LEN_POS) >> 12) & 0x0f) * 4;
		myPayloadDataLength = getIpPktTotalLength() - myIPHdrLength - myTcpHdrLh;
	}

	boolean _isReadSrcPort = false;

	/** 
	 * @return the source port number.
	 */
	public int getSourcePort()
	{
		if (_isReadSrcPort == false)
		{
			_isReadSrcPort = true;
			mySrcPort = ByteUtils.getByteNetOrderTo_uint16(myPacket, _tcpOffset + TCP_SRC_PORT_POS);
		}
		return mySrcPort;
	}

	boolean _isWriteSrcPort = false;

	/**
	 * Set the destination port<br>
	 * 
	 * @param port - 0-65535 (16 bits)
	 *  (no enforcement - lager values would be trimmed)
	 */
	public void setSourcePort(int port)
	{
		_isReadSrcPort = true;
		_isWriteSrcPort = true;
		mySrcPort = port;
	}

	boolean _isReadDstPort = false;

	/** 
	 * @return the destination port number.
	 */
	public int getDestinationPort()
	{
		if (_isReadDstPort == false)
		{
			myDstPort = ByteUtils.getByteNetOrderTo_uint16(myPacket, _tcpOffset + TCP_DST_PORT_POS);
			_isReadDstPort = true;
		}
		return myDstPort;
	}

	boolean _isWriteDstPort = false;

	/**
	 * set the destination port
	 * @param port - 16 bit unsigned.
	 */
	public void setDestinationPort(int port)
	{
		_isWriteDstPort = true;
		_isReadDstPort = true;
		myDstPort = port;
	}

	boolean _isReadSequenceNum = false;

	/** 
	 * @return the packet sequence number.
	 */
	public long getSequenceNumber()
	{
		if (_isReadSequenceNum == false)
		{
			mySequenceNumber = ByteUtils.getByteNetOrderTo_uint32(myPacket, _tcpOffset + TCP_SEQUENCE_NUM_POS);
			_isReadSequenceNum = true;
		}
		return mySequenceNumber;
	}

	/**
	 * set the tcp sequence num
	 * @param sequence - 32 bit unsigned integer.
	 */
	public void setSequenceNum(long sequence)
	{
		_isWriteSequenceNum = true;
		_isReadSequenceNum = true;
		mySequenceNumber = sequence;
	}

	boolean _isReadAckNum = false;

	/** 
	 *@return the packet acknowledgment number.
	 */
	public long getAcknowledgmentNumber()
	{
		if (_isReadAckNum == false)
		{
			_isReadAckNum = true;
			myAcknowledgmentNumber = ByteUtils.getByteNetOrderTo_uint32(myPacket, _tcpOffset + TCP_ACK_NUM_POS);
		}
		return myAcknowledgmentNumber;
	}

	/**
	 * set the acknowledgment number
	 * @param ack - 32 bit unsigned integer. 
	 */
	public void setAckNum(long ack)
	{
		_isWriteAckNum = true;
		_isReadAckNum = true;
		myAcknowledgmentNumber = ack;
	}

	/** 
	 * @return the TCP header length in bytes.
	 */
	public int getTCPHeaderLength()
	{
		return myTcpHdrLh;
	}

	/**
	 * @return the length of the payload data.
	 */
	public int getPayloadDataLength()
	{
		return myPayloadDataLength;
	}

	boolean _isReadWindowSize = false;

	/**
	 * get the window size.
	 */
	public int getWindowSize()
	{
		if (_isReadWindowSize == false)
		{
			myWindowSize = ByteUtils.getByteNetOrderTo_uint16(myPacket, _tcpOffset + TCP_WINDOW_SIZE_POS);
			_isReadWindowSize = true;
		}
		return myWindowSize;
	}

	/**
	 * set the window size.
	 * @param size - 16 bits unsigned integer
	 */
	public void setWindowSize(int size)
	{
		_isWriteWindowSize = true;
		_isReadWindowSize = true;
		myWindowSize = size;
	}

	boolean _isReadTCPCheckSum = false;

	/** 
	 * @return header checksum.
	 */
	public int getTCPChecksum()
	{
		if (_isReadTCPCheckSum == false)
		{
			myTcpChecksum = ByteUtils.getByteNetOrderTo_uint16(myPacket, _tcpOffset + TCP_CHECKSUM_POS);
			_isReadTCPCheckSum = true;
		}
		return myTcpChecksum;
	}

	/**
	 * set the tcp check sum.
	 * set to CALCULATE_TCP_CHECKSUM to be filled automatically.
	 * @param checksum - 16 bit unsigned int
	 * 
	 */
	public void setTCPChecksum(int checksum)
	{
		_isReadTCPCheckSum = true;
		myTcpChecksum = checksum;
	}

	boolean _isReadUrgentPtr = false;

	/** 
	 * @return the urgent pointer.
	 */
	public int getUrgentPointer()
	{

		if (_isReadUrgentPtr == false)
		{
			myUrgentPointer = ByteUtils.getByteNetOrderTo_uint16(myPacket, _tcpOffset + TCP_URGENT_PTR_POS);
			_isReadUrgentPtr = true;
		}
		return myUrgentPointer;
	}
	
	/**
	 * set the urgent ptr val
	 * @param val - 16 bit unsigned int.
	 */
	public void setUrgentPointer(int val)
	{
		_isReadUrgentPtr = true;
		myUrgentPointer = val;
	}

	private boolean _isReadAllFlags = false;

	/**
	 * @return flags field.
	 */
	public int getAllFlags()
	{
		if (_isReadAllFlags == false)
		{
			myFlags = ByteUtils.getByteNetOrderTo_uint16(myPacket, _tcpOffset + TCP_HDR_LEN_POS) & 0x3f;
			_isReadAllFlags = true;
		}
		return myFlags;
	}

	/**
	 * @return true if URG flag is on. 
	 * Check the URG flag, flag indicates if the urgent pointer is valid.
	 */
	public boolean isUrg()
	{
		if (_isReadAllFlags == false) getAllFlags();

		return (myFlags & TCP_URG_MASK) != 0;
	}
	
	/**
	 * set the urgent flag
	 * @param value
	 */
	public void setUrg(boolean value)
	{
		_isReadAllFlags = true;
		myFlags = (value)?(myFlags | TCP_URG_MASK):(myFlags & (~TCP_URG_MASK)); 
	}

	/** 
	 * Check the ACK flag, flag indicates if the ack number is set.
	 */
	public boolean isAck()
	{
		if (_isReadAllFlags == false) getAllFlags();
		return (myFlags & TCP_ACK_MASK) != 0;
	}
	
	/**
	 * set the ack flag state.
	 * @param value - true or false
	 */
	public void setAck(boolean value)
	{
		_isReadAllFlags = true;
		myFlags = (value)?(myFlags | TCP_ACK_MASK):(myFlags & (~TCP_ACK_MASK));
	}

	/** 
	 * Check the PSH flag, flag indicates the receiver should pass the
	 * data to the application as soon as possible.<br>
	 * (not really in use)
	 */
	public boolean isPsh()
	{
		if (_isReadAllFlags == false) getAllFlags();
		return (myFlags & TCP_PSH_MASK) != 0;
	}
	
	/**
	 * set the push flag state
	 * @param value - true or false.
	 */
	public void setPsh(boolean value)
	{
		_isReadAllFlags = true;
		myFlags = (value)?(myFlags | TCP_PSH_MASK):(myFlags & (~TCP_PSH_MASK));
	}

	/** 
	 * Check the RST flag, flag indicates the session should be reset between
	 * the sender and the receiver.
	 */
	public boolean isRst()
	{
		if (_isReadAllFlags == false)
		{
			getAllFlags();
		}
		return (myFlags & TCP_RST_MASK) != 0;
	}
	
	/**
	 * set the reset flag state.
	 * @param value - true or false
	 */
	public void setRst(boolean value)
	{
		_isReadAllFlags = true;
		myFlags = (value)?(myFlags | TCP_RST_MASK):(myFlags & (~TCP_RST_MASK));
	}

	/** 
	 * Check the SYN flag, flag indicates the sequence numbers should
	 * be synchronized between the sender and receiver to initiate
	 * a connection.,br>
	 */
	public boolean isSyn()
	{
		if (_isReadAllFlags == false)
		{
			getAllFlags();
		}

		return (myFlags & TCP_SYN_MASK) != 0;
	}
	
	/**
	 * set the tcp syn flag state.
	 * @param value
	 */
	public void setSyn(boolean value)
	{
		_isReadAllFlags = true;
		myFlags = (value)?(myFlags | TCP_SYN_MASK):(myFlags & (~TCP_SYN_MASK));
	}

	/** 
	 * @return the FIN flag, flag indicates the sender is finished sending.
	 */
	public boolean isFin()
	{
		if (_isReadAllFlags == false) getAllFlags();

		return (myFlags & TCP_FIN_MASK) != 0;
	}
	
	/**
	 * Set the fin flag state.
	 * @param value - true or false
	 */
	public void setFin(boolean value)
	{
		_isReadAllFlags = true;
		myFlags = (value)?(myFlags | TCP_FIN_MASK):(myFlags & (~TCP_FIN_MASK));
	}

	private byte[] _tcpHeaderBytes = null;

	/**
	 * @return the TCP header as a byte array.
	 */
	public byte[] getTCPHeader()
	{
		if (_tcpHeaderBytes == null && _isSniffedPkt)
		{
			_tcpHeaderBytes = ByteUtils.extractBytesArray(myPacket, _tcpOffset, myTcpHdrLh);
		}
		return _tcpHeaderBytes;
	}

	private byte[] _tcpDataBytes = null;

	/** 
	 * @return the TCP data as a byte array.
	 */
	public byte[] getTCPData()
	{
		if (_tcpDataBytes == null && _isSniffedPkt)
		{
			_tcpDataBytes = ByteUtils.extractBytesArray(myPacket, _tcpOffset + myTcpHdrLh, myPayloadDataLength);
		}
		return _tcpDataBytes;
	}
	
	

	/**
	 * will put defaults on all fields that were not set.<br>
	 * mandatory fields:<br>
	 * <br>
	 * src ip and src port.<br>
	 * dst ip and dst port.<br>
	 * payload <br>
	 * 
	 * @Override
	 */
	public void atuoComplete()
	{
		if (_isWriteSequenceNum == false)
			mySequenceNumber = getRandInt();
		
		if (_isWriteWindowSize == false)
			setWindowSize(DEFAULT_WINDOW_SIZE);
		
		super.atuoCompleteNoData();
		
	}

	/**
	 * check if all mandatory fields are filled.<br>
	 *<br>
	 * mandatory fileds:<br>
	 * 1. src ip and src port.<br>
	 * 2. dst ip and dst port.<br>
	 * 3. data <br>
	 */
	public boolean isMandatoryFieldsSet()
	{
		if (super.isMandatoryFieldsSetNoData() == false)
			return false;
		
		if (_isWriteSrcPort == false || _isWriteDstIp == false)
			return false;
		 
		return true;
	}

	/**
	 * set the payload.
	 * @param theData - byte array of the payload, mustn't be longer then the 
	 *  maximum allowed tcp payload.
	 */
	public void setData(byte[] theData)
	{
		if (theData == null)
			return;
		 _tcpDataBytes = theData;
		 myTotalTcpLength = getTCPHeaderLength()+theData.length;
		 setTotaIPlLength(myTotalTcpLength+getIPHeaderLength());
		
	}
	
	/**
	 * 
	 * @return the total tcp part length.
	 */
	public int getTotalTCPPlength()
	{
		return myTotalTcpLength;
	}

	/**
	 * @return the packet as a readable string.
	 */
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		buf.append("TCP : src ip[");
		buf.append(IP.getIPAsString(getSourceIP()));
		buf.append(":" + getSourcePort());
		buf.append("] , dst ip [");
		buf.append(IP.getIPAsString(getDestinationIP()) );
		buf.append(":" + getDestinationPort() + "]" );
		buf.append( new ByteUtils().getAsString(getIPData()));

		return buf.toString();
	}
}
