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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * <p><!--Insert summary here--></p><hr>
 *
 * @author  : $Author: mccann $
 * @version : $Revision: 1.1 $
 * @stereotype thing
 *
 */
public class FileParser {
	
	String file = null;
	
	public FileParser(String file) {
		this.file = file;
	}

	/**
	 * @return Collection strings that are separated by new-lines in the file
	 */
	public Collection getLines() {
		Collection lines = new ArrayList();
		BufferedReader d = null;

		try {
			d = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String line;
		try {
			while ((line = d.readLine()) != null) {
				//System.err.println(line);
				lines.add(line);
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return lines;

	}

	/**
	 * @return a Hash of name value pairs that are in the file as name=value pairs
	 */
	public HashMap getNameValues() {
		// TODO Auto-generated method stub
		return null;
	}

}
