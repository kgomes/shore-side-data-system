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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

import junit.framework.TestCase;
import moos.ssds.data.parsers.BinaryPacketParser;
import moos.ssds.metadata.RecordDescription;
import moos.ssds.metadata.RecordVariable;
import moos.ssds.transmogrify.SSDSDevicePacket;

import org.apache.log4j.Logger;

import com.mindprod.ledatastream.LEDataOutputStream;

/**
 * JUnit TestCase.
 * 
 * @testfamily JUnit
 * @testkind testcase
 * @testsetup Default TestCase
 */
public class TestBinaryPacketParser extends TestCase {

    /**
     * Constructs a test case with the given name.
     */
    public TestBinaryPacketParser(String name) {
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

        // First create one output stream that is big endian
        ByteArrayOutputStream bebyteArray = new ByteArrayOutputStream();
        ByteArrayOutputStream lebyteArray = new ByteArrayOutputStream();

        DataOutputStream bedos = new DataOutputStream(bebyteArray);
        LEDataOutputStream ledos = new LEDataOutputStream(lebyteArray);

        // Now write some things to both
        int testInt = -1;
        long testLong = 9991992938L;
        double testDouble = -34.5664;
        float testFloat = 202099.894F;

        try {
            bedos.writeInt(testInt);
            ledos.writeInt(testInt);
            bedos.writeLong(testLong);
            ledos.writeLong(testLong);
            bedos.writeDouble(testDouble);
            ledos.writeDouble(testDouble);
            bedos.writeFloat(testFloat);
            ledos.writeFloat(testFloat);
            bedos.writeFloat(testFloat);
            ledos.writeFloat(testFloat);
            bedos.writeDouble(testDouble);
            ledos.writeDouble(testDouble);
            bedos.writeLong(testLong);
            ledos.writeLong(testLong);
            bedos.writeInt(testInt);
            ledos.writeInt(testInt);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Grab the bytes
        byte[] bebytes = bebyteArray.toByteArray();
        byte[] lebytes = lebyteArray.toByteArray();

        // Now the record desriptions and recordVaribles should be exactly the
        // same except for endianess. Let's start with big endian
        RecordDescription rd = new RecordDescription();
        try {
            rd.setBufferStyle(RecordDescription.BUFFER_STYLE_BINARY);
            rd
                .setBufferParseType(RecordDescription.PARSE_TYPE_ORDERED_POSITION);
            rd.setEndian(RecordDescription.ENDIAN_BIG);
            rd.setParseable(Boolean.TRUE);
            rd.setRecordType(new Long(1));

            // Now add RecordVariables
            RecordVariable rv1 = new RecordVariable();
            rv1.setColumnIndex(1);
            rv1.setDescription("Test Int First Spot");
            rv1.setFormat("integer");
            rv1.setLongName("First Test Int");
            rv1.setName("integer_one");
            rv1.setUnits("count");
            rd.addRecordVariable(rv1);

            RecordVariable rv2 = new RecordVariable();
            rv2.setColumnIndex(2);
            rv2.setDescription("Test Long First Spot");
            rv2.setFormat("long");
            rv2.setLongName("First Test Long");
            rv2.setName("long_one");
            rv2.setUnits("count");
            rd.addRecordVariable(rv2);

            RecordVariable rv3 = new RecordVariable();
            rv3.setColumnIndex(3);
            rv3.setDescription("Test Double First Spot");
            rv3.setFormat("double");
            rv3.setLongName("First Test Double");
            rv3.setName("double_one");
            rv3.setUnits("NA");
            rd.addRecordVariable(rv3);

            RecordVariable rv4 = new RecordVariable();
            rv4.setColumnIndex(4);
            rv4.setDescription("Test Float First Spot");
            rv4.setFormat("float");
            rv4.setLongName("First Test Float");
            rv4.setName("float_one");
            rv4.setUnits("NA");
            rd.addRecordVariable(rv4);

            RecordVariable rv5 = new RecordVariable();
            rv5.setColumnIndex(5);
            rv5.setDescription("Test Float Second Spot");
            rv5.setFormat("float");
            rv5.setLongName("Second Test Float");
            rv5.setName("float_two");
            rv5.setUnits("NA");
            rd.addRecordVariable(rv5);

            RecordVariable rv6 = new RecordVariable();
            rv6.setColumnIndex(6);
            rv6.setDescription("Test Double Second Spot");
            rv6.setFormat("double");
            rv6.setLongName("Second Test Double");
            rv6.setName("double_two");
            rv6.setUnits("NA");
            rd.addRecordVariable(rv6);

            RecordVariable rv7 = new RecordVariable();
            rv7.setColumnIndex(7);
            rv7.setDescription("Test Long Second Spot");
            rv7.setFormat("long");
            rv7.setLongName("Second Test Long");
            rv7.setName("long_two");
            rv7.setUnits("count");
            rd.addRecordVariable(rv7);

            RecordVariable rv8 = new RecordVariable();
            rv8.setColumnIndex(8);
            rv8.setDescription("Test Int Second Spot");
            rv8.setFormat("int");
            rv8.setLongName("Second Test Int");
            rv8.setName("int_two");
            rv8.setUnits("count");
            rd.addRecordVariable(rv8);

            // Create the test packet
            SSDSDevicePacket testPacket = new SSDSDevicePacket(99L,
                bebytes.length);
            testPacket.setSystemTime(new Date().getTime());
            testPacket.setMetadataRef(1L);
            testPacket.setDataDescriptionVersion(1L);
            testPacket.setMetadataSequenceNumber(1L);
            testPacket.setOtherBuffer(new byte[0]);
            testPacket.setPacketType(1);
            testPacket.setParentId(100);
            testPacket.setPlatformID(100L);
            testPacket.setRecordType(1L);
            testPacket.setSequenceNo(1L);

            // Set the bytes in the data packet to the big endian byte array
            testPacket.setDataBuffer(bebytes);

            // Create the BinaryPacketParser
            BinaryPacketParser bpp = new BinaryPacketParser(rd);

            // Now parse the record
            Map recordMap = bpp.parse(testPacket);

            // Now read the values and make sure they work
            Integer firstInt = (Integer) recordMap.get(rv1);
            assertEquals("First int should be equal", testInt, firstInt
                .intValue());
            Integer secondInt = (Integer) recordMap.get(rv8);
            assertEquals("Second int should be equal", testInt, secondInt
                .intValue());
            Long firstLong = (Long) recordMap.get(rv2);
            assertEquals("First long should be equal", testLong, firstLong
                .longValue());
            Long secondLong = (Long) recordMap.get(rv7);
            assertEquals("Second long should be equal", testLong, secondLong
                .longValue());
            Double firstDouble = (Double) recordMap.get(rv3);
            assertEquals("First double should be equal", testDouble,
                firstDouble.doubleValue(), 0.01);
            Double secondDouble = (Double) recordMap.get(rv6);
            assertEquals("Second double should be equal", testDouble,
                secondDouble.doubleValue(), 0.01);
            Float firstFloat = (Float) recordMap.get(rv4);
            assertEquals("First float should be equal", testFloat, firstFloat
                .floatValue(), 0.01);
            Float secondFloat = (Float) recordMap.get(rv5);
            assertEquals("Second float should be equal", testFloat, secondFloat
                .floatValue(), 0.01);

            // Now set the endian to little and parse the le byte array
            rd.setEndian(RecordDescription.ENDIAN_LITTLE);

            // Create the BinaryRecordParser
            bpp = new BinaryPacketParser(rd);

            // Set the packet bytes to the little endian byte array
            testPacket.setDataBuffer(lebytes);

            // Now parse the record
            recordMap = bpp.parse(testPacket);

            // Now read the values and make sure they work
            firstInt = (Integer) recordMap.get(rv1);
            assertEquals("First int should be equal", testInt, firstInt
                .intValue());
            secondInt = (Integer) recordMap.get(rv8);
            assertEquals("Second int should be equal", testInt, secondInt
                .intValue());
            firstLong = (Long) recordMap.get(rv2);
            assertEquals("First long should be equal", testLong, firstLong
                .longValue());
            secondLong = (Long) recordMap.get(rv7);
            assertEquals("Second long should be equal", testLong, secondLong
                .longValue());
            firstDouble = (Double) recordMap.get(rv3);
            assertEquals("First double should be equal", testDouble,
                firstDouble.doubleValue(), 0.01);
            secondDouble = (Double) recordMap.get(rv6);
            assertEquals("Second double should be equal", testDouble,
                secondDouble.doubleValue(), 0.01);
            firstFloat = (Float) recordMap.get(rv4);
            assertEquals("First float should be equal", testFloat, firstFloat
                .floatValue(), 0.01);
            secondFloat = (Float) recordMap.get(rv5);
            assertEquals("Second float should be equal", testFloat, secondFloat
                .floatValue(), 0.01);
        } catch (Throwable t) {
            logger
                .error("MetadataException caught trying to set up everything:"
                    + t.getMessage());
        }
    }

    /**
     * A log4j logger
     */
    static Logger logger = Logger.getLogger(TestBinaryPacketParser.class);
}
