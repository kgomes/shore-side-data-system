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
package moos.ssds.wrappergenerator.parser;
// $ANTLR 2.7.5 (20050128): "c:/workspace/SSDSBinding/src/java.tree.g" -> "JavaTreeParser.java"$

import antlr.NoViableAltException;
import antlr.RecognitionException;
import antlr.collections.AST;
import antlr.collections.impl.BitSet;


/** Java 1.3 AST Recognizer Grammar
 *
 * Author: (see java.g preamble)
 *
 * This grammar is in the PUBLIC DOMAIN
 */
public class JavaTreeParser extends antlr.TreeParser       implements JavaTreeParserTokenTypes
 {
public JavaTreeParser() {
	tokenNames = _tokenNames;
}

	public final void compilationUnit(AST _t) throws RecognitionException {
		
		AST compilationUnit_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case PACKAGE_DEF:
			{
				packageDefinition(_t);
				_t = _retTree;
				break;
			}
			case 3:
			case CLASS_DEF:
			case INTERFACE_DEF:
			case IMPORT:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			{
			_loop4:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_t.getType()==IMPORT)) {
					importDefinition(_t);
					_t = _retTree;
				}
				else {
					break _loop4;
				}
				
			} while (true);
			}
			{
			_loop6:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_t.getType()==CLASS_DEF||_t.getType()==INTERFACE_DEF)) {
					typeDefinition(_t);
					_t = _retTree;
				}
				else {
					break _loop6;
				}
				
			} while (true);
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	public final void packageDefinition(AST _t) throws RecognitionException {
		
		AST packageDefinition_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			AST __t8 = _t;
			AST tmp1_AST_in = (AST)_t;
			match(_t,PACKAGE_DEF);
			_t = _t.getFirstChild();
			identifier(_t);
			_t = _retTree;
			_t = __t8;
			_t = _t.getNextSibling();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	public final void importDefinition(AST _t) throws RecognitionException {
		
		AST importDefinition_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			AST __t10 = _t;
			AST tmp2_AST_in = (AST)_t;
			match(_t,IMPORT);
			_t = _t.getFirstChild();
			identifierStar(_t);
			_t = _retTree;
			_t = __t10;
			_t = _t.getNextSibling();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	public final void typeDefinition(AST _t) throws RecognitionException {
		
		AST typeDefinition_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case CLASS_DEF:
			{
				AST __t12 = _t;
				AST tmp3_AST_in = (AST)_t;
				match(_t,CLASS_DEF);
				_t = _t.getFirstChild();
				modifiers(_t);
				_t = _retTree;
				AST tmp4_AST_in = (AST)_t;
				match(_t,IDENT);
				_t = _t.getNextSibling();
				extendsClause(_t);
				_t = _retTree;
				implementsClause(_t);
				_t = _retTree;
				objBlock(_t);
				_t = _retTree;
				_t = __t12;
				_t = _t.getNextSibling();
				break;
			}
			case INTERFACE_DEF:
			{
				AST __t13 = _t;
				AST tmp5_AST_in = (AST)_t;
				match(_t,INTERFACE_DEF);
				_t = _t.getFirstChild();
				modifiers(_t);
				_t = _retTree;
				AST tmp6_AST_in = (AST)_t;
				match(_t,IDENT);
				_t = _t.getNextSibling();
				extendsClause(_t);
				_t = _retTree;
				interfaceBlock(_t);
				_t = _retTree;
				_t = __t13;
				_t = _t.getNextSibling();
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	public final void identifier(AST _t) throws RecognitionException {
		
		AST identifier_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case IDENT:
			{
				AST tmp7_AST_in = (AST)_t;
				match(_t,IDENT);
				_t = _t.getNextSibling();
				break;
			}
			case DOT:
			{
				AST __t75 = _t;
				AST tmp8_AST_in = (AST)_t;
				match(_t,DOT);
				_t = _t.getFirstChild();
				identifier(_t);
				_t = _retTree;
				AST tmp9_AST_in = (AST)_t;
				match(_t,IDENT);
				_t = _t.getNextSibling();
				_t = __t75;
				_t = _t.getNextSibling();
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	public final void identifierStar(AST _t) throws RecognitionException {
		
		AST identifierStar_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case IDENT:
			{
				AST tmp10_AST_in = (AST)_t;
				match(_t,IDENT);
				_t = _t.getNextSibling();
				break;
			}
			case DOT:
			{
				AST __t77 = _t;
				AST tmp11_AST_in = (AST)_t;
				match(_t,DOT);
				_t = _t.getFirstChild();
				identifier(_t);
				_t = _retTree;
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case STAR:
				{
					AST tmp12_AST_in = (AST)_t;
					match(_t,STAR);
					_t = _t.getNextSibling();
					break;
				}
				case IDENT:
				{
					AST tmp13_AST_in = (AST)_t;
					match(_t,IDENT);
					_t = _t.getNextSibling();
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				_t = __t77;
				_t = _t.getNextSibling();
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	public final void modifiers(AST _t) throws RecognitionException {
		
		AST modifiers_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			AST __t21 = _t;
			AST tmp14_AST_in = (AST)_t;
			match(_t,MODIFIERS);
			_t = _t.getFirstChild();
			{
			_loop23:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_tokenSet_0.member(_t.getType()))) {
					modifier(_t);
					_t = _retTree;
				}
				else {
					break _loop23;
				}
				
			} while (true);
			}
			_t = __t21;
			_t = _t.getNextSibling();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	public final void extendsClause(AST _t) throws RecognitionException {
		
		AST extendsClause_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			AST __t26 = _t;
			AST tmp15_AST_in = (AST)_t;
			match(_t,EXTENDS_CLAUSE);
			_t = _t.getFirstChild();
			{
			_loop28:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_t.getType()==IDENT||_t.getType()==DOT)) {
					identifier(_t);
					_t = _retTree;
				}
				else {
					break _loop28;
				}
				
			} while (true);
			}
			_t = __t26;
			_t = _t.getNextSibling();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	public final void implementsClause(AST _t) throws RecognitionException {
		
		AST implementsClause_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			AST __t30 = _t;
			AST tmp16_AST_in = (AST)_t;
			match(_t,IMPLEMENTS_CLAUSE);
			_t = _t.getFirstChild();
			{
			_loop32:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_t.getType()==IDENT||_t.getType()==DOT)) {
					identifier(_t);
					_t = _retTree;
				}
				else {
					break _loop32;
				}
				
			} while (true);
			}
			_t = __t30;
			_t = _t.getNextSibling();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	public final void objBlock(AST _t) throws RecognitionException {
		
		AST objBlock_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			AST __t38 = _t;
			AST tmp17_AST_in = (AST)_t;
			match(_t,OBJBLOCK);
			_t = _t.getFirstChild();
			{
			_loop42:
			do {
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case CTOR_DEF:
				{
					ctorDef(_t);
					_t = _retTree;
					break;
				}
				case METHOD_DEF:
				{
					methodDef(_t);
					_t = _retTree;
					break;
				}
				case VARIABLE_DEF:
				{
					variableDef(_t);
					_t = _retTree;
					break;
				}
				case CLASS_DEF:
				case INTERFACE_DEF:
				{
					typeDefinition(_t);
					_t = _retTree;
					break;
				}
				case STATIC_INIT:
				{
					AST __t40 = _t;
					AST tmp18_AST_in = (AST)_t;
					match(_t,STATIC_INIT);
					_t = _t.getFirstChild();
					slist(_t);
					_t = _retTree;
					_t = __t40;
					_t = _t.getNextSibling();
					break;
				}
				case INSTANCE_INIT:
				{
					AST __t41 = _t;
					AST tmp19_AST_in = (AST)_t;
					match(_t,INSTANCE_INIT);
					_t = _t.getFirstChild();
					slist(_t);
					_t = _retTree;
					_t = __t41;
					_t = _t.getNextSibling();
					break;
				}
				default:
				{
					break _loop42;
				}
				}
			} while (true);
			}
			_t = __t38;
			_t = _t.getNextSibling();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	public final void interfaceBlock(AST _t) throws RecognitionException {
		
		AST interfaceBlock_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			AST __t34 = _t;
			AST tmp20_AST_in = (AST)_t;
			match(_t,OBJBLOCK);
			_t = _t.getFirstChild();
			{
			_loop36:
			do {
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case METHOD_DEF:
				{
					methodDecl(_t);
					_t = _retTree;
					break;
				}
				case VARIABLE_DEF:
				{
					variableDef(_t);
					_t = _retTree;
					break;
				}
				case CLASS_DEF:
				case INTERFACE_DEF:
				{
					typeDefinition(_t);
					_t = _retTree;
					break;
				}
				default:
				{
					break _loop36;
				}
				}
			} while (true);
			}
			_t = __t34;
			_t = _t.getNextSibling();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	public final void typeSpec(AST _t) throws RecognitionException {
		
		AST typeSpec_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			AST __t15 = _t;
			AST tmp21_AST_in = (AST)_t;
			match(_t,TYPE);
			_t = _t.getFirstChild();
			typeSpecArray(_t);
			_t = _retTree;
			_t = __t15;
			_t = _t.getNextSibling();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	public final void typeSpecArray(AST _t) throws RecognitionException {
		
		AST typeSpecArray_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case ARRAY_DECLARATOR:
			{
				AST __t17 = _t;
				AST tmp22_AST_in = (AST)_t;
				match(_t,ARRAY_DECLARATOR);
				_t = _t.getFirstChild();
				typeSpecArray(_t);
				_t = _retTree;
				_t = __t17;
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_void:
			case LITERAL_boolean:
			case LITERAL_byte:
			case LITERAL_char:
			case LITERAL_short:
			case LITERAL_int:
			case LITERAL_float:
			case LITERAL_long:
			case LITERAL_double:
			case IDENT:
			case DOT:
			{
				type(_t);
				_t = _retTree;
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	public final void type(AST _t) throws RecognitionException {
		
		AST type_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case IDENT:
			case DOT:
			{
				identifier(_t);
				_t = _retTree;
				break;
			}
			case LITERAL_void:
			case LITERAL_boolean:
			case LITERAL_byte:
			case LITERAL_char:
			case LITERAL_short:
			case LITERAL_int:
			case LITERAL_float:
			case LITERAL_long:
			case LITERAL_double:
			{
				builtInType(_t);
				_t = _retTree;
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	public final void builtInType(AST _t) throws RecognitionException {
		
		AST builtInType_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case LITERAL_void:
			{
				AST tmp23_AST_in = (AST)_t;
				match(_t,LITERAL_void);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_boolean:
			{
				AST tmp24_AST_in = (AST)_t;
				match(_t,LITERAL_boolean);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_byte:
			{
				AST tmp25_AST_in = (AST)_t;
				match(_t,LITERAL_byte);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_char:
			{
				AST tmp26_AST_in = (AST)_t;
				match(_t,LITERAL_char);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_short:
			{
				AST tmp27_AST_in = (AST)_t;
				match(_t,LITERAL_short);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_int:
			{
				AST tmp28_AST_in = (AST)_t;
				match(_t,LITERAL_int);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_float:
			{
				AST tmp29_AST_in = (AST)_t;
				match(_t,LITERAL_float);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_long:
			{
				AST tmp30_AST_in = (AST)_t;
				match(_t,LITERAL_long);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_double:
			{
				AST tmp31_AST_in = (AST)_t;
				match(_t,LITERAL_double);
				_t = _t.getNextSibling();
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	public final void modifier(AST _t) throws RecognitionException {
		
		AST modifier_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case LITERAL_private:
			{
				AST tmp32_AST_in = (AST)_t;
				match(_t,LITERAL_private);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_public:
			{
				AST tmp33_AST_in = (AST)_t;
				match(_t,LITERAL_public);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_protected:
			{
				AST tmp34_AST_in = (AST)_t;
				match(_t,LITERAL_protected);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_static:
			{
				AST tmp35_AST_in = (AST)_t;
				match(_t,LITERAL_static);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_transient:
			{
				AST tmp36_AST_in = (AST)_t;
				match(_t,LITERAL_transient);
				_t = _t.getNextSibling();
				break;
			}
			case FINAL:
			{
				AST tmp37_AST_in = (AST)_t;
				match(_t,FINAL);
				_t = _t.getNextSibling();
				break;
			}
			case ABSTRACT:
			{
				AST tmp38_AST_in = (AST)_t;
				match(_t,ABSTRACT);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_native:
			{
				AST tmp39_AST_in = (AST)_t;
				match(_t,LITERAL_native);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_threadsafe:
			{
				AST tmp40_AST_in = (AST)_t;
				match(_t,LITERAL_threadsafe);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_synchronized:
			{
				AST tmp41_AST_in = (AST)_t;
				match(_t,LITERAL_synchronized);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_const:
			{
				AST tmp42_AST_in = (AST)_t;
				match(_t,LITERAL_const);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_volatile:
			{
				AST tmp43_AST_in = (AST)_t;
				match(_t,LITERAL_volatile);
				_t = _t.getNextSibling();
				break;
			}
			case STRICTFP:
			{
				AST tmp44_AST_in = (AST)_t;
				match(_t,STRICTFP);
				_t = _t.getNextSibling();
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	public final void methodDecl(AST _t) throws RecognitionException {
		
		AST methodDecl_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			AST __t47 = _t;
			AST tmp45_AST_in = (AST)_t;
			match(_t,METHOD_DEF);
			_t = _t.getFirstChild();
			modifiers(_t);
			_t = _retTree;
			typeSpec(_t);
			_t = _retTree;
			methodHead(_t);
			_t = _retTree;
			_t = __t47;
			_t = _t.getNextSibling();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	public final void variableDef(AST _t) throws RecognitionException {
		
		AST variableDef_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			AST __t52 = _t;
			AST tmp46_AST_in = (AST)_t;
			match(_t,VARIABLE_DEF);
			_t = _t.getFirstChild();
			modifiers(_t);
			_t = _retTree;
			typeSpec(_t);
			_t = _retTree;
			variableDeclarator(_t);
			_t = _retTree;
			varInitializer(_t);
			_t = _retTree;
			_t = __t52;
			_t = _t.getNextSibling();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	public final void ctorDef(AST _t) throws RecognitionException {
		
		AST ctorDef_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			AST __t44 = _t;
			AST tmp47_AST_in = (AST)_t;
			match(_t,CTOR_DEF);
			_t = _t.getFirstChild();
			modifiers(_t);
			_t = _retTree;
			methodHead(_t);
			_t = _retTree;
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case SLIST:
			{
				slist(_t);
				_t = _retTree;
				break;
			}
			case 3:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			_t = __t44;
			_t = _t.getNextSibling();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	public final void methodDef(AST _t) throws RecognitionException {
		
		AST methodDef_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			AST __t49 = _t;
			AST tmp48_AST_in = (AST)_t;
			match(_t,METHOD_DEF);
			_t = _t.getFirstChild();
			modifiers(_t);
			_t = _retTree;
			typeSpec(_t);
			_t = _retTree;
			methodHead(_t);
			_t = _retTree;
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case SLIST:
			{
				slist(_t);
				_t = _retTree;
				break;
			}
			case 3:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			_t = __t49;
			_t = _t.getNextSibling();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	public final void slist(AST _t) throws RecognitionException {
		
		AST slist_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			AST __t80 = _t;
			AST tmp49_AST_in = (AST)_t;
			match(_t,SLIST);
			_t = _t.getFirstChild();
			{
			_loop82:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_tokenSet_1.member(_t.getType()))) {
					stat(_t);
					_t = _retTree;
				}
				else {
					break _loop82;
				}
				
			} while (true);
			}
			_t = __t80;
			_t = _t.getNextSibling();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	public final void methodHead(AST _t) throws RecognitionException {
		
		AST methodHead_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			AST tmp50_AST_in = (AST)_t;
			match(_t,IDENT);
			_t = _t.getNextSibling();
			AST __t66 = _t;
			AST tmp51_AST_in = (AST)_t;
			match(_t,PARAMETERS);
			_t = _t.getFirstChild();
			{
			_loop68:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_t.getType()==PARAMETER_DEF)) {
					parameterDef(_t);
					_t = _retTree;
				}
				else {
					break _loop68;
				}
				
			} while (true);
			}
			_t = __t66;
			_t = _t.getNextSibling();
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case LITERAL_throws:
			{
				throwsClause(_t);
				_t = _retTree;
				break;
			}
			case 3:
			case SLIST:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	public final void variableDeclarator(AST _t) throws RecognitionException {
		
		AST variableDeclarator_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case IDENT:
			{
				AST tmp52_AST_in = (AST)_t;
				match(_t,IDENT);
				_t = _t.getNextSibling();
				break;
			}
			case LBRACK:
			{
				AST tmp53_AST_in = (AST)_t;
				match(_t,LBRACK);
				_t = _t.getNextSibling();
				variableDeclarator(_t);
				_t = _retTree;
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	public final void varInitializer(AST _t) throws RecognitionException {
		
		AST varInitializer_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case ASSIGN:
			{
				AST __t59 = _t;
				AST tmp54_AST_in = (AST)_t;
				match(_t,ASSIGN);
				_t = _t.getFirstChild();
				initializer(_t);
				_t = _retTree;
				_t = __t59;
				_t = _t.getNextSibling();
				break;
			}
			case 3:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	public final void parameterDef(AST _t) throws RecognitionException {
		
		AST parameterDef_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			AST __t54 = _t;
			AST tmp55_AST_in = (AST)_t;
			match(_t,PARAMETER_DEF);
			_t = _t.getFirstChild();
			modifiers(_t);
			_t = _retTree;
			typeSpec(_t);
			_t = _retTree;
			AST tmp56_AST_in = (AST)_t;
			match(_t,IDENT);
			_t = _t.getNextSibling();
			_t = __t54;
			_t = _t.getNextSibling();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	public final void objectinitializer(AST _t) throws RecognitionException {
		
		AST objectinitializer_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			AST __t56 = _t;
			AST tmp57_AST_in = (AST)_t;
			match(_t,INSTANCE_INIT);
			_t = _t.getFirstChild();
			slist(_t);
			_t = _retTree;
			_t = __t56;
			_t = _t.getNextSibling();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	public final void initializer(AST _t) throws RecognitionException {
		
		AST initializer_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case EXPR:
			{
				expression(_t);
				_t = _retTree;
				break;
			}
			case ARRAY_INIT:
			{
				arrayInitializer(_t);
				_t = _retTree;
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	public final void expression(AST _t) throws RecognitionException {
		
		AST expression_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			AST __t127 = _t;
			AST tmp58_AST_in = (AST)_t;
			match(_t,EXPR);
			_t = _t.getFirstChild();
			expr(_t);
			_t = _retTree;
			_t = __t127;
			_t = _t.getNextSibling();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	public final void arrayInitializer(AST _t) throws RecognitionException {
		
		AST arrayInitializer_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			AST __t62 = _t;
			AST tmp59_AST_in = (AST)_t;
			match(_t,ARRAY_INIT);
			_t = _t.getFirstChild();
			{
			_loop64:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_t.getType()==EXPR||_t.getType()==ARRAY_INIT)) {
					initializer(_t);
					_t = _retTree;
				}
				else {
					break _loop64;
				}
				
			} while (true);
			}
			_t = __t62;
			_t = _t.getNextSibling();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	public final void throwsClause(AST _t) throws RecognitionException {
		
		AST throwsClause_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			AST __t71 = _t;
			AST tmp60_AST_in = (AST)_t;
			match(_t,LITERAL_throws);
			_t = _t.getFirstChild();
			{
			_loop73:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_t.getType()==IDENT||_t.getType()==DOT)) {
					identifier(_t);
					_t = _retTree;
				}
				else {
					break _loop73;
				}
				
			} while (true);
			}
			_t = __t71;
			_t = _t.getNextSibling();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	public final void stat(AST _t) throws RecognitionException {
		
		AST stat_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case CLASS_DEF:
			case INTERFACE_DEF:
			{
				typeDefinition(_t);
				_t = _retTree;
				break;
			}
			case VARIABLE_DEF:
			{
				variableDef(_t);
				_t = _retTree;
				break;
			}
			case EXPR:
			{
				expression(_t);
				_t = _retTree;
				break;
			}
			case LABELED_STAT:
			{
				AST __t84 = _t;
				AST tmp61_AST_in = (AST)_t;
				match(_t,LABELED_STAT);
				_t = _t.getFirstChild();
				AST tmp62_AST_in = (AST)_t;
				match(_t,IDENT);
				_t = _t.getNextSibling();
				stat(_t);
				_t = _retTree;
				_t = __t84;
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_if:
			{
				AST __t85 = _t;
				AST tmp63_AST_in = (AST)_t;
				match(_t,LITERAL_if);
				_t = _t.getFirstChild();
				expression(_t);
				_t = _retTree;
				stat(_t);
				_t = _retTree;
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case SLIST:
				case VARIABLE_DEF:
				case CLASS_DEF:
				case INTERFACE_DEF:
				case LABELED_STAT:
				case EXPR:
				case EMPTY_STAT:
				case LITERAL_synchronized:
				case LITERAL_if:
				case LITERAL_for:
				case LITERAL_while:
				case LITERAL_do:
				case LITERAL_break:
				case LITERAL_continue:
				case LITERAL_return:
				case LITERAL_switch:
				case LITERAL_throw:
				case LITERAL_try:
				{
					stat(_t);
					_t = _retTree;
					break;
				}
				case 3:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				_t = __t85;
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_for:
			{
				AST __t87 = _t;
				AST tmp64_AST_in = (AST)_t;
				match(_t,LITERAL_for);
				_t = _t.getFirstChild();
				AST __t88 = _t;
				AST tmp65_AST_in = (AST)_t;
				match(_t,FOR_INIT);
				_t = _t.getFirstChild();
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case VARIABLE_DEF:
				{
					{
					int _cnt91=0;
					_loop91:
					do {
						if (_t==null) _t=ASTNULL;
						if ((_t.getType()==VARIABLE_DEF)) {
							variableDef(_t);
							_t = _retTree;
						}
						else {
							if ( _cnt91>=1 ) { break _loop91; } else {throw new NoViableAltException(_t);}
						}
						
						_cnt91++;
					} while (true);
					}
					break;
				}
				case ELIST:
				{
					elist(_t);
					_t = _retTree;
					break;
				}
				case 3:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				_t = __t88;
				_t = _t.getNextSibling();
				AST __t92 = _t;
				AST tmp66_AST_in = (AST)_t;
				match(_t,FOR_CONDITION);
				_t = _t.getFirstChild();
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case EXPR:
				{
					expression(_t);
					_t = _retTree;
					break;
				}
				case 3:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				_t = __t92;
				_t = _t.getNextSibling();
				AST __t94 = _t;
				AST tmp67_AST_in = (AST)_t;
				match(_t,FOR_ITERATOR);
				_t = _t.getFirstChild();
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case ELIST:
				{
					elist(_t);
					_t = _retTree;
					break;
				}
				case 3:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				_t = __t94;
				_t = _t.getNextSibling();
				stat(_t);
				_t = _retTree;
				_t = __t87;
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_while:
			{
				AST __t96 = _t;
				AST tmp68_AST_in = (AST)_t;
				match(_t,LITERAL_while);
				_t = _t.getFirstChild();
				expression(_t);
				_t = _retTree;
				stat(_t);
				_t = _retTree;
				_t = __t96;
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_do:
			{
				AST __t97 = _t;
				AST tmp69_AST_in = (AST)_t;
				match(_t,LITERAL_do);
				_t = _t.getFirstChild();
				stat(_t);
				_t = _retTree;
				expression(_t);
				_t = _retTree;
				_t = __t97;
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_break:
			{
				AST __t98 = _t;
				AST tmp70_AST_in = (AST)_t;
				match(_t,LITERAL_break);
				_t = _t.getFirstChild();
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case IDENT:
				{
					AST tmp71_AST_in = (AST)_t;
					match(_t,IDENT);
					_t = _t.getNextSibling();
					break;
				}
				case 3:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				_t = __t98;
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_continue:
			{
				AST __t100 = _t;
				AST tmp72_AST_in = (AST)_t;
				match(_t,LITERAL_continue);
				_t = _t.getFirstChild();
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case IDENT:
				{
					AST tmp73_AST_in = (AST)_t;
					match(_t,IDENT);
					_t = _t.getNextSibling();
					break;
				}
				case 3:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				_t = __t100;
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_return:
			{
				AST __t102 = _t;
				AST tmp74_AST_in = (AST)_t;
				match(_t,LITERAL_return);
				_t = _t.getFirstChild();
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case EXPR:
				{
					expression(_t);
					_t = _retTree;
					break;
				}
				case 3:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				_t = __t102;
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_switch:
			{
				AST __t104 = _t;
				AST tmp75_AST_in = (AST)_t;
				match(_t,LITERAL_switch);
				_t = _t.getFirstChild();
				expression(_t);
				_t = _retTree;
				{
				_loop106:
				do {
					if (_t==null) _t=ASTNULL;
					if ((_t.getType()==CASE_GROUP)) {
						caseGroup(_t);
						_t = _retTree;
					}
					else {
						break _loop106;
					}
					
				} while (true);
				}
				_t = __t104;
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_throw:
			{
				AST __t107 = _t;
				AST tmp76_AST_in = (AST)_t;
				match(_t,LITERAL_throw);
				_t = _t.getFirstChild();
				expression(_t);
				_t = _retTree;
				_t = __t107;
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_synchronized:
			{
				AST __t108 = _t;
				AST tmp77_AST_in = (AST)_t;
				match(_t,LITERAL_synchronized);
				_t = _t.getFirstChild();
				expression(_t);
				_t = _retTree;
				stat(_t);
				_t = _retTree;
				_t = __t108;
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_try:
			{
				tryBlock(_t);
				_t = _retTree;
				break;
			}
			case SLIST:
			{
				slist(_t);
				_t = _retTree;
				break;
			}
			case EMPTY_STAT:
			{
				AST tmp78_AST_in = (AST)_t;
				match(_t,EMPTY_STAT);
				_t = _t.getNextSibling();
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	public final void elist(AST _t) throws RecognitionException {
		
		AST elist_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			AST __t123 = _t;
			AST tmp79_AST_in = (AST)_t;
			match(_t,ELIST);
			_t = _t.getFirstChild();
			{
			_loop125:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_t.getType()==EXPR)) {
					expression(_t);
					_t = _retTree;
				}
				else {
					break _loop125;
				}
				
			} while (true);
			}
			_t = __t123;
			_t = _t.getNextSibling();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	public final void caseGroup(AST _t) throws RecognitionException {
		
		AST caseGroup_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			AST __t110 = _t;
			AST tmp80_AST_in = (AST)_t;
			match(_t,CASE_GROUP);
			_t = _t.getFirstChild();
			{
			int _cnt113=0;
			_loop113:
			do {
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case LITERAL_case:
				{
					AST __t112 = _t;
					AST tmp81_AST_in = (AST)_t;
					match(_t,LITERAL_case);
					_t = _t.getFirstChild();
					expression(_t);
					_t = _retTree;
					_t = __t112;
					_t = _t.getNextSibling();
					break;
				}
				case LITERAL_default:
				{
					AST tmp82_AST_in = (AST)_t;
					match(_t,LITERAL_default);
					_t = _t.getNextSibling();
					break;
				}
				default:
				{
					if ( _cnt113>=1 ) { break _loop113; } else {throw new NoViableAltException(_t);}
				}
				}
				_cnt113++;
			} while (true);
			}
			slist(_t);
			_t = _retTree;
			_t = __t110;
			_t = _t.getNextSibling();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	public final void tryBlock(AST _t) throws RecognitionException {
		
		AST tryBlock_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			AST __t115 = _t;
			AST tmp83_AST_in = (AST)_t;
			match(_t,LITERAL_try);
			_t = _t.getFirstChild();
			slist(_t);
			_t = _retTree;
			{
			_loop117:
			do {
				if (_t==null) _t=ASTNULL;
				if ((_t.getType()==LITERAL_catch)) {
					handler(_t);
					_t = _retTree;
				}
				else {
					break _loop117;
				}
				
			} while (true);
			}
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case LITERAL_finally:
			{
				AST __t119 = _t;
				AST tmp84_AST_in = (AST)_t;
				match(_t,LITERAL_finally);
				_t = _t.getFirstChild();
				slist(_t);
				_t = _retTree;
				_t = __t119;
				_t = _t.getNextSibling();
				break;
			}
			case 3:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			_t = __t115;
			_t = _t.getNextSibling();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	public final void handler(AST _t) throws RecognitionException {
		
		AST handler_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			AST __t121 = _t;
			AST tmp85_AST_in = (AST)_t;
			match(_t,LITERAL_catch);
			_t = _t.getFirstChild();
			parameterDef(_t);
			_t = _retTree;
			slist(_t);
			_t = _retTree;
			_t = __t121;
			_t = _t.getNextSibling();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	public final void expr(AST _t) throws RecognitionException {
		
		AST expr_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case QUESTION:
			{
				AST __t129 = _t;
				AST tmp86_AST_in = (AST)_t;
				match(_t,QUESTION);
				_t = _t.getFirstChild();
				expr(_t);
				_t = _retTree;
				expr(_t);
				_t = _retTree;
				expr(_t);
				_t = _retTree;
				_t = __t129;
				_t = _t.getNextSibling();
				break;
			}
			case ASSIGN:
			{
				AST __t130 = _t;
				AST tmp87_AST_in = (AST)_t;
				match(_t,ASSIGN);
				_t = _t.getFirstChild();
				expr(_t);
				_t = _retTree;
				expr(_t);
				_t = _retTree;
				_t = __t130;
				_t = _t.getNextSibling();
				break;
			}
			case PLUS_ASSIGN:
			{
				AST __t131 = _t;
				AST tmp88_AST_in = (AST)_t;
				match(_t,PLUS_ASSIGN);
				_t = _t.getFirstChild();
				expr(_t);
				_t = _retTree;
				expr(_t);
				_t = _retTree;
				_t = __t131;
				_t = _t.getNextSibling();
				break;
			}
			case MINUS_ASSIGN:
			{
				AST __t132 = _t;
				AST tmp89_AST_in = (AST)_t;
				match(_t,MINUS_ASSIGN);
				_t = _t.getFirstChild();
				expr(_t);
				_t = _retTree;
				expr(_t);
				_t = _retTree;
				_t = __t132;
				_t = _t.getNextSibling();
				break;
			}
			case STAR_ASSIGN:
			{
				AST __t133 = _t;
				AST tmp90_AST_in = (AST)_t;
				match(_t,STAR_ASSIGN);
				_t = _t.getFirstChild();
				expr(_t);
				_t = _retTree;
				expr(_t);
				_t = _retTree;
				_t = __t133;
				_t = _t.getNextSibling();
				break;
			}
			case DIV_ASSIGN:
			{
				AST __t134 = _t;
				AST tmp91_AST_in = (AST)_t;
				match(_t,DIV_ASSIGN);
				_t = _t.getFirstChild();
				expr(_t);
				_t = _retTree;
				expr(_t);
				_t = _retTree;
				_t = __t134;
				_t = _t.getNextSibling();
				break;
			}
			case MOD_ASSIGN:
			{
				AST __t135 = _t;
				AST tmp92_AST_in = (AST)_t;
				match(_t,MOD_ASSIGN);
				_t = _t.getFirstChild();
				expr(_t);
				_t = _retTree;
				expr(_t);
				_t = _retTree;
				_t = __t135;
				_t = _t.getNextSibling();
				break;
			}
			case SR_ASSIGN:
			{
				AST __t136 = _t;
				AST tmp93_AST_in = (AST)_t;
				match(_t,SR_ASSIGN);
				_t = _t.getFirstChild();
				expr(_t);
				_t = _retTree;
				expr(_t);
				_t = _retTree;
				_t = __t136;
				_t = _t.getNextSibling();
				break;
			}
			case BSR_ASSIGN:
			{
				AST __t137 = _t;
				AST tmp94_AST_in = (AST)_t;
				match(_t,BSR_ASSIGN);
				_t = _t.getFirstChild();
				expr(_t);
				_t = _retTree;
				expr(_t);
				_t = _retTree;
				_t = __t137;
				_t = _t.getNextSibling();
				break;
			}
			case SL_ASSIGN:
			{
				AST __t138 = _t;
				AST tmp95_AST_in = (AST)_t;
				match(_t,SL_ASSIGN);
				_t = _t.getFirstChild();
				expr(_t);
				_t = _retTree;
				expr(_t);
				_t = _retTree;
				_t = __t138;
				_t = _t.getNextSibling();
				break;
			}
			case BAND_ASSIGN:
			{
				AST __t139 = _t;
				AST tmp96_AST_in = (AST)_t;
				match(_t,BAND_ASSIGN);
				_t = _t.getFirstChild();
				expr(_t);
				_t = _retTree;
				expr(_t);
				_t = _retTree;
				_t = __t139;
				_t = _t.getNextSibling();
				break;
			}
			case BXOR_ASSIGN:
			{
				AST __t140 = _t;
				AST tmp97_AST_in = (AST)_t;
				match(_t,BXOR_ASSIGN);
				_t = _t.getFirstChild();
				expr(_t);
				_t = _retTree;
				expr(_t);
				_t = _retTree;
				_t = __t140;
				_t = _t.getNextSibling();
				break;
			}
			case BOR_ASSIGN:
			{
				AST __t141 = _t;
				AST tmp98_AST_in = (AST)_t;
				match(_t,BOR_ASSIGN);
				_t = _t.getFirstChild();
				expr(_t);
				_t = _retTree;
				expr(_t);
				_t = _retTree;
				_t = __t141;
				_t = _t.getNextSibling();
				break;
			}
			case LOR:
			{
				AST __t142 = _t;
				AST tmp99_AST_in = (AST)_t;
				match(_t,LOR);
				_t = _t.getFirstChild();
				expr(_t);
				_t = _retTree;
				expr(_t);
				_t = _retTree;
				_t = __t142;
				_t = _t.getNextSibling();
				break;
			}
			case LAND:
			{
				AST __t143 = _t;
				AST tmp100_AST_in = (AST)_t;
				match(_t,LAND);
				_t = _t.getFirstChild();
				expr(_t);
				_t = _retTree;
				expr(_t);
				_t = _retTree;
				_t = __t143;
				_t = _t.getNextSibling();
				break;
			}
			case BOR:
			{
				AST __t144 = _t;
				AST tmp101_AST_in = (AST)_t;
				match(_t,BOR);
				_t = _t.getFirstChild();
				expr(_t);
				_t = _retTree;
				expr(_t);
				_t = _retTree;
				_t = __t144;
				_t = _t.getNextSibling();
				break;
			}
			case BXOR:
			{
				AST __t145 = _t;
				AST tmp102_AST_in = (AST)_t;
				match(_t,BXOR);
				_t = _t.getFirstChild();
				expr(_t);
				_t = _retTree;
				expr(_t);
				_t = _retTree;
				_t = __t145;
				_t = _t.getNextSibling();
				break;
			}
			case BAND:
			{
				AST __t146 = _t;
				AST tmp103_AST_in = (AST)_t;
				match(_t,BAND);
				_t = _t.getFirstChild();
				expr(_t);
				_t = _retTree;
				expr(_t);
				_t = _retTree;
				_t = __t146;
				_t = _t.getNextSibling();
				break;
			}
			case NOT_EQUAL:
			{
				AST __t147 = _t;
				AST tmp104_AST_in = (AST)_t;
				match(_t,NOT_EQUAL);
				_t = _t.getFirstChild();
				expr(_t);
				_t = _retTree;
				expr(_t);
				_t = _retTree;
				_t = __t147;
				_t = _t.getNextSibling();
				break;
			}
			case EQUAL:
			{
				AST __t148 = _t;
				AST tmp105_AST_in = (AST)_t;
				match(_t,EQUAL);
				_t = _t.getFirstChild();
				expr(_t);
				_t = _retTree;
				expr(_t);
				_t = _retTree;
				_t = __t148;
				_t = _t.getNextSibling();
				break;
			}
			case LT:
			{
				AST __t149 = _t;
				AST tmp106_AST_in = (AST)_t;
				match(_t,LT);
				_t = _t.getFirstChild();
				expr(_t);
				_t = _retTree;
				expr(_t);
				_t = _retTree;
				_t = __t149;
				_t = _t.getNextSibling();
				break;
			}
			case GT:
			{
				AST __t150 = _t;
				AST tmp107_AST_in = (AST)_t;
				match(_t,GT);
				_t = _t.getFirstChild();
				expr(_t);
				_t = _retTree;
				expr(_t);
				_t = _retTree;
				_t = __t150;
				_t = _t.getNextSibling();
				break;
			}
			case LE:
			{
				AST __t151 = _t;
				AST tmp108_AST_in = (AST)_t;
				match(_t,LE);
				_t = _t.getFirstChild();
				expr(_t);
				_t = _retTree;
				expr(_t);
				_t = _retTree;
				_t = __t151;
				_t = _t.getNextSibling();
				break;
			}
			case GE:
			{
				AST __t152 = _t;
				AST tmp109_AST_in = (AST)_t;
				match(_t,GE);
				_t = _t.getFirstChild();
				expr(_t);
				_t = _retTree;
				expr(_t);
				_t = _retTree;
				_t = __t152;
				_t = _t.getNextSibling();
				break;
			}
			case SL:
			{
				AST __t153 = _t;
				AST tmp110_AST_in = (AST)_t;
				match(_t,SL);
				_t = _t.getFirstChild();
				expr(_t);
				_t = _retTree;
				expr(_t);
				_t = _retTree;
				_t = __t153;
				_t = _t.getNextSibling();
				break;
			}
			case SR:
			{
				AST __t154 = _t;
				AST tmp111_AST_in = (AST)_t;
				match(_t,SR);
				_t = _t.getFirstChild();
				expr(_t);
				_t = _retTree;
				expr(_t);
				_t = _retTree;
				_t = __t154;
				_t = _t.getNextSibling();
				break;
			}
			case BSR:
			{
				AST __t155 = _t;
				AST tmp112_AST_in = (AST)_t;
				match(_t,BSR);
				_t = _t.getFirstChild();
				expr(_t);
				_t = _retTree;
				expr(_t);
				_t = _retTree;
				_t = __t155;
				_t = _t.getNextSibling();
				break;
			}
			case PLUS:
			{
				AST __t156 = _t;
				AST tmp113_AST_in = (AST)_t;
				match(_t,PLUS);
				_t = _t.getFirstChild();
				expr(_t);
				_t = _retTree;
				expr(_t);
				_t = _retTree;
				_t = __t156;
				_t = _t.getNextSibling();
				break;
			}
			case MINUS:
			{
				AST __t157 = _t;
				AST tmp114_AST_in = (AST)_t;
				match(_t,MINUS);
				_t = _t.getFirstChild();
				expr(_t);
				_t = _retTree;
				expr(_t);
				_t = _retTree;
				_t = __t157;
				_t = _t.getNextSibling();
				break;
			}
			case DIV:
			{
				AST __t158 = _t;
				AST tmp115_AST_in = (AST)_t;
				match(_t,DIV);
				_t = _t.getFirstChild();
				expr(_t);
				_t = _retTree;
				expr(_t);
				_t = _retTree;
				_t = __t158;
				_t = _t.getNextSibling();
				break;
			}
			case MOD:
			{
				AST __t159 = _t;
				AST tmp116_AST_in = (AST)_t;
				match(_t,MOD);
				_t = _t.getFirstChild();
				expr(_t);
				_t = _retTree;
				expr(_t);
				_t = _retTree;
				_t = __t159;
				_t = _t.getNextSibling();
				break;
			}
			case STAR:
			{
				AST __t160 = _t;
				AST tmp117_AST_in = (AST)_t;
				match(_t,STAR);
				_t = _t.getFirstChild();
				expr(_t);
				_t = _retTree;
				expr(_t);
				_t = _retTree;
				_t = __t160;
				_t = _t.getNextSibling();
				break;
			}
			case INC:
			{
				AST __t161 = _t;
				AST tmp118_AST_in = (AST)_t;
				match(_t,INC);
				_t = _t.getFirstChild();
				expr(_t);
				_t = _retTree;
				_t = __t161;
				_t = _t.getNextSibling();
				break;
			}
			case DEC:
			{
				AST __t162 = _t;
				AST tmp119_AST_in = (AST)_t;
				match(_t,DEC);
				_t = _t.getFirstChild();
				expr(_t);
				_t = _retTree;
				_t = __t162;
				_t = _t.getNextSibling();
				break;
			}
			case POST_INC:
			{
				AST __t163 = _t;
				AST tmp120_AST_in = (AST)_t;
				match(_t,POST_INC);
				_t = _t.getFirstChild();
				expr(_t);
				_t = _retTree;
				_t = __t163;
				_t = _t.getNextSibling();
				break;
			}
			case POST_DEC:
			{
				AST __t164 = _t;
				AST tmp121_AST_in = (AST)_t;
				match(_t,POST_DEC);
				_t = _t.getFirstChild();
				expr(_t);
				_t = _retTree;
				_t = __t164;
				_t = _t.getNextSibling();
				break;
			}
			case BNOT:
			{
				AST __t165 = _t;
				AST tmp122_AST_in = (AST)_t;
				match(_t,BNOT);
				_t = _t.getFirstChild();
				expr(_t);
				_t = _retTree;
				_t = __t165;
				_t = _t.getNextSibling();
				break;
			}
			case LNOT:
			{
				AST __t166 = _t;
				AST tmp123_AST_in = (AST)_t;
				match(_t,LNOT);
				_t = _t.getFirstChild();
				expr(_t);
				_t = _retTree;
				_t = __t166;
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_instanceof:
			{
				AST __t167 = _t;
				AST tmp124_AST_in = (AST)_t;
				match(_t,LITERAL_instanceof);
				_t = _t.getFirstChild();
				expr(_t);
				_t = _retTree;
				expr(_t);
				_t = _retTree;
				_t = __t167;
				_t = _t.getNextSibling();
				break;
			}
			case UNARY_MINUS:
			{
				AST __t168 = _t;
				AST tmp125_AST_in = (AST)_t;
				match(_t,UNARY_MINUS);
				_t = _t.getFirstChild();
				expr(_t);
				_t = _retTree;
				_t = __t168;
				_t = _t.getNextSibling();
				break;
			}
			case UNARY_PLUS:
			{
				AST __t169 = _t;
				AST tmp126_AST_in = (AST)_t;
				match(_t,UNARY_PLUS);
				_t = _t.getFirstChild();
				expr(_t);
				_t = _retTree;
				_t = __t169;
				_t = _t.getNextSibling();
				break;
			}
			case TYPE:
			case TYPECAST:
			case INDEX_OP:
			case METHOD_CALL:
			case SUPER_CTOR_CALL:
			case CTOR_CALL:
			case IDENT:
			case DOT:
			case LITERAL_this:
			case LITERAL_super:
			case LITERAL_true:
			case LITERAL_false:
			case LITERAL_null:
			case LITERAL_new:
			case NUM_INT:
			case CHAR_LITERAL:
			case STRING_LITERAL:
			case NUM_FLOAT:
			case NUM_LONG:
			case NUM_DOUBLE:
			{
				primaryExpression(_t);
				_t = _retTree;
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	public final void primaryExpression(AST _t) throws RecognitionException {
		
		AST primaryExpression_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case IDENT:
			{
				AST tmp127_AST_in = (AST)_t;
				match(_t,IDENT);
				_t = _t.getNextSibling();
				break;
			}
			case DOT:
			{
				AST __t171 = _t;
				AST tmp128_AST_in = (AST)_t;
				match(_t,DOT);
				_t = _t.getFirstChild();
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case TYPE:
				case TYPECAST:
				case INDEX_OP:
				case POST_INC:
				case POST_DEC:
				case METHOD_CALL:
				case UNARY_MINUS:
				case UNARY_PLUS:
				case SUPER_CTOR_CALL:
				case CTOR_CALL:
				case IDENT:
				case DOT:
				case STAR:
				case LITERAL_this:
				case LITERAL_super:
				case ASSIGN:
				case PLUS_ASSIGN:
				case MINUS_ASSIGN:
				case STAR_ASSIGN:
				case DIV_ASSIGN:
				case MOD_ASSIGN:
				case SR_ASSIGN:
				case BSR_ASSIGN:
				case SL_ASSIGN:
				case BAND_ASSIGN:
				case BXOR_ASSIGN:
				case BOR_ASSIGN:
				case QUESTION:
				case LOR:
				case LAND:
				case BOR:
				case BXOR:
				case BAND:
				case NOT_EQUAL:
				case EQUAL:
				case LT:
				case GT:
				case LE:
				case GE:
				case LITERAL_instanceof:
				case SL:
				case SR:
				case BSR:
				case PLUS:
				case MINUS:
				case DIV:
				case MOD:
				case INC:
				case DEC:
				case BNOT:
				case LNOT:
				case LITERAL_true:
				case LITERAL_false:
				case LITERAL_null:
				case LITERAL_new:
				case NUM_INT:
				case CHAR_LITERAL:
				case STRING_LITERAL:
				case NUM_FLOAT:
				case NUM_LONG:
				case NUM_DOUBLE:
				{
					expr(_t);
					_t = _retTree;
					{
					if (_t==null) _t=ASTNULL;
					switch ( _t.getType()) {
					case IDENT:
					{
						AST tmp129_AST_in = (AST)_t;
						match(_t,IDENT);
						_t = _t.getNextSibling();
						break;
					}
					case INDEX_OP:
					{
						arrayIndex(_t);
						_t = _retTree;
						break;
					}
					case LITERAL_this:
					{
						AST tmp130_AST_in = (AST)_t;
						match(_t,LITERAL_this);
						_t = _t.getNextSibling();
						break;
					}
					case LITERAL_class:
					{
						AST tmp131_AST_in = (AST)_t;
						match(_t,LITERAL_class);
						_t = _t.getNextSibling();
						break;
					}
					case LITERAL_new:
					{
						AST __t174 = _t;
						AST tmp132_AST_in = (AST)_t;
						match(_t,LITERAL_new);
						_t = _t.getFirstChild();
						AST tmp133_AST_in = (AST)_t;
						match(_t,IDENT);
						_t = _t.getNextSibling();
						elist(_t);
						_t = _retTree;
						_t = __t174;
						_t = _t.getNextSibling();
						break;
					}
					case LITERAL_super:
					{
						AST tmp134_AST_in = (AST)_t;
						match(_t,LITERAL_super);
						_t = _t.getNextSibling();
						break;
					}
					default:
					{
						throw new NoViableAltException(_t);
					}
					}
					}
					break;
				}
				case ARRAY_DECLARATOR:
				{
					AST __t175 = _t;
					AST tmp135_AST_in = (AST)_t;
					match(_t,ARRAY_DECLARATOR);
					_t = _t.getFirstChild();
					typeSpecArray(_t);
					_t = _retTree;
					_t = __t175;
					_t = _t.getNextSibling();
					break;
				}
				case LITERAL_void:
				case LITERAL_boolean:
				case LITERAL_byte:
				case LITERAL_char:
				case LITERAL_short:
				case LITERAL_int:
				case LITERAL_float:
				case LITERAL_long:
				case LITERAL_double:
				{
					builtInType(_t);
					_t = _retTree;
					{
					if (_t==null) _t=ASTNULL;
					switch ( _t.getType()) {
					case LITERAL_class:
					{
						AST tmp136_AST_in = (AST)_t;
						match(_t,LITERAL_class);
						_t = _t.getNextSibling();
						break;
					}
					case 3:
					{
						break;
					}
					default:
					{
						throw new NoViableAltException(_t);
					}
					}
					}
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				_t = __t171;
				_t = _t.getNextSibling();
				break;
			}
			case INDEX_OP:
			{
				arrayIndex(_t);
				_t = _retTree;
				break;
			}
			case METHOD_CALL:
			{
				AST __t177 = _t;
				AST tmp137_AST_in = (AST)_t;
				match(_t,METHOD_CALL);
				_t = _t.getFirstChild();
				primaryExpression(_t);
				_t = _retTree;
				elist(_t);
				_t = _retTree;
				_t = __t177;
				_t = _t.getNextSibling();
				break;
			}
			case SUPER_CTOR_CALL:
			case CTOR_CALL:
			{
				ctorCall(_t);
				_t = _retTree;
				break;
			}
			case TYPECAST:
			{
				AST __t178 = _t;
				AST tmp138_AST_in = (AST)_t;
				match(_t,TYPECAST);
				_t = _t.getFirstChild();
				typeSpec(_t);
				_t = _retTree;
				expr(_t);
				_t = _retTree;
				_t = __t178;
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_new:
			{
				newExpression(_t);
				_t = _retTree;
				break;
			}
			case NUM_INT:
			case CHAR_LITERAL:
			case STRING_LITERAL:
			case NUM_FLOAT:
			case NUM_LONG:
			case NUM_DOUBLE:
			{
				constant(_t);
				_t = _retTree;
				break;
			}
			case LITERAL_super:
			{
				AST tmp139_AST_in = (AST)_t;
				match(_t,LITERAL_super);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_true:
			{
				AST tmp140_AST_in = (AST)_t;
				match(_t,LITERAL_true);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_false:
			{
				AST tmp141_AST_in = (AST)_t;
				match(_t,LITERAL_false);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_this:
			{
				AST tmp142_AST_in = (AST)_t;
				match(_t,LITERAL_this);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_null:
			{
				AST tmp143_AST_in = (AST)_t;
				match(_t,LITERAL_null);
				_t = _t.getNextSibling();
				break;
			}
			case TYPE:
			{
				typeSpec(_t);
				_t = _retTree;
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	public final void arrayIndex(AST _t) throws RecognitionException {
		
		AST arrayIndex_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			AST __t184 = _t;
			AST tmp144_AST_in = (AST)_t;
			match(_t,INDEX_OP);
			_t = _t.getFirstChild();
			expr(_t);
			_t = _retTree;
			expression(_t);
			_t = _retTree;
			_t = __t184;
			_t = _t.getNextSibling();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	public final void ctorCall(AST _t) throws RecognitionException {
		
		AST ctorCall_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case CTOR_CALL:
			{
				AST __t180 = _t;
				AST tmp145_AST_in = (AST)_t;
				match(_t,CTOR_CALL);
				_t = _t.getFirstChild();
				elist(_t);
				_t = _retTree;
				_t = __t180;
				_t = _t.getNextSibling();
				break;
			}
			case SUPER_CTOR_CALL:
			{
				AST __t181 = _t;
				AST tmp146_AST_in = (AST)_t;
				match(_t,SUPER_CTOR_CALL);
				_t = _t.getFirstChild();
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case ELIST:
				{
					elist(_t);
					_t = _retTree;
					break;
				}
				case TYPE:
				case TYPECAST:
				case INDEX_OP:
				case METHOD_CALL:
				case SUPER_CTOR_CALL:
				case CTOR_CALL:
				case IDENT:
				case DOT:
				case LITERAL_this:
				case LITERAL_super:
				case LITERAL_true:
				case LITERAL_false:
				case LITERAL_null:
				case LITERAL_new:
				case NUM_INT:
				case CHAR_LITERAL:
				case STRING_LITERAL:
				case NUM_FLOAT:
				case NUM_LONG:
				case NUM_DOUBLE:
				{
					primaryExpression(_t);
					_t = _retTree;
					elist(_t);
					_t = _retTree;
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				_t = __t181;
				_t = _t.getNextSibling();
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	public final void newExpression(AST _t) throws RecognitionException {
		
		AST newExpression_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			AST __t187 = _t;
			AST tmp147_AST_in = (AST)_t;
			match(_t,LITERAL_new);
			_t = _t.getFirstChild();
			type(_t);
			_t = _retTree;
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case ARRAY_DECLARATOR:
			{
				newArrayDeclarator(_t);
				_t = _retTree;
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case ARRAY_INIT:
				{
					arrayInitializer(_t);
					_t = _retTree;
					break;
				}
				case 3:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				break;
			}
			case ELIST:
			{
				elist(_t);
				_t = _retTree;
				{
				if (_t==null) _t=ASTNULL;
				switch ( _t.getType()) {
				case OBJBLOCK:
				{
					objBlock(_t);
					_t = _retTree;
					break;
				}
				case 3:
				{
					break;
				}
				default:
				{
					throw new NoViableAltException(_t);
				}
				}
				}
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			_t = __t187;
			_t = _t.getNextSibling();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	public final void constant(AST _t) throws RecognitionException {
		
		AST constant_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case NUM_INT:
			{
				AST tmp148_AST_in = (AST)_t;
				match(_t,NUM_INT);
				_t = _t.getNextSibling();
				break;
			}
			case CHAR_LITERAL:
			{
				AST tmp149_AST_in = (AST)_t;
				match(_t,CHAR_LITERAL);
				_t = _t.getNextSibling();
				break;
			}
			case STRING_LITERAL:
			{
				AST tmp150_AST_in = (AST)_t;
				match(_t,STRING_LITERAL);
				_t = _t.getNextSibling();
				break;
			}
			case NUM_FLOAT:
			{
				AST tmp151_AST_in = (AST)_t;
				match(_t,NUM_FLOAT);
				_t = _t.getNextSibling();
				break;
			}
			case NUM_DOUBLE:
			{
				AST tmp152_AST_in = (AST)_t;
				match(_t,NUM_DOUBLE);
				_t = _t.getNextSibling();
				break;
			}
			case NUM_LONG:
			{
				AST tmp153_AST_in = (AST)_t;
				match(_t,NUM_LONG);
				_t = _t.getNextSibling();
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	public final void newArrayDeclarator(AST _t) throws RecognitionException {
		
		AST newArrayDeclarator_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			AST __t192 = _t;
			AST tmp154_AST_in = (AST)_t;
			match(_t,ARRAY_DECLARATOR);
			_t = _t.getFirstChild();
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case ARRAY_DECLARATOR:
			{
				newArrayDeclarator(_t);
				_t = _retTree;
				break;
			}
			case 3:
			case EXPR:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			{
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case EXPR:
			{
				expression(_t);
				_t = _retTree;
				break;
			}
			case 3:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
			}
			_t = __t192;
			_t = _t.getNextSibling();
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
	}
	
	
	public static final String[] _tokenNames = {
		"<0>",
		"EOF",
		"<2>",
		"NULL_TREE_LOOKAHEAD",
		"BLOCK",
		"MODIFIERS",
		"OBJBLOCK",
		"SLIST",
		"CTOR_DEF",
		"METHOD_DEF",
		"VARIABLE_DEF",
		"INSTANCE_INIT",
		"STATIC_INIT",
		"TYPE",
		"CLASS_DEF",
		"INTERFACE_DEF",
		"PACKAGE_DEF",
		"ARRAY_DECLARATOR",
		"EXTENDS_CLAUSE",
		"IMPLEMENTS_CLAUSE",
		"PARAMETERS",
		"PARAMETER_DEF",
		"LABELED_STAT",
		"TYPECAST",
		"INDEX_OP",
		"POST_INC",
		"POST_DEC",
		"METHOD_CALL",
		"EXPR",
		"ARRAY_INIT",
		"IMPORT",
		"UNARY_MINUS",
		"UNARY_PLUS",
		"CASE_GROUP",
		"ELIST",
		"FOR_INIT",
		"FOR_CONDITION",
		"FOR_ITERATOR",
		"EMPTY_STAT",
		"\"final\"",
		"\"abstract\"",
		"\"strictfp\"",
		"SUPER_CTOR_CALL",
		"CTOR_CALL",
		"\"package\"",
		"SEMI",
		"\"import\"",
		"LBRACK",
		"RBRACK",
		"\"void\"",
		"\"boolean\"",
		"\"byte\"",
		"\"char\"",
		"\"short\"",
		"\"int\"",
		"\"float\"",
		"\"long\"",
		"\"double\"",
		"IDENT",
		"DOT",
		"STAR",
		"\"private\"",
		"\"public\"",
		"\"protected\"",
		"\"static\"",
		"\"transient\"",
		"\"native\"",
		"\"threadsafe\"",
		"\"synchronized\"",
		"\"volatile\"",
		"\"class\"",
		"\"extends\"",
		"\"interface\"",
		"LCURLY",
		"RCURLY",
		"COMMA",
		"\"implements\"",
		"LPAREN",
		"RPAREN",
		"\"this\"",
		"\"super\"",
		"ASSIGN",
		"\"throws\"",
		"COLON",
		"\"if\"",
		"\"else\"",
		"\"for\"",
		"\"while\"",
		"\"do\"",
		"\"break\"",
		"\"continue\"",
		"\"return\"",
		"\"switch\"",
		"\"throw\"",
		"\"case\"",
		"\"default\"",
		"\"try\"",
		"\"finally\"",
		"\"catch\"",
		"PLUS_ASSIGN",
		"MINUS_ASSIGN",
		"STAR_ASSIGN",
		"DIV_ASSIGN",
		"MOD_ASSIGN",
		"SR_ASSIGN",
		"BSR_ASSIGN",
		"SL_ASSIGN",
		"BAND_ASSIGN",
		"BXOR_ASSIGN",
		"BOR_ASSIGN",
		"QUESTION",
		"LOR",
		"LAND",
		"BOR",
		"BXOR",
		"BAND",
		"NOT_EQUAL",
		"EQUAL",
		"LT",
		"GT",
		"LE",
		"GE",
		"\"instanceof\"",
		"SL",
		"SR",
		"BSR",
		"PLUS",
		"MINUS",
		"DIV",
		"MOD",
		"INC",
		"DEC",
		"BNOT",
		"LNOT",
		"\"true\"",
		"\"false\"",
		"\"null\"",
		"\"new\"",
		"NUM_INT",
		"CHAR_LITERAL",
		"STRING_LITERAL",
		"NUM_FLOAT",
		"NUM_LONG",
		"NUM_DOUBLE",
		"WS",
		"SL_COMMENT",
		"ML_COMMENT",
		"ESC",
		"HEX_DIGIT",
		"VOCAB",
		"EXPONENT",
		"FLOAT_SUFFIX",
		"\"const\""
	};
	
	private static final long[] mk_tokenSet_0() {
		long[] data = { -2305839160922996736L, 63L, 16777216L, 0L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
	private static final long[] mk_tokenSet_1() {
		long[] data = { 275150587008L, 5365563408L, 0L, 0L};
		return data;
	}
	public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
	}
	
