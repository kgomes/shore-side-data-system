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

public class SSDSGeoLocatedDevicePacket extends SSDSDevicePacket implements
		java.io.Serializable {

	/**
	 * Currently we are at version 3
	 */
	private static final long serialVersionUID = 3L;

	/**
	 * @see moos.ssds.io.SSDSDevicePacket#SSDSDevicePacket(long)
	 * @param sourceID
	 */
	public SSDSGeoLocatedDevicePacket(long sourceID) {
		super(sourceID);
	}

	/**
	 * @return Returns the latitude.
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * @param latitude
	 *            The latitude to set.
	 */
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	/**
	 * @return Returns the longitude.
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * @param longitude
	 *            The longitude to set.
	 */
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	/**
	 * @return Returns the depth.
	 */
	public float getDepth() {
		return depth;
	}

	/**
	 * @param depth
	 *            The depth to set.
	 */
	public void setDepth(float depth) {
		this.depth = depth;
	}

	/**
	 * Override the toString method
	 */
	public String toString() {
		String dataBufferString = null;
		String otherBufferString = null;
		if (this.getDataBuffer() != null) {
			dataBufferString = new String(this.getDataBuffer());
		} else {
			dataBufferString = new String("");
		}
		if (this.getOtherBuffer() != null) {
			otherBufferString = new String(this.getOtherBuffer());
		} else {
			otherBufferString = new String("");
		}
		String stringToReturn = new String(
				"SSDSGeoLocatedDevicePacket:deviceID=" + this.sourceID()
						+ ";sequenceNumber=" + this.sequenceNo()
						+ ";packetType=" + this.getPacketType()
						+ ";metadataSequenceNumber="
						+ this.getMetadataSequenceNumber()
						+ ";dataDescriptionVersion="
						+ this.getDataDescriptionVersion() + ";platformID="
						+ this.getPlatformID() + ";recordType="
						+ this.getRecordType() + ";dataBuffer="
						+ dataBufferString + ";otherBuffer="
						+ otherBufferString + ";latitude=" + this.latitude
						+ ";longitude=" + this.longitude + ";depth="
						+ this.depth);
		return stringToReturn;
	}

	/**
	 * This latitude where the packet was sampled
	 */
	private double latitude = 0;

	/**
	 * The longitude where the packet was sampled
	 */
	private double longitude = 0;

	/**
	 * The depth where the packet was sampled
	 */
	private float depth = 0;
}
