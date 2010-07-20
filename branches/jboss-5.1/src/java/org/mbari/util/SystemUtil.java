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
package org.mbari.util;

import java.net.*;
import java.util.Properties;
import java.io.BufferedInputStream;

/**
 * <p>Static methods (i.e. functions) for general system services.</p><hr>
 *
 *@author     : $Author: mccann $
 *@created    October 15, 2002
 *@version    : $Revision: 1.2 $
 * @testcase test.org.mbari.util.TestSystemUtil
 */

/*
 *  $Log: SystemUtil.java,v $
 *  Revision 1.2  2004/11/13 00:30:33  mccann
 *  Added more helpful print statement if file is not found.
 *
 *  Revision 1.1  2003/07/01 20:11:11  kgomes
 *  Adding to CVS
 *
 *  Revision 1.5  2003/05/12 22:06:17  brian
 *  Update to use ssds.properties instead of ingest.properties
 *
 *  Revision 1.4  2003/04/16 20:47:11  brian
 *  Minor bug fix
 *
 *  Revision 1.3  2003/02/25 01:50:11  brian
 *  Adding unit tests. Refactored get/setPerson to get/setContact.
 *
 *  Revision 1.2  2002/11/12 17:33:09  brian
 *  Modifications for handiling Moos test mooring data
 *
 *  Revision 1.1  2002/10/22 13:15:08  kgomes
 *  Moved down a directory level
 *
 *  Revision 1.4  2002/10/18 18:34:35  brian
 *  Fixed isue with extracting resources. Resources can now be loaded from JARs or from the Classpath in an identical manner.
 *
 *  Revision 1.3  2002/10/15 23:56:06  brian
 *  Minor modifications and updated javadoc
 *
 *  Revision 1.2  2002/10/14 22:46:13  brian
 *  Added getProperties method
 *
 *  Revision 1.1  2002/10/14 17:45:41  brian
 *  Convieince class for storing system manipulation Utilites. Currently useful for retrieving resources, such as properties files.
 *
 *  Revision 1.2  2002/07/02 21:49:41  brian
 *  updating mbari.jar distribution
 *
 *  Revision 1.2  2002/05/28 23:45:52  brian
 *  Added documentation and comments
 *
 */
public class SystemUtil {

    /**
     *  Gets the URL for the resource specified
     *
     *@param  resource  The name of the resource to be retrieved
     *@return       The URL of the resource
     */
    public static URL getURL(String resource) {
        class ClassLoaderReference1 {
            public ClassLoader getClassLoader() {
                return getClass().getClassLoader();   
            }
        }
        
        
        ClassLoaderReference1 clr = new ClassLoaderReference1();
        ClassLoader cl = clr.getClassLoader();
        return cl.getResource(resource);
    }


    /**
     *  Gets a string name to a resource. Useful
     *  for retrieving images for GUI's or properties files. For example,
     * String s = SystemUtil.getFile("images/test.gif") might return
     * s= "/home/cvs/SSDS/build/dist/ssds.jar!images/test.gif"
     * 
     *
     *@param  resource  Description of the Parameter
     *@return       The name of a resoruce
     */
    public static String getFile(String resource) {
        URL t = SystemUtil.getURL(resource);
        return SystemUtil.getURL(resource).getFile();
    }


    /**
     *  Loads a properties file. For example: 
     * Properties props = SystemUtil.getProperties("ssds.properties");
     * You do not need to specify a full path. It will look for the named file
     * on the classpath and in any jars on the classpath. 
     *
     *@param  propFileName  Name of the properties file to read.
     *@return               A properties object. null if unable to read the specified
     * file
     */
    public static Properties getProperties(String propFileName) {
        
        class ClassLoaderReference2 {
            public ClassLoader getClassLoader() {
                return getClass().getClassLoader();   
            }
        }
        ClassLoader cl = new ClassLoaderReference2().getClassLoader();
        
        Properties props = null;
        try {
            props = new Properties();
            props.load(new BufferedInputStream(cl.getResourceAsStream(propFileName)));
        } catch (Exception e) {
            System.err.println("Could not load file: " + propFileName);
            e.printStackTrace();
        }
        return props;
        
    }
    

    
}
