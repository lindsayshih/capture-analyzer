package capanalyzer.handlers;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import capanalyzer.model.CaptureDbTable;
import capanalyzer.model.Message;


public class DeleteMessageHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection sel = HandlerUtil.getCurrentSelection(event);
		if (sel instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) sel;
			if (selection.getFirstElement() instanceof Message) {
				Message msg = (Message) selection.getFirstElement();
				trashMessage(msg);
			}
		}
		return null;
	}

	static void trashMessage(Message msg) {
		CaptureDbTable trash = getTrash(msg.getCaptureDbTable().getDatabase().getCaptureDbTables());
		CaptureDbTable current = msg.getCaptureDbTable();
		if (trash != current) {
			current.removeMessage(msg);
			trash.addMessage(msg);
		} else {
			trash.removeMessage(msg);
		}
	}

	private static CaptureDbTable getTrash(List<CaptureDbTable> captureDbTables) {
		for (CaptureDbTable captureDbTable : captureDbTables) {
			if ("Trash".equals(captureDbTable.getName())) {
				return captureDbTable;
			}
		}
		return null;
	}

}
