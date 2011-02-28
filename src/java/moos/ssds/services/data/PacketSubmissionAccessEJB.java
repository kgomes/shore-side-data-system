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

import java.sql.SQLException;

import javax.annotation.PostConstruct;
import javax.ejb.CreateException;
import javax.ejb.Stateless;

import moos.ssds.io.PacketOutputManager;
import moos.ssds.io.PacketSQLOutput;
import moos.ssds.services.data.util.DataException;

import org.apache.log4j.Logger;
import org.jboss.ejb3.annotation.LocalBinding;
import org.jboss.ejb3.annotation.RemoteBinding;

/**
 * This EJB is a <code>SessionBean</code> that gives clients the capability of
 * submitting packets that match the SSDS byte array format. These packets will
 * end up in the SQL database storage for the SSDS system.
 * 
 * @author kgomes
 */
@Stateless
@RemoteBinding(jndiBinding = "moos/ssds/services/data/PacketSubmissionAccess")
@LocalBinding(jndiBinding = "moos/ssds/services/data/PacketSubmissionAccessLocal")
public class PacketSubmissionAccessEJB implements PacketSubmissionAccess,
		PacketSubmissionAccessLocal {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The EJB callback that is used when the bean is created
	 */
	@PostConstruct
	public void ejbCreate() throws CreateException {
		logger.debug("Creating the PacketSubmissionEJB");

		// Get the PacketOutputManager singleton
		if (this.packetOutputManager == null) {
			this.packetOutputManager = PacketOutputManager.getInstance();
		}

		logger.debug("OK, the instance should be created");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * moos.ssds.services.data.PacketSubmissionAccess#submitPacketAsByteArray
	 * (long, byte[])
	 */
	@Override
	public void submitPacketAsByteArray(long deviceID, byte[] packetBytes)
			throws DataException {
		// Grab the appropriate PacketSQLOutput
		PacketSQLOutput packetSQLOutput = PacketOutputManager
				.getPacketSQLOutput(null, deviceID);

		// Now write the packet
		try {
			packetSQLOutput.writeBytes(packetBytes);
		} catch (SQLException e) {
			logger.error("SQLException was caught trying to write bytes to SQL DataSource: "
					+ e.getMessage());
			throw new DataException(
					"An SQLException was caught trying to insert the given bytes array: "
							+ e.getMessage());
		} catch (Exception e) {
			logger.error("A " + e.getClass().getName()
					+ " was caught trying to write bytes to SQL DataSource: "
					+ e.getMessage());
			throw new DataException("A " + e.getClass().getName()
					+ " was caught trying to insert the given bytes array: "
					+ e.getMessage());
		}
	}

	PacketOutputManager packetOutputManager = null;

	/** A log4j logger */
	static Logger logger = Logger.getLogger(PacketSubmissionAccessEJB.class);

}