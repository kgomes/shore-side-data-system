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
package moos.ssds.services.data;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.naming.NamingException;

import moos.ssds.io.PacketInput;
import moos.ssds.io.SSDSDevicePacket;
import moos.ssds.io.util.PacketUtility;
import moos.ssds.services.data.PacketSubmissionAccess;
import moos.ssds.services.data.PacketSubmissionAccessHome;
import moos.ssds.services.data.PacketSubmissionAccessUtil;
import moos.ssds.services.data.util.DataException;

import org.apache.log4j.Logger;

/**
 * This class is designed to give the functionality of reading in one
 * <code>PacketInput</code> and sending all the
 * 
 * @author kgomes
 */
public class PacketFileToSQLServicePublisher {

	/**
	 * The constructor
	 * 
	 * @param inputType
	 * @param fullInputPath
	 * @param outputType
	 * @param fullOutputPath
	 */
	public PacketFileToSQLServicePublisher(String fullPathToInputFile,
			String deviceID) throws IOException, RemoteException,
			CreateException, NamingException, Exception, Throwable {

		// Copy variables locally
		this.fullInputPath = fullPathToInputFile;
		this.deviceID = deviceID;
		logger.debug("Incoming file path is " + fullPathToInputFile
				+ " and deviceID is " + deviceID);

		// If either is null
		if ((this.fullInputPath == null) || (this.deviceID == null)) {
			logger.error("Either input path or device ID was null, "
					+ "can't be that way");
			throw new IOException(
					"Either the input path or device ID was null. "
							+ "Both must be specified.");
		}

		// Now create the inputs
		this.inputFile = new File(this.fullInputPath);
		this.pi = new PacketInput(this.inputFile);

		// Check to see if the file exists
		if (!this.inputFile.exists()) {
			logger.error("Could not find the file " + fullPathToInputFile);
			throw new IOException("The input file " + inputFile.getName()
					+ " could not be found");
		}

		// Now get the interface to the service bean
		PacketSubmissionAccessHome packetSubmissionAccessHome = null;
		try {
			packetSubmissionAccessHome = PacketSubmissionAccessUtil.getHome();
		} catch (NamingException e) {
			logger.error("NamingException caught trying to get home"
					+ " interface to packet submission service (message="
					+ e.getMessage());
			throw e;
		} catch (Throwable e) {
			logger
					.error("Throwable caught trying to get home "
							+ "interface to packet submission service (Exception class = "
							+ e.getClass().getName() + ", message= "
							+ e.getMessage());
			throw e;
		}
		if (packetSubmissionAccessHome != null) {
			try {
				this.packetSubmissionAccess = packetSubmissionAccessHome
						.create();
			} catch (RemoteException e) {
				logger.error("RemoteException caught trying to create "
						+ "interface to packetSubmission: " + e.getMessage());
				throw e;
			} catch (CreateException e) {
				logger.error("CreateException caught trying to create "
						+ "interface to packetSubmission: " + e.getMessage());
				throw e;
			}
		}

		if (packetSubmissionAccess == null) {
			throw new Exception("I could not get the remote interface so "
					+ "I can't do anything");
		}
	}

	public void publishPackets() throws IOException, java.sql.SQLException,
			NumberFormatException, DataException {
		if (this.pi != null) {
			while (pi.hasMoreElements()) {
				SSDSDevicePacket ssdsdp = (SSDSDevicePacket) pi.nextElement();
				if (this.packetSubmissionAccess != null) {
					byte[] ssdsBytes = PacketUtility
							.convertSSDSDevicePacketToSSDSByteArray(ssdsdp);
					packetSubmissionAccess.submitPacketAsByteArray(new Long(
							this.deviceID).longValue(), ssdsBytes);
				}
			}
		}
	}

	/**
	 * This is the main to allow the method to be called from the command line.
	 * The user must supply the following in the command line:
	 * <ol>
	 * <li>The type of input to read from (<b>file<b>|<b>sql</b>)</li>
	 * <li>The full path to the file (if file is to be read from). Don't specify
	 * anything if SQL, the SQL server is specified in the
	 * moos/ssds/io/io.properties file that is in the classpath.</li>
	 * <li>The type of output to send to (<b>file</b>|<b>sql</b>)</li>
	 * <li>The full path to the file (if file is to be used to write out to).
	 * Don't specify anything if SQL, the SQL server is specified in the
	 * moos/ssds/io/io.properties file that is in the classpath.</li>
	 * </ol>
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("There were not two arguments specified");
			System.out.println(PacketFileToSQLServicePublisher.getUsage());
			System.exit(0);
		}
		// Grab the command line arguments
		String fullInputFilePath = null;
		String deviceID = null;

		try {
			fullInputFilePath = args[0];
			deviceID = args[1];
		} catch (RuntimeException e2) {
			System.err
					.println("Something was wrong with the incoming arguments");
			System.err.println(PacketFileToSQLServicePublisher.getUsage());
			System.exit(0);
		}

		// Create the converter
		PacketFileToSQLServicePublisher publisher = null;

		try {
			publisher = new PacketFileToSQLServicePublisher(fullInputFilePath,
					deviceID);
		} catch (Throwable e) {
			System.err.println("Something went wrong: " + " ("
					+ e.getClass().getName() + ")" + e.getMessage());
			System.exit(0);
			e.printStackTrace();
		}

		// Now run the conversion
		try {
			publisher.publishPackets();
		} catch (Throwable e) {
			System.err.println("Something went wrong: " + " ("
					+ e.getClass().getName() + ")" + e.getMessage());
			System.exit(0);
			e.printStackTrace();
		}
	}

	/**
	 * Method to print out usage statement
	 * 
	 * @return the usage <code>String</code>
	 */
	private static String getUsage() {
		return "Usage: java moos.ssds.io.util.PacketIOConverter "
				+ "full_path_to_packet_file deviceID_to_publish_from";
	}

	// Some local variables
	String fullInputPath = null;
	String deviceID = null;

	// The input reader stuff
	File inputFile = null;
	PacketInput pi = null;

	// The service interface
	PacketSubmissionAccess packetSubmissionAccess = null;

	// A logger
	Logger logger = Logger.getLogger(getClass());
}
