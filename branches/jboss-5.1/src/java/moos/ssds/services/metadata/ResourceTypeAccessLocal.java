package moos.ssds.services.metadata;

import java.util.Collection;

import javax.ejb.Local;

import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.ResourceType;

@Local
public interface ResourceTypeAccessLocal extends AccessLocal {

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