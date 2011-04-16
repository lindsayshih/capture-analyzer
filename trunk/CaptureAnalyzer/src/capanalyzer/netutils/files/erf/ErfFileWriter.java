package capanalyzer.netutils.files.erf;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import capanalyzer.netutils.files.CaptureFileWriter;

/**
 * Class for creating capture files in erf format.<br>
 * 
 * @author roni bar yanai
 *
 */
public class ErfFileWriter implements CaptureFileWriter
{
	private static final int MAX_PACKET_SIZE = 65356;

	// the out stream
	private FileOutputStream myOutStrm = null;
	
	private BufferedOutputStream myBufferedOutStrm = null;

	private boolean _isopened = false;

	// used to calculate the packets time.
	private long myStartTime = 0;

	// total ~bytes written so far.
	private long myTotalBytes = 0;

	/**
	 * open new file
	 * @param file
	 * @throws IOException - on file creation failure.
	 */
	public ErfFileWriter(File file) throws IOException
	{
		this(file, false);
	}
	
	/**
	 * open new file
	 * @param file - the file name
	 * @throws IOException - on file creation failure.
	 */
	public ErfFileWriter(String file) throws IOException
	{
		this(new File(file), false);
	}

	/**
	 * open new file
	 * @param file
	 * @param append 
	 * @throws IOException - on file creation failure.
	 */
	public ErfFileWriter(File file, boolean append) throws IOException
	{
		if (file == null) throw new IllegalArgumentException("Got null file object");

		init(file, append);
		myStartTime = getNanoTime();
	}

	/**
	 * 
	 * @return time stamp in nano seconds
	 */
	private long getNanoTime()
	{	
		return System.nanoTime();
	}

	/**
	 * open the out stream and write the cap header.
	 * @param file
	 * @throws IOException
	 */
	private void init(File file, boolean append) throws IOException
	{
		myOutStrm = new FileOutputStream(file, append);
		myBufferedOutStrm = new BufferedOutputStream(myOutStrm);

		_isopened = true;
	}
	
	/**
	 * add packet to already opened cap.
	 * if close method was called earlier then will not add it.
	 * @param thepkt
	 * @param time - time offset in micro sec 
	 * @return true if packet added and false otherwise
	 * @throws IOException 
	 * @throws IOException
	 */
	public boolean addPacket(byte[] thepkt,long time) throws IOException
	{

		if (thepkt == null || !_isopened) return false;

		ErfPacketHeader hder = new ErfPacketHeader();

		hder.setTimeValMsec32Uint((time ) % 1000000);
		hder.setTimeValSec32Uint(time / 1000000l);
		
		/////////************** TBD - ADD THE REST OF THE HEADER'S FIELDS ******************///////////////////

		if (thepkt.length > MAX_PACKET_SIZE)
			throw new IOException("Got illeagl packet size : "+thepkt.length);
		
		myBufferedOutStrm.write(hder.getAsByteArray());
		myBufferedOutStrm.write(thepkt);

		myTotalBytes += thepkt.length + ErfPacketHeader.HEADER_SIZE;

		return true;
	
		
	}

	/**
	 * add packet to already opened cap.
	 * if close method was called earlier then will not add it.
	 * @param thepkt
	 * @return true if packet added and false otherwise
	 * @throws IOException
	 */
	public boolean addPacket(byte[] thepkt) throws IOException
	{
		if (thepkt == null || !_isopened) return false;

		ErfPacketHeader hder = new ErfPacketHeader();

		long gap = getNanoTime() - myStartTime; // the gap since start in nano sec

		hder.setTimeValMsec32Uint((gap / 1000) % 1000000);
		hder.setTimeValSec32Uint(gap / 1000000000l);

		/////////************** TBD - ADD THE REST OF THE HEADER'S FIELDS ******************///////////////////

		if (thepkt.length > MAX_PACKET_SIZE)
			throw new IOException("Got illeagl packet size : "+thepkt.length);
		
		myBufferedOutStrm.write(hder.getAsByteArray());
		myBufferedOutStrm.write(thepkt);

		myTotalBytes += thepkt.length + ErfPacketHeader.HEADER_SIZE;

		return true;
	}

	
	/**
	 * add packet to already opened cap.
	 * if close method was called earlier then will not add it.
	 * @param theHder
	 * @param thepkt
	 * @return true if packet added and false otherwise
	 * @throws IOException
	 */
	public boolean addPacket(ErfPacketHeader theHder, byte[] thepkt) throws IOException
	{
		if (thepkt == null || !_isopened) return false;

		if (thepkt.length > MAX_PACKET_SIZE)
			throw new IOException("Got illeagl packet size : "+thepkt.length);
		
		byte[] padding = new byte[theHder.pktRlen16Uint - theHder.pktWlen16Uint - ErfPacketHeader.HEADER_SIZE];
		for (int i = 0; i < padding.length; i++)
		{
			padding[i] = 0;
		}
		
		myBufferedOutStrm.write(theHder.getTheHeaderByteArray());
		myBufferedOutStrm.write(thepkt);
		myBufferedOutStrm.write(padding);

		myTotalBytes += thepkt.length + ErfPacketHeader.HEADER_SIZE + padding.length;

		return true;
	}
	
	/**
	 * close file.
	 * not reversible
	 * @throws IOException
	 */
	public void close() throws IOException
	{
		if (_isopened && myOutStrm != null)
		{
			myBufferedOutStrm.close();
			myOutStrm.close();
			_isopened = false;
			myBufferedOutStrm = null;
			myOutStrm = null;
		}
	}

	/**
	 * @return number of bytes written so far.
	 */
	public long getTotalBytes()
	{
		return myTotalBytes;
	}
}
