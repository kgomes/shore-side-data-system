package org.mbari.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Check if a <tt>URL</tt> or <tt>File</tt> is in a binary or 
 * human readable format. 
 * 
 * @author achase
 * 
 */
public class IsBinary {
    //percentage of ISO Control characters that can be seen before
    //deciding a file is binary
    private static final double ISOControlLimit = .3;

    private static final int BUFFER_SIZE = 1024;

    /**
     * Examine a chunk of data from the url and determine whether the
     * data is in binary or some human readable format. 
     * Returns false if an <tt>IOException</tt> occurs.
     * 
     * @param url The url to examine
     * @return A true value if the url is binary, false otherwise (including
     * error conditions).
     */
    public static boolean isBinary(URL url) {
        int controlCount = 0;
        int locationInStream = 0;
        int numBytesRead = 0;
        try {
            InputStream stream = url.openStream();
            byte[] buffer = new byte[BUFFER_SIZE];
            while (true) {

                numBytesRead =
                    stream.read(buffer, locationInStream, buffer.length);
                locationInStream += numBytesRead;

                char currentChar;
                int numberOfSpaces = 0;

                for (int i = 0; i < numBytesRead; i++) {
                    currentChar = (char) buffer[i];
                    if (Character.isWhitespace(currentChar)) {
                        numberOfSpaces++;
                    } else if (!Character.isLetterOrDigit(currentChar)) {
                        controlCount++;
                    }
                }
                //If all that has been seen is a bunch of white space,
                //refill the buffer and go again.
                if (numberOfSpaces < (int) (numBytesRead / 2)) {
                    break;
                }
                else{
                    controlCount = 0;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        if (controlCount > (int)(numBytesRead*ISOControlLimit)) {
            return true;
        } else {
            return false;
        }

    }

}
