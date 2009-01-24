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
package moos.ssds.simulator;
import java.io.File;

public class FileInfo {
    private File file;
    private long repeatTimes;
    private long repeatInt;
    private boolean serialized;
    private long sequenceNumber;
    private long metadataSequenceNumber;
    private long parentDeviceID;
    private long recordType;
    private long instrumentID;

    public File getFile(){ return file; }

    public void setFile(File file){ this.file = file; }

    public long getRepeatTimes(){ return repeatTimes; }

    public void setRepeatTimes(long repeatTimes){ this.repeatTimes = repeatTimes; }

    public long getRepeatInt(){ return repeatInt; }

    public void setRepeatInt(long repeatInt){ this.repeatInt = repeatInt; }

    public boolean isSerialized(){ return serialized; }

    public void setSerialized(boolean serialized){ this.serialized = serialized; }

    public long getSequenceNumber(){ return sequenceNumber; }

    public void setSequenceNumber(long sequenceNumber){ this.sequenceNumber = sequenceNumber; }

    public long getMetadataSequenceNumber(){ return metadataSequenceNumber; }

    public void setMetadataSequenceNumber(long descriptionID){ this.metadataSequenceNumber = descriptionID; }

    public long getParentDeviceID(){ return parentDeviceID; }

    public void setParentDeviceID(long parentDeviceID){ this.parentDeviceID = parentDeviceID; }

    public long getRecordType(){ return recordType; }

    public void setRecordType(long variableID){ this.recordType = variableID; }

    public long getInstrumentID(){ return instrumentID; }

    public void setInstrumentID(long deviceID){ this.instrumentID = deviceID; }
    
    public String toString() {
    	return new String("InstrumentID->" + this.getInstrumentID() + "\n" +
    	                  "RecordType->" + this.getRecordType() + "\n" +
    	                  "ParentDeviceID->" + this.getParentDeviceID() + "\n" +
    	                  "MetadataSequenceNumber->" + this.getMetadataSequenceNumber() + "\n" +
    	                  "SequenceNumber->" + this.getSequenceNumber() + "\n" +
    	                  "Serialized?->" + this.isSerialized() + "\n" +
    	                  "RepeatInt->" + this.getRepeatInt() + "\n" +
    	                  "RepeatTimes->" + this.getRepeatTimes() + "\n" + 
    	                  "DataFile->" + this.getFile().getName() + "\n"); 
    }
}
