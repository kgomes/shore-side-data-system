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
import java.util.Collection;

/**
 * <p>
 * Interface for classes that need to reference Resources.
 * </p>
 * <hr>
 * 
 * @stereotype role
 * @author : $Author: kgomes $
 * @version : $Revision: 1.1.2.2 $
 */
public interface IResourceOwner extends Serializable {

    /**
     * This method will return a <code>Collection</code> of
     * <code>Resource</code>s that are owned by the object implemnting this
     * interface
     * 
     * @return a <code>Collection</code> of <code>Resource</code>s
     */
    Collection getResources();

    /**
     * This method adds a <code>Resource</code> to a <code>Collection</code>
     * of <code>Resource</code>s that are owned by the object implementing
     * this interface
     * 
     * @param resource
     *            the <code>Resource</code> to be added
     */
    void addResource(Resource resource);

    /**
     * This method removes a <code>Resource</code> from a
     * <code>Collection</code> of <code>Resource</code>s that are owned by
     * the object implementing this interface
     * 
     * @param resource
     *            the <code>Resource</code> to be removed
     */
    void removeResource(Resource resource);
}
