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

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.Arrays;

public class PerlGenerator implements TranslationGenerator {

	private static final String headerLine = "#====================================================================\n";
	private static final String METHOD_SEPERATOR = "#--------------------------------------------------------------------\n#\n";
	private static final String commonImports = "use LWP::Simple;\nuse strict;\nuse Carp;\n";

	public String generateModule(MyClass someClass) {
		if (someClass.name.indexOf("Access") != -1) {
			return generateAccessModule(someClass);
		}
		return generateModelModule(someClass);
	}

	public static void main(String[] args) {
		try {
			// if we have at least two command-line arguments
			if (args.length >= 2) {
				// TODO: create Perl method that generates string
				// representations
				// of SSDS model objects that may be passed as arguments to
				// Access
				// methods such as insert(), update(), and makePersistent().
				TranslationGenerator generator = new PerlGenerator();
				System.err.println("Parsing...");
				Writer writer = new FileWriter(new File(args[0]), true);
				writer.write("\n\n");
				// for each directory/file specified on the command line
				for (int i = 1; i < args.length; i++) {
					TranslationController.translateFile(new File(args[i]),
							writer, generator); // parse
					// it
				}
				writer.close();
			} else
				System.err
						.println("Usage: java PerlGenerator <outputFile(i.e. ssds.pm)>"
								+ "<directory or file name>");
		} catch (Exception e) {
			System.err.println("exception: " + e);
			e.printStackTrace(System.err); // so we can get stack trace
		}
	}

	public String generateModelModule(MyClass modelClass) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(createHeaderAndNewForModel(modelClass));
		Method[] methods = modelClass.getMethods();
		for (int i = 0; i < methods.length; i++) {
			if (methods[i].name.startsWith("is")) {
				buffer.append(createIsMethod(modelClass, methods[i]));
			} else if (methods[i].name.startsWith("get")) {
				if (Arrays.binarySearch(
						TranslationController.SSDS_MODEL_CLASSES,
						methods[i].returnType) >= 0) {
					buffer
							.append(createSsdsGetterMethod(modelClass,
									methods[i]));
				} else if (methods[i].returnType.equals("Collection")) {
					// If get returns a Collection of SSDS Model classes
					String retClass = methods[i].name.substring(3,
							methods[i].name.length() - 1);
					if (Arrays.binarySearch(
							TranslationController.SSDS_MODEL_CLASSES, retClass) >= 0) {
						buffer.append(createSsdsListMethod(modelClass,
								methods[i]));
					}
				}
			} else if (methods[i].name.startsWith("list")) {
				buffer.append(createSsdsListMethod(modelClass, methods[i]));
			} else if (methods[i].name.startsWith("set")) {
				buffer.append(createSsdsSetterMethod(modelClass, methods[i]));
			} else if (methods[i].name.startsWith("add")) {
				buffer.append(createSsdsAddMethod(modelClass, methods[i]));
			} else if (methods[i].name.startsWith("remove")) {
				buffer.append(createSsdsRemoveMethod(modelClass, methods[i]));
			} else if (methods[i].name.endsWith("AsEsecs")) {
				buffer
						.append(createSsdsReturnLongMethod(modelClass,
								methods[i]));
			}
		}
		buffer.append("\n\n=back\n\n=cut\n\n");
		return buffer.toString();
	}

	private String createSsdsSetterMethod(MyClass modelClass, Method method) {
		if (method.parameters.length != 1) {
			throw new IllegalArgumentException(
					"Assumption violated, only expecting 1 parameter."
							+ " Class = " + modelClass.name + " method = "
							+ method.toString());
		}
		if (Arrays.binarySearch(TranslationController.SSDS_MODEL_CLASSES,
				method.parameters[0].type) < 0) {
			return createSsdsPrimitiveSetterMethod(modelClass, method);
		}
		return createSsdsObjectSetterMethod(modelClass, method);
	}

	private String createSsdsAddMethod(MyClass modelClass, Method method) {
		if (method.parameters.length != 1) {
			throw new IllegalArgumentException(
					"Assumption violated, only expecting 1 parameter."
							+ " Class = " + modelClass.name + " method = "
							+ method.toString());
		}
		if (Arrays.binarySearch(TranslationController.SSDS_MODEL_CLASSES,
				method.parameters[0].type) < 0) {
			return createSsdsPrimitiveAddMethod(modelClass, method);
		}
		return createSsdsObjectAddMethod(modelClass, method);
	}

	private String createSsdsRemoveMethod(MyClass modelClass, Method method) {
		if (method.parameters.length != 1) {
			throw new IllegalArgumentException(
					"Assumption violated, only expecting 1 parameter."
							+ " Class = " + modelClass.name + " method = "
							+ method.toString());
		}
		if (Arrays.binarySearch(TranslationController.SSDS_MODEL_CLASSES,
				method.parameters[0].type) < 0) {
			return createSsdsPrimitiveRemoveMethod(modelClass, method);
		}
		return createSsdsObjectRemoveMethod(modelClass, method);
	}

	private String createSsdsPrimitiveSetterMethod(MyClass modelClass,
			Method method) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(METHOD_SEPERATOR + "\n");
		buffer.append("=item B<" + method.name + "(");
		for (int i = 0; i < method.parameters.length; i++) {
			buffer.append(method.parameters[i].type + " "
					+ method.parameters[i].name);
			if (i < method.parameters.length - 1) {
				buffer.append(", ");
			}
		}
		buffer.append(")>\n\n");
		String addorset = method.name.substring(0, 3);
		buffer.append(addorset + " the "
				+ method.getMethodNameWithoutAccessorPrefix() + " for this "
				+ modelClass.name + "\n\n");
		buffer.append("=cut\n\n");
		buffer.append("sub " + method.name + " {\n");
		buffer.append("\tmy $obj = shift;\n");
		String[] paramNames = new String[method.parameters.length];
		for (int i = 0; i < method.parameters.length; i++) {
			paramNames[i] = "$" + method.parameters[i].name + "Arg";
			buffer.append("\tmy " + paramNames[i] + " = shift;\n");
		}

		buffer.append("\tmy $url = $obj->baseUrl . \"objectToInvokeOn=\";\n");
		buffer.append("\tmy $objRep;\n");
		buffer.append(createObjectRepresentation("$obj", "$objRep"));
		buffer.append("\t$url .= $objRep;\n");

		buffer.append("\t$url .= \"&method=" + method.name + "\";\n");
		for (int i = 0; i < method.parameters.length; i++) {
			buffer.append(createParameterArgument(method.parameters[i],
					paramNames[i], "$url", i + 1));
		}
		buffer.append("\treturn $obj->_execSSDSmethod($url);\n");
		buffer.append("}\n\n");
		return buffer.toString();
	}

	private String createSsdsPrimitiveAddMethod(MyClass modelClass,
			Method method) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(METHOD_SEPERATOR + "\n");
		buffer.append("=item B<" + method.name + "(");
		for (int i = 0; i < method.parameters.length; i++) {
			buffer.append(method.parameters[i].type + " "
					+ method.parameters[i].name);
			if (i < method.parameters.length - 1) {
				buffer.append(", ");
			}
		}
		buffer.append(")>\n\n");
		String addorset = method.name.substring(0, 3);
		buffer.append(addorset + " the "
				+ method.getMethodNameWithoutAccessorPrefix() + " for this "
				+ modelClass.name + "\n\n");
		buffer.append("=cut\n\n");
		buffer.append("sub " + method.name + " {\n");
		buffer.append("\tmy $obj = shift;\n");
		String[] paramNames = new String[method.parameters.length];
		for (int i = 0; i < method.parameters.length; i++) {
			paramNames[i] = "$" + method.parameters[i].name + "Arg";
			buffer.append("\tmy " + paramNames[i] + " = shift;\n");
		}

		buffer.append("\tmy $url = $obj->baseUrl . \"objectToInvokeOn=\";\n");
		buffer.append("\tmy $objRep;\n");
		buffer.append(createObjectRepresentation("$obj", "$objRep"));
		buffer.append("\t$url .= $objRep;\n");

		buffer.append("\t$url .= \"&method=" + method.name + "\";\n");
		for (int i = 0; i < method.parameters.length; i++) {
			buffer.append(createParameterArgument(method.parameters[i],
					paramNames[i], "$url", i + 1));
		}
		buffer.append("\treturn $obj->_execSSDSmethod($url);\n");
		buffer.append("}\n\n");
		return buffer.toString();
	}

	private String createSsdsPrimitiveRemoveMethod(MyClass modelClass,
			Method method) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(METHOD_SEPERATOR + "\n");
		buffer.append("=item B<" + method.name + "(");
		for (int i = 0; i < method.parameters.length; i++) {
			buffer.append(method.parameters[i].type + " "
					+ method.parameters[i].name);
			if (i < method.parameters.length - 1) {
				buffer.append(", ");
			}
		}
		buffer.append(")>\n\n");
		String remove = method.name.substring(0, 3);
		buffer.append(remove + " the "
				+ method.getMethodNameWithoutAccessorPrefix() + " for this "
				+ modelClass.name + "\n\n");
		buffer.append("=cut\n\n");
		buffer.append("sub " + method.name + " {\n");
		buffer.append("\tmy $obj = shift;\n");
		String[] paramNames = new String[method.parameters.length];
		for (int i = 0; i < method.parameters.length; i++) {
			paramNames[i] = "$" + method.parameters[i].name + "Arg";
			buffer.append("\tmy " + paramNames[i] + " = shift;\n");
		}

		buffer.append("\tmy $url = $obj->baseUrl . \"objectToInvokeOn=\";\n");
		buffer.append("\tmy $objRep;\n");
		buffer.append(createObjectRepresentation("$obj", "$objRep"));
		buffer.append("\t$url .= $objRep;\n");

		buffer.append("\t$url .= \"&method=" + method.name + "\";\n");
		for (int i = 0; i < method.parameters.length; i++) {
			buffer.append(createParameterArgument(method.parameters[i],
					paramNames[i], "$url", i + 1));
		}
		buffer.append("\treturn $obj->_execSSDSmethod($url);\n");
		buffer.append("}\n\n");
		return buffer.toString();
	}

	private String createSsdsObjectSetterMethod(MyClass modelClass,
			Method method) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(METHOD_SEPERATOR + "\n");
		buffer.append("=item B<" + method.name + "(");
		for (int i = 0; i < method.parameters.length; i++) {
			buffer.append(method.parameters[i].type + " "
					+ method.parameters[i].name);
			if (i < method.parameters.length - 1) {
				buffer.append(", ");
			}
		}
		buffer.append(")>\n\n");
		String setterVariable = method.getMethodNameWithoutAccessorPrefix();
		String setterClass = new String(setterVariable.charAt(0) + "")
				.toUpperCase()
				+ setterVariable.substring(1);

		String addorset = method.name.substring(0, 3);
		buffer.append(addorset + " the " + setterClass + " object for this "
				+ modelClass.name + "\n\n");
		buffer.append("Example:\n\n");
		String setterAccessClass = setterClass + "Access";
		String accessClass = modelClass.name + "Access";
		buffer.append("\tmy $" + makeFirstLetterLowerCase(setterAccessClass)
				+ " = ");
		buffer.append("new SSDS::" + setterAccessClass + "();\n");
		buffer.append("\tmy $" + setterVariable + " = ");
		buffer.append("new SSDS::" + setterClass + "();\n");
		buffer.append("\t# set some attributes of the " + setterClass
				+ " object\n");
		buffer.append("\t$" + setterVariable + "->name($someUniqueName);\n");
		buffer.append("\t...\n");
		buffer.append("\t$" + makeFirstLetterLowerCase(setterAccessClass)
				+ "->insert($" + setterVariable + ");\n\n");
		buffer.append("\t# Find the " + setterClass
				+ " just inserted so that we know its ID\n");
		String foundObj = "$found" + setterClass;
		buffer
				.append("\t# Note - Access class methods may vary from those shown in this example\n");
		buffer.append("\tmy " + foundObj + "s = $" + setterVariable);
		buffer.append("->findByLikeName($someUniqueName);\n");
		buffer.append("\t# " + foundObj + "s now contains an array of objects "
				+ "(because findByLikeName returns a collection\n");
		buffer.append("\tmy " + foundObj + " = $" + foundObj + "s[0];\n\n");
		buffer.append("\t# Insert a " + modelClass.name + "\n");
		buffer.append("\tmy $" + makeFirstLetterLowerCase(accessClass)
				+ " = new SSDS::" + accessClass + "();\n");
		buffer.append("\tmy $" + makeFirstLetterLowerCase(modelClass.name)
				+ " = new SSDS::" + modelClass.name + "();\n");
		buffer.append("\t$" + makeFirstLetterLowerCase(modelClass.name)
				+ "->name($someUniqueName);\n");
		buffer.append("\t$" + makeFirstLetterLowerCase(accessClass)
				+ "->insert($" + makeFirstLetterLowerCase(modelClass.name)
				+ ");\n\n");
		buffer.append("\t# Find the " + modelClass.name
				+ " just inserted and set the " + setterClass + "\n");
		String foundModel = "$found" + modelClass.name;
		buffer
				.append("\t# Note - Again, access class methods will vary, findByName\n"
						+ "\t# is just an example and does not exist for all classes\n");
		buffer.append("\t" + foundModel + "s = $"
				+ makeFirstLetterLowerCase(accessClass)
				+ "->findByName($someUniqueName);\n");
		buffer.append("\t" + foundModel + " = $" + foundModel + "s[0];\n");
		buffer.append("\t" + foundModel + "->" + method.name + "(" + foundObj
				+ ");\n\n");
		buffer.append("=cut\n\n");
		buffer.append("sub " + method.name + " {\n");
		buffer.append("\tmy $obj = shift;\n");
		buffer.append("\tmy $" + makeFirstLetterLowerCase(setterClass)
				+ " = shift;\n");
		buffer.append("\tmy $url = $obj->baseUrl . \"objectToInvokeOn=");
		buffer.append("\" . $obj->className();\n");
		buffer.append("\t$url .= \"|id=\" . $obj->id() if $obj->id();\n");
		buffer.append("\t$url .= \"&method=" + method.name + "\";\n");

		buffer.append(createParameterArgument(method.parameters[0], "$"
				+ makeFirstLetterLowerCase(setterClass), "$url", 1));
		buffer.append("\treturn $obj->_execSSDSmethod($url);\n");
		buffer.append("}\n\n");
		return buffer.toString();
	}

	private String createSsdsObjectAddMethod(MyClass modelClass, Method method) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(METHOD_SEPERATOR + "\n");
		buffer.append("=item B<" + method.name + "(");
		for (int i = 0; i < method.parameters.length; i++) {
			buffer.append(method.parameters[i].type + " "
					+ method.parameters[i].name);
			if (i < method.parameters.length - 1) {
				buffer.append(", ");
			}
		}
		buffer.append(")>\n\n");
		String setterVariable = method.getMethodNameWithoutAccessorPrefix();
		String setterClass = new String(setterVariable.charAt(0) + "")
				.toUpperCase()
				+ setterVariable.substring(1);

		String addorset = method.name.substring(0, 3);
		buffer.append(addorset + " the " + setterClass + " object for this "
				+ modelClass.name + "\n\n");
		buffer.append("Example:\n\n");
		String setterAccessClass = setterClass + "Access";
		String accessClass = modelClass.name + "Access";
		buffer.append("\tmy $" + makeFirstLetterLowerCase(setterAccessClass)
				+ " = ");
		buffer.append("new SSDS::" + setterAccessClass + "();\n");
		buffer.append("\tmy $" + setterVariable + " = ");
		buffer.append("new SSDS::" + setterClass + "();\n");
		buffer.append("\t# set some attributes of the " + setterClass
				+ " object\n");
		buffer.append("\t$" + setterVariable + "->name($someUniqueName);\n");
		buffer.append("\t...\n");
		buffer.append("\t$" + makeFirstLetterLowerCase(setterAccessClass)
				+ "->insert($" + setterVariable + ");\n\n");
		buffer.append("\t# Find the " + setterClass
				+ " just inserted so that we know its ID\n");
		String foundObj = "$found" + setterClass;
		buffer
				.append("\t# Note - Access class methods may vary from those shown in this example\n");
		buffer.append("\tmy " + foundObj + "s = $" + setterVariable);
		buffer.append("->findByLikeName($someUniqueName);\n");
		buffer.append("\t# " + foundObj + "s now contains an array of objects "
				+ "(because findByLikeName returns a collection\n");
		buffer.append("\tmy " + foundObj + " = $" + foundObj + "s[0];\n\n");
		buffer.append("\t# Insert a " + modelClass.name + "\n");
		buffer.append("\tmy $" + makeFirstLetterLowerCase(accessClass)
				+ " = new SSDS::" + accessClass + "();\n");
		buffer.append("\tmy $" + makeFirstLetterLowerCase(modelClass.name)
				+ " = new SSDS::" + modelClass.name + "();\n");
		buffer.append("\t$" + makeFirstLetterLowerCase(modelClass.name)
				+ "->name($someUniqueName);\n");
		buffer.append("\t$" + makeFirstLetterLowerCase(accessClass)
				+ "->insert($" + makeFirstLetterLowerCase(modelClass.name)
				+ ");\n\n");
		buffer.append("\t# Find the " + modelClass.name
				+ " just inserted and set the " + setterClass + "\n");
		String foundModel = "$found" + modelClass.name;
		buffer
				.append("\t# Note - Again, access class methods will vary, findByName\n"
						+ "\t# is just an example and does not exist for all classes\n");
		buffer.append("\t" + foundModel + "s = $"
				+ makeFirstLetterLowerCase(accessClass)
				+ "->findByName($someUniqueName);\n");
		buffer.append("\t" + foundModel + " = $" + foundModel + "s[0];\n");
		buffer.append("\t" + foundModel + "->" + method.name + "(" + foundObj
				+ ");\n\n");
		buffer.append("=cut\n\n");
		buffer.append("sub " + method.name + " {\n");
		buffer.append("\tmy $obj = shift;\n");
		buffer.append("\tmy $" + makeFirstLetterLowerCase(setterClass)
				+ " = shift;\n");
		buffer.append("\tmy $url = $obj->baseUrl . \"objectToInvokeOn=");
		buffer.append("\" . $obj->className();\n");

		buffer.append("\t$url .= \"|id=\" . $obj->id() if $obj->id();\n");

		buffer.append("\t$url .= \"&method=" + method.name + "\";\n");

		buffer.append(createParameterArgument(method.parameters[0], "$"
				+ makeFirstLetterLowerCase(setterClass), "$url", 1));
		buffer.append("\treturn $obj->_execSSDSmethod($url);\n");
		buffer.append("}\n\n");
		return buffer.toString();
	}

	private String createSsdsObjectRemoveMethod(MyClass modelClass,
			Method method) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(METHOD_SEPERATOR + "\n");
		buffer.append("=item B<" + method.name + "(");
		for (int i = 0; i < method.parameters.length; i++) {
			buffer.append(method.parameters[i].type + " "
					+ method.parameters[i].name);
			if (i < method.parameters.length - 1) {
				buffer.append(", ");
			}
		}
		buffer.append(")>\n\n");
		String setterVariable = method.getMethodNameWithoutAccessorPrefix();
		String setterClass = new String(setterVariable.charAt(0) + "")
				.toUpperCase()
				+ setterVariable.substring(1);

		String remove = method.name.substring(0, 3);
		buffer.append(remove + " the " + setterClass + " object for this "
				+ modelClass.name + "\n\n");
		buffer.append("Example:\n\n");
		String setterAccessClass = setterClass + "Access";
		String accessClass = modelClass.name + "Access";
		buffer.append("\tmy $" + makeFirstLetterLowerCase(setterAccessClass)
				+ " = ");
		buffer.append("new SSDS::" + setterAccessClass + "();\n");
		buffer.append("\tmy $" + setterVariable + " = ");
		buffer.append("new SSDS::" + setterClass + "();\n");
		buffer.append("\t# set some attributes of the " + setterClass
				+ " object\n");
		buffer.append("\t$" + setterVariable + "->name($someUniqueName);\n");
		buffer.append("\t...\n");
		buffer.append("\t$" + makeFirstLetterLowerCase(setterAccessClass)
				+ "->insert($" + setterVariable + ");\n\n");
		buffer.append("\t# Find the " + setterClass
				+ " just inserted so that we know its ID\n");
		String foundObj = "$found" + setterClass;
		buffer
				.append("\t# Note - Access class methods may vary from those shown in this example\n");
		buffer.append("\tmy " + foundObj + "s = $" + setterVariable);
		buffer.append("->findByLikeName($someUniqueName);\n");
		buffer.append("\t# " + foundObj + "s now contains an array of objects "
				+ "(because findByLikeName returns a collection\n");
		buffer.append("\tmy " + foundObj + " = $" + foundObj + "s[0];\n\n");
		buffer.append("\t# Insert a " + modelClass.name + "\n");
		buffer.append("\tmy $" + makeFirstLetterLowerCase(accessClass)
				+ " = new SSDS::" + accessClass + "();\n");
		buffer.append("\tmy $" + makeFirstLetterLowerCase(modelClass.name)
				+ " = new SSDS::" + modelClass.name + "();\n");
		buffer.append("\t$" + makeFirstLetterLowerCase(modelClass.name)
				+ "->name($someUniqueName);\n");
		buffer.append("\t$" + makeFirstLetterLowerCase(accessClass)
				+ "->insert($" + makeFirstLetterLowerCase(modelClass.name)
				+ ");\n\n");
		buffer.append("\t# Find the " + modelClass.name
				+ " just inserted and set the " + setterClass + "\n");
		String foundModel = "$found" + modelClass.name;
		buffer
				.append("\t# Note - Again, access class methods will vary, findByName\n"
						+ "\t# is just an example and does not exist for all classes\n");
		buffer.append("\t" + foundModel + "s = $"
				+ makeFirstLetterLowerCase(accessClass)
				+ "->findByName($someUniqueName);\n");
		buffer.append("\t" + foundModel + " = $" + foundModel + "s[0];\n");
		buffer.append("\t" + foundModel + "->" + method.name + "(" + foundObj
				+ ");\n\n");
		buffer.append("=cut\n\n");
		buffer.append("sub " + method.name + " {\n");
		buffer.append("\tmy $obj = shift;\n");
		buffer.append("\tmy $" + makeFirstLetterLowerCase(setterClass)
				+ " = shift;\n");
		buffer.append("\tmy $url = $obj->baseUrl . \"objectToInvokeOn=");
		buffer.append("\" . $obj->className();\n");

		buffer.append("\t$url .= \"|id=\" . $obj->id() if $obj->id();\n");

		buffer.append("\t$url .= \"&method=" + method.name + "\";\n");

		buffer.append(createParameterArgument(method.parameters[0], "$"
				+ makeFirstLetterLowerCase(setterClass), "$url", 1));
		buffer.append("\treturn $obj->_execSSDSmethod($url);\n");
		buffer.append("}\n\n");
		return buffer.toString();
	}

	private String createHeaderAndNewForModel(MyClass modelClass) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(headerLine);
		buffer.append("#                        " + modelClass.name + "\n");
		buffer.append(headerLine);
		buffer.append("package SSDS::" + modelClass.name + ";\n");
		buffer.append(commonImports);
		buffer.append("use Class::ObjectTemplate;\n");
		String[] extensions = modelClass.getClassesExtended();
		buffer.append("\n");
		buffer
				.append("our @ISA = qw(SSDS::ObjectCreator Class::ObjectTemplate");
		for (int i = 0; i < extensions.length; i++) {
			buffer.append(" SSDS::" + extensions[i]);
		}
		buffer.append(");\n");
		buffer.append("\n");
		buffer.append("attributes qw(");
		Method[] methods = modelClass.getMethods();
		StringBuffer methodNames = new StringBuffer();
		for (int i = 0; i < methods.length; i++) {
			if (methods[i].isStatic
					|| !(methods[i].name.startsWith("get") || methods[i].name
							.startsWith("is"))) {
				continue;
			}
			methodNames.append(methods[i].getMethodNameWithoutAccessorPrefix());
			if (i < methods.length - 1) {
				methodNames.append(" ");
			}
		}
		buffer.append(methodNames.toString() + ");\n");
		buffer.append("\n");
		buffer.append("\n=head3 " + modelClass.name + "\n");
		buffer.append("\n" + modelClass.name + " - An SSDS " + modelClass.name
				+ " object\n");
		buffer.append("\n\n=over 4\n");
		buffer.append("\n=item B<new()>\n");
		buffer
				.append("\nInstantiate an SSDS::" + modelClass.name
						+ " object\n");
		buffer
				.append("\nThe following methods may be used to get or set any of the attributes of a\n");
		buffer.append(modelClass.name
				+ " object (each attribute is both a setter and getter):\n");
		buffer.append("\n\t" + methodNames.toString() + "\n");
		buffer
				.append("\n"
						+ "Note: the get_attribute_names() method will return this list of attributes\n");
		buffer.append("\nExample:\n\n");
		buffer.append("\tmy $" + makeFirstLetterLowerCase(modelClass.name)
				+ "Access = ");
		buffer.append("new SSDS::" + modelClass.name + "Access();\n");
		buffer.append("\tmy $" + makeFirstLetterLowerCase(modelClass.name));
		buffer.append(" = $" + makeFirstLetterLowerCase(modelClass.name)
				+ "Access->findByPK(5777);\n");
		buffer.append("\n=cut\n\n");
		buffer.append("sub initialize {\n");
		buffer.append("\tmy $class = shift;\n");
		buffer.append("\tmy $obj = {};\n");
		buffer.append("\tbless $obj, ref($class) || $class;\n");
		buffer.append("\treturn $obj;\n");
		buffer.append("}\n\n");
		buffer
				.append("# save a reference to the class name so that super classes\n");
		buffer.append("# can reference it\n");
		buffer.append("sub className {\n");
		buffer.append("\tmy $self = shift;\n");
		buffer.append("\treturn \"" + modelClass.name + "\";\n");
		buffer.append("}\n\n");
		return buffer.toString();
	}

	private String createHeaderAndNewForAccess(MyClass accessClass) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(headerLine);
		buffer.append("#                        " + accessClass.name + "\n");
		buffer.append(headerLine);
		buffer.append("package SSDS::" + accessClass.name + ";\n\n");
		buffer.append(commonImports);
		buffer.append("\n");

		String[] extensions = accessClass.getClassesExtended();
		buffer.append("\n");
		buffer.append("our @ISA = qw(SSDS::ObjectCreator");
		for (int i = 0; i < extensions.length; i++) {
			buffer.append(" SSDS::" + extensions[i]);
		}
		buffer.append(");\n");
		buffer.append("\n");
		buffer.append("\n=head3 " + accessClass.name + "\n");
		buffer.append("\n" + accessClass.name
				+ " - Contains methods to query for and get ");
		String modelClass = accessClass.name.substring(0, accessClass.name
				.length()
				- "access".length());
		buffer.append(modelClass + " objects\n");
		buffer.append("\n\n=over 4\n");
		buffer.append("\n=item B<new()>\n");
		buffer.append("\nInstantiate an SSDS::" + accessClass.name
				+ " object\n");
		buffer.append("\nExample:\n\n");
		buffer.append("\tmy $" + makeFirstLetterLowerCase(accessClass.name)
				+ " = ");
		buffer.append("new SSDS::" + accessClass.name + "();\n");
		buffer.append("\tmy $" + makeFirstLetterLowerCase(modelClass));
		buffer.append(" = $" + makeFirstLetterLowerCase(accessClass.name)
				+ "->findByPK(5777);\n");
		buffer.append("\n=cut\n\n");
		buffer.append("sub new {\n");
		buffer.append("\tmy $pkg = shift;\n");
		buffer.append("\tmy $obj = $pkg->SUPER::new();\n");
		buffer.append("\treturn $obj;\n");
		buffer.append("}\n\n");
		buffer
				.append("# save a reference to the class name so that super classes\n");
		buffer.append("# can reference it\n");
		buffer.append("sub className {\n");
		buffer.append("\tmy $self = shift;\n");
		buffer.append("\treturn \"" + accessClass.name + "\";\n");
		buffer.append("}\n\n");
		return buffer.toString();
	}

	private String makeFirstLetterLowerCase(String word) {
		String firstLetter = new String(word.charAt(0) + "");
		return firstLetter.toLowerCase() + word.substring(1);
	}

	private String createSsdsGetterMethod(MyClass myClass, Method myMethod) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(createAccessorMethodUpToReturn(myClass, myMethod));
		buffer.append("\treturn $obj->_createSSDSobject($url);\n");
		buffer.append("}\n\n");
		return buffer.toString();
	}

	private String createSsdsReturnLongMethod(MyClass myClass, Method myMethod) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(createAccessorMethodUpToReturn(myClass, myMethod));
		buffer.append("\tmy $ret = $obj->_execSSDSmethod($url);\n");
		buffer.append("\t$ret =~ /java.lang.Long\\|(\\d+)/;\n");
		buffer.append("\treturn $1;\n");
		buffer.append("}\n\n");
		return buffer.toString();
	}

	private String createSsdsListMethod(MyClass myClass, Method myMethod) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(createAccessorMethodUpToReturn(myClass, myMethod));
		buffer.append("\treturn $obj->_createSSDSobjects($url);\n");
		buffer.append("}\n\n");
		return buffer.toString();
	}

	private String createParameterArgument(Parameter parameter,
			String parameterVariableName, String urlVariable, int paramNum) {
		StringBuffer buffer = new StringBuffer();
		String paramType = parameter.type;
		// if the parameter is an interface, strip the interface and use the
		// concrete class
		if (paramType.startsWith("I")) {
			paramType = paramType.substring(1);
		}

		if (Arrays.binarySearch(TranslationController.SSDS_MODEL_CLASSES,
				parameter.type) > 0) {
			buffer.append("\t" + urlVariable + ".= \"&p" + paramNum
					+ "Type=\" . ");
			buffer.append(parameterVariableName + "->className();\n");
			if (paramNum == 1) {
				buffer.append("\tmy $paramRep;\n");
			}
			buffer.append(createObjectRepresentation(parameterVariableName,
					"$paramRep"));
			parameterVariableName = "$paramRep";

		} else {
			buffer.append("\t" + urlVariable + ".= \"&p" + paramNum + "Type=");
			buffer.append(paramType + "\";\n");
		}

		buffer.append("\t" + urlVariable + " .= \"&p" + paramNum
				+ "Value=\" . " + parameterVariableName + ";\n");
		return buffer.toString();
	}

	/*
	 * Create a string with just the id=XXXX representing the object. Some
	 * methods such as the find... methods don't need all the attributes set
	 */
	private String createParameterArgumentWithIdOnly(Parameter parameter,
			String parameterVariableName, String urlVariable, int paramNum) {
		StringBuffer buffer = new StringBuffer();
		String paramType = parameter.type;
		// if the parameter is an interface, strip the interface and use the
		// concrete class
		if (paramType.startsWith("I")) {
			paramType = paramType.substring(1);
		}

		if (Arrays.binarySearch(TranslationController.SSDS_MODEL_CLASSES,
				parameter.type) > 0) {
			buffer.append("\t" + urlVariable + ".= \"&p" + paramNum
					+ "Type=\" . ");
			buffer.append(parameterVariableName + "->className();\n");
			if (paramNum == 1) {
				buffer.append("\tmy $paramRep;\n");
			}
			buffer.append(createObjectRepresentationWithIdOnly(
					parameterVariableName, "$paramRep"));
			parameterVariableName = "$paramRep";

		} else {
			buffer.append("\t" + urlVariable + " .= \"&p" + paramNum + "Type=");
			buffer.append(paramType + "\";\n");
		}

		/*
		 * Add default values for the findBy methods
		 */
		if ("$exactMatchArg".equals(parameterVariableName)) {
			buffer.append("\t" + urlVariable + " .= \"&p" + paramNum
					+ "Value=\";\n");
			buffer.append("\t" + urlVariable
					+ " .= ($exactMatchArg) ? $exactMatchArg : 'true'" + ";\n");
		} else if ("$orderByPropertyNameArg".equals(parameterVariableName)) {
			buffer.append("\t" + urlVariable + " .= \"&p" + paramNum
					+ "Value=\";\n");
			buffer
					.append("\t"
							+ urlVariable
							+ " .= ($orderByPropertyNameArg) ? $orderByPropertyNameArg : 'id'"
							+ ";\n");

		} else if ("$returnFullObjectGraphArg".equals(parameterVariableName)) {
			buffer.append("\t" + urlVariable + " .= \"&p" + paramNum
					+ "Value=\";\n");
			buffer
					.append("\t"
							+ urlVariable
							+ " .= ($returnFullObjectGraphArg) ? $returnFullObjectGraphArg : 'false'"
							+ ";\n");

		} else if ("$ascendingOrDescendingArg".equals(parameterVariableName)) {
			buffer.append("\t" + urlVariable + " .= \"&p" + paramNum
					+ "Value=\";\n");
			buffer
					.append("\t"
							+ urlVariable
							+ " .= ($ascendingOrDescendingArg) ? $ascendingOrDescendingArg : 'false'"
							+ ";\n");

		} else {
			buffer.append("\t" + urlVariable + " .= \"&p" + paramNum
					+ "Value=\" . " + parameterVariableName + ";\n");
		}
		return buffer.toString();
	}

	// creates a string that will be suitable for sending to the SSDS servlet.
	// The string representation will be held in "representationVariable".
	// The generated string should be executed before using the
	// representationVariable.
	// Stop adding parameters if we have an id specified. That's all we need.
	private String createObjectRepresentation(String objectVariableName,
			String representationVariable) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("\t" + representationVariable + " = ");
		buffer.append(objectVariableName + "->className();\n");
		buffer.append("\tforeach my $a (" + objectVariableName
				+ "->get_attribute_names()) {\n");
		buffer.append("\t\t" + representationVariable + " .= "
				+ objectVariableName + "->delim() . $a . \"=\" . ");
		buffer.append(objectVariableName + "->$a if " + objectVariableName
				+ "->$a;\n");
		// kgomes 2008-08-14: I commented this out because it prevents updates
		// from working correctly. For instance if you change an attribute (like
		// 'name' for example) and then call 'update', the string representation
		// only includes the ID so it won't see any of the updated attributes
		// and nothing will change. I have some vague memory of why we did it
		// this way and I think it is because of default values on classes. For
		// example, when instantiating a DataProducer, it will create a default
		// value for type of 'Deployment'. If you do this and then assign an ID
		// to perform an update, the type field will change. I wonder if we
		// shouldn't just disallow changes to fields with default values in the
		// make persisent.
		// buffer.append("\t\tlast if $a eq 'id' && " + objectVariableName +
		// "->$a;\n");
		buffer.append("\t}\n");
		return buffer.toString();
	}

	/*
	 * Create a string with just the id=XXXX representing the object. Some
	 * methods such as the find... methods don't need all the attributes set
	 */
	private String createObjectRepresentationWithIdOnly(
			String objectVariableName, String representationVariable) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("\t" + representationVariable + " = ");
		buffer.append(objectVariableName + "->className();\n");

		buffer.append("\t" + representationVariable + " .= "
				+ objectVariableName + "->delim() . \"id=\" . "
				+ objectVariableName + "->id() if " + objectVariableName
				+ "->id();\n");

		return buffer.toString();
	}

	private String createAccessorMethodUpToReturn(MyClass myClass,
			Method myMethod) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(METHOD_SEPERATOR);
		buffer.append("\n");
		buffer.append("=item B<" + myMethod.name + "()>\n\n");
		if (myMethod.name.startsWith("is")) {
			buffer.append("Return 1 if " + myClass.name + " is "
					+ myMethod.getMethodNameWithoutAccessorPrefix());
			buffer.append(", otherwise returns 0.\n\n");
		} else if (myMethod.name.startsWith("list")) {
			buffer.append("Return the "
					+ myMethod.getMethodNameWithoutAccessorPrefix()
					+ " associated with " + "this " + myClass.name + "\n\n");
		} else {
			buffer.append("Returns the "
					+ myMethod.getMethodNameWithoutAccessorPrefix()
					+ " of type " + myMethod.returnType + " from this "
					+ myClass.name + "\n\n");
		}
		buffer.append("\n\n=cut\n\n");
		buffer.append("sub " + myMethod.name + " {\n");
		buffer.append("\tmy $obj = shift;\n");
		buffer.append("\tmy $url = $obj->baseUrl . \"objectToInvokeOn=\";\n");
		buffer.append("\tmy $objRep;\n");
		buffer.append(createObjectRepresentationWithIdOnly("$obj", "$objRep"));
		buffer.append("\t$url .= $objRep . ");
		buffer.append("\"&method=" + myMethod.name);
		// If we're getting an SSDS model object we need fullGraph
		String getterVariable = myMethod.getMethodNameWithoutAccessorPrefix();
		String getterClass = new String(getterVariable.charAt(0) + "")
				.toUpperCase()
				+ getterVariable.substring(1);
		if (Arrays.binarySearch(TranslationController.SSDS_MODEL_CLASSES,
				getterClass) >= 0
				|| Arrays.binarySearch(
						TranslationController.SSDS_MODEL_CLASSES,
						myMethod.returnType) >= 0) {
			buffer.append("&fullGraph=true");
		} else if ("getRecordVariables".equals(myMethod.name)
				|| "getResources".equals(myMethod.name)) {
			buffer.append("&fullGraph=true");
		} else {
			buffer.append("&fullGraph=false");
		}
		buffer.append("\";\n");
		return buffer.toString();
	}

	private String createPrimitiveReturnMethod(MyClass myClass, Method myMethod) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(METHOD_SEPERATOR + "\n\n");
		buffer.append("=item B<" + myMethod.name + "(");
		StringBuffer paramBuffer = new StringBuffer();
		paramBuffer.append(Parameter.getStringOfParameters(myMethod.parameters,
				",", "$"));
		buffer.append(paramBuffer.toString());
		buffer.append(")>\n\n");
		buffer.append(myMethod.name + " " + paramBuffer.toString()
				+ " in the database.\n\n");
		buffer.append("=cut\n\n");
		buffer.append("sub " + myMethod.name + " {\n");
		buffer.append("\tmy $obj = shift;\n");
		for (int i = 0; i < myMethod.parameters.length; i++) {
			buffer.append("\tmy $param" + i + " = shift;\n");
		}

		buffer.append("\tmy $objRep = ");
		buffer.append("\t$obj->className();\n");

		buffer
				.append("\tmy $url = $obj->baseUrl . \"objectToInvokeOn=\" . $objRep . \"");
		buffer.append("&method=" + myMethod.name + "\";\n");
		for (int i = 0; i < myMethod.parameters.length; i++) {

			buffer.append(createParameterArgument(myMethod.parameters[i],
					"$param" + i, "$url", i + 1));
		}
		buffer.append("\n");
		buffer
				.append("\t# if the return value is a long value, assume it is the object id\n");
		buffer.append("\tmy $retVal = $obj->_returnSSDSinfo($url);\n");
		buffer.append("\treturn $retVal;\n");
		buffer.append("}\n\n");
		return buffer.toString();
	}

	// boolean return values need to have there "true" or "false" translated to
	// a zero or one.
	private String createIsMethod(MyClass myClass, Method myMethod) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(createAccessorMethodUpToReturn(myClass, myMethod));
		buffer.append("\tmy $ret = $obj->_returnSSDSinfo($url);\n");
		buffer.append("\tif ($obj->debug == $_debugLevel{'INFO'}){\n");
		buffer.append("\t\tprint \"Return from URL = $ret\\n\";\n");
		buffer.append("\t}\n");
		buffer.append("\tif ($ret =~ /true/) {\n");
		buffer.append("\t\treturn 1;\n");
		buffer.append("\t}\n");
		buffer.append("\telse {\n");
		buffer.append("\t\treturn 0;\n");
		buffer.append("\t}\n");
		buffer.append("}\n\n");
		return buffer.toString();
	}

	public String generateAccessModule(MyClass accessClass) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(createHeaderAndNewForAccess(accessClass));
		Method[] methods = accessClass.getMethods();
		for (int i = 0; i < methods.length; i++) {
			if (methods[i].name.startsWith("find")) {
				buffer.append(createFindMethod(accessClass, methods[i]));
			} else if (Arrays.binarySearch(
					TranslationController.SSDS_MODEL_CLASSES,
					methods[i].returnType) < 0) {
				buffer.append(createPrimitiveReturnMethod(accessClass,
						methods[i]));
			}
		}
		buffer.append("\n\n=back\n\n=cut\n\n");
		return buffer.toString();
	}

	private String createFindMethod(MyClass accessClass, Method myMethod) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(METHOD_SEPERATOR);
		buffer.append("\n");
		buffer.append("=item B<" + myMethod.name + "(");
		Parameter[] params = myMethod.parameters;
		StringBuffer paramBuffer = new StringBuffer();
		for (int i = 0; i < params.length; i++) {
			paramBuffer.append(params[i].type + " $" + params[i].name);
			if (i < params.length - 1) {
				paramBuffer.append(", ");
			}
		}
		buffer.append(paramBuffer.toString() + ")>\n\n");
		String modelClass = accessClass.name.substring(0, accessClass.name
				.length()
				- "access".length());
		if (myMethod.name.indexOf("ById") != -1) {
			buffer.append("return the " + modelClass + " with the ID = "
					+ paramBuffer.toString() + ".\n\n");
			buffer.append("Example:\n\n");
			buffer.append("\tmy $" + makeFirstLetterLowerCase(accessClass.name)
					+ " = new SSDS::" + accessClass.name + "();\n");
			buffer.append("\tmy $" + makeFirstLetterLowerCase(modelClass)
					+ " = $" + makeFirstLetterLowerCase(accessClass.name));
			buffer.append("->" + myMethod.name + "(5777);\n");
		} else {
			buffer.append("return a list of " + modelClass + "s ");
			if (myMethod.name.indexOf("LikeName") != -1) {
				buffer
						.append("where " + paramBuffer.toString()
								+ " matches any part of the " + modelClass
								+ " name.\n");
			} else if (myMethod.name.indexOf("ByName") != -1) {
				buffer.append("where " + paramBuffer.toString()
						+ " matches the " + modelClass + " name.\n");
			} else if (myMethod.name.indexOf("SQL") != -1) {
				buffer.append("resulting from the SQL statement in "
						+ paramBuffer.toString() + ".\n");
				buffer
						.append("Use of this method requires knowledge of the SSDS database schema.\n");
				buffer
						.append("Very selective queries can be constructed to return just the\n"
								+ modelClass + "s ");
				buffer
						.append("that satisfy a particular set of conatraints.  For example, to find "
								+ modelClass + "s \n");
				buffer
						.append("within specified time bounds.  Complicated queries that join tables may also be \n");
				buffer
						.append("executed, e.g. from within the  popEDwithDBqueries.pl Perl script:\n\n");
				buffer.append("\t$sql = \"SELECT * \\n\";\n");
				buffer
						.append("\t$sql .= \" FROM  dbo.DataProducer INNER JOIN \\n\";\n");
				buffer
						.append("\t$sql .= \"       dbo.Device ON dbo.DataProducer.DeviceID_FK = dbo.Device.id INNER JOIN \\n\"\n");
				buffer
						.append("\t$sql .= \"       dbo.DeviceType ON dbo.Device.DeviceTypeID_FK = dbo.DeviceType.id \\n\"\n");
				buffer
						.append("\t$sql .= \" WHERE (dbo.DeviceType.Name = 'AUV') AND "
								+ "((dbo.DataProducer.StartDateTime BETWEEN '$StartDtg' AND '$EndDtg') \\n\"\n");
				buffer
						.append("\t$sql .= \"       OR (dbo.DataProducer.EndDateTime BETWEEN '$StartDtg' AND '$EndDtg'))\";\n");
			}
		}
		buffer.append("\n\n=cut\n\n");
		buffer.append("sub " + myMethod.name + " {\n");
		buffer.append("\tmy $obj = shift;\n");
		String[] parameterNames = new String[myMethod.parameters.length];
		for (int i = 0; i < parameterNames.length; i++) {
			parameterNames[i] = new String("$" + myMethod.parameters[i].name
					+ "Arg");
			buffer.append("\tmy " + parameterNames[i] + " = shift;\n");
		}
		buffer.append("\tmy $url = $obj->baseUrl . \"objectToInvokeOn=");
		buffer.append("\" . $obj->className() . \"");
		buffer.append("&method=");
		buffer.append(myMethod.name);
		buffer.append("\";\n");
		if (myMethod.parameters.length > 0) {
			for (int i = 0; i < parameterNames.length; i++) {
				buffer.append(createParameterArgumentWithIdOnly(
						myMethod.parameters[i], parameterNames[i], "$url",
						i + 1));
				buffer.append("\n");
			}
		}
		if (myMethod.returnType.indexOf("Collection") != -1) {
			buffer.append("\treturn $obj->_createSSDSobjects($url);\n");
		} else {
			buffer.append("\treturn $obj->_createSSDSobject($url);\n");
		}
		buffer.append("}\n");
		return buffer.toString();
	}

}
