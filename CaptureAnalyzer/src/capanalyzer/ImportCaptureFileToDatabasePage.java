package capanalyzer;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class ImportCaptureFileToDatabasePage extends WizardPage
{
	//boolean nameSet = false;
	//boolean filePicked = false;
	Text captureNameText;
	Text filePathAndNameText; 

	public ImportCaptureFileToDatabasePage()
	{
		super("wizardPage");
		setTitle("Import Capture File to Database");
		setDescription("Please enter information to import new capture file to database.");
		setPageComplete(false);
	}

	public void createControl(Composite parent)
	{
		final Composite container = new Composite(parent, SWT.NULL);
		setControl(container);

		final Label captureNameLabel = new Label(container, SWT.NONE);
		captureNameLabel.setText("Capture Name");

		captureNameText = new Text(container, SWT.BORDER);
	    GridData data1 = new GridData(GridData.FILL_HORIZONTAL);
	    data1.horizontalSpan = 7;
	    captureNameText.addKeyListener(new KeyListener() 
	    {   
			public void keyPressed(KeyEvent e)
			{}

			public void keyReleased(KeyEvent e)
			{
				if (captureNameText.getCharCount()!=0) 
				{  	 
					if(filePathAndNameText.getCharCount()!=0)
						setPageComplete(true);   
					else
						setPageComplete(false); 
				}  	
				else
				{
					setPageComplete(false);
				}
			}  
        }); 
	    captureNameText.setLayoutData(data1);
		
		Label filePathNameLabel = new Label(container, SWT.NONE);
		filePathNameLabel.setText("Capture File");

		filePathAndNameText = new Text(container, SWT.BORDER);
		GridData data2 = new GridData(GridData.FILL_HORIZONTAL);
		data2.horizontalSpan = 6;
		filePathAndNameText.setLayoutData(data2);

		Button open = new Button(container, SWT.PUSH);
		open.setText("...");
		open.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event)
			{
				FileDialog fd = new FileDialog(container.getShell(), SWT.OPEN);
				fd.setText("Open");
				String[] filterExt = { "*.erf", "*.cap" };
				fd.setFilterExtensions(filterExt);
				String filePathName = fd.open();
				if (filePathName != null)
				{
					filePathAndNameText.setText(filePathName);
					
					if(captureNameText.getCharCount()!=0)
					{
						setPageComplete(true);
					}
				}
			}
		});

		// The additional spacing (default is 5,5) is for the decorations
		GridLayoutFactory.swtDefaults().numColumns(8).generateLayout(container);
	}
	
	/**
	 * @return the captureNameText
	 */
	public String getCaptureName()
	{
		return captureNameText.getText();
	}

	/**
	 * @return the filePathAndNameText
	 */
	public String getFilePathAndName()
	{
		return filePathAndNameText.getText();
	}
}
