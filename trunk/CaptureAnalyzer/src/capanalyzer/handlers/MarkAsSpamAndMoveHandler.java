package capanalyzer.handlers;

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import capanalyzer.model.CaptureDbTable;
import capanalyzer.model.Message;


public class MarkAsSpamAndMoveHandler extends AbstractHandler implements
		IHandler {
	
	public static final String MARK_AS_SPAM_COMMAND_ID = "capanalyzer.markAsSpamAndMove";

	
	static void markAndMoveMessage(Message msg) {
		msg.setSpam(true);
		CaptureDbTable junk = msg.getCaptureDbTable().getDatabase().getJunkCaptureDbTable();
		CaptureDbTable current = msg.getCaptureDbTable();
		if (current != junk) {
			current.removeMessage(msg);
			junk.addMessage(msg);
		}
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection sel = HandlerUtil.getCurrentSelection(event);
		if (sel instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) sel;
			Iterator i = selection.iterator();
			while (i.hasNext()) {
				Message msg = (Message) i.next();
				markAndMoveMessage(msg);
			}
		}
		return null;
	}

}
