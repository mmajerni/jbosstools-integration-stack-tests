package org.jboss.tools.bpel.reddeer.condition;

import org.eclipse.reddeer.common.condition.AbstractWaitCondition;
import org.eclipse.reddeer.common.logging.Logger;
import org.eclipse.reddeer.swt.impl.button.PushButton;
import org.eclipse.reddeer.swt.impl.shell.DefaultShell;

/**
 * Condition that specifies if a progress window is still present
 * 
 * @author apodhrad
 * 
 */
public class IsInProgress extends AbstractWaitCondition {

	public static final String DIALOG_TITLE = "Progress Information";
	public static final String ERROR_TITLE = "Error creating tables in view model AccountView";

	private Logger log = Logger.getLogger(this.getClass());

	@Override
	public boolean test() {
		checkError();
		try {
			new DefaultShell(DIALOG_TITLE);
			return true;
		} catch (Exception e) {
			// ok, not in progress
			return false;
		}
	}

	@Override
	public String description() {
		return "Process still in progress...";
	}

	protected void checkError() {
		try {
			new DefaultShell(ERROR_TITLE);
			log.warn(ERROR_TITLE);
			new PushButton("OK").click();
		} catch (Exception e) {
			// ok, there is no error dialog
		}
	}

}
