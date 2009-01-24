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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import javax.activation.URLDataSource;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import moos.ssds.metadata.DataContainer;

/**
 * <p>
 * Context for parsing Multipart mime-type files.
 * </p>
 * <hr>
 * 
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.1 $
 * @stereotype role
 */
public class MultipartMimeFileContext extends BinaryFileContext {

    /**
     * This is the constructor that takes in a <code>IDataFile</code> that has
     * the metadata to tell how to parse the file that is backing the
     * <code>IDataFile</code>
     * 
     * @param source
     *            is the <code>IDataFile</code> that has the metadata that
     *            will tell how to parse the data file
     */
    public MultipartMimeFileContext(DataContainer source) {
        super(source);
    }

    /**
     * Override BinaryFileContext's setSource() to get Multipart Mime type file
     * and skip the header in a more robust fashion.
     * 
     * @param source
     *            is the <code>IDataFile</code> that contains the metadata
     *            that will help in figuring out how to parse the file correctly
     */
    public void setSource(DataContainer source) {
        // Call the BinaryFileContext set source first
        super.setSource(source);
        // Get the record parser
        // TODO KJG - This is already called in the super set source,
        // check with Mike
        // this.setRecordParser(
        // new BinaryRecordParser(source.getRecordDescription()));

        // Need to determine the size of the record
        // TODO KJG - This is already called in the super set source,
        // check with Mike
        // String terminator = recordDescription.getRecordTerminator();
        // if ((terminator != null)
        // && (!terminator.toLowerCase().equals("none"))) {
        // isRecordTerminated = true;
        // } else {
        // // Get the length of the buffer
        // Collection variables = recordDescription.listRecordVariables();
        // Iterator iterator = variables.iterator();
        // IRecordVariable variable = null;
        // Class format = null;
        // bufferLength = 0;
        // while (iterator.hasNext()) {
        // variable = (IRecordVariable) iterator.next();
        // format = (Class) typeMap.get(variable.getFormat());
        // if ((variable == null) || (format == null)) {
        // // Skip variables that do not have a format
        // continue;
        // }
        //
        // if (format == byte.class) {
        // bufferLength += 1;
        // } else if (format == short.class) {
        // bufferLength += 2;
        // } else if (format == int.class) {
        // bufferLength += 4;
        // } else if (format == long.class) {
        // bufferLength += 8;
        // } else if (format == float.class) {
        // bufferLength += 4;
        // }
        // // if no match is found use a double
        // else {
        // bufferLength += 8;
        // }
        // }
        // }

        // Open the stream for reading
        InputStream is = null;
        try {
            URL url = source.getUrl();

            URLDataSource uds = new URLDataSource(url);
            is = uds.getInputStream();

            /*
             * Debug output String input = null; while (reader.ready()) { input =
             * reader.readLine(); System.out.println(input); }
             */
        } catch (Exception e) {
            e.printStackTrace();
        }

        // mailSession works for reading the parts
        Session mailSession = Session.getDefaultInstance(new Properties());

        MimeMessage mimeMsg = null;
        MimeMultipart mp = null;
        try {
            mimeMsg = new MimeMessage(mailSession, is);
            // /mimeMsg.writeTo(System.out);
            mp = (MimeMultipart) mimeMsg.getContent();
        } catch (IOException e3) {
            // TODO Auto-generated catch block
            e3.printStackTrace();
        } catch (MessagingException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        }

        /**
         * Loop through all the parts and dispose of each one appropriately. For
         * now just skip the text part and set up a buffered unput stream for
         * the binary part.
         */

        try {
            int count = mp.getCount();
            for (int i = 0; i < count; i++) {

                MimeBodyPart mbp = (MimeBodyPart) mp.getBodyPart(i);
                if ("text/plain".equals(mbp.getContentType())) {
                    // Skip
                } else if ("application/octet-stream".equals(mbp
                    .getContentType())) {
                    // Set InputStream for superclass to just this binary part
                    in = mbp.getInputStream();
                } else {
                    // skip
                }
            }
        } catch (MessagingException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

    }
}
