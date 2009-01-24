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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import moos.ssds.metadata.RecordDescription;
import moos.ssds.metadata.RecordVariable;

import org.apache.log4j.Logger;

/**
 * <p>
 * This is a class that can be used to parse a byte array into a map of
 * <code>RecordVariable</code> s if the backing data source is of type ASCII.
 * </p>
 * <hr>
 * 
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.2 $
 * @stereotype thing
 */
public class AsciiRecordParser extends RecordParser {

    /**
     * This is the default constructor
     */
    public AsciiRecordParser() {
        super();
    }

    /**
     * This is the constructor that takes in a <code>RecordDescription</code>
     * and sets up the parsing correctly
     * 
     * @param recordDescription
     */
    public AsciiRecordParser(RecordDescription recordDescription) {
        // Call the super constructor
        super(recordDescription);

        // Set the recordDescription locally to make sure the right
        // method gets called
        this.setRecordDescription(recordDescription);

        // Grab the record variables
        recordVariables = recordDescription.getRecordVariables();
    }

    /**
     * This method sets the record description that describes the records that
     * will be parsed
     */
    public void setRecordDescription(RecordDescription recordDescription) {
        // Call it on the parent first
        super.setRecordDescription(recordDescription);
        if (recordDescription != null) {
            // First check for parse regular expression
            if ((recordDescription.getRecordParseRegExp() != null)
                && (!recordDescription.getRecordParseRegExp().equals(""))) {
                // Clear the buffer separator
                bufferSep = null;
                // Now grab the regexp
                recordParseRegularExpression = recordDescription
                    .getRecordParseRegExp();
                // Create the pattern
                recordParsePattern = Pattern
                    .compile(recordParseRegularExpression);
            } else {
                // Clear the record reg exp
                recordParseRegularExpression = null;
                recordParsePattern = null;

                // Find the buffer item seperator
                bufferSep = recordDescription.getBufferItemSeparator();

                // Check for separator aliases
                if (bufferSep == null || bufferSep.equals(" ")
                    || bufferSep.equals("space") || bufferSep.equals("tab")
                    || bufferSep.equals("whitespace") || bufferSep.equals("")) {
                    // Whitespace
                    bufferSep = new String("\\s+");
                } else if (bufferSep.equals("comma")) {
                    // Comma separated
                    bufferSep = new String(",");
                }
            }
        }
    }

    /**
     * This method takes a data record and parses into a <code>Map</code> of
     * <code>RecordVariable</code>s as keys and data as values.
     * 
     * @param record
     *            A record whose contents will be parsed
     * @return A Map (from java collections). The map keys are the
     *         <code>RecordVariable</code>s that describe each variable in
     *         the packet. The values are the corresponding data object (Float,
     *         Double, etc).
     * @see moos.ssds.metadata.TypeMap For references on how RecordVariable
     *      types are mapped to java types.
     */
    public Map parse(byte[] record) throws ParsingException {

        // Clear the message queue
        this.parseMessages = new StringBuffer();

        // Create the map to put parsed data into
        Map parsedData = new HashMap();

        // Now depending on how the parser is setup, either break the record
        // into tokens using the buffer item separator, or use the regepx to
        // pull out the variables
        String recordString = new String(record);
        recordString = recordString.trim();
        String[] values = null;
        int numVariables = this.recordVariables.size();
        if (recordParsePattern != null) {
            values = new String[numVariables];
            // Now try the pattern match
            Matcher matcher = recordParsePattern.matcher(recordString);
            if (matcher.find()) {
                // Loop through the groups
                for (int i = 1; i <= numVariables; i++) {
                    values[(i - 1)] = matcher.group(i);
                }
            }
        } else {
            values = recordString.split(bufferSep);
        }

        // Check for weird conditions
        if (values.length > numVariables) {
            parseMessages
                .append("Something went wrong in parsing, there are supposed to be "
                    + numVariables
                    + " values, but "
                    + values.length
                    + " were found, SSDS will only pay attention to the first "
                    + numVariables + " values.\n");
            parseMessages.append("RecordDescription-->"
                + this.recordDescription.toStringRepresentation("|") + "\n");
            throw new ParsingException(
                "Something went wrong in parsing, there are supposed to be "
                    + numVariables + " values, but " + values.length
                    + " were found.  SSDS will NOT try to parse this record");
        }
        if (values.length < numVariables) {
            parseMessages
                .append("There were fewer values found in the record than was expected (there was "
                    + values.length
                    + " and there should have been "
                    + numVariables + "\n");
            parseMessages.append("RecordDescription-->"
                + this.recordDescription.toStringRepresentation("|") + "\n");
            throw new ParsingException(
                "Could not read enough values from the record string");
        }

        try {
            // Loop over the recordVariables and get the data
            Iterator i = recordVariables.iterator();
            RecordVariable v = null;
            Class format = null;
            while (i.hasNext()) {
                v = (RecordVariable) i.next();
                String value = null;
                value = values[((int) v.getColumnIndex()) - 1];
                if (value != null) {
                    // Trim off any whitespace
                    value = value.trim();
                    if ((v == null)
                        || (typeMap.get(v.getFormat().toLowerCase()) == null)) {
                        // SKip variables that do not have a format
                        parseMessages.append("Skipped variable " + v.getName()
                            + " due to no format\n");
                        continue;
                    }
                    Pattern dataPattern = null;
                    if ((v.getParseRegExp() != null)
                        && (v.getParseRegExp().compareTo("") != 0)) {
                        try {
                            dataPattern = Pattern.compile(v.getParseRegExp());
                        } catch (Exception ex) {
                            parseMessages
                                .append("Could not compile data pattern: "
                                    + v.getParseRegExp() + "\n");
                        }
                    }
                    // Try to pattern grab the actual values if a pattern was
                    // found
                    if (dataPattern != null) {
                        Matcher m = dataPattern.matcher(value);
                        boolean b = m.matches();
                        if (b) {
                            value = m.group(1);
                        }
                    }
                    // If the value starts with a plus, strip it off
                    if (value.startsWith("+")) {
                        value = value.substring(1);
                    }
                    // Try to convert values from scientific notation if
                    // applicable
                    format = (Class) typeMap.get(v.getFormat().toLowerCase());
                    // Check for what type of data
                    if (format == java.lang.String.class) {
                        try {
                            parsedData.put(v, value);
                        } catch (Exception e1) {
                            parseMessages.append("Could not parse "
                                + v.getName() + " string: " + value + "\n");
                            parsedData.put(v, null);
                        }
                    } else if (format == byte.class) {
                        try {
                            parsedData.put(v, Byte.valueOf(value));
                        } catch (Exception e1) {
                            parseMessages.append("Could not parse "
                                + v.getName() + " byte: " + value + "\n");
                            parsedData.put(v, null);
                        }
                    } else if (format == short.class) {
                        try {
                            parsedData.put(v, Short.valueOf(value));
                        } catch (Exception e1) {
                            parseMessages.append("Could not parse "
                                + v.getName() + " short: " + value + "\n");
                            parsedData.put(v, null);
                        }
                    } else if (format == int.class) {
                        boolean parseSucceeded = true;
                        try {
                            parsedData.put(v, Integer.valueOf(value));
                        } catch (Exception e1) {
                            parseSucceeded = false;
                            parseMessages.append("Could not parse "
                                + v.getName() + " int: " + value + "\n");
                            parsedData.put(v, null);
                        }
                        // If this happens see if we can lop off any
                        // decimals. This will lose data, but it will parse.
                        if ((!parseSucceeded) && (value.indexOf(".") >= 0)) {
                            String tempValue = value.substring(0, value
                                .indexOf("."));
                            try {
                                parsedData.put(v, Integer.valueOf(tempValue));
                            } catch (Exception e) {
                                parseSucceeded = false;
                                parseMessages.append("Could not parse "
                                    + v.getName() + " int: " + value + "\n");
                                parsedData.put(v, null);
                            }
                        }
                    } else if (format == long.class) {
                        try {
                            parsedData.put(v, Long.valueOf(value));
                        } catch (Exception e1) {
                            parseMessages.append("Could not parse "
                                + v.getName() + " long: " + value + "\n");
                            parsedData.put(v, null);
                        }
                    } else if (format == double.class) {
                        try {
                            parsedData.put(v, Double.valueOf(value));
                        } catch (Exception e1) {
                            parseMessages.append("Could not parse "
                                + v.getName() + " double: " + value + "\n");
                            parsedData.put(v, null);
                        }
                    } else if (format == float.class) {
                        try {
                            parsedData.put(v, Float.valueOf(value));
                        } catch (Exception e1) {
                            parseMessages.append("Could not parse "
                                + v.getName() + " float: " + value + "\n");
                            parsedData.put(v, null);
                        }
                    } else {
                        try {
                            parsedData.put(v, Double.valueOf(value));
                        } catch (Exception e1) {
                            parseMessages.append("Could not parse "
                                + v.getName() + " default double: " + value);
                            parsedData.put(v, null);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new ParsingException("Unable to parse data from "
                + new String(record));
        }
        return parsedData;
    }

    /**
     * The delimiter to split variables up by
     */
    private String bufferSep = null;

    /**
     * This is the regular expression (and supporting objects) that can be used
     * to parse the whole record into variables
     */
    private String recordParseRegularExpression = null;
    private Pattern recordParsePattern = null;

    /**
     * A log4J logger
     */
    static Logger logger = Logger.getLogger(AsciiRecordParser.class);
}