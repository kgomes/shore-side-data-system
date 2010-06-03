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
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.log4j.Logger;

import moos.ssds.metadata.util.MetadataException;

/**
 * This class describes the records (variables) that are in a
 * <code>DataContainer</code>.
 * <hr>
 * 
 * @stereotype description
 * @hibernate.class table="RecordDescription"
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.20 $
 */
public class RecordDescription implements IMetadataObject {

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
	 * These methods get and set the type of record this refers to. This is due
	 * to the fact that a <code>DataProducer</code> can put out records of more
	 * than one type
	 * 
	 * @hibernate.property not-null="true"
	 * @return the <code>Long</code> that indicates the type of record that this
	 *         description applies to
	 */
	public Long getRecordType() {
		return recordType;
	}

	public void setRecordType(Long recordType) throws MetadataException {
		logger.debug("setRecordType called with recordType = " + recordType);
		if (recordType == null) {
			logger.debug("recordType was null, will set to 0");
			this.recordType = new Long(0);
		} else {
			logger.debug("Seems like a true Long, will set");
			this.recordType = recordType;
		}
	}

	/**
	 * These methods get and set the style of buffer and should match one of the
	 * constants defined in the class (ASCII_BUFFER or BINARY_BUFFER)
	 * 
	 * @hibernate.property
	 * @return a <code>String</code> that indicates what style of buffer is used
	 *         in the record that this describes
	 */
	public String getBufferStyle() {
		return bufferStyle;
	}

	public void setBufferStyle(String bufferStyle) throws MetadataException {
		// First convert if the just don't match case
		if (bufferStyle == null) {
			this.bufferStyle = null;
			return;
		}
		if (bufferStyle.equalsIgnoreCase(BUFFER_STYLE_ASCII))
			bufferStyle = BUFFER_STYLE_ASCII;
		if (bufferStyle.equalsIgnoreCase(BUFFER_STYLE_BINARY))
			bufferStyle = BUFFER_STYLE_BINARY;
		if (!isValidBufferStyle(bufferStyle))
			throw new MetadataException("The bufferStyle of " + bufferStyle
					+ " does not match a recognized buffer style");
		this.bufferStyle = bufferStyle;
	}

	/**
	 * These methods get and set the buffer parse type and should match the
	 * constants defined in the class (FIXED_POSITION, ORDERED_POSITION, or
	 * UNIQUE_TOKEN)
	 * 
	 * @hibernate.property
	 * @return the buffer parse type for the records described by this
	 *         <code>RecordDescription</code>
	 */
	public String getBufferParseType() {
		return bufferParseType;
	}

	public void setBufferParseType(String bufferParseType)
			throws MetadataException {
		if (bufferParseType == null) {
			this.bufferParseType = null;
			return;
		}
		// First remove any case issues
		if (bufferParseType.equalsIgnoreCase(PARSE_TYPE_FIXED_POSITION))
			bufferParseType = PARSE_TYPE_FIXED_POSITION;
		if (bufferParseType.equalsIgnoreCase(PARSE_TYPE_ORDERED_POSITION))
			bufferParseType = PARSE_TYPE_ORDERED_POSITION;
		if (bufferParseType.equalsIgnoreCase(PARSE_TYPE_UNIQUE_TOKEN))
			bufferParseType = PARSE_TYPE_UNIQUE_TOKEN;
		if (!isValidParseType(bufferParseType))
			throw new MetadataException("The bufferParseType of "
					+ bufferParseType + " was not reckognized.");
		this.bufferParseType = bufferParseType;
	}

	/**
	 * These methods get and set the string that defines what separates items in
	 * the buffer.
	 * 
	 * @hibernate.property
	 * @return the <code>String</code> that is used to separate items in the
	 *         buffer
	 */
	public String getBufferItemSeparator() {
		return bufferItemSeparator;
	}

	public void setBufferItemSeparator(String bufferItemSeperator) {
		this.bufferItemSeparator = bufferItemSeperator;
	}

	/**
	 * These methods get and set the type of length of the records
	 * (FIXED_BUFFER_LENGTH or VARIABLE_BUFFER_LENGTH) and should match a
	 * constant defined in the class.
	 * 
	 * @hibernate.property
	 * @return the type of length of the records in the buffer.
	 */
	public String getBufferLengthType() {
		return bufferLengthType;
	}

	public void setBufferLengthType(String bufferLengthType)
			throws MetadataException {
		if (bufferLengthType == null) {
			this.bufferLengthType = null;
			return;
		}
		// Filter out case issues
		if (bufferLengthType.equalsIgnoreCase(BUFFER_LENGTH_TYPE_FIXED))
			bufferLengthType = BUFFER_LENGTH_TYPE_FIXED;
		if (bufferLengthType.equalsIgnoreCase(BUFFER_LENGTH_TYPE_VARIABLE))
			bufferLengthType = BUFFER_LENGTH_TYPE_VARIABLE;
		if (!isValidBufferLengthType(bufferLengthType))
			throw new MetadataException("The bufferLengthType of "
					+ bufferLengthType + " is not reckognized");
		this.bufferLengthType = bufferLengthType;
	}

	/**
	 * These methods get and set the string that is used to indicate the end of
	 * a record
	 * 
	 * @hibernate.property
	 * @return the <code>String</code> that is the terminating record indicator
	 */
	public String getRecordTerminator() {
		return recordTerminator;
	}

	public void setRecordTerminator(String recordTerminator) {
		this.recordTerminator = recordTerminator;
	}

	/**
	 * These methods get and set a <code>boolean</code> to indicate if the
	 * records described here are parseable or not
	 * 
	 * @hibernate.property
	 */
	public Boolean isParseable() {
		return parseable;
	}

	public void setParseable(Boolean parseable) {
		this.parseable = parseable;
	}

	/**
	 * These methods get and set the endianess of the records in the buffer and
	 * should match one of the constants defined in the class (LITTLE_ENDIAN or
	 * BIG_ENDIAN)
	 * 
	 * @hibernate.property
	 * @return a <code>String</code> that indicates what the endianess of the
	 *         buffers are
	 */
	public String getEndian() {
		return endian;
	}

	public void setEndian(String endian) throws MetadataException {
		if (endian == null) {
			this.endian = null;
			return;
		}
		// Fix any case issues
		if (endian.equalsIgnoreCase(ENDIAN_BIG))
			endian = ENDIAN_BIG;
		if (endian.equalsIgnoreCase(ENDIAN_LITTLE))
			endian = ENDIAN_LITTLE;
		if (!isValidEndian(endian))
			throw new MetadataException("The endian of " + endian
					+ " is not recognized");
		this.endian = endian;
	}

	/**
	 * These methods get and set the regular expression that will be used to
	 * parse a data record into variables
	 * 
	 * @hibernate.property length="2048"
	 */
	public String getRecordParseRegExp() {
		return recordParseRegExp;
	}

	public void setRecordParseRegExp(String recordParseRegExp)
			throws MetadataException {

		if (recordParseRegExp == null) {
			this.recordParseRegExp = null;
			return;
		}

		// Validate that the reg exp will compile
		try {
			Pattern pattern = Pattern.compile(recordParseRegExp);
		} catch (PatternSyntaxException e) {
			throw new MetadataException("Pattern could not be compiled: "
					+ e.getMessage());
		}
		// If it compiled, set it
		this.recordParseRegExp = recordParseRegExp;
	}

	/**
	 * These methods get and set the collection of <code>RecordVariable</code>s
	 * for this <code>RecordDescription</code>.
	 * 
	 * @hibernate.set cascade="all" lazy="false" outer-join="true"
	 *                order-by="columnIndex asc"
	 * @hibernate.collection-key column="RecordDescriptionID_FK"
	 * @hibernate.collection-one-to-many 
	 *                                   class="moos.ssds.metadata.RecordVariable"
	 * @return A collection of the <code>RecordVariable<code>s
	 */
	public Collection<RecordVariable> getRecordVariables() {
		return this.recordVariables;
	}

	protected void setRecordVariables(Collection recordVariables) {
		this.recordVariables = recordVariables;
	}

	/**
	 * This method adds the given <code>RecordVariable</code> to the collection
	 * associated with the <code>RecordDescription</code>.
	 * 
	 * @param recordVariable
	 *            the <code>RecordVariable</code> to add
	 */
	public void addRecordVariable(RecordVariable recordVariable) {
		// If null was passed in, just return
		if (recordVariable == null)
			return;

		// Now add the KeyWord to the collection
		if (!this.recordVariables.contains(recordVariable)) {
			this.recordVariables.add(recordVariable);
		}
	}

	/**
	 * This method removes the given <code>RecordVariable</code> from the
	 * collection
	 * 
	 * @param recordVariable
	 *            is the <code>RecordVariable</code> to remove from the
	 *            collection
	 */
	public void removeRecordVariable(RecordVariable recordVariable) {
		if (recordVariable == null)
			return;
		if ((this.recordVariables != null)
				&& (this.recordVariables.contains(recordVariable))) {
			this.recordVariables.remove(recordVariable);
		}
	}

	/**
	 * This method will clear out the collection of <code>RecordVariable</code>s
	 * and keep the integrity of the relationships intact.
	 */
	public void clearRecordVariables() {
		this.recordVariables.clear();
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

	public static boolean isValidBufferStyle(String bufferStyle) {
		boolean result = true;
		if (bufferStyle == null) {
			result = true;
		} else if ((!bufferStyle.equals(BUFFER_STYLE_ASCII))
				&& (!bufferStyle.equals(BUFFER_STYLE_BINARY))
				&& (!bufferStyle.equals(BUFFER_STYLE_MULITPART_MIME))) {
			result = false;
		}
		return result;
	}

	public static boolean isValidBufferLengthType(String bufferLengthType) {
		boolean result = true;
		if (bufferLengthType == null) {
			result = true;
		} else if ((!bufferLengthType.equals(BUFFER_LENGTH_TYPE_FIXED))
				&& (!bufferLengthType.equals(BUFFER_LENGTH_TYPE_VARIABLE))) {
			result = false;
		}
		return result;
	}

	public static boolean isValidEndian(String endian) {
		boolean result = true;
		if (endian == null) {
			result = true;
		} else if ((!endian.equals(ENDIAN_BIG))
				&& (!endian.equals(ENDIAN_LITTLE))) {
			result = false;
		}
		return result;
	}

	public static boolean isValidParseType(String parseType) {
		boolean result = true;
		if (parseType == null) {
			result = true;
		} else if ((!parseType.equals(PARSE_TYPE_FIXED_POSITION))
				&& (!parseType.equals(PARSE_TYPE_ORDERED_POSITION))
				&& (!parseType.equals(PARSE_TYPE_UNIQUE_TOKEN))) {
			result = false;
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
		sb.append("RecordDescription");
		sb.append(delimiter + "id=" + this.getId());
		sb.append(delimiter + "recordType=" + this.getRecordType());
		sb.append(delimiter + "bufferStyle=" + this.getBufferStyle());
		sb.append(delimiter + "bufferParseType=" + this.getBufferParseType());
		sb.append(delimiter + "bufferItemSeparator="
				+ this.getBufferItemSeparator());
		sb.append(delimiter + "bufferLengthType=" + this.getBufferLengthType());
		sb.append(delimiter + "recordTerminator=" + this.getRecordTerminator());
		sb.append(delimiter + "parseable=" + this.isParseable());
		sb.append(delimiter + "endian=" + this.getEndian());
		sb.append(delimiter + "recordParseRegExp="
				+ this.getRecordParseRegExp());
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
			} else if (key.equalsIgnoreCase("recordType")) {
				try {
					this.setRecordType(Long.valueOf(value));
				} catch (NumberFormatException e) {
					throw new MetadataException(
							"The value specified for recordType (" + value
									+ ") could not be converted to a Long: "
									+ e.getMessage());
				}
			} else if (key.equalsIgnoreCase("bufferStyle")) {
				this.setBufferStyle(value);
			} else if (key.equalsIgnoreCase("bufferParseType")) {
				this.setBufferParseType(value);
			} else if (key.equalsIgnoreCase("bufferItemSeparator")) {
				this.setBufferItemSeparator(value);
			} else if (key.equalsIgnoreCase("bufferLengthType")) {
				this.setBufferLengthType(value);
			} else if (key.equalsIgnoreCase("recordTerminator")) {
				this.setRecordTerminator(value);
			} else if (key.equalsIgnoreCase("parseable")) {
				if ((!value.equalsIgnoreCase("true"))
						&& (!value.equalsIgnoreCase("false")))
					throw new MetadataException(
							"Could not convert the specified value for parseable ("
									+ value
									+ ") to a boolean (valid options are 'true' or 'false')");
				this.setParseable(Boolean.valueOf(value));
			} else if (key.equalsIgnoreCase("endian")) {
				this.setEndian(value);
			} else if (key.equalsIgnoreCase("recordParseRegExp")) {
				this.setRecordParseRegExp(value);
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
		// Since there really is no uniqueness here, I will use the default
		// implementation
		return super.equals(obj);
	}

	/**
	 * @see IMetadataObject#hashCode()
	 */
	public int hashCode() {
		// Since there really is no uniqueness here, I will use the default
		// implementation
		return super.hashCode();
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
		recordType = (Long) in.readObject();
		bufferStyle = (String) in.readObject();
		bufferParseType = (String) in.readObject();
		bufferItemSeparator = (String) in.readObject();
		bufferLengthType = (String) in.readObject();
		recordTerminator = (String) in.readObject();
		parseable = (Boolean) in.readObject();
		endian = (String) in.readObject();
		recordParseRegExp = (String) in.readObject();
	}

	/**
	 * This is the method to serialize a RecordDescription to a custom
	 * serialized form
	 * 
	 * @see Externalizable#writeExternal(ObjectOutput)
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(id);
		out.writeObject(recordType);
		out.writeObject(bufferStyle);
		out.writeObject(bufferParseType);
		out.writeObject(bufferItemSeparator);
		out.writeObject(bufferLengthType);
		out.writeObject(recordTerminator);
		out.writeObject(parseable);
		out.writeObject(endian);
		out.writeObject(recordParseRegExp);
	}

	/**
	 * This method overrides the clone method to produce a copy of an object. It
	 * will clear the ID on the newly created copy to prevent odd behavior.
	 */
	protected Object clone() throws CloneNotSupportedException {
		// Create the clone
		RecordDescription clone = new RecordDescription();

		// Set the fields
		try {
			clone.setId(null);
			clone.setRecordType(this.getRecordType());
			clone.setBufferStyle(this.getBufferStyle());
			clone.setBufferParseType(this.getBufferParseType());
			clone.setBufferItemSeparator(this.getBufferItemSeparator());
			clone.setBufferLengthType(this.getBufferLengthType());
			clone.setRecordTerminator(this.getRecordTerminator());
			clone.setParseable(this.isParseable());
			clone.setEndian(this.getEndian());
			clone.setRecordParseRegExp(this.getRecordParseRegExp());
		} catch (MetadataException e) {
		}

		// Now return the clone
		return clone;
	}

	/**
	 * This returns a clone of the <code>RecordDescripton</code> with deep
	 * copies all of its <code>RecordVariables</code>
	 */
	public IMetadataObject deepCopy() throws CloneNotSupportedException {
		logger.debug("deepCopy called.");
		// Grab the clone
		RecordDescription deepClone = (RecordDescription) this.clone();
		logger.debug("The following clone was created:");
		logger.debug(deepClone.toStringRepresentation("|"));

		// Set the record variables
		if ((this.getRecordVariables() != null)
				&& (this.getRecordVariables().size() > 0)) {
			logger.debug("There are " + this.getRecordVariables().size()
					+ " RecordVariables to clone and attach");
			Collection rvs = this.getRecordVariables();
			Iterator rvIter = rvs.iterator();
			while (rvIter.hasNext()) {
				RecordVariable clonedRecordVariable = (RecordVariable) ((RecordVariable) rvIter
						.next()).deepCopy();
				logger
						.debug("The following cloned RecordVariable will be added:");
				logger.debug(clonedRecordVariable.toStringRepresentation("|"));
				deepClone.addRecordVariable(clonedRecordVariable);
			}
		}

		// Now return the deep copy
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
	 * This is an index to indicate which type of record this is describing. A
	 * <code>DataProducer</code> can generate more than one type of record and
	 * this indicates which record this description referrs to.
	 */
	private Long recordType = new Long(0);

	/**
	 * This is the style of buffer (ascii or binary)
	 */
	private String bufferStyle;

	/**
	 * Indicates how data can be parsed from buffer: &quot;FIXED_POSITION&qout;
	 * buffers have items at the same offset from the start of buffer.
	 * &quot;ORDERED_POSITION&quot; must be parsed according to the order in
	 * which items appear, and may have tokens which precede or follow one or
	 * more data items. &quot;UNIQUE_TOKEN&quot; buffer items do not appear in
	 * any fixed order, but must be recognized by their attached token
	 */
	private String bufferParseType;

	/**
	 * For ASCII buffers, the entity that separates each buffer item from the
	 * next. Strings can take the form of a single character separator (for
	 * example, a comma or a space), or a symbolic name. Supported symbols
	 * include 'space', 'tab', and 'whitespace' (all transformed to '\s+', as is
	 * '' and ' ') and 'comma'.
	 */
	private String bufferItemSeparator;

	/**
	 * This describes the buffer length and whether it is variable of fixed. If
	 * 'fixed', all valid packets will be of same length, and packets of other
	 * lengths will be rejected. This characteristic can be used to optimize
	 * random access to data within the saved set of raw packets (but will only
	 * be used according to bufferParseType)
	 */
	private String bufferLengthType;

	/**
	 * The series of characters which terminate each ASCII record
	 */
	private String recordTerminator;

	/**
	 * Indicates whether this data format is regular enough to be parsed
	 */
	private Boolean parseable = new Boolean(false);

	/**
	 * Indicates endian-ness of byte sequences that make up numbers.
	 * &quot;LITTLE_ENDIAN&quot; means the first byte is least significant,
	 * &quot;BIG_ENDIAN&quot; means first byte is most significant (Intel x86
	 * processors are little-endian, Sun SPARC and PowerPC big-endian). Default
	 * endianness is according to the SSDS host(big, so far)
	 */
	private String endian = ENDIAN_LITTLE;

	/**
	 * This <code>String</code> allows a regular expression to be defined for an
	 * entire record. This will allow parsers to use regular expressions to
	 * parse out the variables from a data record. It will have precedence over
	 * delimiters that may be defined.
	 */
	private String recordParseRegExp;

	/**
	 * This is the hibernate version that is used to check for dirty objects
	 */
	private long version = -1;

	/**
	 * This is the <code>Collection</code> of <code>RecordVariable</code>s that
	 * make up this <code>RecordDescription</code>
	 * 
	 * @associates RecordVariable
	 * @directed true
	 * @label unlazy & cascade all
	 */
	private Collection<RecordVariable> recordVariables = new HashSet<RecordVariable>();

	// Some constants
	public static final String BUFFER_STYLE_ASCII = "ASCII";
	public static final String BUFFER_STYLE_BINARY = "binary";
	public static final String BUFFER_STYLE_MULITPART_MIME = "mulitpart_mime";

	public static final String BUFFER_LENGTH_TYPE_FIXED = "fixed";
	public static final String BUFFER_LENGTH_TYPE_VARIABLE = "variable";

	public static final String ENDIAN_BIG = "big";
	public static final String ENDIAN_LITTLE = "little";

	public static final String PARSE_TYPE_FIXED_POSITION = "fixed";
	public static final String PARSE_TYPE_ORDERED_POSITION = "ordered";
	public static final String PARSE_TYPE_UNIQUE_TOKEN = "unique_token";

	static Logger logger = Logger.getLogger(RecordDescription.class);
}