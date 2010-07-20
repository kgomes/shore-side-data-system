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
package test.moos.ssds.wrapper.ogc.sensorml;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;

import org.apache.log4j.Logger;

import test.moos.ssds.metadata.util.TestMetadataFactory;

import moos.ssds.metadata.DataContainer;
import moos.ssds.metadata.DataProducer;
import moos.ssds.metadata.Device;
import moos.ssds.metadata.CommentTag;
import moos.ssds.metadata.RecordVariable;
import moos.ssds.metadata.util.MetadataException;
import moos.ssds.metadata.util.MetadataFactory;
import moos.ssds.wrapper.ogc.sensorml.SensorMLFactory;
import junit.framework.TestCase;

public class TestSensorMLFactory extends TestCase {
	/**
	 * @param arg0
	 */
	public TestSensorMLFactory(String arg0) {
		super(arg0);
	}

	public void testCreateSensorMLObjectFromMetadataObject() {

		logger.debug("Running testCreateSensorMLObjectFromMetadataObject");

		// Create a DataProducer
		DataProducer dpToConvert = new DataProducer();
		try {
			dpToConvert.setName("Test SensorML Deployment");
			dpToConvert.setDescription("This is a test deployment to try and "
					+ "convert to a SensorML serialized XML doc");
		} catch (Exception e) {
			e.printStackTrace();
		}
		dpToConvert.setId(new Long(1));

		// Create a Device
		Device testDevice = new Device();
		testDevice.setId(new Long(2));
		testDevice.generateOwnUuid();
		try {
			testDevice.setDescription("Test Device with ID equal to 2");
		} catch (MetadataException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		testDevice.setMfgName("Test Manufacturer");
		testDevice.setMfgModel("Automaton");
		testDevice.setMfgSerialNumber("THX-1138");
		try {
			testDevice.setInfoUrlList("http://en.wikipedia.org/wiki/THX_1138");
		} catch (MetadataException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		dpToConvert.setDevice(testDevice);

		// Create a DataContainer
		DataContainer output = new DataContainer();
		try {
			output.setDataContainerType(DataContainer.TYPE_STREAM);
		} catch (MetadataException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		output.setId(new Long(99));
		try {
			output.setUriString("http://www.mbari.org/ssds");
		} catch (MetadataException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// A RecordDescription
		moos.ssds.metadata.RecordDescription rd = new moos.ssds.metadata.RecordDescription();
		rd.setParseable(true);
		rd.setBufferItemSeparator(",");
		try {
			rd.setBufferStyle(moos.ssds.metadata.RecordDescription.BUFFER_STYLE_ASCII);
			rd.setRecordType(new Long(1));
		} catch (NumberFormatException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (MetadataException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		// RVs
		RecordVariable rv1 = new RecordVariable();
		try {
			rv1.setName("TestRV1");
			rv1.setDescription("Test RecordVariable 1");
			rv1.setUnits("TestRV1Units");
			rv1.setFormat("Double");
			rv1.setColumnIndex(1);
		} catch (MetadataException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		RecordVariable rv2 = new RecordVariable();
		try {
			rv2.setName("TestRV2");
			rv2.setDescription("Test RecordVariable 2");
			rv2.setUnits("TestRV2Units");
			rv2.setFormat("Double");
			rv2.setColumnIndex(2);
		} catch (MetadataException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		rd.addRecordVariable(rv1);
		rd.addRecordVariable(rv2);
		output.setRecordDescription(rd);

		// Add as output
		dpToConvert.addOutput(output);

		// Create a data record
		String dataRecord = "13.4,-1456.1";

		// Convert it
		Object sensorMLObject = SensorMLFactory
				.createSensorMLFromMetadataObject(dpToConvert, dataRecord
						.getBytes());

		try {
			JAXBContext jaxbContext = JAXBContext
					.newInstance("net.opengis.sensorml.v_1_0_1");
			Marshaller omMarshaller = jaxbContext.createMarshaller();
			omMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
					Boolean.TRUE);
			omMarshaller.marshal(sensorMLObject, System.out);
		} catch (PropertyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		assert (true);
	}

	static Logger logger = Logger.getLogger(TestSensorMLFactory.class);

}
