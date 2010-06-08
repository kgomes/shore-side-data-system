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

import moos.ssds.metadata.util.MetadataException;
import moos.ssds.metadata.util.MetadataValidator;
import moos.ssds.util.XmlDateFormat;

import org.apache.log4j.Logger;

/**
 * This class represents a container of data in the system.
 * <hr>
 * 
 * @stereotype thing
 * @hibernate.class table="DataContainer"
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.35 $
 */
public class DataContainer implements IMetadataObject, IDescription,
		IResourceOwner, IDateRange {

	/**
	 * This is a Log4JLogger that is used to log information to
	 */
	static Logger logger = Logger.getLogger(DataContainer.class);

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
	 * This is a string that categories the type of data container. It should
	 * match one of the constants defined in this class.
	 */
	private String dataContainerType = DataContainer.TYPE_FILE;

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
	 * Flag to indicate if this instance was an 'original' dataset.
	 */
	private Boolean original = new Boolean(false);

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
	 * This is the number of records that are stored in the
	 * <code>DataContainer</code>.
	 */
	private Long numberOfRecords;

	/**
	 * This is a <code>boolean</code> that indicates whether or not the
	 * <code>DataContainer</code> is available through a DODS server (via the
	 * URI supplied)
	 */
	private Boolean dodsAccessible = new Boolean(false);

	/**
	 * This is a <code>String</code> that represents the <code>URL</code> where
	 * the <code>DataContainer</code> can be read from an DODS (OPeNDAP) server.
	 */
	private String dodsUrlString;

	/**
	 * This is boolean to indicate whether or not the data system should try to
	 * keep a parallel NetCDF file that contains the same data that is in the
	 * container described here
	 */
	private Boolean noNetCDF = new Boolean(true);

	/**
	 * These six fields are the geospatial extents that are covered by the data
	 * in the <code>DataContainer</code> (if applicable
	 */
	private Double minLatitude;
	private Double maxLatitude;
	private Double minLongitude;
	private Double maxLongitude;
	private Float minDepth;
	private Float maxDepth;

	/**
	 * This is the <code>Person</code> that is normally thought of as the owner
	 * of the <code>DataContainer</code> (or the point of contact)
	 */
	private Person person;

	/**
	 * This is the <code>HeaderDescription</code> that is associated with the
	 * <code>DataContainer</code>. It is meant to describe any header section
	 * that is contained in the <code>DataContainer</code>
	 */
	private HeaderDescription headerDescription;

	/**
	 * This is the <code>RecordDescription</code> that is associated with the
	 * <code>DataContainer</code>. It is meant to describe the records in the
	 * <code>DataContainer</code> so that it can be parsed automatically
	 */
	private RecordDescription recordDescription;

	/**
	 * This is a collection of <code>DataContainerGroup</code>s that are
	 * associated with the <code>DataContainer</code>
	 */
	private Collection<DataContainerGroup> dataContainerGroups = new HashSet<DataContainerGroup>();

	/**
	 * This is a collection of <code>Keyword</code> objects that can be used to
	 * search for <code>DataContainer</code>s.
	 */
	private Collection<Keyword> keywords = new HashSet<Keyword>();

	/**
	 * This is the <code>Collection</code> of <code>Resource</code>s that are
	 * associated with the <code>DataContainer</code>
	 */
	private Collection<Resource> resources = new HashSet<Resource>();

	/**
	 * This is the <code>DataProducer</code> that created the data in this
	 * container.
	 */
	private DataProducer creator;

	/**
	 * A collection of <code>DataProducer</code>s that use this
	 * <code>DataContainer<code> to create other <code>DataContainer</code>s.
	 */
	private Collection<DataProducer> consumers = new HashSet<DataProducer>();

	/**
	 * A formatter for converting dates to and from XML format
	 */
	private transient XmlDateFormat xmlDateFormat = new XmlDateFormat();

	/**
	 * These are the "types" of DataContainers that exist.
	 */
	public static final String TYPE_FILE = "File";
	public static final String TYPE_STREAM = "Stream";

	/**
	 * This is the Hibernate version that is used to check for dirty objects
	 */
	private long version = -1;

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
	 * This is the name of the <code>DataContainer</code> and it must be 50
	 * characters or less.
	 * 
	 * @see IDescription#getName()
	 * @hibernate.property
	 * @hibernate.column name="name" length="2048"
	 */
	public String getName() {
		return name;
	}

	public void setName(String name) throws MetadataException {
		MetadataValidator.isStringShorterThan(name, 2048);
		this.name = name;
	}

	/**
	 * @see IDescription#getDescription()
	 * @hibernate.property length="2048"
	 */
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) throws MetadataException {
		MetadataValidator.isStringShorterThan(description,
				MetadataValidator.DESCRIPTION_LENGTH);
		this.description = description;
	}

	/**
	 * These methods get and set the &quot;type&quot; of
	 * <code>DataContainer</code> and this string must match one of the
	 * <code>TYPE_<i>XXXXX</i></code> constants defined in this class.
	 * 
	 * @hibernate.property
	 * @return a <code>String</code> that is the &quot;type&quot; of
	 *         <code>DataContainer</code>. This should match one of the
	 *         constants defined in this class.
	 */
	public String getDataContainerType() {
		return dataContainerType;
	}

	public void setDataContainerType(String dataContainerType)
			throws MetadataException {
		if (!isValidDataContainerType(dataContainerType)) {
			throw new MetadataException("The type passed in "
					+ dataContainerType
					+ " does not match one of the constants "
					+ "defined for DataContainer");
		}
		this.dataContainerType = dataContainerType;
	}

	/**
	 * These methods get and set the earliest date (timestamp) that the data in
	 * this <code>DataContainer</code> covers
	 * 
	 * @see IDateRange#getStartDate()
	 * @hibernate.property
	 * @return a <code>Date</code> that is the earliest timestamp of the data
	 *         that is covered by this <code>DataContainer</code>
	 */
	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	/**
	 * These methods get and set the latest date (timestamp) that the data in
	 * this <code>DataContainer</code> covers
	 * 
	 * @see IDateRange#getEndDate()
	 * @hibernate.property
	 * @return a <code>Date</code> that is the latest timestamp of the data that
	 *         is covered by this <code>DataContainer</code>
	 */
	public Date getEndDate() {
		return endDate;
	}

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
	 * These methods get and set a <code>boolean</code> that indicates if the
	 * <code>DataContainer</code> is considered original or not. This normally
	 * means that it was created by some original process that did not have any
	 * data input.
	 * 
	 * @hibernate.property
	 * @return a <code>boolean</code> that indicates if the
	 *         <code>DataContainer</code> is considered original (
	 *         <code>true</code>) or not (<code>false</code>)
	 */
	public Boolean isOriginal() {
		return original;
	}

	public void setOriginal(Boolean original) {
		this.original = original;
	}

	/**
	 * These methods get and set a <code>String</code> that is the parseable
	 * form of a URI reference as defined by RFC 2396: Uniform Resource
	 * Identifiers (URI): Generic Syntax. <b>It must be specified and must be
	 * unique before the object can be persisted in SSDS</b>. The length of the
	 * URI string must be less than 2048 characters.
	 * 
	 * @hibernate.property length="2048"
	 * @hibernate.column name="uriString" unique="true" not-null="true"
	 *                   index="uriString_index"
	 * @return a <code>String</code> that is the ASCII representation of a URI
	 *         Reference
	 * @throws MetadataException
	 *             if something is not right with the URI string
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
	 * These methods get and set a <code>URI</code> that is the object form of
	 * the URI reference specified by the uriString.
	 * 
	 * @return a <code>URI</code> reference that is a unique resource locator
	 *         for the <code>DataContainer</code>
	 */
	public URI getUri() {
		URI uriToReturn = null;
		uriToReturn = URI.create(this.uriString);
		return uriToReturn;
	}

	public void setUri(URI uri) throws MetadataException {
		this.setUriString(uri.toASCIIString());
	}

	/**
	 * These methods get and set a <code>URL</code> that is created from the
	 * <code>URI</code> that was specified for this <code>DataContainer</code>.
	 * 
	 * @return a <code>URL</code> which is simply a <code>URL</code>
	 *         representation of the <code>URI</code> specified for the
	 *         <code>DataContainer</code>. If the URIString cannot be converted
	 *         to a URL (or the URIString is null), the return will be null
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

	public void setUrl(URL url) throws MetadataException {
		this.setUriString(url.toExternalForm());
	}

	/**
	 * These methods get and set the overall length of the
	 * <code>DataContainer</code> in bytes.
	 * 
	 * @hibernate.property
	 * @return a <code>Long</code> that is length of the
	 *         <code>DataContainer</code> in bytes.
	 */
	public Long getContentLength() {
		return contentLength;
	}

	public void setContentLength(Long contentLength) {
		this.contentLength = contentLength;
	}

	/**
	 * These methods get and set a <code>String</code> that helps applications
	 * associate the <code>DataContainer</code> to an application.
	 * 
	 * @hibernate.property
	 * @return a <code>String</code> that is the MIME type for the
	 *         <code>DataConatiner</code>.
	 */
	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	/**
	 * These methods get and set a <code>Long</code> that is meant to reflect
	 * the number of data records that are contained in the
	 * <code>DataContainer</code>.
	 * 
	 * @hibernate.property
	 * @return a <code>Long</code> that is the number of records contained in
	 *         the <code>DataContainer</code>
	 */
	public Long getNumberOfRecords() {
		return numberOfRecords;
	}

	public void setNumberOfRecords(Long numberOfRecords) {
		this.numberOfRecords = numberOfRecords;
	}

	/**
	 * These methods get and set a <code>boolean</code> that indicates whether
	 * or not the <code>DataContainer</code> is available through a DODS
	 * (OPeNDAP) server
	 * 
	 * @hibernate.property
	 * @return a <code>boolean</code> that indicates if the
	 *         <code>DataContainer</code> is available (<code>true</code>)
	 *         through a DODS (OPeNDAP) server or not (<code>false</code>).
	 */
	public Boolean isDodsAccessible() {
		return dodsAccessible;
	}

	public void setDodsAccessible(Boolean dodsAccessible) {
		this.dodsAccessible = dodsAccessible;
	}

	/**
	 * These methods get and set a <code>String</code> that is the string
	 * representation of a URL where the <code>DataContainer</code> can be read
	 * through an DODS (OPeNDAP) server
	 * 
	 * @hibernate.property length="2048"
	 * @return a <code>String</code> a URL that can be used to read the
	 *         <code>DataContainer</code> through a DODS (OPeNDAP) server
	 * @throws MetadataException
	 *             if the supplied URL string is longer than 2048
	 */
	public String getDodsUrlString() {
		return this.dodsUrlString;
	}

	public void setDodsUrlString(String dodsUrlString) throws MetadataException {
		MetadataValidator.isStringShorterThan(dodsUrlString,
				MetadataValidator.URI_STRING_LENGTH);

		this.dodsUrlString = dodsUrlString;
	}

	/**
	 * These methods get and set a <code>URL</code> where
	 * <code>DataContainer</code> can be read from a DODS (OPeNDAP) server (if
	 * applicable)
	 * 
	 * @return a <code>URL</code> where the <code>DataContainer</code> can be
	 *         read through a DODS (OPeNDAP) server
	 */
	public URL getDodsUrl() {

		// First convert the stored string to a URI
		if (dodsUrlString != null) {
			URI uri = null;
			uri = URI.create(this.dodsUrlString);
			URL urlToReturn = null;
			try {
				urlToReturn = uri.toURL();
			} catch (MalformedURLException e) {
				urlToReturn = null;
			}
			return urlToReturn;
		} else {
			return null;
		}

	}

	public void setDodsUrl(URL url) throws MetadataException {
		this.setDodsUrlString(url.toExternalForm());
	}

	/**
	 * These methods get and set a flag that is a check to see if the
	 * <code>DataContainer</code> has been flagged NOT to have a parallel NetCDF
	 * file created by the data system
	 * 
	 * @hibernate.property
	 * @return a <code>boolean</code> that indicates if the data systems is
	 *         supposed to try and keep (<code>true</code>) a parallel NetCDF
	 *         for the data in this <code>DataContainer</code>
	 */
	public Boolean isNoNetCDF() {
		return noNetCDF;
	}

	public void setNoNetCDF(Boolean noNetCDF) {
		this.noNetCDF = noNetCDF;
	}

	/**
	 * These methods get and set the <code>Double</code> that is the minimum
	 * latitude of the data that is contained in the <code>DataContainer</code>.
	 * The range for latitudes is -90 to 90.
	 * 
	 * @hibernate.property
	 * @return a <code>Double</code> that is the minimum latitude. If it has not
	 *         been defined, null is returned
	 * @throws MetadataException
	 *             if the supplied latitude is not between -90 and 90
	 */
	public Double getMinLatitude() {
		return minLatitude;
	}

	public void setMinLatitude(Double minLatitude) throws MetadataException {
		MetadataValidator.isValueBetween(minLatitude,
				MetadataValidator.MIN_LATITUDE, MetadataValidator.MAX_LATITUDE);

		this.minLatitude = minLatitude;
	}

	/**
	 * These methods get and set the <code>Double</code> that is the maximum
	 * latitude of the data that is contained in the <code>DataContainer</code>.
	 * 
	 * @hibernate.property
	 * @return a <code>Double</code> that is the maximum latitude. If it has not
	 *         been defined, null is returned
	 * @throws MetadataException
	 *             if the supplied latitude is not between -90 and 90
	 */
	public Double getMaxLatitude() {
		return maxLatitude;
	}

	public void setMaxLatitude(Double maxLatitude) throws MetadataException {
		MetadataValidator.isValueBetween(maxLatitude,
				MetadataValidator.MIN_LATITUDE, MetadataValidator.MAX_LATITUDE);
		this.maxLatitude = maxLatitude;
	}

	/**
	 * These methods get and set the <code>Double</code> that is the minimum
	 * longitude of the data that is contained in the <code>DataContainer</code>
	 * .
	 * 
	 * @hibernate.property
	 * @return a <code>Double</code> that is the minimum longitude. If it has
	 *         not been defined, null is returned
	 * @throws MetadataException
	 *             if the supplied longitude is not between -360 and 360
	 */
	public Double getMinLongitude() {
		return minLongitude;
	}

	public void setMinLongitude(Double minLongitude) throws MetadataException {
		MetadataValidator.isValueBetween(minLongitude,
				MetadataValidator.MIN_LONGITUDE,
				MetadataValidator.MAX_LONGITUDE);
		this.minLongitude = minLongitude;
	}

	/**
	 * These methods get and set the <code>Double</code> that is the maximum
	 * longitude of the data that is contained in the <code>DataContainer</code>
	 * .
	 * 
	 * @hibernate.property
	 * @return a <code>Double</code> that is the minimum latitude. If it has not
	 *         been defined, null is returned
	 * @throws MetadataException
	 *             if the supplied longitude is not between -360 and 360
	 */
	public Double getMaxLongitude() {
		return maxLongitude;
	}

	public void setMaxLongitude(Double maxLongitude) throws MetadataException {
		MetadataValidator.isValueBetween(maxLongitude,
				MetadataValidator.MIN_LONGITUDE,
				MetadataValidator.MAX_LONGITUDE);
		this.maxLongitude = maxLongitude;
	}

	/**
	 * These methods get and set the <code>Double</code> that is the minimum
	 * vertical coordinate of the data that is contained in the
	 * <code>DataContainer</code>. Depth is measure postive from the surface
	 * down. So 0 is the surface and +100 is 100 meters down.
	 * 
	 * @hibernate.property
	 * @return a <code>Float</code> that is the minimum vertical coordinate. If
	 *         it has not been defined, null is returned
	 * @throws MetadataException
	 *             if the supplied depth is negative.
	 */
	public Float getMinDepth() {
		return minDepth;
	}

	public void setMinDepth(Float minDepth) throws MetadataException {
		MetadataValidator.isValueGreaterThan(minDepth,
				MetadataValidator.DEPTH_MIN);
		this.minDepth = minDepth;
	}

	/**
	 * These methods get and set the <code>Double</code> that is the maximum
	 * vertical coordinate of the data that is contained in the
	 * <code>DataContainer</code>.
	 * 
	 * @hibernate.property
	 * @return a <code>Float</code> that is the maximum vertical coordinate. If
	 *         it has not been defined, null is returned
	 * @throws MetadataException
	 *             if the supplied depth is negative.
	 */
	public Float getMaxDepth() {
		return maxDepth;
	}

	public void setMaxDepth(Float maxDepth) throws MetadataException {
		MetadataValidator.isValueGreaterThan(maxDepth,
				MetadataValidator.DEPTH_MIN);
		this.maxDepth = maxDepth;
	}

	/**
	 * These methods get and set the <code>Person</code> that is usually seen as
	 * the owner of the <code>DataContainer</code> (or point of contact).
	 * 
	 * @hibernate.many-to-one class="moos.ssds.metadata.Person"
	 *                        column="PersonID_FK"
	 *                        foreign-key="DataContainer_Owned_By_Person"
	 *                        cascade="none" lazy="true"
	 * @return the <code>Person</code> that is the owner of the
	 *         <code>DataContainer</code>. Returns null if no owner has been
	 *         defined.
	 */
	public Person getPerson() {
		return this.person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	/**
	 * These methods get and set the <code>HeaderDescription</code> that is
	 * associated with a <code>DataContainer</code>.
	 * 
	 * @hibernate.many-to-one class="moos.ssds.metadata.HeaderDescription"
	 *                        column="HeaderDescriptionID_FK"
	 *                        foreign-key="DataContainer_Has_HeaderDescription"
	 *                        cascade="all" lazy="true"
	 * @return the <code>HeaderDescription</code> that is associated with this
	 *         <code>DataContainer</code>. Returns null if none has been
	 *         defined.
	 */
	public HeaderDescription getHeaderDescription() {
		return headerDescription;
	}

	public void setHeaderDescription(HeaderDescription headerDescription) {
		this.headerDescription = headerDescription;
	}

	/**
	 * These methods get and set the <code>RecordDescription</code> that
	 * describes the records that are contained in this
	 * <code>DataContainer</code>
	 * 
	 * @hibernate.many-to-one class="moos.ssds.metadata.RecordDescription"
	 *                        column="RecordDescriptionID_FK"
	 *                        foreign-key="DataContainer_Has_Records_Described_By_RecordDescription"
	 *                        cascade="all" lazy="true"
	 * @return the <code>RecordDescription</code> that describes all the records
	 *         in this <code>DataContainer</code>
	 */
	public RecordDescription getRecordDescription() {
		return recordDescription;
	}

	public void setRecordDescription(RecordDescription recordDescription) {
		this.recordDescription = recordDescription;
	}

	/**
	 * This method returns the <code>RecordVariable</code>s that are associated
	 * with the <code>DataContainer</code>. It simply grabs the
	 * <code>RecordDescription</code> and then grabs the
	 * <code>RecordVariable</code>s and returns those.
	 * 
	 * @return a <code>Collection</code> of <code>RecordVariable</code> (will
	 *         return an empty collection if there are none).
	 */
	public Collection<RecordVariable> getRecordVariables() {
		// Create and empty one so a null is not returned
		Collection<RecordVariable> recordVariables = new HashSet<RecordVariable>();
		// Now if the RD exists, get the RVs and return those
		if (this.recordDescription != null) {
			recordVariables = this.recordDescription.getRecordVariables();
		}
		return recordVariables;
	}

	/**
	 * This method will add a <code>RecordVariable</code> to the collection of
	 * <code>RecordVariable</code>s that are associated with a
	 * <code>DataContainer</code>. The actual relationship between
	 * <code>RecordVariable</code> and <code>DataContainer</code> goes through a
	 * <code>RecordDescription</code>. This is a convenience method to add
	 * <code>RecordVariable</code> directly to a <code>DataContainer</code> and
	 * it actually adds them to the <code>RecordDescription</code> that is
	 * associated with the <code>DataContainer</code>. If there is no
	 * <code>RecordDescription</code> associated with the
	 * <code>DataContainer</code>, a new one (blank) will be created.
	 * 
	 * @param recordVariable
	 *            is the <code>RecordVariable</code> to add to the collection of
	 *            <code>RecordVariable</code>s associated with the
	 *            <code>DataContainer</code> through the
	 *            <code>RecordDescription</code>.
	 */
	public void addRecordVariable(RecordVariable recordVariable) {
		// Check for an empty argument
		if (recordVariable != null) {

			// First check to see if there is a RecordDescription
			if (this.recordDescription == null) {
				this.recordDescription = new RecordDescription();
				try {
					this.recordDescription.setRecordType(new Long(0));
				} catch (MetadataException e) {
				}
			}

			// Now add it
			this.recordDescription.addRecordVariable(recordVariable);
		}
	}

	/**
	 * These methods get and set the <code>Collection</code> of
	 * <code>DataContainerGroup</code>s that are associated with the
	 * <code>DataContainer</code>
	 * 
	 * @hibernate.set table="DataContainerGroupAssocDataContainer"
	 *                cascade="none" lazy="true"
	 * @hibernate.collection-key column="DataContainerID_FK"
	 * @hibernate.collection-many-to-many column="DataContainerGroupID_FK"
	 *                                    class=
	 *                                    "moos.ssds.metadata.DataContainerGroup"
	 * @return the <code>Collection</code> of <code>DataContainerGroup</code>s
	 *         that have been associated to the <code>DataContainer</code>
	 */
	public Collection<DataContainerGroup> getDataContainerGroups() {
		return dataContainerGroups;
	}

	public void setDataContainerGroups(
			Collection<DataContainerGroup> dataContainerGroups) {
		this.dataContainerGroups = dataContainerGroups;
	}

	/**
	 * This method adds the given <code>DataContainerGroup</code> to the
	 * collection associated with the <code>DataContainer</code>.
	 * 
	 * @param dataContainerGroup
	 *            the <code>DataContainerGroup</code> to add
	 */
	public void addDataContainerGroup(DataContainerGroup dataContainerGroup) {
		// If null was passed in, just return
		if (dataContainerGroup == null)
			return;

		// Make sure collection exists
		if (this.dataContainerGroups == null)
			this.dataContainerGroups = new HashSet<DataContainerGroup>();

		// Now add the DataContainerGroup to the collection
		if (!this.dataContainerGroups.contains(dataContainerGroup)) {
			this.dataContainerGroups.add(dataContainerGroup);
		}
	}

	/**
	 * This method removes the given <code>DataContainerGroup</code> from the
	 * collection
	 * 
	 * @param dataContainerGroup
	 *            is the <code>DataContainerGroup</code> to remove from the
	 *            collection
	 */
	public void removeDataContainerGroup(DataContainerGroup dataContainerGroup) {
		if (dataContainerGroup == null)
			return;
		if ((this.dataContainerGroups != null)
				&& (this.dataContainerGroups.contains(dataContainerGroup))) {
			this.dataContainerGroups.remove(dataContainerGroup);
		}
	}

	/**
	 * This method will clear out the collection of
	 * <code>DataContainerGroup</code>s and keep the integrity of the
	 * relationships intact.
	 */
	public void clearDataContainerGroups() {
		this.dataContainerGroups.clear();
	}

	/**
	 * These methods get and set the collection of keywords associated with the
	 * <code>DataContainer</code>. TODO kgomes 20051102 I should be able to
	 * define the two columns as primary keys, but they might violate the bag
	 * concept, so maybe not
	 * 
	 * @hibernate.set table="DataContainerAssocKeyword" cascade="none"
	 *                lazy="true"
	 * @hibernate.collection-key column="DataContainerID_FK"
	 * @hibernate.collection-many-to-many column="KeywordID_FK"
	 *                                    class="moos.ssds.metadata.Keyword"
	 * @return the collection of <code>Keyword</code> objects.
	 */
	public Collection<Keyword> getKeywords() {
		return keywords;
	}

	public void setKeywords(Collection<Keyword> keywords) {
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

		// Make sure the collection exists
		if (this.keywords == null)
			this.keywords = new HashSet<Keyword>();

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
	 * These methods get and set the <code>Collection</code> of
	 * <code>Resource</code>s that are associated with the
	 * <code>DataContainer</code>
	 * 
	 * @hibernate.set table="DataContainerAssocResource" cascade="none"
	 *                lazy="true"
	 * @hibernate.collection-key column="DataContainerID_FK"
	 * @hibernate.collection-many-to-many column="ResourceID_FK"
	 *                                    class="moos.ssds.metadata.Resource"
	 * @return the <code>Collection</code> of <code>Resource</code>s that are
	 *         associated with the <code>DataContainer</code>
	 */
	public Collection<Resource> getResources() {
		return resources;
	}

	public void setResources(Collection<Resource> resources) {
		this.resources = resources;
	}

	/**
	 * This method adds the given <code>Resource</code> to the collection
	 * associated with the <code>DataContainer</code>.
	 * 
	 * @param resource
	 *            the <code>Resource</code> to add
	 */
	public void addResource(Resource resource) {
		// If null was passed in, just return
		if (resource == null)
			return;

		// Make sure collection exists
		if (this.resources == null)
			this.resources = new HashSet<Resource>();

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
	 * These methods get and set the <code>DataProducer</code> that is
	 * responsible for creating this <code>DataContainer</code>.
	 * 
	 * @hibernate.many-to-one class="moos.ssds.metadata.DataProducer"
	 *                        column="DataProducerID_FK"
	 *                        foreign-key="DataContainer_Was_Created_By_DataProducer"
	 *                        cascade="none" lazy="true" inverse="false"
	 *                        outer-join="true"
	 * @return the <code>DataProducer</code> which created the
	 *         <code>DataContainer</code>
	 */
	public DataProducer getCreator() {
		return creator;
	}

	public void setCreator(DataProducer creator) {
		this.creator = creator;
	}

	/**
	 * These methods get and set a <code>Collection</code> of
	 * <code>DataProducer</code>s that use this <code>DataContainer</code> as
	 * input to their processing
	 * 
	 * @hibernate.set table="DataProducerInput" cascade="none" inverse="true"
	 *                lazy="true"
	 * @hibernate.collection-key column="DataContainerID_FK"
	 * @hibernate.collection-many-to-many column="DataProducerID_FK"
	 *                                    class="moos.ssds.metadata.DataProducer"
	 * @return a <code>Collection</code> of <code>DataProducer</code>s that use
	 *         this <code>DataContainer</code> as input to their processing
	 */
	public Collection<DataProducer> getConsumers() {
		return consumers;
	}

	public void setConsumers(Collection<DataProducer> consumers) {
		this.consumers = consumers;
	}

	/**
	 * This method adds the given <code>DataProducer</code> to the collection
	 * associated with the <code>DataContainer</code>.
	 * 
	 * @param consumer
	 *            the <code>DataProducer</code> to add
	 */
	public void addConsumer(DataProducer consumer) {
		// If null was passed in, just return
		if (consumer == null)
			return;

		// Make sure the collection exists
		if (this.consumers == null)
			this.consumers = new HashSet<DataProducer>();

		// Set the reverse relationship
		if (!consumer.getInputs().contains(this)) {
			consumer.getInputs().add(this);
		}

		// Now add the DataProducer to the collection
		if (!this.consumers.contains(consumer)) {
			this.consumers.add(consumer);
		}
	}

	/**
	 * This method removes the given <code>DataProducer</code> from the
	 * collection
	 * 
	 * @param consumer
	 *            is the <code>DataProducer</code> to remove from the collection
	 */
	public void removeConsumer(DataProducer consumer) {
		if (consumer == null)
			return;
		if ((this.consumers != null) && (this.consumers.contains(consumer))) {
			if (consumer.getInputs().contains(this))
				consumer.getInputs().remove(this);
			this.consumers.remove(consumer);
		}
	}

	/**
	 * This method will clear out the collection of <code>DataProducer</code>s
	 * and keep the integrity of the relationships intact.
	 */
	public void clearConsumers() {
		Iterator<DataProducer> consumerIter = this.consumers.iterator();
		while (consumerIter.hasNext()) {
			DataProducer tempConsumer = (DataProducer) consumerIter.next();
			tempConsumer.getInputs().remove(this);
		}
		this.consumers.clear();
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

	public void setVersion(long version) {
		this.version = version;
	}

	/**
	 * This static method checks to see if the incoming dataContainerType
	 * matches one of the constants defined in the class
	 * 
	 * @param dataContainerType
	 * @return
	 */
	public static boolean isValidDataContainerType(String dataContainerType) {
		boolean result = true;
		if ((dataContainerType == null)
				|| ((!dataContainerType.equals(TYPE_FILE)) && (!dataContainerType
						.equals(TYPE_STREAM)))) {
			result = false;
		}
		return result;
	}

	/**
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
		if (!(obj instanceof DataContainer))
			return false;

		// Cast to DataContainer object
		final DataContainer that = (DataContainer) obj;

		// Now check missing business key (URI)
		if ((this.uriString == null) || (that.getUriString() == null))
			return false;

		// Now check hashcodes
		if (this.hashCode() == that.hashCode()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @see IMetadataObject#hashCode()
	 */
	public int hashCode() {
		// Calculate the hashcode
		int result = 62;

		if (uriString != null)
			result = result + uriString.hashCode();

		// Now return it
		return result;
	}

	/**
	 * @see IMetadataObject#toStringRepresentation(String)
	 */
	public String toStringRepresentation(String delimiter) {
		// If the delimiter is not specified, use a default one
		if (delimiter == null)
			delimiter = IMetadataObject.DEFAULT_DELIMITER;

		StringBuffer sb = new StringBuffer();
		sb.append("DataContainer");
		sb.append(delimiter + "id=" + this.getId());
		sb.append(delimiter + "name=" + this.getName());
		sb.append(delimiter + "description=" + this.getDescription());
		sb.append(delimiter + "dataContainerType="
				+ this.getDataContainerType());
		sb.append(delimiter + "startDate="
				+ xmlDateFormat.format(this.startDate));
		sb.append(delimiter + "endDate=" + xmlDateFormat.format(this.endDate));
		sb.append(delimiter + "original=" + this.isOriginal());
		sb.append(delimiter + "uriString=" + this.getUriString());
		sb.append(delimiter + "contentLength=" + this.getContentLength());
		sb.append(delimiter + "mimeType=" + this.getMimeType());
		sb.append(delimiter + "numberOfRecords=" + this.getNumberOfRecords());
		sb.append(delimiter + "dodsAccessible=" + this.isDodsAccessible());
		sb.append(delimiter + "dodsUrlString=" + this.getDodsUrlString());
		sb.append(delimiter + "noNetCDF=" + this.isNoNetCDF());
		sb.append(delimiter + "minLatitude=" + this.getMinLatitude());
		sb.append(delimiter + "maxLatitude=" + this.getMaxLatitude());
		sb.append(delimiter + "minLongitude=" + this.getMinLongitude());
		sb.append(delimiter + "maxLongitude=" + this.getMaxLongitude());
		sb.append(delimiter + "minDepth=" + this.getMinDepth());
		sb.append(delimiter + "maxDepth=" + this.getMaxDepth());
		return sb.toString();
	}

	/**
	 * In order to use the class, you should first create an empty object, then
	 * call this method, passing in the string representation. As an example:
	 * 
	 * <pre>
	 * DataContainer newDataContainer = new DataContainer();
	 * 
	 * newDataContainer
	 * 		.setValuesFromStringRepresentation(
	 * 				&quot;DataContainer|name=MyDataContainer|description=A really cool DataContainer|uriString=http://my.company.com/file.html&quot;,
	 * 				&quot;|&quot;);
	 * </pre>
	 * 
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
			} else if (key.equalsIgnoreCase("dataContainerType")) {
				this.setDataContainerType(value);
			} else if (key.equalsIgnoreCase("startDate")) {
				this.setStartDate(xmlDateFormat.parse(value));
			} else if (key.equalsIgnoreCase("endDate")) {
				this.setEndDate(xmlDateFormat.parse(value));
			} else if (key.equalsIgnoreCase("original")) {
				if ((!value.equalsIgnoreCase("true"))
						&& (!value.equalsIgnoreCase("false")))
					throw new MetadataException(
							"Could not convert the specified value for original ("
									+ value
									+ ") to a boolean (valid options are 'true' or 'false')");
				this.setOriginal(Boolean.valueOf(value));
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
			} else if (key.equalsIgnoreCase("numberOfRecords")) {
				try {
					this.setNumberOfRecords(Long.valueOf(value));
				} catch (NumberFormatException e) {
					throw new MetadataException(
							"The value specified for numberOfRecords (" + value
									+ ") could not be converted to a Long: "
									+ e.getMessage());
				}
			} else if (key.equalsIgnoreCase("dodsAccessible")) {
				if ((!value.equalsIgnoreCase("true"))
						&& (!value.equalsIgnoreCase("false")))
					throw new MetadataException(
							"Could not convert the specified value for dodsAccessible ("
									+ value
									+ ") to a boolean (valid options are 'true' or 'false')");
				this.setDodsAccessible(Boolean.valueOf(value));
			} else if (key.equalsIgnoreCase("dodsUrlString")) {
				this.setDodsUrlString(value);
			} else if (key.equalsIgnoreCase("dodsUrl")) {
				URL urlToSet = null;
				try {
					urlToSet = new URL(value);
				} catch (MalformedURLException e) {
					throw new MetadataException(
							"The string "
									+ value
									+ " that was specified as the DODS URL, could not be "
									+ "converted to a URL: " + e.getMessage());
				}
				this.setDodsUrl(urlToSet);
			} else if (key.equalsIgnoreCase("noNetCDF")) {
				if ((!value.equalsIgnoreCase("true"))
						&& (!value.equalsIgnoreCase("false")))
					throw new MetadataException(
							"Could not convert the specified value for noNetCDF ("
									+ value
									+ ") to a boolean (valid options are 'true' or 'false')");
				this.setNoNetCDF(Boolean.valueOf(value));
			} else if (key.equalsIgnoreCase("minLatitude")) {
				try {
					this.setMinLatitude(Double.valueOf(value));
				} catch (NumberFormatException e) {
					throw new MetadataException(
							"The value specified for minLatitude (" + value
									+ ") could not be converted to a Double: "
									+ e.getMessage());
				}
			} else if (key.equalsIgnoreCase("maxLatitude")) {
				try {
					this.setMaxLatitude(Double.valueOf(value));
				} catch (NumberFormatException e) {
					throw new MetadataException(
							"The value specified for maxLatitude (" + value
									+ ") could not be converted to a Double: "
									+ e.getMessage());
				}
			} else if (key.equalsIgnoreCase("minLongitude")) {
				try {
					this.setMinLongitude(Double.valueOf(value));
				} catch (NumberFormatException e) {
					throw new MetadataException(
							"The value specified for minLongitude (" + value
									+ ") could not be converted to a Double: "
									+ e.getMessage());
				}
			} else if (key.equalsIgnoreCase("maxLongitude")) {
				try {
					this.setMaxLongitude(Double.valueOf(value));
				} catch (NumberFormatException e) {
					throw new MetadataException(
							"The value specified for maxLongitude (" + value
									+ ") could not be converted to a Double: "
									+ e.getMessage());
				}
			} else if (key.equalsIgnoreCase("minDepth")) {
				try {
					this.setMinDepth(Float.valueOf(value));
				} catch (NumberFormatException e) {
					throw new MetadataException(
							"The value specified for minDepth (" + value
									+ ") could not be converted to a Float: "
									+ e.getMessage());
				}
			} else if (key.equalsIgnoreCase("maxDepth")) {
				try {
					this.setMaxDepth(Float.valueOf(value));
				} catch (NumberFormatException e) {
					throw new MetadataException(
							"The value specified for maxDepth (" + value
									+ ") could not be converted to a Float: "
									+ e.getMessage());
				}
			} else {
				throw new MetadataException("The attribute specified by " + key
						+ " is not a recognized field of "
						+ this.getClass().getName());
			}
		}
	}

	/**
	 * This is the method for re-constituting a DataContainer object from a
	 * serialized stream
	 * 
	 * @see Externalizable#readExternal(ObjectInput)
	 */
	@SuppressWarnings("unchecked")
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		consumers = (Collection<DataProducer>) in.readObject();
		contentLength = (Long) in.readObject();
		creator = (DataProducer) in.readObject();
		dataContainerGroups = (Collection<DataContainerGroup>) in.readObject();
		dataContainerType = (String) in.readObject();
		dateRange = (IDateRange) in.readObject();
		description = (String) in.readObject();
		dodsAccessible = (Boolean) in.readObject();
		dodsUrlString = (String) in.readObject();
		endDate = (Date) in.readObject();
		headerDescription = (HeaderDescription) in.readObject();
		// Read in ID
		Object idObject = in.readObject();
		if (idObject instanceof Integer) {
			Integer intId = (Integer) idObject;
			id = new Long(intId.longValue());
		} else if (idObject instanceof Long) {
			id = (Long) idObject;
		}
		keywords = (Collection<Keyword>) in.readObject();
		maxDepth = (Float) in.readObject();
		maxLatitude = (Double) in.readObject();
		maxLongitude = (Double) in.readObject();
		mimeType = (String) in.readObject();
		minDepth = (Float) in.readObject();
		minLatitude = (Double) in.readObject();
		minLongitude = (Double) in.readObject();
		name = (String) in.readObject();
		noNetCDF = (Boolean) in.readObject();
		numberOfRecords = (Long) in.readObject();
		original = (Boolean) in.readObject();
		person = (Person) in.readObject();
		recordDescription = (RecordDescription) in.readObject();
		resources = (Collection<Resource>) in.readObject();
		startDate = (Date) in.readObject();
		uriString = (String) in.readObject();
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
	 * This is the method to do a custom serialization of the DataContainer
	 * object
	 * 
	 * @see Externalizable#writeExternal(ObjectOutput)
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		// Write out consumers (as null for now)
		out.writeObject(null);
		out.writeObject(contentLength);
		// Write out the creator (as null for now)
		out.writeObject(null);
		// Write out DataContainerGroups (as null for now)
		out.writeObject(null);
		out.writeObject(dataContainerType);
		// Write out date range (as null for now)
		out.writeObject(null);
		out.writeObject(description);
		out.writeObject(dodsAccessible);
		out.writeObject(dodsUrlString);
		out.writeObject(endDate);
		// Write out the HeaderDescription (as null for now)
		out.writeObject(null);
		out.writeObject(id);
		// Write out Keywords (as null for now)
		out.writeObject(null);
		out.writeObject(maxDepth);
		out.writeObject(maxLatitude);
		out.writeObject(maxLongitude);
		out.writeObject(mimeType);
		out.writeObject(minDepth);
		out.writeObject(minLatitude);
		out.writeObject(minLongitude);
		out.writeObject(name);
		out.writeObject(noNetCDF);
		out.writeObject(numberOfRecords);
		out.writeObject(original);
		// Write out the person (as null for now)
		out.writeObject(null);
		// Write out the recordDescription (as null for now)
		out.writeObject(null);
		// Write out resources (as null for now)
		out.writeObject(null);
		out.writeObject(startDate);
		out.writeObject(uriString);
		out.writeObject(version);
	}

	/**
	 * This method overrides the clone method to produce a copy of an object. It
	 * will clear the ID on the newly created copy to prevent odd behavior.
	 */
	protected Object clone() throws CloneNotSupportedException {
		// Create the clone
		DataContainer clone = new DataContainer();

		// Set the fields
		try {
			clone.setId(null);
			clone.setName(this.getName());
			clone.setDescription(this.getDescription());
			clone.setDataContainerType(this.getDataContainerType());
			clone.setStartDate(this.getStartDate());
			clone.setEndDate(this.getEndDate());
			clone.setOriginal(this.isOriginal());
			clone.setUriString(this.getUriString());
			clone.setContentLength(this.getContentLength());
			clone.setMimeType(this.getMimeType());
			clone.setNumberOfRecords(this.getNumberOfRecords());
			clone.setDodsAccessible(this.isDodsAccessible());
			clone.setDodsUrlString(this.getDodsUrlString());
			clone.setNoNetCDF(this.isNoNetCDF());
			clone.setMinLatitude(this.getMinLatitude());
			clone.setMaxLatitude(this.getMaxLatitude());
			clone.setMinLongitude(this.getMinLongitude());
			clone.setMaxLongitude(this.getMaxLongitude());
			clone.setMinDepth(this.getMinDepth());
			clone.setMaxDepth(this.getMaxDepth());
		} catch (MetadataException e) {
		}
		// Now return the clone
		return clone;
	}

	/**
	 * This method returns a copy of the object itself and also fills out the
	 * relationships with deep copies of:
	 * <ol>
	 * <li>Person</li>
	 * <li>HeaderDescription</li>
	 * <li>RecordDescription</li>
	 * <li>DataContainerGroups</li>
	 * <li>Keywords</li>
	 * <li>Resources</li>
	 * </ol>
	 * It does NOT copy the relationships of Creator or Consumers
	 */
	public IMetadataObject deepCopy() throws CloneNotSupportedException {
		logger.debug("deepCopy called");
		// Grab the clone
		DataContainer deepClone = (DataContainer) this.clone();
		if (deepClone != null) {
			logger.debug("Clone created and is:");
			logger.debug(deepClone.toStringRepresentation("|"));
		} else {
			logger.debug("Clone operation returned null!");
		}

		// Now walk the relationships
		if (this.getPerson() != null) {
			Person clonedPerson = (Person) this.getPerson().deepCopy();
			logger.debug("Will add the following cloned person:");
			logger.debug(clonedPerson.toStringRepresentation("|"));
			deepClone.setPerson(clonedPerson);
		} else {
			deepClone.setPerson(null);
		}
		if (this.getHeaderDescription() != null) {
			HeaderDescription clonedHeaderDescription = (HeaderDescription) this
					.getHeaderDescription().deepCopy();
			logger.debug("Will add the following cloned HeaderDescription:");
			logger.debug(clonedHeaderDescription.toStringRepresentation("|"));
			deepClone.setHeaderDescription(clonedHeaderDescription);
		} else {
			deepClone.setHeaderDescription(null);
		}
		if (this.getRecordDescription() != null) {
			RecordDescription clonedRecordDescription = (RecordDescription) this
					.getRecordDescription().deepCopy();
			logger.debug("Will attach the following cloned RecordDescription:");
			logger.debug(clonedRecordDescription.toStringRepresentation("|"));
			deepClone.setRecordDescription(clonedRecordDescription);
		} else {
			deepClone.setRecordDescription(null);
		}
		if ((this.getDataContainerGroups() != null)
				&& (this.getDataContainerGroups().size() > 0)) {
			logger.debug("There are " + this.getDataContainerGroups().size()
					+ " DataContainerGroups to clone and attach.");
			Collection<DataContainerGroup> dcgsToCopy = this
					.getDataContainerGroups();
			Iterator<DataContainerGroup> dcgsIter = dcgsToCopy.iterator();
			while (dcgsIter.hasNext()) {
				DataContainerGroup clonedDataContainerGroup = (DataContainerGroup) ((DataContainerGroup) dcgsIter
						.next()).deepCopy();
				logger
						.debug("Will add the following clone DataContainerGroup:");
				logger.debug(clonedDataContainerGroup
						.toStringRepresentation("|"));
				deepClone.addDataContainerGroup(clonedDataContainerGroup);
			}
		}
		if ((this.getKeywords() != null) && (this.getKeywords().size() > 0)) {
			logger.debug("There are " + this.getKeywords().size()
					+ " Keywords to clone and attach");
			Collection<Keyword> keywordsToCopy = this.getKeywords();
			Iterator<Keyword> keywordIter = keywordsToCopy.iterator();
			while (keywordIter.hasNext()) {
				Keyword clonedKeyword = (Keyword) ((Keyword) keywordIter.next())
						.deepCopy();
				logger.debug("Will add the following cloned Keyword:");
				logger.debug(clonedKeyword.toStringRepresentation("|"));
				deepClone.addKeyword(clonedKeyword);
			}
		}
		if ((this.getResources() != null) && (this.getResources().size() > 0)) {
			logger.debug("There are " + this.getResources().size()
					+ " Resources to clone and add.");
			Collection<Resource> resourcesToCopy = this.getResources();
			Iterator<Resource> resourceIter = resourcesToCopy.iterator();
			while (resourceIter.hasNext()) {
				Resource clonedResource = (Resource) ((Resource) resourceIter
						.next()).deepCopy();
				logger.debug("Will add the following cloned Resource:");
				logger.debug(clonedResource.toStringRepresentation("|"));
				deepClone.addResource(clonedResource);
			}
		}
		return deepClone;
	}

}