package capanalyzer.model;

import java.util.ArrayList;
import java.util.List;

public class Folder extends ModelObject {

	private List<Message> messages;

	private String name;
	private Server server;

	public Folder(String name) {
		this.name = name;
		messages = new ArrayList<Message>();

		if (getName().equals("Inbox")) {
			addMessage(Message.createExample(0));
			addMessage(Message.createExample(1));
			addMessage(Message.createExample(2));
			addMessage(Message.createExample(3));
			addMessage(Message.createExample(4));
			addMessage(Message.createExample(5));
		}
	}

	public String toString() {
		return name + messages.toString();
	}

	public void addMessage(Message message) {
		messages.add(message);
		message.setFolder(this);
		firePropertyChange("messages", null, null);
	}

	public void removeMessage(Message message) {
		messages.remove(message);
		message.setFolder(null);
		// We could provide old and new values, but (null, null) is allowed by
		// the beans spec.
		firePropertyChange("messages", null, null);
	}

	// Return an array - otherwise, change events are swallowed because oldValue
	// and newValue for the "messages" property will be equals() from the point of
	// view of an observer.
	public Message[] getMessages() {
		return messages.toArray(new Message[messages.size()]);
	}

	public String getName() {
		return name;
	}

	public Server getServer() {
		return server;
	}

	void setServer(Server server) {
		this.server = server;
		// We could provide old and new values, but (null, null) is allowed by
		// the beans spec.
		firePropertyChange("server", null, null);
	}

}
