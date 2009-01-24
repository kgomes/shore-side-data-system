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
package test.moos.ssds.metadata;

import java.util.Date;

import junit.framework.TestCase;
import moos.ssds.metadata.DataContainer;
import moos.ssds.metadata.DateRange;
import moos.ssds.metadata.util.MetadataException;
import moos.ssds.metadata.util.MetadataFactory;
import moos.ssds.util.XmlDateFormat;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 * This is the test class to test the DataContainer class
 * 
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.13 $
 */
public class TestDataContainer extends TestCase {

    /**
     * @param arg0
     */
    public TestDataContainer(String arg0) {
        super(arg0);
    }

    protected void setUp() {
        BasicConfigurator.configure();
        logger.setLevel(Level.DEBUG);
        logger.addAppender(new ConsoleAppender(new PatternLayout(
            "%d %-5p [%c %M %L] %m%n")));
    }

    /**
     * This method checks the creation of a <code>DataContainer</code> object
     */
    public void testCreateDataContainerAndSetMethods() {
        // Create the new dataContainer
        DataContainer dataContainer = new DataContainer();

        // Set all the values
        try {
            dataContainer.setId(new Long(1));
            dataContainer.setName("DataContainerOne");
            dataContainer.setDescription("DataContainerOne Description");
            try {
                dataContainer.setDataContainerType("File");
            } catch (MetadataException e) {}
            Date startDate = xmlDateFormat.parse("2003-05-05T16:11:44Z");
            dataContainer.setStartDate(startDate);
            Date endDate = xmlDateFormat.parse("2004-02-01T08:38:19Z");
            dataContainer.setEndDate(endDate);
            dataContainer.setOriginal(new Boolean(true));
            dataContainer
                .setUriString("http://kasatka.shore.mbari.org/DataContainerOne.txt");
            dataContainer.setContentLength(new Long(50000));
            dataContainer.setMimeType("CSV");
            dataContainer.setNumberOfRecords(new Long(800));
            dataContainer.setDodsAccessible(new Boolean(false));
            dataContainer.setDodsUrlString("http://dods.server.com");
            dataContainer.setNoNetCDF(new Boolean(false));
            dataContainer.setMinLatitude(new Double(36.6000));
            dataContainer.setMaxLatitude(new Double(36.8050));
            dataContainer.setMinLongitude(new Double(-121.56));
            dataContainer.setMaxLongitude(new Double(-121.034));
            dataContainer.setMaxDepth(new Float(10.546));
            dataContainer.setMinDepth(new Float(0.00));
        } catch (MetadataException e) {
            assertTrue("MetadataException caught trying to set values: "
                + e.getMessage(), false);
        }

        // Now read all of them back
        assertEquals(dataContainer.getId(), new Long(1));
        assertEquals(dataContainer.getName(), "DataContainerOne");
        assertEquals(dataContainer.getDescription(),
            "DataContainerOne Description");
        assertEquals(dataContainer.getDataContainerType(), "File");
        assertTrue(this.datesEqualToSecond(dataContainer.getStartDate(),
            xmlDateFormat.parse("2003-05-05T16:11:44Z")));
        assertTrue(this.datesEqualToSecond(dataContainer.getEndDate(),
            xmlDateFormat.parse("2004-02-01T08:38:19Z")));
        assertEquals(dataContainer.isOriginal(), new Boolean(true));
        assertEquals(dataContainer.getUriString(),
            "http://kasatka.shore.mbari.org/DataContainerOne.txt");
        // Try the URI format
        assertEquals("URI format should be equal", dataContainer.getUri()
            .toASCIIString(),
            "http://kasatka.shore.mbari.org/DataContainerOne.txt");
        // Try the URL format
        assertEquals("URL format should be equal", dataContainer.getUrl()
            .toExternalForm(),
            "http://kasatka.shore.mbari.org/DataContainerOne.txt");
        logger.debug("URLString = " + dataContainer.getUriString());
        logger.debug("URI.toASCIIString = "
            + dataContainer.getUri().toASCIIString());
        logger.debug("URL.toExternalForm = "
            + dataContainer.getUrl().toExternalForm());
        assertEquals(dataContainer.getContentLength(), new Long(50000));
        assertEquals(dataContainer.getMimeType(), "CSV");
        assertEquals(dataContainer.getNumberOfRecords(), new Long(800));
        assertEquals(dataContainer.isDodsAccessible(), new Boolean(false));
        assertEquals(dataContainer.getDodsUrlString(), "http://dods.server.com");
        assertTrue(!dataContainer.isNoNetCDF().booleanValue());
        assertEquals(dataContainer.getMinLatitude().doubleValue(), new Double(
            36.6000).doubleValue(), 0.00);
        assertEquals(dataContainer.getMaxLatitude().doubleValue(), new Double(
            36.8050).doubleValue(), 0.000);
        assertEquals(dataContainer.getMinLongitude().doubleValue(), new Double(
            -121.56).doubleValue(), 0.00);
        assertEquals(dataContainer.getMaxLongitude().doubleValue(), new Double(
            -121.034).doubleValue(), 0.00);
        assertEquals(dataContainer.getMinDepth().doubleValue(), new Double(0.0)
            .doubleValue(), 0.00);
        assertEquals(dataContainer.getMaxDepth().doubleValue(), new Double(
            10.546).doubleValue(), 0.001);
    }

    public void testCreateDataContainerAndSetValuesByStringRep() {
        DataContainer dataContainer = new DataContainer();
        try {
            dataContainer.setValuesFromStringRepresentation(
                dataContainerOneStringRep, DELIMITER);
        } catch (MetadataException e) {
            assertTrue(
                "MetadataException caught trying to set values from string rep: "
                    + e.getMessage(), false);
        }
        // Now read all of them back
        assertEquals(dataContainer.getId(), new Long(1));
        assertEquals(dataContainer.getName(), "DataContainerOne");
        assertEquals(dataContainer.getDescription(),
            "DataContainerOne Description");
        assertEquals(dataContainer.getDataContainerType(), "File");
        assertTrue(this.datesEqualToSecond(dataContainer.getStartDate(),
            xmlDateFormat.parse("2003-05-05T16:11:44Z")));
        assertTrue(this.datesEqualToSecond(dataContainer.getEndDate(),
            xmlDateFormat.parse("2004-02-01T08:38:19Z")));
        assertEquals(dataContainer.isOriginal(), new Boolean(true));
        assertEquals(dataContainer.getUriString(),
            "http://kasatka.shore.mbari.org/DataContainerOne.txt");
        // Try the URI format
        assertEquals("URI format should be equal", dataContainer.getUri()
            .toASCIIString(),
            "http://kasatka.shore.mbari.org/DataContainerOne.txt");
        // Try the URL format
        assertEquals("URL format should be equal", dataContainer.getUrl()
            .toExternalForm(),
            "http://kasatka.shore.mbari.org/DataContainerOne.txt");
        logger.debug("URLString = " + dataContainer.getUriString());
        logger.debug("URI.toASCIIString = "
            + dataContainer.getUri().toASCIIString());
        logger.debug("URL.toExternalForm = "
            + dataContainer.getUrl().toExternalForm());
        assertEquals(dataContainer.getContentLength(), new Long(50000));
        assertEquals(dataContainer.getMimeType(), "CSV");
        assertEquals(dataContainer.getNumberOfRecords(), new Long(800));
        assertEquals(dataContainer.isDodsAccessible(), new Boolean(false));
        assertEquals(dataContainer.getDodsUrlString(), "http://dods.server.com");
        assertTrue(dataContainer.isNoNetCDF().booleanValue());
        assertEquals(dataContainer.getMinLatitude().doubleValue(), new Double(
            36.6000).doubleValue(), 0.00);
        assertEquals(dataContainer.getMaxLatitude().doubleValue(), new Double(
            36.8050).doubleValue(), 0.000);
        assertEquals(dataContainer.getMinLongitude().doubleValue(), new Double(
            -121.56).doubleValue(), 0.00);
        assertEquals(dataContainer.getMaxLongitude().doubleValue(), new Double(
            -121.034).doubleValue(), 0.00);
        assertEquals(dataContainer.getMinDepth().doubleValue(), new Double(0.0)
            .doubleValue(), 0.00);
        assertEquals(dataContainer.getMaxDepth().doubleValue(),
            new Double(10.0).doubleValue(), 0.001);
    }

    /**
     * This method checks to see if the toStringRepresentation method works
     * properly
     */
    public void testGetAndSetValuesFromStringRepresentation() {

        this.rebuildDataContainerObjectsFromStringReps();

        // Create the new dataContainer
        DataContainer dataContainer = new DataContainer();

        try {
            dataContainer.setId(new Long(1));
            dataContainer.setName("DataContainerOne");
            dataContainer.setDescription("DataContainerOne Description");
            try {
                dataContainer.setDataContainerType("File");
            } catch (MetadataException e) {
                assertTrue(
                    "MetadataException caught trying to set the dataContainerType"
                        + e.getMessage(), false);
            }
            Date startDate = xmlDateFormat.parse("2003-05-05T16:11:44Z");
            dataContainer.setStartDate(startDate);
            Date endDate = xmlDateFormat.parse("2004-02-01T08:38:19Z");
            dataContainer.setEndDate(endDate);
            dataContainer.setOriginal(new Boolean(true));
            dataContainer
                .setUriString("http://kasatka.shore.mbari.org/DataContainerOne.txt");
            dataContainer.setContentLength(new Long(50000));
            dataContainer.setMimeType("CSV");
            dataContainer.setNumberOfRecords(new Long(800));
            dataContainer.setDodsAccessible(new Boolean(false));
            dataContainer.setDodsUrlString("http://dods.server.com");
            dataContainer.setNoNetCDF(new Boolean(true));
            dataContainer.setMinLatitude(new Double(36.6000));
            dataContainer.setMaxLatitude(new Double(36.8050));
            dataContainer.setMinLongitude(new Double(-121.56));
            dataContainer.setMaxLongitude(new Double(-121.034));
            dataContainer.setMaxDepth(new Float(10.0));
            dataContainer.setMinDepth(new Float(0.00));
        } catch (MetadataException e1) {
            assertTrue("MetadataException caught trying to set values: "
                + e1.getMessage(), false);
        }

        // Now check for equals
        assertEquals(
            "The two data container string representations should be the same",
            dataContainer.toStringRepresentation(this.DELIMITER),
            this.dataContainerOneStringRep);

        // Check for exceptions
        // Try a bad date (this should return a null)
        String wrongStringRep = "DataContainer|" + "id=2|"
            + "name=DataContainerTwo|"
            + "description=DataContainerTwo Description|"
            + "dataContainerType=Stream|" + "startDate=200501-01T00:00:00Z|"
            + "endDate=2005-02-01T08:38:19Z|" + "original=true|"
            + "uri=http://kasatka.shore.mbari.org/DataContainerTwo.xls|"
            + "contentLength=1295|" + "mimeType=app/excel|"
            + "numberOfRecords=10|" + "dodsAccessible=false|"
            + "minLatitude=6.0|" + "maxLatitude=45.0|"
            + "minLongitude=-255.34|" + "maxLongitude=-245.567|"
            + "minDepth=0.0|" + "maxDepth=10000";
        try {
            dataContainerTwo.setValuesFromStringRepresentation(wrongStringRep,
                this.DELIMITER);
        } catch (MetadataException e) {}
        assertNull(
            "The date parsed should be null if the string was not parseable",
            dataContainerTwo.getStartDate());

        // Now try a bad URL
        boolean exceptionThrown = false;
        exceptionThrown = false;
        wrongStringRep = "DataContainer|" + "id=2|" + "name=DataContainerTwo|"
            + "description=DataContainerTwo Description|"
            + "dataContainerType=Stream|" + "startDate=2005-01-01T00:00:00Z|"
            + "endDate=2005-02-01T08:38:19Z|" + "original=true|"
            + "url=http//kasatka.shore.mbari.org/DataContainerTwo.xls|"
            + "contentLength=1295|" + "mimeType=app/excel|"
            + "numberOfRecords=10|" + "dodsAccessible=false|"
            + "minLatitude=6.0|" + "maxLatitude=45.0|"
            + "minLongitude=-255.34|" + "maxLongitude=-245.567|"
            + "minDepth=0.0|" + "maxDepth=10000";
        try {
            dataContainerTwo.setValuesFromStringRepresentation(wrongStringRep,
                this.DELIMITER);
        } catch (MetadataException e) {
            exceptionThrown = true;
        }
        assertTrue(
            "An exception should have been thrown setting values from bad string rep (bad url)",
            exceptionThrown);

        // Now a bad contentLength
        exceptionThrown = false;
        wrongStringRep = "DataContainer|" + "id=2|" + "name=DataContainerTwo|"
            + "description=DataContainerTwo Description|"
            + "dataContainerType=Stream|" + "startDate=2005-01-01T00:00:00Z|"
            + "endDate=2005-02-01T08:38:19Z|" + "original=true|"
            + "uri=http://kasatka.shore.mbari.org/DataContainerTwo.xls|"
            + "contentLength=1295MB|" + "mimeType=app/excel|"
            + "numberOfRecords=10|" + "dodsAccessible=false|"
            + "minLatitude=6.0|" + "maxLatitude=45.0|"
            + "minLongitude=-255.34|" + "maxLongitude=-245.567|"
            + "minDepth=0.0|" + "maxDepth=1000";
        try {
            dataContainerTwo.setValuesFromStringRepresentation(wrongStringRep,
                this.DELIMITER);
        } catch (MetadataException e) {
            exceptionThrown = true;
        }
        assertTrue(
            "An exception should have been thrown setting values from bad string rep (bad contentLength)",
            exceptionThrown);

        // Now a bad numberOfRecords
        exceptionThrown = false;
        wrongStringRep = "DataContainer|" + "id=2|" + "name=DataContainerTwo|"
            + "description=DataContainerTwo Description|"
            + "dataContainerType=Stream|" + "startDate=2005-01-01T00:00:00Z|"
            + "endDate=2005-02-01T08:38:19Z|" + "original=true|"
            + "uri=http://kasatka.shore.mbari.org/DataContainerTwo.xls|"
            + "contentLength=1295|" + "mimeType=app/excel|"
            + "numberOfRecords=10Gazillion|" + "dodsAccessible=false|"
            + "minLatitude=6.0|" + "maxLatitude=45.0|"
            + "minLongitude=-255.34|" + "maxLongitude=-245.567|"
            + "minDepth=0.0|" + "maxDepth=10000";
        try {
            dataContainerTwo.setValuesFromStringRepresentation(wrongStringRep,
                this.DELIMITER);
        } catch (MetadataException e) {
            exceptionThrown = true;
        }
        assertTrue(
            "An exception should have been thrown setting values from bad string rep (bad numberOfRecords)",
            exceptionThrown);

        // NOw a bad original
        exceptionThrown = false;
        wrongStringRep = "DataContainer|" + "id=2|" + "name=DataContainerTwo|"
            + "description=DataContainerTwo Description|"
            + "dataContainerType=Stream|" + "startDate=2005-01-01T00:00:00Z|"
            + "endDate=2005-02-01T08:38:19Z|" + "original=you betcha|"
            + "uri=http://kasatka.shore.mbari.org/DataContainerTwo.xls|"
            + "contentLength=1295|" + "mimeType=app/excel|"
            + "numberOfRecords=10|" + "dodsAccessible=false|"
            + "minLatitude=6.0|" + "maxLatitude=45.0|"
            + "minLongitude=-255.34|" + "maxLongitude=-245.567|"
            + "minDepth=0.0|" + "maxDepth=10000";
        try {
            dataContainerTwo.setValuesFromStringRepresentation(wrongStringRep,
                this.DELIMITER);
        } catch (MetadataException e) {
            exceptionThrown = true;
        }
        assertTrue(
            "An exception should have been thrown setting values from bad string rep (bad original)",
            exceptionThrown);

        // Now a bad dodsAccessible
        exceptionThrown = false;
        wrongStringRep = "DataContainer|" + "id=2|" + "name=DataContainerTwo|"
            + "description=DataContainerTwo Description|"
            + "dataContainerType=Stream|" + "startDate=2005-01-01T00:00:00Z|"
            + "endDate=2005-02-01T08:38:19Z|" + "original=true|"
            + "uri=http://kasatka.shore.mbari.org/DataContainerTwo.xls|"
            + "contentLength=1295|" + "mimeType=app/excel|"
            + "numberOfRecords=10|" + "dodsAccessible=of course|"
            + "minLatitude=6.0|" + "maxLatitude=45.0|"
            + "minLongitude=-255.34|" + "maxLongitude=-245.567|"
            + "minDepth=0.0|" + "maxDepth=1000";
        try {
            dataContainerTwo.setValuesFromStringRepresentation(wrongStringRep,
                this.DELIMITER);
        } catch (MetadataException e) {
            exceptionThrown = true;
        }
        assertTrue(
            "An exception should have been thrown setting values from bad string rep (bad dodsAccessible)",
            exceptionThrown);

        // Now a bad minLatitude
        exceptionThrown = false;
        wrongStringRep = "DataContainer|" + "id=2|" + "name=DataContainerTwo|"
            + "description=DataContainerTwo Description|"
            + "dataContainerType=Stream|" + "startDate=2005-01-01T00:00:00Z|"
            + "endDate=2005-02-01T08:38:19Z|" + "original=true|"
            + "uri=http://kasatka.shore.mbari.org/DataContainerTwo.xls|"
            + "contentLength=1295|" + "mimeType=app/excel|"
            + "numberOfRecords=10|" + "dodsAccessible=false|"
            + "minLatitude=6.0 N|" + "maxLatitude=45.0|"
            + "minLongitude=-255.34|" + "maxLongitude=-245.567|"
            + "minDepth=0.0|" + "maxDepth=100000";
        try {
            dataContainerTwo.setValuesFromStringRepresentation(wrongStringRep,
                this.DELIMITER);
        } catch (MetadataException e) {
            exceptionThrown = true;
        }
        assertTrue(
            "An exception should have been thrown setting values from bad string rep (bad latMin)",
            exceptionThrown);

        // Bad LatMax
        exceptionThrown = false;
        wrongStringRep = "DataContainer|" + "id=2|" + "name=DataContainerTwo|"
            + "description=DataContainerTwo Description|"
            + "dataContainerType=Stream|" + "startDate=2005-01-01T00:00:00Z|"
            + "endDate=2005-02-01T08:38:19Z|" + "original=true|"
            + "uri=http://kasatka.shore.mbari.org/DataContainerTwo.xls|"
            + "contentLength=1295|" + "mimeType=app/excel|"
            + "numberOfRecords=10|" + "dodsAccessible=false|"
            + "minLatitude=6.0|" + "maxLatitude=45.0 N|"
            + "minLongitude=-255.34|" + "maxLongitude=-245.567|"
            + "minDepth=0.0|" + "maxDepth=100000";
        try {
            dataContainerTwo.setValuesFromStringRepresentation(wrongStringRep,
                this.DELIMITER);
        } catch (MetadataException e) {
            exceptionThrown = true;
        }
        assertTrue(
            "An exception should have been thrown setting values from bad string rep (bad LatMax)",
            exceptionThrown);

        // Bad LonMin
        exceptionThrown = false;
        wrongStringRep = "DataContainer|" + "id=2|" + "name=DataContainerTwo|"
            + "description=DataContainerTwo Description|"
            + "dataContainerType=Stream|" + "startDate=2005-01-01T00:00:00Z|"
            + "endDate=2005-02-01T08:38:19Z|" + "original=true|"
            + "uri=http://kasatka.shore.mbari.org/DataContainerTwo.xls|"
            + "contentLength=1295|" + "mimeType=app/excel|"
            + "numberOfRecords=10|" + "dodsAccessible=false|"
            + "minLatitude=6.0|" + "maxLatitude=45.0|"
            + "minLongitude=-255.34 E|" + "maxLongitude=-245.567|"
            + "minDepth=0.0|" + "maxDepth=100000";
        try {
            dataContainerTwo.setValuesFromStringRepresentation(wrongStringRep,
                this.DELIMITER);
        } catch (MetadataException e) {
            exceptionThrown = true;
        }
        assertTrue(
            "An exception should have been thrown setting values from bad string rep (bad LonMin)",
            exceptionThrown);

        // Bad LonMax
        exceptionThrown = false;
        wrongStringRep = "DataContainer|" + "id=2|" + "name=DataContainerTwo|"
            + "description=DataContainerTwo Description|"
            + "dataContainerType=Stream|" + "startDate=2005-01-01T00:00:00Z|"
            + "endDate=2005-02-01T08:38:19Z|" + "original=true|"
            + "uri=http://kasatka.shore.mbari.org/DataContainerTwo.xls|"
            + "contentLength=1295|" + "mimeType=app/excel|"
            + "numberOfRecords=10|" + "dodsAccessible=false|"
            + "minLatitude=6.0|" + "maxLatitude=45.0|"
            + "minLongitude=-255.34|" + "maxLongitude=-245.567 E|"
            + "minDepth=0.0|" + "maxDepth=100000";
        try {
            dataContainerTwo.setValuesFromStringRepresentation(wrongStringRep,
                this.DELIMITER);
        } catch (MetadataException e) {
            exceptionThrown = true;
        }
        assertTrue(
            "An exception should have been thrown setting values from bad string rep (bad LonMax)",
            exceptionThrown);

        // Bad Vertical Min
        exceptionThrown = false;
        wrongStringRep = "DataContainer|" + "id=2|" + "name=DataContainerTwo|"
            + "description=DataContainerTwo Description|"
            + "dataContainerType=Stream|" + "startDate=2005-01-01T00:00:00Z|"
            + "endDate=2005-02-01T08:38:19Z|" + "original=true|"
            + "uri=http://kasatka.shore.mbari.org/DataContainerTwo.xls|"
            + "contentLength=1295|" + "mimeType=app/excel|"
            + "numberOfRecords=10|" + "dodsAccessible=false|"
            + "minLatitude=6.0|" + "maxLatitude=45.0|"
            + "minLongitude=-255.34|" + "maxLongitude=-245.567|"
            + "minDepth=-10.0|" + "maxDepth=100000";
        try {
            dataContainerTwo.setValuesFromStringRepresentation(wrongStringRep,
                this.DELIMITER);
        } catch (MetadataException e) {
            exceptionThrown = true;
        }
        assertTrue(
            "An exception should have been thrown setting values from bad string rep (bad VerticalMin)",
            exceptionThrown);

        // Bad vertical Max
        exceptionThrown = false;
        wrongStringRep = "DataContainer|" + "id=2|" + "name=DataContainerTwo|"
            + "description=DataContainerTwo Description|"
            + "dataContainerType=Stream|" + "startDate=2005-01-01T00:00:00Z|"
            + "endDate=2005-02-01T08:38:19Z|" + "original=true|"
            + "uri=http://kasatka.shore.mbari.org/DataContainerTwo.xls|"
            + "contentLength=1295|" + "mimeType=app/excel|"
            + "numberOfRecords=10|" + "dodsAccessible=false|"
            + "minLatitude=6.0|" + "maxLatitude=45.0|"
            + "minLongitude=-255.34|" + "maxLongitude=-245.567|"
            + "minDepth=0.0|" + "maxDepth=-100000";
        try {
            dataContainerTwo.setValuesFromStringRepresentation(wrongStringRep,
                this.DELIMITER);
        } catch (MetadataException e) {
            exceptionThrown = true;
        }
        assertTrue(
            "An exception should have been thrown setting values from bad string rep (bad verticalMax)",
            exceptionThrown);
    }

    /**
     * This method tests the equals and hashCode method
     */
    public void testEqualsAndHashCode() {

        DataContainer dataContainerOne = new DataContainer();
        DataContainer dataContainerTwo = new DataContainer();

        try {
            dataContainerOne.setValuesFromStringRepresentation(
                this.dataContainerTwoStringRep, this.DELIMITER);
            dataContainerTwo.setValuesFromStringRepresentation(
                this.dataContainerTwoStringRep, this.DELIMITER);
        } catch (MetadataException e) {
            logger
                .error("MetadataException caught trying to create two dataContainer objects");
            assertTrue(
                "MetadataException caught trying to screate two dataContainer objects: "
                    + e.getMessage(), false);
        }

        assertTrue("The two dataContainers should be equal (part one).",
            dataContainerOne.equals(dataContainerTwo));
        assertEquals("The two dataContainers should be equal (part two).",
            dataContainerOne, dataContainerTwo);
        assertEquals("Their hashcodes should also be equal", dataContainerOne
            .hashCode(), dataContainerTwo.hashCode());

        // Now change the ID of the second one and they should still be equal
        dataContainerTwo.setId(new Long(3));
        assertTrue(
            "The two dataContainers should still be equal after ID change.",
            dataContainerOne.equals(dataContainerTwo));
        assertTrue("Their hashcodes should still be equal after ID change",
            dataContainerOne.hashCode() == dataContainerTwo.hashCode());

        // Now set the ID back, check equals again
        dataContainerTwo.setId(new Long(2));
        assertTrue(
            "The two dataContainers should be equal (part one) after setting the ID back.",
            dataContainerOne.equals(dataContainerTwo));
        assertEquals(
            "The two dataContainers should be equal (part two) after setting the ID back.",
            dataContainerOne, dataContainerTwo);
        assertEquals(
            "Their hashcodes should also be equal after setting the ID back",
            dataContainerOne.hashCode(), dataContainerTwo.hashCode());

        // Now set the UriString and they should be differnet
        try {
            dataContainerTwo.setUriString("Something different");
            assertTrue(
                "The two dataContainers should NOT be equal (part one) after URI change.",
                !dataContainerOne.equals(dataContainerTwo));
            assertTrue("Their hashcodes should NOT be equal after URI change",
                dataContainerOne.hashCode() != dataContainerTwo.hashCode());

            // Now set it back and change all the non-business key values. The
            // results should be equals
            dataContainerTwo
                .setUriString("http://kasatka.shore.mbari.org/DataContainerTwo.xls");
            dataContainerTwo.setName("new name");
            dataContainerTwo.setDescription("new description");
            dataContainerTwo.setContentLength(new Long(10));
            try {
                dataContainerTwo.setDataContainerType(DataContainer.TYPE_FILE);
            } catch (MetadataException e) {}
            dataContainerTwo.setDodsAccessible(new Boolean(true));
            dataContainerTwo.setEndDate(new Date());
            dataContainerTwo.setMinLatitude(new Double(0.0));
            dataContainerTwo.setMaxLatitude(new Double(0.0));
            dataContainerTwo.setMinLongitude(new Double(0.0));
            dataContainerTwo.setMaxLongitude(new Double(0.0));
            dataContainerTwo.setMaxDepth(new Float(0.0));
            dataContainerTwo.setMinDepth(new Float(0.0));
            dataContainerTwo.setMimeType("Stuck in a box");
            dataContainerTwo.setNumberOfRecords(new Long(8000000));
            dataContainerTwo.setOriginal(new Boolean(false));
            dataContainerTwo.setStartDate(new Date());
        } catch (MetadataException e) {
            assertTrue("MetadataException caught trying to set values: "
                + e.getMessage(), false);
        }
        assertTrue(
            "The two dataContainers should be equal (part one) after setting the URI back but changing other attributes.",
            dataContainerOne.equals(dataContainerTwo));
        assertEquals(
            "The two dataContainers should be equal (part one) after setting the URI back but changing other attributes.",
            dataContainerOne, dataContainerTwo);
        assertEquals(
            "Their hashcodes should also be equal after setting the URI back but changing other attributes.",
            dataContainerOne.hashCode(), dataContainerTwo.hashCode());
    }

    public void testDateRange() {
        DataContainer dataContainerDR = new DataContainer();
        // Set start Date to 2 years after epoch 0
        long numberOfMillisecondsInAnHour = 1000 * 60 * 60;
        long numberOfMillisecondsInADay = numberOfMillisecondsInAnHour * 24;
        long numberOfMillisecondsInAYear = numberOfMillisecondsInADay * 365;

        dataContainerDR.setStartDate(new Date(numberOfMillisecondsInAYear * 2));
        logger.debug("startDate set to " + dataContainerDR.getStartDate());
        // Set end Date to 2 years after that
        dataContainerDR.setEndDate(new Date(numberOfMillisecondsInAYear * 4));
        logger.debug("endDate set to " + dataContainerDR.getEndDate());

        // Now get the daterange object
        DateRange dr = (DateRange) dataContainerDR.getDateRange();
        // Now create a date that is after the DataContainer
        Date afterDC = new Date(numberOfMillisecondsInAYear * 5);
        logger.debug("afterDC is " + afterDC);
        // Now one that is before it
        Date beforeDC = new Date(numberOfMillisecondsInAYear);
        logger.debug("beforeDC is " + beforeDC);
        // And one during it
        Date duringDC = new Date(numberOfMillisecondsInAYear * 3);
        logger.debug("duringDC is " + duringDC);

        // Now check relationships
        assertTrue("DataContainer should be before afterDC date.", dr
            .before(afterDC));
        assertTrue("DataContainer should be after beforeDC date.", dr
            .after(beforeDC));
        assertTrue("DataContainer should be during duringDC date.", dr
            .during(duringDC));

        // Check intervals
        assertTrue("The milliseconds duration should be 2 years worth", dr
            .duration() == numberOfMillisecondsInAYear * 2);
        assertTrue("The seconds duration should be 2 years worth", dr
            .durationSeconds() == (numberOfMillisecondsInAYear * 2) / 1000);
        assertEquals("The hours duration should be 2 years worth", dr
            .durationHours(), new Float(
            (numberOfMillisecondsInAYear * 2) / 3600000).floatValue(), 0.005);
        logger.debug("durationHours = "
            + dr.durationHours()
            + ", float calc = "
            + new Float((numberOfMillisecondsInAYear * 2) / 3600000)
                .floatValue());
        assertEquals("Check duration in days", dr.durationDays(), new Float(
            (numberOfMillisecondsInAYear * 2) / (3600000 * 24)).floatValue(),
            0.005);
        logger.debug("durationDays = "
            + dr.durationDays()
            + ", float calc = "
            + new Float((numberOfMillisecondsInAYear * 2) / (3600000 * 24))
                .floatValue());

    }

    /**
     * A method to get clean copies of the DataContainers
     * 
     * @throws MetadataException
     */
    private void rebuildDataContainerObjectsFromStringReps() {
        try {
            dataContainerOne = (DataContainer) MetadataFactory
                .createMetadataObjectFromStringRepresentation(
                    dataContainerOneStringRep, DELIMITER);
            dataContainerTwo = (DataContainer) MetadataFactory
                .createMetadataObjectFromStringRepresentation(
                    dataContainerTwoStringRep, DELIMITER);
        } catch (MetadataException e) {
            assertTrue("MetadataException caught trying to "
                + "build data containers from strings", false);
        }
    }

    /**
     * This method takes in two date object and checks to see if they are equal
     * to the trunacted seconds. Milliseconds are ignored
     */
    private boolean datesEqualToSecond(Date dateOne, Date dateTwo) {
        if ((dateOne == null) || (dateTwo == null)) {
            return false;
        }
        long dateOneMillis = dateOne.getTime();
        Long dateOneSeconds = new Long(dateOneMillis / 1000);
        long dateTwoMillis = dateTwo.getTime();
        Long dateTwoSeconds = new Long(dateTwoMillis / 1000);
        if (dateOneSeconds.longValue() == dateTwoSeconds.longValue()) {
            return true;
        } else {
            return false;
        }
    }

    private DataContainer dataContainerOne = null;
    private DataContainer dataContainerTwo = null;

    private final String dataContainerOneStringRep = "DataContainer|" + "id=1|"
        + "name=DataContainerOne|"
        + "description=DataContainerOne Description|"
        + "dataContainerType=File|" + "startDate=2003-05-05T16:11:44Z|"
        + "endDate=2004-02-01T08:38:19Z|" + "original=true|"
        + "uriString=http://kasatka.shore.mbari.org/DataContainerOne.txt|"
        + "contentLength=50000|" + "mimeType=CSV|" + "numberOfRecords=800|"
        + "dodsAccessible=false|" + "dodsUrlString=http://dods.server.com|"
        + "noNetCDF=true|" + "minLatitude=36.6|" + "maxLatitude=36.805|"
        + "minLongitude=-121.56|" + "maxLongitude=-121.034|" + "minDepth=0.0|"
        + "maxDepth=10.0";
    private final String dataContainerTwoStringRep = "DataContainer|" + "id=2|"
        + "name=DataContainerTwo|"
        + "description=DataContainerTwo Description|"
        + "dataContainerType=Stream|" + "startDate=2005-01-01T00:00:00Z|"
        + "endDate=2005-02-01T08:38:19Z|" + "original=true|"
        + "uriString=http://kasatka.shore.mbari.org/DataContainerTwo.xls|"
        + "contentLength=1295|" + "mimeType=app/excel|" + "numberOfRecords=10|"
        + "dodsAccessible=false|" + "dodsUrlString=http://dods.server.com|"
        + "noNetCDF=true|" + "minLatitude=6.0|" + "maxLatitude=45.0|"
        + "minLongitude=-255.34|" + "maxLongitude=-245.567|" + "minDepth=0.0|"
        + "maxDepth=10000";

    private final String DELIMITER = "|";

    /**
     * A date formatter
     */
    private XmlDateFormat xmlDateFormat = new XmlDateFormat();

    /**
     * The logger for dumping information to
     */
    static Logger logger = Logger.getLogger(TestDataContainer.class);
}