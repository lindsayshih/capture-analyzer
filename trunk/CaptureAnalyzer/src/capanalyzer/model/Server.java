package capanalyzer.model;

import java.util.ArrayList;
import java.util.List;

public class Server extends ModelObject {
	
	private List<Folder> folders;
	
	private String hostname;
	private String username;
	private String password;
	private int port;
	private Folder junkFolder;

	public Server() {
		folders = new ArrayList<Folder>();
		addFolder(new Folder("Inbox"));
		addFolder(new Folder("Drafts"));
		addFolder(new Folder("Sent"));
		addFolder(new Folder("Junk"));
		addFolder(new Folder("Trash"));
	}

	public Folder getJunkFolder() {
		return junkFolder;
	}

	public void addFolder(final Folder child) {
		folders.add(child);
		child.setServer(this);
		// We could provide old and new values, but (null, null) is allowed by
		// the beans spec.
		firePropertyChange("folders", null, null);
		if (child.getName().equals("Junk")) {
			junkFolder = child;
		}
	}

	public void removeFolder(Folder child) {
		folders.remove(child);
		child.setServer(null);
		// We could provide old and new values, but (null, null) is allowed by
		// the beans spec.
		firePropertyChange("folders", null, null);
	}

	public List<Folder> getFolders() {
		return folders;
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
		return "Server( hostname=\"" + hostname + "\"" + ", username=\""
				+ username + "\"" + ", password=\"" + password + "\""
				+ ", port=" + port + ")";
	}

    Object getDefaultSelection() {
		for (Folder f : folders) {
			if ("Inbox".equals(f.getName())) {
				return f;
			}
		}
		return null;
	}
}
