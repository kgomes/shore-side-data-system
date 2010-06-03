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
package moos.ssds.web
{
	import moos.ssds.metadata.DataProducer;
	
	import mx.collections.ArrayCollection;
	
	public class MetadataHelper
	{
		public function MetadataHelper()
		{
		}

		public static function getPropertyArrayCollection(metadataObject:Object):ArrayCollection {
			// The ArrayCollection to return
			var acToReturn:ArrayCollection = null;
			
			// If the object is a DataProducer
			if (metadataObject is DataProducer) {
				// Cast to a DataProducer				
				var dp:DataProducer = metadataObject as DataProducer;

				// Use the properties to create an array collection
				var array:Array = new Array(
					{"Attribute": "ID", "Value": dp.id},
					{"Attribute": "Name", "Value": dp.name},
					{"Attribute": "Description", "Value": dp.description},
					{"Attribute": "Type", "Value": dp.dataProducerType},
					{"Attribute": "Start Date", "Value": dp.startDate},
					{"Attribute": "End Date", "Value": dp.endDate},
					{"Attribute": "Role", "Value": dp.role},
					{"Attribute": "Nominal Latitude", "Value": dp.nominalLatitude},
					{"Attribute": "Nominal Latitude Accuracy", "Value": dp.nominalLatitudeAccuracy},
					{"Attribute": "Nominal Longitude", "Value": dp.nominalLongitude},
					{"Attribute": "Nominal Longitude Accuracy", "Value": dp.nominalLongitudeAccuracy},
					{"Attribute": "Nominal Depth", "Value": dp.nominalDepth},
					{"Attribute": "Nominal Depth Accuracy", "Value": dp.nominalDepthAccuracy},
					{"Attribute": "Nominal Benthic Altitude", "Value": dp.nominalBenthicAltitude},
					{"Attribute": "Nominal Benthic Altitude Accuracy", "Value": dp.nominalBenthicAltitudeAccuracy},
					{"Attribute": "X Offset", "Value": dp.xoffset},
					{"Attribute": "Y Offset", "Value": dp.yoffset},
					{"Attribute": "Z Offset", "Value": dp.zoffset},
					{"Attribute": "Orientation Description", "Value": dp.orientationDescription},
					{"Attribute": "X3D Orienation Text", "Value": dp.x3DOrientationText},
					{"Attribute": "HostName", "Value": dp.hostName}
					);
				acToReturn = new ArrayCollection(array);
			}
			// If the object is a DataProducer
			return acToReturn;
		}
	}
}