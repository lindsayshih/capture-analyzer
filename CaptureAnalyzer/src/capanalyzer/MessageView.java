package capanalyzer;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;

import capanalyzer.handlers.DeleteViewMessageHandler;
import capanalyzer.handlers.MarkAsSpamAndMoveHandler;
import capanalyzer.handlers.MarkViewAsSpamAndMoveHandler;
import capanalyzer.model.Message;


public class MessageView extends ViewPart implements ISelectionListener {

	public static final String ID = "capanalyzer.view";
	private DataBindingContext dbc;
	
	private Control bodyText;
	private StackLayout stackLayout;
	private Composite messageComposite;
	private Composite emptyComposite;
	private Composite stackComposite;

	public void createPartControl(Composite parent) {
		stackComposite = new Composite(parent, SWT.NONE);
		stackLayout = new StackLayout();
		stackComposite.setLayout(stackLayout);
		messageComposite = new Composite(stackComposite, SWT.NONE);

		emptyComposite = new Composite(stackComposite, SWT.NONE);
		emptyComposite.setLayout(new FillLayout());
		new Label(emptyComposite, SWT.NONE).setText("No message selected");
		
		stackLayout.topControl = emptyComposite;

		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		messageComposite.setLayout(layout);
		
		// top banner
		Composite banner = new Composite(messageComposite, SWT.NONE);
		banner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		layout = new GridLayout();
		layout.marginHeight = 5;
		layout.marginWidth = 10;
		layout.numColumns = 3;
		banner.setLayout(layout);

		// setup bold font
		Font boldFont = JFaceResources.getFontRegistry().getBold(
				JFaceResources.DEFAULT_FONT);

		Label l = new Label(banner, SWT.WRAP);
		l.setText("Subject:");
		l.setFont(boldFont);
		Label subjectLabel = new Label(banner, SWT.WRAP);
		subjectLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));

		final Button spamButton = new Button(banner, SWT.CHECK);
		spamButton.setLayoutData(new GridData());
		spamButton.setText("Spam");

		l = new Label(banner, SWT.WRAP);
		l.setText("From:");
		l.setFont(boldFont);

		Link link = new Link(banner, SWT.NONE);
		final GridData gd_link = new GridData(SWT.FILL, SWT.CENTER, true,
				false, 2, 1);
		link.setLayoutData(gd_link);
		link.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				MessageDialog
						.openInformation(getSite().getShell(),
								"Not Implemented",
								"Imagine the address book or a new message being created now.");
			}
		});

		l = new Label(banner, SWT.WRAP);
		l.setText("Date:");
		l.setFont(boldFont);
		Label date = new Label(banner, SWT.WRAP);
		date
				.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
						2, 1));
		
		// message contents is a field so that it can be referenced from setFocus()
		bodyText = new Text(messageComposite, SWT.MULTI | SWT.WRAP);
		bodyText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		ISelectionService selectionService = (ISelectionService) getSite()
				.getService(ISelectionService.class);
		selectionService.addSelectionListener(MessageTableView.ID, this);

		dbc = new DataBindingContext();

		IObservableValue subjectObservable = BeansObservables
				.observeDetailValue(messageValue, "subject", String.class);
		ISWTObservableValue subjectLabelObservable = SWTObservables
				.observeText(subjectLabel);
		dbc.bindValue(subjectLabelObservable, subjectObservable, null, null);

		dbc.bindValue(SWTObservables.observeText(bodyText, SWT.Modify),
				BeansObservables.observeDetailValue(messageValue, "body",
						String.class));
		dbc.bindValue(SWTObservables.observeSelection(spamButton),
				BeansObservables.observeDetailValue(messageValue, "spam",
						boolean.class));
		dbc.bindValue(SWTObservables.observeText(date), BeansObservables
				.observeDetailValue(messageValue, "date", null));
		dbc.bindValue(SWTObservables.observeText(link), BeansObservables
				.observeDetailValue(messageValue, "from", null), null, new UpdateValueStrategy().setConverter(new IConverter(){
				
					public Object getToType() {
						return String.class;
					}
				
					public Object getFromType() {
						return String.class;
					}
				
					public Object convert(Object from) {
						String withLink = from == null ? "" : from.toString(); //$NON-NLS-1$
						// make sure the string is wrapped
						if (!withLink.startsWith("<a>")) {
							withLink = "<a>" + withLink + "</a>";
						}
						return withLink;
					}
				}));
		activateHandlers();
	}

	// it is important to implement setFocus()!
	public void setFocus() {
		bodyText.setFocus();
	}

	private WritableValue messageValue = new WritableValue();
	private MarkViewAsSpamAndMoveHandler markAsSpamHandler;
	private DeleteViewMessageHandler deleteHandler;

	public void setMessage(Message message) {
		messageValue.setValue(message);
		if (message == null) {
			stackLayout.topControl = emptyComposite;
		} else {
			stackLayout.topControl = messageComposite;
		}
		stackComposite.layout(true, true);
		markAsSpamHandler.setEnabled(message != null);
		deleteHandler.setEnabled(message != null);
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (part instanceof MessageTableView
				&& selection instanceof IStructuredSelection) {
			IStructuredSelection treeSelection = (IStructuredSelection) selection;
			Object message = treeSelection.getFirstElement();
			if (message instanceof Message) {
				setMessage((Message) message);
			} else {
				setMessage(null);
			}
		} else {
			setMessage(null);
		}
	}

	public Message getMessage() {
		return (Message) messageValue.getValue();
	}

	public void dispose() {
		dbc.dispose();
		super.dispose();
	}

	private void activateHandlers() {
		IHandlerService hs = (IHandlerService) getSite().getService(
				IHandlerService.class);
		markAsSpamHandler = new MarkViewAsSpamAndMoveHandler(this);
		hs.activateHandler(MarkAsSpamAndMoveHandler.MARK_AS_SPAM_COMMAND_ID,
				markAsSpamHandler);
		deleteHandler = new DeleteViewMessageHandler(this);
		hs.activateHandler(IWorkbenchCommandConstants.EDIT_DELETE,
				deleteHandler);
	}
}
