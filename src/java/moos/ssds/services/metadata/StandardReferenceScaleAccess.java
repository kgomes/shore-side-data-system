package moos.ssds.services.metadata;

import java.util.Collection;

import javax.ejb.Remote;

import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.StandardReferenceScale;

@Remote
public interface StandardReferenceScaleAccess extends Access {

	/**
	 * @param name
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Collection<StandardReferenceScale> findByName(String name)
			throws MetadataAccessException;

	/**
	 * @param likeName
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Collection<StandardReferenceScale> findByLikeName(
			String likeName) throws MetadataAccessException;

	/**
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Collection<String> findAllNames()
			throws MetadataAccessException;

}