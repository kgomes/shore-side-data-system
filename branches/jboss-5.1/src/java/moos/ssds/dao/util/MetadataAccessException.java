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
package moos.ssds.dao.util;

/**
 * <p>
 * This exception is used when something goes wrong in the layer that accesses
 * the persistence layer and the model within it. It allows you to capture any
 * type of common exception and cast it into one exception (for simplicity of
 * the client)
 * </p>
 * <hr>
 * 
 * @stereotype exception
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.1 $
 */
public class MetadataAccessException extends Exception {

    /**
     * The default constructor
     */
    public MetadataAccessException() {
        super();
    }

    /**
     * This method used used to create an exception and assign the text message
     * 
     * @param message
     *            the message to assign
     */
    public MetadataAccessException(String message) {
        super(message);
    }

    /**
     * This allows you to create a <code>MetadataException</code> by handing
     * it a <code>Throwable</code>
     * 
     * @param th
     *            the <code>Throwable</code> that will be wrapped
     */
    public MetadataAccessException(Throwable th) {
        super(th);
    }

    /**
     * This allows you to create a <code>MetadataException</code> by handing
     * it a message and a <code>Throwable</code>
     * 
     * @param message
     *            the message to assign to the exception
     * @param th
     *            is the <code>Throwable</code> that will be wrapped.
     */
    public MetadataAccessException(String message, Throwable th) {
        super(message, th);
    }

    /**
     * Is the serial version ID needed to control version of the class for
     * serialization
     */
    private static final long serialVersionUID = 1L;

}
