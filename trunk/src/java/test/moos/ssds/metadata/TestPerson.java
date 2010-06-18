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
public class TestPerson extends TestCase {

	/**
	 * The logger for dumping information to
	 */
	static Logger logger = Logger.getLogger(TestPerson.class);

	/**
	 * @param arg0
	 */
	public TestPerson(String arg0) {
		super(arg0);
	}

	protected void setUp() {
	}

	/**
	 * This method checks the creation of a <code>Person</code> object
	 */
	public void testCreatePerson() {
		// Create the new person
		Person person = new Person();

		// Set all the values
		person.setId(new Long(1));
		try {
			person.setFirstname("Kevin");
			person.setSurname("Gomes");
			person.setOrganization("MBARI");
			person.setEmail("kgomes@mbari.org");
			person.setPassword("dumbPassword");
			person.setStatus(Person.STATUS_ACTIVE);
			person.setUsername("kgomes");
		} catch (MetadataException e) {
			assertTrue("MetadataException caught trying to set values: "
					+ e.getMessage(), false);
		}

		// Now read all of them back
		assertEquals(person.getId(), new Long(1));
		assertEquals(person.getFirstname(), "Kevin");
		assertEquals(person.getSurname(), "Gomes");
		assertEquals(person.getOrganization(), "MBARI");
		assertEquals(person.getEmail(), "kgomes@mbari.org");
		assertEquals(person.getPassword(), "dumbPassword");
		assertEquals(person.getStatus(), Person.STATUS_ACTIVE);
		assertEquals(person.getUsername(), "kgomes");
	}

	/**
	 * This test checks to make sure that if the username is set using null or
	 * an empty string, a <code>MetadataAccessException</code> gets thrown
	 */
	public void testUsernameFail() {
		Person person = new Person();

		boolean exceptionThrown = false;

		try {
			person.setUsername(null);
		} catch (MetadataException e) {
			exceptionThrown = true;
		}

		// Check flag
		assertEquals("An exception was supposed to have been "
				+ "thrown because I tried to set a null username",
				exceptionThrown, true);

		// Reset flag
		exceptionThrown = false;

		try {
			person.setUsername("");
		} catch (MetadataException e) {
			exceptionThrown = true;
		}

		// Check flag
		assertEquals("An exception was supposed to have been "
				+ "thrown because I tried to set an empty username",
				exceptionThrown, true);
	}

	/**
	 * This method checks to see if the toStringRepresentation method works
	 * properly
	 */
	public void testToStringRepresentation() {
		// Create the new person
		Person person = new Person();

		// Set all the values
		person.setId(new Long(1));
		try {
			person.setFirstname("Kevin");
			person.setSurname("Gomes");
			person.setOrganization("MBARI");
			person.setEmail("kgomes@mbari.org");
			person.setPassword("dumbPassword");
			person.setStatus(Person.STATUS_ACTIVE);
			person.setUsername("kgomes");
		} catch (MetadataException e) {
			assertTrue("MetadataException caught trying to set values: "
					+ e.getMessage(), false);
		}

		// Check that the string representations are equal
		String stringPerson = person.toStringRepresentation(",");
		String stringRep = "Person," + "id=1," + "firstname=Kevin,"
				+ "surname=Gomes," + "organization=MBARI," + "username=kgomes,"
				+ "email=kgomes@mbari.org," + "phone=null," + "address1=null,"
				+ "address2=null," + "city=null," + "state=null,"
				+ "zipcode=null," + "status=" + Person.STATUS_ACTIVE;
		assertEquals(
				"The string represntation should match the set attributes",
				stringPerson, stringRep);

	}

	/**
	 * This tests the method that sets the values from a string representation
	 */
	public void testSetValuesFromStringRepresentation() {

		// Create the person
		Person person = new Person();

		// Create the string representation
		String stringRep = "Person," + "id=1," + "firstname=Kevin,"
				+ "surname=Gomes," + "organization=MBARI," + "username=kgomes,"
				+ "password=dumbPassword," + "email=kgomes@mbari.org,"
				+ "status=" + Person.STATUS_ACTIVE;

		try {
			person.setValuesFromStringRepresentation(stringRep, ",");
		} catch (MetadataException e) {
			logger.error("MetadataException caught trying to set "
					+ "values from string representation: " + e.getMessage());
		}

		// Now check that everything was set OK
		assertEquals(person.getId(), new Long(1));
		assertEquals(person.getFirstname(), "Kevin");
		assertEquals(person.getSurname(), "Gomes");
		assertEquals(person.getOrganization(), "MBARI");
		assertEquals(person.getEmail(), "kgomes@mbari.org");
		assertEquals(person.getPassword(), "dumbPassword");
		assertEquals(person.getStatus(), Person.STATUS_ACTIVE);
		assertEquals(person.getUsername(), "kgomes");
	}

	/**
	 * This method tests the equals method
	 */
	public void testEquals() {
		// Create the string representation
		String stringRep = "Person," + "id=1," + "firstname=Kevin,"
				+ "surname=Gomes," + "organization=MBARI," + "username=kgomes,"
				+ "password=dumbPassword," + "email=kgomes@mbari.org,"
				+ "status=" + Person.STATUS_ACTIVE;
		String stringRepTwo = "Person," + "id=1," + "firstname=Kevin,"
				+ "surname=Gomes," + "organization=MBARI," + "username=kgomes,"
				+ "password=dumbPassword," + "email=kgomes@mbari.org,"
				+ "status=" + Person.STATUS_ACTIVE;

		Person personOne = new Person();
		Person personTwo = new Person();

		try {
			personOne.setValuesFromStringRepresentation(stringRep, ",");
			personTwo.setValuesFromStringRepresentation(stringRepTwo, ",");
		} catch (MetadataException e) {
			logger
					.error("MetadataException caught trying to create two person objects");
		}

		assertTrue("The two persons should be equal (part one).", personOne
				.equals(personTwo));
		assertEquals("The two persons should be equal (part two).", personOne,
				personTwo);

		// Now change the ID of the second one and they should still be equal
		personTwo.setId(new Long(2));
		assertTrue("The two persons should be equal", personOne
				.equals(personTwo));

		// Now set the ID back, check equals again
		personTwo.setId(new Long(1));
		assertEquals("The two persons should be equal after ID set back.",
				personOne, personTwo);

		// Now set the username and they should be different
		try {
			personTwo.setUsername("jdoe");
		} catch (MetadataException e) {
			logger
					.error("MetadataException caught trying to set the username: "
							+ e.getMessage());
		}
		assertTrue("The two persons should not be equal", !personOne
				.equals(personTwo));

		// Now set it back and change all the non-business key values. The
		// results should be equals
		try {
			personTwo.setUsername("kgomes");
		} catch (MetadataException e) {
			logger
					.error("MetadataException caught trying to set the username: "
							+ e.getMessage());
		}
		try {
			personTwo.setFirstname("John");
			personTwo.setSurname("Gomes");
			personTwo.setOrganization("SELF");
			personTwo.setPassword("pass2");
		} catch (MetadataException e) {
			assertTrue("MetadataException caught trying to set values: "
					+ e.getMessage(), false);
		}
		assertEquals("The two persons should be equal after ID set back.",
				personOne, personTwo);
	}

	/**
	 * This method tests the hashCode method
	 */
	public void testHashCode() {
		// Create the string representation
		String stringRep = "Person," + "id=1," + "firstname=Kevin,"
				+ "surname=Gomes," + "organization=MBARI," + "username=kgomes,"
				+ "password=dumbPassword," + "email=kgomes@mbari.org,"
				+ "status=" + Person.STATUS_ACTIVE;
		String stringRepTwo = "Person," + "id=1," + "firstname=Kevin,"
				+ "surname=Gomes," + "organization=MBARI," + "username=kgomes,"
				+ "password=dumbPassword," + "email=kgomes@mbari.org,"
				+ "status=" + Person.STATUS_ACTIVE;

		Person personOne = new Person();
		Person personTwo = new Person();

		try {
			personOne.setValuesFromStringRepresentation(stringRep, ",");
			personTwo.setValuesFromStringRepresentation(stringRepTwo, ",");
		} catch (MetadataException e) {
			logger
					.error("MetadataException caught trying to create two person objects: "
							+ e.getMessage());
		}

		assertTrue("The two hashCodes should be equal (part one).", personOne
				.hashCode() == personTwo.hashCode());
		assertEquals("The two hashCodes should be equal (part two).", personOne
				.hashCode(), personTwo.hashCode());

		// Now change the ID of the second one and they should still be equal
		personTwo.setId(new Long(2));
		assertTrue("The two hashCodes should be equal",
				personOne.hashCode() == personTwo.hashCode());

		// Now set the ID back, check equals again
		personTwo.setId(new Long(1));
		assertEquals("The two hashCodes should be equal after ID set back.",
				personOne.hashCode(), personTwo.hashCode());

		// Now set the username and they should be different
		try {
			personTwo.setUsername("jdoe");
		} catch (MetadataException e) {
			logger
					.error("MetadataException caught trying to set the username: "
							+ e.getMessage());
		}
		assertTrue(
				"The two hashCodes should not be equal after username change",
				personOne.hashCode() != personTwo.hashCode());

		// Now set it back and change all the non-business key values. The
		// results should be equals
		try {
			personTwo.setUsername("kgomes");
		} catch (MetadataException e) {
			logger
					.error("MetadataException caught trying to set the username: "
							+ e.getMessage());
		}
		try {
			personTwo.setFirstname("John");
			personTwo.setSurname("Gomes");
			personTwo.setOrganization("SELF");
			personTwo.setPassword("pass2");
		} catch (MetadataException e) {
			assertTrue("MetadataException caught trying to set values: "
					+ e.getMessage(), false);
		}
		assertEquals(
				"The two hashCodes should be equal after ID and username same"
						+ ", but different business keys.", personOne
						.hashCode(), personTwo.hashCode());
	}

	public void testPersonXMLBinding() {
		// Grab the file that has the XML in it
		File personXMLFile = new File("src" + File.separator + "resources"
				+ File.separator + "test" + File.separator + "xml"
				+ File.separator + "Person.xml");
		if (!personXMLFile.exists())
			assertTrue("Could not find Person.xml file for testing.", false);
		logger.debug("Will read person XML from "
				+ personXMLFile.getAbsolutePath());

		// Create a file reader
		FileReader personXMLFileReader = null;
		try {
			personXMLFileReader = new FileReader(personXMLFile);
		} catch (FileNotFoundException e2) {
			assertTrue("Error in creating file reader for person XML file: "
					+ e2.getMessage(), false);
		}

		// Grab the binding factory for Persons
		IBindingFactory bfact = null;
		try {
			bfact = BindingDirectory.getFactory(Metadata.class);
		} catch (JiBXException e1) {
			assertTrue("Error in getting Binding Factory for Metadata: "
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
			Person testPerson = null;
			try {

				metadata = (Metadata) uctx.unmarshalDocument(
						personXMLFileReader, null);
				testPerson = metadata.getPersons().iterator().next();
				logger.debug("TesPerson after unmarshalling: "
						+ testPerson.toStringRepresentation("|"));
				if (testPerson.getUserGroups() != null
						&& testPerson.getUserGroups().size() > 0) {
					for (Iterator<UserGroup> iterator = testPerson
							.getUserGroups().iterator(); iterator.hasNext();) {
						UserGroup userGroup = iterator.next();
						logger.debug("User group: "
								+ userGroup.toStringRepresentation("|"));
					}
				}
			} catch (JiBXException e1) {
				assertTrue("Error in unmarshalling Person: " + e1.getMessage(),
						false);
			}

			if (testPerson != null) {
				assertEquals("Person id should match", testPerson.getId()
						.longValue(), Long.parseLong("10"));
				assertEquals("Person first name should match", testPerson
						.getFirstname(), "Test Firstname");
				assertEquals("Person surname should match", testPerson
						.getSurname(), "Test Surname");
				assertEquals("Person organization should match", testPerson
						.getOrganization(), "Test Organization");
				assertEquals("Person username should match", testPerson
						.getUsername(), "testUsername@org.com");
				assertEquals("Person password should match", testPerson
						.getPassword(), "Test password");
				assertEquals("Person email should match",
						testPerson.getEmail(), "test.email@org.com");
				assertEquals("Person phone match", testPerson.getPhone(),
						"123-456-7890");
				assertEquals("Person address1 should match", testPerson
						.getAddress1(), "Test Address 1");
				assertEquals("Person address2 should match", testPerson
						.getAddress2(), "Test Address 2");
				assertEquals("Person city should match", testPerson.getCity(),
						"Test City");
				assertEquals("Person state should match",
						testPerson.getState(), "TS");
				assertEquals("Person zipcode should match", testPerson
						.getZipcode(), "12345");
				assertEquals("Person status should match", testPerson
						.getStatus(), "active");

				// Now iterate over the UserGroups
				for (Iterator<UserGroup> iterator = testPerson.getUserGroups()
						.iterator(); iterator.hasNext();) {
					UserGroup userGroup = iterator.next();
					// Make sure it has an expected name
					assertTrue("UserGroup name is on of the expected ones",
							(userGroup.getGroupName()
									.equals("Test UserGroup 1")
									|| userGroup.getGroupName().equals(
											"Test UserGroup 2")
									|| userGroup.getGroupName().equals(
											"Test UserGroup 3") || userGroup
									.getGroupName().equals("Test UserGroup 4")));
					// And ID
					assertTrue("UserGroup ID is on of the expected ones",
							(userGroup.getId().equals(Long.parseLong("1"))
									|| userGroup.getId().equals(
											Long.parseLong("2"))
									|| userGroup.getId().equals(
											Long.parseLong("3")) || userGroup
									.getId().equals(Long.parseLong("4"))));
				}

				// Now let's change the attributes
				try {
					testPerson.setId(new Long("199"));
					testPerson.setFirstname("NewFirstname");
					testPerson.setSurname("NewSurname");
					testPerson.setOrganization("NewOrganization");
					testPerson.setUsername("NewUsername");
					testPerson.setPassword("NewPassword");
					testPerson.setEmail("NewEmail");
					testPerson.setPhone("NewPhone");
					testPerson.setAddress1("NewAddress1");
					testPerson.setAddress2("NewAddress2");
					testPerson.setCity("NewCity");
					testPerson.setState("NewState");
					testPerson.setZipcode("NewZipcode");
					testPerson.setStatus("inactive");
					for (Iterator<UserGroup> iterator = testPerson
							.getUserGroups().iterator(); iterator.hasNext();) {
						UserGroup userGroup = iterator.next();
						if (userGroup.getGroupName().contains("1"))
							userGroup.setGroupName("NewUserGroupName1");
						if (userGroup.getGroupName().contains("2"))
							userGroup.setGroupName("NewUserGroupName2");
						if (userGroup.getGroupName().contains("3"))
							userGroup.setGroupName("NewUserGroupName3");
						if (userGroup.getGroupName().contains("4"))
							userGroup.setGroupName("NewUserGroupName4");
					}
				} catch (NumberFormatException e1) {
					assertTrue("NumberFormatException in updating Person: "
							+ e1.getMessage(), false);
				} catch (MetadataException e1) {
					assertTrue("MetadataException in updating Person: "
							+ e1.getMessage(), false);
				}
				logger.debug("Changed person attributes "
						+ "and will marshall to XML");

				// Create a string writer
				StringWriter personStringWriter = new StringWriter();

				// Marshall out to XML
				IMarshallingContext mctx = null;
				try {
					mctx = bfact.createMarshallingContext();
				} catch (JiBXException e) {
					assertTrue("Error while creating marshalling context: "
							+ e.getMessage(), false);
				}

				if (mctx != null) {
					mctx.setIndent(2);
					try {
						mctx.marshalDocument(testPerson, "UTF-8", null,
								personStringWriter);
					} catch (JiBXException e) {
						assertTrue("Error while marshalling testPerson "
								+ "after attribute changes: " + e.getMessage(),
								false);
					}

					// Now make sure the resulting string contains all the
					// updates I did
					logger.debug("Marshalled XML after change: "
							+ personStringWriter.toString());
					assertTrue("ID was updated", personStringWriter.toString()
							.contains("id=\"199\""));
					assertTrue("Firstname was updated", personStringWriter
							.toString().contains("NewFirstname"));
					assertTrue("Surname was updated", personStringWriter
							.toString().contains("NewSurname"));
					assertTrue("Organization was updated", personStringWriter
							.toString().contains("NewOrganization"));
					assertTrue("Username was updated", personStringWriter
							.toString().contains("NewUsername"));
					assertTrue("Password was updated", personStringWriter
							.toString().contains("NewPassword"));
					assertTrue("Email was updated", personStringWriter
							.toString().contains("NewEmail"));
					assertTrue("Phone was updated", personStringWriter
							.toString().contains("NewPhone"));
					assertTrue("Address1 was updated", personStringWriter
							.toString().contains("NewAddress1"));
					assertTrue("Address2 was updated", personStringWriter
							.toString().contains("NewAddress2"));
					assertTrue("City was updated", personStringWriter
							.toString().contains("NewCity"));
					assertTrue("State was updated", personStringWriter
							.toString().contains("NewState"));
					assertTrue("Zipcode was updated", personStringWriter
							.toString().contains("NewZipcode"));
					assertTrue("Status was updated", personStringWriter
							.toString().contains("inactive"));
					assertTrue("UserGroup1 was updated", personStringWriter
							.toString().contains("NewUserGroupName1"));
					assertTrue("UserGroup2 was updated", personStringWriter
							.toString().contains("NewUserGroupName2"));
					assertTrue("UserGroup3 was updated", personStringWriter
							.toString().contains("NewUserGroupName3"));
					assertTrue("UserGroup4 was updated", personStringWriter
							.toString().contains("NewUserGroupName4"));
				}

			} else {
				assertTrue("testKeyword came back null!", false);
			}
		}

	}

}