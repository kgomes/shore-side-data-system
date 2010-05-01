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
import moos.ssds.util.XmlDateFormat;

/**
 * A class for maintaining references to just about any type of file. Most
 * likely to be used for referencing configuration files and technical
 * documents. But could be used to reference executables, jars, etc.
 * <hr>
 * 
 * @stereotype thing
 * @hibernate.class table="Resource"
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.20 $
 */
public class Resource implements IMetadataObject, IDescription, IDateRange {

	/**
	 * A default constructor
	 */
	public Resource() {
	}

	/**
	 * This is the constructor that takes in a <code>URL</code> and sets the
	 * local URI string to the URL
	 * 
	 * @param url
	 *            the <code>URL</code> to set as the uniform identifier for this
	 *            resource
	 */
	public Resource(URL url) throws MetadataException {
		this.setUrl(url);
	}

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
	 */
	public String getName() {
		return name;
	}

	public void setName(String name) throws MetadataException {
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
	 * @see IDateRange#getStartDate()
	 * @hibernate.property
	 */
	public Date getStartDate() {
		return startDate;
	}

	/**
	 * @see IDateRange#setStartDate(Date)
	 */
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	/**
	 * @see IDateRange#getEndDate()
	 * @hibernate.property
	 */
	public Date getEndDate() {
		return endDate;
	}

	/**
	 * @see IDateRange#setEndDate(Date)
	 */
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	/**
	 * @see IDateRange#getDateRange()
	 */
	public IDateRange getDateRange() {
		return dateRange;
	}

	/**
	 * These methods get and set a <code>String</code> that is the parseable
	 * form of a URI refernce as defined by RFC 2396: Uniform Rsource
	 * Identifiers (URI): Generic Syntax. <b>It must be specified and must be
	 * unique before the object can be persisted in SSDS</b>.
	 * 
	 * @hibernate.property length="2048"
	 * @hibernate.column name="uriString" unique="true" not-null="true"
	 *                   index="uriString_index"
	 * @return a <code>String</code> that is the ASCII representation of a URI
	 *         Reference
	 */
	public String getUriString() {
		return uriString;
	}

	public void setUriString(String uriString) throws MetadataException {
		MetadataValidator.isObjectNull(uriString);
		MetadataValidator.isStringShorterThan(uriString,
				MetadataValidator.URI_STRING_LENGTH);
		this.uriString = uriString;
	}

	/**
	 * This method returns a <code>URI</code> that is the object form of the URI
	 * reference specified by the uriString.
	 * 
	 * @return a <code>URI</code> reference that is a unique resource locator
	 *         for the <code>Resource</code>
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
	 *            for the <code>Resource</code>
	 */
	public void setUri(URI uri) throws MetadataException {
		this.setUriString(uri.toASCIIString());
	}

	/**
	 * This method returns a <code>URL</code> that is created from the
	 * <code>URI</code> that was specified for this <code>Resource</code>.
	 * 
	 * @return a <code>URL</code> which is simply a <code>URL</code>
	 *         representation of the <code>URI</code> specified for the
	 *         <code>Resource</code>
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
	 * <code>Resource</code>
	 * 
	 * @param url
	 *            is the <code>URL</code> to use as the unique resource locator
	 *            for this <code>Resource</code>
	 */
	public void setUrl(URL url) throws MetadataException {
		this.setUriString(url.toExternalForm());
	}

	/**
	 * This method indicates if this resource is available over HTTP or FTP
	 * 
	 * @return a <code>boolean</code> that indicates if it is (<code>true</code>
	 *         ) available over HTTP/FTP or not
	 */
	public boolean isWebAccessible() {
		if (this.getUrl() == null)
			return false;
		boolean webAccessible = false;
		String protocol = this.getUrl().getProtocol();
		if ((protocol.equals("http")) || (protocol.equals("ftp"))) {
			webAccessible = true;
		}
		return webAccessible;
	}

	/**
	 * TODO kgomes Document this
	 * 
	 * @hibernate.property
	 */
	public Long getContentLength() {
		return contentLength;
	}

	public void setContentLength(Long contentLength) {
		this.contentLength = contentLength;
	}

	/**
	 * TODO kgomes Document this
	 * 
	 * @hibernate.property
	 */
	public String getMimeType() {
		return this.mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	/**
	 * These methods get and set the <code>Person</code> that is usually seen as
	 * the owner of the <code>Device</code> (or point of contact).
	 * 
	 * @hibernate.many-to-one class="moos.ssds.metadata.Person"
	 *                        column="PersonID_FK"
	 *                        foreign-key="Resource_Owned_By_Person"
	 *                        cascade="none" lazy="true"
	 * @return the <code>Person</code> that is the owner of the
	 *         <code>Device</code>. Returns null if no owner has been defined.
	 */
	public Person getPerson() {
		return this.person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	/**
	 * These methods get and set the <code>ResourceType</code> that is
	 * associated with a <code>Resource</code>.
	 * 
	 * @hibernate.many-to-one class="moos.ssds.metadata.ResourceType"
	 *                        column="ResourceTypeID_FK"
	 *                        foreign-key="Resource_Is_Of_ResourceType"
	 *                        cascade="none" lazy="false" outer-join="true"
	 * @return the <code>ResourceType</code> that is associated with this
	 *         device. Returns null if no type has been defined.
	 */
	public ResourceType getResourceType() {
		return this.resourceType;
	}

	public void setResourceType(ResourceType resourceType) {
		this.resourceType = resourceType;
	}

	/**
	 * These methods get and set the <code>ResourceBLOB</code> that is
	 * associated with a <code>Resource</code>.
	 * 
	 * @hibernate.many-to-one class="moos.ssds.metadata.ResourceBLOB"
	 *                        column="ResourceBLOBID_FK"
	 *                        foreign-key="Resource_Has_ResourceBLOB"
	 *                        cascade="all" lazy="true"
	 * @return the <code>ResourceBLOB</code> that is associated with this
	 *         <code>Resource</code>. Returns null if none has been defined.
	 */
	public ResourceBLOB getResourceBLOB() {
		return resourceBLOB;
	}

	public void setResourceBLOB(ResourceBLOB resourceBLOB) {
		this.resourceBLOB = resourceBLOB;
	}

	/**
	 * These methods get and set the collection of keywords associated with the
	 * <code>DataContainer</code>. TODO kgomes 20051102 I should be able to
	 * define the two columns as primary keys, but they might violate the bag
	 * concept, so maybe not
	 * 
	 * @hibernate.set table="ResourceAssocKeyword" cascade="none" lazy="true"
	 * @hibernate.collection-key column="ResourceID_FK"
	 * @hibernate.collection-many-to-many column="KeywordID_FK"
	 *                                    class="moos.ssds.metadata.Keyword"
	 * @return the collection of <code>Keyword</code> objects.
	 */
	public Collection getKeywords() {
		return keywords;
	}

	protected void setKeywords(Collection keywords) {
		this.keywords = keywords;
	}

	/**
	 * This method adds the given <code>Keyword</code> to the collection
	 * associated with the <code>DataContainer</code>.
	 * 
	 * @param keyword
	 *            the <code>Keyword</code> to add
	 */
	public void addKeyword(Keyword keyword) {
		// If null was passed in, just return
		if (keyword == null)
			return;

		// Now add the Keyword to the collection
		if (!this.keywords.contains(keyword)) {
			this.keywords.add(keyword);
		}
	}

	/**
	 * This method removes the given <code>Keyword</code> from the collection
	 * 
	 * @param keyword
	 *            is the <code>Keyword</code> to remove from the collection
	 */
	public void removeKeyword(Keyword keyword) {
		if (keyword == null)
			return;
		if ((this.keywords != null) && (this.keywords.contains(keyword))) {
			this.keywords.remove(keyword);
		}
	}

	/**
	 * This method will clear out the collection of <code>Keyword</code>s and
	 * keep the integrity of the relationships intact.
	 */
	public void clearKeywords() {
		this.keywords.clear();
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
	 * @see IMetadataObject#toStringRepresentation(String)
	 */
	public String toStringRepresentation(String delimiter) {
		// If the delimiter is not specified, use a default one
		if (delimiter == null)
			delimiter = IMetadataObject.DEFAULT_DELIMITER;

		StringBuffer sb = new StringBuffer();
		sb.append("Resource");
		sb.append(delimiter + "id=" + this.getId());
		sb.append(delimiter + "name=" + this.getName());
		sb.append(delimiter + "description=" + this.getDescription());
		sb.append(delimiter + "startDate=" + this.getStartDate());
		sb.append(delimiter + "endDate=" + this.getEndDate());
		sb.append(delimiter + "uriString=" + this.getUriString());
		sb.append(delimiter + "contentLength=" + this.getContentLength());
		sb.append(delimiter + "mimeType=" + this.getMimeType());
		return sb.toString();
	}

	/**
	 * @see IMetadataObject#setValuesFromStringRepresentation
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
			} else if (key.equalsIgnoreCase("startDate")) {
				this.setStartDate(xmlDateFormat.parse(value));
			} else if (key.equalsIgnoreCase("endDate")) {
				this.setEndDate(xmlDateFormat.parse(value));
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
			} else if (key.equalsIgnoreCase("contentLength")) {
				try {
					this.setContentLength(Long.valueOf(value));
				} catch (NumberFormatException e) {
					throw new MetadataException(
							"The value specified for contentLength (" + value
									+ ") could not be converted to a Long: "
									+ e.getMessage());
				}
			} else if (key.equalsIgnoreCase("mimeType")) {
				this.setMimeType(value);
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
	 * checks for equality of the business key which is the URI (<b>NOTE: This
	 * IS a case senstive comparison</b>)
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
		if (!(obj instanceof Resource))
			return false;

		// Cast to Resource object
		final Resource that = (Resource) obj;

		// Now check for missing business key (URI)
		if ((this.uriString == null) || (that.getUriString() == null))
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
	 * @see IMetadataObject#hashCode()
	 */
	public int hashCode() {
		// Calculate the hashcode
		int result = 53;
		if (uriString != null) {
			result = 4 * result + uriString.hashCode();
		}

		// Now return it
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
		startDate = (Date) in.readObject();
		endDate = (Date) in.readObject();
		uriString = (String) in.readObject();
		contentLength = (Long) in.readObject();
		mimeType = (String) in.readObject();
	}

	/**
	 * This is the method to serialize a Resource to a custom serialized form
	 * 
	 * @see Externalizable#writeExternal(ObjectOutput)
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(id);
		out.writeObject(name);
		out.writeObject(description);
		out.writeObject(startDate);
		out.writeObject(endDate);
		out.writeObject(uriString);
		out.writeObject(contentLength);
		out.writeObject(mimeType);
	}

	/**
	 * This method overrides the clone method to produce a copy of an object. It
	 * will clear the ID on the newly created copy to prevent odd behavior.
	 */
	protected Object clone() throws CloneNotSupportedException {
		// Create the clone
		Resource clone = new Resource();

		// Set the fields
		try {
			clone.setId(null);
			clone.setName(this.getName());
			clone.setDescription(this.getDescription());
			clone.setStartDate(this.getStartDate());
			clone.setEndDate(this.getEndDate());
			clone.setUriString(this.getUriString());
			clone.setContentLength(this.getContentLength());
			clone.setMimeType(this.getMimeType());
		} catch (MetadataException e) {
		}

		// Return the clone
		return clone;
	}

	/**
	 * This method returns a clone with deep copies of the <code>Person</code>,
	 * <code>ResourceType</code>, <code>ResourceBLOB</code>,
	 * <code>Keywords</code> filled out
	 */
	public IMetadataObject deepCopy() throws CloneNotSupportedException {
		logger.debug("deepCopy called.");

		// Grab the clone
		Resource deepClone = (Resource) this.clone();
		logger.debug("Cloned resource is:");
		logger.debug(deepClone.toStringRepresentation("|"));

		// Set the relationships
		if (this.getPerson() != null) {
			deepClone.setPerson((Person) this.getPerson().deepCopy());
			logger.debug("Setting the person on the cloned resource to:");
			logger.debug(deepClone.getPerson().toStringRepresentation("|"));
		} else {
			deepClone.setPerson(null);
		}
		if (this.getResourceType() != null) {
			ResourceType clonedResourceType = (ResourceType) this
					.getResourceType().deepCopy();
			logger
					.debug("Setting the resource type on the cloned resource to:");
			logger.debug(clonedResourceType.toStringRepresentation("|"));
			deepClone.setResourceType(clonedResourceType);
		}
		if (this.getResourceBLOB() != null) {
			ResourceBLOB clonedResourceBLOB = (ResourceBLOB) this
					.getResourceBLOB().deepCopy();
			logger
					.debug("Setting the resource blob on the cloned resource to:");
			logger.debug(clonedResourceBLOB.toStringRepresentation("|"));
			deepClone.setResourceBLOB(clonedResourceBLOB);
		}
		if ((this.getKeywords() != null) && (this.getKeywords().size() > 0)) {
			logger.debug("There are " + this.getKeywords().size()
					+ " that need cloning and attaching");
			Collection keywordsToCopy = this.getKeywords();
			Iterator keywordIter = keywordsToCopy.iterator();
			while (keywordIter.hasNext()) {
				Keyword clonedKeyword = (Keyword) ((Keyword) keywordIter.next())
						.deepCopy();
				logger
						.debug("Adding this cloned keyword to the cloned resource type:");
				logger.debug(clonedKeyword.toStringRepresentation("|"));
				deepClone.addKeyword(clonedKeyword);
			}
		}

		// Return the deep clone
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
	 * This is the <code>Date</code> that the earliest data in this
	 * <code>DataContainer</code> applies to
	 */
	private Date startDate;

	/**
	 * This is the <code>Date</code> that the latest data in this
	 * <code>DataContainer</code> applies to
	 */
	private Date endDate;

	/**
	 * This is an attribute that give the range of time that this
	 * <code>DataContainer</code> covers
	 */
	private IDateRange dateRange = new DateRange(this);

	/**
	 * This is a <code>URI</code> that is the resource identifier
	 */
	private String uriString;

	/**
	 * This is the length of the <code>DataContainer</code> in bytes
	 */
	private Long contentLength;

	/**
	 * This is the MIME type that applies to this <code>DataContainer</code> (if
	 * one applies).
	 */
	private String mimeType;

	/**
	 * This is the hibernate version that is used to check for dirty objects
	 */
	private long version = -1;

	/**
	 * This is the <code>Person</code> that is normally thought of as the owner
	 * of the resource
	 * 
	 * @directed true
	 * @label lazy
	 */
	private Person person;

	/**
	 * This is the <code>ResourceType</code> that this <code>Resource</code> is
	 * categorized as
	 * 
	 * @directed true
	 * @label unlazy
	 */
	private ResourceType resourceType;

	/**
	 * This is a <code>Collection</code> of <code>ResourceBLOB</code>s that can
	 * be linked to the <code>Resource</code>
	 * 
	 * @directed true
	 * @label lazy
	 */
	private ResourceBLOB resourceBLOB;

	/**
	 * This is a collection of <code>Keyword</code> objects that can be used to
	 * search for <code>DataContainer</code>s.
	 * 
	 * @associates Keyword
	 * @directed true
	 * @label lazy
	 */
	private Collection keywords = new HashSet();

	/**
	 * This is a date formatting utility
	 */
	private XmlDateFormat xmlDateFormat = new XmlDateFormat();

	/**
	 * This is a Log4JLogger that is used to log information to
	 */
	static Logger logger = Logger.getLogger(Resource.class);
}