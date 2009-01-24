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
package test.moos.ssds.metadata.util;

import java.util.Calendar;
import java.util.TimeZone;

import junit.framework.TestCase;
import moos.ssds.util.XmlDateFormat;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class TestXmlDateFormat extends TestCase {

    /**
     * @param arg0
     */
    public TestXmlDateFormat(String arg0) {
        super(arg0);
    }

    protected void setUp() {
        BasicConfigurator.configure();
        logger.setLevel(Level.DEBUG);
    }

    /**
     * This test checks the equals method which depends on the outputs of the
     * processRun
     */
    public void testOne() {
        // OK, first is the target date that we are going to try to
        // represent and parse in different ways
        Calendar goldCalendar = Calendar.getInstance();
        // Set the time zone
        goldCalendar.setTimeZone(TimeZone.getTimeZone("GMT"));
        // Set the time/date
        goldCalendar.set(Calendar.YEAR, 1941);
        goldCalendar.set(Calendar.MONTH, Calendar.DECEMBER);
        goldCalendar.set(Calendar.DAY_OF_MONTH, 7);
        goldCalendar.set(Calendar.HOUR_OF_DAY, 17);
        goldCalendar.set(Calendar.MINUTE, 53);
        goldCalendar.set(Calendar.SECOND, 0);
        goldCalendar.set(Calendar.MILLISECOND, 0);

        // Create some XML date strings that it should be able to parse
        String xmlDate1 = "1941/12/07 17:53:00Z";
        String xmlDate2 = "1941-12-07 17:53:00Z";
        String xmlDate3 = "1941-12-07 07:53:00Z-10";
        String xmlDate4 = "1941/12/07 07:53:00Z-09-60";
        String xmlDate5 = "1941-12-07 07:53:00-10";
        String xmlDate6 = "1941-12-07 07:53:00-09:60";
        String xmlDate7 = "1941/12/07T17:53:00Z";
        String xmlDate8 = "1941-12-07T17:53:00Z";
        String xmlDate9 = "1941-12-07T07:53:00Z-10";
        String xmlDate10 = "1941/12/07T07:53:00Z-09-60";
        String xmlDate11 = "1941-12-07T07:53:00-10";
        String xmlDate12 = "1941-12-07T07:53:00-09:60";
        String xmlDate13 = "1941-12-07 19:53:00Z+02";
        String xmlDate14 = "1941/12/07 19:53:00Z+01+60";

        Calendar xmlCal1 = Calendar.getInstance();
        xmlCal1.setTimeInMillis(xmlDateFormat.parse(xmlDate1).getTime());
        xmlCal1.set(Calendar.MILLISECOND, 0);
        Calendar xmlCal2 = Calendar.getInstance();
        xmlCal2.setTimeInMillis(xmlDateFormat.parse(xmlDate2).getTime());
        xmlCal2.set(Calendar.MILLISECOND, 0);
        Calendar xmlCal3 = Calendar.getInstance();
        xmlCal3.setTimeInMillis(xmlDateFormat.parse(xmlDate3).getTime());
        xmlCal3.set(Calendar.MILLISECOND, 0);
        Calendar xmlCal4 = Calendar.getInstance();
        xmlCal4.setTimeInMillis(xmlDateFormat.parse(xmlDate4).getTime());
        xmlCal4.set(Calendar.MILLISECOND, 0);
        Calendar xmlCal5 = Calendar.getInstance();
        xmlCal5.setTimeInMillis(xmlDateFormat.parse(xmlDate5).getTime());
        xmlCal5.set(Calendar.MILLISECOND, 0);
        Calendar xmlCal6 = Calendar.getInstance();
        xmlCal6.setTimeInMillis(xmlDateFormat.parse(xmlDate6).getTime());
        xmlCal6.set(Calendar.MILLISECOND, 0);
        Calendar xmlCal7 = Calendar.getInstance();
        xmlCal7.setTimeInMillis(xmlDateFormat.parse(xmlDate7).getTime());
        xmlCal7.set(Calendar.MILLISECOND, 0);
        Calendar xmlCal8 = Calendar.getInstance();
        xmlCal8.setTimeInMillis(xmlDateFormat.parse(xmlDate8).getTime());
        xmlCal8.set(Calendar.MILLISECOND, 0);
        Calendar xmlCal9 = Calendar.getInstance();
        xmlCal9.setTimeInMillis(xmlDateFormat.parse(xmlDate9).getTime());
        xmlCal9.set(Calendar.MILLISECOND, 0);
        Calendar xmlCal10 = Calendar.getInstance();
        xmlCal10.setTimeInMillis(xmlDateFormat.parse(xmlDate10).getTime());
        xmlCal10.set(Calendar.MILLISECOND, 0);
        Calendar xmlCal11 = Calendar.getInstance();
        xmlCal11.setTimeInMillis(xmlDateFormat.parse(xmlDate11).getTime());
        xmlCal11.set(Calendar.MILLISECOND, 0);
        Calendar xmlCal12 = Calendar.getInstance();
        xmlCal12.setTimeInMillis(xmlDateFormat.parse(xmlDate12).getTime());
        xmlCal12.set(Calendar.MILLISECOND, 0);
        Calendar xmlCal13 = Calendar.getInstance();
        xmlCal13.setTimeInMillis(xmlDateFormat.parse(xmlDate13).getTime());
        xmlCal13.set(Calendar.MILLISECOND, 0);
        Calendar xmlCal14 = Calendar.getInstance();
        xmlCal14.setTimeInMillis(xmlDateFormat.parse(xmlDate14).getTime());
        xmlCal14.set(Calendar.MILLISECOND, 0);
        logger.debug("xmlCal1 => " + xmlCal1.getTime());
        logger.debug("xmlCal2 => " + xmlCal2.getTime());
        logger.debug("xmlCal3 => " + xmlCal3.getTime());
        logger.debug("xmlCal4 => " + xmlCal4.getTime());
        logger.debug("xmlCal5 => " + xmlCal5.getTime());
        logger.debug("xmlCal6 => " + xmlCal6.getTime());
        logger.debug("xmlCal7 => " + xmlCal7.getTime());
        logger.debug("xmlCal8 => " + xmlCal8.getTime());
        logger.debug("xmlCal9 => " + xmlCal9.getTime());
        logger.debug("xmlCal10 => " + xmlCal10.getTime());
        logger.debug("xmlCal11 => " + xmlCal11.getTime());
        logger.debug("xmlCal12 => " + xmlCal12.getTime());
        logger.debug("xmlCal13 => " + xmlCal13.getTime());
        logger.debug("xmlCal14 => " + xmlCal14.getTime());

        assertEquals("Date 1 should be equal:", goldCalendar.getTimeInMillis(),
            xmlCal1.getTimeInMillis());
        assertEquals("Date 2 should be equal:", goldCalendar.getTimeInMillis(),
            xmlCal2.getTimeInMillis());
        assertEquals("Date 3 should be equal:", goldCalendar.getTimeInMillis(),
            xmlCal3.getTimeInMillis());
        assertEquals("Date 4 should be equal:", goldCalendar.getTimeInMillis(),
            xmlCal4.getTimeInMillis());
        assertEquals("Date 5 should be equal:", goldCalendar.getTimeInMillis(),
            xmlCal5.getTimeInMillis());
        assertEquals("Date 6 should be equal:", goldCalendar.getTimeInMillis(),
            xmlCal6.getTimeInMillis());
        assertEquals("Date 7 should be equal:", goldCalendar.getTimeInMillis(),
            xmlCal7.getTimeInMillis());
        assertEquals("Date 8 should be equal:", goldCalendar.getTimeInMillis(),
            xmlCal8.getTimeInMillis());
        assertEquals("Date 9 should be equal:", goldCalendar.getTimeInMillis(),
            xmlCal9.getTimeInMillis());
        assertEquals("Date 10 should be equal:",
            goldCalendar.getTimeInMillis(), xmlCal10.getTimeInMillis());
        assertEquals("Date 11 should be equal:",
            goldCalendar.getTimeInMillis(), xmlCal11.getTimeInMillis());
        assertEquals("Date 12 should be equal:",
            goldCalendar.getTimeInMillis(), xmlCal12.getTimeInMillis());
        assertEquals("Date 13 should be equal:",
            goldCalendar.getTimeInMillis(), xmlCal13.getTimeInMillis());
        assertEquals("Date 14 should be equal:",
            goldCalendar.getTimeInMillis(), xmlCal14.getTimeInMillis());
    }

    static Logger logger = Logger.getLogger(TestXmlDateFormat.class);
    private XmlDateFormat xmlDateFormat = new XmlDateFormat();
}