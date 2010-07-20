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
	 * A log4J logger
	 */
	static Logger logger = Logger.getLogger(RecordVariable.class);

	/**
	 * This is the version that we can control for serialization purposes
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * This unique persistence mechanism ID for the <code>RecordVariable</code>
	 */
	private Long id;

	/**
	 * The name of the <code>RecordVariable</code>
	 */
	private String name;

	/**
	 * The description of the <code>RecordVariable</code>
	 */
	private String description;

	/**
	 * The long name associated with the <code>RecordVariable</code>
	 */
	private String longName;

	/**
	 * The format of the <code>RecordVariable</code>
	 */
	private String format;

	/**
	 * A string representation of the units for the <code>RecordVariable</code>
	 */
	private String units;

	/**
	 * This is the column in a data record in which this
	 * <code>RecordVariable</code> resides (used in parsing records)
	 */
	private long columnIndex = -1;

	/**
	 * This is the minimum value at which this <code>RecordVariable</code> is
	 * considered valid
	 */
	private String validMin;

	/**
	 * This is the maximum value at which this <code>RecordVariable</code> is
	 * considered valid
	 */
	private String validMax;

	/**
	 * This is the value that means the value for the
	 * <code>RecordVariable</code> was not defined
	 */
	private String missingValue;

	/**
	 * This is the accuracy associated with the <code>RecordVariable</code>
	 */
	private String accuracy;

	/**
	 * This is the minimum value to be used when displaying data of this
	 * <code>RecordVariable</code>
	 */
	private Double displayMin;

	/**
	 * This is them maximum value to be used when displaying data of this
	 * <code>RecordVariable</code>
	 */
	private Double displayMax;

	/**
	 * The reference scale associated with this <code>RecordVariable</code>
	 */
	private String referenceScale;

	/**
	 * This is the scale of conversion for this <code>RecordVariable</code>
	 */
	private Double conversionScale;

	/**
	 * The offset to use in the conversion of this <code>RecordVariable</code>
	 */
	private Double conversionOffset;

	/**
	 * These are the units of the converted value of this
	 * <code>RecordVariable</code>
	 */
	private String convertedUnits;

	/**
	 * This is the SSDS ID of the sensor that generates this
	 * <code>RecordVariable</code>
	 */
	private Long sourceSensorID;

	/**
	 * The regular expression used to parse the data from the column in which it
	 * is located in the data record
	 */
	private String parseRegExp;

	/**
	 * The <code>StandardVariable</code> that is associated with the
	 * <code>RecordVariable</code>
	 */
	private StandardVariable standardVariable;

	/**
	 * The <code>StandardUnit</code> associated with the units of this
	 * <code>RecordVariable</code>
	 */
	private StandardUnit standardUnit;

	/**
	 * The <code>StandardReferenceScale</code> associated with the
	 * <code>RecordVariable</code>
	 */
	private StandardReferenceScale standardReferenceScale;

	/**
	 * The <code>StandardDomain</code> associated with the
	 * <code>RecordVariable</code>
	 */
	private StandardDomain standardDomain;

	/**
	 * The <code>StandardKeyword</code> associated with the
	 * <code>RecordVariable</code>
	 */
	private StandardKeyword standardKeyword;

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
	 * This is the method to re-constitute and object from a custom
	 * serialization form
	 * 
	 * @see Externalizable#readExternal(ObjectInput)
	 */
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		accuracy = (String) in.readObject();
		columnIndex = (Long) in.readObject();
		conversionOffset = (Double) in.readObject();
		conversionScale = (Double) in.readObject();
		convertedUnits = (String) in.readObject();
		description = (String) in.readObject();
		displayMax = (Double) in.readObject();
		displayMin = (Double) in.readObject();
		format = (String) in.readObject();
		// Read in ID
		Object idObject = in.readObject();
		if (idObject instanceof Integer) {
			Integer intId = (Integer) idObject;
			id = new Long(intId.longValue());
		} else if (idObject instanceof Long) {
			id = (Long) idObject;
		}
		longName = (String) in.readObject();
		missingValue = (String) in.readObject();
		name = (String) in.readObject();
		parseRegExp = (String) in.readObject();
		referenceScale = (String) in.readObject();
		sourceSensorID = (Long) in.readObject();
		standardDomain = (StandardDomain) in.readObject();
		standardKeyword = (StandardKeyword) in.readObject();
		standardReferenceScale = (StandardReferenceScale) in.readObject();
		standardUnit = (StandardUnit) in.readObject();
		standardVariable = (StandardVariable) in.readObject();
		units = (String) in.readObject();
		validMax = (String) in.readObject();
		validMin = (String) in.readObject();
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
	 * This is the method to serialize a RecordVariable to a custom serialized
	 * form
	 * 
	 * @see Externalizable#writeExternal(ObjectOutput)
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(accuracy);
		out.writeObject(columnIndex);
		out.writeObject(conversionOffset);
		out.writeObject(conversionScale);
		out.writeObject(convertedUnits);
		out.writeObject(description);
		out.writeObject(displayMax);
		out.writeObject(displayMin);
		out.writeObject(format);
		out.writeObject(id);
		out.writeObject(longName);
		out.writeObject(missingValue);
		out.writeObject(name);
		out.writeObject(parseRegExp);
		out.writeObject(referenceScale);
		out.writeObject(sourceSensorID);
		// StandardDomain
		out.writeObject(standardDomain);
		// StandardKeyword
		out.writeObject(standardKeyword);
		// StandardReferenceScale
		out.writeObject(standardReferenceScale);
		// StandardUnit
		out.writeObject(standardUnit);
		// StandardVarible
		out.writeObject(standardVariable);
		out.writeObject(units);
		out.writeObject(validMax);
		out.writeObject(validMin);
		out.writeObject(version);
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
	 * <code>StandardKeyword</code>, and <code>StandardDomain</code> filled out
	 */
	public IMetadataObject deepCopy() throws CloneNotSupportedException {
		logger.debug("deepCopy called");
		// Grab the clone
		RecordVariable deepClone = (RecordVariable) this.clone();
		logger.debug("The following clone was created:");
		logger.debug(deepClone.toStringRepresentation("|"));

		// Set the relationships
		if (this.getStandardVariable() != null) {
			StandardVariable clonedStandardVariable = (StandardVariable) this
					.getStandardVariable().deepCopy();
			logger
					.debug("The following cloned StandardVariable will be added:");
			logger.debug(clonedStandardVariable.toStringRepresentation("|"));
			deepClone.setStandardVariable(clonedStandardVariable);
		}
		if (this.getStandardUnit() != null) {
			StandardUnit clonedStandardUnit = (StandardUnit) this
					.getStandardUnit().deepCopy();
			logger.debug("The following cloned StandardUnit will be added:");
			logger.debug(clonedStandardUnit.toStringRepresentation("|"));
			deepClone.setStandardUnit(clonedStandardUnit);
		}
		if (this.getStandardDomain() != null) {
			StandardDomain clonedStandardDomain = (StandardDomain) this
					.getStandardDomain().deepCopy();
			logger.debug("The following cloned StandardDomain will be added:");
			logger.debug(clonedStandardDomain.toStringRepresentation("|"));
			deepClone.setStandardDomain(clonedStandardDomain);
		}
		if (this.getStandardReferenceScale() != null) {
			StandardReferenceScale clonedStandardReferenceScale = (StandardReferenceScale) this
					.getStandardReferenceScale().deepCopy();
			logger
					.debug("The following cloned StandardReferenceScale will be added:");
			logger.debug(clonedStandardReferenceScale
					.toStringRepresentation("|"));
			deepClone.setStandardReferenceScale(clonedStandardReferenceScale);
		}
		if (this.getStandardKeyword() != null) {
			StandardKeyword clonedStandardKeyword = (StandardKeyword) this
					.getStandardKeyword().deepCopy();
			logger.debug("The following cloned StandardKeyword will be added:");
			logger.debug(clonedStandardKeyword.toStringRepresentation("|"));
			deepClone.setStandardKeyword(clonedStandardKeyword);
		}
		// Return the deep clone
		return deepClone;
	}
}