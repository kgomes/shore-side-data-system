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
 * This class represents a producer of data in the system.
 * <hr>
 * 
 * @stereotype moment-interval
 * @hibernate.class table="DataProducer"
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.35 $
 */
public class DataProducer implements IMetadataObject, IDescription,
		IResourceOwner, IDateRange, Externalizable {

	/**
	 * This is a Log4JLogger that is used to log information to
	 */
	static Logger logger = Logger.getLogger(DataProducer.class);

	/**
	 * This is the version that we can control for serialization purposes
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * This is the persistence layer identifier
	 */
	private Long id;

	/**
	 * This is the arbitrary name given to the DataProducer
	 */
	private String name;

	/**
	 * The description of the DataProducer
	 */
	private String description;

	/**
	 * This is a string that categories the type of data producer. It should
	 * match one of the constants defined in this class.
	 */
	private String dataProducerType = DataProducer.TYPE_DEPLOYMENT;

	/**
	 * This is the <code>Date</code> when the data producer started to produce
	 * data
	 */
	private Date startDate;

	/**
	 * This is the <code>Date</code> when the data producer stopped sending data
	 * (or it is the date of the last time it sent data). If this is not
	 * defined, it usually means the data producer is still producing data
	 */
	private Date endDate;

	/**
	 * This is an attribute that gives the range of time that this
	 * <code>DataProducer</code> produced data
	 */
	private transient IDateRange dateRange = new DateRange(this);

	/**
	 * This is the role that the <code>DataProducer</code> can take in its
	 * production of data. It should match one of the constants defined in the
	 * class
	 */
	private String role;

	/**
	 * This is a nominal latitude (decimal degrees) that can be assigned to the
	 * producer of data. It gives the approximate geolocation of the producer
	 * when it was producing data.
	 */
	private Double nominalLatitude = null;

	/**
	 * This is the accuracy that is to be expected in the latitude data
	 */
	private Float nominalLatitudeAccuracy = null;

	/**
	 * This is a nominal longitude (decimal degrees) that can be assigned to the
	 * producer of data. It gives the approximate geolocation of the producer
	 * when it was producing data.
	 */
	private Double nominalLongitude = null;

	/**
	 * This is the accuracy that is to be expected in the longitude data
	 */
	private Float nominalLongitudeAccuracy = null;

	/**
	 * This is a nominal depth (in meters) that can be assigned to the producer
	 * of data. It gives the approximate geolocation of the producer when it was
	 * producing data.
	 */
	private Float nominalDepth = null;

	/**
	 * This is the accuracy that is to be expected in the depth data
	 */
	private Float nominalDepthAccuracy = null;

	/**
	 * This is a nominal altitude (in meters) over the bottom that can be
	 * assigned to the producer of data. It gives the approximate geolocation of
	 * the producer when it was producing data.
	 */
	private Float nominalBenthicAltitude = null;

	/**
	 * This is the accuracy that is to be expected in the benthic altitude data
	 */
	private Float nominalBenthicAltitudeAccuracy = null;

	/**
	 * This is the distance (in meters) that the data producer is offset from
	 * its parent (if applicable). This is the offset in a locally defined X
	 * direction.
	 * 
	 * All lower case to prevent Hibernate PropertyNotFoundException with
	 * access="field"
	 */
	private Float xoffset = null;

	/**
	 * This is the distance (in meters) that the data producer is offset from
	 * its parent (if applicable). This is the offset in a locally defined Y
	 * direction.
	 * 
	 * All lower case to prevent Hibernate PropertyNotFoundException with
	 * access="field"
	 */
	private Float yoffset = null;

	/**
	 * This is the distance (in meters) that the data producer is offset from
	 * its parent (if applicable). This is the offset in a locally defined Z
	 * direction.
	 * 
	 * All lower case to prevent Hibernate PropertyNotFoundException with
	 * access="field"
	 */
	private Float zoffset = null;

	/**
	 * This is a description of how the data producer was oriented when it was
	 * producing data.
	 */
	private String orientationDescription = null;

	/**
	 * The X3D compliant orientation description
	 */
	private String x3DOrientationText = null;

	/**
	 * This the name of the host where the data was produced from.
	 */
	private String hostName;

	/**
	 * This is the <code>Person</code> that is the point of contact for the
	 * <code>DataProducer</code>
	 */
	private Person person;

	/**
	 * This is a <code>Device</code> that is associated with the production of
	 * the data.
	 */
	private Device device;

	/**
	 * This is a <code>Software</code> that is associated with the production of
	 * the data.
	 */
	private Software software;

	/**
	 * This is a <code>DataProducer</code> that is considered to be the parent
	 * of this <code>DataProducer</code> (supports nested data producers).
	 */
	private DataProducer parentDataProducer;

	/**
	 * These are <code>DataProducer</code>s that are considered children of this
	 * <code>DataProducer</code> (supports nested data producers).
	 */
	private Collection<DataProducer> childDataProducers = new HashSet<DataProducer>();

	/**
	 * This is a <code>Collection</code> of <code>DataProducerGroup</code>s that
	 * this <code>DataProducer</code> belongs to
	 */
	private Collection<DataProducerGroup> dataProducerGroups = new HashSet<DataProducerGroup>();

	/**
	 * These are the <code>DataContainer</code>s that were used by the
	 * <code>DataProducer</code> to produce its outputs.
	 */
	private Collection<DataContainer> inputs = new HashSet<DataContainer>();

	/**
	 * These are the output <code>DataContainer</code>s from this
	 * <code>DataProducer</code>.
	 */
	private Collection<DataContainer> outputs = new HashSet<DataContainer>();

	/**
	 * These are any <code>Resource</code>s that are associated with the
	 * <code>DataProducer</code>.
	 */
	private Collection<Resource> resources = new HashSet<Resource>();

	/**
	 * This is a collection of <code>Keyword</code> objects that can be used to
	 * search for <code>DataContainer</code>s.
	 */
	private Collection<Keyword> keywords = new HashSet<Keyword>();

	/**
	 * These are any <code>Event</code>s that have been linked to the
	 * <code>DataProducer</code>.
	 */
	private Collection<Event> events = new HashSet<Event>();

	/**
	 * A date formatter for convenience. This was marked transient so that when
	 * the ActionScript generator operated on this, it was ignored. Should not
	 * affect functionality
	 */
	private transient XmlDateFormat xmlDateFormat = new XmlDateFormat();

	/**
	 * Some constants
	 */
	public static final String TYPE_DEPLOYMENT = "Deployment";
	public static final String TYPE_PROCESS_RUN = "ProcessRun";

	public static final String ROLE_SENSOR = "sensor";
	public static final String ROLE_INSTRUMENT = "instrument";
	public static final String ROLE_PLATFORM = "platform";
	public static final String ROLE_OBSERVATORY = "observatory";

	/**
	 * This is the hibernate version that is used to check for dirty objects
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
	 * @see IDescription#getName()
	 * @hibernate.property access="field"
	 * @hibernate.column name="name" length="2048" not-null="true"
	 */
	public String getName() {
		return name;
	}

	/**
	 * @see IDescription#setName(String)
	 */
	public void setName(String name) throws MetadataException {
		MetadataValidator.isObjectNull(name);
		MetadataValidator.isStringShorterThan(name, 2048);
		this.name = name;
	}

	/**
	 * @see IDescription#getDescription()
	 * @hibernate.property length="2048" access="field"
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
	 * These methods get and set the type of <code>DataProducer</code> and
	 * should match one of the constants defined in the class
	 * 
	 * @hibernate.property access="field"
	 * @hibernate.column name="dataProducerType" not-null="true"
	 *                   index="type_index"
	 * @return a <code>String</code> that is the type of
	 *         <code>DataProducer</code>
	 */
	public String getDataProducerType() {
		return dataProducerType;
	}

	public void setDataProducerType(String dataProducerType)
			throws MetadataException {
		if (!isValidDataProducerType(dataProducerType))
			throw new MetadataException("The specified dataProducerType ("
					+ dataProducerType
					+ ") does not match a constant defined in the class");
		this.dataProducerType = dataProducerType;
	}

	/**
	 * @see IDateRange#getStartDate()
	 * @hibernate.property access="field"
	 */
	public Date getStartDate() {
		return startDate;
	}

	/**
	 * @return the Epoch Seconds (seconds since 1/1/1970) of the start time.
	 */
	public long startDateAsEsecs() {

		return startDate.getTime() / 1000;

	}

	/**
	 * @return the Epoch Seconds (seconds since 1/1/1970) of the end time.
	 */
	public long endDateAsEsecs() {
		return endDate.getTime() / 1000;
	}

	/**
	 * @see IDateRange#setStartDate(Date)
	 */
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	/**
	 * @see IDateRange#getEndDate()
	 * @hibernate.property access="field"
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
	 * These methods get and set the role of the <code>DataProducer</code> and
	 * should match one of the role constants defined in the class
	 * 
	 * @hibernate.property access="field"
	 * @return the <code>String</code> that is the role of the
	 *         <code>DataProducer</code>
	 */
	public String getRole() {
		return role;
	}

	public void setRole(String role) throws MetadataException {
		if (!isValidRole(role))
			throw new MetadataException("The role specified (" + role
					+ ") does not match a constant defined in this class");
		this.role = role;
	}

	/**
	 * These methods set the nominal lattitude that is associated with the
	 * <code>DataProducer</code> when it was producing the data.
	 * 
	 * @hibernate.property access="field"
	 * @return a <code>Double</code> that is the nominal latitute in decimal
	 *         degrees.
	 */
	public Double getNominalLatitude() {
		return nominalLatitude;
	}

	public void setNominalLatitude(Double nominalLatitude)
			throws MetadataException {
		MetadataValidator.isValueBetween(nominalLatitude,
				MetadataValidator.MIN_LATITUDE, MetadataValidator.MAX_LATITUDE);
		this.nominalLatitude = nominalLatitude;
	}

	/**
	 * These methods set the nominal lattitude accuracy that is associated with
	 * the <code>DataProducer</code> when it was producing the data.
	 * 
	 * @hibernate.property access="field"
	 * @return a <code>Double</code> that is the nominal latitute in decimal
	 *         degrees.
	 */
	public Float getNominalLatitudeAccuracy() {
		return nominalLatitudeAccuracy;
	}

	public void setNominalLatitudeAccuracy(Float nominalLatitudeAccuracy) {
		this.nominalLatitudeAccuracy = nominalLatitudeAccuracy;
	}

	/**
	 * These methods set the nominal longitude that is associated with the
	 * <code>DataProducer</code> when it was producing the data.
	 * 
	 * @hibernate.property access="field"
	 * @return a <code>Double</code> that is the nominal longitude in decimal
	 *         degrees.
	 */
	public Double getNominalLongitude() {
		return nominalLongitude;
	}

	public void setNominalLongitude(Double nominalLongitude)
			throws MetadataException {
		MetadataValidator.isValueBetween(nominalLongitude,
				MetadataValidator.MIN_LONGITUDE,
				MetadataValidator.MAX_LONGITUDE);
		this.nominalLongitude = nominalLongitude;
	}

	/**
	 * These methods set the nominal longitude accuracy that is associated with
	 * the <code>DataProducer</code> when it was producing the data.
	 * 
	 * @hibernate.property access="field"
	 * @return a <code>Double</code> that is the nominal latitute in decimal
	 *         degrees.
	 */
	public Float getNominalLongitudeAccuracy() {
		return nominalLongitudeAccuracy;
	}

	public void setNominalLongitudeAccuracy(Float nominalLongitudeAccuracy) {
		this.nominalLongitudeAccuracy = nominalLongitudeAccuracy;
	}

	/**
	 * These methods get/set the nominal depth that is associated with the
	 * <code>DataProducer</code> when it was producing the data.
	 * 
	 * @hibernate.property access="field"
	 * @return a <code>Float</code> that is the nominal depth in meters
	 */
	public Float getNominalDepth() {
		return nominalDepth;
	}

	public void setNominalDepth(Float nominalDepth) throws MetadataException {
		this.nominalDepth = nominalDepth;
	}

	/**
	 * These methods get/set the nominal depth accuracy that is associated with
	 * the <code>DataProducer</code> when it was producing the data.
	 * 
	 * @hibernate.property access="field"
	 * @return a <code>Float</code> that is the nominal depth in meters
	 */
	public Float getNominalDepthAccuracy() {
		return nominalDepthAccuracy;
	}

	public void setNominalDepthAccuracy(Float nominalDepthAccuracy) {
		this.nominalDepthAccuracy = nominalDepthAccuracy;
	}

	/**
	 * These methods get/set the nominal benthic altitude that is associated
	 * with the <code>DataProducer</code> when it was producing the data. This
	 * is the distance of the deployment above the ocean bottom measuered in
	 * meters.
	 * 
	 * @hibernate.property access="field"
	 * @return a <code>Float</code> that is the nominal depth in meters
	 */
	public Float getNominalBenthicAltitude() {
		return nominalBenthicAltitude;
	}

	public void setNominalBenthicAltitude(Float nominalBenthicAltitude) {
		this.nominalBenthicAltitude = nominalBenthicAltitude;
	}

	/**
	 * These methods get/set the nominal benthic altitude accuracy that is
	 * associated with the <code>DataProducer</code> when it was producing the
	 * data.
	 * 
	 * @hibernate.property access="field"
	 * @return a <code>Float</code> that is the nominal depth in meters
	 */
	public Float getNominalBenthicAltitudeAccuracy() {
		return nominalBenthicAltitudeAccuracy;
	}

	public void setNominalBenthicAltitudeAccuracy(
			Float nominalBenthicAltitudeAccuracy) {
		this.nominalBenthicAltitudeAccuracy = nominalBenthicAltitudeAccuracy;
	}

	/**
	 * These methods get/set the offset in meters of the
	 * <code>DataProducer</code> from some local coordinate axis (relative to
	 * its parent) in the X direction
	 * 
	 * @hibernate.property access="field"
	 * @return the <code>Float</code> that is the offset in meters in the local
	 *         X direction
	 */
	public Float getXoffset() {
		return xoffset;
	}

	public void setXoffset(Float xOffset) {
		this.xoffset = xOffset;
	}

	/**
	 * These methods get/set the offset in meters of the
	 * <code>DataProducer</code> from some local coordinate axis (relative to
	 * its parent) in the Y direction
	 * 
	 * @hibernate.property access="field"
	 * @return the <code>Float</code> that is the offset in meters in the local
	 *         Y direction
	 */
	public Float getYoffset() {
		return yoffset;
	}

	public void setYoffset(Float yOffset) {
		this.yoffset = yOffset;
	}

	/**
	 * These methods get/set the offset in meters of the
	 * <code>DataProducer</code> from some local coordinate axis (relative to
	 * its parent) in the Z direction
	 * 
	 * @hibernate.property access="field"
	 * @return the <code>Float</code> that is the offset in meters in the local
	 *         Z direction
	 */
	public Float getZoffset() {
		return zoffset;
	}

	public void setZoffset(Float zOffset) {
		this.zoffset = zOffset;
	}

	/**
	 * These methods get/set the text that describes the orientation of the
	 * <code>DataProducer</code> when it was producing data
	 * 
	 * @hibernate.property access="field"
	 * @return is the <code>String</code> that is the description of the
	 *         orientation of the <code>DataProducer</code> while it was
	 *         producing data
	 */
	public String getOrientationDescription() {
		return orientationDescription;
	}

	public void setOrientationDescription(String orientationDescription) {
		this.orientationDescription = orientationDescription;
	}

	/**
	 * These methods get/set the X3D formatted text that describes the
	 * orientation of the <code>DataProducer</code> when it was producing data.
	 * The format of this text is a 4 number string with the first three numbers
	 * representing a unit axis about which the device is rotated. The fourth
	 * number is the angle in radians about which the device is rotated for this
	 * deployment. The right-hand rule is followed and the orientation of the
	 * x3DModelText of the Device affects the final appearance of the
	 * deployment.
	 * 
	 * @hibernate.property access="field"
	 * @return is the <code>String</code> that is the description in X3D of the
	 *         orientation of the <code>DataProducer</code> while it was
	 *         producing data
	 */
	public String getX3DOrientationText() {
		return x3DOrientationText;
	}

	public void setX3DOrientationText(String x3DOrientationText) {
		this.x3DOrientationText = x3DOrientationText;
	}

	/**
	 * These methods get/set the name of the host that the
	 * <code>DataProducer</code> was producing data from
	 * 
	 * @hibernate.property access="field"
	 * @return the <code>String</code> that is the name of the host where the
	 *         <code>DataProducer</code> produced data from
	 */
	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	/**
	 * These methods get/set the <code>Person</code> that is usually seen as the
	 * owner of the <code>DataProducer</code> (or point of contact).
	 * 
	 * @hibernate.many-to-one class="moos.ssds.metadata.Person"
	 *                        column="PersonID_FK"
	 *                        foreign-key="DataProducer_Owned_By_Person"
	 *                        cascade="none" lazy="true" access="field"
	 * @return the <code>Person</code> that is the owner of the
	 *         <code>DataProducer</code>. Returns null if no owner has been
	 *         defined.
	 */
	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	/**
	 * These methods get/set the <code>Device</code> that is associated with the
	 * <code>DataProducer</code>.
	 * 
	 * @hibernate.many-to-one class="moos.ssds.metadata.Device"
	 *                        column="DeviceID_FK"
	 *                        foreign-key="DataProducer_Associated_To_Device"
	 *                        cascade="none" lazy="false" outer-join="true"
	 * @return the <code>Device</code> that is associated with the
	 *         <code>DataProducer</code>.
	 */
	public Device getDevice() {
		return device;
	}

	public void setDevice(Device device) {
		this.device = device;
	}

	/**
	 * These methods get/set the <code>Software</code> that is associated with
	 * the <code>DataProducer</code>.
	 * 
	 * @hibernate.many-to-one class="moos.ssds.metadata.Software"
	 *                        column="SoftwareID_FK"
	 *                        foreign-key="DataProducer_Associated_To_Software"
	 *                        cascade="none" lazy="true" access="field"
	 * @return the <code>Software</code> that is associated with the
	 *         <code>DataProducer</code>.
	 */
	public Software getSoftware() {
		return software;
	}

	public void setSoftware(Software software) {
		this.software = software;
	}

	/**
	 * These methods get/set the parent <code>DataProducer</code> of another
	 * <code>DataProducer</code>. This allows for nested
	 * <code>DataProducer</code>s.
	 * 
	 * @hibernate.many-to-one class="moos.ssds.metadata.DataProducer"
	 *                        column="ParentID_FK"
	 *                        foreign-key="DataProducer_Has_Child_DataProducers"
	 *                        cascade="none" lazy="true" inverse="false"
	 *                        access="field"
	 * @return the <code>DataProducer</code> that is the parent of the
	 *         <code>DataProducer</code> that the method is called on.
	 */
	public DataProducer getParentDataProducer() {
		return parentDataProducer;
	}

	public void setParentDataProducer(DataProducer parentDataProducer) {
		this.parentDataProducer = parentDataProducer;
	}

	/**
	 * This methods gets/sets the collection of <code>DataProducer</code>s that
	 * are considered children of another <code>DataProducer</code>.
	 * 
	 * @hibernate.set cascade="none" inverse="true" lazy="true" batch-size="5"
	 * @hibernate.collection-key column="ParentID_FK"
	 * @hibernate.collection-one-to-many class="moos.ssds.metadata.DataProducer"
	 * @return the <code>Collection</code> of <code>DataProducer</code>s that
	 *         are considered children of the <code>DataProducer</code> this
	 *         method is called on.
	 */
	public Collection<DataProducer> getChildDataProducers() {
		return childDataProducers;
	}

	public void setChildDataProducers(
			Collection<DataProducer> childDataProducers) {
		this.childDataProducers = childDataProducers;
	}

	/**
	 * This method adds the given <code>DataProducer</code> to the collection
	 * child <code>DataProducer</code>s.
	 * 
	 * @param dataContainerGroup
	 *            the <code>DataContainerGroup</code> to add
	 */
	public void addChildDataProducer(DataProducer childDataProducer) {
		// If null was passed in, just return
		if (childDataProducer == null)
			return;

		// Make sure it is not trying to add itself (this would be very bad)
		if (childDataProducer.equals(this))
			return;

		// Set the reverse relationship
		childDataProducer.setParentDataProducer(this);

		// Now add the DataContainerGroup to the collection
		if (!this.childDataProducers.contains(childDataProducer)) {
			this.childDataProducers.add(childDataProducer);
		}
	}

	/**
	 * This method removes the given <code>DataProducer</code> from the
	 * collection of child <code>DataProducer</code>s
	 * 
	 * @param keyword
	 *            is the <code>DataProducer</code> to remove from the collection
	 */
	public void removeChildDataProducer(DataProducer childDataProducer) {
		if (childDataProducer == null)
			return;
		// Clear the parent
		childDataProducer.setParentDataProducer(null);
		// Now remove from the collection
		if ((this.childDataProducers != null)
				&& (this.childDataProducers.contains(childDataProducer))) {
			this.childDataProducers.remove(childDataProducer);
		}
	}

	/**
	 * This method will clear out the collection of <code>DataProducer</code>s
	 * that are children and keep the integrity of the relationships intact.
	 */
	public void clearChildDataProducers() {
		Iterator<DataProducer> childIter = this.childDataProducers.iterator();
		while (childIter.hasNext()) {
			DataProducer tempChild = (DataProducer) childIter.next();
			tempChild.setParentDataProducer(null);
		}
		this.childDataProducers.clear();
	}

	/**
	 * These methods get and set the <code>Collection</code> of
	 * <code>DataProducerGroup</code>s that are associated with the
	 * <code>DataProducer</code>
	 * 
	 * @hibernate.set table="DataProducerAssocDataProducerGroup" cascade="none"
	 *                lazy="true"
	 * @hibernate.collection-key column="DataProducerID_FK"
	 * @hibernate.collection-many-to-many column="DataProducerGroupID_FK"
	 *                                    class="moos.ssds.metadata.DataProducerGroup"
	 * @return the <code>Collection</code> of <code>DataProducerGroup</code>s
	 *         that have been associated to the <code>DataProducer</code>
	 */
	public Collection<DataProducerGroup> getDataProducerGroups() {
		return dataProducerGroups;
	}

	public void setDataProducerGroups(
			Collection<DataProducerGroup> dataProducerGroups) {
		this.dataProducerGroups = dataProducerGroups;
	}

	/**
	 * This method adds the given <code>DataProducerGroup</code> to the
	 * collection associated with the <code>DataProducer</code>.
	 * 
	 * @param dataProducerGroup
	 *            the <code>DataProducerGroup</code> to add
	 */
	public void addDataProducerGroup(DataProducerGroup dataProducerGroup) {
		// If null was passed in, just return
		if (dataProducerGroup == null)
			return;

		// Now add the DataProducerGroup to the collection
		if (!this.dataProducerGroups.contains(dataProducerGroup)) {
			this.dataProducerGroups.add(dataProducerGroup);
		}
	}

	/**
	 * This method removes the given <code>DataProducerGroup</code> from the
	 * collection
	 * 
	 * @param dataProducerGroup
	 *            is the <code>DataProducerGroup</code> to remove from the
	 *            collection
	 */
	public void removeDataProducerGroup(DataProducerGroup dataProducerGroup) {
		if (dataProducerGroup == null)
			return;
		if ((this.dataProducerGroups != null)
				&& (this.dataProducerGroups.contains(dataProducerGroup))) {
			this.dataProducerGroups.remove(dataProducerGroup);
		}
	}

	/**
	 * This method will clear out the collection of
	 * <code>DataProducerGroup</code>s and keep the integrity of the
	 * relationships intact.
	 */
	public void clearDataProducerGroups() {
		this.dataProducerGroups.clear();
	}

	/**
	 * These methods get and set the <code>Collection</code> of
	 * <code>DataContainer</code>s that are used as inputs to the
	 * <code>DataProducer</code>.
	 * 
	 * @hibernate.set table="DataProducerInput" cascade="none" inverse="false"
	 *                lazy="true"
	 * @hibernate.collection-key column="DataProducerID_FK"
	 * @hibernate.collection-many-to-many column="DataContainerID_FK"
	 *                                    class="moos.ssds.metadata.DataContainer"
	 * @return the <code>Collection</code> of <code>DataContainer</code>s that
	 *         are used for inputs
	 */
	public Collection<DataContainer> getInputs() {
		return inputs;
	}

	public void setInputs(Collection<DataContainer> inputs) {
		this.inputs = inputs;
	}

	/**
	 * This method adds the <code>DataContainer</code> to the collection of
	 * inputs that are used by this <code>DataProducer</code>
	 * 
	 * @param dataContainer
	 *            is the <code>DataContainer</code> to add to the collection of
	 *            inputs
	 */
	public void addInput(DataContainer dataContainer) {
		// If null was passed in, just return
		if (dataContainer == null)
			return;

		// Set the reverse relationship
		if (!dataContainer.getConsumers().contains(this)) {
			dataContainer.getConsumers().add(this);
		}

		// Now add the DataContainer to the collection
		if (!this.inputs.contains(dataContainer)) {
			this.inputs.add(dataContainer);
		}
	}

	/**
	 * This method removes the <code>DataContainer</code> from the collection of
	 * <code>DataContainer</code>s that are listed as inputs to the
	 * <code>DataProducer</code>
	 * 
	 * @param dataContainer
	 *            is the <code>DataContainer</code> to remove from the
	 *            <code>Collection</code> of inputs
	 */
	public void removeInput(DataContainer dataContainer) {
		if (dataContainer == null)
			return;
		if ((this.inputs != null) && (this.inputs.contains(dataContainer))) {
			if (dataContainer.getConsumers().contains(this))
				dataContainer.getConsumers().remove(this);
			this.inputs.remove(dataContainer);
		}
	}

	/**
	 * This method clears the collection of <code>DataContainer</code>s that are
	 * used as inputs to the <code>DataProducer</code> and keeps all
	 * relationships intact
	 */
	public void clearInputs() {
		Iterator<DataContainer> inputIter = this.inputs.iterator();
		while (inputIter.hasNext()) {
			DataContainer tempDC = (DataContainer) inputIter.next();
			tempDC.getConsumers().remove(this);
		}
		this.dataProducerGroups.clear();
	}

	/**
	 * These methods get and set the <code>Collection</code> of
	 * <code>DataContainer</code>s that are the outputs of the
	 * <code>DataProducer</code>
	 * 
	 * @hibernate.set cascade="none" inverse="true" lazy="false"
	 *                outer-join="true"
	 * @hibernate.collection-key column="DataProducerID_FK"
	 * @hibernate.collection-one-to-many 
	 *                                   class="moos.ssds.metadata.DataContainer"
	 * @return the <code>Collection</code> of <code>DataContainer</code> that
	 *         were created by the <code>DataProducer</code>
	 */
	public Collection<DataContainer> getOutputs() {
		return outputs;
	}

	public void setOutputs(Collection<DataContainer> outputs) {
		this.outputs = outputs;
	}

	/**
	 * This method add a <code>DataContainer</code> to the collection of outputs
	 * created by the <code>DataProducer</code>
	 * 
	 * @param dataContainer
	 *            the <code>DataContainer</code> to add to the
	 *            <code>Collection</code> of outputs
	 */
	public void addOutput(DataContainer dataContainer) {
		// If null was passed in, just return
		if (dataContainer == null)
			return;

		// If the output has a creator, remove it from the output collection of
		// that creator
		if (dataContainer.getCreator() != null) {
			dataContainer.getCreator().getOutputs().remove(dataContainer);
		}

		// Set the creator
		dataContainer.setCreator(this);

		// Now add the DataContainer to the collection
		if (!this.outputs.contains(dataContainer)) {
			this.outputs.add(dataContainer);
		}
	}

	/**
	 * This method removes a <code>DataContainer</code> from the collection of
	 * outputs created by the <code>DataProducer</code>.
	 * 
	 * @param dataContainer
	 *            the <code>DataContainer</code> to remove from the
	 *            <code>Collection</code> of outputs
	 */
	public void removeOutput(DataContainer dataContainer) {
		if (dataContainer == null)
			return;
		// Clear the creator of the DataContainer
		dataContainer.setCreator(null);

		if ((this.outputs != null) && (this.outputs.contains(dataContainer)))
			this.outputs.remove(dataContainer);
	}

	/**
	 * This method clears all the associated <code>DataContainer</code>s that
	 * are listed as outputs for the <code>DataProducer</code> and keep the
	 * relationships intact
	 */
	public void clearOutputs() {
		// Iterate over outputs
		if (this.outputs != null) {
			Iterator<DataContainer> outputIter = this.outputs.iterator();
			while (outputIter.hasNext()) {
				((DataContainer) outputIter.next()).setCreator(null);
			}
			this.outputs.clear();
		}

	}

	/**
	 * These methods get and set the <code>Collection</code> of
	 * <code>Resource</code>s that are associated with the
	 * <code>DataProducer</code>
	 * 
	 * @hibernate.set table="DataProducerAssocResource" cascade="none"
	 *                lazy="true"
	 * @hibernate.collection-key column="DataProducerID_FK"
	 * @hibernate.collection-many-to-many column="ResourceID_FK"
	 *                                    class="moos.ssds.metadata.Resource"
	 * @return the <code>Collection</code> of <code>Resource</code>s that are
	 *         associated with the <code>DataProducer</code>
	 */
	public Collection<Resource> getResources() {
		return resources;
	}

	public void setResources(Collection<Resource> resources) {
		this.resources = resources;
	}

	/**
	 * This method adds the given <code>Resource</code> to the collection
	 * associated with the <code>DataProducer</code>.
	 * 
	 * @param resource
	 *            the <code>Resource</code> to add
	 */
	public void addResource(Resource resource) {
		// If null was passed in, just return
		if (resource == null)
			return;

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
	 * These methods get and set the collection of keywords associated with the
	 * <code>DataContainer</code>. TODO kgomes 20051102 I should be able to
	 * define the two columns as primary keys, but they might violate the bag
	 * concept, so maybe not
	 * 
	 * @hibernate.set table="DataProducerAssocKeyword" cascade="none"
	 *                lazy="true"
	 * @hibernate.collection-key column="DataProducerID_FK"
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
	 * <code>Event</code>s that are associated with the
	 * <code>DataProducer</code>
	 * 
	 * @hibernate.set table="DataProducerAssocEvent" cascade="none" lazy="true"
	 * @hibernate.collection-key column="DataProducerID_FK"
	 * @hibernate.collection-many-to-many column="EventID_FK"
	 *                                    class="moos.ssds.metadata.Event"
	 * @return the <code>Collection</code> of <code>Event</code>s that are
	 *         associated with the <code>DataProducer</code>
	 */
	public Collection<Event> getEvents() {
		return events;
	}

	public void setEvents(Collection<Event> events) {
		this.events = events;
	}

	/**
	 * This method adds the given <code>Event</code> to the collection
	 * associated with the <code>DataProducer</code>.
	 * 
	 * @param event
	 *            the <code>Event</code> to add
	 */
	public void addEvent(Event event) {
		// If null was passed in, just return
		if (event == null)
			return;

		// Now add the Event to the collection
		if (!this.events.contains(event)) {
			this.events.add(event);
		}
	}

	/**
	 * This method removes the given <code>Event</code> from the collection
	 * 
	 * @param event
	 *            is the <code>Event</code> to remove from the collection
	 */
	public void removeEvent(Event event) {
		if (event == null)
			return;
		if ((this.events != null) && (this.events.contains(event))) {
			this.events.remove(event);
		}
	}

	/**
	 * This method will clear out the collection of <code>Event</code>s and keep
	 * the integrity of the relationships intact.
	 */
	public void clearEvents() {
		this.events.clear();
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
	 * This static method checks to see if the incoming dataProducerType matches
	 * one of the constants defined in the class
	 * 
	 * @param dataProducerType
	 * @return
	 */
	public static boolean isValidDataProducerType(String dataProducerType) {
		boolean result = true;
		if ((dataProducerType == null)
				|| ((!dataProducerType.equals(TYPE_DEPLOYMENT)) && (!dataProducerType
						.equals(TYPE_PROCESS_RUN)))) {
			result = false;
		}
		return result;
	}

	/**
	 * This static method checks to see if the incoming role matches one of the
	 * constants defined in the class
	 * 
	 * @param role
	 * @return
	 */
	public static boolean isValidRole(String role) {
		boolean result = true;
		if (role == null)
			return true;
		if ((!role.equals(ROLE_INSTRUMENT)) && (!role.equals(ROLE_OBSERVATORY))
				&& (!role.equals(ROLE_PLATFORM)) && (!role.equals(ROLE_SENSOR))) {
			result = false;
		}
		return result;
	}

	/**
	 * This equals method is more complex than most of the others due to the
	 * complexity of the business key. The business key for
	 * <code>DataProducer</code> is based on the data producer type being equal
	 * as well as having an output in common. If both <code>DataProducer</code>s
	 * are of the same type and have one output that they share, they are
	 * considered equal.
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
		if (!(obj instanceof DataProducer))
			return false;

		// Cast to DataProducer object
		final DataProducer that = (DataProducer) obj;

		// Now check business key which is type and output
		if (!this.dataProducerType.equals(that.getDataProducerType())) {
			return false;
		}

		// Now check for missing business keys (type and missing outputs)
		if ((this.outputs == null) || (this.outputs.size() <= 0)
				|| (that.getOutputs() == null)
				|| (that.getOutputs().size() <= 0)
				|| (this.dataProducerType == null)
				|| (that.getDataProducerType() == null)) {
			// Check for ID equality, otherwise, consider not equal
			if ((this.getId() != null) && (that.getId() != null)
					&& (this.getId().longValue() == that.getId().longValue()))
				return true;

			return false;
		}

		// Now check for hashcode equals
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

		// Create the hashcode
		int result = 9;

		// Now add the hashcode for the type
		if (dataProducerType != null)
			result = result + dataProducerType.hashCode();

		// Now the name
		// kgomes - After discussing this with Mike M, we thought name should be
		// more flexible and not be part of the alternate key so I commented
		// this out
		// if (name != null)
		// result = 7 * result + name.hashCode();

		// Now all the hashcodes of the outputs
		if ((outputs != null) && (outputs.size() > 0)) {
			Iterator<DataContainer> outputIter = outputs.iterator();
			while (outputIter.hasNext()) {
				result = result + outputIter.next().hashCode();
			}
		}

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
		sb.append("DataProducer");
		sb.append(delimiter + "id=" + this.getId());
		sb.append(delimiter + "name=" + this.getName());
		sb.append(delimiter + "description=" + this.getDescription());
		sb.append(delimiter + "dataProducerType=" + this.getDataProducerType());
		sb.append(delimiter + "startDate="
				+ xmlDateFormat.format(this.startDate));
		sb.append(delimiter + "endDate=" + xmlDateFormat.format(this.endDate));
		sb.append(delimiter + "role=" + this.getRole());
		sb.append(delimiter + "nominalLatitude=" + this.getNominalLatitude());
		sb.append(delimiter + "nominalLatitudeAccuracy="
				+ this.getNominalLatitudeAccuracy());
		sb.append(delimiter + "nominalLongitude=" + this.getNominalLongitude());
		sb.append(delimiter + "nominalLongitudeAccuracy="
				+ this.getNominalLongitudeAccuracy());
		sb.append(delimiter + "nominalDepth=" + this.getNominalDepth());
		sb.append(delimiter + "nominalDepthAccuracy="
				+ this.getNominalDepthAccuracy());
		sb.append(delimiter + "nominalBenthicAltitude="
				+ this.getNominalBenthicAltitude());
		sb.append(delimiter + "nominalBenthicAltitudeAccuracy="
				+ this.getNominalBenthicAltitudeAccuracy());
		sb.append(delimiter + "xoffset=" + this.getXoffset());
		sb.append(delimiter + "yoffset=" + this.getYoffset());
		sb.append(delimiter + "zoffset=" + this.getZoffset());
		sb.append(delimiter + "orientationDescription="
				+ this.getOrientationDescription());
		sb.append(delimiter + "x3DOrientation=" + this.getX3DOrientationText());
		sb.append(delimiter + "hostName=" + this.getHostName());
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
			} else if (key.equalsIgnoreCase("dataProducerType")) {
				this.setDataProducerType(value);
			} else if (key.equalsIgnoreCase("startDate")) {
				this.setStartDate(xmlDateFormat.parse(value));
			} else if (key.equalsIgnoreCase("endDate")) {
				this.setEndDate(xmlDateFormat.parse(value));
			} else if (key.equalsIgnoreCase("role")) {
				this.setRole(value);
			} else if (key.equalsIgnoreCase("nominalLatitude")) {
				try {
					this.setNominalLatitude(Double.valueOf(value));
				} catch (NumberFormatException e) {
					throw new MetadataException(
							"The value specified for nominalLatitude (" + value
									+ ") could not be converted to a Double: "
									+ e.getMessage());
				}
			} else if (key.equalsIgnoreCase("nominalLatitudeAccuracy")) {
				try {
					this.setNominalLatitudeAccuracy(Float.valueOf(value));
				} catch (NumberFormatException e) {
					throw new MetadataException(
							"The value specified for nominalLatitudeAccuracy ("
									+ value
									+ ") could not be converted to a Float: "
									+ e.getMessage());
				}
			} else if (key.equalsIgnoreCase("nominalLongitude")) {
				try {
					this.setNominalLongitude(Double.valueOf(value));
				} catch (NumberFormatException e) {
					throw new MetadataException(
							"The value specified for nominalLongitude ("
									+ value
									+ ") could not be converted to a Double: "
									+ e.getMessage());
				}
			} else if (key.equalsIgnoreCase("nominalLongitudeAccuracy")) {
				try {
					this.setNominalLongitudeAccuracy(Float.valueOf(value));
				} catch (NumberFormatException e) {
					throw new MetadataException(
							"The value specified for nominalLongitudeAccuracy ("
									+ value
									+ ") could not be converted to a Float: "
									+ e.getMessage());
				}
			} else if (key.equalsIgnoreCase("nominalDepth")) {
				try {
					this.setNominalDepth(Float.valueOf(value));
				} catch (NumberFormatException e) {
					throw new MetadataException(
							"The value specified for nominalDepth (" + value
									+ ") could not be converted to a Float: "
									+ e.getMessage());
				}
			} else if (key.equalsIgnoreCase("nominalDepthAccuracy")) {
				try {
					this.setNominalDepthAccuracy(Float.valueOf(value));
				} catch (NumberFormatException e) {
					throw new MetadataException(
							"The value specified for nominalDepthAccuracy ("
									+ value
									+ ") could not be converted to a Float: "
									+ e.getMessage());
				}
			} else if (key.equalsIgnoreCase("nominalBenthicAltitude")) {
				try {
					this.setNominalBenthicAltitude(Float.valueOf(value));
				} catch (NumberFormatException e) {
					throw new MetadataException(
							"The value specified for nominalBenthicAltitude ("
									+ value
									+ ") could not be converted to a Float: "
									+ e.getMessage());
				}
			} else if (key.equalsIgnoreCase("nominalBenthicAltitudeAccuracy")) {
				try {
					this
							.setNominalBenthicAltitudeAccuracy(Float
									.valueOf(value));
				} catch (NumberFormatException e) {
					throw new MetadataException(
							"The value specified for nominalBenthicAltitudeAccuracy ("
									+ value
									+ ") could not be converted to a Float: "
									+ e.getMessage());
				}
			} else if (key.equalsIgnoreCase("xoffset")) {
				try {
					this.setXoffset(Float.valueOf(value));
				} catch (NumberFormatException e) {
					throw new MetadataException(
							"The value specified for xoffset (" + value
									+ ") could not be converted to a Float: "
									+ e.getMessage());
				}
			} else if (key.equalsIgnoreCase("yoffset")) {
				try {
					this.setYoffset(Float.valueOf(value));
				} catch (NumberFormatException e) {
					throw new MetadataException(
							"The value specified for yoffset (" + value
									+ ") could not be converted to a Float: "
									+ e.getMessage());
				}
			} else if (key.equalsIgnoreCase("zoffset")) {
				try {
					this.setZoffset(Float.valueOf(value));
				} catch (NumberFormatException e) {
					throw new MetadataException(
							"The value specified for zoffset (" + value
									+ ") could not be converted to a Float: "
									+ e.getMessage());
				}
			} else if (key.equalsIgnoreCase("orientationDescription")) {
				this.setOrientationDescription(value);
			} else if (key.equalsIgnoreCase("x3DOrientationText")) {
				this.setX3DOrientationText(value);
			} else if (key.equalsIgnoreCase("hostName")) {
				this.setHostName(value);
			} else {
				throw new MetadataException("The attribute specified by " + key
						+ " is not a recognized field of "
						+ this.getClass().getName());
			}
		}
	}

	/**
	 * This is the method that allows for custom de-serialization of a
	 * <code>DataProducer</code>.
	 */
	@SuppressWarnings("unchecked")
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		childDataProducers = (Collection<DataProducer>) in.readObject();
		dataProducerGroups = (Collection<DataProducerGroup>) in.readObject();
		dataProducerType = (String) in.readObject();
		description = (String) in.readObject();
		device = (Device) in.readObject();
		endDate = (Date) in.readObject();
		events = (Collection<Event>) in.readObject();
		hostName = (String) in.readObject();
		// Read in ID
		Object idObject = in.readObject();
		if (idObject instanceof Integer) {
			Integer intId = (Integer) idObject;
			id = new Long(intId.longValue());
		} else if (idObject instanceof Long) {
			id = (Long) idObject;
		}
		inputs = (Collection<DataContainer>) in.readObject();
		keywords = (Collection<Keyword>) in.readObject();
		name = (String) in.readObject();
		nominalBenthicAltitude = (Float) in.readObject();
		nominalBenthicAltitudeAccuracy = (Float) in.readObject();
		nominalDepth = (Float) in.readObject();
		nominalDepthAccuracy = (Float) in.readObject();
		nominalLatitude = (Double) in.readObject();
		nominalLatitudeAccuracy = (Float) in.readObject();
		nominalLongitude = (Double) in.readObject();
		nominalLongitudeAccuracy = (Float) in.readObject();
		orientationDescription = (String) in.readObject();
		outputs = (Collection<DataContainer>) in.readObject();
		parentDataProducer = (DataProducer) in.readObject();
		person = (Person) in.readObject();
		resources = (Collection<Resource>) in.readObject();
		role = (String) in.readObject();
		software = (Software) in.readObject();
		startDate = (Date) in.readObject();
		// Read in the version
		Object versionObject = in.readObject();
		if (versionObject instanceof Integer) {
			Integer intVersion = (Integer) versionObject;
			version = new Long(intVersion.longValue());
		} else if (versionObject instanceof Long) {
			version = (Long) versionObject;
		}
		x3DOrientationText = (String) in.readObject();
		xoffset = (Float) in.readObject();
		yoffset = (Float) in.readObject();
		zoffset = (Float) in.readObject();
	}

	/**
	 * This is the method that allows for custom serialization of a
	 * <code>DataProducer</code>
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		// Write out the child data producers
		out.writeObject(childDataProducers);
		// Write out data producer group
		out.writeObject(dataProducerGroups);
		out.writeObject(dataProducerType);
		out.writeObject(description);
		// Write out the device
		out.writeObject(device);
		out.writeObject(endDate);
		// Write out the events
		out.writeObject(events);
		out.writeObject(hostName);
		out.writeObject(id);
		// Write out inputs
		out.writeObject(inputs);
		// Write out the keywords
		out.writeObject(keywords);
		out.writeObject(name);
		out.writeObject(nominalBenthicAltitude);
		out.writeObject(nominalBenthicAltitudeAccuracy);
		out.writeObject(nominalDepth);
		out.writeObject(nominalDepthAccuracy);
		out.writeObject(nominalLatitude);
		out.writeObject(nominalLatitudeAccuracy);
		out.writeObject(nominalLongitude);
		out.writeObject(nominalLongitudeAccuracy);
		out.writeObject(orientationDescription);
		// Write out the outputs
		out.writeObject(outputs);
		// Write parent data producer
		out.writeObject(parentDataProducer);
		// Write out the person
		out.writeObject(person);
		// Write out the resources
		out.writeObject(resources);
		out.writeObject(role);
		// Write out the software
		out.writeObject(software);
		out.writeObject(startDate);
		out.writeObject(version);
		out.writeObject(x3DOrientationText);
		out.writeObject(xoffset);
		out.writeObject(yoffset);
		out.writeObject(zoffset);
	}

	/**
	 * This method overrides the clone method to produce a copy of an object. It
	 * will clear the ID on the newly created copy to prevent odd behavior.
	 */
	protected Object clone() throws CloneNotSupportedException {
		// Create the clone
		DataProducer clone = new DataProducer();

		// Copy the fields
		try {
			clone.setId(null);
			clone.setName(this.getName());
			clone.setDescription(this.getDescription());
			clone.setDataProducerType(this.getDataProducerType());
			clone.setStartDate(this.getStartDate());
			clone.setEndDate(this.getEndDate());
			clone.setRole(this.getRole());
			clone.setNominalLatitude(this.getNominalLatitude());
			clone.setNominalLatitudeAccuracy(this.getNominalLatitudeAccuracy());
			clone.setNominalLongitude(this.getNominalLongitude());
			clone.setNominalLongitudeAccuracy(this
					.getNominalLongitudeAccuracy());
			clone.setNominalDepth(this.getNominalDepth());
			clone.setNominalDepthAccuracy(this.getNominalDepthAccuracy());
			clone.setNominalBenthicAltitude(this.getNominalBenthicAltitude());
			clone.setNominalBenthicAltitudeAccuracy(this
					.getNominalBenthicAltitudeAccuracy());
			clone.setXoffset(this.getXoffset());
			clone.setYoffset(this.getYoffset());
			clone.setZoffset(this.getZoffset());
			clone.setOrientationDescription(this.getOrientationDescription());
			clone.setX3DOrientationText(this.getX3DOrientationText());
			clone.setHostName(this.getHostName());
		} catch (MetadataException e) {
		}

		// Now return the clone
		return clone;
	}

	/**
	 * This method returns a clone of <code>DataProducer</code> but then also
	 * returns deep copies of the following relationships:
	 * <ol>
	 * <li>Person</li>
	 * <li>Device</li>
	 * <li>Software</li>
	 * <li>ChildDataProducers</li>
	 * <li>DataProducerGroups</li>
	 * <li>Inputs</li>
	 * <li>Outputs</li>
	 * <li>Resources</li>
	 * <li>Keywords</li>
	 * <li>Events</li>
	 * </ol>
	 * It does NOT make deep copies of ParentDataProducers
	 */
	public IMetadataObject deepCopy() throws CloneNotSupportedException {
		logger.debug("deepCopy called");

		// Grab the clone
		DataProducer deepClone = (DataProducer) this.clone();
		if (deepClone != null) {
			logger.debug("A clone was created and is: ");
			logger.debug(deepClone.toStringRepresentation("|"));
		} else {
			logger.debug("Clone could not be created.");
		}

		// Set the relationships
		if (this.getPerson() != null) {
			logger.debug("Going to clone person:");
			logger.debug(this.getPerson().toStringRepresentation("|"));
			deepClone.setPerson((Person) this.getPerson().deepCopy());
			logger.debug("OK, cloned person is:");
			logger.debug(deepClone.getPerson().toStringRepresentation("|"));
		} else {
			logger.debug("No person to clone");
			deepClone.setPerson(null);
		}
		if (this.getDevice() != null) {
			logger.debug("Going to clone device:");
			logger.debug(this.getDevice().toStringRepresentation("|"));
			deepClone.setDevice((Device) this.getDevice().deepCopy());
			logger.debug("OK, cloned device is:");
			logger.debug(deepClone.getDevice().toStringRepresentation("|"));
		}
		if (this.getSoftware() != null) {
			logger.debug("Going to clone software:");
			logger.debug(this.getSoftware().toStringRepresentation("|"));
			deepClone.setSoftware((Software) this.getSoftware().deepCopy());
			logger.debug("OK, cloned software is:");
			logger.debug(deepClone.getSoftware().toStringRepresentation("|"));
		}
		if ((this.getChildDataProducers() != null)
				&& (this.getChildDataProducers().size() > 0)) {
			logger.debug("There are " + this.getChildDataProducers().size()
					+ " child DataProducers to clone and attach");
			Collection<DataProducer> childDataProducersToCopy = this
					.getChildDataProducers();
			Iterator<DataProducer> childDPITer = childDataProducersToCopy
					.iterator();
			while (childDPITer.hasNext()) {
				DataProducer clonedChildDataProducer = (DataProducer) ((DataProducer) childDPITer
						.next()).deepCopy();
				logger.debug("Adding cloned child data producer:");
				logger.debug(clonedChildDataProducer
						.toStringRepresentation("|"));
				logger.debug("to cloned parent DataProducer:");
				logger.debug(deepClone.toStringRepresentation("|"));
				deepClone.addChildDataProducer(clonedChildDataProducer);
			}
		}
		if ((this.getDataProducerGroups() != null)
				&& (this.getDataProducerGroups().size() > 0)) {
			logger.debug("There are " + this.getDataProducerGroups().size()
					+ " DataProducerGroups to clone and attach");
			Collection<DataProducerGroup> dataProducerGroupToCopy = this
					.getDataProducerGroups();
			Iterator<DataProducerGroup> dpgIter = dataProducerGroupToCopy
					.iterator();
			while (dpgIter.hasNext()) {
				DataProducerGroup clonedDataProducerGroup = (DataProducerGroup) ((DataProducerGroup) dpgIter
						.next()).deepCopy();
				logger
						.debug("Will add the following cloned DataProducerGroup:");
				logger.debug(clonedDataProducerGroup
						.toStringRepresentation("|"));
				deepClone.addDataProducerGroup(clonedDataProducerGroup);
			}
		}
		if ((this.getInputs() != null) && (this.getInputs().size() > 0)) {
			logger.debug("There are " + this.getInputs().size()
					+ " Inputs to clone and attach");
			Collection<DataContainer> inputs = this.getInputs();
			Iterator<DataContainer> inputIter = inputs.iterator();
			while (inputIter.hasNext()) {
				DataContainer clonedDataContainer = (DataContainer) ((DataContainer) inputIter
						.next()).deepCopy();
				logger.debug("Will add the following cloned input:");
				logger.debug(clonedDataContainer.toStringRepresentation("|"));
				deepClone.addInput(clonedDataContainer);
			}
		}
		if ((this.getOutputs() != null) && (this.getOutputs().size() > 0)) {
			logger.debug("There are " + this.getOutputs().size()
					+ " Outputs to clone and attach");
			Collection<DataContainer> outputs = this.getOutputs();
			Iterator<DataContainer> outputIter = outputs.iterator();
			while (outputIter.hasNext()) {
				DataContainer clonedOutput = (DataContainer) ((DataContainer) outputIter
						.next()).deepCopy();
				logger.debug("Will add the following cloned output:");
				logger.debug(clonedOutput.toStringRepresentation("|"));
				deepClone.addOutput(clonedOutput);
			}
		}
		if ((this.getResources() != null) && (this.getResources().size() > 0)) {
			logger.debug("There are " + this.getResources().size()
					+ " Resources to clone and attach");
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
		if ((this.getEvents() != null) && (this.getEvents().size() > 0)) {
			logger.debug("There are " + this.getEvents().size()
					+ " Events to clone and attach");
			Collection<Event> events = this.getEvents();
			Iterator<Event> eventsIter = events.iterator();
			while (eventsIter.hasNext()) {
				Event clonedEvent = (Event) ((Event) eventsIter.next())
						.deepCopy();
				logger.debug("Will add the following cloned event:");
				logger.debug(clonedEvent.toStringRepresentation("|"));
				deepClone.addEvent(clonedEvent);
			}
		}

		// Now return the deep copy
		return deepClone;
	}
}