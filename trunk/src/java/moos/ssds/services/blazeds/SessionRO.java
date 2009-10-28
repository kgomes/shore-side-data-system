/*
 * Copyright 2009 MBARI
 *
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 2.1 
 * (the "License"); you may not use this file except in compliance 
 * with the License. You may obtain a copy of the License at
 *
 * http://www.gnu.org/copyleft/lesser.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package moos.ssds.services.blazeds;

import org.apache.log4j.Logger;

import flex.messaging.FlexContext;
import flex.messaging.FlexSession;

/**
 * This is a class that contains information about the user in the current
 * FlexContext
 * 
 * @author kgomes
 * 
 */
public class SessionRO {

	/**
	 * The Log4J Logger
	 */
	static Logger logger = Logger.getLogger(SessionRO.class);

	/**
	 * The FlexSession object
	 */
	private FlexSession session = null;

	/**
	 * The default constructor
	 */
	public SessionRO() {
		logger.debug("Default constructor called and FlexContext: ");
		logger.debug("FlexClient = " + FlexContext.getFlexClient());
		logger.debug("FlexSession = " + FlexContext.getFlexSession());
		logger.debug("UserPrincipal = " + FlexContext.getUserPrincipal());
		// Grab the session
		session = FlexContext.getFlexSession();
	}

	/**
	 * This is the method that returns the ID of the FlexSession
	 * 
	 * @return
	 */
	public String getSessionId() {
		logger.debug("getSessionID called");
		if (session != null) {
			logger.debug("Session was not null will return ID of "
					+ session.getId());
			return session.getId();
		} else {
			logger.debug("Session was null, so null ID will be returned");
			return null;
		}
	}

	/**
	 * This method returns the username that is associated with the FlexContext
	 * 
	 * @return
	 */
	public String getCurrentUsername() {
		// Check for the principal in the FlexContext
		if (FlexContext.getUserPrincipal() != null) {
			logger.debug("getCurrentUsername called and will return "
					+ FlexContext.getUserPrincipal().getName());
			return FlexContext.getUserPrincipal().getName();
		} else {
			logger.debug("getCurrentUsername will return guest");
			return "Guest";
		}
	}

	/**
	 * A method to check to see if the current user in the in role name that is
	 * supplied
	 * 
	 * @param role
	 * @return
	 */
	public Boolean isUserInRoleSSDSAdmin() {
		logger.debug("isUserInRoleSSDSAdmin called " + " and will return "
				+ session.isUserInRole("SSDS_Admin"));
		return session.isUserInRole("SSDS_Admin");
	}

}
