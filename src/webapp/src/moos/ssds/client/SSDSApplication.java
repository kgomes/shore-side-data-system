package moos.ssds.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class SSDSApplication implements EntryPoint, ValueChangeHandler<String> {

	static final String PLACE = "login";

	private String startingToken = "";

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		// Setup the GUI
		final Label errorLabel = new Label();
		RootPanel.get("errorLabelContainer").add(errorLabel);

		// Grab the current token
		startingToken = History.getToken();

		// Start by showing the login form
		History.addValueChangeHandler(this);
		History.newItem(SSDSApplication.PLACE, true);
	}

	private void showLogin() {

	}

	private void showMainMenu() {
		if (!startingToken.isEmpty()) {
			History.newItem(startingToken, true);
			startingToken = "";
		}
	}

	@Override
	public void onValueChange(ValueChangeEvent<String> event) {
		executeInPanel(RootPanel.get(), event.getValue());
	}

	public void executeInPanel(Panel myPanel, String token) {
		if (myPanel == null) {
			myPanel = RootPanel.get();
		}
		myPanel.clear();
		if (token.isEmpty()) {
			// Do nothing
		} else if (token.equals(SSDSApplication.PLACE)) {
			// Show login
		} // Need to add panels for more actions
	}
}
