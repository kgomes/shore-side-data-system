package moos.ssds.services.metadata;

import java.util.Collection;

import javax.ejb.Local;

import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.DataProducerGroup;

@Local
public interface DataProducerGroupAccessLocal extends AccessLocal {

	/**
	 * @param name
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Collection<DataProducerGroup> findByName(String name,
			boolean exactMatch, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/**
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Collection<String> findAllNames()
			throws MetadataAccessException;

}