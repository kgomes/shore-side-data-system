package moos.ssds.services.metadata;

import java.util.Collection;
import java.util.Date;

import javax.ejb.Remote;

import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.Event;

@Remote
public interface EventAccess extends Access {

	/**
	 */
	public abstract Collection<Event> findByName(String name,
			boolean exactMatch, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/**
	 */
	public abstract Collection<Event> findByLikeName(String likeName,
			String orderByPropertyName, String ascendingOrDescending,
			boolean returnFullObjectGraph) throws MetadataAccessException;

	/**
	 */
	public abstract Event findByNameAndDates(String name, Date startDate,
			Date endDate, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/**
	 */
	public abstract Collection<String> findAllNames()
			throws MetadataAccessException;

	/**
	 */
	public abstract Collection<Event> findWithinDateRange(Date startDate,
			Date endDate, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

}