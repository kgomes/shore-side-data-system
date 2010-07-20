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

import moos.ssds.metadata.util.MetadataFactory;

import org.apache.log4j.Logger;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceConstants;

/**
 * @author achase
 */
public class LoggingDifferenceListener
    extends
        SansSuperficialDifferenceListener {

    /**
     * This is a Log4JLogger that is used to log information to
     */
    static Logger logger = Logger.getLogger(MetadataFactory.class);

    /*
     * (non-Javadoc)
     * 
     * @see org.custommonkey.xmlunit.DifferenceListener#differenceFound(org.custommonkey.xmlunit.Difference)
     */
    public int differenceFound(Difference difference) {
        // Begin check for real differences
        if (difference.getId() == DifferenceConstants.ATTR_NAME_NOT_FOUND
            .getId()) {
            logger.warn("The attribute \""
                + difference.getControlNodeDetail().getValue()
                + "\" is missing from the test document");
            return Diff.RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
        }
        if (difference.getId() == DifferenceConstants.ELEMENT_NUM_ATTRIBUTES
            .getId()) {
            logger
                .warn("Ignoring difference in number of attributes for a node. # in control = "
                    + difference.getControlNodeDetail().getValue()
                    + " # in test = "
                    + difference.getTestNodeDetail().getValue());
            return Diff.RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
        }
        if (difference.getId() == DifferenceConstants.TEXT_VALUE_ID) {
            logger.warn("Ignoring difference in text values");
            logger.warn("Control value = "
                + difference.getControlNodeDetail().getValue());
            logger.warn("Test value = "
                + difference.getTestNodeDetail().getValue());
            return Diff.RETURN_IGNORE_DIFFERENCE_NODES_SIMILAR;
        }
        return super.differenceFound(difference);
    }
}
