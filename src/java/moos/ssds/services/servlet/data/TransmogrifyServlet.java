package moos.ssds.services.servlet.data;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import moos.ssds.util.XmlDateFormat;

import org.apache.log4j.Logger;

/**
 * This is a servlet that takes in a request that should have data parameters in
 * SIAM format. It takes that information, creates the right form of message and
 * then sends it on to the Transmogrify JMS topic
 * 
 * @author kgomes
 * 
 */
public class TransmogrifyServlet extends HttpServlet {

	/**
	 * A default serial version ID number
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * A log4j logger
	 */
	private Logger logger = Logger.getLogger(TransmogrifyServlet.class);

	/**
	 * The ArrayList that contains the list of parameters that are understood by
	 * this servlet
	 */
	private ArrayList<String> validParameters = new ArrayList<String>();

	/**
	 * This is a session that the publishing of messages will be run in.
	 */
	private Session session = null;

	/**
	 * This is the message producer that is actually used to send messages to
	 * the
	 */
	private MessageProducer messageProducer = null;

	/**
	 * A formatter that will be used to parse incoming dates into milliseconds
	 */
	private XmlDateFormat xmlDateFormat = new XmlDateFormat();

	/**
	 * Default constructor
	 */
	public TransmogrifyServlet() {
		logger.debug("Default constructor called");

		// Build the list of parameters that are understood
		validParameters.add("response");
		validParameters.add("StreamID");
		validParameters.add("DevicePacketVersion");
		validParameters.add("SourceID");
		validParameters.add("Timestamp");
		validParameters.add("SequenceNumber");
		validParameters.add("MetadataRef");
		validParameters.add("ParentID");
		validParameters.add("RecordType");
		validParameters.add("SecondStreamID");
		validParameters.add("SecondPacketVersion");
		validParameters.add("FirstBufferLength");
		validParameters.add("FirstBuffer");
		validParameters.add("SecondBufferLength");
		validParameters.add("SecondBuffer");

		// Grab the naming context from the container
		Context context = null;
		try {
			context = new InitialContext();
		} catch (NamingException e) {
			logger.error("NamingException caught trying to "
					+ "get initial context: " + e.getMessage());
		}

		// Look up the connection factory
		ConnectionFactory connectionFactory = null;
		if (context != null) {
			try {
				connectionFactory = (ConnectionFactory) context
						.lookup("ConnectionFactory");
			} catch (NamingException e) {
				logger.error("NamingException caught trying "
						+ "to find the connection factory: " + e.getMessage());
			}
		}

		// Look up the destination to send message to
		Destination destination = null;
		if (context != null) {
			try {
				destination = (Destination) context
						.lookup("topic/SSDSTransmogTopic");
			} catch (NamingException e) {
				logger.error("NamingException caught trying "
						+ "to find the destination for Transmogrify: "
						+ e.getMessage());
			}
		}

		// Try to create the connection
		Connection connection = null;
		if (connectionFactory != null) {
			// Create a connection
			try {
				connection = connectionFactory.createConnection();
			} catch (JMSException e) {
				logger.error("JMSException caught trying to create "
						+ "the connection from the connection factory: "
						+ e.getMessage());
			}

			// If it worked, create a session
			if (connection != null) {
				// Create a session
				try {
					session = connection.createSession(false,
							Session.AUTO_ACKNOWLEDGE);
				} catch (JMSException e) {
					logger.error("JMSException caught trying to create "
							+ "the session from the connection:"
							+ e.getMessage());
				}

				// If the session was good, try to create a producer
				if (session != null) {
					if (destination != null) {
						// Create a message producer
						try {
							messageProducer = session
									.createProducer(destination);
						} catch (JMSException e) {
							logger.error("JMSException caught trying "
									+ "to create the producer from the "
									+ "session: " + e.getMessage());
						}
						// Log an error if the destination was not created
						if (messageProducer == null)
							logger.error("Was not able to "
									+ "create a MessageProducer");
					} else {
						logger.error("The destination that was to be "
								+ "injected by the container "
								+ "appears to be null");
					}
				} else {
					logger.error("Could not seem to create a "
							+ "session from the connection");
				}
			} else {
				logger.error("Could not seem to create a connection "
						+ "using the injected connection factory");
			}
		} else {
			logger.error("ConnectionFactory is NULL and should "
					+ "have been injected by the container!");
		}
	}

	/**
	 * This is the implementation of the method to return some information about
	 * what this particular servlet does
	 */
	public String getServletInfo() {
		return "This servlet takes in parameters that describe the "
				+ "contents of a SIAM formatted packet and constructs a "
				+ "message to send to the Transmogrify JMS topic";
	}

	/**
	 * This is the doPost method defined in the HTTPServlet. In this case, it
	 * simply calls the doGet method passing the request response pair
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

	/**
	 * This is the method that processes the incoming messages. The parameters
	 * that can be sent in are (required are listed in bold):
	 * 
	 * <ol>
	 * <li>StreamID</li>
	 * <li>DevicePacketVersion</li>
	 * <li><b>SourceID</b></li>
	 * <li><b>Timestamp</b></li>
	 * <li><b>SequenceNumber</b></li>
	 * <li>MetadataRef</li>
	 * <li>ParentID</li>
	 * <li>RecordType</li>
	 * <li>SecondStreamID</li>
	 * <li>SecondPacketVersion</li>
	 * <li>FirstBufferLength</li>
	 * <li>FirstBuffer</li>
	 * <li>SecondBufferLength</li>
	 * <li>SecondBuffer</li>
	 * </ol>
	 */
	@SuppressWarnings("unchecked")
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		logger.debug("doGet called");

		// A boolean to track the extraction of parameters
		boolean validRequest = true;

		// The message builder (if needed)
		StringBuilder messageBuilder = new StringBuilder();

		// First, let's make sure all the parameters are understood
		try {
			parametersUnderstood(request);
		} catch (IllegalArgumentException e1) {
			messageBuilder.append("\n<li>Some parameters were not "
					+ "recognized (<b>These are case sensitive!</b>)<ol>"
					+ e1.getMessage() + "</ol></li>");
			messageBuilder.append("\n<li>Valid parameters are:<ol>");
			for (Iterator<String> iterator = validParameters.iterator(); iterator
					.hasNext();) {
				messageBuilder.append("\n<li>" + iterator.next() + "</li>");
			}
			messageBuilder.append("\n</ol></li>");
			validRequest = false;
		}

		// Grab the stream ID
		short streamID = 0x0100;
		try {
			streamID = extractStreamID(request);
			// If the parameter was not specified, set it to default
			if (streamID == -1)
				streamID = 0x100;
		} catch (IllegalArgumentException e) {
			messageBuilder.append("<li>" + e.getMessage() + "</li>");
			validRequest = false;
		}

		// Grab the device packet version
		long devicePacketVersion = 0;
		try {
			devicePacketVersion = extractDevicePacketVersion(request);
			// If it was not specified set it to some default
			if (devicePacketVersion == -1)
				devicePacketVersion = 0;
		} catch (IllegalArgumentException e) {
			messageBuilder.append("<li>" + e.getMessage() + "</li>");
			validRequest = false;
		}

		// Grab the device ID (source ID)
		long sourceID = -1;
		try {
			sourceID = extractSourceID(request);
		} catch (IllegalArgumentException e) {
			messageBuilder.append("<li>" + e.getMessage() + "</li>");
			validRequest = false;
		}
		// Since SourceID is required, make sure it was parsed from the request
		if (sourceID == -1) {
			messageBuilder
					.append("<li>SourceID was not specified, this is required</li>");
			validRequest = false;
		}

		// Grab the timestamp in epoch millis
		long timestamp = -1;
		try {
			timestamp = extractTimestamp(request);
		} catch (IllegalArgumentException e) {
			messageBuilder.append("<li>" + e.getMessage() + "</li>");
			validRequest = false;
		}
		// Since Timestamp is required, make sure it was parsed from the request
		if (timestamp == -1) {
			messageBuilder
					.append("<li>Timestamp was not specified, this is required</li>");
			validRequest = false;
		}

		// If the request was valid, construct and publish the message
		if (validRequest) {
		}

		// If a response was requested, send it
		if (request.getParameter("response") != null
				&& request.getParameter("response").equals("true")) {
			response.setContentType("text/html");

			// Create a print writer to make it easier to print out responses
			PrintWriter out = new PrintWriter(response.getOutputStream());

			// First thing we need to do is verify the parameters
			out.print("\n<h3>You Submitted</h3>");
			out.print("\n<ol>");
			Map<String, String[]> parameterMap = request.getParameterMap();
			for (Iterator<String> iterator = parameterMap.keySet().iterator(); iterator
					.hasNext();) {
				String key = iterator.next();
				String[] parameterValues = (String[]) parameterMap.get(key);
				out
						.print("\n  <li>" + key + "=" + parameterValues[0]
								+ "</li>");
			}
			out.print("\n</ol>");

			out.print("\n<h3>Results: ");
			out.print((validRequest) ? "Success" : "Failed");
			out.print("</h3>");
			out.print("\n<ol>");
			out.print(messageBuilder.toString());
			out.print("</ol>");
			out.flush();
		}

		return;
	}

	/**
	 * This method takes in the servlet request and makes sure that all the
	 * parameters that were specified are recognized as valid parameters. This
	 * is to prevent somebody from spelling something wrong and thinking it goes
	 * in OK, while the servlet simply ignores it. Case sensitivity is one
	 * example.
	 * 
	 * @param request
	 * @return
	 * @throws IllegalArgumentException
	 */
	@SuppressWarnings("unchecked")
	private boolean parametersUnderstood(HttpServletRequest request)
			throws IllegalArgumentException {
		// The flag to indicate validity
		boolean valid = true;

		// The string builder to track which parameters are not valid
		StringBuilder messageBuilder = new StringBuilder();

		// Grab all the parameter names
		Enumeration<String> parameterNames = request.getParameterNames();

		// Verify each one
		while (parameterNames.hasMoreElements()) {
			String parameterName = parameterNames.nextElement();
			if (!validParameters.contains(parameterName)) {
				valid = false;
				messageBuilder.append("\n<li>" + parameterName + "</li>");
			}
		}

		// Throw an exception if not all the parameters were valid
		if (!valid) {
			throw new IllegalArgumentException(messageBuilder.toString());
		}

		// Return the flag
		return valid;
	}

	/**
	 * This method takes in the servlet request, tries to find a parameter named
	 * StreamID and then tries to convert the value to a short. If no value was
	 * sent, a -1 will be returned
	 * 
	 * @param request
	 *            is the HttpServletRequest that may contain the StreamID
	 *            parameter
	 * @return the parameter converted to a java short. A -1 will be returned if
	 *         no parameter was specified in the request
	 * @throws IllegalArgumentException
	 *             if the parameter was specified, but could not be converted to
	 *             a short
	 */
	private short extractStreamID(HttpServletRequest request)
			throws IllegalArgumentException {
		// The short to return
		short streamID = -1;
		// Look for the parameter with name StreamID
		if (request.getParameterMap().containsKey("StreamID")) {
			String[] parameterValueArray = (String[]) request.getParameterMap()
					.get("StreamID");
			// Check to see if it is specified in Hex
			if (parameterValueArray[0].contains("x")
					|| parameterValueArray[0].contains("X")) {
				try {
					streamID = Short.decode(parameterValueArray[0]);
				} catch (Exception e) {
					throw new IllegalArgumentException(
							"The StreamID value could "
									+ "not be decoded to a short:"
									+ e.getMessage());
				}
			} else {
				try {
					streamID = Short.parseShort(parameterValueArray[0]);
				} catch (Exception e) {
					throw new IllegalArgumentException(
							"The StreamID value could "
									+ "not be converted to a short: "
									+ e.getMessage());
				}
			}
		}

		// Return the value
		return streamID;
	}

	/**
	 * This method takes in the servlet request, tries to find a parameter named
	 * DevicePacketVersion and then tries to convert the value to a long. If no
	 * value was sent, a -1 will be returned
	 * 
	 * @param request
	 *            is the HttpServletRequest that may contain the
	 *            DevicePacketVersion parameter
	 * @return the parameter converted to a java long. A -1 will be returned if
	 *         no parameter was specified in the request
	 * @throws IllegalArgumentException
	 *             if the parameter was specified, but could not be converted to
	 *             a long
	 */
	private long extractDevicePacketVersion(HttpServletRequest request)
			throws IllegalArgumentException {
		// The long to return
		long devicePacketVersion = -1;
		// Look for the parameter with name DevicePacketVersion
		if (request.getParameterMap().containsKey("DevicePacketVersion")) {
			String[] parameterValueArray = (String[]) request.getParameterMap()
					.get("DevicePacketVersion");
			try {
				devicePacketVersion = Long.parseLong(parameterValueArray[0]);
			} catch (Exception e) {
				throw new IllegalArgumentException(
						"The DevicePacketVersion value could "
								+ "not be converted to a long: "
								+ e.getMessage());
			}
		}

		// Return the value
		return devicePacketVersion;
	}

	/**
	 * This method takes in the servlet request, tries to find a parameter named
	 * SourceID and then tries to convert the value to a long. If no value was
	 * sent, a -1 will be returned
	 * 
	 * @param request
	 *            is the HttpServletRequest that may contain the SourceID
	 *            parameter
	 * @return the parameter converted to a java long. A -1 will be returned if
	 *         no parameter was specified in the request
	 * @throws IllegalArgumentException
	 *             if the parameter was specified, but could not be converted to
	 *             a long
	 */
	private long extractSourceID(HttpServletRequest request)
			throws IllegalArgumentException {
		// The long to return
		long sourceID = -1;
		// Look for the parameter with name SourceID
		if (request.getParameterMap().containsKey("SourceID")) {
			String[] parameterValueArray = (String[]) request.getParameterMap()
					.get("SourceID");
			try {
				sourceID = Long.parseLong(parameterValueArray[0]);
			} catch (Exception e) {
				throw new IllegalArgumentException("The SourceID value could "
						+ "not be converted to a long: " + e.getMessage());
			}
		}

		// Return the value
		return sourceID;
	}

	/**
	 * This method takes in the servlet request, tries to find a parameter named
	 * Timestamp and then tries to convert the value to a long which represents
	 * epoch milliseconds. If no value was sent, a -1 will be returned
	 * 
	 * @param request
	 *            is the HttpServletRequest that may contain the Timestamp
	 *            parameter
	 * @return the parameter converted to a java long. A -1 will be returned if
	 *         no parameter was specified in the request
	 * @throws IllegalArgumentException
	 *             if the parameter was specified, but could not be converted to
	 *             a long
	 */
	private long extractTimestamp(HttpServletRequest request)
			throws IllegalArgumentException {
		// The long to return
		long timestamp = -1;

		// Look for the parameter with name Timestamp
		if (request.getParameterMap().containsKey("Timestamp")) {
			String[] parameterValueArray = (String[]) request.getParameterMap()
					.get("Timestamp");
			// There are two possibilities here, one is the ISO form and the
			// other is epoch millis. Let's look for a ":" or a "/" to see if it
			// is a ISO form
			if (parameterValueArray[0].contains(":")
					|| parameterValueArray[0].contains("-")) {
				// Create a local date to parse into
				Date parsedDate = null;
				try {
					parsedDate = xmlDateFormat.parse(parameterValueArray[0]);
				} catch (Exception e) {
					throw new IllegalArgumentException("The timestamp looked "
							+ "to be in the ISO form, "
							+ "but could not be parsed: " + e.getMessage());
				}
				// If no exception, but no date, throw an exception
				if (parsedDate == null) {
					throw new IllegalArgumentException("The timestamp looked "
							+ "to be in the ISO form, "
							+ "but could not be parsed for some reason.");
				} else {
					timestamp = parsedDate.getTime();
				}
			} else {
				// Try to convert it to a timestamp directly
				try {
					timestamp = Long.parseLong(parameterValueArray[0]);
				} catch (Exception e) {
					throw new IllegalArgumentException(
							"The Timestamp value could "
									+ "not be converted to a long: "
									+ e.getMessage());
				}
			}
		}

		// Return the value
		return timestamp;
	}

}
