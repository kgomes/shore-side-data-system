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

import java.util.Date;
import java.util.Map;

import junit.framework.TestCase;
import moos.ssds.data.parsers.AsciiPacketParser;
import moos.ssds.metadata.RecordDescription;
import moos.ssds.metadata.RecordVariable;
import moos.ssds.transmogrify.SSDSDevicePacket;

import org.apache.log4j.Logger;

/**
 * JUnit TestCase.
 * 
 * @testfamily JUnit
 * @testkind testcase
 * @testsetup Default TestCase
 */
public class TestAsciiPacketParser extends TestCase {

    /**
     * Constructs a test case with the given name.
     */
    public TestAsciiPacketParser(String name) {
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
    public void testParse1() {

        // First create the SSDSDevicePacket
        SSDSDevicePacket testPacket = new SSDSDevicePacket(99L, ascii1
            .getBytes().length);
        testPacket.setSystemTime(new Date().getTime());
        testPacket.setMetadataRef(1L);
        testPacket.setDataBuffer(ascii1.getBytes());
        testPacket.setDataDescriptionVersion(1L);
        testPacket.setMetadataSequenceNumber(1L);
        testPacket.setOtherBuffer(new byte[0]);
        testPacket.setPacketType(1);
        testPacket.setParentId(100);
        testPacket.setPlatformID(100L);
        testPacket.setRecordType(1L);
        testPacket.setSequenceNo(1L);

        // RecordDescription level
        RecordDescription rd = new RecordDescription();
        try {
            rd
                .setRecordParseRegExp("^\\$(\\w+),\\s*P(\\d+\\.\\d+),\\s*T(\\d+\\.\\d+),\\s*H(\\d+\\.\\d+),\\s*GFL(\\d+\\.\\d+),\\s*GFH(\\d+\\.\\d+),\\s*C(\\d+\\.\\d+),\\s*TC(-*\\d+\\.\\d+),\\s*\\*(\\d+)$");
            rd.setBufferStyle(RecordDescription.BUFFER_STYLE_ASCII);
            rd.setParseable(Boolean.TRUE);
            rd.setRecordType(new Long(1));

            // Now add RecordVariables
            RecordVariable rv1 = new RecordVariable();
            rv1.setColumnIndex(1);
            rv1.setDescription("Data Type");
            rv1.setFormat("String");
            rv1.setLongName("Data Type Long Name");
            rv1.setName("DataType");
            rv1.setUnits("NA");
            rd.addRecordVariable(rv1);

            RecordVariable rv2 = new RecordVariable();
            rv2.setColumnIndex(2);
            rv2.setDescription("This is the pressure in the can");
            rv2.setFormat("double");
            rv2.setLongName("Can Pressure");
            rv2.setName("Pressure");
            rv2.setUnits("millibars");
            rd.addRecordVariable(rv2);

            RecordVariable rv3 = new RecordVariable();
            rv3.setColumnIndex(3);
            rv3
                .setDescription("Temperature measured inside the MMC controller pressure housing");
            rv3.setFormat("double");
            rv3.setLongName("Temperature Inside MMC Can");
            rv3.setName("temperature");
            rv3.setUnits("degree C");
            rd.addRecordVariable(rv3);

            RecordVariable rv4 = new RecordVariable();
            rv4.setColumnIndex(4);
            rv4
                .setDescription("Relative humidity measured inside the MMC controller pressure housing");
            rv4.setFormat("double");
            rv4.setLongName("Relative Humidity");
            rv4.setName("relative_humidity");
            rv4.setUnits("percent");
            rd.addRecordVariable(rv4);

            RecordVariable rv5 = new RecordVariable();
            rv5.setColumnIndex(5);
            rv5
                .setDescription("Ground fault voltage measured on low voltage circuit");
            rv5.setFormat("double");
            rv5.setLongName("Ground Fault Low");
            rv5.setName("ground_fault_low");
            rv5.setUnits("volts");
            rd.addRecordVariable(rv5);

            RecordVariable rv6 = new RecordVariable();
            rv6.setColumnIndex(6);
            rv6
                .setDescription("Ground fault voltage measured on high voltage circuit");
            rv6.setFormat("double");
            rv6.setLongName("Ground Fault High Voltage");
            rv6.setName("ground_fault_high");
            rv6.setUnits("volts");
            rd.addRecordVariable(rv6);

            RecordVariable rv7 = new RecordVariable();
            rv7.setColumnIndex(7);
            rv7.setDescription("The heading of the compass");
            rv7.setFormat("double");
            rv7.setLongName("Compass Heading");
            rv7.setName("heading");
            rv7.setUnits("decimal_degrees");
            rd.addRecordVariable(rv7);

            RecordVariable rv8 = new RecordVariable();
            rv8.setColumnIndex(8);
            rv8.setDescription("This is the number of turns counted");
            rv8.setFormat("double");
            rv8.setLongName("Number of Turns");
            rv8.setName("turn_counter");
            rv8.setUnits("count");
            rd.addRecordVariable(rv8);

            RecordVariable rv9 = new RecordVariable();
            rv9.setColumnIndex(9);
            rv9.setDescription("This is the checksum of the data");
            rv9.setFormat("long");
            rv9.setLongName("Packet Checksum");
            rv9.setName("checksum");
            rv9.setUnits("NA");
            rd.addRecordVariable(rv9);

            // Create the AsciiPacketParser
            AsciiPacketParser app = new AsciiPacketParser(rd);

            // Now parse the record
            Map recordMap = app.parse(testPacket);

            // Now read the values and make sure they work
            String dataType = (String) recordMap.get(rv1);
            assertEquals("The two datatypes should be equal", "PEDATA",
                dataType);
            Double pressure = (Double) recordMap.get(rv2);
            assertEquals("The two pressures should be equal", Double
                .parseDouble("1017.98"), pressure.doubleValue(), 0.01);
            Double temperature = (Double) recordMap.get(rv3);
            assertEquals("The temperature should be equal", Double
                .parseDouble("25.81"), temperature.doubleValue(), 0.01);
            Double humidity = (Double) recordMap.get(rv4);
            assertEquals("The humidity should be equal", Double
                .parseDouble("48.26"), humidity.doubleValue(), 0.01);
            Double gfl = (Double) recordMap.get(rv5);
            assertEquals("The gfl should be equal", Double.parseDouble("0.00"),
                gfl.doubleValue(), 0.01);
            Double gfh = (Double) recordMap.get(rv6);
            assertEquals("The gfh should be equal", Double.parseDouble("0.00"),
                gfh.doubleValue(), 0.01);
            Double ch = (Double) recordMap.get(rv7);
            assertEquals("The compass heading should be equal", Double
                .parseDouble("35.11"), ch.doubleValue(), 0.01);
            Double tc = (Double) recordMap.get(rv8);
            assertEquals("The turns counter should be equal", Double
                .parseDouble("-5.00"), tc.doubleValue(), 0.01);
            Long checksum = (Long) recordMap.get(rv9);
            assertEquals("The checksum should be equal",
                Long.parseLong("3649"), checksum.longValue());

        } catch (Throwable t) {
            logger
                .error("MetadataException caught trying to set up everything:"
                    + t.getMessage());
        }
        // Now let's try it by putting parse regexp's in the variables
        // themselves
        rd = new RecordDescription();
        try {
            rd.setBufferStyle(RecordDescription.BUFFER_STYLE_ASCII);
            rd.setBufferItemSeparator(",");
            rd.setParseable(Boolean.TRUE);
            rd.setRecordType(new Long(1));

            // Now add RecordVariables
            RecordVariable rv1 = new RecordVariable();
            rv1.setColumnIndex(1);
            rv1.setDescription("Data Type");
            rv1.setFormat("String");
            rv1.setLongName("Data Type Long Name");
            rv1.setName("DataType");
            rv1.setUnits("NA");
            rv1.setParseRegExp("\\$(\\w+)");
            rd.addRecordVariable(rv1);

            RecordVariable rv2 = new RecordVariable();
            rv2.setColumnIndex(2);
            rv2.setDescription("This is the pressure in the can");
            rv2.setFormat("double");
            rv2.setLongName("Can Pressure");
            rv2.setName("Pressure");
            rv2.setUnits("millibars");
            rv2.setParseRegExp("P(\\d+\\.\\d+)");
            rd.addRecordVariable(rv2);

            RecordVariable rv3 = new RecordVariable();
            rv3.setColumnIndex(3);
            rv3
                .setDescription("Temperature measured inside the MMC controller pressure housing");
            rv3.setFormat("double");
            rv3.setLongName("Temperature Inside MMC Can");
            rv3.setName("temperature");
            rv3.setUnits("degree C");
            rv3.setParseRegExp("T(\\d+\\.\\d+)");
            rd.addRecordVariable(rv3);

            RecordVariable rv4 = new RecordVariable();
            rv4.setColumnIndex(4);
            rv4
                .setDescription("Relative humidity measured inside the MMC controller pressure housing");
            rv4.setFormat("double");
            rv4.setLongName("Relative Humidity");
            rv4.setName("relative_humidity");
            rv4.setUnits("percent");
            rv4.setParseRegExp("H(\\d+\\.\\d+)");
            rd.addRecordVariable(rv4);

            RecordVariable rv5 = new RecordVariable();
            rv5.setColumnIndex(5);
            rv5
                .setDescription("Ground fault voltage measured on low voltage circuit");
            rv5.setFormat("double");
            rv5.setLongName("Ground Fault Low");
            rv5.setName("ground_fault_low");
            rv5.setUnits("volts");
            rv5.setParseRegExp("GFL(\\d+\\.\\d+)");
            rd.addRecordVariable(rv5);

            RecordVariable rv6 = new RecordVariable();
            rv6.setColumnIndex(6);
            rv6
                .setDescription("Ground fault voltage measured on high voltage circuit");
            rv6.setFormat("double");
            rv6.setLongName("Ground Fault High Voltage");
            rv6.setName("ground_fault_high");
            rv6.setUnits("volts");
            rv6.setParseRegExp("GFH(\\d+\\.\\d+)");
            rd.addRecordVariable(rv6);

            RecordVariable rv7 = new RecordVariable();
            rv7.setColumnIndex(7);
            rv7.setDescription("The heading of the compass");
            rv7.setFormat("double");
            rv7.setLongName("Compass Heading");
            rv7.setName("heading");
            rv7.setUnits("decimal_degrees");
            rv7.setParseRegExp("C(\\d+\\.\\d+)");
            rd.addRecordVariable(rv7);

            RecordVariable rv8 = new RecordVariable();
            rv8.setColumnIndex(8);
            rv8.setDescription("This is the number of turns counted");
            rv8.setFormat("double");
            rv8.setLongName("Number of Turns");
            rv8.setName("turn_counter");
            rv8.setUnits("count");
            rv8.setParseRegExp("TC(-*\\d+\\.\\d+)");
            rd.addRecordVariable(rv8);

            RecordVariable rv9 = new RecordVariable();
            rv9.setColumnIndex(9);
            rv9.setDescription("This is the checksum of the data");
            rv9.setFormat("long");
            rv9.setLongName("Packet Checksum");
            rv9.setName("checksum");
            rv9.setUnits("NA");
            rv9.setParseRegExp("\\*(\\d+)");
            rd.addRecordVariable(rv9);

            // Create the AsciiPacketParser
            AsciiPacketParser app = new AsciiPacketParser(rd);

            // Now parse the record
            Map recordMap = app.parse(testPacket);

            // Now read the values and make sure they work
            String dataType = (String) recordMap.get(rv1);
            assertEquals("The two datatypes should be equal", "PEDATA",
                dataType);
            Double pressure = (Double) recordMap.get(rv2);
            assertEquals("The two pressures should be equal", Double
                .parseDouble("1017.98"), pressure.doubleValue(), 0.01);
            Double temperature = (Double) recordMap.get(rv3);
            assertEquals("The temperature should be equal", Double
                .parseDouble("25.81"), temperature.doubleValue(), 0.01);
            Double humidity = (Double) recordMap.get(rv4);
            assertEquals("The humidity should be equal", Double
                .parseDouble("48.26"), humidity.doubleValue(), 0.01);
            Double gfl = (Double) recordMap.get(rv5);
            assertEquals("The gfl should be equal", Double.parseDouble("0.00"),
                gfl.doubleValue(), 0.01);
            Double gfh = (Double) recordMap.get(rv6);
            assertEquals("The gfh should be equal", Double.parseDouble("0.00"),
                gfh.doubleValue(), 0.01);
            Double ch = (Double) recordMap.get(rv7);
            assertEquals("The compass heading should be equal", Double
                .parseDouble("35.11"), ch.doubleValue(), 0.01);
            Double tc = (Double) recordMap.get(rv8);
            assertEquals("The turns counter should be equal", Double
                .parseDouble("-5.00"), tc.doubleValue(), 0.01);
            Long checksum = (Long) recordMap.get(rv9);
            assertEquals("The checksum should be equal",
                Long.parseLong("3649"), checksum.longValue());
        } catch (Throwable t) {
            logger
                .error("MetadataException caught trying to set up everything:"
                    + t.getMessage());
        }

    }

    /**
     * The strings to parse
     */
    private String ascii1 = "$PEDATA, P1017.98, T25.81, H48.26, GFL0.00, GFH0.00, C35.11, TC-5.00, *3649";

    /**
     * A log4j logger
     */
    static Logger logger = Logger.getLogger(TestAsciiPacketParser.class);
}
