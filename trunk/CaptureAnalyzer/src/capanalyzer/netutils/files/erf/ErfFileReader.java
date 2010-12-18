package capanalyzer.netutils.files.erf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import capanalyzer.netutils.files.CaptureFileBlock;
import capanalyzer.netutils.files.CaptureFileReader;


/**
 * class for reading erf capture files and returning the packets raw bytes.
 * 
 * @see CaptureFileReader
 * @author roni bar-yanai
 */
public class ErfFileReader implements CaptureFileReader
{
	private static final int MAX_PACKET_SIZE = 65356;
	
	private String myFileName = null;

	private InputStream myInStrm = null;
	
	// holds last read packet libcap pkt hdr;
	private ErfPacketHeader myPHDR = null;

	// used to be empty constructor
	// used by the static method for backward computability
	private ErfFileReader()
	{}

	/**
	 * open cap file
	 * @param theFileName
	 * @throws IOException
	 */
	public ErfFileReader(String theFileName) throws IOException
	{
		myFileName = theFileName;
		initStream(theFileName);
	}

	/**
	 * init input stream according to file name.
	 * read the cap file header so will be ready to read next packet.
	 * @param theFileName
	 * @throws IOException
	 */
	private void initStream(String theFileName) throws IOException
	{
		myInStrm = new FileInputStream(new File(theFileName));
	}
	
	/**
	 * return the next packet in the files.
	 * @param in
	 * @return array of bytes.
	 * @throws IOException
	 */
	protected byte[] readNextPacket(InputStream in) throws IOException
	{
		myPHDR = new ErfPacketHeader();
		myPHDR = myPHDR.readNextPcktHeader(in);

		if (myPHDR != null)
		{
			if (myPHDR.pktWlen16Uint > MAX_PACKET_SIZE)
				throw new IOException("Corrupted file !!! illegal packet size : "+myPHDR.pktWlen16Uint);
			
			byte[] toReturn = new byte[(int) myPHDR.pktWlen16Uint];
			if (in.read(toReturn) != toReturn.length)
			{
				throw new IOException("Corrputed file!!!");
			}
			in.skip(myPHDR.pktRlen16Uint - myPHDR.pktWlen16Uint - ErfPacketHeader.HEADER_SIZE);
			return toReturn;
		}
		return null;
	}

	/**
	 * return the cap file raw data as array of packets when each packet is
	 * byte array itself.
	 * @param fileName
	 * @return byte[][]
	 * @throws ErfFileException
	 * @throws IOException
	 */
	private byte[][] readAllCapRawData(String fileName) throws ErfFileException
	{
		InputStream in = null;
		try
		{
			in = new FileInputStream(new File(fileName));
			ArrayList<byte[]> tmp = new ArrayList<byte[]>();
			byte[] pkt = null;
			while ((pkt = readNextPacket(in)) != null)
			{
				tmp.add(pkt);
			}
			return (byte[][]) tmp.toArray(new byte[][] { {} });
		}
		catch (Exception ex)
		{
			throw new ErfFileException(ex.toString());
		}
		finally
		{
			if (in != null) try
			{
				in.close();
			}
			catch (IOException e)
			{

			}
		}
	}

	/**
	 * will return only the PcapPktHeaders
	 * @param fileName
	 * @return array of packets headers.
	 * @throws IOException
	 */
	protected ErfPacketHeader[] getAllPktsHeaders(String fileName) throws IOException
	{
		InputStream in = null;
		try
		{
			in = new FileInputStream(new File(fileName));
			ArrayList<ErfPacketHeader> tmp = new ArrayList<ErfPacketHeader>();
			ErfPacketHeader ph = new ErfPacketHeader();
			
			while ((ph = ph.readNextPcktHeader(in)) != null)
			{
				tmp.add(ph);
				in.skip(ph.pktWlen16Uint + (ph.pktRlen16Uint - ph.pktWlen16Uint - ErfPacketHeader.HEADER_SIZE));
				ph = new ErfPacketHeader();
			}
			return (ErfPacketHeader[]) tmp.toArray(new ErfPacketHeader[] {});
		}
		finally
		{
			if (in != null) in.close();
		}
	}

	/**
	 * @return the next packet in cap file. will return null if no more packets.
	 * @throws IOException
	 */
	public byte[] ReadNextPacket() throws IOException
	{
		if (myInStrm == null)
		{
			initStream(myFileName);
		}
		return readNextPacket(myInStrm);
	}
	
	/**
	 * 
	 * @return next block, return null on end of file
	 * @throws IOException
	 */
	public CaptureFileBlock readNextBlock() throws IOException
	{
		byte[] nextpkt = ReadNextPacket();
		if (nextpkt == null)
		{
			return null;
		}
		return new ErfBlock(myPHDR,nextpkt);
	}

	/**
	 * close the file.
	 *
	 */
	public void close()
	{
		if (myInStrm != null)
		{
			try
			{
				myInStrm.close();
			}
			catch (IOException e)
			{}
			myInStrm = null;
		}
	}
	
	/**
	 * make sure file is closed.
	 */
	protected void finalize() throws Throwable
	{
		close();
	}

	/**
	 * for switching big/small indian
	 * @param num
	 * @return 
	 */
	protected static long pcapflip32(long num)
	{
		long tmp = num;
		tmp = ((tmp & 0x000000FF) << 24) + ((tmp & 0x0000FF00) << 8) + ((tmp & 0x00FF0000) >> 8) + ((tmp & 0xFF000000) >> 24);

		return tmp;
	}

	/**
	 * for switching big/small indian
	 * @param num
	 * @return
	 */
	protected static int pcapflip16(int num)
	{
		int tmp = num;

		tmp = ((tmp & 0x00FF) << 8) + ((tmp & 0xFF00) >> 8);
		return tmp;
	}

	/**
	 * @param fileName
	 * @return all cap data as byte[][] array of byte arrays.
	 * each byte array is a packet in the cap file (udp,tcp...etc)
	 * @throws ErfFileException
	 */
	public static byte[][] readCapRawData(String fileName) throws ErfFileException
	{
		ErfFileReader rd = new ErfFileReader();
		return rd.readAllCapRawData(fileName);
	}
	
	/**
	 * will return only the PcapPktHeaders
	 * @param fileName
	 * @return array of packets headers.
	 * @throws IOException
	 */
	public static ErfPacketHeader[] getPktsHeaders(String fileName) throws IOException
	{
		ErfFileReader rd = new ErfFileReader();
		return rd.getAllPktsHeaders(fileName);
	}
}
