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

import capanalyzer.model.CaptureDbTable;
import capanalyzer.model.Message;


public class SyncWithDatabaseHandler extends AbstractHandler implements IHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection currentSelection = HandlerUtil.getCurrentSelection(event);
		if (currentSelection instanceof IStructuredSelection) {
			Object selected = ((IStructuredSelection) currentSelection)
					.getFirstElement();
			if (selected instanceof CaptureDbTable) {
				final CaptureDbTable captureDbTable = (CaptureDbTable) selected;
				Job syncJob = new Job("Synchronize") {
					protected IStatus run(IProgressMonitor monitor) {
						monitor.beginTask(getName(), 50);
						for (int i = 0; i < 50; i++) {
							try {
								Thread.sleep(400);
							} catch (InterruptedException e) {
								Thread.currentThread().interrupt();
							}
							
							if (monitor.isCanceled())	 
								return Status.CANCEL_STATUS;
							
							monitor.subTask("Stage" + i);
							
							// Note: our model is not thread safe. We are using
							// Display.asyncExec to make sure that the model
							// is only accessed and changed from the UI thread.
							// Using asyncExec ensures that deadlocks cannot
							// happen. (In the current code, there is no deadlock
							// potential because this job does not hold any locks,
							// but that may change in the future.)
							Display.getDefault().asyncExec(new Runnable() {
								public void run() {
									captureDbTable.addMessage(Message.createExample(-1));
								}
							});
							monitor.worked(1);
						}
						monitor.done();
						return Status.OK_STATUS;
					}
				};
				syncJob.setUser(true);
				syncJob.schedule();
			}
		}
		return null;
	}

}
