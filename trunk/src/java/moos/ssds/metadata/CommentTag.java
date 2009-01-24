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
 * This class represents a <code>String</code> that is often used to signify a
 * line of comments in a <code>DataContainer</code>
 * <hr>
 * 
 * @stereotype thing
 * @hibernate.class table="CommentTag"
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.7 $
 */
public class CommentTag implements IMetadataObject {

	/**
	 * This is the default (no argument) constructor
	 */
	public CommentTag() {
	}

	/**
	 * This is the constructor that takes in a string that represents the
	 * comment tag
	 * 
	 * @param tag
	 *            The comment text
	 */
	public CommentTag(String tag) {
		this.tagString = tag;
	}

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
	 * These methods get and set the tag text that is in this
	 * <code>CommentTag</code>. This comment tag must be less than 10
	 * characters
	 * 
	 * @hibernate.property length="10"
	 * @return A <code>String</code> that is the text of the
	 *         <code>CommentTag</code>
	 * @throws MetadataException
	 *             if the value is longer than 10 characters
	 */
	public String getTagString() {
		return tagString;
	}

	public void setTagString(String tagString) throws MetadataException {
		MetadataValidator.isStringShorterThan(tagString,
				MetadataValidator.COMMENT_TAG_LENGTH);
		this.tagString = tagString;
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
		if (!(obj instanceof CommentTag))
			return false;

		// Cast to CommentTag object
		final CommentTag that = (CommentTag) obj;

		// Now check for missing business key (tag string)
		if ((this.tagString == null) || (that.getTagString() == null))
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
		// Calculate hashcode
		int result = 47;
		if (tagString != null)
			result = result + tagString.hashCode();

		// Return it
		return result;
	}

	/**
	 * A method that over-rides the toString method and just returns the tag
	 * string of this <code>CommentTag</code>
	 * 
	 * @return A string that is the TagString of the <code>CommentTag</code>
	 */
	public String toString() {
		return tagString;
	}

	/**
	 * @see IMetadataObject#toStringRepresentation(String)
	 */
	public String toStringRepresentation(String delimiter) {
		if (delimiter == null)
			delimiter = IMetadataObject.DEFAULT_DELIMITER;
		StringBuffer sb = new StringBuffer();
		sb.append("CommentTag");
		sb.append(delimiter + "id=" + this.getId());
		sb.append(delimiter + "tagString=" + this.getTagString());
		return sb.toString();
	}

	/**
	 * In order to use the class, you should first create an empty object, then
	 * call this method, passing in the string representation. As an example:
	 * 
	 * <pre>
	 * CommentTag newCommentTag = new CommentTag();
	 * 
	 * newCommentTag.setValuesFromStringRepresentation(&quot;CommentTag|tagString=#&quot;, &quot;|&quot;);
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
			} else if (key.equalsIgnoreCase("tagString")) {
				this.setTagString(value);
			} else {
				throw new MetadataException("The attribute specified by " + key
						+ " is not a recognized field of "
						+ this.getClass().getName());
			}
		}
	}

	/**
	 * Method for taking a input stream and re-constituting the object
	 * 
	 * @see Externalizable#readExternal(ObjectInput)
	 */
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		id = (Long) in.readObject();
		tagString = (String) in.readObject();
	}

	/**
	 * The method for custom serialization of the object
	 * @see Externalizable#writeExternal(ObjectOutput)
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(id);
		out.writeObject(tagString);
	}

	/**
	 * This method overrides the clone method to produce a copy of an object. It
	 * will clear the ID on the newly created copy to prevent odd behavior.
	 */
	protected Object clone() throws CloneNotSupportedException {
		// Create the clone
		CommentTag clone = new CommentTag();

		// Copy over the fields
		clone.setId(null);
		try {
			clone.setTagString(this.getTagString());
		} catch (MetadataException e) {
		}

		// Now return the clone
		return clone;
	}

	/**
	 * For CommentTags, this simply returns the result of the
	 * <code>clone()</code> operation.
	 */
	public IMetadataObject deepCopy() throws CloneNotSupportedException {
		return (CommentTag) this.clone();
	}

	/**
	 * This is the version that we can control for serialization purposes
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * This is the unique identifier of the <code>CommentTag</code>
	 */
	private Long id;

	/**
	 * This is the String that represents the indicator of a line of comments
	 */
	private String tagString;

	/**
	 * This is the hibernate version that is used to check for dirty objects
	 */
	private long version = -1;

	/**
	 * This is a Log4JLogger that is used to log information to
	 */
	static Logger logger = Logger.getLogger(CommentTag.class);

}