package moos.ssds.services.metadata;

import java.net.URI;
import java.net.URL;
import java.util.Collection;

import javax.ejb.Local;

import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.Person;
import moos.ssds.metadata.Software;

@Local
public interface SoftwareAccessLocal extends AccessLocal {

	/**
	 * @param name
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Collection<Software> findByName(String name)
			throws MetadataAccessException;

	/**
	 * @param likeName
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Collection<Software> findByLikeName(String likeName)
			throws MetadataAccessException;

	/**
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Collection<String> findAllNames()
			throws MetadataAccessException;

	/**
	 * @param name
	 * @param version
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Software findByNameAndSoftwareVersion(String name,
			String version, boolean returnFullObjectGraph)
			throws MetadataAccessException;

	/**
	 * @param uriString
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Collection<Software> findByURIString(String uriString)
			throws MetadataAccessException;

	/**
	 * @param uri
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Collection<Software> findByURI(URI uri)
			throws MetadataAccessException;

	/**
	 * @param url
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Collection<Software> findByURL(URL url)
			throws MetadataAccessException;

	/**
	 * @param person
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Collection<Software> findByPerson(Person person)
			throws MetadataAccessException;

}