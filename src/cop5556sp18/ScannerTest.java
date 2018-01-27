 /**
 * JUunit tests for the Scanner for the class project in COP5556 Programming Language Principles 
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

 import cop5556sp18.Scanner.LexicalException;
 import cop5556sp18.Scanner.Token;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.ExpectedException;

 import static cop5556sp18.Scanner.Kind.*;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;

 public class ScannerTest {

     //set Junit to be able to catch exceptions
     @Rule
     public ExpectedException thrown = ExpectedException.none();


     //To make it easy to print objects and turn this output on and off
     static boolean doPrint = true;
     private void show(Object input) {
         if (doPrint) {
             System.out.println(input.toString());
         }
     }

     /**
      *Retrieves the next token and checks that it is an EOF token.
      *Also checks that this was the last token.
      *
      * @param scanner
      * @return the Token that was retrieved
      */

     Token checkNextIsEOF(Scanner scanner) {
         Token token = scanner.nextToken();
         assertEquals(Scanner.Kind.EOF, token.kind);
         assertFalse(scanner.hasTokens());
         return token;
     }

     Token checkNext(Scanner scanner, Scanner.Kind kind, int pos, int length) {
         Token t = scanner.nextToken();
         assertEquals(scanner.new Token(kind, pos, length), t);
         return t;
     }

     /**
      * Retrieves the next token and checks that its kind, position, length, line, and position in line
      * match the given parameters.
      *
      * @param scanner
      * @param kind
      * @param pos
      * @param length
      * @param line
      * @param pos_in_line
      * @return  the Token that was retrieved
      */
     Token checkNext(Scanner scanner, Scanner.Kind kind, int pos, int length, int line, int pos_in_line) {
         Token t = scanner.nextToken();
         assertEquals(kind, t.kind);
         assertEquals(pos, t.pos);
         assertEquals(length, t.length);
         assertEquals(line, t.line());
         assertEquals(pos_in_line, t.posInLine());
         return t;
     }


     /**
      * Retrieves the next token and checks that its kind and length match the given
      * parameters.  The position, line, and position in line are ignored.
      *
      * @param scanner
      * @param kind
      * @param length
      * @return  the Token that was retrieved
      */





     /**
      * Simple test case with an empty program.  The only Token will be the EOF Token.
      *
      * @throws LexicalException
      */
     @Test
     public void testEmpty() throws LexicalException {
         String input = "";  //The input is the empty string.  This is legal
         show(input);        //Display the input
         Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
         show(scanner);   //Display the Scanner
         checkNextIsEOF(scanner);  //Check that the only token is the EOF token.
     }

     /**
      * Test illustrating how to put a new line in the input program and how to
      * check content of tokens.
      *
      * Because we are using a Java String literal for input, we use \n for the
      * end of line character. (We should also be able to handle \n, \r, and \r\n
      * properly.)
      *
      * Note that if we were reading the input from a file, the end of line
      * character would be inserted by the text editor.
      * Showing the input will let you check your input is
      * what you think it is.
      *
      * @throws LexicalException
      */
     @Test
     public void testSemi() throws LexicalException {
         String input = ";;\n;;";
         Scanner scanner = new Scanner(input).scan();
         show(input);
         show(scanner);
         checkNext(scanner, SEMI, 0, 1, 1, 1);
         checkNext(scanner, SEMI, 1, 1, 1, 2);
         checkNext(scanner, SEMI, 3, 1, 2, 1);
         checkNext(scanner, SEMI, 4, 1, 2, 2);
         checkNextIsEOF(scanner);
     }



     /**
      * This example shows how to test that your scanner is behaving when the
      * input is illegal.  In this case, we are giving it an illegal character '~' in position 2
      *
      * The example shows catching the exception that is thrown by the scanner,
      * looking at it, and checking its contents before rethrowing it.  If caught
      * but not rethrown, then JUnit won't get the exception and the test will fail.
      *
      * The test will work without putting the try-catch block around
      * new Scanner(input).scan(); but then you won't be able to check
      * or display the thrown exception.
      *
      * @throws LexicalException
      */
     @Test
     public void failIllegalChar() throws LexicalException {
         String input = ";;~";
         show(input);
         thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
         try {
             new Scanner(input).scan();
         } catch (LexicalException e) {  //Catch the exception
             show(e);                    //Display it
             assertEquals(2,e.getPos()); //Check that it occurred in the expected position
             throw e;                    //Rethrow exception so JUnit will see it
         }
     }

     @Test
     public void testParens() throws LexicalException {
         String input = "()";
         Scanner scanner = new Scanner(input).scan();
         show(input);
         show(scanner);
         checkNext(scanner, LPAREN, 0, 1, 1, 1);
         checkNext(scanner, RPAREN, 1, 1, 1, 2);
         checkNextIsEOF(scanner);
     }

     @Test
     public void testComments() throws LexicalException{
         String input = "/*dbmsdb*jkafha/jafkgf*/";
         Scanner scanner = new Scanner(input).scan();
         show(input);
         show(scanner);
         checkNextIsEOF(scanner);
     }

     @Test
     public void testIdentifier() throws LexicalException{
         String input = "he t_h";
         Scanner scanner = new Scanner(input).scan();
         show(input);
         show(scanner);
         checkNext(scanner, IDENTIFIER, 0, 2, 1, 1);
         checkNext(scanner, IDENTIFIER, 3, 3, 1, 4);
         checkNextIsEOF(scanner);
     }

     @Test
     public void testDoubleOperators() throws LexicalException{
         String input = ":= << >> <= >= == != **";
         Scanner scanner = new Scanner(input).scan();
         show(input);
         show(scanner);
         //System.out.println(scanner.nextToken().toString());
         checkNext(scanner,OP_ASSIGN, 0, 2, 1, 1);
         checkNext(scanner,LPIXEL, 3, 2, 1, 4);
         checkNext(scanner,RPIXEL, 6, 2, 1, 7);
         checkNext(scanner,OP_LE, 9, 2, 1, 10);
         checkNext(scanner,OP_GE, 12, 2, 1, 13);
         checkNext(scanner,OP_EQ, 15, 2, 1, 16);
         checkNext(scanner,OP_NEQ, 18, 2, 1, 19);
         checkNext(scanner,OP_POWER, 21, 2, 1, 22);
         checkNextIsEOF(scanner);
     }

     //PATHAKS
     @Test
     public void test3() throws LexicalException {
         String input = "  abc";  //The input is the empty string.  This is legal
         show(input);        //Display the input
         Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
         checkNext(scanner, IDENTIFIER, 2, 3);
         show(scanner);   //Display the Scanner

     }
     @Test
     public void test7() throws LexicalException {
         String input = "abcwhile";  //The input is the empty string.  This is illegal
         show(input);        //Display the input



         show(input);        //Display the input
         Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
         checkNext(scanner, IDENTIFIER, 0, 8);
         show(scanner);   //Display the Scanner

     }
     @Test
     public void test11() throws LexicalException {
         String input = "green=123>=34*polar_a";  //The input is the empty string.  This is legal
         show(input);        //Display the input
         thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
         try {
             new Scanner(input).scan();
         } catch (LexicalException e) {  //
             show(e);
             assertEquals(5,e.getPos());
             throw e;
         }

     }

     @Test
     public void test32() throws LexicalException
     {
         String input = "7867b 2147483647";
         show(input);
         Scanner scanner = new Scanner(input).scan();
         show(scanner);
         checkNext(scanner,INTEGER_LITERAL,0,4,1,1);
         checkNext(scanner,IDENTIFIER,4,1,1,5);
         checkNext(scanner, INTEGER_LITERAL, 6, 10, 1, 7);
         checkNextIsEOF(scanner);
     }

     @Test
     public void test28() throws LexicalException
     {
         String input = "000.05";
         show(input);
         Scanner scanner = new Scanner(input).scan();
         show(scanner);
         checkNext(scanner,INTEGER_LITERAL,0,1);
         checkNext(scanner,INTEGER_LITERAL,1,1);
         checkNext(scanner,FLOAT_LITERAL,2,4);

         checkNextIsEOF(scanner);
     }





     @Test
     public void test6() throws LexicalException {
         String input = "M=cos/x;\n widthY**while)";  //The input is the empty string.  This is legal
         show(input);        //Display the input

         thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
         try {
             new Scanner(input).scan();
         } catch (LexicalException e) {  //
             show(e);
             assertEquals(1,e.getPos());
             throw e;
         }



     }




     @Test
     public void test29() throws LexicalException
     {
         String input = "Z=\"\b\";";
         //Scanner scanner = new Scanner(input).scan();
         show(input);


         thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
         try {
             new Scanner(input).scan();
         } catch (LexicalException e) {  //
             show(e);
             assertEquals(1,e.getPos());
             throw e;
         }
     }

     @Test
     public void test21() throws LexicalException
     {
         String input = "0.0aA6";
         //Scanner scanner = new Scanner(input).scan();
         show(input);
         Scanner scanner = new Scanner(input).scan();
         checkNext(scanner,FLOAT_LITERAL,0,3);
         checkNext(scanner,IDENTIFIER,3,3);
         //checkNext(scanner,INTEGER_LITERAL,5,1);
         show(scanner);
     }








     @Test
     public void test10() throws LexicalException {
         String input = "write==Z->zzz?";  //The input is the empty string.  This is legal
         show(input);        //Display the input
         Scanner scanner = new Scanner(input).scan();
         checkNext(scanner,KW_write,0,5);
         checkNext(scanner,OP_EQ,5,2);
         checkNext(scanner,KW_Z,7,1);
         checkNext(scanner,OP_MINUS,8,1);
         checkNext(scanner,OP_GT,9,1);

         checkNext(scanner,IDENTIFIER,10,3);
         checkNext(scanner,OP_QUESTION,13,1);

         show(scanner);
     }

     @Test
     public void test37() throws LexicalException{

         String input = "/'0'";
         show(input);
         thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
         try{
             Scanner scanner = new Scanner(input).scan();
         }
         catch (LexicalException e) {
             show(e);
             assertEquals(1,e.getPos());
             throw e;
         }
     }








     @Test
     public void test4() throws LexicalException {
         String input = "\nabc";
         show(input);
         Scanner scanner = new Scanner(input).scan();

         show(scanner);   //Display the Scanner
     }

     @Test
     public void test5() throws LexicalException {
         String input = "\r\nabc ab \rbc \r\rcosi\n\nxy\nsin_y\r\n";  //The input is the empty string.  This is legal
         show(input);        //Display the input
         Scanner scanner = new Scanner(input).scan();  //Create a Scanner and initialize it
         checkNext(scanner, IDENTIFIER, 2, 3);
         checkNext(scanner, IDENTIFIER,6,2);
         checkNext(scanner, IDENTIFIER,10,2);
         checkNext(scanner, IDENTIFIER,15,4);
         checkNext(scanner, IDENTIFIER,21,2);
         checkNext(scanner, IDENTIFIER,24,5);
         show(scanner);   //Display the Scanner

     }

     @Test
     public void test39() throws LexicalException {
         String input = "28c 938472093482\n";
         show(input);
         thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
         try {
             Scanner scanner = new Scanner(input).scan();
             show(scanner);
         } catch (LexicalException e) {
             show(e);
             assertEquals(15,e.getPos());
             throw e;
         }
     }

     @Test
     public void test38() throws LexicalException{
         String input = "falsey(abcd)01 00\r\npqrs if0!=cart_ytrue filename";
         Scanner scanner = new Scanner(input).scan();
         show(scanner);
         checkNext(scanner,IDENTIFIER,0,6);
         checkNext(scanner,LPAREN,6,1);
         checkNext(scanner, IDENTIFIER,7,4);
         checkNext(scanner,RPAREN,11,1);
         checkNext(scanner, INTEGER_LITERAL,12,1);
         checkNext(scanner, INTEGER_LITERAL,13,1);
         checkNext(scanner, INTEGER_LITERAL,15,1);
         checkNext(scanner, INTEGER_LITERAL,16,1);
         checkNext(scanner, IDENTIFIER,19,4);
         checkNext(scanner, IDENTIFIER,24,3);
         checkNext(scanner, OP_NEQ,27,2);
         checkNext(scanner, IDENTIFIER,29,10);
         checkNext(scanner,KW_filename,40,8);
         checkNextIsEOF(scanner);

     }


     @Test
     public void test56() throws LexicalException{

         String input = "/'0'";
         show(input);
         thrown.expect(LexicalException.class);  //Tell JUnit to expect a LexicalException
         try{
             Scanner scanner = new Scanner(input).scan();
         }
         catch (LexicalException e) {
             show(e);
             assertEquals(1,e.getPos());
             throw e;
         }
     }






 }


