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
	import com.google.maps.LatLng;
	import com.google.maps.LatLngBounds;
	import com.google.maps.Map;
	import com.google.maps.overlays.Marker;
	import com.google.maps.overlays.MarkerOptions;
	import com.google.maps.styles.FillStyle;
	import com.google.maps.styles.StrokeStyle;
	
	import moos.ssds.metadata.DataProducer;
	
	import mx.collections.ArrayCollection;
	
	/**
	 * This class provides some utilities to make working with SSDS objects in Google Maps easier.
	 */
	public class GoogleMapHelper
	{
		/**
		 * The default constructor
		 */
		public function GoogleMapHelper()
		{
		}

		/**
		 * This method takes in a Google Map object and a collection of SSDS DataProducers and
		 * plots them on the Google map and adjusts extents to fit and center all of them. Note
		 * that this particular method will filter out any DataProducers that are not Deployments
		 * and will only plot those that have at least a start date.
		 */
		public static function mapDeployments(map:Map,deployments:ArrayCollection, size:Number, shadow:Boolean):void {
			// Initialize the bounds of the map viewport
			var lowerLeftLat:Number = 0;
			var lowerLeftLon:Number = 0;
			var upperRightLat:Number = 0;
			var upperRightLon:Number = 0;
			
			// Loop over the deployments and put markers where there are lat lon defined
			for (var i:int = 0; i < deployments.length; i++) {
				// Grab the current deployment
				var currentDeployment:DataProducer = deployments[i] as DataProducer;
				
				// Make sure it is a deployment first
				if (currentDeployment.dataProducerType == 'Deployment') {
/*					// Grab the latitude
					var currentLat:Number = currentDeployment.nominalLatitude;
					// Grab the longitude
					var currentLon:Number = currentDeployment.nominalLongitude;
					// Check against extents
					if (currentLat != 0) {
						if (currentLat < lowerLeftLat || lowerLeftLat == 0)
							lowerLeftLat = currentLat;
						if (currentLat > upperRightLat || upperRightLat == 0)
							upperRightLat = currentLat;
					}
					
					// Check longitude against viewport longitudes
					if (currentLon != 0) {
						// First convert it to a positive number if negative
						var tempLon:Number = currentLon;
						if (currentLon < 0) {
							tempLon = 360 + currentLon;
						}
						if (tempLon < lowerLeftLon || lowerLeftLon == 0)
							lowerLeftLon = tempLon;
						if (tempLon > upperRightLon || upperRightLon == 0)
							upperRightLon = tempLon;
					}
					
					// If both lat and lon are not zero create marker and set view port extents
					if (currentLat != 0 && currentLon != 0) {
*/						
						// Create a LatLng point using those from the deployment
						var latLng:LatLng = new LatLng(currentDeployment.nominalLatitude,currentDeployment.nominalLongitude);
						
						// Create the object to contain the options for the marker
						var newMarkerOptions:MarkerOptions = new MarkerOptions();
						
						// Set the line stroke style 
						newMarkerOptions.strokeStyle = new StrokeStyle({color: 0x04549a});
						
						// Now if the deployment is current, color it green, otherwise grey it out
						if (currentDeployment.endDate == null) {
							newMarkerOptions.fillStyle = new FillStyle({color: 0x00ff00, alpha: 0.8});
						} else {
							newMarkerOptions.fillStyle = new FillStyle({color: 0xaaaaaa, alpha: 0.8});
						}
						
						// Set the size based on parameter passed in
						newMarkerOptions.radius = size;
						// Same with shadow
						newMarkerOptions.hasShadow = shadow;
						// Set the tool tip to the name
						newMarkerOptions.tooltip = currentDeployment.name;
						
						// Add it to the map
						map.addOverlay(new Marker(latLng, newMarkerOptions));
						
						// Set view port extents
						var newLatLngBounds:LatLngBounds = new LatLngBounds(new LatLng(lowerLeftLat,lowerLeftLon), new LatLng(upperRightLat, upperRightLon));
						map.setCenter(newLatLngBounds.getCenter());
					}
/*				}
*/
			}
		}
	}
}