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
package moos.ssds.wrapper.ogc.sensorml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;

import moos.ssds.data.parsers.PacketParserContext;
import moos.ssds.data.parsers.Parser;
import moos.ssds.metadata.DataContainer;
import moos.ssds.metadata.DataProducer;
import moos.ssds.metadata.Device;
import moos.ssds.metadata.IMetadataObject;
import moos.ssds.metadata.RecordVariable;
import moos.ssds.transmogrify.SSDSDevicePacket;
import moos.ssds.util.XmlDateFormat;
import net.opengis.gml.v_3_1_1.StringOrRefType;
import net.opengis.sensorml.v_1_0_1.AbstractProcessType;
import net.opengis.sensorml.v_1_0_1.Identification;
import net.opengis.sensorml.v_1_0_1.IoComponentPropertyType;
import net.opengis.sensorml.v_1_0_1.ObjectFactory;
import net.opengis.sensorml.v_1_0_1.Outputs;
import net.opengis.sensorml.v_1_0_1.SensorML;
import net.opengis.sensorml.v_1_0_1.SystemType;
import net.opengis.sensorml.v_1_0_1.Term;
import net.opengis.sensorml.v_1_0_1.Identification.IdentifierList;
import net.opengis.sensorml.v_1_0_1.Identification.IdentifierList.Identifier;
import net.opengis.sensorml.v_1_0_1.Outputs.OutputList;
import net.opengis.sensorml.v_1_0_1.SensorML.Member;
import net.opengis.swe.v_1_0_1.DataComponentPropertyType;
import net.opengis.swe.v_1_0_1.DataRecordType;
import net.opengis.swe.v_1_0_1.Quantity;
import net.opengis.swe.v_1_0_1.UomPropertyType;

public class SensorMLFactory {
	/**
	 * This method takes in an <code>IMetadataObject</code> and converts it to
	 * an equivalent representation in SensorML (using the ogc.dev.java.net
	 * classes). For the SSDS system, we need to map concepts of the SSDS Model
	 * to the SensorML model.
	 * 
	 * @param metadataObject
	 * @return <code>null</code> if the incoming <code>IMetadataObject</code>
	 *         has no equivalent top level concept in SensorML.
	 */
	public static SensorML createSensorMLFromMetadataObject(
			final IMetadataObject metadataObject, byte[] dataRecord) {

		// The SensorML object to return
		SensorML sensorMLObjectToReturn = null;

		// The top level really must be a DataProducer, otherwise there is no
		// equivalent top level concept for the other model objects in SSDS
		if (metadataObject instanceof DataProducer) {
			// Create a SensorML Factory object to use throughout
			net.opengis.sensorml.v_1_0_1.ObjectFactory sensorMLObjectFactory = new net.opengis.sensorml.v_1_0_1.ObjectFactory();

			// Create the top level SensorML
			sensorMLObjectToReturn = sensorMLObjectFactory.createSensorML();

			// Now add the data producer to the list of members
			addDataProducerToSensorMLMemberList((DataProducer) metadataObject,
					sensorMLObjectToReturn.getMember(), sensorMLObjectFactory,
					dataRecord);
		}

		// Return it
		return sensorMLObjectToReturn;
	}

	/**
	 * 
	 * @param dataProducerToConvert
	 * @return
	 */
	public static void addDataProducerToSensorMLMemberList(
			DataProducer dataProducerToConvert,
			List<Member> dataProducerMembers,
			ObjectFactory sensorMLObjectFactory, byte[] dataRecord) {

		// An XML Date Formatter
		XmlDateFormat xmlDateFormatter = new XmlDateFormat();

		// Make sure the incoming DataProducer is not empty
		if (dataProducerToConvert != null) {

			if (dataProducerToConvert.getDataProducerType() != null
					&& !dataProducerToConvert.getDataProducerType().equals("")) {
				// Create a new member
				Member dataProducerMember = sensorMLObjectFactory
						.createSensorMLMember();

				// Split the logic based on the type of DataProducer
				if (dataProducerToConvert.getDataProducerType().equals(
						DataProducer.TYPE_DEPLOYMENT)) {
					// This means we will be creating a SystemType
					SystemType dataProducerSystemType = sensorMLObjectFactory
							.createSystemType();

					// The first thing to do is construct an ID for the System.
					// It should be constructed from a combination of
					// information about the DataProducer. In order to make it
					// unique, it should be a URN.
					StringBuilder sb = new StringBuilder();
					sb.append("urn" + SSDS_URN_PART + ":deployment");

					// If the DataProducer has an ID, use that
					if (dataProducerToConvert.getId() != null
							&& dataProducerToConvert.getId().longValue() > 0) {
						sb.append(":id:" + dataProducerToConvert.getId());
					} else {
						// If not, there must be some other way to identify it.
						// The alternate primary key is based on the outputs,
						// but that is awefully complex. If it is a deployment,
						// try to use the start date
						if (dataProducerToConvert.getStartDate() != null) {
							sb.append(":startDate:"
									+ xmlDateFormatter
											.format(dataProducerToConvert
													.getStartDate()));
						}
						// If there is a device, use that as an identifier
						if (dataProducerToConvert.getDevice() != null) {
							// If the UUID is not null, use that, else use the
							// ID
							if (dataProducerToConvert.getDevice().getUuid() != null
									&& !dataProducerToConvert.getDevice()
											.getUuid().equals("")) {
								sb.append(":device:uuid:"
										+ dataProducerToConvert.getDevice()
												.getUuid());
							} else if (dataProducerToConvert.getDevice()
									.getId() != null
									&& dataProducerToConvert.getDevice()
											.getId().longValue() > 0) {
								sb.append(":device:id:"
										+ dataProducerToConvert.getDevice()
												.getId().toString());
							}
						}
					}

					// Now record it as the ID
					dataProducerSystemType.setId(sb.toString());

					// If there is a description use it as the system
					// description
					if (dataProducerToConvert.getDescription() != null
							&& !dataProducerToConvert.getDescription().equals(
									"")) {
						StringOrRefType description = new StringOrRefType();
						description.setValue(dataProducerToConvert
								.getDescription());
						dataProducerSystemType.setDescription(description);
					}

					// Now if there is a Device associated with it, add the
					// identifiers to the list of identifiers
					if (dataProducerToConvert.getDevice() != null)
						addDeviceToSensorMLIdentificationList(
								dataProducerToConvert.getDevice(),
								dataProducerSystemType.getIdentification(),
								sensorMLObjectFactory);

					// Add a System as the process to the member
					dataProducerMember.setProcess(sensorMLObjectFactory
							.createSystem(dataProducerSystemType));

				} else if (dataProducerToConvert.getDataProducerType().equals(
						DataProducer.TYPE_PROCESS_RUN)) {
					// This means we will be creating a ProcessModelType
				}

				// Now do the stuff that is common to both Deployments and
				// ProcessRuns

				// Let's take the DataContainer that are the Outputs
				if (dataProducerToConvert.getOutputs() != null
						&& dataProducerToConvert.getOutputs().size() > 0) {
					// Create an ObjectFactory for SWE stuff
					net.opengis.swe.v_1_0_1.ObjectFactory sweObjectFactory = new net.opengis.swe.v_1_0_1.ObjectFactory();
					net.opengis.gml.v_3_1_1.ObjectFactory gmlObjectFactory = new net.opengis.gml.v_3_1_1.ObjectFactory();

					// Create a new Outputs for the DataContainers
					Outputs dataContainerOutputs = sensorMLObjectFactory
							.createOutputs();

					// Iterate over the DC's
					for (Iterator dcIter = dataProducerToConvert.getOutputs()
							.iterator(); dcIter.hasNext();) {
						// For each DataContainer there is one-to-one mapping
						// with Outputs
						DataContainer dc = (DataContainer) dcIter.next();

						// If the data record is not null, create a parser for
						// the data record
						Map<RecordVariable, Number> parsedData = null;
						if (dc.getRecordDescription().isParseable()
								.booleanValue()
								&& dataRecord != null && dataRecord.length > 0) {
							// Create a collection of SSDSDevicePackets
							Collection<SSDSDevicePacket> devicePacketCollection = new ArrayList<SSDSDevicePacket>();
							SSDSDevicePacket ssdsDevicePacket = new SSDSDevicePacket(
									dataProducerToConvert.getDevice().getId()
											.longValue(), dataRecord.length);
							ssdsDevicePacket.setDataBuffer(dataRecord);
							devicePacketCollection.add(ssdsDevicePacket);
							PacketParserContext ppc = new PacketParserContext(
									dc, devicePacketCollection);
							// Now try to parse the data
							parsedData = (Map) ppc.next();
						}
						// Create a component property type that will map to the
						// DataContainer
						IoComponentPropertyType ioComponentPropertyType = sensorMLObjectFactory
								.createIoComponentPropertyType();
						ioComponentPropertyType.setName(dc.getName());
						ioComponentPropertyType.setHref(dc.getUriString());

						// Add it to the outputs for the DataContainer
						OutputList dataContainerOutputList = dataContainerOutputs
								.getOutputList();
						if (dataContainerOutputList == null) {
							dataContainerOutputList = sensorMLObjectFactory
									.createOutputsOutputList();
							dataContainerOutputs
									.setOutputList(dataContainerOutputList);
						}

						// Grab the list of outputs
						List<IoComponentPropertyType> outputs = dataContainerOutputList
								.getOutput();
						outputs.add(ioComponentPropertyType);

						// Create a DataRecord to describe the data provided in
						// the DataContainer
						DataRecordType dataRecordType = sweObjectFactory
								.createDataRecordType();

						// Add it to the IOComponentPropertyType
						ioComponentPropertyType
								.setAbstractDataRecord(sweObjectFactory
										.createDataRecord(dataRecordType));

						// To construct an ID for the DataRecord, it will be
						// combination of the DataContainer URI/ID and the
						// RecordType from the RecordDescription
						StringBuilder drIDBuilder = new StringBuilder();
						if (dc.getUriString() != null
								&& !dc.getUriString().equals("")) {
							drIDBuilder.append(dc.getUriString());
						} else {
							drIDBuilder.append("urn" + SSDS_URN_PART
									+ ":dataContainer");
							if (dc.getId() != null)
								drIDBuilder.append(dc.getId().toString());
						}
						// Now check for the recordType
						if (dc.getRecordDescription() != null) {
							if (dc.getRecordDescription().getRecordType() != null)
								drIDBuilder.append(":"
										+ dc.getRecordDescription()
												.getRecordType().toString());
						}

						// Construct the ID from the DataContainer ID and
						// the RecordVariable's RecordType
						dataRecordType.setId(drIDBuilder.toString());

						// Fill in information from the RecordDescription
						// TODO kgomes - find out where to map RD information
						// if (dc.getRecordDescription() != null) {
						// // Get the list of metadata properties
						// List<MetaDataPropertyType> metadataPropertyList =
						// dataRecordType
						// .getMetaDataProperty();
						//							
						// MetaDataPropertyType mdpt =
						// gmlObjectFactory.createMetaDataPropertyType();
						// dc.getRecordDescription().getBufferItemSeparator();
						// dc.getRecordDescription().getBufferLengthType();
						// dc.getRecordDescription().getBufferParseType();
						// dc.getRecordDescription().getBufferStyle();
						// dc.getRecordDescription().getEndian();
						// dc.getRecordDescription().getRecordParseRegExp();
						// dc.getRecordDescription().getRecordTerminator();
						// dc.getRecordDescription().getRecordType();
						// }

						// Grab the list of fields
						List<DataComponentPropertyType> fields = dataRecordType
								.getField();

						// Now loop over the recordvariables
						if (dc.getRecordVariables() != null) {
							for (Iterator rvIter = dc.getRecordVariables()
									.iterator(); rvIter.hasNext();) {
								RecordVariable rv = (RecordVariable) rvIter
										.next();

								// Create a DataComponentPropertyType for the
								// variable
								DataComponentPropertyType rvDataComponentType = sweObjectFactory
										.createDataComponentPropertyType();

								// Set the field name
								rvDataComponentType.setName(rv.getName());

								// Grab the Quantity
								Quantity rvQuantity = rvDataComponentType
										.getQuantity();
								if (rvQuantity == null) {
									rvQuantity = sweObjectFactory
											.createQuantity();
									rvDataComponentType.setQuantity(rvQuantity);
								}
								// Set stuff
								StringOrRefType rvDescription = new StringOrRefType();
								rvDescription.setValue(rv.getDescription());
								rvQuantity.setDescription(rvDescription);
								rvQuantity.setDefinition("urn" + SSDS_URN_PART
										+ ":rv:" + rv.getName());
								if (rv.getUnits() != null
										&& !rv.getUnits().equals("")) {
									// Grab the UOM type
									UomPropertyType uom = rvQuantity.getUom();
									if (uom == null) {
										uom = sweObjectFactory
												.createUomPropertyType();
										rvQuantity.setUom(uom);
									}
									uom.setCode(rv.getUnits());
								}
								// If a data record was passed in, put it in too
								if (parsedData != null) {
									// Grab the number
									Number value = parsedData.get(rv);
									rvQuantity.setValue(value.doubleValue());
								}
								// Add it to the fields
								fields.add(rvDataComponentType);
							}

						}
					}

					// Grab the process from the DataProducerMember
					JAXBElement<? extends AbstractProcessType> processType = dataProducerMember
							.getProcess();

					// Cast it to a system type and add the outputs
					SystemType systemType = (SystemType) processType.getValue();
					systemType.setOutputs(dataContainerOutputs);
				}

				// Add the member to the list
				dataProducerMembers.add(dataProducerMember);
			}
		}
	}

	/**
	 * 
	 * @param deviceToConvert
	 * @return
	 */
	public static void addDeviceToSensorMLIdentificationList(
			Device deviceToConvert, List<Identification> identificationList,
			ObjectFactory sensorMLObjectFactory) {

		// Make sure the incoming object is something
		if (deviceToConvert != null && identificationList != null
				&& sensorMLObjectFactory != null) {

			// Create an Identification
			Identification identification = sensorMLObjectFactory
					.createIdentification();

			// Grab the IdentifierList
			IdentifierList identifierList = identification.getIdentifierList();
			if (identifierList == null) {
				identifierList = sensorMLObjectFactory
						.createIdentificationIdentifierList();
				identification.setIdentifierList(identifierList);
			}

			// Now for each property of the device, create an appropriate
			// identifiers (if existing)

			// The ID of the device
			if (deviceToConvert.getId() != null
					&& deviceToConvert.getId().longValue() > 0) {
				// Create the identifier
				Identifier idIdentifier = sensorMLObjectFactory
						.createIdentificationIdentifierListIdentifier();
				// Set the name
				idIdentifier.setName("Device ID");

				// Try to grab the term
				Term idTerm = sensorMLObjectFactory.createTerm();
				idIdentifier.setTerm(idTerm);

				// Configure the Term
				idIdentifier.getTerm().setDefinition(
						"urn" + SSDS_URN_PART + ":deviceId");
				idIdentifier.getTerm().setValue(
						deviceToConvert.getId().toString());

				// Add it to the list
				identifierList.getIdentifier().add(idIdentifier);
			}

			// Now the UUID
			if (deviceToConvert.getUuid() != null
					&& !deviceToConvert.getUuid().equals("")) {
				// Create the identifier
				Identifier uuidIdentifier = sensorMLObjectFactory
						.createIdentificationIdentifierListIdentifier();
				// Set the name
				uuidIdentifier.setName("UUID");

				// Create a term
				Term uuidTerm = sensorMLObjectFactory.createTerm();
				uuidIdentifier.setTerm(uuidTerm);
				uuidTerm.setDefinition(URN_OGC_IDENTIFIER_PART + ":uuid");
				uuidTerm.setValue(deviceToConvert.getUuid());

				// Add it to the list
				identifierList.getIdentifier().add(uuidIdentifier);
			}

			// Description
			if (deviceToConvert.getDescription() != null
					&& !deviceToConvert.getDescription().equals("")) {
				// Create the identifier
				Identifier descriptionIdentifier = sensorMLObjectFactory
						.createIdentificationIdentifierListIdentifier();
				// Set the name
				descriptionIdentifier.setName("Description");

				// Create a term
				Term descriptionTerm = sensorMLObjectFactory.createTerm();
				descriptionIdentifier.setTerm(descriptionTerm);
				descriptionTerm.setDefinition(URN_OGC_IDENTIFIER_PART
						+ ":description");
				descriptionTerm.setValue(deviceToConvert.getDescription());

				// Add it to the list
				identifierList.getIdentifier().add(descriptionIdentifier);
			}

			// Mfg Name
			if (deviceToConvert.getMfgName() != null
					&& !deviceToConvert.getMfgName().equals("")) {
				// Create the identifier
				Identifier mfgNameIdentifier = sensorMLObjectFactory
						.createIdentificationIdentifierListIdentifier();
				// Set the name
				mfgNameIdentifier.setName("Manufacturer Name");

				// Create a term
				Term mfgNameTerm = sensorMLObjectFactory.createTerm();
				mfgNameIdentifier.setTerm(mfgNameTerm);
				mfgNameTerm.setDefinition(URN_OGC_IDENTIFIER_PART
						+ ":manufacturerName");
				mfgNameTerm.setValue(deviceToConvert.getMfgName());

				// Add it to the list
				identifierList.getIdentifier().add(mfgNameIdentifier);
			}

			// Mfg Model
			if (deviceToConvert.getMfgModel() != null
					&& !deviceToConvert.getMfgModel().equals("")) {
				// Create the identifier
				Identifier mfgModelIdentifier = sensorMLObjectFactory
						.createIdentificationIdentifierListIdentifier();
				// Set the name
				mfgModelIdentifier.setName("Model Number");

				// Create a term
				Term mfgModelTerm = sensorMLObjectFactory.createTerm();
				mfgModelIdentifier.setTerm(mfgModelTerm);
				mfgModelTerm.setDefinition("urn" + SSDS_URN_PART
						+ ":modelNumber");
				mfgModelTerm.setValue(deviceToConvert.getMfgModel());

				// Add it to the list
				identifierList.getIdentifier().add(mfgModelIdentifier);
			}

			// Mfg Serial Number
			if (deviceToConvert.getMfgSerialNumber() != null
					&& !deviceToConvert.getMfgSerialNumber().equals("")) {
				// Create the identifier
				Identifier mfgSerialNumberIdentifier = sensorMLObjectFactory
						.createIdentificationIdentifierListIdentifier();
				// Set the name
				mfgSerialNumberIdentifier.setName("Serial Number");

				// Create a term
				Term mfgSerialNumberTerm = sensorMLObjectFactory.createTerm();
				mfgSerialNumberIdentifier.setTerm(mfgSerialNumberTerm);
				mfgSerialNumberTerm.setDefinition("urn" + SSDS_URN_PART
						+ ":serialNumber");
				mfgSerialNumberTerm.setValue(deviceToConvert
						.getMfgSerialNumber());

				// Add it to the list
				identifierList.getIdentifier().add(mfgSerialNumberIdentifier);
			}

			// InfoUrlList
			if (deviceToConvert.getInfoUrlList() != null
					&& !deviceToConvert.getInfoUrlList().equals("")) {
				// Create the identifier
				Identifier inforUrlListIdentifier = sensorMLObjectFactory
						.createIdentificationIdentifierListIdentifier();
				// Set the name
				inforUrlListIdentifier.setName("Information URL List");

				// Create a term
				Term infoUrlListTerm = sensorMLObjectFactory.createTerm();
				inforUrlListIdentifier.setTerm(infoUrlListTerm);
				infoUrlListTerm.setDefinition("urn" + SSDS_URN_PART
						+ ":infoUrlList");
				infoUrlListTerm.setValue(deviceToConvert.getInfoUrlList());

				// Add it to the list
				identifierList.getIdentifier().add(inforUrlListIdentifier);
			}

			// Add the identification to the list
			identificationList.add(identification);
		}
	}

	private void addDataContainerToOutputList() {

	}

	// Some constants
	static final String URN_OGC_DEF_PREFIX = "urn:ogc:def";

	static final String URN_OGC_OBJECT_PREFIX = "urn:ogc:object";

	static final String URN_OGC_IDENTIFIER_PART = URN_OGC_DEF_PREFIX
			+ ":identifier:OGC";

	static final String SSDS_URN_PART = ":org:mbari:ssds";

}
