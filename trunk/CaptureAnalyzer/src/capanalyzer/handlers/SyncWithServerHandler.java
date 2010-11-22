package capanalyzer.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;

import capanalyzer.model.Folder;
import capanalyzer.model.Message;


public class SyncWithServerHandler extends AbstractHandler implements IHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection currentSelection = HandlerUtil.getCurrentSelection(event);
		if (currentSelection instanceof IStructuredSelection) {
			Object selected = ((IStructuredSelection) currentSelection)
					.getFirstElement();
			if (selected instanceof Folder) {
				final Folder folder = (Folder) selected;
				Job syncJob = new Job("Synchronize") {
					protected IStatus run(IProgressMonitor monitor) {
						monitor.beginTask(getName(), 3);
						for (int i = 0; i < 3; i++) {
							try {
								Thread.sleep(400);
							} catch (InterruptedException e) {
								Thread.currentThread().interrupt();
							}
							// Note: our model is not thread safe. We are using
							// Display.asyncExec to make sure that the model
							// is only accessed and changed from the UI thread.
							// Using asyncExec ensures that deadlocks cannot
							// happen. (In the current code, there is no deadlock
							// potential because this job does not hold any locks,
							// but that may change in the future.)
							Display.getDefault().asyncExec(new Runnable() {
								public void run() {
									folder.addMessage(Message.createExample(-1));
								}
							});
							monitor.worked(1);
						}
						monitor.done();
						return Status.OK_STATUS;
					}
				};
				syncJob.schedule();
			}
		}
		return null;
	}

}
