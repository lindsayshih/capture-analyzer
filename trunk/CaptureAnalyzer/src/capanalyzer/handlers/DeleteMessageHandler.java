package capanalyzer.handlers;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import capanalyzer.model.Folder;
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
		Folder trash = getTrash(msg.getFolder().getServer().getFolders());
		Folder current = msg.getFolder();
		if (trash != current) {
			current.removeMessage(msg);
			trash.addMessage(msg);
		} else {
			trash.removeMessage(msg);
		}
	}

	private static Folder getTrash(List<Folder> folders) {
		for (Folder folder : folders) {
			if ("Trash".equals(folder.getName())) {
				return folder;
			}
		}
		return null;
	}

}
