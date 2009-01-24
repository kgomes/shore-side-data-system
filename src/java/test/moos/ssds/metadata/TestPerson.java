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

import junit.framework.TestCase;
import moos.ssds.metadata.Person;
import moos.ssds.metadata.util.MetadataException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 * This is the test class to test the Person class
 * 
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.4 $
 */
public class TestPerson extends TestCase {

    /**
     * @param arg0
     */
    public TestPerson(String arg0) {
        super(arg0);
    }

    protected void setUp() {
        BasicConfigurator.configure();
        logger.setLevel(Level.DEBUG);
        logger.addAppender(new ConsoleAppender(new PatternLayout(
            "%d %-5p [%c %M %L] %m%n")));
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
            + "thrown because I tried to set a null username", exceptionThrown,
            true);

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
            + "address2=null," + "city=null," + "state=null," + "zipcode=null,"
            + "status=" + Person.STATUS_ACTIVE;
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
                + ", but different business keys.", personOne.hashCode(),
            personTwo.hashCode());
    }

    /**
     * The logger for dumping information to
     */
    static Logger logger = Logger.getLogger(TestPerson.class);
}