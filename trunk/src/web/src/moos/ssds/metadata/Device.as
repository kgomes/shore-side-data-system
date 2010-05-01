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
	
	[Bindable]
	[RemoteClass(alias="moos.ssds.metadata.Device")]
	public class Device implements IExternalizable
	{
		public var id:Number;
		public var uuid:String;
		public var name:String;
		public var description:String;
		public var mfgName:String;
		public var mfgModel:String;
		public var mfgSerialNumber:String;
		public var infoUrlList:String;

		[Embed(source="/images/tree/tree_Device.gif")]
		public var icon:Class;
		
		public function Device()
		{
		}
		
		public function readExternal(input:IDataInput):void{
			id = input.readObject() as Number;
			uuid = input.readObject() as String;
			name = input.readObject() as String;
			description = input.readObject() as String;
			mfgName = input.readObject() as String;
			mfgModel = input.readObject() as String;
			mfgSerialNumber = input.readObject() as String;
			infoUrlList = input.readObject() as String;
		}
		
		public function writeExternal(output:IDataOutput):void {
			output.writeObject(id);
			output.writeObject(uuid);
			output.writeObject(name);
			output.writeObject(description);
			output.writeObject(mfgName);
			output.writeObject(mfgModel);
			output.writeObject(mfgSerialNumber);
			output.writeObject(infoUrlList);
		}
	}
}