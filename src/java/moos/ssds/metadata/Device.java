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
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;

import moos.ssds.metadata.util.MetadataException;
import moos.ssds.metadata.util.MetadataValidator;

import org.apache.log4j.Logger;
import org.doomdark.uuid.UUID;
import org.doomdark.uuid.UUIDGenerator;

/**
 * This class represents a device in the system.
 * <hr>
 * 
 * @stereotype thing
 * @hibernate.class table="Device"
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.20 $
 */
public class Device implements IMetadataObject, IDescription, IResourceOwner {

	/**
	 * The default constructor for the Device object. NOTE: This constructor
	 * will automatically generate a UUID. If you know the UUID, please use the
	 * constructor that takes in the UUID.
	 */
	public Device() {
		description = "";
		this.generateOwnUuid();
	}

	/**
	 * This is the constructor that takes in a <code>String</code> that is a
	 * representation of a UUID.
	 * 
	 * @param uuid
	 *            the <code>String</code> to use for the UUID
	 * @throws MetadataException
	 *             if the incoming <code>String</code> is null or is not a valid
	 *             UUID.
	 */
	public Device(String uuid) throws MetadataException {
		this.setUuid(uuid);
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
	 * This method returns the unique identifier that is assigned to the
	 * <code>Device</code>
	 * 
	 * @hibernate.property
	 * @hibernate.column name="uuid" unique="true" not-null="true"
	 *                   index="uuid_index"
	 */
	public String getUuid() {
		return uuid;
	}

	/**
	 * This method sets the unique identifier for the <code>Device</code> by
	 * passing in the string representation of it.
	 * 
	 * @param uuidString
	 *            is the UUID in string form
	 * @throws MetadataException
	 *             if the uuid is not valid
	 */
	public void setUuid(String uuidString) throws MetadataException {
		if ((uuidString == null) || (uuidString.equals("")))
			throw new MetadataException("uuidString cannot be null");
		UUID uuidTemp = null;
		try {
			uuidTemp = UUID.valueOf(uuidString);
		} catch (NumberFormatException e) {
			throw new MetadataException("Could not convert " + uuidString
					+ " to a valid UUID: " + e.getMessage());
		}
		if ((uuidTemp != null)) {
			this.uuid = uuidString;
		} else {
			throw new MetadataException("Could not convert " + uuidString
					+ " to a UUID");
		}
	}

	/**
	 * This is a helper method to return the UUID in it's byte array form
	 * 
	 * @return a <code>byte</code> array that contains the UUID in byte form. If
	 *         the UUID has not been defined, null is returned
	 */
	public byte[] getUuidAsBytes() {
		byte[] bytesToReturn = null;
		if (this.getUuid() != null) {
			try {
				bytesToReturn = UUID.valueOf(this.uuid).toByteArray();
			} catch (NumberFormatException e) {
				bytesToReturn = null;
			}
		}
		return bytesToReturn;
	}

	/**
	 * This is a helper method to set <code>Device</code> UUID by passing in an
	 * array of bytes
	 * 
	 * @param uuidBytes
	 *            the byte array form of the UUID
	 * @throws MetadataException
	 *             if the bytes are not a valid UUID
	 */
	public void setUuidAsBytes(byte[] uuidBytes) {

		UUID uuidTemp = new UUID(uuidBytes);
		if (uuidTemp != null) {
			this.uuid = uuidTemp.toString();
		}
	}

	/**
	 * This method allows the device to generate its own valid UUID. If the UUID
	 * is already defined, nothing happens.
	 * 
	 * @return <code>true</code> if a UUID was created, <code>false</code> if
	 *         not
	 */
	public boolean generateOwnUuid() {
		// A flag to indicate if one was generated or not
		boolean uuidCreated = false;

		// If the current uuid is not null, see if it can be converted to a UUID
		// object, if not, it will be created in the next step
		UUID uuidTemp = null;
		if (this.getUuid() != null) {
			try {
				uuidTemp = UUID.valueOf(this.getUuid());
			} catch (NumberFormatException e) {
				uuidTemp = null;
			}
		}
		// If there is no current UUID, generate one
		if (uuidTemp == null) {
			UUIDGenerator uuidg = UUIDGenerator.getInstance();
			uuidTemp = uuidg.generateTimeBasedUUID();
			this.uuid = uuidTemp.toString();
			uuidCreated = true;
		} else {
			return false;
		}
		return uuidCreated;
	}

	/**
	 * @see IDescription#getName()
	 * @hibernate.property
	 * @hibernate.column name="name" length="255" index="name_index"
	 */
	public String getName() {
		return name;
	}

	/**
	 * @see IDescription#setName(String)
	 */
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
	 * These methods get and set the name of the manufacturer of the
	 * <code>Device</code>
	 * 
	 * @hibernate.property
	 * @return the name of the manufacturer of the <code>Device</code>
	 */
	public String getMfgName() {
		return mfgName;
	}

	public void setMfgName(String mfgName) {
		this.mfgName = mfgName;
	}

	/**
	 * These methods get and set the model designation that is assigned to the
	 * <code>Device</code> by the manfucturer
	 * 
	 * @hibernate.property
	 * @return the model assigned to the <code>Device</code> by the manufacturer
	 */
	public String getMfgModel() {
		return mfgModel;
	}

	public void setMfgModel(String mfgModel) {
		this.mfgModel = mfgModel;
	}

	/**
	 * These methods get and set the serial number assigned to the
	 * <code>Device</code> by the manufacturer
	 * 
	 * @hibernate.property
	 * @return the serial number assigned by the manufacturer
	 */
	public String getMfgSerialNumber() {
		return mfgSerialNumber;
	}

	public void setMfgSerialNumber(String mfgSerialNumber) {
		this.mfgSerialNumber = mfgSerialNumber;
	}

	/**
	 * These methods get and set the string that is a list of URLs where
	 * information about this <code>Device</code> can be found
	 * 
	 * @hibernate.property length="2048"
	 * @return a <code>String</code> that is a listing of URLs where information
	 *         about this device can be found.
	 */
	public String getInfoUrlList() {
		return infoUrlList;
	}

	public void setInfoUrlList(String infoUrlList) throws MetadataException {
		MetadataValidator.isStringShorterThan(infoUrlList, 2048);
		this.infoUrlList = infoUrlList;
	}

	/**
	 * These methods get and set the <code>Person</code> that is usually seen as
	 * the owner of the <code>Device</code> (or point of contact).
	 * 
	 * @hibernate.many-to-one class="moos.ssds.metadata.Person"
	 *                        column="PersonID_FK"
	 *                        foreign-key="Device_Owned_By_Person"
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
	 * These methods get and set the <code>DeviceType</code> that is associated
	 * with a <code>Device</code>.
	 * 
	 * @hibernate.many-to-one class="moos.ssds.metadata.DeviceType"
	 *                        column="DeviceTypeID_FK"
	 *                        foreign-key="Device_Is_Of_DeviceType"
	 *                        cascade="none" lazy="false" outer-join="true"
	 * @return the <code>DeviceType</code> that is associated with this device.
	 *         Returns null if no type has been defined.
	 */
	public DeviceType getDeviceType() {
		return this.deviceType;
	}

	public void setDeviceType(DeviceType deviceType) {
		this.deviceType = deviceType;
	}

	/**
	 * These methods get and set the <code>Collection</code> of
	 * <code>Resource</code>s that are associated with the <code>Device</code>
	 * 
	 * @hibernate.set table="DeviceAssocResource" cascade="none" lazy="true"
	 * @hibernate.collection-key column="DeviceID_FK"
	 * @hibernate.collection-many-to-many column="ResourceID_FK"
	 *                                    class="moos.ssds.metadata.Resource"
	 * @return the <code>Collection</code> of <code>Resource</code>s that are
	 *         associated with the <code>Device</code>
	 */
	public Collection getResources() {
		return resources;
	}

	public void setResources(Collection resources) {
		this.resources = resources;
	}

	/**
	 * This method add the given <code>Resource</code> to the collection
	 * associated with the <code>Device</code>.
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
	 * @see IMetadataObject#toStringRepresentation(String)
	 */
	public String toStringRepresentation(String delimiter) {
		// If the delimiter is not specified, use a default one
		if (delimiter == null)
			delimiter = IMetadataObject.DEFAULT_DELIMITER;

		StringBuffer sb = new StringBuffer();
		sb.append("Device");
		sb.append(delimiter + "id=" + this.getId());
		sb.append(delimiter + "uuid=" + this.getUuid());
		sb.append(delimiter + "name=" + this.getName());
		sb.append(delimiter + "description=" + this.getDescription());
		sb.append(delimiter + "mfgName=" + this.getMfgName());
		sb.append(delimiter + "mfgModel=" + this.getMfgModel());
		sb.append(delimiter + "mfgSerialNumber=" + this.getMfgSerialNumber());
		sb.append(delimiter + "infoUrlList=" + this.getInfoUrlList());
		return sb.toString();
	}

	/**
	 * In order to use the class, you should first create an empty object, then
	 * call this method, passing in the string representation. As an example:
	 * 
	 * <pre>
	 * Device newDevice = new Device();
	 * 
	 * newDevice
	 * 		.setValuesFromStringRepresentation(
	 * 				&quot;Device|name=CTD|description=A really cool CTD|mfgName=MBARI|mfgModel=CTD001|mfgSerialNumber=THX-1138&quot;,
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
			} else if (key.equalsIgnoreCase("uuid")) {
				this.setUuid(value);
			} else if (key.equalsIgnoreCase("name")) {
				this.setName(value);
			} else if (key.equalsIgnoreCase("description")) {
				this.setDescription(value);
			} else if (key.equalsIgnoreCase("mfgName")) {
				this.setMfgName(value);
			} else if (key.equalsIgnoreCase("mfgModel")) {
				this.setMfgModel(value);
			} else if (key.equalsIgnoreCase("mfgSerialNumber")) {
				this.setMfgSerialNumber(value);
			} else if (key.equalsIgnoreCase("infoUrlList")) {
				this.setInfoUrlList(value);
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
	 * checks for equality of the business key which is the UUID
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
		if (!(obj instanceof Device))
			return false;

		// Cast to Device object
		final Device that = (Device) obj;

		// Now check for missing business key (UUID)
		if ((this.uuid == null) || (that.getUuid() == null))
			return false;

		// Now check hashcodes
		if (this.hashCode() == that.hashCode()) {
			return true;
		} else {
			return false;
		}
	}

	public boolean equalsCompletely(Object obj) {
		// First check to make sure it passes the basic equals test. If it does
		// not, go ahead and return false
		if (!this.equals(obj))
			return false;

		// Since we past the equals test, we know it is the right class, so
		// let's cast it so we can get to the fields
		Device incomingDevice = (Device) obj;

		// Check the ID
		if (incomingDevice.getId() == null) {
			if (this.id != null)
				return false;
		} else {
			if (this.id == null)
				return false;
			if (this.id.longValue() != incomingDevice.getId().longValue())
				return false;
		}

		// Check the UUID
		if (incomingDevice.getUuid() == null) {
			if (this.uuid != null)
				return false;
		} else {
			if (this.uuid == null)
				return false;
			if (!this.uuid.equals(incomingDevice.getUuid()))
				return false;
		}

		// Check the name
		if (incomingDevice.getName() == null) {
			if (this.name != null)
				return false;
		} else {
			if (this.name == null)
				return false;
			if (!this.name.equals(incomingDevice.getName()))
				return false;
		}

		// Check the description
		if (incomingDevice.getDescription() == null) {
			if (this.description != null)
				return false;
		} else {
			if (this.description == null)
				return false;
			if (!this.description.equals(incomingDevice.getDescription()))
				return false;
		}

		// Check the mfgName
		if (incomingDevice.getMfgName() == null) {
			if (this.mfgName != null)
				return false;
		} else {
			if (this.mfgName == null)
				return false;
			if (!this.mfgName.equals(incomingDevice.getMfgName()))
				return false;
		}

		// Check the mfgModel
		if (incomingDevice.getMfgModel() == null) {
			if (this.mfgModel != null)
				return false;
		} else {
			if (this.mfgModel == null)
				return false;
			if (!this.mfgModel.equals(incomingDevice.getMfgModel()))
				return false;
		}

		// Check the mfgSerialNumber
		if (incomingDevice.getMfgSerialNumber() == null) {
			if (this.mfgSerialNumber != null)
				return false;
		} else {
			if (this.mfgSerialNumber == null)
				return false;
			if (!this.mfgSerialNumber.equals(incomingDevice
					.getMfgSerialNumber()))
				return false;
		}

		// Check the infoUrlList
		if (incomingDevice.getInfoUrlList() == null) {
			if (this.infoUrlList != null)
				return false;
		} else {
			if (this.infoUrlList == null)
				return false;
			if (!this.infoUrlList.equals(incomingDevice.getInfoUrlList()))
				return false;
		}

		// Now check the version numbers
		if (incomingDevice.getVersion() != this.version)
			return false;

		// Return true
		return true;
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
		int result = 11;

		if (uuid != null) {
			result = 31 * result + uuid.hashCode();
		}

		// Return it
		return result;
	}

	/**
	 * This is the method that re-contitutes and object from a custom
	 * serialization format
	 * 
	 * @see Externalizable#readExternal(ObjectInput)
	 */
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		// Read the first object
		Object idObject = in.readObject();
		if (idObject instanceof Integer) {
			Integer intId = (Integer) idObject;
			id = new Long(intId.longValue());
		} else if (idObject instanceof Long) {
			id = (Long) idObject;
		}
		uuid = (String) in.readObject();
		name = (String) in.readObject();
		description = (String) in.readObject();
		mfgName = (String) in.readObject();
		mfgModel = (String) in.readObject();
		mfgSerialNumber = (String) in.readObject();
		infoUrlList = (String) in.readObject();
	}

	/**
	 * This is the method to do a custom serialization of a Device
	 * 
	 * @see Externalizable#writeExternal(ObjectOutput)
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(id);
		out.writeObject(uuid);
		out.writeObject(name);
		out.writeObject(description);
		out.writeObject(mfgName);
		out.writeObject(mfgModel);
		out.writeObject(mfgSerialNumber);
		out.writeObject(infoUrlList);
	}

	/**
	 * This method overrides the clone method to produce a copy of an object. It
	 * will clear the ID on the newly created copy to prevent odd behavior.
	 */
	protected Object clone() throws CloneNotSupportedException {
		// Create the clone
		Device clone = new Device();

		// Set the fields
		try {
			clone.setId(null);
			clone.setUuid(this.getUuid());
			clone.setName(this.getName());
			clone.setDescription(this.getDescription());
			clone.setMfgName(this.getMfgName());
			clone.setMfgModel(this.getMfgModel());
			clone.setMfgSerialNumber(this.getMfgSerialNumber());
			clone.setInfoUrlList(this.getInfoUrlList());
		} catch (MetadataException e) {
		}

		// Return the clone
		return clone;
	}

	/**
	 * This method returns a clone of the Device with a deep copy of the
	 * following relationships:
	 * <ol>
	 * <li>Person</li>
	 * <li>DeviceType</li>
	 * <li>Resources</li>
	 * </ol>
	 */
	public IMetadataObject deepCopy() throws CloneNotSupportedException {
		logger.debug("deepCopy called.");
		// Grab the clone
		Device deepClone = (Device) this.clone();
		if (deepClone != null) {
			logger.debug("Device clone is:");
			logger.debug(deepClone.toStringRepresentation("|"));
		}

		// Set the relationships
		if (this.getPerson() != null) {
			logger.debug("Will deep copy person");
			logger.debug(this.getPerson().toStringRepresentation("|"));
			deepClone.setPerson((Person) this.getPerson().deepCopy());
			if (deepClone.getPerson() != null) {
				logger.debug("OK, clone is:");
				logger.debug(deepClone.getPerson().toStringRepresentation("|"));
			} else {
				logger.debug("No clone was set!");
			}
		} else {
			deepClone.setPerson(null);
		}
		if (this.getDeviceType() != null) {
			logger.debug("DeviceType will be copied:");
			logger.debug(this.getDeviceType().toStringRepresentation("|"));
			deepClone.setDeviceType((DeviceType) this.getDeviceType()
					.deepCopy());
			if (deepClone.getDeviceType() != null) {
				logger.debug("Cloned DeviceType is: ");
				logger.debug(deepClone.getDeviceType().toStringRepresentation(
						"|"));
			} else {
				logger.debug("Looks like deep copy of the "
						+ "DeviceType did not take");
			}
		}
		if ((this.getResources() != null) && (this.getResources().size() > 0)) {
			logger.debug("Look like there are " + this.getResources().size()
					+ " resource to clone as well.");
			Collection resourcesToCopy = this.getResources();
			Iterator resourceIter = resourcesToCopy.iterator();
			while (resourceIter.hasNext()) {
				Resource resourceToClone = (Resource) resourceIter.next();
				logger.debug("Will clone resource:");
				logger.debug(resourceToClone.toStringRepresentation("|"));
				deepClone.addResource((Resource) resourceToClone.deepCopy());
			}
		}

		// Now return the deep clone
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
	 * This is a UUID that is assigned to the device
	 */
	private String uuid = null;

	/**
	 * This is the name of the device
	 */
	private String name;

	/**
	 * This is the description of the device
	 */
	private String description;

	/**
	 * This is the name of the manufacturer of the device
	 */
	private String mfgName;

	/**
	 * This is the manufacturer's model designator for the device
	 */
	private String mfgModel;

	/**
	 * This is the manufacturer's serial number assigned to the device
	 */
	private String mfgSerialNumber;

	/**
	 * This is a listing of URLs where information about this device can be
	 * found
	 */
	private String infoUrlList;

	/**
	 * This is the hibernate version that is used to check for dirty objects
	 */
	private long version = -1;

	/**
	 * This is the <code>Person</code> that is normally thought of as the owner
	 * of the device
	 * 
	 * @directed true
	 * @label lazy
	 */
	private Person person;

	/**
	 * This is the <code>DeviceType</code> that this <code>Device</code> is
	 * categorized as
	 * 
	 * @directed true
	 * @label unlazy
	 */
	private DeviceType deviceType;

	/**
	 * This is the <code>Collection</code> of <code>Resource</code>s that are
	 * associated with the <code>Device</code>
	 * 
	 * @associates Resource
	 * @directed true
	 * @label lazy
	 */
	private Collection resources = new HashSet();

	/**
	 * This is a Log4JLogger that is used to log information to
	 */
	static Logger logger = Logger.getLogger(Device.class);

}
