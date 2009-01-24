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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import moos.ssds.wrappergenerator.parser.JavaLexer;
import moos.ssds.wrappergenerator.parser.JavaRecognizer;
import moos.ssds.wrappergenerator.parser.JavaTokenTypes;
import moos.ssds.wrappergenerator.parser.JavaTreeParser;
import antlr.RecognitionException;
import antlr.TokenStreamException;
import antlr.collections.AST;

public class TranslationController {

    public static final String[] SSDS_MODEL_CLASSES = new String[]{
        "AccessBean", "CommentTag", "DataContainer", "DataContainerGroup",
        "DataProducer", "DataProducerGroup", "DateRange", "Device",
        "DeviceType", "Event", "HeaderDescription", "IDateRange",
        "IDescription", "IEvent", "IMetadataObject", "IResourceOwner",
        "Keyword", "Person", "RecordDescription", "RecordVariable", "Resource",
        "ResourceBLOB", "ResourceType", "Software", "StandardDomain",
        "StandardKeyword", "StandardReferenceScale", "StandardUnit",
        "StandardVariable", "UserGroup"};

    public static final String[] LEGAL_JAVA_CLASSES = new String[]{"Boolean",
        "Byte", "Character", "Date", "Double", "Float", "Integer", "Long",
        "Number", "Short", "String"};

    public static final String[] JAVA_PRIMITIVES = new String[]{"boolean",
        "byte", "char", "double", "float", "int", "long", "short"};

    public static String[] ALL_LEGAL_PARAM_TYPES;

    static {
        // make sure the array is sorted so that binary search
        // will perform properly.
        Arrays.sort(SSDS_MODEL_CLASSES);
        ArrayList list = new ArrayList();
        list.addAll(Arrays.asList(SSDS_MODEL_CLASSES));
        list.addAll(Arrays.asList(LEGAL_JAVA_CLASSES));
        list.addAll(Arrays.asList(JAVA_PRIMITIVES));
        ALL_LEGAL_PARAM_TYPES = (String[]) list.toArray(new String[0]);
        Arrays.sort(ALL_LEGAL_PARAM_TYPES);
    }

    /**
     * This is the main entry point for this class. It will create a translated
     * version of the file (or directory of files) using the supplied
     * TranslationGenerator to perform the actual translation. Each translated
     * file will be parsed and the generator.generateModule will be invoked with
     * the MyClass object that came from the source file being used as the
     * methods input.
     * 
     * @param fileToTranslate
     *            The java source file to be translated, or a directory
     *            containing source files
     * @param outputFile
     *            the file where the translated text coming from
     *            generator.generateModule will be appended.
     * @param generator
     *            The translation tool to be used.
     * @throws IOException
     */
    public static void translateFile(File fileToTranslate, Writer outputFile,
        TranslationGenerator generator) throws IOException {
        // If this is a directory, walk each file/dir in that directory
        if (fileToTranslate.isDirectory()) {
            File[] files = fileToTranslate.listFiles(new FileFilter() {

                public boolean accept(File pathname) {
                    if (pathname.isDirectory()) {
                        return false;
                    }
                    return true;
                }
            });
            for (int i = 0; i < files.length; i++)
                translateFile(files[i], outputFile, generator);
        }

        // otherwise, if this is a java file, parse it!
        else if ((fileToTranslate.getName().length() > 5)
            && fileToTranslate.getName().substring(
                fileToTranslate.getName().length() - 5).equals(".java")) {
            System.err.println("   " + fileToTranslate.getAbsolutePath());
            // parseFile(f.getName(), new FileInputStream(f));
            MyClass myClass = parseFile(fileToTranslate.getName(),
                new BufferedReader(new FileReader(fileToTranslate)));
            if (myClass != null) {
                outputFile.write(generator.generateModule(myClass));
                outputFile.flush();
            }
        } else {
            System.err.println("Skipping " + fileToTranslate.getAbsolutePath());
        }
    }

    // Here's where we do the real work...
    static MyClass parseFile(String fileName, Reader fileReader) {

        // Create a scanner that reads from the input stream passed to us
        JavaLexer lexer = new JavaLexer(fileReader);
        lexer.setFilename(fileName);

        // Create a parser that reads from the scanner
        JavaRecognizer parser = new JavaRecognizer(lexer);
        parser.setFilename(fileName);

        // start parsing at the compilationUnit rule
        try {
            parser.compilationUnit();
        } catch (RecognitionException e) {
            e.printStackTrace();
            return null;
        } catch (TokenStreamException e) {
            e.printStackTrace();
            return null;
        }

        // do something with the tree
        return doTreeAction(parser.getAST());

    }

    private static MyClass doTreeAction(AST t) {
        if (t == null)
            return null;
        MyClass classBeingParsed = null;
        JavaTreeParser tparse = new JavaTreeParser();
        try {
            tparse.compilationUnit(t);
            // if this is an interface instead of a class return null
            if (hasSibling(t, JavaTokenTypes.INTERFACE_DEF)) {
                return null;
            }
            AST classDef = findSibling(t, JavaTokenTypes.CLASS_DEF);
            String name = findSibling(classDef.getFirstChild(),
                JavaTokenTypes.IDENT).getText();
            if (name.endsWith("AccessEJB")) {
                name = name.substring(0, name.length() - "EJB".length());
            }
            classBeingParsed = new MyClass(name);
            AST extendsClause = classDef.getFirstChild();
            while (extendsClause != null
                && extendsClause.getType() != JavaTokenTypes.EXTENDS_CLAUSE) {
                extendsClause = extendsClause.getNextSibling();
            }
            if (extendsClause != null) {
                AST extensionClass = extendsClause.getFirstChild();
                while (extensionClass != null) {
                    // only pay attention to extension clauses that extend
                    // other SSDS model classes.
                    if (Arrays.binarySearch(SSDS_MODEL_CLASSES, extensionClass
                        .getText()) >= 0) {
                        classBeingParsed.addClassExtended(extensionClass
                            .getText());
                    }
                    extensionClass = extensionClass.getNextSibling();
                }
            }
            while (t.getType() != JavaTokenTypes.OBJBLOCK) {
                if (t.getNextSibling() != null) {
                    t = t.getNextSibling();
                } else {
                    t = t.getFirstChild();
                }
                if (t == null) {
                    System.err.println("Object Block not Found");
                    break;
                }
            }
            // now find all the public methods and put them in an array
            while (t != null) {
                if (t.getType() == JavaTokenTypes.METHOD_DEF) {
                    Method method = parseMethod(t);
                    if (method != null) {
                        classBeingParsed.addMethod(method);
                    }
                }
                if (t.getNextSibling() != null) {
                    t = t.getNextSibling();
                } else {
                    t = t.getFirstChild();
                }
            }
        } catch (RecognitionException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            return null;
        }
        System.out.println(classBeingParsed.getMethods().length
            + " methods before removing duplicates");
        classBeingParsed.removeDuplicateMethods();
        System.out.println(classBeingParsed.getMethods().length
            + " methods after removing duplicates");
        return classBeingParsed;
    }

    private static Method parseMethod(AST t) {
        String methodName = null;
        String returnType = null;
        if (t == null) {
            throw new NullPointerException();
        }
        if (t.getType() != JavaTokenTypes.METHOD_DEF) {
            throw new IllegalArgumentException(
                "AST must be a method definition (type METHOD_DEF)");
        }
        // first, make sure the method is public.
        AST modifiers = findSibling(t.getFirstChild(), JavaTokenTypes.MODIFIERS);
        // if the modifiers does not have any children, than it's not
        // a public method, so return null.
        modifiers = modifiers.getFirstChild();
        if (modifiers == null) {
            return null;
        }
        boolean isPublic = false;
        boolean isStatic = false;
        while (modifiers != null) {
            if (modifiers.getType() == JavaTokenTypes.LITERAL_public) {
                isPublic = true;
            }
            if (modifiers.getType() == JavaTokenTypes.LITERAL_static) {
                isStatic = true;
            }
            modifiers = modifiers.getNextSibling();
        }
        if (!isPublic) {
            return null;
        }

        // so, now we know the method is public, let's try to find the type
        // returned
        AST type = findSibling(t.getFirstChild(), JavaTokenTypes.TYPE);
        // the type token should always have exactly one child.
        if (type.getFirstChild() == null) {
            throw new RuntimeException("Return type token should have a child");
        }
        returnType = type.getFirstChild().getText();

        // now let's find the name of the method
        AST method = findSibling(t.getFirstChild(), JavaTokenTypes.IDENT);
        methodName = method.getText();

        // now the parameters
        AST params = findSibling(t.getFirstChild(), JavaTokenTypes.PARAMETERS);
        Parameter[] parameters = parseParameters(params);

        Method retval = new Method(methodName, parameters, returnType);
        retval.setStatic(isStatic);
        if (isLegalServletMethod(retval)) {
            return retval;
        }
        return null;
    }

    private static boolean isLegalServletMethod(Method method) {
        // look through the list of parameters, if any of them are of a type
        // other than those listed in ALL_LEGAL_PARAM_TYPES the
        // servlet can not handle the method and it should be skipped.
        boolean legalParams = true;
        Parameter[] parameters = method.parameters;
        for (int i = 0; i < parameters.length; i++) {
            String paramType = parameters[i].type;
            if (Arrays.binarySearch(ALL_LEGAL_PARAM_TYPES, paramType) < 0) {
                legalParams = false;
                break;
            }
        }
        if (!legalParams) {
            return false;
        }
        // objects should not be allowed to set their own ID
        if (method.name.equals("setId")) {
            return false;
        }
        if (method.name.matches(".*StringRepresentation")) {
            return false;
        }
        // hibernate methods
        if (method.name.equals("setVersion")
            || method.name.equals("getVersion")) {
            return false;
        }
        // polymorphed methods that have not been implemented in the DAO as of
        // 11 Jan 2006
        if (method.name.equals("findAllDerivedOutputs")
            || method.name.equals("findAllDeploymentsOfDeviceTypeFromParent")
            || method.name.equals("findDevicesByParentByTypeAndByLocation")) {
            return false;
        }
        return true;
    }

    private static Parameter[] parseParameters(AST params) {
        if (params.getNumberOfChildren() == 0) {
            return new Parameter[0];
        }
        List parameters = new ArrayList();
        AST parameterDef = params.getFirstChild();
        while (parameterDef != null) {
            parameters.add(parseParameterDef(parameterDef));
            parameterDef = parameterDef.getNextSibling();
        }
        return (Parameter[]) parameters.toArray(new Parameter[0]);
    }

    private static Parameter parseParameterDef(AST parameterDef) {
        AST type = findSibling(parameterDef.getFirstChild(),
            JavaTokenTypes.TYPE);
        AST ident = findSibling(parameterDef.getFirstChild(),
            JavaTokenTypes.IDENT);

        return new Parameter(ident.getText(), type.getFirstChild().getText());
    }

    // use this method to check if a node has a sibling of a certain type
    private static boolean hasSibling(AST t, int tokenType) {
        while (t != null && t.getType() != tokenType) {
            t = t.getNextSibling();
        }
        if (t == null) {
            return false;
        }
        return true;
    }

    // this should only be called when a sibling is known to exist.
    // an exception will be thrown if the sibling with the specified
    // token type is not found.
    private static AST findSibling(AST t, int tokenType) {
        while (t != null && t.getType() != tokenType) {
            t = t.getNextSibling();
        }
        if (t == null) {
            throw new RuntimeException(
                "Unable to find the requested token type");
        }
        return t;
    }
}