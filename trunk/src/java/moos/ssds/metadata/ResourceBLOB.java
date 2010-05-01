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

import org.apache.log4j.Logger;

import moos.ssds.metadata.util.MetadataException;
import moos.ssds.metadata.util.MetadataValidator;

/**
 * This class is simply a place to store a resource in the persistent store.
 * <hr>
 * 
 * @stereotype thing
 * @hibernate.class table="ResourceBLOB"
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.10 $
 */
public class ResourceBLOB implements IMetadataObject, IDescription {

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
	 * @hibernate.column name="name" length="2048" not-null="true"
	 */
	public String getName() {
		return name;
	}

	/**
	 * @see IDescription#setName(String)
	 */
	public void setName(String name) throws MetadataException {
		MetadataValidator.isStringShorterThan(name, 2048);
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
	 * @hibernate.property length="2048"
	 * @return
	 */
	public byte[] getByteArray() {
		return byteArray;
	}

	public void setByteArray(byte[] byteArray) throws MetadataException {
		if (byteArray.length > 2048)
			throw new MetadataException(
					"The byte array specified is larger than 2048 bytes, please shorten");
		this.byteArray = byteArray;
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

	public String toStringRepresentation(String delimiter) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setValuesFromStringRepresentation(String stringRepresentation,
			String delimiter) throws MetadataException {
		// TODO Auto-generated method stub

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
		int byteArrayLength = in.readInt();
		byteArray = new byte[byteArrayLength];
		in.read(byteArray);
	}

	/**
	 * This is the method to serialize a ResourceBLOB to a custom serialized
	 * form
	 * 
	 * @see Externalizable#writeExternal(ObjectOutput)
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(id);
		out.writeObject(name);
		out.writeObject(description);
		if (byteArray == null || byteArray.length == 0) {
			out.writeInt(0);
		} else {
			out.writeInt(byteArray.length);
		}
		out.write(byteArray);
	}

	/**
	 * This method overrides the clone method to produce a copy of an object. It
	 * will clear the ID on the newly created copy to prevent odd behavior.
	 */
	protected Object clone() throws CloneNotSupportedException {
		// Create clone
		ResourceBLOB clone = new ResourceBLOB();

		// Set the fields
		try {
			clone.setId(null);
			clone.setName(this.getName());
			clone.setDescription(this.getDescription());
			clone.setByteArray(this.getByteArray());
		} catch (MetadataException e) {
		}

		// Return the clone
		return clone;
	}

	/**
	 * This method simply returns a clone of the object
	 */
	public IMetadataObject deepCopy() throws CloneNotSupportedException {
		ResourceBLOB clonedResourceBLOB = (ResourceBLOB) this.clone();
		logger.debug("deepCopy called and will return clone:");
		logger.debug(clonedResourceBLOB.toStringRepresentation("|"));
		return clonedResourceBLOB;
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
	 * This is the arbitrary name given to the DataProducer
	 */
	private String name;

	/**
	 * The description of the DataProducer
	 */
	private String description;

	/**
	 * This is the byte array that can store any resource in the form of bytes
	 */
	private byte[] byteArray;

	/**
	 * This is the hibernate version that is used to check for dirty objects
	 */
	private long version = -1;

	/**
	 * This is a Log4JLogger that is used to log information to
	 */
	static Logger logger = Logger.getLogger(ResourceBLOB.class);

}
