package capanalyzer.model;

import java.util.ArrayList;
import java.util.List;

public class Database extends ModelObject
{

	private List<CaptureDbTable> captureDbTables;

	private String hostname;
	private String schema;
	private String username;
	private String password;

	private CaptureDbTable junkCaptureDbTable;

	public Database()
	{
		captureDbTables = new ArrayList<CaptureDbTable>();
		addCaptureDbTable(new CaptureDbTable("Inbox"));
		addCaptureDbTable(new CaptureDbTable("Drafts"));
		addCaptureDbTable(new CaptureDbTable("Sent"));
		addCaptureDbTable(new CaptureDbTable("Junk"));
		addCaptureDbTable(new CaptureDbTable("Trash"));
	}

	public CaptureDbTable getJunkCaptureDbTable()
	{
		return junkCaptureDbTable;
	}

	public void addCaptureDbTable(final CaptureDbTable child)
	{
		captureDbTables.add(child);
		child.setDatabase(this);
		firePropertyChange("captureDbTables", null, null); // We could provide old and new values, but (null, null) is allowed by the beans spec.
		if (child.getName().equals("Junk"))
		{
			junkCaptureDbTable = child;
		}
	}

	public void removeCaptureDbTable(CaptureDbTable child)
	{
		captureDbTables.remove(child);
		child.setDatabase(null);
		firePropertyChange("captureDbTables", null, null); // We could provide old and new values, but (null, null) is allowed by the beans spec.
	}

	public List<CaptureDbTable> getCaptureDbTables()
	{
		return captureDbTables;
	}

	public Model getModel()
	{
		return Model.getInstance();
	}

	public String getHostname()
	{
		return hostname;
	}

	public void setHostname(String hostname)
	{
		firePropertyChange("hostname", this.hostname, this.hostname = hostname);
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		firePropertyChange("username", this.username, this.username = username);
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		firePropertyChange("password", this.password, this.password = password);
	}

	public String getSchema()
	{
		return schema;
	}

	public void setSchema(String schema)
	{
		firePropertyChange("schema", this.schema, this.schema = schema);
	}

	public String toString()
	{
		return "Database( hostname=\"" + hostname + "\"" + ", username=\"" + username + "\"" + ", password=\"" + password + "\"" + ", schema=" + schema + ")";
	}

	Object getDefaultSelection()
	{
		for (CaptureDbTable f : captureDbTables)
		{
			if ("Inbox".equals(f.getName()))
			{
				return f;
			}
		}
		return null;
	}
}
