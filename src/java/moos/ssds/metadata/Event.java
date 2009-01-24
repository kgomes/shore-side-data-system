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
import java.util.Date;
import java.util.StringTokenizer;

import moos.ssds.metadata.util.MetadataException;
import moos.ssds.metadata.util.MetadataValidator;
import moos.ssds.util.DateUtils;
import moos.ssds.util.XmlDateFormat;

/**
 * This class represents any event that happens in the system
 * <hr>
 * 
 * @stereotype moment-interval
 * @hibernate.class table="Event"
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.14 $
 */
public class Event implements IMetadataObject, IDescription, IDateRange {

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
	 * These methods get and set the version that Hibernate uses to check for
	 * dirty objects
	 * 
	 * @hibernate.version type=long
	 * @return the <code>long</code> that is the version of the instance of
	 *         the class
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
		if (delimiter == null)
			delimiter = IMetadataObject.DEFAULT_DELIMITER;
		StringBuffer sb = new StringBuffer();
		sb.append("Event");
		sb.append(delimiter + "id=" + this.getId());
		sb.append(delimiter + "name=" + this.getName());
		sb.append(delimiter + "description=" + this.getDescription());
		sb.append(delimiter + "startDate="
				+ xmlDateFormat.format(this.startDate));
		sb.append(delimiter + "endDate=" + xmlDateFormat.format(this.endDate));
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
			} else if (key.equalsIgnoreCase("startDate")) {
				this.setStartDate(xmlDateFormat.parse(value));
			} else if (key.equalsIgnoreCase("endDate")) {
				this.setEndDate(xmlDateFormat.parse(value));
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
		if (!(obj instanceof Event))
			return false;

		// Cast to Event object
		final Event that = (Event) obj;

		// Now check for missing business keys (name and dates)
		if ((this.name == null) || (that.getName() == null)
				|| (this.startDate == null) || (that.getStartDate() == null)
				|| (this.endDate == null) || (that.getEndDate() == null))
			return false;

		// Now compare hashcodes
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
		int result = 3;
		if (name != null) {
			result = result + name.hashCode();
		}
		// Now truncate to seconds and get hashcode
		if (startDate != null)
			result = 3
					* result
					+ new String(DateUtils.roundDateDownToSeconds(startDate)
							.getTime()
							+ "").hashCode();
		if (endDate != null)
			result = 5
					* result
					+ new String(DateUtils.roundDateDownToSeconds(endDate)
							.getTime()
							+ "").hashCode();

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
	}

	/**
	 * This is the method to serialize a Event to a custom serialized form
	 * 
	 * @see Externalizable#writeExternal(ObjectOutput)
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(id);
		out.writeObject(name);
		out.writeObject(description);
		out.writeObject(startDate);
		out.writeObject(endDate);
	}

	/**
	 * This method overrides the clone method to produce a copy of an object. It
	 * will clear the ID on the newly created copy to prevent odd behavior.
	 */
	protected Object clone() throws CloneNotSupportedException {
		// Create the clone
		Event clone = new Event();

		// Set the fields
		try {
			clone.setId(null);
			clone.setName(this.getName());
			clone.setDescription(this.getDescription());
			clone.setStartDate(this.getStartDate());
			clone.setEndDate(this.getEndDate());
		} catch (MetadataException e) {
		}

		// Return the clone
		return clone;
	}

	/**
	 * This simply returns a clone of the <code>Event</code>
	 */
	public IMetadataObject deepCopy() throws CloneNotSupportedException {
		return (Event) this.clone();
	}

	/**
	 * This is the version that we can control for serialization purposes
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * TODO KJG - Document this
	 */
	private Long id;

	/**
	 * TODO KJG - Document this
	 */
	private String name;

	/**
	 * TODO KJG - Document this
	 */
	private String description;

	/**
	 * TODO KJG - Document this
	 */
	private Date startDate;

	/**
	 * TODO KJG - Document this
	 */
	private Date endDate;

	/**
	 * TODO KJG - Document this
	 */
	private IDateRange dateRange = new DateRange(this);

	/**
	 * This is the hibernate version that is used to check for dirty objects
	 */
	private long version = -1;
	/**
	 * TODO KJG - Document this
	 */
	// private IDataProducer dataProducer;
	private XmlDateFormat xmlDateFormat = new XmlDateFormat();
}