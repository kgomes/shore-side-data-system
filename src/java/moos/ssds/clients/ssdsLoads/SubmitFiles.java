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
package moos.ssds.clients.ssdsLoads;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;

import javax.ejb.EJBHome;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import moos.ssds.dao.util.MetadataAccessException;
import moos.ssds.metadata.DataContainer;
import moos.ssds.metadata.DataProducer;
import moos.ssds.metadata.RecordDescription;
import moos.ssds.metadata.RecordVariable;
import moos.ssds.metadata.Resource;
import moos.ssds.metadata.Software;
import moos.ssds.metadata.util.MetadataException;
import moos.ssds.metadata.util.ObjectBuilder;
import moos.ssds.metadata.util.XmlBuilder;
import moos.ssds.services.metadata.DataProducerAccess;
import nu.xom.ValidityException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import dods.dap.Attribute;
import dods.dap.AttributeTable;
import dods.dap.DAS;
import dods.dap.DConnect;
import dods.dap.DDS;
import dods.dap.DGrid;
import dods.dap.DODSException;
import dods.dap.NoSuchAttributeException;
import dods.dap.parser.ParseException;
import dods.util.Getopts;
import dods.util.InvalidSwitch;

/**
 * <p>
 * Read an Input list of files, information about the Software and the
 * ProcessRun used to generate the Output and submit the metadata of all of this
 * to SSDS. User can request just XML-formatted output of the metadata, or
 * request submission of the metadata to SSDS.
 * </p>
 * <hr>
 * 
 * @author : $Author: mccann $
 * @version : $Revision: 1.17.2.7 $
 * @stereotype thing
 */
public class SubmitFiles {

	private static Context jndiContext = null;
	private static String namingUrl = null;

	private static boolean sendXML = false;
	private static boolean submitToSSDS = false;
	private static boolean verbose = false;
	private static final String VERSION = "0.1 Beta";
	private static boolean validateXML = false;
	private static boolean updatePR = false;

	// Use parent package so that moos.ssds.model will inherit
	static Logger logger = Logger.getLogger("moos.ssds");

	/**
	 * Print usage
	 */
	private static void usage() {

		// Lookup default ejbHome
		EJBHome ejbHome = null;

		ejbHome = getHome("moos/ssds/services/metadata/DataProducerAccess",
				namingUrl);

		System.err
				.println("\nUsage: SubmitFiles "
						+ "-i <ifile> -o <ofile> -p|-u <pfile> | -d <dfile> [-j <naming>] [-x] [-s] [-V] [-v]");
		System.err.println();
		System.err
				.println(" -i: Either a .txt or a .xml file specifying the input data set(s)");
		System.err
				.println("     If .txt file: contains \\n separated list of OPeNDAP URLs for output data set(s)");
		System.err
				.println("     If .xml file: SSDS data model of <DataContainer> and child element(s)");

		System.err
				.println(" -o: Either a .txt or a .xml file specifying the output data set");
		System.err
				.println("     If .txt file: contains \\n separated list of OPeNDAP URLs for output data set");
		System.err
				.println("     If .xml file: SSDS data model of <DataContainer> and child element(s)");
		System.err
				.println(" -p: file containing XML formatted information about the ProcessRun that produced the output files ");
		System.err.println("     Example:");
		System.err.println("     <metadata>");
		System.err
				.println("         <ProcessRun name=\"depl2hourly.pl execution\" startDate=\"2003-11-14T16:39:48Z\">");
		// System.err.println(" <hostName=\"archaea.shore.mbari.org\"/>");
		System.err
				.println("             <description>Job that is run to produce data sets for the LAS OASIS web server</description>");
		// System.err.println(" <Person email=\"mccann@mbari.org\"/>");

		System.err
				.println("             <Software name=\"depl2hourly.pl\" version=\"$Id: SubmitFiles.java,v 1.17.2.7 2007/06/20 23:17:14 mccann Exp $\">");
		System.err
				.println("                 <description>Perl script that builds Ferret .jnl files and executes them to produce hourly and depth gridded data sets for visualization and analysis.</description>");
		System.err
				.println("                 <Person email=\"mccann@mbari.org\"/>");
		System.err.println("             </Software>");

		System.err.println("         </ProcessRun>");
		System.err.println("     </metadata>\n");
		System.err.println();
		System.err
				.println(" -u: Same as for -p, except that the ProcessRun record that is the creator of the output file ");
		System.err
				.println("     specified is updated with the contents of the <pfile> XML.");
		System.err.println();
		System.err
				.println(" -d: file containing XML formatted information about Deployments ");
		System.err
				.println("     The -d option must be used independently of the -i, -p (or -u), and -o options.");
		System.err.println();
		System.err
				.println(" -x: Send XML of the metadata to standard output (default is false)");
		System.err
				.println(" -s: Submit the metadata to SSDS (default is false)");
		if (ejbHome != null) {
			try {
				System.err
						.println(" -j: Override default jndi naming provider of jnp://"
								+ jndiContext.getEnvironment().get(
										"java.naming.provider.url"));
			} catch (NamingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		System.err
				.println(" -V: Validate submitted XML against the DTD specified");
		System.err.println(" -v: Turn on verbose output");
		System.err.println();
		System.err.println("Example deployment metadata load:");
		System.err.println("  java -jar ssdsLoads.jar -d deplM1.xml -s\n");
		System.err
				.println("Example data processing metadata with just final xml display):");
		System.err
				.println("  java -jar ssdsLoads.jar -i in.txt -o out.txt -p pr.xml -x");
	}

	/**
	 * Process command line arguments and Submit processed data files to SSDS
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		Collection inputFiles = null;
		Collection outputFiles = null;

		String prUrl = null;
		String deplUrl = null;
		String inUrl = null;
		String outUrl = null;

		if (args.length == 0) {
			usage();
			System.exit(0);
		}

		/*
		 * Process command line arguments
		 */
		try {
			Getopts opts = new Getopts("i:p:u:o:j:xsVvd:", args);

			String optVal;

			// Input files
			optVal = opts.getSwitch(new Character('i')).val;
			if (optVal != null) {
				if (optVal.endsWith(".txt")) {
					FileParser ip = new FileParser(optVal);
					inputFiles = ip.getLines();
				} else if (optVal.endsWith(".xml")) {
					inUrl = optVal;
				}
			}

			// Output files
			optVal = opts.getSwitch(new Character('o')).val;
			if (optVal != null) {
				if (optVal.endsWith(".txt")) {
					FileParser op = new FileParser(optVal);
					outputFiles = op.getLines();
				} else if (optVal.endsWith(".xml")) {
					outUrl = optVal;
				}
			}

			// Capture ProcessRun XML URL
			optVal = opts.getSwitch(new Character('p')).val;
			if (optVal != null) {
				prUrl = optVal;
			}

			// Capture ProcessRun to update XML URL
			optVal = opts.getSwitch(new Character('u')).val;
			if (optVal != null) {
				updatePR = true;
				prUrl = optVal;
			}

			// Capture Deployment XML URL
			optVal = opts.getSwitch(new Character('d')).val;
			if (optVal != null) {
				deplUrl = optVal;
			}

			// Set new EJB host if specified
			optVal = opts.getSwitch(new Character('j')).val;
			if (optVal != null) {
				namingUrl = optVal;
			}

			// Switch to spit out XML
			if (opts.getSwitch(new Character('x')).set) {
				sendXML = true;
			}

			// Switch to submit to SSDS
			if (opts.getSwitch(new Character('s')).set) {
				submitToSSDS = true;
			}

			// Verbose switch
			if (opts.getSwitch(new Character('v')).set) {
				verbose = true;
				logger.setLevel(Level.DEBUG);
			}

			// Print version & exit
			if (opts.getSwitch(new Character('V')).set) {
				validateXML = true;
			}

		} catch (InvalidSwitch e) {
			usage();
			System.exit(1);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/*
		 * Build ProcessRun or Deployment and dispose of as the user has
		 * specified
		 */
		if (prUrl == null && deplUrl == null) {
			System.err
					.println("\nMust provide -p (or -d) option with XML file containing ProcessRun (or Deployment) information.");
			System.exit(1);

			// Call the correct doProcessRun depending on txt or xml files...
		} else if (prUrl != null && inputFiles != null && outputFiles != null) {
			try {
				doProcessRun(inputFiles, outputFiles, prUrl);
			} catch (DODSException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} else if (prUrl != null && inputFiles != null && outUrl != null) {
			try {
				doProcessRun(inputFiles, outUrl, prUrl);
			} catch (DODSException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} else if (prUrl != null && inUrl != null && outputFiles != null) {
			try {
				doProcessRun(inUrl, outputFiles, prUrl);
			} catch (DODSException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} else if (prUrl != null && inUrl != null && outUrl != null) {

			doProcessRun(inUrl, outUrl, prUrl);

		} else if (deplUrl != null) {
			try {
				doDeployment(deplUrl);
			} catch (DODSException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				System.exit(1);
			}
		}

		// we must explicitly exit
		System.exit(0);

	} // End main()

	/**
	 * Read Deployment information from the URL, create objects form it and
	 * dispose of them as the user has specified on the command line.
	 */
	private static void doDeployment(String deplUrl) throws DODSException {
		URL url = null;
		DataProducer depl = new DataProducer();
		logger.debug("doDeployment(): XML file name = " + deplUrl);
		try {
			url = new URL(deplUrl);
		} catch (MalformedURLException e1) {
			File file = new File(deplUrl);
			try {
				url = file.toURL();
			} catch (MalformedURLException e3) {
				logger.info("Failed to construct a URL from file =" + deplUrl);
				e3.printStackTrace();
				System.exit(1);
			}
		}

		logger.debug("url.toString() = " + url.toString());
		logger.debug("url.toExternalForm() = " + url.toExternalForm());

		ObjectBuilder ob = new ObjectBuilder(url);
		if (validateXML) {
			try {
				logger.info("Unmashaling deployment XML with validation...");
				ob.unmarshal(true);
			} catch (ValidityException vE) {
				System.out.println("Cause = " + vE.getCause());
				System.out
						.println("Error at line number " + vE.getLineNumber());
				vE.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			logger.info("Unmashaling deployment XML...");
			try {
				ob.unmarshal(false);
			} catch (ValidityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// Process each top-level deployment in the xml
		Iterator it = ob.listDataProducers().iterator();
		while (it.hasNext()) {
			logger.debug("Getting deployment");
			depl = (DataProducer) it.next();
			logger.debug("depl = " + depl);

			if (depl.getOutputs() != null) {
				logger.debug("Collecting output file urls and removing them fom depl object...");

				// Was .listAllOutputsInclChildren().iterator();
				Iterator itO = depl.getOutputs().iterator();
				// Assume only one output per Deployment, get a new outputFiles
				// collection each time
				while (itO.hasNext()) {
					DataContainer df = (DataContainer) itO.next();
					String dfString = df.getUriString();

					if (dfString != null) {
						String of = df.getUrl().toExternalForm();
						Collection outputFiles = new ArrayList();
						outputFiles.add(of);
						DataProducer d = (DataProducer) df.getCreator();
						d.removeOutput(df);

						// Now add the full datafiles back
						Class partypes[] = new Class[1];
						partypes[0] = DataContainer.class;
						try {
							addFiles(outputFiles, d,
									DataProducer.class.getMethod("addOutput",
											partypes));
						} catch (SecurityException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (NoSuchMethodException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}

			/*
			 * Print out DataProducer info in XML format
			 */
			if (sendXML) {
				XmlBuilder xmlBuilder = new XmlBuilder();
				xmlBuilder.add(depl);

				xmlBuilder.marshal();
				try {
					System.err
							.println("Constructed XML\n================================================\n"
									+ xmlBuilder.toFormattedXML());
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}

			/*
			 * Call Insert DataProducer on a remote interface to the EJB service
			 */
			if (submitToSSDS) {
				DataProducerAccess deploymentAccess = null;
				try {
					Context context = new InitialContext();
					deploymentAccess = (DataProducerAccess) context
							.lookup("moos/ssds/services/metadata/DataProducerAccess");
				} catch (NamingException ex) {
					ex.printStackTrace();
				}

				try {
					// Insert the DataProducer
					System.out.println("\nSubmitting metadata to SSDS...");
					Long id = deploymentAccess.insert(depl);
					System.out.println("Inserted DataProducer id = " + id);
				} catch (MetadataAccessException e) {
					// TODO mpm Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Read ProcessRun information from the URL, create objects form it and
	 * dispose of them as the user has specified on the command line. Build
	 * model by now doing things in the proper order: 1) build PR, 2) addInputs,
	 * and 3) add Outputs. Build the model from the ProcessRun, input files, and
	 * output files Advise user if a required option has been omitted from the
	 * command line
	 * 
	 * @param inputFiles
	 * @param outputFiles
	 */
	private static void doProcessRun(Collection inputFiles,
			Collection outputFiles, String prUrl) throws DODSException {

		DataProducer pr = new DataProducer();
		ObjectBuilder ob = objectBuilderFromUrl(prUrl);
		Iterator it = ob.listDataProducers().iterator();

		/**
		 * The last ProcessRun will be used (there should be only one ProcessRun
		 * in the XML file that is passed).
		 */
		if (it.hasNext()) {
			pr = (DataProducer) it.next();
		} else {
			logger.error("\nFailed to get ProcessRun from url = " + prUrl);
			System.exit(1);
		}

		if (it.hasNext()) {
			logger.info("Only the first ProcessRun is used");
		}

		try {
			// Parameter for addInput and addOutput methods on DataProducer
			Class partypes[] = new Class[1];
			partypes[0] = DataContainer.class;

			if (inputFiles != null) {
				logger.debug("Adding input files with addFiles()...");
				addFiles(inputFiles, pr,
						DataProducer.class.getMethod("addInput", partypes));
			} else {
				logger.error("\nMust provide -i option with file containing input files URLS.");
				System.exit(1);
			}
			if (outputFiles != null) {
				logger.debug("Adding output files with addFiles()...");
				DataContainer df = addFiles(outputFiles, pr,
						DataProducer.class.getMethod("addOutput", partypes));

				// Add the ProcessRun resources to the output DataFiles
				// As of 12/11/03 these Resources are not being persisted by
				// OJB due to some thread unsafe bug. Continue to add them
				// for when OJB is fixed.
				Iterator itR = pr.getResources().iterator();
				while (itR.hasNext()) {
					Resource r = (Resource) itR.next();
					logger.debug("Adding resource r = " + r.getName());
					df.addResource(r);
				}

			} else {
				logger.error("\nMust provide -o option with file containing output files URLS.");
				System.exit(1);
			}
		} catch (SecurityException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (NoSuchMethodException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		// Dispose of the constructed ProcessRun
		disposePRMetadata(pr);
	}

	/**
	 * Build ProcessRun when passed a collection of input files and XML for
	 * output file
	 * 
	 * @param inputFiles
	 * @param outUrl
	 * @param prUrl
	 */
	private static void doProcessRun(Collection inputFiles, String outUrl,
			String prUrl) throws DODSException {

		DataProducer pr = new DataProducer();
		ObjectBuilder ob = objectBuilderFromUrl(prUrl);
		Iterator itPr = ob.listDataProducers().iterator();

		/**
		 * The last ProcessRun will be used (there should be only one ProcessRun
		 * in the XML file that is passed).
		 */
		if (itPr.hasNext()) {
			pr = (DataProducer) itPr.next();
		} else {
			logger.error("\nFailed to get ProcessRun from url = " + prUrl);
			System.exit(1);
		}

		if (itPr.hasNext()) {
			logger.info("Only the first ProcessRun is used");
		}

		Software s = (Software) pr.getSoftware();
		logger.debug("s.name = " + s.getName() + "   s.url = "
				+ s.getUrl().toExternalForm());

		// Parameter for addInput and addOutput methods on DataProducer
		Class partypes[] = new Class[1];
		partypes[0] = DataContainer.class;

		if (inputFiles != null) {

			try {
				logger.debug("Adding input files with addFiles()...");
				addFiles(inputFiles, pr,
						DataProducer.class.getMethod("addInput", partypes));
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			logger.error("\nMust provide -i option with file containing input files URLS.");
			System.exit(1);

		}

		// Get output file information from the xml file and add to pr
		ObjectBuilder ob_out = objectBuilderFromUrl(outUrl);
		Iterator itDf = ob_out.listAll().iterator();

		while (itDf.hasNext()) {
			DataContainer df = (DataContainer) itDf.next();
			pr.addOutput(df);
		}

		// Dispose of the constructed DataProducer
		disposePRMetadata(pr);
	}

	/**
	 * Build ProcessRun when passed XML for input file and a collection of
	 * output files
	 * 
	 * @param inUrl
	 * @param outputFiles
	 * @param prUrl
	 */
	private static void doProcessRun(String inUrl, Collection outputFiles,
			String prUrl) throws DODSException {

		DataProducer pr = new DataProducer();
		ObjectBuilder ob = objectBuilderFromUrl(prUrl);
		Iterator itPr = ob.listDataProducers().iterator();

		/**
		 * The last ProcessRun will be used (there should be only one ProcessRun
		 * in the XML file that is passed).
		 */
		if (itPr.hasNext()) {
			pr = (DataProducer) itPr.next();
		} else {
			logger.error("\nFailed to get ProcessRun from url = " + prUrl);
			System.exit(1);
		}

		if (itPr.hasNext()) {
			logger.info("Only the first ProcessRun is used");
		}

		// Parameter for addInput and addOutput methods on DataProducer
		Class partypes[] = new Class[1];
		partypes[0] = DataContainer.class;

		if (outputFiles != null) {
			logger.debug("Adding output files with addFiles()...");
			DataContainer df = null;
			try {
				df = addFiles(outputFiles, pr,
						DataProducer.class.getMethod("addOutput", partypes));
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// Add the ProcessRun resources to the output DataFiles
			// As of 12/11/03 these Resources are not being persisted by
			// OJB due to some thread unsafe bug. Continue to add them
			// for when OJB is fixed.
			Iterator itR = pr.getResources().iterator();
			while (itR.hasNext()) {
				Resource r = (Resource) itR.next();
				if (df != null) {
					logger.debug("Adding resource r = " + r.getName());
					df.addResource(r);
				}
			}

		} else {
			logger.error("\nMust provide -o option with file containing output files URLS.");
			System.exit(1);
		}

		// Get input file information from the xml file and add to pr
		ObjectBuilder ob_in = objectBuilderFromUrl(inUrl);
		Iterator itDc = ob_in.listAll().iterator();

		while (itDc.hasNext()) {
			DataContainer dc = (DataContainer) itDc.next();
			if (dc instanceof DataContainer) {
				DataContainer df = (DataContainer) dc;
				pr.addInput(df);
			}
		}

		// Dispose of the constructed ProcessRun
		disposePRMetadata(pr);
	}

	/**
	 * Build ProcessRun when passed XML for input file output file
	 * 
	 * @param inUrl
	 * @param outUrl
	 * @param prUrl
	 */
	private static void doProcessRun(String inUrl, String outUrl, String prUrl) {

		DataProducer pr = new DataProducer();
		ObjectBuilder ob = objectBuilderFromUrl(prUrl);
		Iterator itPr = ob.listDataProducers().iterator();

		/**
		 * The last ProcessRun will be used (there should be only one ProcessRun
		 * in the XML file that is passed).
		 */
		if (itPr.hasNext()) {
			pr = (DataProducer) itPr.next();
		} else {
			logger.error("\nFailed to get ProcessRun from url = " + prUrl);
			System.exit(1);
		}

		if (itPr.hasNext()) {
			logger.info("Only the first ProcessRun is used");
		}

		// Get input file information from the xml file and add to pr
		ObjectBuilder ob_in = objectBuilderFromUrl(inUrl);
		Iterator itDfI = ob_in.listAll().iterator();

		while (itDfI.hasNext()) {
			DataContainer df = (DataContainer) itDfI.next();
			pr.addInput(df);
		}

		// Get output file information from the xml file and add to pr
		ObjectBuilder ob_out = objectBuilderFromUrl(outUrl);
		Iterator itDfO = ob_out.listAll().iterator();

		while (itDfO.hasNext()) {
			DataContainer df = (DataContainer) itDfO.next();
			pr.addOutput(df);
		}

		// Dispose of the constructed ProcessRun
		disposePRMetadata(pr);
	}

	/**
	 * Depending on command line flags send metadata to stdout or submit to SSDS
	 * database.
	 * 
	 * @param pr
	 */
	private static void disposePRMetadata(DataProducer pr) { /*
															 * Print out
															 * ProcessRun info
															 * in XML format
															 */
		if (sendXML) {
			XmlBuilder xmlBuilder = new XmlBuilder();
			xmlBuilder.add(pr);
			xmlBuilder.marshal();
			try {
				System.out.println("XML = \n" + xmlBuilder.toFormattedXML());
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		/*
		 * Call Insert ProcessRun on a remote interface to the EJB service
		 */

		if (submitToSSDS) {

			DataProducerAccess processRunAccess = null;

			try {
				Context context = new InitialContext();
				processRunAccess = (DataProducerAccess) context
						.lookup("moos/ssds/services/metadata/DataProducerAccess");
			} catch (NamingException ex) {
				ex.printStackTrace();
			}

			if (updatePR) {
				Long id;
				try {
					System.out
							.println("\nSubmitting metadata to SSDS (updating PR)...");
					id = processRunAccess.update(pr);
					if (id == null) {
						// Go ahead and insert a new ProcessRun
						logger.debug("disposePRMetadata(): null id returned from update(), inserting new ProcessRun.");
						id = processRunAccess.insert(pr);
					}
					System.out.println("done. ProcessRun id = " + id
							+ " updated.");
				} catch (MetadataAccessException e) {
					// Go ahead and insert a new ProcessRun
					logger.debug("disposePRMetadata(): Exception while attempting update(pr), message = "
							+ e.getMessage());
					logger.debug("disposePRMetadata(): Assuming that ProcessRun not yet in database, calling insert(pr)");
					try {
						id = processRunAccess.insert(pr);
						System.out.println("done. New ProcessRun id = " + id
								+ " inserted.");
					} catch (MetadataAccessException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			} else {
				try {
					// Insert the ProcessRun
					System.out.println("\nSubmitting metadata to SSDS...");
					Long id = processRunAccess.insert(pr);
					System.out.println("done. ProcessRun id = " + id);
				} catch (MetadataAccessException e) {
					// TODO mpm Auto-generated catch block
					e.printStackTrace();
				}
			}
		} // End if (submitToSSDS)
	}

	/**
	 * Create an ObjectBuilder from the XML that is in strUrl
	 * 
	 * @param strUrl
	 * @return
	 */
	private static ObjectBuilder objectBuilderFromUrl(String strUrl) {
		URL url = null;
		logger.debug("XML file name = " + strUrl);
		try {
			url = new URL(strUrl);
		} catch (MalformedURLException e1) {
			if (verbose) {
				System.err.println("Failed to construct a URL from strUrl ="
						+ strUrl);
				System.err
						.println("Trying to read it as a file and convert to URL...");
			}
			File file = new File(strUrl);
			try {
				url = file.toURL();
			} catch (MalformedURLException e3) {
				System.err.println("Failed to construct a URL from file ="
						+ strUrl);
				e3.printStackTrace();
				System.exit(1);
			}
		}

		if (verbose) {
			System.err.println("url.toString() = " + url.toString());
			System.err
					.println("url.toExternalForm() = " + url.toExternalForm());
		}

		ObjectBuilder ob = new ObjectBuilder(url);
		if (verbose) {
			System.err.println("Trying to unmarshal with validation...");
		}
		try {
			ob.unmarshal(true);
		} catch (ValidityException vE) {
			System.out.println("Cause = " + vE.getCause());
			System.out.println("Error at line number " + vE.getLineNumber());
			vE.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ob;
	}

	/**
	 * Loop through the files and generate the SSDS DataContainers for each of
	 * them.
	 * 
	 * @param files
	 * @param pr
	 * @param m
	 */
	private static DataContainer addFiles(Collection files, DataProducer dp,
			Method m) throws DODSException {
		boolean accept_deflate = true;
		DataContainer df = null;
		Iterator it = files.iterator();
		while (it.hasNext()) {
			String nextURL = (String) it.next();
			if (!"".equals(nextURL)) {
				if (verbose)
					System.err.println("Fetching: " + nextURL);
				DConnect url = null;
				try {
					url = new DConnect(nextURL, accept_deflate);
				} catch (java.io.FileNotFoundException e) {
					System.err.println(nextURL
							+ " is neither a valid URL nor a filename.");
					System.exit(1);
				}

				if (url.isLocal()) {
					if (verbose)
						System.err
								.println("Assuming that the argument "
										+ nextURL
										+ " is a file\n"
										+ "that contains a DODS data object; decoding.");
				}

				// Add input files to the ProcessRun
				try {
					df = genSSDSDataContainer(url);
				} catch (DODSException e) {
					// TODO mpm Auto-generated catch block
					e.printStackTrace();
				} catch (MetadataException e) {
					// TODO mpm Auto-generated catch block
					e.printStackTrace();
				}

				Object arglist[] = new Object[1];
				arglist[0] = df;
				// System.err.println("Using method = " + m.getName());
				try {
					// m is addInput() or addOutput()
					m.invoke(dp, arglist);
				} catch (IllegalArgumentException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IllegalAccessException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (InvocationTargetException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		} // End while (it.hasNext())

		// Return last DataContainer built. (Used by calling program to add
		// Resources)
		return df;

	} // End addFiles()

	/**
	 * This is where the guts of parsring the metadata from the DODS URL
	 * happens. Pull off the COARDS compliant variable attributes and set them
	 * in SSDS's RecordVariable.
	 * 
	 * @param url
	 * @return an SSDS DataContainer with populated RecordDescription and
	 *         RecordVariables
	 * @throws MetadataException
	 */
	private static DataContainer genSSDSDataContainer(DConnect url)
			throws DODSException, MetadataException {

		DataContainer dataFile = new DataContainer();
		RecordVariable rv;
		String title = "";

		try {
			dataFile.setDodsUrl(new URL(url.URL()));
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (MetadataException e) {
			// TODO mpm Auto-generated catch block
			e.printStackTrace();
		}
		RecordDescription rd = new RecordDescription();
		// Make it not parseable so that Ruminate doesn't kick
		// off multigeneratenetcdf.
		rd.setParseable(new Boolean(false));

		try {

			/*
			 * Pull off all the RecordVariables by getting the DGrid variable
			 * attribute information. Set DataContainer's description from the
			 * title string in NC_GLOBAL.
			 */

			DDS dds = url.getDDS();
			DAS das = url.getDAS();

			if (verbose) {
				System.err
						.println("dds.numVariables() = " + dds.numVariables());
			}

			variablesFromDDS(rd, dds, das);

			title = titleFromDAS(das);

		} catch (DODSException e) {
			logger.error("Failed to get DODS URL: " + url.URL().toString());
			logger.error(e);
			throw e;
		} catch (java.io.FileNotFoundException e) {
			System.err.println(e);
			System.exit(1);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MetadataException e) {
			// TODO mpm Auto-generated catch block
			e.printStackTrace();
		}

		if (verbose)
			System.err.println("Adding rd = " + rd.toString() + " to dataFile");
		dataFile.setRecordDescription(rd);
		dataFile.setDodsAccessible(new Boolean(true));
		try {
			dataFile.setDescription(title);
		} catch (MetadataException e) {
			// TODO mpm Auto-generated catch block
			e.printStackTrace();
		}

		// Get file name from url
		String strUrl = url.URL().toString();
		String shortName = strUrl.substring(strUrl.lastIndexOf('/') + 1,
				strUrl.length());
		if (verbose) {
			System.err.println("Setting DataContainer name to " + shortName);
		}
		dataFile.setName(shortName);

		return dataFile;

	} // End genSSDSDataContainer()

	/**
	 * Pull off all the RecordVariables by getting the DGrid variable attribute
	 * information.
	 * 
	 * @param rd
	 * @param dds
	 * @param das
	 * @throws NoSuchAttributeException
	 * @throws MetadataException
	 */
	private static void variablesFromDDS(RecordDescription rd, DDS dds, DAS das)
			throws NoSuchAttributeException, MetadataException {
		RecordVariable rv;
		Enumeration ddsEnum = dds.getVariables();
		while (ddsEnum.hasMoreElements()) {
			Object obj = ddsEnum.nextElement();
			if (verbose) {
				System.err.println("obj.toString() = " + obj.toString());
			}

			if (obj instanceof DGrid) {
				DGrid dg = (DGrid) obj;
				if (verbose) {
					System.err.println("dg.getName() = " + dg.getName());
					System.err
							.println("dg.getTypeName() = " + dg.getTypeName());
				}

				/*
				 * Attribute att = dg.getAttribute(); AttributeTable at =
				 * das.getAttribute(dg.getName()).getContainer();
				 */
				AttributeTable at = das.getAttributeTable(dg.getName());
				Enumeration varAttEnum = at.getNames();

				// New varible
				rv = new RecordVariable();
				String str = dg.getName();
				rv.setName(str);

				// Loop over attributes of this variable
				while (varAttEnum.hasMoreElements()) {
					Attribute varAtt = at.getAttribute(varAttEnum.nextElement()
							.toString());
					String name = varAtt.getName();
					String val = varAtt.getValueAt(0);

					// Remove any leading and trailing quotes
					int sIndx = 0;
					int eIndx = val.length() - 1;
					if ('"' == val.charAt(sIndx)) {
						sIndx = sIndx + 1;
					}
					if (!('"' == val.charAt(eIndx))) {
						eIndx = eIndx + 1;
					}
					val = val.substring(sIndx, eIndx);

					if (verbose) {
						System.err.println("varAtt.getType= "
								+ varAtt.getTypeString());
						System.err.print("name = " + name);
						System.err.println("  val = " + val);
						System.err.println("  sIndx = " + sIndx + "  eIndx = "
								+ eIndx);
					}

					// Assign SSDS important settings

					// Check for COARDS attributes and set them in
					// RecordVariable
					if ("units".equalsIgnoreCase(name)) {
						rv.setUnits(val);
					}
					if ("long_name".equalsIgnoreCase(name)) {
						rv.setLongName(val);
					}
					if ("missing_value".equalsIgnoreCase(name)) {
						rv.setMissingValue(val);
					}
					if ("Valid_min".equalsIgnoreCase(name)) {
						rv.setValidMin(val);
					}
					if ("Valid_max".equalsIgnoreCase(name)) {
						rv.setValidMax(val);
					}

				} // End if (obj instanceof DGrid)

				// Add this variable to the RecordDescription
				rd.addRecordVariable(rv);
				rv = null;

			} // End if (obj instanceof DGrid)

		} // End while (ddsEnum.hasMoreElements())
	}

	private static String titleFromDAS(DAS das) throws NoSuchAttributeException {
		// Check for Global Attribute and pull off title
		String title = "";
		AttributeTable at2 = das.getAttributeTable("NC_GLOBAL");
		Enumeration globalAttEnum = at2.getNames();

		// Loop over the global attributes & find title
		while (globalAttEnum.hasMoreElements()) {
			Attribute globalAtt = at2.getAttribute(globalAttEnum.nextElement()
					.toString());
			String name = globalAtt.getName();
			String val = globalAtt.getValueAt(0);

			if ("title".equalsIgnoreCase(name)) {
				// Remove any leading and trailing quotes
				int sIndx = 0;
				int eIndx = val.length() - 1;
				if ('"' == val.charAt(sIndx)) {
					sIndx = sIndx + 1;
				}
				if (!('"' == val.charAt(eIndx))) {
					eIndx = eIndx + 1;
				}
				title = val.substring(sIndx, eIndx);
			}
		}
		return title;
	}

	protected static EJBHome getHome(String str, String providerUrl) {

		EJBHome home = null;
		try {
			if (providerUrl != null) {
				Hashtable ht = new Hashtable();
				ht.put("java.naming.provider.url", providerUrl);
				jndiContext = new InitialContext(ht);
			} else {
				jndiContext = new InitialContext();
			}
		} catch (NamingException nex) {
			System.err
					.println("Could not get initial context, naming exception: "
							+ nex.getMessage());

		}

		try {
			home = (EJBHome) jndiContext.lookup(str);
		} catch (NamingException e) {
			System.err
					.println("NamingException: Could not get the SSDS services for str = "
							+ str + " message = " + e.getMessage());
			e.printStackTrace();

		}
		return home;
	}
}