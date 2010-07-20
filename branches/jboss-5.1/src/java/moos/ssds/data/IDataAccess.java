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
package moos.ssds.data;

import java.util.Collection;

import moos.ssds.metadata.RecordVariable;

/**
 * This is the interface that defines a class that provides access to data. The
 * interface defines the methods that need to be supported by the implementing
 * class to provide data to the client.
 */
public interface IDataAccess {

    /**
     * Return the array of <code>Object</code>s that are the data associated
     * with the <code>RecordVariable</code> name that is given.
     * 
     * @param recordVariableName
     *            is the string the is the "Name" of the variable to return data
     *            for.
     * @return an array of <code>Object</code>s that are the data for the
     *         name supplied
     */
    public abstract Object[] getData(String recordVariableName);

    /**
     * Return the array of <code>Object</code>s that are the data for the
     * record variable specified by the incoming <code>IRecordVariable</code>
     * 
     * @param recordVariable
     *            is a <code>IRecordVariable</code> that specifies which
     *            variable the client wants the data for.
     * @return an array of <code>Object</code>s that ar the data for the
     *         specified reocordVariable
     */
    public abstract Object[] getData(RecordVariable recordVariable);

    /**
     * This method returns a collection of <code>RecordVariable</code>s whose
     * data is available in this object
     * 
     * @return a collection of <code>RecordVariable</code>s
     */
    public abstract Collection getRecordVariables();
}
