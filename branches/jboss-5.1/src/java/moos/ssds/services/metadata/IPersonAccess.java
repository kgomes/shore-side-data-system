package moos.ssds.services.metadata;

import java.util.Collection;

import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.Person;

public interface IPersonAccess extends IMetadataAccess {

	/**
	 * This method tries to look up and instantiate a user by their email
	 * address.
	 * 
	 * @param email
	 *            is a <code>java.lang.String</code> that will be used to search
	 *            for matches of a person's email address
	 * @return a <code>Collection</code> of person objects that have that email
	 *         address. If no matches were found, an empty collection is
	 *         returned
	 * @throws MetadataAccessException
	 *             if something goes wrong with the search
	 */
	public abstract Collection<Person> findByEmail(String email,
			boolean exactMatch, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/**
	 * This method tries to look up and instantiate a user by their username
	 * 
	 * @param username
	 *            is a <code>java.lang.String</code> that will be used to search
	 *            for matches of a person's username
	 * @return a <code>MetadataObject</code> of class <code>Person</code> that
	 *         has a username that matches the one specified. If no matches were
	 *         found, an empty collection is returned
	 * @throws MetadataAccessException
	 *             if something goes wrong with the search
	 */
	public abstract Person findByUsername(String username,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/**
	 * This method returns a collection of <code>java.lang.String</code> that
	 * are all the usernames of people in the database
	 * 
	 * @return a <code>Collection</code> of <code>java.lang.String</code>s that
	 *         are all the usernames that are currently in the system. If there
	 *         are no usernames, null is returned.
	 */
	public abstract Collection<String> findAllUsernames()
			throws MetadataAccessException;

}