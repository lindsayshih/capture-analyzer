package capanalyzer;

import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.jface.databinding.viewers.ObservableListTreeContentProvider;
import org.eclipse.jface.databinding.viewers.TreeStructureAdvisor;

import capanalyzer.model.CaptureDbTable;
import capanalyzer.model.Model;
import capanalyzer.model.Database;


public class MailContentProvider extends ObservableListTreeContentProvider {

	public MailContentProvider() {
		super(getObservableListFactory(), getTreeStructureAdvisor());
	}

	// This factory returns an observable list of children for the given parent.
	private static IObservableFactory getObservableListFactory() {
		return new IObservableFactory() {
			public IObservable createObservable(Object parent) {
				if (parent instanceof Model) {
					return BeanProperties.list("databases").observe(parent);
				}
				if (parent instanceof Database) {
					return BeanProperties.list("captureDbTables").observe(parent);
				}
				return null;
			}
		};
	}

	// The following is optional, you can pass null as the advisor, but then
	// setSelection() will not find elements that have not been expanded.
	private static TreeStructureAdvisor getTreeStructureAdvisor() {
		return new TreeStructureAdvisor() {
			public Object getParent(Object element) {
				if (element instanceof CaptureDbTable) {
					return ((CaptureDbTable) element).getDatabase();
				}
				if (element instanceof Database) {
					return ((Database) element).getModel();
				}
				return super.getParent(element);
			}
		};
	}
}
