package org.jboss.tools.teiid.reddeer.wizard.imports;

import java.util.Arrays;

import org.eclipse.reddeer.common.wait.WaitUntil;
import org.eclipse.reddeer.swt.condition.ShellIsAvailable;
import org.eclipse.reddeer.swt.impl.button.CheckBox;
import org.eclipse.reddeer.swt.impl.button.NextButton;
import org.eclipse.reddeer.swt.impl.button.PushButton;
import org.eclipse.reddeer.swt.impl.button.RadioButton;
import org.eclipse.reddeer.swt.impl.combo.DefaultCombo;
import org.eclipse.reddeer.swt.impl.shell.DefaultShell;
import org.eclipse.reddeer.swt.impl.table.DefaultTable;
import org.eclipse.reddeer.swt.impl.text.LabeledText;
import org.eclipse.reddeer.swt.impl.toolbar.DefaultToolItem;

/**
 * Wizard for importing XML schemas
 * 
 * @author lfabriko, apodhrad
 * 
 */
public class XMLSchemaImportWizard extends TeiidImportWizard {

	public static final String DIALOG_TITLE = "Import XML Schema Files";

	private XMLSchemaImportWizard() {
		super(DIALOG_TITLE, "XML Schemas");
		log.info("XML Schema import wizard is opened");
	}

	public static XMLSchemaImportWizard getInstance(){
		return new XMLSchemaImportWizard();
	}
	
	public static XMLSchemaImportWizard openWizard(){
		XMLSchemaImportWizard wizard = new XMLSchemaImportWizard();
		wizard.open();
		return wizard;
	}
	
	public XMLSchemaImportWizard nextPage(){
		log.info("Go to next wizard page");
		new NextButton().click();
		return this;
	}
	
	public XMLSchemaImportWizard activateWizard() {
		new DefaultShell(DIALOG_TITLE);
		return this;
	}
	
	public XMLSchemaImportWizard selectLocalImportMode() {
		return selectImportMode("Import XML schemas from file system");
	}

	public XMLSchemaImportWizard selectRemoteImportMode() {
		return selectImportMode("Import XML schemas via URL");
	}
	
	public XMLSchemaImportWizard selectImportMode(String importMode) {
		log.info("Select import mode to '" + importMode + "'");
		activateWizard();
		new RadioButton(importMode).click();
		return this;
	}
	
	public XMLSchemaImportWizard setFromDirectory(String dir) {
		log.info("Set from directory to '" + dir + "'");
		activateWizard();
		new DefaultCombo(0).setText(dir);
		activateWizard();
		return this;
	}

	public XMLSchemaImportWizard setToDirectory(String dir) {
		log.info("Set to directory to '" + dir + "'");
		activateWizard();
		new LabeledText("Into folder:").setText(dir);
		return this;
	}
	
	public XMLSchemaImportWizard selectSchema(String... schema) {
		log.info("Set schema to '" + Arrays.toString(schema) + "'");
		activateWizard();
		for (int i = 0; i < schema.length; i++) {
			log.info("Select schema '" + schema[i] + "'");
			new DefaultTable().getItem(schema[i]).setChecked(true);
		}
		return this;
	}
	
	public XMLSchemaImportWizard setSchemaURL(String schemaURL, String userName, String password, boolean verifyHostname) {
		log.info("Set schemaURL: '" + schemaURL + "', username: '" + userName +"', password: '" + password +"'");
		activateWizard();
		new DefaultToolItem("Add XML schema URL").click();
		new WaitUntil(new ShellIsAvailable("XML Schema Url"));
		new DefaultShell("XML Schema Url").setFocus();
		new LabeledText("Enter XML schema URL:").setText(schemaURL);
		if(userName != null){
			new LabeledText("User Name").setText(userName);
		}
		if(password != null){
			new LabeledText("Password").setText(password);
		}
		CheckBox checkBox = new CheckBox("Verify Hostname (HTTPS)");
		if(verifyHostname != checkBox.isChecked()){
			checkBox.click();
		}
		new PushButton("OK").click();
		return this;
	}
	
	public XMLSchemaImportWizard addDependentSchemas(boolean checked){
		log.info("Add dependent schemales is : '" + checked + "'");
		activateWizard();
		CheckBox checkBox = new CheckBox("Add Dependent Schema Files");
		if(checked != checkBox.isChecked()){
			checkBox.click();
		}
		return this;
	}
}
