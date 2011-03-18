package moos.ssds.services.metadata;

import java.util.Collection;

import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.StandardDomain;

public interface IStandardDomainAccess extends IMetadataAccess {

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