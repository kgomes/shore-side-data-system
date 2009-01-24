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
package moos.ssds.io.util;

import java.io.InputStream;
import java.io.IOException;
import java.io.PushbackInputStream;

public class CountInputStream extends PushbackInputStream {

    public CountInputStream(InputStream in) {
        this(in, 1);
    }

    public CountInputStream(InputStream in, int size) {
        super(in, size);
//        try {
//            bytesLeft = in.available();
//        }
//        catch (IOException e) {
//            e.printStackTrace();
//        }
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
        bytesRead++;
//        bytesLeft--;
        return in.read();
    }

    /**
     * Reads up to <code>len</code> bytes of data from this input stream
     * into an array of bytes. This method blocks until some input is
     * available.
     * <p>
     * This method simply performs <code>in.read(b, off, len)</code>
     * and returns the result.
     *
     * @param      b     the buffer into which the data is read.
     * @param      off   the start offset of the data.
     * @param      len   the maximum number of bytes read.
     * @return     the total number of bytes read into the buffer, or
     *             <code>-1</code> if there is no more data because the end of
     *             the stream has been reached.
     * @exception  IOException  if an I/O error occurs.
     * @see        java.io.FilterInputStream#in
     */
    public int read(byte b[], int off, int len) throws IOException {
        int i = in.read(b, off, len);
        if (i > 0) {
            bytesRead += i;
//            bytesLeft -= i;
        }
        return i;
    }

    /**
     * Skips over and discards <code>n</code> bytes of data from the
     * input stream. The <code>skip</code> method may, for a variety of
     * reasons, end up skipping over some smaller number of bytes,
     * possibly <code>0</code>. The actual number of bytes skipped is
     * returned.
     * <p>
     * This method
     * simply performs <code>in.skip(n)</code>.
     *
     * @param      n   the number of bytes to be skipped.
     * @return     the actual number of bytes skipped.
     * @exception  IOException  if an I/O error occurs.
     */
    public long skip(long n) throws IOException {
        long i = in.skip(n);
        bytesRead += i;
//        bytesLeft -= i;
        return i;
    }

    public long bytesRead() {
        return bytesRead;
    }

    /**
     * Pushes back a byte by copying it to the front of the pushback buffer.
     * After this method returns, the next byte to be read will have the value
     * <code>(byte)b</code>.
     *
     * @param      b   the <code>int</code> value whose low-order
     * 			byte is to be pushed back.
     * @exception IOException If there is not enough room in the pushback
     *			      buffer for the byte.
     */
    public void unread(int b) throws IOException {
        super.unread(b);
        bytesRead--;
//        bytesLeft++;
    }

    /**
     * Pushes back a portion of an array of bytes by copying it to the front
     * of the pushback buffer.  After this method returns, the next byte to be
     * read will have the value <code>b[off]</code>, the byte after that will
     * have the value <code>b[off+1]</code>, and so forth.
     *
     * @param b the byte array to push back.
     * @param off the start offset of the data.
     * @param len the number of bytes to push back.
     * @exception IOException If there is not enough room in the pushback
     *			      buffer for the specified number of bytes.
     * @since     JDK1.1
     */
    public void unread(byte[] b, int off, int len) throws IOException {
        super.unread(b, off, len);
        bytesRead -= len;
//        bytesLeft += len;
    }

    /**
     * Pushes back an array of bytes by copying it to the front of the
     * pushback buffer.  After this method returns, the next byte to be read
     * will have the value <code>b[0]</code>, the byte after that will have the
     * value <code>b[1]</code>, and so forth.
     *
     * @param b the byte array to push back
     * @exception IOException If there is not enough room in the pushback
     *			      buffer for the specified number of bytes.
     * @since     JDK1.1
     */
    public void unread(byte[] b) throws IOException {
        super.unread(b);
        bytesRead -= b.length;
//        bytesLeft += b.length;
    }

    /**
     * See the general contract of the <code>readLine</code>
     * method of <code>DataInput</code>.
     * <p> Bytes for this operation are read from the contained
     * input stream.
     *
     * <b>WARNING: This method does not properly convert bytes to characters. It is provided
     * only as a means to count the length of lines and is not intended for uses
     * where the contents of the lines are actually used. The implementation of
     * this method is based on the deprecated method readine() in
     * DataInputStream</b><br><br>
     * 
     * As of JDK&nbsp;1.1, the preferred way to read lines of text is via the
     * <code>BufferedReader.readLine()</code> method.  Programs that need to
     * read lines should use:
	 *
     * <blockquote><pre>
     *     BufferedReader d
     *          =&nbsp;new&nbsp;BufferedReader(new&nbsp;InputStreamReader(in));
     * </pre></blockquote>
     *
     * @return     the next line of text from this input stream.
     * @exception  IOException  if an I/O error occurs.
     * @see        java.io.BufferedReader#readLine()
     * @see        java.io.DataInputStream#readLine()
     * @see        java.io.FilterInputStream#in
     */
    public final String readLine() throws IOException {
        char[] lineBuffer = new char[128];
        char buf[] = lineBuffer;

        int room = buf.length;
        int offset = 0;
        int c;

        loop:
        while (true) {
            switch (c = read()) {
                case - 1:
                case '\n':
                    break loop;

                case '\r':
                    int c2 = read();
                    if ((c2 != '\n') && (c2 != -1)) {
                      	unread(c2);
                    }
                    break loop;

                default:
                    if (--room < 0) {
                        buf = new char[offset + 128];
                        room = buf.length - offset - 1;
                        System.arraycopy(lineBuffer, 0, buf, 0, offset);
                        lineBuffer = buf;
                    }
                    buf[offset++] = (char)c;
                    break;
            }
        }
        if ((c == -1) && (offset == 0)) {
            return null;
        }
        return String.copyValueOf(buf, 0, offset);
    }

    /** A count of the number of bytes read from the underlying stream */
    private long bytesRead = 0;
    // KJG - I commented this out because it is not
    // used for anything (the counter is decremented
    // and incremented, but that is it.  Plus the
    // available() method does not work the way you
    // think it does.  It only tracks how many bytes
    // are available that can be read without
    // blocking.  When the input stream is backed
    // by a URL, that is VERY flaky, so I am removing
    // any reliance on it.
//    private long bytesLeft = 0;
    private boolean firstRead = true;
}
