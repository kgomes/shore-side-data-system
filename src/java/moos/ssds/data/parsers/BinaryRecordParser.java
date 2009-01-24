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

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import moos.ssds.metadata.RecordDescription;
import moos.ssds.metadata.RecordVariable;

import com.mindprod.ledatastream.LEDataInputStream;

/**
 * <p>
 * This class is used to parse records from a <code>IDataContainer</code> that
 * are of binary format
 * </p>
 * @stereotype thing
 */
public class BinaryRecordParser extends RecordParser {

    /**
     * This is the default constructor
     */
    public BinaryRecordParser() {
        super();
    }

    /**
     * This is a constructor that takes in a <code>IRecordDescription</code>
     * to help setup the parsing correctly (it contains the
     * <code>IRecordVariables</code>)
     * 
     * @param recordDescription
     *            the <code>IRecordDescription</code> that contains the
     *            metadata that will help this parser correctly parse binary
     *            data records
     */
    public BinaryRecordParser(RecordDescription recordDescription) {
        super(recordDescription);
    }

    /**
     * This is the method that actually reads in the data and parses it into the
     * correct <code>IRecordVariable</code>s that are then containe in the
     * returned map.
     * 
     * @return a <code>Map</code> that contains <code>IRecordVariable</code>s
     *         as keys and their corresponding data as values (for a single
     *         record).
     */
    public Map parse(byte[] buffer) throws ParsingException {

        // Create the data map to return
        Map parsedData = new HashMap();
        // Create a DataInput to read from
        DataInput dis = null;
        try {
            // Check the endianness to create the correct reader
            String endian = recordDescription.getEndian();
            if ((endian != null)
                && (endian.equalsIgnoreCase(RecordDescription.ENDIAN_LITTLE))) {
                dis = new LEDataInputStream(new ByteArrayInputStream(buffer));
            } else {
                dis = new DataInputStream(new ByteArrayInputStream(buffer));
            }

            // Loop over the number of variables to pick off the data from the
            // buffer
            for (int i = 1; i <= recordDescription.getRecordVariables().size(); i++) {
                // Grab the record variables and iterate over them
                Collection variables = recordDescription.getRecordVariables();
                Iterator iterator = variables.iterator();
                RecordVariable rvToUse = null;
                while (iterator.hasNext()) {
                    // Grab a record variable
                    RecordVariable variable = (RecordVariable) iterator.next();
                    // Check to see if it is the one for the current column
                    if ((int) variable.getColumnIndex() == i) {
                        rvToUse = variable;
                        break;
                    }
                }
                if (rvToUse != null) {
                    // Grab the class type of that record variable
                    Class format = (Class) typeMap.get(rvToUse
                        .getFormat());
                    // Create a Number to hold the data read
                    Number value = null;
                    // If the variable or its format is not defined, skip it
                    // TODO KJG - This seems hackish and not very robust
                    if ((rvToUse == null) || (format == null)) {
                        // Skip variables that do not have a format
                        continue;
                    }
                    // Check the format and read the number of bytes
                    // that correspond to that class
                    if (format == byte.class) {
                        value = new Byte(dis.readByte());
                    } else if (format == short.class) {
                        value = new Short(dis.readShort());
                    } else if (format == int.class) {
                        value = new Integer(dis.readInt());
                    } else if (format == long.class) {
                        value = new Long(dis.readLong());
                    } else if (format == float.class) {
                        value = new Float(dis.readFloat());
                    }
                    // if no match is found use a double
                    // TODO KJG - Again hackish and very error prone
                    else {
                        value = new Double(dis.readDouble());
                    }

                    // Put in the map
                    parsedData.put(rvToUse, value);
                }
            }
        } catch (Exception e) {
            throw new ParsingException("Unable to parse packet "
                + e.getMessage());
        }
        return parsedData;
    }

}
