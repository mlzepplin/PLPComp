 /**
 * JUunit tests for the Parser for the class project in COP5556 Programming Language Principles 
 * at the University of Florida, Spring 2018.
 * 
 * This software is solely for the educational benefit of students 
 * enrolled in the course during the Spring 2018 semester.  
 * 
 * This software, and any software derived from it,  may not be shared with others or posted to public web sites,
 * either during the course or afterwards.
 * 
 *  @Beverly A. Sanders, 2018
 */

package cop5556sp18;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import cop5556sp18.SimpleParser;
import cop5556sp18.Scanner;
import cop5556sp18.SimpleParser.SyntaxException;
import cop5556sp18.Scanner.LexicalException;

public class SimpleParserTest {

	//set Junit to be able to catch exceptions
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	
	//To make it easy to print objects and turn this output on and off
	static final boolean doPrint = true;
	private void show(Object input) {
		if (doPrint) {
			System.out.println(input.toString());
		}
	}


	//creates and returns a parser for the given input.
	private SimpleParser makeParser(String input) throws LexicalException {
		show(input);        //Display the input 
		Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
		show(scanner);   //Display the Scanner
		SimpleParser parser = new SimpleParser(scanner);
		return parser;
	}
	
	

	/**
	 * Simple test case with an empty program.  This throws an exception 
	 * because it lacks an identifier and a block. The test case passes because
	 * it expects an exception
	 *  
	 * @throws LexicalException
	 * @throws SyntaxException 
	 */
	@Test
	public void testEmpty() throws LexicalException, SyntaxException {
		String input = "";  //The input is the empty string.  
		SimpleParser parser = makeParser(input);
		thrown.expect(SyntaxException.class);
		parser.parse();
	}
	
	/**
	 * Smallest legal program.
	 *   
	 * @throws LexicalException
	 * @throws SyntaxException 
	 */
	@Test
	public void testSmallest() throws LexicalException, SyntaxException {
		String input = "b{}";  
		SimpleParser parser = makeParser(input);
		parser.parse();
	}	
	
	
	//This test should pass in your complete parser.  It will fail in the starter code.
	//Of course, you would want a better error message. 
	@Test
	public void testDec0() throws LexicalException, SyntaxException {
		String input = "b{int c;}";
		SimpleParser parser = makeParser(input);
		parser.parse();
	}



}
	

