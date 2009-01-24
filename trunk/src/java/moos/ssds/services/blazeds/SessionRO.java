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

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import flex.messaging.FlexContext;
import flex.messaging.FlexSession;

public class SessionRO {

	/**
	 * The Log4J Logger
	 */
	static Logger logger = Logger.getLogger(SessionRO.class);
	public HttpServletRequest request;
	public FlexSession session;

	public SessionRO() {
		logger.debug("SessionRO constructor called");
		request = FlexContext.getHttpRequest();
		session = FlexContext.getFlexSession();
	}

	public String getSessionId() {
		return session.getId();
	}

	public String getCurrentUsername() {
		if (FlexContext.getUserPrincipal() != null) {
			logger.debug("getCurrentUsername called and will return "
					+ FlexContext.getUserPrincipal().getName());
			return FlexContext.getUserPrincipal().getName();
		} else {
			return "Guest";
		}
	}

	public boolean isUserInRole(String role) {
		logger.debug("getUserInRole called with role " + role
				+ " and will return " + session.isUserInRole(role));
		return session.isUserInRole(role);
	}

}
