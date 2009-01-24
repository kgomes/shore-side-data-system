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
package moos.ssds.simulator;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import moos.ssds.jms.PublisherComponent;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.mbari.isi.interfaces.DevicePacket;
import org.mbari.isi.interfaces.MetadataPacket;
import org.mbari.isi.interfaces.SensorDataPacket;

/**
 * <p>This class is used as a simulator for sending serialized packets
 * to SSDS. The packets are of type 
 * <code>org.mbari.isi.interfaces.SensorDataPackets</code>.
 * To use this class, call it on the command line with one
 * command line argument.  The argument is the file name of the data that 
 * you want to wrap up in a packet and send to SSDS.  
 * In order for this to work correctly, you also need to have
 * the ssds-pub-client.X.X-host-topic.jar file in your classpath.  The X's
 * are the version of the jar file, the "host" is the host that the packets
 * will be sent to, and the "topic" is the topic name that will the packets
 * will be published to.</p><hr>
 *
 * @author  : $Author: kgomes $
 * @version : $Revision: 1.6 $
 */
public class PacketGenerator {
	
	/**
	 * This ArrayList contains the list of FileInfo objects that have all the
	 * information necesary for the generator to send packets to SSDS.
	 * @associates FileInfo 
	 */
	private ArrayList list = new ArrayList();

	/**
	 * This is the PublisherComponent that the generator will use to
	 * publish its data packets.
	 */
	private PublisherComponent pub = null;
	
	/**
	 * This is a Log4JLogger that is used to log information to
	 */
	static Logger pgLogger = Logger.getLogger(PacketGenerator.class);

	/**
	 * Constructor that takes in the topicName.  Creates a new PacketGenerator
	 * that will send data to the given topicName (the server that it sends it
	 * to depends on the jar file used in the class path - and hence the 
	 * jndi.properties file it uses).
	 * @param topicName This is the name of the topic the packets will be sent
	 * to.
	 */
    public PacketGenerator() {
    	
		BasicConfigurator.configure();
		pgLogger.setLevel(Level.DEBUG);
    	// Create a new publisherComponent with the given topic name
    	pgLogger.debug("New PacketGenerator, going to create a new PublisherComponent ...");
        pub = new PublisherComponent();
        pgLogger.debug("Done with PublisherComponent.");
    } // End constructor

	/**
	 * This is a setter method to set the name of the topic that the
	 * generator will publish its packets to.
	 * @param topicName The name of the topic that the generator will send
	 * its data to
	 */
    public void setTopicName(String topicName) {
        pub = new PublisherComponent(topicName);
    } // End setTopicName

	/**
	 * This is the getter method to retrieve the name of the topic that
	 * the generator is currently sending packets to.
	 * @return A string that is the name of the topic that packets are
	 * published to
	 */
    public String getTopicName() {
        return pub.getTopicname();
    } // End getTopicName

	/**
	 * This method reads takes in a File and then
	 * generates a DevicePacket from that file
	 * @param file This is the file to read to create the DevicePacket
	 * @return A <code>DevicePacket</code> with the information from the
	 * file that was input.
	 */
    private DevicePacket read(File file) {
    	pgLogger.debug("read with File parameter called.");
    	// Create a local FileInfo object
        FileInfo fileInfo = new FileInfo();
        pgLogger.debug("Created a new FileInfo and calling read with that ...");
        // Set the file in file info
        fileInfo.setFile(file);
        // Now read in the file Info to create
        // the DevicePacket and return it
        return read(fileInfo);
    } // End read

	/**
	 * This method reads in a FileInfo object and generates a DevicePacket
	 * from the information in the FileInfo
	 * @param fileInfo The FileInfo object that contains the information about
	 * the packet to be generated
	 * @return A DevicePacket
	 */
    private DevicePacket read(FileInfo fileInfo) {
    	// Create devicepacket place holder
    	DevicePacket p = null;
    	
    	pgLogger.debug("read with FileInfo parameter called.");
    	// Get the File from the FileInfo object
        File file = fileInfo.getFile();
        // Get the length of the file
        long fileSize = file.length();
		pgLogger.debug("Reading " + file.getAbsolutePath() + " (" + fileSize + " bytes)");
		// Create a byte array with the size of the file
        byte[] fileData = new byte[(int)fileSize];
        // Now stuff in the data from the file in the bye array
        try {
        	// Open buffered input stream and read
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
            in.read(fileData, 0, (int)fileSize);
            String whatReadIn = new String(fileData);
            pgLogger.debug("whatReadIn = " + whatReadIn);
        }
        catch (Exception e) {
        	pgLogger.debug("Exception caught while reading in file->" + e.getMessage());
            e.printStackTrace();
        }
        
        // Check to see if it is a metadata packet and create one of those if needed
        // Metadata packet is defined as a packet with record type 0
        if (fileInfo.getRecordType() == 0) {
        	pgLogger.debug("RecordType was 0, so will create a new MetadataPacket");
        	byte [] dummyCause = new byte[10];
        	p = new MetadataPacket(fileInfo.getInstrumentID(), dummyCause, fileData);
        	p.setMetadataRef(fileInfo.getMetadataSequenceNumber());
        	p.setSequenceNo(fileInfo.getMetadataSequenceNumber());
        	p.setParentId(fileInfo.getParentDeviceID());
        } else {
        	pgLogger.debug("RecordType was not 0, so looks like SensorDataPacket");
        	// This means we are a SensorDataPacket so create one
			SensorDataPacket sdp = new SensorDataPacket(fileInfo.getInstrumentID(), (int)fileSize);
			// Now set the data buffer
			sdp.setDataBuffer(fileData);
			p = sdp;
        } // End if metadata packet or SensorDataPacket
        // Return the DevicePacket
        return p;
    } // End read

	/**
	 * 
	 * @param packet
	 */
//    private void publish(DevicePacket packet) {
//        publish(packet, i);
//        i++;
//    } // End publish

	/**
	 * 
	 * @param packet
	 * @param sequenceNumber
	 */
    private void publish(DevicePacket packet, long sequenceNumber) {
        packet.setSequenceNo(sequenceNumber);
        packet.setSystemTime(System.currentTimeMillis());
        boolean ok = pub.publish(packet);
        if (ok) {
            System.out.println("Published message #" + sequenceNumber);
        }
        else {
            System.out.println("Failed to publish message #" + sequenceNumber);
        }
    } // End publish

//    public void onChange(File file) {
//    }
//
//    public void send(String filename) {
//        File f = new File(filename);
//        DevicePacket p = read(f);
//        publish(p);
//    }


	/**
	 * 
	 * @param propName
	 * @throws IOException
	 */
    public void readStartup(String propName) throws IOException {
    	pgLogger.debug("readStartup called with parameter->" + propName);
    	// The control file that has the information about how to send
    	// the published packets
        File propFile = new File(propName);
        pgLogger.debug("The properties (control file) to be read is " + propName);

		// A BufferedRead to read in the lines from the file
        BufferedReader in = new BufferedReader(new FileReader(propFile));

		// The current line being read
        String line;
        // A tokenizer to parse the read line
        StringTokenizer st = null;
        // An array of string to hold the contens of the parsed line
        String[] buf = null;
        // A file that contains the data record to use to build a packet
        File tmpFile = null;
        // ?
        FileInfo f = null;
        
        // Loop through the file and read each line until a null
        // is returned from the read
        while ((line = in.readLine()) != null) {
        	pgLogger.debug("Line read -> " + line);
        	// Ignore any comment lines
            if (line.startsWith("#")) {
            	pgLogger.debug("Just a comment ... skipping to next line.");
                continue;
            }
            // Create a string tokenizer and use a comma as the delimiter.
            st = new StringTokenizer(line, ",");
            // Create the string array based on the number of tokens
            // in the line
            buf = new String[st.countTokens()];
            // A indexing integer
            int i = 0;
            // Loop through the tokens and put into the array (while
            // incrementing the counter
            while (st.hasMoreTokens()) {
                buf[i] = st.nextToken();
                i++;
            }
			// Using the first token in the string, open the file that
			// it points to (if it exists) 
            try {
            	pgLogger.debug("Going to try to open file " + buf[0]);
                tmpFile = new File(buf[0]);
                // If the file doesn't exist, skip to the next line
                if (!tmpFile.exists()) {
                	pgLogger.debug("File did not exist, skipping to next line.");
                    continue;
                }
            }
            catch (Exception e) {
            	pgLogger.debug("Exception caught->" + e.getMessage() + ". Skipping to next line.");
                continue;
            }

			// Create a new FileInfo object
            f = new FileInfo();
            // Set the file of the data content of the packet
            f.setFile(tmpFile);
            // Now use the rest of the line to populate the FileInfo that
            // is used to construct packets
            try {
            	pgLogger.debug("Setting instrumentID on FileInfo to " + buf[1]);
                f.setInstrumentID(Long.parseLong(buf[1]));
				pgLogger.debug("Setting parentID on FileInfo to " + buf[2]);
                f.setParentDeviceID(Long.parseLong(buf[2]));
				pgLogger.debug("Setting sequenceNumber on FileInfo to " + buf[3]);
				f.setSequenceNumber(Long.parseLong(buf[3]));
				pgLogger.debug("Setting metadataSequenceNumber on FileInfo to " + buf[4]);
                f.setMetadataSequenceNumber(Long.parseLong(buf[4]));
				pgLogger.debug("Setting recordType on FileInfo to " + buf[5]);
                f.setRecordType(Long.parseLong(buf[5]));
				pgLogger.debug("Setting repeatTimes on FileInfo to " + buf[6]);
                f.setRepeatTimes(Long.parseLong(buf[6]));
				pgLogger.debug("Setting repeatInt on FileInfo to " + buf[7]);
                f.setRepeatInt(Long.parseLong(buf[7]));
				pgLogger.debug("Setting serialized on FileInfo to " + buf[8]);
                f.setSerialized(Boolean.getBoolean(buf[8]));
            }
            catch (ArrayIndexOutOfBoundsException e) {
            	pgLogger.debug("ArrayIndexOutOfBounds exception caught while creating FileInfo ->" + e.getMessage());
            }
            catch (Exception e) {
				pgLogger.debug("Exception caught while creating FileInfo ->" + e.getMessage());
            }
            // Add the file info to the list
            list.add(f);
        } // End looping through lines of control file
        pgLogger.debug("The ArrayList of FileInfo objects now has " + list.size() + " objects in it.");
    } // End readStartup

	/**
	 * This method takes the array list of FileInfo objects and creates a thread
	 * for each one.  Then it uses that FileInfo object to publish packets based
	 * on the information in that FileInfo object
	 */
    public void prcFiles() {
    	pgLogger.debug("prcFiles called ...");
    	// A new thread
        Runnable r = null;

		// Loop through the list of FileInfo objects
        for (int i = 0; i < list.size(); i++) {
        	// Create an immutable local FileInfo to
        	// work with in this iteration and grab one
        	// off the arraylist
            final FileInfo fi = (FileInfo)list.get(i);
            // Create new thread to process the FileInfo object
            r = new Runnable() {
            	// This is the run method of the new thread
                public void run() {
                	// Call prc on the file info.
                    try {
                    	// Assign the local FileInfo object
                        fileInfo = fi;
						PacketGenerator.pgLogger.debug("Thread run called with FileInfo->" + fileInfo);
                        // Now process it
                        prc();
                    }
                    catch (Exception e) {
                    	PacketGenerator.pgLogger.debug("Exception caught on run->" + e.getMessage());
                        e.printStackTrace();
                    }
                } // End run

				// This method is used to process the FileInfo object
                private void prc() {
					// Create a device packet
                    DevicePacket p = null;
                    // Loop through the number of repeat time (number of times to
                    // publish the packet
                    PacketGenerator.pgLogger.debug("Will loop through " + fileInfo.getRepeatTimes() + " times.");
                    for (int i = 0; i < fileInfo.getRepeatTimes(); i++) {
                        long sequenceNumber = fileInfo.getSequenceNumber();
                        if (fileInfo.isSerialized()) {
                        } else {
                        	// Create a device packet by passing in the FileInfo object
                            p = read(fileInfo);
                        }
                        // Now publish that DevicePacket
						PacketGenerator.pgLogger.debug("Going to publish device packet " + p + " with sequence number " + sequenceNumber);
                        publish(p, sequenceNumber);
                        // Now go to sleep for the amount of time specified
                        try {
							PacketGenerator.pgLogger.debug("Going to sleep for " + fileInfo.getRepeatInt());
                            Thread.sleep(fileInfo.getRepeatInt());
							PacketGenerator.pgLogger.debug("Awake");
                        } catch (InterruptedException e) {
							PacketGenerator.pgLogger.debug("Exception caught while putting thread to sleep->" + e.getMessage());
                            e.printStackTrace();
                        }
                        // Increment the sequence number
                        sequenceNumber++;
                        // Set the sequence number
                        fileInfo.setSequenceNumber(sequenceNumber);
                    } // End loop through number of repeat times
                } // End prc
                
                // A local (thread) copy of the FileInfo object
                private FileInfo fileInfo;
            }; // End new runnable thread

			// Create the thread and start it
            Thread t = new Thread(r);
            t.start();
        } // End loop through list of FileInfo objects
    } // End prcFiles


	/**
	 * This it the main method to start the packet generator
	 * @param args is an array of strings which contains the command
	 * line parameters.  The first argument is the name of the file
	 * that contains the data that will be wrapped up in an
	 * packet and sent to SSDS.  The host and topic that it is sent to
	 * is determined by which jar file is used when the class is run 
	 */
    public static void main(String[] args) {
    	// Instantiate the PacketGenerator
        PacketGenerator pub1 = new PacketGenerator();
        
        // Now try to read the file that has the control file
        // that will direct how the packets are to be sent
        try {
        	// Read in the control file
            pub1.readStartup(args[0]);
            // Now process all the FileInfo objects in the list
            pub1.prcFiles();
        } catch (Exception e) {
        	PacketGenerator.pgLogger.debug("Exception caught in main->" + e.getMessage());
            e.printStackTrace();
        }

		// Sit and loop through waiting for the user to signal the
		// end of the PacketGenerator
        InputStreamReader inputStreamReader;
        char answer = '0';
        System.out.println("To quit, enter Q or q, then <return>");
        inputStreamReader = new InputStreamReader(System.in);
        while (!((answer == 'q') || (answer == 'Q'))) {
            try {
                answer = (char)inputStreamReader.read();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.exit(0);
    }


}
