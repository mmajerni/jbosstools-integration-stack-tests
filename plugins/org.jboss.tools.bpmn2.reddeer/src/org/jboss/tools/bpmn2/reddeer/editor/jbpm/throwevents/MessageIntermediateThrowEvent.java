package org.jboss.tools.bpmn2.reddeer.editor.jbpm.throwevents;

import org.jboss.tools.bpmn2.reddeer.editor.ElementType;
import org.jboss.tools.bpmn2.reddeer.editor.jbpm.Element;
import org.jboss.tools.bpmn2.reddeer.editor.jbpm.Message;
import org.jboss.tools.bpmn2.reddeer.editor.jbpm.eventdefinitions.MappingType;
import org.jboss.tools.bpmn2.reddeer.properties.shell.MessageSetUpCTab;

/**
 * 
 */
public class MessageIntermediateThrowEvent extends Element {
	
	/**
	 * 
	 * @param name
	 */
	public MessageIntermediateThrowEvent(String name) {
		super(name, ElementType.MESSAGE_INTERMEDIATE_THROW_EVENT);
	}

	public MessageIntermediateThrowEvent(org.jboss.tools.bpmn2.reddeer.editor.Element element){
		super(element);
	}
	
	/**
	 * 
	 * @param message
	 * @param sourceVariable
	 */
	public void setMessageMapping(Message message, String sourceVariable) {
//		properties.getTab("Event", EventTab.class).set(new MessageEventDefinition(message, sourceVariable, Type.SOURCE));
		propertiesHandler.setUp(new MessageSetUpCTab(message, sourceVariable, MappingType.SOURCE));

		refresh();
	}
	
}
