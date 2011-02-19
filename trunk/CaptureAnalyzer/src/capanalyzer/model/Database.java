package capanalyzer.model;

import java.util.ArrayList;
import java.util.List;

public class Database extends ModelObject {
	
	private List<CaptureDbTable> captureDbTables;
	
	private String hostname;
	private String username;
	private String password;
	private int port;
	private CaptureDbTable junkCaptureDbTable;

	public Database() {
		captureDbTables = new ArrayList<CaptureDbTable>();
		addCaptureDbTable(new CaptureDbTable("Inbox"));
		addCaptureDbTable(new CaptureDbTable("Drafts"));
		addCaptureDbTable(new CaptureDbTable("Sent"));
		addCaptureDbTable(new CaptureDbTable("Junk"));
		addCaptureDbTable(new CaptureDbTable("Trash"));
	}

	public CaptureDbTable getJunkCaptureDbTable() {
		return junkCaptureDbTable;
	}

	public void addCaptureDbTable(final CaptureDbTable child) {
		captureDbTables.add(child);
		child.setDatabase(this);
		// We could provide old and new values, but (null, null) is allowed by
		// the beans spec.
		firePropertyChange("captureDbTables", null, null);
		if (child.getName().equals("Junk")) {
			junkCaptureDbTable = child;
		}
	}

	public void removeCaptureDbTable(CaptureDbTable child) {
		captureDbTables.remove(child);
		child.setDatabase(null);
		// We could provide old and new values, but (null, null) is allowed by
		// the beans spec.
		firePropertyChange("captureDbTables", null, null);
	}

	public List<CaptureDbTable> getCaptureDbTables() {
		return captureDbTables;
	}

	public Model getModel() {
		return Model.getInstance();
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		firePropertyChange("hostname", this.hostname, this.hostname = hostname);
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		firePropertyChange("username", this.username, this.username = username);
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		firePropertyChange("password", this.password, this.password = password);
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		firePropertyChange("port", this.port, this.port = port);
	}

	public String toString() {
		return "Database( hostname=\"" + hostname + "\"" + ", username=\""
				+ username + "\"" + ", password=\"" + password + "\""
				+ ", port=" + port + ")";
	}

    Object getDefaultSelection() {
		for (CaptureDbTable f : captureDbTables) {
			if ("Inbox".equals(f.getName())) {
				return f;
			}
		}
		return null;
	}
}
