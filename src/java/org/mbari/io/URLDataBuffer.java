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

import java.net.URL;
import java.io.BufferedInputStream;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * <p>reads the contents of a URL into an in-memory byte buffer</p><hr>
 *
 * @author  : $Author: kgomes $
 * @version : $Revision: 1.1 $
 * @testcase test.org.mbari.io.TestURLDataBuffer
 *
 */
public class URLDataBuffer {

	public static byte[] byteArray(URL url) {
		ArrayList buffer = null;
		try {
			// Read the data from the URL
			BufferedInputStream in = new BufferedInputStream(url.openStream());
			StringBuffer sb = new StringBuffer();
			buffer = new ArrayList();
			int value = 0;
			while ((value = in.read()) > -1) {
				buffer.add(new Byte((byte) value));
			}
			in.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		// Stuff the data into a byte buffer
		byte[] b = new byte[buffer.size()];
		Iterator iterator = buffer.iterator();
		int i = 0;
		while (iterator.hasNext()) {
			b[i] = ((Byte) iterator.next()).byteValue();
			i++;
		}
		return b;
	}
}
