package org.jboss.tools.bpmn2.reddeer.properties.shell;

import org.jboss.reddeer.swt.impl.button.PushButton;
import org.jboss.tools.bpmn2.reddeer.DefaultCombo;
import org.jboss.tools.bpmn2.reddeer.editor.dialog.jbpm.InterfaceDialog;
import org.jboss.tools.bpmn2.reddeer.editor.dialog.jbpm.OperationDialog;
import org.jboss.tools.bpmn2.reddeer.editor.jbpm.ErrorRef;
import org.jboss.tools.bpmn2.reddeer.editor.jbpm.Message;

public class OperationSetUp implements SetUpAble {
	
	private String tabName;
	private String operationContractName;
	private Message inMessage;
	private Message outMessage;
	private ErrorRef errorRef;
	
	
	public OperationSetUp(String tabName, String operationContractName, Message inMessage, Message outMessage, ErrorRef errorRef) {
		this.tabName = tabName;
		this.operationContractName = operationContractName;
		this.inMessage = inMessage;
		this.outMessage = outMessage;
		this.errorRef = errorRef;
	}

	@Override
	public void setUpCTab() {
		DefaultCombo combo = new DefaultCombo("Operation");
		
		String interfaceName = operationContractName.split("/")[0];
		String operationName = operationContractName.split("/")[1];
		
		if (!combo.contains(interfaceName + "/" + operationName)) {
			new PushButton(2).click();
			new InterfaceDialog().select(interfaceName);
			new OperationDialog().addOperation(operationName, inMessage, outMessage, errorRef);
		}
		combo.setSelection(interfaceName + "/" + operationName);
		
		combo = new DefaultCombo("Source");
		if (!combo.contains(inMessage.getName())) {
			throw new UnsupportedOperationException();
		}
		combo.setSelection(inMessage.getName());
		
		combo = new DefaultCombo("Target");
		if (!combo.contains(outMessage.getName())) {
			throw new UnsupportedOperationException();
		}
		combo.setSelection(outMessage.getName());

	}

	@Override
	public String getTabLabel() {
		return tabName;
	}

}
