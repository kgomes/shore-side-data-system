package moos.ssds.services.metadata;

import java.util.Collection;

import javax.ejb.Local;

import moos.ssds.dao.DataContainerGroupDAO;
import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.DataContainer;
import moos.ssds.metadata.DataContainerGroup;

@Local
public interface DataContainerGroupAccessLocal extends AccessLocal {

	/**
	 * @see moos.ssds.dao.DataContainerGroupDAO#findByName(String, boolean)
	 */
	public abstract Collection<DataContainerGroup> findByName(String name,
			boolean exactMatch, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/**
	 * @see DataContainerGroupDAO#findAllNames()
	 */
	public abstract Collection<String> findAllNames()
			throws MetadataAccessException;

	/**
	 * @see DataContainerGroupDAO#findByDataContainer(DataContainer)
	 */
	public abstract Collection<DataContainerGroup> findByDataContainer(
			DataContainer dataContainer, boolean returnFullObjectGraph)
			throws MetadataAccessException;

}