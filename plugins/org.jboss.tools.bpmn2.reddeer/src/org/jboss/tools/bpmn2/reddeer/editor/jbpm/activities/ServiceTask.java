package org.jboss.tools.bpmn2.reddeer.editor.jbpm.activities;

import org.jboss.tools.bpmn2.reddeer.editor.Element;
import org.jboss.tools.bpmn2.reddeer.editor.ElementType;
import org.jboss.tools.bpmn2.reddeer.editor.jbpm.ErrorRef;
import org.jboss.tools.bpmn2.reddeer.editor.jbpm.Message;
import org.jboss.tools.bpmn2.reddeer.editor.jbpm.ParameterMapping;
import org.jboss.tools.bpmn2.reddeer.editor.properties.PropertiesTabs;
import org.jboss.tools.bpmn2.reddeer.editor.properties.SectionToolItemButton;
import org.jboss.tools.bpmn2.reddeer.properties.setup.ParameterMappingSetUp;
import org.jboss.tools.bpmn2.reddeer.properties.setup.CheckBoxSetUp;
import org.jboss.tools.bpmn2.reddeer.properties.setup.ImplementationSetUp;
import org.jboss.tools.bpmn2.reddeer.properties.setup.OperationSetUp;

/**
 * 
 */
public class ServiceTask extends Task {

	/**
	 * 
	 * @param name
	 */
	public ServiceTask(String name) {
		super(name, ElementType.SERVICE_TASK);
	}

	public ServiceTask(Element element) {
		super(element);
	}

	/**
	 * 
	 * @param implementationUri
	 */
	public void setImplementation(String implementationUri) {
		propertiesHandler.setUp(new ImplementationSetUp(PropertiesTabs.SERVICE_TASK_TAB, implementationUri));
	}

	/**
	 * 
	 * @param operationContractName
	 * @param inMessage
	 * @param outMessage
	 * @param errorRef
	 */
	public void setOperation(String operationContractName, Message inMessage, Message outMessage, ErrorRef errorRef) {
		propertiesHandler.setUp(new OperationSetUp(PropertiesTabs.SERVICE_TASK_TAB, operationContractName, inMessage,
				outMessage, errorRef));
	}

	/**
	 *
	 * @param value
	 */
	public void setIsForCompensation(boolean value) {
		propertiesHandler.setUp(new CheckBoxSetUp(PropertiesTabs.SERVICE_TASK_TAB, "Is For Compensation", value));
	}

	/**
	 *
	 * @param parameter
	 */
	public void addParameterMapping(ParameterMapping parameterMapping) {
		propertiesHandler.setUp(new ParameterMappingSetUp(parameterMapping, SectionToolItemButton.ADD));
	}

	public void editParameterMapping(ParameterMapping parameterMapping) {
		propertiesHandler.setUp(new ParameterMappingSetUp(parameterMapping, SectionToolItemButton.EDIT));
	}
}
