package capanalyzer;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import capanalyzer.model.CaptureDbTable;
import capanalyzer.model.Database;


// This is an almost exact copy of MailLabelProvider, but it does not listen
// to changes to the underlying model objects - it does not inherit from
// ObservableMapLabelProvider. Using a data binding label provider with the
// common navigator is not currently supported well because the corresponding
// content provider is not known at the time the constructor of the label
// provider is called. The easiest solution would be to copy code from
// ObservableMapLabelProvider into this class.
public final class NavigatorLabelProvider extends LabelProvider {
	
	private LocalResourceManager resourceManager = new LocalResourceManager(
			JFaceResources.getResources());
	
	private ImageDescriptor databaseImage;
	private ImageDescriptor captureDbTableImage;
	private ImageDescriptor junkCaptureDbTableImage;
	private ImageDescriptor draftsCaptureDbTableImage;
	private ImageDescriptor sentCaptureDbTableImage;
	private ImageDescriptor trashCaptureDbTableImage;

	public NavigatorLabelProvider() {
		initializeImageDescriptors();
	}

	private void initializeImageDescriptors() {
		databaseImage = getDescriptor("server.png");
		captureDbTableImage = getDescriptor("folder.png");
		junkCaptureDbTableImage = getDescriptor("folder_bug.png");
		draftsCaptureDbTableImage = getDescriptor("folder_edit.png");
		sentCaptureDbTableImage = getDescriptor("folder_go.png");
		trashCaptureDbTableImage = getDescriptor("folder_delete.png");
	}

	private ImageDescriptor getDescriptor(String fileName) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(Application.PLUGIN_ID,
				"icons/silk/" + fileName);
	}
	
	public String getText(Object element) {
		if (element instanceof Database) {
			return ((Database) element).getHostname();
		}
		if (element instanceof CaptureDbTable) {
			CaptureDbTable captureDbTable = (CaptureDbTable) element;
			return captureDbTable.getName();
		}
		return null;
	}

	public Image getImage(Object element) {
		if (element instanceof Database) {
			return (Image) resourceManager.get(databaseImage);
		} else if (element instanceof CaptureDbTable) {
			// We really should use constants here
			CaptureDbTable captureDbTable = (CaptureDbTable) element;
			if ("junk".equalsIgnoreCase(captureDbTable.getName())) {
				return (Image) resourceManager.get(junkCaptureDbTableImage);
			} else if ("drafts".equalsIgnoreCase(captureDbTable.getName())) {
				return (Image) resourceManager.get(draftsCaptureDbTableImage);
			} else if ("sent".equalsIgnoreCase(captureDbTable.getName())) {
				return (Image) resourceManager.get(sentCaptureDbTableImage);
			} else if ("trash".equalsIgnoreCase(captureDbTable.getName())) {
				return (Image) resourceManager.get(trashCaptureDbTableImage);
			}
			return (Image) resourceManager.get(captureDbTableImage);
		}
		return null;
	}
	
	public void dispose() {
		super.dispose();
		resourceManager.dispose();
	}
}