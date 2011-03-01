package moos.ssds.services.metadata;

import java.util.Collection;

import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.DeviceType;

public interface IDeviceTypeAccess extends IMetadataAccess {

	/**
	 * This method tries to look up and instantiate a <code>DeviceType</code> by
	 * its name. This is looking for an exact match.
	 * 
	 * @param username
	 *            is a <code>java.lang.String</code> that will be used to search
	 *            for matches of a DeviceType's name
	 * @return a <code>MetadataObject</code> of class <code>DeviceType</code>
	 *         that has a name that matches the one specified. If no matches
	 *         were found, null will be returned
	 * @throws MetadataAccessException
	 *             if something goes wrong with the search
	 */
	public abstract DeviceType findByName(String name,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/**
	 * This method looks for all deviceTypes whose name contain the name
	 * supplied. It could be an exact match of just contain the name. For you
	 * wildcard folks, it is basically looking for all devices whose names match
	 * *likeName*.
	 * 
	 * @param likeName
	 *            is the name that will be used to search for. In SQL terms, it
	 *            will do a LIKE '%likeName%'
	 * @return a <code>Collection</code> of <code>DeviceType</code>s that have
	 *         names like the one specified as the parameter.
	 */
	public abstract Collection<DeviceType> findByLikeName(String likeName,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/**
	 * This method returns a <code>Collection</code> of <code>String</code>s
	 * that are the names of all the deviceTypes that are in SSDS.
	 * 
	 * @return a <code>Collection</code> of <code>String</code>s that are the
	 *         names of all deviceTypes in SSDS.
	 * @throws MetadataAccessException
	 *             if something goes wrong in the method call.
	 */
	public abstract Collection<String> findAllNames()
			throws MetadataAccessException;

}