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

import org.apache.log4j.Logger;

import moos.ssds.metadata.util.MetadataException;
import moos.ssds.metadata.util.MetadataValidator;

/**
 * This class represents a person who is an actor or user of the system.
 * <hr>
 * 
 * @stereotype party
 * @hibernate.class table="Person"
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.14 $
 */
public class Person implements IMetadataObject {

	/**
	 * @see IMetadataObject#getId()
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
	 * These methods get and set the firstname of the person
	 * 
	 * @hibernate.property length="50"
	 * @return a <code>java.lang.String</code> that is the person's firstname
	 */
	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) throws MetadataException {
		MetadataValidator.isStringShorterThan(firstname, 50);
		this.firstname = firstname;
	}

	/**
	 * These methods get and set the surname (or lastname) of the person
	 * 
	 * @hibernate.property length="50"
	 * @return a <code>java.lang.String</code> that is the lastname of the
	 *         person
	 */
	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) throws MetadataException {
		MetadataValidator.isStringShorterThan(surname, 50);
		this.surname = surname;
	}

	/**
	 * These methods get and set a <code>java.lang.String</code> that represents
	 * the organization that the person is affiliated with in respect to the
	 * system. Often this is the name of the organization.
	 * 
	 * @hibernate.property length="50"
	 * @return a <code>java.lang.String</code> that is the identifier of the
	 *         person's organization
	 */
	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) throws MetadataException {
		MetadataValidator.isStringShorterThan(organization, 50);
		this.organization = organization;
	}

	/**
	 * These methods get and set a person's username in the system. This value
	 * cannot be null and must be unique in the system.
	 * 
	 * @hibernate.property
	 * @hibernate.column name="username" unique="true" not-null="true"
	 *                   length="50" index="username_index"
	 * @return a <code>java.lang.String</code> that is the person's username in
	 *         the system.
	 */
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) throws MetadataException {
		if ((username == null) || (username.equals("")))
			throw new MetadataException("username cannot be empty");
		MetadataValidator.isStringShorterThan(username, 50);
		this.username = username;
	}

	/**
	 * These methods get and set the password that is associated with the
	 * person's username. NOTE: If a <code>Person</code> object has been
	 * obtained through any mechanism that would send it over serialization
	 * (transfer between JVM's, serialized to disk, etc.) the will return null
	 * as the password is marked as a transient field for security reasons.
	 * 
	 * @hibernate.property length="50"
	 * @return a <code>java.lang.String</code> that is the password for the
	 *         person.
	 */
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) throws MetadataException {
		MetadataValidator.isStringShorterThan(password, 50);
		this.password = password;
	}

	/**
	 * These methods get and set the email address of the person
	 * 
	 * @hibernate.property
	 * @hibernate.column name="email" length="50" index="email_index"
	 * @return a <code>java.lang.String</code> that is the email address of the
	 *         person
	 */
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) throws MetadataException {
		MetadataValidator.isStringShorterThan(email, 50);
		this.email = email;
	}

	/**
	 * These methods get and set the phone number of the person
	 * 
	 * @hibernate.property length="50"
	 * @return a <code>String</code> that is the phone number of the person
	 */
	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) throws MetadataException {
		MetadataValidator.isStringShorterThan(phone, 50);
		this.phone = phone;
	}

	/**
	 * These methods get and set the first line of the address for the person
	 * 
	 * @hibernate.property length="255"
	 * @return a <code>String</code> that is the first line of the address of
	 *         the person
	 */
	public String getAddress1() {
		return address1;
	}

	public void setAddress1(String address1) throws MetadataException {
		MetadataValidator.isStringShorterThan(address1, 255);
		this.address1 = address1;
	}

	/**
	 * These methods get and set the second line of the person's address
	 * 
	 * @hibernate.property length="255"
	 * @return is a <code>String</code> that is the second line of the person's
	 *         address
	 */
	public String getAddress2() {
		return address2;
	}

	public void setAddress2(String address2) throws MetadataException {
		MetadataValidator.isStringShorterThan(address2, 255);
		this.address2 = address2;
	}

	/**
	 * These methods get and set the city that the person is located in
	 * 
	 * @hibernate.property length="50"
	 * @return is a <code>String</code> that is the city the person is located
	 *         in
	 */
	public String getCity() {
		return city;
	}

	public void setCity(String city) throws MetadataException {
		MetadataValidator.isStringShorterThan(city, 50);
		this.city = city;
	}

	/**
	 * These methods get and set the state the user is located in
	 * 
	 * @hibernate.property length="50"
	 * @return is a <code>String</code> that is the state the person is located
	 *         in
	 */
	public String getState() {
		return state;
	}

	public void setState(String state) throws MetadataException {
		MetadataValidator.isStringShorterThan(state, 50);
		this.state = state;
	}

	/**
	 * These methods get and set the Zipcode of the person's location
	 * 
	 * @hibernate.property length="50"
	 * @return is a <code>String</code> that is the user's zipcode
	 */
	public String getZipcode() {
		return zipcode;
	}

	public void setZipcode(String zipcode) throws MetadataException {
		MetadataValidator.isStringShorterThan(zipcode, 50);
		this.zipcode = zipcode;
	}

	/**
	 * These methods get and set the status of the person. This is just a text
	 * field, but it usually represents the status of the person's user account
	 * relative to the system. It is often something like "active" or "inactive"
	 * 
	 * @hibernate.property length="50"
	 * @return a <code>java.lang.String</code> that is status of the person with
	 *         respect to the system
	 */
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) throws MetadataException {
		// Filter out any case issues
		if (status == null) {
			this.status = null;
			return;
		}
		if (status.equalsIgnoreCase(STATUS_ACTIVE))
			status = STATUS_ACTIVE;
		if (status.equalsIgnoreCase(STATUS_INACTIVE))
			status = STATUS_INACTIVE;
		if (!isValidStatus(status))
			throw new MetadataException("The status specified (" + status
					+ ") is not valid");
		this.status = status;
	}

	/**
	 * This static method checks to see if the incoming status matches one of
	 * the constants defined in the class
	 * 
	 * @param status
	 * @return
	 */
	public static boolean isValidStatus(String status) {
		boolean result = true;
		if ((status == null)
				|| ((!status.equals(STATUS_ACTIVE)) && (!status
						.equals(STATUS_INACTIVE)))) {
			result = false;
		}
		return result;
	}

	/**
	 * These methods get and set the <code>Collection</code> of
	 * <code>UserGroup</code>s that are associated with the <code>Person</code>
	 * 
	 * @hibernate.set table="PersonAssocUserGroup" cascade="none" lazy="true"
	 * @hibernate.collection-key column="PersonID_FK"
	 * @hibernate.collection-many-to-many column="UserGroupID_FK"
	 *                                    class="moos.ssds.metadata.UserGroup"
	 * @return the <code>Collection</code> of <code>UserGroup</code>s that are
	 *         associated with the <code>Person</code>
	 */
	public Collection getUserGroups() {
		return userGroups;
	}

	public void setUserGroups(Collection userGroups) {
		this.userGroups = userGroups;
	}

	/**
	 * This method add the given <code>UserGroup</code> to the collection
	 * associated with the <code>Person</code>.
	 * 
	 * @param userGroup
	 *            the <code>UserGroup</code> to add
	 */
	public void addUserGroup(UserGroup userGroup) {
		// If null was passed in, just return
		if (userGroup == null)
			return;

		// Now add the UserGroup to the collection
		if (!this.userGroups.contains(userGroup)) {
			this.userGroups.add(userGroup);
		}
	}

	/**
	 * This method removes the given <code>UserGroup</code> from the collection
	 * 
	 * @param userGroup
	 *            is the <code>UserGroup</code> to remove from the collection
	 */
	public void removeUserGroup(UserGroup userGroup) {
		if (userGroup == null)
			return;
		if ((this.userGroups != null) && (this.userGroups.contains(userGroup))) {
			this.userGroups.remove(userGroup);
		}
	}

	/**
	 * This method will clear out the collection of <code>UserGroup</code>s and
	 * keep the integrity of the relationships intact.
	 */
	public void clearUserGroups() {
		this.userGroups.clear();
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
	 * The toString method is overridden to spit out the alternate primary key.
	 * I did this so that we will always have something meaningful whenever
	 * something tries to toString an object.
	 */
	public String toString() {
		return this.username;
	}

	/**
	 * Note that with this method, the person's password is not sent out. This
	 * is consistent as the class is implemented so that the password is marked
	 * as transient and will not be transferred anytime serialization of the
	 * object occurs.
	 * 
	 * @see IMetadataObject#toStringRepresentation(String)
	 */
	public String toStringRepresentation(String delimiter) {
		// If the delimiter is not specified, use a default one
		if (delimiter == null)
			delimiter = IMetadataObject.DEFAULT_DELIMITER;

		// Create the string buffer and add all the appropriate attributes
		StringBuffer sb = new StringBuffer();
		sb.append("Person");
		sb.append(delimiter + "id=" + this.getId());
		sb.append(delimiter + "firstname=" + this.getFirstname());
		sb.append(delimiter + "surname=" + this.getSurname());
		sb.append(delimiter + "organization=" + this.getOrganization());
		sb.append(delimiter + "username=" + this.getUsername());
		sb.append(delimiter + "email=" + this.getEmail());
		sb.append(delimiter + "phone=" + this.getPhone());
		sb.append(delimiter + "address1=" + this.getAddress1());
		sb.append(delimiter + "address2=" + this.getAddress2());
		sb.append(delimiter + "city=" + this.getCity());
		sb.append(delimiter + "state=" + this.getState());
		sb.append(delimiter + "zipcode=" + this.getZipcode());
		sb.append(delimiter + "status=" + this.getStatus());

		// Now return it
		return sb.toString();
	}

	/**
	 * Note that with this method, if the person's password sent in, it will not
	 * be deserialized into the object. This is consistent as the class is
	 * implemented so that the password is marked as transient and will not be
	 * transferred anytime serialization of the object occurs. In order to use
	 * the class, you should first create an empty object, then call this
	 * method, passing in the string representation. As an example:
	 * 
	 * <pre>
	 * Person newPerson = new Person();
	 * 
	 * newPerson.setValuesFromStringRepresentation(
	 * 		&quot;Person|firstname=John|surname=Doe|organization=MBARI&quot;, &quot;|&quot;);
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
			} else if (key.equalsIgnoreCase("firstname")) {
				this.setFirstname(value);
			} else if (key.equalsIgnoreCase("surname")) {
				this.setSurname(value);
			} else if (key.equalsIgnoreCase("organization")) {
				this.setOrganization(value);
			} else if (key.equalsIgnoreCase("username")) {
				this.setUsername(value);
			} else if (key.equalsIgnoreCase("password")) {
				this.setPassword(value);
			} else if (key.equalsIgnoreCase("email")) {
				this.setEmail(value);
			} else if (key.equalsIgnoreCase("phone")) {
				this.setPhone(value);
			} else if (key.equalsIgnoreCase("address1")) {
				this.setAddress1(value);
			} else if (key.equalsIgnoreCase("address2")) {
				this.setAddress2(value);
			} else if (key.equalsIgnoreCase("city")) {
				this.setCity(value);
			} else if (key.equalsIgnoreCase("state")) {
				this.setState(value);
			} else if (key.equalsIgnoreCase("zipcode")) {
				this.setZipcode(value);
			} else if (key.equalsIgnoreCase("status")) {
				this.setStatus(value);
			} else {
				throw new MetadataException("The attribute specified by " + key
						+ " is not a recognized field of "
						+ this.getClass().getName());
			}
		}
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
		if (!(obj instanceof Person))
			return false;

		// Cast to Person object
		final Person that = (Person) obj;

		// Now check for missing business key (username)
		if ((this.username == null) || (that.getUsername() == null))
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
		Person incomingPerson = (Person) obj;

		// Check the ID
		if (incomingPerson.getId() == null) {
			if (this.id != null)
				return false;
		} else {
			if (this.id == null)
				return false;
			if (this.id.longValue() != incomingPerson.getId().longValue())
				return false;
		}

		// OK, the ID's look like they are the same, let's check the firstname
		if (incomingPerson.getFirstname() == null) {
			if (this.firstname != null)
				return false;
		} else {
			if (this.firstname == null)
				return false;
			if (!this.firstname.equals(incomingPerson.getFirstname()))
				return false;
		}

		// Check the Surname
		if (incomingPerson.getSurname() == null) {
			if (this.surname != null)
				return false;
		} else {
			if (this.surname == null)
				return false;
			if (!this.surname.equals(incomingPerson.getSurname()))
				return false;
		}

		// Check the organization
		if (incomingPerson.getOrganization() == null) {
			if (this.organization != null)
				return false;
		} else {
			if (this.organization == null)
				return false;
			if (!this.organization.equals(incomingPerson.getOrganization()))
				return false;
		}

		// Check the username
		if (incomingPerson.getUsername() == null) {
			if (this.username != null)
				return false;
		} else {
			if (this.username == null)
				return false;
			if (!this.username.equals(incomingPerson.getUsername()))
				return false;
		}

		// Check the password
		if (incomingPerson.getPassword() == null) {
			if (this.password != null)
				return false;
		} else {
			if (this.password == null)
				return false;
			if (!this.password.equals(incomingPerson.getPassword()))
				return false;
		}

		// Check the email
		if (incomingPerson.getEmail() == null) {
			if (this.email != null)
				return false;
		} else {
			if (this.email == null)
				return false;
			if (!this.email.equals(incomingPerson.getEmail()))
				return false;
		}

		// Check the phone
		if (incomingPerson.getPhone() == null) {
			if (this.phone != null)
				return false;
		} else {
			if (this.phone == null)
				return false;
			if (!this.phone.equals(incomingPerson.getPhone()))
				return false;
		}

		// Check the address1
		if (incomingPerson.getAddress1() == null) {
			if (this.address1 != null)
				return false;
		} else {
			if (this.address1 == null)
				return false;
			if (!this.address1.equals(incomingPerson.getAddress1()))
				return false;
		}

		// Check the address2
		if (incomingPerson.getAddress2() == null) {
			if (this.address2 != null)
				return false;
		} else {
			if (this.address2 == null)
				return false;
			if (!this.address2.equals(incomingPerson.getAddress2()))
				return false;
		}

		// Check the city
		if (incomingPerson.getCity() == null) {
			if (this.city != null)
				return false;
		} else {
			if (this.city == null)
				return false;
			if (!this.city.equals(incomingPerson.getCity()))
				return false;
		}

		// Check the state
		if (incomingPerson.getState() == null) {
			if (this.state != null)
				return false;
		} else {
			if (this.state == null)
				return false;
			if (!this.state.equals(incomingPerson.getState()))
				return false;
		}

		// Check the zipcode
		if (incomingPerson.getZipcode() == null) {
			if (this.zipcode != null)
				return false;
		} else {
			if (this.zipcode == null)
				return false;
			if (!this.zipcode.equals(incomingPerson.getZipcode()))
				return false;
		}

		// Check the status
		if (incomingPerson.getStatus() == null) {
			if (this.status != null)
				return false;
		} else {
			if (this.status == null)
				return false;
			if (!this.status.equals(incomingPerson.getStatus()))
				return false;
		}

		// Now check the version numbers
		if (incomingPerson.getVersion() != this.version)
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
		int result = 14;
		if (username != null) {
			result = 14 + username.hashCode();
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
		firstname = (String) in.readObject();
		surname = (String) in.readObject();
		organization = (String) in.readObject();
		username = (String) in.readObject();
		email = (String) in.readObject();
		phone = (String) in.readObject();
		address1 = (String) in.readObject();
		address2 = (String) in.readObject();
		city = (String) in.readObject();
		state = (String) in.readObject();
		zipcode = (String) in.readObject();
		status = (String) in.readObject();
	}

	/**
	 * This is the method to serialize a Person to a custom serialized form
	 * 
	 * @see Externalizable#writeExternal(ObjectOutput)
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(id);
		out.writeObject(firstname);
		out.writeObject(surname);
		out.writeObject(organization);
		out.writeObject(username);
		out.writeObject(email);
		out.writeObject(phone);
		out.writeObject(address1);
		out.writeObject(address2);
		out.writeObject(city);
		out.writeObject(state);
		out.writeObject(zipcode);
		out.writeObject(status);
	}

	/**
	 * This method overrides the clone method to produce a copy of an object. It
	 * will clear the ID on the newly created copy to prevent odd behavior.
	 */
	protected Object clone() throws CloneNotSupportedException {
		// Create the clone
		Person clone = new Person();

		// Set the fields
		try {
			clone.setId(null);
			clone.setFirstname(this.getFirstname());
			clone.setSurname(this.getSurname());
			clone.setOrganization(this.getOrganization());
			clone.setUsername(this.getUsername());
			clone.setPassword(this.getPassword());
			clone.setEmail(this.getEmail());
			clone.setPhone(this.getPhone());
			clone.setAddress1(this.getAddress1());
			clone.setAddress2(this.getAddress2());
			clone.setCity(this.getCity());
			clone.setState(this.getState());
			clone.setZipcode(this.getZipcode());
			clone.setStatus(this.getStatus());
		} catch (MetadataException e) {
		}

		// Now return the clone
		return clone;
	}

	/**
	 * This method returns a clone of the <code>Person</code> with a deep copy
	 * of the <code>UserGroups</code> filled out
	 */
	public IMetadataObject deepCopy() throws CloneNotSupportedException {
		logger.debug("deepCopy called");
		// Grab the clone
		Person deepClone = (Person) this.clone();
		logger.debug("clone created:");
		logger.debug(deepClone.toStringRepresentation("|"));

		if ((this.getUserGroups() != null) && (this.getUserGroups().size() > 0)) {
			Collection userGroups = this.getUserGroups();
			Iterator userGroupsIter = userGroups.iterator();
			while (userGroupsIter.hasNext()) {
				UserGroup groupToClone = (UserGroup) userGroupsIter.next();
				if (groupToClone != null) {
					logger.debug("Will clone UserGroup:");
					logger.debug(groupToClone.toStringRepresentation("|"));
				}
				deepClone.addUserGroup((UserGroup) groupToClone.deepCopy());
			}
		}

		// Now return the deep clone
		return deepClone;
	}

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
	 * This is the firstname of the person respresented by the instance
	 */
	private String firstname;

	/**
	 * This is the surname (or lastname) of the person represented by the
	 * instance
	 */
	private String surname;

	/**
	 * This is the organization (name) that the person represents as an actor or
	 * user of the system
	 */
	private String organization;

	/**
	 * This is the person's username in the system. It (by definition) must be
	 * unique and not null.
	 */
	private String username;

	/**
	 * This is the person's password in the system.
	 */
	private transient String password;

	/**
	 * This is the email address of the person represented by the instance. This
	 * is also a unique key for the <code>Person</code> class.
	 */
	private String email;

	/**
	 * This is the 10 digit phone number of the person
	 */
	private String phone;

	/**
	 * This is the first line of the person's address
	 */
	private String address1;

	/**
	 * This is the second line of the person's address
	 */
	private String address2;

	/**
	 * This is the city the person's address is in
	 */
	private String city;

	/**
	 * This is the state the person's address is in
	 */
	private String state;

	/**
	 * This is the zipcode of the person's address
	 */
	private String zipcode;

	/**
	 * This is the status of the person relative to the system. It is a free
	 * text field, but often is used to determine if the person's account is
	 * active or not.
	 */
	private String status = Person.STATUS_ACTIVE;

	/**
	 * This is the <code>Collection</code> of <code>UserGroup</code>s that are
	 * associated with the <code>Person</code>
	 * 
	 * @associates UserGroup
	 * @directed true
	 * @label lazy
	 */
	private Collection userGroups = new HashSet();

	/**
	 * Some status constants
	 */
	public static final String STATUS_ACTIVE = "active";
	public static final String STATUS_INACTIVE = "inactive";

	/**
	 * This is the hibernate version that is used to check for dirty objects
	 */
	private long version = -1;

	/**
	 * This is a Log4JLogger that is used to log information to
	 */
	static Logger logger = Logger.getLogger(Person.class);

}