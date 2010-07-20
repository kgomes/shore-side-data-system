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

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import moos.ssds.metadata.RecordDescription;
import moos.ssds.metadata.RecordVariable;
import moos.ssds.metadata.util.MetadataException;

/**
 * This class will take a record that is an NMEA string and convert it to its
 * appropriate <code>RecordVariables</code>
 * 
 * @stereotype thing
 */
public class Nmea21RecordParser extends RecordParser {

    /**
     * This is the default constructor for the parser. It takes no record
     * description as it uses a standard set of <code>RecordVariable</code>s
     * that it converts to.
     */
    public Nmea21RecordParser(String parseAbbreviation) {
        // Super class constructor
        super();

        // Set the abbreviation
        this.parseAbbreviation = parseAbbreviation;

        // RecordDescription creation
        RecordDescription recordDescription = new RecordDescription();
        try {
            recordDescription.setRecordType(new Long(1));
            recordDescription
                .setBufferStyle(RecordDescription.BUFFER_STYLE_ASCII);
            recordDescription
                .setBufferParseType(RecordDescription.PARSE_TYPE_UNIQUE_TOKEN);
            recordDescription.setBufferItemSeparator(",");
            recordDescription
                .setBufferLengthType(RecordDescription.BUFFER_LENGTH_TYPE_FIXED);
            recordDescription.setParseable(new Boolean(true));
        } catch (MetadataException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Now setup the RecordVariables that are CF compliant
        timeRV = new RecordVariable();
        try {
            timeRV.setName("time");
            timeRV.setDescription("Time in epoch seconds");
            timeRV.setLongName("Time of GPS Fix");
            timeRV.setFormat("long");
            timeRV.setUnits("seconds since 1970-01-01 00:00:00");
            timeRV.setColumnIndex(1);
            recordDescription.addRecordVariable(timeRV);
        } catch (MetadataException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        latitudeRV = new RecordVariable();
        try {
            latitudeRV.setName("grid_latitude");
            latitudeRV.setDescription("Latitude of GPS fix");
            latitudeRV.setLongName("Latitude");
            latitudeRV.setFormat("double");
            latitudeRV.setUnits("Decimal Degrees");
            latitudeRV.setColumnIndex(2);
            recordDescription.addRecordVariable(latitudeRV);
        } catch (MetadataException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        longitudeRV = new RecordVariable();
        try {
            longitudeRV.setName("grid_longitude");
            longitudeRV.setDescription("Longitude of GPS fix");
            longitudeRV.setLongName("Longitude");
            longitudeRV.setFormat("double");
            longitudeRV.setUnits("Decimal Degrees");
            longitudeRV.setColumnIndex(3);
            recordDescription.addRecordVariable(longitudeRV);
        } catch (MetadataException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        super.setRecordDescription(recordDescription);
    }

    /**
     * @param record
     *            A record whose contents will be parsed
     * @return A Map (from java collections). The map keys are the
     *         <code>RecordVariables</code> that describe each variable in the
     *         record. The values are the corresponding data object (Float,
     *         Double, etc).
     */
    public Map parse(byte[] record) throws ParsingException {

        // Convert the byte array to a string
        String recordToParse = new String(record);

        // The map to return
        Map parsedData = null;
        if (this.parseAbbreviation.equalsIgnoreCase(RMC_ABBR))
            parsedData = this.parseRMC(recordToParse);
        return parsedData;
    }

    private Map parseRMC(String rmcString) {
        // The map to return
        Map mapToReturn = new HashMap();

        // Create a calendar to build the time
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("GMT"));

        // Split up the record
        String[] tokens = rmcString.split(",");

        // Variables to parse the string into
        // This is the GP type specified first in the line
        if (tokens.length == 12) {
            // This is the time of day the fix was taken
            String timeOfFix = tokens[1];

            // Date of GPS fix
            String dateOfFix = tokens[9];

            // Parse out hour, minute, second
            calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeOfFix
                .substring(0, 2)));
            calendar.set(Calendar.MINUTE, Integer.parseInt(timeOfFix.substring(
                2, 4)));
            calendar.set(Calendar.SECOND, Integer.parseInt(timeOfFix.substring(
                4, 6)));
            calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateOfFix
                .substring(0, 2)));
            calendar.set(Calendar.MONTH, Integer.parseInt(dateOfFix.substring(
                2, 4)) - 1);
            int yearShort = Integer.parseInt(dateOfFix.substring(4, 6));
            if (yearShort <= 79) {
                yearShort += 2000;
            } else {
                yearShort += 1900;
            }

            calendar.set(Calendar.YEAR, yearShort);

            // This is the decimal representation of latitude
            double latitude = Double.parseDouble(tokens[3]);
            // Truncate to dec value
            double latitudeDD = (int) (latitude / 100.0);
            latitudeDD += (latitude - latitudeDD * 100) / 60.0;
            if (tokens[4].equalsIgnoreCase("S")) {
                latitudeDD *= -1; // southern hemisphere is negative DD
            }

            // This is the decimal representation of the longitude
            double longitude = Double.parseDouble(tokens[5]);
            // Truncate to dec value
            double longitudeDD = (int) (longitude / 100.0);
            longitudeDD += (longitude - longitudeDD * 100) / 60.0;
            if (tokens[6].equalsIgnoreCase("W")) {
                longitudeDD *= -1; // wesern hemisphere is negative DD
            }
            mapToReturn.put(timeRV, Long.valueOf(calendar.getTime().getTime()));
            mapToReturn.put(latitudeRV, Double.valueOf(latitudeDD));
            mapToReturn.put(longitudeRV, Double.valueOf(longitudeDD));
        }
        return mapToReturn;
    }

    // Methods to get access to the generated record variables
    public RecordVariable getTimeRecordVariable() {
        return this.timeRV;
    }

    public RecordVariable getLatitudeRecordVariable() {
        return this.latitudeRV;
    }

    public RecordVariable getLongitudeRecordVariable() {
        return this.longitudeRV;
    }

    /**
     * The RecordVariables to return with parsed data
     */
    RecordVariable timeRV = null;
    RecordVariable latitudeRV = null;
    RecordVariable longitudeRV = null;

    /**
     * This is the parse abbreviation used to figure out the record format to
     * parse
     */
    private String parseAbbreviation = null;

    /**
     * Some NMEA types that can be parsed using this class
     */
    public static final String RMC_ABBR = "RMC";
}
