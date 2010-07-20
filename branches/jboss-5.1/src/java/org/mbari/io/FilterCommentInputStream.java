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
package org.mbari.io;

import java.io.InputStream;
import java.io.IOException;
import java.util.Arrays;

import moos.ssds.io.util.CountInputStream;

public class FilterCommentInputStream extends CountInputStream {

    public FilterCommentInputStream(InputStream in, String commentTag) {
        super(in, commentTag.getBytes().length);
        commentBytes = commentTag.getBytes();
    }



    /**
     * Reads the next byte of data from this input stream. The value
     * byte is returned as an <code>int</code> in the range
     * <code>0</code> to <code>255</code>. If no byte is available
     * because the end of the stream has been reached, the value
     * <code>-1</code> is returned. This method blocks until input data
     * is available, the end of the stream is detected, or an exception
     * is thrown.
     * <p>
     * This method
     * simply performs <code>in.read()</code> and returns the result.
     *
     * @return     the next byte of data, or <code>-1</code> if the end of the
     *             stream is reached.
     * @exception  IOException  if an I/O error occurs.
     * @see        java.io.FilterInputStream#in
     */
    public int read() throws IOException {
        int i = super.read();
        if (i > 0) {
            if (((byte) i) == commentBytes[0]) {
                // If the tag is a single character read to the end of the line
                if (commentBytes.length == 1) {
                    i = readToEndOfLine();
                }
                // For longer tags see if the whole tag is present
                else {
                    unread(i);
                    byte[] b = new byte[commentBytes.length];
                    read(b);

                    if (Arrays.equals(b, commentBytes)) {
                        // If it is a tag read to the next line
                        i = readToEndOfLine();
                    } else {
                        byte[] b2 = new byte[commentBytes.length - 1];
                        System.arraycopy(b, 1, b2, 0, b2.length);
                        unread(b);
                    }
                }

            }
        }
        return i;
    }

    private int read1() throws IOException {
        return super.read();
    }

    private int readToEndOfLine() throws IOException {
        int i = 0;
        while(true) {
            i = read1();
            if (i == 10 || i == 13) {
                i = read1();
                if (i != 10 || i != 13) {
                    unread(i);
                }
                break;
            }
        }
        i = read();
        return i;
    }




    private byte[] commentBytes; //0-255
    private long bytesRead = 0;
    private long bytesLeft = 0;
    private boolean firstRead = true;
}