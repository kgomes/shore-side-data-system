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

import java.util.Iterator;

import moos.ssds.metadata.DataContainer;

/**
 * <p>
 * This interface defines the methods that different parsers must implement. It
 * extends the <code>java.util.Iterator</code> interface, but it does <b>NOT</b>
 * support the remove method.
 * </p>
 * <hr>
 * 
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.1 $
 * @stereotype interface
 */
public interface IParser extends Iterator {

    /**
     * This method sets the source that the parser is to use to read data
     * records from. It takes in a <code>IDataContainer</code> and uses that
     * information to find the file to parse and to correctly parse the records
     * in that file.
     * 
     * @param dataContainer
     *            is the <code>IDataContainer</code> that contains all the
     *            necessary metadata to parse a file.
     */
    void setSource(DataContainer dataContainer);

    /**
     * This method returns the <code>ParserContext</code> that is being used
     * in the parsing of the source file.
     * 
     * @return a <code>ParserContext</code> that is being used in the parsing
     *         of the file
     */
    public ParserContext getParserContext();

    /**
     * This method returns the <code>DataContainer</code> that contains the
     * information that is used to correctly parse a file.
     * 
     * @return the <code>DataContainer</code> that contains all the pertinent
     *         information that the parser needs to correctly parse a file.
     */
    public DataContainer getSource();

}
