package capanalyzer;

import java.util.HashMap;

import org.eclipse.core.databinding.AggregateValidationStatus;
import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.swt.ISWTObservable;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.wizard.WizardPageSupport;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import capanalyzer.model.Server;


public class CreateServerPage extends WizardPage {

	private static HashMap<Control, ControlDecoration> decoratorMap = new HashMap<Control, ControlDecoration>();

	public static class HostnameValidator implements IValidator {

		public IStatus validate(Object value) {
			String string = (String) value;
			if (string == null || string.trim().length() == 0) {
				return ValidationStatus
						.error("Please enter a value for Hostname.");
			}
			if (!string.matches("^[-\\w.]+$")) {
				return ValidationStatus
						.error("Hostname must be of the form xxx.xxx.");
			}
			return ValidationStatus.ok();
		}

	}

	public static class NotEmptyValidator implements IValidator {

		private final String fieldname;

		public NotEmptyValidator(String fieldname) {
			this.fieldname = fieldname;
		}

		public IStatus validate(Object value) {
			String string = (String) value;
			if (string == null || string.trim().length() == 0) {
				return ValidationStatus.error("Please enter a value for "
						+ fieldname + ".");
			}
			return ValidationStatus.ok();
		}

	}

	private final Server server;

	public CreateServerPage(Server server) {
		super("wizardPage");
		this.server = server;
		setTitle("Create Server Connection");
		setDescription("Please enter information to create a server connection.");
	}

	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		setControl(container);

		final Label hostnameLabel = new Label(container, SWT.NONE);
		hostnameLabel.setText("Hostname");

		Text hostnameText = new Text(container, SWT.BORDER);
		createControlDecoration(hostnameText);

		Label usernameLabel = new Label(container, SWT.NONE);
		usernameLabel.setText("Username");

		Text usernameText = new Text(container, SWT.BORDER);
		createControlDecoration(usernameText);

		Label passwordLabel = new Label(container, SWT.NONE);
		passwordLabel.setText("Password");

		Text passwordText = new Text(container, SWT.BORDER);
		passwordText.setEchoChar('*');
		createControlDecoration(passwordText);

		Label portLabel = new Label(container, SWT.NONE);
		portLabel.setText("Port");

		Text portText = new Text(container, SWT.BORDER);
		createControlDecoration(portText);
		
		// The additional spacing (default is 5,5) is for the decorations
		GridLayoutFactory.swtDefaults().numColumns(2).spacing(10, 5)
				.generateLayout(container);

		final DataBindingContext dbc = new DataBindingContext();

		bind(dbc, hostnameText, server, "hostname", new HostnameValidator());
		bind(dbc, usernameText, server, "username", new NotEmptyValidator(
				"username"));
		bind(dbc, passwordText, server, "password", new NotEmptyValidator(
				"password"));
		bind(dbc, portText, server, "port", null);

		final AggregateValidationStatus aggregateValidationStatus = new AggregateValidationStatus(
				dbc.getValidationStatusProviders(),
				AggregateValidationStatus.MAX_SEVERITY);

		aggregateValidationStatus.addValueChangeListener(new IValueChangeListener() {
					public void handleValueChange(ValueChangeEvent event) {
						// the invocation of the getValue method is necessary
						// the further changes will be fired
						aggregateValidationStatus.getValue();
						for (Object o : dbc.getBindings()) {
							Binding binding = (Binding) o;
							IStatus status = (IStatus) binding.getValidationStatus().getValue();
							Control control = null;
							if (binding.getTarget() instanceof ISWTObservable) {
								ISWTObservable swtObservable = (ISWTObservable) binding.getTarget();
								control = (Control) swtObservable.getWidget();
							}
							ControlDecoration decoration = decoratorMap.get(control);
							if (decoration != null) {
								if (status.isOK()) {
									decoration.hide();
								} else {
									decoration.setDescriptionText(status.getMessage());
									decoration.show();
								}
							}
						}
					}
				});

		// create this after binding the observables to avoid displaying an
		// error when the wizard page is shown:
		WizardPageSupport.create(this, dbc);
	}

	private void bind(DataBindingContext dbc, Text textWidget, Object bean,
			String property, IValidator validator) {
		UpdateValueStrategy targetToModel = null;
		if (validator != null) {
			targetToModel = new UpdateValueStrategy()
					.setAfterConvertValidator(validator);
		}
		dbc.bindValue(SWTObservables.observeText(textWidget, SWT.Modify),
				BeansObservables.observeValue(bean, property), targetToModel,
				null);
	}

	private void createControlDecoration(Control control) {
		ControlDecoration controlDecoration = new ControlDecoration(control,
				SWT.LEFT | SWT.TOP);
		FieldDecoration fieldDecoration = FieldDecorationRegistry.getDefault()
				.getFieldDecoration(FieldDecorationRegistry.DEC_ERROR);
		controlDecoration.setImage(fieldDecoration.getImage());
		controlDecoration.hide();
		decoratorMap.put(control, controlDecoration);
	}
}
