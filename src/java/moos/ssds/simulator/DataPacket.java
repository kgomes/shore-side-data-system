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

import org.mbari.siam.distributed.DeviceMessagePacket;
import org.mbari.siam.distributed.MetadataPacket;
import org.mbari.siam.distributed.SensorDataPacket;


/**
 * <p><!--Insert summary here--></p><hr>
 *
 * @author  : $Author: kgomes $
 * @version : $Revision: 1.1 $
 * @deprecated 23 Apr 2003 - Using SIAM packets instead.
 */
public class DataPacket extends SensorDataPacket {
    private long metadataID;
    private long platformID;
    private long recordType;

	public static int VERSION_ID = 1;
//    private static final long serialVersionUID = 1487153620725598758L;
    private static final long serialVersionUID = 680021341142244258L;
    /**
     *	Public no-argument constructor is required for the Externalizable interface
     */

    public DataPacket(long ID, int size) {
        super(ID, size);
    }

    /**
     * A copy constructor that casts a <code>SensorDataPacket</code> to a
     * <code>DataPacket</code>. It sets the metadataSequenceNumber, parentDeviceID,
     * and recordType to -1
     *
     *	@param packet A sensor data packet
     *  @see org.mbari.isi.interfaces.SensorDataPacket
     */
    public DataPacket(SensorDataPacket packet) {
        this(packet.sourceID(), packet.dataBuffer().length);
        setMetadataID(-1);
        setPlatformID(-1);
        setRecordType(-1);
        setDataBuffer(packet.dataBuffer());
        setSequenceNo(packet.sequenceNo());
        setSystemTime(packet.systemTime());
    }

    public DataPacket(MetadataPacket packet) {
        this(packet.sourceID(), packet.getBytes().length);
        setMetadataID(-1);
        setPlatformID(-1);
        setRecordType(0);
        setDataBuffer(packet.getBytes());
        setSequenceNo(packet.sequenceNo());
        setSystemTime(packet.systemTime());
    }

    /**
     * Create the DataPacket from a DeviceMessagePacket
     */
    public DataPacket(DeviceMessagePacket packet) {
        this(packet.sourceID(), packet.getMessage().length);
        setMetadataID(-1);
        setPlatformID(-1);
        setRecordType(-1);
        setDataBuffer(packet.getMessage());
        setSequenceNo(packet.sequenceNo());
        setSystemTime(packet.systemTime());
    }

    /**
     * Copy constructor
     */
    public DataPacket(DataPacket packet) {
        this(packet.sourceID(), packet.dataBuffer().length);
        setMetadataID(packet.getMetadataID());
        setPlatformID(packet.getPlatformID());
        setRecordType(packet.getRecordType());
        setDataBuffer(packet.dataBuffer());
        setSequenceNo(packet.sequenceNo());
        setSystemTime(packet.systemTime());
    }

    public long getMetadataID() { return metadataID; }

    public void setMetadataID(long descriptionID) {
        this.metadataID = descriptionID;
    }

    public long getPlatformID() { return platformID; }

    public void setPlatformID(long parentDeviceID) {
        this.platformID = parentDeviceID;
    }

    public long getRecordType() { return recordType; }

    public void setRecordType(long variableID)
        { this.recordType = variableID; }


}
