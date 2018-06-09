package it.unibz.inf.ontop.model.term;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unibz.inf.ontop.exception.FatalTypingException;
import it.unibz.inf.ontop.model.term.functionsymbol.FunctionSymbol;
import it.unibz.inf.ontop.model.type.TypeInference;

/**
 * Functional term that is declared as immutable.
 *
 */
public interface ImmutableFunctionalTerm extends NonVariableTerm, NonConstantTerm {

    ImmutableList<? extends ImmutableTerm> getTerms();

    ImmutableTerm getTerm(int index);

    FunctionSymbol getFunctionSymbol();

    int getArity();

    ImmutableSet<Variable> getVariables();

    default TypeInference inferType() throws FatalTypingException {
        FunctionSymbol functionSymbol = getFunctionSymbol();
        return functionSymbol.inferType(getTerms());
    }

    ImmutableTerm evaluate() throws FatalTypingException;
}
