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
 * This class give the capability to group <code>DataContainer</code>s in
 * different categories that helps with organization, linking and querying
 * <hr>
 * 
 * @stereotype union
 * @hibernate.class table="DataContainerGroup"
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.15 $
 */
public class DataContainerGroup implements IMetadataObject, IDescription {

	/**
	 * This is a Log4JLogger that is used to log information to
	 */
	static Logger logger = Logger.getLogger(DataContainerGroup.class);

	/**
	 * This is the version that we can control for serialization purposes
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * This unique persistence mechanism ID for the
	 * <code>DataProducerGroup</code>
	 */
	private Long id;

	/**
	 * A name assigned to the <code>DataProducerGroup</code>
	 */
	private String name;

	/**
	 * A description of the <code>DataProducerGroup</code>
	 */
	private String description;

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
	 * @hibernate.property
	 * @hibernate.column name="name" length="255" index="name_index"
	 *                   not-null="true" unique="true"
	 */
	public String getName() {
		return name;
	}

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
		if (!(obj instanceof DataContainerGroup))
			return false;

		// Cast to DataContainerGroup object
		final DataContainerGroup that = (DataContainerGroup) obj;

		// Now check for missing business key (name)
		if ((this.name == null) || (that.getName() == null))
			return false;

		// Not check hashcodes
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
		DataContainerGroup incomingDataContainerGroup = (DataContainerGroup) obj;

		// Check the ID
		if (incomingDataContainerGroup.getId() == null) {
			if (this.id != null)
				return false;
		} else {
			if (this.id == null)
				return false;
			if (this.id.longValue() != incomingDataContainerGroup.getId()
					.longValue())
				return false;
		}

		// OK, the ID's look like they are the same, let's check the name
		if (incomingDataContainerGroup.getName() == null) {
			if (this.name != null)
				return false;
		} else {
			if (this.name == null)
				return false;
			if (!this.name.equals(incomingDataContainerGroup.getName()))
				return false;
		}

		// OK, the names should be the same, let's look at the description
		if (incomingDataContainerGroup.getDescription() == null) {
			if (this.description != null)
				return false;
		} else {
			if (this.description == null)
				return false;
			if (!this.description.equals(incomingDataContainerGroup
					.getDescription()))
				return false;
		}

		// Now check the version numbers
		if (incomingDataContainerGroup.getVersion() != this.version)
			return false;

		// Return true
		return true;
	}

	/**
	 * @see IMetadataObject#hashCode()
	 */
	public int hashCode() {
		// Calculate hashcode
		int result = 3;
		if (name != null)
			result = result + name.hashCode();

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
		sb.append("DataContainerGroup");
		sb.append(delimiter + "id=" + this.getId());
		sb.append(delimiter + "name=" + this.getName());
		sb.append(delimiter + "description=" + this.getDescription());
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
			} else {
				throw new MetadataException("The attribute specified by " + key
						+ " is not a recognized field of "
						+ this.getClass().getName());
			}
		}
	}

	/**
	 * The method to re-constitute the object from a custom serialized form
	 * 
	 * @see Externalizable#readExternal(ObjectInput)
	 */
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
	 * The method to do a custom serialization of the DataContainerGroup
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
		DataContainerGroup clone = new DataContainerGroup();

		// Copy the fields
		clone.setId(null);
		try {
			clone.setName(this.getName());
			clone.setDescription(this.getDescription());
		} catch (MetadataException e) {
		}

		// Now return it
		return clone;
	}

	/**
	 * This method simply returns a clone of the <code>DataContainerGroup</code>
	 * with its ID cleared.
	 */
	public IMetadataObject deepCopy() throws CloneNotSupportedException {
		DataContainerGroup clonedDataContainerGroup = (DataContainerGroup) this
				.clone();
		logger.debug("deepCopy called and will return clone:");
		logger.debug(clonedDataContainerGroup.toStringRepresentation("|"));
		return clonedDataContainerGroup;
	}

}
