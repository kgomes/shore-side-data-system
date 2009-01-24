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
package moos.ssds.data.parsers;

import moos.ssds.metadata.DataContainer;
import moos.ssds.metadata.RecordDescription;

import org.apache.log4j.Logger;

/**
 * <p>
 * This class implements the <code>IParser</code> interface.
 * </p>
 * <hr>
 * 
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.2 $
 * @stereotype iterator
 */
public class Parser implements IParser {

    /**
     * The default constructor
     */
    public Parser() {}

    /**
     * This is the constructor that takes in an <code>DataContainer</code> and
     * sets up the source to read from the source described in the
     * <code>DataContainer</code>
     * 
     * @param source
     *            is the <code>DataContainer</code> that has the metadata that
     *            is used to correctly parse the source that the
     *            <code>DataContainer</code> points to.
     */
    public Parser(DataContainer source) {
        setSource(source);
    }

    /**
     * This method is here to support the <code>java.util.Iterator</code>
     * interface but is not supported by this class so it throws a
     * <code>java.lang.UnsupportedOperationException</code>
     * 
     * @see java.util.Iterator#remove()
     * @throws UnsupportedOperationException
     *             because this method is not supported by this class.
     */
    public void remove() throws UnsupportedOperationException {
        throw new UnsupportedOperationException(
            "The Parser class does not support the remove operation");
    }

    /**
     * This method implements the <code>hasNext()</code> method of the
     * Iterator interface. It tells the caller whether or not the parser thinks
     * it can provide more elements from the data source.
     * 
     * @see java.util.Iterator#hasNext()
     * @return a <code>boolean</code> to indicate if the another
     *         <code>Map</code> object can be parsed from the file. <b><font
     *         color="red">NOTE: this method does not always work properly due
     *         to the fact that the backing files are sometimes
     *         <code>java.net.URL</code> so make sure when you call the
     *         <code>next()</code> method, you check for a null return.
     */
    public boolean hasNext() {
        return context.hasNext();
    }

    /**
     * This method returns an <code>Object</code> that is a <code>Map</code>
     * class that contains keys of <code>RecordVariables</code> and their
     * correspoding values for the record that was parsed.
     */
    public Object next() {
        // Return the object from the context that is reading the file
        return context.next();
    }

    /**
     * This method returns the <code>Context</code> that is being used to
     * parse the source.
     * 
     * @return the <code>Context</code> that the parser is using to parse the
     *         file.
     */
    public ParserContext getParserContext() {
        return this.context;
    }

    /**
     * This methods sets the <code>Context</code> that the parser should use
     * to parse the file
     * 
     * @param context
     *            is the <code>Context</code> that the parser should use to
     *            parse the file
     */
    private void setContext(ParserContext context) {
        this.context = context;
    }

    /**
     * The method returns the <code>DataContainer</code> that contains all the
     * metadata about the source of the data to be parsed.
     * 
     * @return a <code>DataContainer</code> that contains the metdata about
     *         the source of the data that this parser will use to parse out
     *         data
     */
    public DataContainer getSource() {
        return source;
    }

    /**
     * This method sets the <code>DataContainer</code> that has the metadata
     * that the parser will use to parse the file. It will also fire off the
     * method to get the correct <code>Context</code> and set that to this
     * parser's <code>Context</code>
     * 
     * @param source
     *            is the <code>DataContainer</code> to use for the metadata
     *            for the parser.
     */
    public void setSource(DataContainer source) {
        // Set the DataContainer
        this.source = source;
        // Now call the static method the get the correct Context and set that
        // the parser's Context
        setContext(getContext(source));
    }

    /**
     * This method takes in a <code>DataContainer</code> and returns the
     * correct <code>ParserContext</code> to be used to parse the data source
     * that backs the <code>DataContainer</code>.
     * 
     * @param source
     *            is the <code>DataContainer</code> that contains all the
     *            appropriate metadata to describe how the backing source of
     *            data is to be parsed.
     * @return the <code>ParserContext</code> that can be used to parse the
     *         data that is pointed to by the <code>DataContainer</code>
     */
    public static ParserContext getContext(DataContainer source) {

        // Create a local pointer to the data contianer
        DataContainer tempDC = source;

        // Declare the FileContext to return
        ParserContext out = null;

        // Check to see if it is a file
        if (tempDC.getDataContainerType().equals(DataContainer.TYPE_FILE)) {
            // Grab the record description and buffer style
            RecordDescription recordDescription = tempDC.getRecordDescription();
            String bufferStyle = recordDescription.getBufferStyle();

            // Depending on the style of buffer, create the appropriate context
            if (bufferStyle
                .equalsIgnoreCase(RecordDescription.BUFFER_STYLE_BINARY)) {
                out = (new BinaryFileContext(tempDC));
            } else if (bufferStyle
                .equalsIgnoreCase(RecordDescription.BUFFER_STYLE_MULITPART_MIME)) {
                out = (new MultipartMimeFileContext(tempDC));
            } else if (bufferStyle
                .equalsIgnoreCase(RecordDescription.BUFFER_STYLE_ASCII)) {
                out = (new AsciiFileContext(tempDC));
            } else {
                return null;
            }
        } else {
            out = (new PacketParserContext(tempDC, null));
        }
        // Now return the context
        return out;
    }

    /**
     * This is the <code>ParserContext</code> that is used to correctly parse
     * the file pointed to by the <code>DataContainer</code>
     */
    private ParserContext context;

    /**
     * This is the <code>DataContainer</code> that contains the metadata to
     * correctly parse the file pointed to.
     */
    private DataContainer source;

    /**
     * The Log4J Logger
     */
    static Logger logger = Logger.getLogger(Parser.class);
}