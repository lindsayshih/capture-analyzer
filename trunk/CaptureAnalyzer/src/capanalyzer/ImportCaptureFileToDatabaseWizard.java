package capanalyzer;
import org.eclipse.jface.wizard.Wizard;


public class ImportCaptureFileToDatabaseWizard extends Wizard
{
	private ImportCaptureFileToDatabasePage importPage;
	
	public ImportCaptureFileToDatabaseWizard()
	{
	}

	public void addPages()
	{
		importPage = new ImportCaptureFileToDatabasePage();
		addPage(importPage);
	}

	public String getWindowTitle()
	{
		return "Import Capture File to Database";
	}

	public boolean performFinish()
	{
		importPage.getCaptureName();
		importPage.getFilePathAndName();
		return true;
	}

}