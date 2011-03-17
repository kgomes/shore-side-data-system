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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringWriter;
import java.util.Iterator;

import junit.framework.TestCase;
import moos.ssds.metadata.Metadata;
import moos.ssds.metadata.Person;
import moos.ssds.metadata.UserGroup;
import moos.ssds.metadata.util.MetadataException;

import org.apache.log4j.Logger;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;

/**
 * This is the test class to test the Person class
 * 
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.4 $
 */
public class TestAUVMissionXML extends TestCase {

	/**
	 * The logger for dumping information to
	 */
	static Logger logger = Logger.getLogger(TestAUVMissionXML.class);

	/**
	 * @param arg0
	 */
	public TestAUVMissionXML(String arg0) {
		super(arg0);
	}

	protected void setUp() {
	}

	public void testXMLBinding() {
		// Grab the file that has the XML in it
		File auvXMLFile = new File("src" + File.separator + "resources"
				+ File.separator + "test" + File.separator + "xml"
				+ File.separator + "AUVMission.xml");
		if (!auvXMLFile.exists())
			assertTrue("Could not find AUVMission.xml file for testing.", false);
		logger.debug("Will read person XML from "
				+ auvXMLFile.getAbsolutePath());

		// Create a file reader
		FileReader auvMissionXMLFileReader = null;
		try {
			auvMissionXMLFileReader = new FileReader(auvXMLFile);
		} catch (FileNotFoundException e2) {
			assertTrue(
					"Error in creating file reader for auv mission XML file: "
							+ e2.getMessage(), false);
		}

		// Grab the binding factory for DataProducer
		IBindingFactory bfact = null;
		try {
			bfact = BindingDirectory.getFactory(Metadata.class);
		} catch (JiBXException e1) {
			assertTrue(
					"Error in getting Binding Factory for Metadata: "
							+ e1.getMessage(), false);
		}

		// Grab a JiBX unmarshalling context
		IUnmarshallingContext uctx = null;
		if (bfact != null) {
			try {
				uctx = bfact.createUnmarshallingContext();
			} catch (JiBXException e) {
				assertTrue("Error in getting UnmarshallingContext for Person: "
						+ e.getMessage(), false);
			}
		}
		// Now unmarshall it
		if (uctx != null) {
			Metadata metadata = null;
			// Person testPerson = null;
			try {

				metadata = (Metadata) uctx.unmarshalDocument(
						auvMissionXMLFileReader, null);
				logger.debug("Metadata-> "
						+ metadata.toStringRepresentation("|"));
			} catch (JiBXException e1) {
				assertTrue("Error in unmarshalling Person: " + e1.getMessage(),
						false);
			}

		}

	}

}