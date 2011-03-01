package moos.ssds.services.metadata;

import java.util.Collection;

import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.ResourceType;

public interface IResourceTypeAccess extends IMetadataAccess {

	/**
	 * @param name
	 * @return
	 */
	public abstract Collection<ResourceType> findByName(String name,
			boolean exactMatch) throws MetadataAccessException;

	/**
	 * @return
	 */
	public abstract Collection<String> findAllNames()
			throws MetadataAccessException;

}