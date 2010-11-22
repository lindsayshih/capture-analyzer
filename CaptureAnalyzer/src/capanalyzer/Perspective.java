package capanalyzer;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class Perspective implements IPerspectiveFactory {

	private final String NAVIGATOR_ID = "capanalyzer.navigatorView";
	private final String SIMPLENAVIGATOR_ID = "capanalyzer.simplenavigateview";

	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);

		IFolderLayout navFolder = layout.createFolder("navigate",
				IPageLayout.LEFT, 0.25f, editorArea);
		navFolder.addView(NAVIGATOR_ID);
		navFolder.addView(SIMPLENAVIGATOR_ID);
		IFolderLayout folder = layout.createFolder("messages", IPageLayout.TOP,
				0.5f, editorArea);
		folder.addPlaceholder(MessageView.ID + ":*");
		folder.addView(MessageView.ID);
		layout.addView(MessageTableView.ID, IPageLayout.TOP, 0.45f, "messages");

		layout.getViewLayout(MessageTableView.ID).setCloseable(false);
		layout.getViewLayout(MessageTableView.ID).setMoveable(false);

		// for messages
		layout.getViewLayout(MessageView.ID).setMoveable(false);
		layout.getViewLayout(MessageView.ID).setCloseable(false);
	}
}
