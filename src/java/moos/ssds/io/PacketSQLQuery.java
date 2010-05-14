// $Header: /home/cvs/ssds/src/java/moos/ssds/io/PacketInput.java,v 1.20
// 2005/04/25 15:32:02 kgomes Exp $

package moos.ssds.io;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;

public class PacketSQLQuery implements Enumeration<byte[]> {

	/** A log4j logger */
	static Logger logger = Logger.getLogger(PacketSQLQuery.class);

	/**
	 * This is the factory that will be used to generate the SQL statements to
	 * execute
	 */
	private PacketSQLQueryFactory packetSQLQueryFactory = null;

	/**
	 * This is the DataSource that the packets will be read from
	 */
	private DataSource dataSource = null;

	/* ********************************************** */
	/* These parameters are for direct DB connections */
	/* ********************************************** */
	private String databaseJDBCUrl = null;
	private String username = null;
	private String password = null;
	// This is a boolean that tells the object if this is supposed to be a
	// direct connection to the database or not
	private boolean directConnection = false;
	/* ************************ */
	/* End direct DB connection */
	/* ************************ */

	/**
	 * This is the counter for which row in the overall result set we are on
	 */
	private int rowCounter = 0;

	/**
	 * This is the size of the array of byte arrays that will be used to cache
	 * query results
	 */
	private int pageSize = 50;

	/**
	 * This array holds the results of the query for returning
	 */
	private Object[] resultsCache = new Object[pageSize];

	/**
	 * A boolean to indicate there is not more data
	 */
	private boolean noMoreData = false;

	/**
	 * This constructor takes in the DataSource that will be used to query data
	 * from and the PacketSQLQueryFactory that will be used to generate the SQL
	 * 
	 * @param dataSource
	 * @param deviceID
	 */
	public PacketSQLQuery(DataSource dataSource,
			PacketSQLQueryFactory packetSQLQueryFactory) throws SQLException {
		logger.debug("PacketSQLQuery constructor called with DataSource = "
				+ dataSource + " and PacketSQLQueryFactory = "
				+ packetSQLQueryFactory);
		// Call the method to set the data sources
		this.setDataSource(dataSource);
		this.packetSQLQueryFactory = packetSQLQueryFactory;
	}

	/**
	 * This is the constructor that takes in several strings to setup a database
	 * connection. This is for times when you want to use the
	 * <code>PacketSQLInput</code> outside of a J2EE container.
	 * 
	 * @param databaseDriverClassName
	 * @param databaseJDBCUrl
	 * @param username
	 * @param password
	 * @param deviceID
	 * @throws ClassNotFoundException
	 */
	public PacketSQLQuery(String databaseDriverClassName,
			String databaseJDBCUrl, String username, String password,
			PacketSQLQueryFactory packetSQLQueryFactory) throws SQLException,
			ClassNotFoundException {
		// First check that incoming parameters are OK
		if ((databaseDriverClassName == null) || (databaseJDBCUrl == null)
				|| (username == null) || (password == null))
			throw new SQLException("One of the constructor parameters "
					+ "was not specified, all four must be.");

		// Set the flag for a direct connection
		this.directConnection = true;

		// Set the local variabless
		this.databaseJDBCUrl = databaseJDBCUrl;
		this.username = username;
		this.password = password;

		// Load the DB driver
		Class.forName(databaseDriverClassName);

		// Set the PacketSQLFactory
		this.packetSQLQueryFactory = packetSQLQueryFactory;
	}

	/**
	 * @return Returns the ds.
	 */
	public DataSource getDataSource() {
		return dataSource;
	}

	/**
	 * This method sets the data source that is used to run the queries against.
	 * If the DataSource is null, the class will try to read information from a
	 * properties file and find a DataSource in the JNDI naming service.
	 * 
	 * @param dataSource
	 */
	private void setDataSource(DataSource dataSource) throws SQLException {
		// Set the flag to turn off the direct connection
		this.directConnection = false;
		// If the data source is null, look up one using the local properties
		if (dataSource == null) {
			logger.debug("No DataSource specified, will "
					+ "construct one from the io.properties");
			Properties ioProperties = null;
			// Create and load the io properties
			ioProperties = new Properties();
			try {
				ioProperties.load(this.getClass().getResourceAsStream(
						"/moos/ssds/io/io.properties"));
			} catch (Exception e) {
				logger.error("Exception trying to read in properties file: "
						+ e.getMessage());
			}
			// Grab JNDI stuff
			String jndiHostName = ioProperties
					.getProperty("io.storage.sql.jndi.server.name");
			String dataSourceJndiName = "java:/"
					+ ioProperties.getProperty("io.storage.sql.jndi.name");
			logger.debug("jndiHostName = " + jndiHostName
					+ ", dataSourceJndiName = " + dataSourceJndiName);

			// Now grab the DataSource from the JNDI
			Context jndiContext = null;
			try {
				jndiContext = new InitialContext();
				if ((jndiHostName != null) && (!jndiHostName.equals(""))) {
					jndiContext.removeFromEnvironment(Context.PROVIDER_URL);
					jndiContext.addToEnvironment(Context.PROVIDER_URL,
							jndiHostName + ":1099");
				}
			} catch (NamingException ne) {
				logger
						.error("!!--> A naming exception was caught while trying "
								+ "to get an initial context: "
								+ ne.getMessage());
				return;
			} catch (Exception e) {
				logger
						.error("!!--> An unknown exception was caught while trying "
								+ "to get an initial context: "
								+ e.getMessage());
				return;
			}
			try {
				this.dataSource = (DataSource) jndiContext
						.lookup(dataSourceJndiName);
				logger.debug("DataSource from JNDI DataSource = "
						+ this.dataSource);
			} catch (NamingException e1) {
				logger.error("Could not get DataSource: " + e1.getMessage());
			}

		} else {
			this.dataSource = dataSource;
		}
	}

	/**
	 * This method runs the query using the parameters stored in the
	 * PacketSQLQueryFactory and then holds the result set
	 */
	public void queryForData() throws SQLException {

		// Make sure the factory is there and has a device ID
		if (packetSQLQueryFactory == null
				|| packetSQLQueryFactory.getDeviceID() <= 0)
			throw new SQLException("It appears that no Device ID "
					+ "was specified on the PacketSQLQueryFactory "
					+ "so no query could be run.");

		// Clear the no more data flag
		noMoreData = false;

		// Rest the row counter
		rowCounter = 0;

		// Call the method to fill the results cache
		fillResultsCache();
	}

	/**
	 * This method connects up to the database, makes a query, skips the rows
	 * that have already been read and then takes the next pageSize of results
	 * and puts them in the cache
	 */
	private void fillResultsCache() throws SQLException {
		// Create a connection to the database
		Connection connection = null;

		// Grab a new connection
		if (!directConnection) {
			logger.debug("It is not a direct connection so we "
					+ "will grab a connection from the DataSource");
			connection = this.dataSource.getConnection();
		} else {
			logger.debug("This is a direct connection, so we "
					+ "will use DriverManager to get a connection");
			connection = DriverManager.getConnection(this.databaseJDBCUrl,
					this.username, this.password);
		}

		// Grab the SQL statement from the factory
		String queryString = packetSQLQueryFactory.getQueryStatement();

		// Now let's run it
		logger.debug("SQL statement is: " + queryString.toString());

		// The prepared statement that is created
		PreparedStatement preparedStatement = connection
				.prepareStatement(queryString.toString());

		// Make sure it looks OK
		ResultSet resultSet = null;
		if (preparedStatement != null) {
			// Run the query
			resultSet = preparedStatement.executeQuery();
		}

		// If the result set is OK, try to skip the number of rows that we have
		// already read
		if (resultSet != null) {
			boolean moveToRowOK = true;
			try {
				resultSet.absolute(rowCounter + 1);
			} catch (Exception e) {
				
			}
		}

	}

	/**
	 * This method returns the fields names of the byte array that is returned
	 * with each iteration
	 * 
	 * @return
	 */
	public String[] listFieldNames() {
		String[] fieldNames = null;
		if (packetSQLQueryFactory != null)
			fieldNames = packetSQLQueryFactory.listReturnFields();
		return fieldNames;
	}

	/**
	 * This method returns the classes and their order in the returned byte
	 * arrayF
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Class[] listFieldClasses() {
		Class[] fieldClasses = null;
		if (packetSQLQueryFactory != null)
			fieldClasses = packetSQLQueryFactory.listReturnClasses();
		return fieldClasses;
	}

	/**
	 * This method returns a boolean that indicates if there are more packets
	 * that can be read from the source. If <code>true</code>, the caller should
	 * be able to call <code>nextElement</code> to retrieve another packet.
	 * 
	 * @return a <code>boolean</code> that indicates if more packets can be read
	 *         from the source. More can be read if <code>true</code>, none if
	 *         <code>false</code>.
	 */
	public boolean hasMoreElements() {
		// Set the return to false as the default
		boolean ok = false;
		if ((!noMoreData) && (resultSet != null)) {
			try {
				if (resultSet.isLast()) {
					ok = false;
				} else {
					ok = true;
				}
			} catch (SQLException e) {
				logger.error("SQLException caught trying to call isLast: "
						+ e.getMessage());
			}
		} else {
			ok = false;
		}
		return ok;
	}

	/**
	 * This method closes the results and connections.
	 */
	public void close() {
		logger.debug("Close called on PacketSQLQuery");
		try {
			// Close the result set
			if (resultSet != null)
				resultSet.close();
			// Close the prepared statement
			if (preparedStatement != null)
				preparedStatement.close();
			// Close the connection
			if (connection != null) {
				connection.close();
				connection = null;
			}
		} catch (SQLException e) {
			logger.error("SQLException caught trying to close: "
					+ e.getMessage());
		} catch (Exception e) {
			logger.error("Exception caught trying to close: " + e.getMessage());
		}
	}

	/**
	 * This method implement the nextElement() method from the
	 * <code>Enumeration</code> interface. When called, it returns the next
	 * available byte [] from the packet stream.
	 * 
	 * @return A byte array that should contain the variables from the fields in
	 *         the database
	 */
	public byte[] nextElement() {

		// The byte array that will be returned
		byte[] byteArrayToReturn = null;

		// If there are results, read the next object
		if (resultSet != null) {
			byteArrayToReturn = readByteArray();
		}

		// Now return it
		return byteArrayToReturn;
	}

	/**
	 * This method is called to read a byte array from the stream
	 * 
	 * @return An byte array that is structured based on the query in the
	 *         associated PacketSQLQueryFactory.
	 */
	public byte[] readByteArray() {

		// Here are a couple of streaming support classes
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		DataOutputStream dataOutputStream = new DataOutputStream(
				byteArrayOutputStream);

		// The array of fields and their associated classes that will be read
		// from the result set
		try {
			// Advance cursor and see if there is something to return
			if (resultSet.next()) {

				// I need to loop over the fields that are in the
				// PacketSQLQueryFactory
				for (int i = 0; i < packetSQLQueryFactory.listReturnFields().length; i++) {
					Object columnResult = resultSet
							.getObject(packetSQLQueryFactory.listReturnFields()[i]);
					try {
						if (packetSQLQueryFactory.listReturnClasses()[i] == int.class) {
							dataOutputStream.writeInt((Integer) columnResult);
						} else if (packetSQLQueryFactory.listReturnClasses()[i] == long.class) {
							dataOutputStream.writeLong((Long) columnResult);
						} else if (packetSQLQueryFactory.listReturnClasses()[i] == byte[].class) {
							dataOutputStream.write((byte[]) columnResult);
						}
					} catch (IOException e) {
						logger.error("IOException caught trying to "
								+ "write query results to DataOutputStream: "
								+ e.getMessage());
					}
				}
			} else {
				noMoreData = true;
			}
		} catch (SQLException e) {
			logger.error("SQLException caught trying to readObject: "
					+ e.getMessage());
		}
		// Return the object
		return byteArrayOutputStream.toByteArray();
	}

	/**
	 * The method that is called during garbage collection
	 */
	protected void finalize() throws Throwable {
		// Close up the connections and such
		this.close();
	}
}