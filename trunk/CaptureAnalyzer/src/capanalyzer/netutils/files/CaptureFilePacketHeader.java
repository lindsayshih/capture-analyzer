package capanalyzer.netutils.files;

public interface CaptureFilePacketHeader
{

	/**
	 * @return the header as little indian.
	 */
	public abstract byte[] getAsByteArray();

	/**
	 *  (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public abstract String toString();

	/**
	 * 
	 * @return
	 */
	public abstract long getTime();

}