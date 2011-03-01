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
package moos.ssds.services.servlet.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import javax.ejb.EJBLocalObject;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import moos.ssds.metadata.util.MetadataFactory;
import moos.ssds.services.metadata.IMetadataAccess;

import org.apache.log4j.Logger;

public class ServletUtils {

	/**
	 * This method takes in a <code>Class</code> and returns all the methods
	 * available and the way they would be called in the servlet
	 * 
	 * @param classToExamine
	 *            is the <code>Class</code> that will be examined for methods
	 * @param format
	 *            is the format that return is in (should match one of the
	 *            formats spelled out in the ServicesAccessServlet
	 * @return
	 */
	public static String getMethodListing(Class classToExamine, String format) {
		// Create a StringBuffer
		StringBuffer methodSignatureBuffer = new StringBuffer();
		AbstractMessageFormat messageFormat;

		// If format not specified, choose default
		if (format == null) {
			format = HTML_FORMAT;
		}
		if (format.equals(TEXT_FORMAT) || format.equals(STRING_FORMAT)) {
			messageFormat = new TextFormat();
		} else if (format.equals(XML_FORMAT)) {
			messageFormat = new XmlFormat();
		} else {
			messageFormat = new HtmlFormat();
		}

		// Start return based on format
		methodSignatureBuffer.append(messageFormat.getMethodsTitle());

		// Grab all the methods on the class
		Method[] classMethods = classToExamine.getMethods();

		// Now loop through and construct each method call
		TreeMap methodSignatureMap = new TreeMap();
		for (int i = 0; i < classMethods.length; i++) {
			StringBuffer methodSignatureStringBuffer = new StringBuffer();
			if ((format.equals(TEXT_FORMAT)) || (format.equals(STRING_FORMAT))) {
				methodSignatureStringBuffer.append((i + 1) + ". ");
			} else {
				methodSignatureStringBuffer.append(messageFormat
						.getBeginMethod());
			}
			methodSignatureStringBuffer.append("method="
					+ classMethods[i].getName());
			// Now grab the parameter type (classes)
			Class[] parameterTypes = classMethods[i].getParameterTypes();
			// Loop through and build the parameter strings
			for (int j = 0; j < parameterTypes.length; j++) {
				methodSignatureStringBuffer.append("&p" + (j + 1) + "Type="
						+ parameterTypes[j].getName());
				methodSignatureStringBuffer.append("&p" + (j + 1)
						+ "Value=XXXXX");
			}
			methodSignatureStringBuffer
					.append("&delimiter=XXX&responseType=text|string|(html)|xml");
			methodSignatureStringBuffer.append(messageFormat.getEndMethod());
			// Now stuff it in the tree map using the method name as key
			methodSignatureMap.put(classMethods[i].getName(),
					methodSignatureStringBuffer.toString());
		}
		// Now that we have the map, print them out in order
		Set keys = methodSignatureMap.keySet();
		Iterator keyIter = keys.iterator();
		while (keyIter.hasNext()) {
			String key = (String) keyIter.next();
			methodSignatureBuffer.append((String) methodSignatureMap.get(key));
		}
		// Tack on any footer
		methodSignatureBuffer.append(messageFormat.getFooter());
		return methodSignatureBuffer.toString();
	}

	/**
	 * This method returns an instance of the local EJB interface class that is
	 * given that has been looked up and is ready to use
	 * 
	 * @param metadataClass
	 *            is the <code>IMetadataObject</code> class that you want to
	 *            find the associated access class for
	 * @return
	 */
	public static IMetadataAccess getLocalAccessInterface(Class metadataClass) {
		// The IMetadataAccess
		IMetadataAccess metadataAccessToReturn = null;

		// Grab just the class name
		String className = metadataClass.getName().substring(
				metadataClass.getName().lastIndexOf(".") + 1);
		String jndiLookup = "moos/ssds/services/metadata/" + className
				+ "Access";

		// Look it up from the container
		Context context = null;
		try {
			context = new InitialContext();
		} catch (NamingException e5) {
			// TODO Auto-generated catch block
			e5.printStackTrace();
		}
		if (context != null) {
			try {
				metadataAccessToReturn = (IMetadataAccess) context
						.lookup(jndiLookup);
			} catch (NamingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// The first thing to do is find the appropriate utility class for the
		// class named
		// Object utilObject = MetadataFactory
		// .getObject("moos.ssds.services.metadata."
		// + metadataClass.getName().substring(
		// metadataClass.getName().lastIndexOf(".") + 1)
		// + "AccessUtil");
		// // Check to make sure the Util class was found
		// if (utilObject != null) {
		// // The parameters (which are empty)
		// Class[] params = new Class[] {};
		// // Try to get the method named "getLocalHome"
		// Method getLocalHomeMethod = null;
		// try {
		// getLocalHomeMethod = utilObject.getClass().getMethod(
		// "getLocalHome", params);
		// } catch (SecurityException e) {
		// logger.error("SecurityException caught: " + e.getMessage());
		// } catch (NoSuchMethodException e) {
		// logger.error("NoSuchMethodException caught: " + e.getMessage());
		// }
		// if (getLocalHomeMethod != null) {
		// Object accessLocalHomeObject = null;
		// try {
		// // Now try to call it
		// accessLocalHomeObject = getLocalHomeMethod.invoke(
		// utilObject, new Object[] {});
		// } catch (IllegalArgumentException e1) {
		// logger.error("IllegalArgumentException caught: "
		// + e1.getMessage());
		// } catch (IllegalAccessException e1) {
		// logger.error("IllegalAccessException caught: "
		// + e1.getMessage());
		// } catch (InvocationTargetException e1) {
		// logger.error("InvocationTargetException caught: "
		// + e1.getMessage());
		// }
		// if (accessLocalHomeObject != null) {
		// // Now in order to create the local interface to the
		// // service, we must use reflection again.
		// Class[] createParams = new Class[] {};
		// Method createMethod = null;
		// try {
		// createMethod = accessLocalHomeObject.getClass()
		// .getMethod("create", createParams);
		// } catch (SecurityException e2) {
		// logger.error("SecurityException caught: "
		// + e2.getMessage());
		// } catch (NoSuchMethodException e2) {
		// logger.error("NoSuchMethodException caught: "
		// + e2.getMessage());
		// }
		// if (createMethod != null) {
		// Object accessLocal = null;
		// try {
		// accessLocal = createMethod.invoke(
		// accessLocalHomeObject, new Object[] {});
		// } catch (IllegalArgumentException e3) {
		// logger.error("IllegalArgumentException caught: "
		// + e3.getMessage());
		// } catch (IllegalAccessException e3) {
		// logger.error("IllegalAccessException caught: "
		// + e3.getMessage());
		// } catch (InvocationTargetException e3) {
		// logger.error("InvocationTargetException caught: "
		// + e3.getMessage());
		// }
		// if (accessLocal != null) {
		// try {
		// metadataAccessToReturn = (IMetadataAccess) accessLocal;
		// } catch (ClassCastException e4) {
		// logger.error("ClassCastException caught: "
		// + e4.getMessage());
		// }
		// }
		// }
		// }
		// }
		//
		// }

		return metadataAccessToReturn;
	}

	/**
	 * 
	 * @param dataAccessClass
	 * @return
	 */
	public static EJBLocalObject getLocalDataAccessInterface(
			Class dataAccessClass) {
		// The Local EJB interface
		EJBLocalObject dataAccessToReturn = null;

		// The first thing to do is find the appropriate utility class for the
		// class named
		Object utilObject = MetadataFactory
				.getObject("moos.ssds.services.data."
						+ dataAccessClass.getName().substring(
								dataAccessClass.getName().lastIndexOf(".") + 1)
						+ "Util");
		// Check to make sure the Util class was found
		if (utilObject != null) {
			// The parameters (which are empty)
			Class[] params = new Class[] {};
			// Try to get the method named "getLocalHome"
			Method getLocalHomeMethod = null;
			try {
				getLocalHomeMethod = utilObject.getClass().getMethod(
						"getLocalHome", params);
			} catch (SecurityException e) {
				logger.error("SecurityException caught: " + e.getMessage());
			} catch (NoSuchMethodException e) {
				logger.error("NoSuchMethodException caught: " + e.getMessage());
			}
			if (getLocalHomeMethod != null) {
				Object accessLocalHomeObject = null;
				try {
					// Now try to call it
					accessLocalHomeObject = getLocalHomeMethod.invoke(
							utilObject, new Object[] {});
				} catch (IllegalArgumentException e1) {
					logger.error("IllegalArgumentException caught: "
							+ e1.getMessage());
				} catch (IllegalAccessException e1) {
					logger.error("IllegalAccessException caught: "
							+ e1.getMessage());
				} catch (InvocationTargetException e1) {
					logger.error("InvocationTargetException caught: "
							+ e1.getMessage());
				}
				if (accessLocalHomeObject != null) {
					// Now in order to create the local interface to the
					// service, we must use reflection again.
					Class[] createParams = new Class[] {};
					Method createMethod = null;
					try {
						createMethod = accessLocalHomeObject.getClass()
								.getMethod("create", createParams);
					} catch (SecurityException e2) {
						logger.error("SecurityException caught: "
								+ e2.getMessage());
					} catch (NoSuchMethodException e2) {
						logger.error("NoSuchMethodException caught: "
								+ e2.getMessage());
					}
					if (createMethod != null) {
						Object accessLocal = null;
						try {
							accessLocal = createMethod.invoke(
									accessLocalHomeObject, new Object[] {});
						} catch (IllegalArgumentException e3) {
							logger.error("IllegalArgumentException caught: "
									+ e3.getMessage());
						} catch (IllegalAccessException e3) {
							logger.error("IllegalAccessException caught: "
									+ e3.getMessage());
						} catch (InvocationTargetException e3) {
							logger.error("InvocationTargetException caught: "
									+ e3.getMessage());
						}
						if (accessLocal != null) {
							try {
								dataAccessToReturn = (EJBLocalObject) accessLocal;
							} catch (ClassCastException e4) {
								logger.error("ClassCastException caught: "
										+ e4.getMessage());
							}
						}
					}
				}
			}

		}

		return dataAccessToReturn;
	}

	/**
	 * These are the different formats that the message can be returned in
	 */
	public static final String TEXT_FORMAT = "text";
	public static final String STRING_FORMAT = "string";
	public static final String HTML_FORMAT = "html";
	public static final String XML_FORMAT = "xml";

	/**
	 * The message that will be returned if the request is not understood
	 */
	public static final String DEFAULT_DELIMITER = "|";

	/**
	 * A log4j Logger
	 */
	static Logger logger = Logger.getLogger(ServletUtils.class);
}

class AbstractMessageFormat {
	// returns the string "Methods" properly formatted
	public String getMethodsTitle() {
		return "";
	}

	// the beginning of a method listing
	public String getBeginMethod() {
		return "";
	}

	public String getEndMethod() {
		return "";
	}

	public String getFooter() {
		return "";
	}
}

class XmlFormat extends AbstractMessageFormat {
	public String getMethodsTitle() {
		return "<ssds:Methods>";
	}

	public String getBeginMethod() {
		return "<ssds:Method><![CDATA[";
	}

	public String getEndMethod() {
		return "]]></ssds:Method>";
	}

	public String getFooter() {
		return "</ssds:Methods>";
	}
}

class HtmlFormat extends AbstractMessageFormat {
	public String getMethodsTitle() {
		return "<br><b>Methods:</b><br><ol>";
	}

	public String getBeginMethod() {
		return "<li><pre>";
	}

	public String getEndMethod() {
		return "</pre></li>";
	}

	public String getFooter() {
		return "</ol>";
	}
}

class TextFormat extends AbstractMessageFormat {
	public String getMethodsTitle() {
		return "\nMethods:\n";
	}

	public String getEndMethod() {
		return "\n";
	}
}
