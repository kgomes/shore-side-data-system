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
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import moos.ssds.metadata.util.MetadataException;
import moos.ssds.metadata.util.MetadataValidator;

/**
 * This class represents a "type" of device. It is a way to create categories of
 * devices that can link similar devices together. Examples might be "AUV",
 * "GPS", "CTD", etc.
 * <hr>
 * 
 * @stereotype role
 * @hibernate.class table="DeviceType"
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.11 $
 */
public class DeviceType implements IMetadataObject, IDescription {

	/**
	 * This is a Log4JLogger that is used to log information to
	 */
	static Logger logger = Logger.getLogger(DeviceType.class);

	/**
	 * This is the version that we can control for serialization purposes
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * This is the persistence layer identifier
	 */
	private Long id;

	/**
	 * This is the name that is a unique type (category) of device
	 */
	private String name;

	/**
	 * The description of that type
	 */
	private String description;

	/**
	 * This is the hibernate version that is used to check for dirty objects
	 */
	private long version = -1;

	/**
	 * @see moos.ssds.metadata.IMetadataObject#getId()
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
	 * @see moss.ssds.metadata.IDescription#getName()
	 * @hibernate.property
	 * @hibernate.column name="name" unique="true" not-null="true" length="255"
	 *                   index="name_index"
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
	 * This method overrides the default equals method and checks for to see if
	 * the objects occupy the same memory space and if not, then it checks for
	 * identical persistent identifiers and if those are not available, it
	 * checks for equality of the business key which is the name
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
		if (!(obj instanceof DeviceType))
			return false;

		// Cast to DeviceType object
		final DeviceType that = (DeviceType) obj;

		// Now check for missing business key (name)
		if ((this.name == null) || (that.getName() == null))
			return false;

		// Now compare hashcodes
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
		// let's
		// cast it so we can get to the fields
		DeviceType incomingDeviceType = (DeviceType) obj;

		// Check the ID
		if (incomingDeviceType.getId() == null) {
			if (this.id != null)
				return false;
		} else {
			if (this.id == null)
				return false;
			if (this.id.longValue() != incomingDeviceType.getId().longValue())
				return false;
		}

		// OK, the ID's look like they are the same, let's check the name
		if (incomingDeviceType.getName() == null) {
			if (this.name != null)
				return false;
		} else {
			if (this.name == null)
				return false;
			if (!this.name.equals(incomingDeviceType.getName()))
				return false;
		}

		// OK, the names should be the same, let's look at the description
		if (incomingDeviceType.getDescription() == null) {
			if (this.description != null)
				return false;
		} else {
			if (this.description == null)
				return false;
			if (!this.description.equals(incomingDeviceType.getDescription()))
				return false;
		}

		// Now check the version numbers
		if (incomingDeviceType.getVersion() != this.version)
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

		// Build the hashcode
		int result = 19;
		if (name != null) {
			result = 29 * result + name.hashCode();
		}

		// Now return it
		return result;
	}

	/**
	 * The toString method is overridden to spit out the alternate primary key.
	 * I did this so that we will always have something meaningful whenever
	 * something tries to toString an object.
	 */
	public String toString() {
		return this.name;
	}

	/**
	 * @see IMetadataObject#toStringRepresentation(String)
	 */
	public String toStringRepresentation(String delimiter) {
		if (delimiter == null)
			delimiter = IMetadataObject.DEFAULT_DELIMITER;
		StringBuffer sb = new StringBuffer();
		sb.append("DeviceType");
		sb.append(delimiter + "id=" + this.getId());
		sb.append(delimiter + "name=" + this.getName());
		sb.append(delimiter + "description=" + this.getDescription());
		return sb.toString();
	}

	/**
	 * In order to use the class, you should first create an empty object, then
	 * call this method, passing in the string representation. As an example:
	 * 
	 * <pre>
	 * DeviceType newDeviceType = new DeviceType();
	 * 
	 * newDeviceType.setValuesFromStringRepresentation(
	 * 		&quot;DeviceType|name=CTD|description=CTD Types&quot;, &quot;|&quot;);
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
			} else {
				throw new MetadataException("The attribute specified by " + key
						+ " is not a recognized field of "
						+ this.getClass().getName());
			}
		}
	}

	/**
	 * This is the method that re-consititutes an object from a custom
	 * serialization form
	 * 
	 * @see Externalizable#readExternal(ObjectInput)
	 */
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		description = (String) in.readObject();
		// Read the first object
		Object idObject = in.readObject();
		if (idObject instanceof Integer) {
			Integer intId = (Integer) idObject;
			id = new Long(intId.longValue());
		} else if (idObject instanceof Long) {
			id = (Long) idObject;
		}
		name = (String) in.readObject();
		Object versionObject = in.readObject();
		if (versionObject instanceof Integer) {
			Integer intVersion = (Integer) versionObject;
			version = new Long(intVersion.longValue());
		} else if (versionObject instanceof Long) {
			version = (Long) versionObject;
		}
	}

	/**
	 * This is the method to do a custom serialization of a DeviceType object
	 * 
	 * @see Externalizable#writeExternal(ObjectOutput)
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(description);
		out.writeObject(id);
		out.writeObject(name);
		out.writeObject(version);
	}

	/**
	 * This method overrides the clone method to produce a copy of an object. It
	 * will clear the ID on the newly created copy to prevent odd behavior.
	 */
	protected Object clone() throws CloneNotSupportedException {
		// Create the clone
		DeviceType clone = new DeviceType();

		// Set the fields
		try {
			clone.setId(null);
			clone.setName(this.getName());
			clone.setDescription(this.getDescription());
		} catch (MetadataException e) {
		}

		// Now return the clone
		return clone;
	}

	/**
	 * This simply returns a clone of the <code>DeviceType</code>
	 */
	public IMetadataObject deepCopy() throws CloneNotSupportedException {
		DeviceType clonedDeviceType = (DeviceType) this.clone();
		logger.debug("deepCopy called and will return clone:");
		logger.debug(clonedDeviceType.toStringRepresentation("|"));
		return clonedDeviceType;
	}
}