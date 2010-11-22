package capanalyzer;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import capanalyzer.model.Folder;
import capanalyzer.model.Server;


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
	
	private ImageDescriptor serverImage;
	private ImageDescriptor folderImage;
	private ImageDescriptor junkFolderImage;
	private ImageDescriptor draftsFolderImage;
	private ImageDescriptor sentFolderImage;
	private ImageDescriptor trashFolderImage;

	public NavigatorLabelProvider() {
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
	
	public String getText(Object element) {
		if (element instanceof Server) {
			return ((Server) element).getHostname();
		}
		if (element instanceof Folder) {
			Folder folder = (Folder) element;
			return folder.getName();
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