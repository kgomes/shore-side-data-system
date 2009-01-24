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

import org.apache.log4j.Logger;

/**
 * This class represents a variable in a data record
 * <hr>
 * 
 * @stereotype description
 * @hibernate.class table="RecordVariable"
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.16 $
 */
public class RecordVariable implements IMetadataObject, IDescription {

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
	 * @hibernate.property length="255" not-null="true" index="name_index"
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
	 * TODO KJG - Document this
	 * 
	 * @hibernate.property length="255"
	 */
	public String getLongName() {
		return longName;
	}

	public void setLongName(String longName) throws MetadataException {
		MetadataValidator.isStringShorterThan(longName, 255);
		this.longName = longName;
	}

	/**
	 * TODO KJG - Document this
	 * 
	 * @hibernate.property
	 */
	public String getFormat() {
		return format;
	}

	public void setFormat(String s) {
		this.format = s;
	}

	/**
	 * TODO KJG - Document this
	 * 
	 * @hibernate.property
	 */
	public String getUnits() {
		return units;
	}

	public void setUnits(String units) {
		this.units = units;
	}

	/**
	 * TODO KJG - Document this
	 * 
	 * @hibernate.property
	 */
	public long getColumnIndex() {
		return columnIndex;
	}

	public void setColumnIndex(long i) {
		this.columnIndex = i;
	}

	/**
	 * TODO KJG - Document this
	 * 
	 * @hibernate.property
	 */
	public String getValidMin() {
		return validMin;
	}

	public void setValidMin(String s) {
		this.validMin = s;
	}

	/**
	 * TODO KJG - Document this
	 * 
	 * @hibernate.property
	 */
	public String getValidMax() {
		return validMax;
	}

	public void setValidMax(String s) {
		this.validMax = s;
	}

	/**
	 * TODO KJG - Document this
	 * 
	 * @hibernate.property
	 */
	public String getMissingValue() {
		return missingValue;
	}

	public void setMissingValue(String s) {
		this.missingValue = s;
	}

	/**
	 * TODO KJG - Document this
	 * 
	 * @hibernate.property
	 */
	public String getAccuracy() {
		return accuracy;
	}

	public void setAccuracy(String accuracy) {
		this.accuracy = accuracy;
	}

	/**
	 * TODO KJG - Document this
	 * 
	 * @hibernate.property
	 */
	public Double getDisplayMin() {
		return displayMin;
	}

	public void setDisplayMin(Double displayMin) {
		this.displayMin = displayMin;
	}

	/**
	 * TODO KJG - Document this
	 * 
	 * @hibernate.property
	 */
	public Double getDisplayMax() {
		return displayMax;
	}

	public void setDisplayMax(Double displayMax) {
		this.displayMax = displayMax;
	}

	/**
	 * TODO KJG - Document this
	 * 
	 * @hibernate.property
	 */
	public String getReferenceScale() {
		return referenceScale;
	}

	public void setReferenceScale(String referenceScale) {
		this.referenceScale = referenceScale;
	}

	/**
	 * TODO KJG - Document this
	 * 
	 * @hibernate.property
	 */
	public Double getConversionScale() {
		return conversionScale;
	}

	public void setConversionScale(Double conversionScale) {
		this.conversionScale = conversionScale;
	}

	/**
	 * TODO KJG - Document this
	 * 
	 * @hibernate.property
	 */
	public Double getConversionOffset() {
		return conversionOffset;
	}

	public void setConversionOffset(Double conversionOffset) {
		this.conversionOffset = conversionOffset;
	}

	/**
	 * TODO KJG - Document this
	 * 
	 * @hibernate.property
	 */
	public String getConvertedUnits() {
		return convertedUnits;
	}

	public void setConvertedUnits(String convertedUnits) {
		this.convertedUnits = convertedUnits;
	}

	/**
	 * TODO KJG - Document this
	 * 
	 * @hibernate.property
	 */
	public Long getSourceSensorID() {
		return sourceSensorID;
	}

	public void setSourceSensorID(Long sourceSensorID) {
		this.sourceSensorID = sourceSensorID;
	}

	/**
	 * TODO KJG - Document this
	 * 
	 * @hibernate.property
	 */
	public String getParseRegExp() {
		return parseRegExp;
	}

	public void setParseRegExp(String parseRegExp) {
		this.parseRegExp = parseRegExp;
	}

	/**
	 * TODO KJG - Document this
	 * 
	 * @hibernate.many-to-one class="moos.ssds.metadata.StandardVariable"
	 *                        column="StandardVariableID_FK"
	 *                        foreign-key="RecordVariable_Is_Of_StandardVariable"
	 *                        cascade="none" lazy="false" outer-join="true"
	 * @return
	 */
	public StandardVariable getStandardVariable() {
		return standardVariable;
	}

	public void setStandardVariable(StandardVariable v) {
		this.standardVariable = v;
	}

	/**
	 * TODO KJG - Document this
	 * 
	 * @hibernate.many-to-one class="moos.ssds.metadata.StandardUnit"
	 *                        column="StandardUnitID_FK"
	 *                        foreign-key="RecordVariable_Has_Unit_Of_StandardUnit"
	 *                        cascade="none" lazy="false" outer-join="true"
	 * @return
	 */
	public StandardUnit getStandardUnit() {
		return standardUnit;
	}

	public void setStandardUnit(StandardUnit standardUnit) {
		this.standardUnit = standardUnit;
	}

	/**
	 * TODO KJG - Document this
	 * 
	 * @hibernate.many-to-one class="moos.ssds.metadata.StandardReferenceScale"
	 *                        column="StandardReferenceScaleID_FK"
	 *                        foreign-key="RecordVariable_Has_Scale_Of_StandardReferenceScale"
	 *                        cascade="none" lazy="false" outer-join="true"
	 * @return
	 */
	public StandardReferenceScale getStandardReferenceScale() {
		return standardReferenceScale;
	}

	public void setStandardReferenceScale(
			StandardReferenceScale standardReferenceScale) {
		this.standardReferenceScale = standardReferenceScale;
	}

	/**
	 * TODO KJG - Document this
	 * 
	 * @hibernate.many-to-one class="moos.ssds.metadata.StandardDomain"
	 *                        column="StandardDomainID_FK"
	 *                        foreign-key="RecordVariable_Is_Of_StandardDomain"
	 *                        cascade="none" lazy="false" outer-join="true"
	 * @return
	 */
	public StandardDomain getStandardDomain() {
		return standardDomain;
	}

	public void setStandardDomain(StandardDomain standardDomain) {
		this.standardDomain = standardDomain;
	}

	/**
	 * TODO KJG - Document this
	 * 
	 * @hibernate.many-to-one class="moos.ssds.metadata.StandardKeyword"
	 *                        column="StandardKeywordID_FK"
	 *                        foreign-key="RecordVariable_Has_StandardKeyword"
	 *                        cascade="none" lazy="false" outer-join="true"
	 * @return
	 */
	public StandardKeyword getStandardKeyword() {
		return standardKeyword;
	}

	public void setStandardKeyword(StandardKeyword standardKeyword) {
		this.standardKeyword = standardKeyword;
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
	 * @see moos.ssds.model.ValueObject#toStringRepresentation(String)
	 */
	public String toStringRepresentation(String delimiter) {
		if (delimiter == null)
			delimiter = "|";
		StringBuffer sb = new StringBuffer();
		sb.append("RecordVariable");
		sb.append(delimiter + "id=" + this.getId());
		sb.append(delimiter + "name=" + this.getName());
		sb.append(delimiter + "description=" + this.getDescription());
		sb.append(delimiter + "longName=" + this.getLongName());
		sb.append(delimiter + "format=" + this.getFormat());
		sb.append(delimiter + "units=" + this.getUnits());
		sb.append(delimiter + "columnIndex=" + this.getColumnIndex());
		sb.append(delimiter + "validMin=" + this.getValidMin());
		sb.append(delimiter + "validMax=" + this.getValidMax());
		sb.append(delimiter + "missingValue=" + this.getMissingValue());
		sb.append(delimiter + "accuracy=" + this.getAccuracy());
		sb.append(delimiter + "displayMin=" + this.getDisplayMin());
		sb.append(delimiter + "displayMax=" + this.getDisplayMax());
		sb.append(delimiter + "referenceScale=" + this.getReferenceScale());
		sb.append(delimiter + "conversionScale=" + this.getConversionScale());
		sb.append(delimiter + "conversionOffset=" + this.getConversionOffset());
		sb.append(delimiter + "convertedUnits=" + this.getConvertedUnits());
		sb.append(delimiter + "sourceSensorID=" + this.getSourceSensorID());
		sb.append(delimiter + "parseRegExp=" + this.getParseRegExp());
		return sb.toString();
	}

	/**
	 * @see moos.ssds.model.ValueObject#setValuesFromStringRepresentation
	 * @param stringRepresentation
	 * @param delimiter
	 */
	public void setValuesFromStringRepresentation(String stringRepresentation,
			String delimiter) throws MetadataException {
		StringTokenizer stok = new StringTokenizer(stringRepresentation,
				delimiter);
		String firstToken = stok.nextToken();
		if ((!this.getClass().getName().equals(firstToken))
				&& (!this.getClass().getName().equals(
						"moos.ssds.metadata." + firstToken)))
			throw new IllegalArgumentException(
					"The class specified by the first token (" + firstToken
							+ " does not match this class "
							+ this.getClass().getName());
		while (stok.hasMoreTokens()) {
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

			if (key.equalsIgnoreCase("id")) {
				try {
					this.setId(new Long(value));
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException(
							"Could not convert the value for id (" + value
									+ ") to a Long");
				}
			} else if (key.equalsIgnoreCase("name")) {
				this.setName(value);
			} else if (key.equalsIgnoreCase("description")) {
				this.setDescription(value);
			} else if (key.equalsIgnoreCase("longName")) {
				this.setLongName(value);
			} else if (key.equalsIgnoreCase("format")) {
				this.setFormat(value);
			} else if (key.equalsIgnoreCase("units")) {
				this.setUnits(value);
			} else if (key.equalsIgnoreCase("columnIndex")) {
				try {
					this.setColumnIndex(Long.parseLong(value));
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException(
							"The value of "
									+ value
									+ " for columnIndex could not be converted to a long");
				}
			} else if (key.equalsIgnoreCase("validMin")) {
				this.setValidMin(value);
			} else if (key.equalsIgnoreCase("validMax")) {
				this.setValidMax(value);
			} else if (key.equalsIgnoreCase("missingValue")) {
				this.setMissingValue(value);
			} else if (key.equalsIgnoreCase("accuracy")) {
				this.setAccuracy(value);
			} else if (key.equalsIgnoreCase("displayMin")) {
				try {
					this.setDisplayMin(Double.valueOf(value));
				} catch (NumberFormatException e) {
					throw new MetadataException(
							"The value specified for displayMin (" + value
									+ ") could not be converted to a Double: "
									+ e.getMessage());
				}
			} else if (key.equalsIgnoreCase("displayMax")) {
				try {
					this.setDisplayMax(Double.valueOf(value));
				} catch (NumberFormatException e) {
					throw new MetadataException(
							"The value specified for displayMax (" + value
									+ ") could not be converted to a Double: "
									+ e.getMessage());
				}
			} else if (key.equalsIgnoreCase("referenceScale")) {
				this.setReferenceScale(value);
			} else if (key.equalsIgnoreCase("conversionScale")) {
				try {
					this.setConversionScale(Double.valueOf(value));
				} catch (NumberFormatException e) {
					throw new MetadataException(
							"The value specified for conversionScale (" + value
									+ ") could not be converted to a Double: "
									+ e.getMessage());
				}
			} else if (key.equalsIgnoreCase("conversionOffset")) {
				try {
					this.setConversionOffset(Double.valueOf(value));
				} catch (NumberFormatException e) {
					throw new MetadataException(
							"The value specified for conversionOffset ("
									+ value
									+ ") could not be converted to a Double: "
									+ e.getMessage());
				}
			} else if (key.equalsIgnoreCase("convertedUnits")) {
				this.setConvertedUnits(value);
			} else if (key.equalsIgnoreCase("sourceSensorID")) {
				try {
					this.setSourceSensorID(Long.valueOf(value));
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException(
							"The value of "
									+ value
									+ " for sourceSensorID could not be converted to a Long");
				}
			} else if (key.equalsIgnoreCase("parseRegExp")) {
				this.setParseRegExp(value);
			} else {
				throw new IllegalArgumentException(
						"The attribute specified by "
								+ key
								+ " is not a recognized field of RecordVariable");
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
		name = (String) in.readObject();
		description = (String) in.readObject();
		longName = (String) in.readObject();
		format = (String) in.readObject();
		units = (String) in.readObject();
		columnIndex = in.readLong();
		validMin = (String) in.readObject();
		validMax = (String) in.readObject();
		missingValue = (String) in.readObject();
		accuracy = (String) in.readObject();
		displayMin = (Double) in.readObject();
		displayMax = (Double) in.readObject();
		referenceScale = (String) in.readObject();
		conversionScale = (Double) in.readObject();
		conversionOffset = (Double) in.readObject();
		convertedUnits = (String) in.readObject();
		sourceSensorID = (Long) in.readObject();
		parseRegExp = (String) in.readObject();
	}

	/**
	 * This is the method to serialize a RecordVariable to a custom serialized
	 * form
	 * 
	 * @see Externalizable#writeExternal(ObjectOutput)
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(id);
		out.writeObject(name);
		out.writeObject(description);
		out.writeObject(longName);
		out.writeObject(format);
		out.writeObject(units);
		out.writeLong(columnIndex);
		out.writeObject(validMin);
		out.writeObject(validMax);
		out.writeObject(missingValue);
		out.writeObject(accuracy);
		out.writeObject(displayMin);
		out.writeObject(displayMax);
		out.writeObject(referenceScale);
		out.writeObject(conversionScale);
		out.writeObject(conversionOffset);
		out.writeObject(convertedUnits);
		out.writeObject(sourceSensorID);
		out.writeObject(parseRegExp);
	}

	/**
	 * This method overrides the clone method to produce a copy of an object. It
	 * will clear the ID on the newly created copy to prevent odd behavior.
	 */
	protected Object clone() throws CloneNotSupportedException {
		// Create the clone
		RecordVariable clone = new RecordVariable();

		// Set the fields
		try {
			clone.setId(null);
			clone.setName(this.getName());
			clone.setDescription(this.getDescription());
			clone.setLongName(this.getLongName());
			clone.setFormat(this.getFormat());
			clone.setUnits(this.getUnits());
			clone.setColumnIndex(this.getColumnIndex());
			clone.setValidMin(this.getValidMin());
			clone.setValidMax(this.getValidMax());
			clone.setMissingValue(this.getMissingValue());
			clone.setAccuracy(this.getAccuracy());
			clone.setDisplayMin(this.getDisplayMin());
			clone.setDisplayMax(this.getDisplayMax());
			clone.setReferenceScale(this.getReferenceScale());
			clone.setConversionScale(this.getConversionScale());
			clone.setConversionOffset(this.getConversionOffset());
			clone.setConvertedUnits(this.getConvertedUnits());
			clone.setSourceSensorID(this.getSourceSensorID());
			clone.setParseRegExp(this.getParseRegExp());
		} catch (MetadataException e) {
		}

		// Now return the clone
		return clone;
	}

	/**
	 * This method returns a clone of the <code>RecordVariable</code> that has
	 * deep copies of the <code>StandardVariable</code>,
	 * <code>StandardUnit</code>, <code>StandardReferenceScale</code>,
	 * <code>StandardKeyword</code>, and <code>StandardDomain</code> filled
	 * out
	 */
	public IMetadataObject deepCopy() throws CloneNotSupportedException {
		// Grab the clone
		RecordVariable deepClone = (RecordVariable) this.clone();

		// Set the relationships
		if (this.getStandardVariable() != null) {
			deepClone.setStandardVariable((StandardVariable) this
					.getStandardVariable().deepCopy());
		}
		if (this.getStandardUnit() != null) {
			deepClone.setStandardUnit((StandardUnit) this.getStandardUnit()
					.deepCopy());
		}
		if (this.getStandardDomain() != null) {
			deepClone.setStandardDomain((StandardDomain) this
					.getStandardDomain().deepCopy());
		}
		if (this.getStandardReferenceScale() != null) {
			deepClone.setStandardReferenceScale((StandardReferenceScale) this
					.getStandardReferenceScale().deepCopy());
		}
		if (this.getStandardKeyword() != null) {
			deepClone.setStandardKeyword((StandardKeyword) this
					.getStandardKeyword().deepCopy());
		}
		// Return the deep clone
		return deepClone;
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
	private String longName;

	/**
	 * TODO KJG - Document this
	 */
	private String format;

	/**
	 * TODO KJG - Document this
	 */
	private String units;

	/**
	 * TODO KJG - Document this
	 */
	private long columnIndex = -1;

	/**
	 * TODO KJG - Document this
	 */
	private String validMin;

	/**
	 * TODO KJG - Document this
	 */
	private String validMax;

	/**
	 * TODO KJG - Document this
	 */
	private String missingValue;

	/**
	 * TODO KJG - Document this
	 */
	private String accuracy;

	/**
	 * TODO KJG - Document this
	 */
	private Double displayMin;

	/**
	 * TODO KJG - Document this
	 */
	private Double displayMax;

	/**
	 * TODO KJG - Document this
	 */
	private String referenceScale;

	/**
	 * TODO KJG - Document this
	 */
	private Double conversionScale;

	/**
	 * TODO KJG - Document this
	 */
	private Double conversionOffset;

	/**
	 * TODO KJG - Document this
	 */
	private String convertedUnits;

	/**
	 * TODO KJG - Document this
	 */
	private Long sourceSensorID;

	/**
	 * TODO KJG - Document this
	 */
	private String parseRegExp;

	/**
	 * TODO KJG - Document this
	 * 
	 * @directed true
	 * @label unlazy
	 */
	private StandardVariable standardVariable;

	/**
	 * TODO KJG - Document this
	 * 
	 * @directed true
	 * @label unlazy
	 */
	private StandardUnit standardUnit;

	/**
	 * TODO KJG - Document this
	 * 
	 * @directed true
	 * @label unlazy
	 */
	private StandardReferenceScale standardReferenceScale;

	/**
	 * TODO KJG - Document this
	 * 
	 * @directed true
	 * @label unlazy
	 */
	private StandardDomain standardDomain;

	/**
	 * TODO KJG - Document this
	 * 
	 * @directed true
	 * @label unlazy
	 */
	private StandardKeyword standardKeyword;

	/**
	 * This is the hibernate version that is used to check for dirty objects
	 */
	private long version = -1;

	/**
	 * A log4J logger
	 */
	static Logger logger = Logger.getLogger(RecordVariable.class);

}