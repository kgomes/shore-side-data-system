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
package moos.ssds.metadata
{
	import flash.utils.IDataInput;
	import flash.utils.IDataOutput;
	import flash.utils.IExternalizable;
	
	import mx.collections.ArrayCollection;

	[RemoteClass(alias="moos.ssds.metadata.DataProducer")]
	public class DataProducer implements IExternalizable
	{
				
		public function DataProducer() {
			
		}
		
		// All the matching properites
		public var id:Number;
		public var name:String;
		public var description:String;
		public var dataProducerType:String;
		public var startDate:Date;
		public var endDate:Date;
		public var role:String;
		public var nominalLatitude:Number;
		public var nominalLatitudeAccuracy:Number;
		public var nominalLongitude:Number;
		public var nominalLongitudeAccuracy:Number;
		public var nominalDepth:Number;
		public var nominalDepthAccuracy:Number;
		public var nominalBenthicAltitude:Number;
		public var nominalBenthicAltitudeAccuracy:Number;
		public var xoffset:Number;
		public var yoffset:Number;
		public var zoffset:Number;
		public var orientationDescription:String;
		public var x3DOrientationText:String;
		public var hostname:String;
		public var icon:Class;
		public var parentDataProducer:DataProducer;
		public var childDataProducers:ArrayCollection;
		
		[Embed(source="/images/tree/tree_CurrentDeployment.gif")]
		private var openDeploymentIcon:Class;
		
		[Embed(source="/images/tree/tree_ClosedDeployment.gif")]
		private var closedDeploymentIcon:Class;
		
		public function readExternal(input:IDataInput):void
		{
			id = input.readObject() as Number;
			name = input.readObject() as String;
			description = input.readObject() as String;
			dataProducerType = input.readObject() as String;
			startDate = input.readObject() as Date;
			endDate = input.readObject() as Date;
			if (endDate == null) {
				icon = openDeploymentIcon;
			} else {
				icon = closedDeploymentIcon;
			}
			role = input.readObject() as String;
			nominalLatitude = input.readObject() as Number;
			nominalLatitudeAccuracy = input.readObject() as Number;
			nominalLongitude = input.readObject() as Number;
			nominalLongitudeAccuracy = input.readObject() as Number;
			nominalDepth = input.readObject() as Number;
			nominalDepthAccuracy = input.readObject() as Number;
			nominalBenthicAltitude = input.readObject() as Number;
			nominalBenthicAltitudeAccuracy = input.readObject() as Number;
			xoffset = input.readObject() as Number;
			yoffset = input.readObject() as Number;
			zoffset = input.readObject() as Number;
			orientationDescription = input.readObject() as String;
			x3DOrientationText = input.readObject() as String;
			hostname = input.readObject() as String;
		}

		public function writeExternal(output:IDataOutput):void
		{
			output.writeObject(id);
			output.writeObject(name);
			output.writeObject(description);
			output.writeObject(dataProducerType);
			output.writeObject(startDate);
			output.writeObject(endDate);
			output.writeObject(role);
			output.writeObject(nominalLatitude);
			output.writeObject(nominalLatitudeAccuracy);
			output.writeObject(nominalLongitude);
			output.writeObject(nominalLongitudeAccuracy);
			output.writeObject(nominalDepth);
			output.writeObject(nominalDepthAccuracy);
			output.writeObject(nominalBenthicAltitude);
			output.writeObject(nominalBenthicAltitudeAccuracy);
			output.writeObject(xoffset);
			output.writeObject(yoffset);
			output.writeObject(zoffset);
			output.writeObject(orientationDescription);
			output.writeObject(x3DOrientationText);
			output.writeObject(hostname);
		}
		
	}
}