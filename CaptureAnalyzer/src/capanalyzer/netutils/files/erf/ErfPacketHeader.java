package capanalyzer.netutils.files.erf;

import java.io.IOException;
import java.io.InputStream;

import capanalyzer.netutils.build.ByteUtils;
import capanalyzer.netutils.files.CaptureFilePacketHeader;


/**
 * erf packet structure:
 * ---------------------
 *  
 *   
 * 0               7 8               15 16               24 25                 32
 * ------------------------------------------------------------------------------
 * |    							timestamp					   		        |
 * ------------------------------------------------------------------------------
 * |                    		    timestamp				        	        |
 * ------------------------------------------------------------------------------
 * | 	  type      |      flags       |                 rlen  	  	            |
 * ------------------------------------------------------------------------------
 * |  			lctr/color			   | 				 wlen   				|
 * ------------------------------------------------------------------------------
 * |  16 bit tcp check sum             |    16 bit urgent pointer               |
 * ------------------------------------------------------------------------------
 * |     					    extension headers                               |
 * |      						   (optional)                                   |
 * -----------------------------------------------------------------------------
 * |																			|
 * |                			    Payload 									|
 * |																			|
 * ----------------------------------------------------------------------------
 *
 * All ERF records share some common fields. 
 * Timestamps are in little-endian (Pentium® native) byte order. 
 * All other fields are in big-endian (network) byte order. 
 * All payload data is captured as a byte stream in network order, no byte or re-ordering is applied
 *  
 * timestamp - The time of arrival of the cell, an ERF 64-bit timestamp.
 * type:
 *   	- Bit 7 - Extension header present.
 *  	- Bit 6:0 - Extension header type. See table below:
 * flags - This byte is divided into several fields as follows:
 *      Bits     Description
 * 		1-0:     Binary enumeration of capture interface:
 * 		2:		 Varying length record (vlen). When set, packets shorter than the snap length are not padded and rlen resembles wlen.
 *				 When clear, longer packets are snapped off at snap length and shorter packets are padded up to the snap length. 
 * 				 rlen resembles snap length.
 * 		3:		 Truncated record - insufficient buffer space.
 * 		4:		 RX error. An error in the received data. Present on the wire
 * 		5: 		 DS error. An internal error generated inside the card annotator. Not present on the wire.
 * 		6:		 Reserved
 * 		7:		 Reserved
 * rlen - Record length in bytes. Total length of the record transferred over the PCI bus to storage.
		  The timestamp of the next ERF record starts exactly rlen bytes after the start of the timestamp of the current ERF record.
 * lctr - Depending upon the ERF type this 16 bit field is either a loss counter of color field. 
 * 		  The loss counter records the number of packets lost between the DAG card and the stream buffer due to overloading on the PCI bus. 
 * 		  The loss is recorded between the current record and the previous record captured on the same stream/interface. 
 * 	      The color field is explained under the appropriate type details.
 * wlen - Wire length. Packet length "on the wire" including some protocol overhead. 
 * 		  The exact interpretation of this quantity depends on physical medium. This may contain padding.
 * extension headers - Extension headers in an ERF record allow extra data relating to each packet to be transported to the host. 
 * 					   Extension header/s are present if bit 7 of the type field is '1'. 
 * 					   If bit 7 is '0', no extension headers are present (ensures backwards compatibility). 
 * 					   Note: There can be more than one Extension header attached to a ERF record.
 * Payload - Payload is the actual data in the record. It can be calculated by either :
 *		 	 Payload = rlen - ERF header - Extension headers (optional) - Protocol header - Padding
 *  
 * @author roni bar-yanai 
 */

public class ErfPacketHeader implements CaptureFilePacketHeader
{
	public static final int HEADER_SIZE = 18;

	protected long timeValSec32Uint = 0;

	protected long timeValMsec32Uint = 0;

	protected int pktType16Uint = 0;

	protected int pktFlags16Uint = 0;

	protected int pktRlen16Uint = 0;

	protected int pktLctrColor16Uint = 0;

	protected int pktWlen16Uint = 0;

	protected int pktPadAndOffset16Uint = 0;

	protected byte[] myOriginalCopy = null;

	/**
	 * read header from in stream.
	 * 
	 * @param in
	 *            - the stream
	 * @return the header
	 * @throws IOException
	 */
	public ErfPacketHeader readNextPcktHeader(InputStream in) throws IOException
	{
		byte[] tmp = new byte[HEADER_SIZE];
		if (in.read(tmp) != tmp.length)
			return null;

		myOriginalCopy = tmp;

		timeValMsec32Uint = ByteUtils.getBytePenOrderTo_uint32(tmp, 0);
		timeValSec32Uint = ByteUtils.getBytePenOrderTo_uint32(tmp, 4);

		pktType16Uint = ByteUtils.getByteNetOrderTo_uint8(tmp, 8);
		pktFlags16Uint = ByteUtils.getByteNetOrderTo_uint8(tmp, 9);
		pktRlen16Uint = ByteUtils.getByteNetOrderTo_uint16(tmp, 10);
		pktLctrColor16Uint = ByteUtils.getByteNetOrderTo_uint16(tmp, 12);
		pktWlen16Uint = ByteUtils.getByteNetOrderTo_uint16(tmp, 14);
		pktPadAndOffset16Uint = ByteUtils.getByteNetOrderTo_uint16(tmp, 16);

		return this;
	}

	/**
	 * @return the header as little indian.
	 */
	public byte[] getAsByteArray()
	{
		byte[] tmp = new byte[HEADER_SIZE];

		ByteUtils.setLittleIndianInBytesArray(tmp, 0, pcapRead32(timeValSec32Uint), 4);
		ByteUtils.setLittleIndianInBytesArray(tmp, 4, pcapRead32(timeValMsec32Uint), 4);
		ByteUtils.setLittleIndianInBytesArray(tmp, 8, pcapRead16(pktType16Uint), 1);
		ByteUtils.setLittleIndianInBytesArray(tmp, 9, pcapRead16(pktFlags16Uint), 1);
		ByteUtils.setLittleIndianInBytesArray(tmp, 10, pcapRead16(pktRlen16Uint), 2);
		ByteUtils.setLittleIndianInBytesArray(tmp, 12, pcapRead16(pktLctrColor16Uint), 2);
		ByteUtils.setLittleIndianInBytesArray(tmp, 14, pcapRead16(pktWlen16Uint), 2);
		ByteUtils.setLittleIndianInBytesArray(tmp, 16, pcapRead16(pktPadAndOffset16Uint), 2);

		return tmp;
	}

	/**
	 * @return the header as read from the stream.
	 */
	protected byte[] getTheHeaderByteArray()
	{
		return myOriginalCopy;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "Time Sec: " + timeValSec32Uint + "\n" + 
				"Time MSec : " + timeValMsec32Uint + "\n" + 
				"PKT Type : " + pktType16Uint + "\n" + 
				"PKT Flags : " + pktFlags16Uint + "\n" + 
				"PKT RLen : " + pktRlen16Uint + "\n" + 
				"PKT LCTR/Color : " + pktLctrColor16Uint + "\n" + 
				"PKT WLen : " + pktWlen16Uint + "\n" + 
				"PKT pad and offset : "	+ pktPadAndOffset16Uint + "\n";
	}

	/**
	 * The time in microsec.
	 * 
	 * @param theTimeValMsec32Uint
	 */
	public void setTimeValMsec32Uint(long theTimeValMsec32Uint)
	{
		timeValMsec32Uint = theTimeValMsec32Uint;
	}

	/**
	 * the time in sec.
	 * 
	 * @param theTimeValSec32Uint
	 */
	public void setTimeValSec32Uint(long theTimeValSec32Uint)
	{
		timeValSec32Uint = theTimeValSec32Uint;
	}

	/**
	 * @return the pktType16Uint
	 */
	public int getPktType16Uint()
	{
		return pktType16Uint;
	}

	/**
	 * @param pktType16Uint
	 *            the pktType16Uint to set
	 */
	public void setPktType16Uint(int pktType16Uint)
	{
		this.pktType16Uint = pktType16Uint;
	}

	/**
	 * @return the pktFlags16Uint
	 */
	public int getPktFlags16Uint()
	{
		return pktFlags16Uint;
	}

	/**
	 * @param pktFlags16Uint
	 *            the pktFlags16Uint to set
	 */
	public void setPktFlags16Uint(int pktFlags16Uint)
	{
		this.pktFlags16Uint = pktFlags16Uint;
	}

	/**
	 * @return the pktRlen16Uint
	 */
	public int getPktRlen16Uint()
	{
		return pktRlen16Uint;
	}

	/**
	 * @param pktRlen16Uint
	 *            the pktRlen16Uint to set
	 */
	public void setPktRlen16Uint(int pktRlen16Uint)
	{
		this.pktRlen16Uint = pktRlen16Uint;
	}

	/**
	 * @return the pktLctrColor16Uint
	 */
	public int getPktLctrColor16Uint()
	{
		return pktLctrColor16Uint;
	}

	/**
	 * @param pktLctrColor16Uint
	 *            the pktLctrColor16Uint to set
	 */
	public void setPktLctrColor16Uint(int pktLctrColor16Uint)
	{
		this.pktLctrColor16Uint = pktLctrColor16Uint;
	}

	/**
	 * @return the pktWlen16Uint
	 */
	public int getPktWlen16Uint()
	{
		return pktWlen16Uint;
	}

	/**
	 * @param pktWlen16Uint
	 *            the pktWlen16Uint to set
	 */
	public void setPktWlen16Uint(int pktWlen16Uint)
	{
		this.pktWlen16Uint = pktWlen16Uint;
	}

	/**
	 * @return the pktPadAndOffset16Uint
	 */
	public int getPktPadAndOffset16Uint()
	{
		return pktPadAndOffset16Uint;
	}

	/**
	 * @param pktPadAndOffset16Uint
	 *            the pktPadAndOffset16Uint to set
	 */
	public void setPktPadAndOffset16Uint(int pktPadAndOffset16Uint)
	{
		this.pktPadAndOffset16Uint = pktPadAndOffset16Uint;
	}

	/**
	 * 
	 * @return
	 */
	public long getTimeValMsec32Uint()
	{
		return timeValMsec32Uint;
	}

	/**
	 * 
	 * @return
	 */
	public long getTimeValSec32Uint()
	{
		return timeValSec32Uint;
	}

	/**
	 * 
	 * @return
	 */
	public long getTime()
	{
		return timeValSec32Uint * 1000000 + timeValMsec32Uint;
	}

	private long pcapRead32(long num)
	{
		long tmp = num;
		tmp = ((tmp & 0x000000FF) << 24) + ((tmp & 0x0000FF00) << 8) + ((tmp & 0x00FF0000) >> 8) + ((tmp & 0xFF000000) >> 24);
		return tmp;
	}

	private int pcapRead16(int num)
	{
		int tmp = num;
		tmp = ((tmp & 0x00FF) << 8) + ((tmp & 0xFF00) >> 8);
		return tmp;
	}
}
