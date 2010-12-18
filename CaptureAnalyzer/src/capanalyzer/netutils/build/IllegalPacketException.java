package capanalyzer.netutils.build;

/**
 * Exception thrown when user try to build illegal packet.<br>
 *
 * @author roni bar-yanai
 *
 */
class IllegalPacketException extends RuntimeException
{
	public IllegalPacketException(String message)
	{
		super(message);
	}
}
