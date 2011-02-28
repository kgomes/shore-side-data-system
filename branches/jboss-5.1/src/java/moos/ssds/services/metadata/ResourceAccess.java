package moos.ssds.services.metadata;

import java.net.URI;
import java.net.URL;
import java.util.Collection;

import javax.ejb.Remote;

import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.Person;
import moos.ssds.metadata.Resource;
import moos.ssds.metadata.ResourceType;

@Remote
public interface ResourceAccess extends Access {

	/**
	 * @param name
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Collection<Resource> findByName(String name)
			throws MetadataAccessException;

	/**
	 * @param likeName
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Collection<Resource> findByLikeName(String likeName)
			throws MetadataAccessException;

	/**
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Collection<String> findAllNames()
			throws MetadataAccessException;

	/**
	 * @param uriString
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Resource findByURIString(String uriString)
			throws MetadataAccessException;

	/**
	 * @param uri
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Collection<Resource> findByURI(URI uri)
			throws MetadataAccessException;

	/**
	 * @param url
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Collection<Resource> findByURL(URL url)
			throws MetadataAccessException;

	/**
	 * @param mimeType
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Collection<Resource> findByMimeType(String mimeType)
			throws MetadataAccessException;

	/**
	 * @param person
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Collection<Resource> findByPerson(Person person)
			throws MetadataAccessException;

	/**
	 * @ejb.interface-method view-type="both"
	 * @ejb.transaction type="Required"
	 * @param resourceType
	 * @return
	 */
	public abstract Collection<Resource> findByResourceType(
			ResourceType resourceType, String orderByPropertyName,
			String ascendingOrDescending, boolean returnFullObjectGraph)
			throws MetadataAccessException;

}