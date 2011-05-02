package capanalyzer.netutils.patternFinder;

/**
 * The class enable to crate large strings in efficient way.
 * when creating big strings using the str=str + "...." a new location in
 * memory is allocated when both of the strigs are being copyed. when using large
 * strings this causes a real overhead.
 * The class locate a big chucnk of memory and copy each string to the end of it.
 * this saves most of copys because usually the added string is small. 
 * @author ronyb
 */
public class BigString
{

	private final static int DEFUALT_CHARS = 1000;

	char[] charsArray = null;

	int index = 0;

	public BigString()
	{
		charsArray = new char[DEFUALT_CHARS];
	}

	public BigString(int size)
	{
		charsArray = new char[size];
	}

	public void addString(String theString)
	{

		char[] theStringAsChars = theString.toCharArray();

		while (index + theStringAsChars.length > charsArray.length)
		{
			doubleArray();
		}

		System.arraycopy(theStringAsChars, 0, charsArray, index, theStringAsChars.length);
		index += theStringAsChars.length;

	}
	
	
	public void addChar(char c)
	{
		if (index>= charsArray.length)
			doubleArray();
		charsArray[index++] = c;
	}

	private void doubleArray()
	{

		char[] tmp = new char[charsArray.length * 2];

		for (int i = 0; i < charsArray.length; i++)
		{
			tmp[i] = charsArray[i];
		}

		charsArray = tmp;
	}

	public String getAsString()
	{
		if (index == 0)
			return "";
		else
			return new String(charsArray, 0, index - 1);
	}

	public String toString()
	{
		return getAsString();
	}

}
