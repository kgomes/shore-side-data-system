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
package moos.ssds.metadata;

import java.io.Serializable;

import moos.ssds.metadata.util.MetadataException;

/**
 * This interface simply defines a contract of what a class has to provide to be
 * &quot;described&quot;.
 * <hr>
 * 
 * @stereotype interface
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.2 $
 */
public interface IDescription extends Serializable {

    /**
     * This method returns the name of the object
     * 
     * @return is the name of the object
     */
    String getName();

    /**
     * This method sets the name of the object
     * 
     * @param name
     *            is the what the object's name will be assigned to
     * @throws MetadataException
     *             if the name is too long
     */
    void setName(String name) throws MetadataException;

    /**
     * This method gets the description associated with the object
     * 
     * @return is the objects description
     */
    String getDescription();

    /**
     * This method sets the description associated with the object. I must be
     * less than 255 characters
     * 
     * @param description
     *            is the desription to set
     * @throws MetadataException
     *             if the length of the incoming description is over 255
     *             characters.
     */
    void setDescription(String description) throws MetadataException;
}
