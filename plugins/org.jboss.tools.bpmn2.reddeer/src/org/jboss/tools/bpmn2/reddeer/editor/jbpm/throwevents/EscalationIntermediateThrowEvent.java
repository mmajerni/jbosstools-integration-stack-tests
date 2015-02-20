package org.jboss.tools.bpmn2.reddeer.editor.jbpm.throwevents;

import org.jboss.tools.bpmn2.reddeer.editor.ElementType;
import org.jboss.tools.bpmn2.reddeer.editor.jbpm.Element;
import org.jboss.tools.bpmn2.reddeer.editor.jbpm.Escalation;
import org.jboss.tools.bpmn2.reddeer.editor.jbpm.eventdefinitions.EscalationEventDefinition;
import org.jboss.tools.bpmn2.reddeer.properties.shell.EventDefinitionSetUp;

/**
 * 
 */
public class EscalationIntermediateThrowEvent extends Element {
	
	/**
	 * 
	 * @param name
	 */
	public EscalationIntermediateThrowEvent(String name) {
		super(name, ElementType.ESCALATION_INTERMEDIATE_THROW_EVENT);
	}
	
	public EscalationIntermediateThrowEvent(org.jboss.tools.bpmn2.reddeer.editor.Element element){
		super(element);
	}

	/**
	 * 
	 * @param escalation
	 */
	public void setEscalation(Escalation escalation, String variableForMapping) {
		select();
		//properties.getTab("Event", EventTab.class).set(new EscalationEventDefinition(escalation, variableForMapping, "Source"));
		propertiesHandler.setUp(new EventDefinitionSetUp(new EscalationEventDefinition(escalation, variableForMapping, "Source")));
		refresh();
	}
	
}
