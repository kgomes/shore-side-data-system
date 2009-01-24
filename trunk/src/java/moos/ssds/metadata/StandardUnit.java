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

import moos.ssds.metadata.util.MetadataException;
import moos.ssds.metadata.util.MetadataValidator;

/**
 * This class represents the concept of a StandardUnit in the system. It can be
 * used to group similar units for simplification of queries, etc.
 * <hr>
 * 
 * @stereotype thing
 * @hibernate.class table="StandardUnit"
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.11 $
 */
public class StandardUnit implements IMetadataObject, IDescription {

	/**
	 * This is the default constructor. It simply sets the Description to an
	 * empty string
	 */
	public StandardUnit() {
		try {
			this.setDescription("");
		} catch (MetadataException e) {
		}
	}

	/**
	 * @see moos.ssds.metadata.IMetadataObject#getId()
	 * @hibernate.id generator-class="identity" type="long"
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @see moos.ssds.metadata.IMetadataObject#setId(Long)
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * This method returns the name of the <code>StandardUnit</code>
	 * 
	 * @hibernate.property
	 * @hibernate.column name="name" unique="true" not-null="true" length="255"
	 *                   index="name_index"
	 * @see moos.ssds.metadata.IDescription#getName()
	 * @return a <code>String</code> that is the name of the
	 *         <code>StandardUnit</code>
	 */
	public String getName() {
		return name;
	}

	/**
	 * This is the method that sets the name of the <code>StandardUnit</code>
	 * 
	 * @see moos.ssds.metadata.IDescription#setName(String)
	 * @param name
	 *            is the name that will be assigned to the
	 *            <code>StandardUnit</code>
	 */
	public void setName(String name) throws MetadataException {
		MetadataValidator.isObjectNull(name);
		MetadataValidator.isStringShorterThan(name,
				MetadataValidator.NAME_LENGTH);
		this.name = name;
	}

	/**
	 * This returns the description that has been assigned to the
	 * <code>StandardUnit</code>
	 * 
	 * @hibernate.property length="2048"
	 * @see moos.ssds.metadata.IDescription#getDescription()
	 * @return the description assigned to the <code>StandardUnit</code>
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * This is the method that sets the description of the
	 * <code>StandardUnit</code>
	 * 
	 * @see moos.ssds.metadata.IDescription#setDescription(String)
	 * @param description
	 *            is the description that will be assigned to the
	 *            <code>StandardUnit</code>
	 */
	public void setDescription(String description) throws MetadataException {
		MetadataValidator.isStringShorterThan(description,
				MetadataValidator.DESCRIPTION_LENGTH);
		this.description = description;
	}

	/**
	 * This returns the long name that has been assigned to the
	 * <code>StandardUnit</code>
	 * 
	 * @hibernate.property length="255"
	 * @return the longName assigned to the <code>StandardUnit</code>
	 */
	public String getLongName() {
		return longName;
	}

	/**
	 * This is the method that sets the longName of the
	 * <code>StandardUnit</code>
	 * 
	 * @param longName
	 *            is the longName that will be assigned to the
	 *            <code>StandardUnit</code>
	 */
	public void setLongName(String longName) throws MetadataException {
		MetadataValidator.isStringShorterThan(longName, 255);
		this.longName = longName;
	}

	/**
	 * This returns the symbol that has been assigned to the
	 * <code>StandardUnit</code>
	 * 
	 * @hibernate.property
	 * @hibernate.column name="symbol" length="50" index="symbol_index"
	 * @return the symbol that has been assigned to the
	 *         <code>StandardUnit</code>
	 */
	public String getSymbol() {
		return symbol;
	}

	/**
	 * This is the method that sets the symbol of the <code>StandardUnit</code>
	 * 
	 * @param symbol
	 *            is the symbol that will be assigned to the
	 *            <code>StandardUnit</code>
	 */
	public void setSymbol(String symbol) throws MetadataException {
		MetadataValidator.isStringShorterThan(symbol, 50);
		this.symbol = symbol;
	}

	/**
	 * This method returns the version that Hibernate uses to check for dirty
	 * objects
	 * 
	 * @hibernate.version type=long
	 * @return the <code>long</code> that is the version of the instance of
	 *         the class
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
		if (!(obj instanceof StandardUnit))
			return false;

		// Cast to StandardUnit object
		final StandardUnit that = (StandardUnit) obj;

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

	/**
	 * This method overrides the default hashCode and had to be implemented
	 * because we overrode the default equals method. They both should base
	 * their calculation on the business key.
	 * 
	 * @see moos.ssds.metadata.IMetadataObject#hashCode()
	 */
	public int hashCode() {
		// Calculate the hashcodes
		int result = 35;
		if (name != null) {
			result = 54 * result + name.hashCode();
		}
		// Now return it
		return result;
	}

	/**
	 * @see moos.ssds.metadata.IMetadataObject#toStringRepresentation(String)
	 */
	public String toStringRepresentation(String delimiter) {
		// If the delimiter is not specified, use a default one
		if (delimiter == null)
			delimiter = IMetadataObject.DEFAULT_DELIMITER;

		StringBuffer sb = new StringBuffer();
		sb.append("StandardUnit");
		sb.append(delimiter + "id=" + this.getId());
		sb.append(delimiter + "name=" + this.getName());
		sb.append(delimiter + "description=" + this.getDescription());
		sb.append(delimiter + "longName=" + this.getLongName());
		sb.append(delimiter + "symbol=" + this.getSymbol());
		return sb.toString();
	}

	/**
	 * In order to use the class, you should first create an empty object, then
	 * call this method, passing in the string representation. As an example:
	 * 
	 * <pre>
	 * StandardUnit newStandardUnit = new StandardUnit();
	 * 
	 * newStandardUnit
	 * 		.setValuesFromStringRepresentation(
	 * 				&quot;StandardUnit|name=UnitOne|description=A nifty unit|longName=A super long name|symbol=U1&quot;,
	 * 				&quot;|&quot;);
	 * </pre>
	 * 
	 * @see moos.ssds.metadata.IMetadataObject#setValuesFromStringRepresentation
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
			} else if (key.equalsIgnoreCase("longName")) {
				this.setLongName(value);
			} else if (key.equalsIgnoreCase("symbol")) {
				this.setSymbol(value);
			} else {
				throw new MetadataException("The attribute specified by " + key
						+ " is not a recognized field of "
						+ this.getClass().getName());
			}
		}
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
		longName = (String) in.readObject();
		symbol = (String) in.readObject();
	}

	/**
	 * This is the method to serialize a StandardUnit to a custom serialized
	 * form
	 * 
	 * @see Externalizable#writeExternal(ObjectOutput)
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(id);
		out.writeObject(name);
		out.writeObject(description);
		out.writeObject(longName);
		out.writeObject(symbol);
	}

	/**
	 * This method overrides the clone method to produce a copy of an object. It
	 * will clear the ID on the newly created copy to prevent odd behavior.
	 */
	protected Object clone() throws CloneNotSupportedException {
		// Create the clone
		StandardUnit clone = new StandardUnit();

		// Set the fields
		try {
			clone.setId(null);
			clone.setName(this.getName());
			clone.setDescription(this.getDescription());
			clone.setLongName(this.getLongName());
			clone.setSymbol(this.getSymbol());
		} catch (MetadataException e) {
		}

		// Return the clone
		return clone;
	}

	/**
	 * This simply returns the clone
	 */
	public IMetadataObject deepCopy() throws CloneNotSupportedException {
		return (StandardUnit) this.clone();
	}

	/**
	 * This is the <code>serialVersionUID</code> that is fixed to control
	 * serialization versions of the class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * This is the ID that is used by the persistence layer to identify specific
	 * instances of the <code>StandardUnit</code>s.
	 */
	private Long id;

	/**
	 * This is the name of the <code>StandardUnit</code>. It is considered a
	 * unique identifier (alternate primary key).
	 */
	private String name;

	/**
	 * This is the text description of the <code>StandardUnit</code>.
	 */
	private String description;

	/**
	 * This is the long name that give more information about the
	 * <code>StandardUnit</code>.
	 */
	private String longName;

	/**
	 * This is a string that is defined as the symbol for the
	 * <code>StandardUnit</code>.
	 */
	private String symbol;

	/**
	 * This is the hibernate version that is used to check for dirty objects
	 */
	private long version = -1;
}
