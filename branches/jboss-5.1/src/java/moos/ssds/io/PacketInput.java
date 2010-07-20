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
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;

import moos.ssds.io.util.PacketUtility;

import org.apache.log4j.Logger;

/**
 * <p>
 * This class is used to read back the serialized packets that the Shore-Side
 * Data System uses to store data coming from streaming sources. It implements
 * the Enumeration interface so that elements can be read from it and it can be
 * queried to see if more elements are available.
 * </p>
 * <hr>
 * 
 * @author : $Author: kgomes $
 * @version : $Revision: 1.20 $
 */
public class PacketInput implements Enumeration<Object> {

	/** A log4j logger */
	static Logger logger = Logger.getLogger(PacketInput.class);

	/** The DataInputStream that will be read from */
	private DataInputStream in = null;

	/**
	 * A secondary DataInputStream that will be used to parse a local byte array
	 */
	// private DataInputStream secondaryDIS;

	/** The FileInputStream if a File is used */
	private FileInputStream fin;

	/** The File that is pointed to by this PacketInput */
	private File file;

	/** The URL that this PacketInput points to (if not using files) */
	private URL packetURL = null;

	/** The URLConnection that is used by this PacketInput */
	private URLConnection packetURLConnection = null;
	private HttpURLConnection httpURLConnection = null;

	/** This is a boolean to indicate if the data source points to a URL */
	private boolean urlSource = false;

	/** The number of bytes that have been read into the packet stream */
	private long bytesReadSoFar = 0;

	/*
	 * These variables are used to keep track of what the values of the key
	 * attributes of sourceID, metadataID, parentID, and recordType are. These
	 * are used so that if the reading gets off track, it can be put back on
	 * track.
	 */
	private Long sourceIDForTracking = null;
	private Long parentIDForTracking = null;

	/*
	 * TODO - THIS IS A HACK. Not sure but for some packets, the reading can get
	 * off track and the buffer sizes can be read incorrectly and be REALLY big.
	 * So I am choosing a logical number to say that the sizes should not exceed
	 * those numbers.
	 */

	// This is the max allowable size of the primary buffer;
	private static final int MAX_FIRST_BUFFER_SIZE = 1000000;
	// This is the max allowable size of the secondary buffer
	private static final int MAX_SECOND_BUFFER_SIZE = 500000;

	/**
	 * No argument constructor. If created with this you will need to call the
	 * <code>setFile(File file)</code> or <code>setURL(URL url)</code> method
	 * before calling <code>readObject</code>
	 */
	public PacketInput() {
	}

	/**
	 * This constructor that is used when reading from a file
	 * 
	 * @param file
	 *            is the <code>File</code> object that points to the serialized
	 *            packets to be read
	 * @throws IOException
	 *             if something with the reading goes wrong.
	 */
	public PacketInput(File file) throws IOException {
		// Call the base constructor
		this();
		// The the file to the parameter
		setFile(file);
	}

	/**
	 * The constructor that is used when reading from a file and the client
	 * wants to start reading from some numbers of bytes into the packet stream.
	 * This was implemented using bytes doing this by number of packets to skip
	 * would be too slow (each packet would be read to be skipped. Packets can
	 * be of variable size).
	 * 
	 * @param file
	 *            is the <code>File</code> object that points to the serialized
	 *            packets to be read
	 * @param numBytesToSkip
	 *            is the number of bytes to skip over before starting to read
	 *            from the serialized stream file.
	 * @throws IOException
	 *             if something goes wrong with the read
	 */
	public PacketInput(File file, long numBytesToSkip) throws IOException {
		// Call the base constructor
		this();
		// Call set the file and use the method to skip over a number of bytes
		setFile(file, numBytesToSkip);
	}

	/**
	 * This is the constructor that is used when reading packets from a URL that
	 * points to a serialized stream of packets.
	 * 
	 * @param url
	 *            is the <code>URL</code> that points to the serialized packet
	 *            file
	 * @throws IOException
	 *             if something goes wrong with the read from the URL
	 */
	public PacketInput(URL url) throws IOException {
		this();
		setURL(url);
	}

	/**
	 * The constructor that is used when reading from a URL and the client wants
	 * to start reading from some numbers of bytes into the packet stream. This
	 * was implemented using bytes doing this by number of packets to skip would
	 * be too slow (each packet would be read to be skipped. Packets can be of
	 * variable size).
	 * 
	 * @param file
	 *            is the <code>File</code> object that points to the serialized
	 *            packets to be read
	 * @param numBytesToSkip
	 *            is the number of bytes to skip over before starting to read
	 *            from the serialized stream file.
	 * @throws IOException
	 *             if something goes wrong with the read
	 */
	public PacketInput(URL url, long numBytesToSkip) throws IOException {
		// Call base constructor
		this();
		// Set the url and skip forward a number of bytes
		setURL(url, numBytesToSkip);
	}

	/**
	 * This methods sets up the reading streams and offsets into the stream by
	 * the given number of bytes
	 * 
	 * @param file
	 *            is the <code>File</code> that contains the serialized packets
	 * @param numBytesToSkip
	 *            is the number of bytes to skip past before starting to read
	 *            from the serialized stream. This helps speed up some access
	 *            methods.
	 * @throws IOException
	 *             if something goes wrong with the reading
	 */
	public void setFile(File file, long numBytesToSkip) throws IOException {

		// Since the file is being reset, also reset the number of bytes
		// that have been read so far
		bytesReadSoFar = 0;

		// Close the DataInputStream if there is one
		if (in != null) {
			in.close();
		}
		// Close the FileInputStream if there is one
		if (fin != null) {
			fin.close();
		}

		// Set the file
		this.file = file;

		// Set the tracking keys
		String fileName = file.getName();
		// Now split off the four keys
		StringTokenizer stok = new StringTokenizer(fileName, "_");
		if (stok.hasMoreTokens()) {
			try {
				sourceIDForTracking = new Long(stok.nextToken());
			} catch (Exception e) {
			}
		}
		if (stok.hasMoreTokens()) {
			try {
				stok.nextToken();
			} catch (Exception e) {
			}
		}
		if (stok.hasMoreTokens()) {
			try {
				stok.nextToken();
			} catch (Exception e) {
			}
		}
		if (stok.hasMoreTokens()) {
			try {
				parentIDForTracking = new Long(stok.nextToken());
			} catch (Exception e) {
			}
		}

		// Clear the urlSource flags
		this.urlSource = false;

		// Create the stream input objects and DO NOT use a BufferedInputStream.
		// This throws off the available() method of FileInputStream so that
		// only one object can be retrieved.
		fin = new FileInputStream(file);
		in = new DataInputStream(fin);

		// Now skip ahead the number of bytes specified (if more than zero)
		if (numBytesToSkip > 0) {
			in.skip(numBytesToSkip);
			this.bytesReadSoFar += numBytesToSkip;
		}
	}

	/**
	 * This method sets up the input streams that read from a file. The file
	 * contains serialized packets.
	 * 
	 * @param file
	 *            is a <code>File</code> object that points to the file of
	 *            serialized packets
	 * @throws IOException
	 *             if something goes wrong with the file opening/setup
	 */
	public void setFile(File file) throws IOException {
		// Set the number of bytes to skip past to zero
		long numBytesToSkip = 0;
		// Call the setup method
		this.setFile(file, numBytesToSkip);
	}

	/**
	 * This method sets up the input streams that are reading from a URL and
	 * skip over the given number of bytes from that stream.
	 * 
	 * @param url
	 *            is the <code>URL</code> that points to the file that contains
	 *            serialized packets
	 * @param numBytesToSkip
	 *            is the number of bytes to read past after setting up the input
	 *            streams from the URL.
	 * @throws IOException
	 *             is something goes wrong in the opening or setup of the URL
	 *             and skip method.
	 */
	public void setURL(URL url, long numBytesToSkip) throws IOException {
		// Try to load some properties from a properties file. If the properties
		// file is found and there is a property called ingest.storage.directory
		// then we can see if the file specified in the URL is available from
		// the local storage.
		Properties ingestProps = new Properties();
		try {
			ingestProps.load(this.getClass().getResourceAsStream(
					"/moos/ssds/io/io.properties"));
		} catch (Exception e) {
			logger.error("Exception trying to read io.properties file: "
					+ e.getMessage());
		}
		// Check for property
		String localPacketStoragePath = ingestProps
				.getProperty("io.storage.directory");
		if ((localPacketStoragePath != null)
				&& (!localPacketStoragePath.equals(""))) {
			// Check to see if file separator is already specified on the
			// storage path
			if (!localPacketStoragePath.endsWith(File.separator))
				localPacketStoragePath = localPacketStoragePath
						+ File.separator;

			// Grab the file name from the URL
			String urlFile = url.getFile();
			urlFile = urlFile.substring(urlFile.lastIndexOf("/") + 1);

			// Now look for the file
			File packetFile = new File(localPacketStoragePath + urlFile);
			if (packetFile.exists()) {
				// logger.debug("A packet file with the same name as the URL was found so that will be used locally");
				try {
					this.setFile(packetFile, numBytesToSkip);
					return;
				} catch (IOException e) {
					logger
							.error("IOException caught trying to call setFile from setURL:"
									+ e.getMessage());
				}
			}
		}

		// Since we are resetting the read source, reset the number of
		// bytes read as well.
		bytesReadSoFar = 0;

		// Grab the file name from the URL
		String urlFile = url.getFile();
		urlFile = urlFile.substring(urlFile.lastIndexOf("/") + 1);

		// Now split off the four keys
		StringTokenizer stok = new StringTokenizer(urlFile, "_");
		if (stok.hasMoreTokens()) {
			try {
				sourceIDForTracking = new Long(stok.nextToken());
			} catch (Exception e) {
			}
		}
		if (stok.hasMoreTokens()) {
			try {
				stok.nextToken();
			} catch (Exception e) {
			}
		}
		if (stok.hasMoreTokens()) {
			try {
				stok.nextToken();
			} catch (Exception e) {
			}
		}
		if (stok.hasMoreTokens()) {
			try {
				parentIDForTracking = new Long(stok.nextToken());
			} catch (Exception e) {
			}
		}

		// Close the DataInputStream (if there is one)
		if (in != null) {
			try {
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// Close the FileInputStream if there is one
		if (fin != null) {
			try {
				fin.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// Close up the URL before reconnecting
		if (httpURLConnection != null) {
			try {
				httpURLConnection.disconnect();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (this.packetURL != null) {
			this.packetURL = null;
		}

		// Set URL flags
		this.urlSource = true;

		// Set the URL
		this.packetURL = url;
		try {
			if (url.getProtocol().equals("http")) {
				this.httpURLConnection = (HttpURLConnection) url
						.openConnection();
				this.httpURLConnection.connect();

				// DO NOT use a BufferedInputStream. This throws off the
				// available90
				// method
				// of FileInputStream so that only one object can be retrieved.
				in = new DataInputStream(this.httpURLConnection
						.getInputStream());
			} else {
				// Create a URL Connection
				this.packetURLConnection = url.openConnection();
				// Now connect
				this.packetURLConnection.connect();
				// DO NOT use a BufferedInputStream. This throws off the
				// available90
				// method
				// of FileInputStream so that only one object can be retrieved.
				in = new DataInputStream(this.packetURLConnection
						.getInputStream());
			}
		} catch (Exception e1) {
			// Clear out the input stream if something went wrong
			in = null;
		}

		// Skip number of bytes if there are any
		if ((in != null) && (numBytesToSkip > 0)) {
			// Now loop through and read each byte, since this is a URL
			for (int i = 1; i <= numBytesToSkip; i++) {
				in.readByte();
				bytesReadSoFar++;
			}
		}
	}

	/**
	 * This method sets up the streams for reading packets from URLs
	 * 
	 * @param url
	 *            is the <code>URL</code> that points to a file containing
	 *            serialized packets
	 * @throws IOException
	 *             if something goes wrong
	 */
	public void setURL(URL url) throws IOException {
		// Call the URL setup method with no bytes to skip
		this.setURL(url, 0L);
	}

	/**
	 * Return the file that is being read from
	 * 
	 * @return a <code>File</code> that points to a file of serialized packets.
	 */
	public File getFile() {
		return file;
	}

	/**
	 * This method returns a boolean that indicates if there are more packets
	 * that can be read from the source. If <code>true</code>, the caller should
	 * be able to call <code>nextElement</code> to retrieve another packet.
	 * <b><font color=\"red\">NOTE: THIS IS NOT RELIABLE WHEN READING FROM A URL
	 * </font> </b>. The available() method on a URL is not reliable, so it is
	 * currently fixed to always be true if a URL is being used.
	 * 
	 * TODO Fix when reading from URL
	 * 
	 * @return a <code>boolean</code> that indicates if more packets can be read
	 *         from the source. More can be read if <code>true</code>, none if
	 *         <code>false</code>.
	 */
	public boolean hasMoreElements() {
		// If no input stream was found
		if (in == null)
			return false;
		// Set the return to false as the default
		boolean ok = false;
		// If the source is a URL, return the current state
		// of the moreURLElement flag.
		if (this.urlSource) {
			try {
				int timeout = 20;
				if (in.available() <= 0) {
					int counter = 0;
					while ((in.available() <= 0) && (counter++ < timeout)) {
						try {
							Thread.sleep(1000);
						} catch (Exception e) {
						}
					}
					if (in.available() <= 0) {
						return false;
					}
				} else {
					ok = true;
				}
			} catch (IOException e) {
				logger.error("IOException when checking available: "
						+ e.getMessage());
			}
		} else {
			// If not a URL, ask the DataInputStream if there
			// is more available
			try {
				ok = in.available() > 0;
			} catch (Exception e) {
				logger
						.error("An exception was caught asking the DataInputStream for available: "
								+ e.getMessage());
			}
		}
		return ok;
	}

	/**
	 * This method closes the input stream(s) that are being used to read packet
	 * from.
	 * 
	 * @throws IOException
	 *             if something goes wrong while close the streams
	 */
	public void close() throws IOException {
		// Close the DataInputStream if there is one
		if (in != null) {
			in.close();
		}
		// Close the FileInputStream if there is one
		if (fin != null) {
			fin.close();
		}
	}

	/**
	 * This method implement the nextElement() method from the
	 * <code>Enumeration</code> interface. When called, it returns the next
	 * available object from the packet stream.
	 * 
	 * @return An object that should be an instance of an SSDSDevicePacket
	 *         Object, it can return null if no object was available
	 */
	public Object nextElement() {
		// The object to return
		Object obj = null;
		// Now read it
		try {
			obj = readObject();
		} catch (EOFException e) {
			// If you get an EOF exception from the read,
			// it is most likely from the URL and you can
			// set the more URL elements to false to indicate
			// that no more are available.
		} catch (IOException e) {
			logger
					.error("There was an IOException while trying get the next element:"
							+ e.getMessage());
		}
		// Now return it
		return obj;
	}

	private byte[] readByteArrayVersion3() throws IOException {

		// Create the output byte array (streams)
		ByteArrayOutputStream byteOS = new ByteArrayOutputStream();
		DataOutputStream dataOS = new DataOutputStream(byteOS);

		// Create a byte array to hold the header keys for the packet
		byte[] headerKeys = new byte[72];

		// Read in those bytes
		in.read(headerKeys);

		// Increment the number of bytes read so far
		this.bytesReadSoFar += 72;

		// OK now parse out the keys from the byte array
		DataInputStream secondaryDIS = new DataInputStream(
				new ByteArrayInputStream(headerKeys));
		// Read in and write sourceID
		dataOS.writeLong(secondaryDIS.readLong());
		// Read in and write parentID
		dataOS.writeLong(secondaryDIS.readLong());
		// Read in and write the packetType
		dataOS.writeInt(secondaryDIS.readInt());
		// Read in and write packet sub type
		dataOS.writeLong(secondaryDIS.readLong());
		// Read in and write metadatasequenceNumber
		dataOS.writeLong(secondaryDIS.readLong());
		// Read in and write the data description version
		dataOS.writeLong(secondaryDIS.readLong());
		// Read in and write timestamp seconds
		dataOS.writeLong(secondaryDIS.readLong());
		// Read in and write the timestamp nanoseconds
		dataOS.writeLong(secondaryDIS.readLong());
		// Read in and write the sequence number
		dataOS.writeLong(secondaryDIS.readLong());
		// Read in and write the size of the first buffer
		int bufferSize = secondaryDIS.readInt();
		// This is an exceptional trap that happens if for some
		// reason the size read in is negative.
		if (bufferSize < 0)
			bufferSize = 0;
		// This is a hack, see docs below where constants are defined
		if (bufferSize > PacketInput.MAX_FIRST_BUFFER_SIZE) {
			// Reset the buffer size to 1
			bufferSize = 0;
		}
		// TODO - THIS IS A MAJOR HACK TO GET URL READING WORKING. For
		// some stupid reason, when reading from a URL, if you run at
		// full bore, you get exceptions thrown, but by inputting a
		// one millisecond sleep, it seems to fix it
		if (this.urlSource) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
			}
		}

		// Create the byte array to store the primary data buffer
		byte[] buffer = new byte[bufferSize];
		// Read in the data buffer
		in.read(buffer);
		// Write out buffer size and buffer
		dataOS.writeInt(bufferSize);
		dataOS.write(buffer);
		// Increment the number of bytes read so far
		this.bytesReadSoFar += bufferSize;
		// Read in the size of the secondary buffer
		int bufferTwoSize = in.readInt();
		// Increment the number of bytes read in
		this.bytesReadSoFar += 4;
		// This is a hack, see docs below where constants are defined
		if (bufferTwoSize > PacketInput.MAX_SECOND_BUFFER_SIZE) {
			// Reset the buffer size to 1
			bufferTwoSize = 0;
		}
		// Allocate an array of bytes that will store the secondary buffer
		byte[] bufferTwo = new byte[bufferTwoSize];
		// Read in the buffer
		in.read(bufferTwo);
		// Write out the buffer size and buffer
		dataOS.writeInt(bufferTwoSize);
		dataOS.write(bufferTwo);
		// Increment the number of bytes read so far
		this.bytesReadSoFar += bufferTwoSize;

		// Now return the byte array
		return byteOS.toByteArray();
	}

	/**
	 * This method is called to read an object from the stream. It checks the
	 * version from the stream and then calls the appropriate read of the
	 * correct version.
	 * 
	 * @return An object that is an <code>SSDSDevicePacket</code> that is read
	 *         from the serialized packet stream. A null is returned if no
	 *         packet was available.
	 * 
	 * @throws IOException
	 *             if something goes wrong with the read
	 */
	private Object readObject() throws IOException {
		// If not input stream was found
		if (in == null)
			return null;
		// The object to return
		Object obj = null;
		// Read in the version from the input stream
		int tmpVersionID = in.readInt();
		// Increase the number of bytes by four that were read
		// from the stream
		this.bytesReadSoFar += 4;

		// Now based on the version value, call the appropriate read method
		switch (tmpVersionID) {
		case 1:
			obj = readVersion1();
			break;
		case 2:
			obj = readVersion2();
			break;
		case 3:
			obj = readVersion3();
			break;
		default:
			// If the version is not one of the defined
			// versions, then there is probably some
			// junk in the stream or the reading got off
			// track, so call a method to try to resolve
			// that situation
			obj = readFromOffTrackStream(tmpVersionID);
		}
		// Return the object
		return obj;
	}

	/**
	 * This is the method that reads packets from the input stream that were
	 * serialized in the version 1 format of packet.
	 * 
	 * @deprecated
	 * @return an Object that is an <code>SSDSDevicePacket</code> that conforms
	 *         to the first version of packet structure
	 * 
	 * @throws IOException
	 *             if something goes wrong with the read.
	 * @throws EOFException
	 *             if the end of the input stream is hit.
	 */
	private Object readVersion1() throws IOException, EOFException {
		// First allocate an array of bytes that has all the header keys
		byte[] headerKeys = new byte[52];
		// Read them from the stream
		in.read(headerKeys);
		// Increment the number of bytes read
		this.bytesReadSoFar += 52;

		// OK now parse out the keys from the byte array
		DataInputStream secondaryDIS = new DataInputStream(
				new ByteArrayInputStream(headerKeys));
		long sourceID = secondaryDIS.readLong();
		long recordType = secondaryDIS.readLong();
		long metadataID = secondaryDIS.readLong();
		long platformID = secondaryDIS.readLong();
		long systemTime = secondaryDIS.readLong();
		long sequenceNo = secondaryDIS.readLong();
		int bufferSize = secondaryDIS.readInt();
		// This is an exceptional trap that happens if for some
		// reason the size read in is negative.
		if (bufferSize < 0)
			bufferSize = 1;
		// This is a hack, see docs below where constants are defined
		if (bufferSize > PacketInput.MAX_FIRST_BUFFER_SIZE) {
			// Create and log an error message
			// StringBuffer errorMessage = new StringBuffer();
			// errorMessage
			// .append("A packet primary buffer size of " + bufferSize);
			// errorMessage.append(" was read from the serialized packet stream ");
			// if (this.file != null) {
			// errorMessage.append("(File: " + this.file.getName() + ")");
			// } else if (this.packetURL != null) {
			// errorMessage.append("(URL: " + this.packetURL.toString() + ")");
			// } else {
			// errorMessage.append("(Unknown packet source)");
			// }
			// errorMessage.append(" with a SIAM timestamp of ");
			// errorMessage.append(new Date(systemTime).toString());
			// errorMessage.append(" and that exceeds the max size of "
			// + PacketInput.MAX_FIRST_BUFFER_SIZE);
			// errorMessage.append(" so it has been reset to 1.");
			// logger.debug(errorMessage.toString());
			// Reset the buffer size to one
			bufferSize = 1;
		}
		// TODO - THIS IS A MAJOR HACK TO GET URL READING WORKING. For
		// some stupid reason, when reading from a URL, if you run at
		// full bore, you get exceptions thrown, but by inputting a
		// one millisecond sleep, it seems to fix it
		if (this.urlSource) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
			}
		}

		// Create a byte buffer to use to read in the primary data buffer
		byte[] buffer = new byte[bufferSize];
		// Now read it
		in.read(buffer);
		// Increment the number of bytes read
		this.bytesReadSoFar += bufferSize;

		// Create a new packet
		SSDSDevicePacket packet = new SSDSDevicePacket(sourceID);
		packet.setMetadataSequenceNumber(metadataID);
		packet.setRecordType(recordType);
		packet.setPlatformID(platformID);
		packet.setSystemTime(systemTime);
		packet.setSequenceNo(sequenceNo);
		packet.setDataBuffer(buffer);

		// Now return it
		return packet;
	}

	/**
	 * This is the method that reads packets from the input stream that were
	 * serialized in the version 2 format of packet.
	 * 
	 * @deprecated
	 * @return an Object that is an <code>SSDSDevicePacket</code> that conforms
	 *         to the second version of packet structure
	 * 
	 * @throws IOException
	 *             if something goes wrong with the read.
	 * @throws EOFException
	 *             if the end of the input stream is hit.
	 */
	private Object readVersion2() throws IOException, EOFException {
		// Setup a byte array that will hold all the header
		// information for the packet
		byte[] headerKeys = new byte[56];
		// Now read in the bytes from the stream
		in.read(headerKeys);
		// Increment the number of bytes read so far
		this.bytesReadSoFar += 56;

		// OK now parse out the keys from the byte array
		DataInputStream secondaryDIS = new DataInputStream(
				new ByteArrayInputStream(headerKeys));
		long sourceID = secondaryDIS.readLong();
		int packetType = secondaryDIS.readInt();
		long metadataID = secondaryDIS.readLong();
		long recordType = secondaryDIS.readLong();
		long platformID = secondaryDIS.readLong();
		long systemTime = secondaryDIS.readLong();
		long sequenceNo = secondaryDIS.readLong();
		int bufferSize = secondaryDIS.readInt();
		// This is an exceptional trap that happens if for some
		// reason the size read in is negative.
		if (bufferSize < 0)
			bufferSize = 1;
		// This is a hack, see docs below where constants are defined
		if (bufferSize > PacketInput.MAX_FIRST_BUFFER_SIZE) {
			// Create and log an error message
			// StringBuffer errorMessage = new StringBuffer();
			// errorMessage
			// .append("A packet primary buffer size of " + bufferSize);
			// errorMessage.append(" was read from the serialized packet stream ");
			// if (this.file != null) {
			// errorMessage.append("(File: " + this.file.getName() + ")");
			// } else if (this.packetURL != null) {
			// errorMessage.append("(URL: " + this.packetURL.toString() + ")");
			// } else {
			// errorMessage.append("(Unknown packet source)");
			// }
			// errorMessage.append(" with a SIAM timestamp of ");
			// errorMessage.append(new Date(systemTime).toString());
			// errorMessage.append(" and that exceeds the max size of "
			// + PacketInput.MAX_FIRST_BUFFER_SIZE);
			// errorMessage.append(" so it has been reset to 1.");
			// logger.debug(errorMessage.toString());
			// Set the buffer size back to 1
			bufferSize = 1;
		}
		// TODO - THIS IS A MAJOR HACK TO GET URL READING WORKING. For
		// some stupid reason, when reading from a URL, if you run at
		// full bore, you get exceptions thrown, but by inputting a
		// one millisecond sleep, it seems to fix it
		if (this.urlSource) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
			}
		}

		// Setup a byte array to hold the primary data buffer
		byte[] buffer = new byte[bufferSize];
		// Read in the bytes
		in.read(buffer);
		// Increment the number of bytes read
		this.bytesReadSoFar += bufferSize;
		// Read in the size of the second data buffer
		int bufferTwoSize = in.readInt();
		// This is an exceptional trap that happens if for some
		// reason the size read in is negative.
		if (bufferTwoSize < 0)
			bufferTwoSize = 1;
		// Increment the number of bytes read
		this.bytesReadSoFar += 4;

		// This is a check that basically says, if the packet is not a metadata
		// packet, the secondary buffer should be a size of one
		if (packetType != 0) {
			bufferTwoSize = 1;
		}
		// This is a hack, see docs below where constants are defined
		if (bufferTwoSize > PacketInput.MAX_SECOND_BUFFER_SIZE) {
			// Create and log an error message
			// StringBuffer errorMessage = new StringBuffer();
			// errorMessage.append("A packet secondary buffer size of "
			// + bufferTwoSize);
			// errorMessage.append(" was read from the serialized packet stream ");
			// if (this.file != null) {
			// errorMessage.append("(File: " + this.file.getName() + ")");
			// } else if (this.packetURL != null) {
			// errorMessage.append("(URL: " + this.packetURL.toString() + ")");
			// } else {
			// errorMessage.append("(Unknown packet source)");
			// }
			// errorMessage.append(" with a SIAM timestamp of ");
			// errorMessage.append(new Date(systemTime).toString());
			// errorMessage.append(" and that exceeds the max size of "
			// + PacketInput.MAX_SECOND_BUFFER_SIZE);
			// errorMessage.append(" so it has been reset to 1.");
			// logger.debug(errorMessage.toString());
			// Reset the secondary buffer size to one
			bufferTwoSize = 1;
		}
		// Create a byte array to hold the second data buffer
		byte[] bufferTwo = new byte[bufferTwoSize];
		// Read it in
		in.read(bufferTwo);
		// Increment the number of bytes read so far
		this.bytesReadSoFar += bufferTwoSize;

		// Create a new packet
		SSDSDevicePacket packet = new SSDSDevicePacket(sourceID);
		packet.setPacketType(packetType);
		packet.setMetadataSequenceNumber(metadataID);
		packet.setRecordType(recordType);
		packet.setPlatformID(platformID);
		packet.setSystemTime(systemTime);
		packet.setSequenceNo(sequenceNo);
		packet.setDataBuffer(buffer);
		packet.setOtherBuffer(bufferTwo);

		// Return it
		return packet;
	}

	/**
	 * This is the method that reads packets from the input stream that were
	 * serialized in the version 3 format of packet.
	 * 
	 * @return an Object that is an <code>SSDSDevicePacket</code> that conforms
	 *         to the third version of packet structure
	 * 
	 * @throws IOException
	 *             if something goes wrong with the read.
	 * @throws EOFException
	 *             if the end of the input stream is hit.
	 */
	private Object readVersion3() throws IOException, EOFException {

		// Read the byte array from the file
		byte[] nextByteArray = readByteArrayVersion3();

		// Now conver to SSDSDevicePacket and return
		return PacketUtility.convertVersion3SSDSByteArrayToSSDSDevicePacket(
				nextByteArray, false);

	}

	/**
	 * This method is used when the reading appears to get off track. Sometimes
	 * it seems like extra junk gets written to data buffers and the version
	 * number reading gets all messed up. The biggest problem is that when the
	 * reading gets hosed, the readObject method keeps trying to read ints (4
	 * bytes) and that may skip over the next valid version if it does not
	 * happen to fall right on the correct 4 bytes. The best way is to shimmy
	 * across the data stream until an integer is found that is the correct
	 * verision. That is what this method is for.
	 * 
	 * @param tmpVersionID
	 *            is an int that was the incorrect version ID that was read from
	 *            the stream. This will be the starting point for the byte
	 *            walking
	 * @return the next <code>SSDSDevicePacket</code> that was found after the
	 *         reading got off track.
	 */
	private Object readFromOffTrackStream(int tmpVersionID) {
		// Create the object to return
		Object objectToReturn = null;

		// In order to do some bit walkin', we need to convert the
		// incoming integer into a byte array. This is done most
		// easily using the BigInteger class, so we convert it
		// to BigInteger
		// logger.debug(bytesReadSoFar +
		// ":bytesReadSoFar (readFromOffTrackStream called)");
		BigInteger bigInt = new BigInteger("" + tmpVersionID);

		// Now convert it to an array of byte
		byte[] tmpVersionBytes = bigInt.toByteArray();

		// This is a boolean to allow the processing to bail out
		// when certain conditions exist
		boolean bailOutOfLoop = false;

		// A tracking number to make sure our reads don't get stuck
		long previousTempNumberOfBytes = 0;
		// Now just keep on looping till something changes to boolean
		while (!bailOutOfLoop) {
			// Clear the object to return in case it
			objectToReturn = null;

			// Create a new byte array that will be used to search
			// for a version number
			byte[] newIntBytes = new byte[4];
			// Now put the last three bytes of the previous number
			// to the first three bytes of the new number (bit sliding)
			try {
				newIntBytes[0] = tmpVersionBytes[1];
				newIntBytes[1] = tmpVersionBytes[2];
				newIntBytes[2] = tmpVersionBytes[3];
			} catch (Exception e2) {
			}

			// Now read the next byte from the stream and put it in the
			// last position in the array
			try {
				newIntBytes[3] = in.readByte();
				// Increment the number of bytes read so far
				bytesReadSoFar++;
			} catch (Exception e1) {
				bailOutOfLoop = true;
			}
			// logger.debug("With new byte, sliding window is : " + new
			// String(newIntBytes));
			// logger.debug("bytesReadSoFar = " + bytesReadSoFar);
			// This is the integer that will be used to check for a valid
			// version number
			int possibleVersion = 0;

			// This is the BigInteger that will be used to go back and
			// forth between byte arrays and integers
			BigInteger possibleVersionBigInt = new BigInteger(newIntBytes);

			// Now convert to an int
			possibleVersion = possibleVersionBigInt.intValue();
			// logger.debug("possibleVersionBigInt = " +
			// possibleVersionBigInt.intValue());

			// Now check to see if the version number matches one of the
			// valid version numbers available
			if ((possibleVersion == 1) || (possibleVersion == 2)
					|| (possibleVersion == 3)) {

				// Store a copy of the number of bytes read so far
				// in case the object returned from the object read
				// is not valid, we can go back to the same spot and
				// keep reading.
				long tempNumberOfBytes = bytesReadSoFar;
				// logger.debug("Possible Version of " + possibleVersion +
				// " found");
				// logger.debug(bytesReadSoFar +
				// ":bytesReadSoFar (after possible version found)");

				// Now, depending on what version number was found, go
				// ahead and try to read in an object of that version.
				// If and EOFException or an IOException is caught, bail
				// out of the loop, any other exception let the loop
				// continue
				try {
					switch (possibleVersion) {
					case 1:
						objectToReturn = readVersion1();
						break;
					case 2:
						objectToReturn = readVersion2();
						break;
					case 3:
						objectToReturn = readVersion3();
						break;
					}
				} catch (EOFException e3) {
					bailOutOfLoop = true;
				} catch (IOException e3) {
					bailOutOfLoop = true;
				} catch (Exception e3) {
				}
				// logger.debug(bytesReadSoFar +
				// ":bytesReadSoFar (after tried to read object)");
				// Now check to make sure its not null, so we can run some
				// checks on it
				if (objectToReturn != null) {
					// Check to make sure it is an SSDSDevicePacket
					if (objectToReturn instanceof SSDSDevicePacket) {
						// Convert it to an SSDSDevicePacket
						SSDSDevicePacket ssdsDP = (SSDSDevicePacket) objectToReturn;
						// Now check the keys to see if it really is possibly an
						// object
						if ((sourceIDForTracking != null)
								&& (parentIDForTracking != null)) {
							// Create a boolean that will indicate if any of the
							// tests of the keys fail
							boolean eureka = true;

							// Check to see if the source ID matches the source
							// ID for
							// this PacketInput
							if (sourceIDForTracking.longValue() != ssdsDP
									.sourceID()) {
								eureka = false;
							}
							// Now do the same for the parentID
							if (parentIDForTracking.longValue() != ssdsDP
									.getPlatformID()) {
								eureka = false;
							}
							// Check to see if it passed the tests
							if (eureka) {
								// We found a packet, so return it an move on
								// logger.debug("Returning the object and moving on");
								return objectToReturn;
							}
						}
					}
				}
				// If we get here, it was not the right object so we need reset
				// the file/url reading stuff so we can go back and keep walking
				// the
				// stream to look for the next valid version number
				// logger.debug("Object not valid");
				// logger.debug(bytesReadSoFar +
				// ":bytesReadSoFar (after object invalidated)");
				// logger.debug(previousTempNumberOfBytes +
				// ":previousTempNumberOfBytes (after object invalidated)");
				// logger.debug(tempNumberOfBytes +
				// ":tempNumberOfBytes (after object invalidated)");
				// Bail out if weird condition exists
				if (tempNumberOfBytes <= previousTempNumberOfBytes) {
					// logger.debug("****** tempNumberOfBytes <= previousTempNumberOfBytes");
					bailOutOfLoop = true;
					return null;
				}
				if (file != null) {
					try {
						// Reset the file and skip over the bytes we have
						// already
						// read through (we subtract three because we want to go
						// back and read the last three bytes of the number that
						// was last found as a possible version
						// setFile(file, tempNumberOfBytes - 3);
						setFile(file, tempNumberOfBytes);
					} catch (IOException e) {
					}
				} else {
					if (packetURL != null) {
						try {
							// Reset the URL and skip over the bytes we have
							// already
							// read through (we subtract three because we want
							// to go
							// back and read the last three bytes of the number
							// that
							// was last found as a possible version
							// setURL(packetURL, tempNumberOfBytes - 3);
							setURL(packetURL, tempNumberOfBytes);
						} catch (IOException e) {
						}
					}
				}
				// logger.debug("Setting previousTempNumberOfBytes to tempNumberOfBytes");
				previousTempNumberOfBytes = tempNumberOfBytes;
			}
			// Now set the last byte array to the one we last tried so we
			// can keep walking the stream
			tmpVersionBytes = newIntBytes;
		}
		// Return whatever was the result of the search
		return objectToReturn;
	}

	/**
	 * This is the method to return the number of bytes that have been read from
	 * the <code>PacketInput</code> source already
	 * 
	 * @return a <code>long</code> indicating how many bytes have already been
	 *         read from the <code>PacketInput</code> source
	 */
	public long getBytesReadSoFar() {
		return bytesReadSoFar;
	}

}