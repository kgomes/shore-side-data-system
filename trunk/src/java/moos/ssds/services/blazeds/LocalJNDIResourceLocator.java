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

import java.text.MessageFormat;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Resource locator that locates local resources using JNDI.
 */
public class LocalJNDIResourceLocator implements IResourceLocator {
	/**
	 * @see moos.ssds.services.blazeds.IResourceLocator#locate(String name)
	 */
	public Object locate(final String name) throws ResourceException {
		try {
			// Grab the local naming context from the container
			final Context ctx = new InitialContext();
			
			// Try to lookup an Object that has the approriate name
			final Object res = ctx.lookup(name);
			
			// Return the results
			return res;
		} catch (NamingException e) {
			// If nothing was found, throw a ResourceException
			throw new ResourceException(
					MessageFormat.format("Error locating local resource {0}",
							new Object[] { name }), e);
		}
	}

}
