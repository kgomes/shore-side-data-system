package moos.ssds.services.metadata;

import java.util.Collection;

import javax.ejb.Local;

import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.RecordVariable;

@Local
public interface RecordVariableAccessLocal extends AccessLocal {

	/**
	 * This method looks for all <code>RecordVariable</code>s whose name is an
	 * exact match of the name supplied.
	 * 
	 * @param name
	 *            is the name that will be used to search for
	 * @return a <code>Collection</code> of <code>RecordVariable</code>s whose
	 *         names exactly match the one specified as the parameter.
	 */
	public abstract Collection<RecordVariable> findByName(String name,
			boolean exactMatch, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/**
	 * This method returns a collection of <code>java.lang.String</code>s that
	 * are all the names of the <code>RecordVariable</code>s in the database
	 * 
	 * @return a <code>Collection</code> of <code>java.lang.String</code>s that
	 *         are all the <code>RecordVariable</code> names that are currently
	 *         in the system. If there are no names, an empty collection is
	 *         returned
	 */
	public abstract Collection<String> findAllNames()
			throws MetadataAccessException;

}