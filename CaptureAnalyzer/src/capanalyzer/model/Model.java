package capanalyzer.model;

import java.util.ArrayList;
import java.util.List;

public class Model extends ModelObject
{
	private static Model model;
	private List<Database> databases = new ArrayList<Database>();

	public Model()
	{
		Database database = new Database();
		database.setHostname("Database");
		database.setSchema("Test Schema");
		database.setUsername("test");
		addDatabase(database);
	}

	public void addDatabase(final Database database)
	{
		databases.add(database);
		firePropertyChange("databases", null, null);
	}

	public void removeDatabase(Database child)
	{
		databases.remove(child);
		firePropertyChange("databases", null, null);
	}

	public List<Database> getDatabases()
	{
		return databases;
	}

	public Object getDefaultSelection()
	{
		return databases.size() > 0 ? databases.get(0).getDefaultSelection() : null;
	}

	public static Model getInstance()
	{
		if (model == null)
		{
			model = new Model();
		}
		return model;
	}
}
