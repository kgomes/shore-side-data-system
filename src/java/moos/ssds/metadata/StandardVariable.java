/*
 * Copyright 2009 MBARI
 *
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 2.1 
 * (the "License"); you may not use this file except in compliance 
 * with the License. You may obtain a copy of the License at
 *
 * http://www.gnu.org/copyleft/lesser.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package moos.ssds.metadata;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import moos.ssds.metadata.util.MetadataException;
import moos.ssds.metadata.util.MetadataValidator;

/**
 * This class represents the concept of a StandardVariable in the system. It can
 * be used to group similar variables for simplification of queries, etc.
 * <hr>
 * 
 * @stereotype desscription
 * @hibernate.class table="StandardVariable"
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.19 $
 */
public class StandardVariable implements IMetadataObject, IDescription {

	/**
	 * This is a Log4JLogger that is used to log information to
	 */
	static Logger logger = Logger.getLogger(StandardVariable.class);

	/**
	 * This is the <code>serialVersionUID</code> that is fixed to control
	 * serialization versions of the class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * This is the unique identifier that is used by the persistent storage
	 * mechanism
	 */
	private Long id;

	/**
	 * The name assigned to the <code>StandardVariable</code>
	 */
	private String name;

	/**
	 * This is the URI that serves to distinguish the namespace of the
	 * StandardVariable
	 */
	private String namespaceUriString;

	/**
	 * The description of the <code>StandardVariable</code>
	 */
	private String description;

	/**
	 * The reference scale used for the <code>StandardVariable</code>
	 */
	private String referenceScale;

	/**
	 * The <code>Collection</code> of <code>StandardUnit</code>s linked to the
	 * <code>StandardVariable</code>
	 */
	private Collection<StandardUnit> standardUnits = new HashSet<StandardUnit>();

	/**
	 * This is the hibernate version that is used to check for dirty objects
	 */
	private long version = -1;

	/**
	 * Default constructor that creates an empty description string.
	 */
	public StandardVariable() {
		try {
			this.setDescription("");
		} catch (MetadataException e) {
		}
	}

	/**
	 * @see moos.ssds.metadata.IMetadataObject#getId()
	 * @hibernate.id generator-class="identity" type="long"
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @see moos.ssds.metadata.IMetadataObject#setId(Long)
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * This method returns the name of the <code>StandardVariable</code>
	 * 
	 * @hibernate.property
	 * @hibernate.column name="name" unique="false" not-null="true" length="255"
	 *                   unique-key="name_uri_namespace_key" index="name_index"
	 * @see moos.ssds.metadata.IDescription#getName()
	 * @return a <code>String</code> that is the name of the
	 *         <code>StandardVariable</code>
	 */
	public String getName() {
		return name;
	}

	/**
	 * This method sets the name of the <code>StandardVariable</code>.
	 * 
	 * @param name
	 *            is the name that will be assigned to the
	 *            <code>StandardVariable</code>.
	 */
	public void setName(String name) throws MetadataException {
		MetadataValidator.isObjectNull(name);
		MetadataValidator.isStringShorterThan(name,
				MetadataValidator.NAME_LENGTH);
		this.name = name;
	}

	/**
	 * These methods get and set a <code>String</code> that is the parseable
	 * form of a URI reference as defined by RFC 2396: Uniform Resource
	 * Identifiers (URI): Generic Syntax. The length of the URI string must be
	 * less than 2048 characters.
	 * 
	 * @hibernate.property length="2048"
	 * @hibernate.column name="namespaceUriString" unique="false"
	 *                   not-null="false" unique-key="name_uri_namespace_key"
	 * @return a <code>String</code> that is the ASCII representation of a URI
	 *         Reference
	 * @throws MetadataException
	 *             if something is not right with the URI string
	 */
	public String getNamespaceUriString() {
		return namespaceUriString;
	}

	public void setNamespaceUriString(String namespaceUriString)
			throws MetadataException {
		MetadataValidator.isStringShorterThan(namespaceUriString,
				MetadataValidator.URI_STRING_LENGTH);
		this.namespaceUriString = namespaceUriString;
	}

	/**
	 * These methods get and set a <code>URI</code> that is the object form of
	 * the URI reference specified by the uriString.
	 * 
	 * @return a <code>URI</code> reference that is a unique resource locator
	 *         for the namespace of the <code>StandardVariable</code>
	 */
	public URI getNamespaceUri() {
		URI uriToReturn = null;
		if (this.namespaceUriString == null)
			return null;
		uriToReturn = URI.create(this.namespaceUriString);
		return uriToReturn;
	}

	public void setNamespaceUri(URI namespaceUri) throws MetadataException {
		if (namespaceUri != null) {
			this.setNamespaceUriString(namespaceUri.toASCIIString());
		} else {
			this.namespaceUriString = null;
		}
	}

	/**
	 * This returns the description that has been assigned to the
	 * <code>StandardVariable</code>
	 * 
	 * @hibernate.property length="2048"
	 * @see moos.ssds.metadata.IDescription#getDescription()
	 * @return the description assigned to the <code>StandardVariable</code>
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * This method sets the description of the <code>StandardVariable</code>.
	 * 
	 * @param description
	 *            is the <code>String</code> that will be set as the
	 *            description.
	 */
	public void setDescription(String description) throws MetadataException {
		MetadataValidator.isStringShorterThan(description,
				MetadataValidator.DESCRIPTION_LENGTH);
		this.description = description;
	}

	/**
	 * This method returns the reference scale of the
	 * <code>StandardVariable</code>
	 * 
	 * @hibernate.property
	 * @hibernate.column name="referenceScale" unique="false" not-null="false"
	 *                   length="50"
	 * @return a <code>String</code> that is the refernce scale of the
	 *         <code>StandardVariable</code>
	 */
	public String getReferenceScale() {
		return referenceScale;
	}

	/**
	 * This method sets the reference scale of the <code>StandardVariable</code>
	 * .
	 * 
	 * @param referenceScale
	 *            is the <code>String</code> that will be used as the reference
	 *            scale.
	 */
	public void setReferenceScale(String referenceScale)
			throws MetadataException {
		if (referenceScale != null)
			MetadataValidator.isStringShorterThan(referenceScale, 50);
		this.referenceScale = referenceScale;
	}

	/**
	 * This method returns the <code>Collection</code> of
	 * <code>StandardUnit</code>s that apply to this standard variable
	 * 
	 * @hibernate.set table="StandardVariableAssocStandardUnit" cascade="none"
	 *                lazy="true"
	 * @hibernate.collection-key column="StandardVariableID_FK"
	 * @hibernate.collection-many-to-many column="StandardUnitID_FK"
	 *                                    class="moos.ssds.metadata.StandardUnit"
	 * @return the <code>Collection</code> of <code>StandardUnit</code>s that
	 *         are associated with the <code>StandardVariable</code>
	 */
	public Collection<StandardUnit> getStandardUnits() {
		return standardUnits;
	}

	/**
	 * This method assigns the <code>Collection</code> of
	 * <code>StandardUnits</code> to the <code>StandardVariable</code>
	 * 
	 * @param standardUnits
	 *            is the <code>Collection</code> that will be assigned
	 */
	public void setStandardUnits(Collection<StandardUnit> standardUnits) {
		this.standardUnits = standardUnits;
	}

	/**
	 * This method add the given <code>StandardUnit</code> to the collection
	 * associated with the <code>StandardVariable</code>.
	 * 
	 * @param standardUnit
	 *            the <code>StandardUnit</code> to add
	 */
	public void addStandardUnit(StandardUnit standardUnit) {
		// If null was passed in, just return
		if (standardUnit == null)
			return;

		// Make sure the collection is there
		if (this.standardUnits == null)
			this.standardUnits = new HashSet<StandardUnit>();

		// Now add the StandardUnit to the collection
		if (!this.standardUnits.contains(standardUnit)) {
			this.standardUnits.add(standardUnit);
		}
	}

	/**
	 * This method removes the given <code>StandardUnit</code> from the
	 * collection
	 * 
	 * @param standardUnit
	 *            is the <code>StandardUnit</code> to remove from the collection
	 */
	public void removeStandardUnit(StandardUnit standardUnit) {
		if (standardUnit == null)
			return;
		if ((this.standardUnits != null)
				&& (this.standardUnits.contains(standardUnit))) {
			this.standardUnits.remove(standardUnit);
		}
	}

	/**
	 * This method will clear out the collection of <code>StandardUnit</code>s
	 * and keep the integrity of the relationships intact.
	 */
	public void clearStandardUnits() {
		this.standardUnits.clear();
	}

	/**
	 * This method returns the version that Hibernate uses to check for dirty
	 * objects
	 * 
	 * @hibernate.version type=long
	 * @return the <code>long</code> that is the version of the instance of the
	 *         class
	 */
	public long getVersion() {
		return version;
	}

	/**
	 * This method is used to set the version that Hibernate will check to
	 * figure out if the object is dirty
	 * 
	 * @param version
	 */
	public void setVersion(long version) {
		this.version = version;
	}

	/**
	 * This method overrides the default equals method and checks for to see if
	 * the objects occupy the same memory space and if not, then it checks for
	 * identical persistent identifiers and if those are not available, it
	 * checks for equality of the business key which is the name and
	 * referenceScale
	 * 
	 * @see moos.ssds.metadata.IMetadataObject#equals(Object)
	 */
	public boolean equals(Object obj) {
		// First check to see if input is null
		if (obj == null)
			return false;

		// Now check JVM identity
		if (this == obj)
			return true;

		// Now check if it is the correct class
		if (!(obj instanceof StandardVariable))
			return false;

		// Cast to StandardUnit object
		final StandardVariable that = (StandardVariable) obj;

		// Now check for missing business key (name and reference scale)
		if ((this.name == null) || (that.getName() == null))
			return false;

		// Now compare hashcodes
		if (this.hashCode() == that.hashCode()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * This method overrides the default hashCode and had to be implemented
	 * because we overrode the default equals method. They both should base
	 * their calculation on the business key.
	 * 
	 * @see moos.ssds.metadata.IMetadataObject#hashCode()
	 */
	public int hashCode() {
		// Calculate the hashcodes
		int result = 72;
		if (name != null) {
			result = 42 * result + name.hashCode();
		}
		if (namespaceUriString != null) {
			result = 9 * result + namespaceUriString.hashCode();
		}

		// Now return it
		return result;
	}

	/**
	 * @see moos.ssds.metadata.IMetadataObject#toStringRepresentation(String)
	 */
	public String toStringRepresentation(String delimiter) {
		// If the delimiter is not specified, use a default one
		if (delimiter == null)
			delimiter = IMetadataObject.DEFAULT_DELIMITER;

		StringBuffer sb = new StringBuffer();
		sb.append("StandardVariable");
		sb.append(delimiter + "id=" + this.getId());
		sb.append(delimiter + "name=" + this.getName());
		sb.append(delimiter + "namespaceUriString="
				+ this.getNamespaceUriString());
		sb.append(delimiter + "description=" + this.getDescription());
		sb.append(delimiter + "referenceScale=" + this.getReferenceScale());
		return sb.toString();
	}

	/**
	 * In order to use the class, you should first create an empty object, then
	 * call this method, passing in the string representation. As an example:
	 * 
	 * <pre>
	 * StandardVariable newStandardVariable = new StandardVariable();
	 * 
	 * newStandardVariable
	 * 		.setValuesFromStringRepresentation(
	 * 				&quot;StandardVariable|name=SVOne|description=A new standard variable|referenceScale=SV Ref Scale|&quot;,
	 * 				&quot;|&quot;);
	 * </pre>
	 * 
	 * @see moos.ssds.metadata.IMetadataObject#setValuesFromStringRepresentation
	 */
	public void setValuesFromStringRepresentation(String stringRepresentation,
			String delimiter) throws MetadataException {
		// If the delimiter is null, use the default delimiter
		String delimiterToUse = delimiter;
		if (delimiterToUse == null)
			delimiterToUse = IMetadataObject.DEFAULT_DELIMITER;

		// Create a string tokenizer that uses the delimiter specified (or the
		// default)
		StringTokenizer stok = new StringTokenizer(stringRepresentation,
				delimiterToUse);

		// Grab the first token, which should be the name of the metadata class
		String firstToken = stok.nextToken();

		// Check to make sure it matches this class and if not, throw an
		// Exception
		if ((!this.getClass().getName().equals(firstToken))
				&& (!this.getClass().getName().equals(
						"moos.ssds.metadata." + firstToken)))
			throw new MetadataException(
					"The class specified by the first token (" + firstToken
							+ " does not match this class "
							+ this.getClass().getName());

		// Now loop over the attribute=value pairs to fill out the object
		while (stok.hasMoreTokens()) {
			// Grab the next pair
			String tok = stok.nextToken();

			// Split on the equals sign
			int firstEquals = tok.indexOf("=");
			String key = null;
			String value = null;
			if (firstEquals >= 0) {
				key = tok.substring(0, firstEquals);
				value = tok.substring(firstEquals + 1);
			} else {
				key = "";
				value = "";
			}

			// Now look for a match on the key and then assign the value
			if (key.equalsIgnoreCase("id")) {
				try {
					this.setId(new Long(value));
				} catch (NumberFormatException e) {
					throw new MetadataException(
							"Could not convert the value for id (" + value
									+ ") to a Long");
				}
			} else if (key.equalsIgnoreCase("name")) {
				this.setName(value);
			} else if (key.equalsIgnoreCase("namespaceUriString")) {
				this.setNamespaceUriString(value);
			} else if (key.equalsIgnoreCase("description")) {
				this.setDescription(value);
			} else if (key.equalsIgnoreCase("referenceScale")) {
				this.setReferenceScale(value);
			} else {
				throw new MetadataException("The attribute specified by " + key
						+ " is not a recognized field of "
						+ this.getClass().getName());
			}
		}
	}

	/**
	 * This is the method to re-consitutute and object from a custom
	 * serialization form
	 * 
	 * @see Externalizable#readExternal(ObjectInput)
	 */
	@SuppressWarnings("unchecked")
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		description = (String) in.readObject();
		// Read in ID
		Object idObject = in.readObject();
		if (idObject instanceof Integer) {
			Integer intId = (Integer) idObject;
			id = new Long(intId.longValue());
		} else if (idObject instanceof Long) {
			id = (Long) idObject;
		}
		name = (String) in.readObject();
		namespaceUriString = (String) in.readObject();
		referenceScale = (String) in.readObject();
		standardUnits = (Collection<StandardUnit>) in.readObject();
		// Read in the version
		Object versionObject = in.readObject();
		if (versionObject instanceof Integer) {
			Integer intVersion = (Integer) versionObject;
			version = new Long(intVersion.longValue());
		} else if (versionObject instanceof Long) {
			version = (Long) versionObject;
		}
	}

	/**
	 * This is the method to serialize a StandardVariable to a custom serialized
	 * form
	 * 
	 * @see Externalizable#writeExternal(ObjectOutput)
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(description);
		out.writeObject(id);
		out.writeObject(name);
		out.writeObject(namespaceUriString);
		out.writeObject(referenceScale);
		// StandardUnits
		out.writeObject(standardUnits);
		out.writeObject(version);
	}

	/**
	 * This method overrides the clone method to produce a copy of an object. It
	 * will clear the ID on the newly created copy to prevent odd behavior.
	 */
	protected Object clone() throws CloneNotSupportedException {
		// Create the clone
		StandardVariable clone = new StandardVariable();

		// Set the fields
		try {
			clone.setId(this.getId());
			clone.setName(this.getName());
			clone.setNamespaceUri(this.getNamespaceUri());
			clone.setDescription(this.getDescription());
			clone.setReferenceScale(this.getReferenceScale());
		} catch (MetadataException e) {
		}

		// Return the clone
		return clone;
	}

	/**
	 * This simply returns the clone
	 */
	public IMetadataObject deepCopy() throws CloneNotSupportedException {
		logger.debug("deepCopy called");
		// Grab the clone
		StandardVariable deepClone = (StandardVariable) this.clone();
		logger.debug("A clone was created and is:");
		logger.debug(deepClone.toStringRepresentation("|"));

		// Set the relationships
		if ((this.getStandardUnits() != null)
				&& (this.getStandardUnits().size() > 0)) {
			logger.debug("There are " + this.getStandardUnits().size()
					+ " StandardUnits to clone and attach");
			Collection<StandardUnit> standardUnits = this.getStandardUnits();
			Iterator<StandardUnit> suIter = standardUnits.iterator();
			while (suIter.hasNext()) {
				StandardUnit clonedStandardUnit = (StandardUnit) ((StandardUnit) suIter
						.next()).deepCopy();
				logger
						.debug("The following cloned StandardUnit will be added:");
				logger.debug(clonedStandardUnit.toStringRepresentation("|"));
				deepClone.addStandardUnit(clonedStandardUnit);
			}
		}

		// Now return the deep clone
		return deepClone;
	}
}