package moos.ssds.services.metadata;

import java.util.Collection;

import javax.ejb.Remote;

import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.StandardDomain;

@Remote
public interface StandardDomainAccess extends Access {

	/**
	 * @param name
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Collection<StandardDomain> findByName(String name)
			throws MetadataAccessException;

	/**
	 * @param likeName
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Collection<StandardDomain> findByLikeName(String likeName)
			throws MetadataAccessException;

	/**
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Collection<String> findAllNames()
			throws MetadataAccessException;

}