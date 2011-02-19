package capanalyzer;

import org.eclipse.jface.wizard.Wizard;

import capanalyzer.model.Model;
import capanalyzer.model.Database;


public class CreateDatabaseWizard extends Wizard {
	private final Database database = new Database();
	public CreateDatabaseWizard() {
		// defaults
		database.setHostname("localhost");
		database.setPort(23);
	}
	public void addPages() {
		addPage(new CreateDatabasePage(database));
	}

	public String getWindowTitle() {
		return "Create Mail Database";
	}

	public boolean performFinish() {
		Model.getInstance().addDatabase(database);
		return true;
	}

}