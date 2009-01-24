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
package moos.ssds.data.parsers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import moos.ssds.metadata.DataContainer;

import org.apache.log4j.Logger;

/**
 * <p>
 * This class provides a <code>ParserContext</code> that can be used to parse
 * ASCII files
 * </p>
 * <hr>
 * 
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.4 $
 * @stereotype role
 */
public class AsciiFileContext extends ParserContext {

    /**
     * This is the constructor that takes in a <code>DataContainer</code> and
     * sets up the source with the metadata from that.
     * 
     * @param source
     *            is the <code>DataContainer</code> that contains the metadata
     *            necessary to parse files that contain ASCII data
     */
    public AsciiFileContext(DataContainer source) {
        setSource(source);
    }

    /**
     * @see ParserContext#hasNext()
     */
    public boolean hasNext() {
        if (this.eofFound)
            return false;
        return true;
    }

    /**
     * @see ParserContext#next()
     */
    public Object next() {
        // The Map that will eventually be returned
        Map data = null;
        // Create a ByteArrayOutputStream so we can just write out the data
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        // Read in bytes till the record terminator is found
        try {
            // Create holder for the next byte to be read
            byte[] nextByte = new byte[1];
            // Create a byte array that will hold the same amount of bytes
            // as the record terminator that is defined in the RecordDescription
            byte[] slidingBuffer = new byte[recordTerminator.getBytes().length];
            // Now keep reading
            while (!this.eofFound) {
                // Read in a byte
                int readResult = in.read(nextByte);
                // If less than zero, end of file was found so bail out
                if (readResult < 0) {
                    this.eofFound = true;
                    return null;
                }

                // Slide the bytes down and add the new one
                for (int i = 0; i < (slidingBuffer.length - 1); i++) {
                    slidingBuffer[i] = slidingBuffer[i + 1];
                }
                // Now write the new byte to the end of the sliding buffer
                slidingBuffer[slidingBuffer.length - 1] = nextByte[0];
                // Write it to the output stream
                bos.write(nextByte);
                // Check to see if record terminator was hit
                if (new String(slidingBuffer).equals(recordTerminator)) {
                    break;
                }
            }
        } catch (IOException e) {
            logger.error("IOException caught: " + e.getMessage());
        }
        // OK, so now I should have a line from the file, map it!
        try {
            data = recordParser.parse(bos.toByteArray());
        } catch (ParsingException e1) {
            appendLogtext("ParsingException caught: " + e1.getMessage());
        }
        return data;
    }

    /**
     * This method sets the <code>DataContainer</code> that will be used for
     * the metadata to parse the file and sets up the associated record parser.
     * 
     * @param source
     *            is the <code>DataContainer</code> to be used to set
     */
    public void setSource(DataContainer source) {

        // A temporary holder
        DataContainer tempDC = source;

        // Clear the log message
        logger.debug("setSource called");
        this.setLogText("");

        // First clear the EOF flag
        this.eofFound = false;

        // Now make sure the incoming DataContainer is a file
        if (!(tempDC.getDataContainerType().equals(DataContainer.TYPE_FILE))) {
            throw new IllegalArgumentException(
                "The DataContainer must be a file and does not appear to be.");
        }

        // Call the super class setSource
        super.setSource(source);

        // Setup the correct record parser
        this.setRecordParser(new AsciiRecordParser(source
            .getRecordDescription()));

        // If the record terminator is supplied, assign it, otherwise try to use
        // newline
        if ((source.getRecordDescription().getRecordTerminator() != null)
            && !(source.getRecordDescription().getRecordTerminator().equals(""))) {
            recordTerminator = source.getRecordDescription()
                .getRecordTerminator();
        } else {
            recordTerminator = "\r\n";
        }

        // TODO Right now assuming I can use a buffer reader to read the ASCII
        // file
        try {
            URL url = source.getUrl();
            in = url.openStream();
            skipHeader();
        } catch (Exception ex) {
            logger.error("Could not open input stream from URL: "
                + ex.getMessage());
            logger.error("Will set eofFound to true, so the "
                + "context will not try to read from it");
            this.eofFound = true;
        }
    }

    /**
     * TODO kgomes implement this!!! This method skips over any header section
     * that is in the InputStream
     */
    private void skipHeader() {}

    /**
     * The recordTerminator to look for when reading in files (that separates
     * the records)
     */
    private String recordTerminator = null;

    /**
     * The bufferedInput stream to use to read in the backing data file
     */
    private InputStream in = null;

    /**
     * This boolean tracks whether or not the end of the InputStream was found
     */
    private boolean eofFound = false;

    /**
     * A log4J logger
     */
    static Logger logger = Logger.getLogger(AsciiFileContext.class);
}