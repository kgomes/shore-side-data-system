package moos.ssds.services.metadata;

import java.util.Collection;

import javax.ejb.Local;

import moos.ssds.dao.KeywordDAO;
import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.IMetadataObject;
import moos.ssds.metadata.Keyword;

@Local
public interface KeywordAccessLocal extends AccessLocal {

	/**
	 * @param name
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Collection<Keyword> findByName(String name,
			boolean exactMatch) throws MetadataAccessException;

	/**
	 * @return
	 * @throws MetadataAccessException
	 */
	public abstract Collection<String> findAllNames()
			throws MetadataAccessException;

	/**
	 * @see KeywordDAO#findByMetadataObject(IMetadataObject)
	 */
	public abstract Collection<Keyword> findByMetadataObject(
			IMetadataObject metadataObject) throws MetadataAccessException;

}