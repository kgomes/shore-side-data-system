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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import javax.jms.BytesMessage;
import javax.sql.DataSource;

import moos.ssds.io.util.PacketUtility;

import org.apache.log4j.Logger;

/**
 * <p>
 * Class for writing <code>DataPackets</code> to a relational database.
 * </p>
 * <hr>
 * 
 * @author : $Author: kgomes $
 * @version : $Revision: 1.2.2.1 $
 */
public class PacketSQLOutput {

	/**
	 * This is the <code>DataSource</code> that the connections will be pulled
	 * from to serialize the data to the DB
	 */
	private DataSource dataSource;

	/**
	 * The string that is used as a delimiter for table queries
	 */
	private String sqlTableDelimiter = null;

	/**
	 * A log4j logger
	 */
	static Logger logger = Logger.getLogger(PacketSQLOutput.class);

	/**
	 * No argument constructor. If created with this you will need to call the
	 * <code>setDataSource(DataSource dataSource)</code> method before calling
	 * <code>writeObject</code>
	 */
	public PacketSQLOutput() {
	}

	/**
	 * This is the constructor that takes in the <code>DataSource</code> where
	 * the packets will be serialized to.
	 * 
	 * @param dataSource
	 *            is the <code>DataSource</code> where the
	 *            <code>SSDSDevicePackets</code> will be written to.
	 */
	public PacketSQLOutput(DataSource dataSource, String sqlTableDelimiter) {
		setDataSource(dataSource);
		this.sqlTableDelimiter = sqlTableDelimiter;
	}

	/**
	 * Specifies the <code>DataSource</code> that will be used to grab database
	 * connections from .
	 * 
	 * @param dataSource
	 *            is the <code>DataSource</code> that will be used to obtain
	 *            <code>Connection</code> s from.
	 */
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * This method returns the <code>DataSource</code> where this
	 * <code>PacketSQLOutput</code> is serializing
	 * <code>SSDSDevicePackets</code> to.
	 * 
	 * @return The <code>DataSource</code> that an instance of PacketOutput is
	 *         writing to.
	 */
	public DataSource getDataSource() {
		return dataSource;
	}

	/**
	 * This writes the byte array out in the current version. There really is no
	 * "version" control here except with what is hard coded.
	 * 
	 * @param bytes
	 */
	public synchronized void writeBytesMessage(BytesMessage bytesMessage)
			throws SQLException {
		this.writeBytesMessageVersion3(bytesMessage);
	}

	/**
	 * This method extracts the byte array from the BytesMessage and the records
	 * it in the database.
	 * 
	 * @param bytesMessage
	 * @throws SQLException
	 */
	private void writeBytesMessageVersion3(BytesMessage bytesMessage)
			throws SQLException {
		// Convert the bytes and then perist
		writeBytesVersion3(PacketUtility
				.extractByteArrayFromBytesMessage(bytesMessage));
	}

	/**
	 * This method simply calls the correct version method that writes the bytes
	 * to the database
	 * 
	 * @param bytes
	 * @throws SQLException
	 */
	public void writeBytes(byte[] bytes) throws SQLException {
		this.writeBytesVersion3(bytes);
	}

	/**
	 * This method takes in a byte array that must be in SSDS format and then
	 * records the entry in the database
	 * 
	 * @param bytes
	 * @throws SQLException
	 */
	public void writeBytesVersion3(byte[] bytes) throws SQLException {

		// Log the message
		logger.debug("writeBytesVersion3 called.");
		PacketUtility.logVersion3SSDSByteArray(bytes, false);

		// Setup the streams to read the bytes
		ByteArrayInputStream byteIS = new ByteArrayInputStream(bytes);
		DataInputStream dataIS = new DataInputStream(byteIS);

		// This assumes that this byte array is in the form of the SSDS
		// specification
		long deviceID = -999999;
		long parentID = -999999;
		int packetType = -999999;
		long packetSubType = -999999;
		long dataDescriptionID = -999999;
		long dataDescriptionVersion = -999999;
		long timestampSeconds = -999999;
		long timestampNanoseconds = -999999;
		long sequenceNumber = -999999;
		int bufferLen = 1;
		byte[] bufferBytes = new byte[bufferLen];
		int bufferTwoLen = 1;
		byte[] bufferTwoBytes = new byte[bufferTwoLen];
		try {
			deviceID = dataIS.readLong();
			parentID = dataIS.readLong();
			packetType = dataIS.readInt();
			packetSubType = dataIS.readLong();
			dataDescriptionID = dataIS.readLong();
			dataDescriptionVersion = dataIS.readLong();
			timestampSeconds = dataIS.readLong();
			timestampNanoseconds = dataIS.readLong();
			sequenceNumber = dataIS.readLong();
			bufferLen = dataIS.readInt();
			bufferBytes = new byte[bufferLen];
			dataIS.read(bufferBytes);
			bufferTwoLen = dataIS.readInt();
			bufferTwoBytes = new byte[bufferTwoLen];
			dataIS.read(bufferTwoBytes);
		} catch (IOException e) {
			logger.error("IOException caught: " + e.getMessage());
		}
		// Grab a connection
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
		} catch (SQLException e) {
			logger.error("Could not get a connection to the database: "
					+ e.getMessage());
		}
		// Try the write
		try {
			// Prepare the statement to insert the data
			PreparedStatement pstmt = connection
					.prepareStatement("INSERT INTO " + this.sqlTableDelimiter
							+ deviceID + this.sqlTableDelimiter + " VALUES "
							+ "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			pstmt.clearParameters();
			pstmt.setInt(1, 3);
			pstmt.setLong(2, parentID);
			pstmt.setInt(3, packetType);
			pstmt.setLong(4, packetSubType);
			pstmt.setLong(5, dataDescriptionID);
			pstmt.setLong(6, dataDescriptionVersion);
			pstmt.setLong(7, timestampSeconds);
			pstmt.setLong(8, timestampNanoseconds);
			pstmt.setLong(9, sequenceNumber);
			pstmt.setInt(10, bufferLen);

			InputStream in = new ByteArrayInputStream(bufferBytes);
			pstmt.setBinaryStream(11, in, bufferLen);
			pstmt.setInt(12, bufferTwoLen);
			in = new ByteArrayInputStream(bufferTwoBytes);
			pstmt.setBinaryStream(13, in, bufferTwoLen);
			pstmt.setNull(14, Types.DOUBLE);
			pstmt.setNull(15, Types.DOUBLE);
			pstmt.setNull(16, Types.FLOAT);
			pstmt.executeUpdate();
			pstmt.close();
		} catch (SQLException e1) {
			logger.error("SQLException while trying to insert data: "
					+ e1.getMessage());
		}

		// Close the connection
		connection.close();
	}
}