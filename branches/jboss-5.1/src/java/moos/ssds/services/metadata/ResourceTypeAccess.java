package moos.ssds.services.metadata;

import java.util.Collection;

import javax.ejb.Remote;

import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.ResourceType;

@Remote
public interface ResourceTypeAccess extends Access {

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