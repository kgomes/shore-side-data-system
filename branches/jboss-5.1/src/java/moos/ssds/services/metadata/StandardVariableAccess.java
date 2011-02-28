package moos.ssds.services.metadata;

import java.util.Collection;

import javax.ejb.Remote;

import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.IMetadataObject;
import moos.ssds.metadata.RecordVariable;
import moos.ssds.metadata.StandardVariable;

@Remote
public interface StandardVariableAccess extends Access {

	/**
	 * This method tries to look up and instantiate a
	 * <code>StandardVariable</code> by its name and reference scale
	 * 
	 * @param name
	 *            is a <code>java.lang.String</code> that will be used to search
	 *            for matches of a <code>StandardVariable</code>'s name
	 * @param referenceScale
	 *            is a <code>java.lang.String</code> that will be used to search
	 *            for matches of a <code>StandardVariable</code>'s reference
	 *            scale
	 * @return a <code>MetadataObject</code> of class
	 *         <code>StandardVariable</code> that has a name and reference scale
	 *         that matches the one specified. If no matches were found, and
	 *         empty collection is returned
	 * @throws MetadataAccessException
	 *             if something goes wrong with the search
	 */
	public abstract IMetadataObject findByNameAndReferenceScale(String name,
			String referenceScale) throws MetadataAccessException;

	/**
	 * This method looks for all <code>StandardVariable</code>s whose name is an
	 * exact match of the name supplied.
	 * 
	 * @param name
	 *            is the name that will be used to search for
	 * @return a <code>Collection</code> of <code>StandardVariable</code>s whose
	 *         names exactly match the one specified as the parameter.
	 */
	public abstract Collection<StandardVariable> findByName(String name)
			throws MetadataAccessException;

	/**
	 * This method looks for all <code>StandardVariable</code>s whose name
	 * contain the name supplied. It could be an exact match of just contain the
	 * name. For you wildcard folks, it is basically looking for all
	 * <code>StandardVariable</code>s whose names match *likeName*.
	 * 
	 * @param likeName
	 *            is the name that will be used to search for. In SQL terms, it
	 *            will do a LIKE '%likeName%'
	 * @return a <code>Collection</code> of <code>StandardVariable</code>s that
	 *         have names like the one specified as the parameter.
	 */
	public abstract Collection<StandardVariable> findByLikeName(String likeName)
			throws MetadataAccessException;

	/**
	 * This method tries to look up all <code>StandardVariable</code>s by their
	 * reference scale
	 * 
	 * @param name
	 *            is a <code>java.lang.String</code> that will be used to search
	 *            for exact matches of a <code>StandardVariable</code>'s
	 *            reference scale (this is case in-sensitive)
	 * @return a <code>Collection</code> of <code>StandardVariable</code>s that
	 *         have a reference scale that exactly matches (case-insensitive)
	 *         the one specified. If no matches were found, an empty collection
	 *         is returned.
	 * @throws MetadataAccessException
	 *             if something goes wrong with the search
	 */
	public abstract Collection<StandardVariable> findByReferenceScale(
			String referenceScale) throws MetadataAccessException;

	/**
	 * This method looks for all <code>StandardVariable</code>s whose
	 * referenceScale contain the referenceScale supplied. It could be an exact
	 * match of just contain the referenceScale. For you wildcard folks, it is
	 * basically looking for all <code>StandardVariable</code>s whose
	 * referenceScales match *likeReferenceScale*.
	 * 
	 * @param likeReferenceScale
	 *            is the referenceScale that will be used to search for. In SQL
	 *            terms, it will do a LIKE '%likeReferenceScale%'
	 * @return a <code>Collection</code> of <code>StandardVariable</code>s that
	 *         have referenceScales like the one specified as the parameter.
	 */
	public abstract Collection<StandardVariable> findByLikeReferenceScale(
			String likeReferenceScale) throws MetadataAccessException;

	/**
	 * This method returns a collection of <code>java.lang.String</code>s that
	 * are all the names of the <code>StandardVariable</code>s in the database
	 * 
	 * @return a <code>Collection</code> of <code>java.lang.String</code>s that
	 *         are all the <code>StandardVariable</code> names that are
	 *         currently in the system. If there are no names, an empty
	 *         collection is returned
	 */
	public abstract Collection<String> findAllNames()
			throws MetadataAccessException;

	/**
	 * This method returns a collection of <code>java.lang.String</code>s that
	 * are all the referenceScales of the <code>StandardVariable</code>s in the
	 * database
	 * 
	 * @return a <code>Collection</code> of <code>java.lang.String</code>s that
	 *         are all the <code>StandardVariable</code> referenceScales that
	 *         are currently in the system. If there are no referenceScales, an
	 *         empty collection is returned
	 */
	public abstract Collection<String> findAllReferenceScales()
			throws MetadataAccessException;

	/**
	 * This method looks for the <code>StandardVariable</code> that is linked to
	 * the given <code>RecordVariable</code>
	 * 
	 * @param recordVariable
	 *            is the <code>RecordVariable</code> that will be used to look
	 *            up the <code>StandardVariable</code>
	 * @return a <code>StandardVariable</code> that is linked to the *
	 *         <code>RecordVariable</code>. Null is returned if there is no
	 *         relationship defined.
	 */
	public abstract StandardVariable findByRecordVariable(
			RecordVariable recordVariable) throws MetadataAccessException;

}