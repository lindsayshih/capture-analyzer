/*
 * Created on 17/10/2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package capanalyzer.netutils.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;



/**
 * Simple class for getting options from args.<br>
 * for examle "-p 9000 -ip 10.1.11.25 -s -l"<br>
 * The class implements methods for getting the values for options or find if<br>
 * they exists...etc<br>
 *   <br>
 * Example:<br> 
 * <code> 
 * public static void main(String []args)<br>
 * {<br>
 *      LineArgs largs = new LineArgs();<br>
 *<br>
 *		largs.addArg("-ip",1,"[the ip] - the listening ip, mandatory");<br>
 *		largs.addArg("-p",1,"[the port] - the listening port, mandatory");<br>
 *		largs.addArg("-t",1,"[number of threads] - the number of threads used, optional (default=1)",new String[]{"1"});<br>
 *		largs.addArg("-s",0,"no parameters - show statistics to string, optional");<br>
 *		<br>
 *		largs.init(args);<br>
 *		if ( !largs.hasOption("-ip") || !largs.hasOption("-p"))<br>
 *		{<br>
 *			System.out.println("Missing parmeters ip or port");<br>
 *			System.out.println(largs.toString());<br>
 *  			System.exit(-1);<br>
 *		}<br>
 *		<br>
 *		<br>
 *		GenericTCPServer g = new GenericTCPServer(largs.getArgAsString("-ip"),largs.getArgAsInt("-p"));<br>
 *		g.setMyNumOfHandlers(largs.getArgAsInt("-t"));<br>
 *		g.startServer();<br>
 *		if (largs.hasOption("-s"))<br>
 *		while(true)<br>
 *		{<br>
 *			Thread.sleep(10000);<br>
 *			System.out.println(g.getStatistics());<br>
 *		}<br>
 *}<br>
 *	</code>	
 * @author ronyb
 */
public class LineArgs
{
	private HashMap myArgs = new HashMap();

	private String myFailReason = "";

	private int myLastInitParametersNum = 0;

	/**
	 * parse the string. after the init called the option can be read.
	 * @param fileds the args from the main method.
	 * @param argsCaseSensitive if true the args will be checked case sensitive.
	 * @return true on success and false otherwise.
	 */
	public boolean init(String fileds[],boolean argsCaseSensitive)
	{

		int idx = 0;

		try
		{
			while (idx < fileds.length)
			{
				String key = fileds[idx];
				if (!argsCaseSensitive)
					key = key.toLowerCase();
				if (fileds[idx].indexOf("-") != -1 && myArgs.containsKey(key))
				{
					if (!argsCaseSensitive)
						fileds[idx] = fileds[idx].toLowerCase();
					Parameter p = (Parameter) myArgs.get(fileds[idx++]);

					if (p.numberOfParameters == Parameter.DYNAMIC)
					{
						ArrayList arrlist = new ArrayList();
						while (idx < fileds.length && !fileds[idx].startsWith("-"))
						{
							arrlist.add(fileds[idx]);
							idx++;
						}
						if (arrlist.size() != 0) idx--;
						p.setValues((String[]) arrlist.toArray(new String[0]));
					}
					else
					{
						String values[] = new String[p.numberOfParameters];
						for (int i = 0; i < p.numberOfParameters; i++, idx++)
						{
							values[i] = fileds[idx];
						}
						p.setValues(values);
					}
					myLastInitParametersNum++;
				}
				else
				{
					idx++;
				}
			}
		}
		catch (Exception e)
		{
			return false;
		}
		return true;
	}
	
	public boolean init(String fileds[])
	{
		return init(fileds,true);		
	}

	/**
	 * parse the string. after the init called the option can be read.
	 * @param line - the string to be parsed
	 * @return true on init success and false otherwise.
	 */
	public boolean init(String line)
	{
		String[] fileds = line.split(" ");
		return init(fileds);
	}

	/**
	 * Add arg .
	 * @param theSign - the arg sign for example -ip
	 * @param theNumberOfParameters - number of parameters followed
	 * @param theDescription - the arg description for example " [the ip] - the listening ip"
	 */
	public void addArg(String theSign, int theNumberOfParameters, String theDescription)
	{
		Parameter p = new Parameter(theSign, theNumberOfParameters, theDescription);
		myArgs.put(theSign, p);
	}

	/**
	 * Add dynamic arg. the number of values will be all values until next "-" encountered.
	 * @param theSign
	 * @param theDescription
	 */
	public void addDynamicArg(String theSign, String theDescription)
	{
		Parameter p = new Parameter(theSign, Parameter.DYNAMIC, theDescription);
		myArgs.put(theSign, p);
	}

	/**
	 * Add arg.
	 * @param theSign - the arg sign for example -ip
	 * @param theNumberOfParameters - number of parameters followed
	 * @param theDescription - the arg description for example " [the ip] - the listening ip"
	 * @param theDefaultValue - the arg default value.
	 */
	public void addArg(String theSign, int theNumberOfParameters, String theDescription, String[] theDefaultValue)
	{
		Parameter p = new Parameter(theSign, theNumberOfParameters, theDescription, theDefaultValue);
		myArgs.put(theSign, p);
	}

	/**
	 * Get the arg value.
	 * if arg not exist then 0 will be returned.
	 * (use hasOption to check that value appeard in the line)
	 * @param theSign - the sign for example "-ip"
	 * @return the option
	 * may throw runtime exception if option don't exists.
	 */
	public int getArgAsInt(String theSign)
	{
		if (myArgs.containsKey(theSign))
		{
			return Integer.parseInt(((Parameter) myArgs.get(theSign)).getValues()[0]);
		}
		else
			throw new OptionNotExistsException();
	}

	/**
	 * Get the arg value.
	 * @param theSign
	 * @return the option as double.
	 * may throw runtime exception if option don't exists.
	 */
	public double getArgAsDouble(String theSign)
	{
		if (myArgs.containsKey(theSign))
		{
			return Double.parseDouble(((Parameter) myArgs.get(theSign)).getValues()[0]);
		}
		else
			throw new OptionNotExistsException();
	}

	/**
	 * return the parameter as a String.
	 * if parameter don't exists then empty string will be returned.
	 * @param theSign
	 * @return the value
	 * may throw runtime exception if option don't exists.
	 */
	public String getArgAsString(String theSign)
	{
		if (myArgs.containsKey(theSign))
		{
			return ((Parameter) myArgs.get(theSign)).getValues()[0];
		}
		throw new OptionNotExistsException();
	}
	
	public String getArgAsString(String theSign,int parameterIndex)
	{
		if (myArgs.containsKey(theSign))
		{
			return ((Parameter) myArgs.get(theSign)).getValues()[parameterIndex];
		}
		throw new OptionNotExistsException();
	}

	public boolean getArgAsBoolean(String theSign)
	{
		if (myArgs.containsKey(theSign))
		{
			return (((Parameter) myArgs.get(theSign)).getValues()[0].toLowerCase().indexOf("true") != -1);
		}
		throw new OptionNotExistsException();
	}

	/**
	 * @param theSign
	 * @return all values attached to the sign.
	 */
	public String[] getAllArgsValues(String theSign)
	{
		if (myArgs.containsKey(theSign))
		{
			return ((Parameter) myArgs.get(theSign)).getValues();
		}
		throw new OptionNotExistsException();
	}

	/**
	 * @param theSign
	 * @param thedefault
	 * @return the sign value or the defualt if not exists.
	 */
	public boolean getArgAsBoolean(String theSign, boolean thedefault)
	{
		if (!hasOption(theSign)) return thedefault;
		if (myArgs.containsKey(theSign))
		{
			return (((Parameter) myArgs.get(theSign)).getValues()[0].toLowerCase().indexOf("true") != -1);
		}
		return thedefault;
	}

	/**
	 * @param theSign
	 * @return true if option exists in the line and else otherwise.
	 */
	public boolean hasOption(String theSign)
	{
		if (myArgs.containsKey(theSign))
		{
			return ((Parameter) myArgs.get(theSign)).isConfigured();
		}
		return false;
	}
	
	public int getOptionNumOfParameters(String theSign)
	{
		if (myArgs.containsKey(theSign))
		{
			return ((Parameter) myArgs.get(theSign)).numberOfParameters;
		}
		return -1;
	}

	/**
	 * @return true if not known parameters were received in the line args and else otherwise.<br>
	 */
	public boolean isEmpty()
	{
		return (myLastInitParametersNum == 0);
	}

	/**
	 * Return all the posible prameters and thier description
	 */
	public String toString()
	{
		String result = "args:\n";
		for (Iterator it = myArgs.values().iterator(); it.hasNext();)
		{
			Parameter p = (Parameter) it.next();
			result = result + p.getDescription() + "\n";

		}
		return result;
	}
	
	public String[] allSigns()
	{
		String[] result = new String[myArgs.size()];
		int i = 0;
		for (Iterator it = myArgs.keySet().iterator(); it.hasNext();)
		{
			result[i] = (String)it.next();
			i++;
		}
		return result;
	}
}

class OptionNotExistsException extends RuntimeException
{
	public OptionNotExistsException()
	{
		super("Don't have that option");
	}

}

/**
 * Data structure for holding the possible args, thier descripation values..
 * @author ronyb
 *
 */
class Parameter
{
	public static final int DYNAMIC = -1;

	String name = null;

	String sign = null;

	int numberOfParameters = -2;

	String descripation = null;

	private String[] values = null;

	boolean isConfigured = false;

	/**
	 * @param sign
	 * @param numberOfParameters
	 * @param descripation
	 */
	public Parameter(String sign, int numberOfParameters, String descripation)
	{
		super();
		this.sign = sign;
		this.numberOfParameters = numberOfParameters;
		this.descripation = descripation;
	}

	/**
	 * @param sign
	 * @param numberOfParameters
	 * @param descripation
	 * @param values
	 */
	public Parameter(String sign, int numberOfParameters, String descripation, String[] values)
	{
		super();
		this.sign = sign;
		this.numberOfParameters = numberOfParameters;
		this.descripation = descripation;
		this.values = values;
	}

	public String getDescription()
	{
		return sign + " " + descripation;
	}

	public String[] getValues()
	{
		return values;
	}

	/**
	 * @param values
	 */
	public void setValues(String[] values)
	{
		this.values = values;
		isConfigured = true;
	}

	/**
	 * @return true if option was configured.
	 */
	public boolean isConfigured()
	{
		return isConfigured;
	}

	protected String getSign() {
		return sign;
	}
}