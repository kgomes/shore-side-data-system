package test.moos.ssds.metadata.util;

import java.net.URL;
import java.util.Collection;
import java.util.Iterator;

import moos.ssds.metadata.util.ObjectBuilder;
import moos.ssds.metadata.util.XmlBuilder;
import nu.xom.Builder;
import nu.xom.Document;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.DifferenceListener;
import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;

public class TestObjectBuilder extends XMLTestCase {

    /**
     * Constructs a test case with the given name.
     */
    public TestObjectBuilder(String name) {
        super(name);
    }

    /**
     * Sets up the fixture, for example, open a network connection. This method
     * is called before a test is executed.
     */
    protected void setUp() {
        // Configure the logger
        BasicConfigurator.configure();
        logger.setLevel(Level.ALL);
        logger.addAppender(new ConsoleAppender(new PatternLayout(
            "%d %-5p [%c %M %L] %m%n")));

        logger.debug("Setting up the test");
        XMLUnit.setIgnoreWhitespace(true);

        // Get the XML file
        try {
            url = this.getClass().getResource("TestObjectBuilder.xml");
        } catch (RuntimeException e1) {
            logger.error("Could not get the xml file to read: "
                + e1.getMessage());
            e1.printStackTrace();
        }
        logger.debug("The url to the XML file is : " + url);
        // Write your code here
        try {
            objectBuilder = new ObjectBuilder(url);
            objectBuilder.unmarshal(false);
            allObjects = objectBuilder.listAll();

        } catch (Throwable e) {
            logger.error("Failure in object builder somewhere: "
                + e.getMessage());
        }
        logger.debug("Setup complete...");
    }

    /**
     * Tears down the fixture, for example, close a network connection. This
     * method is called after a test is executed.
     * 
     * @throws Exception
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        // Write your code here
    }

    /**
     * Test the size of the collection
     */
    public void testCollectionSize() {
        logger.debug("testCollectionSize running");
        assertEquals(1, allObjects.size());
        logger.debug("testCollectionSize complete");
    }

    public void testXmlOutputAgainstInput() throws Exception {
        XmlBuilder xmlBuilder = new XmlBuilder();
        for (Iterator iter = allObjects.iterator(); iter.hasNext();) {
            xmlBuilder.add(iter.next());
        }
        xmlBuilder.marshal();

        Document documentFromObject = xmlBuilder.getDocument();
        logger.debug(xmlBuilder.toFormattedXML());
        URL testUrl = this.getClass().getResource("TestObjectBuilder.xml");
        // make a new copy of the original document to use for comparison
        Document originalDocument = new Builder(false).build(testUrl
            .toExternalForm());
        // TODO achase 20040922: I'm overriding the difference listener here
        // because
        // the xml output by XmlBuilder does not maintain sequence. If it is not
        // overridden, the Diff will quit as soon as it hits the first
        // mis-sequenced
        // element.
        Diff diff = new Diff(originalDocument.toXML(), documentFromObject
            .toXML()) {

            DifferenceListener diffListener = new SansSuperficialDifferenceListener();
            {
                overrideDifferenceListener(diffListener);
            }
        };
        assertTrue(diff.similar());
    }

    // The local objectBuilder object
    private ObjectBuilder objectBuilder = null;

    // The URL of the file to use to test
    private URL url = null;

    // A Collection of all objects in the object builder
    private Collection allObjects = null;

    /**
     * A log4j logger
     */
    static Logger logger = Logger.getLogger(TestObjectBuilder.class);

}