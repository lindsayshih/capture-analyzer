package capanalyzer;

import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.property.Properties;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import capanalyzer.model.CaptureDbTable;
import capanalyzer.model.Database;


final class MailLabelProvider extends ObservableMapLabelProvider {
	
	private LocalResourceManager resourceManager = new LocalResourceManager(
			JFaceResources.getResources());
	
	private ImageDescriptor databaseImage;
	private ImageDescriptor captureDbTableImage;
	private ImageDescriptor junkCaptureDbTableImage;
	private ImageDescriptor draftsCaptureDbTableImage;
	private ImageDescriptor sentCaptureDbTableImage;
	private ImageDescriptor trashCaptureDbTableImage;

	MailLabelProvider(IObservableSet knownElements) {
		// We pass an array of properties so that appropriate listeners
		// are added automatically to each element displayed in the viewer.
		super(Properties.observeEach(knownElements, BeanProperties
				.values(new String[] { "name", "hostname", "messages" })));
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
	
	// We have to override getText because the model is not homogeneous.
	// If each element had a property "name" we wouldn't need this, and
	// could have just listed one property in the constructor.
	public String getText(Object element) {
		if (element instanceof Database) {
			return ((Database) element).getHostname();
		}
		if (element instanceof CaptureDbTable) {
			CaptureDbTable captureDbTable = (CaptureDbTable) element;
			return captureDbTable.getName() + " (" + captureDbTable.getMessages().length + ")";
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