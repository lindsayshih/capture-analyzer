package capanalyzer.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

/**
 *  The class impelement an INI file api.
 *  getting and setting Ini File Parameters.
 * 
 *  comments start with #.
 *  blank will be ignored 
 *  All values before first section will be ignored.
 * 
 *  @author ronyb
 */
public class IniFile
{
	protected File myFile = null;

	protected LinkedList<IniSection> mySectionsList = new LinkedList<IniSection>();
	protected HashMap<String, IniSection> mySectionsHash = new HashMap<String, IniSection>();
	//protected LinkedList myComments = new LinkedList();
	
	protected IniFile()
	{}

	/**
	 * Create ne ini file instance.
	 * 
	 * @param theFileNameIncludingPath - the file name
	 * @param createIfNotExists - it true then will not thorw exception if file don't exists.
	 * @throws FileNotFoundException
	 */
	public IniFile(String theFileNameIncludingPath, boolean createIfNotExists)
			throws FileNotFoundException
	{
		myFile = new File(theFileNameIncludingPath);
		if (!myFile.exists() && !createIfNotExists)
		{
			throw new FileNotFoundException(theFileNameIncludingPath);
		}
		else if (myFile.exists())

		loadFile();
	}
	
	/**
	 * @return the file name.
	 */
	public String getFile()
	{
		return myFile.toString().trim();
	}
	
	/**
	 * Returns the file name without the full path.
	 * @return
	 */
	public String getFileShortName()
	{
		return myFile.getName();
	}

	/**
	 * perform parsing on the file and keep all inforamtion in memory.
	 * ( Ini is not too large to keep in memory)
	 */
	protected void loadFile()
	{
		BufferedReader myBufferedReader = null;
		try // open file
		{
			myBufferedReader = new BufferedReader(new FileReader(myFile));
		}
		catch (FileNotFoundException cantHappen)
		{}

		String line = "";
		IniSection currentSection = null;

		try
		{
			while ((line = myBufferedReader.readLine()) != null)
			{
				if (line.startsWith("[")) // new section
				{
					line = line.replace('[', ' ');
					line = line.replace(']', ' ');
					line = line.trim();
					currentSection = new IniSection(line);
					mySectionsHash.put(line, currentSection);
					mySectionsList.addLast(currentSection);
					continue;
				}

				if (currentSection == null) // for remarks before the first section
				{
					currentSection = new IniSection(null);
					mySectionsList.addLast(currentSection);
				}

                // ignore line that are not parameter(*=*,remark or blank)
				// lines that that are not blank or remark and have parameter without =
				// the parameters will also be the value.
				try 
				{
					
					String[] rm = line.split("=");
					if (rm.length>2) // if '=' appears more then once
					{
						String s = "";
						for (int i=1 ; i<rm.length ; i++)
						{
							if (i!= 1)
							{
								s=s+"=";
							}
							s=s+rm[i].toString();
						}
						currentSection.addParameter(rm[0].toString(), s);
					}
					else if (rm.length == 2)
					{
						currentSection.addParameter(rm[0].toString(), rm[1]
								.toString());
					}
					else if (rm.length == 1 && line.indexOf("=") != -1)
					{
						currentSection.addParameter(rm[0].toString(), "");
					}
					else if (rm.length == 1 && !line.trim().equals(""))
					{
						currentSection.addParameter(rm[0].toString());
					}
					else if (line.startsWith("#") || line.trim().equals(""))
					{
						currentSection.addComment(line);
					}
					else
					{
						currentSection.addParameter(line, line);
					}
				}
				catch (Exception e1)
				{
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Return the wanted key value.
	 * Will return null if key doesn't exists.
	 * 
	 * @param theSection
	 * @param theKey
	 * @return the value or null
	 */
	public String get(String theSection, String theKey)
	{
		IniSection tmp = (IniSection) mySectionsHash.get(theSection);
		if (tmp == null)
			return null;
		
		return tmp.getParameter(theKey);
	}
	
	/**
	 * Return the all parameters in section as string array
	 * @param theSection - the section name
	 * @return string array of the parameters or empty array if none or no such section.
	 */
	public String[] getAllSectionParameters(String theSection)
	{
		IniSection tmp = (IniSection) mySectionsHash.get(theSection);
		if (tmp == null)
			return new String[]{};
		
			return tmp.getAllParameters();
	}
	
	/**
	 * Check is parameters exists in the ini file.
	 * @param theSection 
	 * @param theKey
	 * @return true if exists and false otherwise.
	 */
	public boolean containsParameter(String theSection,String theKey)
	{
		IniSection tmp = (IniSection) mySectionsHash.get(theSection);
		if (tmp == null || tmp.getParameter(theKey) == null)
			return false;
  	    return true; 
	}
	
	/**
	 * Remove the parameter if exists. if parameter not exists then nothing
	 * will be changed.
	 * @param theSection
	 * @param theKey
	 */
	public void removeParameter(String theSection,String theKey)
	{
		IniSection tmp = (IniSection) mySectionsHash.get(theSection);
		if (tmp != null)
		{
			tmp.removeParameter(theKey);
		}
		flushToDisk();
	}
	
	/**
	 * remove all parameters from section
	 * @param theSection
	 */
	public void removeSectionParameters(String theSection)
	{
		IniSection tmp = (IniSection) mySectionsHash.get(theSection);
		if (tmp != null)
		{
			String[] parameters = tmp.getAllParameters();
			for (int i=0;i<parameters.length;i++)
			{
				tmp.removeParameter(parameters[i]);
			}
		}
		flushToDisk();
	}
	/**
	 * remove  section
	 * @param theSection
	 */
	public void removeSection(String theSection)
	{
		IniSection tmp = (IniSection) mySectionsHash.get(theSection);
		if (tmp != null)
		{
			String[] parameters = tmp.getAllParameters();
			for (int i=0;i<parameters.length;i++)
			{
				tmp.removeParameter(parameters[i]);
			}
		}		
		mySectionsList.remove(tmp);
		mySectionsHash.remove(theSection);
		flushToDisk();
	}
	
	/**
	 * Get the key value or the defualt if not exists.
	 * If the key doesn't exists then add to the ini file.
	 * 
	 * @param theSection
	 * @param theKey
	 * @param theDefault
	 * @return the value as a string.
	 */
	public String get(String theSection, String theKey, String theDefault)
	{
		IniSection tmp = (IniSection) mySectionsHash.get(theSection);

		if (tmp == null)
		{
			tmp = new IniSection(theSection);
			tmp.addParameter(theKey, theDefault);
			mySectionsHash.put(theSection, tmp);
			mySectionsList.addLast(tmp);
			flushToDisk();
			return theDefault;
		}
		return tmp.getParameter(theKey, theDefault);
	}
	
	
    /**
     * Set the key value at the wanted section.
     * if key allready exists then it's value will be changed and if not then
     * it will be added.
     * 
     * @param theSection
     * @param theKey
     * @param theValue
     */
	public void set(String theSection, String theKey, String theValue)
	{
		IniSection tmp = (IniSection) mySectionsHash.get(theSection);
		if (tmp == null)
		{
			tmp = new IniSection(theSection);
			mySectionsHash.put(theSection, tmp);
			mySectionsList.addLast(tmp);
		}
		tmp.addParameter(theKey, theValue);
		flushToDisk();
	}
	
	/**
	 * Set the key at the section.
	 * 
	 * @param theSection
	 * @param theKey
	 */
	public void set(String theSection, String theKey)
	{
		IniSection tmp = (IniSection) mySectionsHash.get(theSection);
		if (tmp == null)
		{
			tmp = new IniSection(theSection);
			mySectionsHash.put(theSection, tmp);
			mySectionsList.addLast(tmp);
		}
		tmp.addParameter(theKey);
		flushToDisk();
	}
    
	/**
	 * Get the key value or the defualt if not exists.
	 * If the key doesn't exists then add to the ini file.
	 * 
	 * @param theSection
	 * @param theKey
	 * @param theDefualt
	 * @return the value as an int
	 */
	public int getAsInt(String theSection, String theKey, String theDefualt)
	{
		return Integer.parseInt(get(theSection, theKey, theDefualt));
	}
	

	/**
	 * Get the key value or the defualt if not exists.
	 * If the key doesn't exists then add to the ini file.
	 * @param theSection
	 * @param theKey
	 * @param theDefualt
	 * @return the value as long
	 */
	public long getAsLong(String theSection, String theKey, String theDefualt)
	{
		return Long.parseLong(get(theSection, theKey, theDefualt));
	}
	
	/**
	 * Get the key value or the default if not exists.
	 * If the key doesn't exists then add to the ini file.
	 * @param theSection
	 * @param theKey
	 * @param theDefault
	 * @return the value as double
	 */
	public double getAsDouble(String theSection,String theKey,String theDefault)
	{
		return Double.parseDouble(get(theSection,theKey,theDefault));
	}
    
	/**
	 * Get the key value or the defualt if not exists.
	 * If the key doesn't exists then add to the ini file.
	 * @param theSection
	 * @param theKey
	 * @param theDefualt
	 * @return the value as a short
	 */
	public short getAsShort(String theSection, String theKey, String theDefualt)
	{
		return Short.parseShort(get(theSection, theKey, theDefualt));
	}

	/**
	 * Get the key value or the defualt if not exists.
	 * If the key doesn't exists then add to the ini file.
	 * @param theSection
	 * @param theKey
	 * @param theDefualt
	 * @return the vale as boolean
	 */
	public boolean getAsBool(String theSection, String theKey, String theDefualt)
	{
		return new Boolean(get(theSection, theKey, theDefualt)).booleanValue();
	}
    
	/**
	 * The ini file is kept in memory and any change should be flushed to disk.
	 * Done automatic on set or get that added value.
	 * (no point for caliing this method by the user )
	 */
	public void flushToDisk()
	{
		try
		{
			FileWriter myFileWriter = new FileWriter(myFile);

			for (int i = 0; i < mySectionsList.size(); i++)
			{
				IniSection nextSection = (IniSection) mySectionsList
						.removeFirst();
				mySectionsList.addLast(nextSection);
				nextSection.writeToFile(myFileWriter);
			}

			myFileWriter.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * @return String array with all the ini file sections names. 
	 */
	public String[] getAllSections()
	{
		String[] toReturn = new String[mySectionsHash.size()];
		Iterator<String> i = mySectionsHash.keySet().iterator();
		int index = 0;
		while (i.hasNext())
		{
			toReturn[index++] = (String) i.next();
		}
		return toReturn;
	}
	
	/**
	 * @return String array with all the ini file sections names. 
	 */
	public String[] getAllSectionsOriginalOrder()
	{
		String[] toReturn = new String[mySectionsList.size()];
		Iterator<IniSection> i = mySectionsList.iterator();
		int index = 0;
		while (i.hasNext())
		{
			IniSection sec = (IniSection) i.next();
			toReturn[index++] = sec.myName;
		}
		return toReturn;
	}
	
	
	
	/**
	 * @param theSectionName - the wanted section name
	 * @return true if the section exits in the ini file and false otherwise
	 */
	public boolean isSectionExists(String theSectionName)
	{
		return mySectionsHash.containsKey(theSectionName);
	}
	
	/**
	 * @param theSectionName
	 * @param theParameter
	 * @return true if the ini contains section with the parameter.
	 */
	public boolean isParameterExists(String theSectionName,String theParameter)
	{
		if (isSectionExists(theSectionName) == false)
			return false;
		
		IniSection sec = (IniSection) mySectionsHash.get(theSectionName);
		
		return (sec.getParameter(theParameter) != null);
	}

	/**
	 * The class implelemnts ini section
	 * The class have method for adding key and values or comments 
	 * and have a write method that write the section into the ini file.
	 * ( The parameters and comments order are being kept ) 
	 * @author ronyb
	 */
	class IniSection
	{
		// the keys are being held in hask for quick access
		private HashMap<String, String> myParameters = new HashMap<String, String>();
		// the keys also kept in linked list for quick writing to disk
		private LinkedList<Properties> myLines = new LinkedList<Properties>();
		private String myName = null;

		public IniSection(String theSectionName)
		{
			myName = theSectionName;
		}

		/**
		 * Add parameter to section
		 * @param theKey
		 * @param theValue
		 */
		public void addParameter(String theKey, String theValue)
		{
			myParameters.put(theKey, theValue);
			for (Iterator<Properties> ir = myLines.iterator() ; ir.hasNext() ; )
			{
				Properties p = (Properties) ir.next();
				if (p.myKey.equals(theKey))
				{
					p.myValue = theValue;
					return;
				}
			}
			myLines.add(new Properties(theKey, theValue));
		}
		
		public void addParameter(String theKey)
		{
			myParameters.put(theKey, theKey);
			for (Iterator<Properties> ir = myLines.iterator() ; ir.hasNext() ; )
			{
				Properties p = (Properties) ir.next();
				if (p.myKey.equals(theKey))
				{
					return;
				}
			}
			myLines.add(new Properties(theKey, true));
		}
		
		/**
		 * Remove the parameter.
		 * @param theKey
		 */
		public void removeParameter(String theKey)
		{
			if (myParameters.containsKey(theKey))
			{
				myParameters.remove(theKey);
				Properties toRemove = null; 
				for (Iterator<Properties> ir = myLines.iterator() ; ir.hasNext() ; )
				{
					Properties p = (Properties) ir.next();
					if (p.myKey.equals(theKey))
					{
						toRemove = p;
						break;
					}
				}
				if (toRemove != null)
				{
					myLines.remove(toRemove);
				}
			}
		}
        
		/**
		 * add comment to section
		 * @param theComment
		 */
		public void addComment(String theComment)
		{
			myLines.add(new Properties(theComment));
		}
		
		/**
		 * @return all the parameters in the section as string array.
		 */
		public String[] getAllParameters()
		{
			String[] tmp = new String[myParameters.size()];
			int i=0;
			for (Iterator<String> it = myParameters.keySet().iterator() ; it.hasNext() ; i++)
			{
				tmp[i] = (String) it.next();
			}
			return tmp;
		}
        
		/**
		 * Get the key value and added with the default value if the
		 * key doesn't exists.
		 * 
		 * @param theKey
		 * @param theDefault
		 * @return
		 */
		public String getParameter(String theKey, String theDefault)
		{
			String tmp = (String) myParameters.get(theKey);
			if (tmp == null)
			{
				addParameter(theKey, theDefault);
				return theDefault;
			}
			return tmp;
		}
        
		/**
		 * get the key value.
		 * will return null if key doesn't exists.
		 * @param theKey
		 * @return the key value,
		 */
		public String getParameter(String theKey)
		{
			return (String) myParameters.get(theKey);
		}
        
		/**
		 * Write the section to file.
		 * @param w - opened fileWriter
		 * @throws IOException
		 */
		public void writeToFile(FileWriter w) throws IOException
		{
			if (myName != null)
			{
				w.write("[" + myName + "]");
				w.write("\r\n");
			}

			for (int i = 0; i < myLines.size(); i++)
			{
				Properties p = (Properties) myLines.removeFirst();
				myLines.addLast(p);
				w.write(p.toString());
				w.write("\r\n");
			}
		}

	}
    
	/**
	 * Simple data strucutre for holding key+value or comment
	 * @author ronyb
	 */
	class Properties
	{
		public Properties(String theComment)
		{
			myComment = theComment;
		}

		public Properties(String theKey, String theValue)
		{
			myKey = theKey;
			myValue = theValue;
		}
		
		public Properties(String theKey,boolean theKeyIsYheValue)
		{
			if (theKeyIsYheValue)
			{
				_keyIsThaValue = true;
				myKey = theKey;
			}
			else
			{
				myComment = theKey;
			}
		}
		
		public String myKey = "";
		public String myValue = "";
		public String myComment = "";
		
		private boolean _keyIsThaValue = false;

		public String toString()
		{
			if (myKey.equals(""))
			{
				return myComment;
			}
			if (myKey != null)
			{
				if (myValue == null || _keyIsThaValue)
				{
					return myKey;
				}
				return myKey + "=" + myValue;
			}
			
			return myComment;
		}
	}
}