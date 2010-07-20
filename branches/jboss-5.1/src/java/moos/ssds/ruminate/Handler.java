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
package moos.ssds.ruminate;

import javax.jms.BytesMessage;

/**
 * <p>Interface for classes that handle (i.e process) SIAM or SSDS Packets</p><hr>
 *
 * @author  : $Author: kgomes $
 * @version : $Revision: 1.4 $
 */
public interface Handler {
    
    /**
     * 
     * @param ssdsDevicePacket This is the incoming SSDSDevicepacket
     * to 'handle'
     * TODO Implement process for different SIAM packet types
     */
	void process(BytesMessage bytesMessage);
	
}
