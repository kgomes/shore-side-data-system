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
package test.moos.ssds.services.metadata;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;

import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.DataContainer;
import moos.ssds.metadata.RecordVariable;
import moos.ssds.metadata.StandardVariable;

import org.apache.log4j.Logger;

/**
 * -Enter Description-
 * <hr>
 * 
 * @author : $Author: mccann $
 * @version : $Revision: 1.1.2.2 $
 */

public class TestRecordVariableAccess extends TestAccessCase {

	/**
	 * A constructor
	 * 
	 * @param name
	 */
	public TestRecordVariableAccess(String name) {
		super(name);
	}

	/**
	 * Sets up the fixture, for example, open a network connection. This method
	 * is called before a test is executed.
	 */
	protected void setUp() {

		// Setup the super class
		super.setUp();

	}

	/**
	 * Standard test that is similar to TestDataContainer's test
	 */
	public void testOne() {

		logger.debug("DATA_CONTAINER_ONE one is "
				+ DATA_CONTAINER_ONE.toStringRepresentation("|"));
		this.recordVariableTest(DATA_CONTAINER_ONE);
		logger.debug("Done with testOne");

	}

	/**
	 * Test setting and updating StandardVariable
	 */
	public void testSetStandardVariable() {

		logger.debug("DATA_CONTAINER_ONE one is "
				+ DATA_CONTAINER_ONE.toStringRepresentation("|"));
		this.setAndUpdateStandardVariable(DATA_CONTAINER_ONE);
		logger.debug("Done with testSetStandardVariable");

	}

	/**
	 * Test setting and updating the StandardVariable for a RecordVariable
	 * 
	 * @param standardVariable
	 * @param dataContainer
	 */
	private void setAndUpdateStandardVariable(DataContainer dataContainer) {

		/*
		 * Insert a DataContainer to get some test RecordVaraibles in the
		 * database
		 */
		Long dataContainerId = null;
		dataContainerId = testInsert(dataContainer, DATA_CONTAINER_ACCESS);

		// Now query back by ID and make sure all attributes are equal
		DataContainer persistedDataContainer = null;

		try {
			persistedDataContainer = (DataContainer) DATA_CONTAINER_ACCESS
					.findById(dataContainerId, true);
		} catch (MetadataAccessException e1) {
			logger.error("MetadataAccessException caught during findById: "
					+ e1.getMessage());
		}

		// Now check that they are equal
		assertEquals("The two dataContainers should be considered equal",
				dataContainer, persistedDataContainer);

		// Check all the getter methods are equal
		testEqualityOfAllGetters(dataContainer, persistedDataContainer);

		/*
		 * Insert our test StandardVariables and test their insert
		 */
		Long standardVariableID = null;
		standardVariableID = testInsert(STANDARD_VARIABLE_ONE,
				STANDARD_VARIABLE_ACCESS);

		StandardVariable persistedStandardVariable = null;

		try {
			persistedStandardVariable = (StandardVariable) STANDARD_VARIABLE_ACCESS
					.findById(standardVariableID, false);
		} catch (MetadataAccessException e1) {
			logger.error("MetadataAccessException caught during findById: "
					+ e1.getMessage());
		}

		// Now check that they are equal
		assertEquals("The two standardVariables should be considered equal",
				STANDARD_VARIABLE_ONE, persistedStandardVariable);

		// Check all the getter methods are equal
		testEqualityOfAllGetters(STANDARD_VARIABLE_ONE,
				persistedStandardVariable);

		/*
		 * Set and update a StandardVariable on a RecordVariable
		 */
		HashMap variablesToUpdate = new HashMap();
		Object[] variable1 = new Object[1];
		variable1[0] = STANDARD_VARIABLE_ONE;
		variablesToUpdate.put("StandardVariable", variable1);

		// Set and update a StandardVariable (grab just one)
		RecordVariable recordVariable = null;
		Iterator it = persistedDataContainer.getRecordVariables().iterator();
		while (it.hasNext()) {
			recordVariable = (RecordVariable) it.next();
			break;
		}
		recordVariable.setStandardVariable(persistedStandardVariable);

		// Test the changes
		testUpdate(recordVariable, variablesToUpdate, RECORD_VARIABLE_ACCESS);

		// Retreive the updated RecordVariable from the database
		RecordVariable persistedRecordVariable = null;
		try {
			persistedRecordVariable = (RecordVariable) RECORD_VARIABLE_ACCESS
					.findById(recordVariable.getId(), false);
		} catch (MetadataAccessException e1) {
			logger.error("MetadataAccessException caught during findById: "
					+ e1.getMessage());
		}
		// Check all the getter methods are equal (tests that standardVariable
		// was set)
		testEqualityOfAllGetters(recordVariable, persistedRecordVariable);

		// Remove the DataContainer (along with RVs) and StandardVariable
		testDelete(persistedDataContainer, DATA_CONTAINER_ACCESS);
		testDelete(persistedStandardVariable, STANDARD_VARIABLE_ACCESS);

	}

	/**
	 * Standard CRUD test on local attributes
	 * 
	 * @param dataContainer
	 */
	private void recordVariableTest(DataContainer dataContainer) {

		// Need to insert a DataContainer to get some test RecordVaraibles in
		// the database
		Long dataContainerId = null;
		dataContainerId = testInsert(dataContainer, DATA_CONTAINER_ACCESS);

		// Now query back by ID and make sure all attributes are equal
		DataContainer persistedDataContainer = null;

		try {
			persistedDataContainer = (DataContainer) DATA_CONTAINER_ACCESS
					.findById(dataContainerId, false);
		} catch (MetadataAccessException e1) {
			logger.error("MetadataAccessException caught during findById: "
					+ e1.getMessage());
		}

		// Now check that they are equal
		assertEquals("The two dataContainers should be considered equal",
				dataContainer, persistedDataContainer);

		// Check all the getter methods are equal
		testEqualityOfAllGetters(dataContainer, persistedDataContainer);

		// Create a map with the values to update
		HashMap variablesToUpdate = new HashMap();

		// Change some attributes
		Object[] variable1 = new Object[1];
		variable1[0] = new String("Updated Description");
		variablesToUpdate.put("Description", variable1);

		Object[] variable2 = new Object[1];
		variable2[0] = new String("Updated Name");
		variablesToUpdate.put("Name", variable2);

		// Test the changes
		testUpdate(persistedDataContainer, variablesToUpdate,
				DATA_CONTAINER_ACCESS);

		// Remove the DataContainer
		testDelete(persistedDataContainer, DATA_CONTAINER_ACCESS);
	}

	/**
	 * A log4J logger
	 */
	static Logger logger = Logger.getLogger(TestRecordVariableAccess.class);
}
