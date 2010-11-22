package capanalyzer;

import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.jface.databinding.viewers.ObservableListTreeContentProvider;
import org.eclipse.jface.databinding.viewers.TreeStructureAdvisor;

import capanalyzer.model.Folder;
import capanalyzer.model.Model;
import capanalyzer.model.Server;


public class MailContentProvider extends ObservableListTreeContentProvider {

	public MailContentProvider() {
		super(getObservableListFactory(), getTreeStructureAdvisor());
	}

	// This factory returns an observable list of children for the given parent.
	private static IObservableFactory getObservableListFactory() {
		return new IObservableFactory() {
			public IObservable createObservable(Object parent) {
				if (parent instanceof Model) {
					return BeanProperties.list("servers").observe(parent);
				}
				if (parent instanceof Server) {
					return BeanProperties.list("folders").observe(parent);
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
				if (element instanceof Folder) {
					return ((Folder) element).getServer();
				}
				if (element instanceof Server) {
					return ((Server) element).getModel();
				}
				return super.getParent(element);
			}
		};
	}
}
