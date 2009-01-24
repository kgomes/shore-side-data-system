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
package moos.ssds.ingest;

import java.io.ByteArrayInputStream;
import java.sql.SQLException;
import java.util.Properties;

import javax.jms.BytesMessage;
import javax.jms.JMSException;

import moos.ssds.io.PacketOutputManager;
import moos.ssds.io.PacketSQLOutput;

import org.apache.log4j.Logger;

/**
 * <p>
 * This Message Driven Bean looks for incoming packets and them writes them to a
 * database that is used to store blobs.
 * </p>
 * <hr>
 * 
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.1 $
 *          <br>
 *          XDoclet Stuff for deployment
 * @ejb.bean name="SQLIngest" display-name="SQL Ingest Message-Driven Bean"
 *           description="This is the message driven bean that takes packets and
 *           puts them in a RDB" transaction-type="Container"
 *           acknowledge-mode="Auto-acknowledge"
 *           destination-type="javax.jms.Topic"
 * @ejb.resource-ref res-ref-name="TopicConnectionFactory"
 *                   res-type="javax.jms.TopicConnectionFactory"
 *                   res-auth="Application"
 * @jboss.destination-jndi-name name="topic/${ingest.sql.topic.name}"
 */
public class SQLIngestMDB
    implements
        javax.ejb.MessageDrivenBean,
        javax.jms.MessageListener {

    /**
     * This is the MessageDrivenContext that is set by the container
     */
    private javax.ejb.MessageDrivenContext ctx;

    /**
     * A log4j logger
     */
    static Logger logger = Logger.getLogger(SQLIngestMDB.class);

    /**
     * This is the properties that contains the information needed to ingest
     * incoming packets
     */
    private Properties sqlIngestProps = null;

    /**
     * This is the <code>PacketOutputManager</code> that will manage the
     * <code>PacketOutputs</code> that will be used by the ingest bean to
     * serialize packets
     */
    PacketOutputManager pom = PacketOutputManager.getInstance();

    /**
     * This is the default constructor
     */
    public SQLIngestMDB() {}

    /**
     * This is the callback that the container uses to set the
     * MessageDrivenContext
     */
    public void setMessageDrivenContext(javax.ejb.MessageDrivenContext context) {
        // Set the context
        ctx = context;
    }

    /**
     * This is the callback that the container uses to create this bean
     */
    public void ejbCreate() {
        // Grab the transmogrifier properties for the file
        sqlIngestProps = new Properties();
        try {
            sqlIngestProps.load(this.getClass().getResourceAsStream(
                "/moos/ssds/ingest/ingest.properties"));
        } catch (Exception e) {
            logger.error("Exception trying to read in properties file: "
                + e.getMessage());
        }
    } // End ejbCreate

    /**
     * This is the callback that the container uses when removing this bean
     */
    public void ejbRemove() {} // End ejbRemove

    /**
     * This is the callback method that the container calls when a message is
     * received on the topic that this bean is subscribed to.
     * 
     * @param msg
     *            Is the message object that the topic recieved.
     */
    public void onMessage(javax.jms.Message msg) {
        if (msg instanceof BytesMessage) {
            BytesMessage bytesMessage = (BytesMessage) msg;
            storePacketInDatabase(bytesMessage);
        }
    }

    /**
     * This method takes in a BytesMessage (JMS Message) and records it's
     * contents to storage.
     * 
     * @param bytesMessage
     */
    private void storePacketInDatabase(BytesMessage bytesMessage) {
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
            deviceID = bytesMessage.readLong();
            parentID = bytesMessage.readLong();
            packetType = bytesMessage.readInt();
            packetSubType = bytesMessage.readLong();
            dataDescriptionID = bytesMessage.readLong();
            dataDescriptionVersion = bytesMessage.readLong();
            timestampSeconds = bytesMessage.readLong();
            timestampNanoseconds = bytesMessage.readLong();
            sequenceNumber = bytesMessage.readLong();
            bufferLen = bytesMessage.readInt();
            bufferBytes = new byte[bufferLen];
            bytesMessage.readBytes(bufferBytes);
            bufferTwoLen = bytesMessage.readInt();
            bufferTwoBytes = new byte[bufferTwoLen];
            bytesMessage.readBytes(bufferTwoBytes);

            // Debugging stuff
            StringBuffer hexData = new StringBuffer();
            ByteArrayInputStream byteArrayIS = new ByteArrayInputStream(
                bufferBytes);
            while (byteArrayIS.available() > 0) {
                hexData.append(Integer.toHexString(
                    (0xFF & byteArrayIS.read()) | 0x100).substring(1));
            }
            StringBuffer hexTwoData = new StringBuffer();
            ByteArrayInputStream byteTwoArrayIS = new ByteArrayInputStream(
                bufferTwoBytes);
            while (byteTwoArrayIS.available() > 0) {
                hexData.append(Integer.toHexString(
                    (0xFF & byteTwoArrayIS.read()) | 0x100).substring(1));
            }
            logger.debug("Got bytesMessage and will write to disk:"
                + "deviceID=" + deviceID + "," + "parentID=" + parentID + ","
                + "packetType=" + packetType + "," + "packetSubType="
                + packetSubType + "," + "dataDescriptionID="
                + dataDescriptionID + "," + "timestampSeconds="
                + timestampSeconds + "," + "timestampNanoseconds="
                + timestampNanoseconds + "," + "sequenceNumber="
                + sequenceNumber + "," + "bufferLen=" + bufferLen + ","
                + "bufferBytes(in hex)=" + hexData.toString() + "bufferTwoLen="
                + bufferTwoLen + "," + "bufferTwoBytes(in hex)="
                + hexTwoData.toString());
        } catch (JMSException e) {
            logger.error("JMSException caught: " + e.getMessage());
        }
        PacketSQLOutput po = PacketOutputManager.getPacketSQLOutput(deviceID);
        try {
            po.writeBytesMessage(bytesMessage);
        } catch (SQLException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

    } // End onMessage

} // End IngestMDB
