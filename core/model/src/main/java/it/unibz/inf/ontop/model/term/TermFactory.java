package it.unibz.inf.ontop.model.term;

/*
 * #%L
 * ontop-obdalib-core
 * %%
 * Copyright (C) 2009 - 2014 Free University of Bozen-Bolzano
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import com.google.common.collect.ImmutableList;
import it.unibz.inf.ontop.model.term.functionsymbol.*;
import it.unibz.inf.ontop.model.type.RDFDatatype;
import it.unibz.inf.ontop.model.type.RDFTermType;
import org.apache.commons.rdf.api.IRI;

import java.util.List;

public interface TermFactory {

	/*
	 * Built-in function predicates
	 */
	
	/**
	 * Construct a {@link Function} object. A function expression consists of
	 * functional symbol (or functor) and one or more arguments.
	 * 
	 * @param functor
	 *            the function symbol name.
	 * @param terms
	 *            a list of arguments.
	 * @return the function object.
	 */
	public Function getFunction(Predicate functor, Term... terms);

	Expression getExpression(BooleanFunctionSymbol functor, List<Term> arguments);

	ImmutableExpression getImmutableExpression(BooleanFunctionSymbol functor, ImmutableTerm... arguments);

	ImmutableExpression getImmutableExpression(BooleanFunctionSymbol functor,
											   ImmutableList<? extends ImmutableTerm> arguments);

	ImmutableExpression getImmutableExpression(Expression expression);

	public Function getFunction(Predicate functor, List<Term> terms);

	public ImmutableFunctionalTerm getImmutableFunctionalTerm(FunctionSymbol functor, ImmutableList<? extends ImmutableTerm> terms);

	public ImmutableFunctionalTerm getImmutableFunctionalTerm(FunctionSymbol functor, ImmutableTerm... terms);

	public NonGroundFunctionalTerm getNonGroundFunctionalTerm(FunctionSymbol functor, ImmutableTerm... terms);

	public NonGroundFunctionalTerm getNonGroundFunctionalTerm(FunctionSymbol functor, ImmutableList<ImmutableTerm> terms);


	public Expression getExpression(BooleanFunctionSymbol functor, Term... arguments);

	/*
	 * Boolean function terms
	 */

	public Expression getFunctionEQ(Term firstTerm, Term secondTerm);

	public Expression getFunctionGTE(Term firstTerm, Term secondTerm);

	public Expression getFunctionGT(Term firstTerm, Term secondTerm);

	public Expression getFunctionLTE(Term firstTerm, Term secondTerm);

	public Expression getFunctionLT(Term firstTerm, Term secondTerm);

	public Expression getFunctionNEQ(Term firstTerm, Term secondTerm);

	public Expression getFunctionNOT(Term term);

	public Expression getFunctionAND(Term term1, Term term2);

	public Expression getFunctionOR(Term term1, Term term2);

	public Expression getFunctionIsTrue(Term term);

	public Expression getFunctionIsNull(Term term);

	public Expression getFunctionIsNotNull(Term term);

	public Expression getLANGMATCHESFunction(Term term1, Term term2);
	
	// ROMAN (23 Dec 2015): LIKE comes only from mappings
	public Expression getSQLFunctionLike(Term term1, Term term2);


	/*
	 * Casting values cast(source-value AS destination-type)
	 */
	public Function getFunctionCast(Term term1, Term term2);

	/**
	 * Construct a {@link IRIConstant} object. This type of term is written as a
	 * usual URI construction following the generic URI syntax specification
	 * (RFC 3986).
	 * <p>
	 * <code>
	 * scheme://host:port/path#fragment
	 * </code>
	 * <p>
	 * Examples:
	 * <p>
	 * <code>
	 * http://example.org/some/paths <br />
	 * http://example.org/some/paths/to/resource#frag01 <br />
	 * ftp://example.org/resource.txt <br />
	 * </code>
	 * <p>
	 * are all well-formed URI strings.
	 * 
	 * @param iri
	 *            the URI.
	 * @return a URI constant.
	 */
	public IRIConstant getConstantIRI(IRI iri);
	
	public BNode getConstantBNode(String name);

	public ValueConstant getBooleanConstant(boolean value);

	ValueConstant getNullConstant();

	/**
	 * TODO: explain
	 */
	ValueConstant getProvenanceSpecialConstant();
	
	/**
	 * Construct a {@link ValueConstant} object.
	 * 
	 * @param value
	 *            the value of the constant.
	 * @return the value constant.
	 */
	public ValueConstant getConstantLiteral(String value);

	/**
	 * Construct a {@link ValueConstant} object with a type definition.
	 * <p>
	 * Example:
	 * <p>
	 * <code>
	 * "Person"^^xsd:String <br />
	 * 22^^xsd:Integer
	 * </code>
	 * 
	 * @param value
	 *            the value of the constant.
	 * @param type
	 *            the type of the constant.
	 * @return the value constant.
	 */
	ValueConstant getConstantLiteral(String value, RDFDatatype type);

	ValueConstant getConstantLiteral(String value, IRI type);


	/**
	 * Construct a {@link ValueConstant} object with a language tag.
	 * <p>
	 * Example:
	 * <p>
	 * <code>
	 * "This is American English"@en-US <br />
	 * </code>
	 * 
	 * @param value
	 *            the value of the constant.
	 * @param language
	 *            the language tag for the constant.
	 * @return the value constant.
	 */
	public ValueConstant getConstantLiteral(String value, String language);

	Function getRDFLiteralMutableFunctionalTerm(Term lexicalTerm, String language);
	Function getRDFLiteralMutableFunctionalTerm(Term lexicalTerm, RDFDatatype type);
	Function getRDFLiteralMutableFunctionalTerm(Term lexicalTerm, IRI datatype);

	ImmutableFunctionalTerm getRDFLiteralFunctionalTerm(ImmutableTerm lexicalTerm, String language);
	ImmutableFunctionalTerm getRDFLiteralFunctionalTerm(ImmutableTerm lexicalTerm, RDFDatatype type);
	ImmutableFunctionalTerm getRDFLiteralFunctionalTerm(ImmutableTerm lexicalTerm, IRI datatypeIRI);

	/**
	 * Construct a {@link Variable} object. The variable name is started by a
	 * dollar sign ('$') or a question mark sign ('?'), e.g.:
	 * <p>
	 * <code>
	 * pred($x) <br />
	 * func(?x, ?y)
	 * </code>
	 * 
	 * @param name
	 *            the name of the variable.
	 * @return the variable object.
	 */
	public Variable getVariable(String name);

	RDFTermTypeConstant getRDFTermTypeConstant(RDFTermType type);

	ImmutableFunctionalTerm getRDFFunctionalTerm(ImmutableTerm lexicalTerm, ImmutableTerm typeTerm);

	/**
	 * TODO: use a more precise type for the argument
	 */
	GroundFunctionalTerm getIRIFunctionalTerm(IRI iri);

	ImmutableFunctionalTerm getIRIFunctionalTerm(Variable variable);

	/**
	 * At least one argument for the IRI functional term with an IRI template is required
	 */
	ImmutableFunctionalTerm getIRIFunctionalTerm(String iriTemplate, ImmutableList<? extends ImmutableTerm> arguments);

	/**
	 * When IRIs are encoded into numbers using a dictionary
	 */
	ImmutableFunctionalTerm getRDFFunctionalTerm(int encodedIRI);

	/**
	 * When fact IRIs are decomposed (so as to be included in the mapping)
	 */
	ImmutableFunctionalTerm getIRIFunctionalTerm(IRIStringTemplateFunctionSymbol templateSymbol,
												 ImmutableList<ValueConstant> arguments);

	/**
	 * Temporary
	 */
	Function getIRIMutableFunctionalTerm(String iriTemplate, Term... arguments);
	Function getIRIMutableFunctionalTerm(IRI iri);

	/**
	 * NB: a fresh Bnode template is created
	 */
	ImmutableFunctionalTerm getFreshBnodeFunctionalTerm(Variable variable);
	ImmutableFunctionalTerm getBnodeFunctionalTerm(String bnodeTemplate,
												   ImmutableList<? extends ImmutableTerm> arguments);

	/**
	 * NB: a fresh Bnode template is created
	 */
	ImmutableFunctionalTerm getFreshBnodeFunctionalTerm(ImmutableList<ImmutableTerm> terms);

	/**
	 * Used when building (a fragment of) the lexical part of an RDF term
	 * in a PRE-PROCESSED mapping assertion.
	 *
	 * This functional term must not appear in the final mapping
	 */
	ImmutableFunctionalTerm getPartiallyDefinedToStringCast(Variable variable);
}
