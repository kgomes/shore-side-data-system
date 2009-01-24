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
package moos.ssds.data.util;

/**
 * <p>
 * This class represents exceptions that occur in data access
 * </p>
 * <hr>
 * 
 * @stereotype exception
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.1 $
 */
public class DataException extends Exception {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 1L;

    /**
     * The constructor that takes in a message
     * 
     * @param message
     */
    public DataException(String message) {
        super(message);
    }
}
