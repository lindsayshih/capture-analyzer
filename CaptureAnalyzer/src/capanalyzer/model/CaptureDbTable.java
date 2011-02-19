package capanalyzer.model;

import java.util.ArrayList;
import java.util.List;

public class CaptureDbTable extends ModelObject {

	private List<Message> messages;

	private String name;
	private Database database;

	public CaptureDbTable(String name) {
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
		message.setCaptureDbTable(this);
		firePropertyChange("messages", null, null);
	}

	public void removeMessage(Message message) {
		messages.remove(message);
		message.setCaptureDbTable(null);
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

	public Database getDatabase() {
		return database;
	}

	void setDatabase(Database database) {
		this.database = database;
		// We could provide old and new values, but (null, null) is allowed by
		// the beans spec.
		firePropertyChange("database", null, null);
	}

}
