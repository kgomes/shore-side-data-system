package moos.ssds.services.data;

import javax.ejb.Remote;

import moos.ssds.services.data.util.DataException;

@Remote
public interface PacketSubmissionAccess {

	/**
	 * This is the method to submit a packet to the SQL storage mechanism.
	 * 
	 * @ejb.interface-method view-type="both"
	 * @param deviceID
	 *            This is the source of the packet and should be the SSDS ID of
	 *            the device
	 * @param packetBytes
	 *            this is the byte array that contains the data in the format
	 *            specified in the SSDS packet documentation
	 */
	public abstract void submitPacketAsByteArray(long deviceID,
			byte[] packetBytes) throws DataException;

}