package moos.ssds.services.metadata;

import java.util.Collection;

import javax.ejb.Local;

import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.UserGroup;

@Local
public interface UserGroupAccessLocal extends AccessLocal {

	/**
	 * This method looks for all <code>UserGroup</code>s whose groupName is an
	 * exact match of the name supplied.
	 * 
	 * @param name
	 *            is the name that will be used to search for
	 * @return a <code>Collection</code> of <code>UserGroup</code>s whose names
	 *         exactly match the one specified as the parameter.
	 */
	public abstract Collection<UserGroup> findByGroupName(String groupName,
			boolean exactMatch, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/**
	 * This method returns a count of the number of <code>UserGroup</code>s that
	 * would match the group name specified and whether or not your are
	 * searching for an exact match
	 * 
	 */
	public abstract int countFindByGroupName(String groupName,
			boolean exactMatch) throws MetadataAccessException;

	/**
	 * This method looks for all group names that are available in the
	 * persistent storage
	 * 
	 * @param name
	 *            is the name that will be used to search for
	 * @return a <code>Collection</code> of <code>String</code>s that are all
	 *         the available group names
	 */
	public abstract Collection<String> findAllGroupNames()
			throws MetadataAccessException;

	/**
	 * This method looks returns the count of all group names
	 * 
	 */
	public abstract int countFindAllGroupNames() throws MetadataAccessException;

}