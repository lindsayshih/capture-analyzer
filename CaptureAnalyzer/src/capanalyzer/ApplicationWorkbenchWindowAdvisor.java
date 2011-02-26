package capanalyzer;

import java.io.FileNotFoundException;

import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

import capanalyzer.utils.IniFile;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor
{

	public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer)
	{
		super(configurer);
	}

	public void preWindowOpen()
	{
		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
		configurer.setInitialSize(new Point(700, 500));
		configurer.setShowProgressIndicator(true);
		loadAppConfigFile();
	}

	private void loadAppConfigFile()
	{
		try
		{
			IniFile myIni = new IniFile("./bin/captureAnalyzerConfig.ini", false);
			if(myIni.containsParameter("Database", "driverName")) 
				GlobalConfig.Database.setDriverName(myIni.get("Database", "driverName"));
			if(myIni.containsParameter("Database", "connURL")) 
				GlobalConfig.Database.setConnURL(myIni.get("Database", "connURL"));
			if(myIni.containsParameter("Database", "username")) 
				GlobalConfig.Database.setUsername(myIni.get("Database", "username"));
			if(myIni.containsParameter("Database", "password")) 
				GlobalConfig.Database.setPassword(myIni.get("Database", "password"));
			
			if(myIni.containsParameter("CaptureFileReadParams", "numberOfMaps")) 
				GlobalConfig.CaptureFileReadParams.setNumberOfMaps(myIni.getAsInt("CaptureFileReadParams", "numberOfMaps", GlobalConfig.CaptureFileReadParams.getNumberOfMaps()+""));
			if(myIni.containsParameter("CaptureFileReadParams", "numberOfDbMaps")) 
				GlobalConfig.CaptureFileReadParams.setNumberOfDbMaps(myIni.getAsInt("CaptureFileReadParams", "numberOfDbMaps", GlobalConfig.CaptureFileReadParams.getNumberOfDbMaps()+""));
			if(myIni.containsParameter("CaptureFileReadParams", "sizeOfBuffer")) 
				GlobalConfig.CaptureFileReadParams.setSizeOfBuffer(myIni.getAsInt("CaptureFileReadParams", "sizeOfBuffer", GlobalConfig.CaptureFileReadParams.getSizeOfBuffer()+""));
			if(myIni.containsParameter("CaptureFileReadParams", "agingTime")) 
				GlobalConfig.CaptureFileReadParams.setAgingTime(myIni.getAsInt("CaptureFileReadParams", "agingTime", GlobalConfig.CaptureFileReadParams.getAgingTime()+""));
			
		} catch (FileNotFoundException e)
		{

			e.printStackTrace();
		}
	}
}
