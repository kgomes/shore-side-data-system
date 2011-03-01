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
package moos.ssds.clients.graphing;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.ejb.CreateException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import moos.ssds.metadata.DataContainer;
import moos.ssds.metadata.DataProducer;
import moos.ssds.metadata.Device;
import moos.ssds.metadata.RecordDescription;
import moos.ssds.metadata.RecordVariable;
import moos.ssds.services.data.DeviceDataAccess;
import moos.ssds.services.data.graphing.GeospatialGraphingAccess;
import moos.ssds.services.metadata.IDataContainerAccess;
import moos.ssds.services.metadata.DataProducerAccess;
import moos.ssds.services.metadata.DeviceAccess;
import moos.ssds.util.XmlDateFormat;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.Range;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * This class is the main program that will read the configuration settings from
 * SSDS and create html pages that have charts and data associated with them
 * 
 * @author kgomes
 */
public class DeviceQCPlotCreator {

	private DeviceAccess deva;
	private DataProducerAccess dprodAccess;
	private IDataContainerAccess dca;
	private DeviceDataAccess dda;
	private GeospatialGraphingAccess gga;

	/**
	 * The default constructor
	 */
	public DeviceQCPlotCreator() {
		// Read in the properties
		try {
			deviceQCProperties.load(this.getClass().getResourceAsStream(
					"/moos/ssds/clients/graphing/graphing.properties"));
		} catch (Exception e) {
			logger.error("Exception trying to read in properties file: "
					+ e.getMessage());
		}
		// Now assign the correct properties to the local parameters
		this.databaseDriverClassName = deviceQCProperties
				.getProperty("client.graphing.device.qc.plot.database.jdbc.class.name");
		this.databaseJDBCUrl = deviceQCProperties
				.getProperty("client.graphing.device.qc.plot.database.jdbc.url");
		this.username = deviceQCProperties
				.getProperty("client.graphing.device.qc.plot.database.username");
		this.password = deviceQCProperties
				.getProperty("client.graphing.device.qc.plot.database.password");
		this.plotsBaseDirectory = deviceQCProperties
				.getProperty("client.graphing.device.qc.plot.directory");
		this.plotsBaseUrlString = deviceQCProperties
				.getProperty("client.graphing.device.qc.plot.urlbase");

		// Load the DB driver
		try {
			Class.forName(this.databaseDriverClassName);
		} catch (ClassNotFoundException e) {
			logger.error("Could not find database driver class");
		}

		// Set the run start date
		this.runStartDate = new Date();

		try {
			// Grab the service interfaces
			Context context = new InitialContext();

			// Look up the remote bean
			deva = (DeviceAccess) context
					.lookup("moos/ssds/services/metadata/DeviceAccess");
			dprodAccess = (DataProducerAccess) context
					.lookup("moos/ssds/services/metadata/DataProducerAccess");
			dca = (IDataContainerAccess) context
					.lookup("moos/ssds/services/metadata/DataContainerAccess");
			dda = (DeviceDataAccess) context
					.lookup("moos/ssds/services/data/DeviceDataAccess");
			gga = (GeospatialGraphingAccess) context
					.lookup("moos/ssds/services/data/graphing/GeospatialGraphingAccess");
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * This method reads all the configuration from the SSDS_Data database and
	 * creates all the plots that are specified there
	 */
	public void buildPlots() {

		// Grab all the device IDs
		Collection deviceIDs = this.getDeviceIds();
		if (deviceIDs != null) {
			logger.debug("Got " + deviceIDs.size()
					+ " device IDs from the database");
		} else {
			logger.debug("No deviceIDs were returned");
		}

		// Now loop over those IDs
		Iterator deviceIDIter = deviceIDs.iterator();
		while (deviceIDIter.hasNext()) {

			// Grab the ID
			Long deviceID = (Long) deviceIDIter.next();
			logger.debug("Working with device with ID " + deviceID);

			// Grab the equivalent Device
			Device currentDevice = null;
			try {
				currentDevice = (Device) deva.findById(deviceID, true);
			} catch (Exception e1) {
				logger.error("Exception caught trying to find the Device: "
						+ e1.getMessage());
			}
			if (currentDevice != null) {
				logger.debug("Found matching persistent device: "
						+ currentDevice.toStringRepresentation("|"));
			} else {
				logger.debug("No matching persistent device found");
			}

			// Grab the different chart types
			Collection chartTypes = this.getChartTypes(deviceID);

			// Loop over the chart types
			Iterator chartIter = chartTypes.iterator();
			while (chartIter.hasNext()) {
				String chartType = (String) chartIter.next();
				if (chartType.equalsIgnoreCase("time_series")) {

					// We need to iterate over the packetSubTypes
					Collection packetSubTypes = this
							.getPacketSubTypes(deviceID);
					Iterator packetSubTypesIter = packetSubTypes.iterator();
					while (packetSubTypesIter.hasNext()) {
						// Grab the packet sub type
						Long packetSubType = (Long) packetSubTypesIter.next();

						// Grab the most recent deployment so that I can grab
						// the full
						// RecordVariable to use in graphing
						DataContainer mostRecentDataContainer = null;
						try {
							Collection deployments = dprodAccess.findByDevice(
									currentDevice, "startDate", "desc", false);
							if (deployments != null) {
								logger.debug(deployments.size()
										+ " deployments found for that device");
								Iterator deploymentIter = deployments
										.iterator();
								while ((deploymentIter.hasNext())
										&& (mostRecentDataContainer == null)) {
									DataProducer mostRecentDeployment = (DataProducer) deploymentIter
											.next();
									// Grab the full graph of the data producer
									mostRecentDeployment = (DataProducer) dprodAccess
											.getMetadataObjectGraph(mostRecentDeployment);
									logger.debug("The most recent Deployment is "
											+ mostRecentDeployment
													.toStringRepresentation("|"));
									// Grab the correct output
									Collection mostRecentDeploymentOutputs = new HashSet(
											mostRecentDeployment.getOutputs());
									Iterator mostRecentDeploymentOutputIterator = mostRecentDeploymentOutputs
											.iterator();
									while ((mostRecentDeploymentOutputIterator
											.hasNext())
											&& (mostRecentDataContainer == null)) {
										DataContainer tempOutput = (DataContainer) dca
												.getMetadataObjectGraph((DataContainer) mostRecentDeploymentOutputIterator
														.next());
										logger.debug("Should have the full graph of the "
												+ " data container to compare against: "
												+ tempOutput
														.toStringRepresentation("|"));
										if ((tempOutput.getRecordDescription() != null)
												&& (tempOutput
														.getRecordDescription()
														.getRecordType() != null)
												&& (tempOutput
														.getRecordDescription()
														.getRecordType()
														.longValue() == packetSubType
														.longValue())) {
											mostRecentDataContainer = tempOutput;
										}
									}
									if (mostRecentDataContainer != null) {
										logger.debug("The most recent data container is "
												+ mostRecentDataContainer
														.toStringRepresentation("|"));
									} else {
										logger.debug("No most recent data container was found");
									}
								}
							} else {
								logger.debug("No deployment for that device found");
							}
						} catch (Exception e1) {
							logger.error("Error finding the most recent DataContainer: "
									+ e1.getMessage());
						}

						// Grab the collection of record variable names
						Collection recordVariableNames = this
								.getRecordVariableNames(deviceID, packetSubType);
						if (recordVariableNames != null) {
							logger.debug(recordVariableNames.size()
									+ " record variable names were specified in the database");
						} else {
							logger.debug("No record variable names specified in the database");
						}

						// Find the max number of hours back for a device
						int maxNumHours = this.getMaxNumberOfHoursBack(
								deviceID, packetSubType);
						logger.debug("The max number of hours back for "
								+ "that device in the database is "
								+ maxNumHours);

						// Create the end date (now)
						Calendar startCalendar = GregorianCalendar
								.getInstance();
						startCalendar.setTimeZone(TimeZone.getTimeZone("GMT"));

						// Set the time to the run start date
						startCalendar.setTime(this.runStartDate);
						// Now take off the max number of hours
						startCalendar.add(GregorianCalendar.HOUR, -1
								* maxNumHours);
						logger.debug("StartDate is "
								+ xmlDateFormat.format(startCalendar.getTime()));
						logger.debug("EndDate is "
								+ xmlDateFormat.format(this.runStartDate));

						// Grab the data
						Object[][] data = null;
						try {
							data = dda.getDeviceData(currentDevice,
									packetSubType, recordVariableNames,
									startCalendar.getTime(), this.runStartDate);
						} catch (Exception e) {
							logger.error("Error trying to get data from SSDS for device "
									+ deviceID.toString()
									+ ": "
									+ e.getMessage());
						}
						if (data != null) {
							logger.debug("Found " + data.length
									+ " matching data records");
						} else {
							logger.debug("No data returned");
						}

						// OK, I now have all the data for the largest time
						// window, loop
						// over the different record variable names
						Iterator rvNameIter = recordVariableNames.iterator();
						int rvIndex = 1;
						while (rvNameIter.hasNext()) {
							String rvName = (String) rvNameIter.next();
							logger.debug("Working with the record variable name "
									+ rvName + " (specified in database)");
							// For each name, let's find the matching RV
							RecordVariable matchingRV = null;
							if (mostRecentDataContainer != null) {
								RecordDescription recordDescription = mostRecentDataContainer
										.getRecordDescription();
								if (recordDescription != null) {
									logger.debug("RecordDescription is "
											+ recordDescription
													.toStringRepresentation("|"));
									Collection recordVariables = recordDescription
											.getRecordVariables();
									if (recordVariables != null) {
										logger.debug("There are "
												+ recordVariables.size()
												+ " record variable to search in (from RecordDescription)");
										Iterator rvIter = recordVariables
												.iterator();
										while (rvIter.hasNext()) {
											RecordVariable tempRV = (RecordVariable) rvIter
													.next();
											if (tempRV.getName()
													.equalsIgnoreCase(rvName)) {
												logger.debug("Matched on RV : "
														+ tempRV.toStringRepresentation("|"));
												matchingRV = tempRV;
												break;
											}
										}
									} else {
										logger.debug("No record variable in the record description");
									}
								}
							}
							// Hopefully we should have the correct matching RV
							// Now let's iterate over all the time windows
							// specified in
							// the
							// DB and create the appropriate graphs
							Map propertiesMap = this
									.getAllTimeSeriesGraphProperties(deviceID,
											rvName);
							if (propertiesMap != null) {
								Set numHoursKeys = propertiesMap.keySet();
								Iterator numHoursIter = numHoursKeys.iterator();
								while (numHoursIter.hasNext()) {
									Integer numberOfHours = (Integer) numHoursIter
											.next();
									// Grab the properties
									Properties currentProps = (Properties) propertiesMap
											.get(numberOfHours);
									// Setup some defaults
									int xSize = 600;
									int ySize = 300;
									String xAxisLabel = "Time (GMT)";
									String yAxisLabel = rvName
											+ " (Unknown Units)";
									String title = rvName
											+ " (Unknown Units) vs. Time (GMT)";
									String subtitle = "Unknown Device";
									String pageTitle = "Unknown Device Page";
									Double yMax = null;
									Double yMin = null;

									// Try to grab stuff from the matching
									// variable
									if (matchingRV != null) {
										// Check to see if user wants long
										// variable name used
										if (currentProps
												.containsKey("useLongVariableName")
												&& (currentProps
														.get("useLongVariableName") != null)
												&& ((Boolean) currentProps
														.get("useLongVariableName"))
														.booleanValue()) {
											yAxisLabel = matchingRV
													.getLongName()
													+ " ("
													+ matchingRV.getUnits()
													+ ")";
											title = matchingRV.getLongName()
													+ " ("
													+ matchingRV.getUnits()
													+ ")" + " vs. Time (GMT)";
										} else {
											yAxisLabel = matchingRV.getName()
													+ " ("
													+ matchingRV.getUnits()
													+ ")";
											title = matchingRV.getName() + " ("
													+ matchingRV.getUnits()
													+ ")" + " vs. Time (GMT)";
										}
										yMax = matchingRV.getDisplayMax();
										yMin = matchingRV.getDisplayMin();
									}
									// If device was found, create better
									// subtitle
									if (currentDevice != null) {
										subtitle = "Device "
												+ currentDevice.getName()
												+ " (ID="
												+ currentDevice.getId()
												+ ", Manufacturer="
												+ currentDevice.getMfgName()
												+ ", Model="
												+ currentDevice.getMfgModel()
												+ ")";
										pageTitle = currentDevice.getId()
												.toString();
									}

									// Now override with database or RV
									// information
									// where
									// appropriate
									if (currentProps.containsKey("xSize")
											&& (currentProps.get("xSize") != null))
										xSize = ((Integer) currentProps
												.get("xSize")).intValue();
									if (currentProps.containsKey("ySize")
											&& (currentProps.get("ySize") != null))
										ySize = ((Integer) currentProps
												.get("ySize")).intValue();
									if (currentProps.containsKey("xAxisLabel")
											&& (currentProps
													.getProperty("xAxisLabel") != null))
										xAxisLabel = (String) currentProps
												.get("xAxisLabel");
									if (currentProps.containsKey("yAxisLabel")
											&& (currentProps
													.getProperty("yAxisLabel") != null))
										yAxisLabel = (String) currentProps
												.get("yAxisLabel");
									if (currentProps.containsKey("title")
											&& (currentProps
													.getProperty("title") != null))
										title = (String) currentProps
												.get("title");
									if (currentProps.containsKey("pageTitle")
											&& (currentProps
													.getProperty("pageTitle") != null))
										pageTitle = (String) currentProps
												.get("pageTitle");
									if (currentProps.containsKey("yMax")
											&& (currentProps.get("yMax") != null))
										yMax = (Double) currentProps
												.get("yMax");
									if (currentProps.containsKey("yMin")
											&& (currentProps.get("yMin") != null))
										yMin = (Double) currentProps
												.get("yMin");

									// Build the chart
									JFreeChart chart = this
											.buildTimeSeriesPlot(data, rvIndex,
													numberOfHours.intValue(),
													xSize, ySize, xAxisLabel,
													yAxisLabel, title,
													subtitle, yMax, yMin);

									// First create the directory for the device
									// (if
									// needed)
									File deviceDirectory = new File(
											this.plotsBaseDirectory
													+ File.separator + deviceID);
									if (!deviceDirectory.exists())
										deviceDirectory.mkdir();

									// Now the plots directory
									File devicePlotsDirectory = new File(
											this.plotsBaseDirectory
													+ File.separator + deviceID
													+ File.separator + "plots");
									if (!devicePlotsDirectory.exists())
										devicePlotsDirectory.mkdir();

									// Now the data directory
									File deviceDataDirectory = new File(
											this.plotsBaseDirectory
													+ File.separator + deviceID
													+ File.separator + "data");
									if (!deviceDataDirectory.exists())
										deviceDataDirectory.mkdir();

									// Now create the chart filename
									String chartFilename = this.plotsBaseDirectory
											+ File.separator
											+ deviceID
											+ File.separator
											+ "plots"
											+ File.separator
											+ rvName.trim().replaceAll("\\s+",
													"_")
											+ "_"
											+ numberOfHours
											+ ".jpg";

									// Create the URL to the chart
									String chartUrl = this.plotsBaseUrlString
											+ "/"
											+ deviceID
											+ "/plots/"
											+ rvName.trim().replaceAll("\\s+",
													"_") + "_" + numberOfHours
											+ ".jpg";

									// Create the filename for the data
									String dataFilename = this.plotsBaseDirectory
											+ File.separator
											+ deviceID
											+ File.separator
											+ "data"
											+ File.separator
											+ rvName.trim().replaceAll("\\s+",
													"_")
											+ "_"
											+ numberOfHours
											+ ".htm";

									// Create the URL to the data
									String dataUrl = this.plotsBaseUrlString
											+ "/"
											+ deviceID
											+ "/data/"
											+ rvName.trim().replaceAll("\\s+",
													"_") + "_" + numberOfHours
											+ ".htm";

									// HTML Page url
									String pageUrlString = this.plotsBaseUrlString
											+ "/" + pageTitle + ".htm";

									// Now write the chart to a file
									try {
										ChartUtilities.saveChartAsJPEG(
												new File(chartFilename), chart,
												xSize, ySize,
												new ChartRenderingInfo());
									} catch (IOException e1) {
										logger.error("IOException on chart saving: "
												+ e1.getMessage());
									} catch (Exception e1) {
										logger.error("Exception on chart saving: "
												+ e1.getMessage());
									}

									// Create the data page
									this.buildDataPage(new File(dataFilename),
											data, rvIndex,
											numberOfHours.intValue(),
											xAxisLabel, yAxisLabel, title);

									// TODO kgomes Now update the URLs in the DB
									this.updateURLs(deviceID, "time_series",
											rvName, numberOfHours.intValue(),
											pageUrlString, chartUrl, dataUrl);
								}
							}
							rvIndex++;
						} // End loop over recordVariableName
					} // End loop over packetSubTypes
				} else if (chartType.equalsIgnoreCase("gps")) {
					// We now need to build a GPS plot for the given device.
					// First, grab all the GPS related properties
					Map gpsProperties = this.getAllGpsGraphProperties(deviceID);

					// OK so I have a map of number of hours back to properties
					Set numHoursKeys = gpsProperties.keySet();
					Iterator numHoursIter = numHoursKeys.iterator();
					while (numHoursIter.hasNext()) {
						// The URL of the chart
						String chartUrl = null;

						// Grab the number of hours to build a graph for
						Integer numberOfHours = (Integer) numHoursIter.next();
						// Grab the properties
						Properties currentProps = (Properties) gpsProperties
								.get(numberOfHours);

						// First figure out the start and end dates
						Calendar startCalendar = GregorianCalendar
								.getInstance();
						startCalendar.setTimeZone(TimeZone.getTimeZone("GMT"));

						// Set the time to the run start date
						startCalendar.setTime(this.runStartDate);
						// Now take off the number of hours
						startCalendar.add(GregorianCalendar.HOUR, -1
								* numberOfHours.intValue());

						// Create a device with the current ID
						Device tempDevice = new Device();
						tempDevice.setId(deviceID);

						// Have SSDS build the chart and grab the returned URL
						try {
							if (gga != null)
								chartUrl = gga
										.getGpsChart(
												tempDevice,
												startCalendar.getTime(),
												this.runStartDate,
												((Boolean) currentProps
														.get("showWatchCircle"))
														.booleanValue(),
												((Double) currentProps
														.get("watchCircleDiameterInKm"))
														.doubleValue(),
												((Boolean) currentProps
														.get("scaleChartToFitData"))
														.booleanValue(),
												((Boolean) currentProps
														.get("showAnchor"))
														.booleanValue(),
												((Double) currentProps
														.get("anchorLatitude"))
														.doubleValue(),
												((Double) currentProps
														.get("anchorLongitude"))
														.doubleValue(),
												((Integer) currentProps
														.get("xSize"))
														.intValue(),
												((Integer) currentProps
														.get("ySize"))
														.intValue(),
												(String) currentProps
														.get("Title"));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						this.updateURLs(deviceID, "gps", null,
								numberOfHours.intValue(), null, chartUrl, null);
					}
				}
			}
		}
		// Build the header pages
		this.buildHeadPages();

		// Now build the master index page
		this.buildIndexPage();
	}

	private JFreeChart buildTimeSeriesPlot(Object[][] data, int rvIndex,
			int numberOfHoursBack, int xSize, int ySize, String xAxisLabel,
			String yAxisLabel, String title, String subtitle, Double yMax,
			Double yMin) {
		// The JFreeChart to return
		JFreeChart chartToReturn = null;

		// First figure out the start and end milliseconds
		Calendar startCalendar = GregorianCalendar.getInstance();
		startCalendar.setTimeZone(TimeZone.getTimeZone("GMT"));

		// Set the time to the run start date
		startCalendar.setTime(this.runStartDate);
		// Now take off the number of hours
		startCalendar.add(GregorianCalendar.HOUR, -1 * numberOfHoursBack);
		long endMillis = this.runStartDate.getTime();
		long startMillis = startCalendar.getTimeInMillis();

		// Create the DateAxis
		ValueAxis timeAxis = new DateAxis();
		timeAxis.setLabel(xAxisLabel);

		// Grab the range of time for
		long timeRange = endMillis - startMillis;

		// The date format
		DateFormat df = null;
		if (timeRange <= (1000L * 60L * 60L * 24L)) {
			df = new SimpleDateFormat("MMM-dd-yyyy HH:mm");
			((DateAxis) timeAxis).setTickUnit(new DateTickUnit(
					DateTickUnit.HOUR, 1, df));
		} else if (timeRange <= (1000L * 60L * 60L * 24L * 30L)) {
			df = new SimpleDateFormat("MMM-dd-yyyy");
			((DateAxis) timeAxis).setTickUnit(new DateTickUnit(
					DateTickUnit.DAY, 1, df));
		} else if (timeRange <= (1000L * 60L * 60L * 24L * 30L * 12L)) {
			df = new SimpleDateFormat("MMM-yyyy");
			((DateAxis) timeAxis).setTickUnit(new DateTickUnit(
					DateTickUnit.MONTH, 1, df));
		} else if (timeRange > (1000L * 60L * 60L * 24L * 30L * 12L)) {
			df = new SimpleDateFormat("yyyy");
			((DateAxis) timeAxis).setTickUnit(new DateTickUnit(
					DateTickUnit.YEAR, 1, df));
		}

		// Set label angle
		timeAxis.setVerticalTickLabels(true);
		df.setTimeZone(TimeZone.getTimeZone("GMT"));

		try {
			timeAxis.setRange(startMillis, endMillis);
		} catch (Exception e) {
			System.out
					.println("Exception caught while trying to set date range: "
							+ e.getMessage());
		}

		// Create a XYSeries to hold the data
		XYSeries xy = new XYSeries(yAxisLabel);

		// Create some place holders for the high and low values
		double highestValue = -1 * Double.MAX_VALUE;
		double lowestValue = Double.MAX_VALUE;

		// Loop through the data to add to the series and look for mins/maxs
		if ((data != null) && (data.length > 0)) {
			for (int j = 0; j < data.length; j++) {
				// If the time and data are valid
				if ((data[j][0] != null) && (data[j][rvIndex] != null)
						&& !(data[j][0] instanceof String)
						&& (((Long) data[j][0]).longValue() > startMillis)
						&& (((Long) data[j][0]).longValue() < endMillis)) {
					// Add it to the data series
					Number time = (Number) data[j][0];
					Number dataPoint = (Number) data[j][rvIndex];
					xy.add(time, dataPoint);
					// Check for mins and maxs
					if (dataPoint.doubleValue() > highestValue) {
						highestValue = dataPoint.doubleValue();
					}
					if (dataPoint.doubleValue() < lowestValue) {
						lowestValue = dataPoint.doubleValue();
					}
				}
			}
		}

		// Check to make sure there is data in the series
		XYPlot xyPlot = null;
		if (xy.getItemCount() > 0) {
			// Create the data axis
			NumberAxis numberAxis = new NumberAxis(yAxisLabel);
			// Set the correct range to fit the data
			if (yMax != null)
				highestValue = yMax.doubleValue();
			if (yMin != null)
				lowestValue = yMin.doubleValue();

			// Make sure the values are not equal, if so set the range to be
			// just a bit below and above
			if (lowestValue == highestValue) {
				lowestValue = lowestValue - 1;
				highestValue = highestValue + 1;
			}
			// Set the range
			numberAxis.setRange(lowestValue, highestValue);

			// Creat ethe XYPlot
			xyPlot = new XYPlot(new XYSeriesCollection(xy), timeAxis,
					numberAxis, new StandardXYItemRenderer());

			// Check for some very strange conditions (flat lines, whacked
			// maxs/mins)
			Range range = numberAxis.getRange();
			// Check for flat line
			if (range.getLowerBound() == range.getUpperBound()) {
				numberAxis.setRange(
						range.getLowerBound() - range.getLowerBound() * 0.01,
						range.getUpperBound() + range.getUpperBound() * 0.01);
			}
			// Check for whacked values
			Double whackedRange = new Double("4.0E-320");
			range = numberAxis.getRange();
			if (range.getUpperBound() - range.getLowerBound() < whackedRange
					.doubleValue()) {
				numberAxis.setRange(-0.1, 0.1);
			}
		}

		// Now create the chart and return it
		if (xyPlot != null) {
			chartToReturn = new JFreeChart(title,
					JFreeChart.DEFAULT_TITLE_FONT, xyPlot, false);
			chartToReturn.addSubtitle(new TextTitle(subtitle));
			chartToReturn.setBackgroundPaint(Color.WHITE);
		}

		// Return the chart
		return chartToReturn;
	}

	private void buildDataPage(File dataFile, Object[][] data, int rvIndex,
			int numberOfHoursBack, String xHeader, String yHeader, String title) {
		// First see if the file exists
		try {
			dataFile.createNewFile();
		} catch (IOException e) {
			logger.error("IOException trying to create a new data file: "
					+ e.getMessage());
		}

		// Setup the time window
		// First figure out the start and end milliseconds
		Calendar startCalendar = GregorianCalendar.getInstance();
		startCalendar.setTimeZone(TimeZone.getTimeZone("GMT"));

		// Set the time to the run start date
		startCalendar.setTime(this.runStartDate);
		// Now take off the number of hours
		startCalendar.add(GregorianCalendar.HOUR, -1 * numberOfHoursBack);
		long endMillis = this.runStartDate.getTime();
		long startMillis = startCalendar.getTimeInMillis();

		// A date formatter
		// Now try to write out the data
		PrintWriter htmlWriter = null;
		try {
			htmlWriter = new PrintWriter(new FileOutputStream(dataFile));
		} catch (FileNotFoundException e) {
			logger.error("FileNotFoundException caught: " + e.getMessage());
		}
		if (htmlWriter != null) {
			htmlWriter.println("<html><head><title>" + title
					+ "</title></head><body><pre>");
			// Write the header
			htmlWriter.println(xHeader + "," + yHeader);
			// Now write the data
			if ((data != null) && (data.length > 0)) {
				for (int j = 0; j < data.length; j++) {
					// If the time and data are valid
					if ((data[j][0] != null) && (data[j][rvIndex] != null)
							&& !(data[j][0] instanceof String)
							&& (((Long) data[j][0]).longValue() > startMillis)
							&& (((Long) data[j][0]).longValue() < endMillis)) {
						// Add it to the data series
						Date tempDate = new Date();
						tempDate.setTime(((Long) data[j][0]).longValue());
						htmlWriter.println(xmlDateFormat.format(tempDate) + ","
								+ data[j][rvIndex]);
					}
				}
			}
			// Close out the html
			htmlWriter.println("</pre></body></html>");
			htmlWriter.flush();
			htmlWriter.close();
		}
	}

	/**
	 * This method returns an array of <code>long</code>s that are the device
	 * IDs that have graphs specified
	 * 
	 * @return
	 */
	private Collection getDeviceIds() {
		// The Collection to return
		Collection collectionToReturn = new TreeSet();

		// First grab a connection to the database
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(this.databaseJDBCUrl,
					this.username, this.password);
		} catch (SQLException e) {
			logger.error("SQLException caught trying to connect to SSDS_Data: "
					+ e.getMessage());
		}

		// First query for the list of device IDs
		String queryStatement = "SELECT DISTINCT DeviceID_FK FROM DeviceQCPlotConfig WHERE GraphOn = '1' ORDER BY DeviceID_FK";
		ResultSet resultSet = null;
		PreparedStatement pstmt = null;
		try {
			pstmt = connection.prepareStatement(queryStatement);
		} catch (SQLException e) {
			logger.error("SQLException caught reading config info: "
					+ e.getMessage());
		}
		if (pstmt != null) {
			resultSet = null;
			try {
				resultSet = pstmt.executeQuery();
			} catch (SQLException e) {
				logger.error("SQLException caught trying to read plot config info: "
						+ e.getMessage());
			}
		}
		if (resultSet != null) {
			try {
				while (resultSet.next()) {
					collectionToReturn.add(new Long(resultSet
							.getLong("DeviceID_FK")));
				}
			} catch (SQLException e) {
				logger.error("SQLException caught trying to next the result set: "
						+ e.getMessage());
			}
		}

		// Close up the connection
		try {
			connection.close();
		} catch (SQLException e) {
			logger.error("Could not close the connection effectively: "
					+ e.getMessage());
		}

		// Now return the results
		return collectionToReturn;
	}

	/**
	 * This method returns the different types of chart for a specific deviceF
	 * 
	 * @param deviceID
	 * @return
	 */
	private Collection getChartTypes(Long deviceID) {
		// The Collection of chart types (strings)
		Collection chartTypes = new TreeSet();

		// First grab a connection to the database
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(this.databaseJDBCUrl,
					this.username, this.password);
		} catch (SQLException e) {
			logger.error("SQLException caught trying to connect to SSDS_Data: "
					+ e.getMessage());
		}

		// First query for the list of device IDs
		String queryStatement = "SELECT DISTINCT Chart_Type FROM DeviceQCPlotConfig WHERE DeviceID_FK = '"
				+ deviceID.longValue()
				+ "' AND GraphOn = '1' ORDER BY Chart_Type";
		ResultSet resultSet = null;
		PreparedStatement pstmt = null;
		try {
			pstmt = connection.prepareStatement(queryStatement);
		} catch (SQLException e) {
			logger.error("SQLException caught reading config info: "
					+ e.getMessage());
		}
		if (pstmt != null) {
			resultSet = null;
			try {
				resultSet = pstmt.executeQuery();
			} catch (SQLException e) {
				logger.error("SQLException caught trying to read plot config info: "
						+ e.getMessage());
			}
		}
		if (resultSet != null) {
			try {
				while (resultSet.next()) {
					chartTypes.add(resultSet.getString("Chart_Type"));
				}
			} catch (SQLException e) {
				logger.error("SQLException caught trying to next the result set: "
						+ e.getMessage());
			}
		}

		// Close up the connection
		try {
			connection.close();
		} catch (SQLException e) {
			logger.error("Could not close the connection effectively: "
					+ e.getMessage());
		}

		// Return the results
		return chartTypes;
	}

	/**
	 * This method returns the listing of packet sub types that will be
	 * processed for the given device
	 * 
	 * @param deviceID
	 * @return
	 */
	private Collection getPacketSubTypes(Long deviceID) {
		// The collection to return
		Collection packetSubTypes = new TreeSet();

		// First grab a connection to the database
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(this.databaseJDBCUrl,
					this.username, this.password);
		} catch (SQLException e) {
			logger.error("SQLException caught trying to connect to SSDS_Data: "
					+ e.getMessage());
		}

		// First query for the list of device IDs
		String queryStatement = "SELECT DISTINCT PacketSubType FROM DeviceQCPlotConfig WHERE DeviceID_FK = '"
				+ deviceID.longValue()
				+ "' and Chart_Type = 'time_series' and GraphOn = '1' ORDER BY PacketSubType";
		ResultSet resultSet = null;
		PreparedStatement pstmt = null;
		try {
			pstmt = connection.prepareStatement(queryStatement);
		} catch (SQLException e) {
			logger.error("SQLException caught reading config info: "
					+ e.getMessage());
		}
		if (pstmt != null) {
			resultSet = null;
			try {
				resultSet = pstmt.executeQuery();
			} catch (SQLException e) {
				logger.error("SQLException caught trying to read plot config info: "
						+ e.getMessage());
			}
		}
		if (resultSet != null) {
			try {
				while (resultSet.next()) {
					packetSubTypes.add(new Long(resultSet
							.getInt("PacketSubType")));
				}
			} catch (SQLException e) {
				logger.error("SQLException caught trying to next the result set: "
						+ e.getMessage());
			}
		}

		// Close up the connection
		try {
			connection.close();
		} catch (SQLException e) {
			logger.error("Could not close the connection effectively: "
					+ e.getMessage());
		}

		// Now return them
		return packetSubTypes;
	}

	/**
	 * This method takes in the device ID and returns the collection of names
	 * that charts are to be created for
	 * 
	 * @param deviceID
	 * @return
	 */
	private Collection getRecordVariableNames(Long deviceID, Long packetSubType) {
		// The collection to return
		Collection recordVariableNames = new TreeSet();

		// First grab a connection to the database
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(this.databaseJDBCUrl,
					this.username, this.password);
		} catch (SQLException e) {
			logger.error("SQLException caught trying to connect to SSDS_Data: "
					+ e.getMessage());
		}

		// First query for the list of device IDs
		String queryStatement = "SELECT DISTINCT RecordVariable_Name FROM DeviceQCPlotConfig WHERE DeviceID_FK = '"
				+ deviceID.longValue()
				+ "' and Chart_Type = 'time_series' and GraphOn = '1' ORDER BY RecordVariable_Name";
		ResultSet resultSet = null;
		PreparedStatement pstmt = null;
		try {
			pstmt = connection.prepareStatement(queryStatement);
		} catch (SQLException e) {
			logger.error("SQLException caught reading config info: "
					+ e.getMessage());
		}
		if (pstmt != null) {
			resultSet = null;
			try {
				resultSet = pstmt.executeQuery();
			} catch (SQLException e) {
				logger.error("SQLException caught trying to read plot config info: "
						+ e.getMessage());
			}
		}
		if (resultSet != null) {
			try {
				while (resultSet.next()) {
					recordVariableNames.add(resultSet
							.getString("RecordVariable_Name"));
				}
			} catch (SQLException e) {
				logger.error("SQLException caught trying to next the result set: "
						+ e.getMessage());
			}
		}

		// Close up the connection
		try {
			connection.close();
		} catch (SQLException e) {
			logger.error("Could not close the connection effectively: "
					+ e.getMessage());
		}

		// Now return them
		return recordVariableNames;
	}

	/**
	 * This method returns the largest number of hours back that is requested
	 * for a specific device. This helps in building the data arrays.
	 * 
	 * @param deviceID
	 * @return
	 */
	private int getMaxNumberOfHoursBack(Long deviceID, Long packetSubType) {

		// The largest number of hours back
		int maxNumberOfHoursBack = 0;

		// First grab a connection to the database
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(this.databaseJDBCUrl,
					this.username, this.password);
		} catch (SQLException e) {
			logger.error("SQLException caught trying to connect to SSDS_Data: "
					+ e.getMessage());
		}

		// Find that max
		String queryStatement = "SELECT MAX(NumberOfHoursBack) AS MaxNumberOfHoursBack "
				+ "FROM DeviceQCPlotConfig WHERE DeviceID_FK = '"
				+ deviceID.longValue()
				+ "' and Chart_Type = 'time_series' and PacketSubType = '"
				+ packetSubType.longValue() + "' and GraphOn = '1'";
		ResultSet resultSet = null;
		PreparedStatement pstmt = null;
		try {
			pstmt = connection.prepareStatement(queryStatement);
		} catch (SQLException e) {
			logger.error("SQLException caught reading config info: "
					+ e.getMessage());
		}
		if (pstmt != null) {
			resultSet = null;
			try {
				resultSet = pstmt.executeQuery();
			} catch (SQLException e) {
				logger.error("SQLException caught trying to read plot config info: "
						+ e.getMessage());
			}
		}
		if (resultSet != null) {
			try {
				if (resultSet.next()) {
					maxNumberOfHoursBack = resultSet
							.getInt("MaxNumberOfHoursBack");
				}
			} catch (SQLException e) {
				logger.error("SQLException caught trying to next the result set: "
						+ e.getMessage());
			}
		}

		// Close up the connection
		try {
			connection.close();
		} catch (SQLException e) {
			logger.error("Could not close the connection effectively: "
					+ e.getMessage());
		}

		// Return the result
		return maxNumberOfHoursBack;
	}

	private Map getAllTimeSeriesGraphProperties(Long deviceID,
			String recordVariableName) {
		// The map to return
		Map propertiesMap = new TreeMap();

		// Query for all the fields for the device and recordVariableName and
		// then fill out a properties object and put in the treemap matched up
		// with the number of hours back as the key

		// First grab a connection to the database
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(this.databaseJDBCUrl,
					this.username, this.password);
		} catch (SQLException e) {
			logger.error("SQLException caught trying to connect to SSDS_Data: "
					+ e.getMessage());
		}

		// Find that max
		String queryStatement = "SELECT * "
				+ "FROM DeviceQCPlotConfig WHERE DeviceID_FK = '"
				+ deviceID.longValue()
				+ "' and Chart_Type = 'time_series' and RecordVariable_Name = '"
				+ recordVariableName + "' AND GraphOn = '1'";
		ResultSet resultSet = null;
		PreparedStatement pstmt = null;
		try {
			pstmt = connection.prepareStatement(queryStatement);
		} catch (SQLException e) {
			logger.error("SQLException caught: " + e.getMessage());
		}
		if (pstmt != null) {
			resultSet = null;
			try {
				resultSet = pstmt.executeQuery();
			} catch (SQLException e) {
				logger.error("SQLException caught trying to read plot config info: "
						+ e.getMessage());
			}
		}
		if (resultSet != null) {
			try {
				while (resultSet.next()) {
					Integer numberOfHoursBack = new Integer(
							resultSet.getInt("NumberOfHoursBack"));
					Integer xSize = new Integer(resultSet.getInt("X_Size"));
					if (resultSet.wasNull())
						xSize = null;
					Integer ySize = new Integer(resultSet.getInt("Y_Size"));
					if (resultSet.wasNull())
						ySize = null;
					String xAxisLabel = resultSet.getString("X_Axis_Label");
					if (resultSet.wasNull())
						xAxisLabel = null;
					String yAxisLabel = resultSet.getString("Y_Axis_Label");
					if (resultSet.wasNull())
						yAxisLabel = null;
					String title = resultSet.getString("Title");
					if (resultSet.wasNull())
						title = null;
					String pageTitle = resultSet.getString("Page_Title");
					if (resultSet.wasNull())
						pageTitle = null;
					Double yMax = new Double(resultSet.getFloat("Y_Max"));
					if (resultSet.wasNull())
						yMax = null;
					Double yMin = new Double(resultSet.getFloat("Y_Min"));
					if (resultSet.wasNull())
						yMin = null;
					Boolean useLongVariableName = new Boolean(
							resultSet.getBoolean("Use_Long_Variable_Name"));
					if (resultSet.wasNull())
						useLongVariableName = new Boolean("false");
					Properties tempProperties = new Properties();
					if (xSize != null)
						tempProperties.put("xSize", xSize);
					if (ySize != null)
						tempProperties.put("ySize", ySize);
					if (xAxisLabel != null)
						tempProperties.put("xAxisLabel", xAxisLabel);
					if (yAxisLabel != null)
						tempProperties.put("yAxisLabel", yAxisLabel);
					if (title != null)
						tempProperties.put("title", title);
					if (pageTitle != null)
						tempProperties.put("pageTitle", pageTitle);
					if (yMax != null)
						tempProperties.put("yMax", yMax);
					if (yMin != null)
						tempProperties.put("yMin", yMin);
					if (useLongVariableName != null)
						tempProperties.put("useLongVariableName",
								useLongVariableName);
					propertiesMap.put(numberOfHoursBack, tempProperties);
				}
			} catch (SQLException e) {
				logger.error("SQLException caught trying to next the result set: "
						+ e.getMessage());
			}
		}

		// Close up the connection
		try {
			connection.close();
		} catch (SQLException e) {
			logger.error("Could not close the connection effectively: "
					+ e.getMessage());
		}

		// Return the map
		return propertiesMap;
	}

	/**
	 * This method returns all the graph properties for the devices that have
	 * GPS chart types
	 * 
	 * @param deviceID
	 * @return
	 */
	private Map getAllGpsGraphProperties(Long deviceID) {
		// The map to return
		Map propertiesMap = new TreeMap();

		// Query for all the fields for the device and chart type
		// then fill out a properties object and put in the treemap matched up
		// with the number of hours back as the key

		// First grab a connection to the database
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(this.databaseJDBCUrl,
					this.username, this.password);
		} catch (SQLException e) {
			logger.error("SQLException caught trying to connect to SSDS_Data: "
					+ e.getMessage());
		}

		// Find that max
		String queryStatement = "SELECT * "
				+ "FROM DeviceQCPlotConfig WHERE DeviceID_FK = '"
				+ deviceID.longValue()
				+ "' and Chart_Type = 'gps' and GraphOn = '1'";
		ResultSet resultSet = null;
		PreparedStatement pstmt = null;
		try {
			pstmt = connection.prepareStatement(queryStatement);
		} catch (SQLException e) {
			logger.error("SQLException caught: " + e.getMessage());
		}
		if (pstmt != null) {
			resultSet = null;
			try {
				resultSet = pstmt.executeQuery();
			} catch (SQLException e) {
				logger.error("SQLException caught trying to read plot config info: "
						+ e.getMessage());
			}
		}
		if (resultSet != null) {
			try {
				while (resultSet.next()) {
					Properties tempProperties = new Properties();
					Integer numberOfHoursBack = new Integer(
							resultSet.getInt("NumberOfHoursBack"));
					String chartTitle = resultSet.getString("Title");
					tempProperties.put("Title", chartTitle);
					Integer xSize = new Integer(resultSet.getInt("X_Size"));
					if (resultSet.wasNull())
						xSize = null;
					if (xSize != null)
						tempProperties.put("xSize", xSize);
					Integer ySize = new Integer(resultSet.getInt("Y_Size"));
					if (resultSet.wasNull())
						ySize = null;
					if (ySize != null)
						tempProperties.put("ySize", ySize);
					Boolean showAnchor = new Boolean(
							resultSet.getBoolean("Gps_Show_Anchor"));
					if (resultSet.wasNull())
						showAnchor = new Boolean("false");
					tempProperties.put("showAnchor", showAnchor);
					Double anchorLatitude = new Double(
							resultSet.getFloat("Gps_Anchor_Latitude"));
					if (resultSet.wasNull())
						anchorLatitude = new Double(0.0);
					tempProperties.put("anchorLatitude", anchorLatitude);
					Double anchorLongitude = new Double(
							resultSet.getFloat("Gps_Anchor_Longitude"));
					if (resultSet.wasNull())
						anchorLongitude = new Double(0.0);
					tempProperties.put("anchorLongitude", anchorLongitude);
					Boolean showWatchCircle = new Boolean(
							resultSet.getBoolean("Gps_Show_Watch_Circle"));
					if (resultSet.wasNull())
						showWatchCircle = new Boolean("false");
					tempProperties.put("showWatchCircle", showWatchCircle);
					Double watchCircleDiameterInKm = new Double(
							resultSet.getDouble("Gps_Watch_Circle_Diameter_Km"));
					if (resultSet.wasNull())
						watchCircleDiameterInKm = new Double(0.0);
					tempProperties.put("watchCircleDiameterInKm",
							watchCircleDiameterInKm);
					Boolean scaleChartToFitData = new Boolean(
							resultSet.getBoolean("Gps_Scale_Chart_To_Fit_Data"));
					if (resultSet.wasNull())
						scaleChartToFitData = new Boolean("false");
					tempProperties.put("scaleChartToFitData",
							scaleChartToFitData);
					propertiesMap.put(numberOfHoursBack, tempProperties);
				}
			} catch (SQLException e) {
				logger.error("SQLException caught trying to next the result set: "
						+ e.getMessage());
			}
		}

		// Close up the connection
		try {
			connection.close();
		} catch (SQLException e) {
			logger.error("Could not close the connection effectively: "
					+ e.getMessage());
		}

		// Return the map
		return propertiesMap;
	}

	private void updateURLs(Long deviceID, String chartType,
			String recordVariableName, int numberOfHoursBack,
			String pageURLString, String imageUrlString,
			String dataPageUrlString) {
		// First grab a connection to the database
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(this.databaseJDBCUrl,
					this.username, this.password);
		} catch (SQLException e) {
			logger.error("SQLException caught trying to connect to SSDS_Data: "
					+ e.getMessage());
		}

		// Create the query string
		StringBuffer queryStatementBuffer = new StringBuffer();
		queryStatementBuffer.append("UPDATE DeviceQCPlotConfig SET ");
		queryStatementBuffer.append("Page_URL_String = ");
		if (pageURLString != null) {
			queryStatementBuffer.append("'" + pageURLString + "'");
		} else {
			queryStatementBuffer.append("NULL");
		}
		queryStatementBuffer.append(", Image_URL_String = ");
		if (imageUrlString != null) {
			queryStatementBuffer.append("'" + imageUrlString + "'");
		} else {
			queryStatementBuffer.append("NULL");
		}
		queryStatementBuffer.append(", Data_Page_URL_String = ");
		if (dataPageUrlString != null) {
			queryStatementBuffer.append("'" + dataPageUrlString + "'");
		} else {
			queryStatementBuffer.append("NULL");
		}
		queryStatementBuffer.append(" WHERE DeviceID_FK = '"
				+ deviceID.longValue() + "' and Chart_Type = '" + chartType
				+ "'");
		if (recordVariableName != null) {
			queryStatementBuffer.append(" and RecordVariable_Name = '"
					+ recordVariableName + "'");
		}
		queryStatementBuffer.append(" and NumberOfHoursBack = "
				+ numberOfHoursBack);
		String queryStatement = queryStatementBuffer.toString();
		PreparedStatement pstmt = null;
		try {
			pstmt = connection.prepareStatement(queryStatement);
		} catch (SQLException e) {
			logger.error("SQLException caught: " + e.getMessage());
		}
		if (pstmt != null) {
			try {
				pstmt.executeUpdate();
			} catch (SQLException e) {
				logger.error("SQLException caught trying to read plot config info: "
						+ e.getMessage());
			}
		}

		// Close up the connection
		try {
			connection.close();
		} catch (SQLException e) {
			logger.error("Could not close the connection effectively: "
					+ e.getMessage());
		}

	}

	private void buildHeadPages() {
		// First grab a connection to the database
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(this.databaseJDBCUrl,
					this.username, this.password);
		} catch (SQLException e) {
			logger.error("SQLException caught trying to connect to SSDS_Data: "
					+ e.getMessage());
		}

		// Get all page titles first
		Collection pageTitles = new ArrayList();
		String queryStatement = "SELECT DISTINCT Page_Title "
				+ "FROM DeviceQCPlotConfig WHERE GraphOn = '1'";

		ResultSet resultSet = null;
		PreparedStatement pstmt = null;
		try {
			pstmt = connection.prepareStatement(queryStatement);
		} catch (SQLException e) {
			logger.error("SQLException caught: " + e.getMessage());
		}
		if (pstmt != null) {
			resultSet = null;
			try {
				resultSet = pstmt.executeQuery();
			} catch (SQLException e) {
				logger.error("SQLException caught trying to read plot config info: "
						+ e.getMessage());
			}
		}
		if (resultSet != null) {
			try {
				while (resultSet.next()) {
					pageTitles.add(resultSet.getString("Page_Title"));
				}
			} catch (SQLException e) {
				logger.error("SQLException caught trying to next the result set: "
						+ e.getMessage());
			}
		}

		// Close up the connection
		try {
			connection.close();
		} catch (SQLException e) {
			logger.error("Could not close the connection effectively: "
					+ e.getMessage());
		}

		// OK now that I have the page titles, loop over them to create the
		// pages
		Iterator pageTitleIter = pageTitles.iterator();
		while (pageTitleIter.hasNext()) {
			String pageTitle = (String) pageTitleIter.next();
			// Create the new HTML file
			File htmlPage = new File(this.plotsBaseDirectory + File.separator
					+ pageTitle + ".htm");
			try {
				htmlPage.createNewFile();
			} catch (IOException e1) {
				logger.error("IOException create page with title " + pageTitle
						+ ": " + e1.getMessage());
			}
			PrintWriter htmlWriter = null;
			try {
				htmlWriter = new PrintWriter(new FileOutputStream(htmlPage));
			} catch (FileNotFoundException e1) {
				logger.error("IOException open writer to page with title "
						+ pageTitle + ": " + e1.getMessage());
			}
			// Write the header
			htmlWriter.println("<html><head><title>" + pageTitle
					+ "</title></head><body><center>");
			// Now query for all the entries with that page title
			try {
				connection = DriverManager.getConnection(this.databaseJDBCUrl,
						this.username, this.password);
			} catch (SQLException e) {
				logger.error("SQLException caught trying to connect to SSDS_Data: "
						+ e.getMessage());
			}

			// Get all page titles first
			queryStatement = "SELECT * "
					+ "FROM DeviceQCPlotConfig WHERE Page_Title = '"
					+ pageTitle
					+ "' AND GraphOn = '1' ORDER BY DeviceID_FK, RecordVariable_Name, NumberOfHoursBack";

			resultSet = null;
			pstmt = null;
			try {
				pstmt = connection.prepareStatement(queryStatement);
			} catch (SQLException e) {
				logger.error("SQLException caught: " + e.getMessage());
			}
			if (pstmt != null) {
				resultSet = null;
				try {
					resultSet = pstmt.executeQuery();
				} catch (SQLException e) {
					logger.error("SQLException caught trying to read plot config info: "
							+ e.getMessage());
				}
			}
			if (resultSet != null) {
				try {
					while (resultSet.next()) {
						// Grab the image URL
						String imageURLString = resultSet
								.getString("Image_URL_String");
						String dataPageURLString = resultSet
								.getString("Data_Page_URL_String");
						htmlWriter.println("<img border=\"0\" src=\""
								+ imageURLString + "\"/><br/>");
						if (dataPageURLString != null)
							htmlWriter
									.println("<a href=\""
											+ dataPageURLString
											+ "\" target=\"_blank\">Click Here for the Data</a>");
						htmlWriter.println("<hr/>");
					}
				} catch (SQLException e) {
					logger.error("SQLException caught trying to next the result set: "
							+ e.getMessage());
				}
			}

			// Close up the connection
			try {
				connection.close();
			} catch (SQLException e) {
				logger.error("Could not close the connection effectively: "
						+ e.getMessage());
			}
			// Write the closing stuff
			htmlWriter.println("</center></body></html>");
			htmlWriter.flush();
			htmlWriter.close();
		}

	}

	private void buildIndexPage() {
		// First grab a connection to the database
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(this.databaseJDBCUrl,
					this.username, this.password);
		} catch (SQLException e) {
			logger.error("SQLException caught trying to connect to SSDS_Data: "
					+ e.getMessage());
		}

		// Get all page titles first
		Collection pageTitles = new ArrayList();
		String queryStatement = "SELECT DISTINCT Page_Title "
				+ "FROM DeviceQCPlotConfig WHERE GraphOn = '1' ORDER BY Page_Title";

		ResultSet resultSet = null;
		PreparedStatement pstmt = null;
		try {
			pstmt = connection.prepareStatement(queryStatement);
		} catch (SQLException e) {
			logger.error("SQLException caught: " + e.getMessage());
		}
		if (pstmt != null) {
			resultSet = null;
			try {
				resultSet = pstmt.executeQuery();
			} catch (SQLException e) {
				logger.error("SQLException caught trying to read plot config info: "
						+ e.getMessage());
			}
		}
		if (resultSet != null) {
			try {
				while (resultSet.next()) {
					pageTitles.add(resultSet.getString("Page_Title"));
				}
			} catch (SQLException e) {
				logger.error("SQLException caught trying to next the result set: "
						+ e.getMessage());
			}
		}

		// Close up the connection
		try {
			connection.close();
		} catch (SQLException e) {
			logger.error("Could not close the connection effectively: "
					+ e.getMessage());
		}

		// OK now that I have the page titles, loop over them to write the index
		// page
		// Create the new HTML file
		File htmlPage = new File(this.plotsBaseDirectory + File.separator
				+ "index.htm");
		try {
			htmlPage.createNewFile();
		} catch (IOException e1) {
			logger.error("IOException create index page: " + e1.getMessage());
		}
		PrintWriter htmlWriter = null;
		try {
			htmlWriter = new PrintWriter(new FileOutputStream(htmlPage));
		} catch (FileNotFoundException e1) {
			logger.error("IOException open writer to the index page: "
					+ e1.getMessage());
		}
		// Write the header
		htmlWriter
				.println("<html><head><title>SSDS Generated QC Plots</title></head>"
						+ "<body><center><h2>Pages Containing SSDS Generated Device QC Plots</h2>"
						+ "</center><ol>");
		Iterator pageTitleIter = pageTitles.iterator();
		while (pageTitleIter.hasNext()) {
			String pageTitle = (String) pageTitleIter.next();
			// Remove spaces and encode them as %20
			String pageUrl = pageTitle.replaceAll("\\s+", "%20");

			// Write the page link
			htmlWriter.println("<li><a href=\"" + pageUrl + ".htm\">"
					+ pageTitle + "</a></li>");
		}
		htmlWriter.println("</ol></body></html>");
		htmlWriter.flush();
		htmlWriter.close();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// Create a new DeviceQCPlotCreator
		DeviceQCPlotCreator dqcp = new DeviceQCPlotCreator();

		// Build the plots
		dqcp.buildPlots();
	}

	/**
	 * This is the properties to read in some configuration settings
	 */
	private Properties deviceQCProperties = new Properties();

	/**
	 * The database connection properties
	 */
	private String databaseDriverClassName = null;
	private String databaseJDBCUrl = null;
	private String username = null;
	private String password = null;

	/**
	 * File information for storing plots and pages
	 */
	private String plotsBaseDirectory = null;

	/**
	 * The URL base for accessing the base directory
	 */
	private String plotsBaseUrlString = null;

	/**
	 * This is the date in which the plot creation run was started
	 */
	private Date runStartDate = null;

	/**
	 * A data formatter
	 */
	private XmlDateFormat xmlDateFormat = new XmlDateFormat();

	/**
	 * A log4j logger
	 */
	Logger logger = Logger.getLogger(DeviceQCPlotCreator.class);
}
