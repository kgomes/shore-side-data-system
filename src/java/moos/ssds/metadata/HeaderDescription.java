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
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import moos.ssds.metadata.util.MetadataException;
import moos.ssds.metadata.util.MetadataValidator;

/**
 * This class represents information about the header in a
 * <code>DataContainer</code>.
 * <hr>
 * 
 * @stereotype description
 * @hibernate.class table="HeaderDescription"
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.12 $
 */
public class HeaderDescription implements IMetadataObject {

	/**
	 * This is a Log4JLogger that is used to log information to
	 */
	static Logger logger = Logger.getLogger(HeaderDescription.class);

	/**
	 * This is the version that we can control for serialization purposes
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * This is the persistence layer identifier
	 */
	private Long id;

	/**
	 * This is a string that describes the <code>HeaderDescription</code>
	 */
	private String description;

	/**
	 * This is the number of bytes that the header is offset in the
	 * <code>DataContainer</code>
	 */
	private long byteOffset = 0;

	/**
	 * This is the number of lines that make up the header section of the
	 * <code>DataContainer</code>.
	 */
	private int numHeaderLines = 0;

	/**
	 * This is a collection that contains all the tags that signify comments in
	 * the header description
	 */
	private Collection<CommentTag> commentTags = new HashSet<CommentTag>();

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
	 * These methods get and set the number of bytes that the
	 * <code>HeaderDescription</code> is located at in the
	 * <code>DataContainer</code>
	 * 
	 * @hibernate.property
	 * @return the number of bytes
	 */
	public long getByteOffset() {
		return byteOffset;
	}

	public void setByteOffset(long byteOffset) {
		this.byteOffset = byteOffset;
	}

	/**
	 * This method is used to set the number of bytes that the
	 * <code>HeaderDescription</code> is offset in the
	 * <code>DataContainer</code> but takes a string representation of that
	 * number as a parameter
	 * 
	 * @param byteOffset
	 *            is a string representation of the number of bytes the
	 *            <code>HeaderDesription</code> is offset
	 */
	public void setByteOffset(String byteOffset) {
		setByteOffset(Long.parseLong(byteOffset));
	}

	/**
	 * This method indicates whether or not this <code>HeaderDescription</code>
	 * has a byteOffset in the <code>DataContainer</code>
	 * 
	 * @return a boolean that is <b>true</b> if there is a byte offset,
	 *         <b>false</b> otherwise.
	 */
	public boolean hasByteOffset() {
		return (byteOffset > 0) ? true : false;
	}

	/**
	 * These methods get and set the number of lines that this
	 * <code>HeaderDescription</code> spans.
	 * 
	 * @hibernate.property
	 * @return An integer representing the number of lines the
	 *         <code>HeaderDescription</code> spans
	 */
	public int getNumHeaderLines() {
		return numHeaderLines;
	}

	public void setNumHeaderLines(int numHeaderLines) {
		this.numHeaderLines = numHeaderLines;
	}

	/**
	 * This method sets the number of header lines, but takes a string
	 * representation of that number as an input
	 * 
	 * @param numHeaderLines
	 *            a String representation of the number of lines this
	 *            <code>HeaderDescription</code> spans
	 */
	public void setNumHeaderLines(String numHeaderLines) {
		setNumHeaderLines(Integer.parseInt(numHeaderLines));
	}

	/**
	 * This returns a boolean that indicates if there are header lines in this
	 * <code>HeaderDescription</code>
	 * 
	 * @return a boolean that is <b>true</b> if there are one or more header
	 *         lines, <b>false</b> otherwise
	 */
	public boolean hasHeaderLines() {
		return (numHeaderLines != 0) ? true : false;
	}

	/**
	 * These methods get and set the collection of <code>CommentTag</code>s for
	 * this <code>HeaderDescription</code>.
	 * 
	 * @hibernate.set cascade="all" lazy="false" inverse="true"
	 *                outer-join="true"
	 * @hibernate.collection-key column="HeaderDescriptionID_FK"
	 * @hibernate.collection-one-to-many class="moos.ssds.metadata.CommentTag"
	 * @return A collection of the <code>CommentTag<code>s
	 */
	public Collection<CommentTag> getCommentTags() {
		return this.commentTags;
	}

	/**
	 * This methods sets the <code>CommentTag</code>s that are associated with
	 * the <code>HeaderDescription</code>
	 * 
	 * @param commentTags
	 */
	protected void setCommentTags(Collection<CommentTag> commentTags) {
		this.commentTags = commentTags;
	}

	/**
	 * This method converts all the <code>CommentTag</code> objects to
	 * <code>String</code>s and then returns the collection of
	 * <code>String</code>s.
	 * 
	 * @return a <code>Collection</code> of <code>String</code>s.
	 */
	public Collection<String> getCommentTagsAsStrings() {
		Iterator<CommentTag> i = commentTags.iterator();
		HashSet<String> al = new HashSet<String>();
		CommentTag ct = null;
		while (i.hasNext()) {
			ct = (CommentTag) i.next();
			al.add(ct.getTagString());
		}
		return al;
	}

	/**
	 * This method returns the result of a check to see if any
	 * <code>CommentTag</code>s are present in the
	 * <code>HeaderDescription</code>.
	 * 
	 * @return a boolean that is <b>true</b> if there are
	 *         <code>CommentTag</code>s and <b>false</b> if not
	 */
	public boolean hasCommentTags() {
		return (commentTags.size() > 0) ? true : false;
	}

	/**
	 * This method adds the given <code>CommentTag</code> to the collection
	 * associated with the <code>HeaderDescription</code>.
	 * 
	 * @param commentTag
	 *            the <code>CommentTag</code> to add
	 */
	public void addCommentTag(CommentTag commentTag) {
		// If null was passed in, just return
		if (commentTag == null)
			return;

		// Now add the KeyWord to the collection
		if (!this.commentTags.contains(commentTag)) {
			this.commentTags.add(commentTag);
		}
	}

	/**
	 * This method calls addCommentTag(CommentTag) and converts the incoming
	 * string to a CommentTag object first
	 * 
	 * @param commentTag
	 *            the <code>String</code> that will be converted to a
	 *            <code>CommentTag</code> object and then passed on
	 */
	public void addCommentTag(String commentTag) {
		CommentTag ct = new CommentTag(commentTag);
		this.addCommentTag(ct);
	}

	/**
	 * This method removes the given <code>CommentTag</code> from the collection
	 * 
	 * @param commentTag
	 *            is the <code>CommentTag</code> to remove from the collection
	 */
	public void removeCommentTag(CommentTag commentTag) {
		if (commentTag == null)
			return;
		if ((this.commentTags != null)
				&& (this.commentTags.contains(commentTag))) {
			this.commentTags.remove(commentTag);
		}
	}

	/**
	 * This method takes in a <code>String</code> and converts it to a
	 * <code>CommentTag</code> and then passes it on the the
	 * removeCommentTag(CommentTag) method
	 * 
	 * @param commentTag
	 *            the <code>String</code> that will be removed from the list of
	 *            comment tags
	 */
	public void removeCommentTag(String commentTag) {
		CommentTag ct = new CommentTag(commentTag);
		this.removeCommentTag(ct);
	}

	/**
	 * This method will clear out the collection of <code>CommentTag</code>s and
	 * keep the integrity of the relationships intact.
	 */
	public void clearCommentTags() {
		this.commentTags.clear();
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
	 * @see moos.ssds.metadata.IMetadataObject#toStringRepresentation(String)
	 */
	public String toStringRepresentation(String delimiter) {
		// If the delimiter is not specified, use a default one
		if (delimiter == null)
			delimiter = IMetadataObject.DEFAULT_DELIMITER;

		StringBuffer sb = new StringBuffer();
		sb.append("HeaderDescription");
		sb.append(delimiter + "id=" + this.getId());
		sb.append(delimiter + "description=" + this.getDescription());
		sb.append(delimiter + "byteOffset=" + this.getByteOffset());
		sb.append(delimiter + "numHeaderLines=" + this.getNumHeaderLines());
		return sb.toString();
	}

	/**
	 * In order to use the class, you should first create an empty object, then
	 * call this method, passing in the string representation. As an example:
	 * 
	 * <pre>
	 * HeaderDescription newHeaderDescription = new HeaderDescription();
	 * 
	 * newHeaderDescription
	 * 		.setValuesFromStringRepresentation(
	 * 				&quot;HeaderDescription|description=A really cool CTD|byteOffset=15|numHeaderLines=10&quot;,
	 * 				&quot;|&quot;);
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
			} else if (key.equalsIgnoreCase("description")) {
				this.setDescription(value);
			} else if (key.equalsIgnoreCase("byteOffset")) {
				try {
					this.setByteOffset(Long.parseLong(value));
				} catch (NumberFormatException e) {
					throw new MetadataException(
							"The value specified for byteOffset (" + value
									+ ") could not be converted to a Long: "
									+ e.getMessage());
				}
			} else if (key.equalsIgnoreCase("numHeaderLines")) {
				try {
					this.setNumHeaderLines(Integer.parseInt(value));
				} catch (NumberFormatException e) {
					throw new MetadataException(
							"The value specified for numHeaderLines (" + value
									+ ") could not be converted to a Long: "
									+ e.getMessage());
				}
			} else {
				throw new MetadataException("The attribute specified by " + key
						+ " is not a recognized field of "
						+ this.getClass().getName());
			}
		}
	}

	/**
	 * This is the method to re-constitute an object from a custom serialization
	 * form
	 * 
	 * @see Externalizable#readExternal(ObjectInput)
	 */
	@SuppressWarnings("unchecked")
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		byteOffset = (Long) in.readObject();
		commentTags = (Collection<CommentTag>) in.readObject();
		description = (String) in.readObject();
		// Read in ID
		Object idObject = in.readObject();
		if (idObject instanceof Integer) {
			Integer intId = (Integer) idObject;
			id = new Long(intId.longValue());
		} else if (idObject instanceof Long) {
			id = (Long) idObject;
		}
		numHeaderLines = (Integer) in.readObject();
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
	 * This is the method to serialize a HeaderDescription out to a customized
	 * form
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(byteOffset);
		// Write comment Tags (null for now)
		out.writeObject(null);
		out.writeObject(description);
		out.writeObject(id);
		out.writeObject(numHeaderLines);
		out.writeObject(version);
	}

	/**
	 * This method overrides the clone method to produce a copy of an object. It
	 * will clear the ID on the newly created copy to prevent odd behavior.
	 */
	protected Object clone() throws CloneNotSupportedException {
		// Create the clone
		HeaderDescription clone = new HeaderDescription();

		// Set the fields
		try {
			clone.setId(null);
			clone.setByteOffset(this.getByteOffset());
			clone.setNumHeaderLines(this.getNumHeaderLines());
			clone.setDescription(this.getDescription());
		} catch (MetadataException e) {
		}

		// Return the clone
		return clone;
	}

	/**
	 * This methods returns a clone with deep copies of the
	 * <code>CommentTags</code> also filled out
	 */
	public IMetadataObject deepCopy() throws CloneNotSupportedException {
		logger.debug("deepCopy called");
		// Grab the clone
		HeaderDescription deepClone = (HeaderDescription) this.clone();
		logger.debug("Clone created and is:");
		logger.debug(deepClone.toStringRepresentation("|"));

		// Fill out comment tags
		if ((this.getCommentTags() != null)
				&& (this.getCommentTags().size() > 0)) {
			logger.debug("There are " + this.getCommentTags().size()
					+ " comment tags that will be cloned and attached");
			Collection<CommentTag> commentTags = this.getCommentTags();
			Iterator<CommentTag> commentTagIter = commentTags.iterator();
			while (commentTagIter.hasNext()) {
				CommentTag clonedCommentTag = (CommentTag) ((CommentTag) commentTagIter
						.next()).deepCopy();
				logger.debug("The following cloned CommentTag will be added:");
				logger.debug(clonedCommentTag.toStringRepresentation("|"));
				deepClone.addCommentTag(clonedCommentTag);
			}
		}

		// Now return the deep clone
		return deepClone;
	}
}