package capanalyzer;

import org.eclipse.jface.databinding.viewers.ObservableListTreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import capanalyzer.model.Model;


public class NavigationView extends ViewPart {

	private TreeViewer treeViewer;

	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		treeViewer = new TreeViewer(parent);

		ObservableListTreeContentProvider contentProvider = new MailContentProvider();
		treeViewer.setContentProvider(contentProvider);

		treeViewer.setLabelProvider(new MailLabelProvider(contentProvider
				.getKnownElements()));

		treeViewer.setInput(Model.getInstance());
		Object defaultSelection = Model.getInstance().getDefaultSelection();
		if (defaultSelection != null) {
			treeViewer.setSelection(new StructuredSelection(defaultSelection));
		}

		getSite().setSelectionProvider(treeViewer);
	}

	// it is important to implement setFocus()!
	public void setFocus() {
		treeViewer.getTree().setFocus();
	}

}
