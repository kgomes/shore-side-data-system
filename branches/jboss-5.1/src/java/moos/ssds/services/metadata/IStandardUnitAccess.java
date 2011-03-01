package moos.ssds.services.metadata;

import java.util.Collection;

import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.StandardUnit;

public interface IStandardUnitAccess extends IMetadataAccess {

	/**
	 * This method tries to look up and instantiate a <code>StandardUnit</code>
	 * by its name
	 * 
	 * @param name
	 *            is a <code>java.lang.String</code> that will be used to search
	 *            for matches of a <code>StandardUnit</code>'s name
	 * @return a <code>MetadataObject</code> of class <code>StandardUnit</code>
	 *         that has a name that matches the one specified. If no matches
	 *         were found, and empty collection is returned
	 * @throws MetadataAccessException
	 *             if something goes wrong with the search
	 */
	public abstract StandardUnit findByName(String name)
			throws MetadataAccessException;

	/**
	 * This method looks for all <code>StandardUnit</code>s whose name contain
	 * the name supplied. It could be an exact match of just contain the name.
	 * For you wildcard folks, it is basically looking for all
	 * <code>StandardUnit</code>s whose names match *likeName*.
	 * 
	 * @param likeName
	 *            is the name that will be used to search for. In SQL terms, it
	 *            will do a LIKE '%likeName%'
	 * @return a <code>Collection</code> of <code>StandardUnit</code>s that have
	 *         names like the one specified as the parameter.
	 */
	public abstract Collection<StandardUnit> findByLikeName(String likeName)
			throws MetadataAccessException;

	/**
	 * This method tries to look up all <code>StandardUnit</code>s by their
	 * symbol
	 * 
	 * @param name
	 *            is a <code>java.lang.String</code> that will be used to search
	 *            for exact matches of a <code>StandardUnit</code>'s symbol
	 *            (this is case in-sensitive)
	 * @return a <code>Collection</code> of <code>StandardUnit</code>s that have
	 *         a symbol that exactly matches (case-insensitive) the one
	 *         specified. If no matches were found, an empty collection is
	 *         returned.
	 * @throws MetadataAccessException
	 *             if something goes wrong with the search
	 */
	public abstract Collection<StandardUnit> findBySymbol(String symbol)
			throws MetadataAccessException;

	/**
	 * This method looks for all <code>StandardUnit</code>s whose symbol contain
	 * the symbol supplied. It could be an exact match of just contain the
	 * symbol. For you wildcard folks, it is basically looking for all
	 * <code>StandardUnit</code>s whose symbols match *likeSymbol*.
	 * 
	 * @param likeSymbol
	 *            is the symbol that will be used to search for. In SQL terms,
	 *            it will do a LIKE '%likeSymbol%'
	 * @return a <code>Collection</code> of <code>StandardUnit</code>s that have
	 *         symbols like the one specified as the parameter.
	 */
	public abstract Collection<StandardUnit> findByLikeSymbol(String likeSymbol)
			throws MetadataAccessException;

	/**
	 * This method returns a collection of <code>java.lang.String</code>s that
	 * are all the names of the <code>StandardUnit</code>s in the database
	 * 
	 * @return a <code>Collection</code> of <code>java.lang.String</code>s that
	 *         are all the <code>StandardUnit</code> names that are currently in
	 *         the system. If there are no names, an empty collection is
	 *         returned
	 */
	public abstract Collection<String> findAllNames()
			throws MetadataAccessException;

}