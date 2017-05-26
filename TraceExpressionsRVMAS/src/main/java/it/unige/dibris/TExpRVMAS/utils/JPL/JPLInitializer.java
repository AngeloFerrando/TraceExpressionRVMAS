package it.unige.dibris.TExpRVMAS.utils.JPL;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import org.jpl7.Atom;
import org.jpl7.JPL;
import it.unige.dibris.TExpRVMAS.Exception.JPLInitializationException;
import org.jpl7.PrologException;
import org.jpl7.Query;
import org.jpl7.Term;
import it.unige.dibris.TExpRVMAS.Exception.PrologPredicateFailedException;

/**
 * This class handles all the communication between Java and SWI-Prolog through the use of the JPL library.
 * 
 * @author angeloferrando
 *
 */
public class JPLInitializer {
	
	/**
	 * initialize the JPL environment
	 * 
	 * throws PrologException
	 * @throws FileNotFoundException if library.pl or decamon.pl files are not found
	 */
	public static void init() throws FileNotFoundException{
		JPL.setTraditional();
		JPL.init();
		
		String pathToLibrary = "./src/main/resources/prolog-code/library.pl";
		String pathToDecAMon = "./src/main/resources/prolog-code/decamon.pl";
		
		if(!new File(pathToLibrary).exists()){ 
		    throw new FileNotFoundException("library.pl not found");
		}
		
		if(!new File(pathToDecAMon).exists()){ 
		    throw new FileNotFoundException("decamon.pl not found");
		}
		
		try{
			JPLInitializer.createAndCheck("consult", new Atom(pathToLibrary));
			JPLInitializer.createAndCheck("consult", new Atom(pathToDecAMon));
		} catch(PrologPredicateFailedException | PrologException e){
			throw new JPLInitializationException(e);
		}
	}

	/**
	 * convert a compound term to the corresponding list of terms
	 * @param term is the term that we want to convert
	 * @return the corresponding list of terms
	 */
	public static List<Term> fromCompoundToList(Term term){
		List<Term> l = new ArrayList<>();
		while(term.arity() > 1){
    		l.add(term.arg(1));
    		term = term.arg(2);
    	}
		return l;
	}
	
	/**
	 * Method used to execute a predicate represented as a String
	 * @param predicate is the string representing the predicate term to execute
	 * @return the object corresponding to the opened query (it can be used to retrieve all the information needed)
	 * 
	 * @throws PrologPredicateFailedException if the predicate fails
	 * @throws PrologException if an error occurred during the execution of the query
	 */
	public static Query createAndCheck(String predicate){
		Query query = new Query(predicate);
		if(!query.hasSolution()){
			throw new PrologPredicateFailedException(predicate + " predicate failed");
		}
		return query;
	}
	
	/**
	 * Method used to execute a predicate functor(term) 
	 * @param functor is the functor of the term
	 * @param arg is the term corresponding to the argument of the term
	 * @return the object corresponding to the opened query (it can be used to retrieve all the information needed)
	 * 
	 * @throws PrologPredicateFailedException if the predicate fails
	 * @throws PrologException if an error occurred during the execution of the query
	 */
	public static Query createAndCheck(String functor, Term arg){
		Query query = new Query(functor, arg);
		if(!query.hasSolution()){
			throw new PrologPredicateFailedException(functor + " " + arg + " predicate failed");
		}
		return query;
	}
	
	/**
	 * Method used to execute a predicate functor(term1, ..., termN) where terms = { term1, ..., termN } 
	 * @param functor is the functor of the term
	 * @param args are the terms corresponding to the arguments of the term
	 * @return the object corresponding to the opened query (it can be used to retrieve all the information needed)
	 * 
	 * @throws PrologPredicateFailedException if the predicate fails
	 * @throws PrologException if an error occurred during the execution of the query 
	 */
	public static Query createAndCheck(String functor, Term[] args){
		Query query = new Query(functor, args);
		if(!query.hasSolution()){
			throw new PrologPredicateFailedException(functor + args + " predicate failed");
		}
		return query;
	}
}
