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
import java.io.Serializable;

import moos.ssds.metadata.util.MetadataException;

/**
 * This interface defines the methods that are necessary for object to be
 * handled by the data access and other services. For the data access services
 * to work correctly, each class must have an ID associated with it and it must
 * override the equals and hashcode methods to implement the idea of a "business
 * key". The business key is different from the JVM object identity and the
 * persistence ID in that it can be used to compare two object for equal values
 * even if the object identities are not equal and also for the cases where the
 * persistenceID is null. For serialization and deserialization of the objects
 * outside Java, classes that implement this interface must define their own
 * methods to serialize and deserialize their state. This does not include the
 * relationship, only the attributes within the class.
 * <hr>
 * 
 * @stereotype thing
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.3 $
 */
public interface IMetadataObject extends Serializable, Cloneable,
		Externalizable {

	/**
	 * This is the value that is used for the default delimiter for the string
	 * representation methods (when no delimiter is specified).
	 */
	final static String DEFAULT_DELIMITER = "|";

	/**
	 * This method returns the unique persistence store identifier of the
	 * instance of the class. It has no business meaning, it is solely for the
	 * persistence mechanisms ability to manage object state.
	 * 
	 * @return a <code>java.lang.Long</code> that is the persistence mechanisms
	 *         unique identifier.
	 */
	Long getId();

	/**
	 * This method is used to set the unique identifier for the persistence
	 * layer. This should <b>RARELY</b> be used by clients as the persistence
	 * layer handles object state management.
	 * 
	 * @param id
	 *            is a <code>java.lang.Long</code> that is the persistence
	 *            layer's unique identification to link it to the information in
	 *            the storage mechanism.
	 */
	void setId(Long id);

	/**
	 * This method overrides the default <code>equals</code> method of
	 * <code>java.lang.Object</code> and must be implemented for persistent
	 * classes to check for "business keys". Business keys are unique keys that
	 * can be used to compare two objects to determine if they are equal. With
	 * persistence layers, there can be three levels of equals that it must
	 * account for: the JVM equals (reference points to same memory space),
	 * persistent identifier equivalence, and what is called the "business key"
	 * identity. The JVM equals and the persitence identifier equals can both
	 * fail, but the object can still be equal in some value that is determined
	 * by the business logic. This would also be considered a what is often
	 * called a "natural key" in ORM technologies. It must be implemented by
	 * each class for the persistence layer to work correctly.
	 * 
	 * @param object
	 *            is the <code>java.lang.Object</code> that will be used to
	 *            compare to the current instance. It may be compared at several
	 *            levels: JVM equivalence, persistence identifier equivalence,
	 *            and business key equivalence.
	 * @return a <code>boolean</code> to indicate if the two object are
	 *         considered equal (<code>true</code>) or not (<code>false</code>).
	 */
	boolean equals(Object object);

	/**
	 * This method overrides the <code>hashCode</code> method of
	 * <code>java.lang.Object</code> and needs to be overridden due to the fact
	 * that the <code>equals</code> method is overridden. The hashcode should be
	 * derived from the business key that is talked about in the description of
	 * the <code>equals</code> method. In short if two objects pass the equals
	 * defined by the overridden equals method, they must return the same exact
	 * hashCode.
	 * 
	 * @return the hashcode for the object
	 */
	int hashCode();

	/**
	 * This method returns a string that contains a textual description of what
	 * object it is and what the current status of the object's attributes are
	 * (separated by delimiters). NOTE to developers. <b>Be VERY careful when
	 * you change this method as it is an interface contract with other
	 * applications. Users will write applications to this method. It might be
	 * wise to do things like only add new attributes to the end of the
	 * string.</b>
	 * 
	 * @param delimiter
	 *            is the <code>String</code> that will be used to separate the
	 *            different attributes of the object. If this is null a default
	 *            delimiter will be used
	 * @return a <code>String</code> that is contains the object type and the
	 *         values of its attributes. It takes the form of: <br>
	 *         <code>ClassName|attribute1=value1|attribute2=value2|...</code> <br>
	 *         where the | character is the default delimiter, but will be
	 *         replaced with the specified delimiter (if not null)
	 */
	String toStringRepresentation(String delimiter);

	/**
	 * This method takes in a string that specifies the type of object and the
	 * values of the attributes by their names. This is useful in constructing
	 * objects from the serialized string format. NOTE to developers. Be VERY
	 * careful when you change this method as it is an interface contract with
	 * other applications. Users will write applications to this method.
	 * 
	 * @param stringRepresentation
	 *            is the <code>java.lang.String</code> that contains the class
	 *            name and the attribute names and value. The class is
	 *            reponsible for de-serializing that into an instance of the
	 *            object. It takes the form of: <br>
	 *            <code>ClassName|attribute1=value1|attribute2=value2|...</code> <br>
	 *            where the | character is the default delimiter, but will be
	 *            replaced with the specified delimiter (if not null)
	 * @param delimiter
	 *            This is a <code>java.lang.String</code> that tells the method
	 *            where the different fields are by specifying what separates
	 *            them.
	 * @throws MetadataException
	 */
	void setValuesFromStringRepresentation(String stringRepresentation,
			String delimiter) throws MetadataException;

	/**
	 * This method will return a "deep" copy of the object it is called on. This
	 * means different things for different objects so consult the documentation
	 * on the implemented method to see what is returned.
	 * 
	 * @return
	 */
	public IMetadataObject deepCopy() throws CloneNotSupportedException;

}