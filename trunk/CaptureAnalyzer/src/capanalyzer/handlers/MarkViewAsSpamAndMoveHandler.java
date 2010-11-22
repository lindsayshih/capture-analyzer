package capanalyzer.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;

import capanalyzer.MessageView;
import capanalyzer.model.Message;


public class MarkViewAsSpamAndMoveHandler extends AbstractHandler implements
		IHandler {
	private MessageView messageView;

	public MarkViewAsSpamAndMoveHandler(MessageView view) {
		messageView = view;
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		Message msg = messageView.getMessage();
		if (msg != null) {
			capanalyzer.handlers.MarkAsSpamAndMoveHandler.markAndMoveMessage(msg);
		}
		return null;
	}

	/**
	 * This will fire the enabled state update.
	 * 
	 * @param b
	 */
	public void setEnabled(boolean b) {
		setBaseEnabled(b);
	}
}
