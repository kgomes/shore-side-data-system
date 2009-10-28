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
	// Import the necessary ActionScript classes
	import flash.events.Event;
	import flash.events.EventDispatcher;
	
	import mx.messaging.ChannelSet;
	import mx.messaging.config.ServerConfig;
	import mx.rpc.AsyncResponder;
	import mx.rpc.AsyncToken;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	import mx.rpc.remoting.mxml.RemoteObject;

	public class UserInformation extends EventDispatcher
	{
		[Event(name=INFO_REFRESHED,type="flash.events.Event")]
		[Event(name=LOGIN_SUCCEEDED,type="flash.events.Event")]
		[Event(name=LOGIN_FAILED,type="flash.events.Event")]
		[Event(name=LOGOUT_SUCCEEDED,type="flash.events.Event")]
		[Event(name=LOGOUT_FAILED,type="flash.events.Event")]
		
		// This is the single instance of this class for the application
		private static var userInfo:UserInformation;
		
		// This is the Session Remote Object that holds server state things
		private var ssdsSessionRO:RemoteObject;
			
		// Constants to determine events that can fire
		public static const INFO_REFRESHED:String = "infoRefreshed";
		public static const LOGIN_SUCCEEDED:String = "loginSucceeded";
		public static const LOGIN_FAILED:String = "loginFailed";
		public static const LOGOUT_SUCCEEDED:String = "logoutSucceeded";
		public static const LOGOUT_FAILED:String = "logoutFailed";
		
		// This is the username of the person using the application
		[Bindable]
		public var username:String = "Guest";
		
		// This is a boolean to indicate if the user is an administrator or not
		[Bindable]
		public var isAdmin:Boolean = false;
		
		// The default constructor does nothing
		public function UserInformation(){
			// Create the Remote SessionRO
			ssdsSessionRO = new RemoteObject();
			
			// Setup the SessionRO connection
			ssdsSessionRO.destination = "ssds-session";
			
			// Assign event handlers that will respond when the remote session object is called
			ssdsSessionRO.getCurrentUsername.addEventListener("result", getCurrentUsernameResultHandler);
			ssdsSessionRO.isUserInRoleSSDSAdmin.addEventListener("result", isUserInRoleSSDSAdminResultHandler);
			
			// Call the refresh
			this.refresh();
		}
		
		// The method to return the singleton
		public static function getInstance():UserInformation{
			// Check to see if the singleton instance is null
			if (userInfo == null) {
				// If none exists, create a new one
				userInfo = new UserInformation();
			}
			
			// Return the singleton
			return userInfo;
		}
		
		/**
		 * This method fires off a remote method call on the SessionRO which should 
		 * respond and get current information about the user and store here locally
		 */
		public function refresh():void {
			// Simply call the remote method to get the ssds username
			ssdsSessionRO.getCurrentUsername();
			ssdsSessionRO.isUserInRoleSSDSAdmin();
			
			// Dispatch an Event
			dispatchEvent(new Event(INFO_REFRESHED));
		}
		
		// This method attempts to take the supplied username and password and log into 
		// the SSDS server with those credentials
		public function login(username:String, password:String):void {
			// Grab the channel set from the SSDS RemoteObject destination
			var ssdsChannelSet:ChannelSet = ServerConfig.getChannelSet(ssdsSessionRO.destination);
			
			// Login and hold on to the returned AsyncToken
			var token:AsyncToken = ssdsChannelSet.login(username,password);
			
			// Add an event handler to the token
			token.addResponder(new AsyncResponder(LoginResultEventHandler,LoginFailureEventHandler));
		}
		
		// This method logs the user out of the channel to the remote session object
		public function logout():void {
			// Grab the channel set from the SSDS RemoteObject destination
			var ssdsChannelSet:ChannelSet = ServerConfig.getChannelSet(ssdsSessionRO.destination);
			
			// Logout and hold on to the returned AsyncToken
			var token:AsyncToken = ssdsChannelSet.logout();
			token.addResponder(new AsyncResponder(LogoutResultEventHandler,LogoutFailureEventHandler));
		}
		
		// The function to handle the result of a call to SessionRO.getCurrentUsername
		private function getCurrentUsernameResultHandler(event:ResultEvent):void {
			trace("getCurrentUsernameResultHandler called and handed event: " + event.toString());
			// Grab the user name from the event and assign locally
			this.username = event.result.toString();
		}

		// The function to handle the response from calls to isUserInRoleSSDSAdmin
		private function isUserInRoleSSDSAdminResultHandler(event:ResultEvent):void{
			// Grab the result and assign locally
			trace("isUserInRoleSSDSAdmin result handler returned: " + event.toString());
			this.isAdmin = event.result;	
		}
		
		// The function to handle the event resulting from a successful login
		private function LoginResultEventHandler(event:ResultEvent, token:Object=null):void {
			
			// Update the local user information
			userInfo.refresh();
			
			// Check the result and do the right thing
			switch(event.result) {
				case "success":
					dispatchEvent(new Event(LOGIN_SUCCEEDED));
					break;
				default:
					dispatchEvent(new Event(LOGIN_FAILED));
			}	
		}	
			
		// The function to handle the event resulting from a failed login
		private function LoginFailureEventHandler(event:FaultEvent, token:Object=null):void {
			// Update the local information
			userInfo.refresh();
			
			// Dispatch and event
			dispatchEvent(new Event(LOGIN_FAILED));			
		}
		
		// The function to handle the event resulting from a successful logout
		private function LogoutResultEventHandler(event:ResultEvent, token:Object=null):void {
			// Refresch the user information
			userInfo.refresh()
			
			// Refresh the user information
			switch(event.result) {
				case "success":
					dispatchEvent(new Event(LOGOUT_SUCCEEDED));
					break;
				default:
					dispatchEvent(new Event(LOGOUT_FAILED));
			}	
		}	
			
		// The function to handle the event resulting from a failed logout
		private function LogoutFailureEventHandler(event:FaultEvent, token:Object=null):void {
			// Refresh the user's information
			userInfo.refresh();	
			
			// Dispatch event
			dispatchEvent(new Event(LOGOUT_FAILED));
		}
		
		// The getter for the username
		public function getUsername():String {
			return username;
		}
		
		// The getter for the isAdmin property
		public function getIsAdmin():Boolean {
			return this.isAdmin;
		}
		
	}
}