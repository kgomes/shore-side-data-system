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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import moos.ssds.io.util.CountInputStream;
import moos.ssds.metadata.DataContainer;
import moos.ssds.metadata.HeaderDescription;
import moos.ssds.metadata.RecordVariable;

import org.apache.log4j.Logger;

/**
 * <p>
 * This is the <code>ParseContext</code> that is used to parse binary files
 * </p>
 * <hr>
 * 
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.3 $
 * @stereotype role
 */
class BinaryFileContext extends ParserContext {

    /**
     * This is the constructor that takes in a <code>DataContainer</code> and
     * sets up all the appropriate parsers.
     * 
     * @param source
     *            is the <code>DataContainer</code> that contains the metadata
     *            that describes and points to the file to be parsed with a
     *            binary parser.
     */
    public BinaryFileContext(DataContainer source) {
        logger.debug("New BinaryFileContext being constructed: "
            + source.toStringRepresentation(","));
        setSource(source);
    }

    /**
     * @see ParserContext#hasNext()
     */
    public boolean hasNext() {
        if (this.eofFound)
            return false;
        // Check to see if the buffer length is zero
        if (bufferLength <= 0)
            return false;
        return true;
    }

    /**
     * @see ParserContext#next()
     */
    public Object next() {
        // Create a Map to hold stuff after parsing
        Map data = null;
        // Create a buffer to hold the data for parsing
        byte[] buffer = null;
        // Check for record terminated or not
        if (isRecordTerminated) {
            // TODO KJG - Need to implement the buffer read with record
            // terminated
            // should just be the same as below, but just looking for the
            // correct
            // terminator
        } else {
            // Since record is not terminated, assign the buffer to be the
            // length that was calculated from the record variables
            buffer = new byte[bufferLength];

            // Now create a local variable to track how much of that buffer
            // we still have to read from the source and set it to the total
            // length first
            int stillToRead = bufferLength;

            // Now create an index that will keep track of where we are in
            // the buffer of bytes that have been read so far
            int bufferIndex = 0;

            try {
                // Loop through and read bytes to fill up the buffer
                while (stillToRead > 0) {
                    // Create an array of bytes that is the size of what is
                    // left to fill up the total buffer
                    byte[] tempBuffer = new byte[stillToRead];

                    // First try to grab all the bytes
                    int result = in.read(tempBuffer);

                    // If the result is less than zero, the end of
                    // the stream is reached and null should be
                    // returned (and EOF flag set)
                    if (result < 0) {
                        this.eofFound = true;
                        return null;
                    } else {
                        // Copy the bytes read into the full buffer
                        // and adjust the number to still read
                        for (int j = 0; j < result; j++) {
                            buffer[bufferIndex] = tempBuffer[j];
                            bufferIndex++;
                        }
                        stillToRead = stillToRead - result;
                        if (stillToRead > 0)
                            logger.debug("Looks like though we requested "
                                + (stillToRead + result)
                                + " bytes from the stream, we only got "
                                + result + " and so we still have "
                                + stillToRead + " more bytes to read");
                    }
                }
                // Now I should have the full buffer, go ahead and parse it
                data = recordParser.parse(buffer);
            } catch (IOException e) {
                logger.error("IOException caught while trying to read and "
                    + " parse binary data: " + e.getMessage());
            } catch (ParsingException e) {
                logger
                    .error("ParsingException caught while trying to read and "
                        + " parse binary data: " + e.getMessage());
            } catch (Exception e) {
                logger.error("Exception caught while trying to read and "
                    + " parse binary data: " + e.getMessage());
            }
        }
        // Now return the Map of record variables to data
        return data;

    }

    /**
     * This method sets the <code>DataContainer</code> that has the metadata
     * and points to the file that will be parsed using a binary parser
     * 
     * @param source
     *            is the <code>DataContainer</code> that will be used as the
     *            source of data and metadata to parse individual records from
     *            the file
     */
    public void setSource(DataContainer source) {

        // A temporary holder
        DataContainer tempDC = source;

        // Clear the log message
        logger.debug("setSource called");
        this.setLogText("");

        // First clear the EOF flag
        this.eofFound = false;

        // Now make sure the incoming DataContainer is a File
        if (!(tempDC.getDataContainerType().equals(DataContainer.TYPE_FILE))) {
            throw new IllegalArgumentException(
                "The DataContainer specified to back the BinaryFileParserContext "
                    + "is not described as a file, cannot parse");
        }

        // Call setSource on super class
        super.setSource(source);

        // Create a new BinaryRecordParser to parse each record
        this.setRecordParser(new BinaryRecordParser(source
            .getRecordDescription()));

        // Check to see if the record is terminated, if it is, set the
        // flag, otherwise, calculate how big the data buffers are by
        // looking into the metadata
        String terminator = recordDescription.getRecordTerminator();
        if ((terminator != null) && (!terminator.toLowerCase().equals("none"))) {
            isRecordTerminated = true;
        } else {
            // Get the length of the buffer
            Collection variables = recordDescription.getRecordVariables();
            Iterator iterator = variables.iterator();
            RecordVariable variable = null;
            Class format = null;
            bufferLength = 0;
            while (iterator.hasNext()) {
                variable = (RecordVariable) iterator.next();
                format = (Class) typeMap.get(variable.getFormat());
                if ((variable == null) || (format == null)) {
                    // Skip variables that do not have a format
                    // TODO KJG - this will mess up parsing if there
                    // are bytes though.
                    continue;
                }
                // Based on the format type, add the number of bytes
                if (format == byte.class) {
                    bufferLength += 1;
                } else if (format == short.class) {
                    bufferLength += 2;
                } else if (format == int.class) {
                    bufferLength += 4;
                } else if (format == long.class) {
                    bufferLength += 8;
                } else if (format == float.class) {
                    bufferLength += 4;
                }
                // if no match is found use a double
                // TODO KJG - This is hackish and probably will
                // break parsing
                else {
                    bufferLength += 8;
                }
            }
            logger.debug("Based on variable types, the buffer should be "
                + bufferLength + " bytes long");
        }

        // Open the stream for reading
        try {
            URL url = source.getUrl();
            logger.debug("Going to open InputStream using URL "
                + url.toExternalForm());
            in = url.openStream();
            // bin = new BufferedInputStream(url.openStream());
            // Check to see if there is a header and skip it if necessary
            skipHeader();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method takes the information in the <code>IHeaderDescription</code>
     * of the <code>IDataContainer</code> and skips over the header before
     * starting to parse the records.
     * 
     * @throws IOException
     *             if something goes wrong
     */
    private void skipHeader() throws IOException {
        // Grab the HeaderDescription from the DataFile
        HeaderDescription headerDescription = source.getHeaderDescription();
        // If there is none, just create one for now
        if (headerDescription == null) {
            headerDescription = new HeaderDescription();
        }
        // Grab the URL of the data source that we are working with
        URL url = source.getUrl();
        // Create a CountInputStream to help with certain methods of
        // skipping header lines
        CountInputStream cin = null;

        // This is a variable to keep track of the offset into the
        // data container the header ends
        long offset = 0;

        // Check to see what ways we can find the offset into the stream
        if (headerDescription.hasByteOffset()) {
            offset = (long) headerDescription.getByteOffset();
            logger.debug("BinaryFileContext#skipHeader(): File "
                + url.toExternalForm() + "\n hasByteOffset of " + offset);
            appendLogtext("BinaryFileContext#skipHeader(): File "
                + url.toExternalForm() + "\n hasByteOffset of " + offset);
        } else if (headerDescription.hasHeaderLines()) {
            cin = new CountInputStream(
                new BufferedInputStream(url.openStream()));
            for (int i = 0; i < headerDescription.getNumHeaderLines(); i++) {
                cin.readLine();
            }
            offset = cin.bytesRead();
            cin.close();
            appendLogtext("BinaryFileContext#skipHeader(): File "
                + url.toExternalForm() + "\n hasHeaderLines offset of "
                + offset);
        } else if (headerDescription.hasCommentTags()) {
            /*
             * ASCII headers on Binary files are problematic. CountInputStream
             * is used to get the length of the Header in bytes.
             */
            cin = new CountInputStream(
                new BufferedInputStream(url.openStream()));
            boolean hasTag = true;
            String line = null;
            Collection commentTags = headerDescription
                .getCommentTagsAsStrings();
            String[] tags = new String[commentTags.size()];
            commentTags.toArray(tags);
            while (hasTag) {
                offset = cin.bytesRead();
                line = cin.readLine();
                hasTag = hasCommentTag(line, tags);
            }
            appendLogtext("BinaryFileContext#skipHeader(): File "
                + url.toExternalForm() + "\n hasCommentTags offset of" + offset);
        }
        // Try to skip the number of bytes that the offset specifies, but
        // capture the number that was actually skipped
        // long actualSkipped = bin.skip(offset);
        long actualSkipped = in.skip(offset);
        logger.debug("BinaryFileContext#skipHeader(): Skipped " + actualSkipped
            + " bytes before beginning parsing");
        appendLogtext("BinaryFileContext#skipHeader(): Skipped "
            + actualSkipped + " bytes before beginning parsing");
        long offset2 = offset - actualSkipped;
        while (offset2 > 0) {
            logger.debug("BinaryFileContext#skipHeader(): *** trying to skip "
                + offset2 + " more bytes...");
            appendLogtext("BinaryFileContext#skipHeader(): *** trying to skip "
                + offset2 + " more bytes...");
            // long actualSkipped2 = bin.skip(offset2);
            long actualSkipped2 = in.skip(offset2);
            logger.debug("BinaryFileContext#skipHeader(): Again, skipped "
                + actualSkipped2 + " bytes before beginning parsing");
            appendLogtext("BinaryFileContext#skipHeader(): Again, skipped "
                + actualSkipped2 + " bytes before beginning parsing");
            offset2 = offset2 - actualSkipped2;
        }

    }

    /**
     * Checks to see if the supplied string starts with one of a list of comment
     * tags
     * 
     * @param line
     *            A string that may start with a comment tag
     * @param commentTags
     *            A String array that contains a list of possible commentTags
     *            (such as '#', 'REM', or '//')
     */
    private boolean hasCommentTag(String line, String[] commentTags) {
        boolean hasTag = false;
        if (commentTags != null && line != null) {
            for (int i = 0; i < commentTags.length; i++) {
                if (line.startsWith(commentTags[i])) {
                    hasTag = true;
                    break;
                }
            }
        }
        return hasTag;
    }

    /**
     * This is the InputStream that will be used to read bytes from
     */
    protected InputStream in = null;

    /**
     * This is a boolean to track whether or not the records are terminated
     */
    protected boolean isRecordTerminated;

    /**
     * This is the length of the buffer of data to be read that should
     * constitute one record
     */
    protected int bufferLength = 0;

    /**
     * This boolean tracks whether or not the end of the InputStream was found
     */
    private boolean eofFound = false;

    /**
     * A log4J Logger
     */
    static Logger logger = Logger.getLogger(BinaryFileContext.class);

}