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
 * This class allows users (<code>Person</code>s) to be group for permission
 * issues.
 * 
 * @stereotype group
 * @hibernate.class table="UserGroup"
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.7 $
 */
public class UserGroup implements IMetadataObject {

	/**
	 * This is a Log4JLogger that is used to log information to
	 */
	static Logger logger = Logger.getLogger(UserGroup.class);

	/**
	 * This is the <code>serialVersionUID</code> that is fixed to control
	 * serialization versions of the class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * This is the persistence layer identifier. It is used by the persistence
	 * layer to identify which object in the data store corresponds to the
	 * object.
	 */
	private Long id;

	/**
	 * This is the name of the user group to sort permissions for users
	 */
	private String groupName = null;

	/**
	 * This is the hibernate version that is used to check for dirty objects
	 */
	private long version = -1;

	/**
	 * @see IMetadataObject#getId()
	 * @see moos.ssds.metadata.IMetadataObject#getId()
	 * @hibernate.id generator-class="identity" type="long"
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @see IMetadataObject#setId(java.lang.Long)
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * These methods get and set the name of the group
	 * 
	 * @hibernate.property unique="true" not-null="true" length="255"
	 *                     index="groupName_index"
	 * @return a <code>java.lang.String</code> that is the group's name
	 */
	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) throws MetadataException {
		MetadataValidator.isObjectNull(groupName);
		MetadataValidator.isStringShorterThan(groupName, 50);
		this.groupName = groupName;
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
		if (!(obj instanceof UserGroup))
			return false;

		// Cast to UserGroup object
		final UserGroup that = (UserGroup) obj;

		// Now check for missing business key (groupName)
		if ((this.groupName == null) || (that.getGroupName() == null))
			return false;

		// Now return the equality of hashcode (business key)
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
		UserGroup incomingUserGroup = (UserGroup) obj;

		// Check the ID
		if (incomingUserGroup.getId() == null) {
			if (this.id != null)
				return false;
		} else {
			if (this.id == null)
				return false;
			if (this.id.longValue() != incomingUserGroup.getId().longValue())
				return false;
		}

		// OK, the ID's look like they are the same, let's check the name
		if (incomingUserGroup.getGroupName() == null) {
			if (this.groupName != null)
				return false;
		} else {
			if (this.groupName == null)
				return false;
			if (!this.groupName.equals(incomingUserGroup.getGroupName()))
				return false;
		}

		// Now check the version numbers
		if (incomingUserGroup.getVersion() != this.version)
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
		// Calculate hashcode
		int result = 31;
		if (groupName != null) {
			result = 14 + groupName.hashCode();
		}

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

		// Create the string buffer and add all the appropriate attributes
		StringBuffer sb = new StringBuffer();
		sb.append("UserGroup");
		sb.append(delimiter + "id=" + this.getId());
		sb.append(delimiter + "groupName=" + this.getGroupName());

		// Now return it
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
			// Grab the next pari
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
			} else if (key.equalsIgnoreCase("groupName")) {
				this.setGroupName(value);
			} else {
				throw new MetadataException("The attribute specified by " + key
						+ " is not a recognized field of "
						+ this.getClass().getName());
			}
		}
	}

	/**
	 * This is the method to re-constitute and object from a custom
	 * serialization form
	 * 
	 * @see Externalizable#readExternal(ObjectInput)
	 */
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		groupName = (String) in.readObject();
		// Read in ID
		Object idObject = in.readObject();
		if (idObject instanceof Integer) {
			Integer intId = (Integer) idObject;
			id = new Long(intId.longValue());
		} else if (idObject instanceof Long) {
			id = (Long) idObject;
		}
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
	 * This is the method to serialize a UserGroup to a custom serialized form
	 * 
	 * @see Externalizable#writeExternal(ObjectOutput)
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(groupName);
		out.writeObject(id);
		out.writeObject(version);
	}

	/**
	 * This method overrides the clone method to produce a copy of an object. It
	 * will clear the ID on the newly created copy to prevent odd behavior.
	 */
	protected Object clone() throws CloneNotSupportedException {
		// Create the clone
		UserGroup clone = new UserGroup();

		// Set the fields
		try {
			clone.setId(null);
			clone.setGroupName(this.getGroupName());
		} catch (MetadataException e) {
		}

		// Return the clone
		return clone;
	}

	/**
	 * This simply returns the clone
	 */
	public IMetadataObject deepCopy() throws CloneNotSupportedException {
		UserGroup clonedUserGroup = (UserGroup) this.clone();
		logger.debug("deepCopy called and will return:");
		logger.debug(clonedUserGroup.toStringRepresentation("|"));
		return clonedUserGroup;
	}
}
