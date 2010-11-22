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

import capanalyzer.model.Folder;
import capanalyzer.model.Server;


final class MailLabelProvider extends ObservableMapLabelProvider {
	
	private LocalResourceManager resourceManager = new LocalResourceManager(
			JFaceResources.getResources());
	
	private ImageDescriptor serverImage;
	private ImageDescriptor folderImage;
	private ImageDescriptor junkFolderImage;
	private ImageDescriptor draftsFolderImage;
	private ImageDescriptor sentFolderImage;
	private ImageDescriptor trashFolderImage;

	MailLabelProvider(IObservableSet knownElements) {
		// We pass an array of properties so that appropriate listeners
		// are added automatically to each element displayed in the viewer.
		super(Properties.observeEach(knownElements, BeanProperties
				.values(new String[] { "name", "hostname", "messages" })));
		initializeImageDescriptors();
	}

	private void initializeImageDescriptors() {
		serverImage = getDescriptor("server.png");
		folderImage = getDescriptor("folder.png");
		junkFolderImage = getDescriptor("folder_bug.png");
		draftsFolderImage = getDescriptor("folder_edit.png");
		sentFolderImage = getDescriptor("folder_go.png");
		trashFolderImage = getDescriptor("folder_delete.png");
	}

	private ImageDescriptor getDescriptor(String fileName) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(Application.PLUGIN_ID,
				"icons/silk/" + fileName);
	}
	
	// We have to override getText because the model is not homogeneous.
	// If each element had a property "name" we wouldn't need this, and
	// could have just listed one property in the constructor.
	public String getText(Object element) {
		if (element instanceof Server) {
			return ((Server) element).getHostname();
		}
		if (element instanceof Folder) {
			Folder folder = (Folder) element;
			return folder.getName() + " (" + folder.getMessages().length + ")";
		}
		return null;
	}

	public Image getImage(Object element) {
		if (element instanceof Server) {
			return (Image) resourceManager.get(serverImage);
		} else if (element instanceof Folder) {
			// We really should use constants here
			Folder folder = (Folder) element;
			if ("junk".equalsIgnoreCase(folder.getName())) {
				return (Image) resourceManager.get(junkFolderImage);
			} else if ("drafts".equalsIgnoreCase(folder.getName())) {
				return (Image) resourceManager.get(draftsFolderImage);
			} else if ("sent".equalsIgnoreCase(folder.getName())) {
				return (Image) resourceManager.get(sentFolderImage);
			} else if ("trash".equalsIgnoreCase(folder.getName())) {
				return (Image) resourceManager.get(trashFolderImage);
			}
			return (Image) resourceManager.get(folderImage);
		}
		return null;
	}
	
	public void dispose() {
		super.dispose();
		resourceManager.dispose();
	}
}