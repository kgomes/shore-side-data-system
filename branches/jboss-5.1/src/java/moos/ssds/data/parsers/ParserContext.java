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

import java.util.Map;

import moos.ssds.metadata.DataContainer;
import moos.ssds.metadata.RecordDescription;

import org.apache.log4j.Logger;

/**
 * <p>
 * This class is in abstract class that provides the functionality to parse
 * files of different types.
 * <hr>
 * 
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.2 $
 * @stereotype role
 */
public abstract class ParserContext {

    /**
     * This method is here to support the <code>java.util.Iterator</code>
     * interface, but it is <b>NOT</b> supported in this class. This will throw
     * an exception if called.
     * 
     * @see java.util.Iterator#remove()
     */
    public void remove() throws UnsupportedOperationException {
        throw new UnsupportedOperationException(
            "The ParserContext do not support the remove operation ... sorry");
    }

    /**
     * This method returns an Object that is a <code>Map</code> that contains
     * <code>RecordVariable</code>s as keys and their respective data values
     * as values in the <code>Map</code>. This map applies to one record that
     * was parsed from the <code>DataContainer</code>
     * 
     * @return is a <code>Map</code> that contains
     *         <code>RecordVariables</code>s (keys) and their data values
     *         (value) for a single record parsed from the
     *         <code>DataContainer</code>
     */
    public abstract Object next();

    /**
     * This method implements the <code>hasNext()</code> method of the
     * Iterator interface. It tells the caller whether or not the parser thinks
     * it can provide more elements from the DataContainer.
     * 
     * @see java.util.Iterator#hasNext()
     * @return a <code>boolean</code> to indicate if the another
     *         <code>Map</code> object can be parsed from the file. <b><font
     *         color="red">NOTE: this method does not always work properly due
     *         to the fact that the backing files are sometimes
     *         <code>java.net.URL</code> so make sure when you call the
     *         <code>next()</code> method, you check for a null return.
     */
    public abstract boolean hasNext();

    /**
     * This is the method that returns the <code>DataContainer</code> that
     * contains the metadata that describes the file that this
     * <code>ParserContext</code> is working with
     * 
     * @return a <code>DataContainer</code> that contains metadata about the
     *         file to be parsed with this <code>ParserContext</code>
     */
    public DataContainer getSource() {
        return source;
    }

    /**
     * This method sets the <code>DataContainer</code> that will be used as
     * the source of metadata to help parse the data backed by the
     * <code>DataContainer</code>
     * 
     * @param source
     *            is the <code>DataContainer</code> that has the metadata
     *            necessary to parse the file and records properly
     */
    public void setSource(DataContainer source) {
        // Set the DataContainer
        this.source = source;
        // Grab the RecordDescription for easy access
        this.recordDescription = source.getRecordDescription();
    }

    /**
     * This method sets the <code>RecordParser</code> that will be used to
     * parse individual records in the <code>DataContainer</code>
     * 
     * @param recordParser
     *            is the <code>RecordParser</code> that will be used to parse
     *            individual records in the <code>DataContainer</code>
     */
    protected void setRecordParser(RecordParser recordParser) {
        this.recordParser = recordParser;
    }

    /**
     * This method returns a logging message that was built during the different
     * method calls
     * 
     * @return a <code>String</code> that is the logging message
     */
    public String getLogText() {
        return this.logText.toString();
    }

    /**
     * This method removes the existing log message and replaces it with the
     * incoming string
     * 
     * @param logText
     *            a <code>String</code> that will replace the existing log
     *            message
     */
    protected void setLogText(String logText) {
        this.logText = new StringBuffer();
        this.logText.append(logText);
    }

    /**
     * This method appends to the existing logging message
     * 
     * @param str
     *            is the message to append to the existing message
     */
    protected void appendLogtext(String str) {
        this.logText.append(str + "\n");
    }

    /**
     * This is the <code>RecordParser</code> that will be used to parse
     * individual records that are in the <code>DataContainer</code>
     */
    protected RecordParser recordParser = null;

    /**
     * This is the <code>DataContainer</code> that has the metadata that
     * describes the source file that will be parsed in this class
     */
    protected DataContainer source = null;

    /**
     * This is the <code>RecordDescription</code> that applies to the records
     * contained in the associated <code>DataContainer</code>
     */
    protected RecordDescription recordDescription = null;

    /**
     * This is a <code>Map</code> that provides a <code>class</code> lookup
     * from a key (<code>String</code>)
     */
    protected Map typeMap = VariableFormatMap.getInstance();

    /**
     * This is a <code>StringBuffer</code> that is used to track log message
     * through different method calls on this class
     */
    private StringBuffer logText = new StringBuffer();

    /**
     * A Log4J logger
     */
    static Logger logger = Logger.getLogger(ParserContext.class);
}