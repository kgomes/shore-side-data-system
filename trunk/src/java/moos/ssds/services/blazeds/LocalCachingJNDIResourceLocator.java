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
package moos.ssds.services.blazeds;

import java.util.Hashtable;
import java.util.Map;

/**
 * Resource locator that locates local resources using JNDI then caches the
 * results.
 */
public class LocalCachingJNDIResourceLocator
      extends LocalJNDIResourceLocator
{
   private Map< String, Object > cache = new Hashtable< String, Object >();

   /**
    * @see com.adobe.ac.ejb.IResourceLocator#locate( java.lang.String )
    */
   public Object locate(
         final String name, final Class type ) throws ResourceException
   {
      Object res = cache.get( name );
      if ( res == null )
      {
         res = super.locate( name );

         cache.put(
               name, res );
      }

      return res;
   }

}