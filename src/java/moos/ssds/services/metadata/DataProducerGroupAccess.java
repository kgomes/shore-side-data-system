package moos.ssds.services.metadata;

import java.util.Collection;

import javax.ejb.Remote;

import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.DataProducerGroup;

@Remote
public interface DataProducerGroupAccess extends Access {

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