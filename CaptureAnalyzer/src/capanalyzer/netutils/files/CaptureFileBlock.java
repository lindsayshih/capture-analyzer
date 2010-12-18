package capanalyzer.netutils.files;

public interface CaptureFileBlock
{

	/**
	 * 
	 * @return the packet ErfPacketHeader
	 */
	public abstract CaptureFilePacketHeader getMyPktHdr();

	/**
	 * 
	 * @return the packet as byte array.
	 */
	public abstract byte[] getMyData();

}