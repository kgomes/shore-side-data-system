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


public class Method {

    final String name;
    final String returnType;
    final Parameter[] parameters;
    boolean isStatic = false;

    Method(String methodName, Parameter[] parameters, String returnType) {
        this.name = methodName;
        this.parameters = parameters;
        this.returnType = returnType;
    }
    
    public String toString(){
        StringBuffer retVal = new StringBuffer("public ");
        retVal.append(returnType);
        retVal.append(" ");
        retVal.append(name);
        retVal.append("(");
        for(int i = 0; i < parameters.length; i++){
            retVal.append(parameters[i].toString());
            if(i < parameters.length -1){
                retVal.append(", ");
            }
        }
        retVal.append(")");
        return retVal.toString();
    }

    
    public boolean isStatic() {
        return isStatic;
    }

    
    public void setStatic(boolean isStatic) {
        this.isStatic = isStatic;
    }

    public String getMethodNameWithoutAccessorPrefix() {
        int lengthToStrip = 0;
        if(name.startsWith("get") || name.startsWith("set") ||
            name.startsWith("add")){
            lengthToStrip = 3;
        }else if(name.startsWith("is")){
            lengthToStrip = 2;
        }else if(name.startsWith("list")){
            lengthToStrip = 4;
        }
        String strippedMethodName = name.substring(lengthToStrip);
        strippedMethodName = makeFirstLetterLowerCase(strippedMethodName);
        return strippedMethodName;
    }
    
    private String makeFirstLetterLowerCase(String word){
        String firstLetter = new String(word.charAt(0) + "");
        return firstLetter.toLowerCase() + word.substring(1);
    }
}
