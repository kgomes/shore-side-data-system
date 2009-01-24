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

import junit.framework.TestCase;
import moos.ssds.metadata.Person;
import moos.ssds.metadata.util.MetadataException;
import moos.ssds.metadata.util.MetadataFactory;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * This class tests the <code>MetadataFactory</code> class.
 * 
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.3 $
 */
public class TestMetadataFactory extends TestCase {

	/**
	 * @param arg0
	 */
	public TestMetadataFactory(String arg0) {
		super(arg0);
	}

	protected void setUp() {
		BasicConfigurator.configure();
		logger.setLevel(Level.DEBUG);
		personTwo = new Person();
		personTwo.setId(new Long(id));
		try {
			personTwo.setFirstname(firstname);
			personTwo.setSurname(surname);
			personTwo.setOrganization(organization);
			personTwo.setEmail(email);
			personTwo.setUsername(username);
			personTwo.setPassword(password);
			personTwo.setStatus(status);
		} catch (MetadataException e) {
			logger.error("Could not set properties: " + e.getMessage());
		}
	}

	public void testPersonConstruction() {
		try {
			personOne = (Person) MetadataFactory
					.createMetadataObjectFromStringRepresentation(
							personOneStringRep, "|");
		} catch (MetadataException e) {
			assertTrue(
					"Should not have gotten an exception trying to create the person from string",
					false);
		} catch (ClassCastException e) {
			assertTrue(
					"Should not have gotten an exception trying to cast to a Person",
					false);
		}
		// Now compare
		assertEquals("ID's should be equal.", personOne.getId(), personTwo
				.getId());
		assertEquals("Firstnames should be equal.", personOne.getFirstname(),
				personTwo.getFirstname());
		assertEquals("Surnames should be equal.", personOne.getSurname(),
				personTwo.getSurname());
		assertEquals("Organizations should be equal.", personOne
				.getOrganization(), personTwo.getOrganization());
		assertEquals("Emails should be equal.", personOne.getEmail(), personTwo
				.getEmail());
		assertEquals("Usernames should be equal.", personOne.getUsername(),
				personTwo.getUsername());
		assertEquals("Passwords should be equal.", personOne.getPassword(),
				personTwo.getPassword());
		assertEquals("Status should be equal", personOne.getStatus(), personTwo
				.getStatus());
	}

	static Logger logger = Logger.getLogger(TestMetadataFactory.class);

	// Person stuff
	private String id = "1";

	private String firstname = "John";

	private String surname = "Doe";

	private String organization = "MBARI";

	private String email = "jdoe@mbari.org";

	private String username = "jdoe";

	private String password = "dumbPassword";

	private String status = "active";

	private String personOneStringRep = "Person|" + "id=1|" + "firstname=John|"
			+ "surname=Doe|" + "organization=MBARI|" + "email=jdoe@mbari.org|"
			+ "username=jdoe|" + "password=dumbPassword|" + "status=active";

	private Person personOne = null;

	private Person personTwo = null;

}