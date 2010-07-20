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
package moos.ssds.wrappergenerator;


public class Parameter {

    final String name;
    final String type;

    Parameter(String parameterName, String parameterType) {
        this.name = parameterName;
        this.type = parameterType;
    }
    
    public String toString(){
        return type + " " + name;
    }
    
    public static String getStringOfParameters(Parameter[] parameters, String delimiter, String paramNamePrefix){
        StringBuffer buffer = new StringBuffer();
        for(int i = 0; i < parameters.length; i++){
            buffer.append(parameters[i].type + " " + paramNamePrefix + parameters[i].name);
            if(i < parameters.length -1){
                buffer.append(delimiter + " ");
            }
        }
        return buffer.toString();
    }
}
