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
package test.moos.ssds.data.parsers;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.TimeZone;

import junit.framework.TestCase;
import moos.ssds.data.parsers.Nmea21RecordParser;
import moos.ssds.data.parsers.ParsingException;
import moos.ssds.metadata.RecordDescription;
import moos.ssds.metadata.RecordVariable;

import org.apache.log4j.Logger;

/**
 * JUnit TestCase.
 * 
 * @testfamily JUnit
 * @testkind testcase
 * @testsetup Default TestCase
 */
public class TestNmea21RecordParser extends TestCase {

    /**
     * Constructs a test case with the given name.
     */
    public TestNmea21RecordParser(String name) {
        super(name);
    }

    /**
     * 
     */
    protected void setUp() {}

    /**
     * Tears down the fixture
     */
    protected void tearDown() {}

    /**
     * TODO - Document
     */
    public void testParse() {
        Nmea21RecordParser rp = new Nmea21RecordParser("RMC");
        // For record one
        Map dataMap = null;
        try {
            dataMap = rp.parse(nmea1.getBytes());
        } catch (ParsingException e) {
            logger.error("ParsingException : " + e.getMessage());
        }
        // Now check all the results
        RecordDescription rd = rp.getRecordDescription();
        // Grab the RVs
        Collection recordVariables = rd.getRecordVariables();
        // Iterate over them
        Iterator it = recordVariables.iterator();
        while (it.hasNext()) {
            RecordVariable rvToCheck = (RecordVariable) it.next();
            if (rvToCheck.getName().equalsIgnoreCase("time")) {
                Long time = (Long) dataMap.get(rvToCheck);
                Calendar calendar = Calendar.getInstance();
                Date date = new Date();
                date.setTime(time.longValue());
                calendar.setTime(date);
                calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
                assertEquals("Hours should be equal", 19, calendar
                    .get(Calendar.HOUR_OF_DAY));
                assertEquals("Minutes should be equal", 44, calendar
                    .get(Calendar.MINUTE));
                assertEquals("Seconds should be equal", 41, calendar
                    .get(Calendar.SECOND));
                assertEquals("Year should be equal", 2006, calendar
                    .get(Calendar.YEAR));
                assertEquals("Month should be equal", 5, calendar
                    .get(Calendar.MONTH) + 1);
                assertEquals("Day of Month should be equal", 17, calendar
                    .get(Calendar.DAY_OF_MONTH));
            } else if (rvToCheck.getName().equalsIgnoreCase("grid_latitude")) {
                Double latitude = (Double) dataMap.get(rvToCheck);
                assertEquals("Latitude should be correct", latitude
                    .doubleValue(), 36.83470333333333, 0.0001);
            } else if (rvToCheck.getName().equalsIgnoreCase("grid_longitude")) {
                Double longitude = (Double) dataMap.get(rvToCheck);
                assertEquals("Latitude should be correct", longitude
                    .doubleValue(), -121.89858833333334, 0.0001);
                logger.debug("");
            }
        }
    }

    /**
     * The strings to parse
     */
    private String nmea1 = "$GPRMC,194441,A,3650.0822,N,12153.9153,W,001.5,284.9,170506,014.8,E*60";

    /**
     * A log4j logger
     */
    static Logger logger = Logger.getLogger(TestNmea21RecordParser.class);
}
