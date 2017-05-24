package it.unige.dibris.TExpRVMAS.utils.JPL;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jpl7.Atom;
import org.jpl7.Compound;
import org.jpl7.JPL;
import org.jpl7.Query;
import org.jpl7.Term;

import it.unige.dibris.TExpRVMAS.Exception.PrologException;

public class JPLInitializer {
	
	public static void init(){
		JPL.setTraditional();
		JPL.init();
		
		String pathToLibrary = "./src/main/resources/prolog-code/library.pl";
		String pathToDecAMon = "./src/main/resources/prolog-code/decamon.pl";
		
		JPLInitializer.createAndCheck("consult", new Atom(pathToLibrary));
		JPLInitializer.createAndCheck("consult", new Atom(pathToDecAMon));
	}
	
//	public static void loadTraceExpression(String pathToTraceExpression){
//		JPLInitializer.createAndCheck("consult", new Atom(pathToTraceExpression));
//	}
	
	public static List<Term> fromCompoundToList(Term term){
		List<Term> l = new ArrayList<>();
		while(term.arity() > 1){
    		l.add(term.arg(1));
    		term = term.arg(2);
    	}
		return l;
	}
	
	/* Method used to execute a predicate represented as a String */
	public static Query createAndCheck(String predicate){
		Query query = new Query(predicate);
		if(!query.hasSolution()){
			throw new PrologException(predicate + " predicate failed");
		}
		return query;
	}
	
	/* Method used to execute a predicate functor(term) */
	public static Query createAndCheck(String functor, Term term){
		Query query = new Query(functor, term);
		if(!query.hasSolution()){
			throw new PrologException(functor + " " + term + " predicate failed");
		}
		return query;
	}
	
	/* Method used to execute a predicate functor(term1, ..., termN) where terms = { term1, ..., termN } */
	public static Query createAndCheck(String functor, Term[] terms){
		Query query = new Query(functor, terms);
		if(!query.hasSolution()){
			throw new PrologException(functor + " predicate failed");
		}
		return query;
	}
}
