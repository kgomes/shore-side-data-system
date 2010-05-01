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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import moos.ssds.metadata.util.MetadataException;
import moos.ssds.metadata.util.MetadataValidator;

/**
 * This class represents a piece of software in the system.
 * <hr>
 * 
 * @stereotype thing
 * @hibernate.class table="Software"
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.16 $
 */
public class Software implements IMetadataObject, IDescription, IResourceOwner {

	/**
	 * @see IMetadataObject#getId()
	 * @hibernate.id generator-class="identity" type="long"
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @see IMetadataObject#setId(Long)
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @see IDescription#getName()
	 * @hibernate.property
	 * @hibernate.column name="name" length="255" index="name_index"
	 *                   not-null="true"
	 */
	public String getName() {
		return name;
	}

	/**
	 * @see IDescription#setName(String)
	 */
	public void setName(String name) throws MetadataException {
		MetadataValidator.isObjectNull(name);
		MetadataValidator.isStringShorterThan(name,
				MetadataValidator.NAME_LENGTH);
		this.name = name;
	}

	/**
	 * @see IDescription#getDescription()
	 * @hibernate.property length="2048"
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @see IDescription#setDescription(String)
	 */
	public void setDescription(String description) throws MetadataException {
		MetadataValidator.isStringShorterThan(description,
				MetadataValidator.DESCRIPTION_LENGTH);
		this.description = description;
	}

	/**
	 * These methods get and set a <code>String</code> that is the parseable
	 * form of a URI refernce as defined by RFC 2396: Uniform Rsource
	 * Identifiers (URI): Generic Syntax. <b>It must be specified and must be
	 * unique before the object can be persisted in SSDS</b>.
	 * 
	 * @hibernate.property length="2048"
	 * @hibernate.column name="uriString" unique="true" not-null="false"
	 * @return a <code>String</code> that is the ASCII representation of a URI
	 *         Reference
	 */
	public String getUriString() {
		return uriString;
	}

	public void setUriString(String uriString) throws MetadataException {
		MetadataValidator.isStringShorterThan(uriString,
				MetadataValidator.URI_STRING_LENGTH);
		this.uriString = uriString;
	}

	/**
	 * This method returns a <code>URI</code> that is the object form of the URI
	 * reference specified by the uriString.
	 * 
	 * @return a <code>URI</code> reference that is a unique resource locator
	 *         for the <code>Software</code>
	 */
	public URI getUri() {
		URI uriToReturn = null;
		uriToReturn = URI.create(this.uriString);
		return uriToReturn;
	}

	/**
	 * This method takes in a <code>URI</code> and sets the unique resource
	 * locator for this <code>Resource</code> to the <code>URI</code>.
	 * 
	 * @param uri
	 *            a <code>URI</code> reference that is a unique resource locator
	 *            for the <code>Software</code>
	 */
	public void setUri(URI uri) throws MetadataException {
		this.setUriString(uri.toASCIIString());
	}

	/**
	 * This method returns a <code>URL</code> that is created from the
	 * <code>URI</code> that was specified for this <code>Software</code>.
	 * 
	 * @return a <code>URL</code> which is simply a <code>URL</code>
	 *         representation of the <code>URI</code> specified for the
	 *         <code>Software</code>
	 */
	public URL getUrl() {

		URL urlToReturn = null;
		try {
			urlToReturn = getUri().toURL();
		} catch (MalformedURLException e) {
			urlToReturn = null;
		}
		return urlToReturn;
	}

	/**
	 * This method takes in a <code>URL</code> and converts it to a
	 * <code>URI</code> and stores it as the unique resource locator for the
	 * <code>Software</code>
	 * 
	 * @param url
	 *            is the <code>URL</code> to use as the unique resource locator
	 *            for this <code>Software</code>
	 */
	public void setUrl(URL url) throws MetadataException {
		this.setUriString(url.toExternalForm());
	}

	/**
	 * These methods get and set the version of the software object
	 * 
	 * @hibernate.property not-null="true"
	 * @return a <code>String</code> that identifies the version of the software
	 */
	public String getSoftwareVersion() {
		return softwareVersion;
	}

	public void setSoftwareVersion(String softwareVersion)
			throws MetadataException {
		MetadataValidator.isObjectNull(softwareVersion);
		this.softwareVersion = softwareVersion;
	}

	/**
	 * These methods get and set the <code>Person</code> that is usually seen as
	 * the owner of the <code>Software</code> (or point of contact).
	 * 
	 * @hibernate.many-to-one class="moos.ssds.metadata.Person"
	 *                        column="PersonID_FK"
	 *                        foreign-key="Software_Owned_By_Person"
	 *                        cascade="none" lazy="true"
	 * @return the <code>Person</code> that is the owner of the
	 *         <code>Software</code>. Returns null if no owner has been defined.
	 */
	public Person getPerson() {
		return this.person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	/**
	 * These methods get and set the <code>Collection</code> of
	 * <code>Resource</code>s that are associated with the <code>Software</code>
	 * 
	 * @hibernate.set table="SoftwareAssocResource" cascade="none" lazy="true"
	 * @hibernate.collection-key column="SoftwareID_FK"
	 * @hibernate.collection-many-to-many column="ResourceID_FK"
	 *                                    class="moos.ssds.metadata.Resource"
	 * @return the <code>Collection</code> of <code>Resource</code>s that are
	 *         associated with the <code>Software</code>
	 */
	public Collection getResources() {
		return resources;
	}

	public void setResources(Collection resources) {
		this.resources = resources;
	}

	/**
	 * This method add the given <code>Resource</code> to the collection
	 * associated with the <code>Software</code>.
	 * 
	 * @param resource
	 *            the <code>Resource</code> to add
	 */
	public void addResource(Resource resource) {
		// If null was passed in, just return
		if (resource == null)
			return;

		// Make sure the collection is there
		if (this.resources == null)
			this.resources = new HashSet();

		// Now add the Resource to the collection
		if (!this.resources.contains(resource)) {
			this.resources.add(resource);
		}
	}

	/**
	 * This method removes the given <code>Resource</code> from the collection
	 * 
	 * @param resource
	 *            is the <code>Resource</code> to remove from the collection
	 */
	public void removeResource(Resource resource) {
		if (resource == null)
			return;
		if ((this.resources != null) && (this.resources.contains(resource))) {
			this.resources.remove(resource);
		}
	}

	/**
	 * This method will clear out the collection of <code>Resource</code>s and
	 * keep the integrity of the relationships intact.
	 */
	public void clearResources() {
		this.resources.clear();
	}

	/**
	 * These methods get and set the version that Hibernate uses to check for
	 * dirty objects
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
	 * @see IMetadataObject#setValuesFromStringRepresentation
	 */
	public String toStringRepresentation(String delimiter) {
		// If the delimiter is not specified, use a default one
		if (delimiter == null)
			delimiter = IMetadataObject.DEFAULT_DELIMITER;

		StringBuffer sb = new StringBuffer();
		sb.append("Software");
		sb.append(delimiter + "id=" + this.getId());
		sb.append(delimiter + "name=" + this.getName());
		sb.append(delimiter + "description=" + this.getDescription());
		sb.append(delimiter + "uriString=" + this.getUriString());
		sb.append(delimiter + "softwareVersion=" + this.getSoftwareVersion());
		return sb.toString();
	}

	/**
	 * @see IMetadataObject#setValuesFromStringRepresentation(String, String)
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
			} else if (key.equalsIgnoreCase("description")) {
				this.setDescription(value);
			} else if (key.equalsIgnoreCase("uriString")) {
				this.setUriString(value);
			} else if (key.equalsIgnoreCase("uri")) {
				this.setUri(URI.create(value));
			} else if (key.equalsIgnoreCase("url")) {
				URL urlToSet = null;
				try {
					urlToSet = new URL(value);
				} catch (MalformedURLException e) {
					throw new MetadataException("The string " + value
							+ " that was specified as the URL, could not be "
							+ "converted to a URL: " + e.getMessage());
				}
				this.setUrl(urlToSet);
			} else if (key.equalsIgnoreCase("softwareVersion")) {
				this.setSoftwareVersion(value);
			} else {
				throw new MetadataException("The attribute specified by " + key
						+ " is not a recognized field of "
						+ this.getClass().getName());
			}
		}
	}

	/**
	 * This method overrides the default equals method and checks for to see if
	 * the objects occupy the same memory space and if not, then it checks for
	 * identical persistent identifiers and if those are not available, it
	 * checks for equality of the business key which is the name and version
	 * 
	 * @see IMetadataObject#equals(Object)
	 */
	public boolean equals(Object obj) {
		// First check to see if input is null
		if (obj == null)
			return false;

		// Now check JVM identity
		if (this == obj)
			return true;

		// Now check if it is the correct class
		if (!(obj instanceof Software))
			return false;

		// Cast to Software object
		final Software that = (Software) obj;

		// Now check for missing business key (name and software version)
		if ((this.name == null) || (that.getName() == null)
				|| (this.softwareVersion == null)
				|| (that.getSoftwareVersion() == null))
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
		// Calculate the hashcode
		int result = 14;
		if (name != null) {
			result = 7 * result + name.hashCode();
		}
		if (softwareVersion != null) {
			result = 91 * result + softwareVersion.hashCode();
		}
		return result;
	}

	/**
	 * This is the method to re-consitutute and object from a custom
	 * serialization form
	 * 
	 * @see Externalizable#readExternal(ObjectInput)
	 */
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		id = (Long) in.readObject();
		name = (String) in.readObject();
		description = (String) in.readObject();
		uriString = (String) in.readObject();
		softwareVersion = (String) in.readObject();
	}

	/**
	 * This is the method to serialize a Software to a custom serialized form
	 * 
	 * @see Externalizable#writeExternal(ObjectOutput)
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(id);
		out.writeObject(name);
		out.writeObject(description);
		out.writeObject(uriString);
		out.writeObject(softwareVersion);
	}

	/**
	 * This method overrides the clone method to produce a copy of an object. It
	 * will clear the ID on the newly created copy to prevent odd behavior.
	 */
	protected Object clone() throws CloneNotSupportedException {
		// Create the clone
		Software clone = new Software();

		// Set the fields
		try {
			clone.setId(null);
			clone.setName(this.getName());
			clone.setDescription(this.getDescription());
			clone.setUriString(this.getUriString());
			clone.setSoftwareVersion(this.getSoftwareVersion());
		} catch (MetadataException e) {
		}

		// Return the clone
		return clone;
	}

	/**
	 * This returns a clone with deep copies of <code>Person</code> and
	 * <code>Resources</code> filled out
	 */
	public IMetadataObject deepCopy() throws CloneNotSupportedException {
		logger.debug("deepCopy called");
		// Grab the clone
		Software deepClone = (Software) this.clone();
		logger.debug("The following clone was created:");
		logger.debug(deepClone.toStringRepresentation("|"));

		// Set the relationships
		if (this.getPerson() != null) {
			Person clonedPerson = (Person) this.getPerson().deepCopy();
			logger.debug("The following cloned Person will be added:");
			logger.debug(clonedPerson.toStringRepresentation("|"));
			deepClone.setPerson(clonedPerson);
		} else {
			deepClone.setPerson(null);
		}
		if ((this.getResources() != null) && (this.getResources().size() > 0)) {
			logger.debug("There are " + this.getResources().size()
					+ " Resources that will be cloned.");
			Collection resourcesToCopy = this.getResources();
			Iterator resourceIter = resourcesToCopy.iterator();
			while (resourceIter.hasNext()) {
				Resource clonedResource = (Resource) ((Resource) resourceIter
						.next()).deepCopy();
				logger.debug("The following cloned Resource will be added:");
				logger.debug(clonedResource.toStringRepresentation("|"));
				deepClone.addResource(clonedResource);
			}
		}
		// Now return the deep copy
		return deepClone;
	}

	/**
	 * This is the version that we can control for serialization purposes
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * This is the persistence layer identifier
	 */
	private Long id;

	/**
	 * This is the arbitrary name given to the DataContainer
	 */
	private String name;

	/**
	 * The description of the DataContainer
	 */
	private String description;

	/**
	 * This is a <code>URI</code> that is the resource identifier
	 */
	private String uriString;

	/**
	 * This is the version of software
	 */
	private String softwareVersion;

	/**
	 * This is the hibernate version that is used to check for dirty objects
	 */
	private long version = -1;

	/**
	 * This is the <code>Person</code> that is normally thought of as the owner
	 * of the software
	 * 
	 * @directed true
	 * @label lazy
	 */
	private Person person;

	/**
	 * This is a <code>Collection</code> of <code>Resource</code>s that are
	 * asscociated with the <code>Software</code>
	 * 
	 * @associates Resource
	 * @directed true
	 * @label lazy
	 */
	private Collection resources = new HashSet();

	/**
	 * This is a Log4JLogger that is used to log information to
	 */
	static Logger logger = Logger.getLogger(Software.class);
}