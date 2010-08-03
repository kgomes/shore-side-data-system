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
package moos.ssds.io;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

/**
 * 
 * @author kgomes
 * 
 */
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

	/**
	 * This is the counter for which row in the cache we are on. This means that
	 * this row has already been returned through the nextElement() call.
	 */
	private int cacheRow = -1;

	/**
	 * This is the counter for which row in the overall result set we are on.
	 * This means this row has already been returned from a nextElement() call.
	 */
	private int resultSetRow = -1;

	/**
	 * This is the size of the array of byte arrays that will be used to cache
	 * query results
	 */
	private int cacheSize = 50;

	/**
	 * This array holds the results of the query for returning
	 */
	private Object[] resultsCache = new Object[cacheSize];

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
		logCurrentState();
		// Call the method to set the data sources
		this.setDataSource(dataSource);
		this.packetSQLQueryFactory = packetSQLQueryFactory;
		logger.debug("Data Source set and factory associated");
		logCurrentState();
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
		// If the data source is null, throw an Exception
		if (dataSource == null) {
			throw new SQLException("No DataSource specified.");
		} else {
			this.dataSource = dataSource;
		}
	}

	/**
	 * This method runs the query using the parameters stored in the
	 * PacketSQLQueryFactory and then holds the result set
	 */
	public void queryForData() throws SQLException {
		logger.debug("queryForData called");
		logCurrentState();
		// Make sure the factory is there and has a device ID
		if (packetSQLQueryFactory == null
				|| packetSQLQueryFactory.getDeviceID() <= 0)
			throw new SQLException("It appears that no Device ID "
					+ "was specified on the PacketSQLQueryFactory "
					+ "so no query could be run.");

		// Reset the cache row counter
		cacheRow = -1;

		// Reset the row counter
		resultSetRow = -1;

		// And a new cache
		resultsCache = new Object[cacheSize];

		// Call the method to fill the results cache
		fillResultsCache();
		logger.debug("query for data done");
		logCurrentState();
	}

	/**
	 * This method connects up to the database, makes a query, skips the rows
	 * that have already been read and then takes the next pageSize of results
	 * and puts them in the cache
	 */
	private void fillResultsCache() throws SQLException {
		logger.debug("fillResultsCache called");

		// Create a connection to the database
		Connection connection = null;
		try {
			connection = this.dataSource.getConnection();
		} catch (Exception e) {
			logger.error("Exception caught trying to get "
					+ "Connection from DataSource "
					+ "(not direction connection): " + e.getMessage());
		}

		// Grab the SQL statement from the factory
		String queryString = packetSQLQueryFactory.getQueryStatement();

		// Now let's run it
		logger.debug("SQL statement is: " + queryString.toString());

		// The prepared statement that is created
		PreparedStatement preparedStatement = connection.prepareStatement(
				queryString.toString(), ResultSet.TYPE_SCROLL_INSENSITIVE,
				ResultSet.CONCUR_READ_ONLY);

		// Make sure it looks OK
		ResultSet resultSet = null;
		if (preparedStatement != null) {
			// Run the query
			resultSet = preparedStatement.executeQuery();
			logger.debug("Query was executed");
		} else {
			logger.error("Prepared statement was null!");
		}

		// If the result set is OK, try to skip the number of rows that we have
		// already read and move to the row just before new rows (this is
		// because we will call .next() on the result set)
		if (resultSet != null) {

			boolean moveToRowOK = true;
			if (resultSetRow > 0) {
				logger.debug("Since row counter is " + resultSetRow
						+ " going to try to move to that row in resultSet");
				try {
					resultSet.absolute(resultSetRow + 1);
				} catch (Exception e) {
					logger.error("Exception caught trying to move to row "
							+ (resultSetRow + 1) + " in the resultSet: "
							+ e.getMessage());
					moveToRowOK = false;
				}
			}

			// If it looks like we were able to move ahead to the correct row
			if (moveToRowOK) {
				logger.debug("Looks like we are ready to "
						+ "start filling the cache");

				// Here are a couple of streaming support classes
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				DataOutputStream dataOutputStream = new DataOutputStream(
						byteArrayOutputStream);

				// Now iterate over the number of rows and fill the cache
				for (int i = 0; i < cacheSize; i++) {
					logCurrentState();
					try {
						// Advance cursor and see if there is something to
						// return
						if (resultSet.next()) {
							// Reset the byte array outputstream
							byteArrayOutputStream.reset();

							// I need to loop over the fields that are in the
							// PacketSQLQueryFactory
							for (int j = 0; j < packetSQLQueryFactory
									.listReturnFields().length; j++) {
								try {
									if (packetSQLQueryFactory
											.listReturnClasses()[j] == int.class) {
										Integer integerResult = resultSet
												.getInt(packetSQLQueryFactory
														.listReturnFields()[j]);
										dataOutputStream
												.writeInt(integerResult);
									} else if (packetSQLQueryFactory
											.listReturnClasses()[j] == long.class) {
										Long longResult = resultSet
												.getLong(packetSQLQueryFactory
														.listReturnFields()[j]);
										dataOutputStream.writeLong(longResult);
									} else if (packetSQLQueryFactory
											.listReturnClasses()[j] == byte[].class) {
										byte[] byteResult = resultSet
												.getBytes(packetSQLQueryFactory
														.listReturnFields()[j]);
										dataOutputStream.write(byteResult);
									}
								} catch (IOException e) {
									logger
											.error("IOException caught trying to "
													+ "write query results to DataOutputStream: "
													+ e.getMessage());
								}
							}
							// We should have the full up byte array, so let's
							// stuff it into the page cache
							resultsCache[i] = byteArrayOutputStream
									.toByteArray();
						} else {
							logger.debug("Looks like there is "
									+ "no next() record.");
							resultsCache[i] = null;
						}
					} catch (SQLException e) {
						logger.error("SQLException caught "
								+ "trying to readObject: " + e.getMessage());
					}
				}
			}
			// Reset the cache row counter
			cacheRow = -1;
			logger.debug("Done filling rows");
			logCurrentState();
		} else {
			logger.error("ResultSet was NULL!");
		}

		// Now close everything out
		try {
			logger.debug("Closing everything up: resultSet,  "
					+ "preparedStatement, and connection");
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
		logger.debug("Done with fillResultsCache");
		logCurrentState();
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
		logger.debug("hasMoreElements called with cacheRow = " + cacheRow);
		checkIfCacheNeedsRefresh();

		// Set the return to false as the default
		boolean ok = false;
		if (resultsCache[cacheRow + 1] != null) {
			logger.debug("Will return true to indicate "
					+ "there are more records available");
			ok = true;
		} else {
			logger.debug("Will return false as it looks "
					+ "like there are no more records");
			ok = false;
		}
		return ok;
	}

	/**
	 * This method closes the results and connections.
	 */
	public void close() {
		// Nothing to do here as the connection closing is moved to the cache
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

		// Check if the cache needs refreshing
		checkIfCacheNeedsRefresh();

		// Now move the cacheRow and currentRow index
		cacheRow++;
		resultSetRow++;

		// The byte array that will be returned will be the byte array at the
		// current page index
		byte[] byteArrayToReturn = (byte[]) resultsCache[cacheRow];

		// Now return it
		return byteArrayToReturn;
	}

	/**
	 * 
	 */
	private void checkIfCacheNeedsRefresh() {
		// The first thing to do is check that page counter is not past the end
		// of the results cache
		if (cacheRow + 1 >= cacheSize) {
			logger.debug("ResultSetRow is " + resultSetRow
					+ " which looks to be at the last "
					+ "row of the page so we will need to refresh the cache.");
			// We need to refresh the cache
			try {
				fillResultsCache();
			} catch (SQLException e) {
				logger.error("SQLException caught trying to "
						+ "refresh the cache: " + e.getMessage());
			}
		}
	}

	private void logCurrentState() {
		StringBuilder sb = new StringBuilder();
		sb.append("PacketSQLQuery State:cacheRow = " + cacheRow
				+ ";resultsSetRow = " + resultSetRow);
		sb.append(";ResultsCache Vector = [");
		for (int i = 0; i < cacheSize; i++) {
			if (resultsCache[i] == null) {
				sb.append("0 ");
			} else {
				sb.append("1 ");
			}
		}
		sb.append("]");
		logger.debug(sb.toString());
	}

	/**
	 * The method that is called during garbage collection
	 */
	protected void finalize() throws Throwable {
		// Close up the connections and such
		this.close();
	}
}