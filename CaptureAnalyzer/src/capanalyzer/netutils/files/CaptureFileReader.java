package capanalyzer.netutils.files;

import java.io.IOException;

/**
 * Interface for reading capture file.<br>
 * There are many formats for capture files which may require
 * different handling, but this difference is not relevant when reading
 * a capture file for analyzing it packets.<br>
 * The interface provides the required abstraction.<br>
 * 
 * 
 * @author roni bar yanai
 *
 */
public interface CaptureFileReader
{
	/**
	 * read next packet in file.
	 * @return next packet in file as a raw byte array, if no more packets are 
	 *    available then will return null.
	 * @throws IOException
	 */
	public byte[] readNextPacket() throws IOException;
	
	/**
	 * read next block in file.
	 * @return next block in file as an object with the header and raw byte array, 
	 * if no more blocks are available then will return null.
	 * @throws IOException
	 */
	public CaptureFileBlock readNextBlock() throws IOException;
	
	
	/**
	 * @return the bytesRead
	 */
	public long getBytesRead();
	
	/**
	 * @return the prevBytesRead
	 */
	public long getPrevBytesRead();
	
	/**
	 * @return the capFileSizeInBytes
	 */
	public long getCapFileSizeInBytes();
}
