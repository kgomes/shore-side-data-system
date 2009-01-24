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

import java.lang.reflect.Method;
import java.text.MessageFormat;

import flex.messaging.FactoryInstance;
import flex.messaging.FlexFactory;
import flex.messaging.config.ConfigMap;
import flex.messaging.services.ServiceException;

public class EJBFactory implements FlexFactory
{
   private static final String SOURCE = "source";
   private static final String ERROR_CODE = "EJB.Invocation";

   final IResourceLocator resourceLocator = new LocalCachingJNDIResourceLocator();

   /**
    * Initializes the component with configuration information.
    */
   public void initialize(
         final String id, final ConfigMap configMap )
   {
   }

   /**
    * This method is called when the definition of an instance that this factory
    * looks up is initialized.
    */
   public FactoryInstance createFactoryInstance(
         final String id, final ConfigMap properties )
   {
      final FactoryInstance instance = new FactoryInstance( this, id,
            properties );
      instance.setSource( properties.getPropertyAsString(
            SOURCE, instance.getId() ) );

      return instance;
   }

   /**
    * Returns the instance specified by the source and properties arguments.
    */
   public Object lookup(
         final FactoryInstance instanceInfo )
   {
      Object ejb;

      final String jndiName = instanceInfo.getSource();

      try
      {
         final Object ejbHome = resourceLocator.locate( jndiName );

         final Method method = ejbHome.getClass().getMethod(
               "create", new Class[]
               {} );

         ejb = method.invoke(
               ejbHome, new Object[]
               {} );
      }
      catch ( ResourceException e )
      {
         throw createServiceException(
               MessageFormat.format(
                     "EJB not found {0}", new Object[]
                     { jndiName } ), e );
      }
      catch ( Exception e )
      {
         throw createServiceException(
               MessageFormat.format(
                     "error creating EJB {0}", new Object[]
                     { jndiName } ), e );
      }

      return ejb;
   }

   private ServiceException createServiceException(
         final String msg, final Throwable cause )
   {
      final ServiceException e = new ServiceException();
      e.setMessage( msg );
      e.setRootCause( cause );
      e.setDetails( msg );
      e.setCode( ERROR_CODE );

      return e;
   }
}
