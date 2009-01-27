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
	import flash.events.Event;
	import flash.events.EventDispatcher;

	public class UserInformation extends EventDispatcher
	{
		// This is the single instance of this class for the application
		private static var userInfo:UserInformation;
		
		// Constants to determine events that can fire
		public static const USERNAME_CHANGED:String = "usernameChanged";
		public static const ISADMIN_CHANGED:String = "isAdminChanged";
		
		// This is the username of the person using the application
		private var username:String;
		
		// This is a boolean to indicate if the user is an administrator or not
		private var isAdmin:Boolean;
		
		// The default constructor does nothing
		public function UserInformation(){}
		
		// The method to return the singleton
		public static function getInstance():UserInformation{
			if (userInfo == null) {
				userInfo = new UserInformation();
			}
			return userInfo;
		}
		
		// The getter for the username
		public function getUsername():String {
			return username;
		}
		
		// The setter for the username
		public function setUsername(username:String):void {
			this.username = username;
			dispatchEvent(new Event(USERNAME_CHANGED));
		}
		
		// The getter for the isAdmin property
		public function getIsAdmin():Boolean {
			return this.isAdmin;
		}
		
		// The setter for the isAdmin property
		public function setIsAdmin(isAdmin:Boolean):void {
			this.isAdmin = isAdmin;
			dispatchEvent(new Event(ISADMIN_CHANGED));
		}
	}
}