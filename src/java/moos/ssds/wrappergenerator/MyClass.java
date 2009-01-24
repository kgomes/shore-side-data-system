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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


public class MyClass {
    final String name;
    private List classesExtended = new ArrayList();
    private List methods = new ArrayList();
    
    public MyClass(String name){
        this.name = name;
    }
    
    public synchronized void addMethod(Method method){
        methods.add(method);
    }
    
    public Method[] getMethods(){
        return (Method[])methods.toArray(new Method[0]);
    }
    
    public synchronized void addClassExtended(String className){
        classesExtended.add(className);
    }
    
    public String[] getClassesExtended(){
        return (String[])classesExtended.toArray(new String[0]);
    }
    
    public String toString(){
        StringBuffer buffer = new StringBuffer();
        buffer.append(name);
        buffer.append(" extends ");
        for(Iterator iter = classesExtended.iterator(); iter.hasNext(); ){
            String extension = (String)iter.next();
            buffer.append(extension);
            if(iter.hasNext()){ buffer.append(", ");}
        }
        buffer.append("\n");
        for(Iterator iter = methods.iterator(); iter.hasNext(); ){
            Method method = (Method)iter.next();
            buffer.append(method.toString());
            if(iter.hasNext()){buffer.append("\n");}
        }
        return buffer.toString();
    }

    /**
     * This method will remove the duplicate methods from the class. The purpose
     * is to remove the duplicate methods which have been overloaded to except
     * different types of the same parameter (for instance a method that excepts
     * a number as a java.lang.String, java.lang.Long, and the primitive long).
     * The method which takes a string will be the one that is retained.
     *
     */
    public void removeDuplicateMethods() {
        //keep a list of these methods, can't remove them on the fly because of
        //concurrent modification issues when using Iterator
        List methodsToRemove = new ArrayList();
        HashMap methodMap = new HashMap();
        for(Iterator iter = methods.iterator(); iter.hasNext(); ){
            Method method = (Method)iter.next();
            if(methodMap.containsKey(method.name)){
                if(methodsAreEquivalent(method, (Method)methodMap.get(method.name))){
                    Method methodToKeep = findBestEquivalentMethod(method, (Method)methodMap.get(method.name));
                    if(method == methodToKeep){
                        methodsToRemove.add(methodMap.get(method.name));
                        methodMap.put(method.name, methodToKeep);
                    }else{
                        methodsToRemove.add(method);
                    }
                }
            }else{
                methodMap.put(method.name, method);
            }
        }
        for(Iterator iter = methodsToRemove.iterator(); iter.hasNext(); ){
            methods.remove(iter.next());
        }
    }

    /*
     * The best method, when two methods are equivalent, can be determined in the
     * following order:
     * The method that takes a primitive argument compared to some other method taking
     * a non-primitive argument.
     * The method that takes a non-String argument compared to some other method taking
     * a string 
     */
    private Method findBestEquivalentMethod(Method method1, Method method2){
        for(int i = 0; i < method1.parameters.length; i++){
            String type1 = method1.parameters[i].type;
            String type2 = method2.parameters[i].type;
            if(type1.equals(type2)){
                continue;
            }
            //take a primitive value over a non primitive
            else if(
                (Arrays.binarySearch(TranslationController.JAVA_PRIMITIVES, type1) < 0) &&
                (Arrays.binarySearch(TranslationController.JAVA_PRIMITIVES, type2) >= 0)){
                return method2;
            }else if(
                (Arrays.binarySearch(TranslationController.JAVA_PRIMITIVES, type1) >= 0) &&
                (Arrays.binarySearch(TranslationController.JAVA_PRIMITIVES, type2) < 0)){
                return method1;
            }
            //Take a non-string over a string
            else if(type1.equals("String") &&
                !type2.equals("String")){
                return method2;
            } else if(!type1.equals("String") &&
                type2.equals("String")){
                return method1;
            }
        }
        return method1;
    }
    
    //if two methods have the same number of parameters and there parameter
    //names are the same, they will be considered equivalent regardless of
    //the parameter types
    private boolean methodsAreEquivalent(Method method1, Method method2){
        if(method1.parameters.length != method2.parameters.length){
            System.out.println("Methods have same name and different num parameters");
            return false;
        }
        for(int i = 0; i < method1.parameters.length; i++){
            if(!method1.parameters[i].name.equals(method2.parameters[i].name)){
                return false;
            }
        }
        return true;
    }
}
